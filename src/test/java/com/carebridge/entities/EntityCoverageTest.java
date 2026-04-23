package com.carebridge.entities;

import com.carebridge.enums.EntryType;
import com.carebridge.enums.RiskAssessment;
import com.carebridge.enums.Role;
import org.junit.jupiter.api.*;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class EntityCoverageTest {

    @Test
    @Order(1)
    void testEvent() {
        Event e = new Event();
        e.setTitle("Title");
        e.setDescription("Desc");
        e.setStartAt(Instant.now());
        e.setShowOnBoard(true);
        User u = new User();
        u.setId(1L);
        e.setCreatedBy(u);
        EventType et = new EventType();
        et.setId(1L);
        e.setEventType(et);
        e.setSeenByUsers(new HashSet<>());

        assertEquals("Title", e.getTitle());
        assertEquals("Desc", e.getDescription());
        assertNotNull(e.getStartAt());
        assertTrue(e.isShowOnBoard());
        assertEquals(u, e.getCreatedBy());
        assertEquals(et, e.getEventType());
        assertNotNull(e.getSeenByUsers());
        assertNotNull(e.getEventDate());
        assertNotNull(e.getEventTime());
        
        Event e2 = new Event("T", "D", Instant.now(), false, u, et);
        assertEquals("T", e2.getTitle());
        
        e.prePersist();
        assertNotNull(e.getCreated_at());
        e.preUpdate();
        
        // Equals and HashCode branches
        assertTrue(e.equals(e));
        assertFalse(e.equals(null));
        assertFalse(e.equals(new Object()));
        
        Event e3 = new Event();
        assertFalse(e.equals(e3));
        e.setId(1L);
        assertFalse(e.equals(e3));
        e3.setId(2L);
        assertFalse(e.equals(e3));
        e3.setId(1L);
        assertTrue(e.equals(e3));
        
        e.hashCode();
    }

    @Test
    @Order(2)
    void testUser() {
        User u = new User();
        u.setName("Name");
        u.setEmail("email");
        u.setPassword("pass");
        u.setRole(Role.USER);
        u.setDisplayEmail("de");
        u.setDisplayName("dn");
        u.setDisplayPhone("dp");
        u.setInternalEmail("ie");
        u.setInternalPhone("ip");
        u.setResidents(new ArrayList<>());

        assertEquals("Name", u.getName());
        assertEquals("email", u.getEmail());
        assertEquals(Role.USER, u.getRole());
        assertEquals("de", u.getDisplayEmail());
        assertEquals("dn", u.getDisplayName());
        assertEquals("dp", u.getDisplayPhone());
        assertEquals("ie", u.getInternalEmail());
        assertEquals("ip", u.getInternalPhone());
        assertNotNull(u.getResidents());
        
        Resident r = new Resident();
        r.setId(1L);
        u.addResident(r);
        assertTrue(u.getResidents().contains(r));
        u.addResident(r); // Duplicate branch
        u.addResident(null); // Null branch
        
        u.prePersist();
        assertNotNull(u.getCreated_at());
        u.preUpdate();

        assertTrue(u.verifyPassword("pass"));
        assertFalse(u.verifyPassword("wrong"));
        u.addRole(Role.ADMIN);
        assertEquals(Role.ADMIN, u.getRole());
        u.removeRole("ADMIN");
        assertEquals(Role.USER, u.getRole());
        
        u.addRole(Role.CAREWORKER);
        u.addRole(Role.ADMIN);
        assertTrue(u.getRole().name().contains("ADMIN"));
    }

    @Test
    @Order(3)
    void testResident() {
        Resident r = new Resident();
        r.setFirstName("F");
        r.setLastName("L");
        r.setCprNr("C");
        r.setJournal(new Journal());
        r.setUsers(new HashSet<>());

        assertEquals("F", r.getFirstName());
        assertEquals("L", r.getLastName());
        assertEquals("C", r.getCprNr());
        assertNotNull(r.getJournal());
        assertNotNull(r.getUsers());

        User u = new User();
        r.addUser(u);
        assertTrue(r.getUsers().contains(u));
        r.removeUser(u);
        assertFalse(r.getUsers().contains(u));
    }

    @Test
    @Order(4)
    void testJournal() {
        Journal j = new Journal();
        Resident r = new Resident();
        j.setResident(r);
        j.setEntries(new ArrayList<>());

        assertEquals(r, j.getResident());
        assertNotNull(j.getEntries());
        
        JournalEntry entry = new JournalEntry();
        j.addEntry(entry);
        assertTrue(j.getEntries().contains(entry));
    }

    @Test
    @Order(5)
    void testJournalEntry() {
        JournalEntry e = new JournalEntry();
        e.setTitle("T");
        e.setContent("C");
        e.setEntryType(EntryType.DAILY);
        e.setRiskAssessment(RiskAssessment.LOW);
        User u = new User();
        e.setAuthor(u);
        Journal j = new Journal();
        e.setJournal(j);
        e.setEditCloseTime(LocalDateTime.now());

        assertEquals("T", e.getTitle());
        assertEquals("C", e.getContent());
        assertEquals(EntryType.DAILY, e.getEntryType());
        assertEquals(RiskAssessment.LOW, e.getRiskAssessment());
        assertEquals(u, e.getAuthor());
        assertEquals(j, e.getJournal());

        JournalEntry e2 = new JournalEntry(u, "T2", "C2", RiskAssessment.HIGH, EntryType.INCIDENT);
        assertEquals("T2", e2.getTitle());
    }

    @Test
    @Order(6)
    void testEventType() {
        EventType et = new EventType("N", "#000");
        et.setId(1L);
        assertEquals("N", et.getName());
        assertEquals("#000", et.getColorHex());
        
        EventType et2 = new EventType();
        et2.setName("N");
        et2.setColorHex("#000");
        et2.setId(1L);
        
        assertTrue(et.equals(et2));
        assertTrue(et.equals(et));
        assertFalse(et.equals(null));
        assertFalse(et.equals(new Object()));
        
        et2.setId(2L);
        assertFalse(et.equals(et2));
        
        et.hashCode();
    }
}
