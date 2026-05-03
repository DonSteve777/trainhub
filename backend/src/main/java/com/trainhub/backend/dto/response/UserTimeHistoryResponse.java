package com.trainhub.backend.dto.response;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO con el histórico de tiempos del usuario autenticado, agrupado en tres colecciones:
 * total, workouts (suma de las 8 estaciones) y runs (suma de las 8 carreras).
 * Los tres arrays están alineados por índice: el elemento i corresponde al mismo post.
 */
public class UserTimeHistoryResponse {

    private List<TimeEntry> totalHistory;
    private List<TimeEntry> workoutsHistory;
    private List<TimeEntry> runsHistory;

    public UserTimeHistoryResponse() {}

    public UserTimeHistoryResponse(
            List<TimeEntry> totalHistory,
            List<TimeEntry> workoutsHistory,
            List<TimeEntry> runsHistory) {
        this.totalHistory = totalHistory;
        this.workoutsHistory = workoutsHistory;
        this.runsHistory = runsHistory;
    }

    public List<TimeEntry> getTotalHistory() { return totalHistory; }
    public void setTotalHistory(List<TimeEntry> totalHistory) { this.totalHistory = totalHistory; }

    public List<TimeEntry> getWorkoutsHistory() { return workoutsHistory; }
    public void setWorkoutsHistory(List<TimeEntry> workoutsHistory) { this.workoutsHistory = workoutsHistory; }

    public List<TimeEntry> getRunsHistory() { return runsHistory; }
    public void setRunsHistory(List<TimeEntry> runsHistory) { this.runsHistory = runsHistory; }

    /**
     * Par (tiempo en segundos, fecha) que representa una entrada del histórico.
     */
    public static class TimeEntry {

        private Integer time;
        private LocalDateTime date;

        public TimeEntry() {}

        public TimeEntry(Integer time, LocalDateTime date) {
            this.time = time;
            this.date = date;
        }

        public Integer getTime() { return time; }
        public void setTime(Integer time) { this.time = time; }

        public LocalDateTime getDate() { return date; }
        public void setDate(LocalDateTime date) { this.date = date; }
    }
}
