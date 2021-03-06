package me.edgrrrr.de.commands.market;

import me.edgrrrr.de.DEPlugin;
import me.edgrrrr.de.commands.DivinityCommandMaterials;
import me.edgrrrr.de.config.Setting;
import me.edgrrrr.de.materials.MaterialData;
import me.edgrrrr.de.math.Math;
import me.edgrrrr.de.player.PlayerInventoryManager;
import me.edgrrrr.de.response.ValueResponse;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * A command for selling items in the users hand
 */
public class HandSell extends DivinityCommandMaterials {

    /**
     * Constructor
     *
     * @param app
     */
    public HandSell(DEPlugin app) {
        super(app, "handsell", false, Setting.COMMAND_HAND_SELL_ITEM_ENABLE_BOOLEAN);
    }

    /**
     * ###To be overridden by the actual command
     * For handling a player calling this command
     *
     * @param sender
     * @param args
     * @return
     */
    @Override
    public boolean onPlayerCommand(Player sender, String[] args) {
        int amountToSell = 1;
        boolean sellAll = false;

        switch (args.length) {
            case 0:
                break;

            case 1:
                String arg = args[0];
                if (arg.equalsIgnoreCase("max")) {
                    sellAll = true;
                } else {
                    amountToSell = Math.getInt(args[0]);
                }
                break;

            default:
                this.getMain().getConsole().usage(sender, CommandResponse.InvalidNumberOfArguments.message, this.help.getUsages());
                return true;
        }

        // Ensure amount is above 0
        if (amountToSell < 1) {
            this.getMain().getConsole().send(sender, CommandResponse.InvalidAmountGiven.defaultLogLevel, CommandResponse.InvalidAmountGiven.message);
            return true;
        }


        ItemStack heldItem = PlayerInventoryManager.getHeldItem(sender);

        // Ensure item held is not null
        if (heldItem == null) {
            this.getMain().getConsole().send(sender, CommandResponse.InvalidItemHeld.defaultLogLevel, CommandResponse.InvalidItemHeld.message);
            return true;
        }

        Material material = heldItem.getType();
        String materialName = material.name();
        MaterialData materialData = this.getMain().getMaterialManager().getMaterial(materialName);
        int materialCount = PlayerInventoryManager.getMaterialCount(PlayerInventoryManager.getMaterialSlots(sender, material));

        if (sellAll) {
            amountToSell = materialCount;
        }

        // Ensure player inventory has enough
        if (materialCount < amountToSell) {
            this.getMain().getConsole().logFailedSale(sender, amountToSell, materialData.getCleanName(), String.format(CommandResponse.InvalidInventoryStock.message, materialCount, amountToSell));
            return true;
        }

        // Get item stacks to remove
        // Clone item stacks incase they need to be refunded
        // Get the value
        ItemStack[] itemStacks = PlayerInventoryManager.getMaterialSlotsToCount(sender, material, amountToSell);
        ItemStack[] itemStacksClone = PlayerInventoryManager.cloneItems(itemStacks);
        ValueResponse response = this.getMain().getMaterialManager().getSellValue(itemStacks);

        if (response.isSuccess()) {
            PlayerInventoryManager.removePlayerItems(itemStacks);

            EconomyResponse economyResponse = this.getMain().getEconomyManager().addCash(sender, response.value);
            if (!economyResponse.transactionSuccess()) {
                PlayerInventoryManager.addPlayerItems(sender, itemStacksClone);
                // Handles console, player message and mail
                this.getMain().getConsole().logFailedSale(sender, amountToSell, materialData.getCleanName(), economyResponse.errorMessage);
            }

            else {
                this.getMain().getMaterialManager().editQuantity(materialData, amountToSell);
                // Handles console, player message and mail
                this.getMain().getConsole().logSale(sender, amountToSell, response.value, materialData.getCleanName());
            }
        }

        else {
            // Handles console, player message and mail
            this.getMain().getConsole().logFailedSale(sender, amountToSell, materialData.getCleanName(), response.errorMessage);
        }
        return true;
    }

    /**
     * ###To be overridden by the actual command
     * For the handling of the console calling this command
     *
     * @param args
     * @return
     */
    @Override
    public boolean onConsoleCommand(String[] args) {
        return false;
    }
}
