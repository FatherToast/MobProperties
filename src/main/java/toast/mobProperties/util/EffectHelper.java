package toast.mobProperties.util;

import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.potion.Potion;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import toast.mobProperties.ModMobProperties;
import toast.mobProperties.entry.MobDrops;
import toast.mobProperties.entry.MobStats;
import toast.mobProperties.event.MobDropsInfo;
import toast.mobProperties.event.MobStatsInfo;

public abstract class EffectHelper {
    // The NBT tags used by this mod.
    private static final String TAG_INIT = "MP|Init";

    private static final String TAG_BASE = "mp|drops";

    private static final String TAG_PRE_STATS = "pfs";
    private static final String TAG_STATS = "sfs";
    private static final String TAG_DROPS = "dfs";

    private static final String TAG_XP_BASE = "xpb";
    private static final String TAG_XP_ADD = "xpa";
    private static final String TAG_XP_MULT = "xpm";

    // Legacy tags. Kept for old version compatibility.
    @Deprecated
    private static final String TAG_ADD_DROPS = "add";
    @Deprecated
    private static final String TAG_SPAWNS = "ent";

    // Applies the enchantment to the itemStack at the given level. Called by all other enchantItem methods to do the actual enchanting.
    public static void enchantItem(ItemStack itemStack, Enchantment enchant, int level) {
        if (enchant != null) {
            itemStack.addEnchantment(enchant, level);
        }
    }

    // Randomly enchants the itemStack based on the level (identical to using an enchantment table).
    public static void enchantItem(ItemStack itemStack, int level, boolean treasure) {
        EffectHelper.enchantItem(ModMobProperties.random, itemStack, level, treasure);
    }

    public static void enchantItem(Random random, ItemStack itemStack, int level, boolean treasure) {
        EnchantmentHelper.addRandomEnchantment(random, itemStack, level, treasure);
    }

    // Adds a line of text to the item stack's infobox.
    public static void addItemText(ItemStack itemStack, String text) {
    	String loreList = "Lore";
        NBTTagCompound displayTag = itemStack.getSubCompound("display", true);
        if (!displayTag.hasKey(loreList, Constants.NBT.TAG_LIST)) {
            displayTag.setTag(loreList, new NBTTagList());
        }
        NBTTagString stringTag = new NBTTagString(text);
        displayTag.getTagList(loreList, Constants.NBT.TAG_STRING).appendTag(stringTag);
    }

    // Sets the item's color. No effect on most items.
    public static void dye(ItemStack itemStack, int color) {
        itemStack.getSubCompound("display", true).setInteger("color", color);
    }

    // Adds a custom potion effect to the entity (can put regen on undead, poison on spiders, etc.).
    public static void addPotionEffect(EntityLivingBase entity, Potion potion, int duration, int amplifier, boolean ambient, boolean particles) {
        NBTTagCompound tag = new NBTTagCompound();
        entity.writeToNBT(tag);
    	String entityPotionList = "ActiveEffects";
        if (!tag.hasKey(entityPotionList, Constants.NBT.TAG_LIST)) {
            tag.setTag(entityPotionList, new NBTTagList());
        }
        NBTTagCompound potionTag = new NBTTagCompound();
        if (potion != null) {
            potionTag.setByte("Id", (byte) Potion.getIdFromPotion(potion));
        }
        if (duration != 0) {
            potionTag.setInteger("Duration", duration);
        }
        if (amplifier != 0) {
            potionTag.setByte("Amplifier", (byte) amplifier);
        }
        if (ambient) {
            potionTag.setBoolean("Ambient", ambient);
        }
        potionTag.setBoolean("ShowParticles", particles);
        tag.getTagList(entityPotionList, Constants.NBT.TAG_COMPOUND).appendTag(potionTag);
        entity.readFromNBT(tag);
    }

