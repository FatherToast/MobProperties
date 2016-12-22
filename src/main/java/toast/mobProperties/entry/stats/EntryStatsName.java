package toast.mobProperties.entry.stats;

import java.util.Random;

import net.minecraft.entity.EntityLiving;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemAxe;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemPickaxe;
import net.minecraft.item.ItemSpade;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.item.ItemTool;
import toast.mobProperties.ModMobProperties;
import toast.mobProperties.entry.EntryAbstract;
import toast.mobProperties.entry.IPropertyReader;
import toast.mobProperties.event.ItemStatsInfo;
import toast.mobProperties.event.MobStatsInfo;
import toast.mobProperties.util.FileHelper;

import com.google.gson.JsonObject;

public class EntryStatsName extends EntryAbstract {
    /// The name to set.
    private final String name;

    public EntryStatsName(String path, JsonObject root, int index, JsonObject node, IPropertyReader loader) {
        super(node, path);
        this.name = FileHelper.readText(node, path, "value", "");
    }

    /// Returns an array of required field names.
    @Override
    public String[] getRequiredFields() {
        return new String[] { "value" };
    }

    /// Returns an array of optional field names.
    @Override
    public String[] getOptionalFields() {
        return new String[] { };
    }

    /// Initializes the entity's stats.
    @Override
    public void init(MobStatsInfo mobStats) {
        if (mobStats.theEntity instanceof EntityLiving) {
            String[] nameParts = this.name.split(Character.toString(FileHelper.CHAR_RAND), -1);
            if (nameParts.length == 1) {
                ((EntityLiving) mobStats.theEntity).setCustomNameTag(this.name);
            }
            else {
                String mobName = nameParts[0];
                for (int i = 1; i < nameParts.length; i++) {
                    if (nameParts[i].length() > 0) {
                        char nextLetter = nameParts[i].charAt(0);
                        if (nextLetter == 'c') {
                            mobName += EntryStatsName.getEntityName(mobStats.random);
                        }
                        else if (nextLetter == 'C') {
                            mobName += EntryStatsName.getEntityName(mobStats.random); // TO DO
                        }
                        else if (nextLetter == 't') {
                            mobName += EntryStatsName.SALUTATIONS[mobStats.random.nextInt(EntryStatsName.SALUTATIONS.length)];
                        }
                        else if (nextLetter == 'p') {
                            mobName += EntryStatsName.getPrename(mobStats.random);
                        }
                        else if (nextLetter == 's') {
                            mobName += EntryStatsName.getSurname(mobStats.random);
                        }
                        else if (nextLetter == 'S') {
                            mobName += EntryStatsName.getSurname(mobStats.random); // TO DO
                        }
                        else if (nextLetter == 'd') {
                            mobName += EntryStatsName.DESCRIPTORS[mobStats.random.nextInt(EntryStatsName.DESCRIPTORS.length)];
                        }
                        else if (nextLetter == 'D') {
                            mobName += EntryStatsName.DESCRIPTORS[mobStats.random.nextInt(EntryStatsName.DESCRIPTORS.length)]; // TO DO
                        }
                        else if (nextLetter == 'm') {
                            mobName += EntryStatsName.buildName(mobStats.random);
                        }
                        else if (nextLetter == 'n') {
                            mobName += EntryStatsName.NAMES[mobStats.random.nextInt(EntryStatsName.NAMES.length)];
                        }
                        else {
                            mobName += EntryStatsName.getEntityName(mobStats.random);
                        }
                        mobName += nameParts[i].substring(1);
                    }
                    else {
                        mobName += EntryStatsName.getEntityName(mobStats.random);
                    }
                }
                ((EntityLiving) mobStats.theEntity).setCustomNameTag(mobName);
            }
        }
    }

