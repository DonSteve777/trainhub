package com.trainhub.backend.dto.response;

/**
 * Respuesta de POST /api/goals/{goalId}/join.
 */
public class GoalJoinResponse {

    private Integer goalId;
    private boolean joined;
    private Integer participantsCount;
    private Integer joinPostId;

    public GoalJoinResponse() {}

    public GoalJoinResponse(Integer goalId, boolean joined, Integer participantsCount, Integer joinPostId) {
        this.goalId = goalId;
        this.joined = joined;
        this.participantsCount = participantsCount;
        this.joinPostId = joinPostId;
    }

    public Integer getGoalId() { return goalId; }
    public void setGoalId(Integer goalId) { this.goalId = goalId; }

    public boolean isJoined() { return joined; }
    public void setJoined(boolean joined) { this.joined = joined; }

    public Integer getParticipantsCount() { return participantsCount; }
    public void setParticipantsCount(Integer participantsCount) {
        this.participantsCount = participantsCount;
    }

    public Integer getJoinPostId() { return joinPostId; }
    public void setJoinPostId(Integer joinPostId) { this.joinPostId = joinPostId; }
}
