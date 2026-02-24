package li.cil.oc.client.renderer

import java.util.concurrent.TimeUnit
import com.google.common.cache.CacheBuilder
import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.vertex.{DefaultVertexFormat, PoseStack, Tesselator, VertexFormat}
import li.cil.oc.{OpenComputers, Settings}
import li.cil.oc.client.renderer.font.TextBufferRenderData
import li.cil.oc.util.RenderState
import net.minecraft.client.renderer.{GameRenderer, MultiBufferSource}
import net.minecraftforge.event.TickEvent.ClientTickEvent
import net.minecraftforge.eventbus.api.SubscribeEvent

object TextBufferRenderCache {
  val renderer =
    if (Settings.get.fontRenderer == "texture") new font.StaticFontRenderer()
    else new font.DynamicFontRenderer()

  private val cache = com.google.common.cache.CacheBuilder.newBuilder().
    expireAfterAccess(2, TimeUnit.SECONDS).
    build[TextBufferRenderData, RenderCache]()

  // ----------------------------------------------------------------------- //
  // Rendering
  // ----------------------------------------------------------------------- //

  def render(stack: PoseStack, buffer: TextBufferRenderData): Unit = {
    RenderSystem.setShader(() => GameRenderer.getPositionColorShader)
    RenderSystem.disableTexture()
    RenderSystem.enableBlend()
    RenderSystem.defaultBlendFunc()

    RenderSystem.disableDepthTest()

    val tesselator = Tesselator.getInstance
    val builder = tesselator.getBuilder
    val matrix = stack.last.pose

    builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR)
    builder.vertex(matrix, 0f, 0f, 0f).color(255, 0, 0, 255).endVertex()
    builder.vertex(matrix, 0f, 50f, 0f).color(0, 255, 0, 255).endVertex()
    builder.vertex(matrix, 50f, 50f, 0f).color(0, 0, 255, 255).endVertex()
    builder.vertex(matrix, 50f, 0f, 0f).color(255, 255, 255, 255).endVertex()
    tesselator.end()

    RenderSystem.enableDepthTest()
    RenderSystem.enableTexture()
    /*
    RenderState.checkError(getClass.getName + ".render: entering (aka: wasntme)")

    // すべての文字を生成
    for (line <- buffer.data.buffer) {
      renderer.generateChars(line)
    }

    // Immediateモードで直接描画（キャッシュなし）
    val tesselator = Tesselator.getInstance
    val immediate = MultiBufferSource.immediate(tesselator.getBuilder)

    // バッファを描画
    renderer.drawBuffer(stack, immediate, buffer.data, buffer.viewport._1, buffer.viewport._2)

    // 即座にフラッシュして描画を実行
    immediate.endBatch()

    // dirtyフラグをクリア
    buffer.dirty = false

    RenderState.checkError(getClass.getName + ".render: leaving")
    */
  }

  // ----------------------------------------------------------------------- //
  // ITickHandler
  // ----------------------------------------------------------------------- //

  @SubscribeEvent
  def onTick(e: ClientTickEvent) = cache.cleanUp()
}
