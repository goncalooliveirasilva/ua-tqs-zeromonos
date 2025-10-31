package pt.tqs.hw1.zeromonos_collection.authentication_tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import pt.tqs.hw1.zeromonos_collection.auth.AuthenticationRequest;
import pt.tqs.hw1.zeromonos_collection.auth.AuthenticationResponse;
import pt.tqs.hw1.zeromonos_collection.auth.RegisterRequest;
import pt.tqs.hw1.zeromonos_collection.entity.User;
import pt.tqs.hw1.zeromonos_collection.repository.UserRepository;
import pt.tqs.hw1.zeromonos_collection.service.AuthenticationService;
import pt.tqs.hw1.zeromonos_collection.service.JwtService;

@ExtendWith(MockitoExtension.class)
public class AuthenticationServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthenticationService service;

    @Test
    @DisplayName("Test user register.")
    void testRegister() {
        RegisterRequest request = RegisterRequest.builder()
            .name("Bob")
            .email("bob@email.com")
            .password("pass")
            .build();
        when(passwordEncoder.encode("pass")).thenReturn("encoded");
        when(jwtService.generateToken(any())).thenReturn("jwt-token");

        AuthenticationResponse response = service.register(request);
        assertEquals("jwt-token", response.getToken());
    }

    @Test
    @DisplayName("Test user authentication.")
    void testAuthenticate() {
        AuthenticationRequest request = AuthenticationRequest.builder()
            .email("bob@email.com")
            .password("pass")
            .build();

        when(authenticationManager.authenticate(any())).thenReturn(
            new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );
        User user = User.builder()
            .email("bob@email.com")
            .password("pass")
            .name("Bob")
            .build();
        when(userRepository.findByEmail("bob@email.com")).thenReturn(Optional.of(user));
        when(jwtService.generateToken(user)).thenReturn("jwt-token");

        AuthenticationResponse response = service.authenticate(request);
    
        assertEquals("jwt-token", response.getToken());
    }
}
