package com.carebridge.dao.impl;

import com.carebridge.dao.IDAO;
import com.carebridge.entities.Resident;
import com.carebridge.exceptions.ApiRuntimeException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public class ResidentDAO implements IDAO<Resident, Long> {

    private static final Logger logger = LoggerFactory.getLogger(ResidentDAO.class);

    @PersistenceContext
    private EntityManager em;

    public ResidentDAO() {
    }

    @Override
    @Transactional
    public Resident create(Resident resident) {
        if (resident == null) throw new ApiRuntimeException(400, "Resident cannot be null");
        if (resident.getFirstName() == null || resident.getFirstName().isBlank())
            throw new ApiRuntimeException(400, "firstName is required");
        if (resident.getLastName() == null || resident.getLastName().isBlank())
            throw new ApiRuntimeException(400, "lastName is required");
        if (resident.getCprNr() == null || resident.getCprNr().isBlank())
            throw new ApiRuntimeException(400, "cprNr is required");

        em.persist(resident);
        return resident;
    }

    @Override
    public Resident read(Long id) {
        Resident resident = em.find(Resident.class, id);
        if (resident == null) {
            throw new EntityNotFoundException("Resident not found with ID: " + id);
        }
        return resident;
    }

    @Override
    public List<Resident> readAll() {
        return em.createQuery("SELECT r FROM Resident r", Resident.class).getResultList();
    }

    @Override
    @Transactional
    public Resident update(Long id, Resident updated) {
        Resident managed = em.find(Resident.class, id);
        if (managed == null) {
            throw new ApiRuntimeException(404, "Resident not found with ID: " + id);
        }
        if (updated.getFirstName() != null && !updated.getFirstName().isBlank())
            managed.setFirstName(updated.getFirstName());
        if (updated.getLastName() != null && !updated.getLastName().isBlank())
            managed.setLastName(updated.getLastName());
        if (updated.getCprNr() != null && !updated.getCprNr().isBlank())
            managed.setCprNr(updated.getCprNr());

        return managed;
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Resident managed = em.find(Resident.class, id);
        if (managed == null) {
            throw new EntityNotFoundException("Resident not found with ID: " + id);
        }
        em.remove(managed);
    }
}
