package com.trainhub.backend.model;

import com.trainhub.backend.enums.PostCategory;
import com.trainhub.backend.enums.PostType;
import com.trainhub.backend.enums.TrainingTag;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;

/**
 * Entidad que representa una publicación (entrenamiento) en el sistema.
 * Mapeada a la tabla "posts" de la base de datos.
 */
@Entity
@Table(name = "posts")
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "post_type", nullable = false)
    private PostType postType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "box_id")
    private Box box;

    @Column(name = "title", length = 150)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(name = "training_tag", length = 20)
    private TrainingTag trainingTag;

    @Column(name = "challenge_deadline")
    private OffsetDateTime challengeDeadline;

    @Column(name = "running1")
    private Integer running1;

    @Column(name = "running2")
    private Integer running2;

    @Column(name = "running3")
    private Integer running3;

    @Column(name = "running4")
    private Integer running4;

    @Column(name = "running5")
    private Integer running5;

    @Column(name = "running6")
    private Integer running6;

    @Column(name = "running7")
    private Integer running7;

    @Column(name = "running8")
    private Integer running8;

    @Column(name = "skiErg")
    private Integer skiErg;

    @Column(name = "sledPush")
    private Integer sledPush;

    @Column(name = "sledPull")
    private Integer sledPull;

    @Column(name = "burpeeBroadJump")
    private Integer burpeeBroadJump;

    @Column(name = "row")
    private Integer row;

    @Column(name = "farmersCarry")
    private Integer farmersCarry;

    @Column(name = "sandbagLunges")
    private Integer sandbagLunges;

    @Column(name = "wallBalls")
    private Integer wallBalls;

    @Column(name = "total_time")
    private Integer totalTime;

    @Column(name = "description")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "category")
    private PostCategory category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mate")
    private User mate;

    @Column(name = "creation_date", nullable = false)
    private LocalDateTime creationDate;

    public Post() {}


    public Post(User user, Integer running1, Integer running2, Integer running3, Integer running4, Integer running5, 
        Integer running6, Integer running7, Integer running8, Integer skiErg, Integer sledPush, Integer sledPull, 
        Integer burpeeBroadJump, Integer row, Integer farmersCarry, Integer sandbagLunges, Integer wallBalls, 
        Integer totalTime, LocalDateTime creationDate) {
        this.user = user;
        this.running1 = running1;
        this.running2 = running2;
        this.running3 = running3;
        this.running4 = running4;
        this.running5 = running5;
        this.running6 = running6;
        this.running7 = running7;
        this.running8 = running8;
        this.skiErg = skiErg;
        this.sledPush = sledPush;
        this.sledPull = sledPull;
        this.burpeeBroadJump = burpeeBroadJump;
        this.row = row;
        this.farmersCarry = farmersCarry;
        this.sandbagLunges = sandbagLunges;
        this.wallBalls = wallBalls;
        this.totalTime = totalTime;
        this.creationDate = creationDate;
    }
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public PostType getPostType() { return postType; }
    public void setPostType(PostType postType) { this.postType = postType; }

    public Box getBox() { return box; }
    public void setBox(Box box) { this.box = box; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public TrainingTag getTrainingTag() { return trainingTag; }
    public void setTrainingTag(TrainingTag trainingTag) { this.trainingTag = trainingTag; }

    public OffsetDateTime getChallengeDeadline() { return challengeDeadline; }
    public void setChallengeDeadline(OffsetDateTime challengeDeadline) { this.challengeDeadline = challengeDeadline; }

    public Integer getRunning1() {
        return running1;
    }

    public void setRunning1(Integer running1) {
        this.running1 = running1;
    }

    public Integer getRunning2() {
        return running2;
    }

    public void setRunning2(Integer running2) {
        this.running2 = running2;
    }

    public Integer getRunning3() {
        return running3;
    }

    public void setRunning3(Integer running3) {
        this.running3 = running3;
    }

    public Integer getRunning4() {
        return running4;
    }

    public void setRunning4(Integer running4) {
        this.running4 = running4;
    }

    public Integer getRunning5() {
        return running5;
    }

    public void setRunning5(Integer running5) {
        this.running5 = running5;
    }

    public Integer getRunning6() {
        return running6;
    }

    public void setRunning6(Integer running6) {
        this.running6 = running6;
    }

    public Integer getRunning7() {
        return running7;
    }

    public void setRunning7(Integer running7) {
        this.running7 = running7;
    }

    public Integer getRunning8() {
        return running8;
    }

    public void setRunning8(Integer running8) {
        this.running8 = running8;
    }

    public Integer getSkiErg() {
        return skiErg;
    }

    public void setSkiErg(Integer skiErg) {
        this.skiErg = skiErg;
    }

    public Integer getSledPush() {
        return sledPush;
    }

    public void setSledPush(Integer sledPush) {
        this.sledPush = sledPush;
    }

    public Integer getSledPull() {
        return sledPull;
    }

    public void setSledPull(Integer sledPull) {
        this.sledPull = sledPull;
    }

    public Integer getBurpeeBroadJump() {
        return burpeeBroadJump;
    }

    public void setBurpeeBroadJump(Integer burpeeBroadJump) {
        this.burpeeBroadJump = burpeeBroadJump;
    }

    public Integer getRow() {
        return row;
    }

    public void setRow(Integer row) {
        this.row = row;
    }

    public Integer getFarmersCarry() {
        return farmersCarry;
    }

    public void setFarmersCarry(Integer farmersCarry) {
        this.farmersCarry = farmersCarry;
    }

    public Integer getSandbagLunges() {
        return sandbagLunges;
    }

    public void setSandbagLunges(Integer sandbagLunges) {
        this.sandbagLunges = sandbagLunges;
    }

    public Integer getWallBalls() {
        return wallBalls;
    }

    public void setWallBalls(Integer wallBalls) {
        this.wallBalls = wallBalls;
    }

    public Integer getTotalTime() { return totalTime; }
    public void setTotalTime(Integer totalTime) { this.totalTime = totalTime; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public PostCategory getCategory() { return category; }
    public void setCategory(PostCategory category) { this.category = category; }

    public User getMate() { return mate; }
    public void setMate(User mate) { this.mate = mate; }

    public LocalDateTime getCreationDate() { return creationDate; }
    public void setCreationDate(LocalDateTime creationDate) { this.creationDate = creationDate; }
}
