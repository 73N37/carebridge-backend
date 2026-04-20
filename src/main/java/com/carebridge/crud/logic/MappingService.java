package com.carebridge.crud.logic;

import com.carebridge.crud.annotations.ExcludeFromDTO;
import com.carebridge.crud.data.core.BaseEntity;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.*;

/**
 * 🧠 THE DYNAMIC RECORD ENGINE 🧠
 * Responsible for auto-generating "Virtual Records" (Maps) from Entities.
 * This makes manual DTO files obsolete for standard CRUD operations.
 */
public class MappingService {

    private static final Logger log = LoggerFactory.getLogger(MappingService.class);
    private final ObjectMapper objectMapper;

    public MappingService() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    /**
     * Converts an entity to a Dynamic DTO (Map), respecting @ExcludeFromDTO.
     */
    public Map<String, Object> toMap(BaseEntity entity) {
        if (entity == null) return null;
        
        Map<String, Object> result = new LinkedHashMap<>();
        Class<?> current = entity.getClass();
        
        while (current != null && current != Object.class) {
            for (Field field : current.getDeclaredFields()) {
                // 🔒 Security: Skip fields marked with @ExcludeFromDTO
                if (field.isAnnotationPresent(ExcludeFromDTO.class)) {
                    continue;
                }
                
                // Avoid duplicating fields already found in subclasses
                if (result.containsKey(field.getName())) {
                    continue;
                }

                try {
                    field.setAccessible(true);
                    Object value = field.get(entity);
                    
                    if (value == null) {
                        result.put(field.getName(), null);
                        continue;
                    }

                    // 🛠️ SMART MAPPING FOR RELATIONSHIPS
                    if (value instanceof BaseEntity be) {
                        // 🛑 CRITICAL: Only provide the ID to avoid infinite recursion
                        result.put(field.getName() + "Id", be.getId());
                    } else if (value instanceof Collection<?> col) {
                        // 🛡️ Check if collection is initialized to avoid LazyInitializationException
                        try {
                            if (col != null && org.hibernate.Hibernate.isInitialized(col)) {
                                List<Object> ids = new ArrayList<>();
                                for (Object item : col) {
                                    if (item instanceof BaseEntity be) {
                                        ids.add(be.getId());
                                    }
                                }
                                if (!ids.isEmpty()) {
                                    result.put(field.getName() + "Ids", ids);
                                }
                            }
                        } catch (Exception e) {
                            log.warn("Lazy collection {} on {} could not be processed", field.getName(), entity.getClass().getSimpleName());
                        }
                    } else {
                        // Standard field (String, Long, Instant, etc.)
                        result.put(field.getName(), value);
                    }
                } catch (IllegalAccessException e) {
                    log.warn("Could not access field {} on {}", field.getName(), entity.getClass().getSimpleName());
                }
            }
            current = current.getSuperclass();
        }
        return result;
    }

    /**
     * Converts a list of entities to dynamic DTOs.
     */
    public List<Map<String, Object>> toMapList(List<? extends BaseEntity> entities) {
        if (entities == null) return Collections.emptyList();
        return entities.stream().map(this::toMap).toList();
    }

    /**
     * Converts a Map of data back into an Entity class.
     * This is used for creating/updating entities from dynamic JSON input.
     */
    public <T> T toEntity(Map<String, Object> data, Class<T> entityClass) {
        try {
            // First conversion to get a basic object
            T entity = objectMapper.convertValue(data, entityClass);
            
            // Second pass: Use reflection to fix fields that Jackson might have skipped or mis-mapped
            for (Field field : entityClass.getDeclaredFields()) {
                if (data.containsKey(field.getName())) {
                    Object value = data.get(field.getName());
                    if (value == null) continue;
                    
                    field.setAccessible(true);
                    
                    // Fix Instant
                    if (field.getType().equals(java.time.Instant.class) && value instanceof String s) {
                        field.set(entity, java.time.Instant.parse(s));
                    }
                    // Fix LocalDate
                    else if (field.getType().equals(java.time.LocalDate.class) && value instanceof String s) {
                        field.set(entity, java.time.LocalDate.parse(s));
                    }
                }
            }
            return entity;
        } catch (Exception e) {
            log.error("Failed to convert Map to entity {}", entityClass.getSimpleName(), e);
            // Last resort fallback
            return objectMapper.convertValue(data, entityClass);
        }
    }
}
