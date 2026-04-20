package com.carebridge.controllers.impl;

import com.carebridge.controllers.IController;
import com.carebridge.dao.impl.EventDAO;
import com.carebridge.dao.impl.EventTypeDAO;
import com.carebridge.dao.impl.UserDAO;
import com.carebridge.entities.Event;
import com.carebridge.entities.EventType;
import com.carebridge.entities.User;
import com.carebridge.exceptions.ApiRuntimeException;
import com.carebridge.crud.logic.MappingService;
import io.javalin.http.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Map;
import java.util.stream.Collectors;

public class EventController implements IController<Event, Long> {

    private static final Logger logger = LoggerFactory.getLogger(EventController.class);
    private final EventDAO eventDAO = EventDAO.getInstance();
    private final EventTypeDAO eventTypeDAO = EventTypeDAO.getInstance();
    private final UserDAO userDAO = UserDAO.getInstance();
    private final MappingService mappingService = new MappingService();

    @Override
    public void read(Context ctx) {
        try {
            Long id = parseId(ctx);
            var entity = eventDAO.read(id);
            if (entity == null) {
                ctx.status(404).json("{\"msg\":\"Event not found\"}");
                return;
            }
            ctx.json(mappingService.toMap(entity));
        } catch (ApiRuntimeException e) {
            ctx.status(e.getStatusCode()).json("{\"msg\":\"" + e.getMessage() + "\"}");
        } catch (Exception e) {
            logger.error("read event failed", e);
            ctx.status(500).json("{\"msg\":\"Internal error\"}");
        }
    }

