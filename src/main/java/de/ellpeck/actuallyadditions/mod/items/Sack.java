/*
 * This file ("ItemBag.java") is part of the Actually Additions mod for Minecraft.
 * It is created and owned by Ellpeck and distributed
 * under the Actually Additions License to be found at
 * http://ellpeck.de/actaddlicense
 * View the source code at https://github.com/Ellpeck/ActuallyAdditions
 *
 * © 2015-2017 Ellpeck
 */

package de.ellpeck.actuallyadditions.mod.items;

import de.ellpeck.actuallyadditions.mod.components.ActuallyComponents;
import de.ellpeck.actuallyadditions.mod.inventory.SackContainer;
import de.ellpeck.actuallyadditions.mod.inventory.VoidSackContainer;
import de.ellpeck.actuallyadditions.mod.items.base.ItemBase;
import de.ellpeck.actuallyadditions.mod.sack.SackData;
import de.ellpeck.actuallyadditions.mod.sack.SackManager;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;
import java.util.UUID;

public class Sack extends ItemBase {
    public final boolean isVoid;

    public Sack(boolean isVoid) {
        super(ActuallyItems.defaultProps().stacksTo(1));
        this.isVoid = isVoid;
    }

    @Nonnull
    @Override
    public InteractionResult useOn(UseOnContext context) {
        ItemStack stack = context.getPlayer().getItemInHand(context.getHand());
/*        if (!this.isVoid) { //TODO oh boy this is still old... needs to be converted to the new UUID system.
            Level level = context.getLevel();
            BlockPos clickedPos = context.getClickedPos();
            BlockEntity tile = level.getBlockEntity(clickedPos);
            if (tile != null) {
                if (!context.getLevel().isClientSide) {
                    ItemStackHandlerAA inv = new ItemStackHandlerAA(28);

                    boolean changed = Optional.ofNullable(level.getCapability(Capabilities.ItemHandler.BLOCK, clickedPos, context.getClickedFace()))
                        .map(cap -> {
                            boolean localChanged = false;
                            DrillItem.loadSlotsFromNBT(inv, stack);

                            for (int j = 0; j < inv.getSlots(); j++) {
                                ItemStack invStack = inv.getStackInSlot(j);
                                if (!invStack.isEmpty()) {
                                    for (int i = 0; i < cap.getSlots(); i++) {
                                        ItemStack remain = cap.insertItem(i, invStack, false);
                                        if (!ItemStack.matches(remain, invStack)) {
                                            inv.setStackInSlot(j, remain.copy());
                                            localChanged = true;
                                            if (remain.isEmpty()) {
                                                break;
                                            }
                                            invStack = remain;
                                        }
                                    }
                                }
                            }

                            return localChanged;
                        }).orElse(false);

                    if (changed) {
                        DrillItem.writeSlotsToNBT(inv, stack);
                    }
                }
                return InteractionResult.SUCCESS;
            }
        }*/
        return InteractionResult.PASS;
    }

    @Nonnull
    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, @Nonnull InteractionHand hand) {
        ItemStack sackStack = player.getItemInHand(hand);
        if (!world.isClientSide && hand == InteractionHand.MAIN_HAND && sackStack.getItem() instanceof Sack && player instanceof ServerPlayer) {

            if (!isVoid) {
                SackData data = getData(sackStack);
                if (data == null)
                    return InteractionResultHolder.fail(sackStack);

                UUID uuid = data.getUuid();

                data.updateAccessRecords(player.getName().getString(), System.currentTimeMillis());

                boolean autoInsert = sackStack.getOrDefault(ActuallyComponents.AUTO_INSERT, false);
                player.openMenu(new SimpleMenuProvider((id, inv, entity) ->
                        new SackContainer(id, inv, data.getSpecialHandler(), autoInsert, data.getFilter()), sackStack.getHoverName()), (buffer -> buffer.writeUUID(uuid).writeBoolean(autoInsert).writeInt(data.getFilter().getPackedSettings())));
            } else
                player.openMenu(new SimpleMenuProvider((id, inv, entity) -> new VoidSackContainer(id, inv), sackStack.getHoverName()));
        }
        return InteractionResultHolder.pass(player.getItemInHand(hand));
    }

    public static SackData getData(ItemStack stack) {
        if (!(stack.getItem() instanceof Sack))
            return null;

        UUID uuid;

        if (stack.has(ActuallyComponents.UUID))
            uuid = stack.get(ActuallyComponents.UUID);
        else if(stack.has(DataComponents.CUSTOM_DATA)) {
            CompoundTag tag = stack.get(DataComponents.CUSTOM_DATA).copyTag();
            if (tag.contains("UUID")) {
                uuid = tag.getUUID("UUID");
                stack.set(ActuallyComponents.UUID, uuid);
                stack.update(DataComponents.CUSTOM_DATA, CustomData.EMPTY, $ -> $.update(compoundTag -> compoundTag.remove("UUID")));
            } else {
                uuid = UUID.randomUUID();
                stack.set(ActuallyComponents.UUID, uuid);
            }
        }
        else {
            uuid = UUID.randomUUID();
            stack.set(ActuallyComponents.UUID, uuid);
        }

        return SackManager.get().getOrCreateSack(uuid);
    }
}
