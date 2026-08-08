package com.trainhub.backend.service;

import com.trainhub.backend.dto.response.FeedPostResponse;
import com.trainhub.backend.dto.response.LikeToggleResponse;
import com.trainhub.backend.dto.response.ParticipationToggleResponse;
import com.trainhub.backend.enums.PostType;
import com.trainhub.backend.model.Post;
import com.trainhub.backend.model.PostLike;
import com.trainhub.backend.model.PostLikeId;
import com.trainhub.backend.model.PostParticipant;
import com.trainhub.backend.model.PostParticipantId;
import com.trainhub.backend.model.User;
import com.trainhub.backend.repository.CommentRepository;
import com.trainhub.backend.repository.PostLikeRepository;
import com.trainhub.backend.repository.PostParticipantRepository;
import com.trainhub.backend.repository.PostRepository;
import com.trainhub.backend.repository.UserRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Servicio para obtener el feed de posts de amigos con paginación keyset.
 */
@Service
public class FeedService {

    private final PostRepository postRepository;
    private final PostLikeRepository postLikeRepository;
    private final PostParticipantRepository postParticipantRepository;
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;

    public FeedService(PostRepository postRepository, PostLikeRepository postLikeRepository,
                       PostParticipantRepository postParticipantRepository,
                       CommentRepository commentRepository, UserRepository userRepository) {
        this.postRepository = postRepository;
        this.postLikeRepository = postLikeRepository;
        this.postParticipantRepository = postParticipantRepository;
        this.commentRepository = commentRepository;
        this.userRepository = userRepository;
    }

    public List<FeedPostResponse> getFirstPage(Integer userId, Integer boxId, int size) {
        List<Post> posts = postRepository.findFeedFirstPage(userId, boxId, PageRequest.of(0, size));
        return toResponseList(posts, userId);
    }

    public List<FeedPostResponse> getNextPage(Integer userId, Integer boxId, LocalDateTime cursorDate, Integer cursorId, int size) {
        List<Post> posts = postRepository.findFeedWithCursor(userId, boxId, cursorDate, cursorId, PageRequest.of(0, size));
        return toResponseList(posts, userId);
    }

    /**
     * Devuelve todos los posts de un usuario concreto (sin límite de fecha),
     * enriquecidos con likes, comentarios y si el usuario actual los ha likeado.
     *
     * @param targetUserId  id del usuario cuyo perfil se consulta
     * @param currentUserId id del usuario autenticado que hace la petición
     */
    public List<FeedPostResponse> getUserPosts(Integer targetUserId, Integer currentUserId) {
        List<Post> posts = postRepository.findPostsByUserId(targetUserId);
        return toResponseList(posts, currentUserId);
    }

    /**
     * Da o quita like al post indicado para el usuario dado (toggle).
     * Si el usuario aún no había dado like, lo crea. Si ya lo había dado, lo elimina.
     *
     * @return estado nuevo del like y el conteo actualizado.
     */
    @Transactional
    public LikeToggleResponse toggleLike(Integer postId, Integer userId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Post no encontrado"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));

        PostLikeId likeId = new PostLikeId(postId, userId);
        boolean alreadyLiked = postLikeRepository.existsById(likeId);

        if (alreadyLiked) {
            postLikeRepository.deleteById(likeId);
        } else {
            postLikeRepository.save(new PostLike(post, user, LocalDateTime.now()));
        }

        long newCount = postLikeRepository.countByPostId(postId);
        return new LikeToggleResponse(!alreadyLiked, newCount);
    }

