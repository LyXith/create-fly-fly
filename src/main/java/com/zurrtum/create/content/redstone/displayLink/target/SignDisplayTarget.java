package com.zurrtum.create.content.redstone.displayLink.target;

import net.minecraft.world.level.block.entity.SignTextSlot;
import com.zurrtum.create.api.behaviour.display.DisplayHolder;
import com.zurrtum.create.api.behaviour.display.DisplayTarget;
import com.zurrtum.create.catnip.data.Couple;
import com.zurrtum.create.content.redstone.displayLink.DisplayLinkContext;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.entity.SignText;

import java.util.List;

public class SignDisplayTarget extends DisplayTarget {

    @Override
    public void acceptText(int line, List<MutableComponent> text, DisplayLinkContext context) {
        BlockEntity be = context.getTargetBlockEntity();
        if (!(be instanceof SignBlockEntity sign)) {
            return;
        }

        boolean changed = false;
        Couple<SignText> signText = Couple.createWithContext(front -> sign.getText(front ? SignTextSlot.FRONT : SignTextSlot.BACK));
        DisplayHolder holder = (DisplayHolder) sign;
        for (int i = 0; i < text.size() && i + line < 4; i++) {
            if (i == 0) {
                reserve(line, holder, context);
            }
            if (i > 0 && isReserved(i + line, holder, context)) {
                break;
            }

            final int iFinal = i;
            String content = text.get(iFinal).getString(sign.getMaxTextLineWidth());
            signText = signText.map(st -> st.asMutable().setLine(iFinal + line, Component.literal(content)).asImmutable());
            changed = true;
        }

        if (changed) {
            signText.forEachWithContext((st, front) -> sign.setText(st, front ? SignTextSlot.FRONT : SignTextSlot.BACK));
            context.level().sendBlockUpdated(context.getTargetPos(), sign.getBlockState(), sign.getBlockState(), 2);
        }
    }

    @Override
    public DisplayTargetStats provideStats(DisplayLinkContext context) {
        return new DisplayTargetStats(4, 15, this);
    }

    @Override
    public boolean requiresComponentSanitization() {
        return true;
    }

}
