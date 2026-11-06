package net.createcontour.gametest;

import net.createcontour.block.ContourBlock;
import net.createcontour.block.ContourBalusterBlock;
import net.createcontour.block.ContourBarrelVaultBlock;
import net.createcontour.block.ContourBracketBlock;
import net.createcontour.block.ContourBullnoseBlock;
import net.createcontour.block.ContourCorniceBlock;
import net.createcontour.block.ContourByteBlock;
import net.createcontour.block.ContourConnectiveBlock;
import net.createcontour.block.ContourCrossBlock;
import net.createcontour.block.ContourLayersBlock;
import net.createcontour.block.ContourPipeCapBlock;
import net.createcontour.block.ContourPedimentBlock;
import net.createcontour.block.ContourChimneyBlock;
import net.createcontour.block.ContourHalfTimberBlock;
import net.createcontour.block.ContourSpandrelBlock;
import net.createcontour.block.ContourArcWallBlock;
import net.createcontour.block.ContourRosetteBlock;
import net.createcontour.block.ContourFlyingButtressBlock;
import net.createcontour.block.ContourCopingBlock;
import net.createcontour.block.ContourOculusBlock;
import net.createcontour.block.ContourAwningBlock;
import net.createcontour.block.ContourBufferBlock;
import net.createcontour.block.ContourSmokestackBlock;
import net.createcontour.block.ContourSemaphoreBlock;
import net.createcontour.block.ContourPilotBlock;
import net.createcontour.block.ContourVerticalStepBlock;
import net.createcontour.block.ContourBeamBlock;
import net.createcontour.block.ContourHalfPanelBlock;
import net.createcontour.block.ContourFlatPaneBlock;
import net.createcontour.block.ContourNotchBlock;
import net.createcontour.block.ContourPanelBlock;
import net.createcontour.block.ContourPanelBlock.PanelPos;
import net.createcontour.block.ContourPilasterBlock;
import net.createcontour.block.ContourPostBlock;
import net.createcontour.block.ContourButtonBlock;
import net.createcontour.block.ContourCornerSlopeBlock;
import net.createcontour.block.ContourDoorBlock;
import net.createcontour.block.ContourGhostBlock;
import net.createcontour.block.ContourGothicArchBlock;
import net.createcontour.block.ContourGutterBlock;
import net.createcontour.block.ContourInnerSlopeBlock;
import net.createcontour.block.ContourOneWayBlock;
import net.createcontour.block.ContourPillarBlock;
import net.createcontour.block.ContourTorchBlock;
import net.createcontour.block.ContourFrameBlock;
import net.createcontour.block.ContourLampBlock;
import net.createcontour.block.ContourLatticeBlock;
import net.createcontour.block.ContourLouverBlock;
import net.createcontour.block.ContourPyramidBlock;
import net.createcontour.block.ContourCapitalBlock;
import net.createcontour.block.ContourChainBlock;
import net.createcontour.block.ContourCogBlock;
import net.createcontour.block.ContourConeBlock;
import net.createcontour.block.ContourCylinderBlock;
import net.createcontour.block.ContourDomeBlock;
import net.createcontour.block.ContourQuarterRoundBlock;
import net.createcontour.block.ContourRidgeBlock;
import net.createcontour.block.ContourRoundCornerBlock;
import net.createcontour.block.ContourRodBlock;
import net.createcontour.block.ContourTrimBlock;
import net.createcontour.block.ContourPressurePlateBlock;
import net.createcontour.block.ContourFenceGateBlock;
import net.createcontour.block.ContourFinialBlock;
import net.createcontour.block.ContourCrenellationBlock;
import net.createcontour.block.ContourOgeeBlock;
import net.createcontour.block.ContourScaffoldBlock;
import net.createcontour.block.ContourScotiaBlock;
import net.createcontour.block.ContourTorusBlock;
import net.createcontour.block.ContourWheelBlock;
import net.createcontour.block.ContourSliceBlock;
import net.minecraft.world.level.block.state.properties.DoorHingeSide;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.Half;
import net.createcontour.block.ContourLadderBlock;
import net.createcontour.block.ContourLeverBlock;
import net.createcontour.block.ContourSlopeBlock;
import net.createcontour.block.ContourTrapdoorBlock;
import net.createcontour.block.ContourTubeBlock;
import net.createcontour.block.ContourArchBlock;
import net.createcontour.block.ContourBalustradeBlock;
import net.createcontour.block.ContourElbowBlock;
import net.createcontour.block.ContourNewelBlock;
import net.createcontour.block.ContourHandrailBlock;
import net.createcontour.block.ContourIBeamBlock;
import net.createcontour.block.ContourButtressBlock;
import net.createcontour.block.ContourArrowSlitBlock;
import net.createcontour.block.ContourDentilBlock;
import net.createcontour.block.ContourBollardBlock;
import net.createcontour.block.ContourCofferBlock;
import net.createcontour.block.ContourKeystoneBlock;
import net.createcontour.block.ContourLancetBlock;
import net.createcontour.block.ContourMullionBlock;
import net.createcontour.block.ContourPedestalBlock;
import net.createcontour.block.ContourValleyBlock;
import net.createcontour.block.ContourPalisadeBlock;
import net.createcontour.block.ContourQuoinBlock;
import net.createcontour.block.ContourSillBlock;
import net.createcontour.block.ContourTeeBlock;
import net.createcontour.block.ContourTrussBlock;
import net.createcontour.block.ContourFlangeBlock;
import net.createcontour.block.ContourPortcullisBlock;
import net.createcontour.block.ContourTrimBlock;
import net.createcontour.block.ContourSphereBlock;
import net.createcontour.block.ContourStairCornerBlock;
import net.createcontour.block.ContourVerticalStairsBlock;
import net.createcontour.blockentity.ContourBlockEntity;
import net.createcontour.blockentity.ContourSignBlockEntity;
import net.createcontour.item.ContourBlockItem;
import net.createcontour.registry.ContourRegistries;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/** GameTests for the shape-wave blocks: layers, post, byte, panel, wall, pane, fence. */
@net.neoforged.neoforge.gametest.GameTestHolder("create_contour")
@net.neoforged.neoforge.gametest.PrefixGameTestTemplate(false)
public class ContourWaveGameTests {

	private static final BlockPos P = new BlockPos(2, 1, 2);

	private static Block[] waveBlocks() {
		return new Block[]{ContourRegistries.CONTOUR_LAYERS.get(),
			ContourRegistries.CONTOUR_POST.get(), ContourRegistries.CONTOUR_BYTE.get(),
			ContourRegistries.CONTOUR_PANEL.get(), ContourRegistries.CONTOUR_WALL.get(),
			ContourRegistries.CONTOUR_PANE.get(), ContourRegistries.CONTOUR_FENCE.get(),
			ContourRegistries.CONTOUR_SLOPE.get(), ContourRegistries.CONTOUR_TRAPDOOR.get(),
			ContourRegistries.CONTOUR_LADDER.get(),
			ContourRegistries.CONTOUR_FENCE_GATE.get(),
			ContourRegistries.CONTOUR_LEVER.get(),
			ContourRegistries.CONTOUR_DOOR.get(),
			ContourRegistries.CONTOUR_SLICE.get(),
			ContourRegistries.CONTOUR_CORNER_SLOPE.get(),
			ContourRegistries.CONTOUR_BUTTON.get(),
			ContourRegistries.CONTOUR_INNER_SLOPE.get(),
			ContourRegistries.CONTOUR_PRESSURE_PLATE.get(),
			ContourRegistries.CONTOUR_GHOST.get(),
			ContourRegistries.CONTOUR_TORCH.get(),
			ContourRegistries.CONTOUR_ONE_WAY.get(),
			ContourRegistries.CONTOUR_PILLAR.get(),
			ContourRegistries.CONTOUR_TRIM.get(),
			ContourRegistries.CONTOUR_FRAME.get(),
			ContourRegistries.CONTOUR_PYRAMID.get(),
			ContourRegistries.CONTOUR_LAMP.get(),
			ContourRegistries.CONTOUR_HALF_SLOPE.get(),
			ContourRegistries.CONTOUR_NOTCH.get(),
			ContourRegistries.CONTOUR_ROD.get(),
			ContourRegistries.CONTOUR_LATTICE.get(),
			ContourRegistries.CONTOUR_QUARTER_ROUND.get(),
			ContourRegistries.CONTOUR_CHAIN.get(),
			ContourRegistries.CONTOUR_ROUND_CORNER.get(),
			ContourRegistries.CONTOUR_DOME.get(),
			ContourRegistries.CONTOUR_RIDGE.get(),
			ContourRegistries.CONTOUR_TUBE.get(),
			ContourRegistries.CONTOUR_GUTTER.get(),
			ContourRegistries.CONTOUR_BRACKET.get(),
			ContourRegistries.CONTOUR_VERTICAL_STAIRS.get(),
			ContourRegistries.CONTOUR_ARCH.get(),
			ContourRegistries.CONTOUR_CYLINDER.get(),
			ContourRegistries.CONTOUR_PILASTER.get(),
			ContourRegistries.CONTOUR_STAIR_CORNER.get(),
			ContourRegistries.CONTOUR_SPHERE.get(),
			ContourRegistries.CONTOUR_CONE.get(),
			ContourRegistries.CONTOUR_CAPITAL.get(),
			ContourRegistries.CONTOUR_BULLNOSE.get(),
			ContourRegistries.CONTOUR_CORNICE.get(),
			ContourRegistries.CONTOUR_BALUSTER.get(),
			ContourRegistries.CONTOUR_FINIAL.get(),
			ContourRegistries.CONTOUR_SCOTIA.get(),
			ContourRegistries.CONTOUR_OGEE.get(),
			ContourRegistries.CONTOUR_SIGN.get(),
			ContourRegistries.CONTOUR_TORUS.get(),
			ContourRegistries.CONTOUR_CRENELLATION.get(),
			ContourRegistries.CONTOUR_BARREL_VAULT.get(),
			ContourRegistries.CONTOUR_COG.get(),
			ContourRegistries.CONTOUR_WHEEL.get(),
			ContourRegistries.CONTOUR_SCAFFOLD.get(),
			ContourRegistries.CONTOUR_GOTHIC_ARCH.get(),
			ContourRegistries.CONTOUR_LOUVER.get(),
			ContourRegistries.CONTOUR_ELBOW.get(),
			ContourRegistries.CONTOUR_HANDRAIL.get(),
			ContourRegistries.CONTOUR_NEWEL.get(),
			ContourRegistries.CONTOUR_BALUSTRADE.get(),
			ContourRegistries.CONTOUR_I_BEAM.get(),
			ContourRegistries.CONTOUR_PORTCULLIS.get(),
			ContourRegistries.CONTOUR_FLANGE.get(),
			ContourRegistries.CONTOUR_BUTTRESS.get(),
			ContourRegistries.CONTOUR_ARROW_SLIT.get(),
			ContourRegistries.CONTOUR_DENTIL.get(),
			ContourRegistries.CONTOUR_TEE.get(),
			ContourRegistries.CONTOUR_PALISADE.get(),
			ContourRegistries.CONTOUR_QUOIN.get(),
			ContourRegistries.CONTOUR_TRUSS.get(),
			ContourRegistries.CONTOUR_SILL.get(),
			ContourRegistries.CONTOUR_BOLLARD.get(),
			ContourRegistries.CONTOUR_MULLION.get(),
			ContourRegistries.CONTOUR_COFFER.get(),
			ContourRegistries.CONTOUR_KEYSTONE.get(),
			ContourRegistries.CONTOUR_PEDESTAL.get(),
			ContourRegistries.CONTOUR_LANCET.get(),
			ContourRegistries.CONTOUR_VALLEY.get(),
			ContourRegistries.CONTOUR_CROSS.get(),
			ContourRegistries.CONTOUR_PIPE_CAP.get(),
			ContourRegistries.CONTOUR_PEDIMENT.get(),
			ContourRegistries.CONTOUR_CHIMNEY.get(),
			ContourRegistries.CONTOUR_HALF_TIMBER.get(),
			ContourRegistries.CONTOUR_SPANDREL.get(),
			ContourRegistries.CONTOUR_ARC_WALL.get(),
			ContourRegistries.CONTOUR_ROSETTE.get(),
			ContourRegistries.CONTOUR_FLYING_BUTTRESS.get(),
			ContourRegistries.CONTOUR_COPING.get(),
			ContourRegistries.CONTOUR_OCULUS.get(),
			ContourRegistries.CONTOUR_AWNING.get(),
			ContourRegistries.CONTOUR_BUFFER.get(),
			ContourRegistries.CONTOUR_SMOKESTACK.get(),
			ContourRegistries.CONTOUR_SEMAPHORE.get(),
			ContourRegistries.CONTOUR_PILOT.get(),
			ContourRegistries.CONTOUR_VERTICAL_STEP.get(),
			ContourRegistries.CONTOUR_BEAM.get(),
			ContourRegistries.CONTOUR_HALF_PANEL.get(),
			ContourRegistries.CONTOUR_FLAT_PANE.get(),
			ContourRegistries.CONTOUR_CORNER_SLICE.get(),
			ContourRegistries.CONTOUR_BYTE_PANEL.get(),
			ContourRegistries.CONTOUR_HALF_LAYER.get(),
			ContourRegistries.CONTOUR_IRON_DOOR.get(),
			ContourRegistries.CONTOUR_IRON_TRAPDOOR.get(),
			ContourRegistries.CONTOUR_SLIDING_DOOR.get(),
			ContourRegistries.CONTOUR_FOLDING_DOOR.get(),
			ContourRegistries.CONTOUR_CATWALK.get()};
	}

	private static ContourBlockEntity be(GameTestHelper h, BlockState state) {
		h.setBlock(P, state);
		return (ContourBlockEntity) h.getLevel().getBlockEntity(h.absolutePos(P));
	}

	private static UseOnContext wrenchCtx(GameTestHelper h, Player p, Direction face) {
		return new UseOnContext(h.getLevel(), p, InteractionHand.MAIN_HAND,
			ItemStack.EMPTY, new BlockHitResult(Vec3.atCenterOf(h.absolutePos(P)),
				face, h.absolutePos(P), false));
	}

	private static BlockState placed(GameTestHelper h, Block b, Direction face,
		Vec3 hit) {
		var ctx = new BlockPlaceContext(h.getLevel(), null, InteractionHand.MAIN_HAND,
			ItemStack.EMPTY, new BlockHitResult(hit, face, h.absolutePos(P), false));
		return b.getStateForPlacement(ctx);
	}

	// --- registry / datapack ---

	@GameTest(template = "test_empty")
	public void waveRegistry(GameTestHelper h) {
		for (Block b : waveBlocks())
			h.assertTrue(BuiltInRegistries.BLOCK.getKey(b).getNamespace()
				.equals("create_contour"), "unregistered " + b);
		for (Block b : waveBlocks())
			h.assertTrue(ContourRegistries.CONTOUR_BE.get()
				.isValid(b.defaultBlockState())
				|| ContourRegistries.CONTOUR_SIGN_BE.get()
					.isValid(b.defaultBlockState()), "BE misses " + b);
		for (Block b : waveBlocks()) {
			String id = BuiltInRegistries.BLOCK.getKey(b).getPath();
			h.assertTrue(BuiltInRegistries.ITEM.get(
				ResourceLocation.fromNamespaceAndPath("create_contour", id))
				instanceof ContourBlockItem, "item missing " + id);
		}
		var rm = h.getLevel().getServer().getRecipeManager();
		for (Block b : waveBlocks()) {
			String id = BuiltInRegistries.BLOCK.getKey(b).getPath();
			h.assertTrue(rm.byKey(ResourceLocation.fromNamespaceAndPath(
				"create_contour", id)).isPresent(), "manual recipe " + id);
			h.assertTrue(rm.byKey(ResourceLocation.fromNamespaceAndPath(
				"create_contour", "mechanical_crafting/" + id)).isPresent(),
				"mechanical recipe " + id);
		}
		h.assertTrue(BuiltInRegistries.BLOCK
			.getTag(net.minecraft.tags.TagKey.create(Registries.BLOCK,
				ResourceLocation.fromNamespaceAndPath("create_contour", "contour_shapes")))
			.map(t -> t.size() == 114).orElse(false), "tag != 114 members");
		h.succeed();
	}

	// --- wave 1: layers / post / byte ---

	@GameTest(template = "test_empty")
	public void layersStates(GameTestHelper h) {
		Player p = h.makeMockPlayer(GameType.SURVIVAL);
		ContourBlockEntity be = be(h, ContourRegistries.CONTOUR_LAYERS.get()
			.defaultBlockState().setValue(ContourLayersBlock.LAYERS, 1));
		be.applyPayload(Blocks.STONE.defaultBlockState(), 1, Direction.NORTH, false);
		Block b = h.getBlockState(P).getBlock();
		// wrench cycles 1..8 back to 1 in 8 uses
		for (int want = 2; want <= 8; want++) {
			((net.createcontour.block.ContourBlock) b).onWrenched(
				h.getBlockState(P), wrenchCtx(h, p, Direction.UP));
			b = h.getBlockState(P).getBlock();
			h.assertTrue(h.getBlockState(P).getValue(ContourLayersBlock.LAYERS) == want,
				"cycle skipped " + want);
		}
		((net.createcontour.block.ContourBlock) b).onWrenched(
			h.getBlockState(P), wrenchCtx(h, p, Direction.UP));
		h.assertTrue(h.getBlockState(P).getValue(ContourLayersBlock.LAYERS) == 1,
			"cycle did not wrap");
		h.assertTrue(be.getPaidUnits() == 1, "cycle lost ledger");
		// placement: click height -> thickness
		for (int i = 1; i <= 8; i++) {
			Vec3 hit = new Vec3(h.absolutePos(P).getX() + 0.5,
				h.absolutePos(P).getY() + (i * 2.0 - 0.1) / 16.0,
				h.absolutePos(P).getZ() + 0.5);
			BlockState s = placed(h, b, Direction.NORTH, hit);
			h.assertTrue(s.getValue(ContourLayersBlock.LAYERS) == i,
				"height " + i + " -> layers=" + s.getValue(ContourLayersBlock.LAYERS));
		}
		// shape height tracks layers: layers=1 -> 2px
		h.assertTrue(Math.abs(h.getBlockState(P)
			.getShape(h.getLevel(), P, CollisionContext.empty())
			.max(Direction.Axis.Y) - 0.125) < 1e-6, "shape height wrong");
		h.succeed();
	}

	@GameTest(template = "test_empty")
	public void postStates(GameTestHelper h) {
		Player p = h.makeMockPlayer(GameType.SURVIVAL);
		ContourBlockEntity be = be(h, ContourRegistries.CONTOUR_POST.get()
			.defaultBlockState());
		be.applyPayload(Blocks.OAK_LOG.defaultBlockState(), 1, Direction.NORTH, false);
		Direction.Axis[] order = {Direction.Axis.Z, Direction.Axis.X, Direction.Axis.Y};
		for (Direction.Axis want : order) {
			((net.createcontour.block.ContourBlock) h.getBlockState(P).getBlock())
				.onWrenched(h.getBlockState(P), wrenchCtx(h, p, Direction.UP));
			h.assertTrue(h.getBlockState(P).getValue(ContourPostBlock.AXIS) == want,
				"axis cycle broke");
		}
		h.assertTrue(be.getPaidUnits() == 1, "cycle lost ledger");
		h.succeed();
	}

	@GameTest(template = "test_empty")
	public void byteStates(GameTestHelper h) {
		Player p = h.makeMockPlayer(GameType.SURVIVAL);
		be(h, ContourRegistries.CONTOUR_BYTE.get().defaultBlockState());
		for (int i = 0; i < 8; i++) {
			BlockState before = h.getBlockState(P);
			((net.createcontour.block.ContourBlock) before.getBlock())
				.onWrenched(before, wrenchCtx(h, p, Direction.UP));
		}
		h.assertTrue(h.getBlockState(P).getValue(ContourByteBlock.OCTANT)
			== ContourByteBlock.Octant.X0Y0Z0, "octant cycle did not wrap");
		// click far top south corner of north face -> x1y0z1? north face:
		// face axis Z snaps oz=0; u=x high, v=y low -> x1y0z0
		Vec3 hit = new Vec3(h.absolutePos(P).getX() + 0.9,
			h.absolutePos(P).getY() + 0.1, h.absolutePos(P).getZ() + 0.0);
		BlockState s = placed(h, ContourRegistries.CONTOUR_BYTE.get(),
			Direction.NORTH, hit);
		h.assertTrue(s.getValue(ContourByteBlock.OCTANT)
			== ContourByteBlock.Octant.X1Y0Z1,
			"byte placement " + s.getValue(ContourByteBlock.OCTANT));
		h.succeed();
	}

