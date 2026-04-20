package com.carebridge.config;

import com.carebridge.crud.api.JavalinUniversalController;
import com.carebridge.crud.logic.DynamicCrudManager;
import com.carebridge.controllers.security.AccessController;
import com.carebridge.exceptions.ApiException;
import com.carebridge.routes.Routes;
import com.carebridge.utils.Utils;
import io.javalin.Javalin;
import io.javalin.apibuilder.ApiBuilder;
import io.javalin.config.JavalinConfig;
import io.javalin.http.Context;
import jakarta.persistence.EntityManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ApplicationConfig {

    private static final AccessController accessController = new AccessController();
    private static final Logger logger = LoggerFactory.getLogger(ApplicationConfig.class);
    private static final Routes routes = new Routes();
    private static final String frontEndOrigin = "http://localhost:5173";
    private static int count = 1;

    private static DynamicCrudManager crudManager;
    private static JavalinUniversalController universalController;

    private static synchronized void initializeUniversalCrud() {
        if (crudManager == null) {
            EntityManager em = HibernateConfig.getEntityManagerFactory().createEntityManager();
            crudManager = new DynamicCrudManager(em);
            crudManager.discoverAndRegister("com.carebridge.entities");
            universalController = new JavalinUniversalController(crudManager);
        }
    }

    public static void configuration(JavalinConfig config) {
        config.showJavalinBanner = false;
        config.bundledPlugins.enableRouteOverview("/routes");
        config.router.contextPath = "/api";
        
        config.router.apiBuilder(() -> {
            // Existing routes
            routes.getRoutes().addEndpoints();
            
            // 🌐 UNIVERSAL CRUD ROUTES (v3)
            initializeUniversalCrud();
            ApiBuilder.path("v3", () -> {
                ApiBuilder.get("metadata", universalController::getMetadata, com.carebridge.enums.Role.ANYONE);
                ApiBuilder.path("{resource}", () -> {
                    ApiBuilder.get(universalController::getAll, com.carebridge.enums.Role.ANYONE);
                    ApiBuilder.post(universalController::create, com.carebridge.enums.Role.ANYONE);
                    ApiBuilder.path("{id}", () -> {
                        ApiBuilder.get(universalController::getById, com.carebridge.enums.Role.ANYONE);
                        ApiBuilder.put(universalController::update, com.carebridge.enums.Role.ANYONE);
                        ApiBuilder.delete(universalController::delete, com.carebridge.enums.Role.ANYONE);
                    });
                });
            });
        });

        config.jsonMapper(new io.javalin.json.JavalinJackson(new Utils().getObjectMapper(), false));
    }

    public static Javalin startServer(int port) {
        Javalin app = Javalin.create(ApplicationConfig::configuration);

        app.beforeMatched(accessController::accessHandler);

        app.before(ApplicationConfig::corsHeaders);
        app.options("/*", ApplicationConfig::corsHeadersOptions);

        app.after(ApplicationConfig::afterRequest);

        app.exception(Exception.class, ApplicationConfig::generalExceptionHandler);
        app.exception(ApiException.class, ApplicationConfig::apiExceptionHandler);

        app.start(port);
        return app;
    }

    public static void afterRequest(Context ctx) {
        String requestInfo = ctx.req().getMethod() + " " + ctx.req().getRequestURI();
        logger.info("Request {} - {} was handled with status code {}", count++, requestInfo, ctx.status());
    }

    public static void stopServer(Javalin app) {
        app.stop();
    }

    private static void generalExceptionHandler(Exception e, Context ctx) {
        logger.error("Unhandled exception: ", e);
        ctx.status(500);
        ctx.json(Utils.convertToJsonMessage(ctx, "error", e.getMessage()));
    }

    public static void apiExceptionHandler(ApiException e, Context ctx) {
        ctx.status(e.getStatusCode());
        logger.warn("API exception ({}): {}", e.getStatusCode(), e.getMessage());
        ctx.json(Utils.convertToJsonMessage(ctx, "warning", e.getMessage()));
    }

    private static void corsHeaders(Context ctx) {
        ctx.header("Access-Control-Allow-Origin", frontEndOrigin);
        ctx.header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        ctx.header("Access-Control-Allow-Headers", "Content-Type, Authorization");
        ctx.header("Access-Control-Allow-Credentials", "true");
    }

    private static void corsHeadersOptions(Context ctx) {
        corsHeaders(ctx);
        ctx.status(204);
    }
}
