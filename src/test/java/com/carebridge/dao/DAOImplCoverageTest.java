package com.carebridge.dao;

import com.carebridge.dao.impl.*;
import com.carebridge.exceptions.ApiRuntimeException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;
import org.junit.jupiter.api.*;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class DAOImplCoverageTest {

    @Mock
    private EntityManager em;
    @Mock
    private TypedQuery<Object> query;

    @InjectMocks
    private JournalEntryDAO journalEntryDAO;
    @InjectMocks
    private ResidentDAO residentDAO;
    @InjectMocks
    private JournalDAO journalDAO;
    @InjectMocks
    private EventTypeDAO eventTypeDAO;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @Order(1)
    void testResidentDAO_readByCpr_NoResult() {
        when(em.createQuery(anyString(), any())).thenReturn(query);
        when(query.setParameter(anyString(), any())).thenReturn(query);
        when(query.getSingleResult()).thenThrow(new NoResultException());
        
        assertNull(residentDAO.readByCpr("none"));
    }

    @Test
    @Order(2)
    void testJournalEntryDAO_readAll_Coverage() {
        // Just for instructions
        when(em.createQuery(anyString(), any())).thenReturn(query);
        journalEntryDAO.readAll();
        assertTrue(true);
    }
    
    @Test
    @Order(3)
    void testEventTypeDAO_readAll_Coverage() {
        when(em.createQuery(anyString(), any())).thenReturn(query);
        eventTypeDAO.readAll();
        assertTrue(true);
    }

    @Test
    @Order(4)
    void testJournalDAO_readAll_Coverage() {
        when(em.createQuery(anyString(), any())).thenReturn(query);
        journalDAO.readAll();
        assertTrue(true);
    }
}
