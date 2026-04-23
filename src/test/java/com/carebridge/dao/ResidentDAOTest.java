package com.carebridge.dao;

import com.carebridge.CareBridgeApplication;
import com.carebridge.dao.impl.ResidentDAO;
import com.carebridge.entities.Resident;
import com.carebridge.exceptions.ApiRuntimeException;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = CareBridgeApplication.class)
@ActiveProfiles("test")
@Transactional
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ResidentDAOTest {

    @Autowired
    private ResidentDAO residentDAO;

    private Long createdId;
    private String cpr;

    @BeforeEach
    void setUp() {
        Resident resident = new Resident();
        resident.setFirstName("John");
        resident.setLastName("Doe");
        cpr = "RES" + System.nanoTime();
        resident.setCprNr(cpr);
        Resident created = residentDAO.create(resident);
        createdId = created.getId();
    }

    @Test
    @Order(1)
    void testCreateAndRead() {
        assertNotNull(createdId);
        Resident read = residentDAO.read(createdId);
        assertEquals("John", read.getFirstName());
    }

    @Test
    @Order(2)
    void testReadByCpr() {
        assertNotNull(residentDAO.readByCpr(cpr));
        assertNull(residentDAO.readByCpr("NONEXISTENT"));
    }

    @Test
    @Order(3)
    void testReadAll() {
        List<Resident> all = residentDAO.readAll();
        assertFalse(all.isEmpty());
    }

    @Test
    @Order(4)
    void testUpdate() {
        Resident patch = new Resident();
        patch.setFirstName("Bobby");
        patch.setLastName("Jones");
        patch.setCprNr("NEW" + System.nanoTime());
        Resident updated = residentDAO.update(createdId, patch);
        assertEquals("Bobby", updated.getFirstName());
        assertEquals("Jones", updated.getLastName());
    }

    @Test
    @Order(5)
    void testErrors() {
        assertThrows(ApiRuntimeException.class, () -> residentDAO.create(null));
        assertThrows(Exception.class, () -> residentDAO.read(999999L));
        assertThrows(Exception.class, () -> residentDAO.update(999999L, new Resident()));
        assertThrows(Exception.class, () -> residentDAO.delete(999999L));
    }

    @Test
    @Order(6)
    void testDelete() {
        residentDAO.delete(createdId);
        assertThrows(Exception.class, () -> residentDAO.read(createdId));
    }
}
