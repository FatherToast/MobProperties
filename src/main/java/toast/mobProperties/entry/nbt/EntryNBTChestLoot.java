package toast.mobProperties.entry.nbt;

import java.util.Random;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.WeightedRandomChestContent;
import net.minecraftforge.common.ChestGenHooks;
import toast.mobProperties.FileHelper;
import toast.mobProperties.IPropertyReader;
import toast.mobProperties.entry.EntryAbstract;
import toast.mobProperties.entry.NBTStatsInfo;

import com.google.gson.JsonObject;

public class EntryNBTChestLoot extends EntryAbstract {
    // The name of the chest loot list in GhestGenHooks.
    private final String lootList;
    // The size of the chest's inventory.
    private final int inventorySize;
    // The name to give the inventory list.
    private final String name;
    // The min and max number of items to generate if used. Otherwise, this is null.
    private final double[] counts;

    public EntryNBTChestLoot(String path, JsonObject root, int index, JsonObject node, IPropertyReader loader) {
        super(node, path);
        this.name = FileHelper.readText(node, path, "name", "Items");
        this.lootList = FileHelper.readText(node, path, "loot", "dungeonChest");
        this.inventorySize = FileHelper.readInteger(node, path, "inventory_size", 27);

        double[] countsTmp = FileHelper.readCounts(node, path, "count", -1.0, -1.0);
        if (countsTmp[0] < 0.0 || countsTmp[1] < 0.0) {
            this.counts = null;
        }
        else {
            this.counts = countsTmp;
        }
    }

    // Returns an array of required field names.
    @Override
    public String[] getRequiredFields() {
        return new String[] { };
    }

    // Returns an array of optional field names.
    @Override
    public String[] getOptionalFields() {
        return new String[] { "name", "loot", "inventory_size", "count" };
    }

    // Adds any NBT tags to the list.
    @Override
    public void addTags(NBTStatsInfo nbtStats) {
        InventoryChestLoot chestProxy = new InventoryChestLoot(this.inventorySize);
        ChestGenHooks info = ChestGenHooks.getInfo(this.lootList);
        Random rand = nbtStats.theEntity.getRNG();

        int count;
        if (this.counts == null) {
            count = info.getCount(rand);
        }
        else {
            count = FileHelper.getCount(this.counts);
        }

        WeightedRandomChestContent.generateChestContents(rand, info.getItems(rand), chestProxy, count);
        nbtStats.addTag(this.name, chestProxy.writeToNBT());
    }

    // A partly functional proxy inventory to generate chest loot in.
    private static class InventoryChestLoot implements IInventory {
        // The proxy's inventory.
        private ItemStack[] inventory;

        public InventoryChestLoot(int inventorySize) {
            this.inventory = new ItemStack[inventorySize];
        }

        // Returns this inventory proxy as an NBT tag list.
        public NBTTagList writeToNBT() {
            NBTTagList list = new NBTTagList();
            for (int i = 0; i < this.inventory.length; ++i) {
                if (this.inventory[i] != null) {
                    NBTTagCompound tag = new NBTTagCompound();
                    tag.setByte("Slot", (byte)i);
                    this.inventory[i].writeToNBT(tag);
                    list.appendTag(tag);
                }
            }
            return list;
        }

        @Override
        public int getSizeInventory() {
            return this.inventory.length;
        }

        @Override
        public ItemStack getStackInSlot(int slot) {
            return this.inventory[slot];
        }

        @Override
        public void setInventorySlotContents(int slot, ItemStack itemStack) {
            this.inventory[slot] = itemStack;
            if (itemStack != null && itemStack.stackSize > this.getInventoryStackLimit()) {
                itemStack.stackSize = this.getInventoryStackLimit();
            }
        }

        @Override
        public ItemStack decrStackSize(int slot, int count) {
            return null;
        }

        @Override
        public ItemStack getStackInSlotOnClosing(int slot) {
            return null;
        }

        @Override
        public String getInventoryName() {
            return "Chest";
        }

        @Override
        public boolean hasCustomInventoryName() {
            return false;
        }

        @Override
        public int getInventoryStackLimit() {
            return 64;
        }

        @Override
        public void markDirty() {
            // Do nothing
        }

        @Override
        public boolean isUseableByPlayer(EntityPlayer player) {
            return false;
        }

        @Override
        public void openInventory() {
            // Do nothing

        }

        @Override
        public void closeInventory() {
            // Do nothing
        }

        @Override
        public boolean isItemValidForSlot(int slot, ItemStack itemStack) {
            return true;
        }
    }
}