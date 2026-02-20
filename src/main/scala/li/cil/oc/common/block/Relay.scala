package li.cil.oc.common.block

import li.cil.oc.Settings
import li.cil.oc.common.menu.MenuTypes
import li.cil.oc.common.tileentity
import net.minecraft.world.level.block.state.BlockBehaviour.Properties
import net.minecraft.server.level.ServerPlayer as ServerPlayerEntity
import net.minecraft.core.BlockPos
import net.minecraft.world.level.BlockGetter as IBlockReader
import net.minecraft.world.level.Level as World
import net.minecraft.world.level.block.state.BlockState

class Relay(props: Properties) extends SimpleBlock(props) with traits.GUI with traits.PowerAcceptor {
  override def openGui(player: ServerPlayerEntity, world: World, pos: BlockPos): Unit = world.getBlockEntity(pos) match {
    case te: tileentity.Relay => MenuTypes.openRelayGui(player, te)
    case _ =>
  }

  override def energyThroughput = Settings.get.accessPointRate

  override def newBlockEntity(pos: BlockPos, state: BlockState) = new tileentity.Relay(tileentity.TileEntityTypes.RELAY, pos, state)
}
