package com.carebridge.controllers.security;

import com.carebridge.config.HibernateConfig;
import com.carebridge.dao.security.ISecurityDAO;
import com.carebridge.dao.security.SecurityDAO;
import com.carebridge.security.ITokenSecurity;
import com.carebridge.security.TokenSecurity;
import com.carebridge.entities.User;
import com.carebridge.enums.Role;
import com.carebridge.exceptions.ApiRuntimeException;
import com.carebridge.exceptions.ValidationException;
import com.carebridge.utils.Utils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import io.javalin.http.UnauthorizedResponse;
import io.javalin.security.RouteRole;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class SecurityController implements ISecurityController {

    private static final Logger logger = LoggerFactory.getLogger(SecurityController.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final ITokenSecurity tokenSecurity = new TokenSecurity();
    private static final ISecurityDAO securityDAO = new SecurityDAO(HibernateConfig.getEntityManagerFactory());
    private static SecurityController instance;

    private SecurityController() {
    }

    public static SecurityController getInstance() {
        if (instance == null) {
            instance = new SecurityController();
        }
        return instance;
    }

    @Override
    public Handler login() {
        return ctx -> {
            ObjectNode out = objectMapper.createObjectNode();
            try {
                Map<String, String> userJson = ctx.bodyAsClass(Map.class);
                String email = userJson.get("email");
                String password = userJson.get("password");

                User user = securityDAO.getVerifiedUser(email, password);
                String token = createToken(Map.of("username", user.getEmail(), "roles", Set.of(user.getRole().name())));
                ctx.status(200).json(out.put("token", token).put("email", email));
            } catch (ValidationException e) {
                throw new ApiRuntimeException(401, e.getMessage());
            } catch (Exception e) {
                logger.error("login failed", e);
                ctx.status(500).json(out.put("msg", "Internal error"));
            }
        };
    }

    @Override
    public Handler register() {
        return ctx -> {
            ObjectNode out = objectMapper.createObjectNode();
            try {
                Map<String, Object> body = ctx.bodyAsClass(Map.class);
                
                User user = securityDAO.createUser(
                        (String) body.get("name"),
                        (String) body.get("email"),
                        (String) body.get("password"),
                        (String) body.get("displayName"),
                        (String) body.get("displayEmail"),
                        (String) body.get("displayPhone"),
                        (String) body.get("internalEmail"),
                        (String) body.get("internalPhone"),
                        Role.valueOf(((String) body.getOrDefault("role", "USER")).toUpperCase())
                );

                String token = createToken(Map.of("username", user.getEmail(), "roles", Set.of(user.getRole().name())));
                ctx.status(201).json(out.put("token", token).put("email", user.getEmail()));
            } catch (Exception e) {
                logger.error("Registration failed", e);
                ctx.status(400).json(out.put("msg", e.getMessage()));
            }
        };
    }

    @Override
    public Handler authenticate() {
        return ctx -> {
            String token = ctx.header("Authorization");
            if (token == null || token.isEmpty()) {
                throw new UnauthorizedResponse("No token provided");
            }
            token = token.replace("Bearer ", "");
            if (!tokenSecurity.tokenIsValid(token, Utils.getPropertyValue("SECRET_KEY", "application.properties"))) {
                throw new UnauthorizedResponse("Invalid token");
            }
            if (!tokenSecurity.tokenNotExpired(token)) {
                throw new UnauthorizedResponse("Token expired");
            }
            Map<String, Object> verifiedTokenUser = verifyToken(token);
            ctx.attribute("user", verifiedTokenUser);
        };
    }

    @Override
    public boolean authorize(Map<String, Object> user, Set<RouteRole> allowedRoles) {
        if (user == null) throw new UnauthorizedResponse("You need to log in, dude!");
        Set<String> roles = (Set<String>) user.get("roles");
        var allowed = allowedRoles.stream().map(RouteRole::toString).collect(Collectors.toSet());
        return roles.stream().map(String::toUpperCase).anyMatch(allowed::contains);
    }

    @Override
    public String createToken(Map<String, Object> user) {
        try {
            String username = (String) user.get("username");
            Set<String> roles = (Set<String>) user.get("roles");
            String rolesCsv = String.join(",", roles);
            return tokenSecurity.createToken(
                    username,
                    rolesCsv,
                    Utils.getPropertyValue("ISSUER", "application.properties"),
                    Utils.getPropertyValue("TOKEN_EXPIRE_TIME", "application.properties"),
                    Utils.getPropertyValue("SECRET_KEY", "application.properties")
            );
        } catch (Exception e) {
            logger.error("Token creation failed", e);
            throw new ApiRuntimeException(500, "Could not create token");
        }
    }

    @Override
    public Map<String, Object> verifyToken(String token) throws Exception {
        boolean valid = tokenSecurity.tokenIsValid(token, Utils.getPropertyValue("SECRET_KEY", "application.properties"));
        if (valid) {
            return tokenSecurity.getUserWithRolesFromToken(token);
        } else {
            throw new UnauthorizedResponse("Invalid token");
        }
    }

    public Handler addRole() {
        return ctx -> ctx.status(501).json("{\"msg\":\"Not implemented in enum-role model\"}");
    }

    public void healthCheck(@NotNull Context ctx) {
        ctx.status(200).json("{\"msg\": \"API is up and running\"}");
    }
}