    /// Modifies the item.
    @Override
    public void modifyItem(ItemStatsInfo itemStats) {
        String[] nameParts = this.name.split(Character.toString(FileHelper.CHAR_RAND), -1);
        if (nameParts.length == 1) {
            itemStats.theItem.setStackDisplayName(this.name);
        }
        else {
            String itemName = nameParts[0];
            for (int i = 1; i < nameParts.length; i++) {
                if (nameParts[i].length() > 0) {
                    char nextLetter = nameParts[i].charAt(0);
                    if (nextLetter == 'c') {
                        itemName += EntryStatsName.getItemName(itemStats.theItem, itemStats.random);
                    }
                    else if (nextLetter == 'C') {
                        itemName += EntryStatsName.getItemName(itemStats.theItem, itemStats.random); // TO DO
                    }
                    else if (nextLetter == 'i') {
                        itemName += EntryStatsName.getItemInfo(itemStats.theItem, itemStats.random);
                    }
                    else if (nextLetter == 'p') {
                        itemName += EntryStatsName.ITEM_PREFIXES[itemStats.random.nextInt(EntryStatsName.ITEM_PREFIXES.length)];
                    }
                    else if (nextLetter == 'P') {
                        itemName += EntryStatsName.ITEM_PREFIXES[itemStats.random.nextInt(EntryStatsName.ITEM_PREFIXES.length)]; // TO DO
                    }
                    else if (nextLetter == 's') {
                        itemName += EntryStatsName.ITEM_POSTFIXES[itemStats.random.nextInt(EntryStatsName.ITEM_POSTFIXES.length)];
                    }
                    else if (nextLetter == 'S') {
                        itemName += EntryStatsName.ITEM_POSTFIXES[itemStats.random.nextInt(EntryStatsName.ITEM_POSTFIXES.length)]; // TO DO
                    }
                    else if (nextLetter == 'm') {
                        itemName += EntryStatsName.buildName(itemStats.random);
                    }
                    else if (nextLetter == 'n') {
                        itemName += EntryStatsName.NAMES[itemStats.random.nextInt(EntryStatsName.NAMES.length)];
                    }
                    else {
                        itemName += EntryStatsName.getItemName(itemStats.theItem, itemStats.random);
                    }
                    itemName += nameParts[i].substring(1);
                }
                else {
                    itemName += EntryStatsName.getItemName(itemStats.theItem, itemStats.random);
                }
            }
            itemStats.theItem.setStackDisplayName(itemName);
        }
    }

    /// Returns a mash name.
    public static String buildName(Random random) {
        String name = EntryStatsName.NAME_PARTS[random.nextInt(EntryStatsName.NAME_PARTS.length)] + EntryStatsName.NAME_PARTS[random.nextInt(EntryStatsName.NAME_PARTS.length)].toLowerCase();
        if (random.nextInt(2) == 0) {
            name += EntryStatsName.NAME_PARTS[random.nextInt(EntryStatsName.NAME_PARTS.length)].toLowerCase();
        }
        return name;
    }

    /// Generates a full random name.
    public static String getEntityName(Random random) {
        String name = "";
        if (random.nextInt(4) != 0) {
            name = EntryStatsName.getPrename(random);
            if (random.nextInt(4) != 0) {
                name = EntryStatsName.getSurname(random);
            }
        }
        else if (random.nextInt(4) != 0) {
            name = EntryStatsName.getSurnameNoDescriptors(random);
        }
        else if (random.nextInt(200) != 0) {
            name = EntryStatsName.getPrename(random);
        }
        else {
            name = "\00a7lThe Almighty Lord of the Chickens";
        }
        if (random.nextInt(5) == 0) {
            name = EntryStatsName.SALUTATIONS[random.nextInt(EntryStatsName.SALUTATIONS.length)] + " " + name;
        }
        return name;
    }

    /// Generates a random first name.
    public static String getPrename(Random random) {
        return random.nextInt(2) == 0 ? EntryStatsName.NAMES[random.nextInt(EntryStatsName.NAMES.length)] : EntryStatsName.buildName(random);
    }

    /// Generates a random last name.
    public static String getSurname(Random random) {
        return random.nextInt(2) == 0 ? EntryStatsName.getSurnameNoDescriptors(random) : "the " + EntryStatsName.DESCRIPTORS[random.nextInt(EntryStatsName.DESCRIPTORS.length)];
    }

    /// Generates a random last name.
    public static String getSurnameNoDescriptors(Random random) {
        String name = "";
        if (random.nextInt(10) == 0) {
            if (random.nextInt(2) == 0) {
                name += "Mac";
            }
            else {
                name += "Mc";
            }
        }
        return name + EntryStatsName.buildName(random);
    }

    /// Generates a full random item name.
    public static String getItemName(ItemStack itemStack, Random random) {
        String name = "";
        boolean prefixed = false;
        if (random.nextInt(2) == 0) {
            prefixed = true;
            name += EntryStatsName.ITEM_PREFIXES[random.nextInt(EntryStatsName.ITEM_PREFIXES.length)] + " ";
        }

        name += EntryStatsName.getItemInfo(itemStack, random);

        if (!prefixed) {
            name += " of " + EntryStatsName.ITEM_POSTFIXES[random.nextInt(EntryStatsName.ITEM_POSTFIXES.length)];
        }
        return name;
    }

