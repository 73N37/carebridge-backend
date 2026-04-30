package com.carebridge.crud.data.core;

import com.carebridge.CareBridgeApplication;
import com.carebridge.config.Populator;
import com.carebridge.crud.annotations.CrudResource;
import com.carebridge.crud.logic.DynamicCrudManager;
import com.carebridge.entities.User;
import com.carebridge.enums.Role;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for GenericRepository, exercising all methods and branches.
 * Also covers DynamicCrudManager edge cases: non-BaseEntity, @NotNull/@Positive fields, non-Void DTO.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = CareBridgeApplication.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class GenericRepositoryTest {

    @Autowired
    private DynamicCrudManager crudManager;

    @Autowired
    private Populator populator;

    private GenericRepository<User> userRepo;

    /**
     * A test-only entity class with @NotNull and @Positive fields and a non-Void DTO,
     * used to exercise DynamicCrudManager.inspectFields() branches.
     */
    @CrudResource(path = "test-coverage-entity", dto = String.class)
    static class TestCoverageEntity extends BaseEntity {
        @NotNull
        private String requiredField;

        @Positive
        private int positiveField;
    }

    @BeforeAll
    @SuppressWarnings("unchecked")
    public void setup() {
        populator.populate();
        userRepo = (GenericRepository<User>) crudManager.getMetadata("users").getRepository();
    }

    // ─── DynamicCrudManager coverage ────────────────────────────────────────

    @Test
    @Order(1)
    void testRegisterResource_nonBaseEntity() {
        // Covers: if (!BaseEntity.class.isAssignableFrom(entityClazz)) return;
        // No exception, just a no-op early return
        assertDoesNotThrow(() -> crudManager.registerResource(String.class));
    }

    @Test
    @Order(2)
    void testRegisterResource_withNotNullAndPositiveAndNonVoidDto() {
        // Covers: dto != Void.class → inspectionClass = dtoClass
        // Covers: field.isAnnotationPresent(NotNull.class) = true (short-circuit branch)
        // Covers: field.isAnnotationPresent(Positive.class) = true
        assertDoesNotThrow(() -> crudManager.registerResource(TestCoverageEntity.class));
    }

    @Test
    @Order(3)
    void testDiscoverAndRegister_nullPackage() {
        // Covers: try-catch exception path in discoverAndRegister
        assertDoesNotThrow(() -> crudManager.discoverAndRegister(null));
    }

    // ─── GenericRepository: read operations ──────────────────────────────────

    @Test
    @Order(10)
    void testFindAll() {
        List<User> users = userRepo.findAll();
        assertNotNull(users);
        assertFalse(users.isEmpty());
    }

    @Test
    @Order(11)
    void testFindAllPaged() {
        List<User> page0 = userRepo.findAll(0, 2);
        assertNotNull(page0);

        List<User> page1 = userRepo.findAll(1, 2);
        assertNotNull(page1);
    }

    @Test
    @Order(12)
    void testFindById_notFound() {
        Optional<User> result = userRepo.findById(999999999L);
        assertFalse(result.isPresent());
    }

    @Test
    @Order(13)
    void testExistsById_notFound() {
        assertFalse(userRepo.existsById(999999999L));
    }

    @Test
    @Order(14)
    void testCount() {
        long count = userRepo.count();
        assertTrue(count >= 0);
    }

    // ─── GenericRepository: write operations (transactional) ─────────────────

    @Test
    @Order(20)
    @Transactional
    void testSave_persistNewEntity() {
        // Covers: entity.getId() == null → entityManager.persist(entity)
        User u = new User("Repo Persist", "repo-persist-" + System.nanoTime() + "@test.com", "pass123", Role.USER);
        assertNull(u.getId());
        User saved = userRepo.save(u);
        assertNotNull(saved.getId());
    }

    @Test
    @Order(21)
    @Transactional
    void testSave_mergeExistingEntity() {
        // Covers: entity.getId() != null → entityManager.merge(entity)
        User u = new User("Repo Merge", "repo-merge-" + System.nanoTime() + "@test.com", "pass123", Role.USER);
        userRepo.save(u); // persist — generates ID
        assertNotNull(u.getId());

        u.setName("Merged Name");
        User merged = userRepo.save(u); // merge because id != null
        assertNotNull(merged);
    }

    @Test
    @Order(22)
    @Transactional
    void testDeleteById_found() {
        // Covers: entity != null → entityManager.remove(entity)
        User u = new User("Repo Delete", "repo-del-" + System.nanoTime() + "@test.com", "pass123", Role.USER);
        userRepo.save(u);
        Long id = u.getId();
        assertNotNull(id);

        userRepo.deleteById(id);
        assertFalse(userRepo.existsById(id));
    }

    @Test
    @Order(23)
    @Transactional
    void testDeleteById_notFound() {
        // Covers: entity == null → no-op path
        assertDoesNotThrow(() -> userRepo.deleteById(999999999L));
    }

    @Test
    @Order(24)
    @Transactional
    void testExistsById_found() {
        User u = new User("Repo Exists", "repo-exists-" + System.nanoTime() + "@test.com", "pass123", Role.USER);
        userRepo.save(u);
        assertTrue(userRepo.existsById(u.getId()));
    }

    @Test
    @Order(25)
    @Transactional
    void testFindById_found() {
        User u = new User("Repo FindId", "repo-findid-" + System.nanoTime() + "@test.com", "pass123", Role.USER);
        userRepo.save(u);
        Optional<User> found = userRepo.findById(u.getId());
        assertTrue(found.isPresent());
    }
}
