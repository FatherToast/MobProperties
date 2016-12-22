package toast.mobProperties;

import java.util.Arrays;

import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;

/**
 * This helper class loads, stores, and retrieves config options.
 */
public class Properties {

	public static Properties get() {
		return Properties.INSTANCE;
	}
	public static void load(Configuration configuration) {
		Properties.config = configuration;
        Properties.config.load();
        Properties.INSTANCE = new Properties();
        Properties.config.save();
		Properties.config = null;
	}


	public final GENERAL GENERAL = new GENERAL();
	public class GENERAL extends PropertyCategory {
		@Override
		public String name() { return "_general"; }
		@Override
		protected String comment() {
			return "General and/or miscellaneous options.";
		}

        public final boolean DEBUG = this.prop("_debug_mode", false,
        	"If true, the mod will start up in debug mode.");

        public final boolean AUTO_GEN_FILES = this.prop("auto_generate_files", true,
        	"If this is true, an empty properties file will be generated for every registered entity id.");

    };


	private static Configuration config;
	private static Properties INSTANCE;

    // Contains basic implementations for all config option types, along with some useful constants.
	private static abstract class PropertyCategory {

		/** Range: { -INF, INF } */
		protected static final double[] RDBL_ALL = { Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY };
		/** Range: { 0.0, INF } */
		protected static final double[] RDBL_POS = { 0.0, Double.POSITIVE_INFINITY };
		/** Range: { 0.0, 1.0 } */
		protected static final double[] RDBL_ONE = { 0.0, 1.0 };

		/** Range: { -INF, INF } */
		protected static final float[] RFLT_ALL = { Float.NEGATIVE_INFINITY, Float.POSITIVE_INFINITY };
		/** Range: { 0.0, INF } */
		protected static final float[] RFLT_POS = { 0.0F, Float.POSITIVE_INFINITY };
		/** Range: { 0.0, 1.0 } */
		protected static final float[] RFLT_ONE = { 0.0F, 1.0F };

		/** Range: { MIN, MAX } */
		protected static final int[] RINT_ALL = { Integer.MIN_VALUE, Integer.MAX_VALUE };
		/** Range: { -1, MAX } */
		protected static final int[] RINT_TOKEN_NEG = { -1, Integer.MAX_VALUE };
		/** Range: { 0, MAX } */
		protected static final int[] RINT_POS0 = { 0, Integer.MAX_VALUE };
		/** Range: { 1, MAX } */
		protected static final int[] RINT_POS1 = { 1, Integer.MAX_VALUE };
		/** Range: { 0, SRT } */
		protected static final int[] RINT_SRT_POS = { 0, Short.MAX_VALUE };
		/** Range: { 0, 255 } */
		protected static final int[] RINT_BYT_UNS = { 0, 0xff };
		/** Range: { 0, 127 } */
		protected static final int[] RINT_BYT_POS = { 0, Byte.MAX_VALUE };

		public PropertyCategory() {
	        Properties.config.addCustomCategoryComment(this.name(), this.comment());
		}

		public abstract String name();
		protected abstract String comment();

		protected double[] defaultDblRange() {
			return PropertyCategory.RDBL_POS;
		}
		protected float[] defaultFltRange() {
			return PropertyCategory.RFLT_POS;
		}
		protected int[] defaultIntRange() {
			return PropertyCategory.RINT_POS0;
		}

		protected boolean prop(String key, boolean defaultValue, String comment) {
	    	return this.cprop(key, defaultValue, comment).getBoolean();
	    }
		protected Property cprop(String key, boolean defaultValue, String comment) {
	    	comment = this.amendComment(comment, "Boolean", defaultValue, new Object[] { true, false });
	    	return Properties.config.get(this.name(), key, defaultValue, comment);
	    }

		protected boolean[] prop(String key, boolean[] defaultValues, String comment) {
	    	return this.cprop(key, defaultValues, comment).getBooleanList();
	    }
		protected Property cprop(String key, boolean[] defaultValues, String comment) {
	    	comment = this.amendComment(comment, "Boolean_Array", Arrays.asList(defaultValues).toArray(), new Object[] { true, false });
	    	return Properties.config.get(this.name(), key, defaultValues, comment);
	    }

		protected int prop(String key, int defaultValue, String comment) {
	    	return this.cprop(key, defaultValue, comment).getInt();
	    }
		protected int prop(String key, int defaultValue, String comment, int... range) {
	    	return this.cprop(key, defaultValue, comment, range).getInt();
	    }
		protected Property cprop(String key, int defaultValue, String comment) {
	    	return this.cprop(key, defaultValue, comment, this.defaultIntRange());
	    }
		protected Property cprop(String key, int defaultValue, String comment, int... range) {
	    	comment = this.amendComment(comment, "Integer", defaultValue, range[0], range[1]);
	    	return Properties.config.get(this.name(), key, defaultValue, comment, range[0], range[1]);
	    }

		protected int[] prop(String key, int[] defaultValues, String comment) {
	    	return this.cprop(key, defaultValues, comment).getIntList();
	    }
		protected int[] prop(String key, int[] defaultValues, String comment, int... range) {
	    	return this.cprop(key, defaultValues, comment, range).getIntList();
	    }
		protected Property cprop(String key, int[] defaultValues, String comment) {
	    	return this.cprop(key, defaultValues, comment, this.defaultIntRange());
	    }
		protected Property cprop(String key, int[] defaultValues, String comment, int... range) {
	    	comment = this.amendComment(comment, "Integer_Array", Arrays.asList(defaultValues).toArray(), range[0], range[1]);
	    	return Properties.config.get(this.name(), key, defaultValues, comment, range[0], range[1]);
	    }

