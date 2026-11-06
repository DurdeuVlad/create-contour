package net.createcontour.client;

import java.util.ArrayList;
import java.util.List;
import net.createcontour.block.ContourCornerSlopeBlock;
import net.createcontour.registry.ContourRegistries;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Half;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

/**
 * Hip-roof corner wedge: two vertical triangle caps on the apex's
 * boundary faces, two interior slope triangles meeting at the diagonal
 * ridge, and the full base square. Triangles use the same
 * epsilon-duplicated-vertex quad convention as the slope model.
 */
public class ContourCornerSlopeModel extends ContourModel {


	public ContourCornerSlopeModel(BakedModel original, @Nullable BlockState boundState,
		@Nullable BlockState boundMaterial) {
		super(original, boundState, boundMaterial);
	}

	@Override
	protected int maskFor(BlockState state) {
		return 0;
	}

	@Override
	protected List<float[]> boxesFor(BlockState state) {
		return List.of(); // every face is emitted by extraQuads
	}

	private static Vector3f v(float x, float y, float z) {
		return new Vector3f(x, y, z);
	}

	private static Vector3f[] quad(Vector3f p1, Vector3f p2, Vector3f p3) {
		return ShapeQuads.tri(p1, p2, p3);
	}

	private static void emitTri(List<BakedQuad> out, Direction d,
		Vector3f p1, Vector3f p2, Vector3f p3,
		java.util.function.Function<Direction, List<ShapeQuads.SrcQuad>> srcFor) {
		Vector3f[] q = quad(p1, p2, p3);
		for (ShapeQuads.SrcQuad src : srcFor.apply(d))
			out.add(ShapeQuads.emitFace(d, q, src));
	}

	@Override
	protected List<BakedQuad> extraQuads(BlockState state, @Nullable Direction side,
		java.util.function.Function<Direction, List<ShapeQuads.SrcQuad>> srcFor) {
		ContourCornerSlopeBlock.Corner c =
			state.getValue(ContourCornerSlopeBlock.CORNER);
		boolean top = state.getValue(ContourCornerSlopeBlock.HALF) == Half.TOP;
		float ax = c.ax, az = c.az, bx = 16 - ax, bz = 16 - az;
		float yA = top ? 16 : 0;  // base square plane
		float yB = top ? 0 : 16;  // apex height
		List<BakedQuad> out = new ArrayList<>();

		// base square: DOWN for BOTTOM, UP for TOP
		Direction base = top ? Direction.UP : Direction.DOWN;
		if (side == base) {
			Vector3f[] q = ShapeQuads.faceCorners(base, 0, 16, 0, 16, yA);
			for (ShapeQuads.SrcQuad src : srcFor.apply(base))
				out.add(ShapeQuads.emitFace(base, q, src));
		}
		// z-face triangle at z=az
		Direction zFace = az == 0 ? Direction.NORTH : Direction.SOUTH;
		if (side == zFace)
			emitTri(out, zFace, v(bx, yA, az), v(ax, yA, az), v(ax, yB, az), srcFor);
		// x-face triangle at x=ax
		Direction xFace = ax == 0 ? Direction.WEST : Direction.EAST;
		if (side == xFace)
			emitTri(out, xFace, v(ax, yA, bz), v(ax, yA, az), v(ax, yB, az), srcFor);
		// interior ridge triangles, double-sided
		if (side == null) {
			Vector3f[] ta = quad(v(bx, yA, az), v(bx, yA, bz), v(ax, yB, az));
			Vector3f[] tb = quad(v(ax, yA, bz), v(ax, yB, az), v(bx, yA, bz));
			for (ShapeQuads.SrcQuad src : srcFor.apply(Direction.UP)) {
				out.add(ShapeQuads.emitFace(Direction.UP, ta, src));
				out.add(ShapeQuads.emitFace(Direction.DOWN,
					new Vector3f[]{ta[3], ta[2], ta[1], ta[0]}, src));
				out.add(ShapeQuads.emitFace(Direction.UP, tb, src));
				out.add(ShapeQuads.emitFace(Direction.DOWN,
					new Vector3f[]{tb[3], tb[2], tb[1], tb[0]}, src));
			}
		}
		return out;
	}

	@Override
	protected BlockState neutralState() {
		return ContourRegistries.CONTOUR_CORNER_SLOPE.get().defaultBlockState();
	}
}
