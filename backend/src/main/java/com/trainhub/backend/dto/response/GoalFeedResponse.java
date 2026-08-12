package com.trainhub.backend.dto.response;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

/**
 * Payload anidado de posts GOAL_CREATED / GOAL_JOIN en el feed.
 */
public class GoalFeedResponse {

    private Integer goalId;
    private String goalTitle;
    private String goalStatus;
    private BigDecimal targetValue;
    private String unit;
    private String direction;
    private Integer weeks;
    private OffsetDateTime deadline;
    private Integer creatorUserId;
    private String creatorUsername;
    private Integer participantsCount;
    private boolean joinedByCurrentUser;
    private List<GoalFriendPreviewResponse> friendAvatars;
    /** Solo GOAL_JOIN: resto de participantes además del creador (para el copy). */
    private List<GoalFriendPreviewResponse> otherParticipants;

    public GoalFeedResponse() {}

    public Integer getGoalId() { return goalId; }
    public void setGoalId(Integer goalId) { this.goalId = goalId; }

    public String getGoalTitle() { return goalTitle; }
    public void setGoalTitle(String goalTitle) { this.goalTitle = goalTitle; }

    public String getGoalStatus() { return goalStatus; }
    public void setGoalStatus(String goalStatus) { this.goalStatus = goalStatus; }

    public BigDecimal getTargetValue() { return targetValue; }
    public void setTargetValue(BigDecimal targetValue) { this.targetValue = targetValue; }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }

    public String getDirection() { return direction; }
    public void setDirection(String direction) { this.direction = direction; }

    public Integer getWeeks() { return weeks; }
    public void setWeeks(Integer weeks) { this.weeks = weeks; }

    public OffsetDateTime getDeadline() { return deadline; }
    public void setDeadline(OffsetDateTime deadline) { this.deadline = deadline; }

    public Integer getCreatorUserId() { return creatorUserId; }
    public void setCreatorUserId(Integer creatorUserId) { this.creatorUserId = creatorUserId; }

    public String getCreatorUsername() { return creatorUsername; }
    public void setCreatorUsername(String creatorUsername) { this.creatorUsername = creatorUsername; }

    public Integer getParticipantsCount() { return participantsCount; }
    public void setParticipantsCount(Integer participantsCount) { this.participantsCount = participantsCount; }

    public boolean isJoinedByCurrentUser() { return joinedByCurrentUser; }
    public void setJoinedByCurrentUser(boolean joinedByCurrentUser) {
        this.joinedByCurrentUser = joinedByCurrentUser;
    }

    public List<GoalFriendPreviewResponse> getFriendAvatars() { return friendAvatars; }
    public void setFriendAvatars(List<GoalFriendPreviewResponse> friendAvatars) {
        this.friendAvatars = friendAvatars;
    }

    public List<GoalFriendPreviewResponse> getOtherParticipants() { return otherParticipants; }
    public void setOtherParticipants(List<GoalFriendPreviewResponse> otherParticipants) {
        this.otherParticipants = otherParticipants;
    }
}
