package dev.rm20.anglersalmanac.Systems;

import com.hypixel.hytale.common.util.ArrayUtil;
import com.hypixel.hytale.common.util.AudioUtil;
import com.hypixel.hytale.component.*;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.protocol.SoundCategory;
import com.hypixel.hytale.protocol.packets.assets.UpdateSoundEvents;
import com.hypixel.hytale.server.core.asset.type.soundevent.SoundEventPacketGenerator;
import com.hypixel.hytale.server.core.asset.type.soundevent.config.SoundEvent;
import com.hypixel.hytale.server.core.asset.type.soundevent.config.SoundEventLayer;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.modules.entity.component.AudioComponent;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.modules.entity.system.AudioSystems;
import com.hypixel.hytale.server.core.modules.entity.tracker.NetworkId;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.SoundUtil;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.rm20.anglersalmanac.AnglersAlmanac;
import dev.rm20.anglersalmanac.MinigameManager.MinigameManager;
import dev.rm20.anglersalmanac.components.BobberComponent;
import dev.rm20.anglersalmanac.components.MinigameComponent_TensionBar;
import dev.rm20.anglersalmanac.interactions.LaunchBobberInteraction;
import dev.rm20.anglersalmanac.models.FishingRodData;
import dev.rm20.anglersalmanac.utils.SoundUtils;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntPredicate;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.*;
import java.util.stream.IntStream;

public class MinigameSystem_TensionBar extends EntityTickingSystem<EntityStore> {
    ItemStack fishingRod = null;
    UUID reelAudioEntityId;
    UUID escapeAudioEntityId;
    List<String> soundAssetKeys = Arrays.asList("AA_Fishing_Reel_Slow0", "AA_Fishing_Reel_Slow1", "AA_Fishing_Reel_Slow2", "AA_Fishing_Reel_Slow3");


