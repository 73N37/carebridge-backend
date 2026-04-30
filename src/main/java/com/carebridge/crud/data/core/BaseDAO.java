package com.carebridge.crud.data.core;

import com.carebridge.dao.IDAO;
import jakarta.persistence.EntityManager;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * [DATA LAYER]
 * A generic DAO that is auto-generated per entity annotated with @CrudResource.
 * Implements IDAO<T, Long> using EntityManager directly.
 * Entity-specific DAOs may extend this class to inherit generic CRUD behavior
 * and override methods to add entity-specific validation or queries.
 */
public class BaseDAO<T extends BaseEntity> implements IDAO<T, Long> {

    protected final Class<T> entityClass;
    protected final EntityManager em;

    public BaseDAO(Class<T> entityClass, EntityManager em) {
        this.entityClass = entityClass;
        this.em = em;
    }

    @Override
    public T read(Long id) {
        return em.find(entityClass, id);
    }

    public Optional<T> findById(Long id) {
        return Optional.ofNullable(em.find(entityClass, id));
    }

    @Override
    public List<T> readAll() {
        return em.createQuery("FROM " + entityClass.getSimpleName(), entityClass).getResultList();
    }

    @Override
    @Transactional
    public T create(T entity) {
        em.persist(entity);
        return entity;
    }

    @Override
    @Transactional
    public T update(Long id, T entity) {
        T existing = em.find(entityClass, id);
        if (existing == null) {
            throw new RuntimeException(
                    "Entity of type " + entityClass.getSimpleName() + " not found with id: " + id);
        }
        entity.setId(id);
        return em.merge(entity);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        T entity = em.find(entityClass, id);
        if (entity != null) {
            em.remove(entity);
        }
    }

    public Class<T> getEntityClass() {
        return entityClass;
    }
}
