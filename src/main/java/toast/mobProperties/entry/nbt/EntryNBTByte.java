package toast.mobProperties.entry.nbt;

import net.minecraft.nbt.NBTTagByte;
import toast.mobProperties.entry.EntryAbstract;
import toast.mobProperties.entry.IPropertyReader;
import toast.mobProperties.event.NBTStatsInfo;
import toast.mobProperties.util.FileHelper;

import com.google.gson.JsonObject;

public class EntryNBTByte extends EntryAbstract {
    /// The name of this tag.
    private final String name;
    /// The value of this tag.
    private final double[] values;

    public EntryNBTByte(String path, JsonObject root, int index, JsonObject node, IPropertyReader loader, boolean isBoolean) {
        super(node, path);
        this.name = FileHelper.readText(node, path, "name", "");
        if (isBoolean) {
            this.values = FileHelper.readBoolean(node, path, "value", false) ? new double[] { 1.0, 1.0 } : new double[] { 0.0, 0.0 };
        }
        else {
            this.values = FileHelper.readCounts(node, path, "value", 0.0, 0.0);
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
        return new String[] { "name", "value" };
    }

    /// Adds any NBT tags to the list.
    @Override
    public void addTags(NBTStatsInfo nbtStats) {
        int value = FileHelper.getCount(this.values, nbtStats.random);
        nbtStats.addTag(this.name, new NBTTagByte((byte) value));
    }
}