    /// Generates a random item name.
    public static String getItemInfo(ItemStack itemStack, Random random) {
        String name = "";

        String material = null;
        if (itemStack.getItem() instanceof ItemSword) {
            material = ((ItemSword) itemStack.getItem()).getToolMaterialName();
        }
        else if (itemStack.getItem() instanceof ItemTool) {
            material = ((ItemTool) itemStack.getItem()).getToolMaterialName();
        }
        if (material != null) {
            String[][] materials = {
            		{ "Wooden", "Wood", "Hardwood", "Balsa", "Mahogany", "Linden" },
            		{ "Stone", "Rock", "Lithic", "Marble", "Granite", "Cobblestone", "Basalt", "Diorite" },
            		{ "Iron", "Steel", "Metal", "Metallic", "Rusty", "Wrought Iron" },
            		{ "Diamond", "Zircon", "Gemstone", "Jewel", "Crystal" },
            		{ "Golden", "Gold", "Gilt", "Auric", "Ornate", "Gilded" }
        		};
            int index = -1;
            if (material.equals(Item.ToolMaterial.WOOD.toString())) {
                index = 0;
            }
            else if (material.equals(Item.ToolMaterial.STONE.toString())) {
                index = 1;
            }
            else if (material.equals(Item.ToolMaterial.IRON.toString())) {
                index = 2;
            }
            else if (material.equals(Item.ToolMaterial.DIAMOND.toString())) {
                index = 3;
            }
            else if (material.equals(Item.ToolMaterial.GOLD.toString())) {
                index = 4;
            }
            if (index < 0) {
                name += ModMobProperties.cap(material.toLowerCase()) + " ";
            }
            else {
                name += materials[index][random.nextInt(materials[index].length)] + " ";
            }

            String[] type = { "Tool" };
            if (itemStack.getItem() instanceof ItemSword) {
                type = new String[] { "Sword", "Cutter", "Slicer", "Dicer", "Knife", "Blade", "Machete", "Brand", "Claymore", "Cutlass", "Foil", "Dagger", "Glaive", "Rapier", "Saber", "Scimitar", "Shortsword", "Longsword", "Broadsword", "Calibur" };
            }
            else if (itemStack.getItem() instanceof ItemAxe) {
                type = new String[] { "Axe", "Chopper", "Hatchet", "Tomahawk", "Cleaver", "Hacker", "Tree-Cutter", "Truncator" };
            }
            else if (itemStack.getItem() instanceof ItemPickaxe) {
                type = new String[] { "Pickaxe", "Pick", "Mattock", "Rock-Smasher", "Miner" };
            }
            else if (itemStack.getItem() instanceof ItemSpade) {
                type = new String[] { "Shovel", "Spade", "Digger", "Excavator", "Trowel", "Scoop" };
            }
            name += type[random.nextInt(type.length)];
        }
        else if (itemStack.getItem() instanceof ItemBow) {
            String[] type = { "Bow", "Shortbow", "Longbow", "Flatbow", "Recurve Bow", "Reflex Bow", "Self Bow", "Composite Bow", "Arrow-Flinger" };
            name += type[random.nextInt(type.length)];
        }
        else if (itemStack.getItem() instanceof ItemArmor) {
            String[][] materials = {
            		{ "Leather", "Rawhide", "Lamellar", "Cow Skin" },
            		{ "Chainmail", "Chain", "Chain Link", "Scale" },
            		{ "Iron", "Steel", "Metal", "Rusty", "Wrought Iron" },
            		{ "Diamond", "Zircon", "Gemstone", "Jewel", "Crystal" },
            		{ "Golden", "Gold", "Gilt", "Auric", "Ornate", "Gilded" }
        		};
            material = ((ItemArmor) itemStack.getItem()).getArmorMaterial().toString();
            int index = -1;
            if (material.equals(ItemArmor.ArmorMaterial.LEATHER.toString())) {
                index = 0;
            }
            else if (material.equals(ItemArmor.ArmorMaterial.CHAIN.toString())) {
                index = 1;
            }
            else if (material.equals(ItemArmor.ArmorMaterial.IRON.toString())) {
                index = 2;
            }
            else if (material.equals(ItemArmor.ArmorMaterial.DIAMOND.toString())) {
                index = 3;
            }
            else if (material.equals(ItemArmor.ArmorMaterial.GOLD.toString())) {
                index = 4;
            }
            if (index < 0) {
                name += ModMobProperties.cap(material.toLowerCase()) + " ";
            }
            else {
                name += materials[index][random.nextInt(materials[index].length)] + " ";
            }

            String[] type = { "Armor" };
            switch ( ((ItemArmor) itemStack.getItem()).armorType) {
                case HEAD:
                    type = new String[] { "Helmet", "Cap", "Crown", "Great Helm", "Bassinet", "Sallet", "Close Helm", "Barbute" };
                    break;
                case CHEST:
                    type = new String[] { "Chestplate", "Tunic", "Brigandine", "Hauberk", "Cuirass" };
                    break;
                case LEGS:
                    type = new String[] { "Leggings", "Pants", "Tassets", "Cuisses", "Schynbalds" };
                    break;
                case FEET:
                    type = new String[] { "Boots", "Shoes", "Greaves", "Sabatons", "Sollerets" };
                    break;
				default:
            }
            name += type[random.nextInt(type.length)];
        }
        else {
            name += itemStack.getItem().getItemStackDisplayName(itemStack);
        }

        return name;
    }

