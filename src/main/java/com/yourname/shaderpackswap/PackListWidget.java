package com.yourname.shaderpackswap;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.repository.Pack;

import java.util.List;

public class PackListWidget extends ContainerObjectSelectionList<PackListWidget.PackEntry> {

    public PackListWidget(Minecraft client, int width, int height, int y, int itemHeight) {
        super(client, width, height, y, itemHeight);
    }

    // Removed @Override because it doesn't override anything in 1.21.11
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

        public PackEntry(Minecraft client, Pack pack, SwapConfig config, String shaderName) {
            this.client = client;
            this.pack = pack;
            this.config = config;
            this.shaderName = shaderName;

            this.toggleButton = Button.builder(Component.literal(""), button -> {
                cycleState();
                updateButtonLabel();
            }).bounds(0, 0, 100, 20).build();
            updateButtonLabel();
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
            guiGraphics.drawString(client.font, pack.getTitle(), x + 10, y + 2, 0xFFFFFF);
            guiGraphics.drawString(client.font, pack.getDescription(), x + 10, y + 14, 0x888888);

            this.toggleButton.setX(x + 380 - 105);
            this.toggleButton.setY(y + (36 - 20) / 2);
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
