package org.lushplugins.regrowththemes.theme;

import org.jetbrains.annotations.Nullable;
import org.lushplugins.regrowththemes.schematic.Schematic;

public record Theme(String name, @Nullable Schematic schematic, @Nullable String biome) {}
