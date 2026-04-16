package com.carebridge;

import com.carebridge.crud.UniversalServer;
import com.carebridge.routes.Routes;
import com.carebridge.controllers.security.AccessController;

/**
 * 🌟 THE BEST DEVELOPER EXPERIENCE 🌟
 * This class demonstrates the "One-Liner" entry point.
 * Developers only need to create Entities and run this.
 */
public class UniversalApp {
    public static void main(String[] args) {
        
        // 🚀 ONE FUNCTION TO RULE THEM ALL
        // This single line:
        // 1. Loads .env configuration
        // 2. Uses Reflection to find all @Entity classes
        // 3. Auto-configures the Hibernate database schema
        // 4. Discovers all @CrudResource annotations
        // 5. Instantiates all needed DAOs and Services in memory
        // 6. Generates a fully functional REST API with secure field mapping
        UniversalServer.ignite("com.carebridge.entities", config -> {
            
            // --- PLUG IN LEGACY CAREBRIDGE LOGIC ---
            // We can still add our custom security handlers and hand-written routes!
            AccessController accessController = new AccessController();
            Routes legacyRoutes = new Routes();

            // Add the legacy routes
            config.router.apiBuilder(() -> {
                legacyRoutes.getRoutes().addEndpoints();
            });
            
            // Add the legacy JWT security middleware
            config.router.mount(router -> {
                router.beforeMatched(accessController::accessHandler);
            });
        });
        
    }
}
