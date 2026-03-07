package dev.rm20.anglersalmanac.AlmanacBook;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.rm20.anglersalmanac.AnglersAlmanac;
import dev.rm20.anglersalmanac.models.BookAssetData;
import dev.rm20.anglersalmanac.utils.FishLootManager;
import dev.rm20.anglersalmanac.utils.StampUtil;
import dev.rm20.anglersalmanac.utils.TextUtils;

import javax.annotation.Nonnull;
import java.io.Console;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static dev.rm20.anglersalmanac.AlmanacBook.BookPageManager.OpenPage;

public class FishZoneUiPage extends InteractiveCustomUIPage<StatUiPage.AlmanacGuiData> {
    private final String PlayerUUID;
    private final String PlayerName;
    private final AlmanacDatabase.PlayerStatsData Stats;
    private final String ZoneName;
    private final FishLootManager FishDataRight;
    private final int Page;
    private final BookAssetData.ZoneInfo zoneInfo;

    public FishZoneUiPage(PlayerRef playerRef, String playerUUID, String playerName, AlmanacDatabase.PlayerStatsData stats, String zoneName, FishLootManager fishDataRight, int page,BookAssetData.ZoneInfo ZoneInfo) {
        super(playerRef, CustomPageLifetime.CanDismiss, StatUiPage.AlmanacGuiData.CODEC);
        PlayerUUID = playerUUID;
        PlayerName = playerName;
        Stats = stats;
        ZoneName = zoneName;
        FishDataRight = fishDataRight;
        Page = page;
        zoneInfo = ZoneInfo;
    }

