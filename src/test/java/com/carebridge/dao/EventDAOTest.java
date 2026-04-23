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
import java.util.List;
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
    void testRead() {
        assertNotNull(eventDAO.read(eventId));
        assertNull(eventDAO.read(999999L));
    }

    @Test
    @Order(2)
    void testReadAll() {
        assertFalse(eventDAO.readAll().isEmpty());
    }

    @Test
    @Order(3)
    void testReadByCreator() {
        assertFalse(eventDAO.readByCreator(testUser.getId()).isEmpty());
        assertThrows(ApiRuntimeException.class, () -> eventDAO.readByCreator(null));
    }

    @Test
    @Order(4)
    void testUpdate() {
        Event patch = new Event();
        patch.setTitle("New Title");
        patch.setDescription("New Desc");
        patch.setStartAt(Instant.now().plus(2, ChronoUnit.HOURS));
        patch.setShowOnBoard(true);
        patch.setEventType(testType);
        patch.setCreatedBy(testUser);
        patch.setSeenByUsers(Set.of(testUser));
        
        Event updated = eventDAO.update(eventId, patch);
        assertEquals("New Title", updated.getTitle());
        assertFalse(updated.getSeenByUsers().isEmpty());

        // Partial update branches
        Event patch2 = new Event();
        patch2.setTitle(""); // Blank title check
        eventDAO.update(eventId, patch2);
        
        Event patch3 = new Event();
        patch3.setEventType(null);
        eventDAO.update(eventId, patch3);
    }

    @Test
    @Order(5)
    void testAddRemoveSeenBy() {
        eventDAO.addSeenByUser(eventId, testUser);
        assertTrue(eventDAO.read(eventId).getSeenByUsers().size() >= 1);
        
        eventDAO.removeSeenByUser(eventId, testUser);
        assertTrue(eventDAO.read(eventId).getSeenByUsers().isEmpty());
    }

    @Test
    @Order(6)
    void testReadBetween() {
        Instant from = Instant.now().minus(1, ChronoUnit.DAYS);
        Instant to = Instant.now().plus(1, ChronoUnit.DAYS);
        assertFalse(eventDAO.readBetween(from, to).isEmpty());
    }

    @Test
    @Order(7)
    void testCreateErrors() {
        assertThrows(ApiRuntimeException.class, () -> eventDAO.create(null));
        
        Event e1 = new Event();
        assertThrows(ApiRuntimeException.class, () -> eventDAO.create(e1)); // title blank
        
        e1.setTitle("T");
        assertThrows(ApiRuntimeException.class, () -> eventDAO.create(e1)); // startAt null
        
        e1.setStartAt(Instant.now().minus(1, ChronoUnit.HOURS));
        assertThrows(ApiRuntimeException.class, () -> eventDAO.create(e1)); // startAt past
        
        e1.setStartAt(Instant.now().plus(1, ChronoUnit.HOURS));
        assertThrows(ApiRuntimeException.class, () -> eventDAO.create(e1)); // createdBy null
        
        e1.setCreatedBy(testUser);
        assertThrows(ApiRuntimeException.class, () -> eventDAO.create(e1)); // eventType null
    }

    @Test
    @Order(8)
    void testUpdateDeleteErrors() {
        assertThrows(ApiRuntimeException.class, () -> eventDAO.update(999999L, new Event()));
        assertThrows(ApiRuntimeException.class, () -> eventDAO.delete(999999L));
        assertThrows(ApiRuntimeException.class, () -> eventDAO.addSeenByUser(999999L, testUser));
        assertThrows(ApiRuntimeException.class, () -> eventDAO.removeSeenByUser(999999L, testUser));
    }

    @Test
    @Order(9)
    void testDelete() {
        eventDAO.delete(eventId);
        assertNull(eventDAO.read(eventId));
    }
}
