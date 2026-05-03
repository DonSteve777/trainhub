package com.trainhub.backend.controller;

import com.trainhub.backend.dto.request.UpdateUserProfileRequest;
import com.trainhub.backend.dto.response.UserProfileResponse;
import com.trainhub.backend.dto.response.UserSearchResult;
import com.trainhub.backend.dto.response.UserTimeHistoryResponse;
import com.trainhub.backend.model.User;
import com.trainhub.backend.repository.UserRepository;
import com.trainhub.backend.security.UserPrincipal;
import com.trainhub.backend.service.PostService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Controlador REST para operaciones de usuario.
 */
@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PostService postService;

    @Value("${app.upload.dir}")
    private String uploadDir;

    @Value("${app.base-url}")
    private String baseUrl;

    /**
     * Obtiene la información del perfil del usuario autenticado.
     *
     * @param userPrincipal El usuario autenticado actual
     * @return La información del perfil del usuario
     */
    @GetMapping("/me")
    public ResponseEntity<UserProfileResponse> getCurrentUser(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        User user = userPrincipal.getUser();
        
        UserProfileResponse response = new UserProfileResponse(
                user.getId(),
                user.getEmail(),
                user.getPhotoUrl(),
                user.getUsername(),
                user.getAccountStatus(),
                user.getEmailVerified(),
                user.getGender()
        );
        
        return ResponseEntity.ok(response);
    }

    /**
     * Actualiza el perfil del usuario autenticado.
     *
     * @param userPrincipal El usuario autenticado actual
     * @param request Los datos del perfil a actualizar
     * @return El perfil actualizado
     */
    @PutMapping("/profile")
    public ResponseEntity<UserProfileResponse> updateProfile(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody UpdateUserProfileRequest request) {
        
        User user = userPrincipal.getUser();

        // Verificar si el email ha cambiado y si ya está en uso por otro usuario
        if (!user.getEmail().equals(request.getEmail())) {
            Optional<User> existingUser = userRepository.findByEmail(request.getEmail());
            if (existingUser.isPresent() && !existingUser.get().getId().equals(user.getId())) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "El email ya está en uso por otro usuario"
                );
            }
        }

        // Actualizar los campos del usuario
        user.setEmail(request.getEmail());
        user.setPhotoUrl(request.getPhotoUrl());
        user.setUsername(request.getName());
        user.setGender(request.getGender());

        // Guardar cambios
        User updatedUser = userRepository.save(user);

        // Crear respuesta
        UserProfileResponse response = new UserProfileResponse(
                updatedUser.getId(),
                updatedUser.getEmail(),
                updatedUser.getPhotoUrl(),
                updatedUser.getUsername(),
                updatedUser.getAccountStatus(),
                updatedUser.getEmailVerified(),
                updatedUser.getGender()
        );

        return ResponseEntity.ok(response);
    }

    /**
     * Busca usuarios por prefijo de username (autocomplete).
     * Excluye al propio usuario y devuelve máximo {@code limit} resultados.
     *
     * @param userPrincipal El usuario autenticado actual
     * @param q             Prefijo a buscar (mínimo 1 carácter)
     * @param limit         Número máximo de resultados (máx. 10)
     */
    @GetMapping("/search")
    public ResponseEntity<List<UserSearchResult>> searchUsers(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestParam String q,
            @RequestParam(defaultValue = "5") int limit) {

        if (q == null || q.isBlank()) return ResponseEntity.ok(List.of());

        int safeLimit = Math.min(Math.max(limit, 1), 10);
        List<UserSearchResult> results = userRepository
                .findByUsernamePrefix(q.trim(), userPrincipal.getUser().getId(), PageRequest.of(0, safeLimit))
                .stream()
                .map(u -> new UserSearchResult(u.getId(), u.getUsername(), u.getPhotoUrl()))
                .collect(Collectors.toList());

        return ResponseEntity.ok(results);
    }

    /**
     * Devuelve el histórico de tiempos del usuario autenticado agrupado en tres colecciones:
     * total, workouts y runs, cada una con pares (tiempo, fecha) ordenados cronológicamente.
     *
     * @param userPrincipal El usuario autenticado actual
     * @return Histórico de tiempos del usuario
     */
    @GetMapping("/time-history")
    public ResponseEntity<UserTimeHistoryResponse> getTimeHistory(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        Integer userId = userPrincipal.getUser().getId();
        return ResponseEntity.ok(postService.getUserTimeHistory(userId));
    }

    /**
     * Sube la foto de avatar del usuario autenticado.
     *
     * @param userPrincipal El usuario autenticado actual
     * @param file          El fichero de imagen enviado como multipart/form-data
     * @return La URL pública donde quedó almacenado el avatar
     */
    @PostMapping(value = "/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, String>> uploadAvatar(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestParam("file") MultipartFile file) {

        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No se ha proporcionado ningún fichero");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new ResponseStatusException(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "El fichero debe ser una imagen");
        }

        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf('.'));
        }

        String filename = UUID.randomUUID() + extension;

        try {
            Path uploadPath = Paths.get(uploadDir);
            Files.createDirectories(uploadPath);
            Path destination = uploadPath.resolve(filename);
            file.transferTo(destination.toFile());
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error al guardar el fichero");
        }

        String url = baseUrl + "/uploads/avatars/" + filename;
        return ResponseEntity.ok(Map.of("url", url));
    }
}
