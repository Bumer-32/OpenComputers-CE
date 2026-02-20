package li.cil.oc.common.tileentity

import li.cil.oc.server.component
import net.minecraft.core.BlockPos
import net.minecraft.nbt.CompoundTag
import net.minecraft.world.level.block.entity.{BlockEntity, BlockEntityType}
import net.minecraft.world.level.block.state.BlockState

class Transposer(selfType: BlockEntityType[_ <: Transposer], pos: BlockPos, state: BlockState) extends BlockEntity(selfType, pos, state) with traits.Environment {
  val transposer = new component.Transposer.Block(this)

  def node = transposer.node

  // Used on client side to check whether to render activity indicators.
  var lastOperation = 0L

  override def loadForServer(nbt: CompoundTag): Unit = {
    super.loadForServer(nbt)
    transposer.loadData(nbt)
  }

  override def saveForServer(nbt: CompoundTag): Unit = {
    super.saveForServer(nbt)
    transposer.saveData(nbt)
  }
}
