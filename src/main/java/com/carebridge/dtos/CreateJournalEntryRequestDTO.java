package com.carebridge.dtos;

import com.carebridge.enums.EntryType;
import com.carebridge.enums.RiskAssessment;

public record CreateJournalEntryRequestDTO(
    Long journalId,
    Long authorUserId,
    String title,
    String content,
    EntryType entryType,
    RiskAssessment riskAssessment
) {}
