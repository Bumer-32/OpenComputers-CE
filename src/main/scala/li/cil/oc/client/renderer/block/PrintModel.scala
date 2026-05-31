package li.cil.oc.client.renderer.block

import java.util
import java.util.Collections

import com.mojang.serialization.Dynamic
import li.cil.oc.Settings
import li.cil.oc.client.KeyBindings
import li.cil.oc.client.Textures
import li.cil.oc.common.item.data.PrintData
import li.cil.oc.common.blockentity
import li.cil.oc.util.Color
import li.cil.oc.util.ExtendedAABB
import li.cil.oc.util.ExtendedAABB._
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.client.multiplayer.ClientLevel
import net.minecraft.client.renderer.block.model.BakedQuad
import net.minecraft.client.resources.model.BakedModel
import net.minecraft.client.renderer.block.model.ItemOverrides
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite
import net.minecraft.client.renderer.texture.TextureAtlasSprite
import net.minecraft.SharedConstants
import net.minecraft.nbt.NbtOps
import net.minecraft.nbt.StringTag
import net.minecraft.util.datafix.DataFixers
import net.minecraft.util.datafix.fixes.References
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.item.DyeColor
import net.minecraft.world.item.ItemStack
import net.minecraft.core.Direction
import net.minecraft.resources.ResourceLocation
import net.minecraft.util.RandomSource
import net.minecraft.client.renderer.RenderType
import net.minecraftforge.client.model.data.{ModelData, ModelProperty}

import scala.jdk.CollectionConverters._
import scala.collection.mutable

object PrintModel extends SmartBlockModelBase {
  val PRINT_PROPERTY = new ModelProperty[blockentity.Print]()

  private val VanillaNamespace = "minecraft"
  private val LegacyBlockDataVersion = 1343 // Minecraft 1.12.2.

  override def getOverrides: ItemOverrides = ItemOverride

  override def getQuads(state: BlockState, side: Direction, rand: RandomSource, data: ModelData, renderType: RenderType): util.List[BakedQuad] =
    Option(data.get(PRINT_PROPERTY)) match {
      case Some(t) =>
        val faces = mutable.ArrayBuffer.empty[BakedQuad]
        for (shape <- t.shapes) {
          val bounds  = shape.bounds.rotateTowards(t.facing)
          val texture = resolveTexture(shape.texture)
          faces ++= bakeQuads(makeBox(bounds.minVec, bounds.maxVec), Array.fill(6)(texture), shape.tint.getOrElse(White))
        }
        faces.asJava
      case _ => super.getQuads(state, side, rand)
    }

  private def resolveTexture(name: String): TextureAtlasSprite = {
    def isMissing(s: TextureAtlasSprite) =
      s.contents.name == MissingTextureAtlasSprite.getLocation

    def tryGet(loc: ResourceLocation): Option[TextureAtlasSprite] = {
      val s = Textures.getSprite(loc)
      if (!isMissing(s)) Some(s) else None
    }

    def normalize(loc: ResourceLocation): ResourceLocation = {
      val path =
        if (loc.getPath.startsWith("blocks/")) "block/" + loc.getPath.stripPrefix("blocks/")
        else loc.getPath
      ResourceLocation.fromNamespaceAndPath(loc.getNamespace, path)
    }

    def migrateVanilla(loc: ResourceLocation): ResourceLocation = {
      val normalized = normalize(loc)
      if (normalized.getNamespace == VanillaNamespace) {
        val path = normalized.getPath.stripPrefix("block/")
        val legacyId = ResourceLocation.fromNamespaceAndPath(VanillaNamespace, path).toString

        val fixer = DataFixers.getDataFixer
        val currentVersion = SharedConstants.getCurrentVersion.getDataVersion.getVersion
        val dynamic = new Dynamic(NbtOps.INSTANCE, StringTag.valueOf(legacyId))

        val updatedDynamic = fixer.update(References.BLOCK_NAME, dynamic, LegacyBlockDataVersion, currentVersion)
        val updatedId = updatedDynamic.asString().result().orElse(legacyId)

        Option(ResourceLocation.tryParse(updatedId)) match {
          case Some(r) =>
            val newPath = r.getPath.stripPrefix("block/")
            ResourceLocation.fromNamespaceAndPath(r.getNamespace, "block/" + newPath)
          case None => normalized
        }
      }
      else normalized
    }

    val trimmed = Option(name).map(_.trim).getOrElse("")
    val fallback = ResourceLocation.fromNamespaceAndPath(Settings.resourceDomain, "block/white")

    if (trimmed.isEmpty) return Textures.getSprite(fallback)

    Option(ResourceLocation.tryParse(trimmed)).map(migrateVanilla).flatMap(tryGet)
      .orElse(tryGet(migrateVanilla(ResourceLocation.withDefaultNamespace("block/" + trimmed.stripPrefix("blocks/")))))
      .orElse(tryGet(ResourceLocation.fromNamespaceAndPath(Settings.resourceDomain, "block/" + trimmed.stripPrefix("blocks/"))))
      .orElse(tryGet(fallback))
      .getOrElse(Textures.getSprite(MissingTextureAtlasSprite.getLocation))
  }

  class ItemModel(val stack: ItemStack) extends SmartBlockModelBase {
    val data = new PrintData(stack)

    override def getQuads(state: BlockState, side: Direction, rand: RandomSource): util.List[BakedQuad] = {
      val faces = mutable.ArrayBuffer.empty[BakedQuad]
      val shapes =
        if (data.hasActiveState && KeyBindings.showExtendedTooltips) data.stateOn
        else data.stateOff
      for (shape <- shapes) {
        val bounds  = shape.bounds
        val texture = resolveTexture(shape.texture)
        faces ++= bakeQuads(makeBox(bounds.minVec, bounds.maxVec), Array.fill(6)(texture), shape.tint.getOrElse(White))
      }
      if (shapes.isEmpty) {
        val bounds  = ExtendedAABB.unitBounds
        val texture = resolveTexture(Settings.resourceDomain + ":block/white")
        faces ++= bakeQuads(makeBox(bounds.minVec, bounds.maxVec), Array.fill(6)(texture), Color.rgbValues(DyeColor.LIME))
      }
      faces.asJava
    }
  }

  object ItemOverride extends ItemOverrides {
    override def resolve(originalModel: BakedModel, stack: ItemStack, world: ClientLevel, entity: LivingEntity, seed: Int): BakedModel =
      new ItemModel(stack)
  }
}
