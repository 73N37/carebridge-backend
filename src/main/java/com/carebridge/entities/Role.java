package com.carebridge.entities;

import io.javalin.security.RouteRole;

public enum Role implements RouteRole {
    ANYONE,
    USER,
    ADMIN,
    CAREWORKER,
    GUARDIAN
}
