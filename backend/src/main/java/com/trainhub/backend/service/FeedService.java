package com.trainhub.backend.service;

import com.trainhub.backend.dto.response.FeedHistoryResponse;
import com.trainhub.backend.dto.response.FeedPostResponse;
import com.trainhub.backend.model.Post;
import com.trainhub.backend.repository.CommentRepository;
import com.trainhub.backend.repository.PostLikeRepository;
import com.trainhub.backend.repository.PostRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Servicio para obtener el feed de posts de amigos con paginación keyset.
 */
@Service
public class FeedService {

    private final PostRepository postRepository;
    private final PostLikeRepository postLikeRepository;
    private final CommentRepository commentRepository;

    public FeedService(PostRepository postRepository, PostLikeRepository postLikeRepository,
                       CommentRepository commentRepository) {
        this.postRepository = postRepository;
        this.postLikeRepository = postLikeRepository;
        this.commentRepository = commentRepository;
    }

    public List<FeedPostResponse> getFirstPage(Integer userId, int size) {
        List<Post> posts = postRepository.findFeedFirstPage(userId, PageRequest.of(0, size));
        return toResponseList(posts);
    }

    public List<FeedPostResponse> getNextPage(Integer userId, LocalDateTime cursorDate, Integer cursorId, int size) {
        List<Post> posts = postRepository.findFeedWithCursor(userId, cursorDate, cursorId, PageRequest.of(0, size));
        return toResponseList(posts);
    }

    public FeedHistoryResponse getHistory(Integer userId) {
        List<Object[]> rows = postRepository.findFriendPostTimes(userId);

        return new FeedHistoryResponse(
                col(rows, 0),
                col(rows, 1),  col(rows, 2),  col(rows, 3),  col(rows, 4),
                col(rows, 5),  col(rows, 6),  col(rows, 7),  col(rows, 8),
                col(rows, 9),  col(rows, 10), col(rows, 11), col(rows, 12),
                col(rows, 13), col(rows, 14), col(rows, 15), col(rows, 16)
        );
    }

    private List<Integer> col(List<Object[]> rows, int index) {
        return rows.stream()
                .map(r -> ((Number) r[index]).intValue())
                .collect(Collectors.toList());
    }

    private List<FeedPostResponse> toResponseList(List<Post> posts) {
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

        return posts.stream()
                .map(post -> {
                    FeedPostResponse response = toResponse(post);
                    response.setLikesCount(likeCountByPostId.getOrDefault(post.getId(), 0));
                    response.setCommentsCount(commentCountByPostId.getOrDefault(post.getId(), 0));
                    return response;
                })
                .collect(Collectors.toList());
    }

    private FeedPostResponse toResponse(Post post) {
        return new FeedPostResponse(
                post.getId(),
                post.getUser().getId(),
                post.getUser().getUsername(),
                post.getUser().getPhotoUrl(),
                post.getRunning1(),
                post.getRunning2(),
                post.getRunning3(),
                post.getRunning4(),
                post.getRunning5(),
                post.getRunning6(),
                post.getRunning7(),
                post.getRunning8(),
                post.getSkiErg(),
                post.getSledPush(),
                post.getSledPull(),
                post.getBurpeeBroadJump(),
                post.getRow(),
                post.getFarmersCarry(),
                post.getSandbagLunges(),
                post.getWallBalls(),
                post.getTotalTime(),
                post.getDescription(),
                post.getCreationDate()
        );
    }
}
