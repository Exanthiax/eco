package com.willfp.eco.internal.config.yaml;

import com.willfp.eco.core.EcoPlugin;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EcoUpdatableYamlConfig extends EcoLoadableYamlConfig {
    private final boolean removeUnused;

    private final List<String> updateBlacklist;

    public EcoUpdatableYamlConfig(@NotNull final String configName,
                                  @NotNull final EcoPlugin plugin,
                                  @NotNull final String subDirectoryPath,
                                  @NotNull final Class<?> source,
                                  final boolean removeUnused,
                                  @NotNull final String... updateBlacklist) {
        super(configName, plugin, subDirectoryPath, source);
        this.removeUnused = removeUnused;
        this.updateBlacklist = new ArrayList<>(Arrays.asList(updateBlacklist));
        this.updateBlacklist.removeIf(String::isEmpty);

        plugin.getConfigHandler().addConfig(this);

        update();
    }

    public void update() {
        super.clearCache();
        try {
            this.getHandle().load(this.getConfigFile());

            YamlConfiguration newConfig = this.getConfigInJar();

            if (newConfig.getKeys(true).equals(this.getHandle().getKeys(true))) {
                return;
            }

            newConfig.getKeys(true).forEach((s -> {
                if (!this.getHandle().getKeys(true).contains(s)) {
                    if (updateBlacklist.stream().noneMatch(s::contains)) {
                        this.getHandle().set(s, newConfig.get(s));
                    }
                }
            }));

            if (this.removeUnused) {
                this.getHandle().getKeys(true).forEach((s -> {
                    if (!newConfig.getKeys(true).contains(s)) {
                        if (updateBlacklist.stream().noneMatch(s::contains)) {
                            this.getHandle().set(s, null);
                        }
                    }
                }));
            }

            this.getHandle().save(this.getConfigFile());
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }
    }

    public YamlConfiguration getConfigInJar() {
        InputStream newIn = this.getSource().getResourceAsStream(getResourcePath());

        if (newIn == null) {
            throw new NullPointerException(this.getName() + " is null?");
        }

        BufferedReader reader = new BufferedReader(new InputStreamReader(newIn, StandardCharsets.UTF_8));
        YamlConfiguration newConfig = new YamlConfiguration();

        try {
            newConfig.load(reader);
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }

        return newConfig;
    }
}
