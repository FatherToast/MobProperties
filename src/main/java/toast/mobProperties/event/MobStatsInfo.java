package toast.mobProperties.event;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import toast.mobProperties.entry.IProperty;
import toast.mobProperties.util.EffectHelper;

public class MobStatsInfo {
    // The name for this mod's modifiers.
    public static final String MODIFIER_NAME = "MobProperties|Modifier";
    // The UUIDs for this mod's modifiers.
    public static final UUID[] MODIFIER_UUIDS = new UUID[3];
    static {
        for (int i = MobStatsInfo.MODIFIER_UUIDS.length; i-- > 0;) {
            MobStatsInfo.MODIFIER_UUIDS[i] = UUID.fromString("70A57" + i + "F4-87F1-4D6B-BBCA-266A93B73C72");
        }
    }

    // The entity currently being initialized.
    public final EntityLivingBase theEntity;
    // The proportion of health the entity had before initialization.
    public final float originalDamage;
    // The entity's random number generator.
    public final Random random;
    // The map of modifiers to apply.
    public final HashMap<String, double[]> modifierMap = new HashMap<String, double[]>();
    // The list of drops functions to execute on death.
    public final ArrayList<String> addDropsList = new ArrayList<String>();

    public MobStatsInfo(EntityLivingBase entity) {
        this.theEntity = entity;
        this.originalDamage = entity.getHealth() / entity.getMaxHealth();
        this.random = entity.getRNG();
    }

    // Adds a modifier to the list of modifiers. Combines like modifiers.
    public void addModifier(String name, double value, int operation) {
        double[] values = this.modifierMap.get(name);
        if (values == null) {
            values = new double[MobStatsInfo.MODIFIER_UUIDS.length];
            values[2] = Double.NaN;
            this.modifierMap.put(name, values);
        }
        if (operation < 2) {
            values[operation] += value;
        }
        else if (Double.isNaN(values[operation])) {
            values[operation] = value;
        }
        else {
            values[operation] *= 1.0 + value;
        }
    }

    // Sets a modifier in the list of modifiers.
    public void setModifier(String name, double value, int operation) {
        double[] values = this.modifierMap.get(name);
        if (values == null) {
            values = new double[MobStatsInfo.MODIFIER_UUIDS.length];
            values[2] = Double.NaN;
            this.modifierMap.put(name, values);
        }
        values[operation] = value;
    }

    // Adds the entry to a list of drops functions that will be executed on death.
    public void addDrop(IProperty entry) {
        this.addDropsList.add(entry.getJsonString());
    }

    // Saves any important info to the mob and applies modifiers.
    public void save() {
        // Apply modifiers.
        for (Map.Entry<String, double[]> entry : this.modifierMap.entrySet()) {
            IAttributeInstance attribute = this.theEntity.getAttributeMap().getAttributeInstanceByName(entry.getKey());
            if (attribute != null) {
                for (int op = 0; op < MobStatsInfo.MODIFIER_UUIDS.length; op++)
                    if (entry.getValue()[op] != 0.0 && !Double.isNaN(entry.getValue()[op]) && attribute.getModifier(MobStatsInfo.MODIFIER_UUIDS[op]) == null) {
                        attribute.applyModifier(new AttributeModifier(MobStatsInfo.MODIFIER_UUIDS[op], MobStatsInfo.MODIFIER_NAME, entry.getValue()[op], op));
                    }
            }
        }
        this.theEntity.setHealth(this.theEntity.getMaxHealth() * this.originalDamage);

        // Save drops functions.
        EffectHelper.saveDrops(this);
    }
}
