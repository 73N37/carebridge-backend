package com.carebridge.crud;

import com.carebridge.crud.api.JavalinUniversalController;
import com.carebridge.crud.logic.DynamicCrudManager;
import io.github.cdimascio.dotenv.Dotenv;
import io.javalin.Javalin;
import io.javalin.apibuilder.ApiBuilder;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;
import java.util.Set;
import java.util.function.Consumer;

/**
 * 🚀 THE UNIVERSAL SERVER 🚀
 * The single entry point for the ultimate developer experience.
 * Automatically loads .env, configures Hibernate via reflection, 
 * discovers entities, and generates all REST API routes.
 */
public class UniversalServer {

    private static final Logger log = LoggerFactory.getLogger(UniversalServer.class);
    private static EntityManagerFactory emf;

    /**
     * Ignites the entire backend stack with a single call.
     * @param basePackage The package to scan for @Entity and @CrudResource classes.
     * @return The started Javalin instance.
     */
    public static Javalin ignite(String basePackage) {
        return ignite(basePackage, config -> {});
    }

    /**
     * Ignites the entire backend stack, allowing for custom Javalin configuration (e.g. security handlers).
     * @param basePackage The package to scan for @Entity and @CrudResource classes.
     * @param customJavalinConfig A lambda to add extra configuration or routes to Javalin.
     * @return The started Javalin instance.
     */
    public static Javalin ignite(String basePackage, Consumer<io.javalin.config.JavalinConfig> customJavalinConfig) {
        log.info("🔥 Igniting Universal Server for package: [{}]", basePackage);

        // 1. Load Environment Variables from .env
        Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
        int port = Integer.parseInt(dotenv.get("SERVER_PORT", "7070"));

        // 2. Auto-Discover Entities via Reflection
        Reflections reflections = new Reflections(basePackage);
        Set<Class<?>> entityClasses = reflections.getTypesAnnotatedWith(Entity.class);
        log.info("📦 Discovered {} @Entity classes automatically.", entityClasses.size());

        // 3. Auto-Configure Hibernate
        emf = buildEntityManagerFactory(entityClasses, dotenv);
        EntityManager em = emf.createEntityManager();

        // 4. Initialize Universal CRUD Logic
        DynamicCrudManager crudManager = new DynamicCrudManager(em);
        Set<Class<?>> crudResources = reflections.getTypesAnnotatedWith(com.carebridge.crud.annotations.CrudResource.class);
        for (Class<?> clazz : crudResources) {
            crudManager.registerResource(clazz);
        }
        log.info("✨ Registered {} Universal CRUD Resources.", crudResources.size());

        // 5. Start Server & Auto-Map Universal Routes
        Javalin app = Javalin.create(config -> {
            config.showJavalinBanner = false;
            config.router.contextPath = "/api";

            JavalinUniversalController universalController = new JavalinUniversalController(crudManager);

            config.router.apiBuilder(() -> {
                // Register the dynamic Universal CRUD routes (v3)
                ApiBuilder.path("v3", () -> {
                    ApiBuilder.get("metadata", universalController::getMetadata);
                    ApiBuilder.path("{resource}", () -> {
                        ApiBuilder.get(universalController::getAll);
                        ApiBuilder.post(universalController::create);
                        ApiBuilder.path("{id}", () -> {
                            ApiBuilder.get(universalController::getById);
                            ApiBuilder.put(universalController::update);
                            ApiBuilder.delete(universalController::delete);
                        });
                    });
                });
            });

            // Allow the developer to inject their own logic/security/routes
            customJavalinConfig.accept(config);
        });

        app.start(port);
        log.info("✅ Universal Server is LIVE at http://localhost:{}/api/v3", port);
        return app;
    }

    public static EntityManagerFactory getEntityManagerFactory() {
        if (emf == null) {
            throw new IllegalStateException("Universal Server must be ignited before accessing EntityManagerFactory.");
        }
        return emf;
    }

    /**
     * Builds a Hibernate EntityManagerFactory dynamically from discovered entities and .env settings.
     */
    private static EntityManagerFactory buildEntityManagerFactory(Set<Class<?>> entities, Dotenv dotenv) {
        Configuration configuration = new Configuration();
        Properties props = new Properties();

        // Database connection details from .env
        String host = dotenv.get("DB_HOST", "localhost:5432");
        String db = dotenv.get("DB_NAME", "cruddb");
        String user = dotenv.get("DB_USER", "user");
        String pass = dotenv.get("DB_PASSWORD", "");
        String ssl = dotenv.get("DB_SSLMODE", "disable");
        
        // H2 Memory Fallback for fast prototyping if requested
        if ("true".equalsIgnoreCase(dotenv.get("USE_H2", "false"))) {
            log.warn("🧪 Using H2 In-Memory Database fallback.");
            props.put("hibernate.connection.url", "jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1");
            props.put("hibernate.connection.driver_class", "org.h2.Driver");
            props.put("hibernate.dialect", "org.hibernate.dialect.H2Dialect");
        } else {
            props.put("hibernate.connection.url", "jdbc:postgresql://" + host + "/" + db + "?sslmode=" + ssl);
            props.put("hibernate.connection.username", user);
            props.put("hibernate.connection.password", pass);
            props.put("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
            props.put("hibernate.connection.driver_class", "org.postgresql.Driver");
        }

        // Standard Hibernate settings
        props.put("hibernate.hbm2ddl.auto", dotenv.get("DB_DDL_AUTO", "update"));
        props.put("hibernate.show_sql", dotenv.get("DB_SHOW_SQL", "false"));
        props.put("hibernate.format_sql", "true");
        props.put("hibernate.current_session_context_class", "thread");

        configuration.setProperties(props);
        
        // Register the framework's BaseEntity
        configuration.addAnnotatedClass(com.carebridge.crud.data.core.BaseEntity.class);

        // Register all discovered entities
        for (Class<?> clazz : entities) {
            configuration.addAnnotatedClass(clazz);
        }

        ServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder()
                .applySettings(configuration.getProperties())
                .build();
        SessionFactory sf = configuration.buildSessionFactory(serviceRegistry);
        return sf.unwrap(EntityManagerFactory.class);
    }
}
