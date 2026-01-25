package dev.rm20.anglersalmanac.components;

import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nullable;

public class MinigameComponent  implements Component<EntityStore> {
    // How long a player has been playing
    private float TimePlayed;
    // Total points that the player has
    private int Points;
    // PerfectScore
    private float perfectScore;
    public float getTimePlayed() {
        return TimePlayed;
    }

    public void setTimePlayed(float timePlayed) {
        TimePlayed = timePlayed;
    }

    public int getPoints() {
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

    @Nullable
    @Override
    public Component<EntityStore> clone() {
        MinigameComponent component = new MinigameComponent();
        component.TimePlayed = this.TimePlayed;
        component.Points = this.Points;
        component.perfectScore = this.perfectScore;
        return component;
    }
}
