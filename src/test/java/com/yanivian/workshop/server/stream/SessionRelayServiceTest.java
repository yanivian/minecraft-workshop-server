package com.yanivian.workshop.server.stream;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class SessionRelayServiceTest {

    @Test
    public void testCreateSessionGeneratesValidCode() {
        SessionRelayService service = new SessionRelayService();
        String session1 = service.createSession();
        assertNotNull(session1);
        assertEquals(4, session1.length());
        assertTrue(service.hasSession(session1));

        String session2 = service.createSession();
        assertNotNull(session2);
        assertNotEquals(session1, session2);
        assertTrue(service.hasSession(session2));
    }

    @Test
    public void testGetSessionReturnsCorrectSession() {
        SessionRelayService service = new SessionRelayService();
        String code = service.createSession();
        SessionRelayService.Session session = service.getSession(code);
        assertNotNull(session);
        assertEquals(code, session.id());
        assertNull(session.broadcaster());
        assertTrue(session.viewers().isEmpty());
    }
}