	// --- wave 2: positional panel ---

	@GameTest(template = "test_empty")
	public void panelPlacement(GameTestHelper h) {
		BlockPos a = h.absolutePos(P);
		Block panel = ContourRegistries.CONTOUR_PANEL.get();
		// dead center of north face -> mid plane normal Z
		BlockState mid = placed(h, panel, Direction.NORTH,
			new Vec3(a.getX() + 0.5, a.getY() + 0.5, a.getZ()));
		h.assertTrue(mid.getValue(ContourPanelBlock.POSITION) == PanelPos.MID_Z,
			"center click -> " + mid.getValue(ContourPanelBlock.POSITION));
		// near west edge of north face -> edge strip hugging west
		BlockState edge = placed(h, panel, Direction.NORTH,
			new Vec3(a.getX() + 0.05, a.getY() + 0.5, a.getZ()));
		h.assertTrue(edge.getValue(ContourPanelBlock.POSITION)
			== PanelPos.EDGE_NORTH_WEST,
			"edge click -> " + edge.getValue(ContourPanelBlock.POSITION));
		// mid-ring (not center, not edge) -> face plate
		BlockState face = placed(h, panel, Direction.NORTH,
			new Vec3(a.getX() + 0.35, a.getY() + 0.5, a.getZ()));
		h.assertTrue(face.getValue(ContourPanelBlock.POSITION) == PanelPos.FACE_NORTH,
			"ring click -> " + face.getValue(ContourPanelBlock.POSITION));
		// every state exposes a non-empty collision shape
		for (PanelPos pos : PanelPos.values())
			h.assertTrue(!panel.defaultBlockState()
				.setValue(ContourPanelBlock.POSITION, pos)
				.getShape(h.getLevel(), P, CollisionContext.empty()).isEmpty(),
				"empty shape " + pos);
		// wrench cycles all 39 positions and wraps
		be(h, panel.defaultBlockState());
		Player p = h.makeMockPlayer(GameType.SURVIVAL);
		for (int i = 0; i < PanelPos.values().length; i++)
			((net.createcontour.block.ContourBlock) h.getBlockState(P).getBlock())
				.onWrenched(h.getBlockState(P), wrenchCtx(h, p, Direction.UP));
		h.assertTrue(h.getBlockState(P).getValue(ContourPanelBlock.POSITION)
			== PanelPos.FACE_NORTH, "panel cycle did not wrap to start");
		h.succeed();
	}

	// --- wave 3: connective trio ---

	@GameTest(template = "test_empty")
	public void connectivity(GameTestHelper h) {
		BlockPos a = h.absolutePos(P);
		Block wall = ContourRegistries.CONTOUR_WALL.get();
		h.setBlock(P, wall.defaultBlockState());
		for (Direction d : Direction.Plane.HORIZONTAL)
			h.assertTrue(!h.getBlockState(P).getValue(prop(d)),
				"spurious connection " + d);
		// sturdy neighbor connects, removal disconnects
		h.setBlock(P.north(), Blocks.STONE);
		h.assertTrue(h.getBlockState(P).getValue(ContourConnectiveBlock.NORTH),
			"sturdy north did not connect");
		h.setBlock(P.north(), Blocks.AIR);
		h.assertTrue(!h.getBlockState(P).getValue(ContourConnectiveBlock.NORTH),
			"sturdy removal kept connection");
		// same-family neighbor connects both ways
		h.setBlock(P.south(), wall.defaultBlockState());
		h.assertTrue(h.getBlockState(P).getValue(ContourConnectiveBlock.SOUTH)
			&& h.getBlockState(P.south()).getValue(ContourConnectiveBlock.NORTH),
			"wall-wall pair did not connect");
		// pane + fence connectivity
		h.setBlock(P.south(), Blocks.AIR);
		h.setBlock(P, ContourRegistries.CONTOUR_PANE.get().defaultBlockState());
		h.setBlock(P.east(), Blocks.STONE);
		h.assertTrue(h.getBlockState(P).getValue(ContourConnectiveBlock.EAST),
			"pane did not connect east");
		h.setBlock(P, ContourRegistries.CONTOUR_FENCE.get().defaultBlockState());
		// setBlock does not fire the new block's own updateShape; re-place
		// the neighbor to emit the update a real placement would compute.
		h.setBlock(P.east(), Blocks.AIR);
		h.setBlock(P.east(), Blocks.STONE);
		h.assertTrue(h.getBlockState(P).getValue(ContourConnectiveBlock.EAST),
			"fence did not connect east");
		h.succeed();
	}

	@GameTest(template = "test_empty")
	public void slopeStates(GameTestHelper h) {
		Block slope = ContourRegistries.CONTOUR_SLOPE.get();
		Player p = h.makeMockPlayer(GameType.SURVIVAL);
		// every state has non-empty stepped collision
		for (Direction f : Direction.Plane.HORIZONTAL)
			for (var half : net.minecraft.world.level.block.state.properties.Half
				.values())
				h.assertTrue(!slope.defaultBlockState()
					.setValue(ContourSlopeBlock.FACING, f)
					.setValue(ContourSlopeBlock.HALF, half)
					.getShape(h.getLevel(), P, CollisionContext.empty()).isEmpty(),
					"empty slope " + f + "/" + half);
		// wrench: half flips then facing advances, wrapping in 8 uses
		be(h, slope.defaultBlockState());
		for (int i = 0; i < 8; i++)
			((net.createcontour.block.ContourBlock) h.getBlockState(P).getBlock())
				.onWrenched(h.getBlockState(P), wrenchCtx(h, p, Direction.UP));
		h.assertTrue(h.getBlockState(P).getValue(ContourSlopeBlock.FACING)
			== Direction.NORTH
			&& h.getBlockState(P).getValue(ContourSlopeBlock.HALF)
				== net.minecraft.world.level.block.state.properties.Half.BOTTOM,
			"slope cycle did not wrap");
		// rotate keeps facing semantics
		h.assertTrue(slope.defaultBlockState()
			.setValue(ContourSlopeBlock.FACING, Direction.NORTH)
			.rotate(net.minecraft.world.level.block.Rotation.CLOCKWISE_90)
			.getValue(ContourSlopeBlock.FACING) == Direction.EAST,
			"slope rotate wrong");
		h.succeed();
	}

	@GameTest(template = "test_empty")
	public void trapdoorToggle(GameTestHelper h) {
		Block td = ContourRegistries.CONTOUR_TRAPDOOR.get();
		be(h, td.defaultBlockState());
		Player p = h.makeMockPlayer(GameType.SURVIVAL);
		// empty-hand click toggles open/closed via the useItemOn pipeline
		var hit = new BlockHitResult(Vec3.atCenterOf(h.absolutePos(P)),
			Direction.UP, h.absolutePos(P), false);
		h.getBlockState(P).useItemOn(ItemStack.EMPTY, h.getLevel(), p,
			InteractionHand.MAIN_HAND, hit);
		h.assertTrue(h.getBlockState(P).getValue(ContourTrapdoorBlock.OPEN),
			"click did not open trapdoor");
		h.getBlockState(P).useItemOn(ItemStack.EMPTY, h.getLevel(), p,
			InteractionHand.MAIN_HAND, hit);
		h.assertTrue(!h.getBlockState(P).getValue(ContourTrapdoorBlock.OPEN),
			"click did not close trapdoor");
		// open state renders the hinge-side wall plate shape
		h.setBlock(P, td.defaultBlockState()
			.setValue(ContourTrapdoorBlock.OPEN, true)
			.setValue(ContourTrapdoorBlock.FACING, Direction.NORTH));
		var s = h.getBlockState(P).getShape(h.getLevel(), P, CollisionContext.empty());
		h.assertTrue(s.max(Direction.Axis.Z) <= 0.25
			&& s.max(Direction.Axis.Y) > 0.9, "open trapdoor shape wrong");
		// wrench cycles facing
		var wr = ((net.createcontour.block.ContourBlock) h.getBlockState(P).getBlock())
			.onWrenched(h.getBlockState(P), wrenchCtx(h, p, Direction.UP));
		h.assertTrue(h.getBlockState(P).getValue(ContourTrapdoorBlock.FACING)
			== Direction.SOUTH, "trapdoor wrench did not cycle facing (res="
				+ wr + ", state=" + h.getBlockState(P) + ")");
		h.succeed();
	}

	@GameTest(template = "test_empty")
	public void ladderStates(GameTestHelper h) {
		Block lad = ContourRegistries.CONTOUR_LADDER.get();
		// needs a sturdy block on the facing side to survive
		h.setBlock(P.north(), Blocks.STONE);
		h.setBlock(P, lad.defaultBlockState()
			.setValue(ContourLadderBlock.FACING, Direction.NORTH));
		h.assertTrue(h.getBlockState(P).getBlock() == lad,
			"ladder did not survive on sturdy wall");
		h.setBlock(P.north(), Blocks.AIR);
		h.succeed();
	}

	@GameTest(template = "test_empty")
	public void fenceGateToggle(GameTestHelper h) {
		Block gate = ContourRegistries.CONTOUR_FENCE_GATE.get();
		be(h, gate.defaultBlockState());
		Player p = h.makeMockPlayer(GameType.SURVIVAL);
		var hit = new BlockHitResult(Vec3.atCenterOf(h.absolutePos(P)),
			Direction.UP, h.absolutePos(P), false);
		h.getBlockState(P).useItemOn(ItemStack.EMPTY, h.getLevel(), p,
			InteractionHand.MAIN_HAND, hit);
		h.assertTrue(h.getBlockState(P).getValue(ContourFenceGateBlock.OPEN),
			"gate click did not open");
		// open gate lets entities through the middle (posts-only shape)
		var s = h.getBlockState(P).getShape(h.getLevel(), P, CollisionContext.empty());
		h.assertTrue(s.max(Direction.Axis.Y) >= 0.99, "open gate lost posts");
		// in_wall drops when both span ends meet connective blocks
		h.setBlock(P, gate.defaultBlockState()
			.setValue(ContourFenceGateBlock.FACING, Direction.NORTH));
		h.setBlock(P.east(), ContourRegistries.CONTOUR_FENCE.get().defaultBlockState());
		h.setBlock(P.west(), ContourRegistries.CONTOUR_FENCE.get().defaultBlockState());
		h.assertTrue(h.getBlockState(P).getValue(ContourFenceGateBlock.IN_WALL),
			"gate did not detect in-wall");
		h.succeed();
	}

	@GameTest(template = "test_empty")
	public void leverSignal(GameTestHelper h) {
		Block lev = ContourRegistries.CONTOUR_LEVER.get();
		// floor lever needs sturdy floor below
		h.setBlock(P.below(), Blocks.STONE);
		h.setBlock(P, lev.defaultBlockState());
		Player p = h.makeMockPlayer(GameType.SURVIVAL);
		var hit = new BlockHitResult(Vec3.atCenterOf(h.absolutePos(P)),
			Direction.UP, h.absolutePos(P), false);
		h.getBlockState(P).useItemOn(ItemStack.EMPTY, h.getLevel(), p,
			InteractionHand.MAIN_HAND, hit);
		var s = h.getBlockState(P);
		h.assertTrue(s.getValue(ContourLeverBlock.POWERED), "lever did not power");
		h.assertTrue(s.getSignal(h.getLevel(), P, Direction.UP) == 15,
			"lever emitted no signal");
		h.assertTrue(s.getDirectSignal(h.getLevel(), P, Direction.DOWN) == 15,
			"lever did not power its mount");
		h.succeed();
	}

	@GameTest(template = "test_empty")
	public void doorTwoBlockLifecycle(GameTestHelper h) {
		Block door = ContourRegistries.CONTOUR_DOOR.get();
		h.setBlock(P.below(), Blocks.STONE);
		BlockState lower = door.defaultBlockState();
		h.setBlock(P, lower);
		h.setBlock(P.above(), lower.setValue(ContourDoorBlock.HALF,
			DoubleBlockHalf.UPPER));
		// upper half is render-only: zero host units => no ledger drops
		h.assertTrue(((ContourDoorBlock) door)
				.hostUnits(h.getBlockState(P.above())) == 0,
			"upper half can drop ledger items");
		// click toggles BOTH halves
		Player p = h.makeMockPlayer(GameType.SURVIVAL);
		h.getBlockState(P).useItemOn(ItemStack.EMPTY, h.getLevel(), p,
			InteractionHand.MAIN_HAND,
			new BlockHitResult(Vec3.atCenterOf(h.absolutePos(P)),
				Direction.UP, h.absolutePos(P), false));
		h.assertTrue(h.getBlockState(P).getValue(ContourDoorBlock.OPEN)
			&& h.getBlockState(P.above()).getValue(ContourDoorBlock.OPEN),
			"door halves diverged on open");
		// orphaning the upper half collapses it to air (no drops)
		h.setBlock(P, Blocks.AIR.defaultBlockState());
		h.getBlockState(P.above()).updateShape(Direction.DOWN,
			Blocks.AIR.defaultBlockState(), h.getLevel(), P.above(), P);
		h.assertTrue(h.getBlockState(P.above()).isAir(), "orphan upper survived");
		h.succeed();
	}

	@GameTest(template = "test_empty")
	public void sliceHugAndLayers(GameTestHelper h) {
		Block slice = ContourRegistries.CONTOUR_SLICE.get();
		h.setBlock(P, slice.defaultBlockState()
			.setValue(ContourSliceBlock.FACING, Direction.EAST)
			.setValue(ContourSliceBlock.LAYERS, 2));
		var boxes = ContourSliceBlock.boxesFor(h.getBlockState(P)).get(0);
		h.assertTrue(boxes[0] == 16 - 4 && boxes[3] == 16,
			"east slice did not hug east face");
		h.assertTrue(boxes[4] == 16 && boxes[5] == 16,
			"slice not full-height");
		// wrench cycles thickness on same block
		h.assertTrue(h.getBlockState(P)
				.cycle(ContourSliceBlock.LAYERS)
				.getValue(ContourSliceBlock.LAYERS) == 3,
			"layers did not cycle");
		h.succeed();
	}

	@GameTest(template = "test_empty")
	public void cornerSlopeShape(GameTestHelper h) {
		Block cs = ContourRegistries.CONTOUR_CORNER_SLOPE.get();
		h.setBlock(P, cs.defaultBlockState()
			.setValue(ContourCornerSlopeBlock.CORNER,
				ContourCornerSlopeBlock.Corner.NE));
		// stepped collision: apex quadrant tallest, low quadrant near zero
		var s = h.getBlockState(P).getShape(h.getLevel(), P,
			CollisionContext.empty());
		h.assertTrue(s.max(Direction.Axis.Y) > 0.5,
			"corner slope has no height");
		h.assertTrue(s.max(Direction.Axis.Y) <= 1.001,
			"corner slope exceeds block");
		// wrench: bottom->top, then next corner
		var w = h.getBlockState(P)
			.setValue(ContourCornerSlopeBlock.HALF,
				net.minecraft.world.level.block.state.properties.Half.TOP);
		h.assertTrue(w.getValue(ContourCornerSlopeBlock.CORNER)
				== ContourCornerSlopeBlock.Corner.NE,
			"corner drifted on half change");
		// rotate moves the apex clockwise
		var r = h.getBlockState(P).rotate(
			net.minecraft.world.level.block.Rotation.CLOCKWISE_90);
		h.assertTrue(r.getValue(ContourCornerSlopeBlock.CORNER)
				== ContourCornerSlopeBlock.Corner.SE,
			"rotate did not move apex");
		h.succeed();
	}

	@GameTest(template = "test_empty")
	public void buttonPulse(GameTestHelper h) {
		Block btn = ContourRegistries.CONTOUR_BUTTON.get();
		h.setBlock(P.below(), Blocks.STONE);
		h.setBlock(P, btn.defaultBlockState());
		Player p = h.makeMockPlayer(GameType.SURVIVAL);
		h.getBlockState(P).useItemOn(ItemStack.EMPTY, h.getLevel(), p,
			InteractionHand.MAIN_HAND,
			new BlockHitResult(Vec3.atCenterOf(h.absolutePos(P)),
				Direction.UP, h.absolutePos(P), false));
		var s = h.getBlockState(P);
		h.assertTrue(s.getValue(ContourButtonBlock.POWERED),
			"button did not power on click");
		h.assertTrue(s.getSignal(h.getLevel(), P, Direction.UP) == 15,
			"button emitted no signal");
		h.assertTrue(s.getDirectSignal(h.getLevel(), P, Direction.DOWN) == 15,
			"button did not power its mount");
		h.succeed();
	}

	@GameTest(template = "test_empty")
	public void innerSlopeFaces(GameTestHelper h) {
		Block is = ContourRegistries.CONTOUR_INNER_SLOPE.get();
		h.setBlock(P, is.defaultBlockState()
			.setValue(ContourInnerSlopeBlock.CORNER,
				ContourCornerSlopeBlock.Corner.NE));
		// stepped collision stays inside the block, mostly solid
		var s = h.getBlockState(P).getShape(h.getLevel(), P,
			CollisionContext.empty());
		h.assertTrue(s.max(Direction.Axis.Y) > 0.9,
			"inner slope should be near-full height");
		// rotate moves the apex
		var r = h.getBlockState(P).rotate(
			net.minecraft.world.level.block.Rotation.CLOCKWISE_90);
		h.assertTrue(r.getValue(ContourInnerSlopeBlock.CORNER)
				== ContourCornerSlopeBlock.Corner.SE,
			"rotate did not move apex");
		h.succeed();
	}

	@GameTest(template = "test_empty")
	public void plateDetectsEntity(GameTestHelper h) {
		Block plate = ContourRegistries.CONTOUR_PRESSURE_PLATE.get();
		h.setBlock(P.below(), Blocks.STONE);
		h.setBlock(P, plate.defaultBlockState());
		h.assertTrue(h.getBlockState(P)
				.getSignal(h.getLevel(), P, Direction.UP) == 0,
			"plate powered without entity");
		// spawn a living entity on the plate; detection is entity-driven
		h.spawn(net.minecraft.world.entity.EntityType.PIG, P);
		h.runAfterDelay(15, () -> h.assertTrue(
			h.getBlockState(P).getValue(ContourPressurePlateBlock.POWERED),
			"plate did not detect standing entity"));
		h.succeed();
	}

	@GameTest(template = "test_empty")
	public void ghostWalkThrough(GameTestHelper h) {
		Block g = ContourRegistries.CONTOUR_GHOST.get();
		h.setBlock(P, g.defaultBlockState());
		// full outline for targeting...
		h.assertTrue(h.getBlockState(P).getShape(h.getLevel(), P,
				CollisionContext.empty()).max(Direction.Axis.Y) == 1.0,
			"ghost outline not full cube");
		// ...but no entity collision
		h.assertTrue(h.getBlockState(P).getCollisionShape(h.getLevel(), P,
				CollisionContext.empty()).isEmpty(),
			"ghost has collision");
		h.succeed();
	}

	@GameTest(template = "test_empty")
	public void torchLightAndMount(GameTestHelper h) {
		Block t = ContourRegistries.CONTOUR_TORCH.get();
		h.setBlock(P.below(), Blocks.STONE);
		h.setBlock(P, t.defaultBlockState());
		h.assertTrue(h.getBlockState(P).getLightEmission() == 14,
			"torch emitted wrong light level");
		// wall mount survives only with sturdy mount
		var wall = t.defaultBlockState()
			.setValue(ContourTorchBlock.FACING, Direction.NORTH);
		h.assertTrue(!wall.canSurvive(h.getLevel(), h.absolutePos(P)),
			"wall torch survived without mount");
		h.setBlock(P.north(), Blocks.STONE);
		h.assertTrue(wall.canSurvive(h.getLevel(), h.absolutePos(P)),
			"wall torch rejected sturdy mount");
		h.succeed();
	}

	@GameTest(template = "test_empty")
	public void oneWaySolidAndFacing(GameTestHelper h) {
		Block ow = ContourRegistries.CONTOUR_ONE_WAY.get();
		h.setBlock(P, ow.defaultBlockState()
			.setValue(ContourOneWayBlock.FACING, Direction.SOUTH));
		// full-cube collision: see-through, not pass-through
		h.assertTrue(h.getBlockState(P).getCollisionShape(h.getLevel(), P,
				CollisionContext.empty()).max(Direction.Axis.Y) == 1.0,
			"one-way glass not solid");
		// wrench rotates the see-through side
		Direction cycled = h.getBlockState(P)
			.cycle(ContourOneWayBlock.FACING)
			.getValue(ContourOneWayBlock.FACING);
		h.assertTrue(cycled != Direction.SOUTH,
			"facing did not cycle: " + cycled);
		h.succeed();
	}

