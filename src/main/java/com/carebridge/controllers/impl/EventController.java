package com.carebridge.controllers.impl;

import com.carebridge.dao.impl.EventDAO;
import com.carebridge.dao.impl.EventTypeDAO;
import com.carebridge.dao.impl.UserDAO;
import com.carebridge.entities.Event;
import com.carebridge.entities.EventType;
import com.carebridge.entities.User;
import com.carebridge.exceptions.ApiRuntimeException;
import com.carebridge.crud.logic.MappingService;
import com.carebridge.crud.annotations.DynamicDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/events")
public class EventController {

    private static final Logger logger = LoggerFactory.getLogger(EventController.class);
    private final EventDAO eventDAO;
    private final EventTypeDAO eventTypeDAO;
    private final UserDAO userDAO;
    private final MappingService mappingService;

    public EventController(EventDAO eventDAO, EventTypeDAO eventTypeDAO, UserDAO userDAO, MappingService mappingService) {
        this.eventDAO = eventDAO;
        this.eventTypeDAO = eventTypeDAO;
        this.userDAO = userDAO;
        this.mappingService = mappingService;
    }

    @GetMapping("/{id}")
    @DynamicDTO
    public ResponseEntity<Event> read(@PathVariable Long id) {
        var entity = eventDAO.read(id);
        if (entity == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(entity);
    }

    @GetMapping
    @DynamicDTO
    public List<Event> readAll(
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to,
            @RequestParam(required = false) String tz) {
        
        if (from != null || to != null) {
            ZoneId zone = resolveZone(tz);
            var today = LocalDate.now(zone);
            LocalDate fromDate = parseDateKeywordOrIso(from, today, 0);
            LocalDate toDate = parseDateKeywordOrIso(to, today, 1);

            Instant fromInstant = fromDate.atStartOfDay(zone).toInstant();
            Instant toInstant = toDate.plusDays(1).atStartOfDay(zone).toInstant();

            return eventDAO.readBetween(fromInstant, toInstant);
        }

        return eventDAO.readAll().stream()
                .sorted((a, b) -> a.getStartAt().compareTo(b.getStartAt()))
                .toList();
    }

    private ZoneId resolveZone(String tzParam) {
        if (tzParam == null || tzParam.isBlank()) return ZoneId.of("Europe/Copenhagen");
        try { return ZoneId.of(tzParam); } catch (Exception ex) { return ZoneId.of("Europe/Copenhagen"); }
    }

    private LocalDate parseDateKeywordOrIso(String value, LocalDate today, int defaultOffsetDays) {
        if (value == null || value.isBlank()) return today.plusDays(defaultOffsetDays);
        return switch (value.toLowerCase()) {
            case "today" -> today;
            case "tomorrow" -> today.plusDays(1);
            default -> LocalDate.parse(value);
        };
    }

    @PostMapping
    @DynamicDTO
    public ResponseEntity<Event> create(
            @RequestBody Map<String, Object> body,
            @RequestAttribute("user") Map<String, Object> jwtUser) {
        
        Event e = mappingService.toEntity(body, Event.class);
        
        if (body.containsKey("startAt") && body.get("startAt") != null) {
            e.setStartAt(Instant.parse((String) body.get("startAt")));
        }

        String email = (String) jwtUser.get("username");
        User creator = userDAO.readByEmail(email);
        if (creator == null) throw new ApiRuntimeException(401, "Unauthorized");

        if (body.containsKey("eventTypeId")) {
            Long etId = ((Number) body.get("eventTypeId")).longValue();
            EventType et = eventTypeDAO.read(etId);
            if (et == null) throw new ApiRuntimeException(404, "EventType not found");
            e.setEventType(et);
        }

        e.setCreatedBy(creator);
        var created = eventDAO.create(e);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    @DynamicDTO
    public ResponseEntity<Event> update(
            @PathVariable Long id,
            @RequestBody Map<String, Object> body) {
        
        Event patch = mappingService.toEntity(body, Event.class);

        if (body.containsKey("eventTypeId")) {
            Long etId = ((Number) body.get("eventTypeId")).longValue();
            EventType et = eventTypeDAO.read(etId);
            if (et == null) throw new ApiRuntimeException(404, "EventType not found");
            patch.setEventType(et);
        }
        
        var updated = eventDAO.update(id, patch);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        eventDAO.delete(id);
    }

    @GetMapping("/upcoming")
    @DynamicDTO
    public List<Event> readUpcoming() {
        var now = Instant.now();
        return eventDAO.readAll().stream()
                .filter(e -> e.getStartAt() != null && !e.getStartAt().isBefore(now))
                .toList();
    }

    @PostMapping("/{id}/mark-seen")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void markSeen(@PathVariable Long id, @RequestAttribute("user") Map<String, Object> jwtUser) {
        String email = (String) jwtUser.get("username");
        var user = userDAO.readByEmail(email);
        eventDAO.addSeenByUser(id, user);
    }

    @DeleteMapping("/{id}/mark-seen")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void unmarkSeen(@PathVariable Long id, @RequestAttribute("user") Map<String, Object> jwtUser) {
        String email = (String) jwtUser.get("username");
        var user = userDAO.readByEmail(email);
        eventDAO.removeSeenByUser(id, user);
    }
}
