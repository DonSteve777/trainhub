package com.trainhub.backend.dto.response;

import java.util.List;

/**
 * DTO con los históricos de tiempos de todos los posts de los amigos
 * del usuario autenticado, usados para construir las distribuciones de los gráficos.
 */
public class FeedHistoryResponse {

    private List<Integer> totalsHistory;
    private List<Integer> runHistory;
    private List<Integer> workoutHistory;

    public FeedHistoryResponse() {}

    public FeedHistoryResponse(List<Integer> totalsHistory, List<Integer> runHistory, List<Integer> workoutHistory) {
        this.totalsHistory = totalsHistory;
        this.runHistory = runHistory;
        this.workoutHistory = workoutHistory;
    }

    public List<Integer> getTotalsHistory() { return totalsHistory; }
    public void setTotalsHistory(List<Integer> totalsHistory) { this.totalsHistory = totalsHistory; }

    public List<Integer> getRunHistory() { return runHistory; }
    public void setRunHistory(List<Integer> runHistory) { this.runHistory = runHistory; }

    public List<Integer> getWorkoutHistory() { return workoutHistory; }
    public void setWorkoutHistory(List<Integer> workoutHistory) { this.workoutHistory = workoutHistory; }
}
