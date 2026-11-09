package net.createcontour.client;

import com.simibubi.create.content.decoration.copycat.CopycatModel;
import java.util.List;
import net.createcontour.block.ContourBlock;
import net.createcontour.block.ContourSlabBlock;
import net.createcontour.block.ContourStairsBlock;
import net.createcontour.block.ContourVerticalSlabBlock;
import net.createcontour.blockentity.ContourBlockEntity;
import net.createcontour.item.ContourBlockItem;
import net.createcontour.material.MaterialSafety;
import net.createcontour.registry.ContourRegistries;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.createcontour.block.ContourDoorBlock;
import net.createcontour.block.ContourLampBlock;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

/**
 * Client-only wiring: model wrappers for EVERY blockstate variant and the
 * three inventory models, block/item colors, and the opt-in render smoke
 * (`-Dcreate_contour.renderSmoke=true`) that exercises null-state /
 * empty-data / material paths against the real baked models.
 */
@EventBusSubscriber(modid = "create_contour", value = Dist.CLIENT,
	bus = EventBusSubscriber.Bus.MOD)
public final class ContourClient {

	private ContourClient() {
	}

	private interface ModelFactory {
		ContourModel create(BakedModel original, BlockState boundState,
			BlockState boundMaterial);
	}

	private static final ModelFactory SLAB = ContourSlabModel::new;
	private static final ModelFactory VSLAB = ContourVerticalSlabModel::new;
	private static final ModelFactory STAIRS = ContourStairsModel::new;
	private static final ModelFactory LAYERS = ContourLayersModel::new;
	private static final ModelFactory POST = ContourPostModel::new;
	private static final ModelFactory BYTE = ContourByteModel::new;
	private static final ModelFactory PANEL = ContourPanelModel::new;
	private static final ModelFactory SLOPE = ContourSlopeModel::new;
	private static final ModelFactory HALF_SLOPE =
		(o, st, m) -> new ContourSlopeModel(o, st, m, 8f);
	private static final ModelFactory NOTCH =
		(o, st, m) -> new ContourBoxModel(o, st, m,
			net.createcontour.block.ContourNotchBlock::boxesFor,
			ContourRegistries.CONTOUR_NOTCH);
	private static final ModelFactory ROD =
		(o, st, m) -> new ContourBoxModel(o, st, m,
			net.createcontour.block.ContourRodBlock::boxesFor,
			ContourRegistries.CONTOUR_ROD);
	private static final ModelFactory LATTICE =
		(o, st, m) -> new ContourBoxModel(o, st, m,
			net.createcontour.block.ContourLatticeBlock::boxesFor,
			ContourRegistries.CONTOUR_LATTICE);
	private static final ModelFactory QUARTER_ROUND = ContourQuarterRoundModel::new;
	private static final ModelFactory CHAIN =
		(o, st, m) -> new ContourBoxModel(o, st, m,
			net.createcontour.block.ContourChainBlock::boxesFor,
			ContourRegistries.CONTOUR_CHAIN);
	private static final ModelFactory ROUND_CORNER = ContourRoundCornerModel::new;
	private static final ModelFactory DOME = ContourDomeModel::new;
	private static final ModelFactory RIDGE = ContourRidgeModel::new;
	private static final ModelFactory TUBE =
		(o, st, m) -> new ContourBoxModel(o, st, m,
			net.createcontour.block.ContourTubeBlock::boxesFor,
			ContourRegistries.CONTOUR_TUBE);
	private static final ModelFactory GUTTER =
		(o, st, m) -> new ContourBoxModel(o, st, m,
			net.createcontour.block.ContourGutterBlock::boxesFor,
			ContourRegistries.CONTOUR_GUTTER);
	private static final ModelFactory BRACKET =
		(o, st, m) -> new ContourBoxModel(o, st, m,
			net.createcontour.block.ContourBracketBlock::boxesFor,
			ContourRegistries.CONTOUR_BRACKET);
	private static final ModelFactory VERTICAL_STAIRS =
		(o, st, m) -> new ContourBoxModel(o, st, m,
			net.createcontour.block.ContourVerticalStairsBlock::boxesFor,
			ContourRegistries.CONTOUR_VERTICAL_STAIRS);
	private static final ModelFactory ARCH = ContourArchModel::new;
	private static final ModelFactory CYLINDER = ContourCylinderModel::new;
	private static final ModelFactory PILASTER = ContourPilasterModel::new;
	private static final ModelFactory STAIR_CORNER =
		(o, st, m) -> new ContourBoxModel(o, st, m,
			net.createcontour.block.ContourStairCornerBlock::boxesFor,
			ContourRegistries.CONTOUR_STAIR_CORNER);
	private static final ModelFactory SPHERE = ContourSphereModel::new;
	private static final ModelFactory CONE = ContourConeModel::new;
	private static final ModelFactory CAPITAL =
		(o, st, m) -> new ContourBoxModel(o, st, m,
			net.createcontour.block.ContourCapitalBlock::boxesFor,
			ContourRegistries.CONTOUR_CAPITAL);
	private static final ModelFactory BULLNOSE = ContourBullnoseModel::new;
	private static final ModelFactory CORNICE =
		(o, st, m) -> new ContourBoxModel(o, st, m,
			net.createcontour.block.ContourCorniceBlock::boxesFor,
			ContourRegistries.CONTOUR_CORNICE);
	private static final ModelFactory BALUSTER = ContourBalusterModel::new;
	private static final ModelFactory FINIAL = ContourFinialModel::new;
	// scotia: r5 quarter-arc profile (7 samples)
	private static final float[][] SCOTIA_PROF = new float[7][2];
	static {
		for (int i = 0; i < 7; i++) {
			double t = Math.PI * 0.5 * i / 6;
			SCOTIA_PROF[i][0] = 5f * (float) Math.sin(t);
			SCOTIA_PROF[i][1] = 5f * (float) Math.cos(t);
		}
	}
	// ogee: 8px smoothstep S from (0,8) to (8,0)
	private static final float[][] OGEE_PROF = new float[9][2];
	static {
		for (int i = 0; i < 9; i++) {
			double t = i / 8.0, ss = 3 * t * t - 2 * t * t * t;
			OGEE_PROF[i][0] = (float) (i * 1.0);
			OGEE_PROF[i][1] = (float) (8 * (1 - ss));
		}
	}
	private static final ModelFactory SCOTIA =
		(o, st, m) -> new ContourEdgeProfileModel(o, st, m, SCOTIA_PROF,
			ContourRegistries.CONTOUR_SCOTIA::get);
	private static final ModelFactory OGEE =
		(o, st, m) -> new ContourEdgeProfileModel(o, st, m, OGEE_PROF,
			ContourRegistries.CONTOUR_OGEE::get);
	private static final ModelFactory SIGN =
		(o, st, m) -> new ContourBoxModel(o, st, m,
			net.createcontour.block.ContourSignBlock::boxesFor,
			ContourRegistries.CONTOUR_SIGN);
	private static final ModelFactory TORUS = ContourTorusModel::new;
	private static final ModelFactory CRENELLATION =
		(o, st, m) -> new ContourBoxModel(o, st, m,
			net.createcontour.block.ContourCrenellationBlock::boxesFor,
			ContourRegistries.CONTOUR_CRENELLATION);
	private static final ModelFactory BARREL_VAULT = ContourBarrelVaultModel::new;
	private static final ModelFactory COG =
		(o, st, m) -> new ContourBoxModel(o, st, m,
			net.createcontour.block.ContourCogBlock::boxesFor,
			ContourRegistries.CONTOUR_COG);
	private static final ModelFactory WHEEL =
		(o, st, m) -> new ContourBoxModel(o, st, m,
			net.createcontour.block.ContourWheelBlock::boxesFor,
			ContourRegistries.CONTOUR_WHEEL);
	private static final ModelFactory SCAFFOLD =
		(o, st, m) -> new ContourBoxModel(o, st, m,
			net.createcontour.block.ContourScaffoldBlock::boxesFor,
			ContourRegistries.CONTOUR_SCAFFOLD);
	private static final ModelFactory GOTHIC_ARCH = ContourGothicArchModel::new;
	private static final ModelFactory LOUVER = ContourLouverModel::new;
	private static final ModelFactory ELBOW = ContourElbowModel::new;
	private static final ModelFactory HANDRAIL = ContourHandrailModel::new;
	private static final ModelFactory NEWEL = ContourNewelModel::new;
	private static final ModelFactory BALUSTRADE =
		(o, s, m) -> new ContourBoxModel(o, s, m,
			net.createcontour.block.ContourBalustradeBlock::boxesFor,
			ContourRegistries.CONTOUR_BALUSTRADE);
	private static final ModelFactory I_BEAM =
		(o, s, m) -> new ContourBoxModel(o, s, m,
			net.createcontour.block.ContourIBeamBlock::boxesFor,
			ContourRegistries.CONTOUR_I_BEAM);
	private static final ModelFactory PORTCULLIS =
		(o, s, m) -> new ContourBoxModel(o, s, m,
			net.createcontour.block.ContourPortcullisBlock::boxesFor,
			ContourRegistries.CONTOUR_PORTCULLIS);
	private static final ModelFactory FLANGE = ContourFlangeModel::new;
	private static final ModelFactory BUTTRESS =
		(o, s, m) -> new ContourBoxModel(o, s, m,
			net.createcontour.block.ContourButtressBlock::boxesFor,
			ContourRegistries.CONTOUR_BUTTRESS);
	private static final ModelFactory ARROW_SLIT =
		(o, s, m) -> new ContourBoxModel(o, s, m,
			net.createcontour.block.ContourArrowSlitBlock::boxesFor,
			ContourRegistries.CONTOUR_ARROW_SLIT);
	private static final ModelFactory DENTIL =
		(o, s, m) -> new ContourBoxModel(o, s, m,
			net.createcontour.block.ContourDentilBlock::boxesFor,
			ContourRegistries.CONTOUR_DENTIL);
	private static final ModelFactory TEE = ContourTeeModel::new;
	private static final ModelFactory PALISADE =
		(o, s, m) -> new ContourBoxModel(o, s, m,
			net.createcontour.block.ContourPalisadeBlock::boxesFor,
			ContourRegistries.CONTOUR_PALISADE);
	private static final ModelFactory QUOIN =
		(o, s, m) -> new ContourBoxModel(o, s, m,
			net.createcontour.block.ContourQuoinBlock::boxesFor,
			ContourRegistries.CONTOUR_QUOIN);
	private static final ModelFactory TRUSS = ContourTrussModel::new;
	private static final ModelFactory SILL = ContourSillModel::new;
	private static final ModelFactory BOLLARD = ContourBollardModel::new;
	private static final ModelFactory MULLION =
		(o, s, m) -> new ContourBoxModel(o, s, m,
			net.createcontour.block.ContourMullionBlock::boxesFor,
			ContourRegistries.CONTOUR_MULLION);
	private static final ModelFactory COFFER =
		(o, s, m) -> new ContourBoxModel(o, s, m,
			net.createcontour.block.ContourCofferBlock::boxesFor,
			ContourRegistries.CONTOUR_COFFER);
	private static final ModelFactory KEYSTONE = ContourKeystoneModel::new;
	private static final ModelFactory PEDESTAL =
		(o, s, m) -> new ContourBoxModel(o, s, m,
			net.createcontour.block.ContourPedestalBlock::boxesFor,
			ContourRegistries.CONTOUR_PEDESTAL);
	private static final ModelFactory LANCET =
		(o, s, m) -> new ContourBoxModel(o, s, m,
			net.createcontour.block.ContourLancetBlock::boxesFor,
			ContourRegistries.CONTOUR_LANCET);
	private static final ModelFactory VALLEY = ContourValleyModel::new;
	private static final ModelFactory CROSS = ContourCrossModel::new;
	private static final ModelFactory PIPE_CAP = ContourPipeCapModel::new;
	private static final ModelFactory PEDIMENT =
		(o, s, m) -> new ContourBoxModel(o, s, m,
			net.createcontour.block.ContourPedimentBlock::boxesFor,
			ContourRegistries.CONTOUR_PEDIMENT);
	private static final ModelFactory CHIMNEY =
		(o, s, m) -> new ContourBoxModel(o, s, m,
			net.createcontour.block.ContourChimneyBlock::boxesFor,
			ContourRegistries.CONTOUR_CHIMNEY);
	private static final ModelFactory HALF_TIMBER = ContourHalfTimberModel::new;
	private static final ModelFactory SPANDREL =
		(o, s, m) -> new ContourBoxModel(o, s, m,
			net.createcontour.block.ContourSpandrelBlock::boxesFor,
			ContourRegistries.CONTOUR_SPANDREL);
	private static final ModelFactory ARC_WALL = ContourArcWallModel::new;
	private static final ModelFactory ROSETTE = ContourRosetteModel::new;
	private static final ModelFactory FLYING_BUTTRESS =
		ContourFlyingButtressModel::new;
	private static final ModelFactory COPING =
		(o, s, m) -> new ContourBoxModel(o, s, m,
			net.createcontour.block.ContourCopingBlock::boxesFor,
			ContourRegistries.CONTOUR_COPING);
	private static final ModelFactory OCULUS = ContourOculusModel::new;
	private static final ModelFactory AWNING = ContourAwningModel::new;
	private static final ModelFactory BUFFER =
		(o, s, m) -> new ContourBoxModel(o, s, m,
			net.createcontour.block.ContourBufferBlock::boxesFor,
			ContourRegistries.CONTOUR_BUFFER);
	private static final ModelFactory SMOKESTACK = ContourSmokestackModel::new;
	private static final ModelFactory SEMAPHORE =
		(o, s, m) -> new ContourBoxModel(o, s, m,
			net.createcontour.block.ContourSemaphoreBlock::boxesFor,
			ContourRegistries.CONTOUR_SEMAPHORE);
	private static final ModelFactory PILOT =
		(o, s, m) -> new ContourBoxModel(o, s, m,
			net.createcontour.block.ContourPilotBlock::boxesFor,
			ContourRegistries.CONTOUR_PILOT);
	private static final ModelFactory VERTICAL_STEP =
		(o, s, m) -> new ContourBoxModel(o, s, m,
			net.createcontour.block.ContourVerticalStepBlock::boxesFor,
			ContourRegistries.CONTOUR_VERTICAL_STEP);
	private static final ModelFactory BEAM =
		(o, s, m) -> new ContourBoxModel(o, s, m,
			net.createcontour.block.ContourBeamBlock::boxesFor,
			ContourRegistries.CONTOUR_BEAM);
	private static final ModelFactory HALF_PANEL =
		(o, s, m) -> new ContourBoxModel(o, s, m,
			net.createcontour.block.ContourHalfPanelBlock::boxesFor,
			ContourRegistries.CONTOUR_HALF_PANEL);
	private static final ModelFactory FLAT_PANE =
		(o, s, m) -> new ContourBoxModel(o, s, m,
			net.createcontour.block.ContourFlatPaneBlock::boxesFor,
			ContourRegistries.CONTOUR_FLAT_PANE);
	private static final ModelFactory CORNER_SLICE =
		(o, s, m) -> new ContourBoxModel(o, s, m,
			net.createcontour.block.ContourCornerSliceBlock::boxesFor,
			ContourRegistries.CONTOUR_CORNER_SLICE);
	private static final ModelFactory BYTE_PANEL =
		(o, s, m) -> new ContourBoxModel(o, s, m,
			net.createcontour.block.ContourBytePanelBlock::boxesFor,
			ContourRegistries.CONTOUR_BYTE_PANEL);
	private static final ModelFactory HALF_LAYER =
		(o, s, m) -> new ContourLayersModel(o, s, m, 1f,
			() -> ContourRegistries.CONTOUR_HALF_LAYER.get(), true);
	private static final ModelFactory SLIDING_DOOR =
		(o, s, m) -> new ContourBoxModel(o, s, m,
			net.createcontour.block.ContourSlidingDoorBlock::boxesFor,
			ContourRegistries.CONTOUR_SLIDING_DOOR);
	private static final ModelFactory FOLDING_DOOR =
		(o, s, m) -> new ContourBoxModel(o, s, m,
			net.createcontour.block.ContourFoldingDoorBlock::boxesFor,
			ContourRegistries.CONTOUR_FOLDING_DOOR);
	private static final ModelFactory CATWALK =
		(o, s, m) -> new ContourBoxModel(o, s, m,
			net.createcontour.block.ContourCatwalkBlock::boxesFor,
			ContourRegistries.CONTOUR_CATWALK);
	private static final ModelFactory IRON_DOOR =
		(o, s, m) -> new ContourBoxModel(o, s, m,
			net.createcontour.block.ContourDoorBlock::boxesFor,
			ContourRegistries.CONTOUR_IRON_DOOR);
	private static final ModelFactory IRON_TRAPDOOR =
		(o, s, m) -> new ContourBoxModel(o, s, m,
			net.createcontour.block.ContourTrapdoorBlock::boxesFor,
			ContourRegistries.CONTOUR_IRON_TRAPDOOR);
	private static final ModelFactory TRAPDOOR =
		(o, s, m) -> new ContourBoxModel(o, s, m,
			net.createcontour.block.ContourTrapdoorBlock::boxesFor,
			ContourRegistries.CONTOUR_TRAPDOOR);
	private static final ModelFactory LADDER =
		(o, s, m) -> new ContourBoxModel(o, s, m,
			net.createcontour.block.ContourLadderBlock::boxesFor,
			ContourRegistries.CONTOUR_LADDER);
	private static final ModelFactory FENCE_GATE =
		(o, s, m) -> new ContourBoxModel(o, s, m,
			net.createcontour.block.ContourFenceGateBlock::boxesFor,
			ContourRegistries.CONTOUR_FENCE_GATE);
	private static final ModelFactory LEVER =
		(o, s, m) -> new ContourBoxModel(o, s, m,
			net.createcontour.block.ContourLeverBlock::boxesFor,
			ContourRegistries.CONTOUR_LEVER);
	private static final ModelFactory DOOR =
		(o, s, m) -> new ContourBoxModel(o, s, m,
			net.createcontour.block.ContourDoorBlock::boxesFor,
			ContourRegistries.CONTOUR_DOOR);
	private static final ModelFactory SLICE =
		(o, s, m) -> new ContourBoxModel(o, s, m,
			net.createcontour.block.ContourSliceBlock::boxesFor,
			ContourRegistries.CONTOUR_SLICE);
	private static final ModelFactory BUTTON =
		(o, s, m) -> new ContourBoxModel(o, s, m,
			net.createcontour.block.ContourButtonBlock::boxesFor,
			ContourRegistries.CONTOUR_BUTTON);
	private static final ModelFactory PRESSURE_PLATE =
		(o, s, m) -> new ContourBoxModel(o, s, m,
			net.createcontour.block.ContourPressurePlateBlock::boxesFor,
			ContourRegistries.CONTOUR_PRESSURE_PLATE);
	private static final ModelFactory GHOST =
		(o, s, m) -> new ContourBoxModel(o, s, m,
			net.createcontour.block.ContourGhostBlock::boxesFor,
			ContourRegistries.CONTOUR_GHOST);
	private static final ModelFactory TORCH =
		(o, s, m) -> new ContourBoxModel(o, s, m,
			net.createcontour.block.ContourTorchBlock::boxesFor,
			ContourRegistries.CONTOUR_TORCH);
	private static final ModelFactory PILLAR =
		(o, s, m) -> new ContourBoxModel(o, s, m,
			net.createcontour.block.ContourPillarBlock::boxesFor,
			ContourRegistries.CONTOUR_PILLAR);
	private static final ModelFactory TRIM =
		(o, s, m) -> new ContourBoxModel(o, s, m,
			net.createcontour.block.ContourTrimBlock::boxesFor,
			ContourRegistries.CONTOUR_TRIM);
	private static final ModelFactory FRAME =
		(o, s, m) -> new ContourBoxModel(o, s, m,
			net.createcontour.block.ContourFrameBlock::boxesFor,
			ContourRegistries.CONTOUR_FRAME);
	private static final ModelFactory LAMP =
		(o, s, m) -> new ContourBoxModel(o, s, m,
			net.createcontour.block.ContourLampBlock::boxesFor,
			ContourRegistries.CONTOUR_LAMP);

