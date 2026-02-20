package li.cil.oc.server.agent

import io.netty.util.concurrent.Future
import io.netty.util.concurrent.GenericFutureListener
import net.minecraft.network.Connection
import net.minecraft.network.protocol.{Packet, PacketFlow}

object FakeNetworkManager extends Connection(PacketFlow.CLIENTBOUND) {
  override def send(packetIn: Packet[_]): Unit = {}

  override def send(packetIn: Packet[_], listener: GenericFutureListener[_ <: Future[_ >: Void]]): Unit = {}
}
