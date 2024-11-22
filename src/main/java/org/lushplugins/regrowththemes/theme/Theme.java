package org.lushplugins.regrowththemes.theme;

import org.lushplugins.regrowththemes.schematic.Schematic;

public record Theme(Schematic schematic) {

    public String getName() {
        return schematic.getName();
    }
}
