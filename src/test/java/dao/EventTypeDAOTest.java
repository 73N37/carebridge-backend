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
        String name = "DAO Type " + System.currentTimeMillis();
        EventType type = new EventType(name, "#ABCDEF");
        EventType created = eventTypeDAO.create(type);
        assertNotNull(created.getId());

        EventType read = eventTypeDAO.read(created.getId());
        assertEquals(name, read.getName());
    }

    @Test
    void testReadAllEventTypes() {
        eventTypeDAO.create(new EventType("T1" + System.nanoTime(), "#111111"));
        eventTypeDAO.create(new EventType("T2" + System.nanoTime(), "#222222"));

        List<EventType> all = eventTypeDAO.readAll();
        assertTrue(all.size() >= 2);
    }

    @Test
    void testUpdateEventType() {
        EventType type = new EventType("Old Name " + System.currentTimeMillis(), "#000000");
        EventType created = eventTypeDAO.create(type);

        EventType patch = new EventType();
        patch.setName("New Name " + System.currentTimeMillis());
        EventType updated = eventTypeDAO.update(created.getId(), patch);

        assertNotNull(updated);
    }

    @Test
    void testDeleteEventType() {
        EventType type = new EventType("Delete Type " + System.currentTimeMillis(), "#000000");
        EventType created = eventTypeDAO.create(type);

        eventTypeDAO.delete(created.getId());
        assertNull(eventTypeDAO.read(created.getId()));
    }
}
