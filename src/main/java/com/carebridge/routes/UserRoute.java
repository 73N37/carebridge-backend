package com.carebridge.routes;

import com.carebridge.controllers.impl.UserController;
import com.carebridge.enums.Role;

import io.javalin.apibuilder.EndpointGroup;

import static io.javalin.apibuilder.ApiBuilder.*;

public class UserRoute {
    private final UserController controller = new UserController();

    public EndpointGroup getRoutes() {
        return () -> {
            get("me", controller::me, Role.USER, Role.ADMIN, Role.CAREWORKER, Role.GUARDIAN);
            get("", controller::readAll, Role.ADMIN);
            get("{id}", controller::read, Role.ADMIN);


            post("", controller::create, Role.ADMIN);
            put("{id}", controller::update, Role.ADMIN);
            delete("{id}", controller::delete, Role.ADMIN);
            post("{id}/link-residents", controller::linkResidents, Role.ADMIN);
        };
    }
}
