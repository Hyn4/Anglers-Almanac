package dev.rm20.anglersalmanac.models;

import com.hypixel.hytale.assetstore.AssetExtraInfo;
import com.hypixel.hytale.assetstore.AssetRegistry;
import com.hypixel.hytale.assetstore.AssetStore;
import com.hypixel.hytale.assetstore.codec.AssetBuilderCodec;
import com.hypixel.hytale.assetstore.map.DefaultAssetMap;
import com.hypixel.hytale.assetstore.map.JsonAssetWithMap;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.logger.HytaleLogger;
import dev.rm20.anglersalmanac.AnglersAlmanac;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


public class BookAssetData implements JsonAssetWithMap<String, DefaultAssetMap<String, BookAssetData>> {

    public static final BuilderCodec<ZoneInfo> ZONE_INFO_CODEC = BuilderCodec.builder(ZoneInfo.class, ZoneInfo::new)
            .append(new KeyedCodec<>("ZoneDescription", Codec.STRING), (z, v) -> z.zoneDescription = v, z -> z.zoneDescription).add()
            .append(new KeyedCodec<>("ZoneImage", Codec.STRING), (z, v) -> z.ZoneImage = v, z -> z.ZoneImage).add()
            .append(new KeyedCodec<>("ProgressBarImage", Codec.STRING), (z, v) -> z.ProgressBarImage = v, z -> z.ProgressBarImage).add()
            .append(new KeyedCodec<>("ProgressBarColour", Codec.STRING), (z, v) -> z.ProgressBarColour = v, z -> z.ProgressBarColour).add()
            .build();

    public static final BuilderCodec<PageContext> CONTEXT_CODEC = BuilderCodec.builder(PageContext.class, PageContext::new)
            .append(new KeyedCodec<>("ContextData", Codec.STRING), (c, v) -> c.contextData = v, c -> c.contextData).add()
            .append(new KeyedCodec<>("ZoneInfo", ZONE_INFO_CODEC), (c, v) -> c.zoneInfo = v, c -> c.zoneInfo).add()
            .build();

    public static final BuilderCodec<SpreadTemplate> SPREAD_CODEC = BuilderCodec.builder(SpreadTemplate.class, SpreadTemplate::new)
            .append(new KeyedCodec<>("UiFile", Codec.STRING), (s, v) -> s.uiFile = v, s -> s.uiFile).add()
            .append(new KeyedCodec<>("IsDoublePage", Codec.BOOLEAN), (s, v) -> s.isDoublePage = v, s -> s.isDoublePage).add()
            .append(new KeyedCodec<>("Pages", new ArrayCodec<>(CONTEXT_CODEC, PageContext[]::new)), (s, v) -> s.pages = v, s -> s.pages).add()
            .build();

    public static final BuilderCodec<habitatsInfo> HABITAT_INFO_CODEC = BuilderCodec.builder(habitatsInfo.class, habitatsInfo::new)
            .append(new KeyedCodec<>("ZoneName", Codec.STRING), (h, v) -> h.ZoneName = v, h -> h.ZoneName).add()
            .append(new KeyedCodec<>("Pages", new ArrayCodec<>(SPREAD_CODEC, SpreadTemplate[]::new)), (h, v) -> h.pages = v, h -> h.pages).add()
            .build();


    public static final AssetBuilderCodec<String, BookAssetData> CODEC = AssetBuilderCodec.builder(
                    BookAssetData.class,
                    BookAssetData::new,
                    Codec.STRING,
                    (t, id) -> t.id = id,
                    t -> t.id,
                    (t, data) -> t.data = data,
                    t -> t.data
            )
            .appendInherited(new KeyedCodec<>("Habitats", new ArrayCodec<>(HABITAT_INFO_CODEC, habitatsInfo[]::new)),
                    (t, v) -> t.habitats = v,
                    t -> t.habitats,
                    (t, p) -> t.habitats = p.habitats).add()
            .build();

    // Asset Store

    private static AssetStore<String, BookAssetData, DefaultAssetMap<String, BookAssetData>> ASSET_STORE;

    public static AssetStore<String, BookAssetData, DefaultAssetMap<String, BookAssetData>> getAssetStore() {
        if (ASSET_STORE == null) {
            ASSET_STORE = AssetRegistry.getAssetStore(BookAssetData.class);
        }
        return ASSET_STORE;
    }

    // Fields
    private String id;
    private AssetExtraInfo.Data data;
    private habitatsInfo[] habitats;

    public static class habitatsInfo {
        public String ZoneName;
        public SpreadTemplate[] pages;
    }

    public static class SpreadTemplate {
        public String uiFile;
        public boolean isDoublePage;
        public PageContext[] pages = new PageContext[0];
    }

    public static class ZoneInfo {
        public String zoneDescription;
        public String ZoneImage;
        public String ProgressBarImage;
        public String ProgressBarColour;
    }

    public static class PageContext {
        public String contextData;
        public ZoneInfo zoneInfo;
    }

    public BookAssetData() {
    }

    @Override
    public String getId() {
        return id;
    }

    public List<SpreadTemplate> getFlattenedPages() {
        if (habitats == null) return List.of();

        return Arrays.stream(habitats)
                .filter(habitat -> habitat.pages != null)
                .flatMap(habitat -> Arrays.stream(habitat.pages))
                .toList();
    }


    private Map<String, List<String>> habitatCache;

    public List<String> getFishByHabitat(String habitatName) {
        buildCache();
//        if (habitatCache == null) {
//            buildCache();
//        }
        AnglersAlmanac.getInstance().getLogger().atInfo().log(habitatCache.toString());
        return habitatCache.getOrDefault(habitatName.toLowerCase(), List.of());
    }

    private void buildCache() {
        habitatCache = Arrays.stream(habitats)
                .collect(Collectors.toMap(
                        h -> h.ZoneName.toLowerCase(),
                        h -> Arrays.stream(h.pages)
                                .flatMap(p -> Arrays.stream(p.pages))
                                .map(c -> c.contextData)
                                .filter(d -> d != null && !d.isEmpty())
                                .toList(),
                        (existing, replacement) -> existing
                ));
    }
}
