package toast.mobProperties.entry;

import java.util.List;

import toast.mobProperties.api.DropEntry;
import toast.mobProperties.api.DropsHelper;
import toast.mobProperties.api.IPropertyDrops;
import toast.mobProperties.event.ItemStatsInfo;
import toast.mobProperties.event.MobDropsInfo;
import toast.mobProperties.event.MobStatsInfo;
import toast.mobProperties.event.NBTStatsInfo;
import toast.mobProperties.util.FileHelper;
import toast.mobProperties.util.MobPropertyException;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class PropertyGroup extends EntryAbstract implements IPropertyDrops {
    // The min and max number of times to perform all tasks.
    private final double[] counts;
    // The entry objects included in this property.
    private final IProperty[] entries;

    public PropertyGroup(String path, JsonObject root, int index, JsonObject node, IPropertyReader loader) {
        super(node, path);
        this.counts = FileHelper.readCounts(node, path, "count", 1.0, 1.0);

        JsonArray nodes = node.getAsJsonArray("functions");
        if (nodes == null)
            throw new MobPropertyException("Missing or invalid functions!", path);

        path += "\\functions";
        int length = nodes.size();
        this.entries = new IProperty[length];
        for (int i = 0; i < length; i++) {
            this.entries[i] = loader.readLine(path, root, i, nodes.get(i));
        }
    }

    // Returns an array of required field names.
    @Override
    public String[] getRequiredFields() {
        return new String[] { "functions" };
    }

    // Returns an array of optional field names.
    @Override
    public String[] getOptionalFields() {
        return new String[] { "count" };
    }

    // Initializes the entity's stats.
    @Override
    public void init(MobStatsInfo mobStats) {
        for (int count = FileHelper.getCount(this.counts); count-- > 0;) {
            for (IProperty entry : this.entries) {
                if (entry != null) {
                    entry.init(mobStats);
                }
            }
        }
    }

    // Modifies the item.
    @Override
    public void modifyItem(ItemStatsInfo itemStats) {
        for (int count = FileHelper.getCount(this.counts); count-- > 0;) {
            for (IProperty entry : this.entries) {
                if (entry != null) {
                    entry.modifyItem(itemStats);
                }
            }
        }
    }

    // Adds any NBT tags to the list.
    @Override
    public void addTags(NBTStatsInfo nbtStats) {
        for (int count = FileHelper.getCount(this.counts); count-- > 0;) {
            for (IProperty entry : this.entries) {
                if (entry != null) {
                    entry.addTags(nbtStats);
                }
            }
        }
    }

    // Modifies the list of drops.
    @Override
    public void modifyDrops(MobDropsInfo mobDrops) {
        for (int count = FileHelper.getCount(this.counts); count-- > 0;) {
            for (IProperty entry : this.entries) {
                if (entry != null) {
                    entry.modifyDrops(mobDrops);
                }
            }
        }
    }

    /*
     * @see toast.mobProperties.api.IPropertyDrops#addDrops(java.util.List, double[], double, java.util.List)
     */
    @Override
    public void addDrops(List<DropEntry> dropsList, double[] attempts, double chance, List<String> conditions) {
        chance = DropsHelper.adjustAttemptsAndChance(this.counts, attempts, chance);
        if (chance > 0.0) {
            for (IProperty entry : this.entries) {
                if (entry instanceof IPropertyDrops) {
                    ((IPropertyDrops) entry).addDrops(dropsList, DropsHelper.copy(attempts), chance, DropsHelper.copy(conditions));
                }
            }
        }
    }
}
