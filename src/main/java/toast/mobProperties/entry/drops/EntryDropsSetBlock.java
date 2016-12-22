package toast.mobProperties.entry.drops;

import com.google.gson.JsonObject;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import toast.mobProperties.entry.EntryAbstract;
import toast.mobProperties.entry.IPropertyReader;
import toast.mobProperties.entry.NBTStats;
import toast.mobProperties.event.MobDropsInfo;
import toast.mobProperties.util.FileHelper;
import toast.mobProperties.util.MobPropertyException;

public class EntryDropsSetBlock extends EntryAbstract {
    // The block id.
    private final Block block;
    // The block metadata.
    private final double[] blockData;
    // The code for the block update.
    private final byte update;
    // The min and max offsets.
    private final double[] offsetsX, offsetsY, offsetsZ;
    // The nbt stats for this property.
    private final NBTStats nbtStats;
    // The code for the override. 0=air, 1=all, 2=replaceable.
    private final byte override;

    public EntryDropsSetBlock(String path, JsonObject root, int index, JsonObject node, IPropertyReader loader) {
        super(node, path);
        this.block = FileHelper.readBlock(node, path, "id");
        this.blockData = FileHelper.readCounts(node, path, "data", 0.0, 0.0);
        this.update = (byte) FileHelper.readInteger(node, path, "update", 3);
        this.offsetsX = FileHelper.readCounts(node, path, "x", 0.0, 0.0);
        this.offsetsY = FileHelper.readCounts(node, path, "y", 0.0, 0.0);
        this.offsetsZ = FileHelper.readCounts(node, path, "z", 0.0, 0.0);
        this.nbtStats = new NBTStats(path, root, index, node, loader);

        String text = FileHelper.readText(node, path, "override", "replaceable");
        if (text.equals("true")) {
            this.override = 1;
        }
        else if (text.equals("false")) {
            this.override = 0;
        }
        else if (text.equals("replaceable")) {
            this.override = 2;
        }
        else
            throw new MobPropertyException("Invalid override value! (must be true, false, or replaceable)", path);
    }

    // Returns an array of required field names.
    @Override
    public String[] getRequiredFields() {
        return new String[] { "id" };
    }

    // Returns an array of optional field names.
    @Override
    public String[] getOptionalFields() {
        return new String[] { "data", "update", "x", "y", "z", "override", "tags" };
    }

    // Modifies the list of drops.
    @Override
    public void modifyDrops(MobDropsInfo mobDrops) {
        if (!mobDrops.theEntity.worldObj.isRemote) {
        	BlockPos pos = new BlockPos(mobDrops.theEntity).add(FileHelper.getCount(this.offsetsX), FileHelper.getCount(this.offsetsY), FileHelper.getCount(this.offsetsZ));
            int data = FileHelper.getCount(this.blockData);

            if (this.override != 1) {
                IBlockState blockReplacing = mobDrops.theEntity.worldObj.getBlockState(pos);
                if (blockReplacing.getBlock() != Blocks.AIR && (this.override == 0 || !blockReplacing.getMaterial().isReplaceable()))
                    return;
            }

            mobDrops.theEntity.worldObj.setBlockState(pos, this.block.getStateFromMeta(data), this.update);

            if (this.nbtStats.hasEntries()) {
                TileEntity tileEntity = mobDrops.theEntity.worldObj.getTileEntity(pos);
                if (tileEntity != null) {
                    NBTTagCompound tag = new NBTTagCompound();
                    tileEntity.writeToNBT(tag);
                    this.nbtStats.generate(mobDrops.theEntity, tag, mobDrops);
                    tileEntity.readFromNBT(tag);
                }
            }
        }
    }
}
