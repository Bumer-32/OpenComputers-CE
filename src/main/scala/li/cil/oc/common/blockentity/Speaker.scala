package li.cil.oc.common.blockentity

import li.cil.oc.server.component.SpeakerComponent
import net.minecraft.core.BlockPos
import net.minecraft.nbt.CompoundTag
import net.minecraft.world.level.block.state.BlockState

class Speaker(pos: BlockPos, state: BlockState)
  extends net.minecraft.world.level.block.entity.BlockEntity(TileEntityTypes.SPEAKER.get(), pos, state)
  with traits.Environment {

  val speakerComponent = new SpeakerComponent(this)

  override def node = speakerComponent.node

  override def loadForServer(nbt: CompoundTag): Unit = {
    super.loadForServer(nbt)
    speakerComponent.loadData(nbt)
  }

  override def saveForServer(nbt: CompoundTag): Unit = {
    super.saveForServer(nbt)
    speakerComponent.saveData(nbt)
  }
}
