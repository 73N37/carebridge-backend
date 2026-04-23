package com.carebridge.dao;

import com.carebridge.dao.impl.EventDAO;
import com.carebridge.exceptions.ApiRuntimeException;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.*;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class EventDAOCoverageTest {

    @Mock
    private EntityManager em;

    @InjectMocks
    private EventDAO eventDAO;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @Order(1)
    void testReadException() {
        when(em.createQuery(anyString(), any())).thenThrow(new RuntimeException("Error"));
        assertThrows(ApiRuntimeException.class, () -> eventDAO.read(1L));
    }

    @Test
    @Order(2)
    void testReadAllException() {
        when(em.createQuery(anyString(), any())).thenThrow(new RuntimeException("Error"));
        assertThrows(ApiRuntimeException.class, () -> eventDAO.readAll());
    }

    @Test
    @Order(3)
    void testReadByCreatorException() {
        when(em.createQuery(anyString(), any())).thenThrow(new RuntimeException("Error"));
        assertThrows(ApiRuntimeException.class, () -> eventDAO.readByCreator(1L));
    }

    @Test
    @Order(4)
    void testReadBetweenException() {
        when(em.createQuery(anyString(), any())).thenThrow(new RuntimeException("Error"));
        assertThrows(ApiRuntimeException.class, () -> eventDAO.readBetween(Instant.now(), Instant.now()));
    }
}
