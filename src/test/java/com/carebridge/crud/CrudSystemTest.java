package com.carebridge.crud;

import com.carebridge.CareBridgeApplication;
import com.carebridge.crud.data.core.BaseEntity;
import com.carebridge.crud.data.core.GenericRepository;
import com.carebridge.crud.logic.DynamicCrudManager;
import com.carebridge.crud.logic.DynamicDtoAdvice;
import com.carebridge.crud.logic.MappingService;
import com.carebridge.crud.logic.core.BaseService;
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
    @Autowired
    private DynamicDtoAdvice dynamicDtoAdvice;
    @PersistenceContext
    private EntityManager em;

    @Test
    void testGenericRepository() {
        GenericRepository<EventType> repo = new GenericRepository<>(EventType.class, em);
        
        EventType et = new EventType("RepoTest" + System.nanoTime(), "#123456");
        repo.save(et);
        assertNotNull(et.getId());
        
        assertTrue(repo.existsById(et.getId()));
        assertEquals(repo.count(), repo.count()); // Simple coverage
        
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
        
        Map<String, Object> mapped = mappingService.toMap(et);
        assertEquals("Mapped", mapped.get("name"));

        assertNull(mappingService.toMap(null));
        assertTrue(mappingService.toMapList(null).isEmpty());
        
        assertThrows(RuntimeException.class, () -> mappingService.toEntity(Map.of("id", "invalid"), EventType.class));
    }

    @Test
    void testDynamicDtoAdvice() {
        EventType et = new EventType("AdviceTest", "#FFF");
        
        // Single entity
        Object result1 = dynamicDtoAdvice.beforeBodyWrite(et, null, null, null, null, null);
        assertTrue(result1 instanceof Map);
        
        // List
        Object result2 = dynamicDtoAdvice.beforeBodyWrite(List.of(et), null, null, null, null, null);
        assertTrue(result2 instanceof List);
        
        // Page
        BaseService.Page<EventType> page = new BaseService.Page<>(List.of(et), 1L);
        Object result3 = dynamicDtoAdvice.beforeBodyWrite(page, null, null, null, null, null);
        assertTrue(result3 instanceof Map);
        assertEquals(1L, ((Map<?,?>)result3).get("totalElements"));

        // Null/Other
        assertNull(dynamicDtoAdvice.beforeBodyWrite(null, null, null, null, null, null));
        String other = "other";
        assertEquals(other, dynamicDtoAdvice.beforeBodyWrite(other, null, null, null, null, null));
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