    // Adds a custom potion effect to the item stack.
    public static void addPotionEffect(ItemStack itemStack, Potion potion, int duration, int amplifier, boolean ambient, boolean particles) {
    	String itemPotionList = "CustomPotionEffects";
        if (!itemStack.hasTagCompound() || !itemStack.getTagCompound().hasKey(itemPotionList, Constants.NBT.TAG_LIST)) {
            itemStack.setTagInfo(itemPotionList, new NBTTagList());
        }
        NBTTagCompound tag = new NBTTagCompound();
        tag.setByte("Id", (byte) Potion.getIdFromPotion(potion));
        tag.setInteger("Duration", duration);
        tag.setByte("Amplifier", (byte) amplifier);
        tag.setBoolean("Ambient", ambient);
        tag.setBoolean("ShowParticles", particles);
        itemStack.getTagCompound().getTagList(itemPotionList, Constants.NBT.TAG_COMPOUND).appendTag(tag);
    }

    // Adds a custom attribute modifier to the item stack.
    public static void addModifier(ItemStack itemStack, String attribute, double value, int operation) {
    	String modifierList = "AttributeModifiers";
        if (!itemStack.hasTagCompound() || !itemStack.getTagCompound().hasKey(modifierList, Constants.NBT.TAG_LIST)) {
            itemStack.setTagInfo(modifierList, new NBTTagList());
        }
        NBTTagCompound tag = new NBTTagCompound();
        tag.setString("AttributeName", attribute);
        tag.setString("Name", "MobProperties|" + Integer.toString(ModMobProperties.random.nextInt(), Character.MAX_RADIX));
        tag.setDouble("Amount", value);
        tag.setInteger("Operation", operation);
        UUID id = UUID.randomUUID();
        tag.setLong("UUIDMost", id.getMostSignificantBits());
        tag.setLong("UUIDLeast", id.getLeastSignificantBits());
        itemStack.getTagCompound().getTagList(modifierList, Constants.NBT.TAG_COMPOUND).appendTag(tag);
    }

    // Saves the entity's initialization state.
    public static void setInit(Entity entity, int state) {
        entity.getEntityData().setByte(EffectHelper.TAG_INIT, (byte) state);
    }

    // Loads the entity's initialization state.
    public static byte getInit(Entity entity) {
        return entity.getEntityData().getByte(EffectHelper.TAG_INIT);
    }

    // Saves xp data to the entity.
    public static void saveXP(MobDropsInfo mobDrops) {
        EffectHelper.saveXP(mobDrops.theEntity, mobDrops.xpBase, mobDrops.xpAdd, mobDrops.xpMult);
    }

    private static void saveXP(EntityLivingBase entity, int base, int add, double mult) {
        if (entity instanceof EntityLiving) {
            NBTTagCompound tag = entity.getEntityData().getCompoundTag(EffectHelper.TAG_BASE);
            if (!entity.getEntityData().hasKey(EffectHelper.TAG_BASE, Constants.NBT.TAG_COMPOUND)) {
                entity.getEntityData().setTag(EffectHelper.TAG_BASE, tag);
            }

            if (base >= 0) {
                tag.setInteger(EffectHelper.TAG_XP_BASE, base);
            }
            else {
                tag.removeTag(EffectHelper.TAG_XP_BASE);
            }
            if (add != 0) {
                tag.setInteger(EffectHelper.TAG_XP_ADD, add);
            }
            else {
                tag.removeTag(EffectHelper.TAG_XP_ADD);
            }
            if (mult != 1.0) {
                tag.setDouble(EffectHelper.TAG_XP_MULT, mult);
            }
            else {
                tag.removeTag(EffectHelper.TAG_XP_MULT);
            }
        }
    }

    // Loads xp data to the mob info.
    public static void loadXP(MobDropsInfo mobDrops) {
        if (mobDrops.theEntity instanceof EntityLiving) {
            if (!mobDrops.theEntity.getEntityData().hasKey(EffectHelper.TAG_BASE, Constants.NBT.TAG_COMPOUND))
                return;
            NBTTagCompound tag = mobDrops.theEntity.getEntityData().getCompoundTag(EffectHelper.TAG_BASE);

            if (tag.hasKey(EffectHelper.TAG_XP_BASE, Constants.NBT.TAG_INT)) {
                mobDrops.xpBase = tag.getInteger(EffectHelper.TAG_XP_BASE);
            }
            if (tag.hasKey(EffectHelper.TAG_XP_ADD, Constants.NBT.TAG_INT)) {
                mobDrops.xpAdd = tag.getInteger(EffectHelper.TAG_XP_ADD);
            }
            if (tag.hasKey(EffectHelper.TAG_XP_MULT, Constants.NBT.TAG_DOUBLE)) {
                mobDrops.xpMult = tag.getDouble(EffectHelper.TAG_XP_MULT);
            }
        }
    }

