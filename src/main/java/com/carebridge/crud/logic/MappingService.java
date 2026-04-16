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
                        // For linked entities, we just provide the ID to avoid recursion
                        result.put(field.getName() + "Id", be.getId());
                    } else if (value instanceof Collection<?> col) {
                        // For collections, we provide a list of IDs or skip to avoid LazyInitializationException
                        // In a more advanced version, we could use @Children to decide
                        List<Object> ids = new ArrayList<>();
                        for (Object item : col) {
                            if (item instanceof BaseEntity be) {
                                ids.add(be.getId());
                            }
                        }
                        if (!ids.isEmpty()) {
                            result.put(field.getName() + "Ids", ids);
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
        return objectMapper.convertValue(data, entityClass);
    }
}
