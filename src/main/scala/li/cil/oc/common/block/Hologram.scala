package li.cil.oc.common.block

import java.util

import li.cil.oc.common.tileentity
import li.cil.oc.util.Rarity
import li.cil.oc.util.Tooltip
import net.minecraft.world.level.block.state.BlockBehaviour.Properties as Properties
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.item.TooltipFlag as ITooltipFlag
import net.minecraft.world.entity.player.Player as PlayerEntity
import net.minecraft.world.item.ItemStack
import net.minecraft.core.BlockPos
import net.minecraft.world.phys.shapes.CollisionContext as ISelectionContext
import net.minecraft.world.phys.shapes.VoxelShape
import net.minecraft.world.phys.shapes.Shapes as VoxelShapes
import net.minecraft.network.chat.Component as ITextComponent
import net.minecraft.network.chat.TextComponent as StringTextComponent
import net.minecraft.world.level.BlockGetter as IBlockReader
import net.minecraftforge.api.distmarker.Dist
import net.minecraftforge.api.distmarker.OnlyIn

import scala.collection.convert.ImplicitConversionsToScala._

class Hologram(props: Properties, val tier: Int) extends SimpleBlock(props) {
  val shape = VoxelShapes.box(0, 0, 0, 1, 0.5, 1)

  // ----------------------------------------------------------------------- //

  override def getShape(state: BlockState, world: IBlockReader, pos: BlockPos, ctx: ISelectionContext): VoxelShape = shape

  // ----------------------------------------------------------------------- //

  override protected def tooltipBody(stack: ItemStack, world: IBlockReader, tooltip: util.List[ITextComponent], advanced: ITooltipFlag): Unit = {
    for (curr <- Tooltip.get(getClass.getSimpleName.toLowerCase() + tier)) {
      tooltip.add(new StringTextComponent(curr).setStyle(Tooltip.DefaultStyle))
    }
  }

  // ----------------------------------------------------------------------- //

  override def newBlockEntity(pos: BlockPos, state: BlockState) = new tileentity.Hologram(pos, state, tier)
}