	private static ModelFactory connective(
		java.util.function.Supplier<? extends net.createcontour.block.ContourConnectiveBlock> block) {
		return (o, s, m) -> new ContourConnectiveModel(o, s, m, block.get());
	}

	@SubscribeEvent
	public static void onClientSetup(
		net.neoforged.fml.event.lifecycle.FMLClientSetupEvent event) {
		event.enqueueWork(() -> net.createmod.ponder.foundation.PonderIndex
			.addPlugin(new net.createcontour.client.ponder.ContourPonderPlugin()));
	}

	@SubscribeEvent
	public static void onModifyBakingResult(ModelEvent.ModifyBakingResult event) {
		wrapBlock(event, ContourRegistries.CONTOUR_SLAB.get(), SLAB,
			ContourRegistries.CONTOUR_SLAB_ITEM.get());
		wrapBlock(event, ContourRegistries.CONTOUR_VERTICAL_SLAB.get(), VSLAB,
			ContourRegistries.CONTOUR_VERTICAL_SLAB_ITEM.get());
		wrapBlock(event, ContourRegistries.CONTOUR_STAIRS.get(), STAIRS,
			ContourRegistries.CONTOUR_STAIRS_ITEM.get());
		wrapBlock(event, ContourRegistries.CONTOUR_LAYERS.get(), LAYERS,
			ContourRegistries.CONTOUR_LAYERS_ITEM.get());
		wrapBlock(event, ContourRegistries.CONTOUR_POST.get(), POST,
			ContourRegistries.CONTOUR_POST_ITEM.get());
		wrapBlock(event, ContourRegistries.CONTOUR_BYTE.get(), BYTE,
			ContourRegistries.CONTOUR_BYTE_ITEM.get());
		wrapBlock(event, ContourRegistries.CONTOUR_PANEL.get(), PANEL,
			ContourRegistries.CONTOUR_PANEL_ITEM.get());
		wrapBlock(event, ContourRegistries.CONTOUR_WALL.get(),
			connective(ContourRegistries.CONTOUR_WALL),
			ContourRegistries.CONTOUR_WALL_ITEM.get());
		wrapBlock(event, ContourRegistries.CONTOUR_PANE.get(),
			connective(ContourRegistries.CONTOUR_PANE),
			ContourRegistries.CONTOUR_PANE_ITEM.get());
		wrapBlock(event, ContourRegistries.CONTOUR_FENCE.get(),
			connective(ContourRegistries.CONTOUR_FENCE),
			ContourRegistries.CONTOUR_FENCE_ITEM.get());
		wrapBlock(event, ContourRegistries.CONTOUR_SLOPE.get(), SLOPE,
			ContourRegistries.CONTOUR_SLOPE_ITEM.get());
		wrapBlock(event, ContourRegistries.CONTOUR_TRAPDOOR.get(), TRAPDOOR,
			ContourRegistries.CONTOUR_TRAPDOOR_ITEM.get());
		wrapBlock(event, ContourRegistries.CONTOUR_LADDER.get(), LADDER,
			ContourRegistries.CONTOUR_LADDER_ITEM.get());
		wrapBlock(event, ContourRegistries.CONTOUR_FENCE_GATE.get(), FENCE_GATE,
			ContourRegistries.CONTOUR_FENCE_GATE_ITEM.get());
		wrapBlock(event, ContourRegistries.CONTOUR_LEVER.get(), LEVER,
			ContourRegistries.CONTOUR_LEVER_ITEM.get());
		wrapBlock(event, ContourRegistries.CONTOUR_DOOR.get(), DOOR,
			ContourRegistries.CONTOUR_DOOR_ITEM.get());
		wrapBlock(event, ContourRegistries.CONTOUR_SLICE.get(), SLICE,
			ContourRegistries.CONTOUR_SLICE_ITEM.get());
		wrapBlock(event, ContourRegistries.CONTOUR_CORNER_SLOPE.get(),
			ContourCornerSlopeModel::new,
			ContourRegistries.CONTOUR_CORNER_SLOPE_ITEM.get());
		wrapBlock(event, ContourRegistries.CONTOUR_BUTTON.get(), BUTTON,
			ContourRegistries.CONTOUR_BUTTON_ITEM.get());
		wrapBlock(event, ContourRegistries.CONTOUR_INNER_SLOPE.get(),
			ContourInnerSlopeModel::new,
			ContourRegistries.CONTOUR_INNER_SLOPE_ITEM.get());
		wrapBlock(event, ContourRegistries.CONTOUR_PRESSURE_PLATE.get(),
			PRESSURE_PLATE, ContourRegistries.CONTOUR_PRESSURE_PLATE_ITEM.get());
		wrapBlock(event, ContourRegistries.CONTOUR_GHOST.get(), GHOST,
			ContourRegistries.CONTOUR_GHOST_ITEM.get());
		wrapBlock(event, ContourRegistries.CONTOUR_TORCH.get(), TORCH,
			ContourRegistries.CONTOUR_TORCH_ITEM.get());
		wrapBlock(event, ContourRegistries.CONTOUR_ONE_WAY.get(),
			ContourOneWayModel::new,
			ContourRegistries.CONTOUR_ONE_WAY_ITEM.get());
		wrapBlock(event, ContourRegistries.CONTOUR_PILLAR.get(), PILLAR,
			ContourRegistries.CONTOUR_PILLAR_ITEM.get());
		wrapBlock(event, ContourRegistries.CONTOUR_TRIM.get(), TRIM,
			ContourRegistries.CONTOUR_TRIM_ITEM.get());
		wrapBlock(event, ContourRegistries.CONTOUR_FRAME.get(), FRAME,
			ContourRegistries.CONTOUR_FRAME_ITEM.get());
		wrapBlock(event, ContourRegistries.CONTOUR_HALF_SLOPE.get(), HALF_SLOPE,
			ContourRegistries.CONTOUR_HALF_SLOPE_ITEM.get());
		wrapBlock(event, ContourRegistries.CONTOUR_NOTCH.get(), NOTCH,
			ContourRegistries.CONTOUR_NOTCH_ITEM.get());
		wrapBlock(event, ContourRegistries.CONTOUR_ROD.get(), ROD,
			ContourRegistries.CONTOUR_ROD_ITEM.get());
		wrapBlock(event, ContourRegistries.CONTOUR_LATTICE.get(), LATTICE,
			ContourRegistries.CONTOUR_LATTICE_ITEM.get());
		wrapBlock(event, ContourRegistries.CONTOUR_QUARTER_ROUND.get(),
			QUARTER_ROUND, ContourRegistries.CONTOUR_QUARTER_ROUND_ITEM.get());
		wrapBlock(event, ContourRegistries.CONTOUR_CHAIN.get(), CHAIN,
			ContourRegistries.CONTOUR_CHAIN_ITEM.get());
		wrapBlock(event, ContourRegistries.CONTOUR_ROUND_CORNER.get(),
			ROUND_CORNER, ContourRegistries.CONTOUR_ROUND_CORNER_ITEM.get());
		wrapBlock(event, ContourRegistries.CONTOUR_DOME.get(), DOME,
			ContourRegistries.CONTOUR_DOME_ITEM.get());
		wrapBlock(event, ContourRegistries.CONTOUR_RIDGE.get(), RIDGE,
			ContourRegistries.CONTOUR_RIDGE_ITEM.get());
		wrapBlock(event, ContourRegistries.CONTOUR_TUBE.get(), TUBE,
			ContourRegistries.CONTOUR_TUBE_ITEM.get());
		wrapBlock(event, ContourRegistries.CONTOUR_GUTTER.get(), GUTTER,
			ContourRegistries.CONTOUR_GUTTER_ITEM.get());
		wrapBlock(event, ContourRegistries.CONTOUR_BRACKET.get(), BRACKET,
			ContourRegistries.CONTOUR_BRACKET_ITEM.get());
		wrapBlock(event, ContourRegistries.CONTOUR_VERTICAL_STAIRS.get(),
			VERTICAL_STAIRS,
			ContourRegistries.CONTOUR_VERTICAL_STAIRS_ITEM.get());
		wrapBlock(event, ContourRegistries.CONTOUR_ARCH.get(), ARCH,
			ContourRegistries.CONTOUR_ARCH_ITEM.get());
		wrapBlock(event, ContourRegistries.CONTOUR_CYLINDER.get(), CYLINDER,
			ContourRegistries.CONTOUR_CYLINDER_ITEM.get());
		wrapBlock(event, ContourRegistries.CONTOUR_PILASTER.get(), PILASTER,
			ContourRegistries.CONTOUR_PILASTER_ITEM.get());
		wrapBlock(event, ContourRegistries.CONTOUR_STAIR_CORNER.get(),
			STAIR_CORNER,
			ContourRegistries.CONTOUR_STAIR_CORNER_ITEM.get());
		wrapBlock(event, ContourRegistries.CONTOUR_SPHERE.get(), SPHERE,
			ContourRegistries.CONTOUR_SPHERE_ITEM.get());
		wrapBlock(event, ContourRegistries.CONTOUR_CONE.get(), CONE,
			ContourRegistries.CONTOUR_CONE_ITEM.get());
		wrapBlock(event, ContourRegistries.CONTOUR_CAPITAL.get(), CAPITAL,
			ContourRegistries.CONTOUR_CAPITAL_ITEM.get());
		wrapBlock(event, ContourRegistries.CONTOUR_BULLNOSE.get(), BULLNOSE,
			ContourRegistries.CONTOUR_BULLNOSE_ITEM.get());
		wrapBlock(event, ContourRegistries.CONTOUR_CORNICE.get(), CORNICE,
			ContourRegistries.CONTOUR_CORNICE_ITEM.get());
		wrapBlock(event, ContourRegistries.CONTOUR_BALUSTER.get(), BALUSTER,
			ContourRegistries.CONTOUR_BALUSTER_ITEM.get());
		wrapBlock(event, ContourRegistries.CONTOUR_FINIAL.get(), FINIAL,
			ContourRegistries.CONTOUR_FINIAL_ITEM.get());
		wrapBlock(event, ContourRegistries.CONTOUR_SCOTIA.get(), SCOTIA,
			ContourRegistries.CONTOUR_SCOTIA_ITEM.get());
		wrapBlock(event, ContourRegistries.CONTOUR_OGEE.get(), OGEE,
			ContourRegistries.CONTOUR_OGEE_ITEM.get());
		wrapBlock(event, ContourRegistries.CONTOUR_SIGN.get(), SIGN,
			ContourRegistries.CONTOUR_SIGN_ITEM.get());
		wrapBlock(event, ContourRegistries.CONTOUR_TORUS.get(), TORUS,
			ContourRegistries.CONTOUR_TORUS_ITEM.get());
		wrapBlock(event, ContourRegistries.CONTOUR_CRENELLATION.get(),
			CRENELLATION, ContourRegistries.CONTOUR_CRENELLATION_ITEM.get());
		wrapBlock(event, ContourRegistries.CONTOUR_BARREL_VAULT.get(),
			BARREL_VAULT, ContourRegistries.CONTOUR_BARREL_VAULT_ITEM.get());
		wrapBlock(event, ContourRegistries.CONTOUR_COG.get(), COG,
			ContourRegistries.CONTOUR_COG_ITEM.get());
		wrapBlock(event, ContourRegistries.CONTOUR_WHEEL.get(), WHEEL,
			ContourRegistries.CONTOUR_WHEEL_ITEM.get());
		wrapBlock(event, ContourRegistries.CONTOUR_SCAFFOLD.get(), SCAFFOLD,
			ContourRegistries.CONTOUR_SCAFFOLD_ITEM.get());
		wrapBlock(event, ContourRegistries.CONTOUR_GOTHIC_ARCH.get(),
			GOTHIC_ARCH, ContourRegistries.CONTOUR_GOTHIC_ARCH_ITEM.get());
		wrapBlock(event, ContourRegistries.CONTOUR_LOUVER.get(), LOUVER,
			ContourRegistries.CONTOUR_LOUVER_ITEM.get());
		wrapBlock(event, ContourRegistries.CONTOUR_ELBOW.get(), ELBOW,
			ContourRegistries.CONTOUR_ELBOW_ITEM.get());
		wrapBlock(event, ContourRegistries.CONTOUR_HANDRAIL.get(), HANDRAIL,
			ContourRegistries.CONTOUR_HANDRAIL_ITEM.get());
		wrapBlock(event, ContourRegistries.CONTOUR_NEWEL.get(), NEWEL,
			ContourRegistries.CONTOUR_NEWEL_ITEM.get());
		wrapBlock(event, ContourRegistries.CONTOUR_BALUSTRADE.get(), BALUSTRADE,
			ContourRegistries.CONTOUR_BALUSTRADE_ITEM.get());
		wrapBlock(event, ContourRegistries.CONTOUR_I_BEAM.get(), I_BEAM,
			ContourRegistries.CONTOUR_I_BEAM_ITEM.get());
		wrapBlock(event, ContourRegistries.CONTOUR_PORTCULLIS.get(), PORTCULLIS,
			ContourRegistries.CONTOUR_PORTCULLIS_ITEM.get());
		wrapBlock(event, ContourRegistries.CONTOUR_FLANGE.get(), FLANGE,
			ContourRegistries.CONTOUR_FLANGE_ITEM.get());
		wrapBlock(event, ContourRegistries.CONTOUR_BUTTRESS.get(), BUTTRESS,
			ContourRegistries.CONTOUR_BUTTRESS_ITEM.get());
		wrapBlock(event, ContourRegistries.CONTOUR_ARROW_SLIT.get(), ARROW_SLIT,
			ContourRegistries.CONTOUR_ARROW_SLIT_ITEM.get());
		wrapBlock(event, ContourRegistries.CONTOUR_DENTIL.get(), DENTIL,
			ContourRegistries.CONTOUR_DENTIL_ITEM.get());
		wrapBlock(event, ContourRegistries.CONTOUR_TEE.get(), TEE,
			ContourRegistries.CONTOUR_TEE_ITEM.get());
		wrapBlock(event, ContourRegistries.CONTOUR_PALISADE.get(), PALISADE,
			ContourRegistries.CONTOUR_PALISADE_ITEM.get());
		wrapBlock(event, ContourRegistries.CONTOUR_QUOIN.get(), QUOIN,
			ContourRegistries.CONTOUR_QUOIN_ITEM.get());
		wrapBlock(event, ContourRegistries.CONTOUR_TRUSS.get(), TRUSS,
			ContourRegistries.CONTOUR_TRUSS_ITEM.get());
		wrapBlock(event, ContourRegistries.CONTOUR_SILL.get(), SILL,
			ContourRegistries.CONTOUR_SILL_ITEM.get());
		wrapBlock(event, ContourRegistries.CONTOUR_BOLLARD.get(), BOLLARD,
			ContourRegistries.CONTOUR_BOLLARD_ITEM.get());
		wrapBlock(event, ContourRegistries.CONTOUR_MULLION.get(), MULLION,
			ContourRegistries.CONTOUR_MULLION_ITEM.get());
		wrapBlock(event, ContourRegistries.CONTOUR_COFFER.get(), COFFER,
			ContourRegistries.CONTOUR_COFFER_ITEM.get());
		wrapBlock(event, ContourRegistries.CONTOUR_KEYSTONE.get(), KEYSTONE,
			ContourRegistries.CONTOUR_KEYSTONE_ITEM.get());
		wrapBlock(event, ContourRegistries.CONTOUR_PEDESTAL.get(), PEDESTAL,
			ContourRegistries.CONTOUR_PEDESTAL_ITEM.get());
		wrapBlock(event, ContourRegistries.CONTOUR_LANCET.get(), LANCET,
			ContourRegistries.CONTOUR_LANCET_ITEM.get());
		wrapBlock(event, ContourRegistries.CONTOUR_VALLEY.get(), VALLEY,
			ContourRegistries.CONTOUR_VALLEY_ITEM.get());
		wrapBlock(event, ContourRegistries.CONTOUR_CROSS.get(), CROSS,
			ContourRegistries.CONTOUR_CROSS_ITEM.get());
		wrapBlock(event, ContourRegistries.CONTOUR_PIPE_CAP.get(), PIPE_CAP,
			ContourRegistries.CONTOUR_PIPE_CAP_ITEM.get());
		wrapBlock(event, ContourRegistries.CONTOUR_PEDIMENT.get(), PEDIMENT,
			ContourRegistries.CONTOUR_PEDIMENT_ITEM.get());
		wrapBlock(event, ContourRegistries.CONTOUR_CHIMNEY.get(), CHIMNEY,
			ContourRegistries.CONTOUR_CHIMNEY_ITEM.get());
		wrapBlock(event, ContourRegistries.CONTOUR_HALF_TIMBER.get(), HALF_TIMBER,
			ContourRegistries.CONTOUR_HALF_TIMBER_ITEM.get());
		wrapBlock(event, ContourRegistries.CONTOUR_SPANDREL.get(), SPANDREL,
			ContourRegistries.CONTOUR_SPANDREL_ITEM.get());
		wrapBlock(event, ContourRegistries.CONTOUR_ARC_WALL.get(), ARC_WALL,
			ContourRegistries.CONTOUR_ARC_WALL_ITEM.get());
		wrapBlock(event, ContourRegistries.CONTOUR_ROSETTE.get(), ROSETTE,
			ContourRegistries.CONTOUR_ROSETTE_ITEM.get());
		wrapBlock(event, ContourRegistries.CONTOUR_FLYING_BUTTRESS.get(),
			FLYING_BUTTRESS,
			ContourRegistries.CONTOUR_FLYING_BUTTRESS_ITEM.get());
		wrapBlock(event, ContourRegistries.CONTOUR_COPING.get(), COPING,
			ContourRegistries.CONTOUR_COPING_ITEM.get());
		wrapBlock(event, ContourRegistries.CONTOUR_OCULUS.get(), OCULUS,
			ContourRegistries.CONTOUR_OCULUS_ITEM.get());
		wrapBlock(event, ContourRegistries.CONTOUR_AWNING.get(), AWNING,
			ContourRegistries.CONTOUR_AWNING_ITEM.get());
		wrapBlock(event, ContourRegistries.CONTOUR_BUFFER.get(), BUFFER,
			ContourRegistries.CONTOUR_BUFFER_ITEM.get());
		wrapBlock(event, ContourRegistries.CONTOUR_SMOKESTACK.get(), SMOKESTACK,
			ContourRegistries.CONTOUR_SMOKESTACK_ITEM.get());
		wrapBlock(event, ContourRegistries.CONTOUR_SEMAPHORE.get(), SEMAPHORE,
			ContourRegistries.CONTOUR_SEMAPHORE_ITEM.get());
		wrapBlock(event, ContourRegistries.CONTOUR_PILOT.get(), PILOT,
			ContourRegistries.CONTOUR_PILOT_ITEM.get());
		wrapBlock(event, ContourRegistries.CONTOUR_VERTICAL_STEP.get(), VERTICAL_STEP,
			ContourRegistries.CONTOUR_VERTICAL_STEP_ITEM.get());
		wrapBlock(event, ContourRegistries.CONTOUR_BEAM.get(), BEAM,
			ContourRegistries.CONTOUR_BEAM_ITEM.get());
		wrapBlock(event, ContourRegistries.CONTOUR_HALF_PANEL.get(), HALF_PANEL,
			ContourRegistries.CONTOUR_HALF_PANEL_ITEM.get());
		wrapBlock(event, ContourRegistries.CONTOUR_FLAT_PANE.get(), FLAT_PANE,
			ContourRegistries.CONTOUR_FLAT_PANE_ITEM.get());
		wrapBlock(event, ContourRegistries.CONTOUR_CORNER_SLICE.get(), CORNER_SLICE,
			ContourRegistries.CONTOUR_CORNER_SLICE_ITEM.get());
		wrapBlock(event, ContourRegistries.CONTOUR_BYTE_PANEL.get(), BYTE_PANEL,
			ContourRegistries.CONTOUR_BYTE_PANEL_ITEM.get());
		wrapBlock(event, ContourRegistries.CONTOUR_HALF_LAYER.get(), HALF_LAYER,
			ContourRegistries.CONTOUR_HALF_LAYER_ITEM.get());
		wrapBlock(event, ContourRegistries.CONTOUR_IRON_DOOR.get(), IRON_DOOR,
			ContourRegistries.CONTOUR_IRON_DOOR_ITEM.get());
		wrapBlock(event, ContourRegistries.CONTOUR_IRON_TRAPDOOR.get(), IRON_TRAPDOOR,
			ContourRegistries.CONTOUR_IRON_TRAPDOOR_ITEM.get());
		wrapBlock(event, ContourRegistries.CONTOUR_SLIDING_DOOR.get(), SLIDING_DOOR,
			ContourRegistries.CONTOUR_SLIDING_DOOR_ITEM.get());
		wrapBlock(event, ContourRegistries.CONTOUR_FOLDING_DOOR.get(), FOLDING_DOOR,
			ContourRegistries.CONTOUR_FOLDING_DOOR_ITEM.get());
		wrapBlock(event, ContourRegistries.CONTOUR_CATWALK.get(), CATWALK,
			ContourRegistries.CONTOUR_CATWALK_ITEM.get());
		wrapBlock(event, ContourRegistries.CONTOUR_PYRAMID.get(),
			ContourPyramidModel::new,
			ContourRegistries.CONTOUR_PYRAMID_ITEM.get());
		wrapBlock(event, ContourRegistries.CONTOUR_LAMP.get(), LAMP,
			ContourRegistries.CONTOUR_LAMP_ITEM.get());
	}

