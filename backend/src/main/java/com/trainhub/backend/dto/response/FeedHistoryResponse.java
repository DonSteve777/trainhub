package com.trainhub.backend.dto.response;

import java.util.List;

/**
 * DTO con los históricos de tiempos individuales de todos los posts de los amigos
 * del usuario autenticado, usados para construir las distribuciones de los gráficos.
 * El índice i en todas las listas corresponde al mismo post.
 */
public class FeedHistoryResponse {

    private List<Integer> totalsHistory;

    private List<Integer> r1History;
    private List<Integer> r2History;
    private List<Integer> r3History;
    private List<Integer> r4History;
    private List<Integer> r5History;
    private List<Integer> r6History;
    private List<Integer> r7History;
    private List<Integer> r8History;

    private List<Integer> w1History;
    private List<Integer> w2History;
    private List<Integer> w3History;
    private List<Integer> w4History;
    private List<Integer> w5History;
    private List<Integer> w6History;
    private List<Integer> w7History;
    private List<Integer> w8History;

    public FeedHistoryResponse() {}

    public FeedHistoryResponse(
            List<Integer> totalsHistory,
            List<Integer> r1History, List<Integer> r2History, List<Integer> r3History, List<Integer> r4History,
            List<Integer> r5History, List<Integer> r6History, List<Integer> r7History, List<Integer> r8History,
            List<Integer> w1History, List<Integer> w2History, List<Integer> w3History, List<Integer> w4History,
            List<Integer> w5History, List<Integer> w6History, List<Integer> w7History, List<Integer> w8History) {
        this.totalsHistory = totalsHistory;
        this.r1History = r1History; this.r2History = r2History;
        this.r3History = r3History; this.r4History = r4History;
        this.r5History = r5History; this.r6History = r6History;
        this.r7History = r7History; this.r8History = r8History;
        this.w1History = w1History; this.w2History = w2History;
        this.w3History = w3History; this.w4History = w4History;
        this.w5History = w5History; this.w6History = w6History;
        this.w7History = w7History; this.w8History = w8History;
    }

    public List<Integer> getTotalsHistory() { return totalsHistory; }
    public void setTotalsHistory(List<Integer> v) { this.totalsHistory = v; }

    public List<Integer> getR1History() { return r1History; }
    public void setR1History(List<Integer> v) { this.r1History = v; }

    public List<Integer> getR2History() { return r2History; }
    public void setR2History(List<Integer> v) { this.r2History = v; }

    public List<Integer> getR3History() { return r3History; }
    public void setR3History(List<Integer> v) { this.r3History = v; }

    public List<Integer> getR4History() { return r4History; }
    public void setR4History(List<Integer> v) { this.r4History = v; }

    public List<Integer> getR5History() { return r5History; }
    public void setR5History(List<Integer> v) { this.r5History = v; }

    public List<Integer> getR6History() { return r6History; }
    public void setR6History(List<Integer> v) { this.r6History = v; }

    public List<Integer> getR7History() { return r7History; }
    public void setR7History(List<Integer> v) { this.r7History = v; }

    public List<Integer> getR8History() { return r8History; }
    public void setR8History(List<Integer> v) { this.r8History = v; }

    public List<Integer> getW1History() { return w1History; }
    public void setW1History(List<Integer> v) { this.w1History = v; }

    public List<Integer> getW2History() { return w2History; }
    public void setW2History(List<Integer> v) { this.w2History = v; }

    public List<Integer> getW3History() { return w3History; }
    public void setW3History(List<Integer> v) { this.w3History = v; }

    public List<Integer> getW4History() { return w4History; }
    public void setW4History(List<Integer> v) { this.w4History = v; }

    public List<Integer> getW5History() { return w5History; }
    public void setW5History(List<Integer> v) { this.w5History = v; }

    public List<Integer> getW6History() { return w6History; }
    public void setW6History(List<Integer> v) { this.w6History = v; }

    public List<Integer> getW7History() { return w7History; }
    public void setW7History(List<Integer> v) { this.w7History = v; }

    public List<Integer> getW8History() { return w8History; }
    public void setW8History(List<Integer> v) { this.w8History = v; }
}
