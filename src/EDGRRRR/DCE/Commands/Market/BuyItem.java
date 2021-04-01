package edgrrrr.dce.commands.market;

import edgrrrr.configapi.Setting;
import edgrrrr.dce.DCEPlugin;
import edgrrrr.dce.help.Help;
import edgrrrr.dce.materials.MaterialData;
import edgrrrr.dce.math.Math;
import edgrrrr.dce.player.PlayerInventoryManager;
import edgrrrr.dce.response.ValueResponse;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * A command for buying items from the market
 */
public class BuyItem implements CommandExecutor {
    private final DCEPlugin app;
    private final Help help;

    public BuyItem(DCEPlugin app) {
        this.app = app;
        this.help = this.app.getHelpManager().get("buy");
    }


    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            return true;
        }

        Player player = (Player) sender;

        // Ensure command is enabled
        if (!(this.app.getConfig().getBoolean(Setting.COMMAND_BUY_ITEM_ENABLE_BOOLEAN.path))) {
            this.app.getConsole().severe(player, "This command is not enabled.");
            return true;
        }

        // Ensure market is enabled
        if (!(this.app.getConfig().getBoolean(Setting.MARKET_MATERIALS_ENABLE_BOOLEAN.path))) {
            this.app.getConsole().severe(player, "The market is not enabled.");
            return true;
        }

        String materialName;
        int amountToBuy = 1;
        switch (args.length) {
            // Just material, used default amount of 1
            case 1:
                materialName = args[0];
                break;

            // Material & Amount
            case 2:
                materialName = args[0];
                amountToBuy = Math.getInt(args[1]);
                break;

            default:
                this.app.getConsole().usage(player, "Invalid number of arguments.", this.help.getUsages());
                return true;
        }

        if (amountToBuy < 1) {
            this.app.getConsole().usage(player, "Invalid amount.", this.help.getUsages());
            this.app.getConsole().debug("(BuyItem)Invalid amount: " + amountToBuy);

        } else {
            MaterialData materialData = this.app.getMaterialManager().getMaterial(materialName);
            if (materialData == null) {
                this.app.getConsole().usage(player, "Unknown Item: '" + materialName + "'", this.help.getUsages());
                this.app.getConsole().debug("(BuyItem)Unknown Item: " + materialName);

            } else {
                int availableSpace = PlayerInventoryManager.getAvailableSpace(player, materialData.getMaterial());
                if (amountToBuy > availableSpace) {
                    this.app.getConsole().logFailedPurchase(player, amountToBuy, materialData.getCleanName(), String.format("missing inventory space (%d/%d)", availableSpace, amountToBuy));

                } else {
                    if (amountToBuy > materialData.getQuantity()) {
                        this.app.getConsole().logFailedPurchase(player, amountToBuy, materialData.getCleanName(), String.format("not enough stock (%d/%d)", materialData.getQuantity(), amountToBuy));

                    } else {

                        ItemStack[] itemStacks = PlayerInventoryManager.createItemStacks(materialData.getMaterial(), amountToBuy);
                        ValueResponse priceResponse = this.app.getMaterialManager().getBuyValue(itemStacks);
                        EconomyResponse saleResponse = this.app.getEconomyManager().remCash(player, priceResponse.value);
                        if (saleResponse.transactionSuccess() && priceResponse.isSuccess()) {
                            PlayerInventoryManager.addItemsToPlayer(player, itemStacks);
                            materialData.remQuantity(amountToBuy);

                            // Handles console, message and mail
                            this.app.getConsole().logPurchase(player, amountToBuy, saleResponse.amount, materialData.getCleanName());

                        } else {
                            String errorMessage = "unknown error";
                            if (!saleResponse.transactionSuccess()) errorMessage = saleResponse.errorMessage;
                            else if (priceResponse.isFailure()) errorMessage = priceResponse.errorMessage;

                            // Handles console, message and mail
                            this.app.getConsole().logFailedPurchase(player, amountToBuy, materialData.getCleanName(), errorMessage);
                        }
                    }
                }
            }
        }
        return true;
    }
}