	private static void wrapBlock(ModelEvent.ModifyBakingResult event,
		Block block, ModelFactory factory, net.minecraft.world.item.Item item) {
		for (BlockState state : block.getStateDefinition().getPossibleStates()) {
			ModelResourceLocation mrl = BlockModelShaper.stateToModelLocation(state);
			event.getModels().computeIfPresent(mrl,
				(k, original) -> factory.create(original, state, null));
		}
		ModelResourceLocation inv = ModelResourceLocation.inventory(
			ResourceLocation.fromNamespaceAndPath("create_contour",
				net.minecraft.core.registries.BuiltInRegistries.ITEM
					.getKey(item).getPath()));
		event.getModels().computeIfPresent(inv, (k, original) -> {
			ContourModel wrapped = factory.create(original,
				block.defaultBlockState(), null);
			wrapped.setOverrides(new ContourItemOverrides(original,
				block.defaultBlockState(),
				(o, s, m) -> factory.create(o, s, m)));
			return wrapped;
		});
	}

	@SubscribeEvent
	public static void onRegisterRenderers(
		net.neoforged.neoforge.client.event.EntityRenderersEvent.RegisterRenderers event) {
		event.registerBlockEntityRenderer(ContourRegistries.CONTOUR_SIGN_BE.get(),
			ContourSignRenderer::new);
	}

