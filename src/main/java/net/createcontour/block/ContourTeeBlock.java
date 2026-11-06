package net.createcontour.block;

import java.util.List;
import net.createcontour.blockentity.ContourBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Pipe tee: a straight r3 run (both faces) plus a perpendicular r3
 * branch to a third face - the manifold junction completing the
 * pipe family (tube, elbow, flange, tee). Twelve states: run axis
 * times the four perpendicular branch directions. Collision is
 * two box tubes that never under-fill the junction.
 */
public class ContourTeeBlock extends ContourBlock {

	public enum Tee implements StringRepresentable {
		XD("xd", Direction.Axis.X, Direction.DOWN),
		XU("xu", Direction.Axis.X, Direction.UP),
		XN("xn", Direction.Axis.X, Direction.NORTH),
		XS("xs", Direction.Axis.X, Direction.SOUTH),
		YN("yn", Direction.Axis.Y, Direction.NORTH),
		YS("ys", Direction.Axis.Y, Direction.SOUTH),
		YE("ye", Direction.Axis.Y, Direction.EAST),
		YW("yw", Direction.Axis.Y, Direction.WEST),
		ZD("zd", Direction.Axis.Z, Direction.DOWN),
		ZU("zu", Direction.Axis.Z, Direction.UP),
		ZE("ze", Direction.Axis.Z, Direction.EAST),
		ZW("zw", Direction.Axis.Z, Direction.WEST);
		private final String name;
		public final Direction.Axis run;
		public final Direction branch;
		Tee(String name, Direction.Axis run, Direction branch) {
			this.name = name;
			this.run = run;
			this.branch = branch;
		}
		@Override
		public String getSerializedName() {
			return name;
		}
		public static Tee of(Direction.Axis run, Direction branch) {
			for (Tee t : values())
				if (t.run == run && t.branch == branch)
					return t;
			return XD;
		}
	}

	public static final EnumProperty<Tee> TEE =
		EnumProperty.create("tee", Tee.class);

	public ContourTeeBlock(Properties pProperties) {
		super(pProperties);
		registerDefaultState(defaultBlockState()
			.setValue(WATERLOGGED, false).setValue(TEE, Tee.XU));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder.add(TEE));
	}

	/** Main tube u 0..16 + branch tube v from face to center. */
	public static List<float[]> boxesFor(BlockState state) {
		Tee t = state.getValue(TEE);
		int u = t.run.ordinal(), v = t.branch.getAxis().ordinal();
		int w = 3 - u - v;
		boolean pos = t.branch.getAxisDirection()
			== Direction.AxisDirection.POSITIVE;
		float[] main = {0, 0, 0, 16, 16, 16};
		main[v] = 5f;
		main[v + 3] = 11f;
		main[w] = 5f;
		main[w + 3] = 11f;
		float[] br = {0, 0, 0, 16, 16, 16};
		br[v] = pos ? 8f : 0f;
		br[v + 3] = pos ? 16f : 8f;
		br[u] = 5f;
		br[u + 3] = 11f;
		br[w] = 5f;
		br[w + 3] = 11f;
		return List.of(main, br);
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos,
		CollisionContext ctx) {
		VoxelShape s = Shapes.empty();
		for (float[] b : boxesFor(state))
			s = Shapes.or(s, Block.box(b[0], b[1], b[2], b[3], b[4], b[5]));
		return s;
	}

	@Override
	public boolean useShapeForLightOcclusion(BlockState state) {
		return true;
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext ctx) {
		// Run = clicked face axis; branch = the horizontal direction
		// the player faces when it's perpendicular, else UP.
		Direction.Axis run = ctx.getClickedFace().getAxis();
		Direction look = ctx.getHorizontalDirection();
		Direction branch = look.getAxis() != run ? look : Direction.UP;
		return withWater(defaultBlockState()
			.setValue(TEE, Tee.of(run, branch)), ctx);
	}

	@Override
	public BlockState rotate(BlockState state, Rotation rotation) {
		Tee t = state.getValue(TEE);
		return state.setValue(TEE, Tee.of(t.run == Direction.Axis.X
			? Direction.Axis.Z : t.run == Direction.Axis.Z
				? Direction.Axis.X : Direction.Axis.Y,
			rotation.rotate(t.branch)));
	}

	@Override
	public BlockState mirror(BlockState state, Mirror mirror) {
		Tee t = state.getValue(TEE);
		return state.setValue(TEE, Tee.of(t.run == Direction.Axis.X
			&& mirror == Mirror.LEFT_RIGHT ? Direction.Axis.Z
			: t.run == Direction.Axis.Z && mirror == Mirror.LEFT_RIGHT
				? Direction.Axis.X : t.run,
			mirror.mirror(t.branch)));
	}

	@Override
	protected BlockState wrenchTarget(BlockState state, ContourBlockEntity be) {
		Tee[] vals = Tee.values();
		return state.setValue(TEE,
			vals[(state.getValue(TEE).ordinal() + 1) % vals.length]);
	}

	@Override
	protected Block nextInFamily() {
		return null;
	}
}
