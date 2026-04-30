package com.carebridge.crud.logic.core;

import com.carebridge.crud.data.core.BaseDAO;
import com.carebridge.crud.data.core.BaseEntity;
import com.carebridge.crud.logic.MappingService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * [API/LOGIC LAYER]
 * A generic controller handler that is auto-generated per entity annotated with @CrudResource.
 * Encapsulates all CRUD handler logic for one entity type, delegating persistence to BaseDAO.
 * The UniversalCrudController holds one instance per registered entity and dispatches
 * HTTP requests to it, making the entity the root of trust for the entire stack.
 *
 * <p>Entity-specific controllers may extend this class to inherit the standard CRUD
 * operations and add custom endpoints, while still benefiting from the auto-generated layer.
 */
public class BaseController<T extends BaseEntity> {

    protected final Class<T> entityClass;
    protected final BaseDAO<T> dao;
    protected final EntityManager em;
    protected final MappingService mappingService;
    protected final CrudInterceptor<T> interceptor;

    public BaseController(Class<T> entityClass,
                          BaseDAO<T> dao,
                          EntityManager em,
                          MappingService mappingService,
                          CrudInterceptor<T> interceptor) {
        this.entityClass = entityClass;
        this.dao = dao;
        this.em = em;
        this.mappingService = mappingService;
        this.interceptor = interceptor;
    }

    /**
     * Returns a paginated view of all entities.
     */
    public BaseService.Page<T> getAll(int page, int size) {
        TypedQuery<T> query = em.createQuery("FROM " + entityClass.getSimpleName(), entityClass);
        query.setFirstResult(page * size);
        query.setMaxResults(size);
        List<T> content = query.getResultList();
        long totalElements = em.createQuery(
                "SELECT count(e) FROM " + entityClass.getSimpleName() + " e", Long.class)
                .getSingleResult();
        return new BaseService.Page<>(content, totalElements);
    }

    /**
     * Returns a single entity by ID, or empty if not found.
     */
    public Optional<T> getById(Long id) {
        return dao.findById(id);
    }

    /**
     * Maps the request body to an entity instance without persisting it.
     * Useful when callers need the entity before interceptors fire.
     */
    public T toEntity(Map<String, Object> body) {
        return mappingService.toEntity(body, entityClass);
    }

    /**
     * Creates a new entity. Fires beforeCreate / afterCreate interceptors.
     * Throws RuntimeException when the body already carries an id.
     */
    @Transactional
    public T create(Map<String, Object> body) {
        T entity = mappingService.toEntity(body, entityClass);
        if (entity.getId() != null) {
            throw new RuntimeException("Entity already has an ID. Use update instead.");
        }
        interceptor.beforeCreate(entity);
        T saved = dao.create(entity);
        interceptor.afterCreate(saved);
        return saved;
    }

    /**
     * Updates an existing entity by ID. Fires beforeUpdate / afterUpdate interceptors.
     * Throws RuntimeException when the entity is not found.
     */
    @Transactional
    public T update(Long id, Map<String, Object> body) {
        T entity = mappingService.toEntity(body, entityClass);
        interceptor.beforeUpdate(entity);
        T updated = dao.update(id, entity);
        interceptor.afterUpdate(updated);
        return updated;
    }

    /**
     * Deletes an entity by ID. No-op when the entity does not exist.
     * Fires beforeDelete / afterDelete interceptors.
     */
    @Transactional
    public void delete(Long id) {
        interceptor.beforeDelete(id);
        dao.delete(id);
        interceptor.afterDelete(id);
    }

    public BaseDAO<T> getDao() {
        return dao;
    }

    public CrudInterceptor<T> getInterceptor() {
        return interceptor;
    }
}
