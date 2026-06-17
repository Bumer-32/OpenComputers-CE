package li.cil.oc.server.component

import li.cil.oc.{Constants, Settings, api}
import li.cil.oc.api.driver.DeviceInfo.{DeviceAttribute, DeviceClass}
import li.cil.oc.api.internal.Rack
import li.cil.oc.api.machine.{Arguments, Callback, Context}
import li.cil.oc.api.network.{Component, ComponentConnector, Visibility}
import li.cil.oc.api.prefab.ComponentConnectableRackMountableEnvironment
import net.minecraft.nbt.CompoundTag

import java.util
import scala.jdk.CollectionConverters._

class CapacitorMountable(val rack: Rack) extends ComponentConnectableRackMountableEnvironment {
  setNode(api.Network.newNode(this, Visibility.Network).
    withComponent("rack_capacitor", Visibility.Network).
    withConnector(maxCapacity).
    create())
  
  private final lazy val deviceInfo = Map(
    DeviceAttribute.Class -> DeviceClass.Power,
    DeviceAttribute.Description -> "Battery",
    DeviceAttribute.Vendor -> Constants.DeviceInfo.ViridiaComputronics,
    DeviceAttribute.Product -> "PowerBank X",
    DeviceAttribute.Capacity -> maxCapacity.toString
  )

  override def getDeviceInfo: util.Map[String, String] = deviceInfo.asJava

  @Callback(doc = "function():number; Returns the amount of energy stored in this capacitor.", direct = true)
  def energy(context: Context, args: Arguments): Array[AnyRef] = {
    result(node.localBuffer())
  }

  @Callback(doc = "function():number; Returns the total amount of energy this capacitor can store.", direct = true)
  def maxEnergy(context: Context, args: Arguments): Array[AnyRef] = {
    result(node.localBufferSize())
  }
  
  protected def maxCapacity: Double = Settings.get.bufferCapacitor + Settings.get.bufferCapacitorAdjacencyBonus * 9

  override def getData: CompoundTag = null
}
