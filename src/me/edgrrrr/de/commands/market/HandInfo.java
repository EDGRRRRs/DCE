package me.edgrrrr.de.commands.market;

import me.edgrrrr.de.DEPlugin;
import me.edgrrrr.de.commands.DivinityCommandMaterials;
import me.edgrrrr.de.config.Setting;
import me.edgrrrr.de.materials.MaterialData;
import me.edgrrrr.de.materials.MaterialPotionData;
import me.edgrrrr.de.player.PlayerInventoryManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * A command for getting information about the item the user is currently holding
 */
public class HandInfo extends DivinityCommandMaterials {

    /**
     * Constructor
     *
     * @param app
     */
    public HandInfo(DEPlugin app) {
        super(app, "handinfo", false, Setting.COMMAND_HAND_INFO_ENABLE_BOOLEAN);
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
        switch (args.length) {
            case 0:
                break;

            default:
                this.getMain().getConsole().usage(sender, CommandResponse.InvalidNumberOfArguments.message, this.help.getUsages());
                return true;
        }

        ItemStack heldItem = PlayerInventoryManager.getHeldItemNotNull(sender);
        Material material = heldItem.getType();
        MaterialData materialData = this.getMain().getMaterialManager().getMaterial(material.name());

        this.getMain().getConsole().info(sender, "==[Information for " + materialData.getCleanName() + "]==");
        this.getMain().getConsole().info(sender, "ID: " + materialData.getMaterialID());
        this.getMain().getConsole().info(sender, "Type: " + materialData.getType());
        this.getMain().getConsole().info(sender, "Current Quantity: " + materialData.getQuantity());
        this.getMain().getConsole().info(sender, "Is Banned: " + !(materialData.getAllowed()));
        if (materialData.getEntityName() != null)
            this.getMain().getConsole().info(sender, "Entity Name: " + materialData.getEntityName());
        MaterialPotionData pData = materialData.getPotionData();
        if (pData != null) {
            this.getMain().getConsole().info(sender, "Potion type: " + pData.getType());
            this.getMain().getConsole().info(sender, "Upgraded potion: " + pData.getUpgraded());
            this.getMain().getConsole().info(sender, "Extended potion: " + pData.getExtended());
        }

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
        return false;
    }
}