    @Override
    public void build(@Nonnull Ref<EntityStore> ref, @Nonnull UICommandBuilder uiCommandBuilder, @Nonnull UIEventBuilder uiEventBuilder, @Nonnull Store<EntityStore> store) {
        uiCommandBuilder.append("Almanac/Fish/AlmanacFishZone.ui");
        uiEventBuilder.addEventBinding(
                CustomUIEventBindingType.Activating,
                "#NextPageButton",
                EventData.of(StatUiPage.AlmanacGuiData.KEY_BUTTON, "NextPage"),
                false
        );
        uiEventBuilder.addEventBinding(
                CustomUIEventBindingType.Activating,
                "#PrevPageButton",
                EventData.of(StatUiPage.AlmanacGuiData.KEY_BUTTON, "PrevPage"),
                false
        );
        int totalSlots = 10;
        BookAssetData bookAsset = BookAssetData.getAssetStore().getAssetMap().getAssetMap().values().stream().toList().getFirst();
        List<String> Fish = bookAsset.getFishByHabitat(ZoneName);
        //AnglersAlmanac.getInstance().getLogger().atInfo().log(Fish.toString());
        uiCommandBuilder.set("#Header.Text", ZoneName + " Fish");
        uiCommandBuilder.set("#ZoneHeader.Text", zoneInfo.zoneDescription);
        uiCommandBuilder.set("#ZoneIconImage.AssetPath",zoneInfo.ZoneImage);
        if(zoneInfo.ProgressBarColour != null)
        {
            uiCommandBuilder.set("#FishProgress.Color", zoneInfo.ProgressBarColour);
        }
        //Fish size
        int totalInZone = Fish.size();
        int caughtCount = 0;
        for (int i = 0; i < totalSlots; i++) {
            String missingNodeId = "#ItemIcon" + i + "Missing";
            String iconNodeId = "#ItemIcon" + i;
            String bgNodeId = "#ItemIcon" + i + "BG";

            if (i < Fish.size() - 1) {
                String fishKey = Fish.get(i + 1);
                if (AnglersAlmanac.getInstance().database.hasPlayerCaught(PlayerUUID, fishKey)) {
                    FishLootManager actualItem = FishLootManager.getFishData(fishKey);
                    uiCommandBuilder.set(bgNodeId + ".Visible", true);
                    uiCommandBuilder.set(missingNodeId + ".Visible", false);
                    uiCommandBuilder.set(iconNodeId + ".ItemId", actualItem.getItemID());
                    uiCommandBuilder.set(iconNodeId + ".TooltipText", actualItem.getName());
                    caughtCount++;
                } else {
                    uiCommandBuilder.set(bgNodeId + ".Visible", true);
                    uiCommandBuilder.set(missingNodeId + ".Visible", true);
                    uiCommandBuilder.set(iconNodeId + ".ItemId", "");
                    uiCommandBuilder.set(missingNodeId + ".TooltipText", "Not Found");
                }
            } else {
                uiCommandBuilder.set(bgNodeId + ".Visible", false);
                uiCommandBuilder.set(missingNodeId + ".Visible", false);
                uiCommandBuilder.set(iconNodeId + ".ItemId", "");
            }
        }
        float progressValue = (totalInZone > 0) ? (float) caughtCount / totalInZone : 0f;
        if(progressValue>1){
            progressValue=1;
        }
        //AnglersAlmanac.getInstance().getLogger().atInfo().log(String.valueOf(progressValue));
        uiCommandBuilder.set("#FishProgress.Value", progressValue);
        // Right page

        if (FishDataRight != null) {
            if (AnglersAlmanac.getInstance().database.hasPlayerCaught(PlayerUUID, FishDataRight.getId())) {
                int fishCount = Stats.getFishCount(FishDataRight.getId());
                String cleanName = FishDataRight.getItemID()
                        .replace("Fish_", "")
                        .replace("_Item", "");

                String Image;
                String HabitatInfo;
                FishLootManager.BookInfo bookInfo;
                if (FishDataRight.getBookInfo() != null) {
                    bookInfo = FishDataRight.getBookInfo();
                    Image = Objects.requireNonNullElseGet(bookInfo.image_file, () -> "UI/Custom/Almanac/Fish/Assets/" + cleanName + ".png");
                    HabitatInfo = Objects.requireNonNullElse(bookInfo.habitat_info, "");

                } else {
                    uiCommandBuilder.set("#HabitatSection2.Visible", false);
                    HabitatInfo = "";
                    Image = "UI/Custom/Almanac/Fish/Assets/" + cleanName + ".png";
                }
                uiCommandBuilder.set("#HabitatList2.Text", HabitatInfo);
                String RarityFile = StampUtil.getStamp(cleanName, FishDataRight.getRarity());
                if (RarityFile != null) {
                    String RarityPath = "UI/Custom/Almanac/Fish/Stamps/" + FishDataRight.getRarity() + "/" + RarityFile + ".png";
                    uiCommandBuilder.set("#StampImage2.AssetPath", RarityPath);

                }
                uiCommandBuilder.set("#FishImage2.AssetPath", Image);
                uiCommandBuilder.set("#Header2.TextSpans", Message.raw(FishDataRight.getName()));
                uiCommandBuilder.set("#CountNumber2.TextSpans", Message.raw(String.valueOf(fishCount)));
                uiCommandBuilder.set("#Family2.TextSpans", Message.raw(TextUtils.formatDisplayName(FishDataRight.getFamilyId())));
                uiCommandBuilder.set("#Description2.TextSpans", Message.raw(FishDataRight.getDescription()));
            } else {
                uiCommandBuilder.set("#Header2.TextSpans", Message.raw(TextUtils.seededScrambleText(FishDataRight.getName())));
                uiCommandBuilder.set("#CountNumber2.TextSpans", Message.raw("Not Found"));
                uiCommandBuilder.set("#Family2.TextSpans", Message.raw(TextUtils.seededScrambleText(FishDataRight.getFamilyId())));
                uiCommandBuilder.set("#Description2.TextSpans", Message.raw(TextUtils.seededScrambleText(FishDataRight.getDescription())));
            }
        }
    }

    @Override
    public void handleDataEvent(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store, @Nonnull StatUiPage.AlmanacGuiData data) {
        super.handleDataEvent(ref, store, data);

        Player player = store.getComponent(ref, Player.getComponentType());
        if (player == null || data.getButton() == null) return;
        if (data.getButton().equals("PrevPage")) {
            OpenPage(player, (Page - 1), PlayerUUID, PlayerName);
        }
        if (data.getButton().equals("NextPage")) {
            int newPage = BookPageManager.getNextPage(Page);
            if (newPage == Page) {
                return;
            }
            OpenPage(player, newPage, PlayerUUID, PlayerName);
        }
    }

}