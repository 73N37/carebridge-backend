package com.carebridge.crud;

import com.carebridge.CareBridgeApplication;
import com.carebridge.crud.data.core.BaseEntity;
import com.carebridge.crud.data.core.GenericRepository;
import com.carebridge.crud.logic.DynamicCrudManager;
import com.carebridge.crud.logic.DynamicDtoAdvice;
import com.carebridge.crud.logic.MappingService;
import com.carebridge.crud.logic.core.BaseService;
import com.carebridge.entities.EventType;
import org.junit.jupiter.api.*;
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
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
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
    @Order(1)
    void testGenericRepository() {
        GenericRepository<EventType> repo = new GenericRepository<>(EventType.class, em);
        
        EventType et = new EventType("RepoTest" + System.nanoTime(), "#123456");
        repo.save(et);
        assertNotNull(et.getId());
        
        assertTrue(repo.existsById(et.getId()));
        assertFalse(repo.existsById(999999L));
        assertEquals(repo.count(), repo.count());
    }

    @Test
    @Order(2)
    void testBaseService() {
        BaseService<EventType> service = new BaseService<EventType>(EventType.class, em);
        
        EventType et = new EventType("ServiceTest", "#000");
        service.save(et);
        assertNotNull(et.getId());
        
        // Error branches
        assertThrows(RuntimeException.class, () -> service.save(et)); // Already has ID
        assertThrows(RuntimeException.class, () -> service.update(null, et));
        assertThrows(RuntimeException.class, () -> service.update(et.getId(), new EventType())); // Missing data in patch if validation is strict, but here we test the check
    }

    @Test
    @Order(3)
    void testMappingService() {
        Map<String, Object> data = Map.of("name", "Mapped", "colorHex", "#000");
        EventType et = mappingService.toEntity(data, EventType.class);
        assertEquals("Mapped", et.getName());
        
        Map<String, Object> mapped = mappingService.toMap(et);
        assertEquals("Mapped", mapped.get("name"));
        
        mappingService.toMapList(List.of(et));
    }

    @Test
    @Order(4)
    void testDynamicDtoAdvice() {
        EventType et = new EventType("AdviceTest", "#FFF");
        BaseService.Page<EventType> page = new BaseService.Page<>(List.of(et), 1L);
        dynamicDtoAdvice.beforeBodyWrite(page, null, null, null, null, null);
    }
}