    // Loads xp data to the entity.
    public static void loadXP(EntityLivingBase entity) {
        if (entity instanceof EntityLiving) {
            if (!entity.getEntityData().hasKey(EffectHelper.TAG_BASE, Constants.NBT.TAG_COMPOUND))
                return;
            NBTTagCompound tag = entity.getEntityData().getCompoundTag(EffectHelper.TAG_BASE);

            if (tag.hasKey(EffectHelper.TAG_XP_BASE, Constants.NBT.TAG_INT) || tag.hasKey(EffectHelper.TAG_XP_ADD, Constants.NBT.TAG_INT) || tag.hasKey(EffectHelper.TAG_XP_MULT, Constants.NBT.TAG_DOUBLE)) {
                String[] fieldNames = { "field_70728_aV", "experienceValue" };
                int exp;
                if (tag.hasKey(EffectHelper.TAG_XP_BASE, Constants.NBT.TAG_INT)) {
                    exp = tag.getInteger(EffectHelper.TAG_XP_BASE);
                }
                else {
                    exp = ((Integer) ObfuscationReflectionHelper.getPrivateValue(EntityLiving.class, (EntityLiving) entity, fieldNames)).intValue();
                }
                if (tag.hasKey(EffectHelper.TAG_XP_ADD, Constants.NBT.TAG_INT)) {
                    exp += tag.getInteger(EffectHelper.TAG_XP_ADD);
                }
                if (tag.hasKey(EffectHelper.TAG_XP_MULT, Constants.NBT.TAG_DOUBLE)) {
                    exp = (int) Math.round(exp * tag.getDouble(EffectHelper.TAG_XP_MULT));
                }
                if (exp < 0) {
                    exp = 0;
                }
                ObfuscationReflectionHelper.setPrivateValue(EntityLiving.class, (EntityLiving) entity, Integer.valueOf(exp), fieldNames);
            }
        }
    }

    // Loads stats data and executes it.
    public static void loadStats(EntityLivingBase entity, boolean preStats) {
        if (!entity.getEntityData().hasKey(EffectHelper.TAG_BASE, Constants.NBT.TAG_COMPOUND))
            return;
        NBTTagCompound tag = entity.getEntityData().getCompoundTag(EffectHelper.TAG_BASE);

        String tagName = preStats ? EffectHelper.TAG_PRE_STATS : EffectHelper.TAG_STATS;
        if (tag.hasKey(tagName, Constants.NBT.TAG_LIST)) {
            MobStatsInfo mobStats = new MobStatsInfo(entity);
            new MobStats(entity.getClass().getName(), tag.getTagList(tagName, Constants.NBT.TAG_STRING)).init(mobStats);
            mobStats.save();
        }
    }

    // Saves drops data to the entity to be executed later.
    public static void saveDrops(MobStatsInfo mobStats) {
        if (mobStats.addDropsList.size() == 0)
            return;
        NBTTagCompound tag = mobStats.theEntity.getEntityData().getCompoundTag(EffectHelper.TAG_BASE);
        if (!mobStats.theEntity.getEntityData().hasKey(EffectHelper.TAG_BASE, Constants.NBT.TAG_COMPOUND)) {
            mobStats.theEntity.getEntityData().setTag(EffectHelper.TAG_BASE, tag);
        }

        NBTTagList dropList = tag.getTagList(EffectHelper.TAG_DROPS, Constants.NBT.TAG_STRING);
        if (!tag.hasKey(EffectHelper.TAG_DROPS, Constants.NBT.TAG_LIST)) {
            tag.setTag(EffectHelper.TAG_DROPS, dropList);
        }
        for (String dropsFunction : mobStats.addDropsList) {
            dropList.appendTag(new NBTTagString(dropsFunction));
        }
    }

