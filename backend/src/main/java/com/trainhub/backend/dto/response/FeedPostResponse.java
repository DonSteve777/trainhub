package com.trainhub.backend.dto.response;

import com.trainhub.backend.enums.PostCategory;
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

    private String r1Time;
    private String r2Time;
    private String r3Time;
    private String r4Time;
    private String r5Time;
    private String r6Time;
    private String r7Time;
    private String r8Time;

    private String skiErgTime;
    private String sledPushTime;
    private String sledPullTime;
    private String burpeeBjTime;
    private String rowTime;
    private String farmersCarryTime;
    private String sandbagLungesTime;
    private String wallBallsTime;

    private String totalTime;
    private String description;
    private PostCategory category;
    private Integer mateId;
    private String mateUsername;
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

    /** Solo en BOX_WOD: número de check-ins vinculados. */
    private Integer wodCheckinsCount;
    /** Solo en BOX_WOD: hasta 8 autores de check-in (muro). */
    private List<WodCheckinAuthorResponse> wodCheckinAuthors;

    public FeedPostResponse() {}

    public FeedPostResponse(Integer id, Integer userId, String username, String photoUrl,
                            Integer r1Time, Integer r2Time, Integer r3Time, Integer r4Time,
                            Integer r5Time, Integer r6Time, Integer r7Time, Integer r8Time,
                            Integer skiErgTime, Integer sledPushTime, Integer sledPullTime, Integer burpeeBjTime,
                            Integer rowTime, Integer farmersCarryTime, Integer sandbagLungesTime, Integer wallBallsTime,
                            Integer totalTime, String description, LocalDateTime creationDate) {
        this.id = id;
        this.userId = userId;
        this.username = username;
        this.photoUrl = photoUrl;
        this.r1Time = r1Time.toString();
        this.r2Time = r2Time.toString();
        this.r3Time = r3Time.toString();
        this.r4Time = r4Time.toString();
        this.r5Time = r5Time.toString();
        this.r6Time = r6Time.toString();
        this.r7Time = r7Time.toString();
        this.r8Time = r8Time.toString();
        this.skiErgTime = skiErgTime.toString();
        this.sledPushTime = sledPushTime.toString();
        this.sledPullTime = sledPullTime.toString();
        this.burpeeBjTime = burpeeBjTime.toString();
        this.rowTime = rowTime.toString();
        this.farmersCarryTime = farmersCarryTime.toString();
        this.sandbagLungesTime = sandbagLungesTime.toString();
        this.wallBallsTime = wallBallsTime.toString();
        this.totalTime = totalTime.toString();
        this.description = description;
        this.creationDate = creationDate;
    }

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

    public String getR1Time() { return r1Time; }
    public void setR1Time(String r1Time) { this.r1Time = r1Time; }

    public String getR2Time() { return r2Time; }
    public void setR2Time(String r2Time) { this.r2Time = r2Time; }

    public String getR3Time() { return r3Time; }
    public void setR3Time(String r3Time) { this.r3Time = r3Time; }

    public String getR4Time() { return r4Time; }
    public void setR4Time(String r4Time) { this.r4Time = r4Time; }

    public String getR5Time() { return r5Time; }
    public void setR5Time(String r5Time) { this.r5Time = r5Time; }

    public String getR6Time() { return r6Time; }
    public void setR6Time(String r6Time) { this.r6Time = r6Time; }

    public String getR7Time() { return r7Time; }
    public void setR7Time(String r7Time) { this.r7Time = r7Time; }

    public String getR8Time() { return r8Time; }
    public void setR8Time(String r8Time) { this.r8Time = r8Time; }

    public String getSkiErgTime() { return skiErgTime; }
    public void setSkiErgTime(String skiErgTime) { this.skiErgTime = skiErgTime; }

    public String getSledPushTime() { return sledPushTime; }
    public void setSledPushTime(String sledPushTime) { this.sledPushTime = sledPushTime; }

    public String getSledPullTime() { return sledPullTime; }
    public void setSledPullTime(String sledPullTime) { this.sledPullTime = sledPullTime; }

    public String getBurpeeBjTime() { return burpeeBjTime; }
    public void setBurpeeBjTime(String burpeeBjTime) { this.burpeeBjTime = burpeeBjTime; }

    public String getRowTime() { return rowTime; }
    public void setRowTime(String rowTime) { this.rowTime = rowTime; }

    public String getFarmersCarryTime() { return farmersCarryTime; }
    public void setFarmersCarryTime(String farmersCarryTime) { this.farmersCarryTime = farmersCarryTime; }

    public String getSandbagLungesTime() { return sandbagLungesTime; }
    public void setSandbagLungesTime(String sandbagLungesTime) { this.sandbagLungesTime = sandbagLungesTime; }

    public String getWallBallsTime() { return wallBallsTime; }
    public void setWallBallsTime(String wallBallsTime) { this.wallBallsTime = wallBallsTime; }

    public String getTotalTime() { return totalTime; }
    public void setTotalTime(String totalTime) { this.totalTime = totalTime; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public PostCategory getCategory() { return category; }
    public void setCategory(PostCategory category) { this.category = category; }

    public Integer getMateId() { return mateId; }
    public void setMateId(Integer mateId) { this.mateId = mateId; }

    public String getMateUsername() { return mateUsername; }
    public void setMateUsername(String mateUsername) { this.mateUsername = mateUsername; }

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

        public Builder r1Time(Integer v)           { r.r1Time = toStringOrNull(v); return this; }
        public Builder r2Time(Integer v)           { r.r2Time = toStringOrNull(v); return this; }
        public Builder r3Time(Integer v)           { r.r3Time = toStringOrNull(v); return this; }
        public Builder r4Time(Integer v)           { r.r4Time = toStringOrNull(v); return this; }
        public Builder r5Time(Integer v)           { r.r5Time = toStringOrNull(v); return this; }
        public Builder r6Time(Integer v)           { r.r6Time = toStringOrNull(v); return this; }
        public Builder r7Time(Integer v)           { r.r7Time = toStringOrNull(v); return this; }
        public Builder r8Time(Integer v)           { r.r8Time = toStringOrNull(v); return this; }

        public Builder skiErgTime(Integer v)       { r.skiErgTime = toStringOrNull(v);       return this; }
        public Builder sledPushTime(Integer v)     { r.sledPushTime = toStringOrNull(v);     return this; }
        public Builder sledPullTime(Integer v)     { r.sledPullTime = toStringOrNull(v);     return this; }
        public Builder burpeeBjTime(Integer v)     { r.burpeeBjTime = toStringOrNull(v);     return this; }
        public Builder rowTime(Integer v)          { r.rowTime = toStringOrNull(v);          return this; }
        public Builder farmersCarryTime(Integer v) { r.farmersCarryTime = toStringOrNull(v); return this; }
        public Builder sandbagLungesTime(Integer v){ r.sandbagLungesTime = toStringOrNull(v);return this; }
        public Builder wallBallsTime(Integer v)    { r.wallBallsTime = toStringOrNull(v);    return this; }

        public Builder totalTime(Integer v)        { r.totalTime = toStringOrNull(v); return this; }
        public Builder description(String v)       { r.description = v;          return this; }
        public Builder category(PostCategory v)      { r.category = v;         return this; }
        public Builder mateId(Integer v)             { r.mateId = v;           return this; }
        public Builder mateUsername(String v)        { r.mateUsername = v;     return this; }
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

        private static String toStringOrNull(Integer v) {
            return v == null ? null : v.toString();
        }
    }
}
