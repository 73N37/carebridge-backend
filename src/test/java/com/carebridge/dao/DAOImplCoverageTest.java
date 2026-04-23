package com.carebridge.dao;

import com.carebridge.dao.impl.*;
import org.junit.jupiter.api.*;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class DAOImplCoverageTest {

    private JournalEntryDAO journalEntryDAO;
    private ResidentDAO residentDAO;
    private JournalDAO journalDAO;
    private EventTypeDAO eventTypeDAO;
    private ThrowingEntityManager em;

    @BeforeEach
    void setUp() {
        journalEntryDAO = new JournalEntryDAO();
        residentDAO = new ResidentDAO();
        journalDAO = new JournalDAO();
        eventTypeDAO = new EventTypeDAO();
        em = new ThrowingEntityManager();
        
        ReflectionTestUtils.setField(journalEntryDAO, "em", em);
        ReflectionTestUtils.setField(residentDAO, "em", em);
        ReflectionTestUtils.setField(journalDAO, "em", em);
        ReflectionTestUtils.setField(eventTypeDAO, "em", em);
    }

    @Test
    @Order(1)
    void testResidentDAO_readByCpr_Exception() {
        // Triggers the catch (Exception e) implicitly if any other exception occurs, 
        // but ResidentDAO specifically catches NoResultException to return null.
        // Wait, ResidentDAO.readByCpr has a try-catch for NoResultException.
        assertNull(residentDAO.readByCpr("none"));
    }

    @Test
    @Order(2)
    void testJournalEntryDAO_readAll_Coverage() {
        assertThrows(RuntimeException.class, () -> journalEntryDAO.readAll());
    }
    
    @Test
    @Order(3)
    void testEventTypeDAO_readAll_Coverage() {
        assertThrows(RuntimeException.class, () -> eventTypeDAO.readAll());
    }

    @Test
    @Order(4)
    void testJournalDAO_readAll_Coverage() {
        assertThrows(RuntimeException.class, () -> journalDAO.readAll());
    }
}
