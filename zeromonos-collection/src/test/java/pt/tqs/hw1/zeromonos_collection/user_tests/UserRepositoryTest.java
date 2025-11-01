package pt.tqs.hw1.zeromonos_collection.user_tests;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import pt.tqs.hw1.zeromonos_collection.entity.Role;
import pt.tqs.hw1.zeromonos_collection.entity.User;
import pt.tqs.hw1.zeromonos_collection.repository.UserRepository;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class UserRepositoryTest {
    
    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("Save a new user and retrieve by email")
    void testSaveAndFindByEmail() {
        User user = User.builder()
            .name("Bob")
            .email("bob@email.com")
            .password("secret")
            .role(Role.CITIZEN)
            .build();

        userRepository.save(user);
        Optional<User> found = userRepository.findByEmail("bob@email.com");

        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Bob");
        assertThat(found.get().getRole()).isEqualTo(Role.CITIZEN);
    }

    @Test
    @DisplayName("Find by non-existent email returns empty")
    void testFindByNonExistentEmail() {
        Optional<User> found = userRepository.findByEmail("test@email.com");
        assertThat(found).isNotPresent();
    }
}
