package net.createcontour.client;

import com.mojang.math.Axis;
import net.createcontour.block.ContourSignBlock;
import net.createcontour.blockentity.ContourSignBlockEntity;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import com.mojang.blaze3d.vertex.PoseStack;

/**
 * Draws the sign's 4 lines on the plate's outer face: wall signs
 * render on the facing plane, floor/ceiling signs on the horizontal
 * plate face. Text scale fits 4x9-unit lines into the 8px band and
 * shrinks further if a line is wider than the 14px plate.
 */
public class ContourSignRenderer
	implements BlockEntityRenderer<ContourSignBlockEntity> {

	private static final float BASE_SCALE = 2f / 16f / 9f; // 2px per line

	private final Font font;

	public ContourSignRenderer(BlockEntityRendererProvider.Context ctx) {
		font = ctx.getFont();
	}

	@Override
	public void render(ContourSignBlockEntity be, float partial, PoseStack pose,
		MultiBufferSource buffer, int light, int overlay) {
		BlockState state = be.getBlockState();
		if (!(state.getBlock() instanceof ContourSignBlock))
			return;
		Direction f = state.getValue(ContourSignBlock.FACING);

		int maxW = 1;
		for (int i = 0; i < ContourSignBlockEntity.LINES; i++)
			maxW = Math.max(maxW, font.width(be.getLine(i)));
		if (maxW == 1)
			return;
		float scale = Math.min(BASE_SCALE, 14f / 16f / maxW);

		pose.pushPose();
		switch (f) {
			case NORTH -> {
				pose.translate(0.5, 0.5, -0.002);
				pose.mulPose(Axis.YP.rotationDegrees(180));
			}
			case SOUTH -> {
				pose.translate(0.5, 0.5, 1.002);
			}
			case EAST -> {
				pose.translate(1.002, 0.5, 0.5);
				pose.mulPose(Axis.YP.rotationDegrees(90));
			}
			case WEST -> {
				pose.translate(-0.002, 0.5, 0.5);
				pose.mulPose(Axis.YP.rotationDegrees(-90));
			}
			case UP -> {
				pose.translate(0.5, 0.628, 0.5);
				pose.mulPose(Axis.YP.rotationDegrees(180));
				pose.mulPose(Axis.XP.rotationDegrees(-90));
			}
			case DOWN -> {
				pose.translate(0.5, 0.372, 0.5);
				pose.mulPose(Axis.XP.rotationDegrees(90));
			}
		}
		pose.scale(scale, -scale, scale); // -y: font rows grow downward
		for (int i = 0; i < ContourSignBlockEntity.LINES; i++) {
			String line = be.getLine(i);
			if (line.isEmpty())
				continue;
			font.drawInBatch(line, -font.width(line) / 2f,
				-18f + i * 9f, 0xFF202020, false,
				pose.last().pose(), buffer, Font.DisplayMode.POLYGON_OFFSET,
				0, light);
		}
		pose.popPose();
	}
}
