package li.cil.oc.util

import li.cil.oc.api.network.EnvironmentHost
import net.minecraft.block.Block
import net.minecraft.block.Blocks
import net.minecraft.block.BlockState
import net.minecraft.block.material.Material
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.Direction
import net.minecraft.util.math.BlockPos
import net.minecraft.world.level.Level
import net.minecraft.world.level.BlockGetter

import scala.language.implicitConversions

object ExtendedLevel {

  implicit def extendedBlockAccess(getter: BlockGetter): ExtendedBlockAccess = new ExtendedBlockAccess(getter)

  implicit def extendedLevel(level: Level): ExtendedLevel = new ExtendedLevel(world)

  class ExtendedBlockAccess(val getter: BlockGetter) {
    def getBlock(position: BlockPosition) = getter.getBlockState(position.toBlockPos).getBlock

    def getBlockMapColor(position: BlockPosition) = getBlockMetadata(position).getMapColor(getter, position.toBlockPos)

    def getBlockMetadata(position: BlockPosition) = getter.getBlockState(position.toBlockPos)

    def getBlockEntity(position: BlockPosition): BlockEntity = getter.getBlockEntity(position.toBlockPos)

    def getBlockEntity(host: EnvironmentHost): BlockEntity = getBlockEntity(BlockPosition(host))

    def isAirBlock(position: BlockPosition) = {
      val state = getter.getBlockState(position.toBlockPos)
      state.getBlock.isAir(state, getter, position.toBlockPos)
    }
  }

  class ExtendedLevel(override val level: Level) extends ExtendedBlockAccess(level) {
    def blockExists(position: BlockPosition) = level.isLoaded(position.toBlockPos)

    def breakBlock(position: BlockPosition, drops: Boolean = true) = level.destroyBlock(position.toBlockPos, drops)

    def destroyBlockInWorldPartially(entityId: Int, position: BlockPosition, progress: Int) = level.destroyBlockProgress(entityId, position.toBlockPos, progress)

    def extinguishFire(player: PlayerEntity, position: BlockPosition, side: Direction) = {
      val pos = position.toBlockPos
      val state = level.getBlockState(pos)
      if (state.getMaterial == Material.FIRE) {
        level.setBlock(pos, Blocks.AIR.defaultBlockState, 3)
        true
      }
      else false
    }

    def getBlockHardness(position: BlockPosition) = level.getBlockState(position.toBlockPos).getDestroySpeed(world, position.toBlockPos)

    def getBlockHarvestLevel(position: BlockPosition) = getBlock(position).getHarvestLevel(getBlockMetadata(position))

    def getBlockHarvestTool(position: BlockPosition) = getBlock(position).getHarvestTool(getBlockMetadata(position))

    def computeRedstoneSignal(position: BlockPosition, side: Direction) = math.max(level.isBlockProvidingPowerTo(position.offset(side), side), level.getIndirectPowerLevelTo(position.offset(side), side))

    def isBlockProvidingPowerTo(position: BlockPosition, side: Direction) = level.getDirectSignal(position.toBlockPos, side)

    def getIndirectPowerLevelTo(position: BlockPosition, side: Direction) = level.getSignal(position.toBlockPos, side)

    def notifyBlockUpdate(pos: BlockPos): Unit = level.sendBlockUpdated(pos, level.getBlockState(pos), level.getBlockState(pos), 3)

    def notifyBlockUpdate(position: BlockPosition): Unit = level.sendBlockUpdated(position.toBlockPos, level.getBlockState(position.toBlockPos), level.getBlockState(position.toBlockPos), 3)

    def notifyBlockUpdate(position: BlockPosition, oldState: BlockState, newState: BlockState, flags: Int = 3): Unit = level.sendBlockUpdated(position.toBlockPos, oldState, newState, flags)

    def notifyBlockOfNeighborChange(position: BlockPosition, block: Block) = level.neighborChanged(position.toBlockPos, block, position.toBlockPos)

    @Deprecated
    def notifyBlocksOfNeighborChange(position: BlockPosition, block: Block, updateObservers: Boolean) = level.updateNeighborsAt(position.toBlockPos, block)

    def notifyBlocksOfNeighborChange(position: BlockPosition, block: Block, side: Direction) = level.updateNeighborsAtExceptFromFacing(position.toBlockPos, block, side)

    def playAuxSFX(id: Int, position: BlockPosition, data: Int) = level.levelEvent(id, position.toBlockPos, data)

    def setBlock(position: BlockPosition, block: Block) = level.setBlockAndUpdate(position.toBlockPos, block.defaultBlockState)

    @Deprecated
    def setBlock(position: BlockPosition, block: Block, metadata: Int, flag: Int) = {
      val states = block.getStateDefinition.getPossibleStates
      val state = if (metadata >= 0 && metadata < states.size) states.get(metadata) else block.defaultBlockState
      level.setBlock(position.toBlockPos, state, flag)
    }

    def setBlockToAir(position: BlockPosition) = level.setBlockAndUpdate(position.toBlockPos, Blocks.AIR.defaultBlockState)

    def isLoaded(position: BlockPosition) = level.isLoaded(position.toBlockPos)
  }

}
