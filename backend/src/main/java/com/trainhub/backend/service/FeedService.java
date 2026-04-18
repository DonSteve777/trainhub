package com.trainhub.backend.service;

import com.trainhub.backend.dto.response.FeedPostResponse;
import com.trainhub.backend.model.Post;
import com.trainhub.backend.repository.PostRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio para obtener el feed de posts de amigos con paginación keyset.
 */
@Service
public class FeedService {

    private final PostRepository postRepository;

    public FeedService(PostRepository postRepository) {
        this.postRepository = postRepository;
    }

    public List<FeedPostResponse> getFirstPage(Integer userId, int size) {
        List<Post> posts = postRepository.findFeedFirstPage(userId, PageRequest.of(0, size));
        return posts.stream().map(this::toResponse).collect(Collectors.toList());
    }

    public List<FeedPostResponse> getNextPage(Integer userId, LocalDateTime cursorDate, Integer cursorId, int size) {
        List<Post> posts = postRepository.findFeedWithCursor(userId, cursorDate, cursorId, PageRequest.of(0, size));
        return posts.stream().map(this::toResponse).collect(Collectors.toList());
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
