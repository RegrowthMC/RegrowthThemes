package org.lushplugins.regrowththemes.schematic;

import org.jetbrains.annotations.Nullable;
import org.lushplugins.regrowththemes.RegrowthThemes;
import org.lushplugins.regrowththemes.utils.FileUtils;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.UUID;

public class SchematicManager {
    private final HashMap<String, Schematic> schematics = new HashMap<>();
    private final HashMap<UUID, SchematicUser> users = new HashMap<>();

    public @Nullable Schematic getSchematic(String schematicName) {
        return schematics.get(schematicName);
    }

    public @Nullable Schematic getOrLoadSchematic(String schematicName) {
        return schematics.computeIfAbsent(schematicName, (ignored) -> Schematic.load(schematicName));
    }

    public Collection<Schematic> getSchematics() {
        return schematics.values();
    }

    public void addSchematic(Schematic schematic) {
        schematics.put(schematic.getName(), schematic);
    }

    public void removeSchematic(Schematic schematic) {
        removeSchematic(schematic.getName());
    }

    public void removeSchematic(String schematicName) {
        schematics.remove(schematicName);
    }

    public SchematicUser getUser(UUID uuid) {
        return users.get(uuid);
    }

    public void updateUser(UUID uuid, String schematicName) {
        SchematicUser user = getUser(uuid);
        if (user != null) {
            Schematic schematic = user.getCurrentSchematic();
            if (schematic != null) {
                schematic.removeUser(uuid);

                if (!schematic.hasUsers()) {
                    removeSchematic(schematic);
                }
            }


            user.setCurrentSchematic(schematicName);
        } else {
            users.put(uuid, new SchematicUser(schematicName));
        }

        Schematic newSchematic = getSchematic(schematicName);
        if (newSchematic != null) {
            newSchematic.addUser(uuid);
        }
    }

    public void removeUser(UUID uuid) {
        SchematicUser user = users.remove(uuid);
        if (user == null) {
            return;
        }

        String schematicName = user.getCurrentSchematicName();
        Schematic schematic = getSchematic(schematicName);
        if (schematic == null) {
            return;
        }

        schematic.removeUser(uuid);
    }
}
