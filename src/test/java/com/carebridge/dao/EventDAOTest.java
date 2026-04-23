package com.carebridge.dao;

import com.carebridge.CareBridgeApplication;
import com.carebridge.dao.impl.EventDAO;
import com.carebridge.dao.impl.EventTypeDAO;
import com.carebridge.dao.impl.UserDAO;
import com.carebridge.entities.Event;
import com.carebridge.entities.EventType;
import com.carebridge.entities.User;
import com.carebridge.enums.Role;
import com.carebridge.exceptions.ApiRuntimeException;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = CareBridgeApplication.class)
@ActiveProfiles("test")
@Transactional
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class EventDAOTest {

    @Autowired
    private EventDAO eventDAO;
    @Autowired
    private UserDAO userDAO;
    @Autowired
    private EventTypeDAO eventTypeDAO;

    private User testUser;
    private EventType testType;
    private Long eventId;

    @BeforeEach
    void setUp() {
        testUser = new User("Event Creator", "creator" + System.nanoTime() + "@example.com", "pass", Role.ADMIN);
        userDAO.create(testUser);

        testType = new EventType("DAO Event Type " + System.nanoTime(), "#123456");
        eventTypeDAO.create(testType);

        Event event = new Event();
        event.setTitle("Setup Event");
        event.setStartAt(Instant.now().plus(1, ChronoUnit.HOURS));
        event.setCreatedBy(testUser);
        event.setEventType(testType);
        Event created = eventDAO.create(event);
        eventId = created.getId();
    }

    @Test
    @Order(1)
    void testCreateBranches() {
        // 1. e is null
        assertThrows(ApiRuntimeException.class, () -> eventDAO.create(null));

        // 2. title is null
        Event e1 = new Event();
        e1.setTitle(null);
        assertThrows(ApiRuntimeException.class, () -> eventDAO.create(e1));

        // 3. title is blank
        e1.setTitle("  ");
        assertThrows(ApiRuntimeException.class, () -> eventDAO.create(e1));

        // 4. startAt is null
        e1.setTitle("Valid Title");
        e1.setStartAt(null);
        assertThrows(ApiRuntimeException.class, () -> eventDAO.create(e1));

        // 5. startAt is in the past
        e1.setStartAt(Instant.now().minus(1, ChronoUnit.HOURS));
        assertThrows(ApiRuntimeException.class, () -> eventDAO.create(e1));

        // 6. createdBy is null
        e1.setStartAt(Instant.now().plus(1, ChronoUnit.HOURS));
        e1.setCreatedBy(null);
        assertThrows(ApiRuntimeException.class, () -> eventDAO.create(e1));

        // 7. eventType is null
        e1.setCreatedBy(testUser);
        e1.setEventType(null);
        assertThrows(ApiRuntimeException.class, () -> eventDAO.create(e1));
    }

    @Test
    @Order(2)
    void testUpdateBranches() {
        // Case: existing null (already covered by testOtherBranches but adding here for clarity)
        assertThrows(ApiRuntimeException.class, () -> eventDAO.update(999999L, new Event()));

        // Case 1: All partial update fields null/blank
        Event p1 = new Event();
        p1.setTitle(null);
        p1.setDescription(null);
        p1.setStartAt(null);
        p1.setEventType(null);
        p1.setCreatedBy(null);
        p1.setSeenByUsers(null);
        
        Event u1 = eventDAO.update(eventId, p1);
        assertEquals("Setup Event", u1.getTitle());

        // Case 2: Title blank, seenByUsers empty
        Event p2 = new Event();
        p2.setTitle(" ");
        p2.setSeenByUsers(new HashSet<>());
        Event u2 = eventDAO.update(eventId, p2);
        assertEquals("Setup Event", u2.getTitle());
        assertTrue(u2.getSeenByUsers().isEmpty());

        // Case 3: All valid
        Event p3 = new Event();
        p3.setTitle("NewT");
        p3.setDescription("NewD");
        p3.setStartAt(Instant.now().plus(5, ChronoUnit.HOURS));
        p3.setEventType(testType);
        p3.setCreatedBy(testUser);
        p3.setSeenByUsers(Set.of(testUser));
        
        Event u3 = eventDAO.update(eventId, p3);
        assertEquals("NewT", u3.getTitle());
        assertEquals("NewD", u3.getDescription());
        assertFalse(u3.getSeenByUsers().isEmpty());
    }

    @Test
    @Order(3)
    void testReadAndQueryBranches() {
        assertNotNull(eventDAO.read(eventId));
        assertNull(eventDAO.read(999999L));
        
        assertFalse(eventDAO.readAll().isEmpty());
        assertFalse(eventDAO.readByCreator(testUser.getId()).isEmpty());
        assertThrows(ApiRuntimeException.class, () -> eventDAO.readByCreator(null));
        
        assertFalse(eventDAO.readBetween(Instant.now().minusSeconds(10), Instant.now().plusSeconds(7200)).isEmpty());
    }

    @Test
    @Order(4)
    void testMarkSeenBranches() {
        assertThrows(ApiRuntimeException.class, () -> eventDAO.addSeenByUser(999999L, testUser));
        assertThrows(ApiRuntimeException.class, () -> eventDAO.removeSeenByUser(999999L, testUser));
        
        eventDAO.addSeenByUser(eventId, testUser);
        eventDAO.removeSeenByUser(eventId, testUser);
    }

    @Test
    @Order(5)
    void testDelete() {
        assertThrows(ApiRuntimeException.class, () -> eventDAO.delete(999999L));
        eventDAO.delete(eventId);
        assertNull(eventDAO.read(eventId));
    }
}
