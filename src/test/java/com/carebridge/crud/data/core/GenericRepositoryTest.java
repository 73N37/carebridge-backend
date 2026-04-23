package com.carebridge.crud.data.core;

import com.carebridge.CareBridgeApplication;
import com.carebridge.entities.EventType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = CareBridgeApplication.class)
@ActiveProfiles("test")
@Transactional
public class GenericRepositoryTest {

    @PersistenceContext
    private EntityManager entityManager;

    private GenericRepository<EventType> repository;

    @BeforeEach
    void setUp() {
        repository = new GenericRepository<>(EventType.class, entityManager);
    }

    @Test
    void testFindAll() {
        EventType et1 = new EventType("Type 1", "#111111");
        EventType et2 = new EventType("Type 2", "#222222");
        repository.save(et1);
        repository.save(et2);

        List<EventType> all = repository.findAll();
        assertTrue(all.size() >= 2);
        assertTrue(all.stream().anyMatch(e -> e.getName().equals("Type 1")));
        assertTrue(all.stream().anyMatch(e -> e.getName().equals("Type 2")));
    }

    @Test
    void testFindAllWithPagination() {
        // First, clear existing ones to have a clean state for pagination testing if possible, 
        // but since it is @Transactional it might be better to just work with what we have.
        // Actually, let's just add enough and check relative results.
        
        repository.save(new EventType("P1", "#100000"));
        repository.save(new EventType("P2", "#200000"));
        repository.save(new EventType("P3", "#300000"));

        List<EventType> page0 = repository.findAll(0, 2);
        assertEquals(2, page0.size());

        List<EventType> page1 = repository.findAll(1, 2);
        assertTrue(page1.size() >= 1);
    }

    @Test
    void testFindById() {
        EventType et = new EventType("Find Me", "#333333");
        repository.save(et);
        Long id = et.getId();

        Optional<EventType> found = repository.findById(id);
        assertTrue(found.isPresent());
        assertEquals("Find Me", found.get().getName());

        Optional<EventType> notFound = repository.findById(-1L);
        assertFalse(notFound.isPresent());
    }

    @Test
    void testSave() {
        // Branch: id == null (persist)
        EventType et1 = new EventType("New", "#444444");
        assertNull(et1.getId());
        EventType saved1 = repository.save(et1);
        assertNotNull(saved1.getId());
        
        // Branch: id != null (merge)
        saved1.setName("Updated");
        EventType saved2 = repository.save(saved1);
        assertEquals(saved1.getId(), saved2.getId());
        assertEquals("Updated", saved2.getName());
        
        // Ensure it's actually merged in DB
        entityManager.flush();
        entityManager.clear();
        EventType found = repository.findById(saved1.getId()).orElseThrow();
        assertEquals("Updated", found.getName());
    }

    @Test
    void testDeleteById() {
        EventType et = new EventType("Delete Me", "#555555");
        repository.save(et);
        Long id = et.getId();
        assertTrue(repository.existsById(id));

        // Branch: entity != null
        repository.deleteById(id);
        assertFalse(repository.existsById(id));

        // Branch: entity == null
        assertDoesNotThrow(() -> repository.deleteById(id)); // Already deleted
        assertDoesNotThrow(() -> repository.deleteById(-1L));
    }

    @Test
    void testExistsById() {
        EventType et = new EventType("Exist Check", "#666666");
        repository.save(et);
        assertTrue(repository.existsById(et.getId()));
        assertFalse(repository.existsById(-1L));
    }

    @Test
    void testCount() {
        long initialCount = repository.count();
        repository.save(new EventType("Count 1", "#777777"));
        repository.save(new EventType("Count 2", "#888888"));
        assertEquals(initialCount + 2, repository.count());
    }
}