	@SubscribeEvent
	public static void onBlockColors(RegisterColorHandlersEvent.Block event) {
		event.register((state, level, pos, tint) -> {
			try {
				if (level != null && pos != null
					&& level.getBlockEntity(pos) instanceof ContourBlockEntity be) {
					BlockState mat = be.getMaterial();
					if (mat != null && MaterialSafety.isAccepted(
						MaterialSafety.normalize(mat)))
						return Minecraft.getInstance().getBlockColors()
							.getColor(mat, level, pos, tint);
				}
			} catch (RuntimeException e) {
				// fall through to white
			}
			return -1; // white / no tint
		}, ContourRegistries.CONTOUR_SLAB.get(),
			ContourRegistries.CONTOUR_VERTICAL_SLAB.get(),
			ContourRegistries.CONTOUR_STAIRS.get(),
			ContourRegistries.CONTOUR_LAYERS.get(),
			ContourRegistries.CONTOUR_POST.get(),
			ContourRegistries.CONTOUR_BYTE.get(),
			ContourRegistries.CONTOUR_PANEL.get(),
			ContourRegistries.CONTOUR_WALL.get(),
			ContourRegistries.CONTOUR_PANE.get(),
			ContourRegistries.CONTOUR_FENCE.get(),
			ContourRegistries.CONTOUR_SLOPE.get(),
			ContourRegistries.CONTOUR_TRAPDOOR.get(),
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
			ContourRegistries.CONTOUR_LAMP.get());
	}

