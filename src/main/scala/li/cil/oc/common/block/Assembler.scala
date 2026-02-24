package li.cil.oc.common.block

import li.cil.oc.Settings
import li.cil.oc.common.menu.MenuTypes
import li.cil.oc.common.tileentity
import net.minecraft.world.level.block.state.BlockBehaviour.Properties
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.server.level.ServerPlayer as ServerPlayerEntity
import net.minecraft.core.BlockPos
import net.minecraft.world.phys.shapes.CollisionContext as ISelectionContext
import net.minecraft.world.phys.shapes.VoxelShape
import net.minecraft.world.phys.shapes.Shapes as VoxelShapes
import net.minecraft.world.level.BlockGetter as IBlockReader
import net.minecraft.world.level.Level as World

class Assembler(props: Properties) extends SimpleBlock(props) with traits.PowerAcceptor with traits.StateAware with traits.GUI {
  override def energyThroughput = Settings.get.assemblerRate

  val blockShape = {
    val bottom = Block.box(0, 0, 0, 16, 7, 16)
    val mid = Block.box(2, 7, 2, 14, 9, 14)
    val top = Block.box(0, 9, 0, 16, 16, 16)
    VoxelShapes.or(top, bottom, mid)
  }

  override def getShape(state: BlockState, world: IBlockReader, pos: BlockPos, ctx: ISelectionContext): VoxelShape = blockShape

  override def openGui(player: ServerPlayerEntity, world: World, pos: BlockPos): Unit = world.getBlockEntity(pos) match {
    case te: tileentity.Assembler => MenuTypes.openAssemblerGui(player, te)
    case _ =>
  }

  override def newBlockEntity(pos: BlockPos, state: BlockState) = new tileentity.Assembler(tileentity.TileEntityTypes.ASSEMBLER, pos, state)
}
