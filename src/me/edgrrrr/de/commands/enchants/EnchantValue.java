package me.edgrrrr.de.commands.enchants;

import me.edgrrrr.de.DEPlugin;
import me.edgrrrr.de.commands.DivinityCommandEnchant;
import me.edgrrrr.de.config.Setting;
import me.edgrrrr.de.enchants.EnchantData;
import me.edgrrrr.de.math.Math;
import me.edgrrrr.de.response.ValueResponse;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * A command for valuing enchants
 */
public class EnchantValue extends DivinityCommandEnchant {

    /**
     * Constructor
     *
     * @param app
     */
    public EnchantValue(DEPlugin app) {
        super(app, "evalue", true, Setting.COMMAND_E_VALUE_ENABLE_BOOLEAN);
    }

    /**
     * For handling a player calling this command
     *
     * @param sender
     * @param args
     * @return
     */
    @Override
    public boolean onPlayerCommand(Player sender, String[] args) {
        // The name of the enchant
        // The number of levels to sell
        // If all levels should be sold
        String enchantName= "";
        int enchantLevels = 1;

        switch (args.length) {
            // If user enters the name
            // sell maximum of enchant given
            case 1:
                enchantName = args[0];
                break;

            // If user enters name and level
            // Sell enchant level times
            case 2:
                enchantName = args[0];
                enchantLevels = Math.getInt(args[1]);
                break;

            // If wrong number of arguments
            default:
                this.getMain().getConsole().usage(sender, CommandResponse.InvalidNumberOfArguments.message, this.help.getUsages());
                return true;
        }

        // If only handling one enchant
        // Ensure enchant exists
        EnchantData enchantData = this.getMain().getEnchantmentManager().getEnchant(enchantName);
        if (enchantData == null) {
            this.getMain().getConsole().usage(sender, String.format(CommandResponse.InvalidEnchantName.message, enchantName), this.help.getUsages());
            return true;
        }

        ItemStack itemStack = new ItemStack(Material.DIAMOND_SWORD);

        // Get value
        // Remove enchants, add quantity and add cash
        ValueResponse valueResponse1 = this.getMain().getEnchantmentManager().getBuyValue(itemStack, enchantName, enchantLevels);
        itemStack.addUnsafeEnchantment(enchantData.getEnchantment(), enchantLevels);
        ValueResponse valueResponse2 = this.getMain().getEnchantmentManager().getSellValue(itemStack, enchantName, enchantLevels);
        if (valueResponse1.isFailure()) {
            this.getMain().getConsole().warn(sender, "Couldn't determine buy value of %d %s because %s", enchantLevels, enchantName, valueResponse1.errorMessage);
        } else {
            this.getMain().getConsole().info(sender, "Buy: %d %s costs £%,.2f", enchantLevels, enchantName, valueResponse1.value);
        }
        if (valueResponse2.isFailure()) {
            this.getMain().getConsole().warn(sender, "Couldn't determine sell value of %d %s because %s", enchantLevels, enchantName, valueResponse2.errorMessage);
        } else {
            this.getMain().getConsole().info(sender, "Sell: %d %s costs £%,.2f", enchantLevels, enchantName, valueResponse2.value);
        }

        // Graceful exit :)
        return true;
    }

    /**
     * For the handling of the console calling this command
     *
     * @param args
     * @return
     */
    @Override
    public boolean onConsoleCommand(String[] args) {
        return this.onPlayerCommand(null, args);
    }
}
