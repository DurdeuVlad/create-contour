package net.createcontour.block;

import java.util.List;
import net.createcontour.blockentity.ContourBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.api.distmarker.Dist;

/**
 * Material sign: a 14x10 plate on any face - wall mount with a
 * small nib, floor post, or ceiling hanger. FACING is the outward
 * text direction. Right-click with an empty or non-material hand
 * opens the 4-line editor (client-side); the wrench cycles the
 * facing. Text lives on ContourSignBlockEntity and syncs with the
 * standard BE packet.
 */
public class ContourSignBlock extends ContourBlock {

	public static final EnumProperty<Direction> FACING =
		BlockStateProperties.FACING;

	public ContourSignBlock(Properties pProperties) {
		super(pProperties);
		registerDefaultState(defaultBlockState()
			.setValue(WATERLOGGED, false).setValue(FACING, Direction.UP));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder.add(FACING));
	}

	@Override
	public net.minecraft.world.level.block.entity.BlockEntityType<? extends com.simibubi.create.content.decoration.copycat.CopycatBlockEntity>
		getBlockEntityType() {
		return net.createcontour.registry.ContourRegistries.CONTOUR_SIGN_BE.get();
	}

	/** Plate + mount geometry per facing; shared by render and collision. */
	public static List<float[]> boxesFor(BlockState state) {
		Direction f = state.getValue(FACING);
		int a = f.getAxis().ordinal();
		boolean pos = f.getAxisDirection() == Direction.AxisDirection.POSITIVE;
		List<float[]> out = new java.util.ArrayList<>(2);
		if (f.getAxis() == Direction.Axis.Y) {
			// floor post or ceiling hanger + horizontal plate
			float[] stem = {6, 0, 6, 10, 8, 10};
			float[] plate = {1, 0, 1, 15, 2, 15};
			if (pos) { // UP: post below, plate on top
				stem[4] = 8;
				plate[1] = 8; plate[4] = 10;
			} else {   // DOWN: stem above, plate below
				stem[1] = 8; stem[4] = 16;
				plate[1] = 6; plate[4] = 8;
			}
			out.add(stem);
			out.add(plate);
			return out;
		}
		// wall plate: 2px thick against the face + 2px mount nib
		float[] plate = {1, 3, 1, 15, 13, 15};
		float[] nib = {7, 6, 7, 9, 10, 9};
		if (pos) {
			plate[a] = 14; plate[a + 3] = 16;
			nib[a] = 12; nib[a + 3] = 14;
		} else {
			plate[a] = 0; plate[a + 3] = 2;
			nib[a] = 2; nib[a + 3] = 4;
		}
		out.add(plate);
		out.add(nib);
		return out;
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
		return withWater(defaultBlockState()
			.setValue(FACING, ctx.getClickedFace()), ctx);
	}

	@Override
	public BlockState rotate(BlockState state, Rotation rotation) {
		return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
	}

	@Override
	public BlockState mirror(BlockState state, Mirror mirror) {
		return state.rotate(mirror.getRotation(state.getValue(FACING)));
	}

	/**
	 * Empty or non-material click opens the editor on the client;
	 * sneak suppresses it (block interaction convention). The actual
	 * write is server-validated by SignTextPayload.
	 */
	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level level,
		BlockPos pos, Player player, BlockHitResult hit) {
		if (player.isShiftKeyDown())
			return InteractionResult.PASS;
		if (level.isClientSide && FMLEnvironment.dist == Dist.CLIENT)
			net.createcontour.client.SignClient.openSignEditor(pos);
		return InteractionResult.sidedSuccess(level.isClientSide);
	}

	@Override
	protected BlockState wrenchTarget(BlockState state, ContourBlockEntity be) {
		return state.cycle(FACING);
	}

	@Override
	protected Block nextInFamily() {
		return null;
	}
}
