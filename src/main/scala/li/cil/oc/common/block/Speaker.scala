package li.cil.oc.common.block

import li.cil.oc.common.blockentity
import net.minecraft.core.BlockPos
import net.minecraft.world.level.block.state.BlockBehaviour.Properties
import net.minecraft.world.level.block.state.BlockState

class Speaker(props: Properties) extends SimpleBlock(props) {
  override def newBlockEntity(pos: BlockPos, state: BlockState) = new blockentity.Speaker(pos, state)
}
