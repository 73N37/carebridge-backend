package com.carebridge;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(classes = CareBridgeApplication.class)
@ActiveProfiles("test")
public class CareBridgeApplicationTest {

    @Test
    void contextLoads() {
        // Verifies that the Spring application context starts without errors
    }
}
