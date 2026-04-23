package dao;

import com.carebridge.CareBridgeApplication;
import com.carebridge.dao.impl.ResidentDAO;
import com.carebridge.entities.Resident;
import com.carebridge.exceptions.ApiRuntimeException;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.orm.jpa.JpaObjectRetrievalFailureException;
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

    private static Long createdId;

    @Test
    @Order(1)
    void testCreateAndRead() {
        Resident resident = new Resident();
        resident.setFirstName("John");
        resident.setLastName("Doe");
        resident.setCprNr("123" + System.nanoTime());

        Resident created = residentDAO.create(resident);
        assertNotNull(created.getId());
        createdId = created.getId();

        Resident read = residentDAO.read(createdId);
        assertEquals("John", read.getFirstName());
    }

    @Test
    @Order(2)
    void testReadAll() {
        List<Resident> all = residentDAO.readAll();
        assertFalse(all.isEmpty());
    }

    @Test
    @Order(3)
    void testUpdate() {
        Resident patch = new Resident();
        patch.setFirstName("Bobby");
        Resident updated = residentDAO.update(createdId, patch);
        assertEquals("Bobby", updated.getFirstName());
    }

    @Test
    @Order(4)
    void testErrors() {
        assertThrows(ApiRuntimeException.class, () -> residentDAO.create(null));
        assertThrows(JpaObjectRetrievalFailureException.class, () -> residentDAO.read(999999L));
        assertThrows(ApiRuntimeException.class, () -> residentDAO.update(999999L, new Resident()));
        assertThrows(JpaObjectRetrievalFailureException.class, () -> residentDAO.delete(999999L));
    }

    @Test
    @Order(5)
    void testDelete() {
        residentDAO.delete(createdId);
        assertThrows(JpaObjectRetrievalFailureException.class, () -> residentDAO.read(createdId));
    }
}
