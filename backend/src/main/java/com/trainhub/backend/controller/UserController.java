package com.trainhub.backend.controller;

import com.trainhub.backend.dto.request.UpdateUserProfileRequest;
import com.trainhub.backend.dto.response.FeedPostResponse;
import com.trainhub.backend.dto.response.FriendPreviewResponse;
import com.trainhub.backend.dto.response.StreakResponse;
import com.trainhub.backend.dto.response.UserProfileResponse;
import com.trainhub.backend.dto.response.UserSearchResult;
import com.trainhub.backend.dto.response.WeeklyConstancyResponse;
import com.trainhub.backend.enums.FriendshipStatus;
import com.trainhub.backend.model.Box;
import com.trainhub.backend.model.Friendship;
import com.trainhub.backend.model.FriendshipId;
import com.trainhub.backend.model.User;
import com.trainhub.backend.repository.BoxRepository;
import com.trainhub.backend.repository.FriendshipRepository;
import com.trainhub.backend.repository.UserRepository;
import com.trainhub.backend.security.UserPrincipal;
import com.trainhub.backend.service.FeedService;
import com.trainhub.backend.service.PostService;
import jakarta.validation.Valid;
import java.time.LocalDateTime;
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
    private BoxRepository boxRepository;

    @Autowired
    private FriendshipRepository friendshipRepository;

    @Autowired
    private PostService postService;

    @Autowired
    private FeedService feedService;

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
                user.getGender(),
                user.getRole(),
                user.getBox() != null ? user.getBox().getId() : null
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

        if (request.getBoxId() == null) {
            user.setBox(null);
        } else {
            Box box = boxRepository.findById(request.getBoxId())
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.BAD_REQUEST,
                            "El box indicado no existe"
                    ));
            user.setBox(box);
        }

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
                updatedUser.getGender(),
                updatedUser.getRole(),
                updatedUser.getBox() != null ? updatedUser.getBox().getId() : null
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
        Integer currentUserId = userPrincipal.getUser().getId();
        List<UserSearchResult> results = userRepository
                .findByUsernamePrefix(q.trim(), currentUserId, PageRequest.of(0, safeLimit))
                .stream()
                .map(u -> {
                    String status = friendshipRepository.findBetween(currentUserId, u.getId())
                            .map(f -> f.getStatus().name())
                            .orElse("NONE");
                    return new UserSearchResult(u.getId(), u.getUsername(), u.getPhotoUrl(), status);
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(results);
    }

    /**
     * Devuelve la racha actual de actividad del usuario autenticado.
     *
     * @param userPrincipal El usuario autenticado actual
     * @return Racha actual de entrenamiento del usuario
     */
    @GetMapping("/streak")
    public ResponseEntity<StreakResponse> getStreak(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        Integer userId = userPrincipal.getUser().getId();
        return ResponseEntity.ok(postService.getStreak(userId));
    }

    /**
     * Constancia semanal del usuario autenticado (semanas ISO + dots L–D).
     *
     * @param userPrincipal El usuario autenticado actual
     * @return Constancia semanal del usuario
     */
    @GetMapping("/constancy")
    public ResponseEntity<WeeklyConstancyResponse> getWeeklyConstancy(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        Integer userId = userPrincipal.getUser().getId();
        return ResponseEntity.ok(postService.getWeeklyConstancy(userId));
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

    /**
     * Devuelve el estado de la relación entre el usuario autenticado y el indicado.
     *
     * @param userPrincipal usuario autenticado
     * @param targetUserId  id del usuario a comprobar
     * @return {@code {"status": "NONE" | "PENDING" | "FRIEND"}}
     */
    @GetMapping("/{targetUserId}/friendship-status")
    public ResponseEntity<Map<String, String>> friendshipStatus(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Integer targetUserId) {

        Integer currentUserId = userPrincipal.getUser().getId();
        String status = friendshipRepository.findBetween(currentUserId, targetUserId)
                .map(f -> f.getStatus().name())
                .orElse("NONE");
        return ResponseEntity.ok(Map.of("status", status));
    }

    /**
     * Lista los amigos ya aceptados del usuario autenticado.
     */
    @GetMapping("/friends")
    public ResponseEntity<List<FriendPreviewResponse>> getFriends(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        Integer currentUserId = userPrincipal.getUser().getId();

        List<Object[]> rows = friendshipRepository.findFriendsForUser(currentUserId);
        List<FriendPreviewResponse> friends = rows.stream()
                .map(row -> {
                    Integer friendId = row[0] instanceof Number ? ((Number) row[0]).intValue() : (Integer) row[0];
                    String username = (String) row[1];
                    String photoUrl = (String) row[2];
                    return new FriendPreviewResponse(friendId, username, photoUrl);
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(friends);
    }

    /**
     * Elimina una amistad aceptada (status = FRIEND) entre el usuario autenticado y {@code targetUserId}.
     */
    @DeleteMapping("/{targetUserId}/friendship")
    public ResponseEntity<Void> removeFriendship(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Integer targetUserId) {

        Integer currentUserId = userPrincipal.getUser().getId();

        if (currentUserId.equals(targetUserId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No puedes eliminar tu propia amistad");
        }

        Friendship friendship = friendshipRepository.findBetween(currentUserId, targetUserId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Amistad no encontrada"));

        if (friendship.getStatus() != FriendshipStatus.FRIEND) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "No son amigos (amistad pendiente no eliminable con este endpoint)");
        }

        friendshipRepository.delete(friendship);
        return ResponseEntity.ok().build();
    }

    /**
     * Envía una solicitud de amistad al usuario indicado (crea fila con status PENDING).
     * Devuelve 409 si ya existe alguna relación entre ambos.
     *
     * @param userPrincipal usuario autenticado (quien envía la solicitud)
     * @param targetUserId  id del destinatario
     */
    @PostMapping("/{targetUserId}/friend-request")
    public ResponseEntity<Void> sendFriendRequest(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Integer targetUserId) {

        Integer currentUserId = userPrincipal.getUser().getId();

        if (currentUserId.equals(targetUserId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No puedes enviarte una solicitud a ti mismo");
        }

        if (friendshipRepository.findBetween(currentUserId, targetUserId).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe una relación con este usuario");
        }

        Integer userAId = Math.min(currentUserId, targetUserId);
        Integer userBId = Math.max(currentUserId, targetUserId);
        friendshipRepository.save(new Friendship(
                new FriendshipId(userAId, userBId),
                FriendshipStatus.PENDING,
                currentUserId,
                LocalDateTime.now()
        ));
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * Acepta una solicitud de amistad pendiente enviada por el usuario indicado.
     *
     * @param userPrincipal usuario autenticado (receptor de la solicitud)
     * @param requesterId   id del usuario que envió la solicitud
     */
    @PostMapping("/{requesterId}/friend-request/accept")
    public ResponseEntity<Void> acceptFriendRequest(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Integer requesterId) {

        Integer currentUserId = userPrincipal.getUser().getId();

        Friendship friendship = friendshipRepository.findBetween(currentUserId, requesterId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Solicitud no encontrada"));

        if (!friendship.getRequesterId().equals(requesterId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No puedes aceptar una solicitud que tú enviaste");
        }
        if (friendship.getStatus() != FriendshipStatus.PENDING) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "La solicitud ya fue procesada");
        }

        friendship.setStatus(FriendshipStatus.FRIEND);
        friendship.setAcceptedAt(LocalDateTime.now());
        friendshipRepository.save(friendship);
        return ResponseEntity.ok().build();
    }

    /**
     * Rechaza (elimina) una solicitud de amistad pendiente enviada por el usuario indicado.
     *
     * @param userPrincipal usuario autenticado (receptor de la solicitud)
     * @param requesterId   id del usuario que envió la solicitud
     */
    @DeleteMapping("/{requesterId}/friend-request")
    public ResponseEntity<Void> rejectFriendRequest(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Integer requesterId) {

        Integer currentUserId = userPrincipal.getUser().getId();

        Friendship friendship = friendshipRepository.findBetween(currentUserId, requesterId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Solicitud no encontrada"));

        if (!friendship.getRequesterId().equals(requesterId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No puedes rechazar una solicitud que tú enviaste");
        }
        if (friendship.getStatus() != FriendshipStatus.PENDING) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "La solicitud ya fue procesada");
        }

        friendshipRepository.delete(friendship);
        return ResponseEntity.ok().build();
    }

    /**
     * Devuelve el perfil público (sin datos sensibles) de cualquier usuario por id.
     *
     * @param userId id del usuario a consultar
     * @return perfil público del usuario (id, nombre, foto, género)
     */
    @GetMapping("/{userId}/profile")
    public ResponseEntity<UserProfileResponse> getUserProfile(
            @PathVariable Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));
        UserProfileResponse response = new UserProfileResponse(
                user.getId(),
                null,
                user.getPhotoUrl(),
                user.getUsername(),
                null,
                null,
                user.getGender()
        );
        return ResponseEntity.ok(response);
    }

    /**
     * Devuelve todos los posts de un usuario concreto, del más reciente al más antiguo.
     * Sin límite de fecha: se incluye el historial completo del usuario.
     *
     * @param userPrincipal usuario autenticado (necesario para saber qué posts ha likeado)
     * @param targetUserId  id del usuario cuyo perfil se consulta
     */
    @GetMapping("/{targetUserId}/posts")
    public ResponseEntity<List<FeedPostResponse>> getUserPosts(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Integer targetUserId) {

        Integer currentUserId = userPrincipal.getUser().getId();
        return ResponseEntity.ok(feedService.getUserPosts(targetUserId, currentUserId));
    }
}
