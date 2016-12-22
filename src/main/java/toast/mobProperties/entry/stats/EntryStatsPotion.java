package toast.mobProperties.entry.stats;

import com.google.gson.JsonObject;

import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import toast.mobProperties.entry.EntryAbstract;
import toast.mobProperties.entry.IPropertyReader;
import toast.mobProperties.event.ItemStatsInfo;
import toast.mobProperties.event.MobStatsInfo;
import toast.mobProperties.util.EffectHelper;
import toast.mobProperties.util.FileHelper;

public class EntryStatsPotion extends EntryAbstract {
    /// The potion to apply.
    private final Potion potion;
    /// The min and max potion amplifier.
    private final double[] amplifiers;
    /// The min and max potion duration.
    private final double[] durations;
    /// If true, the particles will be less visible.
    private final boolean ambient;
    /// If false, the particles will not be spawned.
    private final boolean particles;
    /// If true, this will apply the potion to entities through NBT manipulation.
    private final boolean override;

    public EntryStatsPotion(String path, JsonObject root, int index, JsonObject node, IPropertyReader loader) {
        super(node, path);
        this.potion = FileHelper.readPotion(node, path, "id");
        this.amplifiers = FileHelper.readCounts(node, path, "amplifier", 0.0, 0.0);
        this.durations = FileHelper.readCounts(node, path, "duration", Integer.MAX_VALUE, Integer.MAX_VALUE);
        this.ambient = FileHelper.readBoolean(node, path, "ambient", false);
        this.particles = FileHelper.readBoolean(node, path, "particles", true);
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
        return new String[] { "amplifier", "duration", "ambient", "particles", "override" };
    }

    /// Initializes the entity's stats.
    @Override
    public void init(MobStatsInfo mobStats) {
        int amplifier = FileHelper.getCount(this.amplifiers, mobStats.random);
        int duration = FileHelper.getCount(this.durations, mobStats.random);
        if (this.override) {
            EffectHelper.addPotionEffect(mobStats.theEntity, this.potion, duration, amplifier, this.ambient, this.particles);
        }
        else {
            mobStats.theEntity.addPotionEffect(new PotionEffect(this.potion, duration, amplifier, this.ambient, this.particles));
        }
    }

    /// Modifies the item.
    @Override
    public void modifyItem(ItemStatsInfo itemStats) {
        int amplifier = FileHelper.getCount(this.amplifiers, itemStats.random);
        int duration = FileHelper.getCount(this.durations, itemStats.random);
        EffectHelper.addPotionEffect(itemStats.theItem, this.potion, duration, amplifier, this.ambient, this.particles);
    }
}