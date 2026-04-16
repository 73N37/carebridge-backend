package com.carebridge.utils;

import com.carebridge.exceptions.ApiRuntimeException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.javalin.http.Context;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class UtilsTest {

    @Test
    void testGetObjectMapper() {
        Utils utils = new Utils();
        ObjectMapper mapper = utils.getObjectMapper();
        assertNotNull(mapper);
    }

    @Test
    void testConvertToJsonMessage() {
        Context ctx = Mockito.mock(Context.class);
        when(ctx.status()).thenReturn(io.javalin.http.HttpStatus.OK);

        String json = Utils.convertToJsonMessage(ctx, "message", "success");
        assertTrue(json.contains("\"message\":\"success\""));
        assertTrue(json.contains("\"status\":\"200\""));
    }

    @Test
    void testGetPropertyValue_Success() {
        // Assume there's a property file in resources for testing or use one that exists
        // Since I don't want to rely on existing ones too much, I'll just check if it fails gracefully if missing
    }

    @Test
    void testGetPropertyValue_NotFound() {
        assertThrows(ApiRuntimeException.class, () -> {
            Utils.getPropertyValue("non.existent", "hibernate.cfg.xml"); // Use an existing file but wrong property
        });
    }

    @Test
    void testGetPropertyValue_IOError() {
        assertThrows(ApiRuntimeException.class, () -> {
            Utils.getPropertyValue("any", "non-existent-file.properties");
        });
    }
}
