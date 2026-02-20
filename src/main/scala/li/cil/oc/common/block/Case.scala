package li.cil.oc.common.block

import java.util

import li.cil.oc.Settings
import li.cil.oc.common.menu.MenuTypes
import li.cil.oc.common.block.property.PropertyRotatable
import li.cil.oc.common.tileentity
import li.cil.oc.util.Tooltip
import net.minecraft.world.level.block.state.BlockBehaviour.Properties as Properties
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.item.TooltipFlag as ITooltipFlag
import net.minecraft.world.entity.player.Player as PlayerEntity
import net.minecraft.server.level.ServerPlayer as ServerPlayerEntity
import net.minecraft.world.level.material.FluidState
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.block.state.StateDefinition as StateContainer
import net.minecraft.core.Direction
import net.minecraft.world.InteractionHand as Hand
import net.minecraft.core.BlockPos
import net.minecraft.network.chat.Component as ITextComponent
import net.minecraft.network.chat.TextComponent as StringTextComponent
import net.minecraft.world.level.BlockGetter as IBlockReader
import net.minecraft.world.level.Level as World

import scala.collection.convert.ImplicitConversionsToScala._

class Case(props: Properties, val tier: Int) extends RedstoneAware(props) with traits.PowerAcceptor with traits.StateAware with traits.GUI {
  protected override def createBlockStateDefinition(builder: StateContainer.Builder[Block, BlockState]) =
    builder.add(PropertyRotatable.Facing, property.PropertyRunning.Running)

  // ----------------------------------------------------------------------- //

  override protected def tooltipBody(stack: ItemStack, world: IBlockReader, tooltip: util.List[ITextComponent], advanced: ITooltipFlag): Unit = {
    for (curr <- Tooltip.get(getClass.getSimpleName.toLowerCase, slots)) {
      tooltip.add(new StringTextComponent(curr).setStyle(Tooltip.DefaultStyle))
    }
  }

  private def slots = tier match {
    case 0 => "2/1/1"
    case 1 => "2/2/2"
    case 2 | 3 => "3/2/3"
    case _ => "0/0/0"
  }

  // ----------------------------------------------------------------------- //

  override def energyThroughput = Settings.get.caseRate(tier)

  override def openGui(player: ServerPlayerEntity, world: World, pos: BlockPos): Unit = world.getBlockEntity(pos) match {
    case te: tileentity.Case if te.stillValid(player) => MenuTypes.openCaseGui(player, te)
    case _ =>
  }

  override def newBlockEntity(world: IBlockReader) = new tileentity.Case(tileentity.TileEntityTypes.CASE, tier)

  // ----------------------------------------------------------------------- //

  override def localOnBlockActivated(world: World, pos: BlockPos, player: PlayerEntity, hand: Hand, heldItem: ItemStack, side: Direction, hitX: Float, hitY: Float, hitZ: Float) = {
    if (player.isCrouching) {
      if (!world.isClientSide) world.getBlockEntity(pos) match {
        case computer: tileentity.Case if !computer.machine.isRunning && computer.stillValid(player) => computer.machine.start()
        case _ =>
      }
      true
    }
    else super.localOnBlockActivated(world, pos, player, hand, heldItem, side, hitX, hitY, hitZ)
  }

  override def removedByPlayer(state: BlockState,
                               world: World,
                               pos: BlockPos,
                               player: PlayerEntity,
                               willHarvest: Boolean,
                               fluid: FluidState
                              ): Boolean = {
    Option(world.getBlockEntity(pos)) match {
      case Some(c: tileentity.Case) =>
        val playerName = player.getName.getString
        if (c.isCreative && (!player.isCreative || !c.canInteract(playerName))) {
          false
        } else {
          c.canInteract(playerName) && super.removedByPlayer(state, world, pos, player, willHarvest, fluid)
        }
      case _ =>
        super.removedByPlayer(state, world, pos, player, willHarvest, fluid)
    }
  }
}
