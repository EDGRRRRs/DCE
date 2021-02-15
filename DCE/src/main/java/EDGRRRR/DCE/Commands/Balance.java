package EDGRRRR.DCE.Commands;

import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import EDGRRRR.DCE.Main.DCEPlugin;

/**
 * A command executor class for replying to /balance
 */
public class Balance implements CommandExecutor {
    private DCEPlugin app;
    private String usage = "/balance or /balance <username>";

    public Balance(DCEPlugin app) {
        this.app = app;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Ensure sender is player or return
        if (!(sender instanceof Player)) {
            return true;
        }

        // Create player object
        Player from = (Player) sender;

        // Ensure command is enabled
        if (!(app.getConfig().getBoolean(app.getConf().strComBalance))) {
            app.getCon().severe(from, "This command is not enabled.");
            return true;
        }

        // Use case scenarios
        // command - returns the callers balance.
        // command <username> - returns the usernames balance.
        Player to = null;
        OfflinePlayer toOff = null;

        switch (args.length) {
            case 1:
                // Get online player
                to = app.getServer().getPlayer(args[0]);
                // If they aren't online or don't exist. Do the dirty offline call.
                if (to == null){
                    toOff = app.getOfflinePlayer(args[0], false);
                }
                break;

            default:
                // any number of args.. just return their own.
                to = from;
                break;
        }

        if (to == null && toOff == null){
            app.getCon().usage(from, "Invalid player name.", usage);
            return true;
        }

        double balance = 0.0;
        if (!(to == null)) {
            balance = app.getEco().round(app.getEco().getBalance(to));
            if (!(from == to)) {
                app.getCon().info(from, to.getName() + "'s Balance: £" + balance);
            } else {
                app.getCon().info(from, "Balance: £" + balance);
            }
        } else {
            balance = app.getEco().round(app.getEco().getBalance(toOff));
            app.getCon().info(from, toOff.getName() + "'s Balance: £" + balance);
        }
        // Graceful exit
        return true;
    }
}