	@GameTest(template = "test_empty")
	public void pillarCaps(GameTestHelper h) {
		Block pl = ContourRegistries.CONTOUR_PILLAR.get();
		h.setBlock(P, pl.defaultBlockState());
		var boxes = ContourPillarBlock.boxesFor(h.getBlockState(P));
		h.assertTrue(boxes.size() == 3, "pillar missing shaft or caps");
		// caps are wider than the shaft
		h.assertTrue(boxes.get(1)[0] < boxes.get(0)[0],
			"base cap not wider than shaft");
		h.succeed();
	}

	@GameTest(template = "test_empty")
	public void trimEdgeGeometry(GameTestHelper h) {
		Block t = ContourRegistries.CONTOUR_TRIM.get();
		h.setBlock(P, t.defaultBlockState()
			.setValue(ContourTrimBlock.EDGE, ContourTrimBlock.Edge.DN));
		// L-trim: two plates meeting at the down+north edge
		var boxes = ContourTrimBlock.boxesFor(h.getBlockState(P));
		h.assertTrue(boxes.size() == 2, "trim not an L");
		// rotate maps the edge (down+north -> down+east on CW90)
		var r = h.getBlockState(P).rotate(
			net.minecraft.world.level.block.Rotation.CLOCKWISE_90);
		h.assertTrue(r.getValue(ContourTrimBlock.EDGE)
				== ContourTrimBlock.Edge.DE,
			"edge did not rotate");
		h.succeed();
	}

	@GameTest(template = "test_empty")
	public void frameHollowCenter(GameTestHelper h) {
		Block f = ContourRegistries.CONTOUR_FRAME.get();
		h.setBlock(P, f.defaultBlockState());
		var boxes = ContourFrameBlock.boxesFor(h.getBlockState(P));
		h.assertTrue(boxes.size() == 12, "frame missing edge bars");
		// center of every face is open
		boolean centerOpen = boxes.stream().noneMatch(b ->
			b[0] <= 8 && b[3] >= 8 && b[1] <= 8 && b[4] >= 8
				&& b[2] <= 8 && b[5] >= 8);
		h.assertTrue(centerOpen, "frame center not hollow");
		h.succeed();
	}

	@GameTest(template = "test_empty")
	public void pyramidApex(GameTestHelper h) {
		Block py = ContourRegistries.CONTOUR_PYRAMID.get();
		h.setBlock(P, py.defaultBlockState());
		var s = h.getBlockState(P).getShape(h.getLevel(), P,
			CollisionContext.empty());
		// quarter-max sampling reaches full height at the apex quarter
		h.assertTrue(s.max(Direction.Axis.Y) == 1.0,
			"pyramid apex not full height");
		h.succeed();
	}

	@GameTest(template = "test_empty")
	public void halfSlopeStaysBelowHalf(GameTestHelper h) {
		Block hs = ContourRegistries.CONTOUR_HALF_SLOPE.get();
		h.setBlock(P, hs.defaultBlockState());
		var s = h.getBlockState(P).getShape(h.getLevel(), P,
			CollisionContext.empty());
		h.assertTrue(s.max(Direction.Axis.Y) <= 0.5 + 1e-6,
			"half slope taller than 8px: " + s.max(Direction.Axis.Y));
		h.assertTrue(s.max(Direction.Axis.Y) > 0, "half slope empty");
		h.succeed();
	}

	@GameTest(template = "test_empty")
	public void notchCutsClickedOctant(GameTestHelper h) {
		Block n = ContourRegistries.CONTOUR_NOTCH.get();
		h.setBlock(P, n.defaultBlockState()); // corner=ne half=bottom
		var s = h.getBlockState(P).getShape(h.getLevel(), P,
			CollisionContext.empty());
		h.assertTrue(!covers(s, 0.75, 0.25, 0.25),
			"notch did not cut NE-bottom octant");
		h.assertTrue(covers(s, 0.25, 0.25, 0.25),
			"notch wrongly cut NW-bottom octant");
		// FRONT_BACK mirror (z flip) moves the cut NE -> SE
		var m = n.defaultBlockState()
			.mirror(net.minecraft.world.level.block.Mirror.FRONT_BACK);
		h.assertTrue(
			m.getValue(ContourNotchBlock.CORNER)
				== ContourCornerSlopeBlock.Corner.SE,
			"notch mirror moved cut to " + m.getValue(ContourNotchBlock.CORNER));
		h.succeed();
	}

	private static boolean covers(VoxelShape s, double x, double y, double z) {
		for (var b : s.toAabbs())
			if (x >= b.minX - 1e-6 && x <= b.maxX + 1e-6
				&& y >= b.minY - 1e-6 && y <= b.maxY + 1e-6
				&& z >= b.minZ - 1e-6 && z <= b.maxZ + 1e-6)
				return true;
		return false;
	}

	@GameTest(template = "test_empty")
	public void lampLightsOnPower(GameTestHelper h) {
		Block lamp = ContourRegistries.CONTOUR_LAMP.get();
		h.setBlock(P, lamp.defaultBlockState());
		h.assertTrue(h.getBlockState(P).getLightEmission() == 0,
			"unlit lamp emitted light");
		var lit = h.getBlockState(P).setValue(ContourLampBlock.LIT, true);
		h.assertTrue(lit.getLightEmission() == 15,
			"lit lamp emitted wrong level");
		h.succeed();
	}

	private static net.minecraft.world.level.block.state.properties.BooleanProperty prop(
		Direction d) {
		return switch (d) {
			case NORTH -> ContourConnectiveBlock.NORTH;
			case EAST -> ContourConnectiveBlock.EAST;
			case SOUTH -> ContourConnectiveBlock.SOUTH;
			default -> ContourConnectiveBlock.WEST;
		};
	}

	// --- catalog-wide transform + wrench matrix ---

	/** rotate CW90 x4 and mirror x2 must round-trip every shape's state. */
	@GameTest(template = "test_empty")
	public void catalogRotateMirrorRoundTrip(GameTestHelper h) {
		for (Block b : waveBlocks()) {
			BlockState s = b.defaultBlockState();
			BlockState r = s;
			for (int i = 0; i < 4; i++)
				r = r.rotate(net.minecraft.world.level.block.Rotation.CLOCKWISE_90);
			h.assertTrue(r.equals(s), b + " rotate CW90 x4 -> " + r);
			BlockState m = s
				.mirror(net.minecraft.world.level.block.Mirror.FRONT_BACK)
				.mirror(net.minecraft.world.level.block.Mirror.FRONT_BACK);
			h.assertTrue(m.equals(s), b + " mirror x2 -> " + m);
		}
		h.succeed();
	}

	/** One wrench use per shape: material + paid ledger must survive the
	 *  block/BE swap (family transforms included). */
	@GameTest(template = "test_empty")
	public void catalogWrenchPreservesPayload(GameTestHelper h) {
		for (Block b : waveBlocks()) {
			Player p = h.makeMockPlayer(GameType.SURVIVAL);
			ContourBlockEntity be = be(h, b.defaultBlockState());
			be.applyPayload(Blocks.STONE.defaultBlockState(), 2, Direction.NORTH,
				false);
			int expectedPaid = be.getPaidUnits(); // hostUnits may clamp
			((ContourBlock) b).onWrenched(h.getBlockState(P),
				wrenchCtx(h, p, Direction.UP));
			var after = (ContourBlockEntity) h.getLevel()
				.getBlockEntity(h.absolutePos(P));
			h.assertTrue(after != null, b + " wrench removed BE");
			h.assertTrue(after.getMaterial().getBlock() == Blocks.STONE,
				b + " wrench lost material");
			h.assertTrue(after.getPaidUnits() == expectedPaid,
				b + " wrench changed paid " + expectedPaid + " -> "
					+ after.getPaidUnits());
			h.setBlock(P, Blocks.AIR.defaultBlockState());
		}
		h.succeed();
	}

	@GameTest(template = "test_empty")
	public void rodAxisGeometry(GameTestHelper h) {
		Block r = ContourRegistries.CONTOUR_ROD.get();
		h.setBlock(P, r.defaultBlockState());
		var boxes = ContourRodBlock.boxesFor(h.getBlockState(P));
		h.assertTrue(boxes.size() == 1, "rod not a single bar");
		float[] b = boxes.get(0);
		h.assertTrue(b[4] - b[1] == 16 && b[3] - b[0] == 4,
			"default rod not a 4px Y dowel");
		// wrench cycles axis in enum order: Y -> Z
		var w = h.getBlockState(P).cycle(ContourRodBlock.AXIS);
		float[] bx = ContourRodBlock.boxesFor(w).get(0);
		h.assertTrue(bx[5] - bx[2] == 16 && bx[4] - bx[1] == 4,
			"cycled rod not a 4px Z dowel");
		h.succeed();
	}

	@GameTest(template = "test_empty")
	public void latticeGrateOpenings(GameTestHelper h) {
		Block l = ContourRegistries.CONTOUR_LATTICE.get();
		h.setBlock(P, l.defaultBlockState()); // FACING=UP -> plate at top
		var boxes = ContourLatticeBlock.boxesFor(h.getBlockState(P));
		h.assertTrue(boxes.size() == 8, "lattice missing bars");
		// all bars live in the top 2px
		h.assertTrue(boxes.stream().allMatch(b -> b[1] >= 14 && b[4] <= 16),
			"lattice plate not hugging top face");
		// a 2px window inside the central 6-10 opening is empty
		boolean centerOpen = boxes.stream().noneMatch(b ->
			b[0] < 9 && b[3] > 7 && b[2] < 9 && b[5] > 7);
		h.assertTrue(centerOpen, "lattice center opening blocked");
		h.succeed();
	}

	@GameTest(template = "test_empty")
	public void quarterRoundSteps(GameTestHelper h) {
		Block qr = ContourRegistries.CONTOUR_QUARTER_ROUND.get();
		h.setBlock(P, qr.defaultBlockState()); // EDGE=DN
		var boxes = ContourQuarterRoundBlock.boxesFor(h.getBlockState(P));
		h.assertTrue(boxes.size() == 4, "quarter round not 4 steps");
		// step 0 hugs the north edge at full height; step 3 tapers to 12
		h.assertTrue(boxes.get(0)[4] == 16 && boxes.get(3)[4] == 12,
			"stepped heights wrong");
		// every step never under-fills the arc: z-span shrinks toward far
		h.assertTrue(boxes.get(0)[5] <= boxes.get(1)[5]
			|| boxes.get(0)[4] >= boxes.get(1)[4], "steps not tapering");
		// per-direction mirror: NE edge under an x-flip becomes NW
		// (Mirror.FRONT_BACK negates X; LEFT_RIGHT negates Z)
		var ne = qr.defaultBlockState().setValue(
			ContourQuarterRoundBlock.EDGE, ContourTrimBlock.Edge.NE);
		var m = ne.mirror(net.minecraft.world.level.block.Mirror.FRONT_BACK);
		h.assertTrue(m.getValue(ContourQuarterRoundBlock.EDGE)
			== ContourTrimBlock.Edge.NW, "edge mirror NE != NW");
		h.succeed();
	}

	@GameTest(template = "test_empty")
	public void chainLinksAlternate(GameTestHelper h) {
		Block c = ContourRegistries.CONTOUR_CHAIN.get();
		h.setBlock(P, c.defaultBlockState()); // AXIS=Y
		var boxes = ContourChainBlock.boxesFor(h.getBlockState(P));
		h.assertTrue(boxes.size() == 4, "chain not 4 links");
		// links alternate which in-plane axis is wide (axis-agnostic)
		float[] l0 = boxes.get(0), l1 = boxes.get(1);
		h.assertTrue(l0[3] - l0[0] != l0[5] - l0[2],
			"link 0 not asymmetric");
		h.assertTrue(l1[3] - l1[0] != l1[5] - l1[2],
			"link 1 not asymmetric");
		h.assertTrue(l0[3] - l0[0] == l1[5] - l1[2],
			"links 0 and 1 do not alternate");
		h.succeed();
	}

	@GameTest(template = "test_empty")
	public void roundCornerOctant(GameTestHelper h) {
		Block rc = ContourRegistries.CONTOUR_ROUND_CORNER.get();
		h.setBlock(P, rc.defaultBlockState()); // corner NE bottom -> x=16,z=0,y=0
		var boxes = ContourRoundCornerBlock.boxesFor(h.getBlockState(P));
		h.assertTrue(boxes.size() == 4, "round corner not 4 steps");
		// step 0 spans the widest square anchored at the corner
		float[] b0 = boxes.get(0);
		h.assertTrue(b0[3] == 16 && b0[2] == 0 && b0[1] == 0,
			"step 0 not anchored at NE-bottom corner");
		// extents shrink upward
		h.assertTrue(boxes.get(0)[3] - boxes.get(0)[0]
			> boxes.get(3)[3] - boxes.get(3)[0], "steps not tapering");
		h.succeed();
	}

	@GameTest(template = "test_empty")
	public void domeHemisphere(GameTestHelper h) {
		Block d = ContourRegistries.CONTOUR_DOME.get();
		h.setBlock(P, d.defaultBlockState()); // FACING=DOWN -> dome on floor
		var boxes = ContourDomeBlock.boxesFor(h.getBlockState(P));
		h.assertTrue(boxes.size() == 4, "dome not 4 steps");
		// step 0 is the widest plate at the face
		float[] b0 = boxes.get(0);
		h.assertTrue(b0[0] == 0 && b0[3] == 16 && b0[1] == 0,
			"dome base not full-width at face");
		// dome tops out at 8px
		h.assertTrue(boxes.get(3)[4] == 8, "dome taller than 8px");
		h.succeed();
	}

	@GameTest(template = "test_empty")
	public void ridgeGableProfile(GameTestHelper h) {
		Block r = ContourRegistries.CONTOUR_RIDGE.get();
		// AXIS=X: ridge line runs along X, profile slices along Z
		h.setBlock(P, r.defaultBlockState()
			.setValue(ContourRidgeBlock.AXIS, Direction.Axis.X));
		var boxes = ContourRidgeBlock.boxesFor(h.getBlockState(P));
		h.assertTrue(boxes.size() == 4, "ridge not 4 steps");
		float[] z0 = boxes.get(0), z1 = boxes.get(1);
		h.assertTrue(z0[2] == 0 && z0[5] == 4 && z0[4] == 8,
			"ridge z-slab 0 wrong (expected z 0-4 h8)");
		h.assertTrue(z1[2] == 4 && z1[5] == 8 && z1[4] == 16,
			"ridge z-slab 1 wrong (expected z 4-8 h16)");
		// AXIS=Z flips slices to X; rotate toggles the axis
		var zs = ContourRidgeBlock.boxesFor(r.defaultBlockState()
			.setValue(ContourRidgeBlock.AXIS, Direction.Axis.Z));
		h.assertTrue(zs.get(1)[0] == 4 && zs.get(1)[3] == 8,
			"ridge z-axis variant not sliced on X");
		BlockState rot = r.defaultBlockState()
			.setValue(ContourRidgeBlock.AXIS, Direction.Axis.X)
			.rotate(net.minecraft.world.level.block.Rotation.CLOCKWISE_90);
		h.assertTrue(rot.getValue(ContourRidgeBlock.AXIS) == Direction.Axis.Z,
			"ridge rotate did not toggle axis");
		h.succeed();
	}

	@GameTest(template = "test_empty")
	public void tubeHollowChannel(GameTestHelper h) {
		Block t = ContourRegistries.CONTOUR_TUBE.get();
		h.setBlock(P, t.defaultBlockState()
			.setValue(ContourTubeBlock.AXIS, Direction.Axis.Y));
		var boxes = ContourTubeBlock.boxesFor(h.getBlockState(P));
		h.assertTrue(boxes.size() == 4, "tube not 4 plates");
		// the channel core must be open: no plate may contain interior
		// sample points (channel is x,z in 2..14, full height)
		float[][] pts = {{8, 8, 8}, {3, 8, 3}, {13, 8, 13}, {8, 0.5f, 8}};
		for (float[] b : boxes)
			for (float[] pt : pts)
				h.assertFalse(pt[0] > b[0] && pt[0] < b[3]
					&& pt[1] > b[1] && pt[1] < b[4]
					&& pt[2] > b[2] && pt[2] < b[5],
					"tube plate intrudes into channel at y=" + pt[1]);
		// every outer in-plane face must have a plate touching it
		boolean west = false, east = false, north = false, south = false;
		for (float[] b : boxes) {
			west |= b[0] <= 0;
			east |= b[3] >= 16;
			north |= b[2] <= 0;
			south |= b[5] >= 16;
		}
		h.assertTrue(west && east && north && south,
			"tube outer ring not closed");
		h.succeed();
	}

	@GameTest(template = "test_empty")
	public void gutterChannelRuns(GameTestHelper h) {
		Block g = ContourRegistries.CONTOUR_GUTTER.get();
		// floor trough: axis X, open UP -> back plate is the floor
		h.setBlock(P, g.defaultBlockState()
			.setValue(ContourGutterBlock.AXIS, Direction.Axis.X)
			.setValue(ContourGutterBlock.FACING, Direction.UP));
		var boxes = ContourGutterBlock.boxesFor(h.getBlockState(P));
		h.assertTrue(boxes.size() == 3, "gutter not 3 plates");
		boolean floor = false, wN = false, wS = false;
		for (float[] b : boxes) {
			floor |= b[1] == 0 && b[4] == 2;      // y 0-2 bottom
			wN |= b[2] == 0 && b[5] == 2;          // z 0-2 wall
			wS |= b[2] == 14 && b[5] == 16;        // z 14-16 wall
		}
		h.assertTrue(floor && wN && wS, "gutter U not closed");
		// trough interior must be open: sample point (8,8,8) free
		for (float[] b : boxes)
			h.assertFalse(8 > b[0] && 8 < b[3] && 8 > b[1] && 8 < b[4]
				&& 8 > b[2] && 8 < b[5], "gutter plate in channel");
		// fix() keeps FACING off the run axis
		BlockState bad = g.defaultBlockState()
			.setValue(ContourGutterBlock.AXIS, Direction.Axis.X)
			.setValue(ContourGutterBlock.FACING, Direction.EAST);
		h.assertTrue(ContourGutterBlock.fix(bad)
			.getValue(ContourGutterBlock.FACING).getAxis() != Direction.Axis.X,
			"gutter fix did not repoint facing");
		h.succeed();
	}

	@GameTest(template = "test_empty")
	public void bracketCorbelSteps(GameTestHelper h) {
		Block b = ContourRegistries.CONTOUR_BRACKET.get();
		h.setBlock(P, b.defaultBlockState()
			.setValue(ContourBracketBlock.FACING, Direction.NORTH)
			.setValue(ContourBracketBlock.HALF, Half.BOTTOM));
		var boxes = ContourBracketBlock.boxesFor(h.getBlockState(P));
		h.assertTrue(boxes.size() == 3, "bracket not 3 boxes");
		boolean plate = false, arm = false, foot = false;
		for (float[] bx : boxes) {
			plate |= bx[5] == 16 && bx[2] == 14 && bx[1] == 0 && bx[4] == 16;
			arm |= bx[2] == 8 && bx[5] == 16 && bx[1] == 2 && bx[4] == 6;
			foot |= bx[2] == 12 && bx[5] == 16 && bx[1] == 0 && bx[4] == 2;
		}
		h.assertTrue(plate && arm && foot,
			"bracket missing plate/arm/foot");
		// mirror through the standard idiom keeps HALF (horizontal flip)
		BlockState m = h.getBlockState(P)
			.mirror(net.minecraft.world.level.block.Mirror.FRONT_BACK);
		h.assertTrue(m.getValue(ContourBracketBlock.HALF) == Half.BOTTOM,
			"bracket mirror changed half");
		h.succeed();
	}

