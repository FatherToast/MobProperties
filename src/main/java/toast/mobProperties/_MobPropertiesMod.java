package toast.mobProperties;

import java.util.Random;

import net.minecraft.command.ServerCommandManager;
import net.minecraftforge.common.config.Configuration;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;

@Mod(modid = _MobPropertiesMod.MODID, name = "Mob Properties", version = _MobPropertiesMod.VERSION)
public class _MobPropertiesMod {
    /* TO DO *\
        > improvements
            > allow "lore" to accept string arrays
            > allow number ranges to accept number arrays
            > "Riding" tag needs to be implemented
            > stripped-down stats for nonliving entities for use in "spawn" function?
        > more conditions
            ?
        > expression evaluation engine
            > looting modifier *
            > nbt-defined variables
    \* ** ** */
    // This mod's id.
    public static final String MODID = "MobProperties";
    // This mod's version.
    public static final String VERSION = "1.0.2";

    // If true, this mod starts up in debug mode.
    public static final boolean debug = false;
    // The mod's random number generator.
    public static final Random random = new Random();

    // Called before initialization. Loads the properties/configurations.
    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        _MobPropertiesMod.debugConsole("Loading in debug mode!");
        Properties.init(new Configuration(event.getSuggestedConfigurationFile()));
        FileHelper.init(event.getModConfigurationDirectory());
    }

    // Called during initialization. Registers entities, mob spawns, and renderers.
    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        new TickHandler();
        new EventHandler();
    }

    // Called after initialization. Used to check for dependencies.
    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        _MobPropertiesMod.console("Loading mob properties...");
        _MobPropertiesMod.console("Loaded " + FileHelper.load() + " mob properties!");
        if (!EventHandler.DISABLED && Properties.getBoolean(Properties.GENERAL, "auto_generate_files")) {
            _MobPropertiesMod.console("Generating default mob properties...");
            _MobPropertiesMod.console("Generated " + FileHelper.generateDefaults() + " mob properties!");
        }
    }

    // Called as the server is starting.
    @Mod.EventHandler
    public void serverStarting(FMLServerStartingEvent event) {
        ServerCommandManager commandManager = (ServerCommandManager) event.getServer().getCommandManager();
        commandManager.registerCommand(new CommandReload());
        commandManager.registerCommand(new CommandInfoWand());
    }

    // Makes the first letter upper case.
    public static String cap(String string) {
        int length = string.length();
        if (length <= 0)
            return "";
        if (length == 1)
            return string.toUpperCase();
        return Character.toString(Character.toUpperCase(string.charAt(0))) + string.substring(1);
    }

    // Makes the first letter lower case.
    public static String decap(String string) {
        int length = string.length();
        if (length <= 0)
            return "";
        if (length == 1)
            return string.toLowerCase();
        return Character.toString(Character.toLowerCase(string.charAt(0))) + string.substring(1);
    }

    // Prints the message to the console with this mod's name tag.
    public static void console(String message) {
        System.out.println("[" + _MobPropertiesMod.MODID + "] " + message);
    }

    // Prints the message to the console with this mod's name tag if debugging is enabled.
    public static void debugConsole(String message) {
        if (_MobPropertiesMod.debug) {
            System.out.println("[" + _MobPropertiesMod.MODID + "] (debug) " + message);
        }
    }

    // Throws a runtime exception with a message and this mod's name tag if debugging is enabled.
    public static void debugException(String message) {
        if (_MobPropertiesMod.debug)
            throw new RuntimeException("[" + _MobPropertiesMod.MODID + "] " + message);
		_MobPropertiesMod.console("[ERROR] " + message);
    }
}