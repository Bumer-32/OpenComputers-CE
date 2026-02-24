package li.cil.oc.common.block

import li.cil.oc.common.tileentity
import li.cil.oc.common.tileentity.TileEntityTypes
import net.minecraft.core.BlockPos
import net.minecraft.world.level.block.entity.{BlockEntity, BlockEntityType}
import net.minecraft.world.level.block.state.BlockBehaviour.Properties
import net.minecraft.world.level.{BlockGetter => IBlockReader}
import net.minecraft.world.level.{Level => World}
import net.minecraft.world.level.block.state.BlockState

class PowerDistributor(props: Properties) extends SimpleBlock(props) with traits.Tickable {
  override def newBlockEntity(pos: BlockPos, state: BlockState) = new tileentity.PowerDistributor(pos, state)

  override def getBlockEntityType: BlockEntityType[_ <: BlockEntity] = TileEntityTypes.POWER_DISTRIBUTOR.get()
}

