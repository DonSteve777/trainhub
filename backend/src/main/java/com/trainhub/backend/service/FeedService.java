package com.trainhub.backend.service;

import com.trainhub.backend.dto.response.FeedPostResponse;
import com.trainhub.backend.dto.response.GoalFeedResponse;
import com.trainhub.backend.dto.response.GoalFriendPreviewResponse;
import com.trainhub.backend.dto.response.LikeToggleResponse;
import com.trainhub.backend.dto.response.ParticipationToggleResponse;
import com.trainhub.backend.dto.response.WeeklyConstancyResponse;
import com.trainhub.backend.dto.response.WodCheckinAuthorResponse;
import com.trainhub.backend.enums.PostType;
import com.trainhub.backend.model.Goal;
import com.trainhub.backend.model.GoalParticipant;
import com.trainhub.backend.model.Post;
import com.trainhub.backend.model.PostLike;
import com.trainhub.backend.model.PostLikeId;
import com.trainhub.backend.model.PostParticipant;
import com.trainhub.backend.model.PostParticipantId;
import com.trainhub.backend.model.User;
import com.trainhub.backend.repository.CommentRepository;
import com.trainhub.backend.repository.GoalParticipantRepository;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Servicio para obtener el feed de posts de amigos con paginación keyset.
 */
@Service
public class FeedService {

    private static final int WOD_MURO_AUTHORS_LIMIT = 8;
    private static final int GOAL_AVATARS_LIMIT = 8;

    private final PostRepository postRepository;
    private final PostLikeRepository postLikeRepository;
    private final PostParticipantRepository postParticipantRepository;
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final PostService postService;
    private final GoalParticipantRepository goalParticipantRepository;

