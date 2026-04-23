package com.carebridge.dao;

import com.carebridge.CareBridgeApplication;
import com.carebridge.dao.impl.EventTypeDAO;
import com.carebridge.entities.EventType;
import com.carebridge.exceptions.ApiRuntimeException;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = CareBridgeApplication.class)
@ActiveProfiles("test")
@Transactional
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class EventTypeDAOTest {

    @Autowired
    private EventTypeDAO eventTypeDAO;

    private Long createdId;

    @BeforeEach
    void setUp() {
        String name = "DAO Type " + System.nanoTime();
        EventType type = new EventType(name, "#ABCDEF");
        EventType created = eventTypeDAO.create(type);
        createdId = created.getId();
    }

    @Test
    @Order(1)
    void testCreateAndReadEventType() {
        assertNotNull(createdId);
        EventType read = eventTypeDAO.read(createdId);
        assertNotNull(read);
    }

    @Test
    @Order(2)
    void testReadAllEventTypes() {
        List<EventType> all = eventTypeDAO.readAll();
        assertTrue(all.size() >= 1);
    }

    @Test
    @Order(3)
    void testUpdateEventType() {
        String newName = "New Name " + System.nanoTime();
        EventType patch = new EventType();
        patch.setName(newName);
        EventType updated = eventTypeDAO.update(createdId, patch);
        assertEquals(newName, updated.getName());
    }

    @Test
    @Order(4)
    void testDAOErrors() {
        assertThrows(Exception.class, () -> eventTypeDAO.create(null));
        assertThrows(Exception.class, () -> eventTypeDAO.update(999999L, new EventType()));
        assertThrows(Exception.class, () -> eventTypeDAO.delete(999999L));
    }

    @Test
    @Order(5)
    void testDeleteEventType() {
        eventTypeDAO.delete(createdId);
        assertNull(eventTypeDAO.read(createdId));
    }
}
