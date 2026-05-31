package li.cil.oc.integration.opencomputers

import li.cil.oc.Constants
import li.cil.oc.api
import li.cil.oc.api.driver.EnvironmentProvider
import li.cil.oc.api.driver.DriverBlock
import li.cil.oc.api.network.ManagedEnvironment
import li.cil.oc.common.blockentity
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level

object DriverSpeaker extends DriverBlock {

  override def worksWith(level: Level, pos: BlockPos, side: Direction): Boolean =
    level.getBlockEntity(pos).isInstanceOf[blockentity.Speaker]

  override def createEnvironment(level: Level, pos: BlockPos, side: Direction): ManagedEnvironment =
    level.getBlockEntity(pos) match {
      case speaker: blockentity.Speaker => speaker.speakerComponent
      case _ => null
    }

  object Provider extends EnvironmentProvider {
    override def getEnvironment(stack: ItemStack): Class[_] = {
      if (stack.getItem.isInstanceOf[net.minecraft.world.item.BlockItem]) {
        val block = stack.getItem.asInstanceOf[net.minecraft.world.item.BlockItem].getBlock
        if (api.Items.get(Constants.BlockName.Speaker) != null &&
          api.Items.get(Constants.BlockName.Speaker).block == block)
          classOf[blockentity.Speaker]
        else null
      } else null
    }
  }
}
