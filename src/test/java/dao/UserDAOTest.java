package dao;

import com.carebridge.CareBridgeApplication;
import com.carebridge.dao.impl.UserDAO;
import com.carebridge.entities.User;
import com.carebridge.enums.Role;
import com.carebridge.exceptions.ApiRuntimeException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = CareBridgeApplication.class)
@ActiveProfiles("test")
@Transactional
public class UserDAOTest {

    @Autowired
    private UserDAO userDAO;

    @Test
    void testCreateAndReadUser() {
        User user = new User();
        user.setName("DAO Test User");
        String email = "daotest" + System.nanoTime() + "@example.com";
        user.setEmail(email);
        user.setRole(Role.USER);
        user.setPassword("password123");

        User created = userDAO.create(user);
        assertNotNull(created.getId());

        User read = userDAO.read(created.getId());
        assertNotNull(read);
        assertEquals(email, read.getEmail());
    }

    @Test
    void testReadNonExistentUser() {
        assertNull(userDAO.read(999999L));
    }

    @Test
    void testReadByEmail() {
        User user = new User();
        user.setName("Email Test");
        String email = "emailtest" + System.nanoTime() + "@example.com";
        user.setEmail(email);
        user.setRole(Role.USER);
        user.setPassword("pass");
        userDAO.create(user);

        User read = userDAO.readByEmail(email);
        assertNotNull(read);
        assertEquals("Email Test", read.getName());
    }

    @Test
    void testReadByBlankEmail() {
        assertThrows(ApiRuntimeException.class, () -> userDAO.readByEmail(""));
        assertThrows(ApiRuntimeException.class, () -> userDAO.readByEmail(null));
    }

    @Test
    void testReadAll() {
        User user = new User();
        user.setName("All Test");
        user.setEmail("all" + System.nanoTime() + "@example.com");
        user.setRole(Role.USER);
        user.setPassword("pass");
        userDAO.create(user);

        List<User> users = userDAO.readAll();
        assertTrue(users.size() > 0);
    }

    @Test
    void testCreateNullUser() {
        assertThrows(ApiRuntimeException.class, () -> userDAO.create(null));
    }

    @Test
    void testCreateUserWithBlankFields() {
        User user = new User();
        assertThrows(ApiRuntimeException.class, () -> userDAO.create(user)); // Missing email/name
        
        user.setEmail("test" + System.nanoTime() + "@test.com");
        assertThrows(ApiRuntimeException.class, () -> userDAO.create(user)); // Missing name
    }

    @Test
    void testCreateDuplicateEmail() {
        String email = "duplicate" + System.nanoTime() + "@example.com";
        User user1 = new User();
        user1.setName("User 1");
        user1.setEmail(email);
        user1.setPassword("pass");
        userDAO.create(user1);

        User user2 = new User();
        user2.setName("User 2");
        user2.setEmail(email);
        user2.setPassword("pass");
        assertThrows(ApiRuntimeException.class, () -> userDAO.create(user2));
    }

    @Test
    void testUpdateUser() {
        User user = new User();
        user.setName("Original Name");
        user.setEmail("update" + System.nanoTime() + "@example.com");
        user.setRole(Role.USER);
        user.setPassword("pass");
        User created = userDAO.create(user);

        User patch = new User();
        patch.setName("New Name");
        patch.setEmail("newemail" + System.nanoTime() + "@example.com");
        patch.setRole(Role.ADMIN);
        User updated = userDAO.update(created.getId(), patch);

        assertEquals("New Name", updated.getName());
        assertNotNull(updated.getEmail());
        assertEquals(Role.ADMIN, updated.getRole());
    }

    @Test
    void testDeleteUser() {
        User user = new User();
        user.setName("Delete Me");
        user.setEmail("delete" + System.nanoTime() + "@example.com");
        user.setRole(Role.USER);
        user.setPassword("delete123");
        User created = userDAO.create(user);

        userDAO.delete(created.getId());
        assertNull(userDAO.read(created.getId()));
    }

    @Test
    void testDeleteNonExistentUser() {
        assertThrows(ApiRuntimeException.class, () -> userDAO.delete(999999L));
    }
}
