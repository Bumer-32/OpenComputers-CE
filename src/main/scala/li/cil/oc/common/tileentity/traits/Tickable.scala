package li.cil.oc.common.tileentity.traits

trait Tickable extends TileEntity {
  def tick(): Unit = updateEntity()
}
