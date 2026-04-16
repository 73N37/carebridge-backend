package com.carebridge.dtos;

public record ResidentResponseDTO(
    Long id,
    String firstName,
    String lastName,
    Long journalId
) {}
