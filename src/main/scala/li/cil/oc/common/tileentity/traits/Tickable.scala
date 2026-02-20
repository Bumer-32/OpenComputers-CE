package li.cil.oc.common.tileentity.traits

trait Tickable extends BlockEntity {
  def tick(): Unit = updateEntity()
  def updateEntity(): Unit = {}
}
