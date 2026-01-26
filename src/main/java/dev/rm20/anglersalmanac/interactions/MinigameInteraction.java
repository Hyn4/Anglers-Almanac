package dev.rm20.anglersalmanac.interactions;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.protocol.InteractionState;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.SimpleInstantInteraction;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.rm20.anglersalmanac.AnglersAlmanac;
import dev.rm20.anglersalmanac.models.FishingRodData;
import org.jspecify.annotations.NonNull;

public class MinigameInteraction extends SimpleInstantInteraction {
    public static final BuilderCodec<MinigameInteraction> CODEC = BuilderCodec.builder(
            MinigameInteraction.class, MinigameInteraction::new, SimpleInstantInteraction.CODEC
    ).build();

    @Override
    protected void firstRun(@NonNull InteractionType interactionType, @NonNull InteractionContext context, @NonNull CooldownHandler cooldownHandler) {
        CommandBuffer<EntityStore> commandBuffer = context.getCommandBuffer();
        Ref<EntityStore> playerRef = context.getOwningEntity();
        ItemStack heldItem = context.getHeldItem();
        if (commandBuffer == null || playerRef == null || heldItem == null) return;

        AnglersAlmanac.LOGGER.atInfo().log("Testing if can do minigame interaction.");

        // Cancel interaction if the rod is not in minigame mode.
        FishingRodData meta = heldItem.getFromMetadataOrNull(FishingRodData.KEY, FishingRodData.CODEC);
        if(meta == null){
            context.getState().state = InteractionState.Failed;
            return;
        }
        if(meta.getMode() != 1){
            context.getState().state = InteractionState.Failed;
            return;
        }

        AnglersAlmanac.LOGGER.atInfo().log("Doing minigame interaction.");


    }


}
