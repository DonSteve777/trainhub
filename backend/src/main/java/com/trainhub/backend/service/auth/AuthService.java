package com.trainhub.backend.service.auth;

import com.trainhub.backend.config.JwtConfig;
import com.trainhub.backend.dto.request.LoginRequest;
import com.trainhub.backend.dto.request.RegisterRequest;
import com.trainhub.backend.dto.response.AuthResponse;
import com.trainhub.backend.model.RefreshToken;
import com.trainhub.backend.model.User;
import com.trainhub.backend.model.enums.AccountStatus;
import com.trainhub.backend.repository.RefreshTokenRepository;
import com.trainhub.backend.repository.UserRepository;
import com.trainhub.backend.security.UserDetailsServiceImpl;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Optional;

/**
 * Servicio de autenticación que maneja registro, login y refresh token.
 * Implementa la lógica de negocio para autenticación JWT según Security_Design.md.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtConfig jwtConfig;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    /**
     * Registra un nuevo usuario en el sistema.
     * 
     * @param request Datos del usuario a registrar
     * @return AuthResponse con access token y refresh token
     * @throws IllegalArgumentException si el email ya existe
     */
    public AuthResponse register(RegisterRequest request) {
        log.info("Intentando registrar usuario con email: {}", request.getEmail());

        // Verificar si el email ya existe
        if (userRepository.existsByEmail(request.getEmail())) {
            log.warn("Intento de registro con email ya existente: {}", request.getEmail());
            throw new IllegalArgumentException("El email ya está registrado");
        }

        // Hash de la contraseña con BCrypt (cost factor 12)
        String passwordHash = passwordEncoder.encode(request.getPassword());

        // Crear nuevo usuario con estado PENDING_CONFIRMATION
        User user = new User(
            request.getName(),
            request.getEmail(),
            passwordHash,
            AccountStatus.PENDING_CONFIRMATION
        );

        user = userRepository.save(user);
        log.info("Usuario registrado exitosamente con ID: {}", user.getId());

        // Generar tokens
        String accessToken = generateAccessToken(user);
        String refreshToken = generateRefreshToken(user);

        // Guardar refresh token en base de datos
        saveRefreshToken(user, refreshToken);

        return buildAuthResponse(user, accessToken);
    }

    /**
     * Autentica un usuario y genera tokens JWT usando AuthenticationManager.
     * 
     * @param request Credenciales de login
     * @return AuthResponse con access token y refresh token
     * @throws IllegalArgumentException si las credenciales son inválidas o la cuenta está bloqueada
     */
    public AuthResponse login(LoginRequest request) {
        log.info("Intentando login para email: {}", request.getEmail());

        try {
            // Usar AuthenticationManager para autenticar (valida credenciales automáticamente)
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    request.getEmail(),
                    request.getPassword()
                )
            );

            // Obtener el UserDetails del resultado de autenticación
            UserDetailsServiceImpl.CustomUserDetails userDetails = 
                (UserDetailsServiceImpl.CustomUserDetails) authentication.getPrincipal();
            User user = userDetails.getUser();

            // Verificar estado de la cuenta (AuthenticationManager ya verifica si está bloqueada
            // a través de isAccountNonLocked(), pero verificamos explícitamente por claridad)
            if (user.getAccountStatus() == AccountStatus.BLOCKED) {
                log.warn("Intento de login con cuenta bloqueada: {}", request.getEmail());
                throw new IllegalArgumentException("La cuenta está bloqueada");
            }

            // Actualizar último login
            user.setLastLogin(LocalDateTime.now());
            userRepository.save(user);

            log.info("Login exitoso para usuario ID: {}", user.getId());

            // Generar tokens
            String accessToken = generateAccessToken(user);
            String refreshToken = generateRefreshToken(user);

            // Revocar tokens anteriores del usuario (rotación de refresh token)
            revokeUserTokens(user);

            // Guardar nuevo refresh token
            saveRefreshToken(user, refreshToken);

            return buildAuthResponse(user, accessToken);

        } catch (BadCredentialsException | UsernameNotFoundException e) {
            log.warn("Intento de login fallido para email: {} - {}", request.getEmail(), e.getMessage());
            throw new IllegalArgumentException("Credenciales inválidas", e);
        }
    }

    /**
     * Renueva el access token usando un refresh token válido.
     * Implementa rotación de refresh token (invalida el anterior y genera uno nuevo).
     * 
     * @param refreshTokenValue El refresh token a validar
     * @return AuthResponse con nuevo access token y nuevo refresh token
     * @throws IllegalArgumentException si el refresh token es inválido, expirado o revocado
     */
    public AuthResponse refreshToken(String refreshTokenValue) {
        log.info("Intentando renovar token");

        // Hash del refresh token para buscar en BD
        String tokenHash = hashToken(refreshTokenValue);

        // Buscar refresh token en base de datos
        RefreshToken refreshToken = refreshTokenRepository.findByTokenHash(tokenHash)
            .orElseThrow(() -> {
                log.warn("Refresh token no encontrado en BD");
                return new IllegalArgumentException("Refresh token inválido");
            });

        // Validar que el token no esté revocado ni expirado
        if (!refreshToken.isValid()) {
            log.warn("Refresh token inválido (revocado o expirado) para usuario ID: {}", 
                refreshToken.getUser().getId());
            throw new IllegalArgumentException("Refresh token inválido o expirado");
        }

        User user = refreshToken.getUser();

        // Verificar estado de la cuenta
        if (user.getAccountStatus() == AccountStatus.BLOCKED) {
            log.warn("Intento de refresh con cuenta bloqueada: {}", user.getEmail());
            throw new IllegalArgumentException("La cuenta está bloqueada");
        }

        log.info("Refresh token válido, generando nuevos tokens para usuario ID: {}", user.getId());

        // Revocar el refresh token actual (rotación)
        refreshToken.setRevoked(true);
        refreshTokenRepository.save(refreshToken);

        // Generar nuevos tokens
        String newAccessToken = generateAccessToken(user);
        String newRefreshToken = generateRefreshToken(user);

        // Guardar nuevo refresh token
        saveRefreshToken(user, newRefreshToken);

        return buildAuthResponse(user, newAccessToken);
    }

    /**
     * Genera un access token JWT para el usuario.
     * 
     * @param user Usuario para el cual generar el token
     * @return Access token JWT como String
     */
    private String generateAccessToken(User user) {
        Instant now = Instant.now();
        Instant expiration = now.plusMillis(jwtConfig.getAccessTokenExpiration());

        SecretKey key = Keys.hmacShaKeyFor(jwtConfig.getSecret().getBytes(StandardCharsets.UTF_8));

        return Jwts.builder()
            .subject(String.valueOf(user.getId()))
            .claim("email", user.getEmail())
            .claim("roles", "USER") // Por ahora solo USER, se puede expandir
            .issuedAt(Date.from(now))
            .expiration(Date.from(expiration))
            .signWith(key)
            .compact();
    }

    /**
     * Genera un refresh token JWT para el usuario.
     * 
     * @param user Usuario para el cual generar el token
     * @return Refresh token JWT como String
     */
    private String generateRefreshToken(User user) {
        Instant now = Instant.now();
        Instant expiration = now.plusMillis(jwtConfig.getRefreshTokenExpiration());

        SecretKey key = Keys.hmacShaKeyFor(jwtConfig.getSecret().getBytes(StandardCharsets.UTF_8));

        return Jwts.builder()
            .subject(String.valueOf(user.getId()))
            .claim("type", "refresh")
            .issuedAt(Date.from(now))
            .expiration(Date.from(expiration))
            .signWith(key)
            .compact();
    }

    /**
     * Guarda un refresh token en la base de datos.
     * 
     * @param user Usuario propietario del token
     * @param refreshTokenValue Valor del refresh token (JWT)
     */
    private void saveRefreshToken(User user, String refreshTokenValue) {
        String tokenHash = hashToken(refreshTokenValue);
        LocalDateTime expirationDate = LocalDateTime.ofInstant(
            Instant.now().plusMillis(jwtConfig.getRefreshTokenExpiration()),
            ZoneId.systemDefault()
        );

        RefreshToken refreshToken = new RefreshToken(tokenHash, user, expirationDate);
        refreshTokenRepository.save(refreshToken);
    }

    /**
     * Revoca todos los refresh tokens válidos de un usuario.
     * 
     * @param user Usuario cuyos tokens se deben revocar
     */
    private void revokeUserTokens(User user) {
        LocalDateTime now = LocalDateTime.now();
        refreshTokenRepository.findValidTokensByUser(user, now).forEach(token -> {
            token.setRevoked(true);
            refreshTokenRepository.save(token);
        });
    }

    /**
     * Genera el hash SHA-256 de un token para almacenarlo en BD.
     * 
     * @param token Token a hashear
     * @return Hash del token como String hexadecimal
     */
    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            log.error("Error al generar hash del token", e);
            throw new RuntimeException("Error al procesar token", e);
        }
    }

    /**
     * Construye la respuesta de autenticación.
     * 
     * @param user Usuario autenticado
     * @param accessToken Access token generado
     * @return AuthResponse con token e información del usuario
     */
    private AuthResponse buildAuthResponse(User user, String accessToken) {
        AuthResponse.UserInfo userInfo = AuthResponse.UserInfo.builder()
            .id(user.getId())
            .name(user.getName())
            .email(user.getEmail())
            .bio(user.getBio())
            .photoUrl(user.getPhotoUrl())
            .emailVerified(user.getEmailVerified())
            .registrationDate(user.getRegistrationDate())
            .lastLogin(user.getLastLogin())
            .build();

        return AuthResponse.builder()
            .accessToken(accessToken)
            .user(userInfo)
            .build();
    }

    /**
     * Valida y extrae información de un access token JWT.
     * 
     * @param token Access token a validar
     * @return Claims del token si es válido
     * @throws IllegalArgumentException si el token es inválido o expirado
     */
    public Claims validateToken(String token) {
        try {
            SecretKey key = Keys.hmacShaKeyFor(jwtConfig.getSecret().getBytes(StandardCharsets.UTF_8));
            return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        } catch (Exception e) {
            log.warn("Token inválido: {}", e.getMessage());
            throw new IllegalArgumentException("Token inválido o expirado", e);
        }
    }
}

