package edgrrrr.dce.commands;

import edgrrrr.configapi.Setting;
import edgrrrr.dce.DCEPlugin;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * The default inherited class for all Divinity Command Tab Completer
 */
public abstract class DivinityCommandEnchantTC extends DivinityCommandTC {
    protected final boolean marketIsEnabled;

    /**
     * Constructor
     * @param app
     * @param hasConsoleSupport
     * @param commandSetting
     */
    public DivinityCommandEnchantTC(DCEPlugin app, boolean hasConsoleSupport, Setting commandSetting) {
        super(app, hasConsoleSupport, commandSetting);
        this.marketIsEnabled = this.app.getConfig().getBoolean(Setting.MARKET_ENCHANTS_ENABLE_BOOLEAN.path);
    }

    /**
     * The pre-handling of onPlayerCommand
     * Checks the command is enabled
     * @param sender
     * @param args
     * @return
     */
    public List<String> _onPlayerTabComplete(Player sender, String[] args) {
        if (!this.isEnabled) {
            return null;
        } else if (!this.marketIsEnabled) {
            return null;
        } else {
            return this.onPlayerTabCompleter(sender, args);
        }
    }

    /**
     * The pre-handling of the onConsoleCommand
     * Checks the command is enabled and has console support
     * @param args
     * @return
     */
    public List<String> _onConsoleTabComplete(String[] args) {
        if (!this.isEnabled) {
            return null;
        } else if (!this.hasConsoleSupport) {
            return null;
        } else if (!this.marketIsEnabled) {
            return null;
        } else {
            return this.onConsoleTabCompleter(args);
        }
    }
}