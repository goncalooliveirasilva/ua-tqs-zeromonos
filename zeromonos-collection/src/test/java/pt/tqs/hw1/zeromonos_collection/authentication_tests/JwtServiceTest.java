package pt.tqs.hw1.zeromonos_collection.authentication_tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UserDetails;

import pt.tqs.hw1.zeromonos_collection.entity.Role;
import pt.tqs.hw1.zeromonos_collection.entity.User;
import pt.tqs.hw1.zeromonos_collection.service.JwtService;

class JwtServiceTest {

    @Test
    @DisplayName("Test token generation and validation.")
    void testGenerateAndValidateToken() {
        String secretKey = "dGhpc2lzYXNlY3JldGtleWZvcnRlc3RpbmcxMjM0abcd5678";
        JwtService service = new JwtService(secretKey);
        UserDetails user = User.builder()
            .email("test@email.com")
            .password("pass")
            .name("name")
            .role(Role.CITIZEN)
            .build();
        String token = service.generateToken(user);

        assertTrue(service.isTokenValid(token, user));
        assertEquals("test@email.com", service.getUsername(token));
    }
}
