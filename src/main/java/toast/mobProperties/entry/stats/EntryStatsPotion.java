package toast.mobProperties.entry.stats;

import net.minecraft.potion.PotionEffect;
import toast.mobProperties.EffectHelper;
import toast.mobProperties.FileHelper;
import toast.mobProperties.IPropertyReader;
import toast.mobProperties.entry.EntryAbstract;
import toast.mobProperties.entry.ItemStatsInfo;
import toast.mobProperties.entry.MobStatsInfo;

import com.google.gson.JsonObject;

public class EntryStatsPotion extends EntryAbstract {
    /// The attribute name to modify.
    private final int potionId;
    /// The min and max potion amplifier.
    private final double[] amplifiers;
    /// The min and max potion duration.
    private final double[] durations;
    /// If true, the particles will be less visible.
    private final boolean ambient;
    /// If true, this will apply the potion to entities through NBT manipulation.
    private final boolean override;

    public EntryStatsPotion(String path, JsonObject root, int index, JsonObject node, IPropertyReader loader) {
        super(node, path);
        this.potionId = FileHelper.readPotion(node, path, "id").id;
        this.amplifiers = FileHelper.readCounts(node, path, "amplifier", 0.0, 0.0);
        this.durations = FileHelper.readCounts(node, path, "duration", Integer.MAX_VALUE, Integer.MAX_VALUE);
        this.ambient = FileHelper.readBoolean(node, path, "ambient", false);
        this.override = FileHelper.readBoolean(node, path, "override", false);
    }

    /// Returns an array of required field names.
    @Override
    public String[] getRequiredFields() {
        return new String[] { "id" };
    }

    /// Returns an array of optional field names.
    @Override
    public String[] getOptionalFields() {
        return new String[] { "amplifier", "duration", "ambient", "override" };
    }

    /// Initializes the entity's stats.
    @Override
    public void init(MobStatsInfo mobStats) {
        int amplifier = FileHelper.getCount(this.amplifiers, mobStats.random);
        int duration = FileHelper.getCount(this.durations, mobStats.random);
        if (this.override) {
            EffectHelper.addPotionEffect(mobStats.theEntity, this.potionId, duration, amplifier, this.ambient);
        }
        else {
            mobStats.theEntity.addPotionEffect(new PotionEffect(this.potionId, duration, amplifier, this.ambient));
        }
    }

    /// Modifies the item.
    @Override
    public void modifyItem(ItemStatsInfo itemStats) {
        int amplifier = FileHelper.getCount(this.amplifiers, itemStats.random);
        int duration = FileHelper.getCount(this.durations, itemStats.random);
        EffectHelper.addPotionEffect(itemStats.theItem, this.potionId, duration, amplifier, this.ambient);
    }
}