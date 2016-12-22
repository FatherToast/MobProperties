package toast.mobProperties.entry.drops;

import java.util.List;

import toast.mobProperties.api.DropEntry;
import toast.mobProperties.api.IPropertyDrops;
import toast.mobProperties.entry.EntryAbstract;
import toast.mobProperties.entry.IPropertyReader;
import toast.mobProperties.event.MobDropsInfo;
import toast.mobProperties.util.FileHelper;
import toast.mobProperties.util.MobPropertyException;

import com.google.gson.JsonObject;

public class EntryDropsDefault extends EntryAbstract implements IPropertyDrops {
    // The value to set for the default.
    private final byte value;

    public EntryDropsDefault(String path, JsonObject root, int index, JsonObject node, IPropertyReader loader) {
        super(node, path);
        String text = FileHelper.readText(node, path, "value", "");
        if (text.equals("true")) {
            this.value = 1;
        }
        else if (text.equals("false")) {
            this.value = 0;
        }
        else if (text.equals("equipment")) {
            this.value = 2;
        }
        else {
            this.value = 1;
            throw new MobPropertyException("Invalid default value! (must be true, false, or equipment)", path);
        }
    }

    // Returns an array of required field names.
    @Override
    public String[] getRequiredFields() {
        return new String[] { "value" };
    }

    // Returns an array of optional field names.
    @Override
    public String[] getOptionalFields() {
        return new String[] { };
    }

    // Modifies the list of drops.
    @Override
    public void modifyDrops(MobDropsInfo mobDrops) {
        mobDrops.defaultBehavior = this.value;
    }

    /*
     * @see toast.mobProperties.api.IPropertyDrops#addDrops(java.util.List, double[], double, java.util.List)
     */
    @Override
    public void addDrops(List<DropEntry> dropsList, double[] attempts, double chance, List<String> conditions) {
        dropsList.add(new DropEntry(this.value, attempts, chance, conditions));
    }
}