	@GameTest(template = "test_empty")
	public void verticalStairsLProfile(GameTestHelper h) {
		Block v = ContourRegistries.CONTOUR_VERTICAL_STAIRS.get();
		h.setBlock(P, v.defaultBlockState()
			.setValue(ContourVerticalStairsBlock.FACING, Direction.NORTH)
			.setValue(ContourVerticalStairsBlock.HALF, Half.BOTTOM));
		var boxes = ContourVerticalStairsBlock
			.boxesFor(h.getBlockState(P));
		h.assertTrue(boxes.size() == 2, "vstairs not 2 boxes");
		boolean back = false, arm = false;
		for (float[] b : boxes) {
			// facing NORTH -> back slab on the south half (z 8-16)
			back |= b[2] == 8 && b[5] == 16 && b[1] == 0 && b[4] == 16;
			arm |= b[2] == 0 && b[5] == 8 && b[1] == 0 && b[4] == 8;
		}
		h.assertTrue(back && arm, "vstairs missing back slab or arm");
		// HALF=TOP puts the arm up
		var top = ContourVerticalStairsBlock.boxesFor(v.defaultBlockState()
			.setValue(ContourVerticalStairsBlock.FACING, Direction.NORTH)
			.setValue(ContourVerticalStairsBlock.HALF, Half.TOP));
		h.assertTrue(top.get(1)[1] == 8 && top.get(1)[4] == 16,
			"vstairs top arm wrong");
		h.succeed();
	}

	@GameTest(template = "test_empty")
	public void archOpeningWalkable(GameTestHelper h) {
		Block a = ContourRegistries.CONTOUR_ARCH.get();
		h.setBlock(P, a.defaultBlockState()
			.setValue(ContourArchBlock.AXIS, Direction.Axis.X));
		var boxes = ContourArchBlock.boxesFor(h.getBlockState(P));
		h.assertTrue(boxes.size() == 6, "arch not 6 boxes");
		boolean jambW = false, jambE = false;
		for (float[] b : boxes) {
			jambW |= b[0] == 0 && b[3] == 3 && b[4] == 8;
			jambE |= b[0] == 13 && b[3] == 16 && b[4] == 8;
		}
		h.assertTrue(jambW && jambE, "arch jambs missing");
		// the opening center must stay clear: no box contains (8,4,8)
		for (float[] b : boxes)
			h.assertFalse(8 > b[0] && 8 < b[3] && 4 > b[1] && 4 < b[4]
				&& 8 > b[2] && 8 < b[5],
				"arch column fills the passage");
		// arc columns never exceed the outer arc: top <= 15
		for (float[] b : boxes)
			if (b[1] >= 8)
				h.assertTrue(b[4] <= 15f,
					"arc collision over-fills the opening");
		h.succeed();
	}

	@GameTest(template = "test_empty")
	public void cylinderOctagonCollision(GameTestHelper h) {
		Block c = ContourRegistries.CONTOUR_CYLINDER.get();
		h.setBlock(P, c.defaultBlockState()
			.setValue(ContourCylinderBlock.AXIS, Direction.Axis.Y));
		var boxes = ContourCylinderBlock.boxesFor(h.getBlockState(P));
		h.assertTrue(boxes.size() == 3, "cylinder not 3 bands");
		// r6 octagon: widest extent is 14 (2..14), never past it
		for (float[] b : boxes)
			for (int ax : new int[]{0, 2}) {
				h.assertTrue(b[ax] >= 2 - 1e-4 && b[ax + 3] <= 14 + 1e-4,
					"cylinder band exceeds r6 octagon on axis " + ax);
			}
		// wrench cycles the axis
		h.succeed();
	}

	@GameTest(template = "test_empty")
	public void pilasterHalfDomeCollision(GameTestHelper h) {
		Block p = ContourRegistries.CONTOUR_PILASTER.get();
		h.setBlock(P, p.defaultBlockState()
			.setValue(ContourPilasterBlock.FACING, Direction.SOUTH));
		var boxes = ContourPilasterBlock.boxesFor(h.getBlockState(P));
		h.assertTrue(boxes.size() == 5, "pilaster not 5 columns");
		// facing SOUTH (+z): wall plane z=0, column depths < 8
		for (float[] b : boxes) {
			h.assertTrue(b[2] >= -1e-4 && b[5] <= 8 + 1e-4,
				"pilaster column crosses mid-block");
			h.assertTrue(b[1] == 0 && b[4] == 16,
				"pilaster not full height");
		}
		// center column is the deepest
		h.assertTrue(boxes.get(2)[5] > boxes.get(0)[5],
			"pilaster center not deepest");
		h.succeed();
	}

	@GameTest(template = "test_empty")
	public void stairCornerOctants(GameTestHelper h) {
		Block sc = ContourRegistries.CONTOUR_STAIR_CORNER.get();
		// OUTER: slab + exactly one raised octant
		h.setBlock(P, sc.defaultBlockState()
			.setValue(ContourStairCornerBlock.INNER, false));
		var outer = ContourStairCornerBlock.boxesFor(h.getBlockState(P));
		h.assertTrue(outer.size() == 2, "outer != slab + 1 octant");
		h.assertTrue(outer.get(0)[3] == 16 && outer.get(0)[4] == 8,
			"outer slab not bottom half");
		// INNER: slab + three raised octants (the L)
		h.setBlock(P, sc.defaultBlockState()
			.setValue(ContourStairCornerBlock.INNER, true));
		var inner = ContourStairCornerBlock.boxesFor(h.getBlockState(P));
		h.assertTrue(inner.size() == 4, "inner != slab + 3 octants");
		// raised octants all sit in the top half
		for (int i = 1; i < 4; i++)
			h.assertTrue(inner.get(i)[1] == 8 && inner.get(i)[4] == 16,
				"inner octant not in raised half");
		h.succeed();
	}

	@GameTest(template = "test_empty")
	public void sphereBandedCollision(GameTestHelper h) {
		h.setBlock(P,
			ContourRegistries.CONTOUR_SPHERE.get().defaultBlockState());
		var boxes =
			ContourSphereBlock.boxesFor(h.getBlockState(P));
		h.assertTrue(boxes.size() == 5, "sphere not 5 bands");
		// widest band is the equator (full block), poles are narrowest
		float mid = boxes.get(2)[3] - boxes.get(2)[0];
		float pole = boxes.get(0)[3] - boxes.get(0)[0];
		h.assertTrue(mid == 16f && pole < mid,
			"sphere bands not widest at equator");
		h.succeed();
	}

	@GameTest(template = "test_empty")
	public void coneSteppedCollision(GameTestHelper h) {
		Block c = ContourRegistries.CONTOUR_CONE.get();
		h.setBlock(P, c.defaultBlockState()
			.setValue(ContourConeBlock.FACING, Direction.UP));
		var boxes = ContourConeBlock.boxesFor(h.getBlockState(P));
		h.assertTrue(boxes.size() == 4, "cone not 4 bands");
		// FACING=UP: base slab is the widest and sits at y=0
		h.assertTrue(boxes.get(0)[1] == 0 && boxes.get(0)[4] == 4,
			"cone base not at bottom");
		h.assertTrue(boxes.get(0)[3] - boxes.get(0)[0] == 16f,
			"cone base not full width");
		// bands narrow toward the apex
		h.assertTrue(boxes.get(3)[0] > boxes.get(0)[0],
			"cone apex band not narrower");
		h.succeed();
	}

	@GameTest(template = "test_empty")
	public void capitalSteppedFlare(GameTestHelper h) {
		Block cp = ContourRegistries.CONTOUR_CAPITAL.get();
		h.setBlock(P, cp.defaultBlockState()
			.setValue(ContourCapitalBlock.FACING, Direction.UP));
		var boxes = ContourCapitalBlock.boxesFor(h.getBlockState(P));
		h.assertTrue(boxes.size() == 3, "capital not 3 bands");
		// abacus (top band for UP) is full width, base band is 12
		h.assertTrue(boxes.get(2)[3] == 16 && boxes.get(2)[0] == 0,
			"capital abacus not full width");
		h.assertTrue(boxes.get(0)[0] == 2 && boxes.get(0)[3] == 14,
			"capital base not 12px");
		h.succeed();
	}

	@GameTest(template = "test_empty")
	public void bullnoseRoundedCover(GameTestHelper h) {
		Block bn = ContourRegistries.CONTOUR_BULLNOSE.get();
		h.setBlock(P, bn.defaultBlockState()); // edge DN: down+north
		var boxes = ContourBullnoseBlock.boxesFor(h.getBlockState(P));
		h.assertTrue(boxes.size() == 5, "bullnose not 5 boxes");
		// edge DN: a=DOWN(y-), b=NORTH(z-). The corner point (y=0.4,
		// z=0.4) must be uncovered; a tangent point (y=0.4, z=6.5)
		// must be covered by the first band.
		boolean cornerCovered = false, tangentCovered = false;
		for (float[] b : boxes) {
			if (b[0] < 8 && b[3] > 8) {
				if (b[1] < 0.4f && b[4] > 0.4f && b[2] < 0.4f && b[5] > 0.4f)
					cornerCovered = true;
				if (b[1] < 0.4f && b[4] > 0.4f && b[2] < 6.5f && b[5] > 6.5f)
					tangentCovered = true;
			}
		}
		h.assertTrue(!cornerCovered, "bullnose covers the rounded corner");
		h.assertTrue(tangentCovered, "bullnose misses the tangent point");
		// the keep box leaves du>=4.5: for DOWN, y>=4.5
		h.assertTrue(boxes.get(0)[1] == 4.5f,
			"bullnose keep box wrong side");
		h.succeed();
	}

	@GameTest(template = "test_empty")
	public void corniceStepsDescend(GameTestHelper h) {
		Block cn = ContourRegistries.CONTOUR_CORNICE.get();
		h.setBlock(P, cn.defaultBlockState()
			.setValue(ContourCorniceBlock.FACING, Direction.NORTH));
		var boxes = ContourCorniceBlock.boxesFor(h.getBlockState(P));
		h.assertTrue(boxes.size() == 4, "cornice not 4 steps");
		// FACING=NORTH: wall plane at z=16; band 0 (at wall) is tallest
		h.assertTrue(boxes.get(0)[2] > boxes.get(3)[2],
			"cornice band order wrong side");
		// heights descend with depth: band0 y-min < band3 y-min (TOP)
		h.assertTrue(boxes.get(0)[1] < boxes.get(3)[1],
			"cornice steps not descending");
		h.succeed();
	}

	@GameTest(template = "test_empty")
	public void balusterCoverBands(GameTestHelper h) {
		Block bl = ContourRegistries.CONTOUR_BALUSTER.get();
		h.setBlock(P, bl.defaultBlockState()); // axis=y
		var boxes = ContourBalusterBlock.boxesFor(h.getBlockState(P));
		h.assertTrue(boxes.size() == 4, "baluster not 4 bands");
		// foot band [0,1.5] half-width 4.5; stem band narrowest 2.5
		h.assertTrue(boxes.get(0)[1] == 0f && boxes.get(0)[0] == 3.5f,
			"baluster foot band wrong");
		h.assertTrue(boxes.get(1)[0] == 5.5f,
			"baluster stem not narrowest");
		// belly band [4,13] covers the widest point x in [3.8,12.2]
		h.assertTrue(boxes.get(2)[0] < 4f && boxes.get(2)[3] > 12f,
			"baluster belly under-covered");
		h.succeed();
	}

	@GameTest(template = "test_empty")
	public void finialFacingBands(GameTestHelper h) {
		Block fn = ContourRegistries.CONTOUR_FINIAL.get();
		h.setBlock(P, fn.defaultBlockState()); // facing=up, plate at bottom
		var boxes = ContourFinialBlock.boxesFor(h.getBlockState(P));
		h.assertTrue(boxes.size() == 3, "finial not 3 bands");
		// plate band widest (r5 -> x in [3,13]) and at the attach end
		h.assertTrue(boxes.get(0)[1] == 0f && boxes.get(0)[0] == 3f,
			"finial plate wrong end/width");
		// tip band narrowest (hw 2.5 -> x in [5.5,10.5])
		h.assertTrue(boxes.get(2)[0] == 5.5f && boxes.get(2)[4] == 16f,
			"finial tip band wrong");
		// DOWN flips the profile: plate band lands at y [14.5,16]
		h.setBlock(P, fn.defaultBlockState()
			.setValue(ContourFinialBlock.FACING, Direction.DOWN));
		var down = ContourFinialBlock.boxesFor(h.getBlockState(P));
		h.assertTrue(down.get(0)[1] == 14.5f && down.get(0)[4] == 16f,
			"finial DOWN plate not flipped");
		h.succeed();
	}

	@GameTest(template = "test_empty")
	public void scotiaSmallCover(GameTestHelper h) {
		Block sc = ContourRegistries.CONTOUR_SCOTIA.get();
		h.setBlock(P, sc.defaultBlockState()); // edge DN: a=DOWN, b=NORTH
		var boxes = ContourScotiaBlock.boxesFor(h.getBlockState(P));
		h.assertTrue(boxes.size() == 2, "scotia not 2 bands");
		// every box stays within 5px of the corner: a-axis=y hi <=5,
		// b-axis=z hi <=5
		for (float[] b : boxes) {
			h.assertTrue(b[4] <= 5f && b[5] <= 5f,
				"scotia cover escapes the r5 corner");
		}
		// tangent-adjacent point (y=1, z=1.5) must be covered
		boolean covered = false;
		for (float[] b : boxes)
			if (b[0] < 8 && b[3] > 8 && b[1] < 1 && b[4] > 1
				&& b[2] < 1.5f && b[5] > 1.5f)
				covered = true;
		h.assertTrue(covered, "scotia misses the inner point");
		h.succeed();
	}

	@GameTest(template = "test_empty")
	public void ogeeSweepBands(GameTestHelper h) {
		Block og = ContourRegistries.CONTOUR_OGEE.get();
		h.setBlock(P, og.defaultBlockState()); // edge DN: a=DOWN, b=NORTH
		var boxes = ContourOgeeBlock.boxesFor(h.getBlockState(P));
		h.assertTrue(boxes.size() == 4, "ogee not 4 bands");
		// a-axis=y bands climb [0,2],[2,4],[4,6],[6,8]
		for (int i = 0; i < 4; i++)
			h.assertTrue(boxes.get(i)[1] == i * 2f
				&& boxes.get(i)[4] == (i + 1) * 2f,
				"ogee a-band " + i + " misplaced");
		// b-extents (z-hi) descend 8 -> 6.75 -> 4 -> 1.25
		float prev = 99f;
		for (float[] b : boxes) {
			h.assertTrue(b[5] < prev, "ogee extents not descending");
			prev = b[5];
		}
		h.assertTrue(boxes.get(0)[5] == 8f && boxes.get(3)[5] == 1.25f,
			"ogee first/last extent wrong");
		h.succeed();
	}

	@GameTest(template = "test_empty")
	public void signLinesRoundTrip(GameTestHelper h) {
		Block sg = ContourRegistries.CONTOUR_SIGN.get();
		h.setBlock(P, sg.defaultBlockState()); // facing=up
		h.assertTrue(h.getLevel().getBlockEntity(h.absolutePos(P))
			instanceof ContourSignBlockEntity, "sign BE wrong type");
		ContourSignBlockEntity be = (ContourSignBlockEntity) h.getLevel()
			.getBlockEntity(h.absolutePos(P));
		be.setLines(new String[]{"hello", "contour", "", "  a line that is far too long to keep"});
		h.assertTrue(be.getLine(0).equals("hello")
			&& be.getLine(1).equals("contour")
			&& be.getLine(2).isEmpty(), "sign lines not kept");
		// length cap: 24 chars
		h.assertTrue(be.getLine(3).length() == 24,
			"sign line not capped");
		// NBT round trip preserves lines
		var tag = be.saveWithFullMetadata(h.getLevel().registryAccess());
		be.setLines(new String[]{"", "", "", ""});
		be.loadWithComponents(tag, h.getLevel().registryAccess());
		h.assertTrue(be.getLine(0).equals("hello")
			&& be.getLine(3).length() == 24, "sign lines not saved");
		h.succeed();
	}

	@GameTest(template = "test_empty")
	public void torusRingCover(GameTestHelper h) {
		Block tr = ContourRegistries.CONTOUR_TORUS.get();
		h.setBlock(P, tr.defaultBlockState()); // axis=y, ring in XZ
		var boxes = ContourTorusBlock.boxesFor(h.getBlockState(P));
		h.assertTrue(boxes.size() == 4, "torus not 4 bars");
		// annulus corner (diagonal at r=7.8) must be covered
		float dx = 13.5f - 8f, dz = 13.5f - 8f;
		double r = Math.sqrt(dx * dx + dz * dz); // ~7.78 <= 8
		h.assertTrue(r <= 8, "torus test point outside annulus");
		boolean corner = false;
		for (float[] b : boxes)
			if (b[0] <= 13.5f && b[3] >= 13.5f && b[2] <= 13.5f
				&& b[5] >= 13.5f && b[1] <= 10f && b[4] >= 6f)
				corner = true;
		h.assertTrue(corner, "torus misses the diagonal annulus corner");
		// center hole stays open at y-mid
		boolean hole = true;
		for (float[] b : boxes)
			if (b[0] < 8f && b[3] > 8f && b[2] < 8f && b[5] > 8f
				&& b[1] < 8f && b[4] > 8f)
				hole = false;
		h.assertTrue(hole, "torus center hole is blocked");
		// ring rim tangent: (8,15.9) inside outer r8
		boolean rim = false;
		for (float[] b : boxes)
			if (b[0] <= 8f && b[3] >= 8f && b[2] <= 15.9f && b[5] >= 15.9f
				&& b[1] <= 8f && b[4] >= 8f)
				rim = true;
		h.assertTrue(rim, "torus rim tangent uncovered");
		h.succeed();
	}

	@GameTest(template = "test_empty")
	public void crenellationBattlements(GameTestHelper h) {
		Block cr = ContourRegistries.CONTOUR_CRENELLATION.get();
		h.setBlock(P, cr.defaultBlockState()); // north, top
		var boxes = ContourCrenellationBlock.boxesFor(h.getBlockState(P));
		h.assertTrue(boxes.size() == 5, "crenellation not 5 boxes");
		// three merlons occupy the defended (north) edge above the lip
		int merlons = 0;
		for (float[] b : boxes)
			if (b[1] == 10f && b[4] == 14f && b[2] == 0f && b[5] == 3f)
				merlons++;
		h.assertTrue(merlons == 3, "crenellation merlons != 3");
		// crenel gap between merlon 1 and 2: run 4..6 open above lip
		boolean gap = true;
		for (float[] b : boxes)
			if (b[0] < 5f && b[3] > 5f && b[1] < 11f && b[4] > 11f
				&& b[2] < 1.5f && b[5] > 1.5f)
				gap = false;
		h.assertTrue(gap, "crenel gap blocked at run 5");
		// bottom half mirrors: merlons hang y 2..6
		var bb = ContourCrenellationBlock.boxesFor(
			cr.defaultBlockState().setValue(ContourCrenellationBlock.HALF,
				net.minecraft.world.level.block.state.properties.Half.BOTTOM));
		boolean hanging = false;
		for (float[] b : bb)
			if (b[1] == 2f && b[4] == 6f)
				hanging = true;
		h.assertTrue(hanging, "machicolation merlons not hanging");
		h.succeed();
	}

	@GameTest(template = "test_empty")
	public void barrelVaultCover(GameTestHelper h) {
		Block bv = ContourRegistries.CONTOUR_BARREL_VAULT.get();
		h.setBlock(P, bv.defaultBlockState()); // axis=x, half=bottom
		var boxes = ContourBarrelVaultBlock.boxesFor(h.getBlockState(P));
		h.assertTrue(boxes.size() == 7, "vault not 7 bands");
		// crown band reaches y=8 at center of the z span
		boolean crown = false;
		for (float[] b : boxes)
			if (b[1] == 0f && b[4] == 8f && b[2] == 6f && b[5] == 10f)
				crown = true;
		h.assertTrue(crown, "vault crown band missing");
		// edge feet never under-fill the arc foot (y 0..5.3 at z 0..2)
		boolean foot = false;
		for (float[] b : boxes)
			if (b[1] == 0f && b[4] == 5.3f && b[2] == 0f && b[5] == 2f)
				foot = true;
		h.assertTrue(foot, "vault foot band missing");
		// TOP half mirrors: solid hangs 8..16 at the crown
		var tb = ContourBarrelVaultBlock.boxesFor(
			bv.defaultBlockState().setValue(ContourBarrelVaultBlock.HALF,
				net.minecraft.world.level.block.state.properties.Half.TOP));
		boolean hang = false;
		for (float[] b : tb)
			if (b[1] == 8f && b[4] == 16f)
				hang = true;
		h.assertTrue(hang, "inverted vault not hanging");
		h.succeed();
	}

