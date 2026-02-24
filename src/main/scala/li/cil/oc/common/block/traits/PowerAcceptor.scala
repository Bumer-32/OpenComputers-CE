package li.cil.oc.common.block.traits

import java.util

import li.cil.oc.common.block.SimpleBlock
import li.cil.oc.util.Tooltip
import net.minecraft.world.item.{TooltipFlag => ITooltipFlag}
import net.minecraft.world.entity.player.{Player => PlayerEntity}
import net.minecraft.world.item.ItemStack
import net.minecraft.network.chat.{Component => ITextComponent}
import net.minecraft.network.chat.{TextComponent => StringTextComponent}
import net.minecraft.world.level.{BlockGetter => IBlockReader}

import scala.collection.convert.ImplicitConversionsToScala._

trait PowerAcceptor extends SimpleBlock {
  def energyThroughput: Double

  // ----------------------------------------------------------------------- //

  override protected def tooltipTail(stack: ItemStack, world: IBlockReader, tooltip: util.List[ITextComponent], advanced: ITooltipFlag): Unit = {
    super.tooltipTail(stack, world, tooltip, advanced)
    for (curr <- Tooltip.extended("poweracceptor", energyThroughput.toInt)) {
      tooltip.add(new StringTextComponent(curr).setStyle(Tooltip.DefaultStyle))
    }
  }
}
