package org.lushplugins.regrowththemes.theme;

import org.lushplugins.regrowththemes.schematic.BukkitSchematic;

public record Theme(BukkitSchematic schematic) {

    public String getName() {
        return schematic.getName();
    }
}
