package com.carebridge.controllers.impl;

import com.carebridge.controllers.IController;
import com.carebridge.dao.impl.*;
import com.carebridge.entities.JournalEntry;
import com.carebridge.entities.Journal;
import com.carebridge.entities.User;
import com.carebridge.exceptions.ApiRuntimeException;
import com.carebridge.crud.logic.MappingService;
import io.javalin.http.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class JournalEntryController implements IController<JournalEntry, Long>
{

    private static final Logger logger = LoggerFactory.getLogger(JournalEntryController.class);
    private final JournalEntryDAO journalEntryDAO = JournalEntryDAO.getInstance();
    private final JournalDAO journalDAO = JournalDAO.getInstance();
    private final UserDAO userDAO = UserDAO.getInstance();
    private final MappingService mappingService = new MappingService();

    public void findAllEntriesByJournal(Context ctx) {
        try {
            Long journalId = Long.parseLong(ctx.pathParam("journalId"));
            List<Long> ids = journalEntryDAO.getEntryIdsByJournalId(journalId);
            ctx.json(ids);
        } catch (Exception e) {
            logger.error("Error", e);
            ctx.status(500).result("Internal server error");
        }
    }

    public void create(Context ctx) {
        try {
            Long journalId = Long.parseLong(ctx.pathParam("journalId"));
            Map<String, Object> body = ctx.bodyAsClass(Map.class);
            JournalEntry entry = mappingService.toEntity(body, JournalEntry.class);

            var tokenUser = ctx.attribute("user");
            String email = null;
            if (tokenUser instanceof Map<?, ?> ju) {
                email = (String) ju.get("username");
            }

            if (email == null) throw new ApiRuntimeException(401, "Could not find user from token");

            User author = userDAO.readByEmail(email);
            if (author == null) {
                ctx.status(401).json("{\"msg\":\"Author not found\"}");
                return;
            }

            Journal journal = journalDAO.read(journalId);
            if (journal == null) {
                throw new IllegalArgumentException("Journal not found with ID: " + journalId);
            }

            if (entry.getTitle() == null || entry.getTitle().isBlank()) {
                throw new IllegalArgumentException("Title is required.");
            }
            if (entry.getContent() == null || entry.getContent().isBlank()) {
                throw new IllegalArgumentException("Content is required.");
            }

            entry.setJournal(journal);
            entry.setAuthor(author);

            LocalDateTime now = LocalDateTime.now();
            entry.setCreatedAt(now);
            entry.setUpdatedAt(now);
            entry.setEditCloseTime(now.plusHours(24));

            journalEntryDAO.create(entry);
            ctx.status(201).json(mappingService.toMap(entry));

            if (ctx.status().getCode() == 201) {
                journalDAO.addEntryToJournal(journal, entry);
            }

        } catch (IllegalArgumentException e) {
            ctx.status(400).result(e.getMessage());
        } catch (Exception e) {
            logger.error("Error", e);
            ctx.status(500).result("Internal server error");
        }
    }

    public void update(Context ctx) {
        try {
            Long journalId = Long.parseLong(ctx.pathParam("journalId"));
            Long entryId = Long.parseLong(ctx.pathParam("entryId"));
            Map<String, Object> body = ctx.bodyAsClass(Map.class);

            Journal journal = journalDAO.read(journalId);
            if (journal == null) throw new IllegalArgumentException("Journal not found");

            JournalEntry entry = journalEntryDAO.read(entryId);
            if (entry == null) throw new IllegalArgumentException("Journal entry not found");

            if (entry.getJournal() == null || !entry.getJournal().getId().equals(journalId)) {
                throw new IllegalArgumentException("Entry does not belong to specified journal.");
            }

            LocalDateTime now = LocalDateTime.now();
            if (entry.getEditCloseTime() == null || now.isAfter(entry.getEditCloseTime())) {
                throw new IllegalArgumentException("Edit window has closed.");
            }

            if (body.containsKey("content")) {
                entry.setContent((String) body.get("content"));
            }
            entry.setUpdatedAt(now);

            journalEntryDAO.update(entryId, entry);
            ctx.status(200).json(mappingService.toMap(entry));

        } catch (IllegalArgumentException e) {
            ctx.status(400).result(e.getMessage());
        } catch (Exception e) {
            logger.error("Error", e);
            ctx.status(500).result("Internal server error");
        }
    }

    @Override
    public void delete(Context ctx) {}

    @Override
    public boolean validatePrimaryKey(Long id) {
        return id != null && id > 0;
    }

    @Override
    public JournalEntry validateEntity(Context ctx) { return null; }

    public void read(Context ctx) {
        try {
            Long entryId = Long.parseLong(ctx.pathParam("entryId"));
            JournalEntry entry = journalEntryDAO.read(entryId);
            if (entry == null) throw new IllegalArgumentException("Journal entry not found");
            ctx.json(mappingService.toMap(entry));
        } catch (Exception e) {
            logger.error("Error", e);
            ctx.status(500).result("Internal server error");
        }
    }

    @Override
    public void readAll(Context ctx) {}
}
