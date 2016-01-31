package toast.mobProperties.api;

import java.util.List;

import net.minecraft.item.Item;

/**
 * Contains all info about a single item drop modification.
 */
public class DropEntry {
    /** The item being added/removed. */
    private final byte setDefault;

    /** The item being added/removed. */
    private final Item item;
    /** The min and max item damage. */
    private final double[] damages;
    /** The min and max item counts. */
    private final double[] counts;

    /** The min and max attempts for the DropEntry to take effect. */
    private final double[] attempts;
    /** The chance for this DropEntry to take effect. */
    private final double chance;
    /** The conditions that must be fulfilled for this DropEntry to take effect. */
    private final String[] conditions;

    /**
     * Constructs a DropEntry that only defines a "default" function's setting.
     * Only the last called "default" function setting is used.
     *
     * @param setDefault The value for the default that has been set.
     * @param attempts The range of attempts for this drop to take effect (length 2 array).
     * @param chance The net chance that this will take effect, granted all conditions are met.
     * @param conditions Array of "if_X" functions this drop is contained in.
     */
    public DropEntry(byte setDefault, double[] attempts, double chance, List<String> conditions) {
        this.setDefault = setDefault;

        this.item = null;
        this.damages = null;
        this.counts = null;

        this.attempts = attempts;
        this.chance = chance;
        this.conditions = conditions.toArray(new String[0]);
    }

    /**
     * Constructs a DropEntry that contains basic info about the item drop.
     *
     * @param item The item being dropped.
     * @param damages The range of damage values that can be dropped (length 2 array).
     * @param counts The range of possible stack sizes for the drop (length 2 array).
     * @param attempts The range of attempts for this drop to take effect (length 2 array).
     * @param chance The net chance that this will take effect, granted all conditions are met.
     * @param conditions Array of "if_X" functions this drop is contained in.
     */
    public DropEntry(Item item, double[] damages, double[] counts, double[] attempts, double chance, List<String> conditions) {
        this.setDefault = -1;

        this.item = item;
        this.damages = damages;
        this.counts = counts;

        this.attempts = attempts;
        this.chance = chance;
        this.conditions = conditions.toArray(new String[0]);
    }

    /** @return The default value set by this entry, if it represents a "default" function.<br>
     *-1 = not applicable, 0 = false, 1 = true, 2 = equipment only */
    public byte getDefault() {
        return this.setDefault;
    }

    /** @return The item drop being modified. */
    public Item getItem() {
        return this.item;
    }
    /** @return The min and max damage value of the item in a length-2 array.
     *          If removing, a negative value matches any damage value. */
    public double[] getDamages() {
        return this.damages;
    }
    /** @return The min and max stack size of the item in a length-2 array.
     *          A negative value implies removal, may be {@link Double#NEGATIVE_INFINITY}. */
    public double[] getCounts() {
        return this.counts;
    }

    /** @return The min and max attempts for the DropEntry to take effect in a length-2 array. */
    public double[] getAttempts() {
        return this.attempts;
    }
    /** @return The net chance that this DropEntry will take effect, assuming all
     *          conditions are met. */
    public double getChance() {
        return this.chance;
    }
    /** @return An array of all if statements ("if_recently_hit", "!if_wet", etc.) that
     *          must be satisfied for this DropEntry to take effect. */
    public String[] getConditions() {
        return this.conditions;
    }
}
