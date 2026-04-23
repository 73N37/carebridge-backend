package dao;

import com.carebridge.CareBridgeApplication;
import com.carebridge.dao.impl.JournalDAO;
import com.carebridge.dao.impl.JournalEntryDAO;
import com.carebridge.dao.impl.ResidentDAO;
import com.carebridge.dao.impl.UserDAO;
import com.carebridge.entities.Journal;
import com.carebridge.entities.JournalEntry;
import com.carebridge.entities.Resident;
import com.carebridge.entities.User;
import com.carebridge.enums.EntryType;
import com.carebridge.enums.RiskAssessment;
import com.carebridge.enums.Role;
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
public class JournalDAOTest {

    @Autowired
    private JournalDAO journalDAO;
    @Autowired
    private JournalEntryDAO entryDAO;
    @Autowired
    private ResidentDAO residentDAO;
    @Autowired
    private UserDAO userDAO;

    private static User testUser;
    private static Resident testResident;
    private static Long journalId;
    private static Long entryId;

    @BeforeAll
    public static void setup(@Autowired UserDAO uDAO, @Autowired ResidentDAO rDAO) {
        testUser = new User("Journal User", "juser" + System.nanoTime() + "@test.com", "pass", Role.USER);
        uDAO.create(testUser);

        testResident = new Resident();
        testResident.setFirstName("Resident");
        testResident.setLastName("One");
        testResident.setCprNr("123" + System.nanoTime());
        rDAO.create(testResident);
    }

    @Test
    @Order(1)
    void testCreateJournal() {
        Journal journal = new Journal();
        journal.setResident(testResident);
        Journal created = journalDAO.create(journal);
        assertNotNull(created.getId());
        journalId = created.getId();
    }

    @Test
    @Order(2)
    void testReadJournal() {
        assertNotNull(journalDAO.read(journalId));
        assertFalse(journalDAO.readAll().isEmpty());
    }

    @Test
    @Order(3)
    void testCreateEntry() {
        JournalEntry entry = new JournalEntry(testUser, "Title", "Content", RiskAssessment.LOW, EntryType.DAILY);
        Journal j = new Journal(); j.setId(journalId);
        entry.setJournal(j);
        
        JournalEntry created = entryDAO.create(entry);
        assertNotNull(created.getId());
        entryId = created.getId();
    }

    @Test
    @Order(4)
    void testUpdateEntry() {
        JournalEntry patch = new JournalEntry();
        patch.setTitle("Updated");
        JournalEntry updated = entryDAO.update(entryId, patch);
        assertEquals("Updated", updated.getTitle());
    }

    @Test
    @Order(5)
    void testGetEntryIds() {
        List<Long> ids = entryDAO.getEntryIdsByJournalId(journalId);
        assertTrue(ids.contains(entryId));
    }

    @Test
    @Order(6)
    void testAddEntryToJournal() {
        Journal j = journalDAO.read(journalId);
        JournalEntry e = entryDAO.read(entryId);
        journalDAO.addEntryToJournal(j, e);
        
        Journal read = journalDAO.read(journalId);
        assertFalse(read.getEntries().isEmpty());
    }

    @Test
    @Order(7)
    void testErrors() {
        assertThrows(ApiRuntimeException.class, () -> journalDAO.update(9999L, new Journal()));
        assertThrows(ApiRuntimeException.class, () -> entryDAO.update(9999L, new JournalEntry()));
    }

    @Test
    @Order(8)
    void testDelete() {
        entryDAO.delete(entryId);
        assertNull(entryDAO.read(entryId));
        journalDAO.delete(journalId);
        assertNull(journalDAO.read(journalId));
    }
}
