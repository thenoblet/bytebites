package gtp.bytebites.auth.controller;

import gtp.bytebites.auth.dto.request.LoginRequest;
import gtp.bytebites.auth.dto.request.RegisterRequest;
import gtp.bytebites.auth.dto.response.ErrorResponse;
import gtp.bytebites.auth.dto.response.JwtResponse;
import gtp.bytebites.auth.exception.EmailExistException;
import gtp.bytebites.auth.service.AuthService;

import gtp.bytebites.util.dto.ApiResponse;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Valid;
import jakarta.validation.Validator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Authentication controller handling user registration, login, logout, and token operations.
 * <p>
 * Provides REST endpoints for core authentication workflows including:
 * <ul>
 *   <li>User registration and validation</li>
 *   <li>JWT-based authentication</li>
 *   <li>OAuth2 integration endpoints</li>
 *   <li>Token management</li>
 *   <li>Authorization checks</li>
 * </ul>
 * </p>
 *
 * @see AuthService
 * @see JwtResponse
 * @see org.springframework.security.authentication
 */
@RestController
@RequestMapping("/api/v1/auth")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AuthController {
    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final AuthService authService;
    private final Validator validator;

    /**
     * Constructs an AuthController with required dependencies.
     *
     * @param authService the authentication service for business logic
     * @param validator the validator for request validation
     */
    public AuthController(AuthService authService, Validator validator) {
        this.authService = authService;
        this.validator = validator;
    }

    /**
     * Registers a new user account.
     * <p>
     * Validates the registration request and creates a new user if validation passes.
     * Returns a JWT response on success or appropriate error responses for failures.
     * </p>
     *
     * @param request the registration request containing user details
     * @return ResponseEntity containing either:
     *         <ul>
     *           <li>JwtResponse on success (200 OK)</li>
     *           <li>ErrorResponse for validation failures (400 Bad Request)</li>
     *           <li>ErrorResponse for email conflicts (409 Conflict)</li>
     *           <li>ErrorResponse for server errors (500 Internal Server Error)</li>
     *         </ul>
     * @throws ConstraintViolationException if request validation fails
     * @see RegisterRequest
     * @see JwtResponse
     * @see ErrorResponse
     */
    @PostMapping(
            value = "/register",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<ApiResponse<JwtResponse>> register(@RequestBody RegisterRequest request) {
        Set<ConstraintViolation<RegisterRequest>> violations =
                validator.validate(request, RegisterRequest.NonOAuthValidation.class);

        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }

        try {
            JwtResponse response = authService.register(request);
            return ResponseEntity.ok(ApiResponse.success(response, "Registration successful"));
        } catch (EmailExistException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Registration error", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("An unexpected error occurred"));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<JwtResponse>> login(@Valid @RequestBody LoginRequest request) {
        try {
            JwtResponse response = authService.login(request);
            return ResponseEntity.ok(ApiResponse.success(response, "Login successful"));
        } catch (BadCredentialsException e) {
            log.warn("Login failed for email: {}", request.email());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Invalid email or password"));
        } catch (Exception e) {
            log.error("Login error", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("An unexpected error occurred"));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<String>> logout() {
        return ResponseEntity.ok(ApiResponse.success("Logged out successfully"));
    }

    @GetMapping("/oauth2/login/success")
    public ResponseEntity<ApiResponse<Map<String, String>>> loginSuccess(@RequestParam String token) {
        return ResponseEntity.ok(ApiResponse.success(
                Map.of("token", token),
                "Login successful"
        ));
    }

    @GetMapping("/oauth2/failure")
    public ResponseEntity<ApiResponse<String>> handleFailure(@RequestParam String error) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error("OAuth2 authentication failed: " + error));
    }

    @GetMapping("/check-roles")
    public ResponseEntity<ApiResponse<Map<String, Object>>> checkRoles(Authentication authentication) {
        Map<String, Object> response = Map.of(
                "username", authentication.getName(),
                "roles", authentication.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .collect(Collectors.toList())
        );
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/auth/check-auth")
    public ResponseEntity<ApiResponse<Map<String, Object>>> checkAuthentication(Authentication authentication) {
        Map<String, Object> response = Map.of(
                "name", authentication.getName(),
                "authorities", authentication.getAuthorities()
        );
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/token-refresh")
    public ResponseEntity<ApiResponse<Map<String, Object>>> refreshToken() {
        return ResponseEntity.ok(ApiResponse.success(
                Map.of("expires_in", 3600),
                "Token refreshed successfully"
        ));
    }
}