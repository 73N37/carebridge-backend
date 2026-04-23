package com.carebridge.dao;

import com.carebridge.dao.impl.UserDAO;
import com.carebridge.exceptions.ApiRuntimeException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.junit.jupiter.api.*;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class UserDAOCoverageTest {

    @Mock
    private EntityManager em;

    @Mock
    private TypedQuery<Object> query;

    @InjectMocks
    private UserDAO userDAO;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @Order(1)
    void testReadException() {
        when(em.createQuery(anyString(), any())).thenThrow(new RuntimeException("DB Error"));
        assertThrows(ApiRuntimeException.class, () -> userDAO.read(1L));
    }

    @Test
    @Order(2)
    void testReadByEmailException() {
        when(em.createQuery(anyString(), any())).thenThrow(new RuntimeException("DB Error"));
        assertThrows(ApiRuntimeException.class, () -> userDAO.readByEmail("test@test.com"));
    }

    @Test
    @Order(3)
    void testReadAllException() {
        when(em.createQuery(anyString(), any())).thenThrow(new RuntimeException("DB Error"));
        assertThrows(ApiRuntimeException.class, () -> userDAO.readAll());
    }
}