    @Override
    public void readAll(Context ctx) {
        try {
            String fromParam = ctx.queryParam("from");
            String toParam = ctx.queryParam("to");
            String tzParam = ctx.queryParam("tz");

            if (fromParam != null || toParam != null) {
                ZoneId zone = resolveZone(tzParam);
                var today = LocalDate.now(zone);
                LocalDate fromDate = parseDateKeywordOrIso(fromParam, today, 0);
                LocalDate toDate = parseDateKeywordOrIso(toParam, today, 1);

                Instant fromInstant = fromDate.atStartOfDay(zone).toInstant();
                Instant toInstant = toDate.plusDays(1).atStartOfDay(zone).toInstant();

                var list = eventDAO.readBetween(fromInstant, toInstant);
                ctx.json(mappingService.toMapList(list));
                return;
            }

            var list = eventDAO.readAll().stream()
                    .sorted((a, b) -> a.getStartAt().compareTo(b.getStartAt()))
                    .toList();
            ctx.json(mappingService.toMapList(list));

        } catch (Exception e) {
            logger.error("readAll events failed", e);
            ctx.status(500).json("{\"msg\":\"Internal error\"}");
        }
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

    @Override
    public void create(Context ctx) {
        try {
            Map<String, Object> body = ctx.bodyAsClass(Map.class);
            Event e = mappingService.toEntity(body, Event.class);
            
            // 🛡️ Manual override for Java Time types which often fail in reflection-based Map -> Entity conversion
            if (body.containsKey("startAt") && body.get("startAt") != null) {
                e.setStartAt(Instant.parse((String) body.get("startAt")));
            }

            if (e.getTitle() == null || e.getTitle().isBlank()) throw new ApiRuntimeException(400, "title required");
            if (e.getStartAt() == null) throw new ApiRuntimeException(400, "startAt required");

            var tokenUser = ctx.attribute("user");
            String email = null;
            if (tokenUser instanceof Map<?, ?> ju) email = (String) ju.get("username");

            if (email == null) throw new ApiRuntimeException(401, "Unauthorized");

            User creator = userDAO.readByEmail(email);
            if (creator == null) throw new ApiRuntimeException(401, "Unauthorized");

            if (body.containsKey("eventTypeId")) {
                Long etId = parseLong(body.get("eventTypeId"));
                EventType et = eventTypeDAO.read(etId);
                if (et == null) throw new ApiRuntimeException(404, "EventType not found");
                e.setEventType(et);
            }

            e.setCreatedBy(creator);
            var created = eventDAO.create(e);
            ctx.status(201).json(mappingService.toMap(created));
        } catch (ApiRuntimeException e) {
            ctx.status(e.getStatusCode()).json("{\"msg\":\"" + e.getMessage() + "\"}");
        } catch (Exception e) {
            logger.error("create event failed", e);
            ctx.status(500).json("{\"msg\":\"Internal error\"}");
        }
    }

    @Override
    public void update(Context ctx) {
        try {
            Long id = parseId(ctx);
            Map<String, Object> body = ctx.bodyAsClass(Map.class);
            Event patch = mappingService.toEntity(body, Event.class);

            if (body.containsKey("eventTypeId")) {
                Long etId = parseLong(body.get("eventTypeId"));
                EventType et = eventTypeDAO.read(etId);
                if (et == null) throw new ApiRuntimeException(404, "EventType not found");
                patch.setEventType(et);
            }
            
            var updated = eventDAO.update(id, patch);
            if (updated == null) {
                ctx.status(404).json("{\"msg\":\"Event not found\"}");
                return;
            }
            ctx.json(mappingService.toMap(updated));
        } catch (ApiRuntimeException e) {
            ctx.status(e.getStatusCode()).json("{\"msg\":\"" + e.getMessage() + "\"}");
        } catch (Exception e) {
            logger.error("update event failed", e);
            ctx.status(500).json("{\"msg\":\"Internal error\"}");
        }
    }

    @Override
    public void delete(Context ctx) {
        try {
            Long id = parseId(ctx);
            eventDAO.delete(id);
            ctx.status(204);
        } catch (ApiRuntimeException e) {
            ctx.status(e.getStatusCode()).json("{\"msg\":\"" + e.getMessage() + "\"}");
        } catch (Exception e) {
            logger.error("delete event failed", e);
            ctx.status(500).json("{\"msg\":\"Internal error\"}");
        }
    }

    public void readByCreator(Context ctx) {
        try {
            Long userId = Long.parseLong(ctx.pathParam("userId"));
            var list = eventDAO.readByCreator(userId);
            ctx.json(mappingService.toMapList(list));
        } catch (NumberFormatException ex) {
            ctx.status(400).json("{\"msg\":\"Invalid userId\"}");
        } catch (Exception e) {
            logger.error("readByCreator failed", e);
            ctx.status(500).json("{\"msg\":\"Internal error\"}");
        }
    }

    public void readUpcoming(Context ctx) {
        try {
            var now = Instant.now();
            var list = eventDAO.readAll().stream()
                    .filter(e -> e.getStartAt() != null && !e.getStartAt().isBefore(now))
                    .toList();
            ctx.json(mappingService.toMapList(list));
        } catch (Exception e) {
            logger.error("readUpcoming failed", e);
            ctx.status(500).json("{\"msg\":\"Internal error\"}");
        }
    }

    public void markSeen(Context ctx) {
        try {
            Long eventId = parseId(ctx);
            var tokenUser = ctx.attribute("user");
            String email = null;
            if (tokenUser instanceof Map<?, ?> ju) email = (String) ju.get("username");

            if (email == null) throw new ApiRuntimeException(401, "Unauthorized");
            var user = userDAO.readByEmail(email);
            if (user == null) throw new ApiRuntimeException(401, "Unauthorized");

            eventDAO.addSeenByUser(eventId, user);
            ctx.status(204);
        } catch (ApiRuntimeException e) {
            ctx.status(e.getStatusCode()).json("{\"msg\":\"" + e.getMessage() + "\"}");
        } catch (Exception e) {
            logger.error("markSeen failed", e);
            ctx.status(500).json("{\"msg\":\"Internal error\"}");
        }
    }

    public void unmarkSeen(Context ctx) {
        try {
            Long eventId = parseId(ctx);
            var tokenUser = ctx.attribute("user");
            String email = null;
            if (tokenUser instanceof Map<?, ?> ju) email = (String) ju.get("username");

            if (email == null) throw new ApiRuntimeException(401, "Unauthorized");
            var user = userDAO.readByEmail(email);
            if (user == null) throw new ApiRuntimeException(401, "Unauthorized");

            eventDAO.removeSeenByUser(eventId, user);
            ctx.status(204);
        } catch (ApiRuntimeException e) {
            ctx.status(e.getStatusCode()).json("{\"msg\":\"" + e.getMessage() + "\"}");
        } catch (Exception e) {
            logger.error("unmarkSeen failed", e);
            ctx.status(500).json("{\"msg\":\"Internal error\"}");
        }
    }

    @Override
    public boolean validatePrimaryKey(Long id) {
        return id != null && id > 0;
    }

    @Override
    public Event validateEntity(Context ctx) { return null; }

    private Long parseLong(Object obj) {
        if (obj instanceof Number n) return n.longValue();
        if (obj instanceof String s) return Long.parseLong(s);
        throw new ApiRuntimeException(400, "Invalid number format");
    }

    private Long parseId(Context ctx) {
        try { return Long.parseLong(ctx.pathParam("id")); } catch (Exception ex) { throw new ApiRuntimeException(400, "Invalid id"); }
    }
}
