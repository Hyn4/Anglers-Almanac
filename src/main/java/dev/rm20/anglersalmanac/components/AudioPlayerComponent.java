package dev.rm20.anglersalmanac.components;

import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.server.core.asset.common.OggVorbisInfoCache;
import com.hypixel.hytale.server.core.asset.type.soundevent.config.SoundEvent;
import com.hypixel.hytale.server.core.modules.entity.system.AudioSystems;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.rm20.anglersalmanac.AnglersAlmanac;
import org.jspecify.annotations.Nullable;

import javax.sound.sampled.spi.AudioFileReader;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class AudioPlayerComponent implements Component<EntityStore> {
    public static ComponentType<EntityStore, MinigameComponent_TensionBar> COMPONENT_TYPE;
    private List<String> sounds = new ArrayList<>();
    private HashMap<String, Double> soundDurations = new HashMap<>();
    public long playNextTime = 0;
    public long lastPlayTime = 0;


    public void addSound(String soundEventId){
        if(sounds.contains(soundEventId)) return; // Already contains sound, don't duplicate.
        sounds.add(soundEventId);
        soundDurations.put(soundEventId, readDurationOf(soundEventId));
    }

    public void removeSound(String soundEventId){
        sounds.remove(soundEventId);
        soundDurations.remove(soundEventId);
    }

    private double readDurationOf(String soundEventId){
        if(!sounds.contains(soundEventId)) return 0;
        AnglersAlmanac.LOGGER.atInfo().log("file path: %s", SoundEvent.getAssetMap().getPath(soundEventId).toString());
        AnglersAlmanac.LOGGER.atInfo().log("duration: %s", OggVorbisInfoCache.getNow(SoundEvent.getAssetMap().getPath(soundEventId).toString()).duration);
        return OggVorbisInfoCache.getNow(SoundEvent.getAssetMap().getPath(soundEventId).toString()).duration;
    }

    public double getDurationOf(String soundEventId){
        if(!sounds.contains(soundEventId)) return 0;
        return soundDurations.get(soundEventId);
    }

    public boolean isCurrentlyPlaying(){
        //TimeUnit.SECONDS.convert(System.nanoTime() - lastCastOrReelTime, TimeUnit.NANOSECONDS) >= cooldownTime;
        return TimeUnit.SECONDS.convert(System.nanoTime() - lastPlayTime, TimeUnit.NANOSECONDS) < playNextTime;
    }

    public void playRandomSound(Vector3d pos){
        String soundId = sounds.get(new Random().nextInt(sounds.size()));
        playSound(soundId, pos);
    }

    public void playSound(String soundId, Vector3d pos){

        playNextTime = System.nanoTime() + TimeUnit.NANOSECONDS.convert((long)getDurationOf(soundId), TimeUnit.SECONDS);
        lastPlayTime = System.nanoTime();
    }


    @Override
    public @Nullable Component<EntityStore> clone() {
        return null;
    }
}
