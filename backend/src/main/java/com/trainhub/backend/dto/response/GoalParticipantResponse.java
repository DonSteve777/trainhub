package com.trainhub.backend.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * Participante de un objetivo, con sus marcas.
 */
public class GoalParticipantResponse {

    private Integer userId;
    private String username;
    private String avatarUrl;
    private boolean owner;
    private boolean me;
    private OffsetDateTime startedAt;
    private OffsetDateTime endsAt;
    private List<GoalMarkResponse> marks;

    public GoalParticipantResponse(Integer userId, String username, String avatarUrl,
                                   boolean owner, boolean me,
                                   OffsetDateTime startedAt, OffsetDateTime endsAt,
                                   List<GoalMarkResponse> marks) {
        this.userId = userId;
        this.username = username;
        this.avatarUrl = avatarUrl;
        this.owner = owner;
        this.me = me;
        this.startedAt = startedAt;
        this.endsAt = endsAt;
        this.marks = marks;
    }

    public Integer getUserId() { return userId; }
    public String getUsername() { return username; }
    public String getAvatarUrl() { return avatarUrl; }

    @JsonProperty("isOwner")
    public boolean isOwner() { return owner; }

    @JsonProperty("isMe")
    public boolean isMe() { return me; }

    public OffsetDateTime getStartedAt() { return startedAt; }
    public OffsetDateTime getEndsAt() { return endsAt; }
    public List<GoalMarkResponse> getMarks() { return marks; }
}
