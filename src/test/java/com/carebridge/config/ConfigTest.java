package com.carebridge.config;

import com.carebridge.CareBridgeApplication;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = CareBridgeApplication.class)
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ConfigTest {

    @Autowired
    private ApplicationContext context;

    @Test
    @Order(1)
    void testApplicationConfig() {
        ObjectMapper mapper = context.getBean(ObjectMapper.class);
        assertNotNull(mapper);
    }
}
