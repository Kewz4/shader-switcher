package com.yourname.shaderpackswap;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class PackListWidget extends ContainerObjectSelectionList<PackListWidget.PackEntry> {

    public PackListWidget(Minecraft client, int width, int height, int y, int itemHeight) {
        super(client, width, height, y, itemHeight);
    }

    @Override
    public int getRowWidth() {
        return 380;
    }

    @Override
    protected int getScrollbarPosition() {
        return this.getX() + this.width - 6;
    }

    public void addEntryToWidget(PackEntry entry) {
        super.addEntry(entry);
    }

    public static class PackEntry extends ContainerObjectSelectionList.Entry<PackEntry> {
        private final Minecraft client;
        private final Pack pack;
        private final SwapConfig config;
        private final String shaderName;
        private final Button toggleButton;

        // Use fully qualified name to avoid import issues
        private net.minecraft.resources.ResourceLocation iconLocation;

        // Fallback if ResourceLocation is missing (unlikely, but to compile)
        private static final net.minecraft.resources.ResourceLocation DEFAULT_ICON =
            net.minecraft.resources.ResourceLocation.parse("textures/misc/unknown_pack.png");

        public PackEntry(Minecraft client, Pack pack, SwapConfig config, String shaderName) {
            this.client = client;
            this.pack = pack;
            this.config = config;
            this.shaderName = shaderName;
            this.iconLocation = loadIcon();

            this.toggleButton = Button.builder(Component.literal(""), button -> {
                cycleState();
                updateButtonLabel();
            }).bounds(0, 0, 100, 20).build();
            updateButtonLabel();
        }

        private net.minecraft.resources.ResourceLocation loadIcon() {
            try {
                return DEFAULT_ICON;
            } catch (Exception e) {
                return DEFAULT_ICON;
            }
        }

        private void cycleState() {
            List<String> toEnable = config.getPacksToEnable(shaderName);
            List<String> toDisable = config.getPacksToDisable(shaderName);
            String packId = pack.getId();

            if (toEnable.contains(packId)) {
                toEnable.remove(packId);
                toDisable.add(packId);
            } else if (toDisable.contains(packId)) {
                toDisable.remove(packId);
            } else {
                toEnable.add(packId);
            }
        }

        private void updateButtonLabel() {
            String packId = pack.getId();
            List<String> toEnable = config.getPacksToEnable(shaderName);
            List<String> toDisable = config.getPacksToDisable(shaderName);

            if (toEnable.contains(packId)) {
                this.toggleButton.setMessage(Component.literal("ENABLED").withStyle(style -> style.withColor(0x55FF55)));
            } else if (toDisable.contains(packId)) {
                this.toggleButton.setMessage(Component.literal("DISABLED").withStyle(style -> style.withColor(0xFF5555)));
            } else {
                this.toggleButton.setMessage(Component.literal("DEFAULT").withStyle(style -> style.withColor(0xAAAAAA)));
            }
        }

        @Override
        public void renderContent(GuiGraphics guiGraphics, int x, int y, boolean hovering, float partialTick) {
            // Icon (32x32)
            guiGraphics.blit(this.iconLocation, x + 4, y + 2, 0, 0, 32, 32, 32, 32);

            // Title
            guiGraphics.drawString(client.font, pack.getTitle(), x + 40, y + 2, 0xFFFFFF);

            // Description (truncated)
            guiGraphics.drawString(client.font, pack.getDescription(), x + 40, y + 14, 0x888888);

            // Button
            this.toggleButton.setX(x + 380 - 105);
            this.toggleButton.setY(y + (36 - 20) / 2); // 36 is item height
            this.toggleButton.render(guiGraphics, 0, 0, partialTick);
        }

        @Override
        public List<? extends GuiEventListener> children() {
            return java.util.List.of(this.toggleButton);
        }

        @Override
        public List<? extends NarratableEntry> narratables() {
            return java.util.List.of(this.toggleButton);
        }
    }
}
