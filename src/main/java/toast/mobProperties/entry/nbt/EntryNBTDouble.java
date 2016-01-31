package toast.mobProperties.entry.nbt;

import net.minecraft.nbt.NBTTagDouble;
import toast.mobProperties.FileHelper;
import toast.mobProperties.IPropertyReader;
import toast.mobProperties.entry.NBTStatsInfo;

import com.google.gson.JsonObject;

public class EntryNBTDouble extends EntryNBTNumber {
    public EntryNBTDouble(String path, JsonObject root, int index, JsonObject node, IPropertyReader loader) {
        super(path, root, index, node, loader);
    }

    /// Adds any NBT tags to the list.
    @Override
    public void addTags(NBTStatsInfo nbtStats) {
        double value = FileHelper.getValue(this.values, nbtStats.random);
        nbtStats.addTag(this.name, new NBTTagDouble(value));
    }
}