package li.cil.oc.client.renderer.item

import com.mojang.blaze3d.vertex.PoseStack          // 1.18.2: com.mojang.blaze3d.matrix.MatrixStack → PoseStack
import com.mojang.blaze3d.vertex.VertexConsumer      // 1.18.2: IVertexBuilder → VertexConsumer
import li.cil.oc.Settings
// 1.18.2: ModelRenderer は廃止。代わりに ModelPart + LayerDefinition を使う。
import net.minecraft.client.model.HumanoidModel      // 1.18.2: BipedModel → HumanoidModel
import net.minecraft.client.model.geom.ModelPart
import net.minecraft.client.model.geom.PartPose
import net.minecraft.client.model.geom.builders.CubeListBuilder
import net.minecraft.client.model.geom.builders.LayerDefinition
import net.minecraft.client.model.geom.builders.MeshDefinition
import net.minecraft.client.renderer.LightTexture
import net.minecraft.client.renderer.texture.OverlayTexture
import net.minecraft.world.entity.LivingEntity
import net.minecraft.resources.ResourceLocation      // 1.18.2: net.minecraft.util.ResourceLocation → resources

// ---------------------------------------------------------------------------
// コンパニオンオブジェクト: createBodyLayer() でモデル定義を行う
// 1.18.2 では "モデルの形状定義（LayerDefinition）" と
// "実際のモデルインスタンス（ModelPart）" が分離された。
// ---------------------------------------------------------------------------
object HoverBootRenderer {
  val texture = new ResourceLocation(Settings.resourceDomain, "textures/model/drone.png")

