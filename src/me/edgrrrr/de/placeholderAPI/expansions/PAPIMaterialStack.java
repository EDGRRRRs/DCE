package me.edgrrrr.de.placeholderAPI.expansions;

import me.edgrrrr.de.DEPlugin;
import me.edgrrrr.de.materials.MaterialData;
import me.edgrrrr.de.placeholderAPI.DivinityExpansion;
import org.bukkit.OfflinePlayer;

public class PAPIMaterialStack extends DivinityExpansion {
    public PAPIMaterialStack(DEPlugin main) {
        super(main, "^material_stack_(.*)$");
    }

    @Override
    public String getResult(OfflinePlayer player, String value) {
        String material = value.replaceFirst(this.value, "$1");
        MaterialData materialData = this.getMain().getMaterialManager().getMaterial(material);
        if (materialData != null) return String.format("%d", materialData.getMaterial().getMaxStackSize());
        else return String.format("Unknown material '%s'", material);
    }
}
