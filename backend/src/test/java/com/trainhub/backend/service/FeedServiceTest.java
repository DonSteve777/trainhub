package com.trainhub.backend.service;

import com.trainhub.backend.dto.response.FeedPostResponse;
import com.trainhub.backend.model.Post;
import com.trainhub.backend.model.User;
import com.trainhub.backend.repository.CommentRepository;
import com.trainhub.backend.repository.PostLikeRepository;
import com.trainhub.backend.repository.PostRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.List;

import static com.trainhub.backend.util.TestFixtures.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FeedServiceTest {

    @Mock private PostRepository postRepository;
    @Mock private PostLikeRepository postLikeRepository;
    @Mock private CommentRepository commentRepository;

    @InjectMocks
    private FeedService feedService;

    private final LocalDateTime NOW = LocalDateTime.of(2026, 4, 18, 10, 0);

    private User userA;
    private User userB;
    private Post post1;
    private Post post2;

    @BeforeEach
    void setUp() {
        userA = aUser(1, "alice");
        userB = aUser(2, "bob");
        post1 = aPost(10, userA, NOW);
        post2 = aPost(20, userB, NOW.minusHours(1));
    }

    // -------------------------------------------------------------------------
    // Caso 1: lista de posts vacía → resultado vacío, sin llamar a repositorios
    // -------------------------------------------------------------------------

    @Test
    void getFirstPage_whenNoPostsFound_returnsEmptyList() {
        when(postRepository.findFeedFirstPage(eq(1), any(PageRequest.class)))
                .thenReturn(List.of());

        List<FeedPostResponse> result = feedService.getFirstPage(1, 10);

        assertThat(result).isEmpty();
    }

    // -------------------------------------------------------------------------
    // Caso 2: un post con likes y comentarios
    // -------------------------------------------------------------------------

    @Test
    void getFirstPage_singlePost_returnsCorrectCounts() {
        when(postRepository.findFeedFirstPage(eq(1), any(PageRequest.class)))
                .thenReturn(List.of(post1));
        when(postLikeRepository.countByPostIds(List.of(10)))
                .thenReturn(List.<Object[]>of(likeRow(10, 5L)));
        when(commentRepository.countByPostIds(List.of(10)))
                .thenReturn(List.<Object[]>of(commentRow(10, 3L)));

        List<FeedPostResponse> result = feedService.getFirstPage(1, 10);

        assertThat(result).hasSize(1);
        FeedPostResponse response = result.get(0);
        assertThat(response.getId()).isEqualTo(10);
        assertThat(response.getLikesCount()).isEqualTo(5);
        assertThat(response.getCommentsCount()).isEqualTo(3);
    }

    // -------------------------------------------------------------------------
    // Caso 3: post sin ningún like ni comentario (no aparece en el GROUP BY)
    //         → deben ser 0, no null
    // -------------------------------------------------------------------------

    @Test
    void getFirstPage_postWithNoLikesNorComments_returnsCounts0() {
        when(postRepository.findFeedFirstPage(eq(1), any(PageRequest.class)))
                .thenReturn(List.of(post1));
        when(postLikeRepository.countByPostIds(List.of(10)))
                .thenReturn(List.of());
        when(commentRepository.countByPostIds(List.of(10)))
                .thenReturn(List.of());

        List<FeedPostResponse> result = feedService.getFirstPage(1, 10);

        FeedPostResponse response = result.get(0);
        assertThat(response.getLikesCount()).isEqualTo(0);
        assertThat(response.getCommentsCount()).isEqualTo(0);
    }

    // -------------------------------------------------------------------------
    // Caso 4: varios posts con conteos distintos
    // -------------------------------------------------------------------------

    @Test
    void getFirstPage_multiplePosts_eachGetsCorrectCounts() {
        when(postRepository.findFeedFirstPage(eq(1), any(PageRequest.class)))
                .thenReturn(List.of(post1, post2));
        when(postLikeRepository.countByPostIds(List.of(10, 20)))
                .thenReturn(List.<Object[]>of(likeRow(10, 4L), likeRow(20, 1L)));
        when(commentRepository.countByPostIds(List.of(10, 20)))
                .thenReturn(List.<Object[]>of(commentRow(10, 2L)));  // post2 no tiene comentarios

        List<FeedPostResponse> result = feedService.getFirstPage(1, 10);

        assertThat(result).hasSize(2);

        FeedPostResponse r1 = result.stream().filter(r -> r.getId().equals(10)).findFirst().orElseThrow();
        assertThat(r1.getLikesCount()).isEqualTo(4);
        assertThat(r1.getCommentsCount()).isEqualTo(2);

        FeedPostResponse r2 = result.stream().filter(r -> r.getId().equals(20)).findFirst().orElseThrow();
        assertThat(r2.getLikesCount()).isEqualTo(1);
        assertThat(r2.getCommentsCount()).isEqualTo(0);
    }

    // -------------------------------------------------------------------------
    // Caso 5: getNextPage delega en toResponseList con el mismo comportamiento
    // -------------------------------------------------------------------------

    @Test
    void getNextPage_appliesSameCountLogic() {
        LocalDateTime cursor = NOW.minusMinutes(5);
        when(postRepository.findFeedWithCursor(eq(1), eq(cursor), eq(10), any(PageRequest.class)))
                .thenReturn(List.of(post2));
        when(postLikeRepository.countByPostIds(List.of(20)))
                .thenReturn(List.<Object[]>of(likeRow(20, 7L)));
        when(commentRepository.countByPostIds(List.of(20)))
                .thenReturn(List.<Object[]>of(commentRow(20, 0L)));

        List<FeedPostResponse> result = feedService.getNextPage(1, cursor, 10, 10);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getLikesCount()).isEqualTo(7);
        assertThat(result.get(0).getCommentsCount()).isEqualTo(0);
    }
}
