package li.cil.oc.common.block

import java.util
import li.cil.oc.common.tileentity
import li.cil.oc.integration.Mods
import li.cil.oc.util.Tooltip
import net.minecraft.core.BlockPos
import net.minecraft.world.level.block.state.BlockBehaviour.Properties
import net.minecraft.world.item.TooltipFlag as ITooltipFlag
import net.minecraft.world.entity.player.Player as PlayerEntity
import net.minecraft.world.item.ItemStack
import net.minecraft.network.chat.Component as ITextComponent
import net.minecraft.network.chat.TextComponent as StringTextComponent
import net.minecraft.world.level.BlockGetter as IBlockReader
import net.minecraft.world.level.Level as World
import net.minecraft.world.level.block.state.BlockState

import scala.collection.convert.ImplicitConversionsToScala.*

class Redstone(props: Properties) extends RedstoneAware(props) {
  override protected def tooltipTail(stack: ItemStack, world: IBlockReader, tooltip: util.List[ITextComponent], advanced: ITooltipFlag): Unit = {
    super.tooltipTail(stack, world, tooltip, advanced)
    // todo more generic way for redstone mods to provide lines
    if (Mods.ProjectRedTransmission.isModAvailable) {
      for (curr <- Tooltip.get("redstonecard.ProjectRed")) tooltip.add(new StringTextComponent(curr).setStyle(Tooltip.DefaultStyle))
    }
  }

  // ----------------------------------------------------------------------- //

  override def newBlockEntity(pos: BlockPos, state: BlockState) = new tileentity.Redstone(tileentity.TileEntityTypes.REDSTONE_IO, pos, state)
}
