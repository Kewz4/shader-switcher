package com.yourname.shaderpackswap;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.*;
import java.nio.file.*;
import java.util.Arrays;

public class ConfigLoader {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = FabricLoader.getInstance()
            .getConfigDir().resolve("shaderpacks_swap.json");

    private static SwapConfig INSTANCE;
    public static boolean configChanged = false;

    public static SwapConfig load() {
        if (INSTANCE != null) return INSTANCE;
        return reload();
    }

    public static SwapConfig reload() {
        if (!Files.exists(CONFIG_PATH)) {
            SwapConfig defaults = new SwapConfig();

            // Defaults: Disable these when ON
            defaults.getPacksToDisable(SwapConfig.SHADERS_ON_GLOBAL).addAll(Arrays.asList(
                "file/§aBiomeBloom§8.zip",
                "file/Benigamer'enhanced visuals 1.9.zip"
            ));

            // Defaults: Re-enable these when OFF
            defaults.getPacksToEnable(SwapConfig.SHADERS_OFF).addAll(Arrays.asList(
                "file/§aBiomeBloom§8.zip",
                "file/Benigamer'enhanced visuals 1.9.zip"
            ));

            save(defaults);
            System.out.println("[ShaderPackSwap] Config not found, created default at: " + CONFIG_PATH);
            INSTANCE = defaults;
            return defaults;
        }
        try (Reader reader = Files.newBufferedReader(CONFIG_PATH)) {
            INSTANCE = GSON.fromJson(reader, SwapConfig.class);
            return INSTANCE;
        } catch (IOException e) {
            System.err.println("[ShaderPackSwap] Failed to load config: " + e.getMessage());
            INSTANCE = new SwapConfig();
            return INSTANCE;
        }
    }

    public static void save(SwapConfig config) {
        INSTANCE = config; // Update cache
        try (Writer writer = Files.newBufferedWriter(CONFIG_PATH)) {
            GSON.toJson(config, writer);
        } catch (IOException e) {
            System.err.println("[ShaderPackSwap] Failed to save config: " + e.getMessage());
        }
    }
}
