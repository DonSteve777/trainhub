package com.trainhub.backend.dto.response;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO con el histórico de tiempos de un amigo, para pintar sus marcas
 * en las gráficas de progresión del usuario autenticado.
 */
public class FriendTimeHistoryResponse {

    private String username;
    private List<TimeEntry> totalHistory;
    private List<TimeEntry> workoutsHistory;
    private List<TimeEntry> runsHistory;

    public FriendTimeHistoryResponse() {}

    public FriendTimeHistoryResponse(
            String username,
            List<TimeEntry> totalHistory,
            List<TimeEntry> workoutsHistory,
            List<TimeEntry> runsHistory) {
        this.username = username;
        this.totalHistory = totalHistory;
        this.workoutsHistory = workoutsHistory;
        this.runsHistory = runsHistory;
    }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public List<TimeEntry> getTotalHistory() { return totalHistory; }
    public void setTotalHistory(List<TimeEntry> totalHistory) { this.totalHistory = totalHistory; }

    public List<TimeEntry> getWorkoutsHistory() { return workoutsHistory; }
    public void setWorkoutsHistory(List<TimeEntry> workoutsHistory) { this.workoutsHistory = workoutsHistory; }

    public List<TimeEntry> getRunsHistory() { return runsHistory; }
    public void setRunsHistory(List<TimeEntry> runsHistory) { this.runsHistory = runsHistory; }

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
