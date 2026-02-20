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
        // Attempt to set row width if possible, or rely on defaults.
        // In 1.21, layout might be different.
        // If the list is blank, it might be 0 width.
        // We can try to add entries and see.
    }

    // Define without @Override to act as getter if supported or property
    public int getRowWidth() {
        return 400;
    }

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
        public void renderContent(GuiGraphics guiGraphics, int mouseX, int mouseY, boolean hovering, float partialTick) {
            // Assume matrix translated to entry position.
            // Render text centered relative to assumed row width (400)
            guiGraphics.drawCenteredString(Minecraft.getInstance().font, this.text, 200, 2, 0xFFFFFF);
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
        public void renderContent(GuiGraphics guiGraphics, int mouseX, int mouseY, boolean hovering, float partialTick) {
            // Relative positioning
            this.button.setX(70); // 400/2 - 260/2 = 70
            this.button.setY(0);
            this.button.render(guiGraphics, mouseX, mouseY, partialTick);
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
