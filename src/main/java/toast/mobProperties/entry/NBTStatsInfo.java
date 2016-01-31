package toast.mobProperties.entry;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Random;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

public class NBTStatsInfo {
    // The mob info this is a part of.
    public final Object parent;
    // The item currently being initialized (null if the entity is being initialized).
    public final ItemStack theItem;
    // The entity that is being affected.
    public final EntityLivingBase theEntity;
    // The entity's random number generator.
    public final Random random;

    // List containing all tags that will be added to the mob/item.
    private final ArrayList<NBTWrapper> tags = new ArrayList<NBTWrapper>();

    public NBTStatsInfo(ItemStack item, EntityLivingBase entity, Object mobInfo) {
        this.parent = mobInfo;
        this.theItem = item;
        this.theEntity = entity;
        this.random = entity.getRNG();
    }

    // Adds a tag to this info.
    public void addTag(String name, NBTBase tag) {
        this.tags.add(new NBTWrapper(name, tag));
    }

    // Writes all tags to the given tag compound and returns that compound.
    public NBTTagCompound writeTo(NBTTagCompound compound) {
        for (NBTWrapper wrapper : this.tags) {
            if (wrapper.getTag() == null) {
            	compound.removeTag(wrapper.getName());
            }
            else if (wrapper.getTag().getClass() == NBTTagCompound.class) {
                this.writeCompound(compound, wrapper);
            }
            else {
                compound.setTag(wrapper.getName(), wrapper.getTag());
            }
        }
        return compound;
    }
    public NBTTagList writeTo(NBTTagList list) {
        for (NBTWrapper wrapper : this.tags) {
        	if (wrapper.getTag() == null) {
        		list.removeTag(list.tagCount() - 1);
        	}
        	else {
				list.appendTag(wrapper.getTag());
			}
        }
        return list;
    }

    // Called recursively to copy all the NBT tags from a wrapped compound.
    private void writeCompound(NBTTagCompound compound, NBTWrapper wrapper) {
        NBTTagCompound copyTo = compound.getCompoundTag(wrapper.getName());
        if (!compound.hasKey(wrapper.getName())) {
            compound.setTag(wrapper.getName(), copyTo);
        }

        NBTTagCompound copyFrom = (NBTTagCompound) wrapper.getTag();
        for (String name : (Collection<String>) copyFrom.func_150296_c()) {
            NBTBase tag = copyFrom.getTag(name);
            if (tag.getClass() == NBTTagCompound.class) {
                this.writeCompound(copyTo, new NBTWrapper(name, tag));
            }
            else {
                copyTo.setTag(name, tag.copy());
            }
        }
    }

    /** Wrapper class to store an NBT tag with its name. */
    private static class NBTWrapper {
        private final String name;
        private final NBTBase tag;

        public NBTWrapper(String name, NBTBase tag) {
            this.name = name;
            this.tag = tag;
        }

        /** @return the name tag for the wrapped NBTBase. Empty string only if in a list. */
        public String getName() {
            return this.name;
        }

        /** @return the wrapped NBTBase instance. */
        public NBTBase getTag() {
            return this.tag;
        }
    }
}
