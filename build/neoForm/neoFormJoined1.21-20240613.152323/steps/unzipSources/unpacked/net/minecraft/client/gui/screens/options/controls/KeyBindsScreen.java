package net.minecraft.client.gui.screens.options.controls;

import com.mojang.blaze3d.platform.InputConstants;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Options;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.OptionsSubScreen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class KeyBindsScreen extends OptionsSubScreen {
    private static final Component TITLE = Component.translatable("controls.keybinds.title");
    @Nullable
    public KeyMapping selectedKey;
    public long lastKeySelection;
    private KeyBindsList keyBindsList;
    private Button resetButton;

    public KeyBindsScreen(Screen pLastScreen, Options pOptions) {
        super(pLastScreen, pOptions, TITLE);
    }

    @Override
    protected void addContents() {
        this.keyBindsList = this.layout.addToContents(new KeyBindsList(this, this.minecraft));
    }

    @Override
    protected void addOptions() {
    }

    @Override
    protected void addFooter() {
        this.resetButton = Button.builder(Component.translatable("controls.resetAll"), p_346345_ -> {
            for (KeyMapping keymapping : this.options.keyMappings) {
                keymapping.setToDefault();
            }

            this.keyBindsList.resetMappingAndUpdateButtons();
        }).build();
        LinearLayout linearlayout = this.layout.addToFooter(LinearLayout.horizontal().spacing(8));
        linearlayout.addChild(this.resetButton);
        linearlayout.addChild(Button.builder(CommonComponents.GUI_DONE, p_345169_ -> this.onClose()).build());
    }

    @Override
    protected void repositionElements() {
        this.layout.arrangeElements();
        this.keyBindsList.updateSize(this.width, this.layout);
    }

    /**
     * Called when a mouse button is clicked within the GUI element.
     * <p>
     * @return {@code true} if the event is consumed, {@code false} otherwise.
     *
     * @param pMouseX the X coordinate of the mouse.
     * @param pMouseY the Y coordinate of the mouse.
     * @param pButton the button that was clicked.
     */
    @Override
    public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
        if (this.selectedKey != null) {
            this.options.setKey(this.selectedKey, InputConstants.Type.MOUSE.getOrCreate(pButton));
            this.selectedKey = null;
            this.keyBindsList.resetMappingAndUpdateButtons();
            return true;
        } else {
            return super.mouseClicked(pMouseX, pMouseY, pButton);
        }
    }

    /**
     * Called when a keyboard key is pressed within the GUI element.
     * <p>
     * @return {@code true} if the event is consumed, {@code false} otherwise.
     *
     * @param pKeyCode   the key code of the pressed key.
     * @param pScanCode  the scan code of the pressed key.
     * @param pModifiers the keyboard modifiers.
     */
    @Override
    public boolean keyPressed(int pKeyCode, int pScanCode, int pModifiers) {
        if (this.selectedKey != null) {
            if (pKeyCode == 256) {
                this.selectedKey.setKeyModifierAndCode(net.neoforged.neoforge.client.settings.KeyModifier.getActiveModifier(), InputConstants.UNKNOWN);
                this.options.setKey(this.selectedKey, InputConstants.UNKNOWN);
            } else {
                this.selectedKey.setKeyModifierAndCode(net.neoforged.neoforge.client.settings.KeyModifier.getActiveModifier(), InputConstants.getKey(pKeyCode, pScanCode));
                this.options.setKey(this.selectedKey, InputConstants.getKey(pKeyCode, pScanCode));
            }

            if(!net.neoforged.neoforge.client.settings.KeyModifier.isKeyCodeModifier(this.selectedKey.getKey()))
            this.selectedKey = null;
            this.lastKeySelection = Util.getMillis();
            this.keyBindsList.resetMappingAndUpdateButtons();
            return true;
        } else {
            return super.keyPressed(pKeyCode, pScanCode, pModifiers);
        }
    }

    /**
     * Renders the graphical user interface (GUI) element.
     *
     * @param pGuiGraphics the GuiGraphics object used for rendering.
     * @param pMouseX      the x-coordinate of the mouse cursor.
     * @param pMouseY      the y-coordinate of the mouse cursor.
     * @param pPartialTick the partial tick time.
     */
    @Override
    public void render(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        super.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
        boolean flag = false;

        for (KeyMapping keymapping : this.options.keyMappings) {
            if (!keymapping.isDefault()) {
                flag = true;
                break;
            }
        }

        this.resetButton.active = flag;
    }
}
