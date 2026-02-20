package com.yourname.shaderpackswap;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

public class PackSelectionScreen extends Screen {
    private final Screen parent;
    private final SwapConfig config;
    private final String shaderName;
    private final List<String> allPacks;
    private int page = 0;
    private static final int ITEMS_PER_PAGE = 8;

    public PackSelectionScreen(Screen parent, SwapConfig config, String shaderName) {
        super(Component.literal("Configure Packs: " + shaderName));
        this.parent = parent;
        this.config = config;
        this.shaderName = shaderName;
        this.allPacks = new ArrayList<>(Minecraft.getInstance().getResourcePackRepository().getAvailableIds());
        this.allPacks.remove("vanilla");
        // Sort for easier finding
        this.allPacks.sort(String::compareToIgnoreCase);
    }

    @Override
    protected void init() {
        int y = 40;
        int center = this.width / 2;

        int start = page * ITEMS_PER_PAGE;
        int end = Math.min(start + ITEMS_PER_PAGE, allPacks.size());

        for (int i = start; i < end; i++) {
            String packId = allPacks.get(i);
            String label = getLabel(packId);

            this.addRenderableWidget(Button.builder(Component.literal(label), button -> {
                cycleState(packId);
                button.setMessage(Component.literal(getLabel(packId)));
            }).bounds(center - 150, y, 300, 20).build());

            y += 25;
        }

        // Pagination
        if (page > 0) {
            this.addRenderableWidget(Button.builder(Component.literal("< Prev"), button -> {
                page--;
                this.rebuildWidgets();
            }).bounds(center - 100, this.height - 50, 90, 20).build());
        }
        if (end < allPacks.size()) {
            this.addRenderableWidget(Button.builder(Component.literal("Next >"), button -> {
                page++;
                this.rebuildWidgets();
            }).bounds(center + 10, this.height - 50, 90, 20).build());
        }

        // Back
        this.addRenderableWidget(Button.builder(Component.literal("Back"), button -> {
            this.minecraft.setScreen(parent);
        }).bounds(center - 100, this.height - 25, 200, 20).build());
    }

    private String getLabel(String packId) {
        if (config.getPacksToEnable(shaderName).contains(packId)) {
             return packId + ": [ENABLED]";
        } else if (config.getPacksToDisable(shaderName).contains(packId)) {
             return packId + ": [DISABLED]";
        } else {
             return packId + ": [DEFAULT]";
        }
    }

    private void cycleState(String packId) {
        List<String> toEnable = config.getPacksToEnable(shaderName);
        List<String> toDisable = config.getPacksToDisable(shaderName);

        if (toEnable.contains(packId)) {
            // Enabled -> Disabled
            toEnable.remove(packId);
            toDisable.add(packId);
        } else if (toDisable.contains(packId)) {
            // Disabled -> Default
            toDisable.remove(packId);
        } else {
            // Default -> Enabled
            toEnable.add(packId);
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 20, 0xFFFFFF);
        guiGraphics.drawCenteredString(this.font, "Page " + (page + 1), this.width / 2, this.height - 70, 0xAAAAAA);
    }
}