	@SubscribeEvent
	public static void onItemColors(RegisterColorHandlersEvent.Item event) {
		event.register((stack, tint) -> {
			try {
				Minecraft mc = Minecraft.getInstance();
				if (mc.level == null)
					return -1;
				BlockState mat = ContourBlockItem.storedMaterial(
					mc.level.registryAccess(), stack);
				if (mat == null)
					return -1;
				return mc.getItemColors().getColor(
					new net.minecraft.world.item.ItemStack(mat.getBlock().asItem()), tint);
			} catch (RuntimeException e) {
				return -1;
			}
		}, ContourRegistries.CONTOUR_SLAB_ITEM.get(),
			ContourRegistries.CONTOUR_VERTICAL_SLAB_ITEM.get(),
			ContourRegistries.CONTOUR_STAIRS_ITEM.get(),
			ContourRegistries.CONTOUR_LAYERS_ITEM.get(),
			ContourRegistries.CONTOUR_POST_ITEM.get(),
			ContourRegistries.CONTOUR_BYTE_ITEM.get(),
			ContourRegistries.CONTOUR_PANEL_ITEM.get(),
			ContourRegistries.CONTOUR_WALL_ITEM.get(),
			ContourRegistries.CONTOUR_PANE_ITEM.get(),
			ContourRegistries.CONTOUR_FENCE_ITEM.get(),
			ContourRegistries.CONTOUR_SLOPE_ITEM.get(),
			ContourRegistries.CONTOUR_TRAPDOOR_ITEM.get(),
			ContourRegistries.CONTOUR_LADDER_ITEM.get(),
			ContourRegistries.CONTOUR_FENCE_GATE_ITEM.get(),
			ContourRegistries.CONTOUR_LEVER_ITEM.get(),
			ContourRegistries.CONTOUR_DOOR_ITEM.get(),
			ContourRegistries.CONTOUR_SLICE_ITEM.get(),
			ContourRegistries.CONTOUR_CORNER_SLOPE_ITEM.get(),
			ContourRegistries.CONTOUR_BUTTON_ITEM.get(),
			ContourRegistries.CONTOUR_INNER_SLOPE_ITEM.get(),
			ContourRegistries.CONTOUR_PRESSURE_PLATE_ITEM.get(),
			ContourRegistries.CONTOUR_GHOST_ITEM.get(),
			ContourRegistries.CONTOUR_TORCH_ITEM.get(),
			ContourRegistries.CONTOUR_ONE_WAY_ITEM.get(),
			ContourRegistries.CONTOUR_PILLAR_ITEM.get(),
			ContourRegistries.CONTOUR_TRIM_ITEM.get(),
			ContourRegistries.CONTOUR_FRAME_ITEM.get(),
			ContourRegistries.CONTOUR_PYRAMID_ITEM.get(),
			ContourRegistries.CONTOUR_LAMP_ITEM.get());
	}

	// --- opt-in render smoke (-Dcreate_contour.renderSmoke=true) ---

	@net.neoforged.fml.common.EventBusSubscriber(
		modid = "create_contour", value = Dist.CLIENT)
	public static final class RenderSmoke {

		private static boolean ran;
		private static int waited;
		private static final BlockPos QA_SYNC_POS = new BlockPos(109, -60, 112);

		@net.neoforged.bus.api.SubscribeEvent
		public static void onClientTick(
			net.neoforged.neoforge.client.event.ClientTickEvent.Post event) {
			if (!Boolean.getBoolean("create_contour.renderSmoke")
				|| Minecraft.getInstance().level == null
				|| Minecraft.getInstance().player == null)
				return;
			if (visualStage > 0 && visualStage < GALLERY_LAST_STAGE) {
				if (++visualTicks > (visualStage == 1 ? 60 : 30)) {
					visualTicks = 0;
					visualStage++;
				} else {
					return;
				}
				switch (visualStage) {
					case 2 -> {
						grab("wave-visual-a");
						moveCam(101.5, -57.0, 133.0, 180f, 15f);
					}
					case 3 -> {
						grab("wave-visual-b");
						moveCam(122.5, -57.0, 140.0, 180f, 15f);
					}
					case 4 -> {
						grab("wave-visual-c");
						moveCam(110.5, -52.0, 118.0, 0f, 25f);
					}
					case 5 -> {
						grab("wave-visual-d");
						moveCam(122.5, -58.5, 137.0, 0f, 20f);
					}
					case 6 -> {
						grab("wave-visual-e");
						moveCam(117.5, -58.0, 143.0, 0f, 15f);
					}
					case 7 -> {
						grab("wave-visual-f");
						moveCam(105.5, -57.0, 174.0, 180f, 12f);
					}
					case 8 -> {
						grab("wave-visual-g");
						com.mojang.logging.LogUtils.getLogger()
							.info("[contour-smoke] visual captures done");
					}
					case 9 -> {
						buildContinuationGallery(Minecraft.getInstance());
						frameGallery(0);
					}
					case 10, 11, 12, 13, 14, 15, 16 -> {
						int shot = visualStage - GALLERY_SHOT_STAGE;
						if (shot < GALLERY_NAMES.length) {
							grab("continuum-" + GALLERY_NAMES[shot]);
							if (shot + 1 < GALLERY_NAMES.length)
								frameGallery(shot + 1);
							else
								com.mojang.logging.LogUtils.getLogger().info(
									"[contour-smoke] continuation gallery done groups={}",
									GALLERY_NAMES.length);
						}
					}
					default -> {
					}
				}
				return;
			}
			if (visualStage >= GALLERY_LAST_STAGE && ponderOpenStage >= 0) {
				ponderOpen(com.mojang.logging.LogUtils.getLogger());
				return;
			}
			if (ran)
				return;
			// wait until the live-sync fixture BE exists (bounded 200 ticks)
			if (!(Minecraft.getInstance().level.getBlockEntity(QA_SYNC_POS)
				instanceof ContourBlockEntity)) {
				if (++waited > 200) {
					ran = true;
					com.mojang.logging.LogUtils.getLogger().error(
						"[contour-smoke] FAIL fixture BE missing at {}", QA_SYNC_POS);
					throw new IllegalStateException(
						"[contour-smoke] fixture BE missing");
				}
				return;
			}
			ran = true;
			var log = com.mojang.logging.LogUtils.getLogger();
			log.info("[contour-smoke] START");
			ponderSmoke(log);
			run();
			log.info("[contour-smoke] COMPLETE passed={} failed={} total={}",
				passed, failed, passed + failed);
			if (failed > 0 && Boolean.getBoolean("create_contour.renderSmoke"))
				throw new IllegalStateException(
					"[contour-smoke] " + failed + " failures");
			buildVisualScene(Minecraft.getInstance());
			visualStage = 1;
		}

		private static void ponderSmoke(org.slf4j.Logger log) {
			try {
				var access = net.createmod.ponder.foundation.PonderIndex
					.getSceneAccess();
				boolean placer = access.doScenesExistForId(
					ContourRegistries.CONTOUR_PLACER.getId());
				boolean blank = access.doScenesExistForId(
					ContourRegistries.CONTOUR_BLANK_ITEM.getId());
				boolean nbt = net.createmod.ponder.foundation.registration
					.PonderSceneRegistry.loadSchematic(ResourceLocation
						.fromNamespaceAndPath("create_contour", "placer"))
					!= null;
				log.info("[contour-smoke] ponder placer={} blank={} nbt={}",
					placer, blank, nbt);
				if (!placer || !blank || !nbt)
					failed++;
				else
					passed++;
			} catch (Throwable t) {
				failed++;
				log.error("[contour-smoke] ponder check threw", t);
			}
		}

		private static int ponderOpenStage;

		private static void ponderOpen(org.slf4j.Logger log) {
			Minecraft mc = Minecraft.getInstance();
			if (ponderOpenStage == 0) {
				try {
					mc.setScreen(net.createmod.ponder.foundation.ui.PonderUI
						.of(new net.minecraft.world.item.ItemStack(
							ContourRegistries.CONTOUR_PLACER.get())));
					ponderOpenStage = 1;
				} catch (Throwable t) {
					failed++;
					ponderOpenStage = -1;
					log.error("[contour-smoke] ponder OPEN threw", t);
				}
			} else if (ponderOpenStage == 1) {
				if (mc.screen instanceof net.createmod.ponder.foundation.ui.PonderUI) {
					log.info("[contour-smoke] ponder OPEN ok");
					passed++;
				} else {
					failed++;
					log.error("[contour-smoke] ponder screen not open: {}",
						mc.screen);
				}
				mc.setScreen(null);
				ponderOpenStage = -1;
			}
		}

	private static int visualStage, visualTicks;

	// Continuation gallery (ADR-017): the stage chain is explicit and
	// sequential - stages 1..8 are the wave-visual captures, stage 9
	// builds the gallery and frames group 0, stages 10..16 grab
	// "continuum-<name>" once per mate-pair group, then the ponder-open
	// stage runs once visualStage >= GALLERY_LAST_STAGE.
	private static final BlockPos GALLERY_BASE = new BlockPos(140, -60, 112);
	private static final String[] GALLERY_NAMES = {
		"slope-slab", "byte-pair", "panel-stone", "stairs-slab",
		"round-slope", "panel-bytes", "vslab-byte"};
	private static final int GALLERY_SHOT_STAGE = 10;
	private static final int GALLERY_LAST_STAGE =
		GALLERY_SHOT_STAGE + GALLERY_NAMES.length - 1;

	/** Frames mate-pair group i: SSE of the pair, looking north-west.
	 *  The feet y is just above the gallery floor so the shot works
	 *  whether the player lands or hovers. */
	private static void frameGallery(int i) {
		moveCam(GALLERY_BASE.getX() + i * 4 + 2.0,
			GALLERY_BASE.getY() + 1.0, GALLERY_BASE.getZ() + 5.0,
			160f, 15f);
	}

	/** Air-clear then place: a persisted identical blockstate would
	 *  early-return and skip BE creation. */
	private static void put(net.minecraft.world.level.Level level,
		BlockPos pos, BlockState s) {
		level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
		level.setBlock(pos, s, 3);
	}

	private static ContourBlockEntity cbeAt(
		net.minecraft.world.level.Level level, BlockPos pos) {
		var be = level.getChunkAt(pos) instanceof
			net.minecraft.world.level.chunk.LevelChunk lc
			? lc.getBlockEntity(pos,
				net.minecraft.world.level.chunk.LevelChunk.EntityCreationType.IMMEDIATE)
			: level.getBlockEntity(pos);
		return be instanceof ContourBlockEntity cbe ? cbe : null;
	}

	/** Paint a placed shape; facing/top mirror what setPlacedBy would
	 *  store as the canonical orientation for that state. */
	private static void paintAt(net.minecraft.world.level.Level level,
		BlockPos pos, BlockState mat, Direction facing, boolean top) {
		var cbe = cbeAt(level, pos);
		if (cbe != null)
			cbe.applyPayload(mat, 1, facing, top);
	}

	private static void moveCam(double x, double y, double z, float yaw,
		float pitch) {
		Minecraft.getInstance().player.moveTo(x, y, z, yaw, pitch);
		Minecraft.getInstance().player.setYHeadRot(yaw);
	}

	/** One wide + one close shot of the built scene. */
	private static void grab(String name) {
		Minecraft mc = Minecraft.getInstance();
		java.io.File dir = new java.io.File(mc.gameDirectory, "screenshots");
		dir.mkdirs();
		java.io.File out = new java.io.File(dir, name + ".png");
		try (com.mojang.blaze3d.platform.NativeImage img =
				net.minecraft.client.Screenshot
					.takeScreenshot(mc.getMainRenderTarget())) {
			img.writeToFile(out);
			com.mojang.logging.LogUtils.getLogger()
				.info("[contour-smoke] shot {} -> {}", name, out);
		} catch (Exception e) {
			com.mojang.logging.LogUtils.getLogger()
				.error("[contour-smoke] shot {} failed", name, e);
		}
	}

