package toast.mobProperties.entry;

import java.util.List;

import net.minecraft.nbt.NBTTagList;
import toast.mobProperties.api.DropEntry;
import toast.mobProperties.api.DropsHelper;
import toast.mobProperties.api.IPropertyDrops;
import toast.mobProperties.entry.drops.EntryDropsAdd;
import toast.mobProperties.entry.drops.EntryDropsDefault;
import toast.mobProperties.entry.drops.EntryDropsRemove;
import toast.mobProperties.entry.drops.EntryDropsSchematic;
import toast.mobProperties.entry.drops.EntryDropsSetBlock;
import toast.mobProperties.entry.drops.EntryDropsSpawn;
import toast.mobProperties.entry.drops.EntryDropsXP;
import toast.mobProperties.event.ItemStatsInfo;
import toast.mobProperties.event.MobDropsInfo;
import toast.mobProperties.event.MobStatsInfo;
import toast.mobProperties.event.NBTStatsInfo;
import toast.mobProperties.util.FileHelper;
import toast.mobProperties.util.MobPropertyException;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class MobDrops implements IProperty, IPropertyReader, IPropertyDrops {
    // The min and max number of times to perform all tasks.
    private final double[] counts;
    // The entry objects included in this property.
    private final IProperty[] entries;

    public MobDrops(String entityName, NBTTagList dropData) {
        this.counts = new double[] { 1.0, 1.0 };
        int length = dropData.tagCount();
        this.entries = new IProperty[length];
        String line;
        JsonObject node;
        for (int i = 0; i < length; i++) {
            line = dropData.getStringTagAt(i);
            node = FileHelper.loadFunctionFromString(entityName, line, i);
            if (node != null) {
                this.entries[i] = this.readLine(entityName, node, i, node);
            }
        }
    }

    public MobDrops(String path, JsonObject root, JsonArray nodes) {
        this.counts = new double[] { 1.0, 1.0 };
        int length = nodes.size();
        this.entries = new IProperty[length];
        for (int i = 0; i < length; i++) {
            this.entries[i] = this.readLine(path, root, i, nodes.get(i));
        }
    }

    public MobDrops(String path, JsonObject root, int index, JsonObject node, IPropertyReader loader) {
        FileHelper.verify(node, path, this);
        this.counts = FileHelper.readCounts(node, path, "count", 1.0, 1.0);

        JsonArray nodes = node.getAsJsonArray("functions");
        if (nodes == null)
            throw new MobPropertyException("Missing or invalid functions!", path);

        path += "\\functions";
        int length = nodes.size();
        this.entries = new IProperty[length];
        for (int i = 0; i < length; i++) {
            this.entries[i] = this.readLine(path, root, i, nodes.get(i));
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

    // Returns this property's Json string.
	@Override
    public String getJsonString() {
        throw new UnsupportedOperationException("Non-functions are not used as Json strings!");
    }

    // Initializes the entity's stats.
    @Override
    public void init(MobStatsInfo mobStats) {
        for (int count = FileHelper.getCount(this.counts); count-- > 0;) {
            for (IProperty entry : this.entries) {
                if (entry != null) {
					mobStats.addDrop(entry);
                }
            }
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
        for (IProperty entry : this.entries) {
            if (entry != null) {
                entry.modifyDrops(mobDrops);
            }
        }
    }

    // Loads a line as a mob property.
    @Override
    public IProperty readLine(String path, JsonObject root, int index, JsonElement node) {
        path += "\\entry_" + (index + 1);
        if (!node.isJsonObject())
            throw new MobPropertyException("Invalid node (object expected)!", path);
        JsonObject objNode = node.getAsJsonObject();
        String function = null;
        try {
            function = objNode.get("function").getAsString();
        }
        catch (NullPointerException ex) {
            // Do nothing
        }
        catch (IllegalArgumentException ex) {
            // Do nothing
        }
        if (function == null)
            throw new MobPropertyException("Missing function name!", path);
        path += "(" + function + ")";

        if (function.equals("all"))
            return new PropertyGroup(path, root, index, objNode, this);
        if (function.equals("choose"))
            return new PropertyChoose(path, root, index, objNode, this);
        if (function.equals("external"))
            return new PropertyExternal(path, root, index, objNode, this);
        if (function.equals("command"))
            return new EntryCommand(path, root, index, objNode, this);

        if (function.equals("default"))
            return new EntryDropsDefault(path, root, index, objNode, this);
        if (function.equals("add"))
            return new EntryDropsAdd(path, root, index, objNode, this);
        if (function.equals("remove"))
            return new EntryDropsRemove(path, root, index, objNode, this);
        if (function.equals("spawn"))
            return new EntryDropsSpawn(path, root, index, objNode, this);
        if (function.equals("xp"))
            return new EntryDropsXP(path, root, index, objNode, this);
        if (function.equals("set_block"))
            return new EntryDropsSetBlock(path, root, index, objNode, this);
        if (function.equals("schematic"))
            return new EntryDropsSchematic(path, root, index, objNode, this);

        boolean inverted = false;
        if (function.startsWith(Character.toString(FileHelper.CHAR_INVERT))) {
            inverted = true;
            function = function.substring(1);
        }
        if (function.startsWith("if_"))
            return new PropertyGroupConditional(path, root, index, objNode, this, function.substring(3), inverted);

        throw new MobPropertyException("Invalid function name!", path);
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
