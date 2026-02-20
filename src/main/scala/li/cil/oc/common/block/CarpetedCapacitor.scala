package li.cil.oc.common.block

import li.cil.oc.common.tileentity
import net.minecraft.world.level.block.state.BlockBehaviour.Properties
import net.minecraft.world.level.BlockGetter as IBlockReader

class CarpetedCapacitor(props: Properties) extends Capacitor(props) {
  override def newBlockEntity(world: IBlockReader) = new tileentity.CarpetedCapacitor(tileentity.TileEntityTypes.CARPETED_CAPACITOR)
}
