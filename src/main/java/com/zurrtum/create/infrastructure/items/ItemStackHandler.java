package com.zurrtum.create.infrastructure.items;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.*;
import com.zurrtum.create.foundation.codec.CreateCodecs;
import com.zurrtum.create.foundation.item.ItemHelper;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.apache.commons.lang3.mutable.MutableInt;

public class ItemStackHandler implements ItemInventory {
    public static final Codec<ItemStackHandler> CODEC = Codec.of(ItemStackHandler::encode, ItemStackHandler::decode);

    protected final NonNullList<ItemStack> stacks;

    public ItemStackHandler() {
        this(1);
    }

    public ItemStackHandler(int size) {
        stacks = NonNullList.withSize(size, ItemStack.EMPTY);
    }

    @Override
    public int getContainerSize() {
        return stacks.size();
    }

    @Override
    public ItemStack getItem(int slot) {
        if (slot >= getContainerSize()) {
            return ItemStack.EMPTY;
        }
        return stacks.get(slot);
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        if (slot >= getContainerSize()) {
            return;
        }
        stacks.set(slot, stack);
    }

    public NonNullList<ItemStack> getStacks() {
        return stacks;
    }

    public void writeSlots(ValueOutput view) {
        view.store("Inventory", CreateCodecs.ITEM_LIST_CODEC, stacks);
    }

    public void readSlots(ValueInput view) {
        view.read("Inventory", CreateCodecs.ITEM_LIST_CODEC).ifPresentOrElse(
            list -> {
                for (int i = 0, size = list.size(); i < size; i++) {
                    stacks.set(i, list.get(i));
                }
            }, stacks::clear
        );
    }

    public void write(ValueOutput view) {
        ItemHelper.writeStacks(view, "Inventory", stacks);
    }

    public void read(ValueInput view) {
        ItemHelper.readStacks(view, "Inventory", stacks);
    }

    private static <T> DataResult<T> encode(ItemStackHandler input, DynamicOps<T> ops, T prefix) {
        RecordBuilder<T> map = ops.mapBuilder();
        map.add("Size", ops.createInt(input.stacks.size()));
        ListBuilder<T> list = ops.listBuilder();
        int last = input.stacks.size();
        while (last > 0 && input.stacks.get(last - 1).isEmpty()) {
            last--;
        }
        for (int i = 0; i < last; i++) {
            list.add(ItemStack.OPTIONAL_CODEC.encodeStart(ops, input.stacks.get(i)));
        }
        map.add("Stacks", list.build(ops.empty()));
        return map.build(prefix);
    }

    private static <T> DataResult<Pair<ItemStackHandler, T>> decode(DynamicOps<T> ops, T input) {
        MapLike<T> map = ops.getMap(input).getOrThrow();
        int size = ops.getNumberValue(map.get("Size")).getOrThrow().intValue();
        ItemStackHandler handler = new ItemStackHandler(size);
        MutableInt i = new MutableInt();
        ops.getList(map.get("Stacks")).getOrThrow().accept(item -> {
            int slot = i.getAndIncrement();
            if (slot < size) {
                handler.stacks.set(
                    slot,
                    ItemStack.OPTIONAL_CODEC.parse(ops, item).result().orElse(ItemStack.EMPTY)
                );
            }
        });
        return DataResult.success(Pair.of(handler, ops.empty()));
    }
}
