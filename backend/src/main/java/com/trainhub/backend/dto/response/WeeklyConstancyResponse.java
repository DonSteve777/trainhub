package com.trainhub.backend.dto.response;

import java.util.List;

/**
 * Constancia semanal de entrenamiento (semanas ISO con ≥ N días de actividad).
 */
public class WeeklyConstancyResponse {

    private final int streakWeeks;
    private final List<Boolean> weekActiveDays;
    private final int weekActiveCount;

    public WeeklyConstancyResponse(int streakWeeks, List<Boolean> weekActiveDays, int weekActiveCount) {
        this.streakWeeks = streakWeeks;
        this.weekActiveDays = weekActiveDays;
        this.weekActiveCount = weekActiveCount;
    }

    public int getStreakWeeks() {
        return streakWeeks;
    }

    public List<Boolean> getWeekActiveDays() {
        return weekActiveDays;
    }

    public int getWeekActiveCount() {
        return weekActiveCount;
    }
}
