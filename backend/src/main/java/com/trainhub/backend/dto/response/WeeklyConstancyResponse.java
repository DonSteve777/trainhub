package com.trainhub.backend.dto.response;

import com.trainhub.backend.enums.TrainingTag;

import java.util.List;

/**
 * Constancia semanal de entrenamiento (semanas ISO con ≥ N días de actividad).
 */
public class WeeklyConstancyResponse {

    private final int streakWeeks;
    /** Tags L→D de la semana ISO actual; {@code null} = día inactivo. */
    private final List<TrainingTag> weekDayTags;
    private final int weekActiveCount;

    public WeeklyConstancyResponse(int streakWeeks, List<TrainingTag> weekDayTags, int weekActiveCount) {
        this.streakWeeks = streakWeeks;
        this.weekDayTags = weekDayTags;
        this.weekActiveCount = weekActiveCount;
    }

    public int getStreakWeeks() {
        return streakWeeks;
    }

    public List<TrainingTag> getWeekDayTags() {
        return weekDayTags;
    }

    public int getWeekActiveCount() {
        return weekActiveCount;
    }
}