    /**
     * Apunta o desapunta al usuario de un reto de box (toggle).
     *
     * @return estado nuevo de la participación y el conteo actualizado.
     */
    @Transactional
    public ParticipationToggleResponse toggleParticipation(Integer postId, Integer userId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Post no encontrado"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));

        if (post.getPostType() != PostType.BOX_CHALLENGE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Solo se puede participar en retos de box");
        }

        PostParticipantId participantId = new PostParticipantId(postId, userId);
        boolean alreadyJoined = postParticipantRepository.existsById(participantId);

        if (alreadyJoined) {
            postParticipantRepository.deleteById(participantId);
        } else {
            postParticipantRepository.save(new PostParticipant(post, user, LocalDateTime.now()));
        }

        long newCount = postParticipantRepository.countByPostId(postId);
        return new ParticipationToggleResponse(!alreadyJoined, newCount);
    }

    private List<FeedPostResponse> toResponseList(List<Post> posts, Integer userId) {
        if (posts.isEmpty()) return List.of();

        List<Integer> postIds = posts.stream().map(Post::getId).collect(Collectors.toList());

        Map<Integer, Integer> likeCountByPostId = postLikeRepository.countByPostIds(postIds)
                .stream()
                .collect(Collectors.toMap(
                        row -> (Integer) row[0],
                        row -> ((Long) row[1]).intValue()
                ));

        Map<Integer, Integer> commentCountByPostId = commentRepository.countByPostIds(postIds)
                .stream()
                .collect(Collectors.toMap(
                        row -> (Integer) row[0],
                        row -> ((Long) row[1]).intValue()
                ));

        Map<Integer, Integer> participantCountByPostId = postParticipantRepository.countByPostIds(postIds)
                .stream()
                .collect(Collectors.toMap(
                        row -> (Integer) row[0],
                        row -> ((Long) row[1]).intValue()
                ));

        Set<Integer> likedByUser = Set.copyOf(postLikeRepository.findLikedPostIds(postIds, userId));
        Set<Integer> joinedByUser = Set.copyOf(postParticipantRepository.findJoinedPostIds(postIds, userId));

        return posts.stream()
                .map(post -> {
                    FeedPostResponse response = toResponse(post);
                    response.setLikesCount(likeCountByPostId.getOrDefault(post.getId(), 0));
                    response.setCommentsCount(commentCountByPostId.getOrDefault(post.getId(), 0));
                    response.setLikedByCurrentUser(likedByUser.contains(post.getId()));
                    response.setParticipantsCount(participantCountByPostId.getOrDefault(post.getId(), 0));
                    response.setJoinedByCurrentUser(joinedByUser.contains(post.getId()));
                    return response;
                })
                .collect(Collectors.toList());
    }

    private FeedPostResponse toResponse(Post post) {
        return FeedPostResponse.builder()
                .id(post.getId())
                .userId(post.getUser().getId())
                .username(post.getUser().getUsername())
                .photoUrl(post.getUser().getPhotoUrl())
                .postType(post.getPostType())
                .boxId(post.getBox() != null ? post.getBox().getId() : null)
                .title(post.getTitle())
                .trainingTag(post.getTrainingTag())
                .challengeDeadline(post.getChallengeDeadline())
                .r1Time(post.getRunning1())
                .r2Time(post.getRunning2())
                .r3Time(post.getRunning3())
                .r4Time(post.getRunning4())
                .r5Time(post.getRunning5())
                .r6Time(post.getRunning6())
                .r7Time(post.getRunning7())
                .r8Time(post.getRunning8())
                .skiErgTime(post.getSkiErg())
                .sledPushTime(post.getSledPush())
                .sledPullTime(post.getSledPull())
                .burpeeBjTime(post.getBurpeeBroadJump())
                .rowTime(post.getRow())
                .farmersCarryTime(post.getFarmersCarry())
                .sandbagLungesTime(post.getSandbagLunges())
                .wallBallsTime(post.getWallBalls())
                .totalTime(post.getTotalTime())
                .description(post.getDescription())
                .category(post.getCategory())
                .mateId(post.getMate() != null ? post.getMate().getId() : null)
                .mateUsername(post.getMate() != null ? post.getMate().getUsername() : null)
                .creationDate(post.getCreationDate())
                .build();
    }
}
