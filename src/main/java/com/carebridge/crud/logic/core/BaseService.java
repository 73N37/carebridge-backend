package com.carebridge.crud.logic.core;

import com.carebridge.crud.data.core.BaseEntity;
import com.carebridge.crud.data.core.GenericRepository;

import java.util.List;
import java.util.Optional;

/**
 * [LOGIC LAYER]
 * A generic service that provides standard CRUD operations.
 */
public abstract class BaseService<T extends BaseEntity> {
    
    protected abstract GenericRepository<T> getRepository();

    public List<T> findAll() {
        return getRepository().findAll();
    }

    public Page<T> findAll(int page, int size) {
        List<T> content = getRepository().findAll(page * size, size);
        long totalElements = getRepository().count();
        return new Page<>(content, totalElements);
    }

    public Optional<T> findById(Long id) {
        return getRepository().findById(id);
    }

    public T save(T entity) {
        return getRepository().save(entity);
    }

    public T update(Long id, T entity) {
        if (!getRepository().existsById(id)) {
            throw new RuntimeException("Entity not found with id: " + id);
        }
        entity.setId(id);
        return getRepository().save(entity);
    }

    public void deleteById(Long id) {
        getRepository().deleteById(id);
    }

    public static class Page<T> {
        private final List<T> content;
        private final long totalElements;

        public Page(List<T> content, long totalElements) {
            this.content = content;
            this.totalElements = totalElements;
        }

        public List<T> getContent() { return content; }
        public long getTotalElements() { return totalElements; }
    }
}
