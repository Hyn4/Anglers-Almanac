package dev.rm20.anglersalmanac.models;

import com.google.common.flogger.MetadataKey;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;

import java.util.UUID;

public class FishingRodData {
    public static final String KEY = "AnglersAlmanacBoundBobber";
    public static final BuilderCodec<FishingRodData> CODEC = BuilderCodec.builder(FishingRodData.class, FishingRodData::new)
            .append(new KeyedCodec<>("BoundBobber", Codec.UUID_BINARY), (metaData, value) -> metaData.boundBobber = value, (config) -> config.boundBobber).add()
            .build();
    public static final KeyedCodec<FishingRodData> KEYED_CODEC = new KeyedCodec<>(KEY, CODEC);
    private UUID boundBobber = null;

    public UUID getBoundBobber() {
        return this.boundBobber;
    }
    
    public void setBoundBobber(UUID uuid) {
        this.boundBobber = uuid;
    }
}
