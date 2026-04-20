package com.carebridge.controllers.impl;

import com.carebridge.dao.impl.*;
import com.carebridge.entities.JournalEntry;
import com.carebridge.entities.Journal;
import com.carebridge.entities.User;
import com.carebridge.exceptions.ApiRuntimeException;
import com.carebridge.crud.logic.MappingService;
import com.carebridge.crud.annotations.DynamicDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/journals/{journalId}/journal-entries")
public class JournalEntryController {

    private static final Logger logger = LoggerFactory.getLogger(JournalEntryController.class);
    private final JournalEntryDAO journalEntryDAO;
    private final JournalDAO journalDAO;
    private final UserDAO userDAO;
    private final MappingService mappingService;

    public JournalEntryController(JournalEntryDAO journalEntryDAO, JournalDAO journalDAO, UserDAO userDAO, MappingService mappingService) {
        this.journalEntryDAO = journalEntryDAO;
        this.journalDAO = journalDAO;
        this.userDAO = userDAO;
        this.mappingService = mappingService;
    }

    @GetMapping
    public List<Long> findAllEntriesByJournal(@PathVariable Long journalId) {
        return journalEntryDAO.getEntryIdsByJournalId(journalId);
    }

    @PostMapping
    @DynamicDTO
    public ResponseEntity<JournalEntry> create(
            @PathVariable Long journalId,
            @RequestBody Map<String, Object> body,
            @RequestAttribute("user") Map<String, Object> jwtUser) {
        
        JournalEntry entry = mappingService.toEntity(body, JournalEntry.class);
        String email = (String) jwtUser.get("username");

        User author = userDAO.readByEmail(email);
        if (author == null) throw new ApiRuntimeException(401, "Author not found");

        Journal journal = journalDAO.read(journalId);
        if (journal == null) throw new ApiRuntimeException(404, "Journal not found");

        if (entry.getTitle() == null || entry.getTitle().isBlank()) throw new ApiRuntimeException(400, "Title is required");
        if (entry.getContent() == null || entry.getContent().isBlank()) throw new ApiRuntimeException(400, "Content is required");

        entry.setJournal(journal);
        entry.setAuthor(author);

        LocalDateTime now = LocalDateTime.now();
        entry.setCreatedAt(now);
        entry.setUpdatedAt(now);
        entry.setEditCloseTime(now.plusHours(24));

        journalEntryDAO.create(entry);
        journalDAO.addEntryToJournal(journal, entry);

        return ResponseEntity.status(HttpStatus.CREATED).body(entry);
    }

    @PutMapping("/{entryId}")
    @DynamicDTO
    public ResponseEntity<JournalEntry> update(
            @PathVariable Long journalId,
            @PathVariable Long entryId,
            @RequestBody Map<String, Object> body) {
        
        JournalEntry entry = journalEntryDAO.read(entryId);
        if (entry == null) throw new ApiRuntimeException(404, "Journal entry not found");

        if (entry.getJournal() == null || !entry.getJournal().getId().equals(journalId)) {
            throw new ApiRuntimeException(400, "Entry does not belong to specified journal.");
        }

        LocalDateTime now = LocalDateTime.now();
        if (entry.getEditCloseTime() == null || now.isAfter(entry.getEditCloseTime())) {
            throw new ApiRuntimeException(400, "Edit window has closed.");
        }

        if (body.containsKey("content")) {
            entry.setContent((String) body.get("content"));
        }
        entry.setUpdatedAt(now);

        journalEntryDAO.update(entryId, entry);
        return ResponseEntity.ok(entry);
    }

    @GetMapping("/{entryId}")
    @DynamicDTO
    public ResponseEntity<JournalEntry> read(
            @PathVariable Long journalId,
            @PathVariable Long entryId) {
        
        JournalEntry entry = journalEntryDAO.read(entryId);
        if (entry == null) return ResponseEntity.notFound().build();
        
        if (entry.getJournal() == null || !entry.getJournal().getId().equals(journalId)) {
            return ResponseEntity.badRequest().build();
        }
        
        return ResponseEntity.ok(entry);
    }
}
