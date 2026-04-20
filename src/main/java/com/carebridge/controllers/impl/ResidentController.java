package com.carebridge.controllers.impl;

import com.carebridge.controllers.IController;
import com.carebridge.dao.impl.ResidentDAO;
import com.carebridge.dao.impl.UserDAO;
import com.carebridge.entities.Journal;
import com.carebridge.entities.Resident;
import com.carebridge.entities.User;
import com.carebridge.crud.logic.MappingService;
import io.javalin.http.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class ResidentController implements IController<Resident, Long> {

    private static final Logger logger = LoggerFactory.getLogger(ResidentController.class);
    private final ResidentDAO residentDAO = ResidentDAO.getInstance();
    private final UserDAO userDAO = UserDAO.getInstance();
    private final MappingService mappingService = new MappingService();

    public ResidentController() {
    }

    public void create(Context ctx) {
        try {
            Map<String, Object> body = ctx.bodyAsClass(Map.class);
            Resident resident = mappingService.toEntity(body, Resident.class);

            if (resident.getFirstName() == null || resident.getFirstName().isBlank()) {
                throw new IllegalArgumentException("firstName is required");
            }
            if (resident.getLastName() == null || resident.getLastName().isBlank()) {
                throw new IllegalArgumentException("lastName is required");
            }

            // create single linked journal
            Journal journal = new Journal();
            resident.setJournal(journal);
            journal.setResident(resident);

            var tokenUser = ctx.attribute("user");
            String email = null;
            if (tokenUser instanceof Map<?, ?> ju) {
                email = (String) ju.get("username");
            }

            if (email != null) {
                User user = userDAO.readByEmail(email);
                if (user != null) {
                    resident.addUser(user);
                }
            }

            Resident created = residentDAO.create(resident);
            Map<String, Object> resp = mappingService.toMap(created);

            ctx.status(201);
            ctx.header("Location", "/api/residents/" + created.getId());
            ctx.json(resp);

        } catch (IllegalArgumentException e) {
            ctx.status(400).result(e.getMessage());
        } catch (Exception e) {
            logger.error("Failed to create resident", e);
            ctx.status(500).result("Internal server error");
        }
    }

    @Override
    public void delete(Context ctx) { throw new UnsupportedOperationException(); }

    @Override
    public boolean validatePrimaryKey(Long id) {
        return id != null && id > 0;
    }

    @Override
    public Resident validateEntity(Context ctx) { return null; }

    @Override
    public void read(Context ctx) { throw new UnsupportedOperationException(); }

    @Override
    public void readAll(Context ctx) { throw new UnsupportedOperationException(); }

    @Override
    public void update(Context ctx) { throw new UnsupportedOperationException(); }
}
