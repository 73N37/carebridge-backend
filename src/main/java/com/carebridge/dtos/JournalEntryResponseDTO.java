package com.carebridge.dtos;

import com.carebridge.enums.EntryType;
import com.carebridge.enums.RiskAssessment;
import java.time.LocalDateTime;

public record JournalEntryResponseDTO(
    Long id,
    Long journalId,
    Long authorUserId,
    String title,
    String content,
    EntryType entryType,
    RiskAssessment riskAssessment,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    LocalDateTime editCloseTime
) {}
