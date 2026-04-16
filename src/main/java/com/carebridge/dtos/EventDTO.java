package com.carebridge.dtos;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public record EventDTO(
    Long id,
    String title,
    String description,
    Instant startAt,
    boolean showOnBoard,
    Long createdById,
    Long eventTypeId,
    LocalDate eventDate,
    LocalTime eventTime,
    boolean seenByCurrentUser,
    List<Long> seenByUserIds
) {}