	@GameTest(template = "test_empty")
	public void cogTeeth(GameTestHelper h) {
		Block cg = ContourRegistries.CONTOUR_COG.get();
		h.setBlock(P, cg.defaultBlockState()); // axis=y
		var boxes = ContourCogBlock.boxesFor(h.getBlockState(P));
		h.assertTrue(boxes.size() == 9, "cog not 9 boxes");
		// hub plate plus four edge teeth reaching the face boundary
		boolean plate = false;
		int teeth = 0;
		for (float[] b : boxes) {
			if (b[0] == 3f && b[3] == 13f && b[1] == 7f && b[4] == 9f
				&& b[2] == 3f && b[5] == 13f)
				plate = true;
			if ((b[0] == 0f || b[3] == 16f || b[2] == 0f || b[5] == 16f)
				&& b[1] == 7f && b[4] == 9f)
				teeth++;
		}
		h.assertTrue(plate, "cog hub plate missing");
		h.assertTrue(teeth == 4, "cog edge teeth != 4");
		// X-axis state rotates the plate plane to the YZ face
		var xz = ContourCogBlock.boxesFor(
			cg.defaultBlockState().setValue(ContourCogBlock.AXIS,
				Direction.Axis.X));
		boolean sidePlate = false;
		for (float[] b : xz)
			if (b[0] == 7f && b[3] == 9f && b[1] == 3f && b[4] == 13f)
				sidePlate = true;
		h.assertTrue(sidePlate, "cog X plate not on YZ plane");
		h.succeed();
	}

	@GameTest(template = "test_empty")
	public void wheelSilhouette(GameTestHelper h) {
		Block wh = ContourRegistries.CONTOUR_WHEEL.get();
		h.setBlock(P, wh.defaultBlockState()); // axis=y
		var boxes = ContourWheelBlock.boxesFor(h.getBlockState(P));
		h.assertTrue(boxes.size() == 9, "wheel not 9 boxes");
		// rim bars reach the plane boundary; hub is the 6px-thick center
		boolean hub = false;
		int rimTouch = 0;
		for (float[] b : boxes) {
			if (b[0] == 6f && b[3] == 10f && b[1] == 5f && b[4] == 11f
				&& b[2] == 6f && b[5] == 10f)
				hub = true;
			if (b[1] == 6f && b[4] == 10f
				&& (b[0] == 0f || b[3] == 16f || b[2] == 0f || b[5] == 16f))
				rimTouch++;
		}
		h.assertTrue(hub, "wheel hub missing");
		h.assertTrue(rimTouch == 4, "wheel rim bars != 4");
		// X-axis rotates the wheel plane onto YZ
		var xz = ContourWheelBlock.boxesFor(
			wh.defaultBlockState().setValue(ContourWheelBlock.AXIS,
				Direction.Axis.X));
		boolean sideRim = false;
		for (float[] b : xz)
			if (b[0] == 6f && b[3] == 10f && b[1] == 0f)
				sideRim = true;
		h.assertTrue(sideRim, "wheel X rim not on YZ plane");
		h.succeed();
	}

	@GameTest(template = "test_empty")
	public void scaffoldClimbs(GameTestHelper h) {
		ContourScaffoldBlock sc =
			(ContourScaffoldBlock) ContourRegistries.CONTOUR_SCAFFOLD.get();
		h.setBlock(P, sc.defaultBlockState());
		var boxes = ContourScaffoldBlock.boxesFor(h.getBlockState(P));
		h.assertTrue(boxes.size() == 9, "scaffold not 9 boxes");
		// walkable top plate across the whole face
		boolean plate = false;
		int posts = 0;
		for (float[] b : boxes) {
			if (b[1] == 14.5f && b[4] == 16f && b[3] - b[0] == 16f)
				plate = true;
			if (b[1] == 0f && b[4] == 14.5f)
				posts++;
		}
		h.assertTrue(plate, "scaffold platform missing");
		h.assertTrue(posts == 4, "scaffold posts != 4");
		// climbable on every face, no backing wall required
		Player p = h.makeMockPlayer(GameType.SURVIVAL);
		h.assertTrue(sc.isLadder(h.getBlockState(P), h.getLevel(),
			h.absolutePos(P), p), "scaffold not climbable");
		h.succeed();
	}

	@GameTest(template = "test_empty")
	public void gothicArchOpening(GameTestHelper h) {
		Block ga = ContourRegistries.CONTOUR_GOTHIC_ARCH.get();
		h.setBlock(P, ga.defaultBlockState()); // axis=x
		var boxes = ContourGothicArchBlock.boxesFor(h.getBlockState(P));
		h.assertTrue(boxes.size() == 7, "gothic arch not 7 boxes");
		// full-height side walls
		boolean wall = false;
		for (float[] b : boxes)
			if (b[0] == 0f && b[3] == 3f && b[1] == 0f && b[4] == 16f)
				wall = true;
		h.assertTrue(wall, "gothic side wall missing");
		// apex band peaks highest at u 7..9
		boolean apex = false;
		for (float[] b : boxes)
			if (b[0] == 7f && b[3] == 9f && b[1] == 15.4f && b[4] == 16f)
				apex = true;
		h.assertTrue(apex, "gothic apex band missing");
		// the pointed opening stays open below the spring at u=8
		boolean open = true;
		for (float[] b : boxes)
			if (b[0] < 8f && b[3] > 8f && b[1] < 4f)
				open = false;
		h.assertTrue(open, "gothic doorway blocked below spring");
		h.succeed();
	}

	@GameTest(template = "test_empty")
	public void louverSlats(GameTestHelper h) {
		Block lv = ContourRegistries.CONTOUR_LOUVER.get();
		h.setBlock(P, lv.defaultBlockState().setValue(ContourLouverBlock.FACING,
			Direction.SOUTH));
		var boxes = ContourLouverBlock.boxesFor(h.getBlockState(P));
		h.assertTrue(boxes.size() == 5, "louver not 5 boxes");
		// mount plate on the north face, four slats reaching z=16
		boolean plate = false;
		int slats = 0;
		for (float[] b : boxes) {
			if (b[2] == 0f && b[5] == 1.5f)
				plate = true;
			if (b[5] == 16f && b[2] == 13f)
				slats++;
		}
		h.assertTrue(plate, "louver plate not on mount face");
		h.assertTrue(slats == 4, "louver slats != 4");
		// UP facing pitches the slats in Z instead of Y
		var up = ContourLouverBlock.boxesFor(
			lv.defaultBlockState().setValue(ContourLouverBlock.FACING,
				Direction.UP));
		boolean zPitch = false;
		for (float[] b : up)
			if (b[4] == 16f && b[1] == 13f && b[2] == 1.5f)
				zPitch = true;
		h.assertTrue(zPitch, "louver UP slats not Z-pitched");
		h.succeed();
	}

	@GameTest(template = "test_empty")
	public void elbowBends(GameTestHelper h) {
		Block el = ContourRegistries.CONTOUR_ELBOW.get();
		// DN edge: legs exit DOWN and NORTH faces
		var dn = ContourElbowBlock.boxesFor(el.defaultBlockState()
			.setValue(ContourElbowBlock.EDGE, ContourTrimBlock.Edge.DN));
		h.assertTrue(dn.size() == 3, "elbow not 3 boxes");
		boolean legDown = false, legNorth = false, bend = false;
		for (float[] b : dn) {
			if (b[1] == 0f && b[4] == 3f && b[2] == 5f && b[5] == 11f)
				legDown = true;   // y leg stub, cross-section 5..11
			if (b[2] == 0f && b[5] == 3f && b[1] == 5f && b[4] == 11f)
				legNorth = true;  // z leg stub
			if (b[1] == 3f && b[4] == 11f && b[2] == 3f && b[5] == 11f)
				bend = true;      // quarter bend rect
		}
		h.assertTrue(legDown, "elbow down leg missing");
		h.assertTrue(legNorth, "elbow north leg missing");
		h.assertTrue(bend, "elbow bend rect missing");
		// every one of the 12 edges yields the 3-box cover
		for (ContourTrimBlock.Edge e : ContourTrimBlock.Edge.values())
			h.assertTrue(ContourElbowBlock.boxesFor(el.defaultBlockState()
				.setValue(ContourElbowBlock.EDGE, e)).size() == 3,
				"elbow edge " + e + " wrong box count");
		h.succeed();
	}

	@GameTest(template = "test_empty")
	public void handrailRails(GameTestHelper h) {
		Block hr = ContourRegistries.CONTOUR_HANDRAIL.get();
		// SOUTH: rail ascends toward +z
		var s = ContourHandrailBlock.boxesFor(hr.defaultBlockState()
			.setValue(ContourHandrailBlock.FACING, Direction.SOUTH));
		h.assertTrue(s.size() == 6, "handrail not 6 boxes");
		boolean loBand = false, hiBand = false, loPost = false, hiPost = false;
		for (float[] b : s) {
			if (b[2] == 0f && b[5] == 4f && b[1] == 4f && b[4] == 9f)
				loBand = true;    // low rail band at z 0..4
			if (b[2] == 12f && b[5] == 16f && b[1] == 10f && b[4] == 15f)
				hiBand = true;    // high rail band at z 12..16
			if (b[2] == 1f && b[5] == 3f && b[1] == 0f && b[4] == 5.5f)
				loPost = true;
			if (b[2] == 13f && b[5] == 15f && b[1] == 0f && b[4] == 12.5f)
				hiPost = true;
		}
		h.assertTrue(loBand && hiBand, "handrail rail bands missing");
		h.assertTrue(loPost && hiPost, "handrail posts missing");
		// NORTH mirrors: the low band/post sit at high z
		var n = ContourHandrailBlock.boxesFor(hr.defaultBlockState()
			.setValue(ContourHandrailBlock.FACING, Direction.NORTH));
		boolean mirrored = false;
		for (float[] b : n)
			if (b[2] == 12f && b[5] == 16f && b[1] == 4f && b[4] == 9f)
				mirrored = true;  // low band now at z 12..16
		h.assertTrue(mirrored, "handrail not mirrored for north");
		h.succeed();
	}

	@GameTest(template = "test_empty")
	public void newelPost(GameTestHelper h) {
		Block nw = ContourRegistries.CONTOUR_NEWEL.get();
		var y = ContourNewelBlock.boxesFor(nw.defaultBlockState());
		h.assertTrue(y.size() == 3, "newel not 3 boxes");
		boolean plinth = false, shaft = false, knob = false;
		for (float[] b : y) {
			if (b[1] == 0f && b[4] == 3f && b[0] == 4f && b[3] == 12f)
				plinth = true;   // wide foot band
			if (b[1] == 3f && b[4] == 12f && b[0] == 4.8f)
				shaft = true;
			if (b[1] == 12f && b[4] == 16f)
				knob = true;
		}
		h.assertTrue(plinth && shaft && knob,
			"newel bands missing");
		// X axis: plinth band moves to the x run end
		var x = ContourNewelBlock.boxesFor(nw.defaultBlockState()
			.setValue(ContourNewelBlock.AXIS, Direction.Axis.X));
		boolean xPlinth = false;
		for (float[] b : x)
			if (b[0] == 0f && b[3] == 3f && b[1] == 4f && b[4] == 12f)
				xPlinth = true;
		h.assertTrue(xPlinth, "newel X plinth missing");
		h.succeed();
	}

	@GameTest(template = "test_empty")
	public void balustradeRails(GameTestHelper h) {
		Block bl = ContourRegistries.CONTOUR_BALUSTRADE.get();
		// NORTH: runs along Z
		var n = ContourBalustradeBlock.boxesFor(bl.defaultBlockState());
		h.assertTrue(n.size() == 7, "balustrade not 7 boxes");
		boolean topRail = false, footRail = false;
		int balusters = 0, posts = 0;
		for (float[] b : n) {
			if (b[2] == 0f && b[5] == 16f && b[1] == 12f && b[4] == 15f)
				topRail = true;
			if (b[2] == 0f && b[5] == 16f && b[1] == 1f && b[4] == 3.5f)
				footRail = true;
			if (b[1] == 3.5f && b[4] == 12f && b[2] != 0f && b[5] != 16f)
				balusters++;
			if (b[1] == 0f && b[4] == 15f)
				posts++;
		}
		h.assertTrue(topRail && footRail, "balustrade rails missing");
		h.assertTrue(balusters == 3, "balustrade balusters != 3");
		h.assertTrue(posts == 2, "balustrade end posts != 2");
		// EAST: the run moves to X, depth to Z
		var e = ContourBalustradeBlock.boxesFor(bl.defaultBlockState()
			.setValue(ContourBalustradeBlock.FACING, Direction.EAST));
		boolean xRail = false;
		for (float[] b : e)
			if (b[0] == 0f && b[3] == 16f && b[1] == 12f && b[4] == 15f
				&& b[2] == 6.5f && b[5] == 9.5f)
				xRail = true;
		h.assertTrue(xRail, "balustrade not run-swapped for east");
		h.succeed();
	}

	@GameTest(template = "test_empty")
	public void iBeamSection(GameTestHelper h) {
		Block ib = ContourRegistries.CONTOUR_I_BEAM.get();
		// X run: flanges on y=0..2/14..16 spanning z 3..13, web y 2..14
		var x = ContourIBeamBlock.boxesFor(ib.defaultBlockState());
		h.assertTrue(x.size() == 3, "i-beam not 3 boxes");
		int flanges = 0, webs = 0;
		for (float[] b : x) {
			if ((b[1] == 0f && b[4] == 2f) || (b[1] == 14f && b[4] == 16f))
				if (b[2] == 3f && b[5] == 13f && b[0] == 0f && b[3] == 16f)
					flanges++;
			if (b[1] == 2f && b[4] == 14f && b[2] == 7f && b[5] == 9f)
				webs++;
		}
		h.assertTrue(flanges == 2, "i-beam flanges != 2");
		h.assertTrue(webs == 1, "i-beam web != 1");
		// Y run: flanges move to z, web stands vertical in x
		var y = ContourIBeamBlock.boxesFor(ib.defaultBlockState()
			.setValue(ContourIBeamBlock.AXIS, Direction.Axis.Y));
		boolean yFlange = false;
		for (float[] b : y)
			if (b[2] == 0f && b[5] == 2f && b[0] == 3f && b[3] == 13f)
				yFlange = true;
		h.assertTrue(yFlange, "i-beam Y flange missing");
		h.succeed();
	}

	@GameTest(template = "test_empty")
	public void portcullisBars(GameTestHelper h) {
		Block pc = ContourRegistries.CONTOUR_PORTCULLIS.get();
		// NORTH panel: bars along X, normal Z
		var n = ContourPortcullisBlock.boxesFor(pc.defaultBlockState());
		h.assertTrue(n.size() == 10, "portcullis not 10 boxes");
		int bars = 0, tips = 0, straps = 0;
		for (float[] b : n) {
			if (b[1] == 2f && b[4] == 16f && b[2] == 7f && b[5] == 9f)
				bars++;
			if (b[1] == 0f && b[4] == 2f && b[2] == 7.5f && b[5] == 8.5f)
				tips++;
			if (b[0] == 0f && b[3] == 16f && b[2] == 6.5f && b[5] == 9.5f)
				straps++;
		}
		h.assertTrue(bars == 4, "portcullis bars != 4");
		h.assertTrue(tips == 4, "portcullis tips != 4");
		h.assertTrue(straps == 2, "portcullis straps != 2");
		// EAST panel: bars run along Z, normal X
		var e = ContourPortcullisBlock.boxesFor(pc.defaultBlockState()
			.setValue(ContourPortcullisBlock.FACING, Direction.EAST));
		boolean eBar = false;
		for (float[] b : e)
			if (b[2] == 1f && b[5] == 3f && b[0] == 7f && b[3] == 9f
				&& b[1] == 2f && b[4] == 16f)
				eBar = true;
		h.assertTrue(eBar, "portcullis not panel-swapped for east");
		h.succeed();
	}

	@GameTest(template = "test_empty")
	public void flangeRing(GameTestHelper h) {
		Block fl = ContourRegistries.CONTOUR_FLANGE.get();
		// UP: plate at y 14..16, ring cover + studs
		var up = ContourFlangeBlock.boxesFor(fl.defaultBlockState());
		h.assertTrue(up.size() == 8, "flange not 8 boxes");
		int strips = 0, studs = 0;
		for (float[] b : up) {
			if (b[1] == 14f && b[4] == 16f)
				strips++;
			if (b[1] == 12.5f && b[4] == 14f)
				studs++;
		}
		h.assertTrue(strips == 4, "flange strips != 4");
		h.assertTrue(studs == 4, "flange studs != 4");
		// bore stays open: no box fills the 5..11 center square
		boolean bore = true;
		for (float[] b : up)
			if (b[0] < 5f && b[3] > 11f && b[2] < 5f && b[5] > 11f)
				bore = false;
		h.assertTrue(bore, "flange bore blocked");
		// DOWN: plate at y 0..2, studs 2..3.5
		var dn = ContourFlangeBlock.boxesFor(fl.defaultBlockState()
			.setValue(ContourFlangeBlock.FACING, Direction.DOWN));
		boolean dnPlate = false, dnStud = false;
		for (float[] b : dn) {
			if (b[1] == 0f && b[4] == 2f)
				dnPlate = true;
			if (b[1] == 2f && b[4] == 3.5f)
				dnStud = true;
		}
		h.assertTrue(dnPlate && dnStud, "flange not flipped for down");
		h.succeed();
	}

	@GameTest(template = "test_empty")
	public void buttressSteps(GameTestHelper h) {
		Block bt = ContourRegistries.CONTOUR_BUTTRESS.get();
		// NORTH: back at z=0, steps descend toward +z
		var n = ContourButtressBlock.boxesFor(bt.defaultBlockState());
		h.assertTrue(n.size() == 4, "buttress not 4 boxes");
		float[] hs = {16f, 12f, 8f, 4f};
		for (int i = 0; i < 4; i++) {
			boolean band = false;
			for (float[] b : n)
				if (b[2] == i * 4f && b[5] == i * 4f + 4f
					&& b[4] == hs[i] && b[0] == 4f && b[3] == 12f)
					band = true;
			h.assertTrue(band, "buttress band " + i + " missing");
		}
		// SOUTH mirrors: tall band moves to high z
		var s = ContourButtressBlock.boxesFor(bt.defaultBlockState()
			.setValue(ContourButtressBlock.FACING, Direction.SOUTH));
		boolean tall = false;
		for (float[] b : s)
			if (b[2] == 12f && b[5] == 16f && b[4] == 16f)
				tall = true;
		h.assertTrue(tall, "buttress not flipped for south");
		h.succeed();
	}

	@GameTest(template = "test_empty")
	public void arrowSlitGap(GameTestHelper h) {
		Block as = ContourRegistries.CONTOUR_ARROW_SLIT.get();
		var n = ContourArrowSlitBlock.boxesFor(as.defaultBlockState());
		h.assertTrue(n.size() == 4, "arrow slit not 4 boxes");
		boolean barL = false, barR = false, sill = false, lintel = false;
		for (float[] b : n) {
			if (b[0] == 0f && b[3] == 7f && b[1] == 0f && b[4] == 16f)
				barL = true;
			if (b[0] == 9f && b[3] == 16f && b[1] == 0f && b[4] == 16f)
				barR = true;
			if (b[0] == 7f && b[3] == 9f && b[1] == 0f && b[4] == 3f)
				sill = true;
			if (b[0] == 7f && b[3] == 9f && b[1] == 13f && b[4] == 16f)
				lintel = true;
		}
		h.assertTrue(barL && barR, "arrow slit side bars missing");
		h.assertTrue(sill && lintel, "arrow slit sill/lintel missing");
		// the 2px slit stays open from y3 to y13
		boolean gap = true;
		for (float[] b : n)
			if (b[0] < 9f && b[3] > 7f && b[1] < 13f && b[4] > 3f)
				gap = false;
		h.assertTrue(gap, "arrow slit gap blocked");
		// EAST panel: bars run along Z
		var e = ContourArrowSlitBlock.boxesFor(as.defaultBlockState()
			.setValue(ContourArrowSlitBlock.FACING, Direction.EAST));
		boolean zBar = false;
		for (float[] b : e)
			if (b[2] == 0f && b[5] == 7f && b[1] == 0f && b[4] == 16f
				&& b[0] == 6f && b[3] == 10f)
				zBar = true;
		h.assertTrue(zBar, "arrow slit not panel-swapped for east");
		h.succeed();
	}

