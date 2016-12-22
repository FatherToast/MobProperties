package toast.mobProperties.entry;

import net.minecraft.nbt.NBTTagList;
import toast.mobProperties.entry.stats.EntryStatsEquip;
import toast.mobProperties.entry.stats.EntryStatsModifier;
import toast.mobProperties.entry.stats.EntryStatsNBT;
import toast.mobProperties.entry.stats.EntryStatsName;
import toast.mobProperties.entry.stats.EntryStatsPotion;
import toast.mobProperties.entry.stats.EntryStatsRiding;
import toast.mobProperties.event.ItemStatsInfo;
import toast.mobProperties.event.MobDropsInfo;
import toast.mobProperties.event.MobStatsInfo;
import toast.mobProperties.event.NBTStatsInfo;
import toast.mobProperties.util.FileHelper;
import toast.mobProperties.util.MobPropertyException;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class MobStats implements IProperty, IPropertyReader {
    /// The entry objects included in this property.
    public final IProperty[] entries;

    public MobStats(String entityName, NBTTagList statData) {
        int length = statData.tagCount();
        this.entries = new IProperty[length];
        String line;
        JsonObject node;
        for (int i = 0; i < length; i++) {
            line = statData.getStringTagAt(i);
            node = FileHelper.loadFunctionFromString(entityName, line, i);
            if (node != null) {
                this.entries[i] = this.readLine(entityName, node, i, node);
            }
        }
    }

    public MobStats(String path, JsonObject root, JsonArray nodes) {
        int length = nodes.size();
        this.entries = new IProperty[length];
        for (int i = 0; i < length; i++) {
            this.entries[i] = this.readLine(path, root, i, nodes.get(i));
        }
    }

    public MobStats(String path, JsonObject root, int index, JsonObject node, IPropertyReader loader) {
        JsonArray nodes;
        try {
            nodes = node.getAsJsonArray("stats");
        }
        catch (IllegalArgumentException ex) {
            nodes = null;
        }
        if (nodes == null) {
            this.entries = new IProperty[0];
        }
        else {
            int length = nodes.size();
            this.entries = new IProperty[length];
            for (int i = 0; i < length; i++) {
                this.entries[i] = this.readLine(path, root, i, nodes.get(i));
            }
        }
    }

    /// Returns an array of required field names.
    @Override
    public String[] getRequiredFields() {
        return new String[] { };
    }

    /// Returns an array of optional field names.
    @Override
    public String[] getOptionalFields() {
        return new String[] { };
    }

    // Returns this property's Json string.
	@Override
    public String getJsonString() {
        throw new UnsupportedOperationException("Non-functions are not used as Json strings!");
    }

    /// Initializes the entity's stats.
    @Override
    public void init(MobStatsInfo mobStats) {
        for (IProperty entry : this.entries) {
            if (entry != null) {
                entry.init(mobStats);
            }
        }
    }

    /// Modifies the item.
    @Override
    public void modifyItem(ItemStatsInfo itemStats) {
        throw new UnsupportedOperationException("Non-item properties can not modify items!");
    }

    /// Adds any NBT tags to the list.
    @Override
    public void addTags(NBTStatsInfo nbtStats) {
        throw new UnsupportedOperationException("Non-nbt properties can not modify nbt!");
    }

    /// Modifies the list of drops.
    @Override
    public void modifyDrops(MobDropsInfo mobDrops) {
        throw new UnsupportedOperationException("Stats properties can not modify drops!");
    }

    /// Loads a line as a mob property.
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

        if (function.equals("drops"))
            return new MobDrops(path, root, index, objNode, this);
        if (function.equals("all"))
            return new PropertyGroup(path, root, index, objNode, this);
        if (function.equals("choose"))
            return new PropertyChoose(path, root, index, objNode, this);
        if (function.equals("external"))
            return new PropertyExternal(path, root, index, objNode, this);
        if (function.equals("command"))
            return new EntryCommand(path, root, index, objNode, this);

        if (function.equals("name"))
            return new EntryStatsName(path, root, index, objNode, this);
        if (function.equals("modifier"))
            return new EntryStatsModifier(path, root, index, objNode, this);
        if (function.equals("equip"))
            return new EntryStatsEquip(path, root, index, objNode, this);
        if (function.equals("potion"))
            return new EntryStatsPotion(path, root, index, objNode, this);
        if (function.equals("riding"))
            return new EntryStatsRiding(path, root, index, objNode, this, true);
        if (function.equals("ridden_by"))
            return new EntryStatsRiding(path, root, index, objNode, this, false);
        if (function.equals("nbt"))
            return new EntryStatsNBT(path, root, index, objNode, this);

        boolean inverted = false;
        if (function.startsWith(Character.toString(FileHelper.CHAR_INVERT))) {
            inverted = true;
            function = function.substring(1);
        }
        if (function.startsWith("if_"))
            return new PropertyGroupConditional(path, root, index, objNode, this, function.substring(3), inverted);

        throw new MobPropertyException("Invalid function name!", path);
    }
}
