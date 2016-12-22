package toast.mobProperties.entry.nbt;

import com.google.gson.JsonObject;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.nbt.NBTTagShort;
import toast.mobProperties.entry.EntryAbstract;
import toast.mobProperties.entry.IPropertyReader;
import toast.mobProperties.event.NBTStatsInfo;
import toast.mobProperties.util.FileHelper;

public class EntryNBTEnchantId extends EntryAbstract {
    /// The name of this tag.
    protected final String name;
    /// The value of this tag.
    protected final short value;

    public EntryNBTEnchantId(String path, JsonObject root, int index, JsonObject node, IPropertyReader loader) {
        super(node, path);
        this.name = FileHelper.readText(node, path, "name", "id");
        this.value = (short) Enchantment.getEnchantmentID(FileHelper.readEnchant(node, path, "value"));
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
        nbtStats.addTag(this.name, new NBTTagShort(this.value));
    }
}