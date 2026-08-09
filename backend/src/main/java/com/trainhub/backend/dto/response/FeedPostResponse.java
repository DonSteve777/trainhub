package com.trainhub.backend.dto.response;

import com.trainhub.backend.enums.PostType;
import com.trainhub.backend.enums.TrainingTag;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;

/**
 * DTO que representa un post en el feed del usuario.
 */
public class FeedPostResponse {

    private Integer id;
    private Integer userId;
    private String username;
    private String photoUrl;
    private PostType postType;
    private Integer boxId;
    private String title;
    private TrainingTag trainingTag;
    private OffsetDateTime challengeDeadline;

    private String description;
    private LocalDateTime creationDate;

    private Integer commentsCount;
    private Integer likesCount;
    private boolean likedByCurrentUser;
    private Integer participantsCount;
    private boolean joinedByCurrentUser;

    /** Solo en CHECKIN: semanas ISO consecutivas con ≥ N días de actividad. Null en el resto. */
    private Integer streakWeeks;
    /** Solo en CHECKIN: 7 tags L→D (null = inactivo) de la semana ISO actual. Null en el resto. */
    private List<TrainingTag> weekDayTags;

    /** Solo en CHECKIN vinculado: id del BOX_WOD. */
    private Integer wodPostId;
    /** Solo en CHECKIN vinculado: título del BOX_WOD. */
    private String wodTitle;

    /** Solo en BOX_WOD: número de participantes apuntados. */
    private Integer wodCheckinsCount;
    /** Solo en BOX_WOD: hasta 8 participantes (muro). */
    private List<WodCheckinAuthorResponse> wodCheckinAuthors;

    public FeedPostResponse() {}

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPhotoUrl() { return photoUrl; }
    public void setPhotoUrl(String photoUrl) { this.photoUrl = photoUrl; }

    public PostType getPostType() { return postType; }
    public void setPostType(PostType postType) { this.postType = postType; }

    public Integer getBoxId() { return boxId; }
    public void setBoxId(Integer boxId) { this.boxId = boxId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public TrainingTag getTrainingTag() { return trainingTag; }
    public void setTrainingTag(TrainingTag trainingTag) { this.trainingTag = trainingTag; }

    public OffsetDateTime getChallengeDeadline() { return challengeDeadline; }
    public void setChallengeDeadline(OffsetDateTime challengeDeadline) { this.challengeDeadline = challengeDeadline; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDateTime getCreationDate() { return creationDate; }
    public void setCreationDate(LocalDateTime creationDate) { this.creationDate = creationDate; }

    public Integer getCommentsCount() { return commentsCount; }
    public void setCommentsCount(Integer commentsCount) { this.commentsCount = commentsCount; }

    public Integer getLikesCount() { return likesCount; }
    public void setLikesCount(Integer likesCount) { this.likesCount = likesCount; }

    public boolean isLikedByCurrentUser() { return likedByCurrentUser; }
    public void setLikedByCurrentUser(boolean likedByCurrentUser) { this.likedByCurrentUser = likedByCurrentUser; }

    public Integer getParticipantsCount() { return participantsCount; }
    public void setParticipantsCount(Integer participantsCount) { this.participantsCount = participantsCount; }

    public boolean isJoinedByCurrentUser() { return joinedByCurrentUser; }
    public void setJoinedByCurrentUser(boolean joinedByCurrentUser) { this.joinedByCurrentUser = joinedByCurrentUser; }

    public Integer getStreakWeeks() { return streakWeeks; }
    public void setStreakWeeks(Integer streakWeeks) { this.streakWeeks = streakWeeks; }

    public List<TrainingTag> getWeekDayTags() { return weekDayTags; }
    public void setWeekDayTags(List<TrainingTag> weekDayTags) { this.weekDayTags = weekDayTags; }

    public Integer getWodPostId() { return wodPostId; }
    public void setWodPostId(Integer wodPostId) { this.wodPostId = wodPostId; }

    public String getWodTitle() { return wodTitle; }
    public void setWodTitle(String wodTitle) { this.wodTitle = wodTitle; }

    public Integer getWodCheckinsCount() { return wodCheckinsCount; }
    public void setWodCheckinsCount(Integer wodCheckinsCount) { this.wodCheckinsCount = wodCheckinsCount; }

    public List<WodCheckinAuthorResponse> getWodCheckinAuthors() { return wodCheckinAuthors; }
    public void setWodCheckinAuthors(List<WodCheckinAuthorResponse> wodCheckinAuthors) {
        this.wodCheckinAuthors = wodCheckinAuthors;
    }

    public static Builder builder() { return new Builder(); }

    public static final class Builder {
        private final FeedPostResponse r = new FeedPostResponse();

        public Builder id(Integer v)               { r.id = v;                return this; }
        public Builder userId(Integer v)           { r.userId = v;            return this; }
        public Builder username(String v)          { r.username = v;          return this; }
        public Builder photoUrl(String v)          { r.photoUrl = v;          return this; }
        public Builder postType(PostType v)        { r.postType = v;          return this; }
        public Builder boxId(Integer v)            { r.boxId = v;             return this; }
        public Builder title(String v)             { r.title = v;             return this; }
        public Builder trainingTag(TrainingTag v)  { r.trainingTag = v;       return this; }
        public Builder challengeDeadline(OffsetDateTime v) { r.challengeDeadline = v; return this; }
        public Builder description(String v)       { r.description = v;          return this; }
        public Builder creationDate(java.time.LocalDateTime v) { r.creationDate = v; return this; }
        public Builder participantsCount(Integer v)  { r.participantsCount = v; return this; }
        public Builder joinedByCurrentUser(boolean v) { r.joinedByCurrentUser = v; return this; }
        public Builder streakWeeks(Integer v)        { r.streakWeeks = v;        return this; }
        public Builder weekDayTags(List<TrainingTag> v) { r.weekDayTags = v;  return this; }
        public Builder wodPostId(Integer v)          { r.wodPostId = v;         return this; }
        public Builder wodTitle(String v)            { r.wodTitle = v;          return this; }
        public Builder wodCheckinsCount(Integer v)   { r.wodCheckinsCount = v;  return this; }
        public Builder wodCheckinAuthors(List<WodCheckinAuthorResponse> v) {
            r.wodCheckinAuthors = v;
            return this;
        }

        public FeedPostResponse build() { return r; }
    }
}
