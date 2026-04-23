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
import org.junit.jupiter.api.BeforeEach;
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
public class JournalDAOTest {

    @Autowired
    private JournalDAO journalDAO;
    @Autowired
    private JournalEntryDAO entryDAO;
    @Autowired
    private ResidentDAO residentDAO;
    @Autowired
    private UserDAO userDAO;

    private User testUser;
    private Resident testResident;

    @BeforeEach
    void setUp() {
        testUser = new User("Journal User", "juser" + System.nanoTime() + "@test.com", "pass", Role.USER);
        userDAO.create(testUser);

        testResident = new Resident();
        testResident.setFirstName("Resident");
        testResident.setLastName("One");
        testResident.setCprNr("123" + System.nanoTime());
        residentDAO.create(testResident);
    }

    @Test
    void testJournalCRUD() {
        Journal journal = new Journal();
        journal.setResident(testResident);
        
        Journal created = journalDAO.create(journal);
        assertNotNull(created.getId());
        
        Journal read = journalDAO.read(created.getId());
        assertNotNull(read);
        
        List<Journal> all = journalDAO.readAll();
        assertFalse(all.isEmpty());
        
        Journal updated = journalDAO.update(created.getId(), new Journal());
        assertNotNull(updated);
        
        assertThrows(ApiRuntimeException.class, () -> journalDAO.update(9999L, new Journal()));
        
        journalDAO.delete(created.getId());
        assertNull(journalDAO.read(created.getId()));
    }

    @Test
    void testJournalEntryCRUD() {
        Journal journal = new Journal();
        journal.setResident(testResident);
        journalDAO.create(journal);

        JournalEntry entry = new JournalEntry(testUser, "Entry Title", "Content", RiskAssessment.LOW, EntryType.DAILY);
        entry.setJournal(journal);
        
        JournalEntry created = entryDAO.create(entry);
        assertNotNull(created.getId());
        
        JournalEntry read = entryDAO.read(created.getId());
        assertEquals("Entry Title", read.getTitle());
        
        JournalEntry patch = new JournalEntry();
        patch.setTitle("New Title");
        JournalEntry updated = entryDAO.update(created.getId(), patch);
        assertEquals("New Title", updated.getTitle());
        assertEquals("Content", updated.getContent());
        
        assertThrows(ApiRuntimeException.class, () -> entryDAO.update(9999L, new JournalEntry()));
        
        List<Long> ids = entryDAO.getEntryIdsByJournalId(journal.getId());
        assertTrue(ids.contains(created.getId()));
        
        entryDAO.delete(created.getId());
        assertNull(entryDAO.read(created.getId()));
    }

    @Test
    void testAddEntryToJournal() {
        Journal journal = new Journal();
        journal.setResident(testResident);
        journalDAO.create(journal);

        JournalEntry entry = new JournalEntry(testUser, "Title", "Content", RiskAssessment.LOW, EntryType.DAILY);
        entryDAO.create(entry);

        journalDAO.addEntryToJournal(journal, entry);
        
        Journal read = journalDAO.read(journal.getId());
        assertFalse(read.getEntries().isEmpty());
    }
}
