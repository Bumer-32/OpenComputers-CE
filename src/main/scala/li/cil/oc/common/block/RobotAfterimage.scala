package li.cil.oc.common.block

import java.util.Random
import li.cil.oc.Constants
import li.cil.oc.Settings
import li.cil.oc.api
import li.cil.oc.common.tileentity
import net.minecraft.world.level.block.state.BlockBehaviour.Properties
import net.minecraft.world.level.block.{Block, Blocks}
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.entity.player.Player as PlayerEntity
import net.minecraft.world.level.material.FluidState
import net.minecraft.world.item.ItemStack
import net.minecraft.world.InteractionResult as ActionResultType
import net.minecraft.core.Direction
import net.minecraft.world.InteractionHand as Hand
import net.minecraft.core.BlockPos
import net.minecraft.world.phys.BlockHitResult as BlockRayTraceResult
import net.minecraft.world.phys.HitResult as RayTraceResult
import net.minecraft.world.phys.shapes.CollisionContext as ISelectionContext
import net.minecraft.world.phys.shapes.VoxelShape
import net.minecraft.world.level.BlockGetter as IBlockReader
import net.minecraft.world.level.Level as World
import net.minecraft.server.level.ServerLevel as ServerWorld
import net.minecraft.world.ticks.ScheduledTick

class RobotAfterimage(props: Properties) extends SimpleBlock(props) {
  override def getPickBlock(state: BlockState, target: RayTraceResult, world: IBlockReader, pos: BlockPos, player: PlayerEntity): ItemStack =
    findMovingRobot(world, pos) match {
      case Some(robot) => robot.info.createItemStack()
      case _ => ItemStack.EMPTY
    }

  override def getShape(state: BlockState, world: IBlockReader, pos: BlockPos, ctx: ISelectionContext): VoxelShape = {
    findMovingRobot(world, pos) match {
      case Some(robot) =>
        val block = robot.getBlockState.getBlock.asInstanceOf[SimpleBlock]
        val shape = block.getShape(state, world, robot.getBlockPos, ctx)
        val delta = robot.moveFrom.fold(BlockPos.ZERO)(vec => {
          val blockPos = robot.getBlockPos
          new BlockPos(blockPos.getX - vec.getX, blockPos.getY - vec.getY, blockPos.getZ - vec.getZ)
        })
        shape.move(delta.getX, delta.getY, delta.getZ)
      case _ => super.getShape(state, world, pos, ctx)
    }
  }

  // ----------------------------------------------------------------------- //

  override def onPlace(
                        state: BlockState,
                        world: World,
                        pos: BlockPos,
                        prevState: BlockState,
                        moved: Boolean
                      ): Unit = {
    super.onPlace(state, world, pos, prevState, moved)

    if (!world.isClientSide) {
      val delay = Math.max((Settings.get.moveDelay * 20).toInt, 1) - 1
      val triggerTime = world.getGameTime + delay.toLong

      world.getBlockTicks.schedule(new ScheduledTick)(this, pos, triggerTime, world.nextSubTickCount)
    }
  }

  override def tick(state: BlockState, world: ServerWorld, pos: BlockPos, rand: Random): Unit = {
    world.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState)
  }

  override def removedByPlayer(
                                state: BlockState,
                                world: World,
                                pos: BlockPos,
                                player: PlayerEntity,
                                willHarvest: Boolean,
                                fluid: FluidState
                              ): Boolean = {
    findMovingRobot(world, pos) match {
      case Some(robot) if robot.isAnimatingMove && robot.moveFrom.contains(pos) =>
        robot.proxy.getBlockState.getBlock.removedByPlayer(state, world, pos, player, false, fluid)
      case _ =>
        super.removedByPlayer(state, world, pos, player, willHarvest, fluid)
    }
  }

  @Deprecated
  override def use(state: BlockState, world: World, pos: BlockPos, player: PlayerEntity, hand: Hand, trace: BlockRayTraceResult): ActionResultType = {
    findMovingRobot(world, pos) match {
      case Some(robot) => api.Items.get(Constants.BlockName.Robot).block.use(world.getBlockState(robot.getBlockPos), world, robot.getBlockPos, player, hand, trace)
      case _ => if (world.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState)) ActionResultType.sidedSuccess(world.isClientSide) else ActionResultType.PASS
    }
  }

  def findMovingRobot(world: IBlockReader, pos: BlockPos): Option[tileentity.Robot] = {
    for (side <- Direction.values) {
      val tpos = pos.relative(side)
      if (world match {
        case world: World => world.isLoaded(tpos)
        case _ => true
      }) world.getBlockEntity(tpos) match {
        case proxy: tileentity.RobotProxy if proxy.robot.moveFrom.contains(pos) => return Some(proxy.robot)
        case _ =>
      }
    }
    None
  }
}
