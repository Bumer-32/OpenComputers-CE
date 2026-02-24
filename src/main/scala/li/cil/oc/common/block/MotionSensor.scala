package li.cil.oc.common.block

import li.cil.oc.common.tileentity
import net.minecraft.core.BlockPos
import net.minecraft.world.level.block.state.BlockBehaviour.Properties
import net.minecraft.world.level.BlockGetter as IBlockReader
import net.minecraft.world.level.Level as World
import net.minecraft.world.level.block.state.BlockState

class MotionSensor(props: Properties) extends SimpleBlock(props) {
  override def newBlockEntity(pos: BlockPos, state: BlockState) = new tileentity.MotionSensor(pos, state)
}
