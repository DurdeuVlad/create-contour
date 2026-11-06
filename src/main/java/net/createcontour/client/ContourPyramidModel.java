package net.createcontour.client;

import java.util.ArrayList;
import java.util.List;
import net.createcontour.block.ContourPyramidBlock;
import net.createcontour.registry.ContourRegistries;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Half;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

/**
 * Pyramid: base square, a triangle cap on each of the four side faces,
 * and four interior slope triangles meeting at the center apex. Same
 * epsilon-quad triangle convention as the other slope models.
 */
public class ContourPyramidModel extends ContourModel {


	public ContourPyramidModel(BakedModel original, @Nullable BlockState boundState,
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
		boolean top = state.getValue(ContourPyramidBlock.HALF) == Half.TOP;
		float yA = top ? 16 : 0;  // base plane
		float yB = top ? 0 : 16;  // apex height
		Vector3f apex = v(8, yB, 8);
		List<BakedQuad> out = new ArrayList<>();

		Direction base = top ? Direction.UP : Direction.DOWN;
		if (side == base) {
			Vector3f[] q = ShapeQuads.faceCorners(base, 0, 16, 0, 16, yA);
			for (ShapeQuads.SrcQuad src : srcFor.apply(base))
				out.add(ShapeQuads.emitFace(base, q, src));
		}
		// side triangle caps: base edge + apex on that face's midline
		if (side == Direction.NORTH)
			emitTri(out, Direction.NORTH,
				v(0, yA, 0), v(16, yA, 0), v(8, yB, 0), srcFor);
		if (side == Direction.SOUTH)
			emitTri(out, Direction.SOUTH,
				v(16, yA, 16), v(0, yA, 16), v(8, yB, 16), srcFor);
		if (side == Direction.WEST)
			emitTri(out, Direction.WEST,
				v(0, yA, 16), v(0, yA, 0), v(0, yB, 8), srcFor);
		if (side == Direction.EAST)
			emitTri(out, Direction.EAST,
				v(16, yA, 0), v(16, yA, 16), v(16, yB, 8), srcFor);
		// four interior slope triangles, double-sided
		if (side == null) {
			Vector3f[][] tris = {
				quad(v(0, yA, 0), v(0, yA, 16), apex),   // west slope
				quad(v(0, yA, 0), v(16, yA, 0), apex),   // north slope
				quad(v(16, yA, 0), v(16, yA, 16), apex), // east slope
				quad(v(0, yA, 16), v(16, yA, 16), apex), // south slope
			};
			for (Vector3f[] t : tris)
				for (ShapeQuads.SrcQuad src : srcFor.apply(Direction.UP)) {
					out.add(ShapeQuads.emitFace(Direction.UP, t, src));
					out.add(ShapeQuads.emitFace(Direction.DOWN,
						new Vector3f[]{t[3], t[2], t[1], t[0]}, src));
				}
		}
		return out;
	}

	@Override
	protected BlockState neutralState() {
		return ContourRegistries.CONTOUR_PYRAMID.get().defaultBlockState();
	}
}