	/** Builds the catalog scene on the integrated server: one cell per
	 *  shape, painted with a cycling material so the texture pipeline is
	 *  exercised visually. */
	private static void buildVisualScene(Minecraft mc) {
		var server = mc.getSingleplayerServer();
		if (server == null)
			return;
		var level = server.overworld();
		BlockPos base = new BlockPos(94, -60, 126);
		var mats = new BlockState[]{
			Blocks.STONE.defaultBlockState(),
			Blocks.OAK_LOG.defaultBlockState().setValue(
				net.minecraft.world.level.block.RotatedPillarBlock.AXIS,
				Direction.Axis.Y),
			Blocks.GLASS.defaultBlockState(),
			net.minecraft.core.registries.BuiltInRegistries.BLOCK.get(
				ResourceLocation.fromNamespaceAndPath("create",
					"andesite_casing")).defaultBlockState(),
			Blocks.OAK_LEAVES.defaultBlockState().setValue(
				net.minecraft.world.level.block.LeavesBlock.PERSISTENT, true)};
		Block[] shapes = {ContourRegistries.CONTOUR_SLAB.get(),
			ContourRegistries.CONTOUR_VERTICAL_SLAB.get(),
			ContourRegistries.CONTOUR_STAIRS.get(),
			ContourRegistries.CONTOUR_LAYERS.get(),
			ContourRegistries.CONTOUR_POST.get(),
			ContourRegistries.CONTOUR_BYTE.get(),
			ContourRegistries.CONTOUR_PANEL.get(),
			ContourRegistries.CONTOUR_WALL.get(),
			ContourRegistries.CONTOUR_PANE.get(),
			ContourRegistries.CONTOUR_FENCE.get(),
			ContourRegistries.CONTOUR_SLOPE.get(),
			ContourRegistries.CONTOUR_TRAPDOOR.get(),
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
		// platform: sized to the catalog so every row lands on stone
		int sceneRows = (shapes.length + 10) / 11;
		for (int x = 0; x < 33; x++)
			for (int z = 0; z < sceneRows * 6 + 2; z++)
				level.setBlock(base.offset(x - 1, -1, z - 1),
					Blocks.STONE.defaultBlockState(), 3);
		for (int i = 0; i < shapes.length; i++) {
			int col = i % 11, row = i / 11;
			BlockPos pos = base.offset(col * 3, 0, row * 6);
			BlockState s = shapes[i].defaultBlockState();
			if (shapes[i] == ContourRegistries.CONTOUR_SIGN.get())
				s = s.setValue(net.createcontour.block.ContourSignBlock.FACING,
					Direction.SOUTH);
			if (shapes[i] == ContourRegistries.CONTOUR_CRENELLATION.get())
				s = s.setValue(
					net.createcontour.block.ContourCrenellationBlock.FACING,
					Direction.SOUTH);
			if (shapes[i] == ContourRegistries.CONTOUR_WHEEL.get())
				s = s.setValue(net.createcontour.block.ContourWheelBlock.AXIS,
					Direction.Axis.X);
			// force a real state transition: a persisted identical
			// blockstate would early-return without re-creating its BE
			level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
			if (shapes[i] == ContourRegistries.CONTOUR_LAMP.get())
				s = s.setValue(ContourLampBlock.LIT, row == 2);
			level.setBlock(pos, s, 3);
			if (shapes[i] == ContourRegistries.CONTOUR_DOOR.get())
				level.setBlock(pos.above(), s.setValue(ContourDoorBlock.HALF,
					net.minecraft.world.level.block.state.properties.DoubleBlockHalf.UPPER), 3);
			// connective shapes show an arm with a neighbor to the east
			if (shapes[i] == ContourRegistries.CONTOUR_WALL.get()
				|| shapes[i] == ContourRegistries.CONTOUR_PANE.get()
				|| shapes[i] == ContourRegistries.CONTOUR_FENCE.get())
				level.setBlock(pos.east(), s, 3);
			var be = level.getChunkAt(pos) instanceof net.minecraft.world.level.chunk.LevelChunk lc
				? lc.getBlockEntity(pos, net.minecraft.world.level.chunk.LevelChunk.EntityCreationType.IMMEDIATE)
				: level.getBlockEntity(pos);
			if (be instanceof ContourBlockEntity cbe)
				cbe.applyPayload(mats[i % mats.length], 1, Direction.NORTH, false);
			if (be instanceof net.createcontour.blockentity.ContourSignBlockEntity sbe) {
				sbe.applyPayload(Blocks.WHITE_CONCRETE.defaultBlockState(),
					1, Direction.NORTH, false);
				sbe.setLines(new String[]{"CONTOUR", "SIGNS", "WORK", ""});
			}
			var be2 = level.getBlockEntity(pos.east());
			if (be2 instanceof ContourBlockEntity cbe2
				&& (shapes[i] == ContourRegistries.CONTOUR_WALL.get()
					|| shapes[i] == ContourRegistries.CONTOUR_PANE.get()
					|| shapes[i] == ContourRegistries.CONTOUR_FENCE.get()))
				cbe2.applyPayload(mats[i % mats.length], 1, Direction.NORTH,
					false);
			if (shapes[i] == ContourRegistries.CONTOUR_DOOR.get()) {
				var ub = level.getBlockEntity(pos.above());
				if (ub instanceof ContourBlockEntity cub)
					cub.applyPayload(mats[i % mats.length], 0, Direction.NORTH,
						false);
			}
		}
		// camera spot: south of scene looking north at the grid
		moveCam(110.5, -50.0, 172.0, 180f, 20f);
		com.mojang.logging.LogUtils.getLogger()
			.info("[contour-smoke] visual scene built at {}", base);
	}

	/** ADR-017 continuation gallery: a row of mate-pair groups east of
	 *  the catalog scene (x >= 140 stays clear of the platform ending
	 *  near x = 127 and the sync fixture at x = 109). Each group is
	 *  painted a single material so the rendered pair should read as
	 *  one continuous surface - the shots are the acceptance evidence. */
	private static void buildContinuationGallery(Minecraft mc) {
		var server = mc.getSingleplayerServer();
		if (server == null)
			return;
		var level = server.overworld();
		var stone = Blocks.STONE.defaultBlockState();
		var planks = Blocks.OAK_PLANKS.defaultBlockState();
		var casing = net.minecraft.core.registries.BuiltInRegistries.BLOCK
			.get(ResourceLocation.fromNamespaceAndPath("create",
				"andesite_casing")).defaultBlockState();
		var bricks = Blocks.BRICKS.defaultBlockState();
		var concrete = Blocks.WHITE_CONCRETE.defaultBlockState();
		// clean stage + stone floor under the whole row
		for (int x = -2; x <= GALLERY_NAMES.length * 4 + 1; x++)
			for (int z = -2; z <= 5; z++) {
				level.setBlock(GALLERY_BASE.offset(x, -1, z), stone, 3);
				for (int y = 0; y <= 3; y++)
					level.setBlock(GALLERY_BASE.offset(x, y, z),
						Blocks.AIR.defaultBlockState(), 3);
			}

		// slope -> top slab: ramp tip meets the slab's upper ledge.
		BlockPos g = GALLERY_BASE;
		put(level, g, ContourRegistries.CONTOUR_SLOPE.get().defaultBlockState()
			.setValue(net.createcontour.block.ContourSlopeBlock.FACING,
				Direction.SOUTH)
			.setValue(net.createcontour.block.ContourSlopeBlock.HALF,
				net.minecraft.world.level.block.state.properties.Half.BOTTOM));
		put(level, g.south(), ContourRegistries.CONTOUR_SLAB.get()
			.defaultBlockState().setValue(ContourSlabBlock.TYPE,
				net.minecraft.world.level.block.state.properties.SlabType.TOP));
		paintAt(level, g, stone, Direction.SOUTH, false);
		paintAt(level, g.south(), stone, Direction.NORTH, true);

		// four bytes in one cell compose a bottom slab (BYTE_EXTRAS);
		// a real bottom slab continues the same surface east.
		g = GALLERY_BASE.offset(4, 0, 0);
		put(level, g, ContourRegistries.CONTOUR_BYTE.get().defaultBlockState()
			.setValue(net.createcontour.block.ContourByteBlock.OCTANT,
				net.createcontour.block.ContourByteBlock.Octant.X0Y0Z0));
		var byteBe = cbeAt(level, g);
		if (byteBe != null) {
			byteBe.applyPayload(planks, 1, Direction.NORTH, false);
			byteBe.addByteOccupant(
				net.createcontour.block.ContourByteBlock.Octant.X1Y0Z0.ordinal(),
				planks, 1);
			byteBe.addByteOccupant(
				net.createcontour.block.ContourByteBlock.Octant.X0Y0Z1.ordinal(),
				planks, 1);
			byteBe.addByteOccupant(
				net.createcontour.block.ContourByteBlock.Octant.X1Y0Z1.ordinal(),
				planks, 1);
		}
		put(level, g.east(), ContourRegistries.CONTOUR_SLAB.get()
			.defaultBlockState().setValue(ContourSlabBlock.TYPE,
				net.minecraft.world.level.block.state.properties.SlabType.BOTTOM));
		paintAt(level, g.east(), planks, Direction.NORTH, false);

		// face panel -> vanilla block: coplanar tops across the
		// Contour<->vanilla seam.
		g = GALLERY_BASE.offset(8, 0, 0);
		put(level, g, ContourRegistries.CONTOUR_PANEL.get().defaultBlockState()
			.setValue(net.createcontour.block.ContourPanelBlock.POSITION,
				net.createcontour.block.ContourPanelBlock.PanelPos.FACE_UP));
		put(level, g.east(), stone);
		paintAt(level, g, stone, Direction.NORTH, false);

		// stairs -> top slab: the upper step continues into a landing.
		g = GALLERY_BASE.offset(12, 0, 0);
		put(level, g, ContourRegistries.CONTOUR_STAIRS.get().defaultBlockState()
			.setValue(ContourStairsBlock.FACING, Direction.SOUTH)
			.setValue(ContourStairsBlock.HALF,
				net.minecraft.world.level.block.state.properties.Half.BOTTOM)
			.setValue(ContourStairsBlock.STAIRS_SHAPE,
				net.minecraft.world.level.block.state.properties.StairsShape.STRAIGHT));
		put(level, g.south(), ContourRegistries.CONTOUR_SLAB.get()
			.defaultBlockState().setValue(ContourSlabBlock.TYPE,
				net.minecraft.world.level.block.state.properties.SlabType.TOP));
		paintAt(level, g, casing, Direction.SOUTH, false);
		paintAt(level, g.south(), casing, Direction.NORTH, true);

		// slope -> quarter round: the ramp runs into the lathe form's
		// solid cap face (round wraps the up+north edge at the seam).
		g = GALLERY_BASE.offset(16, 0, 0);
		put(level, g, ContourRegistries.CONTOUR_SLOPE.get().defaultBlockState()
			.setValue(net.createcontour.block.ContourSlopeBlock.FACING,
				Direction.SOUTH)
			.setValue(net.createcontour.block.ContourSlopeBlock.HALF,
				net.minecraft.world.level.block.state.properties.Half.BOTTOM));
		put(level, g.south(), ContourRegistries.CONTOUR_QUARTER_ROUND.get()
			.defaultBlockState()
			.setValue(net.createcontour.block.ContourQuarterRoundBlock.EDGE,
				net.createcontour.block.ContourTrimBlock.Edge.UN));
		paintAt(level, g, bricks, Direction.SOUTH, false);
		paintAt(level, g.south(), bricks, Direction.NORTH, false);

		// half panel -> byte panel on the same top plane: the half-face
		// plate continues into a face quadrant across the boundary.
		g = GALLERY_BASE.offset(20, 0, 0);
		put(level, g, ContourRegistries.CONTOUR_HALF_PANEL.get()
			.defaultBlockState()
			.setValue(net.createcontour.block.ContourHalfPanelBlock.FACE,
				Direction.UP)
			.setValue(net.createcontour.block.ContourHalfPanelBlock.HALF,
				Direction.SOUTH));
		put(level, g.south(), ContourRegistries.CONTOUR_BYTE_PANEL.get()
			.defaultBlockState()
			.setValue(net.createcontour.block.ContourBytePanelBlock.FACING,
				Direction.UP)
			.setValue(net.createcontour.block.ContourBytePanelBlock.QUADRANT, 0));
		paintAt(level, g, concrete, Direction.NORTH, false);
		paintAt(level, g.south(), concrete, Direction.NORTH, false);

		// vertical slab -> byte column: four bytes continue the
		// half-wall one cell further (BYTE_EXTRAS a second time).
		g = GALLERY_BASE.offset(24, 0, 0);
		put(level, g, ContourRegistries.CONTOUR_VERTICAL_SLAB.get()
			.defaultBlockState().setValue(ContourVerticalSlabBlock.FACING,
				Direction.SOUTH));
		put(level, g.south(), ContourRegistries.CONTOUR_BYTE.get()
			.defaultBlockState()
			.setValue(net.createcontour.block.ContourByteBlock.OCTANT,
				net.createcontour.block.ContourByteBlock.Octant.X0Y0Z0));
		var wallBe = cbeAt(level, g.south());
		if (wallBe != null) {
			wallBe.applyPayload(casing, 1, Direction.NORTH, false);
			wallBe.addByteOccupant(
				net.createcontour.block.ContourByteBlock.Octant.X1Y0Z0.ordinal(),
				casing, 1);
			wallBe.addByteOccupant(
				net.createcontour.block.ContourByteBlock.Octant.X0Y1Z0.ordinal(),
				casing, 1);
			wallBe.addByteOccupant(
				net.createcontour.block.ContourByteBlock.Octant.X1Y1Z0.ordinal(),
				casing, 1);
		}
		paintAt(level, g, casing, Direction.SOUTH, false);
		com.mojang.logging.LogUtils.getLogger().info(
			"[contour-smoke] continuation gallery built at {} groups={}",
			GALLERY_BASE, GALLERY_NAMES.length);
	}

		private static int passed, failed;

		private static List<net.minecraft.client.renderer.block.model.BakedQuad> allQuads(
			BakedModel m, net.minecraft.world.level.block.state.BlockState s,
			ModelData d) {
			java.util.ArrayList<net.minecraft.client.renderer.block.model.BakedQuad> out =
				new java.util.ArrayList<>();
			out.addAll(m.getQuads(s, null, RandomSource.create(42), d, null));
			for (Direction dir : Direction.values())
				out.addAll(m.getQuads(s, dir, RandomSource.create(42), d, null));
			return out;
		}

		private static void checkGeometry(net.minecraft.client.renderer.block.model.BakedQuad q) {
			int[] verts = q.getVertices();
			org.joml.Vector3f[] p = new org.joml.Vector3f[4];
			for (int i = 0; i < 4; i++)
				p[i] = new org.joml.Vector3f(
					Float.intBitsToFloat(verts[i * 8]),
					Float.intBitsToFloat(verts[i * 8 + 1]),
					Float.intBitsToFloat(verts[i * 8 + 2]));
			for (org.joml.Vector3f v : p)
				check("finite-01", () -> {
					boolean ok = v.x() >= -1e-4 && v.x() <= 1.0001
						&& v.y() >= -1e-4 && v.y() <= 1.0001
						&& v.z() >= -1e-4 && v.z() <= 1.0001;
					if (!ok)
						report("finite-01", "vert=" + v);
					return ok;
				});
			org.joml.Vector3f e1 = new org.joml.Vector3f(p[1]).sub(p[0]);
			org.joml.Vector3f e2 = new org.joml.Vector3f(p[2]).sub(p[0]);
			org.joml.Vector3f e3 = new org.joml.Vector3f(p[3]).sub(p[0]);
			org.joml.Vector3f n1 = new org.joml.Vector3f(e1).cross(e2);
			org.joml.Vector3f n2 = new org.joml.Vector3f(e2).cross(e3);
			float dx = q.getDirection().getStepX(), dy = q.getDirection().getStepY(),
				dz = q.getDirection().getStepZ();
			check("wind1", () -> {
				boolean ok = n1.x() * dx + n1.y() * dy + n1.z() * dz > 0;
				if (!ok)
					report("wind1", "dir=" + q.getDirection() + " p0=" + p[0] + " p1="
						+ p[1] + " p2=" + p[2] + " p3=" + p[3]);
				return ok;
			});
			check("wind2", () -> {
				boolean ok = n2.x() * dx + n2.y() * dy + n2.z() * dz > 0;
				if (!ok)
					report("wind2", "dir=" + q.getDirection() + " p0=" + p[0] + " p1="
						+ p[1] + " p2=" + p[2] + " p3=" + p[3]);
				return ok;
			});
		}

		private static void run() {
			RandomSource rand = RandomSource.create(42);
			Minecraft mc = Minecraft.getInstance();
			Block[] blocks = {ContourRegistries.CONTOUR_SLAB.get(),
				ContourRegistries.CONTOUR_VERTICAL_SLAB.get(),
				ContourRegistries.CONTOUR_STAIRS.get(),
				ContourRegistries.CONTOUR_LAYERS.get(),
				ContourRegistries.CONTOUR_POST.get(),
				ContourRegistries.CONTOUR_BYTE.get(),
				ContourRegistries.CONTOUR_PANEL.get(),
				ContourRegistries.CONTOUR_WALL.get(),
				ContourRegistries.CONTOUR_PANE.get(),
				ContourRegistries.CONTOUR_FENCE.get(),
				ContourRegistries.CONTOUR_SLOPE.get(),
				ContourRegistries.CONTOUR_TRAPDOOR.get(),
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
			for (Block block : blocks) {
				for (BlockState state : block.getStateDefinition().getPossibleStates()) {
					BakedModel m = mc.getBlockRenderer().getBlockModel(state);
					String tag = block + "/" + state.getValues();
					check("allquads/" + tag, () -> !allQuads(m, state, ModelData.EMPTY).isEmpty());
					check("stateless/" + tag, () -> !allQuads(m, null, ModelData.EMPTY).isEmpty());
					check("nulldata/" + tag, () -> !allQuads(m, state, null).isEmpty());
					check("particle-null/" + tag, () -> m.getParticleIcon(null) != null);
					check("particle/" + tag, () -> m.getParticleIcon() != null);
					check("getModelData-safe/" + tag, () -> {
						ModelData d = m.getModelData(mc.level, BlockPos.ZERO, state, ModelData.EMPTY);
						return d != null;
					});
				}
				// grid geometry: masks 1/255/bottom-slab with one neutral src/face
				BakedModel any = mc.getBlockRenderer().getBlockModel(block.defaultBlockState());
				var sprite = any.getParticleIcon();
				java.util.function.Function<Direction, List<ShapeQuads.SrcQuad>> src =
					d -> List.of(ShapeQuads.neutral(d, sprite));
				checkGrid(block + "/mask1", 1, 6, src);
				checkGrid(block + "/mask255", 255, 24, src);
				checkGrid(block + "/maskBottom", 0b00110011, 16, src);
				// materials: 7 states on each default model
				BakedModel m = mc.getBlockRenderer().getBlockModel(block.defaultBlockState());
				BlockState[] mats = {
					net.minecraft.world.level.block.Blocks.STONE.defaultBlockState(),
					net.minecraft.world.level.block.Blocks.OAK_SLAB.defaultBlockState(),
					net.minecraft.world.level.block.Blocks.GLASS.defaultBlockState(),
					net.minecraft.world.level.block.Blocks.OAK_LOG.defaultBlockState()
						.setValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.AXIS, Direction.Axis.X),
					net.minecraft.world.level.block.Blocks.OAK_LOG.defaultBlockState()
						.setValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.AXIS, Direction.Axis.Z),
					net.minecraft.world.level.block.Blocks.OAK_LEAVES.defaultBlockState(),
					net.minecraft.world.level.block.Blocks.WHITE_STAINED_GLASS.defaultBlockState(),
					net.minecraft.core.registries.BuiltInRegistries.BLOCK
						.get(ResourceLocation.fromNamespaceAndPath("create", "andesite_casing"))
						.defaultBlockState()};
				check("wrapped-ours/" + block, () -> m instanceof ContourModel);
				// neutral material data + missing data -> OUR sprites only
				ModelData neutralMd = ModelData.builder()
					.with(CopycatModel.MATERIAL_PROPERTY, MaterialSafety.neutral()).build();
				check("neutral-own-sprites/" + block, () -> {
					for (var q : allQuads(m, block.defaultBlockState(), neutralMd))
						if (!"create_contour".equals(q.getSprite().contents()
							.name().getNamespace()))
							return false;
					for (var q : allQuads(m, block.defaultBlockState(), ModelData.EMPTY))
						if (!"create_contour".equals(q.getSprite().contents()
							.name().getNamespace()))
							return false;
					return "create_contour".equals(m.getParticleIcon(neutralMd)
						.contents().name().getNamespace());
				});
				for (BlockState mat : mats) {
					String mn = mat.getBlock().toString();
					ModelData md = ModelData.builder()
						.with(CopycatModel.MATERIAL_PROPERTY, mat).build();
					List<net.minecraft.client.renderer.block.model.BakedQuad> quads =
						allQuads(m, block.defaultBlockState(), md);
					check("material/" + mn, () -> !quads.isEmpty());
					check("material-geom/" + mn, () -> {
						for (var q : quads) checkGeometry(q);
						return true;
					});
					// exact render-layer delegation vs the material's own model
					check("renderType/" + mn, () -> {
						var expected = mc.getBlockRenderer().getBlockModel(mat)
							.getRenderTypes(mat, RandomSource.create(7), ModelData.EMPTY);
						var actual = m.getRenderTypes(block.defaultBlockState(),
							RandomSource.create(7), md);
						return net.minecraft.client.renderer.RenderType.chunkBufferLayers()
							.stream().allMatch(t -> expected.contains(t) == actual.contains(t));
					});
				}
			}
			// CTM: a UV-shifted source (andesite_casing at the matrix pos)
			// must keep its atlas region through emit - U/V ranges match.
			check("ctm-uv-region", () -> {
				BlockState mat = net.minecraft.core.registries.BuiltInRegistries.BLOCK
					.get(ResourceLocation.fromNamespaceAndPath("create", "andesite_casing"))
					.defaultBlockState();
				BakedModel mm = mc.getBlockRenderer().getBlockModel(mat);
				ModelData sourceData = mm.getModelData(mc.level,
					new BlockPos(115, -60, 96), mat, ModelData.EMPTY);
				net.minecraft.client.renderer.block.model.BakedQuad src = null;
				for (var q : mm.getQuads(mat, Direction.UP,
					RandomSource.create(3), sourceData, null)) {
					var sp = q.getSprite();
					for (int i = 0; i < 4; i++) {
						float u = Float.intBitsToFloat(q.getVertices()[i * 8 + 4]);
						float v = Float.intBitsToFloat(q.getVertices()[i * 8 + 5]);
						if (u < sp.getU0() - 1e-6 || u > sp.getU1() + 1e-6
							|| v < sp.getV0() - 1e-6 || v > sp.getV1() + 1e-6)
							src = q;
					}
				}
				if (src == null) {
					report("ctm-uv-region",
						"FAIL no shifted source quad fixture exercised");
					return false;
				}
				var sq = ShapeQuads.extract(src);
				float[] srcRange = {min4(sq.u()), max4(sq.u()), min4(sq.v()), max4(sq.v())};
				float[] out = {Float.MAX_VALUE, -Float.MAX_VALUE,
					Float.MAX_VALUE, -Float.MAX_VALUE};
				for (var q : ShapeQuads.emit(255, Direction.UP,
					d -> List.of(sq))) {
					for (int i = 0; i < 4; i++) {
						out[0] = Math.min(out[0], Float.intBitsToFloat(q.getVertices()[i * 8 + 4]));
						out[1] = Math.max(out[1], Float.intBitsToFloat(q.getVertices()[i * 8 + 4]));
						out[2] = Math.min(out[2], Float.intBitsToFloat(q.getVertices()[i * 8 + 5]));
						out[3] = Math.max(out[3], Float.intBitsToFloat(q.getVertices()[i * 8 + 5]));
					}
				}
				for (int i = 0; i < 4; i++)
					if (Math.abs(out[i] - srcRange[i]) > 1e-6)
						return false;
				return true;
			});
			// LIVE client sync: real handleUpdateTag packet handler on the
			// fixture BE must update material AND client aux light instantly
			// (the remesh/aux bug that left painted shapes gray until reload).
			check("livesync-glow", () -> {
				var level = mc.level;
				var be = (ContourBlockEntity) level.getBlockEntity(QA_SYNC_POS);
				var provider = level.registryAccess();
				var snapshot = be.saveWithFullMetadata(provider);
				try {
					var glow = be.saveWithFullMetadata(provider);
					var glowMat = new net.minecraft.nbt.CompoundTag();
					glowMat.putString("Name", "minecraft:glowstone");
					glow.put("Material", glowMat);
					glow.putInt("PaidUnits", 1);
					be.handleUpdateTag(glow, provider);
					if (be.getMaterial().getBlock()
						!= net.minecraft.world.level.block.Blocks.GLOWSTONE)
						return false;
					if (level.getAuxLightManager(QA_SYNC_POS)
						.getLightAt(QA_SYNC_POS) != 15)
						return false;
					var stone = be.saveWithFullMetadata(provider);
					var stoneMat = new net.minecraft.nbt.CompoundTag();
					stoneMat.putString("Name", "minecraft:stone");
					stone.put("Material", stoneMat);
					stone.putInt("PaidUnits", 0);
					be.handleUpdateTag(stone, provider);
					return level.getAuxLightManager(QA_SYNC_POS)
						.getLightAt(QA_SYNC_POS) == 0;
				} finally {
					be.handleUpdateTag(snapshot, provider);
				}
			});
			// items: painted payload resolves a bound painted model
			for (var item : new net.minecraft.world.item.Item[]{
				ContourRegistries.CONTOUR_SLAB_ITEM.get(),
				ContourRegistries.CONTOUR_VERTICAL_SLAB_ITEM.get(),
				ContourRegistries.CONTOUR_STAIRS_ITEM.get(),
				ContourRegistries.CONTOUR_LAYERS_ITEM.get(),
				ContourRegistries.CONTOUR_POST_ITEM.get(),
				ContourRegistries.CONTOUR_BYTE_ITEM.get(),
				ContourRegistries.CONTOUR_PANEL_ITEM.get(),
				ContourRegistries.CONTOUR_WALL_ITEM.get(),
				ContourRegistries.CONTOUR_PANE_ITEM.get(),
				ContourRegistries.CONTOUR_FENCE_ITEM.get(),
				ContourRegistries.CONTOUR_SLOPE_ITEM.get(),
				ContourRegistries.CONTOUR_TRAPDOOR_ITEM.get(),
				ContourRegistries.CONTOUR_LADDER_ITEM.get(),
				ContourRegistries.CONTOUR_FENCE_GATE_ITEM.get(),
				ContourRegistries.CONTOUR_LEVER_ITEM.get(),
				ContourRegistries.CONTOUR_DOOR_ITEM.get(),
				ContourRegistries.CONTOUR_SLICE_ITEM.get(),
				ContourRegistries.CONTOUR_CORNER_SLOPE_ITEM.get(),
				ContourRegistries.CONTOUR_BUTTON_ITEM.get(),
				ContourRegistries.CONTOUR_INNER_SLOPE_ITEM.get(),
				ContourRegistries.CONTOUR_PRESSURE_PLATE_ITEM.get(),
				ContourRegistries.CONTOUR_GHOST_ITEM.get(),
				ContourRegistries.CONTOUR_TORCH_ITEM.get(),
				ContourRegistries.CONTOUR_ONE_WAY_ITEM.get(),
				ContourRegistries.CONTOUR_PILLAR_ITEM.get(),
				ContourRegistries.CONTOUR_TRIM_ITEM.get(),
				ContourRegistries.CONTOUR_FRAME_ITEM.get(),
				ContourRegistries.CONTOUR_PYRAMID_ITEM.get(),
				ContourRegistries.CONTOUR_LAMP_ITEM.get()}) {
				BakedModel im = mc.getItemRenderer().getItemModelShaper()
					.getItemModel(new net.minecraft.world.item.ItemStack(item));
				check("item-neutral/" + item, () -> im instanceof ContourModel);
				net.minecraft.world.item.ItemStack painted =
					new net.minecraft.world.item.ItemStack(item);
				ContourBlockItem.writePayload(painted,
					net.minecraft.world.level.block.Blocks.STONE.defaultBlockState(),
					0, Direction.NORTH, false);
				BakedModel resolved = im.getOverrides().resolve(im, painted, mc.level, mc.player, 0);
				check("item-painted/" + item, () ->
					resolved instanceof ContourModel && resolved != im);
				// item render boundary: passes + render types must carry the
				// bound material through ItemRenderer (the NF wrapper would
				// delegate to the unbound original otherwise)
				for (BlockState mat : new BlockState[]{
					net.minecraft.world.level.block.Blocks.STONE.defaultBlockState(),
					net.minecraft.world.level.block.Blocks.OAK_LOG.defaultBlockState()
						.setValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.AXIS,
							Direction.Axis.X),
					net.minecraft.world.level.block.Blocks.GLASS.defaultBlockState(),
					net.minecraft.world.level.block.Blocks.OAK_LEAVES.defaultBlockState(),
					net.minecraft.world.level.block.Blocks.WHITE_STAINED_GLASS.defaultBlockState()}) {
					net.minecraft.world.item.ItemStack pi =
						new net.minecraft.world.item.ItemStack(item);
					ContourBlockItem.writePayload(pi, mat, 0, Direction.NORTH, false);
					BakedModel bound = im.getOverrides().resolve(im, pi, mc.level, mc.player, 0);
					String mn = item + "/" + mat.getBlock();
					check("itempass/" + mn, () -> {
						var passes = bound.getRenderPasses(pi, false);
						return passes.size() == 1 && passes.get(0) == bound;
					});
					check("itempass-quads/" + mn, () -> {
						for (var q : allQuads(bound, null, ModelData.EMPTY))
							if ("create_contour".equals(q.getSprite().contents()
								.name().getNamespace()))
								return false;
						return !allQuads(bound, null, ModelData.EMPTY).isEmpty();
					});
					check("itemtypes/" + mn, () -> {
						var matItem = new net.minecraft.world.item.ItemStack(
							mat.getBlock().asItem());
						for (boolean fab : new boolean[]{false, true}) {
							var expected = mc.getBlockRenderer().getBlockModel(mat)
								.getRenderTypes(matItem, fab);
							var actual = bound.getRenderTypes(pi, fab);
							if (!new java.util.HashSet<>(expected)
								.equals(new java.util.HashSet<>(actual)))
								return false;
						}
						return true;
					});
					// REAL renderer path: handleCameraTransforms must return
					// the BOUND model for every context/hand, and the real
					// getModel entrypoint must resolve the painted material.
					check("itemxform/" + mn, () -> {
						for (var ctx : net.minecraft.world.item.ItemDisplayContext.values())
							for (boolean left : new boolean[]{false, true}) {
								var pose = new com.mojang.blaze3d.vertex.PoseStack();
								var after = net.neoforged.neoforge.client.ClientHooks
									.handleCameraTransforms(pose, bound, ctx, left);
								if (after != bound
									|| after.getRenderPasses(pi, false).get(0) != bound)
									return false;
							}
						return true;
					});
					check("itemreal/" + mn, () -> {
						var real = mc.getItemRenderer().getModel(pi, mc.level, mc.player, 0);
						if (!(real instanceof ContourModel))
							return false;
						for (var q : allQuads(real, null, ModelData.EMPTY))
							if ("create_contour".equals(q.getSprite().contents()
								.name().getNamespace()))
								return false;
						return !allQuads(real, null, ModelData.EMPTY).isEmpty();
					});
				}
			}
		}