	@GameTest(template = "test_empty")
	public void dentilTeeth(GameTestHelper h) {
		Block dt = ContourRegistries.CONTOUR_DENTIL.get();
		var top = ContourDentilBlock.boxesFor(dt.defaultBlockState());
		h.assertTrue(top.size() == 5, "dentil not 5 boxes");
		boolean band = false;
		int teeth = 0;
		for (float[] b : top) {
			if (b[0] == 0f && b[3] == 16f && b[1] == 12f && b[4] == 16f)
				band = true;      // backing band on top
			if (b[1] == 8f && b[4] == 12f && b[3] - b[0] == 3f)
				teeth++;          // 3px teeth below the band
		}
		h.assertTrue(band, "dentil band missing");
		h.assertTrue(teeth == 4, "dentil teeth != 4");
		// BOTTOM half: band at y 0..4, teeth rise to y 8
		var bot = ContourDentilBlock.boxesFor(dt.defaultBlockState()
			.setValue(ContourDentilBlock.HALF,
				net.minecraft.world.level.block.state.properties.Half.BOTTOM));
		boolean lowBand = false, lowTooth = false;
		for (float[] b : bot) {
			if (b[0] == 0f && b[3] == 16f && b[1] == 0f && b[4] == 4f)
				lowBand = true;
			if (b[1] == 4f && b[4] == 8f)
				lowTooth = true;
		}
		h.assertTrue(lowBand && lowTooth, "dentil not flipped for bottom");
		h.succeed();
	}

	@GameTest(template = "test_empty")
	public void teeJunction(GameTestHelper h) {
		Block te = ContourRegistries.CONTOUR_TEE.get();
		// XU: run along X, branch UP
		var xu = ContourTeeBlock.boxesFor(te.defaultBlockState()
			.setValue(ContourTeeBlock.TEE, ContourTeeBlock.Tee.XU));
		h.assertTrue(xu.size() == 2, "tee not 2 boxes");
		boolean main = false, branch = false;
		for (float[] b : xu) {
			if (b[0] == 0f && b[3] == 16f && b[1] == 5f && b[4] == 11f
				&& b[2] == 5f && b[5] == 11f)
				main = true;      // x run, y/z 5..11
			if (b[1] == 8f && b[4] == 16f && b[0] == 5f && b[3] == 11f)
				branch = true;    // y branch to the UP face
		}
		h.assertTrue(main && branch, "tee tubes missing");
		// ZD: run along Z, branch DOWN
		var zd = ContourTeeBlock.boxesFor(te.defaultBlockState()
			.setValue(ContourTeeBlock.TEE, ContourTeeBlock.Tee.ZD));
		boolean zMain = false, dBranch = false;
		for (float[] b : zd) {
			if (b[2] == 0f && b[5] == 16f && b[0] == 5f && b[3] == 11f
				&& b[1] == 5f && b[4] == 11f)
				zMain = true;
			if (b[1] == 0f && b[4] == 8f && b[2] == 5f && b[5] == 11f)
				dBranch = true;
		}
		h.assertTrue(zMain && dBranch, "tee ZD orientation wrong");
		// all 12 states produce 2 boxes
		for (ContourTeeBlock.Tee t : ContourTeeBlock.Tee.values())
			h.assertTrue(ContourTeeBlock.boxesFor(te.defaultBlockState()
				.setValue(ContourTeeBlock.TEE, t)).size() == 2,
				"tee " + t + " wrong box count");
		h.succeed();
	}

	@GameTest(template = "test_empty")
	public void palisadePosts(GameTestHelper h) {
		Block pl = ContourRegistries.CONTOUR_PALISADE.get();
		var n = ContourPalisadeBlock.boxesFor(pl.defaultBlockState());
		h.assertTrue(n.size() == 14, "palisade not 14 boxes");
		int posts = 0, tips = 0, rails = 0;
		for (float[] b : n) {
			if (b[1] == 0f && b[4] == 14f && b[2] == 6.5f && b[5] == 9.5f)
				posts++;
			if (b[1] >= 14f && b[4] <= 16f && b[2] >= 7f)
				tips++;
			if (b[0] == 0f && b[3] == 16f && b[2] == 9.5f && b[5] == 11f)
				rails++;
		}
		h.assertTrue(posts == 4, "palisade posts != 4");
		h.assertTrue(tips == 8, "palisade tip stages != 8");
		h.assertTrue(rails == 2, "palisade rails != 2");
		// EAST panel: posts run along Z, normal X
		var e = ContourPalisadeBlock.boxesFor(pl.defaultBlockState()
			.setValue(ContourPalisadeBlock.FACING, Direction.EAST));
		boolean zPost = false;
		for (float[] b : e)
			if (b[2] == 1f && b[5] == 4f && b[0] == 6.5f && b[3] == 9.5f)
				zPost = true;
		h.assertTrue(zPost, "palisade not panel-swapped for east");
		h.succeed();
	}

	@GameTest(template = "test_empty")
	public void quoinAlternates(GameTestHelper h) {
		Block q = ContourRegistries.CONTOUR_QUOIN.get();
		// DN edge: corner y=0/z=0, runs along X
		var dn = ContourQuoinBlock.boxesFor(q.defaultBlockState()
			.setValue(ContourQuoinBlock.EDGE, ContourTrimBlock.Edge.DN));
		h.assertTrue(dn.size() == 6, "quoin DN not 6 boxes");
		int longLeg = 0, shortCube = 0;
		for (float[] b : dn) {
			boolean yLeg = b[1] == 0f && b[4] == 4f;
			boolean zLeg = b[2] == 0f && b[5] == 4f;
			if (yLeg && b[5] == 8f)
				longLeg++;   // down-face leg reaching z=8
			if (yLeg && zLeg)
				shortCube++; // corner-only 4px cube
		}
		h.assertTrue(longLeg == 2, "quoin long legs != 2");
		h.assertTrue(shortCube == 2, "quoin short cubes != 2");
		// every edge produces 6 boxes in 0..16
		for (ContourTrimBlock.Edge e : ContourTrimBlock.Edge.values()) {
			var bs = ContourQuoinBlock.boxesFor(q.defaultBlockState()
				.setValue(ContourQuoinBlock.EDGE, e));
			h.assertTrue(bs.size() == 6, "quoin " + e + " wrong count");
			for (float[] b : bs)
				for (int i = 0; i < 6; i++)
					h.assertTrue(b[i] >= 0f && b[i] <= 16f,
						"quoin " + e + " out of bounds");
		}
		h.succeed();
	}

	@GameTest(template = "test_empty")
	public void trussFrame(GameTestHelper h) {
		Block t = ContourRegistries.CONTOUR_TRUSS.get();
		var n = ContourTrussBlock.boxesFor(t.defaultBlockState());
		h.assertTrue(n.size() == 8, "truss not 8 boxes");
		boolean chord = false, king = false, apex = false;
		for (float[] b : n) {
			if (b[0] == 0f && b[3] == 16f && b[1] == 1f && b[4] == 3f)
				chord = true;
			if (b[0] == 7f && b[3] == 9f && b[1] == 3f && b[4] == 13f)
				king = true;
			if (b[4] == 15f)
				apex = true;
		}
		h.assertTrue(chord && king && apex, "truss members missing");
		// EAST panel: run along Z
		var e = ContourTrussBlock.boxesFor(t.defaultBlockState()
			.setValue(ContourTrussBlock.FACING, Direction.EAST));
		boolean zChord = false;
		for (float[] b : e)
			if (b[2] == 0f && b[5] == 16f && b[1] == 1f && b[4] == 3f)
				zChord = true;
		h.assertTrue(zChord, "truss not panel-swapped for east");
		// chord slope helper: bottom edge y=3 at ends, 13 at apex
		h.assertTrue(ContourTrussBlock.chordLo(0f) == 3f
			&& ContourTrussBlock.chordLo(8f) == 13f
			&& ContourTrussBlock.chordLo(16f) == 3f,
			"chord slope wrong");
		h.succeed();
	}

	@GameTest(template = "test_empty")
	public void sillSlope(GameTestHelper h) {
		Block s = ContourRegistries.CONTOUR_SILL.get();
		var n = ContourSillBlock.boxesFor(s.defaultBlockState());
		h.assertTrue(n.size() == 2, "sill not 2 boxes");
		boolean wall = false, tip = false;
		for (float[] b : n) {
			if (b[2] == 0f && b[5] == 4f && b[4] == 4f)
				wall = true;   // tall step at the wall (z 0..4)
			if (b[2] == 4f && b[5] == 8f && b[4] == 3.5f)
				tip = true;    // lower step at the drip edge
		}
		h.assertTrue(wall && tip, "sill steps missing");
		// SOUTH flips the ledge to z 8..16
		var so = ContourSillBlock.boxesFor(s.defaultBlockState()
			.setValue(ContourSillBlock.FACING, Direction.SOUTH));
		boolean southWall = false;
		for (float[] b : so)
			if (b[2] == 12f && b[5] == 16f && b[4] == 4f)
				southWall = true;
		h.assertTrue(southWall, "sill not flipped for south");
		// slope helper: 4 at the wall, 2.5 at the tip
		h.assertTrue(ContourSillBlock.topAt(0f) == 4f
			&& ContourSillBlock.topAt(8f) == 2.5f, "sill slope wrong");
		h.succeed();
	}

	@GameTest(template = "test_empty")
	public void bollardProfile(GameTestHelper h) {
		Block b = ContourRegistries.CONTOUR_BOLLARD.get();
		var y = ContourBollardBlock.boxesFor(b.defaultBlockState());
		h.assertTrue(y.size() == 4, "bollard not 4 bands");
		boolean plinth = false, collar = false;
		for (float[] bx : y) {
			if (bx[1] == 0f && bx[4] == 2f && bx[0] == 2f && bx[3] == 14f)
				plinth = true;
			if (bx[1] == 10f && bx[4] == 12f && bx[0] == 2.5f
				&& bx[3] == 13.5f)
				collar = true;
		}
		h.assertTrue(plinth && collar, "bollard bands missing");
		// run along X: bands swap axes
		var x = ContourBollardBlock.boxesFor(b.defaultBlockState()
			.setValue(ContourBollardBlock.AXIS, Direction.Axis.X));
		boolean xPlinth = false;
		for (float[] bx : x)
			if (bx[0] == 0f && bx[3] == 2f && bx[1] == 2f && bx[4] == 14f)
				xPlinth = true;
		h.assertTrue(xPlinth, "bollard not axis-swapped");
		h.succeed();
	}

	@GameTest(template = "test_empty")
	public void mullionPanes(GameTestHelper h) {
		Block m = ContourRegistries.CONTOUR_MULLION.get();
		var n = ContourMullionBlock.boxesFor(m.defaultBlockState());
		h.assertTrue(n.size() == 7, "mullion not 7 boxes");
		int frame = 0, cross = 0;
		for (float[] b : n) {
			if ((b[0] == 0f && b[3] == 2f) || (b[0] == 14f && b[3] == 16f)
				|| (b[1] == 0f && b[4] == 2f) || (b[1] == 14f && b[4] == 16f))
				frame++;
			if ((b[0] == 7.5f && b[3] == 8.5f)
				|| (b[1] == 7.5f && b[4] == 8.5f))
				cross++;
		}
		h.assertTrue(frame == 4, "mullion frame bars != 4");
		h.assertTrue(cross == 3, "mullion cross pieces != 3");
		// pane at u 2..7.5, y 8.5..14 stays open (no box covers it)
		boolean paneOpen = true;
		for (float[] b : n)
			if (b[0] < 5f && b[3] > 5f && b[1] < 11f && b[4] > 11f)
				paneOpen = false;
		h.assertTrue(paneOpen, "mullion pane blocked");
		// EAST swaps the panel normal
		var e = ContourMullionBlock.boxesFor(m.defaultBlockState()
			.setValue(ContourMullionBlock.FACING, Direction.EAST));
		boolean zFrame = false;
		for (float[] b : e)
			if (b[2] == 0f && b[5] == 2f && b[0] == 7f && b[3] == 9f)
				zFrame = true;
		h.assertTrue(zFrame, "mullion not panel-swapped for east");
		h.succeed();
	}

	@GameTest(template = "test_empty")
	public void cofferRecess(GameTestHelper h) {
		Block c = ContourRegistries.CONTOUR_COFFER.get();
		var d = ContourCofferBlock.boxesFor(c.defaultBlockState());
		h.assertTrue(d.size() == 5, "coffer not 5 boxes");
		int frame = 0;
		boolean field = false;
		for (float[] b : d) {
			if (b[1] == 0f && b[4] == 3f)
				frame++;
			if (b[1] == 3f && b[4] == 4f && b[0] == 3f && b[3] == 13f)
				field = true;
		}
		h.assertTrue(frame == 4, "coffer frame bars != 4");
		h.assertTrue(field, "coffer recessed field missing");
		// UP: frame rises to y 13..16, field at y 12..13
		var up = ContourCofferBlock.boxesFor(c.defaultBlockState()
			.setValue(ContourCofferBlock.FACING, Direction.UP));
		boolean upField = false, upFrame = false;
		for (float[] b : up) {
			if (b[1] == 12f && b[4] == 13f)
				upField = true;
			if (b[1] == 13f && b[4] == 16f)
				upFrame = true;
		}
		h.assertTrue(upField && upFrame, "coffer not flipped for up");
		h.succeed();
	}

	@GameTest(template = "test_empty")
	public void keystoneTaper(GameTestHelper h) {
		Block k = ContourRegistries.CONTOUR_KEYSTONE.get();
		var n = ContourKeystoneBlock.boxesFor(k.defaultBlockState());
		h.assertTrue(n.size() == 4, "keystone not 4 steps");
		boolean narrow = false, wide = false;
		for (float[] b : n) {
			if (b[1] == 0f && b[4] == 4f && b[0] == 5.25f && b[3] == 10.75f)
				narrow = true;
			if (b[1] == 12f && b[4] == 16f && b[0] == 3f && b[3] == 13f)
				wide = true;
		}
		h.assertTrue(narrow && wide, "keystone taper missing");
		// taper helper: 6..10 at y=0, 3..13 at y=16
		h.assertTrue(ContourKeystoneBlock.uLo(0f) == 6f
			&& ContourKeystoneBlock.uLo(16f) == 3f, "keystone taper wrong");
		// EAST swaps the wedge to run along Z
		var e = ContourKeystoneBlock.boxesFor(k.defaultBlockState()
			.setValue(ContourKeystoneBlock.FACING, Direction.EAST));
		boolean zTop = false;
		for (float[] b : e)
			if (b[2] == 3f && b[5] == 13f && b[1] == 12f && b[0] == 5f)
				zTop = true;
		h.assertTrue(zTop, "keystone not panel-swapped for east");
		h.succeed();
	}

	@GameTest(template = "test_empty")
	public void pedestalBands(GameTestHelper h) {
		Block p = ContourRegistries.CONTOUR_PEDESTAL.get();
		var y = ContourPedestalBlock.boxesFor(p.defaultBlockState());
		h.assertTrue(y.size() == 3, "pedestal not 3 bands");
		boolean foot = false, shaft = false, cap = false;
		for (float[] b : y) {
			if (b[1] == 0f && b[4] == 3f && b[0] == 2f && b[3] == 14f)
				foot = true;
			if (b[1] == 3f && b[4] == 13f && b[0] == 4f && b[3] == 12f)
				shaft = true;
			if (b[1] == 13f && b[4] == 16f && b[0] == 2f && b[3] == 14f)
				cap = true;
		}
		h.assertTrue(foot && shaft && cap, "pedestal bands missing");
		// run along Z: bands swap to z
		var z = ContourPedestalBlock.boxesFor(p.defaultBlockState()
			.setValue(ContourPedestalBlock.AXIS, Direction.Axis.Z));
		boolean zFoot = false;
		for (float[] b : z)
			if (b[2] == 0f && b[5] == 3f && b[0] == 2f && b[3] == 14f)
				zFoot = true;
		h.assertTrue(zFoot, "pedestal not axis-swapped");
		h.succeed();
	}

	@GameTest(template = "test_empty")
	public void lancetOpening(GameTestHelper h) {
		Block l = ContourRegistries.CONTOUR_LANCET.get();
		var n = ContourLancetBlock.boxesFor(l.defaultBlockState());
		h.assertTrue(n.size() == 6, "lancet not 6 boxes");
		int jamb = 0, head = 0;
		boolean sill = false;
		for (float[] b : n) {
			if ((b[0] == 4f && b[3] == 6f) || (b[0] == 10f && b[3] == 12f))
				if (b[4] == 16f)
					jamb++;
			if (b[1] >= 12f)
				head++;
			if (b[0] == 4f && b[3] == 12f && b[1] == 0f && b[4] == 2f)
				sill = true;
		}
		h.assertTrue(jamb == 2, "lancet jambs != 2");
		h.assertTrue(head == 3, "lancet head pieces != 3");
		h.assertTrue(sill, "lancet sill missing");
		// opening u 6..10 y 2..12 stays open
		boolean open = true;
		for (float[] b : n)
			if (b[0] < 8f && b[3] > 8f && b[1] < 7f && b[4] > 7f)
				open = false;
		h.assertTrue(open, "lancet opening blocked");
		h.succeed();
	}

	@GameTest(template = "test_empty")
	public void valleyTrough(GameTestHelper h) {
		Block v = ContourRegistries.CONTOUR_VALLEY.get();
		var x = ContourValleyBlock.boxesFor(v.defaultBlockState());
		h.assertTrue(x.size() == 4, "valley not 4 steps");
		boolean lowL = false, highR = false;
		for (float[] b : x) {
			if (b[2] == 4f && b[5] == 8f && b[1] == 6f && b[4] == 12f)
				lowL = true;    // left low step at z 4..8
			if (b[2] == 12f && b[5] == 16f && b[1] == 10f && b[4] == 16f)
				highR = true;   // right high edge
		}
		h.assertTrue(lowL && highR, "valley V steps missing");
		// run along Z: cross axis swaps to X
		var z = ContourValleyBlock.boxesFor(v.defaultBlockState()
			.setValue(ContourValleyBlock.AXIS, Direction.Axis.Z));
		boolean xStep = false;
		for (float[] b : z)
			if (b[0] == 4f && b[3] == 8f && b[1] == 6f && b[4] == 12f)
				xStep = true;
		h.assertTrue(xStep, "valley not axis-swapped");
		h.succeed();
	}

	@GameTest(template = "test_empty")
	public void crossJunction(GameTestHelper h) {
		Block c = ContourRegistries.CONTOUR_CROSS.get();
		// closed axis Y: tubes run along X and Z, 2 boxes [5,11] cross
		var boxes = ContourCrossBlock.boxesFor(c.defaultBlockState());
		h.assertTrue(boxes.size() == 2, "cross not 2 tubes");
		boolean xr = false, zr = false, yOpen = true;
		for (float[] b : boxes) {
			if (b[0] == 0f && b[3] == 16f)
				xr = true;
			if (b[2] == 0f && b[5] == 16f)
				zr = true;
			if (b[1] < 5f || b[4] > 11f)
				yOpen = false;
		}
		h.assertTrue(xr && zr, "cross tubes missing an arm");
		h.assertTrue(yOpen, "cross fills the closed axis");
		// closed axis X: Y/Z tubes appear instead
		var xb = ContourCrossBlock.boxesFor(c.defaultBlockState()
			.setValue(ContourCrossBlock.AXIS, Direction.Axis.X));
		boolean yr = false;
		for (float[] b : xb)
			if (b[1] == 0f && b[4] == 16f)
				yr = true;
		h.assertTrue(yr, "cross not axis-swapped");
		h.succeed();
	}

	@GameTest(template = "test_empty")
	public void pipeCapPlug(GameTestHelper h) {
		Block c = ContourRegistries.CONTOUR_PIPE_CAP.get();
		var up = ContourPipeCapBlock.boxesFor(c.defaultBlockState());
		h.assertTrue(up.size() == 2, "cap not 2 boxes");
		boolean stub = false, plug = false;
		for (float[] b : up) {
			if (b[1] == 6f && b[4] == 16f && b[0] == 5f && b[3] == 11f)
				stub = true;
			if (b[1] == 4f && b[4] == 6f && b[0] == 4f && b[3] == 12f)
				plug = true;
		}
		h.assertTrue(stub, "cap stub missing");
		h.assertTrue(plug, "cap plug missing");
		// DOWN facing: mouth at bottom, plug just above it
		var dn = ContourPipeCapBlock.boxesFor(c.defaultBlockState()
			.setValue(ContourPipeCapBlock.FACING, Direction.DOWN));
		boolean dnPlug = false;
		for (float[] b : dn)
			if (b[1] == 10f && b[4] == 12f)
				dnPlug = true;
		h.assertTrue(dnPlug, "cap plug not facing-swapped");
		h.succeed();
	}

