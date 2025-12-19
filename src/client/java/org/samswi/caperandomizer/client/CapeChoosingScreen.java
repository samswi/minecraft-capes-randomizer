package org.samswi.caperandomizer.client;

import com.google.gson.JsonElement;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.*;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.tab.TabManager;
import net.minecraft.client.gui.widget.*;
import net.minecraft.client.input.AbstractInput;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.ReloadableTexture;
import net.minecraft.client.texture.TextureContents;
import net.minecraft.client.util.Icons;
import net.minecraft.resource.ResourceManager;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CapeChoosingScreen extends Screen {

    private ScrollableLayoutWidget scrollableLayoutWidget;
    GridWidget grid;
    GridWidget.Adder gridAdder;
    Screen oldScreen;
    boolean noCapes;
    final MinecraftClient client = MinecraftClient.getInstance();
    public final ThreePartsLayoutWidget layout = new ThreePartsLayoutWidget(this);

    public CapeChoosingScreen(Screen screen) {
        super(Text.of("hi hi hello :)"));

        layout.addHeader(Text.of("Select favorite capes"), client.textRenderer);
        this.layout.addFooter(ButtonWidget.builder(ScreenTexts.DONE, (button) -> {
            this.close();
        }).width(200).build());
        grid = new GridWidget();
        grid.getMainPositioner()
                        .margin(4);
        gridAdder = grid.createAdder(3);
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
                        capeWidget.defaultButton.setMessage(Text.of("Default"));
                        capeWidget.isDefault = true;
                    }
                    gridAdder.add(capeWidget);
                }
            }

            scrollableLayoutWidget = new ScrollableLayoutWidget(client, grid, layout.getContentHeight());

            layout.addBody(scrollableLayoutWidget);
        }
        else {
            noCapes = true;
            layout.addBody(new TextWidget(Text.of("No capes found!"), client.textRenderer));
        }
    }

    public void undefaultEverything(){
        grid.forEachChild((element) -> {
            if (element instanceof CapeWidget) {
                ((CapeWidget) element).defaultButton.active = true;
                ((CapeWidget) element).isDefault = false;
                ((CapeWidget) element).defaultButton.setMessage(Text.of("Set default"));
            }
        });
    }

    @Override
    public void close() {
        client.setScreen(oldScreen);
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

        layout.refreshPositions();
        if (!noCapes) {
            scrollableLayoutWidget.setHeight(layout.getContentHeight());
            scrollableLayoutWidget.refreshPositions();
            scrollableLayoutWidget.setPosition(scrollableLayoutWidget.getX(), layout.getHeaderHeight());
            grid.refreshPositions();
        }
        layout.forEachChild(this::addDrawableChild);

    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        context.drawTexture(RenderPipelines.GUI_TEXTURED, Screen.FOOTER_SEPARATOR_TEXTURE, 0, this.height - this.layout.getFooterHeight(), 0.0F, 0.0F, this.width, 2, 32, 2);
        context.drawTexture(RenderPipelines.GUI_TEXTURED, Screen.HEADER_SEPARATOR_TEXTURE, 0, this.layout.getHeaderHeight() - 2, 0.0F, 0.0F, this.width, 2, 32, 2);
        this.renderDarkening(context, 0, this.layout.getHeaderHeight(), this.width, layout.getContentHeight());
        super.render(context, mouseX, mouseY, deltaTicks);
    }

    public class CapeWidget extends PressableWidget implements ParentElement {

        boolean toggled;
        boolean isDefault;
        public ButtonWidget defaultButton;
        public Cape associatedCape;
        ReloadableTexture capeTexture;
        List<Element> children = new ArrayList<Element>(1);

        public CapeWidget(int i, int j, int k, int l, boolean toggled, Cape cape) {
            super(i, j, k, l, Text.empty());
            this.toggled = toggled;
            this.associatedCape = cape;

            capeTexture = new ReloadableTexture(Identifier.of("capes:" + associatedCape.id)) {
                @Override
                public TextureContents loadContents(ResourceManager resourceManager) throws IOException {
                    return new TextureContents(NativeImage.read(Files.newInputStream(Path.of(CapeRandomizerClient.capeTexturesFolder + "/" + associatedCape.id + ".png"))), null);
                }
            };
            MinecraftClient.getInstance().getTextureManager().registerTexture(Identifier.of("capes:" + associatedCape.id), capeTexture);
            defaultButton = ButtonWidget.builder(Text.of("Set default"), (button) -> {
                 undefaultEverything();
                CapeRandomizerClient.favoriteCapes.addProperty("default", associatedCape.id);
                this.isDefault = true;
                button.active = false;
                CapeRandomizerClient.defaultCape = associatedCape;
                button.setMessage(Text.of("Default"));
            }).size(70, 20).build();
             children.add(defaultButton);
        }

        @Override
        protected void drawIcon(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
            context.fill(getX() + 1, getY() + 1, getX() + getWidth() - 1, getY() + getHeight() - 1, toggled ? 0x4400FF00 : 0x44FF0000);
            defaultButton.setPosition(getX() + 5, getY() + 111);
            defaultButton.render(context, mouseX, mouseY, deltaTicks);
            context.drawTexture(RenderPipelines.GUI_TEXTURED, Identifier.of("capes:" + associatedCape.id), getX() + 10, getY() + 10, (float)1, (float)1, 60, 96, 10, 16, 64, 32, toggled ? 0xFFFFFFFF : 0x44FFFFFF);
        }

        @Override
        public void onPress(AbstractInput input) {
            toggled = !toggled;
            CapeRandomizerClient.favoriteCapes.getAsJsonObject("capes").addProperty(associatedCape.id, toggled);
        }

        @Override
        public void onClick(Click click, boolean doubled) {
            if (defaultButton.mouseClicked(click, false)) {
                return;
            }
            onPress(click);
        }

        @Override
        protected void appendClickableNarrations(NarrationMessageBuilder builder) {

        }

        @Override
        public List<? extends Element> children() {
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
        public @Nullable Element getFocused() {
            return null;
        }

        @Override
        public void setFocused(@Nullable Element focused) {

        }


    }


}