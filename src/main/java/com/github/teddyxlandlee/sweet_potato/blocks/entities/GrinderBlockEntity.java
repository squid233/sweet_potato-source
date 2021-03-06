package com.github.teddyxlandlee.sweet_potato.blocks.entities;

import com.github.teddyxlandlee.annotation.NonMinecraftNorFabric;
import com.github.teddyxlandlee.sweet_potato.recipe.GrinderRecipe;
import com.github.teddyxlandlee.sweet_potato.screen.GrinderScreenHandler;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.LockableContainerBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeType;
import net.minecraft.recipe.RecipeUnlocker;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.tag.Tag;
import net.minecraft.tag.TagContainer;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.util.Tickable;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.registry.Registry;

import javax.annotation.Nullable;
import java.util.Iterator;

import static com.github.teddyxlandlee.sweet_potato.ExampleMod.GRINDER;
import static com.github.teddyxlandlee.sweet_potato.ExampleMod.MODID;

public class GrinderBlockEntity extends LockableContainerBlockEntity implements Tickable, RecipeUnlocker {
    public static final BlockEntityType<GrinderBlockEntity> GRINDER_BLOCK_ENTITY = Registry.register(Registry.BLOCK_ENTITY_TYPE, new Identifier(MODID, "grinder"),
            BlockEntityType.Builder.create(GrinderBlockEntity::new, GRINDER).build(null));

    private int grindTime;
    private int grindTimeTotal;

    protected final PropertyDelegate propertyDelegate;
    protected DefaultedList<ItemStack> inventory;

    private final Object2IntOpenHashMap<Identifier> recipesUsed;//?
    protected final RecipeType<GrinderRecipe> grinderRecipeType;

    public GrinderBlockEntity(BlockEntityType<?> blockEntityType, RecipeType<GrinderRecipe> recipeType) {
        super(blockEntityType);
        this.inventory = DefaultedList.ofSize(2, ItemStack.EMPTY);
        this.propertyDelegate = new PropertyDelegate() {
            @Override
            public int get(int index) {
                switch (index) {
                    case 0:
                        return GrinderBlockEntity.this.grindTime;
                    case 1:
                        return GrinderBlockEntity.this.grindTimeTotal;
                    default:
                        return 0;
                }
            }

            @Override
            public void set(int index, int value) {
                switch (index) {
                    case 0:
                        GrinderBlockEntity.this.grindTime = value;
                    case 1:
                        GrinderBlockEntity.this.grindTimeTotal = value;
                }
            }

            @Override
            public int size() {
                return 2;
            }
        };
        this.recipesUsed = new Object2IntOpenHashMap<>();
        this.grinderRecipeType = recipeType;
    }

    @NonMinecraftNorFabric
    private boolean isGrinding() {
        return this.grindTime > 0;
    }

    @NonMinecraftNorFabric
    protected int getGrindTime() {
        assert this.world != null;
        return this.world.getRecipeManager().getFirstMatch(this.grinderRecipeType, this, this.world).map(GrinderRecipe::getGrindTime).orElse(200);
    }

    @Override
    protected Text getContainerName() {
        return new TranslatableText("container.sweet_potato.grinding");
    }

    @Override
    protected ScreenHandler createScreenHandler(int syncId, PlayerInventory playerInventory) {
        return new GrinderScreenHandler(syncId, playerInventory);
    }

    @Override
    public int size() {
        return this.inventory.size();
    }

    @Override
    public boolean isEmpty() {  // ?
        Iterator<?> iterator = this.inventory.iterator();

        ItemStack itemStack;
        do {
            if (!iterator.hasNext())
                return true;

            itemStack = (ItemStack) iterator.next();
        } while(itemStack.isEmpty());

        return false;
    }

    @Override
    public ItemStack getStack(int slot) {
        return this.inventory.get(slot);
    }

    @Override
    public ItemStack removeStack(int slot, int amount) {
        return Inventories.splitStack(this.inventory, slot, amount);
    }

    @Override
    public ItemStack removeStack(int slot) {
        return Inventories.removeStack(this.inventory, slot);
    }

    @Override
    public void setStack(int slot, ItemStack stack) {
        ItemStack itemStack = this.inventory.get(slot);
        boolean equal = !stack.isEmpty() && stack.isItemEqualIgnoreDamage(itemStack) && ItemStack.areTagsEqual(stack, itemStack);
        this.inventory.set(slot, stack);
        if (stack.getCount() > this.getMaxCountPerStack())
            stack.setCount(this.getMaxCountPerStack());

        if (slot == 0 && !equal) {
            this.grindTime = 0;
            this.markDirty();
        }
    }

