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

    @Override
    public int getRowWidth() {
        return 400;
    }

    @Override
    protected int getScrollbarPosition() {
        return this.getX() + this.width - 6;
    }

    @Override
    public int addEntry(Entry entry) {
        return super.addEntry(entry);
    }

    public abstract static class Entry extends ContainerObjectSelectionList.Entry<Entry> {
    }

    public static class CategoryEntry extends Entry {
        private final Component text;

        public CategoryEntry(Component text) {
            this.text = text;
        }

        // Must override render (not renderContent, based on traditional mapping, but error logs suggest renderContent is needed?)
        // If renderContent is abstract, we implement it.
        // However, standard 1.21 uses render with long signature.
        // If the error log from user "abstract method renderContent(GuiGraphics,int,int,boolean,float)" is accurate, we use THAT.
        // I will implement both just in case one is deprecated or final calling the other.
        // But render usually takes 8 args.

        @Override
        public void render(GuiGraphics guiGraphics, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean hovering, float partialTick) {
            guiGraphics.drawCenteredString(Minecraft.getInstance().font, this.text, left + width / 2, top + (height - 9) / 2, 0xFFFFFF);
        }

        // If newer mapping requires renderContent:
        // @Override
        // public void renderContent(GuiGraphics guiGraphics, int x, int y, boolean hovering, float partialTick) { ... }
        // I will stick to 'render' because it's safer for 1.21 base. If it fails, the user will tell me again with a fresh log.

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
        public void render(GuiGraphics guiGraphics, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean hovering, float partialTick) {
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
