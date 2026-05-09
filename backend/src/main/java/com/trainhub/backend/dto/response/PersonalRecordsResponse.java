package com.trainhub.backend.dto.response;

import java.time.LocalDateTime;

/**
 * Records personales all-time del usuario: mejor tiempo para cada segmento HYROX.
 * Un campo puede ser null si el usuario no tiene ningún post registrado para ese segmento.
 */
public class PersonalRecordsResponse {

    private RecordEntry bestTotal;
    private RecordEntry bestRunning;
    private RecordEntry bestSkiErg;
    private RecordEntry bestSledPush;
    private RecordEntry bestSledPull;
    private RecordEntry bestBurpeeBj;
    private RecordEntry bestRow;
    private RecordEntry bestFarmersCarry;
    private RecordEntry bestSandbagLunges;
    private RecordEntry bestWallBalls;

    public PersonalRecordsResponse() {}

    public PersonalRecordsResponse(
            RecordEntry bestTotal,
            RecordEntry bestRunning,
            RecordEntry bestSkiErg,
            RecordEntry bestSledPush,
            RecordEntry bestSledPull,
            RecordEntry bestBurpeeBj,
            RecordEntry bestRow,
            RecordEntry bestFarmersCarry,
            RecordEntry bestSandbagLunges,
            RecordEntry bestWallBalls) {
        this.bestTotal = bestTotal;
        this.bestRunning = bestRunning;
        this.bestSkiErg = bestSkiErg;
        this.bestSledPush = bestSledPush;
        this.bestSledPull = bestSledPull;
        this.bestBurpeeBj = bestBurpeeBj;
        this.bestRow = bestRow;
        this.bestFarmersCarry = bestFarmersCarry;
        this.bestSandbagLunges = bestSandbagLunges;
        this.bestWallBalls = bestWallBalls;
    }

    public RecordEntry getBestTotal()          { return bestTotal; }
    public RecordEntry getBestRunning()        { return bestRunning; }
    public RecordEntry getBestSkiErg()         { return bestSkiErg; }
    public RecordEntry getBestSledPush()       { return bestSledPush; }
    public RecordEntry getBestSledPull()       { return bestSledPull; }
    public RecordEntry getBestBurpeeBj()       { return bestBurpeeBj; }
    public RecordEntry getBestRow()            { return bestRow; }
    public RecordEntry getBestFarmersCarry()   { return bestFarmersCarry; }
    public RecordEntry getBestSandbagLunges()  { return bestSandbagLunges; }
    public RecordEntry getBestWallBalls()      { return bestWallBalls; }

    /**
     * Par (tiempo en segundos, fecha del post) que representa un record personal.
     */
    public static class RecordEntry {

        private Integer time;
        private LocalDateTime date;

        public RecordEntry() {}

        public RecordEntry(Integer time, LocalDateTime date) {
            this.time = time;
            this.date = date;
        }

        public Integer getTime() { return time; }
        public void setTime(Integer time) { this.time = time; }

        public LocalDateTime getDate() { return date; }
        public void setDate(LocalDateTime date) { this.date = date; }
    }
}