    /// List of all possible item prefixes.
    public static final String[] ITEM_PREFIXES = { "Mighty", "Supreme", "Superior", "Ultimate", "Shoddy", "Flimsy", "Curious", "Secret", "Pathetic", "Crying", "Eagle's", "Errant", "Unholy", "Questionable", "Mean", "Hungry", "Thirsty", "Feeble", "Wise", "Sage's", "Magical", "Mythical", "Legendary", "Not Very Nice", "Jerk's", "Doctor's", "Misunderstood", "Angry", "Knight's", "Bishop's", "Godly", "Special", "Toasty", "Shiny", "Shimmering", "Light", "Dark", "Odd-Smelling", "Funky", "Slightly Overdone", "Half-Baked", "Cracked", "Sticky", "\u00a7kAlien", "Baby", "Manly", "Rough", "Scary", "Undoubtable", "Honest", "Non-Suspicious", "Boring", "Odd", "Lazy", "Super", "Nifty", "Ogre-Slaying" };
    /// List of all possible item postfixes.
    public static final String[] ITEM_POSTFIXES = { "Mightiness", "Supremity", "Superiority", "Flimsiness", "Curiousity", "Secrets", "Patheticness", "Crying", "The Eagles", "Unholiness", "Questionable Integrity", "Meanness", "Hunger", "Thirst", "Wisdom", "The Sages", "Magic", "Myths", "Legends", "The Jerks", "The Doctor", "Misunderstanding", "Anger", "The Gods", "Toast", "Shininess", "Shimmering", "The Light", "Darkness", "Strange Odors", "Funk", "Slight Abnormality", "Baking", "Breaking", "Stickiness", "Babies", "Manliness", "Roughness", "Scary Stuff", "Doubt", "Honesty", "Nothing", "Boringness", "Oddness", "Laziness", "Super Something", "Nifty Things", "Ogre-Slaying" };

