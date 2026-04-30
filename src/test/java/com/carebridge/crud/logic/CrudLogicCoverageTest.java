package com.carebridge.crud.logic;

import com.carebridge.CareBridgeApplication;
import com.carebridge.crud.data.core.BaseEntity;
import com.carebridge.crud.logic.core.BaseService;
import com.carebridge.crud.logic.core.CrudInterceptor;
import com.carebridge.entities.Resident;
import com.carebridge.entities.User;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Coverage tests for DynamicCrudManager, DynamicDtoAdvice, MappingService, and CrudInterceptor.
 */
@SpringBootTest(classes = CareBridgeApplication.class)
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class CrudLogicCoverageTest {

    @Autowired
    private DynamicCrudManager crudManager;

    @Autowired
    private MappingService mappingService;

    @Autowired
    private DynamicDtoAdvice dynamicDtoAdvice;

    // ────────────────────────────────────────────────
    //  MappingService edge cases
    // ────────────────────────────────────────────────

    @Test
    @Order(1)
    void testMappingService_toMap_null() {
        // entity == null branch
        assertNull(mappingService.toMap(null));
    }

    @Test
    @Order(2)
    void testMappingService_toMapList_null() {
        // entities == null branch
        List<Map<String, Object>> result = mappingService.toMapList(null);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @Order(3)
    void testMappingService_toMapList_emptyList() {
        List<Map<String, Object>> result = mappingService.toMapList(List.of());
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @Order(4)
    void testMappingService_toEntity_conversionFailure() {
        // Passing an invalid enum value → objectMapper throws → wrapped in RuntimeException
        assertThrows(RuntimeException.class, () ->
                mappingService.toEntity(Map.of("role", "TOTALLY_INVALID_ROLE"), User.class)
        );
    }

    // ────────────────────────────────────────────────
    //  DynamicCrudManager edge cases
    // ────────────────────────────────────────────────

    @Test
    @Order(5)
    void testDynamicCrudManager_registerInterceptor() {
        CrudInterceptor<Resident> interceptor = new CrudInterceptor<Resident>() {};
        // Should not throw; just adds to internal map
        crudManager.registerInterceptor(Resident.class, interceptor);
    }

    @Test
    @Order(6)
    void testDynamicCrudManager_registerResource_nonBaseEntity() {
        // !BaseEntity.class.isAssignableFrom(String.class) → true → returns early
        assertDoesNotThrow(() -> crudManager.registerResource(String.class));
    }

    @Test
    @Order(7)
    void testDynamicCrudManager_getResources_notEmpty() {
        Map<String, ResourceMetadata<?>> resources = crudManager.getResources();
        assertFalse(resources.isEmpty());
        assertTrue(resources.containsKey("residents"));
    }

    @Test
    @Order(8)
    void testDynamicCrudManager_getMetadata_notFound() {
        assertNull(crudManager.getMetadata("nonexistent-xyz-resource"));
    }

    @Test
    @Order(9)
    void testDynamicCrudManager_discoverAndRegister_unknownPackage() {
        // try block runs, Reflections finds nothing – should not throw
        assertDoesNotThrow(() ->
                crudManager.discoverAndRegister("com.nonexistent.package.that.does.not.exist")
        );
    }

    @Test
    @Order(19)
    void testDynamicCrudManager_discoverAndRegister_nullPackage_catchesException() {
        // null package → Reflections throws NullPointerException → catch(Exception e) covered
        assertDoesNotThrow(() -> crudManager.discoverAndRegister(null));
    }

    // ────────────────────────────────────────────────
    //  CrudInterceptor default methods
    // ────────────────────────────────────────────────

    @Test
    @Order(10)
    void testCrudInterceptor_defaultMethods() {
        CrudInterceptor<Resident> interceptor = new CrudInterceptor<Resident>() {};
        Resident r = new Resident();
        assertDoesNotThrow(() -> interceptor.beforeCreate(r));
        assertDoesNotThrow(() -> interceptor.afterCreate(r));
        assertDoesNotThrow(() -> interceptor.beforeUpdate(r));
        assertDoesNotThrow(() -> interceptor.afterUpdate(r));
        assertDoesNotThrow(() -> interceptor.beforeDelete(1L));
        assertDoesNotThrow(() -> interceptor.afterDelete(1L));
    }

    // ────────────────────────────────────────────────
    //  DynamicDtoAdvice edge cases
    // ────────────────────────────────────────────────

    @Test
    @Order(11)
    void testDynamicDtoAdvice_beforeBodyWrite_null() {
        // body == null → return null
        Object result = dynamicDtoAdvice.beforeBodyWrite(null, null, null, null, null, null);
        assertNull(result);
    }

    @Test
    @Order(12)
    void testDynamicDtoAdvice_beforeBodyWrite_plainMapBody() {
        // body is not BaseEntity, not Collection, not Page → falls through to "return body"
        Map<String, String> plainBody = Map.of("msg", "hello");
        Object result = dynamicDtoAdvice.beforeBodyWrite(plainBody, null, null, null, null, null);
        assertEquals(plainBody, result);
    }

    @Test
    @Order(13)
    void testDynamicDtoAdvice_beforeBodyWrite_collectionOfNonEntities() {
        // Collection where items are NOT BaseEntity → item returned as-is
        List<String> strings = List.of("alpha", "beta");
        Object result = dynamicDtoAdvice.beforeBodyWrite(strings, null, null, null, null, null);
        assertNotNull(result);
        assertInstanceOf(List.class, result);
    }

    @Test
    @Order(14)
    void testDynamicDtoAdvice_beforeBodyWrite_pageWithNonEntityItems() {
        // BaseService.Page where content items are Maps (not BaseEntity)
        List<Map<String, Object>> items = List.of(
                Map.of("key", (Object) "value"),
                Map.of("k2", (Object) "v2")
        );
        BaseService.Page<Map<String, Object>> page = new BaseService.Page<>(items, 2L);
        Object result = dynamicDtoAdvice.beforeBodyWrite(page, null, MediaType.APPLICATION_JSON, null, null, null);
        assertNotNull(result);
        assertInstanceOf(Map.class, result);
        @SuppressWarnings("unchecked")
        Map<String, Object> map = (Map<String, Object>) result;
        assertEquals(2L, map.get("totalElements"));
    }

    @Test
    @Order(15)
    void testDynamicDtoAdvice_beforeBodyWrite_pageWithEntityItems() {
        // BaseService.Page where content items ARE BaseEntities → toMap called
        Resident r = new Resident();
        r.setFirstName("PageEntityTest");
        List<BaseEntity> items = List.of(r);
        BaseService.Page<BaseEntity> page = new BaseService.Page<>(items, 1L);
        Object result = dynamicDtoAdvice.beforeBodyWrite(page, null, MediaType.APPLICATION_JSON, null, null, null);
        assertNotNull(result);
        assertInstanceOf(Map.class, result);
    }

    @Test
    @Order(16)
    void testDynamicDtoAdvice_beforeBodyWrite_singleEntity() {
        // body instanceof BaseEntity → mappingService.toMap(entity)
        Resident r = new Resident();
        r.setFirstName("SingleEntityTest");
        Object result = dynamicDtoAdvice.beforeBodyWrite(r, null, null, null, null, null);
        assertNotNull(result);
        assertInstanceOf(Map.class, result);
    }

    @Test
    @Order(17)
    void testDynamicDtoAdvice_beforeBodyWrite_collectionOfEntities() {
        // Collection of BaseEntities → mapped to list of maps
        Resident r1 = new Resident();
        r1.setFirstName("Ent1");
        Resident r2 = new Resident();
        r2.setFirstName("Ent2");
        List<BaseEntity> items = List.of(r1, r2);
        Object result = dynamicDtoAdvice.beforeBodyWrite(items, null, null, null, null, null);
        assertNotNull(result);
        assertInstanceOf(List.class, result);
    }

    // ────────────────────────────────────────────────
    //  ResourceMetadata builder
    // ────────────────────────────────────────────────

    @Test
    @Order(18)
    void testResourceMetadata_fieldInfo() {
        ResourceMetadata.FieldInfo field = new ResourceMetadata.FieldInfo(
                "testField", "String", true, Map.of("min", 0, "max", 255)
        );
        assertEquals("testField", field.getName());
        assertEquals("String", field.getType());
        assertTrue(field.isRequired());
        assertEquals(0, field.getConstraints().get("min"));
    }

    @Test
    @Order(20)
    void testMappingService_toMap_entityWithNonEmptyCollection() {
        // Covers: Hibernate.isInitialized(col) true, item instanceof BaseEntity true, !ids.isEmpty() true
        Resident resident = new Resident();
        resident.setFirstName("CollectionTest");
        User user = new User();
        user.setId(1L);
        resident.addUser(user);

        Map<String, Object> map = mappingService.toMap(resident);
        assertNotNull(map);
        // 'usersIds' should be present since users set is non-empty
        assertTrue(map.containsKey("usersIds"));
    }
}
