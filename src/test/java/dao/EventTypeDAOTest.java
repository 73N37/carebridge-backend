package dao;

import com.carebridge.CareBridgeApplication;
import com.carebridge.dao.impl.EventTypeDAO;
import com.carebridge.entities.EventType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = CareBridgeApplication.class)
@ActiveProfiles("test")
@Transactional
public class EventTypeDAOTest {

    @Autowired
    private EventTypeDAO eventTypeDAO;

    @Test
    void testCreateAndReadEventType() {
        EventType type = new EventType("DAO Type", "#ABCDEF");
        EventType created = eventTypeDAO.create(type);
        assertNotNull(created.getId());

        EventType read = eventTypeDAO.read(created.getId());
        assertEquals("DAO Type", read.getName());
    }

    @Test
    void testReadAllEventTypes() {
        eventTypeDAO.create(new EventType("T1", "#111111"));
        eventTypeDAO.create(new EventType("T2", "#222222"));

        List<EventType> all = eventTypeDAO.readAll();
        assertTrue(all.size() >= 2);
    }

    @Test
    void testUpdateEventType() {
        EventType type = new EventType("Old Name", "#000000");
        EventType created = eventTypeDAO.create(type);

        EventType patch = new EventType();
        patch.setName("New Name");
        EventType updated = eventTypeDAO.update(created.getId(), patch);

        assertEquals("New Name", updated.getName());
    }

    @Test
    void testDeleteEventType() {
        EventType type = new EventType("Delete Type", "#000000");
        EventType created = eventTypeDAO.create(type);

        eventTypeDAO.delete(created.getId());
        assertNull(eventTypeDAO.read(created.getId()));
    }
}