    public FeedService(PostRepository postRepository, PostLikeRepository postLikeRepository,
                       PostParticipantRepository postParticipantRepository,
                       CommentRepository commentRepository, UserRepository userRepository,
                       PostService postService,
                       GoalParticipantRepository goalParticipantRepository) {
        this.postRepository = postRepository;
        this.postLikeRepository = postLikeRepository;
        this.postParticipantRepository = postParticipantRepository;
        this.commentRepository = commentRepository;
        this.userRepository = userRepository;
        this.postService = postService;
        this.goalParticipantRepository = goalParticipantRepository;
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
     * Apunta o desapunta al usuario de un reto o WOD de box (toggle).
     *
     * @return estado nuevo de la participación y el conteo actualizado.
     */
    @Transactional
    public ParticipationToggleResponse toggleParticipation(Integer postId, Integer userId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Post no encontrado"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));

        if (post.getPostType() != PostType.BOX_CHALLENGE && post.getPostType() != PostType.BOX_WOD) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Solo se puede participar en retos o WOD de box");
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

    /**
     * Lista completa de usuarios apuntados a un BOX_WOD o BOX_CHALLENGE.
     */
    public List<WodCheckinAuthorResponse> getWodParticipants(Integer postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Post no encontrado"));

        if (post.getPostType() != PostType.BOX_WOD && post.getPostType() != PostType.BOX_CHALLENGE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Solo retos y WOD tienen participantes");
        }

        return postParticipantRepository.findParticipantAuthorsByPostId(postId).stream()
                .map(row -> new WodCheckinAuthorResponse(
                        (Integer) row[0],
                        (String) row[1],
                        (String) row[2],
                        (LocalDateTime) row[3]
                ))
                .collect(Collectors.toList());
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

        Map<Integer, WeeklyConstancyResponse> constancyByUserId = loadConstancyForCheckinAuthors(posts);
        WodMuroData wodMuro = loadWodMuroData(posts);
        GoalFeedData goalData = loadGoalFeedData(posts, userId);

        return posts.stream()
                .map(post -> {
                    FeedPostResponse response = toResponse(post);
                    response.setLikesCount(likeCountByPostId.getOrDefault(post.getId(), 0));
                    response.setCommentsCount(commentCountByPostId.getOrDefault(post.getId(), 0));
                    response.setLikedByCurrentUser(likedByUser.contains(post.getId()));
                    response.setParticipantsCount(participantCountByPostId.getOrDefault(post.getId(), 0));
                    response.setJoinedByCurrentUser(joinedByUser.contains(post.getId()));
                    if (post.getPostType() == PostType.CHECKIN) {
                        WeeklyConstancyResponse constancy = constancyByUserId.get(post.getUser().getId());
                        if (constancy != null) {
                            response.setStreakWeeks(constancy.getStreakWeeks());
                            response.setWeekDayTags(constancy.getWeekDayTags());
                        }
                    }
                    if (post.getPostType() == PostType.BOX_WOD) {
                        response.setWodCheckinsCount(participantCountByPostId.getOrDefault(post.getId(), 0));
                    }
                    if (post.getPostType() == PostType.BOX_WOD || post.getPostType() == PostType.BOX_CHALLENGE) {
                        response.setWodCheckinAuthors(
                                wodMuro.authors().getOrDefault(post.getId(), List.of())
                        );
                    }
                    if (isGoalPost(post.getPostType()) && post.getGoal() != null) {
                        GoalFeedResponse goalPayload = goalData.byGoalId().get(post.getGoal().getId());
                        if (goalPayload != null) {
                            GoalFeedResponse copy = copyGoalFeed(goalPayload);
                            if (post.getPostType() == PostType.GOAL_JOIN) {
                                copy.setOtherParticipants(
                                        buildOtherParticipants(
                                                goalData.participantsByGoalId()
                                                        .getOrDefault(post.getGoal().getId(), List.of()),
                                                post.getUser().getId()
                                        )
                                );
                            }
                            response.setGoal(copy);
                            response.setParticipantsCount(copy.getParticipantsCount());
                            response.setJoinedByCurrentUser(copy.isJoinedByCurrentUser());
                        }
                    }
                    return response;
                })
                .collect(Collectors.toList());
    }

    private boolean isGoalPost(PostType type) {
        return type == PostType.GOAL_CREATED || type == PostType.GOAL_JOIN;
    }

    private GoalFeedData loadGoalFeedData(List<Post> posts, Integer currentUserId) {
        List<Integer> goalIds = posts.stream()
                .filter(p -> isGoalPost(p.getPostType()) && p.getGoal() != null)
                .map(p -> p.getGoal().getId())
                .distinct()
                .collect(Collectors.toList());

        if (goalIds.isEmpty()) {
            return new GoalFeedData(Map.of(), Map.of());
        }

        List<GoalParticipant> allParticipants = goalParticipantRepository.findByGoalIdsWithUser(goalIds);
        Map<Integer, List<GoalParticipant>> byGoal = allParticipants.stream()
                .collect(Collectors.groupingBy(gp -> gp.getId().getGoalId()));

        Set<Integer> joinedGoalIds = new HashSet<>(
                goalParticipantRepository.findParticipatingGoalIds(goalIds, currentUserId)
        );

        Map<Integer, Goal> goalsById = posts.stream()
                .filter(p -> p.getGoal() != null)
                .map(Post::getGoal)
                .collect(Collectors.toMap(Goal::getId, g -> g, (a, b) -> a));

        Map<Integer, GoalFeedResponse> byGoalId = new HashMap<>();
        for (Integer goalId : goalIds) {
            Goal goal = goalsById.get(goalId);
            if (goal == null) continue;
            List<GoalParticipant> participants = byGoal.getOrDefault(goalId, List.of());

            GoalFeedResponse payload = new GoalFeedResponse();
            payload.setGoalId(goal.getId());
            payload.setGoalTitle(goal.getTitle());
            payload.setGoalStatus(goal.getStatus().name());
            payload.setTargetValue(goal.getTargetValue());
            payload.setUnit(goal.getUnit().name().toLowerCase());
            payload.setDirection(goal.getDirection().name().toLowerCase());
            payload.setWeeks(goal.getWeeks());
            payload.setDeadline(goal.getDeadline());
            payload.setCreatorUserId(goal.getCreatedBy().getId());
            payload.setCreatorUsername(goal.getCreatedBy().getUsername());
            payload.setParticipantsCount(participants.size());
            payload.setJoinedByCurrentUser(joinedGoalIds.contains(goalId));
            payload.setFriendAvatars(buildFriendAvatars(participants));
            byGoalId.put(goalId, payload);
        }

        return new GoalFeedData(byGoalId, byGoal);
    }

    private List<GoalFriendPreviewResponse> buildFriendAvatars(List<GoalParticipant> participants) {
        List<GoalFriendPreviewResponse> avatars = new ArrayList<>();
        for (GoalParticipant gp : participants) {
            if (avatars.size() >= GOAL_AVATARS_LIMIT) break;
            User u = gp.getUser();
            avatars.add(new GoalFriendPreviewResponse(u.getId(), u.getUsername(), u.getPhotoUrl()));
        }
        return avatars;
    }

    private List<GoalFriendPreviewResponse> buildOtherParticipants(
            List<GoalParticipant> participants, Integer joinAuthorUserId) {
        List<GoalFriendPreviewResponse> others = new ArrayList<>();
        for (GoalParticipant gp : participants) {
            if (gp.isOwner()) continue;
            if (gp.getUser().getId().equals(joinAuthorUserId)) continue;
            User u = gp.getUser();
            others.add(new GoalFriendPreviewResponse(u.getId(), u.getUsername(), u.getPhotoUrl()));
        }
        return others;
    }

    private GoalFeedResponse copyGoalFeed(GoalFeedResponse src) {
        GoalFeedResponse copy = new GoalFeedResponse();
        copy.setGoalId(src.getGoalId());
        copy.setGoalTitle(src.getGoalTitle());
        copy.setGoalStatus(src.getGoalStatus());
        copy.setTargetValue(src.getTargetValue());
        copy.setUnit(src.getUnit());
        copy.setDirection(src.getDirection());
        copy.setWeeks(src.getWeeks());
        copy.setDeadline(src.getDeadline());
        copy.setCreatorUserId(src.getCreatorUserId());
        copy.setCreatorUsername(src.getCreatorUsername());
        copy.setParticipantsCount(src.getParticipantsCount());
        copy.setJoinedByCurrentUser(src.isJoinedByCurrentUser());
        copy.setFriendAvatars(src.getFriendAvatars());
        return copy;
    }

    private record GoalFeedData(
            Map<Integer, GoalFeedResponse> byGoalId,
            Map<Integer, List<GoalParticipant>> participantsByGoalId
    ) {}

    /**
     * Batch de avatares de participantes para BOX_WOD y BOX_CHALLENGE de la página.
     */
    private WodMuroData loadWodMuroData(List<Post> posts) {
        List<Integer> postIdsWithParticipants = posts.stream()
                .filter(post -> post.getPostType() == PostType.BOX_WOD
                        || post.getPostType() == PostType.BOX_CHALLENGE)
                .map(Post::getId)
                .collect(Collectors.toList());

        if (postIdsWithParticipants.isEmpty()) {
            return new WodMuroData(Map.of());
        }

        Map<Integer, List<WodCheckinAuthorResponse>> authorsByWod = new HashMap<>();
        Map<Integer, Set<Integer>> seenUsersByWod = new HashMap<>();

        for (Object[] row : postParticipantRepository.findParticipantAuthorsByPostIds(postIdsWithParticipants)) {
            Integer wodId = (Integer) row[0];
            Integer authorUserId = (Integer) row[1];
            Set<Integer> seen = seenUsersByWod.computeIfAbsent(wodId, ignored -> new HashSet<>());
            if (!seen.add(authorUserId)) {
                continue;
            }
            List<WodCheckinAuthorResponse> authors = authorsByWod.computeIfAbsent(wodId, ignored -> new ArrayList<>());
            if (authors.size() >= WOD_MURO_AUTHORS_LIMIT) {
                continue;
            }
            authors.add(new WodCheckinAuthorResponse(
                    authorUserId,
                    (String) row[2],
                    (String) row[3]
            ));
        }

        return new WodMuroData(authorsByWod);
    }

    private record WodMuroData(Map<Integer, List<WodCheckinAuthorResponse>> authors) {}

    /**
     * Una sola query de actividad para los autores de posts CHECKIN de la página,
     * y cálculo de constancia semanal por usuario.
     */
    private Map<Integer, WeeklyConstancyResponse> loadConstancyForCheckinAuthors(List<Post> posts) {
        Set<Integer> checkinAuthorIds = posts.stream()
                .filter(post -> post.getPostType() == PostType.CHECKIN)
                .map(post -> post.getUser().getId())
                .collect(Collectors.toSet());

        if (checkinAuthorIds.isEmpty()) {
            return Map.of();
        }

        Map<Integer, List<PostService.ActivityDay>> daysByUserId = new HashMap<>();
        for (Object[] row : postRepository.findActivityDaysForUsers(checkinAuthorIds)) {
            Integer authorId = ((Number) row[0]).intValue();
            daysByUserId
                    .computeIfAbsent(authorId, ignored -> new ArrayList<>())
                    .add(PostService.toActivityDay(row[1], row[2], row[3]));
        }

        Map<Integer, WeeklyConstancyResponse> constancyByUserId = new HashMap<>();
        for (Integer authorId : checkinAuthorIds) {
            constancyByUserId.put(
                    authorId,
                    postService.calculateWeeklyConstancy(daysByUserId.getOrDefault(authorId, List.of()))
            );
        }
        return constancyByUserId;
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
                .description(post.getDescription())
                .creationDate(post.getCreationDate())
                .wodPostId(post.getWodPost() != null ? post.getWodPost().getId() : null)
                .wodTitle(post.getWodPost() != null ? post.getWodPost().getTitle() : null)
                .build();
    }
}
