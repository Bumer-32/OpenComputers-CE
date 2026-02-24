package li.cil.oc.client.renderer.block

import java.util
import java.util.Collections

import li.cil.oc.client.Textures
import net.minecraft.world.level.block.state.BlockState                 // 1.18.2: net.minecraft.block → world.level.block.state
import net.minecraft.client.renderer.block.model.BakedQuad              // 1.18.2: net.minecraft.client.renderer.model → block.model
import net.minecraft.client.resources.model.BakedModel                  // 1.18.2: IBakedModel → BakedModel
import net.minecraft.client.renderer.block.model.ItemOverrides           // 1.18.2: ItemOverrideList → ItemOverrides
import net.minecraft.client.multiplayer.ClientLevel                      // 1.18.2: ClientWorld → ClientLevel
import net.minecraft.world.entity.LivingEntity                           // 1.18.2: net.minecraft.entity → world.entity
import net.minecraft.world.item.ItemStack                                // 1.18.2: net.minecraft.item → world.item
import net.minecraft.core.Direction                                      // 1.18.2: net.minecraft.util.Direction → core.Direction
import net.minecraft.world.phys.Vec3                                     // 1.18.2: Vector3d → Vec3

import scala.collection.mutable
import scala.jdk.CollectionConverters._

object DroneModel extends SmartBlockModelBase {
  override def getOverrides: ItemOverrides = ItemOverride

  override def getQuads(state: BlockState, side: Direction, rand: util.Random): util.List[BakedQuad] = {
    val faces = mutable.ArrayBuffer.empty[BakedQuad]
    faces ++= Boxes.flatMap(box => bakeQuads(box, Array.fill(6)(droneTexture), None).toSeq)
    faces.asJava
  }

  protected def droneTexture = Textures.getSprite(Textures.Item.DroneItem)

  // 1.18.2: new Vector3d(...) → new Vec3(...)
  protected def Boxes = Array(
    makeBox(new Vec3(1f / 16f, 7f / 16f, 1f / 16f),  new Vec3(7f / 16f,  8f / 16f, 7f / 16f)),
    makeBox(new Vec3(1f / 16f, 7f / 16f, 9f / 16f),  new Vec3(7f / 16f,  8f / 16f, 15f / 16f)),
    makeBox(new Vec3(9f / 16f, 7f / 16f, 1f / 16f),  new Vec3(15f / 16f, 8f / 16f, 7f / 16f)),
    makeBox(new Vec3(9f / 16f, 7f / 16f, 9f / 16f),  new Vec3(15f / 16f, 8f / 16f, 15f / 16f)),
    rotateBox(makeBox(new Vec3(6f / 16f, 6f / 16f, 6f / 16f), new Vec3(10f / 16f, 9f / 16f, 10f / 16f)), 45)
  )

  object ItemOverride extends ItemOverrides {
    // 1.18.2: resolve に seed: Int 引数が追加された
    override def resolve(originalModel: BakedModel, stack: ItemStack, world: ClientLevel, entity: LivingEntity, seed: Int): BakedModel =
      DroneModel
  }
}