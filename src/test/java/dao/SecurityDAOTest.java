package dao;

import com.carebridge.CareBridgeApplication;
import com.carebridge.dao.security.SecurityDAO;
import com.carebridge.entities.User;
import com.carebridge.enums.Role;
import com.carebridge.exceptions.ValidationException;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = CareBridgeApplication.class)
@ActiveProfiles("test")
@Transactional
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class SecurityDAOTest {

    @Autowired
    private SecurityDAO securityDAO;

    private static String email;

    @Test
    @Order(1)
    void testCreateUser() {
        email = "sec" + System.nanoTime() + "@test.com";
        User user = securityDAO.createUser("Sec", email, "pass", "DN", "DE", "DP", "IE", "IP", Role.USER);
        assertNotNull(user.getId());
    }

    @Test
    @Order(2)
    void testVerifyUser() throws ValidationException {
        User verified = securityDAO.getVerifiedUser(email, "pass");
        assertNotNull(verified);
        assertEquals(email, verified.getEmail());

        assertThrows(ValidationException.class, () -> securityDAO.getVerifiedUser(email, "wrong"));
        assertThrows(ValidationException.class, () -> securityDAO.getVerifiedUser("none", "pass"));
    }

    @Test
    @Order(3)
    void testChangeRole() throws ValidationException {
        User user = securityDAO.getVerifiedUser(email, "pass");
        User updated = securityDAO.changeRole(user.getId(), Role.ADMIN);
        assertEquals(Role.ADMIN, updated.getRole());
        
        assertNull(securityDAO.changeRole(999999L, Role.USER));
    }
}