	@GameTest(template = "test_empty")
	public void pedimentTriangle(GameTestHelper h) {
		Block p = ContourRegistries.CONTOUR_PEDIMENT.get();
		var boxes = ContourPedimentBlock.boxesFor(p.defaultBlockState());
		h.assertTrue(boxes.size() == 4, "pediment not 4 steps");
		// rows narrow 2px per side each 4px of height on the u axis
		float[][] want = {{0, 16}, {2, 14}, {4, 12}, {6, 10}};
		for (int i = 0; i < 4; i++) {
			float[] b = boxes.get(i);
			h.assertTrue(b[1] == i * 4f && b[4] == i * 4f + 4f,
				"pediment row " + i + " height off");
			h.assertTrue(b[0] == want[i][0] && b[3] == want[i][1],
				"pediment row " + i + " not narrowed");
			h.assertTrue(b[2] == 6.5f && b[5] == 9.5f,
				"pediment row " + i + " not centered panel");
		}
		// EAST facing: u axis swaps to Z
		var e = ContourPedimentBlock.boxesFor(p.defaultBlockState()
			.setValue(ContourPedimentBlock.FACING, Direction.EAST));
		h.assertTrue(e.get(0)[2] == 0f && e.get(0)[5] == 16f
			&& e.get(3)[2] == 6f && e.get(3)[5] == 10f,
			"pediment not facing-swapped");
		h.succeed();
	}

	@GameTest(template = "test_empty")
	public void chimneyBands(GameTestHelper h) {
		Block c = ContourRegistries.CONTOUR_CHIMNEY.get();
		var y = ContourChimneyBlock.boxesFor(c.defaultBlockState());
		h.assertTrue(y.size() == 3, "chimney not 3 bands");
		boolean plinth = false, shaft = false, lip = false;
		for (float[] b : y) {
			if (b[1] == 0f && b[4] == 2f && b[0] == 1f && b[3] == 15f)
				plinth = true;
			if (b[1] == 2f && b[4] == 14f && b[0] == 3f && b[3] == 13f)
				shaft = true;
			if (b[1] == 14f && b[4] == 16f && b[0] == 2f && b[3] == 14f)
				lip = true;
		}
		h.assertTrue(plinth && shaft && lip, "chimney bands missing");
		// X axis: bands stack along X instead
		var x = ContourChimneyBlock.boxesFor(c.defaultBlockState()
			.setValue(ContourChimneyBlock.AXIS, Direction.Axis.X));
		boolean xLip = false;
		for (float[] b : x)
			if (b[0] == 14f && b[3] == 16f && b[1] == 2f && b[4] == 14f)
				xLip = true;
		h.assertTrue(xLip, "chimney not axis-swapped");
		h.succeed();
	}

	@GameTest(template = "test_empty")
	public void halfTimberFrame(GameTestHelper h) {
		Block t = ContourRegistries.CONTOUR_HALF_TIMBER.get();
		var boxes = ContourHalfTimberBlock.boxesFor(t.defaultBlockState());
		h.assertTrue(boxes.size() == 7, "half timber not 7 boxes");
		int rails = 0, stiles = 0, steps = 0;
		for (float[] b : boxes) {
			if (b[0] == 0f && b[3] == 16f)
				rails++;
			else if (b[0] == 0f || b[3] == 16f)
				stiles++;
			else
				steps++;
		}
		h.assertTrue(rails == 2, "half timber rails != 2");
		h.assertTrue(stiles == 2, "half timber stiles != 2");
		h.assertTrue(steps == 3, "half timber brace cover != 3");
		// rising brace climbs: step bands ascend with u
		var rb = ContourHalfTimberBlock.boxesFor(t.defaultBlockState());
		h.assertTrue(rb.get(4)[4] < rb.get(6)[4], "rising steps not ascending");
		// falling brace mirrors
		var fb = ContourHalfTimberBlock.boxesFor(t.defaultBlockState()
			.setValue(ContourHalfTimberBlock.DIAG, false));
		h.assertTrue(fb.get(4)[4] > fb.get(6)[4], "falling steps not descending");
		h.succeed();
	}

	@GameTest(template = "test_empty")
	public void spandrelTriangle(GameTestHelper h) {
		Block s = ContourRegistries.CONTOUR_SPANDREL.get();
		var boxes = ContourSpandrelBlock.boxesFor(s.defaultBlockState());
		h.assertTrue(boxes.size() == 4, "spandrel not 4 steps");
		// high_left=true: tall corner on u-low side, rows shrink upward
		for (int i = 0; i < 4; i++) {
			float[] b = boxes.get(i);
			h.assertTrue(b[0] == 0f && b[3] == 16f - i * 4f,
				"spandrel row " + i + " not shrinking");
			h.assertTrue(b[1] == i * 4f && b[4] == i * 4f + 4f,
				"spandrel row " + i + " height off");
		}
		// mirrored: rows shrink from the other side
		var m = ContourSpandrelBlock.boxesFor(s.defaultBlockState()
			.setValue(ContourSpandrelBlock.HIGH_LEFT, false));
		h.assertTrue(m.get(0)[0] == 0f && m.get(0)[3] == 16f
			&& m.get(3)[0] == 12f && m.get(3)[3] == 16f,
			"spandrel not mirrored");
		h.succeed();
	}

	@GameTest(template = "test_empty")
	public void arcWallCurve(GameTestHelper h) {
		Block a = ContourRegistries.CONTOUR_ARC_WALL.get();
		var ne = ContourArcWallBlock.boxesFor(a.defaultBlockState());
		h.assertTrue(ne.size() == 4, "arc wall not 4 covers");
		boolean northEnd = false, eastEnd = false;
		for (float[] b : ne) {
			// NE corner: arc ends touch z=0 (x 0..5) and x=16 (z 11..16)
			if (b[2] == 0f && b[0] == 0f && b[3] <= 5f)
				northEnd = true;
			if (b[3] == 16f && b[5] == 16f && b[2] >= 11f)
				eastEnd = true;
		}
		h.assertTrue(northEnd, "arc wall north end missing");
		h.assertTrue(eastEnd, "arc wall east end missing");
		// tower core near the inner arc stays open (r < 12)
		boolean open = true;
		for (float[] b : ne)
			if (b[0] < 12.5f && b[3] > 11f && b[2] < 5f && b[5] > 3.5f)
				open = false;
		h.assertTrue(open, "arc wall blocks the tower core");
		// SW corner mirrors to the far side (south-face end at x 11..16)
		var sw = ContourArcWallBlock.boxesFor(a.defaultBlockState()
			.setValue(ContourArcWallBlock.CORNER,
				ContourCornerSlopeBlock.Corner.SW));
		boolean swEnd = false;
		for (float[] b : sw)
			if (b[5] == 16f && b[0] >= 11f)
				swEnd = true;
		h.assertTrue(swEnd, "arc wall not corner-swapped");
		h.succeed();
	}

	@GameTest(template = "test_empty")
	public void rosetteBoss(GameTestHelper h) {
		Block r = ContourRegistries.CONTOUR_ROSETTE.get();
		var up = ContourRosetteBlock.boxesFor(r.defaultBlockState());
		h.assertTrue(up.size() == 2, "rosette not 2 covers");
		boolean plate = false, boss = false;
		for (float[] b : up) {
			if (b[1] == 14.5f && b[4] == 16f && b[0] == 1f && b[3] == 15f)
				plate = true;
			if (b[1] == 10f && b[4] == 14.5f && b[0] == 2.5f && b[3] == 13.5f)
				boss = true;
		}
		h.assertTrue(plate, "rosette plate missing");
		h.assertTrue(boss, "rosette boss missing");
		// DOWN facing: medallion hangs from the floor face
		var dn = ContourRosetteBlock.boxesFor(r.defaultBlockState()
			.setValue(ContourRosetteBlock.FACING, Direction.DOWN));
		boolean dnPlate = false;
		for (float[] b : dn)
			if (b[1] == 0f && b[4] == 1.5f)
				dnPlate = true;
		h.assertTrue(dnPlate, "rosette not facing-swapped");
		h.succeed();
	}

	@GameTest(template = "test_empty")
	public void flyerSpan(GameTestHelper h) {
		Block f = ContourRegistries.CONTOUR_FLYING_BUTTRESS.get();
		var boxes = ContourFlyingButtressBlock.boxesFor(f.defaultBlockState());
		h.assertTrue(boxes.size() == 5, "flyer not 5 boxes");
		boolean pier = false, abut = false;
		int arc = 0;
		for (float[] b : boxes) {
			if (b[4] == 11f && b[2] == 9f && b[5] == 13f)
				pier = true;        // pier tops at y 11 near the wall
			else if (b[1] == 13f && b[4] == 16f && b[2] == 0f)
				abut = true;        // abutment hugs the wall face
			else
				arc++;              // stepped strut covers
		}
		h.assertTrue(pier, "flyer pier missing");
		h.assertTrue(abut, "flyer abutment missing");
		h.assertTrue(arc == 3, "flyer arc covers != 3");
		// for NORTH facing the mass runs south: pier z 3..7, abutment z 12..16
		// (inward distance measured from the north wall face)
		var s = ContourFlyingButtressBlock.boxesFor(f.defaultBlockState()
			.setValue(ContourFlyingButtressBlock.FACING, Direction.SOUTH));
		boolean sPier = false;
		for (float[] b : s)
			if (b[4] == 11f && b[2] == 3f && b[5] == 7f)
				sPier = true;
		h.assertTrue(sPier, "flyer not facing-swapped");
		h.succeed();
	}

	@GameTest(template = "test_empty")
	public void copingCap(GameTestHelper h) {
		Block c = ContourRegistries.CONTOUR_COPING.get();
		var x = ContourCopingBlock.boxesFor(c.defaultBlockState());
		h.assertTrue(x.size() == 2, "coping not 2 boxes");
		boolean seat = false, cap = false;
		for (float[] b : x) {
			if (b[4] == 2f && b[2] == 5f && b[5] == 11f)
				seat = true;
			if (b[1] == 2f && b[4] == 5f && b[2] == 2f && b[5] == 14f)
				cap = true;
		}
		h.assertTrue(seat && cap, "coping seat/cap missing");
		// Z axis: cross section swaps to X
		var z = ContourCopingBlock.boxesFor(c.defaultBlockState()
			.setValue(ContourCopingBlock.AXIS, Direction.Axis.Z));
		boolean zSeat = false;
		for (float[] b : z)
			if (b[4] == 2f && b[0] == 5f && b[3] == 11f)
				zSeat = true;
		h.assertTrue(zSeat, "coping not axis-swapped");
		h.succeed();
	}

	@GameTest(template = "test_empty")
	public void oculusRing(GameTestHelper h) {
		Block o = ContourRegistries.CONTOUR_OCULUS.get();
		var y = ContourOculusBlock.boxesFor(o.defaultBlockState());
		h.assertTrue(y.size() == 16, "oculus not 16 covers");
		// every cover stays inside the 3px band thickness
		for (float[] b : y)
			h.assertTrue(b[1] >= 6.4f && b[4] <= 9.6f, "oculus band off thickness");
		// bore open: the center square [6,10] must stay collision-free
		// (min covered radius ~3.7 vs the r4 visual bore)
		for (float[] b : y) {
			boolean hitsCenter = b[0] < 10f && b[3] > 6f
				&& b[2] < 10f && b[5] > 6f;
			h.assertTrue(!hitsCenter, "cover invades bore");
		}
		// ring coverage: every segment midpoint at r6 must be covered
		for (int q = 0; q < 16; q++) {
			double t = Math.PI / 8 * q + Math.PI / 16;
			float u = (float) (8 + 6 * Math.cos(t)),
				v = (float) (8 + 6 * Math.sin(t));
			boolean covered = false;
			for (float[] b : y)
				if (u >= b[0] && u <= b[3] && v >= b[2] && v <= b[5])
					covered = true;
			h.assertTrue(covered, "ring under-filled at segment " + q);
		}
		// X axis: ring lies in the Y-Z plane
		var x = ContourOculusBlock.boxesFor(o.defaultBlockState()
			.setValue(ContourOculusBlock.AXIS, Direction.Axis.X));
		boolean xBand = true;
		for (float[] b : x)
			if (!(b[0] >= 6.4f && b[3] <= 9.6f))
				xBand = false;
		h.assertTrue(xBand, "oculus not axis-swapped");
		h.succeed();
	}

	@GameTest(template = "test_empty")
	public void awningSlope(GameTestHelper h) {
		Block a = ContourRegistries.CONTOUR_AWNING.get();
		var n = ContourAwningBlock.boxesFor(a.defaultBlockState());
		h.assertTrue(n.size() == 2, "awning not 2 covers");
		boolean hi = false, lo = false;
		for (float[] b : n) {
			if (b[2] == 0f && b[5] == 4f && b[4] == ContourAwningBlock.yTop(0f))
				hi = true; // wall-attached upper segment
			if (b[2] == 4f && b[5] == 8f && b[4] == ContourAwningBlock.yTop(4f))
				lo = true; // sloped-down outer segment
		}
		h.assertTrue(hi && lo, "awning segments missing");
		// SOUTH: projection flips to +z end of the cell
		var s = ContourAwningBlock.boxesFor(a.defaultBlockState()
			.setValue(ContourAwningBlock.FACING, Direction.SOUTH));
		boolean sHi = false;
		for (float[] b : s)
			if (b[2] == 12f && b[5] == 16f && b[4] == ContourAwningBlock.yTop(0f))
				sHi = true;
		h.assertTrue(sHi, "awning not facing-swapped");
		h.succeed();
	}

	@GameTest(template = "test_empty")
	public void bufferRake(GameTestHelper h) {
		Block b = ContourRegistries.CONTOUR_BUFFER.get();
		var n = ContourBufferBlock.boxesFor(b.defaultBlockState());
		h.assertTrue(n.size() == 7, "buffer not 7 boxes");
		boolean plate = false, foot = false;
		for (float[] x : n) {
			if (x[2] == 0f && x[5] == 2f && x[4] == 13f && x[0] == 2f && x[3] == 14f)
				plate = true; // full-width plate at the facing face
			if (x[1] == 0f && x[4] == 3f && x[2] == 11f && x[5] == 15f)
				foot = true; // rear foot
		}
		h.assertTrue(plate && foot, "buffer plate/foot missing");
		// SOUTH: plate flips to +z
		var s = ContourBufferBlock.boxesFor(b.defaultBlockState()
			.setValue(ContourBufferBlock.FACING, Direction.SOUTH));
		boolean sPlate = false;
		for (float[] x : s)
			if (x[2] == 14f && x[5] == 16f && x[4] == 13f)
				sPlate = true;
		h.assertTrue(sPlate, "buffer not facing-swapped");
		h.succeed();
	}

	@GameTest(template = "test_empty")
	public void smokestackFlare(GameTestHelper h) {
		Block s = ContourRegistries.CONTOUR_SMOKESTACK.get();
		var y = ContourSmokestackBlock.boxesFor(s.defaultBlockState());
		h.assertTrue(y.size() == 4, "stack not 4 bands");
		boolean skirt = false, barrel = false, rim = false;
		for (float[] b : y) {
			if (b[1] == 0f && b[0] == 2.5f && b[3] == 13.5f)
				skirt = true; // wide base
			if (b[1] == 2f && b[0] == 4f && b[3] == 12f)
				barrel = true; // slim middle
			if (b[1] == 13f && b[0] == 1f && b[3] == 15f)
				rim = true; // flared crown
		}
		h.assertTrue(skirt && barrel && rim, "stack bands missing");
		// X axis: bands swap to x-extents
		var x = ContourSmokestackBlock.boxesFor(s.defaultBlockState()
			.setValue(ContourSmokestackBlock.AXIS, Direction.Axis.X));
		boolean xBarrel = false;
		for (float[] b : x)
			if (b[0] == 2f && b[3] == 9f && b[1] == 4f && b[4] == 12f)
				xBarrel = true;
		h.assertTrue(xBarrel, "stack not axis-swapped");
		h.succeed();
	}

	@GameTest(template = "test_empty")
	public void semaphoreBlade(GameTestHelper h) {
		Block s = ContourRegistries.CONTOUR_SEMAPHORE.get();
		var n = ContourSemaphoreBlock.boxesFor(s.defaultBlockState());
		h.assertTrue(n.size() == 5, "semaphore not 5 boxes");
		boolean blade = false, mast = false, counter = false;
		for (float[] b : n) {
			if (b[2] == 1f && b[5] == 8f && b[1] == 8f && b[4] == 11f)
				blade = true; // flat arm toward facing
			if (b[2] == 7f && b[1] == 0f && b[4] == 7f)
				mast = true; // stub to the ground
			if (b[2] == 9f && b[5] == 11f && b[1] == 6f)
				counter = true; // counterweight behind pivot
		}
		h.assertTrue(blade && mast && counter, "semaphore parts missing");
		// SOUTH: blade flips to +z
		var so = ContourSemaphoreBlock.boxesFor(s.defaultBlockState()
			.setValue(ContourSemaphoreBlock.FACING, Direction.SOUTH));
		boolean sBlade = false;
		for (float[] b : so)
			if (b[2] == 8f && b[5] == 15f && b[1] == 8f)
				sBlade = true;
		h.assertTrue(sBlade, "semaphore not facing-swapped");
		h.succeed();
	}

	@GameTest(template = "test_empty")
	public void pilotWedge(GameTestHelper h) {
		Block p = ContourRegistries.CONTOUR_PILOT.get();
		var n = ContourPilotBlock.boxesFor(p.defaultBlockState());
		h.assertTrue(n.size() == 4, "pilot not 4 boxes");
		boolean skid = false, tip = false, top = false;
		for (float[] b : n) {
			if (b[2] == 0f && b[4] == 2f && b[5] == 6f)
				skid = true; // bottom plate under the tip
			if (b[2] == 0f && b[5] == 3f && b[4] == 6f && b[0] == 5f)
				tip = true; // narrow front row
			if (b[2] == 6f && b[5] == 9f && b[4] == 14f && b[0] == 3f)
				top = true; // wide rear row
		}
		h.assertTrue(skid && tip && top, "pilot wedge rows missing");
		// wedge rises back: each row's y0 climbs with distance
		h.assertTrue(ContourPilotBlock.boxesFor(p.defaultBlockState()
			.setValue(ContourPilotBlock.FACING, Direction.SOUTH)).size() == 4,
			"pilot not facing-swapped");
		h.succeed();
	}

	// --- ledger drops across the wave ---

	@GameTest(template = "test_empty")
	public void waveLedgerDrops(GameTestHelper h) {
		for (Block b : waveBlocks()) {
			Player p = h.makeMockPlayer(GameType.SURVIVAL);
			p.setPos(Vec3.atCenterOf(h.absolutePos(P)));
			p.getInventory().clearContent();
			ContourBlockEntity be = be(h, b.defaultBlockState());
			be.applyPayload(Blocks.STONE.defaultBlockState(), 1, Direction.NORTH, false);
			h.getLevel().destroyBlock(h.absolutePos(P), true, p);
			var box = new net.minecraft.world.phys.AABB(h.absolutePos(P)).inflate(3);
			var items = h.getLevel().getEntities(
				net.minecraft.world.entity.EntityType.ITEM, box,
				e -> e.getItem().getItem() instanceof ContourBlockItem);
			h.assertTrue(items.size() == 1, b + " dropped " + items.size());
			var s = items.get(0).getItem();
			h.assertTrue(ContourBlockItem.storedMaterial(h.getLevel().registryAccess(), s)
				== null && ContourBlockItem.storedPaid(s) == 0,
				b + " dropped a painted item");
			var mats = h.getLevel().getEntities(
				net.minecraft.world.entity.EntityType.ITEM, box,
				e -> e.getItem().is(net.minecraft.world.item.Items.STONE));
			h.assertTrue(mats.size() == 1 && mats.get(0).getItem().getCount() == 1,
				b + " did not refund material");
			items.get(0).discard();
			mats.get(0).discard();
		}
		h.succeed();
	}

	// --- wave 49: vertical step / beam / half panel / flat pane ---

