package com.carebridge.crud.logic;

import com.carebridge.crud.annotations.ExcludeFromDTO;
import com.carebridge.crud.data.core.BaseEntity;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * [LOGIC LAYER]
 * Decoupled mapping service responsible for Entity <-> DTO (Map) conversions.
 * Respects @ExcludeFromDTO to filter sensitive data.
 */
public class MappingService {

    private final ObjectMapper objectMapper;

    public MappingService() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    /**
     * Converts an entity to a Map, excluding fields marked with @ExcludeFromDTO.
     */
    public Map<String, Object> toMap(BaseEntity entity) {
        Map<String, Object> result = new HashMap<>();
        Class<?> current = entity.getClass();
        
        while (current != null && current != Object.class) {
            for (Field field : current.getDeclaredFields()) {
                // 🔒 @ExcludeFromDTO Check
                if (field.isAnnotationPresent(ExcludeFromDTO.class)) {
                    continue;
                }
                
                try {
                    field.setAccessible(true);
                    Object value = field.get(entity);
                    // Avoid recursion or deep mapping for now, just flat map
                    result.put(field.getName(), value);
                } catch (IllegalAccessException e) {
                    // Skip if inaccessible
                }
            }
            current = current.getSuperclass();
        }
        return result;
    }

    /**
     * Converts a Map of data back into an Entity class.
     */
    public <T> T toEntity(Map<String, Object> data, Class<T> entityClass) {
        return objectMapper.convertValue(data, entityClass);
    }
}
