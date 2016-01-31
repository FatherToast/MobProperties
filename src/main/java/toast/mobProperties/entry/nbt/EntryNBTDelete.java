package toast.mobProperties.entry.nbt;

import toast.mobProperties.FileHelper;
import toast.mobProperties.IPropertyReader;
import toast.mobProperties.entry.EntryAbstract;
import toast.mobProperties.entry.NBTStatsInfo;

import com.google.gson.JsonObject;

public class EntryNBTDelete extends EntryAbstract {
    /// The name of the tag to delete.
    private final String name;

    public EntryNBTDelete(String path, JsonObject root, int index, JsonObject node, IPropertyReader loader) {
        super(node, path);
        this.name = FileHelper.readText(node, path, "name", "");
    }

    /// Returns an array of required field names.
    @Override
    public String[] getRequiredFields() {
        return new String[] { };
    }

    /// Returns an array of optional field names.
    @Override
    public String[] getOptionalFields() {
        return new String[] { "name" };
    }

    /// Adds any NBT tags to the list.
    @Override
    public void addTags(NBTStatsInfo nbtStats) {
        nbtStats.addTag(this.name, null);
    }
}