    @Override
    public void tick(float deltaTime, int i, @NonNull ArchetypeChunk<EntityStore> archetypeChunk, @NonNull Store<EntityStore> store, @NonNull CommandBuffer<EntityStore> commandBuffer) {

        MinigameComponent_TensionBar game = store.getComponent(archetypeChunk.getReferenceTo(i), MinigameComponent_TensionBar.COMPONENT_TYPE);

        Ref<EntityStore> playerRef = game.ownerRef;
        Player player = store.getComponent(playerRef, Player.getComponentType());
        ItemStack rodItem = player.getInventory().getActiveHotbarItem(); // TODO ensure that this is always actually the rod. (cancel minigame if switched off)
        Vector3d playerPos = store.getComponent(playerRef, TransformComponent.getComponentType()).getPosition().clone();

        if(rodItem == null)
        {
            return;
        }
        FishingRodData rodMeta = rodItem.getFromMetadataOrNull(FishingRodData.KEYED_CODEC);
        if(rodMeta != null)
        {
            fishingRod = rodItem;
        }


        switch (game.stateTrigger){
            case FISHMOVE:
                game.nextFishMoveTime = new Random().nextFloat() * 3f;
                game.fishMoveTimer = 0f;
                float maxFishVel = AnglersAlmanac.MINIGAME_CONFIG_TENSIONBAR.get().fishMaxVeocity;
                game.fishVelocity = (maxFishVel*-1f) + new Random().nextFloat() * (maxFishVel - (maxFishVel*-1f));
                if(game.fishPos <= 5) game.fishVelocity = Math.abs(game.fishVelocity); // Always ensure that fish moves away from edges if near top / bottom.
                if(game.fishPos >= 95) game.fishVelocity = Math.abs(game.fishVelocity) * -1f; //  ^
                game.stateTrigger = MinigameComponent_TensionBar.Trigger.NOTRIGGER;
                break;
            case FAIL:
                AnglersAlmanac.LOGGER.atInfo().log("YOU FAIL");
                // Reel in the rod which the bobber owner is using.
                LaunchBobberInteraction.cancelFishing(commandBuffer, player, fishingRod);
                break;
            case SUCCESS:
                AnglersAlmanac.LOGGER.atInfo().log("YOU WIN");
                // Deal rewards.
                String lootID = MinigameManager.FirstRoll(game.bobberRef, player, commandBuffer, store.getComponent(game.bobberRef, BobberComponent.getComponentType()).getWaterDepth());
                MinigameManager.DropLoot(lootID, player, commandBuffer,game.bobberRef);

                // Finish fishing.
                LaunchBobberInteraction.cancelFishing(commandBuffer, player, fishingRod);
                break;
        }


        // Do minigame logic.

        PlayerRef playerRefObj = store.getComponent(playerRef, PlayerRef.getComponentType());
        AudioComponent audioComponent = store.getComponent(store.getExternalData().getRefFromUUID(game.selfUUID), AudioComponent.getComponentType());


        // Check if bar is over the fish and check win state.
        if(game.fishPos < game.barPos +  AnglersAlmanac.MINIGAME_CONFIG_TENSIONBAR.get().barRadius && game.fishPos > game.barPos - AnglersAlmanac.MINIGAME_CONFIG_TENSIONBAR.get().barRadius){
            game.fightProgress += AnglersAlmanac.MINIGAME_CONFIG_TENSIONBAR.get().fishReelRate * deltaTime;

            // TODO Pause old sound when switching to new sound.

            // Remove old audio
            if(escapeAudioEntityId != null) {
                //SoundUtils.changeSoundAssetVolume(playerRefObj, reelAudioID, 0);
                commandBuffer.getExternalData().getWorld().execute(() -> {
                        commandBuffer.removeEntity(commandBuffer.getExternalData().getRefFromUUID(escapeAudioEntityId), RemoveReason.REMOVE);
                    escapeAudioEntityId = null;
                    });

                AnglersAlmanac.LOGGER.atInfo().log("Removed escape sound");
            }

            // Add new audio if not currently playing.
            if(reelAudioEntityId == null || commandBuffer.getExternalData().getRefFromUUID(reelAudioEntityId) == null) {
                //SoundUtils.changeSoundAssetVolume(playerRefObj, reelAudioID, 1);
                //SoundUtil.playSoundEvent3d(reelAudioIndex, SoundCategory.SFX, store.getComponent(game.ownerRef, TransformComponent.getComponentType()).getPosition(), playerRef.getStore());
                //String reelAudioID = soundAssetKeys.get(new Random().nextInt(soundAssetKeys.size()));
                String reelAudioID = "AA_Fishing_Reel_Slow";
                //SoundUtil.playSoundEventEntity(SoundEvent.getAssetMap().getIndex(reelAudioID), store.getComponent(playerRef, NetworkId.getComponentType()).getId(), store);
                reelAudioEntityId = SoundUtils.createNewSoundEntity(reelAudioID, playerPos, store);
                //audioComponent.addSound(reelAudioIndex);
                AnglersAlmanac.LOGGER.atInfo().log("Playing reel slow sound");
            }


            // Check win condition.
            if(game.fightProgress >= 1.0f){
                game.stateTrigger = MinigameComponent_TensionBar.Trigger.SUCCESS;
                return;
            }
        }else{
            game.fightProgress -= AnglersAlmanac.MINIGAME_CONFIG_TENSIONBAR.get().fishEscapeRate * deltaTime;



            // Remove old audio
            if(reelAudioEntityId != null) {
                //SoundUtils.changeSoundAssetVolume(playerRefObj, reelAudioID, 0);
                commandBuffer.getExternalData().getWorld().execute(() -> {
                    commandBuffer.removeEntity(commandBuffer.getExternalData().getRefFromUUID(reelAudioEntityId), RemoveReason.REMOVE);
                    reelAudioEntityId = null;
                });


                AnglersAlmanac.LOGGER.atInfo().log("Removing reel slow sound");
            }

            // Add new audio
            if(escapeAudioEntityId == null || commandBuffer.getExternalData().getRefFromUUID(escapeAudioEntityId) == null) {
                //SoundUtils.changeSoundAssetVolume(playerRefObj, escapeAudioID, 1);
                //SoundUtil.playSoundEvent3d(escapeAudioIndex, SoundCategory.SFX, store.getComponent(game.ownerRef, TransformComponent.getComponentType()).getPosition(), playerRef.getStore());
                String escapeAudioID = "AA_Fishing_Line_Tension";
                escapeAudioEntityId = SoundUtils.createNewSoundEntity(escapeAudioID, playerPos, store);
                //audioComponent.addSound(escapeAudioIndex);
                AnglersAlmanac.LOGGER.atInfo().log("Playing line tension sound");
            }

            // Check win condition.
            if(game.fightProgress <= 0f){
                game.stateTrigger = MinigameComponent_TensionBar.Trigger.FAIL;
                return;
            }
        }

        // Check if fish will change velocity or direction.
        if(game.fishMoveTimer >= game.nextFishMoveTime){
            game.stateTrigger = MinigameComponent_TensionBar.Trigger.FISHMOVE;
        }

        // Apply bar motion. (Rising is computed in MinigameInteraction by changing barVelocity)
        game.barVelocity = Math.clamp(game.barVelocity - (AnglersAlmanac.MINIGAME_CONFIG_TENSIONBAR.get().barGravity*AnglersAlmanac.MINIGAME_CONFIG_TENSIONBAR.get().barAcceleration), -AnglersAlmanac.MINIGAME_CONFIG_TENSIONBAR.get().barGravity, AnglersAlmanac.MINIGAME_CONFIG_TENSIONBAR.get().barSpeed);
        game.barPos = Math.clamp(game.barPos + (game.barVelocity * deltaTime), 0f, 1.0f);

        // Apply fish movement.
        game.fishPos = Math.clamp(game.fishPos + (game.fishVelocity*deltaTime), 0f, 1.0f);

        game.updateMinigameModelPositions(store);
        game.fishMoveTimer += deltaTime;

    }

    @Override
    public @Nullable Query<EntityStore> getQuery() {
        return MinigameComponent_TensionBar.COMPONENT_TYPE;
    }
}
