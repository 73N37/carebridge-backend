package com.carebridge.services.mappers;

import com.carebridge.dtos.EventDTO;
import com.carebridge.entities.Event;
import com.carebridge.entities.User;

import java.util.List;
import java.util.Objects;

public class EventMapper {

    public static EventDTO toDTO(Event event) {
        return toDTO(event, null);
    }

    public static EventDTO toDTO(Event event, Long currentUserId) {
        if (event == null) return null;

        boolean seenByCurrentUser = false;
        if (currentUserId != null && event.getSeenByUsers() != null) {
            seenByCurrentUser = event.getSeenByUsers().stream()
                    .map(User::getId)
                    .anyMatch(id -> Objects.equals(id, currentUserId));
        }

        List<Long> seenIds = event.getSeenByUsers() == null
                ? List.of()
                : event.getSeenByUsers().stream().map(User::getId).toList();

        return new EventDTO(
            event.getId(),
            event.getTitle(),
            event.getDescription(),
            event.getStartAt(),
            event.isShowOnBoard(),
            event.getCreatedBy() != null ? event.getCreatedBy().getId() : null,
            event.getEventType() != null ? event.getEventType().getId() : null,
            event.getEventDate(),
            event.getEventTime(),
            seenByCurrentUser,
            seenIds
        );
    }
}
