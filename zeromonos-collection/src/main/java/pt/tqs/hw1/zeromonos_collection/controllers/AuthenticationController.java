package pt.tqs.hw1.zeromonos_collection.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import pt.tqs.hw1.zeromonos_collection.auth.AuthenticationRequest;
import pt.tqs.hw1.zeromonos_collection.auth.AuthenticationResponse;
import pt.tqs.hw1.zeromonos_collection.auth.RegisterRequest;
import pt.tqs.hw1.zeromonos_collection.entity.Role;
import pt.tqs.hw1.zeromonos_collection.service.AuthenticationService;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Slf4j
class AuthenticationController {

    private final AuthenticationService service;

    @PostMapping("/register")
    public ResponseEntity<AuthenticationResponse> register(
        @RequestBody RegisterRequest request,
        @RequestParam Role role) {
            try {
            log.info("Registration request for email={}", request.getEmail());
            return ResponseEntity.ok(service.register(request, role));
        } catch (IllegalStateException e) {
            log.error("Registration request failed:", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(null);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> authenticate(@RequestBody AuthenticationRequest request) {
        log.info("Authentication request for email={}", request.getEmail());
        return ResponseEntity.ok(service.authenticate(request));
    }
}
