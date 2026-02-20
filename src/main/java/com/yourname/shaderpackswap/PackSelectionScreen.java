package com.yourname.shaderpackswap;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.repository.PackSource;

import java.util.Collection;

public class PackSelectionScreen extends Screen {
    private final Screen parent;
    private final SwapConfig config;
    private final String shaderName;
    private PackListWidget list;

    public PackSelectionScreen(Screen parent, SwapConfig config, String shaderName) {
        super(Component.literal("Configure Packs: " + shaderName));
        this.parent = parent;
        this.config = config;
        this.shaderName = shaderName;
    }

    @Override
    protected void init() {
        this.list = new PackListWidget(this.minecraft, this.width, this.height - 64, 32, 36);
        this.addRenderableWidget(this.list);

        PackRepository manager = Minecraft.getInstance().getResourcePackRepository();
        Collection<Pack> packs = manager.getAvailablePacks();

        for (Pack pack : packs) {
            if (shouldShowPack(pack)) {
                this.list.addEntryToWidget(new PackListWidget.PackEntry(this.minecraft, pack, config, shaderName));
            }
        }

        this.addRenderableWidget(Button.builder(Component.literal("Back"), button -> {
            this.minecraft.setScreen(parent);
        }).bounds(this.width / 2 - 100, this.height - 25, 200, 20).build());
    }

    private boolean shouldShowPack(Pack pack) {
        if (pack.isRequired()) return false;
        if (pack.getId().equals("vanilla")) return false;
        if (pack.getPackSource() == PackSource.BUILT_IN) return false;
        if (pack.getId().equals("fabric") || pack.getId().equals("modmenu")) return false;
        return true;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 15, 0xFFFFFF);
    }
}
