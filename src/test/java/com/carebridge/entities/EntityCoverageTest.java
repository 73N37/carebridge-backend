package com.carebridge.entities;

import com.carebridge.enums.EntryType;
import com.carebridge.enums.RiskAssessment;
import com.carebridge.enums.Role;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;

public class EntityCoverageTest {

    @Test
    void testEvent() {
        Event e = new Event();
        e.setTitle("Title");
        e.setDescription("Desc");
        e.setStartAt(Instant.now());
        e.setShowOnBoard(true);
        User u = new User();
        e.setCreatedBy(u);
        EventType et = new EventType();
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
        assertNull(e.getCreated_at());
        assertNull(e.getUpdated_at());

        Event e2 = new Event("T", "D", Instant.now(), false, u, et);
        assertEquals("T", e2.getTitle());
        
        e.prePersist();
        assertNotNull(e.getCreated_at());
        e.preUpdate();
        
        assertTrue(e.equals(e));
        assertFalse(e.equals(null));
        assertFalse(e.equals(new Object()));
        
        e.hashCode();
    }

    @Test
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
        assertNotNull(u.getPasswordHash());
        assertNull(u.getCreated_at());
        assertNull(u.getUpdated_at());

        Resident r = new Resident();
        u.addResident(r);
        assertTrue(u.getResidents().contains(r));
        
        u.prePersist();
        assertNotNull(u.getCreated_at());
        u.preUpdate();

        assertTrue(u.verifyPassword("pass"));
        u.addRole(Role.ADMIN);
        assertEquals(Role.ADMIN, u.getRole());
        u.removeRole("ADMIN");
        assertEquals(Role.USER, u.getRole());
    }

    @Test
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
        e.setCreatedAt(LocalDateTime.now());
        e.setUpdatedAt(LocalDateTime.now());

        assertEquals("T", e.getTitle());
        assertEquals("C", e.getContent());
        assertEquals(EntryType.DAILY, e.getEntryType());
        assertEquals(RiskAssessment.LOW, e.getRiskAssessment());
        assertEquals(u, e.getAuthor());
        assertEquals(j, e.getJournal());
        assertNotNull(e.getEditCloseTime());
        assertNotNull(e.getCreatedAt());
        assertNotNull(e.getUpdatedAt());

        JournalEntry e2 = new JournalEntry(u, "T2", "C2", RiskAssessment.HIGH, EntryType.INCIDENT);
        assertEquals("T2", e2.getTitle());
    }
}
