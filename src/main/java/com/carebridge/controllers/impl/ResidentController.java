package com.carebridge.controllers.impl;

import com.carebridge.dao.impl.ResidentDAO;
import com.carebridge.dao.impl.UserDAO;
import com.carebridge.entities.Journal;
import com.carebridge.entities.Resident;
import com.carebridge.entities.User;
import com.carebridge.crud.logic.MappingService;
import com.carebridge.crud.annotations.DynamicDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/residents")
public class ResidentController {

    private static final Logger logger = LoggerFactory.getLogger(ResidentController.class);
    private final ResidentDAO residentDAO;
    private final UserDAO userDAO;
    private final MappingService mappingService;

    public ResidentController(ResidentDAO residentDAO, UserDAO userDAO, MappingService mappingService) {
        this.residentDAO = residentDAO;
        this.userDAO = userDAO;
        this.mappingService = mappingService;
    }

    @PostMapping("/create")
    @DynamicDTO
    public ResponseEntity<Resident> create(
            @RequestBody Map<String, Object> body,
            @RequestAttribute(value = "user", required = false) Map<String, Object> jwtUser) {
        
        try {
            Resident resident = mappingService.toEntity(body, Resident.class);

            if (resident.getFirstName() == null || resident.getFirstName().isBlank()) {
                throw new IllegalArgumentException("firstName is required");
            }
            if (resident.getLastName() == null || resident.getLastName().isBlank()) {
                throw new IllegalArgumentException("lastName is required");
            }

            Journal journal = new Journal();
            resident.setJournal(journal);
            journal.setResident(resident);

            if (jwtUser != null) {
                String email = (String) jwtUser.get("username");
                User user = userDAO.readByEmail(email);
                if (user != null) {
                    resident.addUser(user);
                }
            }

            Resident created = residentDAO.create(resident);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .header("Location", "/api/residents/" + created.getId())
                    .body(created);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
