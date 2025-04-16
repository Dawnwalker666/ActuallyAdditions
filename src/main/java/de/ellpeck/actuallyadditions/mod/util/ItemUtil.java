/*
 * This file ("ItemUtil.java") is part of the Actually Additions mod for Minecraft.
 * It is created and owned by Ellpeck and distributed
 * under the Actually Additions License to be found at
 * http://ellpeck.de/actaddlicense
 * View the source code at https://github.com/Ellpeck/ActuallyAdditions
 *
 * © 2015-2017 Ellpeck
 */

package de.ellpeck.actuallyadditions.mod.util;

import de.ellpeck.actuallyadditions.mod.components.ActuallyComponents;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;

public final class ItemUtil {

    public static Item getItemFromName(String name) {
        return BuiltInRegistries.ITEM.get(ResourceLocation.tryParse(name));
    }

    //    public static boolean contains(ItemStack[] array, ItemStack stack, boolean checkWildcard) {
    //        return getPlaceAt(array, stack, checkWildcard) != -1;
    //    }
    //
    //    public static int getPlaceAt(ItemStack[] array, ItemStack stack, boolean checkWildcard) {
    //        return getPlaceAt(Arrays.asList(array), stack, checkWildcard);
    //    }
    //
    //    public static int getPlaceAt(List<ItemStack> list, ItemStack stack, boolean checkWildcard) {
    //        if (list != null && list.size() > 0) {
    //            for (int i = 0; i < list.size(); i++) {
    //                if (!StackUtil.isValid(stack) && !StackUtil.isValid(list.get(i)) || areItemsEqual(stack, list.get(i), checkWildcard)) {
    //                    return i;
    //                }
    //            }
    //        }
    //        return -1;
    //    }

    @Deprecated
    public static boolean areItemsEqual(ItemStack stack1, ItemStack stack2, boolean checkWildcard) {
        return ItemStack.isSameItem(stack1,stack2);
        //return StackUtil.isValid(stack1) && StackUtil.isValid(stack2) && (stack1.isItemEqual(stack2) || checkWildcard && stack1.getItem() == stack2.getItem() && (stack1.getItemDamage() == Util.WILDCARD || stack2.getItemDamage() == Util.WILDCARD));
    }

    //    /**
    //     * Returns true if list contains stack or if both contain null
    //     */
    //    public static boolean contains(List<ItemStack> list, ItemStack stack, boolean checkWildcard) {
    //        return !(list == null || list.isEmpty()) && getPlaceAt(list, stack, checkWildcard) != -1;
    //    }

    public static void removeEnchantment(ItemStack stack, Holder<Enchantment> e) {
        ItemEnchantments enchantments = stack.getTagEnchantments();
        ItemEnchantments.Mutable itemenchantments$mutable = new ItemEnchantments.Mutable(enchantments);

        itemenchantments$mutable.removeIf((enchantment) -> enchantment == e);

        EnchantmentHelper.setEnchantments(stack, itemenchantments$mutable.toImmutable());
    }

    public static boolean canBeStacked(ItemStack stack1, ItemStack stack2) {
        return ItemStack.isSameItemSameComponents(stack1, stack2);
    }

    public static boolean isEnabled(ItemStack stack) {
        return stack.getOrDefault(ActuallyComponents.ENABLED, false);
    }

    public static void changeEnabled(Player player, InteractionHand hand) {
        changeEnabled(player.getItemInHand(hand));
    }

    public static void changeEnabled(ItemStack stack) {
        boolean isEnabled = isEnabled(stack);
        stack.set(ActuallyComponents.ENABLED, !isEnabled);
    }
}
