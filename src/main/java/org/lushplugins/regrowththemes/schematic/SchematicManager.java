package org.lushplugins.regrowththemes.schematic;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;
import org.lushplugins.regrowththemes.RegrowthThemes;
import org.lushplugins.regrowththemes.theme.Theme;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class SchematicManager {
    private final Map<String, CompletableFuture<Schematic>> schematics = new HashMap<>();
    private final Map<UUID, String> users = new HashMap<>();

    public @Nullable Schematic getSchematic(String schematicName) {
        CompletableFuture<Schematic> future = schematics.get(schematicName);
        return future != null && future.isDone() ? future.join() : null;
    }

    public CompletableFuture<Schematic> getOrLoadSchematic(String schematicName) {
        return schematics.computeIfAbsent(schematicName, Schematic::load);
    }

    public Collection<Schematic> getCachedSchematics() {
        return schematics.values().stream()
            .filter(CompletableFuture::isDone)
            .map(CompletableFuture::join)
            .toList();
    }

    public void addSchematic(String schematicName, Schematic schematic) {
        schematics.put(schematicName, CompletableFuture.completedFuture(schematic));
    }

    public void removeSchematic(String schematicName) {
        schematics.remove(schematicName);
    }

    public @Nullable Schematic getViewing(UUID uuid) {
        String schematicName = users.get(uuid);
        if (schematicName != null) {
            return getSchematic(schematicName);
        }

        Theme theme = RegrowthThemes.getInstance().getConfigManager().getCurrentTheme();
        return theme != null ? getSchematic(theme.name()) : null;
    }

    public Set<UUID> getViewing(String schematicName) {
        // This is hacky but works until a better viewing system is implemented
        if (RegrowthThemes.getInstance().getConfigManager().isCurrentTheme(schematicName)) {
            Set<UUID> viewers = Bukkit.getOnlinePlayers().stream()
                .map(Player::getUniqueId)
                .collect(Collectors.toCollection(HashSet::new));

            viewers.removeAll(users.entrySet().stream()
                .filter(entry -> !entry.getValue().equalsIgnoreCase(schematicName))
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet()));

            return viewers;
        }

        return users.entrySet().stream()
            .filter(entry -> entry.getValue().equalsIgnoreCase(schematicName))
            .map(Map.Entry::getKey)
            .collect(Collectors.toSet());
    }

    public void setViewing(UUID uuid, String schematicName) {
        String currentSchematicName = users.get(uuid);
        if (getSchematic(currentSchematicName) instanceof Schematic modifying) {
            modifying.removeEditor(uuid);

            if (!RegrowthThemes.getInstance().getConfigManager().isCurrentTheme(currentSchematicName)) {
                if (!modifying.hasEditors()) {
                    removeSchematic(currentSchematicName);
                }
            }
        }

        users.put(uuid, schematicName);
    }

    public void setEditing(UUID uuid, String schematicName) {
        setViewing(uuid, schematicName);

        Schematic schematic = getViewing(uuid);
        if (schematic != null) {
            schematic.addEditor(uuid);
        }
    }

    public void invalidateUser(UUID uuid) {
        String schemName = users.remove(uuid);

        Schematic schematic = getSchematic(schemName);
        if (schematic != null) {
            schematic.removeEditor(uuid);
        }
    }
}
