package li.cil.oc.common.tileentity

import li.cil.oc.server.component
import net.minecraft.core.BlockPos
import net.minecraft.nbt.CompoundTag as CompoundNBT
import net.minecraft.world.level.block.entity.BlockEntity as TileEntity
import net.minecraft.world.level.block.entity.BlockEntityType as TileEntityType
import net.minecraft.world.level.block.state.BlockState

class Geolyzer(selfType: TileEntityType[_ <: Geolyzer], pos: BlockPos, state: BlockState) extends TileEntity(selfType, pos, state) with traits.Environment {
  val geolyzer = new component.Geolyzer(this)

  def node = geolyzer.node

  override def loadForServer(nbt: CompoundNBT): Unit = {
    super.loadForServer(nbt)
    geolyzer.loadData(nbt)
  }

  override def saveForServer(nbt: CompoundNBT): Unit = {
    super.saveForServer(nbt)
    geolyzer.saveData(nbt)
  }
}
