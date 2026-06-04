package net.nicguzzo.wands.items;

import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public record PaletteTooltip(List<ItemStack> items) implements TooltipComponent {
}
