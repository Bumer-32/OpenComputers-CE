package li.cil.oc.common.tileentity.traits

import net.minecraft.world.level.block.state.BlockState

trait Tickable extends TileEntity {
  def tick(): Unit = updateEntity()
}
