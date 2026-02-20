package li.cil.oc.common.block

import li.cil.oc.common.tileentity
import net.minecraft.world.level.block.state.BlockBehaviour.Properties as Properties
import net.minecraft.world.level.BlockGetter as IBlockReader
import net.minecraft.world.level.Level as World

class Geolyzer(props: Properties) extends SimpleBlock(props) {
  override def newBlockEntity(world: IBlockReader) = new tileentity.Geolyzer(tileentity.TileEntityTypes.GEOLYZER)
}
