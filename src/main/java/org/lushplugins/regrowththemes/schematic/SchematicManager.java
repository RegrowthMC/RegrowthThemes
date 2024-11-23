package org.lushplugins.regrowththemes.schematic;

import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashMap;
import java.util.UUID;

public class SchematicManager {
    private final HashMap<String, BukkitSchematic> schematics = new HashMap<>();
    private final HashMap<UUID, SchematicUser> users = new HashMap<>();

    public @Nullable BukkitSchematic getSchematic(String schematicName) {
        return schematics.get(schematicName);
    }

    public @Nullable BukkitSchematic getOrLoadSchematic(String schematicName) {
        return schematics.computeIfAbsent(schematicName, (ignored) -> BukkitSchematic.load(schematicName));
    }

    public Collection<BukkitSchematic> getSchematics() {
        return schematics.values();
    }

    public void addSchematic(BukkitSchematic schematic) {
        schematics.put(schematic.getName(), schematic);
    }

    public void removeSchematic(BukkitSchematic schematic) {
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
            BukkitSchematic schematic = user.getCurrentSchematic();
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

        BukkitSchematic newSchematic = getSchematic(schematicName);
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
        BukkitSchematic schematic = getSchematic(schematicName);
        if (schematic == null) {
            return;
        }

        schematic.removeUser(uuid);
    }
}
