package dev.rm20.anglersalmanac.Systems;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.server.core.entity.ItemUtils;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.rm20.anglersalmanac.AnglersAlmanac;
import dev.rm20.anglersalmanac.components.MinigameComponent;
import dev.rm20.anglersalmanac.interactions.LaunchBobberInteraction;
import dev.rm20.anglersalmanac.models.FishingRodData;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Random;

public class MinigameSystem extends EntityTickingSystem<EntityStore> {
    @Override
    public void tick(float v, int i, @NonNull ArchetypeChunk<EntityStore> archetypeChunk, @NonNull Store<EntityStore> store, @NonNull CommandBuffer<EntityStore> commandBuffer) {

        MinigameComponent game = store.getComponent(archetypeChunk.getReferenceTo(i), MinigameComponent.getComponentType());

        Ref<EntityStore> playerRef = game.ownerRef;
        Player player = store.getComponent(playerRef, Player.getComponentType());
        ItemStack rodItem = player.getInventory().getActiveHotbarItem(); // TODO ensure that this is always actually the rod. (cancel minigame if switched off)
        FishingRodData rodMeta = rodItem.getFromMetadataOrNull(FishingRodData.KEYED_CODEC);

        switch (game.stateTrigger){
            case FISHMOVE:
                game.nextFishMoveTime = new Random().nextFloat() * 3f;
                game.fishMoveTimer = 0f;
                float maxFishVel = AnglersAlmanac.MINIGAME_CONFIG.get().fishMaxVeocity;
                game.fishVelocity = (maxFishVel*-1f) + new Random().nextFloat() * (maxFishVel - (maxFishVel*-1f));
                if(game.fishPos <= 5) game.fishVelocity = Math.abs(game.fishVelocity); // Always ensure that fish moves away from edges if near top / bottom.
                if(game.fishPos >= 95) game.fishVelocity = Math.abs(game.fishVelocity) * -1f; //  ^
                game.stateTrigger = MinigameComponent.Trigger.NOTRIGGER;
                break;
            case FAIL:
                AnglersAlmanac.LOGGER.atInfo().log("YOU FAIL");
                // Reel in the rod which the bobber owner is using.

                LaunchBobberInteraction.stopFishing(commandBuffer, player, rodItem);
                break;
            case SUCCESS:
                AnglersAlmanac.LOGGER.atInfo().log("YOU WIN");
                // Spawn fish rewards!
                //ItemStack fishStack = DefaultLootTableGenerator.createRandomFish(DefaultLootTableGenerator.GENERIC());
                //if (!fishStack.isEmpty()) {
                //    ItemUtils.throwItem(store.getExternalData().getWorld().getEntityRef(bobber.ownerID), fishStack, 4.0F, commandBuffer);
                //}
                // Reel in the rod which the bobber owner is using.
                //UseFishingRodInteraction.reelRod(store.getComponent(store.getExternalData().getWorld().getEntityRef(bobber.ownerID), Player.getComponentType()), store.getExternalData().getWorld(), bobber);
                LaunchBobberInteraction.stopFishing(commandBuffer, player, rodItem);
                break;
        }

        game.stateTrigger = MinigameComponent.Trigger.SUCCESS;

    }

    @Override
    public @Nullable Query<EntityStore> getQuery() {
        return MinigameComponent.getComponentType();
    }
}
