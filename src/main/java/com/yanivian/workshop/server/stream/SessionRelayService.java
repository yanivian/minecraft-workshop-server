package com.yanivian.workshop.server.stream;

import io.javalin.websocket.WsConfig;
import io.javalin.websocket.WsContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Manages real-time streaming sessions pairing a Minecraft server (broadcaster)
 * with one or more WorldXR tabletop AR clients (viewers).
 */
public class SessionRelayService {

    private static final Logger logger = LoggerFactory.getLogger(SessionRelayService.class);

    public static class Session {
        private final String id;
        private WsContext broadcaster;
        private final Set<WsContext> viewers = ConcurrentHashMap.newKeySet();
        private String lastBaselineJson;

        public Session(String id) {
            this.id = id;
        }

        public String id() { return id; }
        public WsContext broadcaster() { return broadcaster; }
        public Set<WsContext> viewers() { return viewers; }
        public String lastBaselineJson() { return lastBaselineJson; }
        public void setLastBaselineJson(String json) { this.lastBaselineJson = json; }
    }

    private final Map<String, Session> activeSessions = new ConcurrentHashMap<>();

    /**
     * Generates a unique 4-digit session code for easy typing in VR/AR.
     */
    public String createSession() {
        for (int attempts = 0; attempts < 100; attempts++) {
            String code = String.valueOf(ThreadLocalRandom.current().nextInt(1000, 10000));
            if (!activeSessions.containsKey(code)) {
                activeSessions.put(code, new Session(code));
                logger.info("Created new streaming session: {}", code);
                return code;
            }
        }
        String fallback = String.valueOf(System.currentTimeMillis() % 10000);
        activeSessions.put(fallback, new Session(fallback));
        return fallback;
    }

    public Session getSession(String sessionId) {
        return activeSessions.get(sessionId);
    }

    public boolean hasSession(String sessionId) {
        return activeSessions.containsKey(sessionId);
    }

    public void configureWebSocket(WsConfig ws) {
        ws.onConnect(ctx -> {
            String sessionId = ctx.pathParam("sessionId");
            String role = ctx.queryParam("role");
            if (role == null) role = "viewer";

            Session session = activeSessions.computeIfAbsent(sessionId, Session::new);

            if ("broadcaster".equalsIgnoreCase(role)) {
                session.broadcaster = ctx;
                ctx.attribute("role", "broadcaster");
                ctx.attribute("sessionId", sessionId);
                logger.info("[WS Stream] Broadcaster (Minecraft Server) connected to session {}", sessionId);
            } else {
                session.viewers.add(ctx);
                ctx.attribute("role", "viewer");
                ctx.attribute("sessionId", sessionId);
                logger.info("[WS Stream] Viewer (XR Phone) connected to session {} (total viewers: {})",
                    sessionId, session.viewers.size());

                // If a baseline is already available, send it to the new viewer immediately
                if (session.lastBaselineJson != null) {
                    ctx.send(session.lastBaselineJson);
                }
            }
        });

        ws.onMessage(ctx -> {
            String sessionId = ctx.attribute("sessionId");
            String role = ctx.attribute("role");
            Session session = activeSessions.get(sessionId);
            if (session == null) return;

            String message = ctx.message();

            if ("broadcaster".equalsIgnoreCase(role)) {
                // If it's a baseline snapshot, cache it for late-joining viewers
                if (message.contains("\"type\":\"baseline\"") || message.contains("\"type\": \"baseline\"")) {
                    session.setLastBaselineJson(message);
                }

                // Broadcast message to all connected XR viewers
                for (WsContext viewer : session.viewers) {
                    if (viewer.session.isOpen()) {
                        viewer.send(message);
                    }
                }
            } else {
                // Message from XR viewer (e.g. request baseline or ping) -> forward to broadcaster
                if (session.broadcaster != null && session.broadcaster.session.isOpen()) {
                    session.broadcaster.send(message);
                }
            }
        });

        ws.onBinaryMessage(ctx -> {
            String sessionId = ctx.attribute("sessionId");
            String role = ctx.attribute("role");
            Session session = activeSessions.get(sessionId);
            if (session == null) return;

            if ("broadcaster".equalsIgnoreCase(role)) {
                byte[] data = ctx.data();
                for (WsContext viewer : session.viewers) {
                    if (viewer.session.isOpen()) {
                        viewer.send(data);
                    }
                }
            }
        });

        ws.onClose(ctx -> {
            String sessionId = ctx.attribute("sessionId");
            String role = ctx.attribute("role");
            Session session = activeSessions.get(sessionId);
            if (session == null) return;

            if ("broadcaster".equalsIgnoreCase(role)) {
                logger.info("[WS Stream] Broadcaster disconnected from session {}", sessionId);
                session.broadcaster = null;
            } else {
                session.viewers.remove(ctx);
                logger.info("[WS Stream] Viewer disconnected from session {} (remaining: {})",
                    sessionId, session.viewers.size());
            }

            if (session.broadcaster == null && session.viewers.isEmpty()) {
                activeSessions.remove(sessionId);
                logger.info("[WS Stream] Session {} expired and removed", sessionId);
            }
        });

        ws.onError(ctx -> {
            logger.warn("[WS Stream] Error on session {}: {}",
                ctx.attribute("sessionId"), ctx.error() != null ? ctx.error().getMessage() : "unknown");
        });
    }
}
