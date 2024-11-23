package org.lushplugins.regrowththemes.schematic;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lushplugins.regrowththemes.RegrowthThemes;

public class SchematicUser {
    private String currentSchematic;

    public SchematicUser(String currentSchematic) {
        this.currentSchematic = currentSchematic;
    }

    public String getCurrentSchematicName() {
        return currentSchematic;
    }

    public @Nullable BukkitSchematic getCurrentSchematic() {
        if (currentSchematic == null) {
            return null;
        }

        return RegrowthThemes.getInstance().getSchematicManager().getSchematic(currentSchematic);
    }

    public void setCurrentSchematic(@NotNull Schematic schematic) {
        setCurrentSchematic(schematic.getName());
    }

    public void setCurrentSchematic(@NotNull String schematicName) {
        this.currentSchematic = schematicName;
    }
}
