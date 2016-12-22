package toast.mobProperties.api;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.EntityList;
import toast.mobProperties.entry.MobProperties;

/**
 * The class primarily used to interface with the Mob Properties mod.<br>
 * Analyzes the loaded properties and creates a list of drop data that can be read by other mods.
 */
public class MobPropertiesAPI {
    /**
     * Builds a list containing information about all drops modified by class by this mod
     * for a specific entity class.
     *
     * @param entityClass The class of entity to get the information for.
     * @return The list of all drop entries modifying the entity's drops.
     */
    public static List<DropEntry> getDrops(Class entityClass) {
        List<DropEntry> dropsList = new ArrayList<DropEntry>();
        MobProperties properties = MobProperties.getProperties(EntityList.getEntityStringFromClass(entityClass));
        if (properties != null) {
            properties.addDrops(dropsList);
        }
        return dropsList;
    }
}
