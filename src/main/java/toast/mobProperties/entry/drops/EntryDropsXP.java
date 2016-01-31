package toast.mobProperties.entry.drops;

import net.minecraft.entity.EntityLiving;
import toast.mobProperties.FileHelper;
import toast.mobProperties.IPropertyReader;
import toast.mobProperties.MobPropertyException;
import toast.mobProperties.entry.EntryAbstract;
import toast.mobProperties.entry.MobDropsInfo;

import com.google.gson.JsonObject;

public class EntryDropsXP extends EntryAbstract {
    /// The operator to perform.
    private final byte operator;
    /// The value of this entry.
    private final double[] values;

    public EntryDropsXP(String path, JsonObject root, int index, JsonObject node, IPropertyReader loader) {
        super(node, path);
        this.values = FileHelper.readCounts(node, path, "value", 0.0, 0.0);
        String text = FileHelper.readText(node, path, "operation", "set");
        if (text.equals("set")) {
            this.operator = 0;
        }
        else if (text.equals("add")) {
            this.operator = 1;
        }
        else if (text.equals("mult")) {
            this.operator = 2;
        }
        else {
            this.operator = 0;
            throw new MobPropertyException("Invalid operation! (must be set, add, or mult)", path);
        }
    }

    /// Returns an array of required field names.
    @Override
    public String[] getRequiredFields() {
        return new String[] { "value" };
    }

    /// Returns an array of optional field names.
    @Override
    public String[] getOptionalFields() {
        return new String[] { "operation" };
    }

    /// Modifies the list of drops.
    @Override
    public void modifyDrops(MobDropsInfo mobDrops) {
        if (mobDrops.theEntity instanceof EntityLiving) {
            switch (this.operator) {
                case 0: /// set
                    mobDrops.xpBase = FileHelper.getCount(this.values, mobDrops.random);
                    break;
                case 1: /// add
                    mobDrops.xpAdd += FileHelper.getCount(this.values, mobDrops.random);
                    break;
                case 2: /// mult
                    mobDrops.xpMult += FileHelper.getValue(this.values, mobDrops.random);
                    break;
            }
        }
    }
}