package toast.mobProperties.entry.nbt;

import net.minecraft.nbt.NBTTagCompound;
import toast.mobProperties.FileHelper;
import toast.mobProperties.IPropertyReader;
import toast.mobProperties.NBTStats;
import toast.mobProperties.entry.EntryAbstract;
import toast.mobProperties.entry.NBTStatsInfo;

import com.google.gson.JsonObject;

public class EntryNBTCompound extends EntryAbstract {
    // The name of this tag.
    private final String name;
    // The entry objects included in this property.
    private final NBTStats nbtStatsObj;

    public EntryNBTCompound(String path, JsonObject root, int index, JsonObject node, IPropertyReader loader) {
        super(node, path);
        this.name = FileHelper.readText(node, path, "name", "");
        this.nbtStatsObj = new NBTStats(path, root, index, node, loader);
    }

    // Returns an array of required field names.
    @Override
    public String[] getRequiredFields() {
        return new String[] { };
    }

    // Returns an array of optional field names.
    @Override
    public String[] getOptionalFields() {
        return new String[] { "name", "tags" };
    }

    // Adds any NBT tags to the list.
    @Override
    public void addTags(NBTStatsInfo nbtStats) {
        NBTTagCompound tag = new NBTTagCompound();
        this.nbtStatsObj.generate(nbtStats.theEntity, nbtStats.theItem, tag, nbtStats);
        nbtStats.addTag(this.name, tag);
    }
}
