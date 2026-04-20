package com.carebridge.dao.impl;

import com.carebridge.dao.IDAO;
import com.carebridge.entities.EventType;
import com.carebridge.exceptions.ApiRuntimeException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public class EventTypeDAO implements IDAO<EventType, Long> {

    @PersistenceContext
    private EntityManager em;

    public EventTypeDAO() {
    }

    @Override
    public EventType read(Long id) {
        return em.find(EventType.class, id);
    }

    @Override
    public List<EventType> readAll() {
        return em.createQuery("FROM EventType", EventType.class).getResultList();
    }

    @Override
    @Transactional
    public EventType create(EventType type) {
        em.persist(type);
        return type;
    }

    @Override
    @Transactional
    public EventType update(Long id, EventType updated) {
        EventType existing = em.find(EventType.class, id);
        if (existing == null) throw new ApiRuntimeException(404, "EventType not found");
        if (updated.getName() != null) existing.setName(updated.getName());
        if (updated.getColorHex() != null) existing.setColorHex(updated.getColorHex());
        return existing;
    }

    @Override
    @Transactional
    public void delete(Long id) {
        EventType type = em.find(EventType.class, id);
        if (type != null) em.remove(type);
    }
}
