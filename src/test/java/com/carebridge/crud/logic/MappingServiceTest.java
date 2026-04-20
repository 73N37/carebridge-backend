package com.carebridge.crud.logic;

import com.carebridge.crud.annotations.ExcludeFromDTO;
import com.carebridge.crud.data.core.BaseEntity;
import com.carebridge.entities.User;
import com.carebridge.enums.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class MappingServiceTest {

    private MappingService mappingService;

    @BeforeEach
    void setUp() {
        mappingService = new MappingService();
    }

    @Test
    void testToMap_BasicEntity() {
        User user = new User();
        user.setId(1L);
        user.setName("Test User");
        user.setEmail("test@example.com");
        user.setRole(Role.USER);

        Map<String, Object> map = mappingService.toMap(user);

        assertNotNull(map);
        assertEquals(1L, map.get("id"));
        assertEquals("Test User", map.get("name"));
        assertEquals("test@example.com", map.get("email"));
        assertEquals(Role.USER, map.get("role"));
        // passwordHash should be excluded if annotated with @ExcludeFromDTO
    }

    @Test
    void testToMap_ExcludeAnnotation() {
        // We'll use a local class for testing exclusion if needed, 
        // but let's assume User has some excluded fields.
        User user = new User();
        user.setPassword("secret"); // This sets passwordHash

        Map<String, Object> map = mappingService.toMap(user);
        
        assertFalse(map.containsKey("passwordHash"), "passwordHash should be excluded from DTO");
    }

    @Test
    void testToEntity_BasicMap() {
        Map<String, Object> data = Map.of(
            "name", "New User",
            "email", "new@example.com",
            "role", "ADMIN"
        );

        User user = mappingService.toEntity(data, User.class);

        assertNotNull(user);
        assertEquals("New User", user.getName());
        assertEquals("new@example.com", user.getEmail());
        assertEquals(Role.ADMIN, user.getRole());
    }

    @Test
    void testToEntity_WithInstant() {
        String nowStr = "2026-04-20T10:00:00Z";
        Map<String, Object> data = Map.of(
            "title", "Event Title",
            "startAt", nowStr
        );

        // We'll use a class that has an Instant field. 
        // Event has startAt as Instant.
        com.carebridge.entities.Event event = mappingService.toEntity(data, com.carebridge.entities.Event.class);

        assertNotNull(event);
        assertEquals("Event Title", event.getTitle());
        assertEquals(Instant.parse(nowStr), event.getStartAt());
    }
    
    @Test
    void testToMap_NestedBaseEntity() {
        User creator = new User();
        creator.setId(99L);
        
        com.carebridge.entities.Event event = new com.carebridge.entities.Event();
        event.setId(1L);
        event.setTitle("My Event");
        event.setCreatedBy(creator);
        
        Map<String, Object> map = mappingService.toMap(event);
        
        assertEquals(1L, map.get("id"));
        assertEquals("My Event", map.get("title"));
        assertEquals(99L, map.get("createdBy" + "Id"), "Nested BaseEntity should be mapped to its ID");
        assertFalse(map.containsKey("createdBy"), "Full nested entity should not be in the map");
    }
}
