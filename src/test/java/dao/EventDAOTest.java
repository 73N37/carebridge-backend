package dao;

import com.carebridge.CareBridgeApplication;
import com.carebridge.dao.impl.EventDAO;
import com.carebridge.dao.impl.EventTypeDAO;
import com.carebridge.dao.impl.UserDAO;
import com.carebridge.entities.Event;
import com.carebridge.entities.EventType;
import com.carebridge.entities.User;
import com.carebridge.enums.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

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
        testUser.setEmail("creator@example.com");
        testUser.setRole(Role.ADMIN);
        testUser.setPassword("pass");
        userDAO.create(testUser);

        testType = new EventType("DAO Event Type", "#123456");
        eventTypeDAO.create(testType);
    }

    @Test
    void testCreateAndReadEvent() {
        Event event = new Event();
        event.setTitle("DAO Test Event");
        event.setStartAt(Instant.now().plusSeconds(3600));
        event.setCreatedBy(testUser);
        event.setEventType(testType);

        Event created = eventDAO.create(event);
        assertNotNull(created.getId());

        Event read = eventDAO.read(created.getId());
        assertEquals("DAO Test Event", read.getTitle());
    }

    @Test
    void testReadByCreator() {
        Event event = new Event();
        event.setTitle("Creator Test");
        event.setStartAt(Instant.now().plusSeconds(3600));
        event.setCreatedBy(testUser);
        event.setEventType(testType);
        Event created = eventDAO.create(event);

        List<Event> byCreator = eventDAO.readByCreator(testUser.getId());
        assertTrue(byCreator.stream().anyMatch(e -> e.getId().equals(created.getId())));
    }
}
