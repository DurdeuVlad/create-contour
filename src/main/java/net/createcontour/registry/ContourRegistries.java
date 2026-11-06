package net.createcontour.registry;

import com.simibubi.create.content.decoration.copycat.CopycatBlockEntity;
import net.createcontour.CreateContour;
import net.createcontour.block.ContourButtonBlock;
import net.createcontour.block.ContourArchBlock;
import net.createcontour.block.ContourBarrelVaultBlock;
import net.createcontour.block.ContourBracketBlock;
import net.createcontour.block.ContourByteBlock;
import net.createcontour.block.ContourBalusterBlock;
import net.createcontour.block.ContourBullnoseBlock;
import net.createcontour.block.ContourCapitalBlock;
import net.createcontour.block.ContourChainBlock;
import net.createcontour.block.ContourCogBlock;
import net.createcontour.block.ContourConeBlock;
import net.createcontour.block.ContourCorniceBlock;
import net.createcontour.block.ContourCrenellationBlock;
import net.createcontour.block.ContourCornerSlopeBlock;
import net.createcontour.block.ContourCylinderBlock;
import net.createcontour.block.ContourDomeBlock;
import net.createcontour.block.ContourDoorBlock;
import net.createcontour.block.ContourInnerSlopeBlock;
import net.createcontour.block.ContourFenceBlock;
import net.createcontour.block.ContourFrameBlock;
import net.createcontour.block.ContourFenceGateBlock;
import net.createcontour.block.ContourFinialBlock;
import net.createcontour.block.ContourOgeeBlock;
import net.createcontour.block.ContourScotiaBlock;
import net.createcontour.block.ContourSignBlock;
import net.createcontour.blockentity.ContourSignBlockEntity;
import net.createcontour.block.ContourGhostBlock;
import net.createcontour.block.ContourGothicArchBlock;
import net.createcontour.block.ContourGutterBlock;
import net.createcontour.block.ContourHalfSlopeBlock;
import net.createcontour.block.ContourLadderBlock;
import net.createcontour.block.ContourLampBlock;
import net.createcontour.block.ContourLatticeBlock;
import net.createcontour.block.ContourLouverBlock;
import net.createcontour.block.ContourLayersBlock;
import net.createcontour.block.ContourLeverBlock;
import net.createcontour.block.ContourNotchBlock;
import net.createcontour.block.ContourOneWayBlock;
import net.createcontour.block.ContourPaneBlock;
import net.createcontour.block.ContourPilasterBlock;
import net.createcontour.block.ContourPillarBlock;
import net.createcontour.block.ContourPanelBlock;
import net.createcontour.block.ContourPostBlock;
import net.createcontour.block.ContourSphereBlock;
import net.createcontour.block.ContourStairCornerBlock;
import net.createcontour.block.ContourPressurePlateBlock;
import net.createcontour.block.ContourPyramidBlock;
import net.createcontour.block.ContourQuarterRoundBlock;
import net.createcontour.block.ContourRidgeBlock;
import net.createcontour.block.ContourRodBlock;
import net.createcontour.block.ContourRoundCornerBlock;
import net.createcontour.block.ContourScaffoldBlock;
import net.createcontour.block.ContourSlabBlock;
import net.createcontour.block.ContourSliceBlock;
import net.createcontour.block.ContourSlopeBlock;
import net.createcontour.block.ContourStairsBlock;
import net.createcontour.block.ContourTorchBlock;
import net.createcontour.block.ContourTrapdoorBlock;
import net.createcontour.block.ContourTorusBlock;
import net.createcontour.block.ContourTrimBlock;
import net.createcontour.block.ContourTubeBlock;
import net.createcontour.block.ContourVerticalSlabBlock;
import net.createcontour.block.ContourVerticalStairsBlock;
import net.createcontour.block.ContourWallBlock;
import net.createcontour.block.ContourElbowBlock;
import net.createcontour.block.ContourBalustradeBlock;
import net.createcontour.block.ContourHandrailBlock;
import net.createcontour.block.ContourIBeamBlock;
import net.createcontour.block.ContourNewelBlock;
import net.createcontour.block.ContourArrowSlitBlock;
import net.createcontour.block.ContourButtressBlock;
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
import net.createcontour.block.ContourCrossBlock;
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
import net.createcontour.block.ContourFlatPaneBlock;import net.createcontour.block.ContourCornerSliceBlock;
import net.createcontour.block.ContourBytePanelBlock;
import net.createcontour.block.ContourHalfLayerBlock;
import net.createcontour.block.ContourIronDoorBlock;
import net.createcontour.block.ContourIronTrapdoorBlock;
import net.createcontour.block.ContourSlidingDoorBlock;
import net.createcontour.block.ContourFoldingDoorBlock;
import net.createcontour.block.ContourCatwalkBlock;

