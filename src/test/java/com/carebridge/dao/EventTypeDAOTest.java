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
    void testReadAndReadAll() {
        assertNotNull(eventTypeDAO.read(createdId));
        assertFalse(eventTypeDAO.readAll().isEmpty());
    }

    @Test
    @Order(2)
    void testUpdateBranches() {
        EventType patch = new EventType();
        patch.setName("NewN");
        patch.setColorHex("#123");
        EventType updated = eventTypeDAO.update(createdId, patch);
        assertEquals("NewN", updated.getName());
        assertEquals("#123", updated.getColorHex());

        // Null branches
        EventType patch2 = new EventType();
        patch2.setName(null);
        patch2.setColorHex(null);
        EventType updated2 = eventTypeDAO.update(createdId, patch2);
        assertEquals("NewN", updated2.getName());
        
        assertThrows(ApiRuntimeException.class, () -> eventTypeDAO.update(999999L, new EventType()));
    }

    @Test
    @Order(3)
    void testDelete() {
        eventTypeDAO.delete(createdId);
        assertNull(eventTypeDAO.read(createdId));
        
        // delete null check
        eventTypeDAO.delete(999999L);
    }
}
