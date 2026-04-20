package com.carebridge.controllers.security;

import com.carebridge.enums.Role;
import io.javalin.http.Context;
import io.javalin.http.UnauthorizedResponse;
import io.javalin.security.RouteRole;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class AccessController {

    private final SecurityController securityController = SecurityController.getInstance();

    public void accessHandler(Context ctx) {
        Set<RouteRole> allowedRoles = ctx.routeRoles();
        // System.out.println("DEBUG: Path=" + ctx.path() + ", Roles=" + allowedRoles);

        // 1️⃣ Hvis ruten er åben for alle (ANYONE), lad dem passere
        if (allowedRoles.contains(Role.ANYONE)) {
            return;
        }

        // 2️⃣ Hent token fra Authorization header
        String token = ctx.header("Authorization");
        if (token == null || token.isEmpty()) {
            throw new UnauthorizedResponse("No token provided. Please log in.");
        }

        // 3️⃣ Fjern "Bearer " prefix hvis tilstede
        token = token.replace("Bearer ", "");

        // 4️⃣ Verificer JWT token
        Map<String, Object> user;
        try {
            user = securityController.verifyToken(token);
            ctx.attribute("user", user);
        } catch (Exception e) {
            throw new UnauthorizedResponse("You need to log in, dude! Or your token is invalid.");
        }

        // 5️⃣ Tjek at bruger har tilladte roller
        if (!securityController.authorize(user, allowedRoles)) {
            throw new UnauthorizedResponse("Forbidden. Needed roles: " + allowedRoles);
        }
    }
}
