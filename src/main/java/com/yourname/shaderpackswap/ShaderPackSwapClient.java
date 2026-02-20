package com.yourname.shaderpackswap;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.server.packs.repository.PackRepository;
import net.irisshaders.iris.api.v0.IrisApi;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

@Environment(EnvType.CLIENT)
public class ShaderPackSwapClient implements ClientModInitializer {

    private SwapConfig config;
    private boolean previousShaderState = false;
    private String previousShaderName = "";
    private int cooldownTicks = 0;
    private static final int COOLDOWN = 40; // 2 seconds

    // Caching for shader pack name
    private String cachedShaderName = "";
    private long lastCheckTime = 0;
    private static final long CHECK_INTERVAL = 2000; // 2 seconds in ms
    private FileTime lastModifiedTime = null;

    @Override
    public void onInitializeClient() {
        config = ConfigLoader.load();

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.level == null) return;
            if (cooldownTicks > 0) {
                cooldownTicks--;
                return;
            }

            // Always get the latest config instance (it's cached in ConfigLoader)
            config = ConfigLoader.load();

            IrisApi irisApi;
            try {
                irisApi = IrisApi.getInstance();
            } catch (Throwable t) {
                return;
            }
            if (irisApi == null) return;

            boolean shadersNowOn = irisApi.isShaderPackInUse();
            String currentShaderName = getShaderPackName();

            // Check for explicit config change or state change
            boolean configForceReload = ConfigLoader.configChanged;

            if (configForceReload || shadersNowOn != previousShaderState || (shadersNowOn && !currentShaderName.equals(previousShaderName))) {
                if (configForceReload) {
                    System.out.println("[ShaderPackSwap] Config changed, forcing reload...");
                    ConfigLoader.configChanged = false;
                }

                previousShaderState = shadersNowOn;
                previousShaderName = currentShaderName;

                handleShaderToggle(shadersNowOn, currentShaderName, client);
                cooldownTicks = COOLDOWN;
            }
        });
    }

    private String getShaderPackName() {
        long now = System.currentTimeMillis();
        if (now - lastCheckTime < CHECK_INTERVAL) {
            return cachedShaderName;
        }
        lastCheckTime = now;

        try {
            Path configPath = FabricLoader.getInstance().getConfigDir().resolve("iris.properties");
            if (Files.exists(configPath)) {
                FileTime modifiedTime = Files.getLastModifiedTime(configPath);
                if (lastModifiedTime != null && modifiedTime.equals(lastModifiedTime)) {
                    return cachedShaderName;
                }
                lastModifiedTime = modifiedTime;

                Properties props = new Properties();
                try (BufferedReader reader = Files.newBufferedReader(configPath)) {
                    props.load(reader);
                    String pack = props.getProperty("shaderPack");
                    cachedShaderName = pack != null ? pack : "";
                    return cachedShaderName;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return cachedShaderName;
    }

    private void handleShaderToggle(boolean shadersOn, String shaderName, Minecraft client) {
        PackRepository manager = client.getResourcePackRepository();
        Collection<String> currentEnabled = manager.getSelectedIds();
        List<String> newPacks = new ArrayList<>(currentEnabled);

        if (shadersOn) {
            // Apply Global ON Disable
            newPacks.removeAll(config.getPacksToDisable(SwapConfig.SHADERS_ON_GLOBAL));
            // Apply Global ON Enable
            for (String pack : config.getPacksToEnable(SwapConfig.SHADERS_ON_GLOBAL)) {
                if (!newPacks.contains(pack)) newPacks.add(pack);
            }

            // Apply Specific Disable
            newPacks.removeAll(config.getPacksToDisable(shaderName));
            // Apply Specific Enable
            for (String pack : config.getPacksToEnable(shaderName)) {
                if (!newPacks.contains(pack)) newPacks.add(pack);
            }
        } else {
            // Apply Global OFF Disable
            newPacks.removeAll(config.getPacksToDisable(SwapConfig.SHADERS_OFF));
            // Apply Global OFF Enable
            for (String pack : config.getPacksToEnable(SwapConfig.SHADERS_OFF)) {
                if (!newPacks.contains(pack)) newPacks.add(pack);
            }
        }

        // Always keep vanilla
        if (!newPacks.contains("vanilla")) {
            newPacks.add(0, "vanilla");
        }

        if (newPacks.equals(new ArrayList<>(currentEnabled))) {
            System.out.println("[ShaderPackSwap] Pack list unchanged, skipping reload.");
            return;
        }

        System.out.println("[ShaderPackSwap] Shaders toggled/changed/config-update (" + (shadersOn ? "ON: " + shaderName : "OFF") + "), swapping resource packs...");
        System.out.println("[ShaderPackSwap] New pack list: " + newPacks);

        manager.setSelected(newPacks);
        client.options.resourcePacks = new ArrayList<>(newPacks);
        client.options.save();
        client.reloadResourcePacks();
    }
}
