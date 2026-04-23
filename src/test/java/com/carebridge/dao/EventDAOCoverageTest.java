package com.carebridge.dao;

import com.carebridge.dao.impl.EventDAO;
import com.carebridge.exceptions.ApiRuntimeException;
import org.junit.jupiter.api.*;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertThrows;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class EventDAOCoverageTest {

    private EventDAO eventDAO;
    private ThrowingEntityManager em;

    @BeforeEach
    void setUp() {
        eventDAO = new EventDAO();
        em = new ThrowingEntityManager();
        ReflectionTestUtils.setField(eventDAO, "em", em);
    }

    @Test
    @Order(1)
    void testReadException() {
        assertThrows(ApiRuntimeException.class, () -> eventDAO.read(1L));
    }

    @Test
    @Order(2)
    void testReadAllException() {
        assertThrows(ApiRuntimeException.class, () -> eventDAO.readAll());
    }

    @Test
    @Order(3)
    void testReadByCreatorException() {
        assertThrows(ApiRuntimeException.class, () -> eventDAO.readByCreator(1L));
    }

    @Test
    @Order(4)
    void testReadBetweenException() {
        assertThrows(ApiRuntimeException.class, () -> eventDAO.readBetween(Instant.now(), Instant.now()));
    }
}
