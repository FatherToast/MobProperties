package toast.mobProperties.entry.nbt;

import java.util.Random;

import com.google.gson.JsonObject;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.WorldServer;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.LootTable;
import net.minecraft.world.storage.loot.LootTableList;
import toast.mobProperties.ModMobProperties;
import toast.mobProperties.entry.EntryAbstract;
import toast.mobProperties.entry.IPropertyReader;
import toast.mobProperties.event.NBTStatsInfo;
import toast.mobProperties.util.FileHelper;

public class EntryNBTChestLoot extends EntryAbstract {
    // The name of the chest loot list.
	private final ResourceLocation loot;
    // The size of the chest's inventory.
    private final int inventorySize;
    // The name to give the inventory list.
    private final String name;

    public EntryNBTChestLoot(String path, JsonObject root, int index, JsonObject node, IPropertyReader loader) {
        super(node, path);
        this.name = FileHelper.readText(node, path, "name", "Items");
        this.loot = new ResourceLocation(FileHelper.readText(node, path, "loot", LootTableList.CHESTS_SIMPLE_DUNGEON.toString()));
        this.inventorySize = FileHelper.readInteger(node, path, "inventory_size", 27);

        if (node.has("count")) {
            ModMobProperties.logWarning("Chest loot \"count\" is now determined in the loot table (field is ignored) at " + path);
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
    	if (nbtStats.theEntity.worldObj instanceof WorldServer) {
    		WorldServer world = (WorldServer) nbtStats.theEntity.worldObj;
	        Random random = nbtStats.theEntity.getRNG();
	        InventoryNBTChestLoot chestProxy = new InventoryNBTChestLoot(this.inventorySize);

	        LootTable lootTable = nbtStats.theEntity.worldObj.getLootTableManager().getLootTableFromLocation(this.loot);

	        LootContext.Builder lootBuilder = new LootContext.Builder(world);

	        lootTable.fillInventory(chestProxy, random, lootBuilder.build());
	        nbtStats.addTag(this.name, chestProxy.writeToNBT());
    	}
    }

    // A partly functional proxy inventory to generate chest loot in.
    private static class InventoryNBTChestLoot implements IInventory {
        // The proxy's inventory.
        private ItemStack[] inventory;

        public InventoryNBTChestLoot(int inventorySize) {
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
		public String getName() {
			return "Chest";
		}
		@Override
		public ITextComponent getDisplayName() {
			return new TextComponentString(this.getName());
		}
        @Override
        public int getInventoryStackLimit() {
            return 64;
        }

        @Override
        public ItemStack decrStackSize(int slot, int count) { return null; }
		@Override
		public void clear() { }
        @Override
        public void markDirty() { }
        @Override
        public boolean isUseableByPlayer(EntityPlayer player) { return false; }
        @Override
        public boolean isItemValidForSlot(int slot, ItemStack itemStack) { return true; }
		@Override
		public boolean hasCustomName() { return false; }
		@Override
		public ItemStack removeStackFromSlot(int index) { return null; }
		@Override
		public void openInventory(EntityPlayer player) { }
		@Override
		public void closeInventory(EntityPlayer player) { }
		@Override
		public int getField(int id) { return 0; }
		@Override
		public void setField(int id, int value) { }
		@Override
		public int getFieldCount() { return 0; }
    }
}