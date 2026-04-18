package com.trainhub.backend.dto.response;

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

    private String w1Time;
    private String w2Time;
    private String w3Time;
    private String w4Time;
    private String w5Time;
    private String w6Time;
    private String w7Time;
    private String w8Time;

    private String totalTime;
    private LocalDateTime creationDate;

    public FeedPostResponse() {}

    public FeedPostResponse(Integer id, Integer userId, String username, String photoUrl,
                            String r1Time, String r2Time, String r3Time, String r4Time,
                            String r5Time, String r6Time, String r7Time, String r8Time,
                            String w1Time, String w2Time, String w3Time, String w4Time,
                            String w5Time, String w6Time, String w7Time, String w8Time,
                            String totalTime, LocalDateTime creationDate) {
        this.id = id;
        this.userId = userId;
        this.username = username;
        this.photoUrl = photoUrl;
        this.r1Time = r1Time;
        this.r2Time = r2Time;
        this.r3Time = r3Time;
        this.r4Time = r4Time;
        this.r5Time = r5Time;
        this.r6Time = r6Time;
        this.r7Time = r7Time;
        this.r8Time = r8Time;
        this.w1Time = w1Time;
        this.w2Time = w2Time;
        this.w3Time = w3Time;
        this.w4Time = w4Time;
        this.w5Time = w5Time;
        this.w6Time = w6Time;
        this.w7Time = w7Time;
        this.w8Time = w8Time;
        this.totalTime = totalTime;
        this.creationDate = creationDate;
    }

    public FeedPostResponse(Integer id2, Integer id3, String username2, String photoUrl2, Integer r1Time2,
            Integer r2Time2, Integer r3Time2, Integer r4Time2, Integer r5Time2, Integer r6Time2, Integer r7Time2,
            Integer r8Time2, Integer w1Time2, Integer w2Time2, Integer w3Time2, Integer w4Time2, Integer w5Time2,
            Integer w6Time2, Integer w7Time2, Integer w8Time2, Integer totalTime2, LocalDateTime creationDate2) {
        //TODO Auto-generated constructor stub
        this.id = id2;
        this.userId = id3;
        this.username = username2;
        this.photoUrl = photoUrl2;
        this.r1Time = r1Time2.toString();
        this.r2Time = r2Time2.toString();
        this.r3Time = r3Time2.toString();
        this.r4Time = r4Time2.toString();
        this.r5Time = r5Time2.toString();
        this.r6Time = r6Time2.toString();
        this.r7Time = r7Time2.toString();
        this.r8Time = r8Time2.toString();
        this.w1Time = w1Time2.toString();
        this.w2Time = w2Time2.toString();
        this.w3Time = w3Time2.toString();
        this.w4Time = w4Time2.toString();
        this.w5Time = w5Time2.toString();
        this.w6Time = w6Time2.toString();
        this.w7Time = w7Time2.toString();
        this.w8Time = w8Time2.toString();
        this.totalTime = totalTime2.toString();
        this.creationDate = creationDate2;
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

    public String getW1Time() { return w1Time; }
    public void setW1Time(String w1Time) { this.w1Time = w1Time; }

    public String getW2Time() { return w2Time; }
    public void setW2Time(String w2Time) { this.w2Time = w2Time; }

    public String getW3Time() { return w3Time; }
    public void setW3Time(String w3Time) { this.w3Time = w3Time; }

    public String getW4Time() { return w4Time; }
    public void setW4Time(String w4Time) { this.w4Time = w4Time; }

    public String getW5Time() { return w5Time; }
    public void setW5Time(String w5Time) { this.w5Time = w5Time; }

    public String getW6Time() { return w6Time; }
    public void setW6Time(String w6Time) { this.w6Time = w6Time; }

    public String getW7Time() { return w7Time; }
    public void setW7Time(String w7Time) { this.w7Time = w7Time; }

    public String getW8Time() { return w8Time; }
    public void setW8Time(String w8Time) { this.w8Time = w8Time; }

    public String getTotalTime() { return totalTime; }
    public void setTotalTime(String totalTime) { this.totalTime = totalTime; }

    public LocalDateTime getCreationDate() { return creationDate; }
    public void setCreationDate(LocalDateTime creationDate) { this.creationDate = creationDate; }
}
