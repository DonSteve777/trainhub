package com.trainhub.backend.dto.request;

import com.trainhub.backend.enums.PostCategory;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * DTO para la solicitud de creación de un nuevo post (entrenamiento).
 * Los tiempos están en segundos y deben ser >= 0.
 */
public class NewPostRequest {

    @NotNull @Min(0) private Integer r1Time;
    @NotNull @Min(0) private Integer r2Time;
    @NotNull @Min(0) private Integer r3Time;
    @NotNull @Min(0) private Integer r4Time;
    @NotNull @Min(0) private Integer r5Time;
    @NotNull @Min(0) private Integer r6Time;
    @NotNull @Min(0) private Integer r7Time;
    @NotNull @Min(0) private Integer r8Time;

    @NotNull @Min(0) private Integer skiErg;
    @NotNull @Min(0) private Integer sledPush;
    @NotNull @Min(0) private Integer sledPull;
    @NotNull @Min(0) private Integer burpeeBroadJump;
    @NotNull @Min(0) private Integer row;
    @NotNull @Min(0) private Integer farmersCarry;
    @NotNull @Min(0) private Integer sandbagLunges;
    @NotNull @Min(0) private Integer wallBalls;

    @NotNull private PostCategory category;

    private String mateUsername;

    public NewPostRequest() {}

    // Getters y Setters
    public Integer getR1Time() { return r1Time; }
    public void setR1Time(Integer r1Time) { this.r1Time = r1Time; }

    public Integer getR2Time() { return r2Time; }
    public void setR2Time(Integer r2Time) { this.r2Time = r2Time; }

    public Integer getR3Time() { return r3Time; }
    public void setR3Time(Integer r3Time) { this.r3Time = r3Time; }

    public Integer getR4Time() { return r4Time; }
    public void setR4Time(Integer r4Time) { this.r4Time = r4Time; }

    public Integer getR5Time() { return r5Time; }
    public void setR5Time(Integer r5Time) { this.r5Time = r5Time; }

    public Integer getR6Time() { return r6Time; }
    public void setR6Time(Integer r6Time) { this.r6Time = r6Time; }

    public Integer getR7Time() { return r7Time; }
    public void setR7Time(Integer r7Time) { this.r7Time = r7Time; }

    public Integer getR8Time() { return r8Time; }
    public void setR8Time(Integer r8Time) { this.r8Time = r8Time; }

    public Integer getSkiErg() {
        return skiErg;
    }

    public Integer getSledPush() {
        return sledPush;
    }

    public Integer getSledPull() {
        return sledPull;
    }

    public Integer getBurpeeBroadJump() {
        return burpeeBroadJump;
    }

    public Integer getRow() {
        return row;
    }

    public Integer getFarmersCarry() {
        return farmersCarry;
    }

    public Integer getSandbagLunges() {
        return sandbagLunges;
    }

    public Integer getWallBalls() {
        return wallBalls;
    }

    public void setSkiErg(Integer skiErg) {
        this.skiErg = skiErg;
    }

    public void setSledPush(Integer sledPush) {
        this.sledPush = sledPush;
    }

    public void setSledPull(Integer sledPull) {
        this.sledPull = sledPull;
    }

    public void setBurpeeBroadJump(Integer burpeeBroadJump) {
        this.burpeeBroadJump = burpeeBroadJump;
    }

    public void setRow(Integer row) {
        this.row = row;
    }

    public void setFarmersCarry(Integer farmersCarry) {
        this.farmersCarry = farmersCarry;
    }

    public void setSandbagLunges(Integer sandbagLunges) {
        this.sandbagLunges = sandbagLunges;
    }

    public void setWallBalls(Integer wallBalls) {
        this.wallBalls = wallBalls;
    }

    public PostCategory getCategory() { return category; }
    public void setCategory(PostCategory category) { this.category = category; }

    public String getMateUsername() { return mateUsername; }
    public void setMateUsername(String mateUsername) { this.mateUsername = mateUsername; }
}
