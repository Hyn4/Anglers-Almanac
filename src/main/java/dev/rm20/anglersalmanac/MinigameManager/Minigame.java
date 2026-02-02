package dev.rm20.anglersalmanac.MinigameManager;

import dev.rm20.anglersalmanac.metadata.FishingRodData;
import dev.rm20.anglersalmanac.utils.FishLootManager;

public abstract class Minigame {

    // How long a player has been playing
    private float TimePlayed = 0;
    // Total points that the player has
    private int Points = 0;
    // PerfectScore
    private float perfectScore = 100;
    public enum PerformanceRating {FAIL, GOOD, GREAT, PERFECT}

    public FishLootManager fishHooked;

    public float getTimePlayed() {
        return TimePlayed;
    }

    public void setTimePlayed(float timePlayed) {
        TimePlayed = timePlayed;
    }

    public  int getPoints() {
        return Points;
    }

    public void setPoints(int points) {
        Points = points;
    }

    public float getPerfectScore() {
        return perfectScore;
    }

    public void setPerfectScore(float perfectScore) {
        this.perfectScore = perfectScore;
    }

    public int getPerformancePercentage(){
        return (int) (Points / perfectScore) * 100;
    }

    public PerformanceRating getPerformanceRating(int performancePercentage){
        if(performancePercentage >= 95){
            return PerformanceRating.PERFECT;
        }
        if(performancePercentage >= 80){
            return PerformanceRating.GREAT;
        }
        if(performancePercentage >= 40){
            return PerformanceRating.GOOD;
        }

       return PerformanceRating.FAIL;
    }

    /*
    public abstract void applyDifficultyModifer(FishLootManager.MinigameStats stats);
    public abstract void applyFishBehaviourModifer(FishLootManager.MinigameStats stats);
    public abstract void applyFishStaminaModifer(FishLootManager.MinigameStats stats);
    public abstract void applyRodSizeModifer(FishingRodData rodData);
    public abstract void applyRodDifficultyModifer(FishingRodData rodData);
    public abstract void applyRodStaminaModifer(FishingRodData rodData);
     */

}
