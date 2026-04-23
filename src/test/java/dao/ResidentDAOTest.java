package dao;

import com.carebridge.CareBridgeApplication;
import com.carebridge.dao.impl.ResidentDAO;
import com.carebridge.entities.Resident;
import com.carebridge.exceptions.ApiRuntimeException;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = CareBridgeApplication.class)
@ActiveProfiles("test")
@Transactional
public class ResidentDAOTest {

    @Autowired
    private ResidentDAO residentDAO;

    @Test
    void testCreateAndRead() {
        Resident resident = new Resident();
        resident.setFirstName("John");
        resident.setLastName("Doe");
        resident.setCprNr("1234567890");

        Resident created = residentDAO.create(resident);
        assertNotNull(created.getId());

        Resident read = residentDAO.read(created.getId());
        assertEquals("John", read.getFirstName());
    }

    @Test
    void testReadAll() {
        Resident resident = new Resident();
        resident.setFirstName("Alice");
        resident.setLastName("Smith");
        resident.setCprNr("1111111111");
        residentDAO.create(resident);

        List<Resident> all = residentDAO.readAll();
        assertFalse(all.isEmpty());
    }

    @Test
    void testUpdate() {
        Resident resident = new Resident();
        resident.setFirstName("Bob");
        resident.setLastName("Jones");
        resident.setCprNr("2222222222");
        Resident created = residentDAO.create(resident);

        Resident patch = new Resident();
        patch.setFirstName("Bobby");
        Resident updated = residentDAO.update(created.getId(), patch);
        assertEquals("Bobby", updated.getFirstName());
        assertEquals("Jones", updated.getLastName());
    }

    @Test
    void testDelete() {
        Resident resident = new Resident();
        resident.setFirstName("Charlie");
        resident.setLastName("Brown");
        resident.setCprNr("3333333333");
        Resident created = residentDAO.create(resident);

        residentDAO.delete(created.getId());
        assertThrows(EntityNotFoundException.class, () -> residentDAO.read(created.getId()));
    }

    @Test
    void testErrorCases() {
        assertThrows(ApiRuntimeException.class, () -> residentDAO.create(null));
        assertThrows(ApiRuntimeException.class, () -> residentDAO.create(new Resident())); // Missing fields
        assertThrows(EntityNotFoundException.class, () -> residentDAO.read(999L));
        assertThrows(ApiRuntimeException.class, () -> residentDAO.update(999L, new Resident()));
        assertThrows(EntityNotFoundException.class, () -> residentDAO.delete(999L));
    }
}
