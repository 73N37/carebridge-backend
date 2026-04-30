package com.carebridge.crud;

import com.carebridge.CareBridgeApplication;
import com.carebridge.crud.data.core.GenericRepository;
import com.carebridge.crud.logic.core.BaseService;
import com.carebridge.entities.Resident;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Spring integration tests covering GenericRepository and BaseService.
 * All tests run within a transaction that is rolled back after each test.
 */
@SpringBootTest(classes = CareBridgeApplication.class)
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class SpringIntegrationCoverageTest {

    @PersistenceContext
    private EntityManager entityManager;

    // ────────────────────────────────────────────────
    //  GenericRepository tests
    // ────────────────────────────────────────────────

    @Test
    @Order(1)
    @Transactional
    public void testGenericRepository_findAll() {
        GenericRepository<Resident> repo = new GenericRepository<>(Resident.class, entityManager);
        List<Resident> all = repo.findAll();
        assertNotNull(all);
    }

    @Test
    @Order(2)
    @Transactional
    public void testGenericRepository_findAll_paginated() {
        GenericRepository<Resident> repo = new GenericRepository<>(Resident.class, entityManager);
        List<Resident> page = repo.findAll(0, 5);
        assertNotNull(page);
    }

    @Test
    @Order(3)
    @Transactional
    public void testGenericRepository_findById_notFound() {
        GenericRepository<Resident> repo = new GenericRepository<>(Resident.class, entityManager);
        Optional<Resident> opt = repo.findById(999999999L);
        assertFalse(opt.isPresent());
    }

    @Test
    @Order(4)
    @Transactional
    public void testGenericRepository_findById_found() {
        GenericRepository<Resident> repo = new GenericRepository<>(Resident.class, entityManager);
        Resident r = new Resident();
        r.setFirstName("FindByIdTest");
        entityManager.persist(r);
        entityManager.flush();

        Optional<Resident> opt = repo.findById(r.getId());
        assertTrue(opt.isPresent());
        assertEquals("FindByIdTest", opt.get().getFirstName());
    }

    @Test
    @Order(5)
    @Transactional
    public void testGenericRepository_save_newEntity() {
        // entity.getId() == null → persist
        GenericRepository<Resident> repo = new GenericRepository<>(Resident.class, entityManager);
        Resident r = new Resident();
        r.setFirstName("SaveNew");
        Resident saved = repo.save(r);
        entityManager.flush();
        assertNotNull(saved.getId());
    }

    @Test
    @Order(6)
    @Transactional
    public void testGenericRepository_save_existingEntity() {
        // entity.getId() != null → merge
        GenericRepository<Resident> repo = new GenericRepository<>(Resident.class, entityManager);
        Resident r = new Resident();
        r.setFirstName("SaveExisting");
        entityManager.persist(r);
        entityManager.flush();

        r.setFirstName("SaveExisting-Updated");
        Resident merged = repo.save(r);
        assertEquals("SaveExisting-Updated", merged.getFirstName());
    }

    @Test
    @Order(7)
    @Transactional
    public void testGenericRepository_deleteById_found() {
        GenericRepository<Resident> repo = new GenericRepository<>(Resident.class, entityManager);
        Resident r = new Resident();
        r.setFirstName("ToDelete");
        entityManager.persist(r);
        entityManager.flush();
        Long id = r.getId();

        repo.deleteById(id);
        entityManager.flush();
        assertNull(entityManager.find(Resident.class, id));
    }

    @Test
    @Order(8)
    @Transactional
    public void testGenericRepository_deleteById_notFound() {
        // entity == null → silently skips
        GenericRepository<Resident> repo = new GenericRepository<>(Resident.class, entityManager);
        assertDoesNotThrow(() -> repo.deleteById(999999999L));
    }

    @Test
    @Order(9)
    @Transactional
    public void testGenericRepository_existsById() {
        GenericRepository<Resident> repo = new GenericRepository<>(Resident.class, entityManager);
        Resident r = new Resident();
        r.setFirstName("ExistsTest");
        entityManager.persist(r);
        entityManager.flush();

        assertTrue(repo.existsById(r.getId()));
        assertFalse(repo.existsById(999999999L));
    }

    @Test
    @Order(10)
    @Transactional
    public void testGenericRepository_count() {
        GenericRepository<Resident> repo = new GenericRepository<>(Resident.class, entityManager);
        long count = repo.count();
        assertTrue(count >= 0);
    }

    // ────────────────────────────────────────────────
    //  BaseService tests
    // ────────────────────────────────────────────────

    @Test
    @Order(11)
    @Transactional
    public void testBaseService_findAll() {
        BaseService<Resident> svc = new BaseService<>(Resident.class, entityManager);
        List<Resident> all = svc.findAll();
        assertNotNull(all);
    }

    @Test
    @Order(12)
    @Transactional
    public void testBaseService_findAll_paginated() {
        BaseService<Resident> svc = new BaseService<>(Resident.class, entityManager);
        BaseService.Page<Resident> page = svc.findAll(0, 10);
        assertNotNull(page.getContent());
        assertTrue(page.getTotalElements() >= 0);
    }

    @Test
    @Order(13)
    @Transactional
    public void testBaseService_findById_notFound() {
        BaseService<Resident> svc = new BaseService<>(Resident.class, entityManager);
        Optional<Resident> opt = svc.findById(999999999L);
        assertFalse(opt.isPresent());
    }

    @Test
    @Order(14)
    @Transactional
    public void testBaseService_findById_found() {
        BaseService<Resident> svc = new BaseService<>(Resident.class, entityManager);
        Resident r = new Resident();
        r.setFirstName("BaseServiceFind");
        entityManager.persist(r);
        entityManager.flush();

        Optional<Resident> opt = svc.findById(r.getId());
        assertTrue(opt.isPresent());
    }

    @Test
    @Order(15)
    @Transactional
    public void testBaseService_save_nullId() {
        // entity.getId() == null → persist
        BaseService<Resident> svc = new BaseService<>(Resident.class, entityManager);
        Resident r = new Resident();
        r.setFirstName("BaseServiceSave");
        Resident saved = svc.save(r);
        entityManager.flush();
        assertNotNull(saved);
    }

    @Test
    @Order(16)
    @Transactional
    public void testBaseService_save_nonNullId_throwsException() {
        // entity.getId() != null → throws "Entity already has an ID. Use update instead."
        BaseService<Resident> svc = new BaseService<>(Resident.class, entityManager);
        Resident r = new Resident();
        r.setId(9999998L);
        assertThrows(RuntimeException.class, () -> svc.save(r));
    }

    @Test
    @Order(17)
    @Transactional
    public void testBaseService_update_found() {
        BaseService<Resident> svc = new BaseService<>(Resident.class, entityManager);
        Resident r = new Resident();
        r.setFirstName("BaseServiceUpdate");
        entityManager.persist(r);
        entityManager.flush();

        Resident patch = new Resident();
        patch.setFirstName("BaseServiceUpdated");
        Resident result = svc.update(r.getId(), patch);
        assertNotNull(result);
    }

    @Test
    @Order(18)
    @Transactional
    public void testBaseService_update_notFound() {
        // existing == null → throws "Entity not found with id: ..."
        BaseService<Resident> svc = new BaseService<>(Resident.class, entityManager);
        Resident patch = new Resident();
        assertThrows(RuntimeException.class, () -> svc.update(999999999L, patch));
    }

    @Test
    @Order(19)
    @Transactional
    public void testBaseService_deleteById_found() {
        BaseService<Resident> svc = new BaseService<>(Resident.class, entityManager);
        Resident r = new Resident();
        r.setFirstName("BaseServiceDelete");
        entityManager.persist(r);
        entityManager.flush();
        Long id = r.getId();

        svc.deleteById(id);
        entityManager.flush();
        assertNull(entityManager.find(Resident.class, id));
    }

    @Test
    @Order(20)
    @Transactional
    public void testBaseService_deleteById_notFound() {
        // entity == null → silently skips
        BaseService<Resident> svc = new BaseService<>(Resident.class, entityManager);
        assertDoesNotThrow(() -> svc.deleteById(999999999L));
    }

    @Test
    @Order(21)
    @Transactional
    public void testBaseService_page_getters() {
        List<Resident> content = List.of(new Resident(), new Resident());
        BaseService.Page<Resident> page = new BaseService.Page<>(content, 42L);
        assertEquals(2, page.getContent().size());
        assertEquals(42L, page.getTotalElements());
    }
}
