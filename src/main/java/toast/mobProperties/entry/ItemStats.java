package toast.mobProperties.entry;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import toast.mobProperties.entry.item.EntryItemColor;
import toast.mobProperties.entry.item.EntryItemEnchant;
import toast.mobProperties.entry.item.EntryItemLore;
import toast.mobProperties.entry.stats.EntryStatsModifier;
import toast.mobProperties.entry.stats.EntryStatsNBT;
import toast.mobProperties.entry.stats.EntryStatsName;
import toast.mobProperties.entry.stats.EntryStatsPotion;
import toast.mobProperties.event.ItemStatsInfo;
import toast.mobProperties.util.FileHelper;
import toast.mobProperties.util.MobPropertyException;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class ItemStats implements IPropertyReader {
    // The property reader this is a part of.
    public final IPropertyReader parent;
    // The entry objects included in this property.
    public final IProperty[] entries;

    public ItemStats(String path, JsonObject root, int index, JsonObject node, IPropertyReader loader) {
        this.parent = loader;
        JsonArray nodes = node.getAsJsonArray("item_stats");
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

    // Generates an appropriate item stack with a stack size of 1.
    public ItemStack generate(EntityLivingBase entity, Item item, int damage, Object mobInfo) {
        ItemStack itemStack = new ItemStack(item, 1, damage);
        ItemStatsInfo info = new ItemStatsInfo(itemStack, entity, mobInfo);
        for (IProperty entry : this.entries)
            if (entry != null) {
                entry.modifyItem(info);
            }
        return itemStack;
    }
    public void generate(ItemStatsInfo info) {
        for (IProperty entry : this.entries) {
            if (entry != null) {
                entry.modifyItem(info);
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

        if (function.equals("name"))
            return new EntryStatsName(path, root, index, objNode, this);
        if (function.equals("modifier"))
            return new EntryStatsModifier(path, root, index, objNode, this);
        if (function.equals("potion"))
            return new EntryStatsPotion(path, root, index, objNode, this);
        if (function.equals("nbt"))
            return new EntryStatsNBT(path, root, index, objNode, this);

        if (function.equals("enchant"))
            return new EntryItemEnchant(path, root, index, objNode, this);
        if (function.equals("lore"))
            return new EntryItemLore(path, root, index, objNode, this);
        if (function.equals("color"))
            return new EntryItemColor(path, root, index, objNode, this);

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
