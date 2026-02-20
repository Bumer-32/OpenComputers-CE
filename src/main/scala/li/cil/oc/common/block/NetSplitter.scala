package li.cil.oc.common.block

import li.cil.oc.common.tileentity
import li.cil.oc.integration.util.Wrench
import net.minecraft.world.level.block.state.BlockBehaviour.Properties as Properties
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.entity.player.Player as PlayerEntity
import net.minecraft.world.item.ItemStack
import net.minecraft.world.InteractionResult as ActionResultType
import net.minecraft.core.Direction
import net.minecraft.world.InteractionHand as Hand
import net.minecraft.core.BlockPos
import net.minecraft.world.phys.BlockHitResult as BlockRayTraceResult
import net.minecraft.world.level.BlockGetter as IBlockReader
import net.minecraft.world.level.Level as World

class NetSplitter(props: Properties) extends RedstoneAware(props) {
  override def newBlockEntity(world: IBlockReader) = new tileentity.NetSplitter(tileentity.TileEntityTypes.NET_SPLITTER)

  // ----------------------------------------------------------------------- //

  // NOTE: must not be final for immibis microblocks to work.
  override def use(state: BlockState, world: World, pos: BlockPos, player: PlayerEntity, hand: Hand, trace: BlockRayTraceResult): ActionResultType = {
    if (Wrench.holdsApplicableWrench(player, pos)) {
      val side = trace.getDirection
      val sideToToggle = if (player.isCrouching) side.getOpposite else side
      world.getBlockEntity(pos) match {
        case splitter: tileentity.NetSplitter =>
          if (!world.isClientSide) {
            val oldValue = splitter.openSides(sideToToggle.ordinal())
            splitter.setSideOpen(sideToToggle, !oldValue)
          }
          ActionResultType.sidedSuccess(world.isClientSide)
        case _ => ActionResultType.PASS
      }
    }
    else super.use(state, world, pos, player, hand, trace)
  }
}
