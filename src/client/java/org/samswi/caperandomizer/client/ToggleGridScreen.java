package org.samswi.caperandomizer.client;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.GridWidget;
import net.minecraft.client.gui.widget.PressableWidget;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

public class ToggleGridScreen extends Screen {

    public ToggleGridScreen() {
        super(Text.literal("Toggle Grid Example"));
    }

    @Override
    protected void init() {
        super.init();

        GridWidget grid = new GridWidget();
        grid.getMainPositioner()
                .margin(4)
                .alignHorizontalCenter();

        GridWidget.Adder gridAdder = grid.createAdder(2);

        for (int i = 0; i < 12; i++) {
            gridAdder.add(new ToggleButtonWidget(0, 0, 80, 20, "Toggle " + i));
        }

        grid.refreshPositions();
        grid.setX((this.width - grid.getWidth()) / 2);
        grid.setY((this.height - grid.getHeight()) / 2);

        grid.forEachChild(this::addDrawableChild);
    }

    // Custom toggle button
    class ToggleButtonWidget extends PressableWidget {
        private boolean toggled;

        public ToggleButtonWidget(int x, int y, int width, int height, String label) {
            super(x, y, width, height, Text.literal(label));
        }

        @Override
        public void onPress() {
            toggled = !toggled;
        }

        @Override
        public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
            int color = toggled ? 0xFF00FF00 : 0xFFFF0000; // green if on, red if off
            context.fill(getX(), getY(), getX() + width, getY() + height, color);
            context.drawCenteredTextWithShadow(textRenderer, getMessage(), getX() + width / 2, getY() + (height - 8) / 2, 0xFFFFFF);
        }

        @Override
        protected void appendClickableNarrations(NarrationMessageBuilder builder) {

        }
    }
}