package com.trainhub.backend.model;

import com.trainhub.backend.enums.PostCategory;
import jakarta.persistence.*;
import java.time.LocalDateTime;

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

    @Column(name = "running1", nullable = false)
    private Integer running1;

    @Column(name = "running2", nullable = false)
    private Integer running2;

    @Column(name = "running3", nullable = false)
    private Integer running3;

    @Column(name = "running4", nullable = false)
    private Integer running4;

    @Column(name = "running5", nullable = false)
    private Integer running5;

    @Column(name = "running6", nullable = false)
    private Integer running6;

    @Column(name = "running7", nullable = false)
    private Integer running7;

    @Column(name = "running8", nullable = false)
    private Integer running8;

    @Column(name = "skiErg", nullable = false)
    private Integer skiErg;

    @Column(name = "sledPush", nullable = false)
    private Integer sledPush;

    @Column(name = "sledPull", nullable = false)
    private Integer sledPull;

    @Column(name = "burpeeBroadJump", nullable = false)
    private Integer burpeeBroadJump;

    @Column(name = "row", nullable = false)
    private Integer row;

    @Column(name = "farmersCarry", nullable = false)
    private Integer farmersCarry;

    @Column(name = "sandbagLunges", nullable = false)
    private Integer sandbagLunges;

    @Column(name = "wallBalls", nullable = false)
    private Integer wallBalls;

    @Column(name = "total_time", nullable = false)
    private Integer totalTime;

    @Column(name = "description")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false)
    private PostCategory category;

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

    public LocalDateTime getCreationDate() { return creationDate; }
    public void setCreationDate(LocalDateTime creationDate) { this.creationDate = creationDate; }
}
