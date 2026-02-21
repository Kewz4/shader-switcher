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

    public int getRowWidth() {
        return 380;
    }

    protected int getScrollbarPosition() {
        return this.getX() + this.width - 6;
    }

    public void addEntryToWidget(PackEntry entry) {
        super.addEntry(entry);
    }

    // Public wrappers
    public int getRowTopAt(int index) {
        return super.getRowTop(index);
    }

    public int getRowLeftAt() {
        return super.getRowLeft();
    }

    public static class PackEntry extends ContainerObjectSelectionList.Entry<PackEntry> {
        private final Minecraft client;
        private final Pack pack;
        private final SwapConfig config;
        private final String shaderName;
        private final Button toggleButton;
        private final PackListWidget parent;

        public PackEntry(Minecraft client, Pack pack, SwapConfig config, String shaderName, PackListWidget parent) {
            this.client = client;
            this.pack = pack;
            this.config = config;
            this.shaderName = shaderName;
            this.parent = parent;

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
        public void renderContent(GuiGraphics guiGraphics, int mouseX, int mouseY, boolean hovering, float partialTick) {
            int index = parent.children().indexOf(this);
            int top = parent.getRowTopAt(index);
            int left = parent.getRowLeftAt();
            int width = parent.getRowWidth();

            guiGraphics.drawString(client.font, pack.getTitle(), left + 10, top + 2, 0xFFFFFF);
            guiGraphics.drawString(client.font, pack.getDescription(), left + 10, top + 14, 0x888888);

            this.toggleButton.setX(left + width - 105);
            this.toggleButton.setY(top + 8); // Center roughly
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
    }
}