		private static float min4(float[] a) {
			float m = a[0];
			for (float f : a) m = Math.min(m, f);
			return m;
		}

		private static float max4(float[] a) {
			float m = a[0];
			for (float f : a) m = Math.max(m, f);
			return m;
		}

		private static void checkGrid(String name, int mask, int expected,
			java.util.function.Function<Direction, List<ShapeQuads.SrcQuad>> src) {
			java.util.HashSet<String> seen = new java.util.HashSet<>();
			int total = 0;
			for (Direction d : new Direction[]{null, Direction.UP, Direction.DOWN,
				Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST}) {
				for (var q : ShapeQuads.emit(mask, d, src)) {
					total++;
					String sig = q.getDirection() + "/"
						+ java.util.Arrays.hashCode(q.getVertices());
					check(name + "/unique", () -> seen.add(sig));
					checkGeometry(q);
				}
			}
			final int t = total;
			check(name + "/count", () -> t == expected);
		}

		private static void check(String name, ThrowingBool test) {
			try {
				if (test.run()) {
					passed++;
					report(name, "PASS");
				} else {
					failed++;
					report(name, "FAIL");
				}
			} catch (RuntimeException t) {
				failed++;
				report(name, "FAIL " + t);
			}
		}

		private static void report(String name, String outcome) {
			com.mojang.logging.LogUtils.getLogger()
				.info("[contour-smoke] {} {}", outcome, name);
		}

		@FunctionalInterface
		private interface ThrowingBool {
			boolean run() throws RuntimeException;
		}
	}
}
