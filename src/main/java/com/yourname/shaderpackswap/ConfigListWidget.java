package com.yourname.shaderpackswap;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.network.chat.Component;

import java.util.List;

// Switched to ObjectSelectionList to avoid ContainerObjectSelectionList issues with render/renderContent
public class ConfigListWidget extends ObjectSelectionList<ConfigListWidget.Entry> {

    public ConfigListWidget(Minecraft client, int width, int height, int y, int itemHeight) {
        super(client, width, height, y, itemHeight);
    }

    public int getRowWidth() {
        return 400;
    }

    protected int getScrollbarPosition() {
        return this.getX() + this.width - 6;
    }

    public void addEntryToWidget(Entry entry) {
        super.addEntry(entry);
    }

    public abstract static class Entry extends ObjectSelectionList.Entry<Entry> {
    }

    public static class CategoryEntry extends Entry {
        private final Component text;

        public CategoryEntry(Component text) {
            this.text = text;
        }

        @Override
        public void render(GuiGraphics guiGraphics, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean hovering, float partialTick) {
            guiGraphics.drawCenteredString(Minecraft.getInstance().font, this.text, left + width / 2, top + (height - 9) / 2, 0xFFFFFF);
        }

        @Override
        public Component getNarration() {
            return text;
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            return false;
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
        public void render(GuiGraphics guiGraphics, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean hovering, float partialTick) {
            this.button.setX(left + (width - this.button.getWidth()) / 2);
            this.button.setY(top);
            this.button.render(guiGraphics, mouseX, mouseY, partialTick);
        }

        @Override
        public Component getNarration() {
            return button.getMessage();
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (this.button.mouseClicked(mouseX, mouseY, button)) {
                return true;
            }
            return super.mouseClicked(mouseX, mouseY, button);
        }
    }
}
