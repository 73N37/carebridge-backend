package com.carebridge.controllers.security;

import io.javalin.http.Handler;
import io.javalin.security.RouteRole;

import java.util.Map;
import java.util.Set;

public interface ISecurityController {
    Handler login();

    Handler register();

    Handler authenticate();

    boolean authorize(Map<String, Object> user, Set<RouteRole> allowedRoles);

    String createToken(Map<String, Object> user) throws Exception;

    Map<String, Object> verifyToken(String token) throws Exception;
}
