package com.zurrtum.create.content.processing.basin;

import com.zurrtum.create.foundation.item.ItemHelper;
import com.zurrtum.create.infrastructure.items.ItemInventory;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class BasinInventory implements ItemInventory {
    private final BasinBlockEntity blockEntity;
    private final NonNullList<ItemStack> stacks = NonNullList.withSize(18, ItemStack.EMPTY);
    private boolean check = true;

    public BasinInventory(BasinBlockEntity be) {
        blockEntity = be;
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        if (check) {
            if (slot > 9) {
                return false;
            }
            for (int i = 0; i < slot; i++) {
                ItemStack itemStack = stacks.get(i);
                if (itemStack.isEmpty() || ItemStack.isSameItemSameComponents(itemStack, stack)) {
                    return false;
                }
            }
        }
        return true;
    }

    public void disableCheck() {
        check = false;
    }

    public void enableCheck() {
        check = true;
    }

    @Override
    public int getContainerSize() {
        return 18;
    }

    @Override
    public ItemStack getItem(int slot) {
        if (slot >= 18) {
            return ItemStack.EMPTY;
        }
        return stacks.get(slot);
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        if (slot >= 18) {
            return;
        }
        stacks.set(slot, stack);
    }

    @Override
    public void setChanged() {
        blockEntity.notifyChangeOfContents();
        blockEntity.notifyUpdate();
    }

    public void write(ValueOutput view) {
        ValueOutput inventory = view.child("Inventory");
        ItemHelper.writeStacks(inventory, "Input", stacks.subList(0, 9));
        ItemHelper.writeStacks(inventory, "Output", stacks.subList(9, 18));
    }

    public void read(ValueInput view) {
        view.child("Inventory").ifPresentOrElse(
            inventory -> {
                ItemHelper.readStacks(inventory, "Input", stacks.subList(0, 9));
                ItemHelper.readStacks(inventory, "Output", stacks.subList(9, 18));
            }, stacks::clear
        );
    }
}