  // 1.18.2: モデルのジオメトリ定義。以前の texOffs/addBox/addChild 呼び出しと
  // 完全に等価な寸法・UV・階層構造を LayerDefinition で再現する。
  def createBodyLayer(): LayerDefinition = {
    val mesh = new MeshDefinition()
    val root = mesh.getRoot

    // ---- 脚のパーツ ----
    // HumanoidModel が "left_leg" / "right_leg" という名前で参照する。
    // 元の createShallowCopy() は「バニラの脚テクスチャのキューブなし版」と同義なので、
    // ここでは CubeListBuilder.create() のみ（キューブなし）で定義する。
    val rightLeg = root.addOrReplaceChild("right_leg",
      CubeListBuilder.create(),
      PartPose.offset(-1.9f, 12f, 0f))  // バニラと同じオフセット

    val leftLeg = root.addOrReplaceChild("left_leg",
      CubeListBuilder.create(),
      PartPose.offset(1.9f, 12f, 0f))

    // ---- ブーツのアタッチポイント ----
    // 元コード: bootRight.y = 10.1f / bootLeft.y = 10.11f（親相対 Y）
    val bootRight = rightLeg.addOrReplaceChild("boot_right",
      CubeListBuilder.create(),
      PartPose.offset(0f, 10.1f, 0f))

    val bootLeft = leftLeg.addOrReplaceChild("boot_left",
      CubeListBuilder.create(),
      PartPose.offset(0f, 10.11f, 0f))

    // ---- droneBody（両方のブーツに同形状を個別定義）----
    // 元コード: droneBody は bootLeft・bootRight 両方に addChild されていたが、
    // 1.18.2 の ModelPart は1つの親しか持てないため、同一形状を別インスタンスで定義する。
    // yRot = Math.toRadians(45) を PartPose で表現。
    def addDroneBody(parent: net.minecraft.client.model.geom.builders.PartDefinition, name: String): Unit = {
      parent.addOrReplaceChild(name,
        CubeListBuilder.create()
          .texOffs(0, 23).addBox(-3, 1, -3, 6, 1, 6)   // top
          .texOffs(0, 1) .addBox(-1, 0, -1, 2, 1, 2)   // middle
          .texOffs(0, 17).addBox(-2, -1, -2, 4, 1, 4), // bottom
        PartPose.offsetAndRotation(0, 0, 0, 0, math.toRadians(45).toFloat, 0))
    }
    addDroneBody(bootLeft,  "drone_body_left")
    addDroneBody(bootRight, "drone_body_right")

    // ---- ウィング（bootLeft 側: wing0, wing1）----
    val wing0 = bootLeft.addOrReplaceChild("wing0",
      CubeListBuilder.create()
        .texOffs(0, 9) .addBox(-1, 0, -7, 6, 1, 6) // flap0
        .texOffs(0, 27).addBox(0, -1, -3, 1, 3, 1), // pin0
      PartPose.ZERO)

    val wing1 = bootLeft.addOrReplaceChild("wing1",
      CubeListBuilder.create()
        .texOffs(0, 9) .addBox(-1, 0, 1, 6, 1, 6)  // flap1
        .texOffs(0, 27).addBox(0, -1, 2, 1, 3, 1),  // pin1
      PartPose.ZERO)

    // ---- ウィング（bootRight 側: wing2, wing3）----
    val wing2 = bootRight.addOrReplaceChild("wing2",
      CubeListBuilder.create()
        .texOffs(0, 9) .addBox(-5, 0, 1, 6, 1, 6)   // flap2
        .texOffs(0, 27).addBox(-1, -1, 2, 1, 3, 1),  // pin2
      PartPose.ZERO)

    val wing3 = bootRight.addOrReplaceChild("wing3",
      CubeListBuilder.create()
        .texOffs(0, 9) .addBox(-5, 0, -7, 6, 1, 6)  // flap3
        .texOffs(0, 27).addBox(-1, -1, -3, 1, 3, 1), // pin3
      PartPose.ZERO)

    // ---- ライトパーツ（各ウィングの子として定義）----
    // 元コード: LightModelRenderer は render() をオーバーライドしてフルブライト描画していた。
    // 1.18.2 では ModelPart をサブクラス化できないため、
    // ライトパーツはここで形状だけ定義し、renderToBuffer() 内で
    // 「本体レンダー中は非表示 → 別途フルブライトで再描画」という手順で同じ視覚効果を実現する。
    wing0.addOrReplaceChild("light0",
      CubeListBuilder.create().texOffs(24, 0).addBox(-1, 0, -7, 6, 1, 6), PartPose.ZERO)
    wing1.addOrReplaceChild("light1",
      CubeListBuilder.create().texOffs(24, 0).addBox(-1, 0, 1, 6, 1, 6),  PartPose.ZERO)
    wing2.addOrReplaceChild("light2",
      CubeListBuilder.create().texOffs(24, 0).addBox(-5, 0, 1, 6, 1, 6),  PartPose.ZERO)
    wing3.addOrReplaceChild("light3",
      CubeListBuilder.create().texOffs(24, 0).addBox(-5, 0, -7, 6, 1, 6), PartPose.ZERO)

    // テクスチャサイズ 64x32 を指定（元コードの texWidth/texHeight と同じ）
    LayerDefinition.create(mesh, 64, 32)
  }
}

// ---------------------------------------------------------------------------
// HoverBootRenderer クラス本体
// ---------------------------------------------------------------------------
// 1.18.2: BipedModel[LivingEntity](0.5f) → HumanoidModel[LivingEntity](root)
// HumanoidModel のコンストラクタは焼き付け済みの ModelPart ルートを受け取る。
class HoverBootRenderer(root: ModelPart) extends HumanoidModel[LivingEntity](root) {

  // 各パーツへの参照（getChild は名前でアクセス）
  // HumanoidModel 親クラスが leftLeg / rightLeg フィールドを持っているのでそれを活用する。
  private val bootLeft  = leftLeg.getChild("boot_left")
  private val bootRight = rightLeg.getChild("boot_right")

  // ライトパーツへの参照（フルブライト描画用）
  private val light0 = bootLeft.getChild("wing0").getChild("light0")
  private val light1 = bootLeft.getChild("wing1").getChild("light1")
  private val light2 = bootRight.getChild("wing2").getChild("light2")
  private val light3 = bootRight.getChild("wing3").getChild("light3")

