package edgrrrr.de.commands.enchants;

import edgrrrr.configapi.Setting;
import edgrrrr.de.DEPlugin;
import edgrrrr.de.commands.DivinityCommandEnchant;
import edgrrrr.de.enchants.EnchantData;
import edgrrrr.de.math.Math;
import edgrrrr.de.response.ValueResponse;
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
                this.app.getConsole().usage(sender, CommandResponse.InvalidNumberOfArguments.message, this.help.getUsages());
                return true;
        }

        // If only handling one enchant
        // Ensure enchant exists
        EnchantData enchantData = this.app.getEnchantmentManager().getEnchant(enchantName);
        if (enchantData == null) {
            this.app.getConsole().usage(sender, String.format(CommandResponse.InvalidEnchantName.message, enchantName), this.help.getUsages());
            return true;
        }

        ItemStack itemStack = new ItemStack(Material.DIAMOND_SWORD);
        itemStack.addUnsafeEnchantment(enchantData.getEnchantment(), enchantLevels);

        // Get value
        // Remove enchants, add quantity and add cash
        ValueResponse valueResponse1 = this.app.getEnchantmentManager().getSellValue(itemStack, enchantName, enchantLevels);
        ValueResponse valueResponse2 = this.app.getEnchantmentManager().getBuyValue(enchantName, enchantLevels);
        if (valueResponse2.isFailure()) {
            this.app.getConsole().warn(sender, String.format("Couldn't determine buy value of &d %s because %s", enchantLevels, enchantName, valueResponse2.errorMessage));
        } else {
            this.app.getConsole().info(sender, String.format("Buy: %d %s costs £%,.2f", enchantLevels, enchantName, valueResponse2.value));
        }
        if (valueResponse1.isFailure()) {
            this.app.getConsole().warn(sender, String.format("Couldn't determine sell value of &d %s because %s", enchantLevels, enchantName, valueResponse1.errorMessage));
        } else {
            this.app.getConsole().info(sender, String.format("Sell: %d %s costs £%,.2f", enchantLevels, enchantName, valueResponse1.value));
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