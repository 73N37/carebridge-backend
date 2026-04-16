package com.carebridge.config;

import com.carebridge.crud.api.JavalinUniversalController;
import com.carebridge.crud.logic.DynamicCrudManager;
import com.carebridge.controllers.security.AccessController;
import com.carebridge.exceptions.ApiException;
import com.carebridge.exceptions.ApiRuntimeException;
import com.carebridge.routes.Routes;
import com.carebridge.utils.Utils;
import io.javalin.Javalin;
import io.javalin.config.JavalinConfig;
import io.javalin.http.Context;
import jakarta.persistence.EntityManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class ApplicationConfig {

    private static final Logger logger = LoggerFactory.getLogger(ApplicationConfig.class);
    private static final AccessController accessController = new AccessController();
    private static final Routes routes = new Routes();
    
    private static DynamicCrudManager crudManager;
    private static JavalinUniversalController universalController;

    public static Javalin startServer(int port) {
        // Ensure CRUD components are initialized with the correct EMF
        initializeUniversalCrud();
        
        Javalin app = Javalin.create(ApplicationConfig::configuration);
        app.start(port);
        return app;
    }

    private static synchronized void initializeUniversalCrud() {
        EntityManager em = HibernateConfig.getEntityManagerFactory().createEntityManager();
        crudManager = new DynamicCrudManager(em);
        crudManager.discoverAndRegister("com.carebridge.entities");
        universalController = new JavalinUniversalController(crudManager);
    }

    public static void configuration(JavalinConfig config) {
        config.showJavalinBanner = false;

        config.router.apiBuilder(() -> {
            io.javalin.apibuilder.ApiBuilder.get("/", ctx -> ctx.result("Carebridge API is running"));
            io.javalin.apibuilder.ApiBuilder.path("/api", () -> {
                routes.getRoutes().addEndpoints();
                
                // 🌟 UNIVERSAL CRUD API (v3)
                if (universalController != null) {
                    io.javalin.apibuilder.ApiBuilder.path("v3", universalController.getRoutes());
                }
            });
        });

        config.router.mount(router -> {
            router.beforeMatched(accessController::accessHandler);
        });

        config.bundledPlugins.enableCors(cors -> {
            cors.addRule(it -> {
                it.anyHost();
            });
        });

        config.router.mount(router -> {
            router.before(ApplicationConfig::corsHeaders);
            router.options("/*", ApplicationConfig::corsHeadersOptions);
        });

        // EXCEPTION HANDLING
        config.router.mount(router -> {
            router.exception(ApiException.class, (e, ctx) -> {
                ctx.status(e.getStatusCode()).json(Map.of("msg", e.getMessage()));
            });
            router.exception(ApiRuntimeException.class, (e, ctx) -> {
                ctx.status(e.getStatusCode()).json(Map.of("msg", e.getMessage()));
            });
            router.exception(io.javalin.validation.ValidationException.class, (e, ctx) -> {
                ctx.status(400).json(Map.of("msg", e.getMessage()));
            });
            router.exception(Exception.class, (e, ctx) -> {
                logger.error("Unhandled exception", e);
                ctx.status(500).json(Map.of("msg", "Internal server error"));
            });
        });
    }

    private static void corsHeaders(Context ctx) {
        ctx.header("Access-Control-Allow-Origin", "*");
        ctx.header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        ctx.header("Access-Control-Allow-Headers", "Content-Type, Authorization");
        ctx.header("Access-Control-Allow-Credentials", "true");
    }

    private static void corsHeadersOptions(Context ctx) {
        corsHeaders(ctx);
        ctx.status(204);
    }
}