    @Override
    public boolean canPlayerUse(PlayerEntity player) {
        assert this.world != null;
        return this.world.getBlockEntity(this.pos) == this && player.squaredDistanceTo((double) this.pos.getX() + 0.5D, (double) this.pos.getY() + 0.5D, (double) this.pos.getZ() + 0.5D) <= 64.0D;
    }

    @Override
    public void clear() {
        this.inventory.clear();
    }

    @Override
    public void tick() {
        boolean shallMarkDirty = false;
        assert this.world != null;  // stupid IDEA
        if (!this.world.isClient) {
            if (!(!this.isGrinding() || this.inventory.get(0).isEmpty())) {
                Recipe<?> recipe = this.world.getRecipeManager().getFirstMatch(this.grinderRecipeType, this, this.world).orElse(null);
                /*if (!this.isGrinding()) {
                    shallMarkDirty = true;
                    ++this.grindTime;
                    if (this.grindTime == this.grindTimeTotal) {    // Done once
                        this.grindTime = 0;
                        this.grindTimeTotal = this.getGrindTime();
                        this.craftRecipe(recipe);
                    }
                }*/
                if (!this.isGrinding() && this.canAcceptRecipeOutput(recipe)) {
                    if (this.isGrinding()) {
                        shallMarkDirty = true;
                    }
                }
                if (this.isGrinding() && this.canAcceptRecipeOutput(recipe)) {
                    ++this.grindTime;
                    if (this.grindTime == this.grindTimeTotal) {
                        // once done
                        this.grindTime = 0;
                        this.grindTimeTotal = this.getGrindTime();
                        this.craftRecipe(recipe);
                        shallMarkDirty = true;
                    }
                } else {
                    this.grindTime = 0;
                }
            }
            /*else {
                if (!grinding && this.grindTime > 0)
                    this.grindTime = MathHelper.clamp(this.grindTime - 2, 0, this.grindTimeTotal);
            }*/
        }

        if (shallMarkDirty)
            markDirty();
    }

    private void craftRecipe(@Nullable Recipe<?> recipe) {
        if (recipe != null && this.canAcceptRecipeOutput(recipe)) {
            ItemStack input = this.inventory.get(0);
            ItemStack recipeOutput = recipe.getOutput();
            ItemStack invOutput = this.inventory.get(2);

            if (invOutput.isEmpty())
                this.inventory.set(1, recipeOutput.copy());
            else if (invOutput.getItem() == recipeOutput.getItem())
                invOutput.increment(1);

            assert this.world != null;  // stupid IntelliJ
            if (!this.world.isClient)
                this.setLastRecipe(recipe);

            input.decrement(1);
        }
    }

    protected boolean canAcceptRecipeOutput(@Nullable Recipe<?> recipe) {
        if (!this.inventory.get(0).isEmpty() && recipe != null) {
            ItemStack output = recipe.getOutput();
            if (output.isEmpty())
                return false;
            else {
                ItemStack outInv = this.inventory.get(1);
                if (outInv.isEmpty())
                    return true;
                if (!outInv.isItemEqualIgnoreDamage(output))
                    return false;
                if (outInv.getCount() < this.getMaxCountPerStack() && outInv.getCount() < outInv.getMaxCount())
                    return true;
                return outInv.getCount() < output.getMaxCount();
            }
        } return false;
    }

    @Override
    public void setLastRecipe(@Nullable Recipe<?> recipe) {
        if (recipe != null) {
            Identifier id = recipe.getId();
            this.recipesUsed.addTo(id, 1);
        }
    }

    @Nullable
    @Override
    public Recipe<?> getLastRecipe() {
        return null;
    }

    @Override
    public void fromTag(BlockState state, CompoundTag tag) {
        super.fromTag(state, tag);
        this.inventory = DefaultedList.ofSize(this.size(), ItemStack.EMPTY);
        Inventories.fromTag(tag, this.inventory);
        this.grindTime = tag.getShort("GrindTime");
        this.grindTimeTotal = tag.getShort("GrindTimeTotal");

        CompoundTag recipeUsed = new CompoundTag();
        for (String nextKey : recipeUsed.getKeys()) {
            this.recipesUsed.put(new Identifier(nextKey), recipeUsed.getInt(nextKey));
        }
    }

    @Override
    public CompoundTag toTag(CompoundTag tag) {
        super.toTag(tag);
        tag.putShort("GrindTime", (short) grindTime);
        tag.putShort("GrindTimeTotal", (short) grindTimeTotal);
        Inventories.toTag(tag, this.inventory);

        CompoundTag recipeUsed = new CompoundTag();
        this.recipesUsed.forEach(((identifier, integer) -> {
            recipeUsed.putInt(identifier.toString(), integer);
        }));
        tag.put("RecipesUsed", recipeUsed);
        return tag;
    }
}
