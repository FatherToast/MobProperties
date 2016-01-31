package toast.mobProperties.entry;

import java.util.List;

import toast.mobProperties.FileHelper;
import toast.mobProperties.IProperty;
import toast.mobProperties.IPropertyReader;
import toast.mobProperties.MobPropertyException;
import toast.mobProperties._MobPropertiesMod;
import toast.mobProperties.api.DropEntry;
import toast.mobProperties.api.DropsHelper;
import toast.mobProperties.api.IPropertyDrops;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class PropertyChoose extends EntryAbstract implements IPropertyDrops {
    /// The min and max number of times to choose an object.
    private final double[] counts;
    /// The entry objects included in this property.
    private final IProperty[] entries;
    /// The individual weight for each object.
    private final int[] weights;
    /// The total weight of all objects.
    private final int totalWeight;

    public PropertyChoose(String path, JsonObject root, int index, JsonObject node, IPropertyReader loader) {
        super(node, path);
        this.counts = FileHelper.readCounts(node, path, "count", 1.0, 1.0);

        JsonArray nodes = node.getAsJsonArray("functions");
        if (nodes == null)
            throw new MobPropertyException("Missing or invalid functions!", path);

        path += "\\functions";
        String subpath;
        int length = nodes.size();
        this.entries = new IProperty[length];
        this.weights = new int[length];
        JsonElement subnode;
        for (int i = 0; i < length; i++) {
            subnode = nodes.get(i);
            subpath = path + "\\entry_" + (i + 1);
            if (!subnode.isJsonObject())
                throw new MobPropertyException("Invalid node (object expected)!", subpath);
            this.weights[i] = FileHelper.readWeight(subnode.getAsJsonObject(), subpath, 1);
            if (this.weights[i] <= 0)
                throw new MobPropertyException("Invalid property weight! (" + this.weights[0] + ": must be a positive integer)", path);
            if (subnode.getAsJsonObject().has("function")) {
                this.entries[i] = loader.readLine(path, root, i, subnode);
            }
            else {
                this.entries[i] = null;
            }
        }

        int weight = 0;
        for (int i = length; i-- > 0;) {
            weight += this.weights[i];
        }
        this.totalWeight = weight;
    }

    /// Returns an array of required field names.
    @Override
    public String[] getRequiredFields() {
        return new String[] { "functions" };
    }

    /// Returns an array of optional field names.
    @Override
    public String[] getOptionalFields() {
        return new String[] { "count" };
    }

    /// Initializes the entity's stats.
    @Override
    public void init(MobStatsInfo mobStats) {
        if (this.totalWeight <= 0)
            return;
        choose: for (int count = FileHelper.getCount(this.counts); count-- > 0;) {
            int choice = mobStats.theEntity.getRNG().nextInt(this.totalWeight);
            for (int i = this.weights.length; i-- > 0;) {
                if ( (choice -= this.weights[i]) < 0) {
                    if (this.entries[i] != null) {
                        this.entries[i].init(mobStats);
                    }
                    continue choose;
                }
            }
            _MobPropertiesMod.debugException("Error choosing weighted item! " + choice + "/" + this.totalWeight);
        }
    }

    /// Modifies the item.
    @Override
    public void modifyItem(ItemStatsInfo itemStats) {
        if (this.totalWeight <= 0)
            return;
        choose: for (int count = FileHelper.getCount(this.counts); count-- > 0;) {
            int choice = itemStats.theEntity.getRNG().nextInt(this.totalWeight);
            for (int i = this.weights.length; i-- > 0;) {
                if ( (choice -= this.weights[i]) < 0) {
                    if (this.entries[i] != null) {
                        this.entries[i].modifyItem(itemStats);
                    }
                    continue choose;
                }
            }
            _MobPropertiesMod.debugException("Error choosing weighted item! " + choice + "/" + this.totalWeight);
        }
    }

    /// Adds any NBT tags to the list.
    @Override
    public void addTags(NBTStatsInfo nbtStats) {
        if (this.totalWeight <= 0)
            return;
        choose: for (int count = FileHelper.getCount(this.counts); count-- > 0;) {
            int choice = nbtStats.theEntity.getRNG().nextInt(this.totalWeight);
            for (int i = this.weights.length; i-- > 0;) {
                if ( (choice -= this.weights[i]) < 0) {
                    if (this.entries[i] != null) {
                        this.entries[i].addTags(nbtStats);
                    }
                    continue choose;
                }
            }
            _MobPropertiesMod.debugException("Error choosing weighted item! " + choice + "/" + this.totalWeight);
        }
    }

    /// Modifies the list of drops.
    @Override
    public void modifyDrops(MobDropsInfo mobDrops) {
        if (this.totalWeight <= 0)
            return;
        choose: for (int count = FileHelper.getCount(this.counts); count-- > 0;) {
            int choice = mobDrops.theEntity.getRNG().nextInt(this.totalWeight);
            for (int i = this.weights.length; i-- > 0;) {
                if ( (choice -= this.weights[i]) < 0) {
                    if (this.entries[i] != null) {
                        this.entries[i].modifyDrops(mobDrops);
                    }
                    continue choose;
                }
            }
            _MobPropertiesMod.debugException("Error choosing weighted item! " + choice + "/" + this.totalWeight);
        }
    }

    /*
     * @see toast.mobProperties.api.IPropertyDrops#addDrops(java.util.List, double[], double, java.util.List)
     */
    @Override
    public void addDrops(List<DropEntry> dropsList, double[] attempts, double chance, List<String> conditions) {
        if (this.totalWeight <= 0)
            return;
        chance = DropsHelper.adjustAttemptsAndChance(this.counts, attempts, chance) / this.totalWeight; // Adjust for the total weight
        if (chance > 0.0) {
            for (int i = 0; i < this.entries.length; i++) {
                if (this.entries[i] instanceof IPropertyDrops) {
                    // Adjust for the item's chance to be chosen
                    ((IPropertyDrops) this.entries[i]).addDrops(dropsList, DropsHelper.copy(attempts), chance * this.weights[i], DropsHelper.copy(conditions));
                }
            }
        }
    }
}