import net.createcontour.block.ContourPortcullisBlock;
import net.createcontour.block.ContourWheelBlock;
import net.createcontour.blockentity.ContourBlockEntity;
import net.createcontour.item.ContourBlockItem;
import net.minecraft.world.item.BlockItem;
import net.createcontour.item.ContourCodexItem;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ContourRegistries {

	public static final DeferredRegister.Blocks BLOCKS =
		DeferredRegister.createBlocks(CreateContour.MOD_ID);
	public static final DeferredRegister.Items ITEMS =
		DeferredRegister.createItems(CreateContour.MOD_ID);
	public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
		DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, CreateContour.MOD_ID);
	public static final DeferredRegister<CreativeModeTab> CREATIVE_TABS =
		DeferredRegister.create(Registries.CREATIVE_MODE_TAB, CreateContour.MOD_ID);

	private static BlockBehaviour.Properties shapeProps() {
		return BlockBehaviour.Properties.of()
			.noOcclusion()
			.dynamicShape()
			.sound(SoundType.STONE)
			.strength(1.5f);
	}

	public static final DeferredBlock<ContourSlabBlock> CONTOUR_SLAB =
		BLOCKS.register("contour_slab", () -> new ContourSlabBlock(shapeProps()));
	public static final DeferredBlock<ContourVerticalSlabBlock> CONTOUR_VERTICAL_SLAB =
		BLOCKS.register("contour_vertical_slab", () -> new ContourVerticalSlabBlock(shapeProps()));
	public static final DeferredBlock<ContourStairsBlock> CONTOUR_STAIRS =
		BLOCKS.register("contour_stairs", () -> new ContourStairsBlock(shapeProps()));
	public static final DeferredBlock<ContourLayersBlock> CONTOUR_LAYERS =
		BLOCKS.register("contour_layers", () -> new ContourLayersBlock(shapeProps()));
	public static final DeferredBlock<ContourPostBlock> CONTOUR_POST =
		BLOCKS.register("contour_post", () -> new ContourPostBlock(shapeProps()));
	public static final DeferredBlock<ContourByteBlock> CONTOUR_BYTE =
		BLOCKS.register("contour_byte", () -> new ContourByteBlock(shapeProps()));
	public static final DeferredBlock<ContourPanelBlock> CONTOUR_PANEL =
		BLOCKS.register("contour_panel", () -> new ContourPanelBlock(shapeProps()));
	public static final DeferredBlock<ContourWallBlock> CONTOUR_WALL =
		BLOCKS.register("contour_wall", () -> new ContourWallBlock(shapeProps()));
	public static final DeferredBlock<ContourPaneBlock> CONTOUR_PANE =
		BLOCKS.register("contour_pane", () -> new ContourPaneBlock(shapeProps()));
	public static final DeferredBlock<ContourFenceBlock> CONTOUR_FENCE =
		BLOCKS.register("contour_fence", () -> new ContourFenceBlock(shapeProps()));
	public static final DeferredBlock<ContourSlopeBlock> CONTOUR_SLOPE =
		BLOCKS.register("contour_slope", () -> new ContourSlopeBlock(shapeProps()));
	public static final DeferredBlock<ContourTrapdoorBlock> CONTOUR_TRAPDOOR =
		BLOCKS.register("contour_trapdoor", () -> new ContourTrapdoorBlock(shapeProps()));
	public static final DeferredBlock<ContourLadderBlock> CONTOUR_LADDER =
		BLOCKS.register("contour_ladder", () -> new ContourLadderBlock(shapeProps()));
	public static final DeferredBlock<ContourFenceGateBlock> CONTOUR_FENCE_GATE =
		BLOCKS.register("contour_fence_gate", () -> new ContourFenceGateBlock(shapeProps()));
	public static final DeferredBlock<ContourLeverBlock> CONTOUR_LEVER =
		BLOCKS.register("contour_lever", () -> new ContourLeverBlock(shapeProps()));
	public static final DeferredBlock<ContourDoorBlock> CONTOUR_DOOR =
		BLOCKS.register("contour_door", () -> new ContourDoorBlock(shapeProps()));
	public static final DeferredBlock<ContourSliceBlock> CONTOUR_SLICE =
		BLOCKS.register("contour_slice", () -> new ContourSliceBlock(shapeProps()));
	public static final DeferredBlock<ContourCornerSlopeBlock> CONTOUR_CORNER_SLOPE =
		BLOCKS.register("contour_corner_slope",
			() -> new ContourCornerSlopeBlock(shapeProps()));
	public static final DeferredBlock<ContourButtonBlock> CONTOUR_BUTTON =
		BLOCKS.register("contour_button", () -> new ContourButtonBlock(shapeProps()));
	public static final DeferredBlock<ContourInnerSlopeBlock> CONTOUR_INNER_SLOPE =
		BLOCKS.register("contour_inner_slope",
			() -> new ContourInnerSlopeBlock(shapeProps()));
	public static final DeferredBlock<ContourPressurePlateBlock> CONTOUR_PRESSURE_PLATE =
		BLOCKS.register("contour_pressure_plate",
			() -> new ContourPressurePlateBlock(shapeProps()));
	public static final DeferredBlock<ContourGhostBlock> CONTOUR_GHOST =
		BLOCKS.register("contour_ghost", () -> new ContourGhostBlock(shapeProps()));
	public static final DeferredBlock<ContourTorchBlock> CONTOUR_TORCH =
		BLOCKS.register("contour_torch",
			() -> new ContourTorchBlock(shapeProps().lightLevel(s -> 14)));
	public static final DeferredBlock<ContourOneWayBlock> CONTOUR_ONE_WAY =
		BLOCKS.register("contour_one_way", () -> new ContourOneWayBlock(shapeProps()));
	public static final DeferredBlock<ContourPillarBlock> CONTOUR_PILLAR =
		BLOCKS.register("contour_pillar", () -> new ContourPillarBlock(shapeProps()));
	public static final DeferredBlock<ContourTrimBlock> CONTOUR_TRIM =
		BLOCKS.register("contour_trim", () -> new ContourTrimBlock(shapeProps()));
	public static final DeferredBlock<ContourFrameBlock> CONTOUR_FRAME =
		BLOCKS.register("contour_frame", () -> new ContourFrameBlock(shapeProps()));
	public static final DeferredBlock<ContourPyramidBlock> CONTOUR_PYRAMID =
		BLOCKS.register("contour_pyramid", () -> new ContourPyramidBlock(shapeProps()));
	public static final DeferredBlock<ContourLampBlock> CONTOUR_LAMP =
		BLOCKS.register("contour_lamp", () -> new ContourLampBlock(shapeProps()
			.lightLevel(s -> s.getValue(ContourLampBlock.LIT) ? 15 : 0)));
	public static final DeferredBlock<ContourHalfSlopeBlock> CONTOUR_HALF_SLOPE =
		BLOCKS.register("contour_half_slope", () -> new ContourHalfSlopeBlock(shapeProps()));
	public static final DeferredBlock<ContourNotchBlock> CONTOUR_NOTCH =
		BLOCKS.register("contour_notch", () -> new ContourNotchBlock(shapeProps()));
	public static final DeferredBlock<ContourRodBlock> CONTOUR_ROD =
		BLOCKS.register("contour_rod", () -> new ContourRodBlock(shapeProps()));
	public static final DeferredBlock<ContourLatticeBlock> CONTOUR_LATTICE =
		BLOCKS.register("contour_lattice", () -> new ContourLatticeBlock(shapeProps()));
	public static final DeferredBlock<ContourQuarterRoundBlock> CONTOUR_QUARTER_ROUND =
		BLOCKS.register("contour_quarter_round",
			() -> new ContourQuarterRoundBlock(shapeProps()));
	public static final DeferredBlock<ContourChainBlock> CONTOUR_CHAIN =
		BLOCKS.register("contour_chain", () -> new ContourChainBlock(shapeProps()));
	public static final DeferredBlock<ContourRoundCornerBlock> CONTOUR_ROUND_CORNER =
		BLOCKS.register("contour_round_corner",
			() -> new ContourRoundCornerBlock(shapeProps()));
	public static final DeferredBlock<ContourDomeBlock> CONTOUR_DOME =
		BLOCKS.register("contour_dome", () -> new ContourDomeBlock(shapeProps()));
	public static final DeferredBlock<ContourRidgeBlock> CONTOUR_RIDGE =
		BLOCKS.register("contour_ridge", () -> new ContourRidgeBlock(shapeProps()));
	public static final DeferredBlock<ContourTubeBlock> CONTOUR_TUBE =
		BLOCKS.register("contour_tube", () -> new ContourTubeBlock(shapeProps()));
	public static final DeferredBlock<ContourGutterBlock> CONTOUR_GUTTER =
		BLOCKS.register("contour_gutter", () -> new ContourGutterBlock(shapeProps()));
	public static final DeferredBlock<ContourBracketBlock> CONTOUR_BRACKET =
		BLOCKS.register("contour_bracket", () -> new ContourBracketBlock(shapeProps()));
	public static final DeferredBlock<ContourVerticalStairsBlock> CONTOUR_VERTICAL_STAIRS =
		BLOCKS.register("contour_vertical_stairs",
			() -> new ContourVerticalStairsBlock(shapeProps()));
	public static final DeferredBlock<ContourArchBlock> CONTOUR_ARCH =
		BLOCKS.register("contour_arch", () -> new ContourArchBlock(shapeProps()));
	public static final DeferredBlock<ContourCylinderBlock> CONTOUR_CYLINDER =
		BLOCKS.register("contour_cylinder", () -> new ContourCylinderBlock(shapeProps()));
	public static final DeferredBlock<ContourPilasterBlock> CONTOUR_PILASTER =
		BLOCKS.register("contour_pilaster", () -> new ContourPilasterBlock(shapeProps()));
	public static final DeferredBlock<ContourStairCornerBlock> CONTOUR_STAIR_CORNER =
		BLOCKS.register("contour_stair_corner",
			() -> new ContourStairCornerBlock(shapeProps()));
	public static final DeferredBlock<ContourSphereBlock> CONTOUR_SPHERE =
		BLOCKS.register("contour_sphere", () -> new ContourSphereBlock(shapeProps()));
	public static final DeferredBlock<ContourConeBlock> CONTOUR_CONE =
		BLOCKS.register("contour_cone", () -> new ContourConeBlock(shapeProps()));
	public static final DeferredBlock<ContourCapitalBlock> CONTOUR_CAPITAL =
		BLOCKS.register("contour_capital", () -> new ContourCapitalBlock(shapeProps()));
	public static final DeferredBlock<ContourBullnoseBlock> CONTOUR_BULLNOSE =
		BLOCKS.register("contour_bullnose", () -> new ContourBullnoseBlock(shapeProps()));
	public static final DeferredBlock<ContourCorniceBlock> CONTOUR_CORNICE =
		BLOCKS.register("contour_cornice", () -> new ContourCorniceBlock(shapeProps()));
	public static final DeferredBlock<ContourBalusterBlock> CONTOUR_BALUSTER =
		BLOCKS.register("contour_baluster", () -> new ContourBalusterBlock(shapeProps()));
	public static final DeferredBlock<ContourFinialBlock> CONTOUR_FINIAL =
		BLOCKS.register("contour_finial", () -> new ContourFinialBlock(shapeProps()));
	public static final DeferredBlock<ContourScotiaBlock> CONTOUR_SCOTIA =
		BLOCKS.register("contour_scotia", () -> new ContourScotiaBlock(shapeProps()));
	public static final DeferredBlock<ContourOgeeBlock> CONTOUR_OGEE =
		BLOCKS.register("contour_ogee", () -> new ContourOgeeBlock(shapeProps()));
	public static final DeferredBlock<ContourSignBlock> CONTOUR_SIGN =
		BLOCKS.register("contour_sign", () -> new ContourSignBlock(shapeProps()));
	public static final DeferredBlock<ContourTorusBlock> CONTOUR_TORUS =
		BLOCKS.register("contour_torus", () -> new ContourTorusBlock(shapeProps()));
	public static final DeferredBlock<ContourCrenellationBlock> CONTOUR_CRENELLATION =
		BLOCKS.register("contour_crenellation",
			() -> new ContourCrenellationBlock(shapeProps()));
	public static final DeferredBlock<ContourBarrelVaultBlock> CONTOUR_BARREL_VAULT =
		BLOCKS.register("contour_barrel_vault",
			() -> new ContourBarrelVaultBlock(shapeProps()));
	public static final DeferredBlock<ContourCogBlock> CONTOUR_COG =
		BLOCKS.register("contour_cog", () -> new ContourCogBlock(shapeProps()));
	public static final DeferredBlock<ContourWheelBlock> CONTOUR_WHEEL =
		BLOCKS.register("contour_wheel",
			() -> new ContourWheelBlock(shapeProps()));
	public static final DeferredBlock<ContourScaffoldBlock> CONTOUR_SCAFFOLD =
		BLOCKS.register("contour_scaffold",
			() -> new ContourScaffoldBlock(shapeProps()));
	public static final DeferredBlock<ContourGothicArchBlock> CONTOUR_GOTHIC_ARCH =
		BLOCKS.register("contour_gothic_arch",
			() -> new ContourGothicArchBlock(shapeProps()));
	public static final DeferredBlock<ContourLouverBlock> CONTOUR_LOUVER =
		BLOCKS.register("contour_louver",
			() -> new ContourLouverBlock(shapeProps()));
	public static final DeferredBlock<ContourElbowBlock> CONTOUR_ELBOW =
		BLOCKS.register("contour_elbow",
			() -> new ContourElbowBlock(shapeProps()));
	public static final DeferredBlock<ContourHandrailBlock> CONTOUR_HANDRAIL =
		BLOCKS.register("contour_handrail",
			() -> new ContourHandrailBlock(shapeProps()));
	public static final DeferredBlock<ContourNewelBlock> CONTOUR_NEWEL =
		BLOCKS.register("contour_newel",
			() -> new ContourNewelBlock(shapeProps()));
	public static final DeferredBlock<ContourBalustradeBlock> CONTOUR_BALUSTRADE =
		BLOCKS.register("contour_balustrade",
			() -> new ContourBalustradeBlock(shapeProps()));
	public static final DeferredBlock<ContourIBeamBlock> CONTOUR_I_BEAM =
		BLOCKS.register("contour_i_beam",
			() -> new ContourIBeamBlock(shapeProps()));
	public static final DeferredBlock<ContourPortcullisBlock> CONTOUR_PORTCULLIS =
		BLOCKS.register("contour_portcullis",
			() -> new ContourPortcullisBlock(shapeProps()));
	public static final DeferredBlock<ContourFlangeBlock> CONTOUR_FLANGE =
		BLOCKS.register("contour_flange",
			() -> new ContourFlangeBlock(shapeProps()));
	public static final DeferredBlock<ContourButtressBlock> CONTOUR_BUTTRESS =
		BLOCKS.register("contour_buttress",
			() -> new ContourButtressBlock(shapeProps()));
	public static final DeferredBlock<ContourArrowSlitBlock> CONTOUR_ARROW_SLIT =
		BLOCKS.register("contour_arrow_slit",
			() -> new ContourArrowSlitBlock(shapeProps()));
	public static final DeferredBlock<ContourDentilBlock> CONTOUR_DENTIL =
		BLOCKS.register("contour_dentil",
			() -> new ContourDentilBlock(shapeProps()));
	public static final DeferredBlock<ContourTeeBlock> CONTOUR_TEE =
		BLOCKS.register("contour_tee",
			() -> new ContourTeeBlock(shapeProps()));
	public static final DeferredBlock<ContourPalisadeBlock> CONTOUR_PALISADE =
		BLOCKS.register("contour_palisade",
			() -> new ContourPalisadeBlock(shapeProps()));
	public static final DeferredBlock<ContourQuoinBlock> CONTOUR_QUOIN =
		BLOCKS.register("contour_quoin",
			() -> new ContourQuoinBlock(shapeProps()));
	public static final DeferredBlock<ContourTrussBlock> CONTOUR_TRUSS =
		BLOCKS.register("contour_truss",
			() -> new ContourTrussBlock(shapeProps()));
	public static final DeferredBlock<ContourSillBlock> CONTOUR_SILL =
		BLOCKS.register("contour_sill",
			() -> new ContourSillBlock(shapeProps()));
	public static final DeferredBlock<ContourBollardBlock> CONTOUR_BOLLARD =
		BLOCKS.register("contour_bollard",
			() -> new ContourBollardBlock(shapeProps()));
	public static final DeferredBlock<ContourMullionBlock> CONTOUR_MULLION =
		BLOCKS.register("contour_mullion",
			() -> new ContourMullionBlock(shapeProps()));
	public static final DeferredBlock<ContourCofferBlock> CONTOUR_COFFER =
		BLOCKS.register("contour_coffer",
			() -> new ContourCofferBlock(shapeProps()));
	public static final DeferredBlock<ContourKeystoneBlock> CONTOUR_KEYSTONE =
		BLOCKS.register("contour_keystone",
			() -> new ContourKeystoneBlock(shapeProps()));
	public static final DeferredBlock<ContourPedestalBlock> CONTOUR_PEDESTAL =
		BLOCKS.register("contour_pedestal",
			() -> new ContourPedestalBlock(shapeProps()));
	public static final DeferredBlock<ContourLancetBlock> CONTOUR_LANCET =
		BLOCKS.register("contour_lancet",
			() -> new ContourLancetBlock(shapeProps()));
	public static final DeferredBlock<ContourValleyBlock> CONTOUR_VALLEY =
		BLOCKS.register("contour_valley",
			() -> new ContourValleyBlock(shapeProps()));
	public static final DeferredBlock<ContourCrossBlock> CONTOUR_CROSS =
		BLOCKS.register("contour_cross",
			() -> new ContourCrossBlock(shapeProps()));
	public static final DeferredBlock<ContourPipeCapBlock> CONTOUR_PIPE_CAP =
		BLOCKS.register("contour_pipe_cap",
			() -> new ContourPipeCapBlock(shapeProps()));
	public static final DeferredBlock<ContourPedimentBlock> CONTOUR_PEDIMENT =
		BLOCKS.register("contour_pediment",
			() -> new ContourPedimentBlock(shapeProps()));
	public static final DeferredBlock<ContourChimneyBlock> CONTOUR_CHIMNEY =
		BLOCKS.register("contour_chimney",
			() -> new ContourChimneyBlock(shapeProps()));
	public static final DeferredBlock<ContourHalfTimberBlock> CONTOUR_HALF_TIMBER =
		BLOCKS.register("contour_half_timber",
			() -> new ContourHalfTimberBlock(shapeProps()));
	public static final DeferredBlock<ContourSpandrelBlock> CONTOUR_SPANDREL =
		BLOCKS.register("contour_spandrel",
			() -> new ContourSpandrelBlock(shapeProps()));
	public static final DeferredBlock<ContourArcWallBlock> CONTOUR_ARC_WALL =
		BLOCKS.register("contour_arc_wall",
			() -> new ContourArcWallBlock(shapeProps()));
	public static final DeferredBlock<ContourRosetteBlock> CONTOUR_ROSETTE =
		BLOCKS.register("contour_rosette",
			() -> new ContourRosetteBlock(shapeProps()));
	public static final DeferredBlock<ContourFlyingButtressBlock> CONTOUR_FLYING_BUTTRESS =
		BLOCKS.register("contour_flying_buttress",
			() -> new ContourFlyingButtressBlock(shapeProps()));
	public static final DeferredBlock<ContourCopingBlock> CONTOUR_COPING =
		BLOCKS.register("contour_coping",
			() -> new ContourCopingBlock(shapeProps()));
	public static final DeferredBlock<ContourOculusBlock> CONTOUR_OCULUS =
		BLOCKS.register("contour_oculus",
			() -> new ContourOculusBlock(shapeProps()));
	public static final DeferredBlock<ContourAwningBlock> CONTOUR_AWNING =
		BLOCKS.register("contour_awning",
			() -> new ContourAwningBlock(shapeProps()));
	public static final DeferredBlock<ContourBufferBlock> CONTOUR_BUFFER =
		BLOCKS.register("contour_buffer",
			() -> new ContourBufferBlock(shapeProps()));
	public static final DeferredBlock<ContourSmokestackBlock> CONTOUR_SMOKESTACK =
		BLOCKS.register("contour_smokestack",
			() -> new ContourSmokestackBlock(shapeProps()));
	public static final DeferredBlock<ContourSemaphoreBlock> CONTOUR_SEMAPHORE =
		BLOCKS.register("contour_semaphore",
			() -> new ContourSemaphoreBlock(shapeProps()));
	public static final DeferredBlock<ContourPilotBlock> CONTOUR_PILOT =
		BLOCKS.register("contour_pilot",
			() -> new ContourPilotBlock(shapeProps()));
	public static final DeferredBlock<ContourVerticalStepBlock> CONTOUR_VERTICAL_STEP =
		BLOCKS.register("contour_vertical_step",
			() -> new ContourVerticalStepBlock(shapeProps()));
	public static final DeferredBlock<ContourBeamBlock> CONTOUR_BEAM =
		BLOCKS.register("contour_beam",
			() -> new ContourBeamBlock(shapeProps()));
	public static final DeferredBlock<ContourHalfPanelBlock> CONTOUR_HALF_PANEL =
		BLOCKS.register("contour_half_panel",
			() -> new ContourHalfPanelBlock(shapeProps()));
	public static final DeferredBlock<ContourFlatPaneBlock> CONTOUR_FLAT_PANE =
		BLOCKS.register("contour_flat_pane",
			() -> new ContourFlatPaneBlock(shapeProps()));

	public static final DeferredBlock<ContourCornerSliceBlock> CONTOUR_CORNER_SLICE =
		BLOCKS.register("contour_corner_slice",
			() -> new ContourCornerSliceBlock(shapeProps()));

	public static final DeferredBlock<ContourBytePanelBlock> CONTOUR_BYTE_PANEL =
		BLOCKS.register("contour_byte_panel",
			() -> new ContourBytePanelBlock(shapeProps()));

	public static final DeferredBlock<ContourHalfLayerBlock> CONTOUR_HALF_LAYER =
		BLOCKS.register("contour_half_layer",
			() -> new ContourHalfLayerBlock(shapeProps()));

	public static final DeferredBlock<ContourIronDoorBlock> CONTOUR_IRON_DOOR =
		BLOCKS.register("contour_iron_door",
			() -> new ContourIronDoorBlock(shapeProps()));

	public static final DeferredBlock<ContourIronTrapdoorBlock> CONTOUR_IRON_TRAPDOOR =
		BLOCKS.register("contour_iron_trapdoor",
			() -> new ContourIronTrapdoorBlock(shapeProps()));

	public static final DeferredBlock<ContourSlidingDoorBlock> CONTOUR_SLIDING_DOOR =
		BLOCKS.register("contour_sliding_door",
			() -> new ContourSlidingDoorBlock(shapeProps()));

	public static final DeferredBlock<ContourFoldingDoorBlock> CONTOUR_FOLDING_DOOR =
		BLOCKS.register("contour_folding_door",
			() -> new ContourFoldingDoorBlock(shapeProps()));

	public static final DeferredBlock<ContourCatwalkBlock> CONTOUR_CATWALK =
		BLOCKS.register("contour_catwalk",
			() -> new ContourCatwalkBlock(shapeProps()));

	public static final DeferredItem<ContourBlockItem> CONTOUR_SLAB_ITEM =
		ITEMS.register("contour_slab", () -> new ContourBlockItem(CONTOUR_SLAB.get()));
	public static final DeferredItem<ContourBlockItem> CONTOUR_VERTICAL_SLAB_ITEM =
		ITEMS.register("contour_vertical_slab", () -> new ContourBlockItem(CONTOUR_VERTICAL_SLAB.get()));
	public static final DeferredItem<ContourBlockItem> CONTOUR_STAIRS_ITEM =
		ITEMS.register("contour_stairs", () -> new ContourBlockItem(CONTOUR_STAIRS.get()));
	public static final DeferredItem<ContourBlockItem> CONTOUR_LAYERS_ITEM =
		ITEMS.register("contour_layers", () -> new ContourBlockItem(CONTOUR_LAYERS.get()));
	public static final DeferredItem<ContourBlockItem> CONTOUR_POST_ITEM =
		ITEMS.register("contour_post", () -> new ContourBlockItem(CONTOUR_POST.get()));
	public static final DeferredItem<ContourBlockItem> CONTOUR_BYTE_ITEM =
		ITEMS.register("contour_byte", () -> new ContourBlockItem(CONTOUR_BYTE.get()));
	public static final DeferredItem<ContourBlockItem> CONTOUR_PANEL_ITEM =
		ITEMS.register("contour_panel", () -> new ContourBlockItem(CONTOUR_PANEL.get()));
	public static final DeferredItem<ContourBlockItem> CONTOUR_WALL_ITEM =
		ITEMS.register("contour_wall", () -> new ContourBlockItem(CONTOUR_WALL.get()));
	public static final DeferredItem<ContourBlockItem> CONTOUR_PANE_ITEM =
		ITEMS.register("contour_pane", () -> new ContourBlockItem(CONTOUR_PANE.get()));
	public static final DeferredItem<ContourBlockItem> CONTOUR_FENCE_ITEM =
		ITEMS.register("contour_fence", () -> new ContourBlockItem(CONTOUR_FENCE.get()));
	public static final DeferredItem<ContourBlockItem> CONTOUR_SLOPE_ITEM =
		ITEMS.register("contour_slope", () -> new ContourBlockItem(CONTOUR_SLOPE.get()));
	public static final DeferredItem<ContourBlockItem> CONTOUR_TRAPDOOR_ITEM =
		ITEMS.register("contour_trapdoor", () -> new ContourBlockItem(CONTOUR_TRAPDOOR.get()));
	public static final DeferredItem<ContourBlockItem> CONTOUR_LADDER_ITEM =
		ITEMS.register("contour_ladder", () -> new ContourBlockItem(CONTOUR_LADDER.get()));
	public static final DeferredItem<ContourBlockItem> CONTOUR_FENCE_GATE_ITEM =
		ITEMS.register("contour_fence_gate", () -> new ContourBlockItem(CONTOUR_FENCE_GATE.get()));
	public static final DeferredItem<ContourBlockItem> CONTOUR_LEVER_ITEM =
		ITEMS.register("contour_lever", () -> new ContourBlockItem(CONTOUR_LEVER.get()));
	public static final DeferredItem<ContourBlockItem> CONTOUR_DOOR_ITEM =
		ITEMS.register("contour_door", () -> new ContourBlockItem(CONTOUR_DOOR.get()));
	public static final DeferredItem<ContourBlockItem> CONTOUR_SLICE_ITEM =
		ITEMS.register("contour_slice", () -> new ContourBlockItem(CONTOUR_SLICE.get()));
	public static final DeferredItem<ContourBlockItem> CONTOUR_CORNER_SLOPE_ITEM =
		ITEMS.register("contour_corner_slope",
			() -> new ContourBlockItem(CONTOUR_CORNER_SLOPE.get()));
	public static final DeferredItem<ContourBlockItem> CONTOUR_BUTTON_ITEM =
		ITEMS.register("contour_button", () -> new ContourBlockItem(CONTOUR_BUTTON.get()));
	public static final DeferredItem<ContourBlockItem> CONTOUR_INNER_SLOPE_ITEM =
		ITEMS.register("contour_inner_slope",
			() -> new ContourBlockItem(CONTOUR_INNER_SLOPE.get()));
	public static final DeferredItem<ContourBlockItem> CONTOUR_PRESSURE_PLATE_ITEM =
		ITEMS.register("contour_pressure_plate",
			() -> new ContourBlockItem(CONTOUR_PRESSURE_PLATE.get()));
	public static final DeferredItem<ContourBlockItem> CONTOUR_GHOST_ITEM =
		ITEMS.register("contour_ghost", () -> new ContourBlockItem(CONTOUR_GHOST.get()));
	public static final DeferredItem<ContourBlockItem> CONTOUR_TORCH_ITEM =
		ITEMS.register("contour_torch", () -> new ContourBlockItem(CONTOUR_TORCH.get()));
	public static final DeferredItem<ContourBlockItem> CONTOUR_ONE_WAY_ITEM =
		ITEMS.register("contour_one_way", () -> new ContourBlockItem(CONTOUR_ONE_WAY.get()));
	public static final DeferredItem<ContourBlockItem> CONTOUR_PILLAR_ITEM =
		ITEMS.register("contour_pillar", () -> new ContourBlockItem(CONTOUR_PILLAR.get()));
	public static final DeferredItem<ContourBlockItem> CONTOUR_TRIM_ITEM =
		ITEMS.register("contour_trim", () -> new ContourBlockItem(CONTOUR_TRIM.get()));
	public static final DeferredItem<ContourBlockItem> CONTOUR_FRAME_ITEM =
		ITEMS.register("contour_frame", () -> new ContourBlockItem(CONTOUR_FRAME.get()));
	public static final DeferredItem<ContourBlockItem> CONTOUR_PYRAMID_ITEM =
		ITEMS.register("contour_pyramid", () -> new ContourBlockItem(CONTOUR_PYRAMID.get()));
	public static final DeferredItem<ContourBlockItem> CONTOUR_LAMP_ITEM =
		ITEMS.register("contour_lamp", () -> new ContourBlockItem(CONTOUR_LAMP.get()));
	public static final DeferredItem<ContourBlockItem> CONTOUR_HALF_SLOPE_ITEM =
		ITEMS.register("contour_half_slope",
			() -> new ContourBlockItem(CONTOUR_HALF_SLOPE.get()));
	public static final DeferredItem<ContourBlockItem> CONTOUR_NOTCH_ITEM =
		ITEMS.register("contour_notch",
			() -> new ContourBlockItem(CONTOUR_NOTCH.get()));
	public static final DeferredItem<ContourBlockItem> CONTOUR_ROD_ITEM =
		ITEMS.register("contour_rod",
			() -> new ContourBlockItem(CONTOUR_ROD.get()));
	public static final DeferredItem<ContourBlockItem> CONTOUR_LATTICE_ITEM =
		ITEMS.register("contour_lattice",
			() -> new ContourBlockItem(CONTOUR_LATTICE.get()));
	public static final DeferredItem<ContourBlockItem> CONTOUR_QUARTER_ROUND_ITEM =
		ITEMS.register("contour_quarter_round",
			() -> new ContourBlockItem(CONTOUR_QUARTER_ROUND.get()));
	public static final DeferredItem<ContourBlockItem> CONTOUR_CHAIN_ITEM =
		ITEMS.register("contour_chain",
			() -> new ContourBlockItem(CONTOUR_CHAIN.get()));
	public static final DeferredItem<ContourBlockItem> CONTOUR_ROUND_CORNER_ITEM =
		ITEMS.register("contour_round_corner",
			() -> new ContourBlockItem(CONTOUR_ROUND_CORNER.get()));
	public static final DeferredItem<ContourBlockItem> CONTOUR_DOME_ITEM =
		ITEMS.register("contour_dome",
			() -> new ContourBlockItem(CONTOUR_DOME.get()));
	public static final DeferredItem<ContourBlockItem> CONTOUR_RIDGE_ITEM =
		ITEMS.register("contour_ridge",
			() -> new ContourBlockItem(CONTOUR_RIDGE.get()));
	public static final DeferredItem<ContourBlockItem> CONTOUR_TUBE_ITEM =
		ITEMS.register("contour_tube",
			() -> new ContourBlockItem(CONTOUR_TUBE.get()));
	public static final DeferredItem<ContourBlockItem> CONTOUR_GUTTER_ITEM =
		ITEMS.register("contour_gutter",
			() -> new ContourBlockItem(CONTOUR_GUTTER.get()));
	public static final DeferredItem<ContourBlockItem> CONTOUR_BRACKET_ITEM =
		ITEMS.register("contour_bracket",
			() -> new ContourBlockItem(CONTOUR_BRACKET.get()));
	public static final DeferredItem<ContourBlockItem> CONTOUR_VERTICAL_STAIRS_ITEM =
		ITEMS.register("contour_vertical_stairs",
			() -> new ContourBlockItem(CONTOUR_VERTICAL_STAIRS.get()));
	public static final DeferredItem<ContourBlockItem> CONTOUR_ARCH_ITEM =
		ITEMS.register("contour_arch",
			() -> new ContourBlockItem(CONTOUR_ARCH.get()));
	public static final DeferredItem<ContourBlockItem> CONTOUR_CYLINDER_ITEM =
		ITEMS.register("contour_cylinder",
			() -> new ContourBlockItem(CONTOUR_CYLINDER.get()));
	public static final DeferredItem<ContourBlockItem> CONTOUR_PILASTER_ITEM =
		ITEMS.register("contour_pilaster",
			() -> new ContourBlockItem(CONTOUR_PILASTER.get()));
	public static final DeferredItem<ContourBlockItem> CONTOUR_STAIR_CORNER_ITEM =
		ITEMS.register("contour_stair_corner",
			() -> new ContourBlockItem(CONTOUR_STAIR_CORNER.get()));
	public static final DeferredItem<ContourBlockItem> CONTOUR_SPHERE_ITEM =
		ITEMS.register("contour_sphere",
			() -> new ContourBlockItem(CONTOUR_SPHERE.get()));
	public static final DeferredItem<ContourBlockItem> CONTOUR_CONE_ITEM =
		ITEMS.register("contour_cone",
			() -> new ContourBlockItem(CONTOUR_CONE.get()));
	public static final DeferredItem<ContourBlockItem> CONTOUR_CAPITAL_ITEM =
		ITEMS.register("contour_capital",
			() -> new ContourBlockItem(CONTOUR_CAPITAL.get()));
	public static final DeferredItem<ContourBlockItem> CONTOUR_BULLNOSE_ITEM =
		ITEMS.register("contour_bullnose",
			() -> new ContourBlockItem(CONTOUR_BULLNOSE.get()));
	public static final DeferredItem<ContourBlockItem> CONTOUR_CORNICE_ITEM =
		ITEMS.register("contour_cornice",
			() -> new ContourBlockItem(CONTOUR_CORNICE.get()));
	public static final DeferredItem<ContourBlockItem> CONTOUR_BALUSTER_ITEM =
		ITEMS.register("contour_baluster",
			() -> new ContourBlockItem(CONTOUR_BALUSTER.get()));
	public static final DeferredItem<ContourBlockItem> CONTOUR_FINIAL_ITEM =
		ITEMS.register("contour_finial",
			() -> new ContourBlockItem(CONTOUR_FINIAL.get()));
	public static final DeferredItem<ContourBlockItem> CONTOUR_SCOTIA_ITEM =
		ITEMS.register("contour_scotia",
			() -> new ContourBlockItem(CONTOUR_SCOTIA.get()));
	public static final DeferredItem<ContourBlockItem> CONTOUR_OGEE_ITEM =
		ITEMS.register("contour_ogee",
			() -> new ContourBlockItem(CONTOUR_OGEE.get()));
	public static final DeferredItem<ContourBlockItem> CONTOUR_SIGN_ITEM =
		ITEMS.register("contour_sign",
			() -> new ContourBlockItem(CONTOUR_SIGN.get()));
	public static final DeferredItem<ContourBlockItem> CONTOUR_TORUS_ITEM =
		ITEMS.register("contour_torus",
			() -> new ContourBlockItem(CONTOUR_TORUS.get()));
	public static final DeferredItem<ContourBlockItem> CONTOUR_CRENELLATION_ITEM =
		ITEMS.register("contour_crenellation",
			() -> new ContourBlockItem(CONTOUR_CRENELLATION.get()));
	public static final DeferredItem<ContourBlockItem> CONTOUR_BARREL_VAULT_ITEM =
		ITEMS.register("contour_barrel_vault",
			() -> new ContourBlockItem(CONTOUR_BARREL_VAULT.get()));
	public static final DeferredItem<ContourBlockItem> CONTOUR_COG_ITEM =
		ITEMS.register("contour_cog",
			() -> new ContourBlockItem(CONTOUR_COG.get()));
	public static final DeferredItem<ContourBlockItem> CONTOUR_WHEEL_ITEM =
		ITEMS.register("contour_wheel",
			() -> new ContourBlockItem(CONTOUR_WHEEL.get()));
	public static final DeferredItem<ContourBlockItem> CONTOUR_SCAFFOLD_ITEM =
		ITEMS.register("contour_scaffold",
			() -> new ContourBlockItem(CONTOUR_SCAFFOLD.get()));
	public static final DeferredItem<ContourBlockItem> CONTOUR_GOTHIC_ARCH_ITEM =
		ITEMS.register("contour_gothic_arch",
			() -> new ContourBlockItem(CONTOUR_GOTHIC_ARCH.get()));
	public static final DeferredItem<ContourBlockItem> CONTOUR_LOUVER_ITEM =
		ITEMS.register("contour_louver",
			() -> new ContourBlockItem(CONTOUR_LOUVER.get()));
	public static final DeferredItem<ContourBlockItem> CONTOUR_ELBOW_ITEM =
		ITEMS.register("contour_elbow",
			() -> new ContourBlockItem(CONTOUR_ELBOW.get()));
	public static final DeferredItem<ContourBlockItem> CONTOUR_HANDRAIL_ITEM =
		ITEMS.register("contour_handrail",
			() -> new ContourBlockItem(CONTOUR_HANDRAIL.get()));
	public static final DeferredItem<ContourBlockItem> CONTOUR_NEWEL_ITEM =
		ITEMS.register("contour_newel",
			() -> new ContourBlockItem(CONTOUR_NEWEL.get()));
	public static final DeferredItem<ContourBlockItem> CONTOUR_BALUSTRADE_ITEM =
		ITEMS.register("contour_balustrade",
			() -> new ContourBlockItem(CONTOUR_BALUSTRADE.get()));
	public static final DeferredItem<ContourBlockItem> CONTOUR_I_BEAM_ITEM =
		ITEMS.register("contour_i_beam",
			() -> new ContourBlockItem(CONTOUR_I_BEAM.get()));
	public static final DeferredItem<ContourBlockItem> CONTOUR_PORTCULLIS_ITEM =
		ITEMS.register("contour_portcullis",
			() -> new ContourBlockItem(CONTOUR_PORTCULLIS.get()));
	public static final DeferredItem<ContourBlockItem> CONTOUR_FLANGE_ITEM =
		ITEMS.register("contour_flange",
			() -> new ContourBlockItem(CONTOUR_FLANGE.get()));
	public static final DeferredItem<ContourBlockItem> CONTOUR_BUTTRESS_ITEM =
		ITEMS.register("contour_buttress",
			() -> new ContourBlockItem(CONTOUR_BUTTRESS.get()));
	public static final DeferredItem<ContourBlockItem> CONTOUR_ARROW_SLIT_ITEM =
		ITEMS.register("contour_arrow_slit",
			() -> new ContourBlockItem(CONTOUR_ARROW_SLIT.get()));
	public static final DeferredItem<ContourBlockItem> CONTOUR_DENTIL_ITEM =
		ITEMS.register("contour_dentil",
			() -> new ContourBlockItem(CONTOUR_DENTIL.get()));
	public static final DeferredItem<ContourBlockItem> CONTOUR_TEE_ITEM =
		ITEMS.register("contour_tee",
			() -> new ContourBlockItem(CONTOUR_TEE.get()));
	public static final DeferredItem<ContourBlockItem> CONTOUR_PALISADE_ITEM =
		ITEMS.register("contour_palisade",
			() -> new ContourBlockItem(CONTOUR_PALISADE.get()));
	public static final DeferredItem<ContourBlockItem> CONTOUR_QUOIN_ITEM =
		ITEMS.register("contour_quoin",
			() -> new ContourBlockItem(CONTOUR_QUOIN.get()));
	public static final DeferredItem<ContourBlockItem> CONTOUR_TRUSS_ITEM =
		ITEMS.register("contour_truss",
			() -> new ContourBlockItem(CONTOUR_TRUSS.get()));
	public static final DeferredItem<ContourBlockItem> CONTOUR_SILL_ITEM =
		ITEMS.register("contour_sill",
			() -> new ContourBlockItem(CONTOUR_SILL.get()));
	public static final DeferredItem<ContourBlockItem> CONTOUR_BOLLARD_ITEM =
		ITEMS.register("contour_bollard",
			() -> new ContourBlockItem(CONTOUR_BOLLARD.get()));
	public static final DeferredItem<ContourBlockItem> CONTOUR_MULLION_ITEM =
		ITEMS.register("contour_mullion",
			() -> new ContourBlockItem(CONTOUR_MULLION.get()));
	public static final DeferredItem<ContourBlockItem> CONTOUR_COFFER_ITEM =
		ITEMS.register("contour_coffer",
			() -> new ContourBlockItem(CONTOUR_COFFER.get()));
	public static final DeferredItem<ContourBlockItem> CONTOUR_KEYSTONE_ITEM =
		ITEMS.register("contour_keystone",
			() -> new ContourBlockItem(CONTOUR_KEYSTONE.get()));
	public static final DeferredItem<ContourBlockItem> CONTOUR_PEDESTAL_ITEM =
		ITEMS.register("contour_pedestal",
			() -> new ContourBlockItem(CONTOUR_PEDESTAL.get()));
	public static final DeferredItem<ContourBlockItem> CONTOUR_LANCET_ITEM =
		ITEMS.register("contour_lancet",
			() -> new ContourBlockItem(CONTOUR_LANCET.get()));
	public static final DeferredItem<ContourBlockItem> CONTOUR_VALLEY_ITEM =
		ITEMS.register("contour_valley",
			() -> new ContourBlockItem(CONTOUR_VALLEY.get()));
	public static final DeferredItem<ContourBlockItem> CONTOUR_CROSS_ITEM =
		ITEMS.register("contour_cross",
			() -> new ContourBlockItem(CONTOUR_CROSS.get()));
	public static final DeferredItem<ContourBlockItem> CONTOUR_PIPE_CAP_ITEM =
		ITEMS.register("contour_pipe_cap",
			() -> new ContourBlockItem(CONTOUR_PIPE_CAP.get()));
	public static final DeferredItem<ContourBlockItem> CONTOUR_PEDIMENT_ITEM =
		ITEMS.register("contour_pediment",
			() -> new ContourBlockItem(CONTOUR_PEDIMENT.get()));
	public static final DeferredItem<ContourBlockItem> CONTOUR_CHIMNEY_ITEM =
		ITEMS.register("contour_chimney",
			() -> new ContourBlockItem(CONTOUR_CHIMNEY.get()));
	public static final DeferredItem<ContourBlockItem> CONTOUR_HALF_TIMBER_ITEM =
		ITEMS.register("contour_half_timber",
			() -> new ContourBlockItem(CONTOUR_HALF_TIMBER.get()));
	public static final DeferredItem<ContourBlockItem> CONTOUR_SPANDREL_ITEM =
		ITEMS.register("contour_spandrel",
			() -> new ContourBlockItem(CONTOUR_SPANDREL.get()));
	public static final DeferredItem<ContourBlockItem> CONTOUR_ARC_WALL_ITEM =
		ITEMS.register("contour_arc_wall",
			() -> new ContourBlockItem(CONTOUR_ARC_WALL.get()));
	public static final DeferredItem<ContourBlockItem> CONTOUR_ROSETTE_ITEM =
		ITEMS.register("contour_rosette",
			() -> new ContourBlockItem(CONTOUR_ROSETTE.get()));
	public static final DeferredItem<ContourBlockItem> CONTOUR_FLYING_BUTTRESS_ITEM =
		ITEMS.register("contour_flying_buttress",
			() -> new ContourBlockItem(CONTOUR_FLYING_BUTTRESS.get()));
	public static final DeferredItem<ContourBlockItem> CONTOUR_COPING_ITEM =
		ITEMS.register("contour_coping",
			() -> new ContourBlockItem(CONTOUR_COPING.get()));
	public static final DeferredItem<ContourBlockItem> CONTOUR_OCULUS_ITEM =
		ITEMS.register("contour_oculus",
			() -> new ContourBlockItem(CONTOUR_OCULUS.get()));
	public static final DeferredItem<ContourBlockItem> CONTOUR_AWNING_ITEM =
		ITEMS.register("contour_awning",
			() -> new ContourBlockItem(CONTOUR_AWNING.get()));
	public static final DeferredItem<ContourBlockItem> CONTOUR_BUFFER_ITEM =
		ITEMS.register("contour_buffer",
			() -> new ContourBlockItem(CONTOUR_BUFFER.get()));
	public static final DeferredItem<ContourBlockItem> CONTOUR_SMOKESTACK_ITEM =
		ITEMS.register("contour_smokestack",
			() -> new ContourBlockItem(CONTOUR_SMOKESTACK.get()));
	public static final DeferredItem<ContourBlockItem> CONTOUR_SEMAPHORE_ITEM =
		ITEMS.register("contour_semaphore",
			() -> new ContourBlockItem(CONTOUR_SEMAPHORE.get()));
	public static final DeferredItem<ContourBlockItem> CONTOUR_PILOT_ITEM =
		ITEMS.register("contour_pilot",
			() -> new ContourBlockItem(CONTOUR_PILOT.get()));
	public static final DeferredItem<ContourBlockItem> CONTOUR_VERTICAL_STEP_ITEM =
		ITEMS.register("contour_vertical_step",
			() -> new ContourBlockItem(CONTOUR_VERTICAL_STEP.get()));
	public static final DeferredItem<ContourBlockItem> CONTOUR_BEAM_ITEM =
		ITEMS.register("contour_beam",
			() -> new ContourBlockItem(CONTOUR_BEAM.get()));
	public static final DeferredItem<ContourBlockItem> CONTOUR_HALF_PANEL_ITEM =
		ITEMS.register("contour_half_panel",
			() -> new ContourBlockItem(CONTOUR_HALF_PANEL.get()));
	public static final DeferredItem<ContourBlockItem> CONTOUR_FLAT_PANE_ITEM =
		ITEMS.register("contour_flat_pane",
			() -> new ContourBlockItem(CONTOUR_FLAT_PANE.get()));

	public static final DeferredItem<ContourBlockItem> CONTOUR_CORNER_SLICE_ITEM =
		ITEMS.register("contour_corner_slice",
			() -> new ContourBlockItem(CONTOUR_CORNER_SLICE.get()));

	public static final DeferredItem<ContourBlockItem> CONTOUR_BYTE_PANEL_ITEM =
		ITEMS.register("contour_byte_panel",
			() -> new ContourBlockItem(CONTOUR_BYTE_PANEL.get()));

	public static final DeferredItem<ContourBlockItem> CONTOUR_HALF_LAYER_ITEM =
		ITEMS.register("contour_half_layer",
			() -> new ContourBlockItem(CONTOUR_HALF_LAYER.get()));

	public static final DeferredItem<ContourBlockItem> CONTOUR_IRON_DOOR_ITEM =
		ITEMS.register("contour_iron_door",
			() -> new ContourBlockItem(CONTOUR_IRON_DOOR.get()));

	public static final DeferredItem<ContourBlockItem> CONTOUR_IRON_TRAPDOOR_ITEM =
		ITEMS.register("contour_iron_trapdoor",
			() -> new ContourBlockItem(CONTOUR_IRON_TRAPDOOR.get()));

	public static final DeferredItem<ContourBlockItem> CONTOUR_SLIDING_DOOR_ITEM =
		ITEMS.register("contour_sliding_door",
			() -> new ContourBlockItem(CONTOUR_SLIDING_DOOR.get()));

	public static final DeferredItem<ContourBlockItem> CONTOUR_FOLDING_DOOR_ITEM =
		ITEMS.register("contour_folding_door",
			() -> new ContourBlockItem(CONTOUR_FOLDING_DOOR.get()));

	public static final DeferredItem<ContourBlockItem> CONTOUR_CATWALK_ITEM =
		ITEMS.register("contour_catwalk",
			() -> new ContourBlockItem(CONTOUR_CATWALK.get()));

	public static final DeferredItem<ContourCodexItem> CONTOUR_CODEX =
		ITEMS.register("contour_codex", () -> new ContourCodexItem(
			new net.minecraft.world.item.Item.Properties().stacksTo(1)));

	/**
	 * Placer feedstock: the "basic block" the place gun consumes - a
	 * plain neutral cube, NOT a ContourBlock (no material copying, no
	 * shape tag membership, not listed in shape pickers).
	 */
	public static final DeferredBlock<Block> CONTOUR_BLANK =
		BLOCKS.register("contour_blank",
			() -> new Block(BlockBehaviour.Properties.of()
				.sound(SoundType.STONE).strength(1.5f)));
	public static final DeferredItem<BlockItem> CONTOUR_BLANK_ITEM =
		ITEMS.register("contour_blank",
			() -> new BlockItem(CONTOUR_BLANK.get(),
				new net.minecraft.world.item.Item.Properties()));

	public static final DeferredItem<net.createcontour.item.ContourPlacerItem> CONTOUR_PLACER =
		ITEMS.register("contour_placer", () -> new net.createcontour.item.ContourPlacerItem(
			new net.minecraft.world.item.Item.Properties().stacksTo(1)));

	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ContourBlockEntity>> CONTOUR_BE =
		BLOCK_ENTITIES.register("contour", () -> BlockEntityType.Builder
			.of(ContourBlockEntity::new,
				CONTOUR_SLAB.get(), CONTOUR_VERTICAL_SLAB.get(), CONTOUR_STAIRS.get(),
				CONTOUR_LAYERS.get(), CONTOUR_POST.get(), CONTOUR_BYTE.get(),
				CONTOUR_PANEL.get(), CONTOUR_WALL.get(), CONTOUR_PANE.get(),
				CONTOUR_FENCE.get(), CONTOUR_SLOPE.get(), CONTOUR_TRAPDOOR.get(),
				CONTOUR_LADDER.get(), CONTOUR_FENCE_GATE.get(), CONTOUR_LEVER.get(),
				CONTOUR_DOOR.get(), CONTOUR_SLICE.get(), CONTOUR_CORNER_SLOPE.get(),
				CONTOUR_BUTTON.get(), CONTOUR_INNER_SLOPE.get(),
				CONTOUR_PRESSURE_PLATE.get(), CONTOUR_GHOST.get(), CONTOUR_TORCH.get(),
				CONTOUR_ONE_WAY.get(), CONTOUR_PILLAR.get(), CONTOUR_TRIM.get(),
				CONTOUR_FRAME.get(), CONTOUR_PYRAMID.get(), CONTOUR_LAMP.get(),
				CONTOUR_HALF_SLOPE.get(), CONTOUR_NOTCH.get(), CONTOUR_ROD.get(),
				CONTOUR_LATTICE.get(), CONTOUR_QUARTER_ROUND.get(),
				CONTOUR_CHAIN.get(), CONTOUR_ROUND_CORNER.get(),
				CONTOUR_DOME.get(), CONTOUR_RIDGE.get(), CONTOUR_TUBE.get(),
				CONTOUR_GUTTER.get(), CONTOUR_BRACKET.get(),
				CONTOUR_VERTICAL_STAIRS.get(), CONTOUR_ARCH.get(),
				CONTOUR_CYLINDER.get(), CONTOUR_PILASTER.get(),
				CONTOUR_STAIR_CORNER.get(), CONTOUR_SPHERE.get(),
				CONTOUR_CONE.get(), CONTOUR_CAPITAL.get(),
				CONTOUR_BULLNOSE.get(), CONTOUR_CORNICE.get(),
				CONTOUR_BALUSTER.get(), CONTOUR_FINIAL.get(),
				CONTOUR_SCOTIA.get(), CONTOUR_OGEE.get(),
				CONTOUR_TORUS.get(), CONTOUR_CRENELLATION.get(),
				CONTOUR_BARREL_VAULT.get(), CONTOUR_COG.get(),
				CONTOUR_WHEEL.get(), CONTOUR_SCAFFOLD.get(),
				CONTOUR_GOTHIC_ARCH.get(), CONTOUR_LOUVER.get(),
				CONTOUR_ELBOW.get(), CONTOUR_HANDRAIL.get(),
				CONTOUR_NEWEL.get(), CONTOUR_BALUSTRADE.get(),
				CONTOUR_I_BEAM.get(), CONTOUR_PORTCULLIS.get(),
				CONTOUR_FLANGE.get(), CONTOUR_BUTTRESS.get(),
				CONTOUR_ARROW_SLIT.get(), CONTOUR_DENTIL.get(),
				CONTOUR_TEE.get(), CONTOUR_PALISADE.get(),
				CONTOUR_QUOIN.get(), CONTOUR_TRUSS.get(),
				CONTOUR_SILL.get(), CONTOUR_BOLLARD.get(),
				CONTOUR_MULLION.get(), CONTOUR_COFFER.get(),
				CONTOUR_KEYSTONE.get(), CONTOUR_PEDESTAL.get(),
				CONTOUR_LANCET.get(), CONTOUR_VALLEY.get(),
				CONTOUR_CROSS.get(), CONTOUR_PIPE_CAP.get(),
				CONTOUR_PEDIMENT.get(), CONTOUR_CHIMNEY.get(),
				CONTOUR_HALF_TIMBER.get(), CONTOUR_SPANDREL.get(),
				CONTOUR_ARC_WALL.get(), CONTOUR_ROSETTE.get(),
				CONTOUR_FLYING_BUTTRESS.get(), CONTOUR_COPING.get(),
				CONTOUR_OCULUS.get(), CONTOUR_AWNING.get(),
				CONTOUR_BUFFER.get(), CONTOUR_SMOKESTACK.get(),
				CONTOUR_SEMAPHORE.get(), CONTOUR_PILOT.get(),
				CONTOUR_VERTICAL_STEP.get(), CONTOUR_BEAM.get(),
				CONTOUR_HALF_PANEL.get(), CONTOUR_FLAT_PANE.get(),
				CONTOUR_CORNER_SLICE.get(), CONTOUR_BYTE_PANEL.get(), CONTOUR_HALF_LAYER.get(), CONTOUR_IRON_DOOR.get(), CONTOUR_IRON_TRAPDOOR.get(),
				CONTOUR_SLIDING_DOOR.get(), CONTOUR_FOLDING_DOOR.get(),
				CONTOUR_CATWALK.get())
			.build(null));

	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ContourSignBlockEntity>> CONTOUR_SIGN_BE =
		BLOCK_ENTITIES.register("contour_sign", () -> BlockEntityType.Builder
			.of(ContourSignBlockEntity::new, CONTOUR_SIGN.get())
			.build(null));

	public static final DeferredHolder<CreativeModeTab, CreativeModeTab> CONTOUR_TAB =
		CREATIVE_TABS.register("contour", () -> CreativeModeTab.builder()
			.title(net.minecraft.network.chat.Component.translatable(
				"itemGroup.create_contour"))
			.icon(() -> new ItemStack(CONTOUR_SLAB_ITEM.get()))
			.displayItems((params, output) -> {
				output.accept(CONTOUR_SLAB_ITEM.get());
				output.accept(CONTOUR_VERTICAL_SLAB_ITEM.get());
				output.accept(CONTOUR_STAIRS_ITEM.get());
				output.accept(CONTOUR_LAYERS_ITEM.get());
				output.accept(CONTOUR_POST_ITEM.get());
				output.accept(CONTOUR_BYTE_ITEM.get());
				output.accept(CONTOUR_PANEL_ITEM.get());
				output.accept(CONTOUR_WALL_ITEM.get());
				output.accept(CONTOUR_PANE_ITEM.get());
				output.accept(CONTOUR_FENCE_ITEM.get());
				output.accept(CONTOUR_SLOPE_ITEM.get());
				output.accept(CONTOUR_TRAPDOOR_ITEM.get());
				output.accept(CONTOUR_LADDER_ITEM.get());
				output.accept(CONTOUR_FENCE_GATE_ITEM.get());
				output.accept(CONTOUR_LEVER_ITEM.get());
				output.accept(CONTOUR_DOOR_ITEM.get());
				output.accept(CONTOUR_SLICE_ITEM.get());
				output.accept(CONTOUR_CORNER_SLOPE_ITEM.get());
				output.accept(CONTOUR_BUTTON_ITEM.get());
				output.accept(CONTOUR_INNER_SLOPE_ITEM.get());
				output.accept(CONTOUR_PRESSURE_PLATE_ITEM.get());
				output.accept(CONTOUR_GHOST_ITEM.get());
				output.accept(CONTOUR_TORCH_ITEM.get());
				output.accept(CONTOUR_ONE_WAY_ITEM.get());
				output.accept(CONTOUR_PILLAR_ITEM.get());
				output.accept(CONTOUR_TRIM_ITEM.get());
				output.accept(CONTOUR_FRAME_ITEM.get());
				output.accept(CONTOUR_PYRAMID_ITEM.get());
				output.accept(CONTOUR_LAMP_ITEM.get());
				output.accept(CONTOUR_HALF_SLOPE_ITEM.get());
				output.accept(CONTOUR_NOTCH_ITEM.get());
				output.accept(CONTOUR_ROD_ITEM.get());
				output.accept(CONTOUR_LATTICE_ITEM.get());
				output.accept(CONTOUR_QUARTER_ROUND_ITEM.get());
				output.accept(CONTOUR_CHAIN_ITEM.get());
				output.accept(CONTOUR_ROUND_CORNER_ITEM.get());
				output.accept(CONTOUR_DOME_ITEM.get());
				output.accept(CONTOUR_RIDGE_ITEM.get());
				output.accept(CONTOUR_TUBE_ITEM.get());
				output.accept(CONTOUR_GUTTER_ITEM.get());
				output.accept(CONTOUR_BRACKET_ITEM.get());
				output.accept(CONTOUR_VERTICAL_STAIRS_ITEM.get());
				output.accept(CONTOUR_ARCH_ITEM.get());
				output.accept(CONTOUR_CYLINDER_ITEM.get());
				output.accept(CONTOUR_PILASTER_ITEM.get());
				output.accept(CONTOUR_STAIR_CORNER_ITEM.get());
				output.accept(CONTOUR_SPHERE_ITEM.get());
				output.accept(CONTOUR_CONE_ITEM.get());
				output.accept(CONTOUR_CAPITAL_ITEM.get());
				output.accept(CONTOUR_BULLNOSE_ITEM.get());
				output.accept(CONTOUR_CORNICE_ITEM.get());
				output.accept(CONTOUR_BALUSTER_ITEM.get());
				output.accept(CONTOUR_FINIAL_ITEM.get());
				output.accept(CONTOUR_SCOTIA_ITEM.get());
				output.accept(CONTOUR_OGEE_ITEM.get());
				output.accept(CONTOUR_SIGN_ITEM.get());
				output.accept(CONTOUR_TORUS_ITEM.get());
				output.accept(CONTOUR_CRENELLATION_ITEM.get());
				output.accept(CONTOUR_BARREL_VAULT_ITEM.get());
				output.accept(CONTOUR_COG_ITEM.get());
				output.accept(CONTOUR_WHEEL_ITEM.get());
				output.accept(CONTOUR_SCAFFOLD_ITEM.get());
				output.accept(CONTOUR_GOTHIC_ARCH_ITEM.get());
				output.accept(CONTOUR_LOUVER_ITEM.get());
				output.accept(CONTOUR_ELBOW_ITEM.get());
				output.accept(CONTOUR_HANDRAIL_ITEM.get());
				output.accept(CONTOUR_NEWEL_ITEM.get());
				output.accept(CONTOUR_BALUSTRADE_ITEM.get());
				output.accept(CONTOUR_I_BEAM_ITEM.get());
				output.accept(CONTOUR_PORTCULLIS_ITEM.get());
				output.accept(CONTOUR_FLANGE_ITEM.get());
				output.accept(CONTOUR_BUTTRESS_ITEM.get());
				output.accept(CONTOUR_ARROW_SLIT_ITEM.get());
				output.accept(CONTOUR_DENTIL_ITEM.get());
				output.accept(CONTOUR_TEE_ITEM.get());
				output.accept(CONTOUR_PALISADE_ITEM.get());
				output.accept(CONTOUR_QUOIN_ITEM.get());
				output.accept(CONTOUR_TRUSS_ITEM.get());
				output.accept(CONTOUR_SILL_ITEM.get());
				output.accept(CONTOUR_BOLLARD_ITEM.get());
				output.accept(CONTOUR_MULLION_ITEM.get());
				output.accept(CONTOUR_COFFER_ITEM.get());
				output.accept(CONTOUR_KEYSTONE_ITEM.get());
				output.accept(CONTOUR_PEDESTAL_ITEM.get());
				output.accept(CONTOUR_LANCET_ITEM.get());
				output.accept(CONTOUR_VALLEY_ITEM.get());
				output.accept(CONTOUR_CROSS_ITEM.get());
				output.accept(CONTOUR_PIPE_CAP_ITEM.get());
				output.accept(CONTOUR_PEDIMENT_ITEM.get());
				output.accept(CONTOUR_CHIMNEY_ITEM.get());
				output.accept(CONTOUR_HALF_TIMBER_ITEM.get());
				output.accept(CONTOUR_SPANDREL_ITEM.get());
				output.accept(CONTOUR_ARC_WALL_ITEM.get());
				output.accept(CONTOUR_ROSETTE_ITEM.get());
				output.accept(CONTOUR_FLYING_BUTTRESS_ITEM.get());
				output.accept(CONTOUR_COPING_ITEM.get());
				output.accept(CONTOUR_OCULUS_ITEM.get());
				output.accept(CONTOUR_AWNING_ITEM.get());
				output.accept(CONTOUR_BUFFER_ITEM.get());
				output.accept(CONTOUR_SMOKESTACK_ITEM.get());
				output.accept(CONTOUR_SEMAPHORE_ITEM.get());
				output.accept(CONTOUR_PILOT_ITEM.get());
				output.accept(CONTOUR_VERTICAL_STEP_ITEM.get());
				output.accept(CONTOUR_BEAM_ITEM.get());
				output.accept(CONTOUR_HALF_PANEL_ITEM.get());
				output.accept(CONTOUR_FLAT_PANE_ITEM.get());
								output.accept(CONTOUR_CORNER_SLICE_ITEM.get());
				output.accept(CONTOUR_BYTE_PANEL_ITEM.get());
				output.accept(CONTOUR_HALF_LAYER_ITEM.get());
				output.accept(CONTOUR_IRON_DOOR_ITEM.get());
				output.accept(CONTOUR_IRON_TRAPDOOR_ITEM.get());
				output.accept(CONTOUR_SLIDING_DOOR_ITEM.get());
				output.accept(CONTOUR_FOLDING_DOOR_ITEM.get());
				output.accept(CONTOUR_CATWALK_ITEM.get());
				output.accept(CONTOUR_CODEX.get());
				output.accept(CONTOUR_BLANK_ITEM.get());
				output.accept(CONTOUR_PLACER.get());
			})
			.build());

	private ContourRegistries() {
	}

	public static void register(IEventBus modBus) {
		BLOCKS.register(modBus);
		ITEMS.register(modBus);
		BLOCK_ENTITIES.register(modBus);
		CREATIVE_TABS.register(modBus);
	}
}
