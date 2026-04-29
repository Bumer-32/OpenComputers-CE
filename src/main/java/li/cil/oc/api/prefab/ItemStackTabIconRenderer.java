package li.cil.oc.api.prefab;

import com.mojang.blaze3d.vertex.PoseStack;
import li.cil.oc.api.manual.TabIconRenderer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Vector4f;

/**
 * Simple implementation of a tab icon renderer using an item stack as its graphic.
 * In 1.18, this class isn't required
 */
@SuppressWarnings("UnusedDeclaration")
@Deprecated(forRemoval = true)
public class ItemStackTabIconRenderer implements TabIconRenderer {
    private final ItemStack stack;

    public ItemStackTabIconRenderer(ItemStack stack) {
        this.stack = stack;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void render(GuiGraphics graphics) {
        Vector4f vec = new Vector4f(0, 0, 0, 1);
        vec.mul(graphics.pose().last().pose());

        graphics.renderItem(stack, (int) vec.x(), (int) vec.y());

        graphics.renderItemDecorations(
                net.minecraft.client.Minecraft.getInstance().font,
                stack,
                (int) vec.x(),
                (int) vec.y()
        );
    }
}
