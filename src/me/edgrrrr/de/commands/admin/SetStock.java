package me.edgrrrr.de.commands.admin;

import me.edgrrrr.de.DEPlugin;
import me.edgrrrr.de.commands.DivinityCommand;
import me.edgrrrr.de.config.Setting;
import me.edgrrrr.de.materials.MaterialData;
import me.edgrrrr.de.math.Math;
import org.bukkit.entity.Player;

/**
 * A command for setting the stock of a material
 */
public class SetStock extends DivinityCommand {

    /**
     * Constructor
     *
     * @param app
     */
    public SetStock(DEPlugin app) {
        super(app, "setstock", true, Setting.COMMAND_SET_STOCK_ENABLE_BOOLEAN);
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
        MaterialData materialData = null;
        int stock = -1;
        switch (args.length) {
            case 2:
                materialData = this.getMain().getMaterialManager().getMaterial(args[0]);
                stock = Math.getInt(args[1]);
                break;

            default:
                this.getMain().getConsole().usage(sender, CommandResponse.InvalidNumberOfArguments.message, this.help.getUsages());
                return true;
        }

        // Ensure material exists
        if (materialData == null) {
            this.getMain().getConsole().send(sender, CommandResponse.InvalidItemName.defaultLogLevel, CommandResponse.InvalidItemName.message, args[0]);
            return true;
        }

        // Ensure stock is greater than 0
        if (stock < 0) {
            this.getMain().getConsole().send(sender, CommandResponse.InvalidStockAmount.defaultLogLevel, CommandResponse.InvalidStockAmount.message, stock, 0);
            return true;
        }


        int previousStock = materialData.getQuantity();
        double previousValue = this.getMain().getMaterialManager().getUserPrice(materialData.getQuantity());
        this.getMain().getMaterialManager().setQuantity(materialData, stock);
        this.getMain().getConsole().send(sender, CommandResponse.StockCountChanged.defaultLogLevel, CommandResponse.StockCountChanged.message, previousStock, this.getMain().getConsole().formatMoney(previousValue), stock, this.getMain().getConsole().formatMoney(this.getMain().getMaterialManager().getUserPrice(materialData.getQuantity())));

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
