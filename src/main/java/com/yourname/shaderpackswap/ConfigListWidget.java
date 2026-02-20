package com.yourname.shaderpackswap;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.network.chat.Component;

import java.util.List;

public class ConfigListWidget extends ContainerObjectSelectionList<ConfigListWidget.Entry> {

    public ConfigListWidget(Minecraft client, int width, int height, int y, int itemHeight) {
        super(client, width, height, y, itemHeight);
    }

    // Removed @Override because it doesn't override anything in 1.21.11
    public int getRowWidth() {
        return 400;
    }

    // Removed @Override because it might not override anything in 1.21.11
    protected int getScrollbarPosition() {
        return this.getX() + this.width - 6;
    }

    public void addEntryToWidget(Entry entry) {
        super.addEntry(entry);
    }

    public abstract static class Entry extends ContainerObjectSelectionList.Entry<Entry> {
    }

    public static class CategoryEntry extends Entry {
        private final Component text;

        public CategoryEntry(Component text) {
            this.text = text;
        }

        @Override
        public void renderContent(GuiGraphics guiGraphics, int x, int y, boolean hovering, float partialTick) {
            guiGraphics.drawCenteredString(Minecraft.getInstance().font, this.text, x + 200, y + 2, 0xFFFFFF);
        }

        @Override
        public List<? extends GuiEventListener> children() {
            return List.of();
        }

        @Override
        public List<? extends NarratableEntry> narratables() {
            return List.of();
        }
    }

    public static class ProfileEntry extends Entry {
        private final Button button;

        public ProfileEntry(Component text, Button.OnPress onPress) {
            this.button = Button.builder(text, onPress)
                    .bounds(0, 0, 260, 20)
                    .build();
        }

        @Override
        public void renderContent(GuiGraphics guiGraphics, int x, int y, boolean hovering, float partialTick) {
            this.button.setX(x + (400 - this.button.getWidth()) / 2);
            this.button.setY(y);
            this.button.render(guiGraphics, 0, 0, partialTick);
        }

        @Override
        public List<? extends GuiEventListener> children() {
            return List.of(this.button);
        }

        @Override
        public List<? extends NarratableEntry> narratables() {
            return List.of(this.button);
        }
    }
}
