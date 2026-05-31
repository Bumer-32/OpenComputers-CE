package li.cil.oc.server.component

import li.cil.oc.Constants
import li.cil.oc.api.Network
import li.cil.oc.api.driver.DeviceInfo
import li.cil.oc.api.driver.DeviceInfo.{DeviceAttribute, DeviceClass}
import li.cil.oc.api.machine.{Arguments, Callback, Context}
import li.cil.oc.api.network.{EnvironmentHost, Node, Visibility}
import li.cil.oc.api.prefab.AbstractManagedEnvironment
import net.minecraft.nbt.CompoundTag

import java.util
import scala.jdk.CollectionConverters._

class SpeakerComponent(val host: EnvironmentHost) extends AbstractManagedEnvironment with DeviceInfo {

  override val node: Node = Network.newNode(this, Visibility.Network)
    .withComponent("speaker")
    .create()

  /**
   * 0 = Mono
   * 1 = Stereo L
   * 2 = Stereo R
   */
  private var _mode: Int = 0
  private var _channel: Int = 0

  def channel: Int = _channel
  def mode: Int = _mode

  private final lazy val deviceInfo = Map(
    DeviceAttribute.Class       -> DeviceClass.Multimedia,
    DeviceAttribute.Description -> "Speaker",
    DeviceAttribute.Vendor      -> Constants.DeviceInfo.ViridiaComputronics,
    DeviceAttribute.Product     -> "SoundOut Block"
  )

  override def getDeviceInfo: util.Map[String, String] = deviceInfo.asJava

  // ----------------------------------------------------------------------- //

  @Callback(direct = true, doc = "function(channel:number) -- set the output channel index.")
  def setChannel(context: Context, args: Arguments): Array[AnyRef] = {
    _channel = args.checkInteger(0)
    null
  }

  @Callback(direct = true, doc = "function():number -- get the output channel index.")
  def getChannel(context: Context, args: Arguments): Array[AnyRef] = {
    result(_channel)
  }

  @Callback(direct = true, doc = "function(mode:number) -- set the stereo mode. 0=mono, 1=stereo L, 2=stereo R.")
  def setMode(context: Context, args: Arguments): Array[AnyRef] = {
    val m = args.checkInteger(0)
    if (m < 0 || m > 2) throw new IllegalArgumentException(s"invalid mode: $m (0=mono, 1=stereo L, 2=stereo R)")
    _mode = m
    null
  }

  @Callback(direct = true, doc = "function():number -- get the stereo mode. 0=mono, 1=stereo L, 2=stereo R.")
  def getMode(context: Context, args: Arguments): Array[AnyRef] = {
    result(_mode)
  }

  // ----------------------------------------------------------------------- //

  override def loadData(nbt: CompoundTag): Unit = {
    super.loadData(nbt)
    _channel = nbt.getInt("channel")
    _mode    = nbt.getInt("mode")
  }

  override def saveData(nbt: CompoundTag): Unit = {
    super.saveData(nbt)
    nbt.putInt("channel", _channel)
    nbt.putInt("mode", _mode)
  }
}
