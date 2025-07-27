package dev.lucaargolo.charta.client.gui.components;

import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class CustomCheckboxWidgdet extends Checkbox {

    private OnValueChange onValueChange;
    public CustomCheckboxWidgdet(int x, int y, int width, int height, Component message, boolean selected) {
        super(x, y, width, height, message, selected);
        onValueChange = OnValueChange.NOP;
    }

    public void onPress() {
        super.onPress();
        onValueChange.onValueChange(this,this.selected());
    }

    public void setOnValueChange(OnValueChange func)
    {
        onValueChange = func;
    }

    @OnlyIn(Dist.CLIENT)
    public interface OnValueChange {
        OnValueChange NOP = (checkbox, selected) -> {
        };

        void onValueChange(Checkbox checkbox, boolean selected);
    }
}