		protected float prop(String key, float defaultValue, String comment) {
	    	return (float) this.cprop(key, defaultValue, comment).getDouble();
	    }
	    protected float prop(String key, float defaultValue, String comment, float... range) {
	    	return (float) this.cprop(key, defaultValue, comment, range).getDouble();
	    }
	    protected Property cprop(String key, float defaultValue, String comment) {
	    	return this.cprop(key, defaultValue, comment, this.defaultFltRange());
	    }
	    protected Property cprop(String key, float defaultValue, String comment, float... range) {
	    	comment = this.amendComment(comment, "Float", defaultValue, range[0], range[1]);
	    	return Properties.config.get(this.name(), key, this.prettyFloatToDouble(defaultValue), comment, this.prettyFloatToDouble(range[0]), this.prettyFloatToDouble(range[1]));
	    }

		protected double prop(String key, double defaultValue, String comment) {
	    	return this.cprop(key, defaultValue, comment).getDouble();
	    }
	    protected double prop(String key, double defaultValue, String comment, double... range) {
	    	return this.cprop(key, defaultValue, comment, range).getDouble();
	    }
	    protected Property cprop(String key, double defaultValue, String comment) {
	    	return this.cprop(key, defaultValue, comment, this.defaultDblRange());
	    }
	    protected Property cprop(String key, double defaultValue, String comment, double... range) {
	    	comment = this.amendComment(comment, "Double", defaultValue, range[0], range[1]);
	    	return Properties.config.get(this.name(), key, defaultValue, comment, range[0], range[1]);
	    }

	    protected double[] prop(String key, double[] defaultValues, String comment) {
	    	return this.cprop(key, defaultValues, comment).getDoubleList();
	    }
	    protected double[] prop(String key, double[] defaultValues, String comment, double... range) {
	    	return this.cprop(key, defaultValues, comment, range).getDoubleList();
	    }
	    protected Property cprop(String key, double[] defaultValues, String comment) {
	    	return this.cprop(key, defaultValues, comment, this.defaultDblRange());
	    }
	    protected Property cprop(String key, double[] defaultValues, String comment, double... range) {
	    	comment = this.amendComment(comment, "Double_Array", Arrays.asList(defaultValues).toArray(), range[0], range[1]);
	    	return Properties.config.get(this.name(), key, defaultValues, comment, range[0], range[1]);
	    }

	    protected String prop(String key, String defaultValue, String comment, String valueDescription) {
	    	return this.cprop(key, defaultValue, comment, valueDescription).getString();
	    }
	    protected String prop(String key, String defaultValue, String comment, String... validValues) {
	    	return this.cprop(key, defaultValue, comment, validValues).getString();
	    }
	    protected Property cprop(String key, String defaultValue, String comment, String valueDescription) {
	    	comment = this.amendComment(comment, "String", defaultValue, valueDescription);
	    	return Properties.config.get(this.name(), key, defaultValue, comment, new String[0]);
	    }
	    protected Property cprop(String key, String defaultValue, String comment, String... validValues) {
	    	comment = this.amendComment(comment, "String", defaultValue, validValues);
	    	return Properties.config.get(this.name(), key, defaultValue, comment, validValues);
	    }

	    private String amendComment(String comment, String type, Object[] defaultValues, String description) {
	    	return this.amendComment(comment, type, this.toReadable(defaultValues), description);
	    }
	    private String amendComment(String comment, String type, Object[] defaultValues, Object min, Object max) {
	    	return this.amendComment(comment, type, this.toReadable(defaultValues), min, max);
	    }
	    private String amendComment(String comment, String type, Object[] defaultValues, Object[] validValues) {
	    	return this.amendComment(comment, type, this.toReadable(defaultValues), validValues);
	    }
	    private String amendComment(String comment, String type, Object defaultValue, String description) {
	    	return new StringBuilder(comment).append("\n   >> ").append(type).append(":[ ")
	    		.append("Value={ ").append(description).append(" }, Default=").append(defaultValue)
	    		.append(" ]").toString();
	    }
	    private String amendComment(String comment, String type, Object defaultValue, Object min, Object max) {
	    	return new StringBuilder(comment).append("\n   >> ").append(type).append(":[ ")
	    		.append("Range={ ").append(min).append(", ").append(max).append(" }, Default=").append(defaultValue)
	    		.append(" ]").toString();
	    }
	    private String amendComment(String comment, String type, Object defaultValue, Object[] validValues) {
	    	if (validValues.length < 2) throw new IllegalArgumentException("Attempted to create config with no options!");

	    	return new StringBuilder(comment).append("\n   >> ").append(type).append(":[ ")
	    		.append("Valid_Values={ ").append(this.toReadable(validValues)).append(" }, Default=").append(defaultValue)
	    		.append(" ]").toString();
	    }

	    private double prettyFloatToDouble(float f) {
	    	return Double.parseDouble(Float.toString(f));
	    }
	    private String toReadable(Object[] array) {
	    	if (array.length <= 0) return "";

	    	StringBuilder commentBuilder = new StringBuilder();
    		for (Object value : array) {
    			commentBuilder.append(value).append(", ");
    		}
    		return commentBuilder.substring(0, commentBuilder.length() - 2).toString();
	    }
	}
}