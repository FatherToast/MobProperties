package toast.mobProperties.entry.stats;

import com.google.gson.JsonObject;

import toast.mobProperties.ModMobProperties;
import toast.mobProperties.entry.EntryAbstract;
import toast.mobProperties.entry.IPropertyReader;
import toast.mobProperties.event.ItemStatsInfo;
import toast.mobProperties.event.MobStatsInfo;
import toast.mobProperties.util.EffectHelper;
import toast.mobProperties.util.FileHelper;
import toast.mobProperties.util.MobPropertyException;

public class EntryStatsModifier extends EntryAbstract {
    /// The attribute name to modify.
    public final String name;
    /// The min and max amount to modify by.
    public final double[] values;
    /// The operation to perform.
    public final int operation;
    /// If true, operation is ignored and this sets the base attribute. Only affects mobs.
    public final boolean override;

    public EntryStatsModifier(String path, JsonObject root, int index, JsonObject node, IPropertyReader loader) {
        super(node, path);
        this.name = FileHelper.readText(node, path, "attribute", "");
        this.values = FileHelper.readCounts(node, path, "value", 0.0, 0.0);
        this.operation = FileHelper.readInteger(node, path, "operator", 0);
        if (this.operation < 0 || this.operation > 2)
            throw new MobPropertyException("Invalid operator! (" + this.operation + ": 0=add, 1=additive_multiplier, 2=multiplicative_multiplier)", path);
        this.override = FileHelper.readBoolean(node, path, "override", false);
    }

    /// Returns an array of required field names.
    @Override
    public String[] getRequiredFields() {
        return new String[] { "attribute", "value" };
    }

    /// Returns an array of optional field names.
    @Override
    public String[] getOptionalFields() {
        return new String[] { "operator", "override" };
    }

    /// Initializes the entity's stats.
    @Override
    public void init(MobStatsInfo mobStats) {
        double value = FileHelper.getValue(this.values, mobStats.random);
        if (this.override) {
			if (mobStats.theEntity.getAttributeMap().getAttributeInstanceByName(this.name) == null) {
				ModMobProperties.logError("Attempted to override invalid attribute \"" + this.name + "\" for " + mobStats.theEntity.toString());
			}
			else {
				mobStats.theEntity.getAttributeMap().getAttributeInstanceByName(this.name).setBaseValue(value);
			}
        }
        else {
            mobStats.addModifier(this.name, value, this.operation);
        }
    }

    /// Modifies the item.
    @Override
    public void modifyItem(ItemStatsInfo itemStats) {
        double value = FileHelper.getValue(this.values, itemStats.random);
        EffectHelper.addModifier(itemStats.theItem, this.name, value, this.operation);
    }
}