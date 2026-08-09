package com.trainhub.backend.service;

import com.trainhub.backend.dto.response.WeeklyConstancyResponse;
import com.trainhub.backend.enums.TrainingTag;
import com.trainhub.backend.repository.PostRepository;
import com.trainhub.backend.repository.UserRepository;
import com.trainhub.backend.service.PostService.ActivityDay;
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

    private static ActivityDay day(LocalDate date, TrainingTag tag) {
        return new ActivityDay(date, tag);
    }

    private static Object[] row(LocalDate date, TrainingTag tag, String postType) {
        return new Object[]{date, tag.name(), postType};
    }

    @Test
    void getStreakReturnsZeroWhenUserHasNoActivity() {
        Integer userId = 1;
        when(postRepository.findActivityDaysForUser(userId)).thenReturn(List.of());

        assertThat(postService.getStreak(userId).getCurrentStreak()).isZero();
    }

    @Test
    void getStreakCountsActivityOnlyToday() {
        Integer userId = 1;
        LocalDate today = LocalDate.now();
        when(postRepository.findActivityDaysForUser(userId)).thenReturn(List.<Object[]>of(
                row(today, TrainingTag.CLASE, "CHECKIN")
        ));

        assertThat(postService.getStreak(userId).getCurrentStreak()).isEqualTo(1);
    }

    @Test
    void getStreakCountsYesterdayAndPreviousDayWhenTodayHasNoActivity() {
        Integer userId = 1;
        LocalDate today = LocalDate.now();
        when(postRepository.findActivityDaysForUser(userId)).thenReturn(List.of(
                row(today.minusDays(1), TrainingTag.FUERZA, "CHECKIN"),
                row(today.minusDays(2), TrainingTag.CLASE, "CHECKIN")
        ));

        assertThat(postService.getStreak(userId).getCurrentStreak()).isEqualTo(2);
    }

    @Test
    void getStreakStopsAtGapBetweenTodayAndPreviousDay() {
        Integer userId = 1;
        LocalDate today = LocalDate.now();
        when(postRepository.findActivityDaysForUser(userId)).thenReturn(List.of(
                row(today, TrainingTag.HYROX, "RESULT"),
                row(today.minusDays(2), TrainingTag.FUERZA, "CHECKIN")
        ));

        assertThat(postService.getStreak(userId).getCurrentStreak()).isEqualTo(1);
    }

    @Test
    void getStreakReturnsZeroWhenLatestActivityIsBeforeYesterday() {
        Integer userId = 1;
        LocalDate today = LocalDate.now();
        when(postRepository.findActivityDaysForUser(userId)).thenReturn(List.<Object[]>of(
                row(today.minusDays(2), TrainingTag.CLASE, "CHECKIN")
        ));

        assertThat(postService.getStreak(userId).getCurrentStreak()).isZero();
    }

    @Test
    void getStreakIgnoresBoxContentBecauseRepositoryOnlyReturnsOwnTrainingActivity() {
        Integer userId = 1;
        LocalDate today = LocalDate.now();
        when(postRepository.findActivityDaysForUser(userId)).thenReturn(List.of(
                row(today.minusDays(1), TrainingTag.FUERZA, "CHECKIN"),
                row(today.minusDays(2), TrainingTag.CLASE, "CHECKIN")
        ));

        assertThat(postService.getStreak(userId).getCurrentStreak()).isEqualTo(2);
    }

    @Test
    void weeklyConstancyReturnsZeroAndEmptyDotsWhenNoActivity() {
        WeeklyConstancyResponse result = postService.calculateWeeklyConstancy(List.of());

        assertThat(result.getStreakWeeks()).isZero();
        assertThat(result.getWeekActiveCount()).isZero();
        assertThat(result.getWeekDayTags()).containsExactly(null, null, null, null, null, null, null);
    }

    @Test
    void weeklyConstancyMarksTagsOfCurrentIsoWeek() {
        LocalDate monday = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate wednesday = monday.plusDays(2);
        LocalDate friday = monday.plusDays(4);

        WeeklyConstancyResponse result = postService.calculateWeeklyConstancy(List.of(
                day(monday, TrainingTag.CLASE),
                day(wednesday, TrainingTag.FUERZA),
                day(friday, TrainingTag.HYROX)
        ));

        assertThat(result.getWeekDayTags()).containsExactly(
                TrainingTag.CLASE, null, TrainingTag.FUERZA, null, TrainingTag.HYROX, null, null
        );
        assertThat(result.getWeekActiveCount()).isEqualTo(3);
        assertThat(result.getStreakWeeks()).isEqualTo(1);
    }

    @Test
    void weeklyConstancyDoesNotCountWeekBelowMinimum() {
        LocalDate monday = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate prevMonday = monday.minusWeeks(1);

        WeeklyConstancyResponse result = postService.calculateWeeklyConstancy(List.of(
                day(prevMonday, TrainingTag.CLASE),
                day(prevMonday.plusDays(1), TrainingTag.FUERZA)
        ));

        assertThat(result.getStreakWeeks()).isZero();
        assertThat(result.getWeekActiveCount()).isZero();
    }

    @Test
    void weeklyConstancyAnchorsOnPreviousWeekWhenCurrentWeekBelowMinimum() {
        LocalDate monday = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate prevMonday = monday.minusWeeks(1);

        WeeklyConstancyResponse result = postService.calculateWeeklyConstancy(List.of(
                day(monday, TrainingTag.CLASE),
                day(prevMonday, TrainingTag.FUERZA),
                day(prevMonday.plusDays(1), TrainingTag.HYROX),
                day(prevMonday.plusDays(2), TrainingTag.CARRERA)
        ));

        assertThat(result.getStreakWeeks()).isEqualTo(1);
        assertThat(result.getWeekActiveCount()).isEqualTo(1);
        assertThat(result.getWeekDayTags().get(0)).isEqualTo(TrainingTag.CLASE);
    }

    @Test
    void weeklyConstancyCountsConsecutiveWeeksAndBreaksOnGap() {
        LocalDate monday = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate week1 = monday.minusWeeks(1);
        LocalDate week2 = monday.minusWeeks(2);
        LocalDate week4 = monday.minusWeeks(4);

        List<ActivityDay> dates = new ArrayList<>();
        dates.add(day(monday, TrainingTag.CLASE));
        dates.addAll(List.of(
                day(week1, TrainingTag.FUERZA),
                day(week1.plusDays(1), TrainingTag.HYROX),
                day(week1.plusDays(2), TrainingTag.CLASE)
        ));
        dates.addAll(List.of(
                day(week2, TrainingTag.CARRERA),
                day(week2.plusDays(2), TrainingTag.FUERZA),
                day(week2.plusDays(4), TrainingTag.OTRO)
        ));
        dates.addAll(List.of(
                day(week4, TrainingTag.CLASE),
                day(week4.plusDays(1), TrainingTag.FUERZA),
                day(week4.plusDays(3), TrainingTag.HYROX)
        ));

        WeeklyConstancyResponse result = postService.calculateWeeklyConstancy(dates);

        assertThat(result.getStreakWeeks()).isEqualTo(2);
    }

    @Test
    void weeklyConstancyIncludesCurrentWeekWhenItReachesMinimum() {
        LocalDate monday = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate prevMonday = monday.minusWeeks(1);

        WeeklyConstancyResponse result = postService.calculateWeeklyConstancy(List.of(
                day(monday, TrainingTag.CLASE),
                day(monday.plusDays(1), TrainingTag.FUERZA),
                day(monday.plusDays(2), TrainingTag.HYROX),
                day(prevMonday, TrainingTag.CARRERA),
                day(prevMonday.plusDays(1), TrainingTag.OTRO),
                day(prevMonday.plusDays(3), TrainingTag.CLASE)
        ));

        assertThat(result.getStreakWeeks()).isEqualTo(2);
        assertThat(result.getWeekActiveCount()).isEqualTo(3);
    }

    @Test
    void weeklyConstancyUsesConfiguredMinimumDaysPerWeek() {
        PostService strictService = new PostService(postRepository, userRepository, 4);
        LocalDate monday = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate prevMonday = monday.minusWeeks(1);

        List<ActivityDay> threeDaysPrevWeek = List.of(
                day(prevMonday, TrainingTag.CLASE),
                day(prevMonday.plusDays(1), TrainingTag.FUERZA),
                day(prevMonday.plusDays(2), TrainingTag.HYROX)
        );

        assertThat(postService.calculateWeeklyConstancy(threeDaysPrevWeek).getStreakWeeks()).isEqualTo(1);
        assertThat(strictService.calculateWeeklyConstancy(threeDaysPrevWeek).getStreakWeeks()).isZero();
    }

    @Test
    void getWeeklyConstancyLoadsActivityDaysFromRepository() {
        Integer userId = 7;
        LocalDate monday = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        when(postRepository.findActivityDaysForUser(userId)).thenReturn(List.of(
                row(monday.minusWeeks(1), TrainingTag.CLASE, "CHECKIN"),
                row(monday.minusWeeks(1).plusDays(1), TrainingTag.FUERZA, "CHECKIN"),
                new Object[]{monday.minusWeeks(1).plusDays(2), null, "RESULT"}
        ));

        WeeklyConstancyResponse result = postService.getWeeklyConstancy(userId);
        assertThat(result.getStreakWeeks()).isEqualTo(1);
    }

    @Test
    void resultWithoutTagResolvesToHyrox() {
        ActivityDay day = PostService.toActivityDay(LocalDate.now(), null, "RESULT");
        assertThat(day.tag()).isEqualTo(TrainingTag.HYROX);
    }
}
