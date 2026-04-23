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
    void testUpdateBranchesExhaustive() {
        // Combinations to hit every IF in update method:
        // if (updated.getName() != null && !updated.getName().isBlank())
        // if (updated.getEmail() != null && !updated.getEmail().isBlank())
        // if (updated.getRole() != null)

        // Case 1: All null/blank (false branches)
        User p1 = new User();
        p1.setName(" ");
        p1.setEmail(null);
        p1.setRole(null);
        User u1 = userDAO.update(createdId, p1);
        assertEquals("DAO Test User", u1.getName());
        assertEquals(email, u1.getEmail());
        assertEquals(Role.USER, u1.getRole());

        // Case 2: Email blank, Role valid
        User p2 = new User();
        p2.setName(null);
        p2.setEmail("");
        p2.setRole(Role.ADMIN);
        User u2 = userDAO.update(createdId, p2);
        assertEquals(email, u2.getEmail());
        assertEquals(Role.ADMIN, u2.getRole());

        // Case 3: Name valid, Email valid, Role null
        User p3 = new User();
        p3.setName("NewN");
        p3.setEmail("newE@t.com");
        p3.setRole(null);
        User u3 = userDAO.update(createdId, p3);
        assertEquals("NewN", u3.getName());
        assertEquals("newE@t.com", u3.getEmail());
        assertEquals(Role.ADMIN, u3.getRole()); // Still ADMIN from previous test step in same transaction? No, fresh setup. Wait.
        // Actually, it's fresh setup PER TEST method.
    }

    @Test
    @Order(2)
    void testCreateBranches() {
        // Role null branch
        User u1 = new User("N1", "e1" + System.nanoTime() + "@t.com", "p", null);
        User c1 = userDAO.create(u1);
        assertEquals(Role.USER, c1.getRole());
        
        // Role non-null branch
        User u2 = new User("N2", "e2" + System.nanoTime() + "@t.com", "p", Role.ADMIN);
        User c2 = userDAO.create(u2);
        assertEquals(Role.ADMIN, c2.getRole());
    }

    @Test
    @Order(3)
    void testReadAndReadByEmailBranches() {
        assertNotNull(userDAO.read(createdId));
        assertNull(userDAO.read(999999L));
        
        assertNotNull(userDAO.readByEmail(email));
        assertNull(userDAO.readByEmail("none@test.com"));
        assertThrows(ApiRuntimeException.class, () -> userDAO.readByEmail(""));
    }

    @Test
    @Order(4)
    void testErrors() {
        assertThrows(ApiRuntimeException.class, () -> userDAO.create(null));
        User u = new User(); u.setName("N");
        assertThrows(ApiRuntimeException.class, () -> userDAO.create(u)); // Email null
        u.setEmail("");
        assertThrows(ApiRuntimeException.class, () -> userDAO.create(u)); // Email blank
        u.setEmail("e@t.com"); u.setName(null);
        assertThrows(ApiRuntimeException.class, () -> userDAO.create(u)); // Name null
        u.setName(" ");
        assertThrows(ApiRuntimeException.class, () -> userDAO.create(u)); // Name blank
        
        User dup = new User("D", email, "p", Role.USER);
        assertThrows(ApiRuntimeException.class, () -> userDAO.create(dup));
        
        assertThrows(ApiRuntimeException.class, () -> userDAO.update(999999L, new User()));
        assertThrows(ApiRuntimeException.class, () -> userDAO.delete(999999L));
    }

    @Test
    @Order(5)
    void testReadAll() {
        assertFalse(userDAO.readAll().isEmpty());
    }

    @Test
    @Order(6)
    void testDelete() {
        userDAO.delete(createdId);
    }
}
