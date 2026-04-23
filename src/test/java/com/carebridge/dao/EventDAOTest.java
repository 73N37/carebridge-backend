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
    void testReadBranches() {
        assertNotNull(eventDAO.read(eventId));
        assertNull(eventDAO.read(999999L));
    }

    @Test
    @Order(2)
    void testReadByCreatorBranches() {
        assertFalse(eventDAO.readByCreator(testUser.getId()).isEmpty());
        // userId null branch
        assertThrows(ApiRuntimeException.class, () -> eventDAO.readByCreator(null));
    }

    @Test
    @Order(3)
    void testCreateBranches() {
        // e null
        assertThrows(ApiRuntimeException.class, () -> eventDAO.create(null));
        
        Event e = new Event();
        // title null
        assertThrows(ApiRuntimeException.class, () -> eventDAO.create(e));
        // title blank
        e.setTitle(" ");
        assertThrows(ApiRuntimeException.class, () -> eventDAO.create(e));
        
        // startAt null
        e.setTitle("T");
        assertThrows(ApiRuntimeException.class, () -> eventDAO.create(e));
        
        // startAt past
        e.setStartAt(Instant.now().minus(1, ChronoUnit.HOURS));
        assertThrows(ApiRuntimeException.class, () -> eventDAO.create(e));
        
        // createdBy null
        e.setStartAt(Instant.now().plus(1, ChronoUnit.HOURS));
        assertThrows(ApiRuntimeException.class, () -> eventDAO.create(e));
        
        // eventType null
        e.setCreatedBy(testUser);
        assertThrows(ApiRuntimeException.class, () -> eventDAO.create(e));
    }

    @Test
    @Order(4)
    void testUpdateBranches() {
        // existing null
        assertThrows(ApiRuntimeException.class, () -> eventDAO.update(999999L, new Event()));
        
        Event patch = new Event();
        patch.setTitle("New");
        patch.setDescription("D");
        patch.setStartAt(Instant.now().plus(2, ChronoUnit.HOURS));
        patch.setShowOnBoard(true);
        patch.setEventType(testType);
        patch.setCreatedBy(testUser);
        patch.setSeenByUsers(Set.of(testUser));
        
        Event updated = eventDAO.update(eventId, patch);
        assertEquals("New", updated.getTitle());
        assertEquals("D", updated.getDescription());
        assertFalse(updated.getSeenByUsers().isEmpty());

        // Branches with null/blank fields (should NOT update)
        Event patch2 = new Event();
        patch2.setTitle("");
        patch2.setDescription(null);
        patch2.setStartAt(null);
        patch2.setEventType(null);
        patch2.setCreatedBy(null);
        patch2.setSeenByUsers(new HashSet<>());
        
        Event updated2 = eventDAO.update(eventId, patch2);
        assertEquals("New", updated2.getTitle());
        assertEquals("D", updated2.getDescription());
    }

    @Test
    @Order(5)
    void testDeleteBranches() {
        assertThrows(ApiRuntimeException.class, () -> eventDAO.delete(999999L));
        eventDAO.delete(eventId);
    }

    @Test
    @Order(6)
    void testAddRemoveSeenByBranches() {
        assertThrows(ApiRuntimeException.class, () -> eventDAO.addSeenByUser(999999L, testUser));
        assertThrows(ApiRuntimeException.class, () -> eventDAO.removeSeenByUser(999999L, testUser));
        
        eventDAO.addSeenByUser(eventId, testUser);
        eventDAO.removeSeenByUser(eventId, testUser);
    }
    
    @Test
    @Order(7)
    void testReadAllAndReadBetween() {
        assertFalse(eventDAO.readAll().isEmpty());
        assertFalse(eventDAO.readBetween(Instant.now().minusSeconds(10), Instant.now().plusSeconds(7200)).isEmpty());
    }
}
