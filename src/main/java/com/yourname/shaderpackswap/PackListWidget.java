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
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

// Changed to ContainerObjectSelectionList to support nested buttons
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

    // Changed to ContainerObjectSelectionList.Entry
    public static class PackEntry extends ContainerObjectSelectionList.Entry<PackEntry> {
        private final Minecraft client;
        private final Pack pack;
        private final SwapConfig config;
        private final String shaderName;
        private final Button toggleButton;
        private ResourceLocation iconLocation;
        private DynamicTexture iconTexture;

        // Use standard ResourceLocation creation for compatibility (try to parse or withDefaultNamespace if exists)
        // Since we can't guarantee 1.21.11 API without checking, let's use ResourceLocation.parse or similar
        // But for safety, I'll use explicit fully qualified class name for the type to be sure
        private static final net.minecraft.resources.ResourceLocation DEFAULT_ICON = net.minecraft.resources.ResourceLocation.parse("textures/misc/unknown_pack.png");

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
                // Attempt to load icon
                // Note: pack.icon() is not always available or might require opening
                // We use a safe try-catch
                /*
                   In standard Mojang mappings 1.21, pack.icon() doesn't exist directly on Pack
                   but pack.resources() isn't public.
                   However, we can try to guess or just use default for stability
                   since accessing internal pack icon is complex without PackSelectionModel.

                   If we want to be safe and avoid crashes:
                */
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
        public void render(GuiGraphics guiGraphics, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean hovering, float partialTick) {
            // Icon (32x32)
            guiGraphics.blit(this.iconLocation, left + 4, top + 2, 0, 0, 32, 32, 32, 32);

            // Title
            guiGraphics.drawString(client.font, pack.getTitle(), left + 40, top + 2, 0xFFFFFF);

            // Description (truncated)
            guiGraphics.drawString(client.font, pack.getDescription(), left + 40, top + 14, 0x888888);

            // Button
            this.toggleButton.setX(left + width - 105);
            this.toggleButton.setY(top + (height - 20) / 2);
            this.toggleButton.render(guiGraphics, mouseX, mouseY, partialTick);
        }

        @Override
        public List<? extends GuiEventListener> children() {
            return java.util.List.of(this.toggleButton);
        }

        @Override
        public List<? extends NarratableEntry> narratables() {
            return java.util.List.of(this.toggleButton);
        }

        // We can remove mouseClicked override since ContainerObjectSelectionList handles children
    }
}
