package com.carebridge.crud;

import com.carebridge.CareBridgeApplication;
import com.carebridge.crud.data.core.BaseEntity;
import com.carebridge.crud.data.core.GenericRepository;
import com.carebridge.crud.logic.DynamicCrudManager;
import com.carebridge.crud.logic.MappingService;
import com.carebridge.entities.EventType;
import com.carebridge.entities.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = CareBridgeApplication.class)
@ActiveProfiles("test")
@Transactional
public class CrudSystemTest {

    @Autowired
    private DynamicCrudManager crudManager;
    @Autowired
    private MappingService mappingService;
    @PersistenceContext
    private EntityManager em;

    @Test
    void testGenericRepository() {
        GenericRepository<EventType> repo = new GenericRepository<>(EventType.class, em);
        
        EventType et = new EventType("RepoTest" + System.nanoTime(), "#123456");
        repo.save(et);
        assertNotNull(et.getId());
        
        assertTrue(repo.existsById(et.getId()));
        assertEquals(1, repo.count());
        
        List<EventType> all = repo.findAll(0, 10);
        assertFalse(all.isEmpty());
        
        repo.deleteById(et.getId());
        assertFalse(repo.existsById(et.getId()));
    }

    @Test
    void testDynamicCrudManager() {
        assertNotNull(crudManager.getResources());
        assertNotNull(crudManager.getMetadata("event-types"));
        assertNull(crudManager.getMetadata("non-existent"));
    }

    @Test
    void testMappingService() {
        Map<String, Object> data = Map.of("name", "Mapped", "colorHex", "#000");
        EventType et = mappingService.toEntity(data, EventType.class);
        assertEquals("Mapped", et.getName());
        
        // Test entity to Map conversion coverage
        Map<String, Object> mapped = mappingService.toMap(et);
        assertEquals("Mapped", mapped.get("name"));
    }

    @Test
    void testBaseEntityHierarchy() {
        EventType parent = new EventType("Parent", "#1");
        EventType child = new EventType("Child", "#2");
        child.setParent(parent);
        parent.getChildren().add(child);
        
        EventType grandchild = new EventType("Grandchild", "#3");
        grandchild.setParent(child);
        child.getChildren().add(grandchild);
        
        List<BaseEntity> grandchildren = parent.getGrandchildren();
        assertFalse(grandchildren.isEmpty());
        assertEquals("Grandchild", ((EventType)grandchildren.get(0)).getName());
    }
}
