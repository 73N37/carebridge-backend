package com.carebridge.controllers.impl;

import com.carebridge.dao.impl.EventTypeDAO;
import com.carebridge.entities.EventType;
import com.carebridge.crud.logic.MappingService;
import com.carebridge.crud.annotations.DynamicDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/event-types")
public class EventTypeController {

    private static final Logger logger = LoggerFactory.getLogger(EventTypeController.class);
    private final EventTypeDAO eventTypeDAO;
    private final MappingService mappingService;

    public EventTypeController(EventTypeDAO eventTypeDAO, MappingService mappingService) {
        this.eventTypeDAO = eventTypeDAO;
        this.mappingService = mappingService;
    }

    @GetMapping("/{id}")
    @DynamicDTO
    public ResponseEntity<EventType> read(@PathVariable Long id) {
        var entity = eventTypeDAO.read(id);
        if (entity == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(entity);
    }

    @GetMapping
    @DynamicDTO
    public List<EventType> readAll() {
        return eventTypeDAO.readAll();
    }

    @PostMapping
    @DynamicDTO
    public ResponseEntity<EventType> create(@RequestBody Map<String, Object> body) {
        var entity = mappingService.toEntity(body, EventType.class);
        var created = eventTypeDAO.create(entity);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    @DynamicDTO
    public ResponseEntity<EventType> update(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        var patch = mappingService.toEntity(body, EventType.class);
        var updated = eventTypeDAO.update(id, patch);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        eventTypeDAO.delete(id);
    }
}
