package com.yourname.shaderpackswap;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.*;
import java.nio.file.*;

public class ConfigLoader {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = FabricLoader.getInstance()
            .getConfigDir().resolve("shaderpacks_swap.json");

    public static SwapConfig load() {
        if (!Files.exists(CONFIG_PATH)) {
            SwapConfig defaults = new SwapConfig();
            save(defaults);
            System.out.println("[ShaderPackSwap] Config not found, created default at: " + CONFIG_PATH);
            System.out.println("[ShaderPackSwap] Edit it to add your resource pack names (e.g. \"file/MyPack.zip\")");
            return defaults;
        }
        try (Reader reader = Files.newBufferedReader(CONFIG_PATH)) {
            return GSON.fromJson(reader, SwapConfig.class);
        } catch (IOException e) {
            System.err.println("[ShaderPackSwap] Failed to load config: " + e.getMessage());
            return new SwapConfig();
        }
    }

    public static void save(SwapConfig config) {
        try (Writer writer = Files.newBufferedWriter(CONFIG_PATH)) {
            GSON.toJson(config, writer);
        } catch (IOException e) {
            System.err.println("[ShaderPackSwap] Failed to save config: " + e.getMessage());
        }
    }
}