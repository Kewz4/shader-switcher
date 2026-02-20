package com.yourname.shaderpackswap;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class ConfigScreen extends Screen {
    private final Screen parent;
    private final SwapConfig config;

    public ConfigScreen(Screen parent) {
        super(Component.literal("ShaderPack Swap Configuration"));
        this.parent = parent;
        this.config = ConfigLoader.load(); // Load fresh config
    }

    @Override
    protected void init() {
        int y = 40;
        int center = this.width / 2;

        // Global OFF
        this.addRenderableWidget(Button.builder(Component.literal("Configure: Shaders OFF"), button -> {
            this.minecraft.setScreen(new PackSelectionScreen(this, config, SwapConfig.SHADERS_OFF));
        }).bounds(center - 100, y, 200, 20).build());
        y += 25;

        // Global ON
        this.addRenderableWidget(Button.builder(Component.literal("Configure: Shaders ON (Global)"), button -> {
            this.minecraft.setScreen(new PackSelectionScreen(this, config, SwapConfig.SHADERS_ON_GLOBAL));
        }).bounds(center - 100, y, 200, 20).build());
        y += 25;

        // Detect shaders
        List<String> shaders = detectShaders();
        for (String shader : shaders) {
            this.addRenderableWidget(Button.builder(Component.literal("Configure: " + shader), button -> {
                this.minecraft.setScreen(new PackSelectionScreen(this, config, shader));
            }).bounds(center - 100, y, 200, 20).build());
            y += 25;
            if (y > this.height - 60) break; // Simple overflow protection
        }

        // Save & Exit
        this.addRenderableWidget(Button.builder(Component.literal("Done"), button -> {
            ConfigLoader.save(config);
            ConfigLoader.configChanged = true;
            this.minecraft.setScreen(parent);
        }).bounds(center - 100, this.height - 30, 200, 20).build());
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 20, 0xFFFFFF);
    }

    private List<String> detectShaders() {
        List<String> list = new ArrayList<>();
        try {
            Path shaderPacksDir = Minecraft.getInstance().gameDirectory.toPath().resolve("shaderpacks");
            if (Files.exists(shaderPacksDir)) {
                try (Stream<Path> stream = Files.list(shaderPacksDir)) {
                     stream.forEach(path -> {
                         String name = path.getFileName().toString();
                         if (name.endsWith(".zip") || Files.isDirectory(path)) {
                             list.add(name);
                         }
                     });
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return list;
    }
}
