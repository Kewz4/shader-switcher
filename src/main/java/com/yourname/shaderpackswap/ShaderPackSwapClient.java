package com.yourname.shaderpackswap;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.server.packs.repository.PackRepository;
import net.irisshaders.iris.api.v0.IrisApi;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Environment(EnvType.CLIENT)
public class ShaderPackSwapClient implements ClientModInitializer {

    private SwapConfig config;
    private boolean previousShaderState = false;
    private int cooldownTicks = 0;
    private static final int COOLDOWN = 100; // 5 seconds at 20tps

    @Override
    public void onInitializeClient() {
        config = ConfigLoader.load();

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            // Wait until the client is fully in a world
            if (client.level == null) return;

            // Cooldown after reload to avoid re-triggering
            if (cooldownTicks > 0) {
                cooldownTicks--;
                return;
            }

            // Safety check in case Iris isn't loaded
            IrisApi irisApi;
            try {
                irisApi = IrisApi.getInstance();
            } catch (Throwable t) {
                return;
            }
            if (irisApi == null) return;

            boolean shadersNowOn = irisApi.isShaderPackInUse();

            if (shadersNowOn != previousShaderState) {
                previousShaderState = shadersNowOn;
                handleShaderToggle(shadersNowOn, client);
                cooldownTicks = COOLDOWN;
            }
        });
    }

    private void handleShaderToggle(boolean shadersOn, Minecraft client) {
        PackRepository manager = client.getResourcePackRepository();
        Collection<String> currentEnabled = manager.getSelectedIds();
        List<String> newPacks = new ArrayList<>(currentEnabled);

        if (shadersOn) {
            // Shaders just turned ON:
            // remove packs that should be disabled with shaders on
            newPacks.removeAll(config.packsToDisableWhenShadersOn);
            // re-add packs that were disabled when shaders were off
            for (String pack : config.packsToDisableWhenShadersOff) {
                if (!newPacks.contains(pack)) {
                    newPacks.add(pack);
                }
            }
        } else {
            // Shaders just turned OFF:
            // remove packs that should be disabled with shaders off
            newPacks.removeAll(config.packsToDisableWhenShadersOff);
            // re-add packs that were disabled when shaders were on
            for (String pack : config.packsToDisableWhenShadersOn) {
                if (!newPacks.contains(pack)) {
                    newPacks.add(pack);
                }
            }
        }

        // Always keep vanilla
        if (!newPacks.contains("vanilla")) {
            newPacks.add(0, "vanilla");
        }

        // Only reload if the list actually changed
        if (newPacks.equals(new ArrayList<>(currentEnabled))) {
            System.out.println("[ShaderPackSwap] Pack list unchanged, skipping reload.");
            return;
        }

        System.out.println("[ShaderPackSwap] Shaders toggled " + (shadersOn ? "ON" : "OFF") + ", swapping resource packs...");
        System.out.println("[ShaderPackSwap] New pack list: " + newPacks);

        manager.setSelected(newPacks);
        client.options.resourcePacks = new ArrayList<>(newPacks);
        client.options.save();
        client.reloadResourcePacks();
    }
}