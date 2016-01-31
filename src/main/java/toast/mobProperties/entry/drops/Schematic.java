package toast.mobProperties.entry.drops;

import java.util.Collection;
import java.util.HashSet;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagDouble;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.world.World;

public class Schematic {

	/// Size in the x direction (width).
	private final short xSize;
	/// Size in the y direction (height).
	private final short ySize;
	/// Size in the z direction (length).
	private final short zSize;

	/// Array of blocks (the index of the block at X,Y,Z is (Y * zSize + Z) * xSize + X).
	public final Block[] blocks;
	/// Array of block metadata (the index of the block at X,Y,Z is (Y * zSize + Z) * xSize + X).
	public final byte[] metadata;
	/// Array of tile entities.
	public final NBTTagCompound[] tileEntities;

	/// Array of entities.
	public final NBTTagCompound[] entities;

	public Schematic(NBTTagCompound tag) {
		NBTTagList tagList;
		this.xSize = tag.getShort("Width");
		this.ySize = tag.getShort("Height");
		this.zSize = tag.getShort("Length");

		byte[] blockIds = tag.getByteArray("Blocks");
		this.blocks = new Block[blockIds.length];
		for (int i = blockIds.length; i-- > 0;) {
			this.blocks[i] = Block.getBlockById(blockIds[i] & 0xff);
		}
		this.metadata = tag.getByteArray("Data");
		tagList = tag.getTagList("TileEntities", tag.getId());
		this.tileEntities = new NBTTagCompound[tagList.tagCount()];
		for (int i = tagList.tagCount(); i-- > 0;) {
			this.tileEntities[i] = (NBTTagCompound) tagList.getCompoundTagAt(i).copy();
		}

		tagList = tag.getTagList("Entities", tag.getId());
		this.entities = new NBTTagCompound[tagList.tagCount()];
		for (int i = tagList.tagCount(); i-- > 0;) {
			this.entities[i] = (NBTTagCompound) tagList.getCompoundTagAt(i).copy();
		}
	}

	/// The size of this schematic in the x direction.
	public int getXSize() {
		return this.xSize;
	}
	/// The size of this schematic in the x direction.
	public int getYSize() {
		return this.ySize;
	}
	/// The size of this schematic in the x direction.
	public int getZSize() {
		return this.zSize;
	}

	/// Places this schematic in the world with given coordinates, optionally updating, overriding blocks, and overriding air.
	public void place(World world, int x, int y, int z, byte update, byte blockOverride, byte airOverride) {
		// Place blocks
		int index;
		byte override;
		HashSet<ChunkCoordinates> skipTileEntities = new HashSet<ChunkCoordinates>();
		for (int y0 = 0; y0 < this.ySize; y0++) {
			for (int z0 = 0; z0 < this.zSize; z0++) {
				for (int x0 = 0; x0 < this.xSize; x0++) {
					index = (y0 * this.zSize + z0) * this.xSize + x0;
					override = this.blocks[index] == Blocks.air ? airOverride : blockOverride;
		            if (override != 1) {
		                Block blockReplacing = world.getBlock(x + x0, y + y0, z + z0);
		                if (blockReplacing != Blocks.air && (override == 0 || !blockReplacing.getMaterial().isReplaceable())) {
		                	skipTileEntities.add(new ChunkCoordinates(x + x0, y + y0, z + z0));
							continue;
						}
		            }
					world.setBlock(x + x0, y + y0, z + z0, this.blocks[index], this.metadata[index] & 0xf, update);
				}
			}
		}

        // Spawn entities
        NBTTagCompound tag, riderTag;
        Entity entity, rider, mount;
        for (int i = this.entities.length; i-- > 0;) {
            tag = Schematic.copyEntityTagRel(this.entities[i], x, y, z);
            entity = EntityList.createEntityFromNBT(tag, world);
            if (entity != null) {
                world.spawnEntityInWorld(entity);
                rider = entity;
                for (riderTag = tag; riderTag.hasKey("Riding", tag.getId()); riderTag = riderTag.getCompoundTag("Riding")) {
                    mount = EntityList.createEntityFromNBT(riderTag.getCompoundTag("Riding"), world);
                    if (mount != null) {
                        world.spawnEntityInWorld(mount);
	                    rider.mountEntity(mount);
	                }
	                rider = mount;
	            }
	        }
		}

		// Load tile entities
		TileEntity tileEntity;
		for (int i = this.tileEntities.length; i-- > 0;) {
            tag = (NBTTagCompound) this.tileEntities[i].copy();
            tag.setInteger("x", tag.getInteger("x") + x);
            tag.setInteger("y", tag.getInteger("y") + y);
            tag.setInteger("z", tag.getInteger("z") + z);
            tileEntity = world.getTileEntity(tag.getInteger("x"), tag.getInteger("y"), tag.getInteger("z"));
            if (tileEntity != null && !skipTileEntities.contains(new ChunkCoordinates(tileEntity.xCoord, tileEntity.yCoord, tileEntity.zCoord))) {
            	tileEntity.readFromNBT(tag);
            }
        }
	}

    /// Creates a deep copy of generic mod data, updates its position to the given coords, and returns the new tag.
    private static NBTTagCompound copyEntityTagRel(NBTTagCompound copyFrom, int x, int y, int z) {
    	NBTTagCompound copyTo = new NBTTagCompound();
        for (String name : (Collection<String>) copyFrom.func_150296_c()) { // Get tags
            NBTBase tag = copyFrom.getTag(name);
            if (name.equals("Pos")) {
                NBTTagList posFrom = (NBTTagList) tag;
                NBTTagList posTo = new NBTTagList();
                posTo.appendTag(new NBTTagDouble(posFrom.func_150309_d(0) + x)); // get(index)
                posTo.appendTag(new NBTTagDouble(posFrom.func_150309_d(1) + y));
                posTo.appendTag(new NBTTagDouble(posFrom.func_150309_d(2) + z));
                copyTo.setTag(name, posTo);
            }
            else if (name.equals("Riding")) {
                copyTo.setTag(name, Schematic.copyEntityTagRel((NBTTagCompound) tag, x, y, z));
            }
            else {
                copyTo.setTag(name, tag.copy());
            }
        }
        return copyTo;
    }
}
