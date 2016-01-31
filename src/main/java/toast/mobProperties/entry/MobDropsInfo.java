package toast.mobProperties.entry;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import toast.mobProperties.EffectHelper;
import toast.mobProperties.ItemStats;

public class MobDropsInfo {
    /// The entity currently dropping items.
    public final EntityLivingBase theEntity;
    /// The entity's random number generator.
    public final Random random;
    /// The lethal source of damage.
    public final DamageSource theSource;
    /// The list of drops.
    public final ArrayList<EntityItem> dropsList;
    /// The list of drops to be added.
    public final ArrayList<EntityItem> addDropsList = new ArrayList<EntityItem>();
    /// The looting level of the attacker.
    public final int looting;
    /// True if the entity was damaged by a player recently.
    public final boolean recentlyHit;
    /// True if the drop is rare.
    public final boolean rare;
    /// True if the drop is deemed super rare.
    public final boolean superRare;

    /// Filter for the original drops. 1=keep all, 0=destroy all, 2=keep equipment only
    public byte defaultBehavior = 1;

    /// The mob's base XP. -1 if not used.
    public int xpBase = -1;
    /// The mob's additional XP.
    public int xpAdd = 0;
    /// The mob's XP multiplier.
    public double xpMult = 1.0;

    public MobDropsInfo(EntityLivingBase entity, DamageSource source, ArrayList<EntityItem> drops, int lootingLevel, boolean recentHit, int specialDropValue) {
        this.theEntity = entity;
        this.random = entity.getRNG();
        this.theSource = source;
        this.dropsList = drops;
        this.looting = lootingLevel;
        this.recentlyHit = recentHit;
        if (this.recentlyHit) {
            this.rare = specialDropValue < 5;
            this.superRare = specialDropValue <= 0;
        }
        else {
            this.rare = false;
            this.superRare = false;
        }
        EffectHelper.loadXP(this);
    }

    /// Adds the item to the list of drops. If the stack size is negative, removes the item.
    public void addDrop(Item item, int damage, int count, ItemStats itemStats) {
        ItemStack dropStack = itemStats == null ? new ItemStack(item, 1, damage) : itemStats.generate(this.theEntity, item, damage, this);
        if (dropStack.getItem() != null) {
            if (count > 0) {
                EntityItem drop;
                while (count-- > 0) {
                    drop = new EntityItem(this.theEntity.worldObj, this.theEntity.posX, this.theEntity.posY, this.theEntity.posZ, dropStack.copy());
                    drop.delayBeforeCanPickup = 10;
                    this.addDropsList.add(drop);
                }
            }
            else if (count < 0) {
                this.removeDrop(item, damage, count);
            }
        }
    }

    /// Removes the item stack. Called when an item with a negative stack size is added.
    private void removeDrop(Item item, int damage, int count) {
        boolean infinite = count == -Integer.MAX_VALUE;
        count = this.removeDrop(item, damage, count, infinite, this.addDropsList);
        if (infinite || count < 0) {
            this.removeDrop(item, damage, count, infinite, this.dropsList);
        }
    }

    private int removeDrop(Item item, int damage, int count, boolean infinite, ArrayList<EntityItem> drops) {
        EntityItem drop;
        for (Iterator<EntityItem> iterator = drops.iterator(); iterator.hasNext();) {
            drop = iterator.next();
            ItemStack dropStack = drop.getEntityItem();
            if (item == dropStack.getItem() && (damage < 0 || damage == dropStack.getItemDamage())) {
                if (infinite) {
                    iterator.remove();
                }
                else {
                    count += dropStack.stackSize;
                    if (count < 0) {
                        iterator.remove();
                    }
                    else if (count == 0) {
                        iterator.remove();
                        return 0;
                    }
                    else {
                        dropStack.stackSize = count;
                        drop.setEntityItemStack(dropStack);
                        return 0;
                    }
                }
            }
        }
        return count;
    }

    /// Applies the default behavior and adds the queued drops.
    public void applyDefaultAndAddDrops() {
        switch (this.defaultBehavior) {
            case 1:
                break;
            case 0:
                this.dropsList.clear();
                break;
            case 2:
                EntityItem drop;
                for (Iterator<EntityItem> iterator = this.dropsList.iterator(); iterator.hasNext();) {
                    drop = iterator.next();
                    if (!drop.getEntityItem().isItemStackDamageable()) {
                        iterator.remove();
                    }
                }
                break;
        }
        this.dropsList.addAll(this.addDropsList);

        /// Save entity xp and load it to the entity.
        EffectHelper.saveXP(this);
        EffectHelper.loadXP(this.theEntity);
    }
}