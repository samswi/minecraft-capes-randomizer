package org.samswi.caperandomizer.client;

import com.google.gson.JsonElement;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.*;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ScrollableLayout;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.components.events.ContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.ReloadableTexture;
import net.minecraft.client.renderer.texture.TextureContents;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CapeChoosingScreen extends Screen {

    private ScrollableLayout scrollableLayoutWidget;
    GridLayout grid;
    GridLayout.RowHelper gridAdder;
    Screen oldScreen;
    boolean noCapes;
    final Minecraft client = Minecraft.getInstance();
    public final HeaderAndFooterLayout layout = new HeaderAndFooterLayout(this);

    public CapeChoosingScreen(Screen screen) {
        super(Component.nullToEmpty("hi hi hello :)"));

        layout.addTitleHeader(Component.nullToEmpty("Select favorite capes"), minecraft.font);
        this.layout.addToFooter(Button.builder(CommonComponents.GUI_DONE, (button) -> {
            this.onClose();
        }).width(200).build());
        grid = new GridLayout();
        grid.defaultCellSetting()
                        .padding(4);
        gridAdder = grid.createRowHelper(3);
        if (CapeRandomizerClient.favoriteCapes != null) {
            for (Map.Entry<String, JsonElement> entry : CapeRandomizerClient.favoriteCapes.getAsJsonObject("capes").entrySet()) {
                Cape cape = null;
                CapeWidget capeWidget;
                for (Cape i : CapeRandomizerClient.ownedCapesList) {
                    if (i.id.matches(entry.getKey())) cape = i;
                }
                if (cape != null) {
                    capeWidget = new CapeWidget(0, 0, 80, 136, CapeRandomizerClient.favoriteCapes.getAsJsonObject("capes").get(cape.id).getAsBoolean(), cape);
                    if (cape.id.equals(CapeRandomizerClient.defaultCape.id)) {
                        capeWidget.defaultButton.active = false;
                        capeWidget.defaultButton.setMessage(Component.nullToEmpty("Default"));
                        capeWidget.isDefault = true;
                    }
                    gridAdder.addChild(capeWidget);
                }
            }

            scrollableLayoutWidget = new ScrollableLayout(minecraft, grid, layout.getContentHeight());

            layout.addToContents(scrollableLayoutWidget);
        }
        else {
            noCapes = true;
            layout.addToContents(new StringWidget(Component.nullToEmpty("No capes found!"), minecraft.font));
        }
    }

    public void undefaultEverything(){
        grid.visitWidgets((element) -> {
            if (element instanceof CapeWidget) {
                ((CapeWidget) element).defaultButton.active = true;
                ((CapeWidget) element).isDefault = false;
                ((CapeWidget) element).defaultButton.setMessage(Component.nullToEmpty("Set default"));
            }
        });
    }

    @Override
    public void onClose() {
        minecraft.setScreen(oldScreen);
        try {
            CapeRandomizerClient.saveJsonToFile(CapeRandomizerClient.favoriteCapes, CapeRandomizerClient.favoriteCapesFile);
        } catch (Exception ignore) {}
        CapeRandomizerClient.refreshFavoriteCapes();
        new Thread(CapeRandomizerClient::equipRandomCape).start();
    }

    @Override
    protected void init() {
        super.init();

        layout.setPosition(0, 0);

        layout.arrangeElements();
        if (!noCapes) {
            scrollableLayoutWidget.setMaxHeight(layout.getContentHeight());
            scrollableLayoutWidget.arrangeElements();
            scrollableLayoutWidget.setPosition(scrollableLayoutWidget.getX(), layout.getHeaderHeight());
            grid.arrangeElements();
        }
        layout.visitWidgets(this::addRenderableWidget);

    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float deltaTicks) {
        context.blit(RenderPipelines.GUI_TEXTURED, Screen.FOOTER_SEPARATOR, 0, this.height - this.layout.getFooterHeight(), 0.0F, 0.0F, this.width, 2, 32, 2);
        context.blit(RenderPipelines.GUI_TEXTURED, Screen.HEADER_SEPARATOR, 0, this.layout.getHeaderHeight() - 2, 0.0F, 0.0F, this.width, 2, 32, 2);
        this.renderMenuBackground(context, 0, this.layout.getHeaderHeight(), this.width, layout.getContentHeight());
        super.render(context, mouseX, mouseY, deltaTicks);
    }

    public class CapeWidget extends AbstractButton implements ContainerEventHandler {

        boolean toggled;
        boolean isDefault;
        public Button defaultButton;
        public Cape associatedCape;
        ReloadableTexture capeTexture;
        List<GuiEventListener> children = new ArrayList<GuiEventListener>(1);

        public CapeWidget(int i, int j, int k, int l, boolean toggled, Cape cape) {
            super(i, j, k, l, Component.empty());
            this.toggled = toggled;
            this.associatedCape = cape;

            capeTexture = new ReloadableTexture(Identifier.parse("capes:" + associatedCape.id)) {
                @Override
                public TextureContents loadContents(ResourceManager resourceManager) throws IOException {
                    return new TextureContents(NativeImage.read(Files.newInputStream(Path.of(CapeRandomizerClient.capeTexturesFolder + "/" + associatedCape.id + ".png"))), null);
                }
            };
            Minecraft.getInstance().getTextureManager().registerAndLoad(Identifier.parse("capes:" + associatedCape.id), capeTexture);
            defaultButton = Button.builder(Component.nullToEmpty("Set default"), (button) -> {
                 undefaultEverything();
                CapeRandomizerClient.favoriteCapes.addProperty("default", associatedCape.id);
                this.isDefault = true;
                button.active = false;
                CapeRandomizerClient.defaultCape = associatedCape;
                button.setMessage(Component.nullToEmpty("Default"));
            }).size(70, 20).build();
             children.add(defaultButton);
        }

        @Override
        protected void renderContents(GuiGraphics context, int mouseX, int mouseY, float deltaTicks) {
            context.fill(getX() + 1, getY() + 1, getX() + getWidth() - 1, getY() + getHeight() - 1, toggled ? 0x4400FF00 : 0x44FF0000);
            defaultButton.setPosition(getX() + 5, getY() + 111);
            defaultButton.render(context, mouseX, mouseY, deltaTicks);
            context.blit(RenderPipelines.GUI_TEXTURED, Identifier.parse("capes:" + associatedCape.id), getX() + 10, getY() + 10, (float)1, (float)1, 60, 96, 10, 16, 64, 32, toggled ? 0xFFFFFFFF : 0x44FFFFFF);
        }

        @Override
        public void onPress(InputWithModifiers input) {
            toggled = !toggled;
            CapeRandomizerClient.favoriteCapes.getAsJsonObject("capes").addProperty(associatedCape.id, toggled);
        }

        @Override
        public void onClick(MouseButtonEvent click, boolean doubled) {
            if (defaultButton.mouseClicked(click, false)) {
                return;
            }
            onPress(click);
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput builder) {

        }

        @Override
        public List<? extends GuiEventListener> children() {
            return children;
        }

        @Override
        public boolean isDragging() {
            return false;
        }

        @Override
        public void setDragging(boolean dragging) {

        }

        @Override
        public @Nullable GuiEventListener getFocused() {
            return null;
        }

        @Override
        public void setFocused(@Nullable GuiEventListener focused) {

        }


    }


}