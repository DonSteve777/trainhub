package com.trainhub.backend.service;

import com.trainhub.backend.dto.response.LikerResponse;
import com.trainhub.backend.dto.response.NotificationResponse;
import com.trainhub.backend.model.User;
import com.trainhub.backend.repository.CommentRepository;
import com.trainhub.backend.repository.FriendshipRepository;
import com.trainhub.backend.repository.PostLikeRepository;
import com.trainhub.backend.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Servicio para gestionar las notificaciones del usuario.
 * Las notificaciones se derivan en tiempo real de post_likes y comments;
 * el campo notifications_last_seen_at en users actúa como marca de agua
 * para distinguir notificaciones leídas de no leídas.
 */
@Service
public class NotificationService {

    private final PostLikeRepository postLikeRepository;
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final FriendshipRepository friendshipRepository;

    public NotificationService(PostLikeRepository postLikeRepository,
                               CommentRepository commentRepository,
                               UserRepository userRepository,
                               FriendshipRepository friendshipRepository) {
        this.postLikeRepository = postLikeRepository;
        this.commentRepository = commentRepository;
        this.userRepository = userRepository;
        this.friendshipRepository = friendshipRepository;
    }

    /**
     * Devuelve las notificaciones del usuario: likes y comentarios sobre sus posts,
     * una entrada por (tipo, post). Ordenadas por fecha de la última acción (más reciente primero).
     */
    public List<NotificationResponse> getNotifications(Integer userId) {
        User owner = findUser(userId);
        LocalDateTime lastSeen = owner.getNotificationsLastSeenAt();

        List<NotificationResponse> result = new ArrayList<>();
        result.addAll(buildLikeNotifications(userId, lastSeen));
        result.addAll(buildCommentNotifications(userId, lastSeen));
        result.addAll(buildFriendRequestNotifications(userId, lastSeen));

        result.sort(Comparator.comparing(NotificationResponse::getLastActionAt).reversed());
        return result;
    }

    /**
     * Cuenta cuántos posts del usuario tienen al menos una acción no leída
     * (like o comentario cuya fecha es posterior a notifications_last_seen_at).
     */
    public long getUnreadCount(Integer userId) {
        User owner = findUser(userId);
        LocalDateTime lastSeen = owner.getNotificationsLastSeenAt();

        long unreadLikes = countUnreadByType(
                postLikeRepository.findLikesOnUserPosts(userId), lastSeen);
        long unreadComments = countUnreadByType(
                commentRepository.findCommentsOnUserPosts(userId), lastSeen);
        long unreadFriendRequests = countUnreadFriendRequests(userId, lastSeen);

        return unreadLikes + unreadComments + unreadFriendRequests;
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

    // ----------------------------------------------------------------
    // Helpers privados
    // ----------------------------------------------------------------

    private List<NotificationResponse> buildLikeNotifications(Integer userId, LocalDateTime lastSeen) {
        List<Object[]> rows = postLikeRepository.findLikesOnUserPosts(userId);

        Map<Integer, List<Object[]>> byPost = new LinkedHashMap<>();
        for (Object[] row : rows) {
            Integer postId = (Integer) row[0];
            byPost.computeIfAbsent(postId, k -> new ArrayList<>()).add(row);
        }

        List<NotificationResponse> result = new ArrayList<>();
        for (Map.Entry<Integer, List<Object[]>> entry : byPost.entrySet()) {
            List<Object[]> likes = entry.getValue();
            Object[] first = likes.get(0);

            Integer postId            = (Integer)       first[0];
            LocalDateTime postDate    = (LocalDateTime) first[1];
            String actorUsername      = (String)        first[3];
            String actorPhotoUrl      = (String)        first[4];
            LocalDateTime lastActionAt = (LocalDateTime) first[5];

            boolean unread = lastSeen == null || lastActionAt.isAfter(lastSeen);

            result.add(new NotificationResponse(
                    "LIKE", postId, postDate, actorUsername, actorPhotoUrl,
                    lastActionAt, likes.size(), unread
            ));
        }
        return result;
    }

    private List<NotificationResponse> buildCommentNotifications(Integer userId, LocalDateTime lastSeen) {
        List<Object[]> rows = commentRepository.findCommentsOnUserPosts(userId);

        Map<Integer, List<Object[]>> byPost = new LinkedHashMap<>();
        for (Object[] row : rows) {
            Integer postId = (Integer) row[0];
            byPost.computeIfAbsent(postId, k -> new ArrayList<>()).add(row);
        }

        List<NotificationResponse> result = new ArrayList<>();
        for (Map.Entry<Integer, List<Object[]>> entry : byPost.entrySet()) {
            List<Object[]> comments = entry.getValue();
            Object[] first = comments.get(0);

            Integer postId             = (Integer)       first[0];
            LocalDateTime postDate     = (LocalDateTime) first[1];
            String actorUsername       = (String)        first[3];
            String actorPhotoUrl       = (String)        first[4];
            LocalDateTime lastActionAt = (LocalDateTime) first[5];

            boolean unread = lastSeen == null || lastActionAt.isAfter(lastSeen);

            result.add(new NotificationResponse(
                    "COMMENT", postId, postDate, actorUsername, actorPhotoUrl,
                    lastActionAt, comments.size(), unread
            ));
        }
        return result;
    }

    /**
     * Cuenta posts distintos cuya acción más reciente es posterior a lastSeen.
     * Las rows siguen el formato [postId, ..., ..., ..., ..., actionDate].
     */
    private long countUnreadByType(List<Object[]> rows, LocalDateTime lastSeen) {
        if (lastSeen == null) {
            return rows.stream().map(r -> (Integer) r[0]).distinct().count();
        }

        Map<Integer, LocalDateTime> latestByPost = new LinkedHashMap<>();
        for (Object[] row : rows) {
            Integer postId = (Integer) row[0];
            LocalDateTime actionAt = (LocalDateTime) row[5];
            latestByPost.merge(postId, actionAt,
                    (existing, incoming) -> incoming.isAfter(existing) ? incoming : existing);
        }

        return latestByPost.values().stream()
                .filter(latest -> latest.isAfter(lastSeen))
                .count();
    }

    private List<NotificationResponse> buildFriendRequestNotifications(
            Integer userId, LocalDateTime lastSeen) {

        List<Object[]> rows = friendshipRepository.findPendingRequestsForUser(userId);
        List<NotificationResponse> result = new ArrayList<>();

        for (Object[] row : rows) {
            Integer actorId            = (Integer)       row[0];
            LocalDateTime createdAt    = (LocalDateTime) row[1];
            String actorUsername       = (String)        row[2];
            String actorPhotoUrl       = (String)        row[3];

            boolean unread = lastSeen == null || createdAt.isAfter(lastSeen);

            NotificationResponse n = new NotificationResponse(
                    "FRIEND_REQUEST",
                    null,
                    null,
                    actorUsername,
                    actorPhotoUrl,
                    createdAt,
                    1,
                    unread
            );
            n.setActorId(actorId);
            result.add(n);
        }
        return result;
    }

    private long countUnreadFriendRequests(Integer userId, LocalDateTime lastSeen) {
        List<Object[]> rows = friendshipRepository.findPendingRequestsForUser(userId);
        if (lastSeen == null) return rows.size();
        return rows.stream()
                .filter(row -> ((LocalDateTime) row[1]).isAfter(lastSeen))
                .count();
    }

    private User findUser(Integer userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));
    }
}
