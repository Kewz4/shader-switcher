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

    public int getRowWidth() {
        return 400;
    }

    protected int getScrollbarPosition() {
        return this.getX() + this.width - 6;
    }

    public void addEntryToWidget(Entry entry) {
        super.addEntry(entry);
    }

    public int getRowTopAt(int index) {
        return super.getRowTop(index);
    }

    public int getRowLeftAt() {
        return super.getRowLeft();
    }

    public abstract static class Entry extends ContainerObjectSelectionList.Entry<Entry> {
    }

    public static class CategoryEntry extends Entry {
        private final Component text;
        private final ConfigListWidget parent;

        public CategoryEntry(Component text, ConfigListWidget parent) {
            this.text = text;
            this.parent = parent;
        }

        @Override
        public void renderContent(GuiGraphics guiGraphics, int mouseX, int mouseY, boolean hovering, float partialTick) {
            int index = parent.children().indexOf(this);
            int top = parent.getRowTopAt(index);
            int left = parent.getRowLeftAt();
            int width = parent.getRowWidth();

            // Fix: Push pose and translate Z to ensure text renders above list background
            guiGraphics.pose().pushPose();
            guiGraphics.pose().translate(0, 0, 1);

            guiGraphics.drawCenteredString(Minecraft.getInstance().font, this.text, left + width / 2, top + 2, 0xFFFFFF);

            guiGraphics.pose().popPose();
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
        private final ConfigListWidget parent;

        public ProfileEntry(Component text, Button.OnPress onPress, ConfigListWidget parent) {
            this.parent = parent;
            this.button = Button.builder(text, onPress)
                    .bounds(0, 0, 260, 20)
                    .build();
        }

        @Override
        public void renderContent(GuiGraphics guiGraphics, int mouseX, int mouseY, boolean hovering, float partialTick) {
            int index = parent.children().indexOf(this);
            int top = parent.getRowTopAt(index);
            int left = parent.getRowLeftAt();
            int width = parent.getRowWidth();

            this.button.setX(left + (width - this.button.getWidth()) / 2);
            this.button.setY(top);
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
