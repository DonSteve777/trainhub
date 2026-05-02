package com.trainhub.backend.service;

import com.trainhub.backend.dto.response.LikerResponse;
import com.trainhub.backend.dto.response.NotificationResponse;
import com.trainhub.backend.model.User;
import com.trainhub.backend.repository.PostLikeRepository;
import com.trainhub.backend.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Servicio para gestionar las notificaciones de likes sobre posts del usuario.
 * Las notificaciones se derivan en tiempo real de la tabla post_likes;
 * el campo notifications_last_seen_at en users actúa como marca de agua
 * para distinguir notificaciones leídas de no leídas.
 */
@Service
public class NotificationService {

    private final PostLikeRepository postLikeRepository;
    private final UserRepository userRepository;

    public NotificationService(PostLikeRepository postLikeRepository, UserRepository userRepository) {
        this.postLikeRepository = postLikeRepository;
        this.userRepository = userRepository;
    }

    /**
     * Devuelve las notificaciones del usuario: una entrada por cada post suyo
     * que haya recibido al menos un like, con los datos del liker más reciente.
     * Ordenadas por fecha del último like (más reciente primero).
     */
    public List<NotificationResponse> getNotifications(Integer userId) {
        User owner = findUser(userId);
        List<Object[]> rows = postLikeRepository.findLikesOnUserPosts(userId);
        LocalDateTime lastSeen = owner.getNotificationsLastSeenAt();

        // Agrupa filas por postId preservando el orden (primera fila = like más reciente)
        Map<Integer, List<Object[]>> byPost = new LinkedHashMap<>();
        for (Object[] row : rows) {
            Integer postId = (Integer) row[0];
            byPost.computeIfAbsent(postId, k -> new ArrayList<>()).add(row);
        }

        List<NotificationResponse> result = new ArrayList<>();
        for (Map.Entry<Integer, List<Object[]>> entry : byPost.entrySet()) {
            List<Object[]> likes = entry.getValue();
            Object[] first = likes.get(0); // el like más reciente (ORDER BY createdAt DESC)

            Integer postId              = (Integer)       first[0];
            LocalDateTime postDate      = (LocalDateTime) first[1];
            String lastLikerUsername    = (String)        first[3];
            String lastLikerPhotoUrl    = (String)        first[4];
            LocalDateTime lastLikedAt   = (LocalDateTime) first[5];

            boolean unread = lastSeen == null || lastLikedAt.isAfter(lastSeen);

            result.add(new NotificationResponse(
                    postId, postDate, lastLikerUsername, lastLikerPhotoUrl,
                    lastLikedAt, likes.size(), unread
            ));
        }

        return result;
    }

    /**
     * Cuenta cuántos posts del usuario tienen al menos un like no leído
     * (es decir, cuyo like más reciente es posterior a notifications_last_seen_at).
     */
    public long getUnreadCount(Integer userId) {
        User owner = findUser(userId);
        List<Object[]> rows = postLikeRepository.findLikesOnUserPosts(userId);
        LocalDateTime lastSeen = owner.getNotificationsLastSeenAt();

        if (lastSeen == null) {
            // Todos son no leídos: contar posts distintos con al menos un like
            return rows.stream()
                    .map(r -> (Integer) r[0])
                    .distinct()
                    .count();
        }

        // Agrupar por post y ver si el like más reciente de cada post es posterior a lastSeen
        Map<Integer, LocalDateTime> latestLikeByPost = new LinkedHashMap<>();
        for (Object[] row : rows) {
            Integer postId = (Integer) row[0];
            LocalDateTime likedAt = (LocalDateTime) row[5];
            latestLikeByPost.merge(postId, likedAt, (existing, incoming) ->
                    incoming.isAfter(existing) ? incoming : existing);
        }

        return latestLikeByPost.values().stream()
                .filter(latest -> latest.isAfter(lastSeen))
                .count();
    }

    /**
     * Actualiza la marca de agua del usuario al momento actual,
     * marcando todas las notificaciones existentes como vistas.
     */
    @Transactional
    public void markSeen(Integer userId) {
        User user = findUser(userId);
        user.setNotificationsLastSeenAt(LocalDateTime.now());
        userRepository.save(user);
    }

    /**
     * Devuelve la lista de usuarios que dieron like a un post concreto,
     * ordenados por fecha descendente.
     */
    public List<LikerResponse> getPostLikers(Integer postId) {
        List<Object[]> rows = postLikeRepository.findLikersByPostId(postId);
        List<LikerResponse> result = new ArrayList<>();
        for (Object[] row : rows) {
            result.add(new LikerResponse(
                    (Integer)       row[0],
                    (String)        row[1],
                    (String)        row[2],
                    (LocalDateTime) row[3]
            ));
        }
        return result;
    }

    private User findUser(Integer userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));
    }
}
