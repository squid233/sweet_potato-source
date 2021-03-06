package com.github.teddyxlandlee.sweet_potato.items;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public abstract class SweetPotatoItem extends Item {
    public boolean isFood() {
        return true;
    }

    public SweetPotatoItem(Settings settings) {
        super(settings);
    }

    /*
    public ItemStack finishUsing(ItemStack stack, World world, LivingEntity user) {
        ItemStack itemStack = super.finishUsing(stack, world, user);
        ItemStack survivalItemStack;
        if (Objects.equals(itemStack.getItem().getFoodComponent(), RAW))
        //if ((Objects.equals(itemStack.getItem(), ExampleMod.PURPLE_POTATO)) ||
        //        (Objects.equals(itemStack.getItem(), ExampleMod.WHITE_POTATO)) ||
        //        (Objects.equals(itemStack.getItem(), ExampleMod.RED_POTATO)))
            survivalItemStack = new ItemStack(ExampleMod.PEEL);
        else if (Objects.equals(itemStack.getItem().getFoodComponent(), BAKED))
        //else if ((Objects.equals(itemStack.getItem(), ExampleMod.BAKED_PURPLE_POTATO)) ||
        //        (Objects.equals(itemStack.getItem(), ExampleMod.BAKED_WHITE_POTATO)) ||
        //        (Objects.equals(itemStack.getItem(), ExampleMod.BAKED_RED_POTATO)))
            survivalItemStack = new ItemStack(ExampleMod.BAKED_PEEL);
        else {
            survivalItemStack = new ItemStack(Items.AIR);
            System.out.println("Error: is it really a sweet potato item? You maybe changed the food component.");
        }

        return user instanceof PlayerEntity && ((PlayerEntity)user).abilities.creativeMode
                ? itemStack : survivalItemStack;
    }
     */
    public ItemStack finishUsing(ItemStack stack, World world, LivingEntity user) {
        return super.finishUsing(stack, world, user);
        //if (!(user instanceof PlayerEntity && ((PlayerEntity)user).abilities.creativeMode))
        //    stack.setCount(stack.getCount() - 1);
        //return stack;
    }
}
