package com.trainhub.backend.dto.response;

import com.trainhub.backend.enums.PostCategory;
import java.time.LocalDateTime;

/**
 * DTO que representa un post en el feed del usuario.
 */
public class FeedPostResponse {

    private Integer id;
    private Integer userId;
    private String username;
    private String photoUrl;

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
    private LocalDateTime creationDate;

    private Integer commentsCount;
    private Integer likesCount;
    private boolean likedByCurrentUser;

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

    public LocalDateTime getCreationDate() { return creationDate; }
    public void setCreationDate(LocalDateTime creationDate) { this.creationDate = creationDate; }

    public Integer getCommentsCount() { return commentsCount; }
    public void setCommentsCount(Integer commentsCount) { this.commentsCount = commentsCount; }

    public Integer getLikesCount() { return likesCount; }
    public void setLikesCount(Integer likesCount) { this.likesCount = likesCount; }

    public boolean isLikedByCurrentUser() { return likedByCurrentUser; }
    public void setLikedByCurrentUser(boolean likedByCurrentUser) { this.likedByCurrentUser = likedByCurrentUser; }

    public static Builder builder() { return new Builder(); }

    public static final class Builder {
        private final FeedPostResponse r = new FeedPostResponse();

        public Builder id(Integer v)               { r.id = v;                return this; }
        public Builder userId(Integer v)           { r.userId = v;            return this; }
        public Builder username(String v)          { r.username = v;          return this; }
        public Builder photoUrl(String v)          { r.photoUrl = v;          return this; }

        public Builder r1Time(Integer v)           { r.r1Time = v.toString(); return this; }
        public Builder r2Time(Integer v)           { r.r2Time = v.toString(); return this; }
        public Builder r3Time(Integer v)           { r.r3Time = v.toString(); return this; }
        public Builder r4Time(Integer v)           { r.r4Time = v.toString(); return this; }
        public Builder r5Time(Integer v)           { r.r5Time = v.toString(); return this; }
        public Builder r6Time(Integer v)           { r.r6Time = v.toString(); return this; }
        public Builder r7Time(Integer v)           { r.r7Time = v.toString(); return this; }
        public Builder r8Time(Integer v)           { r.r8Time = v.toString(); return this; }

        public Builder skiErgTime(Integer v)       { r.skiErgTime = v.toString();       return this; }
        public Builder sledPushTime(Integer v)     { r.sledPushTime = v.toString();     return this; }
        public Builder sledPullTime(Integer v)     { r.sledPullTime = v.toString();     return this; }
        public Builder burpeeBjTime(Integer v)     { r.burpeeBjTime = v.toString();     return this; }
        public Builder rowTime(Integer v)          { r.rowTime = v.toString();          return this; }
        public Builder farmersCarryTime(Integer v) { r.farmersCarryTime = v.toString(); return this; }
        public Builder sandbagLungesTime(Integer v){ r.sandbagLungesTime = v.toString();return this; }
        public Builder wallBallsTime(Integer v)    { r.wallBallsTime = v.toString();    return this; }

        public Builder totalTime(Integer v)        { r.totalTime = v.toString(); return this; }
        public Builder description(String v)       { r.description = v;          return this; }
        public Builder category(PostCategory v)    { r.category = v;             return this; }
        public Builder creationDate(java.time.LocalDateTime v) { r.creationDate = v; return this; }

        public FeedPostResponse build() { return r; }
    }
}