    // Loads drops data and executes it.
    public static void loadDrops(MobDropsInfo mobDrops) {
        if (!mobDrops.theEntity.getEntityData().hasKey(EffectHelper.TAG_BASE, Constants.NBT.TAG_COMPOUND))
            return;
        NBTTagCompound tag = mobDrops.theEntity.getEntityData().getCompoundTag(EffectHelper.TAG_BASE);

        if (tag.hasKey(EffectHelper.TAG_DROPS, Constants.NBT.TAG_LIST)) {
            new MobDrops(mobDrops.theEntity.getClass().getName(), tag.getTagList(EffectHelper.TAG_DROPS, Constants.NBT.TAG_STRING)).modifyDrops(mobDrops);
        }
    }

    // Loads legacy drops data to the drops list.
    @Deprecated
    public static void loadLegacyDrops(EntityLivingBase entity, List<EntityItem> drops) {
        if (!entity.getEntityData().hasKey(EffectHelper.TAG_BASE, Constants.NBT.TAG_COMPOUND))
            return;
        NBTTagCompound tag = entity.getEntityData().getCompoundTag(EffectHelper.TAG_BASE);

        if (tag.hasKey(EffectHelper.TAG_ADD_DROPS, Constants.NBT.TAG_LIST)) {
            NBTTagList dropList = tag.getTagList(EffectHelper.TAG_ADD_DROPS, Constants.NBT.TAG_COMPOUND);
            int length = dropList.tagCount();
            ItemStack itemStack;
            EntityItem drop;
            for (int i = 0; i < length; i++) {
                itemStack = ItemStack.loadItemStackFromNBT(dropList.getCompoundTagAt(i));
                if (itemStack == null || itemStack.stackSize == 0) {
                    continue;
                }
                if (itemStack.stackSize > 0) {
                    int count = itemStack.stackSize;
                    itemStack.stackSize = 1;
                    while (count-- > 0) {
                        drop = new EntityItem(entity.worldObj, entity.posX, entity.posY, entity.posZ, itemStack.copy());
                        drop.setDefaultPickupDelay();
                        drops.add(drop);
                    }
                }
                else {
                    EffectHelper.removeDrop(itemStack, entity, drops);
                }
            }
        }
        if (tag.hasKey(EffectHelper.TAG_SPAWNS, Constants.NBT.TAG_LIST)) {
            NBTTagList spawnList = tag.getTagList(EffectHelper.TAG_SPAWNS, Constants.NBT.TAG_COMPOUND);
            int length = spawnList.tagCount();
            Entity mob;
            for (int i = 0; i < length; i++) {
                mob = EntityList.createEntityFromNBT(spawnList.getCompoundTagAt(i), entity.worldObj);
                if (mob != null) {
                    mob.rotationYaw += entity.rotationYaw;
                    mob.setPosition(entity.posX + mob.posX, entity.posY + mob.posY, entity.posZ + mob.posZ);
                    entity.worldObj.spawnEntityInWorld(mob);
                }
            }
        }
    }

    // Removes the item stack. Called when an item with a negative stack size is loaded.
    @Deprecated
    private static void removeDrop(ItemStack itemStack, EntityLivingBase entity, List<EntityItem> drops) {
        boolean infinite = itemStack.stackSize == -Integer.MAX_VALUE;
        EffectHelper.removeDrop(itemStack, infinite, entity, drops);
    }

    @Deprecated
    private static int removeDrop(ItemStack itemStack, boolean infinite, EntityLivingBase entity, List<EntityItem> drops) {
        EntityItem drop;
        for (Iterator<EntityItem> iterator = drops.iterator(); iterator.hasNext();) {
            drop = iterator.next();
            ItemStack dropStack = drop.getEntityItem();
            if (itemStack.getItem() == dropStack.getItem() && (itemStack.getItemDamage() < 0 || itemStack.getItemDamage() == dropStack.getItemDamage())) {
                if (infinite) {
                    iterator.remove();
                }
                else {
                    itemStack.stackSize += dropStack.stackSize;
                    if (itemStack.stackSize < 0) {
                        iterator.remove();
                    }
                    else if (itemStack.stackSize == 0) {
                        iterator.remove();
                        return 0;
                    }
                    else {
                        dropStack.stackSize = itemStack.stackSize;
                        drop.setEntityItemStack(dropStack);
                        return 0;
                    }
                }
            }
        }
        return itemStack.stackSize;
    }
}