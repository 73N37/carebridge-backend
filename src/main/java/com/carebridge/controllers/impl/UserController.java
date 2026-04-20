package com.carebridge.controllers.impl;

import com.carebridge.config.HibernateConfig;
import com.carebridge.config.Populator;
import com.carebridge.controllers.IController;
import com.carebridge.dao.impl.ResidentDAO;
import com.carebridge.dao.impl.UserDAO;
import com.carebridge.entities.Resident;
import com.carebridge.entities.User;
import com.carebridge.exceptions.ApiRuntimeException;
import com.carebridge.crud.logic.MappingService;
import io.javalin.http.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class UserController implements IController<User, Long> {

    private final UserDAO dao = UserDAO.getInstance();
    private final UserDAO userDAO = UserDAO.getInstance();
    private final ResidentDAO residentDAO = ResidentDAO.getInstance();
    private final MappingService mappingService = new MappingService();
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Override
    public void read(Context ctx) {
        try {
            Long id = parseId(ctx);
            User user = dao.read(id);
            if (user == null) {
                ctx.status(404).json("{\"msg\":\"User not found\"}");
                return;
            }
            ctx.json(mappingService.toMap(user));
        } catch (ApiRuntimeException e) {
            ctx.status(e.getStatusCode()).json("{\"msg\":\"" + e.getMessage() + "\"}");
        } catch (Exception e) {
            logger.error("Error", e);
            ctx.status(500).json("{\"msg\":\"Internal error\"}");
        }
    }

    @Override
    public void readAll(Context ctx) {
        try {
            List<User> users = dao.readAll();
            ctx.json(mappingService.toMapList(users));
        } catch (Exception e) {
            logger.error("Error", e);
            ctx.status(500).json("{\"msg\":\"Internal error\"}");
        }
    }

    @Override
    public void create(Context ctx) {
        try {
            Map<String, Object> body = ctx.bodyAsClass(Map.class);
            User user = mappingService.toEntity(body, User.class);
            
            // Password needs special handling because it might be hash or raw
            if (body.containsKey("password")) {
                user.setPassword((String) body.get("password"));
            }

            User created = dao.create(user);
            ctx.status(201).json(mappingService.toMap(created));
        } catch (Exception e) {
            logger.error("Error", e);
            ctx.status(500).json("{\"msg\":\"Internal error\"}");
        }
    }

    @Override
    public void update(Context ctx) {
        try {
            Long id = parseId(ctx);
            Map<String, Object> body = ctx.bodyAsClass(Map.class);
            User user = mappingService.toEntity(body, User.class);
            
            if (body.containsKey("password")) {
                user.setPassword((String) body.get("password"));
            }

            User updated = dao.update(id, user);
            if (updated == null) {
                ctx.status(404).json("{\"msg\":\"User not found\"}");
                return;
            }
            ctx.json(mappingService.toMap(updated));
        } catch (ApiRuntimeException e) {
            ctx.status(e.getStatusCode()).json("{\"msg\":\"" + e.getMessage() + "\"}");
        } catch (Exception e) {
            logger.error("Error", e);
            ctx.status(500).json("{\"msg\":\"Internal error\"}");
        }
    }

    @Override
    public void delete(Context ctx) {
        try {
            Long id = parseId(ctx);
            userDAO.delete(id);
            ctx.status(204);
        } catch (ApiRuntimeException e) {
            ctx.status(e.getStatusCode()).json("{\"msg\":\"" + e.getMessage() + "\"}");
        } catch (Exception e) {
            logger.error("Error", e);
            ctx.status(500).json("{\"msg\":\"Internal error\"}");
        }
    }

    public void me(Context ctx) {
        try {
            var jwtUser = ctx.attribute("user");
            String email = null;
            if (jwtUser instanceof Map<?, ?> ju) {
                email = (String) ju.get("username");
            }

            if (email == null) {
                ctx.status(401).json("{\"msg\":\"Unauthorized\"}");
                return;
            }

            User user = userDAO.readByEmail(email);
            if (user == null) {
                ctx.status(404).json("{\"msg\":\"User not found\"}");
                return;
            }
            ctx.json(mappingService.toMap(user));
        } catch (Exception e) {
            logger.error("Error", e);
            ctx.status(500).json("{\"msg\":\"Internal error\"}");
        }
    }

    public void populate(Context ctx) {
        try {
            Populator.populate(HibernateConfig.getEntityManagerFactory());
            ctx.status(200).json(Map.of("msg", "Database populated"));
        } catch (Exception e) {
            logger.error("Error", e);
            ctx.status(500).json(Map.of("msg", "Error during population: " + e.getMessage()));
        }
    }

    public void linkResidents(Context ctx) {
        try {
            Long userId = Long.parseLong(ctx.pathParam("id"));
            Map<String, Object> body = ctx.bodyAsClass(Map.class);
            List<Number> residentIds = (List<Number>) body.get("residentIds");

            User user = userDAO.read(userId);
            if (user == null) {
                ctx.status(404).json(Map.of("msg", "Bruger ikke fundet"));
                return;
            }

            // Hent residents
            List<Resident> residentsToLink = new ArrayList<>();
            for (Number residentId : residentIds) {
                Resident r = residentDAO.read(residentId.longValue());
                if (r != null) {
                    residentsToLink.add(r);
                }
            }

            user.setResidents(residentsToLink);
            userDAO.update(userId, user);

            ctx.status(200).json(Map.of("msg", "Beboere tilknyttet"));
        } catch (Exception e) {
            logger.error("Error", e);
            ctx.status(500).json(Map.of("msg", "Kunne ikke tilknytte beboere"));
        }
    }

    @Override
    public boolean validatePrimaryKey(Long id) {
        return id != null && id > 0;
    }

    private Long parseId(Context ctx) {
        try {
            return Long.parseLong(ctx.pathParam("id"));
        } catch (NumberFormatException e) {
            throw new ApiRuntimeException(400, "Invalid ID format");
        }
    }

    @Override
    public User validateEntity(Context ctx) {
        return ctx.bodyAsClass(User.class);
    }
}
