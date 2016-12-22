package toast.mobProperties.entry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import toast.mobProperties.api.DropEntry;
import toast.mobProperties.api.IPropertyDrops;
import toast.mobProperties.event.ItemStatsInfo;
import toast.mobProperties.event.MobDropsInfo;
import toast.mobProperties.event.MobStatsInfo;
import toast.mobProperties.event.NBTStatsInfo;
import toast.mobProperties.util.FileHelper;
import toast.mobProperties.util.MobPropertyException;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class MobProperties implements IProperty {
    // Mapping of all loaded properties to the entity id.
    private static final HashMap<String, MobProperties> PROPERTIES_MAP = new HashMap<String, MobProperties>();

    // Returns the mob properties for the given entity/id.
    public static MobProperties getProperties(EntityLivingBase entity) {
        return MobProperties.getProperties(EntityList.getEntityString(entity));
    }

    public static MobProperties getProperties(String entityId) {
        return MobProperties.PROPERTIES_MAP.get(entityId);
    }

    // Unloads all properties.
    public static void unload() {
        MobProperties.PROPERTIES_MAP.clear();
    }

    // Turns a string of info into data. Crashes the game if something goes wrong.
    public static void load(String path, JsonObject node) {
        String id = null;
        try {
            id = node.get("_name").getAsString();
        }
        catch (Exception ex) {
            throw new MobPropertyException("Missing or invalid mob id!", path, ex);
        }
        if (id == null || id == "")
            throw new MobPropertyException("Mob id cannot be null or empty!", path);
        if (MobProperties.PROPERTIES_MAP.containsKey(id))
            throw new MobPropertyException("Duplicate mob file! (id: " + id + ")", path);

        JsonArray preStatNodes = node.getAsJsonArray("pre_stats");
        JsonArray statNodes = node.getAsJsonArray("stats");
        JsonArray dropNodes = node.getAsJsonArray("drops");

        MobProperties.PROPERTIES_MAP.put(id, new MobProperties(path, node, id, preStatNodes, statNodes, dropNodes));
    }

    // The entity id used for this category.
    private final String entityId;
    // Array of altered mob stats.
    private final IProperty preStats, stats;
    // Array of added drop contents.
    private final IProperty drops;

    private MobProperties(String path, JsonObject root, String id, JsonArray preStatNodes, JsonArray statNodes, JsonArray dropNodes) {
        FileHelper.verify(root, path, this);
        this.entityId = id;
        this.preStats = preStatNodes == null ? null : new MobStats(path + "\\pre_stats", root, preStatNodes);
        this.stats = statNodes == null ? null : new MobStats(path + "\\stats", root, statNodes);
        this.drops = dropNodes == null ? null : new MobDrops(path + "\\drops", root, dropNodes);
    }

    // Returns an array of required field names.
    @Override
    public String[] getRequiredFields() {
        return new String[] { "_name" };
    }

    // Returns an array of optional field names.
    @Override
    public String[] getOptionalFields() {
        return new String[] { "drops", "stats", "pre_stats" };
    }

    // Returns this property's Json string.
	@Override
    public String getJsonString() {
        throw new UnsupportedOperationException("Non-functions are not used as Json strings!");
    }

    // Called before normal initialization to prep stats.
    public void preInit(MobStatsInfo mobStats) {
        if (this.preStats != null) {
            this.preStats.init(mobStats);
        }
    }

    // Initializes the entity's stats.
    @Override
    public void init(MobStatsInfo mobStats) {
        if (this.stats != null) {
            this.stats.init(mobStats);
        }
    }

    // Modifies the item.
    @Override
    public void modifyItem(ItemStatsInfo itemStats) {
        throw new UnsupportedOperationException("Non-item properties can not modify items!");
    }

    // Adds any NBT tags to the list.
    @Override
    public void addTags(NBTStatsInfo nbtStats) {
        throw new UnsupportedOperationException("Non-nbt properties can not modify nbt!");
    }

    // Modifies the list of drops.
    @Override
    public void modifyDrops(MobDropsInfo mobDrops) {
        if (this.drops != null) {
            this.drops.modifyDrops(mobDrops);
        }
    }

    // Populates a list with all the mob drop modifications made by these properties.
    public void addDrops(List<DropEntry> dropsList) {
        if (this.drops instanceof IPropertyDrops) {
            ((IPropertyDrops) this.drops).addDrops(dropsList, new double[] { 1.0, 1.0 }, 1.0, new ArrayList<String>());
        }
    }

    // Returns the string entity id these properties apply to.
    public String getEntityId() {
        return this.entityId;
    }
}
