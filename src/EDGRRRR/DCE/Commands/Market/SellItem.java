package edgrrrr.dce.commands.market;

import edgrrrr.configapi.Setting;
import edgrrrr.dce.DCEPlugin;
import edgrrrr.dce.help.Help;
import edgrrrr.dce.materials.MaterialData;
import edgrrrr.dce.math.Math;
import edgrrrr.dce.player.PlayerInventoryManager;
import edgrrrr.dce.response.ValueResponse;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * A command for selling items to the market
 */
public class SellItem implements CommandExecutor {
    private final DCEPlugin app;
    private final Help help;

    public SellItem(DCEPlugin app) {
        this.app = app;
        this.help = this.app.getHelpManager().get("sell");
    }


    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            return true;
        }

        Player player = (Player) sender;

        // Ensure command is enabled
        if (!(this.app.getConfig().getBoolean(Setting.COMMAND_SELL_ITEM_ENABLE_BOOLEAN.path))) {
            this.app.getConsole().severe(player, "This command is not enabled.");
            return true;
        }

        // Ensure market is enabled
        if (!(this.app.getConfig().getBoolean(Setting.MARKET_MATERIALS_ENABLE_BOOLEAN.path))) {
            this.app.getConsole().severe(player, "The market is not enabled.");
            return true;
        }

        String materialName;
        int amountToSell = 1;

        switch (args.length) {
            // Just material, used default amount of 1
            case 1:
                materialName = args[0];
                break;

            // Material & Amount
            case 2:
                materialName = args[0];
                amountToSell = Math.getInt(args[1]);
                break;

            default:
                this.app.getConsole().usage(player, "Invalid number of arguments.", this.help.getUsages());
                return true;
        }

        if (amountToSell < 1) {
            this.app.getConsole().usage(player, "Invalid amount.", this.help.getUsages());
            this.app.getConsole().debug("(SellItem)Invalid item amount: " + materialName);

        } else {
            MaterialData materialData = this.app.getMaterialManager().getMaterial(materialName);
            if (materialData == null) {
                this.app.getConsole().usage(player, "Unknown Item: '" + materialName + "'", this.help.getUsages());
                this.app.getConsole().debug("(SellItem)Unknown item search: " + materialName);

            } else {
                Material material = materialData.getMaterial();
                ItemStack[] totalUserMaterials = PlayerInventoryManager.getMaterialSlots(player, material);
                int userAmount = PlayerInventoryManager.getMaterialCount(totalUserMaterials);

                ItemStack[] itemStacks = PlayerInventoryManager.getMaterialSlotsToCount(player, material, amountToSell);
                ValueResponse valueResponse = this.app.getMaterialManager().getSellValue(itemStacks);

                if (valueResponse.isFailure()) {
                    this.app.getConsole().logFailedSale(player, amountToSell, materialData.getCleanName(), valueResponse.errorMessage);

                } else {
                    if (userAmount >= amountToSell) {
                        PlayerInventoryManager.removeMaterialsFromPlayer(itemStacks);
                        materialData.addQuantity(amountToSell);
                        if (!this.app.getEconomyManager().addCash(player, valueResponse.value).transactionSuccess()) {this.app.getConsole().severe(player,"An error occurred on funding your account, show this message to an admin.");}

                        this.app.getConsole().logSale(player, amountToSell, valueResponse.value, materialData.getCleanName());
                    } else {
                        this.app.getConsole().logFailedSale(player, amountToSell, materialData.getCleanName(), String.format("you do not have enough of this material. (missing %d)", amountToSell - userAmount));
                    }
                }
            }
        }

        return true;
    }
}
