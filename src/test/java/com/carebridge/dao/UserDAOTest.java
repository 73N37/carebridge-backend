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
    void testReadBranches() {
        // ID exists
        assertNotNull(userDAO.read(createdId));
        // ID not exists (list.isEmpty() branch)
        assertNull(userDAO.read(999999L));
    }

    @Test
    @Order(2)
    void testReadByEmailBranches() {
        // Success
        assertNotNull(userDAO.readByEmail(email));
        // Email not found (list.isEmpty() branch)
        assertNull(userDAO.readByEmail("notfound@test.com"));
        // Email null
        assertThrows(ApiRuntimeException.class, () -> userDAO.readByEmail(null));
        // Email blank
        assertThrows(ApiRuntimeException.class, () -> userDAO.readByEmail("  "));
    }

    @Test
    @Order(3)
    void testCreateBranches() {
        // u null
        assertThrows(ApiRuntimeException.class, () -> userDAO.create(null));
        
        // email null
        User u1 = new User();
        u1.setName("N");
        assertThrows(ApiRuntimeException.class, () -> userDAO.create(u1));
        
        // email blank
        u1.setEmail("");
        assertThrows(ApiRuntimeException.class, () -> userDAO.create(u1));
        
        // name null
        u1.setEmail("e1@t.com");
        u1.setName(null);
        assertThrows(ApiRuntimeException.class, () -> userDAO.create(u1));
        
        // name blank
        u1.setName(" ");
        assertThrows(ApiRuntimeException.class, () -> userDAO.create(u1));
        
        // role null (sets default)
        User u2 = new User();
        u2.setName("N2");
        u2.setEmail("e2@t.com");
        u2.setPassword("p");
        u2.setRole(null);
        User c2 = userDAO.create(u2);
        assertEquals(Role.USER, c2.getRole());
        
        // exists (exists branch)
        User u3 = new User();
        u3.setName("N3");
        u3.setEmail(email);
        u3.setPassword("p");
        assertThrows(ApiRuntimeException.class, () -> userDAO.create(u3));
    }

    @Test
    @Order(4)
    void testUpdateBranches() {
        // existing null
        assertThrows(ApiRuntimeException.class, () -> userDAO.update(999999L, new User()));
        
        // patch name blank/null (no change)
        User patch = new User();
        patch.setName("");
        patch.setEmail(null);
        patch.setRole(null);
        User updated = userDAO.update(createdId, patch);
        assertEquals("DAO Test User", updated.getName());
        assertEquals(email, updated.getEmail());
        
        // Success branches
        patch.setName("New");
        patch.setEmail("new@t.com");
        patch.setRole(Role.ADMIN);
        updated = userDAO.update(createdId, patch);
        assertEquals("New", updated.getName());
        assertEquals("new@t.com", updated.getEmail());
        assertEquals(Role.ADMIN, updated.getRole());
    }

    @Test
    @Order(5)
    void testDeleteBranches() {
        // u null
        assertThrows(ApiRuntimeException.class, () -> userDAO.delete(999999L));
        // Success
        userDAO.delete(createdId);
        assertNull(userDAO.read(createdId));
    }

    @Test
    @Order(6)
    void testReadAll() {
        assertFalse(userDAO.readAll().isEmpty());
    }
}
