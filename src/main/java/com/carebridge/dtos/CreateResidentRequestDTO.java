package com.carebridge.dtos;

public record CreateResidentRequestDTO(
    String firstName,
    String lastName,
    String cprNr,
    String userId,
    String guardianId
) {}
