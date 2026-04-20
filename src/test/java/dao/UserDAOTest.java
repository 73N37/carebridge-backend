package dao;

import com.carebridge.CareBridgeApplication;
import com.carebridge.dao.impl.UserDAO;
import com.carebridge.entities.User;
import com.carebridge.enums.Role;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

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
        user.setEmail("daotest@example.com");
        user.setRole(Role.USER);
        user.setPassword("password123");

        User created = userDAO.create(user);
        assertNotNull(created.getId());

        User read = userDAO.read(created.getId());
        assertEquals("daotest@example.com", read.getEmail());
    }

    @Test
    void testReadByEmail() {
        User user = new User();
        user.setName("Email Test");
        user.setEmail("emailtest@example.com");
        user.setRole(Role.USER);
        user.setPassword("pass");
        userDAO.create(user);

        User read = userDAO.readByEmail("emailtest@example.com");
        assertNotNull(read);
        assertEquals("Email Test", read.getName());
    }

    @Test
    void testUpdateUser() {
        User user = new User();
        user.setName("Original Name");
        user.setEmail("update@example.com");
        user.setRole(Role.USER);
        user.setPassword("pass");
        User created = userDAO.create(user);

        User patch = new User();
        patch.setName("New Name");
        User updated = userDAO.update(created.getId(), patch);

        assertEquals("New Name", updated.getName());
    }

    @Test
    void testDeleteUser() {
        User user = new User();
        user.setName("Delete Me");
        user.setEmail("delete@example.com");
        user.setRole(Role.USER);
        user.setPassword("delete123");
        User created = userDAO.create(user);

        userDAO.delete(created.getId());
        assertNull(userDAO.read(created.getId()));
    }
}
