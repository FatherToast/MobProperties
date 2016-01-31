package toast.mobProperties.entry.nbt;

import net.minecraft.nbt.NBTTagShort;
import toast.mobProperties.FileHelper;
import toast.mobProperties.IPropertyReader;
import toast.mobProperties.entry.NBTStatsInfo;

import com.google.gson.JsonObject;

public class EntryNBTShort extends EntryNBTNumber {
    public EntryNBTShort(String path, JsonObject root, int index, JsonObject node, IPropertyReader loader) {
        super(path, root, index, node, loader);
    }

    /// Adds any NBT tags to the list.
    @Override
    public void addTags(NBTStatsInfo nbtStats) {
        int value = FileHelper.getCount(this.values, nbtStats.random);
        nbtStats.addTag(this.name, new NBTTagShort((short) value));
    }
}