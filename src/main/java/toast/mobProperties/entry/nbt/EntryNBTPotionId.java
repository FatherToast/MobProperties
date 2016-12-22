package toast.mobProperties.entry.nbt;

import net.minecraft.nbt.NBTTagByte;
import net.minecraft.potion.Potion;
import toast.mobProperties.entry.EntryAbstract;
import toast.mobProperties.entry.IPropertyReader;
import toast.mobProperties.event.NBTStatsInfo;
import toast.mobProperties.util.FileHelper;

import com.google.gson.JsonObject;

public class EntryNBTPotionId extends EntryAbstract {
    /// The name of this tag.
    protected final String name;
    /// The value of this tag.
    protected final byte value;

    public EntryNBTPotionId(String path, JsonObject root, int index, JsonObject node, IPropertyReader loader) {
        super(node, path);
        this.name = FileHelper.readText(node, path, "name", "Id");
        this.value = (byte) Potion.getIdFromPotion(FileHelper.readPotion(node, path, "value"));
    }

    /// Returns an array of required field names.
    @Override
    public String[] getRequiredFields() {
        return new String[] { "value" };
    }

    /// Returns an array of optional field names.
    @Override
    public String[] getOptionalFields() {
        return new String[] { "name" };
    }

    /// Adds any NBT tags to the list.
    @Override
    public void addTags(NBTStatsInfo nbtStats) {
        nbtStats.addTag(this.name, new NBTTagByte(this.value));
    }
}