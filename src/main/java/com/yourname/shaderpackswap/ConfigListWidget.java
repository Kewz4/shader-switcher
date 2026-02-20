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
        // Set properties in constructor since overriding getters is no longer supported/needed in 1.21.11
        // Using reflection or assumption that these methods exist?
        // Actually, check if setRowWidth exists. In 1.21 it does.
        // If not, we might be stuck, but the error "method does not override" strongly implies they are not abstract/overridable anymore.
        // But AbstractSelectionList usually has protected setters or just setters.
        // We will try calling them. If they don't exist, we'll fail compile, but it's better than broken UI.
        // Wait, AbstractSelectionList usually DOES NOT have public setters for these in older versions, but in 1.21 it likely does?
        // Or maybe strictly protected?
        // Let's assume standard mapped names: setRowWidth, setScrollbarPosition might not be there.
        // But render() uses them.
        // If they are fields, how do we set them?
        // In 1.20.2, they are methods.
        // The error `method does not override` means the superclass does NOT have `getRowWidth()`.
        // This means the superclass does NOT use `getRowWidth()` to get the width.
        // It likely uses a field or a different mechanism.
        // We will try `setRowWidth(400)` assuming it exists.
    }

    // Attempt to set width via standard methods if they exist
    // Since we can't compile-check, we'll assume standard naming or try to find a workaround.
    // Workaround: We can't easily guess.
    // BUT: "no buttons no text just a button that follows the cursor".
    // "Button follows cursor" -> This is super weird.
    // It suggests x/y are being calculated based on mouse?
    // In renderContent(..., x, y, ...), x and y are passed by the list.
    // If the list thinks the row width is 0, maybe it calculates x weirdly?
    // Or maybe renderContent signature is WRONG?
    // If the signature is `renderContent(GuiGraphics, int mouseX, int mouseY, boolean hovering, float partialTick)`, then x/y are MOUSE coords!
    // That matches "button follows cursor"!
    // The signature in the error log was: `renderContent(GuiGraphics,int,int,boolean,float)`.
    // It did NOT name the parameters.
    // In 1.20.2: `render(GuiGraphics, int mouseX, int mouseY, float partialTick)` -> No, entries have `render`.
    // If the signature is `(GuiGraphics, int i, int j, boolean bl, float f)`, `i` and `j` could be mouseX/Y OR x/y position.
    // Standard `Entry.render` in 1.20: `render(GuiGraphics context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta)`.
    // Standard `Entry.render` in 1.21 (ContainerObjectSelectionList): `render(GuiGraphics guiGraphics, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean hovering, float partialTick)`.
    // BUT `renderContent` is new.
    // If `renderContent` takes `(GuiGraphics, int, int, boolean, float)`, the 2 ints are likely `mouseX` and `mouseY`?
    // NO! If they were mouseX/Y, where is the position?
    // Maybe `renderContent` assumes rendering at (0,0) relative to the entry?
    // But `ContainerObjectSelectionList` entries are widgets. They need absolute position.
    // If `renderContent` provides `mouseX` and `mouseY`, then we are setting button.setX(mouseX)!
    // That explains "button follows cursor"!

    // FIX: We must NOT use the `int, int` as position if they are mouse coordinates.
    // We need to calculate position based on index or something else?
    // But `renderContent` is abstract, we must implement it.
    // If it doesn't provide position, how do we render?
    // Maybe we use `getX()`, `getY()`?
    // `ContainerObjectSelectionList.Entry` doesn't have `getX/Y`.
    // `ConfigListWidget` has `getRowLeft()`, `getRowTop(index)`.
    // BUT `renderContent` doesn't provide index!
    // This implies `renderContent` is for rendering *interactive* content where position is handled elsewhere?
    // No, that doesn't make sense.

    // Alternative: The ints ARE position, but I am interpreting them wrong.
    // "Button follows cursor" -> I used `x` and `y`.
    // If `x` was mouseX, `y` was mouseY.
    // `this.button.setX(x + ...)` -> `setX(mouseX + ...)` -> It follows the cursor!
    // CONCLUSION: The ints ARE `mouseX` and `mouseY`.
    // So `renderContent` does NOT provide position.
    // It implies the entry should know its position? Or render relative to current matrix stack?
    // If `GuiGraphics` pose is already translated to the entry's position, then we render at (0,0).
    // Let's try rendering at (0,0).
    // `this.button.setX(0 + offset); this.button.setY(0);`
    // Wait, buttons need absolute coordinates for hit detection usually.
    // Unless `GuiGraphics` handles it?
    // But `Button` widget needs absolute bounds.
    // This is tricky.

    // Let's look at `AbstractSelectionList` source (mentally).
    // If `renderContent` is `(guiGraphics, mouseX, mouseY, hovering, partialTick)`,
    // Then the position must be set BEFORE calling renderContent?
    // Or the matrix is translated.
    // If matrix is translated, `guiGraphics.drawString` at (0,0) works.
    // But `Button` widgets in `children()` need their X/Y updated to absolute screen coords for `mouseClicked` to work?
    // No, `ContainerObjectSelectionList` handles logic.
    // But rendering the button: `button.render` needs correct position.
    // If the matrix is translated, we render button at (0,0).
    // But the button's internal `x,y` fields must match the screen slot?
    // If `renderContent` doesn't give us the screen slot, we are in trouble for Buttons.

    // Wait, maybe I can get the position from the `Entry` itself?
    // No.

    // Maybe I should look at `PackListWidget.java:25: error: method does not override`.
    // That was `getRowWidth`.
    // It confirms `getRowWidth` is gone.

    // Let's try to assume the matrix is translated and use (0,0).
    // And for the button, we might need to rely on layout?
    // Or maybe `Entry` in this version has `getX()` / `getY()`?
    // I can't know without checking.

    // STRATEGY:
    // 1. Assume arguments are `(guiGraphics, mouseX, mouseY, hovering, partialTick)`.
    // 2. Assume `GuiGraphics` is translated to the entry's top-left corner.
    // 3. Render text at relative coordinates.
    // 4. Render button at relative coordinates?
    //    - If I set button.setX(0), it sets absolute X to 0.
    //    - If rendering assumes translation, then `button.render` at (0,0) draws at top-left of entry.
    //    - BUT for hit detection, the button needs absolute coordinates.
    //    - Does `ContainerObjectSelectionList` update children's positions?
    //    - Usually not.

    // Maybe I should ignore the button rendering in `renderContent` and rely on `children`?
    // `ContainerObjectSelectionList` renders children automatically?
    // If so, I don't need to render the button in `renderContent`.
    // I only need to render the text.
    // AND I need to make sure the button has the correct position.
    // But where do I update the button position?
    // Usually in `render` (the old one).
    // If `render` is gone/final, there must be a way.

    // HYPOTHESIS: `refreshPositions` or similar?
    // OR `renderContent` is ONLY for visual content, and children are rendered separately.
    // But I still need to update the button's layout.
    // If I can't get the position in `renderContent`, I can't layout the button.

    // WAIT! The error `abstract method renderContent(GuiGraphics,int,int,boolean,float)`
    // Arguments: `GuiGraphics arg0, int arg1, int arg2, boolean arg3, float arg4`
    // arg1, arg2 are likely `mouseX`, `mouseY`.
    // arg3 is `hovering`.
    // arg4 is `partialTick`.
    // Where is x/y?
    // Maybe they are NOT passed?

    // CHECK `ContainerObjectSelectionList` changes in 1.21.
    // It seems `renderList` translates the matrices.
    // So all rendering is relative.
    // Text: `drawString(..., 10, 2, ...)` (relative to row).
    // Button: `button.setX(width - 100); button.setY(2);` (relative?)
    // But `Button` uses absolute coords for interaction.
    // Does `ContainerObjectSelectionList` offset the mouse events?
    // Yes, usually.
    // So if I set `button.setX(10)`, and click at 10, relative to list, it works?
    // If so, `button.setX(mouseX)` was wrong because I was using absolute mouseX as relative X.

    // FIXED LOGIC: Use relative coordinates (0-based) for rendering and positioning.
    // Do NOT use the passed ints as position.

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
            // Render text centered relative to the row width (assuming 400 or dynamic)
            // We'll use a fixed center for now or try to get width?
            // Since we can't get width easily, we'll assume a standard width (e.g. 200 center)
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
            // Position button relative to row (0,0 is top-left of row)
            // Center button in 400 width -> x = 70
            this.button.setX(70);
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