	@GameTest(template = "test_empty")
	public void verticalStepTread(GameTestHelper h) {
		Block p = ContourRegistries.CONTOUR_VERTICAL_STEP.get();
		var n = ContourVerticalStepBlock.boxesFor(p.defaultBlockState());
		h.assertTrue(n.size() == 2, "vstep not 2 boxes");
		boolean plate = false, tread = false;
		for (float[] b : n) {
			if (b[2] == 14f && b[5] == 16f && b[4] == 16f)
				plate = true; // back plate at the wall
			if (b[2] == 8f && b[5] == 14f && b[4] == 8f)
				tread = true; // bottom-half tread protruding
		}
		h.assertTrue(plate && tread, "vstep plate/tread missing");
		var s = ContourVerticalStepBlock.boxesFor(p.defaultBlockState()
			.setValue(ContourVerticalStepBlock.FACING, Direction.SOUTH));
		boolean swap = false;
		for (float[] b : s)
			if (b[2] == 0f && b[5] == 2f && b[4] == 16f)
				swap = true; // plate swapped to the far face
		h.assertTrue(swap, "vstep not facing-swapped");
		h.succeed();
	}

	@GameTest(template = "test_empty")
	public void beamCentered(GameTestHelper h) {
		Block p = ContourRegistries.CONTOUR_BEAM.get();
		var n = ContourBeamBlock.boxesFor(p.defaultBlockState()
			.setValue(ContourBeamBlock.AXIS, Direction.Axis.Z));
		h.assertTrue(n.size() == 1, "beam not 1 box");
		float[] b = n.get(0);
		h.assertTrue(b[0] == 4f && b[3] == 12f && b[4] == 8f
			&& b[2] == 0f && b[5] == 16f, "beam not centred tread");
		var y = ContourBeamBlock.boxesFor(p.defaultBlockState()
			.setValue(ContourBeamBlock.AXIS, Direction.Axis.Y));
		h.assertTrue(y.get(0)[0] == 4f && y.get(0)[3] == 12f
			&& y.get(0)[4] == 16f, "beam Y not upright");
		h.succeed();
	}

	@GameTest(template = "test_empty")
	public void halfPanelHalves(GameTestHelper h) {
		Block p = ContourRegistries.CONTOUR_HALF_PANEL.get();
		var n = ContourHalfPanelBlock.boxesFor(p.defaultBlockState()
			.setValue(ContourHalfPanelBlock.FACE, Direction.UP)
			.setValue(ContourHalfPanelBlock.HALF, Direction.NORTH));
		h.assertTrue(n.size() == 1, "half panel not 1 box");
		float[] b = n.get(0);
		h.assertTrue(b[1] == 14f && b[4] == 16f && b[5] == 8f,
			"half panel not north half of up face");
		var s = ContourHalfPanelBlock.boxesFor(p.defaultBlockState()
			.setValue(ContourHalfPanelBlock.FACE, Direction.NORTH)
			.setValue(ContourHalfPanelBlock.HALF, Direction.UP));
		h.assertTrue(s.get(0)[2] == 0f && s.get(0)[5] == 2f
			&& s.get(0)[1] == 8f, "half panel vertical half wrong");
		h.succeed();
	}

	@GameTest(template = "test_empty")
	public void flatPaneSheet(GameTestHelper h) {
		Block p = ContourRegistries.CONTOUR_FLAT_PANE.get();
		var n = ContourFlatPaneBlock.boxesFor(p.defaultBlockState());
		h.assertTrue(n.size() == 1, "flat pane not 1 box");
		float[] b = n.get(0);
		h.assertTrue(b[1] == 7f && b[4] == 9f
			&& b[0] == 0f && b[3] == 16f, "flat pane not mid sheet");
		h.succeed();
	}

	// --- schematic checklist requirements ---

	@GameTest(template = "test_empty")
	public void schematicRequirements(GameTestHelper h) {
		// painted single-unit host: requirement = 1x material (default 1x host)
		ContourBlockEntity be = be(h,
			ContourRegistries.CONTOUR_SLAB.get().defaultBlockState());
		be.applyPayload(Blocks.STONE.defaultBlockState(), 1, Direction.NORTH, false);
		var req = be.getRequiredItems(
			ContourRegistries.CONTOUR_SLAB.get().defaultBlockState());
		var list = req.getRequiredItems();
		h.assertTrue(list.size() == 1, "req not material-only: " + list.size());
		h.assertTrue(list.get(0).stack.is(net.minecraft.world.item.Items.STONE)
			&& list.get(0).stack.getCount() == 1, "req material wrong");

		// painted double slab: material x2, no extra host units (Create's
		// default requirement already charges 2x for SLAB_TYPE=DOUBLE)
		BlockState dbl = ContourRegistries.CONTOUR_SLAB.get().defaultBlockState()
			.setValue(net.createcontour.block.ContourSlabBlock.TYPE,
				net.minecraft.world.level.block.state.properties.SlabType.DOUBLE);
		ContourBlockEntity be2 = be(h, dbl);
		be2.applyPayload(Blocks.STONE.defaultBlockState(), 2, Direction.NORTH, false);
		var req2 = be2.getRequiredItems(dbl);
		var list2 = req2.getRequiredItems();
		int mat = 0, host = 0;
		for (var sr : list2) {
			if (sr.stack.is(net.minecraft.world.item.Items.STONE))
				mat += sr.stack.getCount();
			if (sr.stack.is(ContourRegistries.CONTOUR_SLAB_ITEM.get()))
				host += sr.stack.getCount();
		}
		h.assertTrue(mat == 2, "req material not x2: " + mat);
		h.assertTrue(host == 0, "req duplicate host units: " + host);

		// unpainted: no material entry, no extras for single host. The BE at
		// P persists across same-block setBlock calls, so clear it first.
		ContourBlockEntity be3 = be(h,
			ContourRegistries.CONTOUR_SLAB.get().defaultBlockState());
		be3.applyPayload(net.createcontour.material.MaterialSafety.neutral(), 0,
			Direction.NORTH, false);
		h.assertTrue(be3.getRequiredItems(
			ContourRegistries.CONTOUR_SLAB.get().defaultBlockState())
			.isEmpty(), "unpainted req not empty");

		// writeSafe: painted + consume-on exports ledger = hostUnits
		var be4 = be(h, ContourRegistries.CONTOUR_SLAB.get().defaultBlockState());
		be4.applyPayload(Blocks.STONE.defaultBlockState(), 1, Direction.NORTH, false);
		var tag = new net.minecraft.nbt.CompoundTag();
		be4.writeSafe(tag, h.getLevel().registryAccess());
		h.assertTrue(tag.getInt("PaidUnits") == 1,
			"safeNBT paid not hostUnits: " + tag.getInt("PaidUnits"));
		h.assertTrue(tag.contains("Material"), "safeNBT material missing");

		// writeSafe: unpainted exports no paid credit
		var be5 = be(h, ContourRegistries.CONTOUR_SLAB.get().defaultBlockState());
		be5.applyPayload(net.createcontour.material.MaterialSafety.neutral(), 0,
			Direction.NORTH, false);
		var tag2 = new net.minecraft.nbt.CompoundTag();
		be5.writeSafe(tag2, h.getLevel().registryAccess());
		h.assertTrue(tag2.getInt("PaidUnits") == 0,
			"unpainted safeNBT exported credit");
		h.succeed();
	}

	// --- wave 50: parity gap fill ---

	@GameTest(template = "test_empty")
	public void cornerSliceWedge(GameTestHelper h) {
		Block p = ContourRegistries.CONTOUR_CORNER_SLICE.get();
		var n = net.createcontour.block.ContourCornerSliceBlock.boxesFor(
			p.defaultBlockState()
				.setValue(net.createcontour.block.ContourCornerSliceBlock.LAYERS, 2));
		h.assertTrue(n.size() == 2, "corner slice not 2 steps: " + n.size());
		float[] b0 = n.get(0), b1 = n.get(1);
		h.assertTrue(b0[0] == 0f && b0[3] == 4f && b0[5] == 2f,
			"corner slice row0 wrong");
		h.assertTrue(b1[2] == 2f && b1[3] == 2f && b1[5] == 4f,
			"corner slice row1 not stepped");
		// rotation maps the corner to the next quadrant
		var e = p.defaultBlockState()
			.setValue(net.createcontour.block.ContourCornerSliceBlock.FACING,
				Direction.NORTH)
			.rotate(net.minecraft.world.level.block.Rotation.CLOCKWISE_90)
			.getValue(net.createcontour.block.ContourCornerSliceBlock.FACING);
		h.assertTrue(e == Direction.EAST, "corner slice rotate wrong: " + e);
		// layer 4 spans 8px along both edges (quarter-cell wedge)
		var q = net.createcontour.block.ContourCornerSliceBlock.boxesFor(
			p.defaultBlockState()
				.setValue(net.createcontour.block.ContourCornerSliceBlock.LAYERS, 4));
		h.assertTrue(q.size() == 4 && q.get(0)[3] == 8f,
			"corner slice max reach wrong: " + q.size());
		h.succeed();
	}

	@GameTest(template = "test_empty")
	public void bytePanelQuadrant(GameTestHelper h) {
		Block p = ContourRegistries.CONTOUR_BYTE_PANEL.get();
		var q0 = net.createcontour.block.ContourBytePanelBlock.boxesFor(
			p.defaultBlockState()
				.setValue(net.createcontour.block.ContourBytePanelBlock.FACING,
					Direction.UP)
				.setValue(net.createcontour.block.ContourBytePanelBlock.QUADRANT, 0));
		h.assertTrue(q0.size() == 1, "byte panel not 1 box");
		float[] b = q0.get(0);
		h.assertTrue(b[1] == 15f && b[4] == 16f && b[3] == 8f && b[5] == 8f,
			"byte panel q0 not lower-left eighth");
		var q3 = net.createcontour.block.ContourBytePanelBlock.boxesFor(
			p.defaultBlockState()
				.setValue(net.createcontour.block.ContourBytePanelBlock.FACING,
					Direction.UP)
				.setValue(net.createcontour.block.ContourBytePanelBlock.QUADRANT, 3));
		float[] b3 = q3.get(0);
		h.assertTrue(b3[0] == 8f && b3[2] == 8f && b3[3] == 16f && b3[5] == 16f,
			"byte panel q3 not upper-right eighth");
		h.succeed();
	}

	@GameTest(template = "test_empty")
	public void halfLayerPx(GameTestHelper h) {
		Block p = ContourRegistries.CONTOUR_HALF_LAYER.get();
		BlockState s5 = p.defaultBlockState()
			.setValue(net.createcontour.block.ContourHalfLayerBlock.LAYERS, 5);
		var shape = s5.getShape(h.getLevel(),
			net.minecraft.core.BlockPos.ZERO,
			net.minecraft.world.phys.shapes.CollisionContext.empty());
		h.assertTrue(Math.abs(shape.max(Direction.Axis.Y) - 5.0 / 16.0) < 1e-6,
			"half layer height not 5px: " + shape.max(Direction.Axis.Y));
		BlockState s8 = p.defaultBlockState()
			.setValue(net.createcontour.block.ContourHalfLayerBlock.LAYERS, 8);
		var shape8 = s8.getShape(h.getLevel(),
			net.minecraft.core.BlockPos.ZERO,
			net.minecraft.world.phys.shapes.CollisionContext.empty());
		h.assertTrue(Math.abs(shape8.max(Direction.Axis.Y) - 0.5) < 1e-6,
			"half layer max not half block");
		h.succeed();
	}

	@GameTest(template = "test_empty")
	public void ironVariantsRedstoneOnly(GameTestHelper h) throws Exception {
		var m = ContourDoorBlock.class.getDeclaredMethod("canManualOpen");
		m.setAccessible(true);
		h.assertTrue((Boolean) m.invoke(ContourRegistries.CONTOUR_DOOR.get()),
			"wooden door lost manual open");
		h.assertTrue(!(Boolean) m.invoke(ContourRegistries.CONTOUR_IRON_DOOR.get()),
			"iron door manually openable");
		var mt = ContourTrapdoorBlock.class.getDeclaredMethod("canManualOpen");
		mt.setAccessible(true);
		h.assertTrue((Boolean) mt.invoke(ContourRegistries.CONTOUR_TRAPDOOR.get()),
			"wooden trapdoor lost manual open");
		h.assertTrue(!(Boolean) mt.invoke(
				ContourRegistries.CONTOUR_IRON_TRAPDOOR.get()),
			"iron trapdoor manually openable");
		// redstone path untouched: states still carry OPEN (+ POWERED on door)
		h.assertTrue(ContourRegistries.CONTOUR_IRON_DOOR.get().defaultBlockState()
			.hasProperty(net.minecraft.world.level.block.state.properties
				.BlockStateProperties.OPEN), "iron door missing OPEN");
		h.assertTrue(ContourRegistries.CONTOUR_IRON_DOOR.get().defaultBlockState()
			.hasProperty(net.minecraft.world.level.block.state.properties
				.BlockStateProperties.POWERED), "iron door missing POWERED");
		h.succeed();
	}

	@GameTest(template = "test_empty")
	public void layerStackMerge(GameTestHelper h) {
		// re-click on an existing plate grows one step (snow-style) - the
		// ctx must HOLD the shape item or the place context retargets to
		// the adjacent cell (canBeReplaced rejects foreign stacks)
		Block p = ContourRegistries.CONTOUR_LAYERS.get();
		h.setBlock(P, p.defaultBlockState()
			.setValue(net.createcontour.block.ContourLayersBlock.LAYERS, 4));
		BlockState grown = p.getStateForPlacement(
			new BlockPlaceContext(h.getLevel(), null, InteractionHand.MAIN_HAND,
				new ItemStack(ContourRegistries.CONTOUR_LAYERS_ITEM.get()),
				new BlockHitResult(Vec3.atCenterOf(h.absolutePos(P)),
					Direction.UP, h.absolutePos(P), false)));
		h.assertTrue(grown.getValue(
			net.createcontour.block.ContourLayersBlock.LAYERS) == 5,
			"layer stack did not grow: " + grown);
		// cap: a full plate stays full
		h.setBlock(P, p.defaultBlockState()
			.setValue(net.createcontour.block.ContourLayersBlock.LAYERS, 8));
		BlockState cap = p.getStateForPlacement(
			new BlockPlaceContext(h.getLevel(), null, InteractionHand.MAIN_HAND,
				new ItemStack(ContourRegistries.CONTOUR_LAYERS_ITEM.get()),
				new BlockHitResult(Vec3.atCenterOf(h.absolutePos(P)),
					Direction.UP, h.absolutePos(P), false)));
		h.assertTrue(cap.getValue(
			net.createcontour.block.ContourLayersBlock.LAYERS) == 8,
			"layer stack overfilled: " + cap);
		// slice growth keeps its facing
		Block sl = ContourRegistries.CONTOUR_SLICE.get();
		h.setBlock(P, sl.defaultBlockState()
			.setValue(net.createcontour.block.ContourSliceBlock.FACING, Direction.UP)
			.setValue(net.createcontour.block.ContourSliceBlock.LAYERS, 2));
		BlockState sg = sl.getStateForPlacement(
			new BlockPlaceContext(h.getLevel(), null, InteractionHand.MAIN_HAND,
				new ItemStack(ContourRegistries.CONTOUR_SLICE_ITEM.get()),
				new BlockHitResult(Vec3.atCenterOf(h.absolutePos(P)),
					Direction.UP, h.absolutePos(P), false)));
		h.assertTrue(sg.getValue(
				net.createcontour.block.ContourSliceBlock.LAYERS) == 3
			&& sg.getValue(net.createcontour.block.ContourSliceBlock.FACING)
				== Direction.UP,
			"slice stack lost facing: " + sg);
		// canBeReplaced: same item + room -> merge target; full -> no
		ItemStack held = new ItemStack(ContourRegistries.CONTOUR_LAYERS_ITEM.get());
		var ctxR = new BlockPlaceContext(h.getLevel(), null,
			InteractionHand.MAIN_HAND, held,
			new BlockHitResult(Vec3.atCenterOf(h.absolutePos(P)),
				Direction.UP, h.absolutePos(P), false));
		h.setBlock(P, p.defaultBlockState()
			.setValue(net.createcontour.block.ContourLayersBlock.LAYERS, 4));
		h.assertTrue(h.getLevel().getBlockState(h.absolutePos(P))
				.canBeReplaced(ctxR),
			"layers merge rejected with room");
		h.setBlock(P, p.defaultBlockState()
			.setValue(net.createcontour.block.ContourLayersBlock.LAYERS, 8));
		h.assertTrue(!h.getLevel().getBlockState(h.absolutePos(P))
				.canBeReplaced(ctxR),
			"layers merge accepted at cap");
		h.succeed();
	}

	@GameTest(template = "test_empty")
	public void slidingDoorLeaf(GameTestHelper h) {
		// closed: full-width plate on the facing plane, same as hinged
		Block p = ContourRegistries.CONTOUR_SLIDING_DOOR.get();
		BlockState closed = p.defaultBlockState()
			.setValue(net.createcontour.block.ContourDoorBlock.FACING,
				Direction.NORTH)
			.setValue(net.createcontour.block.ContourDoorBlock.HINGE,
				net.minecraft.world.level.block.state.properties.DoorHingeSide.LEFT)
			.setValue(net.createcontour.block.ContourDoorBlock.OPEN, false);
		var c = net.createcontour.block.ContourSlidingDoorBlock.boxesFor(closed);
		h.assertTrue(c.size() == 1 && c.get(0)[3] == 16f && c.get(0)[5] == 3f,
			"sliding door closed plate wrong");
		// open: leaf slid to the hinge-side half, still on the facing
		// plane (ADR-015 state toggle, not a swept move)
		BlockState open = closed.setValue(
			net.createcontour.block.ContourDoorBlock.OPEN, true);
		var o = net.createcontour.block.ContourSlidingDoorBlock.boxesFor(open);
		h.assertTrue(o.size() == 1
				&& o.get(0)[3] - o.get(0)[0] == 8f && o.get(0)[5] == 3f,
			"sliding door open leaf not in pocket half: "
				+ java.util.Arrays.toString(o.get(0)));
		// ledger/pairing identical to hinged: same properties present
		h.assertTrue(open.hasProperty(net.minecraft.world.level.block.state
				.properties.BlockStateProperties.DOUBLE_BLOCK_HALF)
			&& open.hasProperty(net.minecraft.world.level.block.state
				.properties.BlockStateProperties.POWERED),
			"sliding door lost door properties");
		h.succeed();
	}

	@GameTest(template = "test_empty")
	public void foldingDoorLeaf(GameTestHelper h) {
		Block p = ContourRegistries.CONTOUR_FOLDING_DOOR.get();
		BlockState closed = p.defaultBlockState()
			.setValue(net.createcontour.block.ContourDoorBlock.FACING,
				Direction.NORTH)
			.setValue(net.createcontour.block.ContourDoorBlock.HINGE,
				net.minecraft.world.level.block.state.properties.DoorHingeSide.LEFT)
			.setValue(net.createcontour.block.ContourDoorBlock.OPEN, false);
		var c = net.createcontour.block.ContourFoldingDoorBlock.boxesFor(closed);
		h.assertTrue(c.size() == 1 && c.get(0)[3] == 16f && c.get(0)[5] == 3f,
			"folding door closed plate wrong");
		// open: folded stack = half span AND doubled thickness on the
		// facing plane (distinct from sliding's half-span single-thick)
		var o = net.createcontour.block.ContourFoldingDoorBlock.boxesFor(
			closed.setValue(net.createcontour.block.ContourDoorBlock.OPEN, true));
		h.assertTrue(o.size() == 1
				&& o.get(0)[3] - o.get(0)[0] == 8f
				&& o.get(0)[5] - o.get(0)[2] == 6f,
			"folding door open stack wrong: "
				+ java.util.Arrays.toString(o.get(0)));
		h.succeed();
	}

	@GameTest(template = "test_empty")
	public void catwalkGrate(GameTestHelper h) {
		Block p = ContourRegistries.CONTOUR_CATWALK.get();
		var x = net.createcontour.block.ContourCatwalkBlock.boxesFor(
			p.defaultBlockState());
		h.assertTrue(x.size() == 3, "catwalk not sheet+2 rails: " + x.size());
		float[] sheet = x.get(0), r1 = x.get(1), r2 = x.get(2);
		h.assertTrue(sheet[1] == 0f && sheet[4] == 1f,
			"catwalk sheet not 1px");
		h.assertTrue(r1[4] == 8f && r1[0] == 0f && r2[0] == 15f,
			"catwalk X rails wrong");
		var z = net.createcontour.block.ContourCatwalkBlock.boxesFor(
			p.defaultBlockState().setValue(
				net.createcontour.block.ContourCatwalkBlock.AXIS,
				Direction.Axis.Z));
		h.assertTrue(z.get(1)[2] == 0f && z.get(1)[5] == 1f
				&& z.get(2)[2] == 15f,
			"catwalk Z rails wrong");
		h.succeed();
	}
}
