package me.edgrrrr.de.materials;

import me.edgrrrr.de.DEPlugin;
import me.edgrrrr.de.DivinityModule;
import me.edgrrrr.de.config.Setting;
import me.edgrrrr.de.math.Math;
import me.edgrrrr.de.response.MultiValueResponse;
import me.edgrrrr.de.response.ValueResponse;
import net.milkbowl.vault.economy.EconomyResponse.ResponseType;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class MaterialManager extends DivinityModule {
    // Save time scheduler
    private BukkitRunnable saveTimer;
    // Stores the default items.json file location
    private final String materialsFile = "materials.yml";
    private final String aliasesFile = "aliases.yml";
    // Stores the materials and the aliases
    private HashMap<String, String> aliases;
    private HashMap<String, MaterialData> materials;
    // Used for calculating inflation/deflation
    private int totalMaterials;
    private int defaultTotalMaterials;
    // Other settings
    private double buyScale;
    private double sellScale;
    private double baseQuantity;
    private boolean itemDmgScaling;
    private boolean dynamicPricing;
    private boolean wholeMarketInflation;
    // Stores items
    private FileConfiguration config;

    /**
     * Constructor You will likely need to call loadMaterials and loadAliases to
     * populate the aliases and materials with data from the program
     *
     * @param main - The plugin
     */
    public MaterialManager(DEPlugin main) {
        super(main);
    }

    /**
     * Initialisation of the object
     */
    @Override
    public void init() {
        this.buyScale = this.getConfig().getDouble(Setting.MARKET_MATERIALS_BUY_TAX_FLOAT);
        this.sellScale = this.getConfig().getDouble(Setting.MARKET_MATERIALS_SELL_TAX_FLOAT);
        this.baseQuantity = this.getConfig().getInt(Setting.MARKET_MATERIALS_BASE_QUANTITY_INTEGER);
        this.itemDmgScaling = this.getConfig().getBoolean(Setting.MARKET_MATERIALS_ITEM_DMG_SCALING_BOOLEAN);
        this.dynamicPricing = this.getConfig().getBoolean(Setting.MARKET_MATERIALS_DYN_PRICING_BOOLEAN);
        this.wholeMarketInflation = this.getConfig().getBoolean(Setting.MARKET_MATERIALS_WHOLE_MARKET_INF_BOOLEAN);
        int timer = Math.getTicks(this.getConfig().getInt(Setting.MARKET_SAVE_TIMER_INTEGER));
        this.saveTimer = new BukkitRunnable() {
            @Override
            public void run() {
                saveMaterials();
            }
        };
        this.saveTimer.runTaskTimer(this.getMain(), timer, timer);
        this.loadMaterials();
        this.loadAliases();
    }

    /**
     * Shutdown of the object
     */
    @Override
    public void deinit() {
        this.saveTimer.cancel();
        this.saveMaterials();
    }

    /**
     * Returns the /buy price scaling
     */
    public double getBuyScale() {
        return this.buyScale;
    }

    /**
     * Returns the /sell price scaling
     */
    public double getSellScale() {
        return this.sellScale;
    }

    /**
     * Returns a material from the materialData HashMap Will be none if no alias or
     * direct name is found.
     *
     * @param alias - The alias or name of the material to get.
     * @return MaterialData - Returns the material data corresponding to the string supplied.
     */
    public MaterialData getMaterial(String alias) {
        // Check aliases
        // If alias is empty then get directly from materials list
        // get the material and return it, could be Null
        String matID = this.aliases.get(alias);
        if (matID == null)
            matID = alias;
        return this.materials.get(matID);
    }

    /**
     * Returns a material from the materialData HashMap, Will be none if no direct name is found.
     *
     * @param exactID - The alias or name of the material to get.
     * @return MaterialData - Returns the material data corresponding to the string supplied.
     */
    public MaterialData getMaterialID(String exactID) {
        // Check aliases
        // If alias is empty then get directly from materials list
        // get the material and return it, could be Null
        return this.materials.get(exactID);
    }

    /**
     * Returns all the material names
     * @return String[]
     */
    public String[] getMaterialNames() {
        ArrayList<String> materialNames = new ArrayList<>();
        for (MaterialData materialData : this.materials.values()) {
            materialNames.add(materialData.getCleanName().toLowerCase().replace(" ", ""));
        }
        for (String alias : this.aliases.keySet()) {
            materialNames.add(alias.toLowerCase().replace(" ", ""));
        }

        return materialNames.toArray(new String[0]);
    }

    /**
     * Returns all the material names that start with startsWith
     * @param startsWith
     * @return String[]
     */
    public String[] getMaterialNames(String startsWith) {
        ArrayList<String> materialNames = new ArrayList<>();
        for (String materialName : this.getMaterialNames()) {
            if (materialName.toLowerCase().startsWith(startsWith.toLowerCase())) {
                materialNames.add(materialName);
            }
        }

        return materialNames.toArray(new String[0]);
    }

    /**
     * Returns all the material aliases that belong to the material ID given
     * @param materialID
     * @return String[]
     */
    public String[] getMaterialAliases(String materialID) {
        ArrayList<String> aliases = new ArrayList<>();
        for (String alias : this.aliases.keySet()) {
            if (this.aliases.get(alias).equalsIgnoreCase(materialID)) {
                aliases.add(alias);
            }
        }
        return aliases.toArray(new String[0]);
    }

    /**
     * Returns all the material aliases that belong to the material id's given, that start with startsWith
     * @param materialIDs
     * @param startsWith
     * @return String[]
     */
    public String[] getMaterialAliases(String[] materialIDs, String startsWith) {
        String[] materialAliases = this.getMaterialAliases(materialIDs);
        ArrayList<String> newMaterialAliases = new ArrayList<>();
        for (String materialAlias : materialAliases) {
            if (materialAlias.toLowerCase().startsWith(startsWith.toLowerCase())) {
                newMaterialAliases.add(materialAlias);
            }
        }

        return newMaterialAliases.toArray(new String[0]);
    }

    /**
     * Returns all the material aliases that belong to the material id's given
     * @param materialIDs
     * @return
     */
    public String[] getMaterialAliases(String[] materialIDs) {
        ArrayList<String> materialAliases = new ArrayList<>();
        for (String materialID : materialIDs) {
            materialAliases.addAll(Arrays.asList(this.getMaterialAliases(materialID)));
        }

        return materialAliases.toArray(new String[0]);
    }

    public int getMaterialCount() {
        return this.materials.size();
    }

    public int getAliasCount() {
        return this.aliases.size();
    }

    /**
     * Returns the scaling of price for an item, based on its durability and damage.
     *
     * @param itemStack - The itemstack containing the material with the specified damage.
     * @return double - The level of price scaling to apply. For example .9 = 90% of full price. Maximum value is 1 for undamaged.
     */
    private static double getDamageValue(ItemStack itemStack) {
        // Instantiate damage value
        double damageValue = 1.0;

        // Get meta and cast to damageable, for getting the items durability
        // Get durability and max durability
        Damageable dmg = (Damageable) itemStack.getItemMeta();
        if (dmg == null) return damageValue;
        double durability = dmg.getDamage();
        double maxDurability = itemStack.getType().getMaxDurability();

        // If max durability > 0 - Meaning the item is damageable (aka a tool)
        // Adjust damage value to be the percentage of health left on the item.
        // 50% damaged = .5 scaling (50% of full price)
        // Durability is in the form of 1 = 1 damage (if item has 10 health, 1 durability = 9 health)
        // Hence maxDurability - durability / maxDurability
        if (maxDurability > 0) {
            damageValue = (maxDurability - durability) / maxDurability;
        }

        return damageValue;
    }

    private double getDamageScaling(ItemStack itemStack) {
        if (this.itemDmgScaling) {
            return MaterialManager.getDamageValue(itemStack);
        } else {
            return 1.0;
        }
    }

    /**
     * Returns the combined sell value of all the items given
     *
     * @param itemStacks - The items to calculate the price for
     * @return MaterialValue - The value of the items, or not if an error occurred.
     */
    public ValueResponse getSellValue(ItemStack[] itemStacks) {
        double value = 0.0;

        // Loop through items and add up the sell value of each item
        for (ItemStack itemStack : itemStacks) {
            ValueResponse mv = this.getSellValue(itemStack);
            if (mv.isSuccess()) {
                value += mv.value;
            } else {
                return new ValueResponse(0.0, mv.responseType, mv.errorMessage);
            }
        }

        return new ValueResponse(value, ResponseType.SUCCESS, "");
    }

    /**
     * Returns the sell value for a single stack of items.
     *
     * @param itemStack - The itemStack to get the value of
     * @return MaterialValue - The price of the itemstack if no errors occurred.
     */
    public ValueResponse getSellValue(ItemStack itemStack) {
        ValueResponse response;

        if (this.getEnchant().isEnchanted(itemStack)) {
            response = new ValueResponse(0.0, ResponseType.FAILURE, "item is enchanted.");

        } else {
            MaterialData materialData = this.getMaterial(itemStack.getType().name());

            if (materialData == null) {
                response = new ValueResponse(0.0, ResponseType.FAILURE, "item cannot be found.");
            } else {
                if (!materialData.getAllowed()) {
                    response = new ValueResponse(0.0, ResponseType.FAILURE, "item is banned.");
                } else {
                    double value = this.calculatePrice(itemStack.getAmount(), materialData.getQuantity(), (this.sellScale * this.getDamageScaling(itemStack)), false);
                    if (value > 0) {
                        response = new ValueResponse(value, ResponseType.SUCCESS, "");
                    } else {
                        response = new ValueResponse(0.0, ResponseType.FAILURE, "market is saturated.");
                    }
                }
            }
        }

        return response;
    }

    public MultiValueResponse getBulkSellValue(ItemStack[] itemStacks) {
        // Store values
        HashMap<String, Double> values = MultiValueResponse.createValues();
        // Store quantities
        HashMap<String, Integer> quantities = MultiValueResponse.createQuantities();
        // Error
        String error = "";
        // Error type
        ResponseType response = ResponseType.SUCCESS;

        // Loop through stacks
        for (ItemStack itemStack : itemStacks) {
            // Get this stack value
            ValueResponse valueResponse = this.getSellValue(itemStack);

            // If valuation succeeded
            if (valueResponse.isSuccess()) {
                // get material id
                MaterialData materialData = this.getMaterialID(itemStack.getType().name());
                Material material = materialData.getMaterial();
                // If values contains key, add together
                if (values.containsKey(material.name())) {
                    values.put(material.name(), valueResponse.value + values.remove(material.name()));
                    quantities.put(material.name(), itemStack.getAmount() + quantities.remove(material.name()));
                }
                // Else just add value response
                else {
                    values.put(material.name(), valueResponse.value);
                    quantities.put(material.name(), itemStack.getAmount());
                }
            } else {
                response = valueResponse.responseType;
                error = valueResponse.errorMessage;
                break;
            }
        }

        return new MultiValueResponse(values, quantities, response, error);
    }

    /**
     * Returns the price of buying the given items.
     *
     * @param itemStacks - The items to get the price for
     * @return MaterialValue
     */
    public ValueResponse getBuyValue(ItemStack[] itemStacks) {
        double value = 0.0;
        for (ItemStack itemStack : itemStacks) {
            ValueResponse mv = this.getBuyValue(itemStack);
            if (mv.isSuccess()) {
                value += mv.value;
            } else {
                return new ValueResponse(0.0, mv.responseType, mv.errorMessage);
            }
        }

        return new ValueResponse(value, ResponseType.SUCCESS, "");
    }

    /**
     * Returns the value of an itemstack
     *
     * @param itemStack - The item stack to get the value of
     * @return MaterialValue
     */
    public ValueResponse getBuyValue(ItemStack itemStack) {
        ValueResponse response;

        MaterialData materialData = this.getMaterial(itemStack.getType().name());
        if (materialData == null) {
            response = new ValueResponse(0.0, ResponseType.FAILURE, "item cannot be found.");
        } else {
            if (!materialData.getAllowed()) {
                response = new ValueResponse(0.0, ResponseType.FAILURE, "item cannot be bought or sold.");
            } else {
                double value = this.calculatePrice(itemStack.getAmount(), materialData.getQuantity(), this.buyScale, true);
                if (value > 0) {
                    response = new ValueResponse(value, ResponseType.SUCCESS, "");
                } else {
                    response = new ValueResponse(0.0, ResponseType.FAILURE, "market is saturated.");
                }
            }
        }

        return response;
    }

    /**
     * Returns the market price based on stock
     *
     * @param stock - The stock of the material
     * @return double
     */
    public double getMarketPrice(double stock) {
        return this.getPrice(stock, this.sellScale, this.getInflation());
    }

    /**
     * Returns the user price based on stock
     *
     * @param stock - The stock of the material
     * @return double
     */
    public double getUserPrice(double stock) {
        return this.getPrice(stock, this.buyScale, this.getInflation());
    }

    /**
     * Sets the price of a material
     * @param materialData - The material to set
     * @param value - The value to set the price to
     */
    public void setPrice(MaterialData materialData, double value) {
        materialData.setQuantity(this.calculateStock(value, this.buyScale, this.getInflation()));
    }

    /**
     * Edits the quantity of a material by quantity
     * @param materialData - The material to edit
     * @param quantity - The quantity to edit by. Can be negative.
     */
    public void editQuantity(MaterialData materialData, int quantity) {
        materialData.editQuantity(quantity);
        this.editTotalMaterials(quantity);
    }

    /**
     * Sets the quantity of material
     * @param materialData - The material to set.
     * @param quantity - The quantity to edit by. Can be negative.
     */
    public void setQuantity(MaterialData materialData, int quantity) {
        this.editQuantity(materialData, quantity - materialData.getQuantity());
    }

    /**
     * Calculates the price of a material * amount
     * This is not the same as price * amount -- Factors in price change and inflation change during purchase
     *
     * @param amount   - The amount to calculate the price for
     * @param stock    - The stock of the material
     * @param scale    - The scaling to apply, such as tax
     * @param purchase - Whether this is a purchase from or sale to the market
     * @return double
     */
    public double calculatePrice(double amount, double stock, double scale, boolean purchase) {
        return Math.calculatePrice(this.baseQuantity, stock, this.defaultTotalMaterials, this.totalMaterials, amount, scale, purchase, this.dynamicPricing, this.wholeMarketInflation);
    }

    /**
     * Returns the price for an item based on it's stock and the scale to apply
     * Scale of 1.2 = 20% additive Scale of .8 = 20% reduction
     *
     * @param stock     - The stock of the material
     * @param scale     - The scaling to apply to the price
     * @param inflation - The level of inflation
     * @return double - The price of the material
     */
    public double getPrice(double stock, double scale, double inflation) {
        if (!this.wholeMarketInflation) {
            inflation = 1.0;
        }
        return Math.getPrice(this.baseQuantity, stock, scale, inflation);
    }

    /**
     * Calculates the stock based on the price.
     *
     * @param price     - The price of the item
     * @param scale     - The scale of the price
     * @param inflation - The inflation of the price
     * @return int - The level of stock required for this price.
     */
    public int calculateStock(double price, double scale, double inflation) {
        return (int) ((this.baseQuantity / price) * scale * inflation);
    }

    /**
     * Gets the market-wide level of inflation
     *
     * @return double - The level of inflation
     */
    public double getInflation() {
        if (this.wholeMarketInflation) {
            return Math.getInflation(this.defaultTotalMaterials, this.totalMaterials);
        } else {
            return 1.0;
        }
    }

    /**
     * Adds or removes the amount from the stock
     * Used to track inflation
     *
     * @param amount - The amount to add or remove. Negative to remove.
     */
    private void editTotalMaterials(int amount) {
        this.totalMaterials += amount;
    }

    /**
     * Returns the total number of materials in the market
     * @return int
     */
    public int getTotalMaterials() {
        return totalMaterials;
    }

    /**
     * Returns the total default number of materials in the market
     * @return int
     */
    public int getDefaultTotalMaterials() {
        return defaultTotalMaterials;
    }

    /**
     * Loads aliases from the aliases file into the aliases variable
     */
    public void loadAliases() {
        FileConfiguration config = this.getConfig().loadFile(this.aliasesFile);
        HashMap<String, String> values = new HashMap<>();
        for (String key : config.getKeys(false)) {
            String value = config.getString(key);
            if (this.getMaterialID(value) == null) {
                if (!this.getConfig().getBoolean(Setting.IGNORE_ALIAS_ERRORS_BOOLEAN)) this.getConsole().warn("Bad config value in %s: '%s' - Corresponding value '%s' does not exist.", this.aliasesFile, key, value);
                continue;
            }
            values.put(key, value);
        }
        this.aliases = values;
        this.getConsole().info("Loaded %s aliases from %s", values.size(), this.aliasesFile);
    }

    /**
     * Loads the materials from the materials file into the materials variable
     */
    public void loadMaterials() {
        // Load the config
        this.config = this.getConfig().loadFile(this.materialsFile);
        FileConfiguration defaultConf = this.getConfig().readResource(this.materialsFile);
        // Set material counts
        this.defaultTotalMaterials = 0;
        this.totalMaterials = 0;
        // Create a HashMap to store the values
        HashMap<String, MaterialData> values = new HashMap<>();
        // Loop through keys and get data
        // Add data to a MaterialData and put in HashMap under key
        for (String key : this.config.getKeys(false)) {
            ConfigurationSection data = this.config.getConfigurationSection(key);
            ConfigurationSection defaultData = defaultConf.getConfigurationSection(key);
            if (data == null) {
                if (!this.getConfig().getBoolean(Setting.IGNORE_MATERIAL_ERRORS_BOOLEAN)) this.getConsole().warn("Bad config value in %s: '%s' - Data is null", this.materialsFile, key);
                continue;
            }

            MaterialData mData = new MaterialData(data, defaultData);
            if (mData.getMaterial() == null) {
                if (!this.getConfig().getBoolean(Setting.IGNORE_MATERIAL_ERRORS_BOOLEAN)) this.getConsole().warn("Bad config value in %s: '%s' - Material does not exist.", this.materialsFile, key);
                continue;
            }
            this.defaultTotalMaterials += mData.getDefaultQuantity();
            this.totalMaterials += mData.getQuantity();
            values.put(key, mData);
        }
        // Copy values into materials
        this.materials = values;
        this.getConsole().info("Loaded %s materials (current/default quantities: %s / %s) from %s", values.size(), this.totalMaterials, this.defaultTotalMaterials, this.materialsFile);
    }

    /**
     * Saves a value to a key
     *
     * @param key   - The map key
     * @param value - The map value
     */
    private void setData(String key, Object value) {
        this.config.set(key, value);
    }

    /**
     * Saves the material data to the config
     *
     * @param material - The material to save
     */
    private void saveMaterial(MaterialData material) {
        this.setData(material.getMaterialID(), material.getConfigData());
    }

    /**
     * Loops through the materials and saves their data to the config
     * Then saves the config to the config file
     */
    public void saveMaterials() {
        // Save materials
        for (MaterialData materialD : materials.values()) {
            this.saveMaterial(materialD);
        }

        // save
        this.saveFile();
        this.getConsole().info("Materials saved.");
    }

    /**
     * Saves the config to the config file
     */
    private void saveFile() {
        // load back all info
        FileConfiguration config = this.getConfig().loadFile(this.materialsFile);
        for (String key : config.getKeys(false)) {
            if (!this.materials.containsKey(key)) continue;

            config.set(key, this.config.get(key));
        }

        this.getConfig().saveFile(config, this.materialsFile);
    }
}
