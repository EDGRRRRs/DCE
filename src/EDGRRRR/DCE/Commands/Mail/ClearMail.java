package EDGRRRR.DCE.Commands.Mail;

import EDGRRRR.DCE.Mail.Mail;
import EDGRRRR.DCE.Mail.MailList;
import EDGRRRR.DCE.Main.DCEPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;

public class ClearMail implements CommandExecutor {
    private final DCEPlugin app;
    private final String usage = "/clearMail read | /clearMail unread | /clearMail all";

    public ClearMail(DCEPlugin app) {
        this.app = app;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            return true;
        }

        Player player = (Player) sender;

        // Ensure command is enabled
        if (!(this.app.getConfig().getBoolean(this.app.getConfigManager().strComClearMail))) {
            this.app.getConsoleManager().severe(player, "This command is not enabled.");
            return true;
        }

        boolean clearRead = false;
        boolean clearUnread = false;
        switch (args.length) {
            case 1:
                String arg = args[0].toLowerCase();
                switch (arg) {
                    case "all":
                        clearRead = true;
                        clearUnread = true;
                        break;

                    case "read":
                        clearRead = true;
                        break;

                    case "unread":
                        clearUnread = true;
                        break;

                    default:
                        this.app.getConsoleManager().usage(player, "Invalid arguments.", this.usage);
                        return true;
                }
                break;

            default:
                this.app.getConsoleManager().usage(player, "Invalid number of arguments.", this.usage);
                return true;
        }

        MailList mailList = this.app.getMailManager().getMailList(player);
        HashMap<String, Mail> allMail = mailList.getAllMail();
        ArrayList<String> readMail = mailList.getReadMail();
        ArrayList<String> unreadMail = mailList.getUnreadMail();
        ArrayList<String> mailToClear = new ArrayList<>();
        int readMailCleared = 0;
        int unreadMailCleared = 0;

        if (allMail.isEmpty()) {
            this.app.getConsoleManager().warn(player, "You have no mail to clear.");
        } else {
            if (clearRead) {
                mailToClear.addAll(readMail);
                readMailCleared = readMail.size();
            }
            if (clearUnread) {
                mailToClear.addAll(unreadMail);
                unreadMailCleared = unreadMail.size();
            }

            for (String mailID : mailToClear) {
                mailList.removeMail(mailID);
            }
            this.app.getConsoleManager().info(player, String.format("Removed %d mail. (%d unread & %d read)", mailToClear.size(), unreadMailCleared, readMailCleared));

        }
        return true;
    }
}
