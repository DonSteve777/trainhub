package com.trainhub.backend.service;

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
