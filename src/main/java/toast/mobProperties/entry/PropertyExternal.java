package toast.mobProperties.entry;

import java.util.HashMap;
import java.util.List;

import toast.mobProperties.FileHelper;
import toast.mobProperties.IPropertyReader;
import toast.mobProperties.ItemStats;
import toast.mobProperties.MobDrops;
import toast.mobProperties.MobPropertyException;
import toast.mobProperties.MobStats;
import toast.mobProperties.NBTStats;
import toast.mobProperties.api.DropEntry;
import toast.mobProperties.api.DropsHelper;
import toast.mobProperties.api.IPropertyDrops;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class PropertyExternal extends EntryAbstract implements IPropertyDrops {
    // Mapping of all loaded external functions to their file name.
    private static final HashMap<String, MobDrops> DROPS_MAP = new HashMap<String, MobDrops>();
    private static final HashMap<String, MobStats> STATS_MAP = new HashMap<String, MobStats>();
    private static final HashMap<String, ItemStats> ITEMS_MAP = new HashMap<String, ItemStats>();
    private static final HashMap<String, NBTStats> NBT_MAP = new HashMap<String, NBTStats>();

    // Unloads all properties.
    public static void unload() {
        PropertyExternal.DROPS_MAP.clear();
        PropertyExternal.STATS_MAP.clear();
        PropertyExternal.ITEMS_MAP.clear();
        PropertyExternal.NBT_MAP.clear();
    }

    // Loads a single external function from Json.
    public static void load(String type, String path, String fileName, JsonObject node) {
        String name = fileName.substring(0, fileName.length() - FileHelper.FILE_EXT.length());
        if (type.equals("drops")) {
            PropertyExternal.loadDrop(path, name, node);
        }
        else if (type.equals("stats")) {
            PropertyExternal.loadStat(path, name, node);
        }
        else if (type.equals("items")) {
            PropertyExternal.loadItem(path, name, node);
        }
        else if (type.equals("nbt")) {
            PropertyExternal.loadNbt(path, name, node);
        }
    }
    private static void loadDrop(String path, String name, JsonObject node) {
        if (PropertyExternal.DROPS_MAP.containsKey(name))
            throw new MobPropertyException("Duplicate external drops property! (name: " + name + ")", path);

        JsonObject dummyRoot = new JsonObject();
        JsonArray dummyArray = new JsonArray();
        dummyArray.add(node);
        dummyRoot.add("drops", dummyArray);
        PropertyExternal.DROPS_MAP.put(name, new MobDrops(path, dummyRoot, dummyArray));
    }
    private static void loadStat(String path, String name, JsonObject node) {
        if (PropertyExternal.STATS_MAP.containsKey(name))
            throw new MobPropertyException("Duplicate external stats property! (name: " + name + ")", path);

        JsonObject dummyRoot = new JsonObject();
        JsonArray dummyArray = new JsonArray();
        dummyArray.add(node);
        dummyRoot.add("stats", dummyArray);
        PropertyExternal.STATS_MAP.put(name, new MobStats(path, dummyRoot, dummyArray));
    }
    private static void loadItem(String path, String name, JsonObject node) {
        if (PropertyExternal.ITEMS_MAP.containsKey(name))
            throw new MobPropertyException("Duplicate external item stats property! (name: " + name + ")", path);

        JsonObject dummyRoot = new JsonObject();
        JsonArray dummyArray = new JsonArray();
        dummyArray.add(node);
        dummyRoot.add("item_stats", dummyArray);
        PropertyExternal.ITEMS_MAP.put(name, new ItemStats(path, dummyRoot, 0, dummyRoot, null));
    }
    private static void loadNbt(String path, String name, JsonObject node) {
        if (PropertyExternal.NBT_MAP.containsKey(name))
            throw new MobPropertyException("Duplicate external nbt stats property! (name: " + name + ")", path);

        JsonObject dummyRoot = new JsonObject();
        JsonArray dummyArray = new JsonArray();
        dummyArray.add(node);
        dummyRoot.add("tags", dummyArray);
        PropertyExternal.NBT_MAP.put(name, new NBTStats(path, dummyRoot, 0, dummyRoot, null));
    }

    // The min and max number of times to perform the task.
    private final double[] counts;
    // The name of the external function to use.
    private final String externalFunction;

    public PropertyExternal(String path, JsonObject root, int index, JsonObject node, IPropertyReader loader) {
        super(node, path);
        this.counts = FileHelper.readCounts(node, path, "count", 1.0, 1.0);

        this.externalFunction = FileHelper.readText(node, path, "file", "");
        if (this.externalFunction == "")
            throw new MobPropertyException("Missing or invalid external file name!", path);
    }

    // Returns an array of required field names.
    @Override
    public String[] getRequiredFields() {
        return new String[] { "file" };
    }

    // Returns an array of optional field names.
    @Override
    public String[] getOptionalFields() {
        return new String[] { "count" };
    }

    // Initializes the entity's stats.
    @Override
    public void init(MobStatsInfo mobStats) {
        MobStats stats = PropertyExternal.STATS_MAP.get(this.externalFunction);
        if (stats != null) {
            for (int count = FileHelper.getCount(this.counts); count-- > 0;) {
                stats.init(mobStats);
            }
        }
        else
			throw new RuntimeException("[ERROR] External stats function \"" + this.externalFunction + ".json\" not found!");
    }

    // Modifies the item.
    @Override
    public void modifyItem(ItemStatsInfo itemStats) {
        ItemStats stats = PropertyExternal.ITEMS_MAP.get(this.externalFunction);
        if (stats != null) {
            for (int count = FileHelper.getCount(this.counts); count-- > 0;) {
                stats.generate(itemStats);
            }
        }
        else
			throw new RuntimeException("[ERROR] External item stats function \"" + this.externalFunction + ".json\" not found!");
    }

    // Adds any NBT tags to the list.
    @Override
    public void addTags(NBTStatsInfo nbtStats) {
        NBTStats stats = PropertyExternal.NBT_MAP.get(this.externalFunction);
        if (stats != null) {
            for (int count = FileHelper.getCount(this.counts); count-- > 0;) {
                stats.generate(nbtStats);
            }
        }
        else
			throw new RuntimeException("[ERROR] External nbt function \"" + this.externalFunction + ".json\" not found!");
    }

    // Modifies the list of drops.
    @Override
    public void modifyDrops(MobDropsInfo mobDrops) {
        MobDrops drops = PropertyExternal.DROPS_MAP.get(this.externalFunction);
        if (drops != null) {
            for (int count = FileHelper.getCount(this.counts); count-- > 0;) {
                drops.modifyDrops(mobDrops);
            }
        }
        else
			throw new RuntimeException("[ERROR] External drops function \"" + this.externalFunction + ".json\" not found!");
    }

    /*
     * @see toast.mobProperties.api.IPropertyDrops#addDrops(java.util.List, double[], double, java.util.List)
     */
    @Override
    public void addDrops(List<DropEntry> dropsList, double[] attempts, double chance, List<String> conditions) {
        chance = DropsHelper.adjustAttemptsAndChance(this.counts, attempts, chance);
        if (chance > 0.0) {
            MobDrops drops = PropertyExternal.DROPS_MAP.get(this.externalFunction);
            if (drops != null) {
                drops.addDrops(dropsList, DropsHelper.copy(attempts), chance, DropsHelper.copy(conditions));
            }
        }
    }
}
