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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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
public class EventDAOTest {

    @Autowired
    private EventDAO eventDAO;
    @Autowired
    private UserDAO userDAO;
    @Autowired
    private EventTypeDAO eventTypeDAO;

    private User testUser;
    private EventType testType;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setName("Event Creator");
        testUser.setEmail("creator@example.com" + System.currentTimeMillis());
        testUser.setRole(Role.ADMIN);
        testUser.setPassword("pass");
        userDAO.create(testUser);

        testType = new EventType("DAO Event Type " + System.currentTimeMillis(), "#123456");
        eventTypeDAO.create(testType);
    }

    @Test
    void testCreateAndReadEvent() {
        Event event = new Event();
        event.setTitle("DAO Test Event");
        event.setStartAt(Instant.now().plus(1, ChronoUnit.HOURS));
        event.setCreatedBy(testUser);
        event.setEventType(testType);

        Event created = eventDAO.create(event);
        assertNotNull(created.getId());

        Event read = eventDAO.read(created.getId());
        assertEquals("DAO Test Event", read.getTitle());
        assertNotNull(read.getCreatedBy());
        assertNotNull(read.getEventType());
    }

    @Test
    void testReadByCreator() {
        Event event = new Event();
        event.setTitle("Creator Test");
        event.setStartAt(Instant.now().plus(1, ChronoUnit.HOURS));
        event.setCreatedBy(testUser);
        event.setEventType(testType);
        Event created = eventDAO.create(event);

        List<Event> byCreator = eventDAO.readByCreator(testUser.getId());
        assertTrue(byCreator.stream().anyMatch(e -> e.getId().equals(created.getId())));
    }

    @Test
    void testReadAll() {
        Event event = new Event();
        event.setTitle("All Test");
        event.setStartAt(Instant.now().plus(1, ChronoUnit.HOURS));
        event.setCreatedBy(testUser);
        event.setEventType(testType);
        eventDAO.create(event);

        List<Event> all = eventDAO.readAll();
        assertFalse(all.isEmpty());
    }

    @Test
    void testUpdate() {
        Event event = new Event();
        event.setTitle("Old Title");
        event.setStartAt(Instant.now().plus(1, ChronoUnit.HOURS));
        event.setCreatedBy(testUser);
        event.setEventType(testType);
        Event created = eventDAO.create(event);

        Event patch = new Event();
        patch.setTitle("New Title");
        patch.setDescription("New Description");
        patch.setShowOnBoard(true);
        
        Event updated = eventDAO.update(created.getId(), patch);
        assertEquals("New Title", updated.getTitle());
        assertEquals("New Description", updated.getDescription());
        assertTrue(updated.isShowOnBoard());
    }

    @Test
    void testDelete() {
        Event event = new Event();
        event.setTitle("Delete Me");
        event.setStartAt(Instant.now().plus(1, ChronoUnit.HOURS));
        event.setCreatedBy(testUser);
        event.setEventType(testType);
        Event created = eventDAO.create(event);

        eventDAO.delete(created.getId());
        assertNull(eventDAO.read(created.getId()));
    }

    @Test
    void testReadBetween() {
        Instant from = Instant.now().plus(10, ChronoUnit.MINUTES);
        Instant to = Instant.now().plus(20, ChronoUnit.MINUTES);
        
        Event event = new Event();
        event.setTitle("Between Test");
        event.setStartAt(Instant.now().plus(15, ChronoUnit.MINUTES));
        event.setCreatedBy(testUser);
        event.setEventType(testType);
        eventDAO.create(event);

        List<Event> between = eventDAO.readBetween(from, to);
        assertFalse(between.isEmpty());
    }

    @Test
    void testAddRemoveSeenBy() {
        Event event = new Event();
        event.setTitle("Seen Test");
        event.setStartAt(Instant.now().plus(1, ChronoUnit.HOURS));
        event.setCreatedBy(testUser);
        event.setEventType(testType);
        Event created = eventDAO.create(event);

        eventDAO.addSeenByUser(created.getId(), testUser);
        Event read = eventDAO.read(created.getId());
        assertTrue(read.getSeenByUsers().stream().anyMatch(u -> u.getId().equals(testUser.getId())));

        eventDAO.removeSeenByUser(created.getId(), testUser);
        read = eventDAO.read(created.getId());
        assertFalse(read.getSeenByUsers().stream().anyMatch(u -> u.getId().equals(testUser.getId())));
    }

    @Test
    void testErrorCases() {
        assertThrows(ApiRuntimeException.class, () -> eventDAO.readByCreator(null));
        assertThrows(ApiRuntimeException.class, () -> eventDAO.create(null));
        assertThrows(ApiRuntimeException.class, () -> eventDAO.create(new Event())); // Missing fields
        assertThrows(ApiRuntimeException.class, () -> eventDAO.update(999L, new Event()));
        assertThrows(ApiRuntimeException.class, () -> eventDAO.delete(999L));
    }
}
