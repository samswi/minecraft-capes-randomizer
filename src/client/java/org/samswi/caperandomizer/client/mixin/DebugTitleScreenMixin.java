package org.samswi.caperandomizer.client.mixin;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import org.samswi.caperandomizer.client.CapeChoosingScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TitleScreen.class)
public abstract class DebugTitleScreenMixin extends Screen {
    protected DebugTitleScreenMixin(Text title) {
        super(title);
    }

    @Unique
    ButtonWidget myButton = ButtonWidget.builder(
            Text.of("hello"),
            (myButton) -> {
                if (this.client != null) {
                    this.client.setScreen(new CapeChoosingScreen());
                }
            }
    )
            .dimensions(0, 0, 40,40)
            .build();

    @Shadow protected abstract int addDevelopmentWidgets(int y, int spacingY);

    @Inject(at = @At("TAIL"), method = "init")
    protected void addDebugButton(CallbackInfo ci){
        TitleScreen thisObject = (TitleScreen) (Object)this;

        this.addDrawableChild(myButton);
    }
}
