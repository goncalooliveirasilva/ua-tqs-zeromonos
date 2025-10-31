package pt.tqs.hw1.zeromonos_collection.authentication_tests;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import pt.tqs.hw1.zeromonos_collection.auth.RegisterRequest;
import pt.tqs.hw1.zeromonos_collection.repository.UserRepository;

@SpringBootTest // to load the entire context (security, etc.)
@AutoConfigureMockMvc // to just simulate http calls
public class AuthenticationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void cleanDatabase() {
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("Test /register endpoint")
    void testRegister() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
            .name("Bob")
            .email("bob@email.com")
            .password("pass")
            .build();

        mockMvc.perform(post("/api/v1/auth/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.token").exists());
    }

    @Test
    @DisplayName("Test /login endpoint.")
    void testLogin() throws Exception {
        RegisterRequest register = RegisterRequest.builder()
            .name("Bob")
            .email("bob@email.com")
            .password("pass")
            .build();

        mockMvc.perform(post("/api/v1/auth/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(register)))
            .andExpect(status().isOk());

        RegisterRequest login = RegisterRequest.builder()
            .name("Bob")
            .email("bob@email.com")
            .password("pass")
            .build();

        mockMvc.perform(post("/api/v1/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(login)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.token").exists());
    }

    @Test
    @DisplayName("Registering existing email should throw exception")
    void testRegisterDuplicateEmail() throws Exception {
        RegisterRequest register = RegisterRequest.builder()
            .name("Bob")
            .email("bob@email.com")
            .password("pass")
            .build();

        mockMvc.perform(post("/api/v1/auth/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(register)))
            .andExpect(status().isOk());

        mockMvc.perform(post("/api/v1/auth/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(register)))
            .andExpect(status().isConflict());
        
    }
}
