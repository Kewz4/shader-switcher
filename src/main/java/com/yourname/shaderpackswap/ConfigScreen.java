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
    private ConfigListWidget list;

    public ConfigScreen(Screen parent) {
        super(Component.literal("ShaderPack Swap Configuration"));
        this.parent = parent;
        this.config = ConfigLoader.load(); // Load fresh config
    }

    @Override
    protected void init() {
        this.list = new ConfigListWidget(this.minecraft, this.width, this.height - 64, 32, 25);
        this.addRenderableWidget(this.list);

        // Global Category
        this.list.addEntry(new ConfigListWidget.CategoryEntry(Component.literal("§lGlobal Settings").withStyle(style -> style.withBold(true))));

        this.list.addEntry(new ConfigListWidget.ProfileEntry(Component.literal("Global: Shaders OFF"), button -> {
            this.minecraft.setScreen(new PackSelectionScreen(this, config, SwapConfig.SHADERS_OFF));
        }));

        this.list.addEntry(new ConfigListWidget.ProfileEntry(Component.literal("Global: Shaders ON"), button -> {
            this.minecraft.setScreen(new PackSelectionScreen(this, config, SwapConfig.SHADERS_ON_GLOBAL));
        }));

        // Spacer
        this.list.addEntry(new ConfigListWidget.CategoryEntry(Component.literal("")));

        // Shaders Category
        this.list.addEntry(new ConfigListWidget.CategoryEntry(Component.literal("§lShader Specific Settings").withStyle(style -> style.withBold(true))));
        this.list.addEntry(new ConfigListWidget.CategoryEntry(Component.literal("§7(Overrides Global Settings)")));

        List<String> shaders = detectShaders();
        if (shaders.isEmpty()) {
            this.list.addEntry(new ConfigListWidget.CategoryEntry(Component.literal("No shaders detected in shaderpacks folder.")));
        } else {
            for (String shader : shaders) {
                this.list.addEntry(new ConfigListWidget.ProfileEntry(Component.literal(shader), button -> {
                    this.minecraft.setScreen(new PackSelectionScreen(this, config, shader));
                }));
            }
        }

        // Done Button
        this.addRenderableWidget(Button.builder(Component.literal("Done"), button -> {
            ConfigLoader.save(config);
            ConfigLoader.configChanged = true;
            this.minecraft.setScreen(parent);
        }).bounds(this.width / 2 - 100, this.height - 25, 200, 20).build());
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 15, 0xFFFFFF);
    }

    private List<String> detectShaders() {
        List<String> list = new ArrayList<>();
        try {
            Path shaderPacksDir = Minecraft.getInstance().gameDirectory.toPath().resolve("shaderpacks");
            if (Files.exists(shaderPacksDir)) {
                try (Stream<Path> stream = Files.list(shaderPacksDir)) {
                     stream.forEach(path -> {
                         String name = path.getFileName().toString();
                         // Simple filter for zip/folders, ignore meta files
                         if ((name.endsWith(".zip") || Files.isDirectory(path)) && !name.startsWith(".")) {
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
