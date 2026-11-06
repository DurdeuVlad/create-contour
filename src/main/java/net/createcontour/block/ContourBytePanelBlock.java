package net.createcontour.block;

import java.util.List;
import net.createcontour.blockentity.ContourBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Byte panel: the quarter-size sibling of the face panel - an 8x8x1px
 * plate occupying one of the four quadrants of a face. FACING selects
 * the face, QUADRANT (0-3) picks which corner of that face the plate
 * hugs, read clockwise from the face's lower-left as seen by the
 * placer. Placement derives both from the hit face and hit point. The
 * wrench cycles the quadrant around the face.
 */
public class ContourBytePanelBlock extends ContourBlock {

	public static final EnumProperty<Direction> FACING =
		BlockStateProperties.FACING;
	public static final IntegerProperty QUADRANT =
		IntegerProperty.create("quadrant", 0, 3);

	public ContourBytePanelBlock(Properties pProperties) {
		super(pProperties);
		registerDefaultState(defaultBlockState().setValue(WATERLOGGED, false)
			.setValue(FACING, Direction.UP).setValue(QUADRANT, 0));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder.add(FACING).add(QUADRANT));
	}

	/** Quadrant order: 0 lower-left, 1 lower-right, 2 upper-left, 3 upper-right (viewed on the face). */
	public static List<float[]> boxesFor(BlockState state) {
		Direction f = state.getValue(FACING);
		int q = state.getValue(QUADRANT);
		boolean hiU = (q & 2) != 0, hiV = (q & 1) != 0;
		return List.of(switch (f) {
			case DOWN -> new float[]{hiU ? 8 : 0, 0, hiV ? 8 : 0,
				hiU ? 16 : 8, 1, hiV ? 16 : 8};
			case UP -> new float[]{hiU ? 8 : 0, 15, hiV ? 8 : 0,
				hiU ? 16 : 8, 16, hiV ? 16 : 8};
			case NORTH -> new float[]{hiU ? 8 : 0, hiV ? 8 : 0, 0,
				hiU ? 16 : 8, hiV ? 16 : 8, 1};
			case SOUTH -> new float[]{hiU ? 0 : 8, hiV ? 8 : 0, 15,
				hiU ? 8 : 16, hiV ? 16 : 8, 16};
			case WEST -> new float[]{0, hiV ? 8 : 0, hiU ? 0 : 8,
				1, hiV ? 16 : 8, hiU ? 8 : 16};
			default -> new float[]{15, hiV ? 8 : 0, hiU ? 0 : 8,
				16, hiV ? 16 : 8, hiU ? 8 : 16};
		});
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
		Direction f = ctx.getClickedFace().getOpposite();
		Vec3 hit = ctx.getClickLocation();
		BlockPos p = ctx.getClickedPos();
		double hx = hit.x - p.getX(), hy = hit.y - p.getY(), hz = hit.z - p.getZ();
		boolean hiU, hiV;
		switch (f) {
			case DOWN, UP -> { hiU = hx >= 0.5; hiV = hz >= 0.5; }
			case NORTH -> { hiU = hx >= 0.5; hiV = hy >= 0.5; }
			case SOUTH -> { hiU = hx < 0.5; hiV = hy >= 0.5; }
			case WEST -> { hiU = hz < 0.5; hiV = hy >= 0.5; }
			default -> { hiU = hz < 0.5; hiV = hy >= 0.5; }
		}
		return withWater(defaultBlockState().setValue(FACING, f)
			.setValue(QUADRANT, (hiU ? 2 : 0) | (hiV ? 1 : 0)), ctx);
	}

	@Override
	public BlockState rotate(BlockState state, Rotation rotation) {
		Direction f = state.getValue(FACING);
		return state.setValue(FACING, rotation.rotate(f));
	}

	@Override
	public BlockState mirror(BlockState state, Mirror mirror) {
		return rotate(state, mirror.getRotation(state.getValue(FACING)));
	}

	@Override
	protected BlockState wrenchTarget(BlockState state, ContourBlockEntity be) {
		return state.cycle(QUADRANT);
	}

	@Override
	protected Block nextInFamily() {
		return null;
	}
}
