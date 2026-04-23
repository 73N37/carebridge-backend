package com.carebridge.dao;

import com.carebridge.CareBridgeApplication;
import com.carebridge.dao.impl.UserDAO;
import com.carebridge.entities.User;
import com.carebridge.enums.Role;
import com.carebridge.exceptions.ApiRuntimeException;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = CareBridgeApplication.class)
@ActiveProfiles("test")
@Transactional
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class UserDAOTest {

    @Autowired
    private UserDAO userDAO;

    private Long createdId;
    private String email;

    @BeforeEach
    void setUp() {
        User user = new User();
        user.setName("DAO Test User");
        email = "daotest" + System.nanoTime() + "@example.com";
        user.setEmail(email);
        user.setRole(Role.USER);
        user.setPassword("password123");
        User created = userDAO.create(user);
        createdId = created.getId();
    }

    @Test
    @Order(1)
    void testCreateAndReadUser() {
        assertNotNull(createdId);
        User read = userDAO.read(createdId);
        assertEquals(email, read.getEmail());
    }

    @Test
    @Order(2)
    void testReadByEmail() {
        User read = userDAO.readByEmail(email);
        assertNotNull(read);
    }

    @Test
    @Order(3)
    void testReadAll() {
        List<User> users = userDAO.readAll();
        assertTrue(users.size() > 0);
    }

    @Test
    @Order(4)
    void testUpdateUser() {
        User patch = new User();
        patch.setName("New Name");
        User updated = userDAO.update(createdId, patch);
        assertEquals("New Name", updated.getName());
    }

    @Test
    @Order(5)
    void testErrors() {
        assertNull(userDAO.read(999999L));
        assertThrows(ApiRuntimeException.class, () -> userDAO.readByEmail(""));
        assertThrows(Exception.class, () -> userDAO.create(null));
        assertThrows(ApiRuntimeException.class, () -> userDAO.update(999999L, new User()));
        assertThrows(ApiRuntimeException.class, () -> userDAO.delete(999999L));
    }

    @Test
    @Order(6)
    void testDeleteUser() {
        userDAO.delete(createdId);
        assertNull(userDAO.read(createdId));
    }
}
