package net.createcontour.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;
import dev.engine_room.flywheel.api.visualization.VisualizationManager;
import net.createcontour.blockentity.ContourCogwheelBlockEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;

/**
 * Baked fallback renderer for {@code contour_cogwheel} (issue #23) - the
 * documented non-instanced path for when Flywheel visuals are off (the
 * normal case is the instanced {@link ContourCogwheelVisual}; Create's
 * own kinetic BEs fall back exactly this way).
 *
 * The rotating geometry is the same painted MESH emission, captured once
 * per (axis, material) into {@link net.createmod.catnip.render.SuperByteBuffer}s
 * by {@link ContourKineticGeometry#buffers} and spun each frame by
 * {@link KineticBlockEntityRenderer#renderRotatingBuffer} - angle,
 * direction and the small-cog parity offset all come from Create's own
 * kinetic transform, so the paint visibly rotates at network speed.
 */
public class ContourCogwheelRenderer
	extends KineticBlockEntityRenderer<ContourCogwheelBlockEntity> {

	public ContourCogwheelRenderer(BlockEntityRendererProvider.Context context) {
		super(context);
	}

	@Override
	protected void renderSafe(ContourCogwheelBlockEntity be, float partialTicks,
		PoseStack ms, MultiBufferSource buffer, int light, int overlay) {
		if (VisualizationManager.supportsVisualization(be.getLevel()))
			return; // instanced visual owns the render
		for (ContourKineticGeometry.BufferEntry entry : ContourKineticGeometry
			.buffers(be.getBlockState(), be.getMaterial())) {
			VertexConsumer vb = buffer.getBuffer(entry.type());
			renderRotatingBuffer(be, entry.buffer(), ms, vb, light);
		}
	}
}