  private val allLightParts = Seq(light0, light1, light2, light3)

  // 光の色（元コードと同じデフォルト値）
  var lightColor: Int = 0x66DD55

  // 元コードと同じ: 頭・帽子・胴体・腕を非表示にする。
  // HumanoidModel では head, hat, body, rightArm, leftArm フィールドが定義されている。
  head.visible    = false
  hat.visible     = false
  body.visible    = false
  rightArm.visible = false
  leftArm.visible  = false

  override def setupAnim(entity: LivingEntity, f1: Float, f2: Float, f3: Float, f4: Float, f5: Float): Unit = {
    super.setupAnim(entity, f1, f2, f3, f4, f5)
    // 元コードと同じ補正
    crouching = entity.isCrouching
    young     = false
  }

  override def renderToBuffer(
                               poseStack: PoseStack,
                               consumer: VertexConsumer,
                               light: Int,
                               overlay: Int,
                               r: Float, g: Float, b: Float, a: Float
                             ): Unit = {
    // ---- ステップ1: ライトパーツを一時的に非表示にしてから通常レンダー ----
    // これにより super.renderToBuffer ではライト以外のパーツだけが light で描画される。
    allLightParts.foreach(_.visible = false)
    super.renderToBuffer(poseStack, consumer, light, overlay, r, g, b, a)
    allLightParts.foreach(_.visible = true)

    // ---- ステップ2: ライトパーツをフルブライト + 乗算カラーで個別描画 ----
    // 元の LightModelRenderer.render() と等価:
    //   super.render(stack, builder, LightTexture.pack(15, 15), OverlayTexture.NO_OVERLAY, r*rm, g*gm, b*bm, a)
    //
    // 1.18.2 では ModelPart.translateAndRotate(PoseStack) で親のトランスフォームを手動適用してから
    // ModelPart.render() で当該パーツ自身のトランスフォーム＋描画を行う。
    val rm = ((lightColor >>> 16) & 0xFF) / 255f
    val gm = ((lightColor >>> 8)  & 0xFF) / 255f
    val bm = ((lightColor >>> 0)  & 0xFF) / 255f
    val fullBright = LightTexture.pack(15, 15)

    // --- light0 (wing0 の子 → bootLeft の孫 → leftLeg の子孫) ---
    poseStack.pushPose()
    leftLeg.translateAndRotate(poseStack)
    bootLeft.translateAndRotate(poseStack)
    bootLeft.getChild("wing0").translateAndRotate(poseStack)
    light0.render(poseStack, consumer, fullBright, overlay, r * rm, g * gm, b * bm, a)
    poseStack.popPose()

    // --- light1 (wing1 の子) ---
    poseStack.pushPose()
    leftLeg.translateAndRotate(poseStack)
    bootLeft.translateAndRotate(poseStack)
    bootLeft.getChild("wing1").translateAndRotate(poseStack)
    light1.render(poseStack, consumer, fullBright, overlay, r * rm, g * gm, b * bm, a)
    poseStack.popPose()

    // --- light2 (wing2 の子 → bootRight の孫 → rightLeg の子孫) ---
    poseStack.pushPose()
    rightLeg.translateAndRotate(poseStack)
    bootRight.translateAndRotate(poseStack)
    bootRight.getChild("wing2").translateAndRotate(poseStack)
    light2.render(poseStack, consumer, fullBright, overlay, r * rm, g * gm, b * bm, a)
    poseStack.popPose()

    // --- light3 (wing3 の子) ---
    poseStack.pushPose()
    rightLeg.translateAndRotate(poseStack)
    bootRight.translateAndRotate(poseStack)
    bootRight.getChild("wing3").translateAndRotate(poseStack)
    light3.render(poseStack, consumer, fullBright, overlay, r * rm, g * gm, b * bm, a)
    poseStack.popPose()
  }
}