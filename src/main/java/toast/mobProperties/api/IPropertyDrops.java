package toast.mobProperties.api;

import java.util.List;

/**
 * Implemented by functions that add or remove drops in order to build a list of changes
 * the mod makes to mob drops.
 */
public interface IPropertyDrops
{
    /**
     * Adds all drop modifications this property applies.
     * This is only called initially on the main MobDrops object in a mob's MobProperties,
     * then recursively builds up all the data from its entries.
     *
     * @param dropsList The list to add drop data to.
     * @param attempts The min and max number of times this property will be applied.
     * @param chance The total chance this property will be applied at all.
     * @param conditions The list of conditions that must be satisfied for this property to apply.
     */
    public void addDrops(List<DropEntry> dropsList, double[] attempts, double chance, List<String> conditions);
}
