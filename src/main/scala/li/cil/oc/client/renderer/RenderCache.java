package li.cil.oc.client.renderer;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class RenderCache implements MultiBufferSource {

    public static class DrawEntry implements AutoCloseable {
        private final RenderType type;
        private VertexBuffer vertexBuffer;

        public DrawEntry(RenderType type, BufferBuilder builder) {
            this.type = type;
            try {
                builder.end();
                this.vertexBuffer = new VertexBuffer();
                this.vertexBuffer.bind();
                this.vertexBuffer.upload(builder);
                VertexBuffer.unbind();
            } catch (Exception e) {
                if (this.vertexBuffer != null) this.vertexBuffer.close();
                this.vertexBuffer = null;
            }
        }

        public void render(Matrix4f modelView, Matrix4f projection) {
            if (this.vertexBuffer == null) return;

            this.type.setupRenderState();
            ShaderInstance shader = RenderSystem.getShader();

            if (shader == null) {
                if (this.type.format().getElements().contains(com.mojang.blaze3d.vertex.DefaultVertexFormat.ELEMENT_UV0)) {
                    RenderSystem.setShader(GameRenderer::getPositionColorTexShader);
                } else {
                    RenderSystem.setShader(GameRenderer::getPositionColorShader);
                }
                shader = RenderSystem.getShader();
            }

            if (shader != null) {
                RenderSystem.polygonOffset(-1.0f, -10.0f);
                RenderSystem.enablePolygonOffset();

                this.vertexBuffer.bind();
                this.vertexBuffer.drawWithShader(modelView, projection, shader);

                RenderSystem.polygonOffset(0.0f, 0.0f);
                RenderSystem.disablePolygonOffset();
            }

            this.type.clearRenderState();
        }

        @Override
        public void close() {
            if (vertexBuffer != null) {
                vertexBuffer.close();
                vertexBuffer = null;
            }
        }
    }

    private final List<DrawEntry> cached = new ArrayList<>();
    private RenderType activeType;
    private BufferBuilder activeBuilder;

    public RenderCache() {}

    public boolean isEmpty() { return cached.isEmpty() && activeBuilder == null; }

    public void clear() {
        cached.forEach(DrawEntry::close);
        cached.clear();
        activeType = null;
        activeBuilder = null;
    }

    private void flush() {
        if (activeType != null && activeBuilder != null) {
            cached.add(new DrawEntry(activeType, activeBuilder));
        }
        activeType = null;
        activeBuilder = null;
    }

    @Override
    public @NotNull VertexConsumer getBuffer(@NotNull RenderType type) {
        if (activeType != null && !activeType.equals(type)) {
            flush();
        }
        if (activeBuilder == null) {
            activeType = type;
            activeBuilder = new BufferBuilder(2048);
            activeBuilder.begin(type.mode(), type.format());
        }
        return activeBuilder;
    }

    public void finish() {
        flush();
    }

    public void render(PoseStack poseStack) {
        if (isEmpty()) return;

        // IMPORTANT: Combine RenderSystem's current model-view stack with the passed PoseStack.
        //
        // During GUI rendering, GameRenderer sets RenderSystem.getModelViewStack() to
        // identity + translate(0, 0, -2000) before calling Screen.render(), and uses an
        // orthographic projection with near=1000, far=3000.
        //
        // If we only use poseStack.last().pose() (which contains GUI screen-space transforms
        // like translate/scale but NO Z offset), all vertices remain at Z=0 in view space,
        // which is OUTSIDE the [1000, 3000] frustum -> clipped and invisible in GUI.
        //
        // During block-entity rendering, RenderSystem.getModelViewStack() is effectively
        // identity, so multiplying by it is a no-op and existing behaviour is preserved.
        Matrix4f modelView = RenderSystem.getModelViewStack().last().pose().copy();
        modelView.multiply(poseStack.last().pose());

        Matrix4f projection = RenderSystem.getProjectionMatrix();

        Matrix3f identityNormal = new Matrix3f();
        identityNormal.setIdentity();

        Matrix3f oldInverseRotation = RenderSystem.getInverseViewRotationMatrix();
        RenderSystem.setInverseViewRotationMatrix(identityNormal);

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        for (DrawEntry entry : cached) {
            entry.render(modelView, projection);
        }

        VertexBuffer.unbind();
        RenderSystem.setInverseViewRotationMatrix(oldInverseRotation);
    }
}