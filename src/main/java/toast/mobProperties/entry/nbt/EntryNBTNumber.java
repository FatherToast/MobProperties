package toast.mobProperties.entry.nbt;

import toast.mobProperties.entry.EntryAbstract;
import toast.mobProperties.entry.IPropertyReader;
import toast.mobProperties.util.FileHelper;

import com.google.gson.JsonObject;

public abstract class EntryNBTNumber extends EntryAbstract {
    /// The name of this tag.
    protected final String name;
    /// The value of this tag.
    protected final double[] values;

    public EntryNBTNumber(String path, JsonObject root, int index, JsonObject node, IPropertyReader loader) {
        super(node, path);
        this.name = FileHelper.readText(node, path, "name", "");
        this.values = FileHelper.readCounts(node, path, "value", 0.0, 0.0);
    }

    /// Returns an array of required field names.
    @Override
    public String[] getRequiredFields() {
        return new String[] { };
    }

    /// Returns an array of optional field names.
    @Override
    public String[] getOptionalFields() {
        return new String[] { "name", "value" };
    }
}