    /// List of all possible full names.
    public static final String[] NAMES = { "Albert", "Andrew", "Anderson", "Andy", "Allan", "Arthur", "Aaron", "Allison", "Arielle", "Amanda", "Anne", "Annie", "Amy", "Alana", "Brandon", "Brady", "Bernard", "Ben", "Benjamin", "Bob", "Bobette", "Brooke", "Brandy", "Beatrice", "Bea", "Bella", "Becky", "Carlton", "Carl", "Calvin", "Cameron", "Carson", "Chase", "Cassandra", "Cassie", "Cas", "Carol", "Carly", "Cherise", "Charlotte", "Cheryl", "Chasity", "Danny", "Drake", "Daniel", "Derrel", "David", "Dave", "Donovan", "Don", "Donald", "Drew", "Derrick", "Darla", "Donna", "Dora", "Danielle", "Edward", "Elliot", "Ed", "Edson", "Elton", "Eddison", "Earl", "Eric", "Ericson", "Eddie", "Ediovany", "Emma", "Elizabeth", "Eliza", "Esperanza", "Esper", "Esmeralda", "Emi", "Emily", "Elaine", "Fernando", "Ferdinand", "Fred", "Feddie", "Fredward", "Frank", "Franklin", "Felix", "Felicia", "Fran", "Greg", "Gregory", "George", "Gerald", "Gina", "Geraldine", "Gabby", "Hendrix", "Henry", "Hobbes", "Herbert", "Heath", "Henderson", "Helga", "Hera", "Helen", "Helena", "Hannah", "Ike", "Issac", "Israel", "Ismael", "Irlanda", "Isabelle", "Irene", "Irenia", "Jimmy", "Jim", "Justin", "Jacob", "Jake", "Jon", "Johnson", "Jonny", "Jonathan", "Josh", "Joshua", "Julian", "Jesus", "Jericho", "Jeb", "Jess", "Joan", "Jill", "Jillian", "Jessica", "Jennifer", "Jenny", "Jen", "Judy", "Kenneth", "Kenny", "Ken", "Keith", "Kevin", "Karen", "Kassandra", "Kassie", "Leonard", "Leo", "Leroy", "Lee", "Lenny", "Luke", "Lucas", "Liam", "Lorraine", "Latasha", "Lauren", "Laquisha", "Livia", "Lydia", "Lila", "Lilly", "Lillian", "Lilith", "Lana", "Mason", "Mike", "Mickey", "Mario", "Manny", "Mark", "Marcus", "Martin", "Marty", "Matthew", "Matt", "Max", "Maximillian", "Marth", "Mia", "Marriah", "Maddison", "Maddie", "Marissa", "Miranda", "Mary", "Martha", "Melonie", "Melody", "Mel", "Minnie", "Nathan", "Nathaniel", "Nate", "Ned", "Nick", "Norman", "Nicholas", "Natasha", "Nicki", "Nora", "Nelly", "Nina", "Orville", "Oliver", "Orlando", "Owen", "Olsen", "Odin", "Olaf", "Ortega", "Olivia", "Patrick", "Pat", "Paul", "Perry", "Pinnochio", "Patrice", "Patricia", "Pennie", "Petunia", "Patti", "Pernelle", "Quade", "Quincy", "Quentin", "Quinn", "Roberto", "Robbie", "Rob", "Robert", "Roy", "Roland", "Ronald", "Richard", "Rick", "Ricky", "Rose", "Rosa", "Rhonda", "Rebecca", "Roberta", "Sparky", "Shiloh", "Stephen", "Steve", "Saul", "Sheen", "Shane", "Sean", "Sampson", "Samuel", "Sammy", "Stefan", "Sasha", "Sam", "Susan", "Suzy", "Shelby", "Samantha", "Sheila", "Sharon", "Sally", "Stephanie", "Sandra", "Sandy", "Sage", "Tim", "Thomas", "Thompson", "Tyson", "Tyler", "Tom", "Tyrone", "Timmothy", "Tamara", "Tabby", "Tabitha", "Tessa", "Tiara", "Tyra", "Uriel", "Ursala", "Uma", "Victor", "Vincent", "Vince", "Vance", "Vinny", "Velma", "Victoria", "Veronica", "Wilson", "Wally", "Wallace", "Will", "Wilard", "William", "Wilhelm", "Xavier", "Xandra", "Young", "Yvonne", "Yolanda", "Zach", "Zachary" };
    /// List of all name parts.
    public static final String[] NAME_PARTS = { "Grab", "Thar", "Ger", "Ald", "Mas", "On", "O", "Din", "Thor", "Jon", "Ath", "Burb", "En", "A", "E", "I", "U", "Hab", "Bloo", "Ena", "Dit", "Aph", "Ern", "Bor", "Dav", "Id", "Toast", "Son", "Dottir", "For", "Wen", "Lob", "Ed", "Die", "Van", "Y", "Zap", "Ear", "Ben", "Don", "Bran", "Gro", "Jen", "Bob", "Ette", "Ere", "Man", "Qua", "Bro", "Cree", "Per", "Skel", "Ton", "Zom", "Bie", "Wolf", "End", "Er", "Pig", "Sil", "Ver", "Fish", "Cow", "Chic", "Ken", "Sheep", "Squid", "Hell" };
    /// List of salutations.
    public static final String[] SALUTATIONS = { "Mr.", "Mister", "Sir", "Mrs.", "Miss", "Madam", "Dr.", "Doctor", "Lord", "Father", "Grandfather", "Mother", "Grandmother" };
    /// List of all mob descriptors.
    public static final String[] DESCRIPTORS = { "Mighty", "Supreme", "Superior", "Ultimate", "Lame", "Wimpy", "Curious", "Sneaky", "Pathetic", "Crying", "Eagle", "Errant", "Unholy", "Questionable", "Mean", "Hungry", "Thirsty", "Feeble", "Wise", "Sage", "Magical", "Mythical", "Legendary", "Not Very Nice", "Jerk", "Doctor", "Misunderstood", "Angry", "Knight", "Bishop", "Godly", "Special", "Toasty", "Shiny", "Shimmering", "Light", "Dark", "Odd-Smelling", "Funky", "Rock Smasher", "Son of Herobrine", "Cracked", "Sticky", "\u00a7kAlien", "Baby", "Manly", "Rough", "Scary", "Undoubtable", "Honest", "Non-Suspicious", "Boring", "Odd", "Lazy", "Super", "Nifty", "Ogre Slayer", "Pig Thief", "Dirt Digger", "Really Cool", "Doominator", "... Something" };
}