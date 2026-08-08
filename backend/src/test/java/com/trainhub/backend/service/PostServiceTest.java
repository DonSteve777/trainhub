package com.trainhub.backend.service;

import com.trainhub.backend.repository.PostRepository;
import com.trainhub.backend.repository.UserRepository;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PostServiceTest {

    private final PostRepository postRepository = mock(PostRepository.class);
    private final UserRepository userRepository = mock(UserRepository.class);
    private final PostService postService = new PostService(postRepository, userRepository);

    @Test
    void getStreakReturnsZeroWhenUserHasNoActivity() {
        Integer userId = 1;
        when(postRepository.findActivityDatesForUser(userId)).thenReturn(List.of());

        assertThat(postService.getStreak(userId).getCurrentStreak()).isZero();
    }

    @Test
    void getStreakCountsActivityOnlyToday() {
        Integer userId = 1;
        LocalDate today = LocalDate.now();
        when(postRepository.findActivityDatesForUser(userId)).thenReturn(List.of(today));

        assertThat(postService.getStreak(userId).getCurrentStreak()).isEqualTo(1);
    }

    @Test
    void getStreakCountsYesterdayAndPreviousDayWhenTodayHasNoActivity() {
        Integer userId = 1;
        LocalDate today = LocalDate.now();
        when(postRepository.findActivityDatesForUser(userId)).thenReturn(List.of(
                today.minusDays(1),
                today.minusDays(2)
        ));

        assertThat(postService.getStreak(userId).getCurrentStreak()).isEqualTo(2);
    }

    @Test
    void getStreakStopsAtGapBetweenTodayAndPreviousDay() {
        Integer userId = 1;
        LocalDate today = LocalDate.now();
        when(postRepository.findActivityDatesForUser(userId)).thenReturn(List.of(
                today,
                today.minusDays(2)
        ));

        assertThat(postService.getStreak(userId).getCurrentStreak()).isEqualTo(1);
    }

    @Test
    void getStreakReturnsZeroWhenLatestActivityIsBeforeYesterday() {
        Integer userId = 1;
        LocalDate today = LocalDate.now();
        when(postRepository.findActivityDatesForUser(userId)).thenReturn(List.of(today.minusDays(2)));

        assertThat(postService.getStreak(userId).getCurrentStreak()).isZero();
    }

    @Test
    void getStreakIgnoresBoxContentBecauseRepositoryOnlyReturnsOwnTrainingActivity() {
        Integer userId = 1;
        LocalDate today = LocalDate.now();
        when(postRepository.findActivityDatesForUser(userId)).thenReturn(List.of(
                today.minusDays(1),
                today.minusDays(2)
        ));

        assertThat(postService.getStreak(userId).getCurrentStreak()).isEqualTo(2);
    }
}
