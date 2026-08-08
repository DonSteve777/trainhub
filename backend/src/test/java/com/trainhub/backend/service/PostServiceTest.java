package com.trainhub.backend.service;

import com.trainhub.backend.dto.response.WeeklyConstancyResponse;
import com.trainhub.backend.repository.PostRepository;
import com.trainhub.backend.repository.UserRepository;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PostServiceTest {

    private static final int MIN_DAYS_PER_WEEK = 3;

    private final PostRepository postRepository = mock(PostRepository.class);
    private final UserRepository userRepository = mock(UserRepository.class);
    private final PostService postService = new PostService(postRepository, userRepository, MIN_DAYS_PER_WEEK);

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

    @Test
    void weeklyConstancyReturnsZeroAndEmptyDotsWhenNoActivity() {
        WeeklyConstancyResponse result = postService.calculateWeeklyConstancy(List.of());

        assertThat(result.getStreakWeeks()).isZero();
        assertThat(result.getWeekActiveCount()).isZero();
        assertThat(result.getWeekActiveDays()).containsExactly(false, false, false, false, false, false, false);
    }

    @Test
    void weeklyConstancyMarksActiveDaysOfCurrentIsoWeek() {
        LocalDate monday = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate wednesday = monday.plusDays(2);
        LocalDate friday = monday.plusDays(4);

        WeeklyConstancyResponse result = postService.calculateWeeklyConstancy(List.of(monday, wednesday, friday));

        assertThat(result.getWeekActiveDays()).containsExactly(
                true, false, true, false, true, false, false
        );
        assertThat(result.getWeekActiveCount()).isEqualTo(3);
        assertThat(result.getStreakWeeks()).isEqualTo(1);
    }

    @Test
    void weeklyConstancyDoesNotCountWeekBelowMinimum() {
        LocalDate monday = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate prevMonday = monday.minusWeeks(1);

        // Semana anterior: solo 2 días (< N=3) → no cuenta
        WeeklyConstancyResponse result = postService.calculateWeeklyConstancy(List.of(
                prevMonday,
                prevMonday.plusDays(1)
        ));

        assertThat(result.getStreakWeeks()).isZero();
        assertThat(result.getWeekActiveCount()).isZero();
    }

    @Test
    void weeklyConstancyAnchorsOnPreviousWeekWhenCurrentWeekBelowMinimum() {
        LocalDate monday = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate prevMonday = monday.minusWeeks(1);

        // Semana actual: 1 día (< N); semana anterior: 3 días
        WeeklyConstancyResponse result = postService.calculateWeeklyConstancy(List.of(
                monday,
                prevMonday,
                prevMonday.plusDays(1),
                prevMonday.plusDays(2)
        ));

        assertThat(result.getStreakWeeks()).isEqualTo(1);
        assertThat(result.getWeekActiveCount()).isEqualTo(1);
        assertThat(result.getWeekActiveDays().get(0)).isTrue();
    }

    @Test
    void weeklyConstancyCountsConsecutiveWeeksAndBreaksOnGap() {
        LocalDate monday = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate week1 = monday.minusWeeks(1);
        LocalDate week2 = monday.minusWeeks(2);
        LocalDate week4 = monday.minusWeeks(4);

        List<LocalDate> dates = new ArrayList<>();
        // Semana actual en curso (< N): no ancla
        dates.add(monday);
        // Semanas -1 y -2 con ≥ N
        dates.addAll(List.of(week1, week1.plusDays(1), week1.plusDays(2)));
        dates.addAll(List.of(week2, week2.plusDays(2), week2.plusDays(4)));
        // Semana -3 vacía (hueco); semana -4 con ≥ N no se suma
        dates.addAll(List.of(week4, week4.plusDays(1), week4.plusDays(3)));

        WeeklyConstancyResponse result = postService.calculateWeeklyConstancy(dates);

        assertThat(result.getStreakWeeks()).isEqualTo(2);
    }

    @Test
    void weeklyConstancyIncludesCurrentWeekWhenItReachesMinimum() {
        LocalDate monday = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate prevMonday = monday.minusWeeks(1);

        WeeklyConstancyResponse result = postService.calculateWeeklyConstancy(List.of(
                monday,
                monday.plusDays(1),
                monday.plusDays(2),
                prevMonday,
                prevMonday.plusDays(1),
                prevMonday.plusDays(3)
        ));

        assertThat(result.getStreakWeeks()).isEqualTo(2);
        assertThat(result.getWeekActiveCount()).isEqualTo(3);
    }

    @Test
    void weeklyConstancyUsesConfiguredMinimumDaysPerWeek() {
        PostService strictService = new PostService(postRepository, userRepository, 4);
        LocalDate monday = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate prevMonday = monday.minusWeeks(1);

        List<LocalDate> threeDaysPrevWeek = List.of(
                prevMonday,
                prevMonday.plusDays(1),
                prevMonday.plusDays(2)
        );

        assertThat(postService.calculateWeeklyConstancy(threeDaysPrevWeek).getStreakWeeks()).isEqualTo(1);
        assertThat(strictService.calculateWeeklyConstancy(threeDaysPrevWeek).getStreakWeeks()).isZero();
    }

    @Test
    void getWeeklyConstancyLoadsActivityDatesFromRepository() {
        Integer userId = 7;
        LocalDate monday = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        when(postRepository.findActivityDatesForUser(userId)).thenReturn(List.of(
                monday.minusWeeks(1),
                monday.minusWeeks(1).plusDays(1),
                monday.minusWeeks(1).plusDays(2)
        ));

        assertThat(postService.getWeeklyConstancy(userId).getStreakWeeks()).isEqualTo(1);
    }
}
