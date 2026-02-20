package li.cil.oc.common.tileentity

import li.cil.oc.api.network.Node
import li.cil.oc.server.component
import net.minecraft.nbt.{CompoundTag => CompoundNBT}
import net.minecraft.world.level.block.entity.{BlockEntity => TileEntity}
import net.minecraft.world.level.block.entity.{BlockEntityType => TileEntityType}

class MotionSensor(selfType: TileEntityType[_ <: MotionSensor]) extends TileEntity(selfType) with traits.Environment with traits.Tickable {
  val motionSensor = new component.MotionSensor(this)

  def node: Node = motionSensor.node

  override def updateEntity(): Unit = {
    super.updateEntity()
    if (isServer) {
      motionSensor.update()
    }
  }

  override def loadForServer(nbt: CompoundNBT): Unit = {
    super.loadForServer(nbt)
    motionSensor.loadData(nbt)
  }

  override def saveForServer(nbt: CompoundNBT): Unit = {
    super.saveForServer(nbt)
    motionSensor.saveData(nbt)
  }
}
