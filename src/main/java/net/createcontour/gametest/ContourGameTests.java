package net.createcontour.gametest;

import java.util.List;
import net.createcontour.block.ContourSlabBlock;
import net.createcontour.block.ContourStairsBlock;
import net.createcontour.block.ContourVerticalSlabBlock;
import net.createcontour.blockentity.ContourBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.createcontour.config.ContourConfig;
import net.createcontour.item.ContourBlockItem;
import net.createcontour.material.MaterialPolicy;
import net.createcontour.material.MaterialSafety;
import net.createcontour.registry.ContourRegistries;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.block.state.properties.StairsShape;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;

/** Common-boundary GameTests for the first three Contour shapes. */
@net.neoforged.neoforge.gametest.GameTestHolder("create_contour")
@net.neoforged.neoforge.gametest.PrefixGameTestTemplate(false)
public class ContourGameTests {

	// Anchor at (2,1,2) so P.north(2)/P.west(2) stay inside the 5x5x5 area.
	private static final BlockPos P = new BlockPos(2, 1, 2);

	// --- registry / datapack ---

	@GameTest(template = "test_empty")
	public void registryAndRecipes(GameTestHelper h) {
		for (Block b : new Block[]{ContourRegistries.CONTOUR_SLAB.get(),
			ContourRegistries.CONTOUR_VERTICAL_SLAB.get(), ContourRegistries.CONTOUR_STAIRS.get()})
			h.assertTrue(BuiltInRegistries.BLOCK.getKey(b).getNamespace().equals("create_contour"),
				"block not registered");
		h.assertTrue(ContourRegistries.CONTOUR_BE.get() != null, "BE type missing");
		var rm = h.getLevel().getServer().getRecipeManager();
		for (String id : new String[]{"contour_slab", "contour_vertical_slab", "contour_stairs"})
			h.assertTrue(rm.byKey(net.minecraft.resources.ResourceLocation
				.fromNamespaceAndPath("create_contour", id)).isPresent(), "recipe missing: " + id);
		h.assertTrue(BuiltInRegistries.BLOCK
			.getTag(net.minecraft.tags.TagKey.create(Registries.BLOCK,
				ResourceLocation.fromNamespaceAndPath("create_contour", "contour_shapes")))
			.map(t -> t.size() == 114).orElse(false), "contour_shapes tag missing members");
		h.succeed();
	}

	// --- stairs parity vs vanilla oracle (all 40 states) ---

	@GameTest(template = "test_empty")
	public void stairsParity(GameTestHelper h) {
		StairBlock vanilla = (StairBlock) Blocks.OAK_STAIRS;
		for (Half half : Half.values())
			for (Direction f : Direction.Plane.HORIZONTAL)
				for (StairsShape shape : StairsShape.values()) {
					BlockState vs = vanilla.defaultBlockState()
						.setValue(StairBlock.FACING, f).setValue(StairBlock.HALF, half)
						.setValue(StairBlock.SHAPE, shape).setValue(StairBlock.WATERLOGGED, false);
					BlockState os = ContourRegistries.CONTOUR_STAIRS.get().defaultBlockState()
						.setValue(ContourStairsBlock.FACING, f).setValue(ContourStairsBlock.HALF, half)
						.setValue(ContourStairsBlock.STAIRS_SHAPE, shape).setValue(ContourStairsBlock.WATERLOGGED, false);
					var vo = vs.getShape(h.getLevel(), P, CollisionContext.empty());
					var oo = os.getShape(h.getLevel(), P, CollisionContext.empty());
					h.assertTrue(!Shapes.joinIsNotEmpty(vo, oo, BooleanOp.NOT_SAME),
						"voxel mismatch " + half + "/" + f + "/" + shape);
					for (Rotation r : Rotation.values())
						h.assertTrue(stateEq(os.rotate(r), vs.rotate(r)),
							"rotate mismatch " + r);
					for (Mirror m : Mirror.values())
						h.assertTrue(stateEq(os.mirror(m), vs.mirror(m)),
							"mirror mismatch " + m);
				}
		h.succeed();
	}

	private static boolean stateEq(BlockState ours, BlockState vanilla) {
		return ours.getValue(ContourStairsBlock.FACING) == vanilla.getValue(StairBlock.FACING)
			&& ours.getValue(ContourStairsBlock.HALF) == vanilla.getValue(StairBlock.HALF)
			&& ours.getValue(ContourStairsBlock.STAIRS_SHAPE) == vanilla.getValue(StairBlock.SHAPE);
	}

	// --- stair connection form/unform ---

	@GameTest(template = "test_empty")
	public void stairsConnections(GameTestHelper h) {
		h.setBlock(P, ContourRegistries.CONTOUR_STAIRS.get().defaultBlockState()
			.setValue(ContourStairsBlock.FACING, Direction.NORTH));
		// Perpendicular stair on the low (SOUTH) side -> INNER corner.
		h.setBlock(P.south(), Blocks.OAK_STAIRS.defaultBlockState()
			.setValue(StairBlock.FACING, Direction.WEST));
		BlockState s = h.getBlockState(P);
		h.assertTrue(s.getValue(ContourStairsBlock.STAIRS_SHAPE) == StairsShape.INNER_RIGHT
			|| s.getValue(ContourStairsBlock.STAIRS_SHAPE) == StairsShape.INNER_LEFT,
			"expected inner corner, got " + s.getValue(ContourStairsBlock.STAIRS_SHAPE));
		h.setBlock(P.south(), Blocks.AIR);
		h.assertTrue(h.getBlockState(P).getValue(ContourStairsBlock.STAIRS_SHAPE)
			== StairsShape.STRAIGHT, "corner did not revert after removal");
		h.succeed();
	}

	// --- slab / vertical geometry states ---

	@GameTest(template = "test_empty")
	public void slabAndVerticalStates(GameTestHelper h) {
		BlockState b = ContourRegistries.CONTOUR_SLAB.get().defaultBlockState()
			.setValue(ContourSlabBlock.TYPE, SlabType.BOTTOM);
		h.setBlock(P, b);
		var bs = h.getBlockState(P).getShape(h.getLevel(), P, CollisionContext.empty());
		h.assertTrue(!Shapes.joinIsNotEmpty(bs, Block.box(0, 8, 0, 16, 16, 16), BooleanOp.AND)
			&& Shapes.joinIsNotEmpty(bs, Block.box(0, 0, 0, 16, 8, 16), BooleanOp.AND),
			"bottom slab shape wrong");
		BlockState d = b.setValue(ContourSlabBlock.TYPE, SlabType.DOUBLE)
			.setValue(ContourSlabBlock.WATERLOGGED, false);
		h.assertTrue(ContourRegistries.CONTOUR_SLAB.get().hostUnits(d) == 2, "double units!=2");
		for (Direction f : Direction.Plane.HORIZONTAL) {
			BlockState vs = ContourRegistries.CONTOUR_VERTICAL_SLAB.get().defaultBlockState()
				.setValue(ContourVerticalSlabBlock.FACING, f);
			h.assertTrue(!vs.getShape(h.getLevel(), P, CollisionContext.empty()).isEmpty(),
				"vertical slab shape empty " + f);
		}
		h.succeed();
	}

	// --- BE persistence / schema ---

	private ContourBlockEntity be(GameTestHelper h, BlockState s) {
		return be(h, P, s);
	}

	private ContourBlockEntity be(GameTestHelper h, BlockPos pos, BlockState s) {
		h.setBlock(pos, s);
		return (ContourBlockEntity) h.getLevel().getBlockEntity(h.absolutePos(pos));
	}

	@GameTest(template = "test_empty")
	public void bePersistence(GameTestHelper h) {
		ContourBlockEntity be = be(h, ContourRegistries.CONTOUR_SLAB.get().defaultBlockState());
		BlockState log = Blocks.OAK_LOG.defaultBlockState()
			.setValue(BlockStateProperties.AXIS, Direction.Axis.X);
		be.applyPayload(log, 1, Direction.EAST, true);
		var provider = h.getLevel().registryAccess();
		CompoundTag tag = be.saveWithFullMetadata(provider);
		h.assertTrue(tag.getInt("ContourVersion") == ContourBlockEntity.SCHEMA_VERSION, "version");
		ContourBlockEntity be2 = new ContourBlockEntity(P,
			ContourRegistries.CONTOUR_SLAB.get().defaultBlockState());
		be2.loadWithComponents(tag, provider);
		h.assertTrue(be2.getMaterial().getValue(BlockStateProperties.AXIS) == Direction.Axis.X,
			"material axis lost");
		h.assertTrue(be2.getPaidUnits() == 1 && be2.getFacing() == Direction.EAST && be2.isTop(),
			"fields not preserved");

		// invalid / unknown-registry material -> neutral + paid 0
		CompoundTag bad = tag.copy();
		CompoundTag m = new CompoundTag();
		m.putString("Name", "create_contour:does_not_exist");
		bad.put("Material", m);
		bad.putInt("PaidUnits", 7);
		ContourBlockEntity be3 = new ContourBlockEntity(P,
			ContourRegistries.CONTOUR_SLAB.get().defaultBlockState());
		be3.loadWithComponents(bad, provider);
		h.assertTrue(MaterialSafety.isNeutral(be3.getMaterial()) && be3.getPaidUnits() == 0,
			"invalid material not neutralized");

		// v0 -> v1 migration (no version key) parses material, defaults paid
		CompoundTag v0 = new CompoundTag();
		v0.put("Material", net.minecraft.nbt.NbtUtils.writeBlockState(log));
		ContourBlockEntity be4 = new ContourBlockEntity(P,
			ContourRegistries.CONTOUR_SLAB.get().defaultBlockState());
		be4.loadWithComponents(v0, provider);
		h.assertTrue(be4.getMaterial().getBlock() == Blocks.OAK_LOG && be4.getPaidUnits() == 0,
			"v0 migration failed");

		// future v99: raw preserved on save + frozen + writeSafe carries no credit
		CompoundTag future = tag.copy();
		future.putInt("ContourVersion", 99);
		future.putInt("PaidUnits", 2);
		future.putString("FutureField", "keepme");
		ContourBlockEntity be5 = new ContourBlockEntity(P,
			ContourRegistries.CONTOUR_SLAB.get().defaultBlockState());
		be5.loadWithComponents(future, provider);
		h.assertTrue(be5.isFrozen(), "v99 not frozen");
		// future tag over a PAINTED BE: raw preserved, operational view reset
		be.applyPayload(Blocks.GLOWSTONE.defaultBlockState(), 1, Direction.NORTH, false);
		be.loadWithComponents(future, provider);
		h.assertTrue(be.isFrozen() && MaterialSafety.isNeutral(be.getMaterial())
			&& be.getPaidUnits() == 0 && be.getFacing() == Direction.NORTH
			&& !be.isTop(), "frozen keeps stale paint");
		h.assertTrue(be.saveWithFullMetadata(provider).equals(future),
			"raw future compound not fully retained");
		// paid clamp uses a FRESH unfrozen BE at a DIFFERENT pos (the frozen
		// one at P refuses applyPayload by design)
		BlockPos other = P.above();
		h.setBlock(other, ContourRegistries.CONTOUR_SLAB.get().defaultBlockState());
		ContourBlockEntity clamp = (ContourBlockEntity) h.getLevel()
			.getBlockEntity(h.absolutePos(other));
		clamp.applyPayload(Blocks.STONE.defaultBlockState(), 7, Direction.NORTH, false);
		h.assertTrue(clamp.getPaidUnits() == 1, "paid clamp != hostUnits 1");
		CompoundTag saved = be5.saveWithFullMetadata(provider);
		h.assertTrue(saved.getInt("ContourVersion") == 99
			&& "keepme".equals(saved.getString("FutureField")), "raw future payload lost");
		CompoundTag safe = new CompoundTag();
		be5.writeSafe(safe, provider);
		h.assertTrue(safe.getInt("PaidUnits") == 0 && !safe.contains("Item"),
			"writeSafe leaked credit/item");

		// FROZEN MUTATION REFUSAL: attempts to mutate the frozen BE are
		// refused - operational view stays neutral/0 and the raw future
		// compound is still fully retained
		be.applyPayload(log, Integer.MAX_VALUE, Direction.NORTH, false);
		h.assertTrue(MaterialSafety.isNeutral(be.getMaterial())
			&& be.getPaidUnits() == 0
			&& be.saveWithFullMetadata(provider).equals(future),
			"frozen accepted mutation");
		h.succeed();
	}

	// --- accounting regressions (lead-authored diagnostic cases) ---

	@GameTest(template = "test_empty")
	public void creativeBreakRefundAndBlankPlace(GameTestHelper h) {
		boolean wasConsume = ContourConfig.consumeMaterial();
		try {
			ContourConfig.CONSUME_MATERIAL.set(true);
			// paid-1 slab: drops are blank shape + material refund
			ContourBlockEntity src = be(h,
				ContourRegistries.CONTOUR_SLAB.get().defaultBlockState());
			src.applyPayload(Blocks.STONE.defaultBlockState(), 1, Direction.NORTH, false);
			var drops = ((ContourSlabBlock) h.getBlockState(P).getBlock())
				.ledgerDrops(h.getBlockState(P), src);
			h.assertTrue(drops.size() == 2
				&& drops.get(0).getItem() instanceof ContourBlockItem
				&& ContourBlockItem.storedMaterial(h.getLevel().registryAccess(),
					drops.get(0)) == null
				&& drops.get(1).is(Items.STONE) && drops.get(1).getCount() == 1,
				"expected blank shape + stone refund, got " + drops);
			ItemStack blank = drops.get(0);
			h.setBlock(P, Blocks.AIR); // remove credit origin
			Player c = player(h);
			c.getAbilities().instabuild = true; // creative
			h.setBlock(P.south(2), Blocks.STONE);
			c.setItemInHand(InteractionHand.MAIN_HAND, blank);
			h.assertTrue(c.getOffhandItem().isEmpty(), "offhand not empty");
			blank.getItem().useOn(ctx(h, c, blank, P.south(2), Direction.UP));
			ContourBlockEntity nbe = (ContourBlockEntity) h.getLevel()
				.getBlockEntity(h.absolutePos(P.south(2).above()));
			h.assertTrue(nbe != null && nbe.getMaterial().getBlock() == Blocks.STONE,
				"creative look-paint failed");
			h.assertTrue(nbe.getPaidUnits() == 0,
				"creative placement minted credit: " + nbe.getPaidUnits());
			h.assertTrue(c.getMainHandItem().getCount() == 1,
				"creative held shape not intact");
		} finally {
			ContourConfig.CONSUME_MATERIAL.set(wasConsume);
		}
		h.succeed();
	}

	@GameTest(template = "test_empty")
	public void unsupportedStoredNoConsume(GameTestHelper h) {
		boolean wasConsume = ContourConfig.consumeMaterial();
		try {
			ContourConfig.CONSUME_MATERIAL.set(true);
			Player p = player(h);
			// blank frame whose stored material is an unsupported block
			ItemStack frame = new ItemStack(ContourRegistries.CONTOUR_SLAB_ITEM.get());
			ContourBlockItem.writePayload(frame, Blocks.CHEST.defaultBlockState(),
				0, Direction.NORTH, false);
			p.setItemInHand(InteractionHand.MAIN_HAND, frame);
			p.getInventory().add(new ItemStack(Items.CHEST, 1));
			h.setBlock(P.east(2), Blocks.STONE);
			var result = frame.getItem().useOn(ctx(h, p, frame, P.east(2), Direction.UP));
			h.assertTrue(result.consumesAction(),
				"neutral fallback placement must still place");
			ContourBlockEntity nbe = (ContourBlockEntity) h.getLevel()
				.getBlockEntity(h.absolutePos(P.east(2).above()));
			h.assertTrue(nbe != null, "no BE after fallback placement");
			h.assertTrue(MaterialSafety.isNeutral(nbe.getMaterial())
				&& nbe.getPaidUnits() == 0,
				"unsupported stored material was not sanitized");
			h.assertTrue(p.getInventory().countItem(
				ContourRegistries.CONTOUR_SLAB_ITEM.get()) == 0,
				"frame not consumed once");
			h.assertTrue(p.getInventory().countItem(Items.CHEST) == 1,
				"unsupported stored material consumed the real chest");
		} finally {
			ContourConfig.CONSUME_MATERIAL.set(wasConsume);
		}
		h.succeed();
	}

	@GameTest(template = "test_empty")
	public void missingLegacyNoRetarget(GameTestHelper h) {
		boolean wasConsume = ContourConfig.consumeMaterial();
		try {
			ContourConfig.CONSUME_MATERIAL.set(true);
			// unpaid stone bottom-slab target
			ContourBlockEntity target = be(h,
				ContourRegistries.CONTOUR_SLAB.get().defaultBlockState());
			target.applyPayload(Blocks.STONE.defaultBlockState(), 0, Direction.NORTH, false);
			Player p = player(h);
			// legacy-id frame for a removed material, stale paid 1
			ItemStack frame = new ItemStack(ContourRegistries.CONTOUR_SLAB_ITEM.get());
			CustomData.update(DataComponents.CUSTOM_DATA, frame, t -> {
				t.putString("contour_material_id", "missing_mod:removed");
				t.putInt("contour_paid", 1);
			});
			p.setItemInHand(InteractionHand.MAIN_HAND, frame);
			p.setItemInHand(InteractionHand.OFF_HAND, new ItemStack(Items.STONE, 1));
			frame.getItem().useOn(ctx(h, p, frame, P, Direction.UP));
			ContourBlockEntity fresh = (ContourBlockEntity) h.getLevel()
				.getBlockEntity(h.absolutePos(P));
			h.assertTrue(fresh != null && fresh.getPaidUnits() == 0,
				"stale legacy credit retargeted into merge: "
					+ (fresh == null ? -1 : fresh.getPaidUnits()));
			h.assertTrue(p.getOffhandItem().getCount() == 1,
				"offhand stone consumed by invalid stored material");
			// no neighbor (incl UP, the real adjacent cell) may mint credit
			for (Direction d : Direction.values()) {
				BlockPos adj = h.absolutePos(P.relative(d));
				var b = h.getLevel().getBlockEntity(adj);
				if (b instanceof ContourBlockEntity cbe)
					h.assertTrue(cbe.getPaidUnits() == 0,
						"neighbor host minted credit at " + d);
			}
		} finally {
			ContourConfig.CONSUME_MATERIAL.set(wasConsume);
		}
		h.succeed();
	}

	@GameTest(template = "test_empty")
	public void creativeCtrlPickNoMergeSeed(GameTestHelper h) {
		boolean wasConsume = ContourConfig.consumeMaterial();
		try {
			ContourConfig.CONSUME_MATERIAL.set(true);
			// source paid stone slab + creative clone (paid 0 template) carrying
			// the source's full BE tag as vanilla Ctrl+pick would attach
			ContourBlockEntity src = be(h, P.east(2),
				ContourRegistries.CONTOUR_SLAB.get().defaultBlockState());
			src.applyPayload(Blocks.STONE.defaultBlockState(), 1, Direction.NORTH, false);
			ItemStack picked = ((ContourSlabBlock) h.getBlockState(P.east(2)).getBlock())
				.getCloneItemStack(h.getBlockState(P.east(2)), null, h.getLevel(),
					h.absolutePos(P.east(2)), null);
			h.assertTrue(ContourBlockItem.storedPaid(picked) == 0, "clone paid");
			picked.set(DataComponents.BLOCK_ENTITY_DATA,
				CustomData.of(src.saveWithFullMetadata(h.getLevel().registryAccess())));
			// unpaid stone target
			ContourBlockEntity target = be(h,
				ContourRegistries.CONTOUR_SLAB.get().defaultBlockState());
			target.applyPayload(Blocks.STONE.defaultBlockState(), 0, Direction.NORTH, false);
			// real ServerPlayer for hasPermissions(4): constructed directly to
			// skip the playerLoggedIn event (Create's TrackGraphSync packet
			// crashes the makeMockServerPlayerInLevel path)
			ServerPlayer c = new ServerPlayer(h.getLevel().getServer(), h.getLevel(),
				new com.mojang.authlib.GameProfile(java.util.UUID.randomUUID(), "qactrl"),
				net.minecraft.server.level.ClientInformation.createDefault());
			c.getAbilities().instabuild = true;
			// vanilla only honors BLOCK_ENTITY_DATA with gm-block permission;
			// preserve and restore any prior op entry for this profile
			var server = h.getLevel().getServer();
			var ops = server.getPlayerList().getOps();
			var priorOp = ops.get(c.getGameProfile());
			ops.add(new net.minecraft.server.players.ServerOpListEntry(
				c.getGameProfile(), 4, false));
			try {
				h.assertTrue(c.canUseGameMasterBlocks(),
					"mock player lacks permission 4 - BE data not exercised");
				c.setItemInHand(InteractionHand.MAIN_HAND, picked);
				picked.getItem().useOn(ctx(h, c, picked, P, Direction.UP));
				ContourBlockEntity fresh = (ContourBlockEntity) h.getLevel()
					.getBlockEntity(h.absolutePos(P));
				h.assertTrue(h.getBlockState(P).getValue(ContourSlabBlock.TYPE)
					== net.minecraft.world.level.block.state.properties.SlabType.DOUBLE,
					"merge did not produce double");
				h.assertTrue(fresh != null && fresh.getMaterial().getBlock() == Blocks.STONE,
					"merge lost stone");
				h.assertTrue(fresh != null && fresh.getPaidUnits() == 0,
					"imported BE data seeded merge credit: "
						+ (fresh == null ? -1 : fresh.getPaidUnits()));
				h.assertTrue(ContourBlockItem.storedPaid(c.getMainHandItem()) == 0,
					"template gained credit");
			} finally {
				ops.remove(c.getGameProfile());
				if (priorOp != null)
					ops.add(priorOp);
			}
		} finally {
			ContourConfig.CONSUME_MATERIAL.set(wasConsume);
		}
		h.succeed();
	}

	// --- material acceptance ---

	@GameTest(template = "test_empty")
	public void materialAcceptance(GameTestHelper h) {
		h.assertTrue(MaterialSafety.isAccepted(Blocks.OAK_SLAB.defaultBlockState()),
			"slab material rejected");
		h.assertTrue(MaterialSafety.isAccepted(Blocks.GLASS.defaultBlockState()),
			"glass rejected");
		h.assertTrue(MaterialSafety.isAccepted(Blocks.OAK_LEAVES.defaultBlockState()),
			"leaves rejected");
		h.assertTrue(!MaterialSafety.isAccepted(Blocks.AIR.defaultBlockState()), "air accepted");
		h.assertTrue(!MaterialSafety.isAccepted(
			ContourRegistries.CONTOUR_SLAB.get().defaultBlockState()), "recursion accepted");
		h.assertTrue(!MaterialSafety.isAccepted(Blocks.CHEST.defaultBlockState()), "BE accepted");
		h.assertTrue(!MaterialSafety.isAccepted(Blocks.WATER.defaultBlockState()), "fluid accepted");
		h.succeed();
	}

	// --- paint economics ---

	// makeMockServerPlayerInLevel fires playerLoggedIn, which crashes under
	// Create 6.0.10 (TrackGraphSync sends an unregistered payload to the mock
	// connection). makeMockPlayer returns a plain mock without login events.
	private Player player(GameTestHelper h) {
		return h.makeMockPlayer(GameType.SURVIVAL);
	}

	@GameTest(template = "test_empty")
	public void visualMaterialWithoutCollision(GameTestHelper h) {
		BlockState torch = Blocks.TORCH.defaultBlockState();
		h.assertTrue(torch.getCollisionShape(EmptyBlockGetter.INSTANCE, BlockPos.ZERO).isEmpty(),
			"fixture must have no physical collision");
		h.assertTrue(!torch.getShape(EmptyBlockGetter.INSTANCE, BlockPos.ZERO).isEmpty(),
			"fixture must have a visible selection outline");
		h.assertTrue(!MaterialSafety.isAccepted(Blocks.LIGHT.defaultBlockState()),
			"empty-outline material should remain rejected");
		h.assertTrue(MaterialSafety.isAccepted(torch),
			"visual material must not depend on physical collision");
		boolean wasConsume = ContourConfig.consumeMaterial();
		try {
			ContourConfig.CONSUME_MATERIAL.set(true);
			Player p = player(h);
			p.getInventory().add(new ItemStack(Items.TORCH, 3));
			Block[] hosts = {ContourRegistries.CONTOUR_SLAB.get(),
				ContourRegistries.CONTOUR_VERTICAL_SLAB.get(), ContourRegistries.CONTOUR_STAIRS.get()};
			for (int i = 0; i < hosts.length; i++) {
				ContourBlockEntity target = be(h, P.offset(i - 1, 0, 0), hosts[i].defaultBlockState());
				h.assertTrue(MaterialPolicy.paint(h.getLevel(), target, p, torch),
					"noncolliding visual paint rejected on host " + i);
				h.assertTrue(target.getMaterial() == torch && target.getPaidUnits() == 1,
					"noncolliding visual paint lost material or credit on host " + i);
				h.assertTrue(p.getInventory().countItem(Items.TORCH) == 2 - i,
					"noncolliding visual material was not consumed exactly once");
			}
		} finally {
			ContourConfig.CONSUME_MATERIAL.set(wasConsume);
		}
		h.succeed();
	}

	@GameTest(template = "test_empty")
	public void paintEconomics(GameTestHelper h) {
		ServerLevel level = h.getLevel();
		Player p = player(h);
		ContourBlockEntity be = be(h, ContourRegistries.CONTOUR_SLAB.get().defaultBlockState());
		BlockState oak = Blocks.OAK_PLANKS.defaultBlockState();

		ContourConfig.CONSUME_MATERIAL.set(true);
		p.getInventory().add(new ItemStack(Items.OAK_PLANKS, 1));
		h.assertTrue(MaterialPolicy.paint(level, be, p, oak), "paint failed with item present");
		h.assertTrue(be.getPaidUnits() == 1 && be.getMaterial().getBlock() == Blocks.OAK_PLANKS,
			"paid ledger wrong");
		h.assertTrue(p.getInventory().countItem(Items.OAK_PLANKS) == 0, "item not consumed");

		// missing matching item: reject atomically
		p.getInventory().clearContent();
		BlockState old = be.getMaterial();
		h.assertTrue(!MaterialPolicy.paint(level, be, p, Blocks.STONE.defaultBlockState()),
			"missing-item paint should fail");
		h.assertTrue(be.getMaterial().getBlock() == old.getBlock() && be.getPaidUnits() == 1,
			"failed paint mutated state");

		// repaint: new consumption validated, old paid refunded
		p.getInventory().add(new ItemStack(Items.STONE, 1));
		h.assertTrue(MaterialPolicy.paint(level, be, p, Blocks.STONE.defaultBlockState()),
			"repaint failed");
		h.assertTrue(p.getInventory().countItem(Items.OAK_PLANKS) == 1,
			"old paid unit not refunded");
		h.assertTrue(p.getInventory().countItem(Items.STONE) == 0, "new item not consumed");

		// config OFF: zero cost, paid ledger zeroed for new paint
		ContourConfig.CONSUME_MATERIAL.set(false);
		h.assertTrue(MaterialPolicy.paint(level, be, p, Blocks.DIORITE.defaultBlockState()),
			"config-off paint failed");
		h.assertTrue(be.getPaidUnits() == 0, "config-off paint minted credit");
		ContourConfig.CONSUME_MATERIAL.set(true);

		// same-material re-axis: free, ledger preserved
		be.applyPayload(oak, 1, Direction.NORTH, false);
		BlockState logX = Blocks.OAK_LOG.defaultBlockState()
			.setValue(BlockStateProperties.AXIS, Direction.Axis.X);
		// (same block check uses same block identity; log vs planks differ -
		// use log->log axis change instead)
		be.setMaterial(logX);
		int before = p.getInventory().countItem(Items.OAK_LOG);
		h.assertTrue(MaterialPolicy.paint(level, be, p,
			logX.setValue(BlockStateProperties.AXIS, Direction.Axis.Z)), "reaxis failed");
		h.assertTrue(p.getInventory().countItem(Items.OAK_LOG) == before, "reaxis charged");
		h.succeed();
	}

	// --- wrench cycle / double reject / sneak-wrench ---

	// Discriminator for the pack finding: a REAL survival break must emit
	// the ledger-carrying shape stack through dropResources->handleBlockDrops.
	@GameTest(template = "test_empty")
	public void survivalBreakDropsBlankAndMaterial(GameTestHelper h) {
		Player p = player(h);
		ContourBlockEntity be = be(h, ContourRegistries.CONTOUR_STAIRS.get()
			.defaultBlockState());
		be.applyPayload(Blocks.STONE.defaultBlockState(), 1, Direction.NORTH, true);
		p.setPos(Vec3.atCenterOf(h.absolutePos(P)));
		p.getInventory().clearContent();
		boolean removed = h.getLevel().destroyBlock(h.absolutePos(P), true, p);
		h.assertTrue(removed, "host not removed by destroyBlock");
		h.assertTrue(h.getLevel().getBlockState(h.absolutePos(P)).isAir(),
			"host did not become air");
		var box = new net.minecraft.world.phys.AABB(h.absolutePos(P)).inflate(3);
		var items = h.getLevel().getEntities(net.minecraft.world.entity.EntityType.ITEM,
			box, e -> e.getItem().getItem() instanceof ContourBlockItem);
		h.assertTrue(items.size() == 1, "expected exactly 1 shape stack, got " + items.size());
		var stack = items.get(0).getItem();
		h.assertTrue(stack.getCount() == 1, "shape stack count " + stack.getCount());
		h.assertTrue(ContourBlockItem.storedMaterial(h.getLevel().registryAccess(), stack)
			== null && ContourBlockItem.storedPaid(stack) == 0,
			"shape drop carried paint payload");
		var mats = h.getLevel().getEntities(net.minecraft.world.entity.EntityType.ITEM,
			box, e -> e.getItem().is(Items.STONE));
		h.assertTrue(mats.size() == 1 && mats.get(0).getItem().getCount() == 1,
			"paid material not refunded as item");
		h.succeed();
	}

	@GameTest(template = "test_empty")
	public void wrenchCycleAndPickup(GameTestHelper h) {
		Player p = h.makeMockPlayer(GameType.SURVIVAL);
		ContourBlockEntity be = be(h, ContourRegistries.CONTOUR_SLAB.get().defaultBlockState());
		be.applyPayload(Blocks.OAK_PLANKS.defaultBlockState(), 1, Direction.EAST, true);
		BlockState state = h.getBlockState(P);
		var res = ((ContourSlabBlock) state.getBlock()).onWrenched(state,
			new net.minecraft.world.item.context.UseOnContext(h.getLevel(), p,
				InteractionHand.MAIN_HAND, ItemStack.EMPTY,
				new BlockHitResult(Vec3.atCenterOf(h.absolutePos(P)), Direction.UP,
					h.absolutePos(P), false)));
		h.assertTrue(res.consumesAction(), "wrench cycle not consumed");
		h.assertTrue(h.getBlockState(P).getBlock() instanceof ContourVerticalSlabBlock,
			"cycle did not move to vertical slab");
		ContourBlockEntity nbe = (ContourBlockEntity) h.getLevel()
			.getBlockEntity(h.absolutePos(P));
		h.assertTrue(nbe.getMaterial().getBlock() == Blocks.OAK_PLANKS && nbe.getPaidUnits() == 1
			&& nbe.getFacing() == Direction.EAST && nbe.isTop(), "cycle lost owned state");

		// double slab rejects cycle
		ContourBlockEntity dbe = be(h, ContourRegistries.CONTOUR_SLAB.get().defaultBlockState()
			.setValue(ContourSlabBlock.TYPE, SlabType.DOUBLE));
		var res2 = ((ContourSlabBlock) h.getBlockState(P).getBlock()).onWrenched(
			h.getBlockState(P), new net.minecraft.world.item.context.UseOnContext(
				h.getLevel(), p, InteractionHand.MAIN_HAND, ItemStack.EMPTY,
				new BlockHitResult(Vec3.atCenterOf(h.absolutePos(P)), Direction.UP,
					h.absolutePos(P), false)));
		h.assertTrue(res2 == net.minecraft.world.InteractionResult.FAIL, "double cycle allowed");
		h.succeed();
	}

	// --- sneak + shape item = plain adjacent placement ---

	@GameTest(template = "test_empty")
	public void sneakPlaceAdjacent(GameTestHelper h) {
		boolean wasConsume = ContourConfig.consumeMaterial();
		try {
			ContourConfig.CONSUME_MATERIAL.set(true);
			Player p = player(h);
			p.setShiftKeyDown(true);
			ContourBlockEntity be = be(h, ContourRegistries.CONTOUR_STAIRS.get().defaultBlockState());
			ItemStack template = new ItemStack(ContourRegistries.CONTOUR_SLAB_ITEM.get());
			ContourBlockItem.writePayload(template, Blocks.OAK_PLANKS.defaultBlockState(), 0,
				Direction.NORTH, false);
			p.setItemInHand(InteractionHand.MAIN_HAND, template);
			p.getInventory().add(new ItemStack(Items.OAK_PLANKS, 1));
			h.assertTrue(template.getItem().useOn(ctx(h, p, template, P, Direction.UP))
				.consumesAction(), "sneak place not consumed");
			h.assertTrue(MaterialSafety.isNeutral(be.getMaterial()),
				"sneak place repainted the clicked target");
			ContourBlockEntity placed = (ContourBlockEntity) h.getLevel()
				.getBlockEntity(h.absolutePos(P.above()));
			h.assertTrue(placed != null
				&& placed.getMaterial().getBlock() == Blocks.OAK_PLANKS
				&& placed.getPaidUnits() == 1, "adjacent placement lost paint/credit");
			h.assertTrue(p.getInventory().countItem(
				ContourRegistries.CONTOUR_SLAB_ITEM.get()) == 0, "shape item not consumed");
		} finally {
			ContourConfig.CONSUME_MATERIAL.set(wasConsume);
		}
		h.succeed();
	}

	// --- pick-block returns the blank item ---

	@GameTest(template = "test_empty")
	public void pickBlockIsBlank(GameTestHelper h) {
		ContourBlockEntity be = be(h, ContourRegistries.CONTOUR_SLAB.get().defaultBlockState());
		be.applyPayload(Blocks.OAK_PLANKS.defaultBlockState(), 1, Direction.NORTH, false);
		ItemStack picked = ((ContourSlabBlock) h.getBlockState(P).getBlock())
			.getCloneItemStack(h.getBlockState(P), null, h.getLevel(),
				h.absolutePos(P), null);
		h.assertTrue(ContourBlockItem.storedPaid(picked) == 0
			&& ContourBlockItem.storedMaterial(h.getLevel().registryAccess(), picked) == null,
			"pick carried paint payload");
		h.succeed();
	}

	// --- placement escrow + useOn paths (this batch) ---

	private UseOnContext ctx(GameTestHelper h, Player p, ItemStack held,
		BlockPos pos, Direction face) {
		return new UseOnContext(h.getLevel(), p, InteractionHand.MAIN_HAND, held,
			new BlockHitResult(Vec3.atCenterOf(h.absolutePos(pos)), face,
				h.absolutePos(pos), false));
	}

	@GameTest(template = "test_empty", batch = "contour_config")
	public void itemPlaceAccounting(GameTestHelper h) {
		try {
			ContourConfig.CONSUME_MATERIAL.set(true);
			Player p = player(h);
			BlockPos floor = P;
			h.setBlock(floor, Blocks.STONE);

			// template paint + 1 oak + last frame -> cost 1, paid 1
			ItemStack frame = new ItemStack(ContourRegistries.CONTOUR_SLAB_ITEM.get());
			ContourBlockItem.writePayload(frame, Blocks.OAK_PLANKS.defaultBlockState(),
				0, Direction.NORTH, false);
			p.setItemInHand(InteractionHand.MAIN_HAND, frame);
			p.getInventory().add(new ItemStack(Items.OAK_PLANKS, 1));
			h.assertTrue(frame.getItem().useOn(ctx(h, p, frame, floor, Direction.UP))
				.consumesAction(), "placement not consumed");
			ContourBlockEntity be = (ContourBlockEntity) h.getLevel()
				.getBlockEntity(h.absolutePos(floor.above()));
			h.assertTrue(be != null && be.getMaterial().getBlock() == Blocks.OAK_PLANKS
				&& be.getPaidUnits() == 1, "escrowed paint ledger wrong");
			h.assertTrue(p.getInventory().countItem(Items.OAK_PLANKS) == 0
				&& p.getInventory().countItem(ContourRegistries.CONTOUR_SLAB_ITEM.get()) == 0,
				"oak/frame not consumed once each");

			// stored > offhand > look priority: stored template wins
			Player p2 = player(h);
			h.setBlock(P.east(2), Blocks.STONE);
			ItemStack frame2 = new ItemStack(ContourRegistries.CONTOUR_SLAB_ITEM.get());
			ContourBlockItem.writePayload(frame2, Blocks.STONE.defaultBlockState(),
				0, Direction.NORTH, false);
			p2.setItemInHand(InteractionHand.MAIN_HAND, frame2);
			p2.setItemInHand(InteractionHand.OFF_HAND, new ItemStack(Items.DIRT, 3));
			p2.getInventory().add(new ItemStack(Items.STONE, 1));
			h.assertTrue(frame2.getItem().useOn(ctx(h, p2, frame2, P.east(2), Direction.UP))
				.consumesAction(), "stored-priority place failed");
			ContourBlockEntity be2 = (ContourBlockEntity) h.getLevel()
				.getBlockEntity(h.absolutePos(P.east(2).above()));
			h.assertTrue(be2.getMaterial().getBlock() == Blocks.STONE, "stored lost to offhand");
			h.assertTrue(p2.getOffhandItem().getCount() == 3
				&& p2.getInventory().countItem(Items.STONE) == 0, "offhand paid instead of stored");

			// offhand-only payment counts
			Player p3 = player(h);
			h.setBlock(P.west(2), Blocks.STONE);
			ItemStack frame3 = new ItemStack(ContourRegistries.CONTOUR_SLAB_ITEM.get());
			p3.setItemInHand(InteractionHand.MAIN_HAND, frame3);
			p3.setItemInHand(InteractionHand.OFF_HAND, new ItemStack(Items.DIORITE, 1));
			h.assertTrue(frame3.getItem().useOn(ctx(h, p3, frame3, P.west(2), Direction.UP))
				.consumesAction(), "offhand-only place failed");
			ContourBlockEntity be3 = (ContourBlockEntity) h.getLevel()
				.getBlockEntity(h.absolutePos(P.west(2).above()));
			h.assertTrue(be3.getMaterial().getBlock() == Blocks.DIORITE
				&& be3.getPaidUnits() == 1 && p3.getOffhandItem().isEmpty(),
				"offhand-only payment failed");

			// missing matching material -> FAIL before any mutation
			Player p4 = player(h);
			h.setBlock(P.north(2), Blocks.STONE);
			ItemStack frame4 = new ItemStack(ContourRegistries.CONTOUR_SLAB_ITEM.get());
			ContourBlockItem.writePayload(frame4, Blocks.GRANITE.defaultBlockState(),
				0, Direction.NORTH, false);
			p4.setItemInHand(InteractionHand.MAIN_HAND, frame4);
			h.assertTrue(frame4.getItem().useOn(ctx(h, p4, frame4, P.north(2), Direction.UP))
				== InteractionResult.FAIL, "missing-item place did not fail");
			h.assertTrue(h.getBlockState(P.north(2).above()).isAir(), "world mutated on reject");
			h.assertTrue(p4.getItemInHand(InteractionHand.MAIN_HAND).getCount() == 1,
				"frame consumed on reject");

			// PREPAID placement: material without needing inventory
			Player p5 = player(h);
			h.setBlock(P.south(2), Blocks.STONE);
			ItemStack prep = new ItemStack(ContourRegistries.CONTOUR_SLAB_ITEM.get());
			ContourBlockItem.writePayload(prep, Blocks.ANDESITE.defaultBlockState(),
				1, Direction.EAST, true);
			p5.setItemInHand(InteractionHand.MAIN_HAND, prep);
			h.assertTrue(prep.getItem().useOn(ctx(h, p5, prep, P.south(2), Direction.UP))
				.consumesAction(), "prepaid place failed");
			ContourBlockEntity be5 = (ContourBlockEntity) h.getLevel()
				.getBlockEntity(h.absolutePos(P.south(2).above()));
			h.assertTrue(be5.getMaterial().getBlock() == Blocks.ANDESITE
				&& be5.getPaidUnits() == 1 && be5.getFacing() == Direction.EAST && be5.isTop(),
				"prepaid ledger/canonical wrong");

			// config OFF: template paint costs zero, paid 0
			ContourConfig.CONSUME_MATERIAL.set(false);
			Player p6 = player(h);
			h.setBlock(P.east(2).south(2), Blocks.STONE);
			ItemStack frame6 = new ItemStack(ContourRegistries.CONTOUR_SLAB_ITEM.get());
			ContourBlockItem.writePayload(frame6, Blocks.OAK_PLANKS.defaultBlockState(),
				0, Direction.NORTH, false);
			p6.setItemInHand(InteractionHand.MAIN_HAND, frame6);
			h.assertTrue(frame6.getItem().useOn(ctx(h, p6, frame6, P.east(2).south(2), Direction.UP))
				.consumesAction(), "config-off place failed");
			ContourBlockEntity be6 = (ContourBlockEntity) h.getLevel()
				.getBlockEntity(h.absolutePos(P.east(2).south(2).above()));
			h.assertTrue(be6.getMaterial().getBlock() == Blocks.OAK_PLANKS
				&& be6.getPaidUnits() == 0, "config-off painted credit");
		} finally {
			ContourConfig.CONSUME_MATERIAL.set(true);
		}
		h.succeed();
	}

	@GameTest(template = "test_empty", batch = "contour_config")
	public void doubleMergeLedger(GameTestHelper h) {
		try {
			ContourConfig.CONSUME_MATERIAL.set(true);
			Player p = player(h);
			// seed a painted single slab with paid 1
			ContourBlockEntity be = be(h, ContourRegistries.CONTOUR_SLAB.get()
				.defaultBlockState().setValue(ContourSlabBlock.TYPE, SlabType.BOTTOM));
			be.applyPayload(Blocks.OAK_PLANKS.defaultBlockState(), 1, Direction.NORTH, false);
			// merge second item with same material, template cost 1
			ItemStack frame = new ItemStack(ContourRegistries.CONTOUR_SLAB_ITEM.get());
			ContourBlockItem.writePayload(frame, Blocks.OAK_PLANKS.defaultBlockState(),
				0, Direction.NORTH, false);
			p.setItemInHand(InteractionHand.MAIN_HAND, frame);
			p.getInventory().add(new ItemStack(Items.OAK_PLANKS, 1));
			h.assertTrue(frame.getItem().useOn(ctx(h, p, frame, P, Direction.NORTH))
				.consumesAction(), "merge place failed");
			h.assertTrue(h.getBlockState(P).getValue(ContourSlabBlock.TYPE) == SlabType.DOUBLE,
				"merge did not produce double");
			h.assertTrue(be.getPaidUnits() == 2, "merge ledger != 2, got " + be.getPaidUnits());
			h.assertTrue(p.getInventory().countItem(Items.OAK_PLANKS) == 0, "merge cost unpaid");

			// drops: two blank shape items + material refund stack
			var params = new net.minecraft.world.level.storage.loot.LootParams.Builder(
				h.getLevel()).withParameter(
					net.minecraft.world.level.storage.loot.parameters.LootContextParams.BLOCK_ENTITY, be);
			java.util.List<ItemStack> drops = ((ContourSlabBlock) h.getBlockState(P).getBlock())
				.getDrops(h.getBlockState(P), params);
			h.assertTrue(drops.size() == 3, "double drop count != 3: " + drops.size());
			h.assertTrue(drops.get(0).getItem() instanceof ContourBlockItem
				&& drops.get(1).getItem() instanceof ContourBlockItem
				&& ContourBlockItem.storedMaterial(h.getLevel().registryAccess(),
					drops.get(0)) == null
				&& ContourBlockItem.storedMaterial(h.getLevel().registryAccess(),
					drops.get(1)) == null,
				"shape drops carried paint");
			h.assertTrue(drops.get(2).is(Items.OAK_PLANKS)
				&& drops.get(2).getCount() == 2,
				"material refund wrong: " + drops.get(2));
			ContourConfig.CONSUME_MATERIAL.set(false);
		} finally {
			ContourConfig.CONSUME_MATERIAL.set(true);
		}
		h.succeed();
	}

	@GameTest(template = "test_empty")
	public void paintValidationAndCreativeRepaint(GameTestHelper h) {
		Player p = player(h);
		ContourBlockEntity be = be(h, ContourRegistries.CONTOUR_STAIRS.get().defaultBlockState());
		// invalid paint inputs leave material/ledger/inventory unchanged
		h.setBlock(P.west(), Blocks.CHEST);
		BlockState oldMat = be.getMaterial();
		int oldPaid = be.getPaidUnits();
		h.assertTrue(!net.createcontour.material.MaterialPolicy.paint(h.getLevel(), be, p,
			h.getBlockState(P.west())), "BE material accepted");
		h.assertTrue(!net.createcontour.material.MaterialPolicy.paint(h.getLevel(), be, p,
			Blocks.AIR.defaultBlockState()), "air accepted");
		h.assertTrue(!net.createcontour.material.MaterialPolicy.paint(h.getLevel(), be, p,
			ContourRegistries.CONTOUR_SLAB.get().defaultBlockState()), "recursion accepted");
		h.assertTrue(be.getMaterial() == oldMat && be.getPaidUnits() == oldPaid,
			"invalid paint mutated target");
		p.setShiftKeyDown(false);

		// creative paint: free, zero ledger credit (makeMockPlayer does not
		// set abilities.instabuild - set it explicitly)
		Player c = h.makeMockPlayer(GameType.CREATIVE);
		c.getAbilities().instabuild = true;
		// creative repaint of a PAID block must NOT refund the old ledger
		be.applyPayload(Blocks.OAK_PLANKS.defaultBlockState(), 1, Direction.NORTH, false);
		h.assertTrue(net.createcontour.material.MaterialPolicy.paint(h.getLevel(), be, c,
			Blocks.STONE.defaultBlockState()), "creative paint failed");
		h.assertTrue(be.getMaterial().getBlock() == Blocks.STONE && be.getPaidUnits() == 0,
			"creative minted credit");
		h.assertTrue(c.getInventory().countItem(Items.OAK_PLANKS) == 0,
			"creative paint refunded old paid material");
		h.succeed();
	}

	@GameTest(template = "test_empty")
	public void auxLightAcrossPaintAndWrench(GameTestHelper h) {
		ContourBlockEntity be = be(h, ContourRegistries.CONTOUR_SLAB.get().defaultBlockState());
		BlockPos abs = h.absolutePos(P);
		be.applyPayload(Blocks.GLOWSTONE.defaultBlockState(), 1, Direction.NORTH, false);
		h.assertTrue(h.getLevel().getAuxLightManager(abs).getLightAt(abs) == 15,
			"glowstone aux != 15");
		// slab -> vertical wrench keeps material + aux light
		ContourSlabBlock slab = ContourRegistries.CONTOUR_SLAB.get();
		h.assertTrue(slab.onWrenched(h.getBlockState(P),
				new UseOnContext(h.getLevel(), null, InteractionHand.MAIN_HAND,
					ItemStack.EMPTY, new BlockHitResult(
						Vec3.atCenterOf(abs), Direction.UP, abs, false)))
			.consumesAction(), "wrench failed");
		BlockEntity nbe = h.getLevel().getBlockEntity(abs);
		h.assertTrue(nbe instanceof ContourBlockEntity
			&& ((ContourBlockEntity) nbe).getMaterial().getBlock() == Blocks.GLOWSTONE
			&& ((ContourBlockEntity) nbe).getPaidUnits() == 1,
			"wrench lost material/ledger");
		h.assertTrue(h.getLevel().getAuxLightManager(abs).getLightAt(abs) == 15,
			"wrench dropped aux light");
		((ContourBlockEntity) nbe).setMaterial(Blocks.STONE.defaultBlockState());
		h.assertTrue(h.getLevel().getAuxLightManager(abs).getLightAt(abs) == 0,
			"stone still emits aux light");
		h.succeed();
	}

	// --- reservation / pickup regressions (final common batch) ---

	@GameTest(template = "test_empty")
	public void reservationRefundComponents(GameTestHelper h) {
		Player p = player(h);
		// Material stack carrying a custom-name component.
		ItemStack oak = new ItemStack(Items.OAK_PLANKS, 62);
		oak.set(DataComponents.CUSTOM_NAME, Component.literal("paid-oak"));
		p.getInventory().add(oak);
		var res = net.createcontour.material.MaterialPolicy.reserve(p,
			Items.OAK_PLANKS, 2);
		h.assertTrue(res != null && res.totalCount() == 2, "reserve failed");
		// Original is now 60; grow it back to 63 so only ONE refunded unit
		// fits, and fill every other free slot (main + offhand) so the
		// second unit MUST drop to the world.
		p.getInventory().getItem(0).setCount(63);
		for (int i = 0; i < p.getInventory().items.size(); i++)
			if (p.getInventory().getItem(i).isEmpty())
				p.getInventory().setItem(i, new ItemStack(Items.DIRT));
		p.setItemInHand(InteractionHand.OFF_HAND, new ItemStack(Items.DIRT));
		int totalBefore = p.getInventory().countItem(Items.OAK_PLANKS);
		net.createcontour.material.MaterialPolicy.refund(res);
		int totalAfter = p.getInventory().countItem(Items.OAK_PLANKS);
		h.assertTrue(totalAfter - totalBefore == 1,
			"refund over-added: " + totalBefore + "->" + totalAfter);
		// The remainder must drop as an item entity with the same component
		// (the mock player lives at world origin, not the test area).
		boolean dropped = h.getLevel().getEntitiesOfClass(
			net.minecraft.world.entity.item.ItemEntity.class,
			new net.minecraft.world.phys.AABB(
				Vec3.atCenterOf(p.blockPosition().offset(-4, -4, -4)),
				Vec3.atCenterOf(p.blockPosition().offset(4, 4, 4))))
			.stream().anyMatch(e -> e.getItem().is(Items.OAK_PLANKS)
				&& e.getItem().has(DataComponents.CUSTOM_NAME));
		h.assertTrue(dropped, "remainder drop missing or components lost");
		// Named oak inside inventory still carries the component.
		boolean kept = false;
		for (ItemStack s : p.getInventory().items)
			if (s.has(DataComponents.CUSTOM_NAME))
				kept = true;
		h.assertTrue(kept, "component lost on refund");
		// Second refund is a no-op.
		net.createcontour.material.MaterialPolicy.refund(res);
		h.assertTrue(p.getInventory().countItem(Items.OAK_PLANKS) == totalAfter,
			"refund ran twice");
		h.succeed();
	}

	@GameTest(template = "test_empty")
	public void reservationRestoresOrigin(GameTestHelper h) {
		Player p = player(h);
		// (a) named component stone at LATE main slot 20, earlier holes empty
		ItemStack named = new ItemStack(Items.STONE, 1);
		named.set(DataComponents.CUSTOM_NAME, Component.literal("orig"));
		p.getInventory().setItem(20, named);
		ItemStack expected = named.copy();
		var res = net.createcontour.material.MaterialPolicy.reserve(p, Items.STONE, 1);
		h.assertTrue(res != null && res.totalCount() == 1, "reserve failed");
		h.assertTrue(p.getInventory().getItem(20).isEmpty(), "slot not taken");
		net.createcontour.material.MaterialPolicy.refund(res);
		ItemStack back = p.getInventory().getItem(20);
		h.assertTrue(back.getCount() == 1
			&& ItemStack.isSameItemSameComponents(back, expected),
			"origin slot not restored exactly");
		for (int i = 0; i < 20; i++)
			h.assertTrue(p.getInventory().getItem(i).isEmpty(),
				"refund leaked to earlier slot " + i);
		// (b) offhand-only refund returns to OFFHAND, main stays empty
		Player p2 = player(h);
		ItemStack named2 = new ItemStack(Items.STONE, 1);
		named2.set(DataComponents.CUSTOM_NAME, Component.literal("off"));
		p2.setItemInHand(InteractionHand.OFF_HAND, named2);
		ItemStack expected2 = named2.copy();
		var res2 = net.createcontour.material.MaterialPolicy.reserve(p2, Items.STONE, 1);
		h.assertTrue(res2 != null && res2.totalCount() == 1, "reserve2 failed");
		h.assertTrue(p2.getOffhandItem().isEmpty(), "offhand not taken");
		net.createcontour.material.MaterialPolicy.refund(res2);
		h.assertTrue(p2.getOffhandItem().getCount() == 1
			&& ItemStack.isSameItemSameComponents(p2.getOffhandItem(), expected2),
			"offhand refund not restored exactly");
		for (int i = 0; i < 36; i++)
			h.assertTrue(p2.getInventory().getItem(i).isEmpty(),
				"offhand refund leaked to main " + i);
		net.createcontour.material.MaterialPolicy.refund(res2); // no-op
		h.assertTrue(p2.getOffhandItem().getCount() == 1, "refund twice");
		// (c) mutated origin: refund must NOT overwrite the new contents;
		// the named fragment returns via a compatible free slot
		Player p3 = player(h);
		ItemStack named3 = new ItemStack(Items.STONE, 1);
		named3.set(DataComponents.CUSTOM_NAME, Component.literal("c3"));
		p3.getInventory().setItem(20, named3);
		ItemStack expected3 = named3.copy();
		var res3 = net.createcontour.material.MaterialPolicy.reserve(p3, Items.STONE, 1);
		h.assertTrue(res3 != null && res3.totalCount() == 1, "reserve3 failed");
		h.assertTrue(p3.getInventory().getItem(20).isEmpty(), "slot3 not taken");
		p3.getInventory().setItem(20, new ItemStack(Items.DIRT, 5)); // mutated
		net.createcontour.material.MaterialPolicy.refund(res3);
		h.assertTrue(p3.getInventory().getItem(20).is(Items.DIRT)
			&& p3.getInventory().getItem(20).getCount() == 5,
			"refund overwrote mutated slot");
		boolean namedBack = false;
		for (int i = 0; i < 36; i++)
			if (ItemStack.isSameItemSameComponents(p3.getInventory().getItem(i), expected3))
				namedBack = true;
		h.assertTrue(namedBack && p3.getInventory().countItem(Items.STONE) == 1,
			"named fragment lost");
		net.createcontour.material.MaterialPolicy.refund(res3); // no-op
		h.assertTrue(p3.getInventory().countItem(Items.STONE) == 1
			&& p3.getInventory().getItem(20).is(Items.DIRT)
			&& p3.getInventory().getItem(20).getCount() == 5, "refund3 ran twice");
		// (d) origin mutated to DIRT count 0 carrying a DIFFERENT name:
		// refund must restore the ORIGINAL named stone, not the dirt
		Player p4 = player(h);
		ItemStack named4 = new ItemStack(Items.STONE, 1);
		named4.set(DataComponents.CUSTOM_NAME, Component.literal("real"));
		p4.getInventory().setItem(20, named4);
		ItemStack expected4 = named4.copy();
		var res4 = net.createcontour.material.MaterialPolicy.reserve(p4, Items.STONE, 1);
		h.assertTrue(res4 != null && res4.totalCount() == 1, "reserve4 failed");
		ItemStack ghost = new ItemStack(Items.DIRT, 0);
		ghost.set(DataComponents.CUSTOM_NAME, Component.literal("other"));
		p4.getInventory().setItem(20, ghost);
		net.createcontour.material.MaterialPolicy.refund(res4);
		h.assertTrue(ItemStack.isSameItemSameComponents(
				p4.getInventory().getItem(20), expected4)
				&& p4.getInventory().getItem(20).getCount() == 1,
			"empty-mutated origin minted wrong item");
		h.assertTrue(p4.getInventory().countItem(Items.DIRT) == 0, "dirt minted");
		// (e) origin mutated to STONE count 0 with WRONG name: refund
		// restores the ORIGINAL name/components, not 'wrong'
		Player p5 = player(h);
		ItemStack named5 = new ItemStack(Items.STONE, 1);
		named5.set(DataComponents.CUSTOM_NAME, Component.literal("real5"));
		p5.getInventory().setItem(20, named5);
		ItemStack expected5 = named5.copy();
		var res5 = net.createcontour.material.MaterialPolicy.reserve(p5, Items.STONE, 1);
		h.assertTrue(res5 != null && res5.totalCount() == 1, "reserve5 failed");
		ItemStack ghost5 = new ItemStack(Items.STONE, 0);
		ghost5.set(DataComponents.CUSTOM_NAME, Component.literal("wrong"));
		p5.getInventory().setItem(20, ghost5);
		net.createcontour.material.MaterialPolicy.refund(res5);
		h.assertTrue(ItemStack.isSameItemSameComponents(
				p5.getInventory().getItem(20), expected5)
				&& p5.getInventory().getItem(20).getCount() == 1,
			"empty-mutated origin kept wrong name");
		h.succeed();
	}

	@GameTest(template = "test_empty", batch = "contour_config")
	public void blankPickupAndReplace(GameTestHelper h) {
		try {
			ContourConfig.CONSUME_MATERIAL.set(false); // merge costs zero
			Player p = player(h);
			ContourBlockEntity be = be(h, ContourRegistries.CONTOUR_SLAB.get()
				.defaultBlockState().setValue(ContourSlabBlock.TYPE, SlabType.BOTTOM));
			be.applyPayload(Blocks.OAK_PLANKS.defaultBlockState(), 1, Direction.EAST, true);
			// merge a second frame with the SAME material template
			// (config OFF -> zero cost, paid unchanged)
			ItemStack frame = new ItemStack(ContourRegistries.CONTOUR_SLAB_ITEM.get());
			ContourBlockItem.writePayload(frame, Blocks.OAK_PLANKS.defaultBlockState(),
				0, Direction.NORTH, false);
			p.setItemInHand(InteractionHand.MAIN_HAND, frame);
			h.assertTrue(frame.getItem().useOn(ctx(h, p, frame, P, Direction.NORTH))
				.consumesAction(), "merge place failed");
			h.assertTrue(be.getPaidUnits() == 1, "free merge changed ledger");
			// sneak-wrench returns 2 blank items + the paid material
			p.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
			var res = ((ContourSlabBlock) h.getBlockState(P).getBlock())
				.onSneakWrenched(h.getBlockState(P),
					new UseOnContext(h.getLevel(), p, InteractionHand.MAIN_HAND,
						ItemStack.EMPTY, new BlockHitResult(
							Vec3.atCenterOf(h.absolutePos(P)), Direction.UP,
							h.absolutePos(P), false)));
			h.assertTrue(res.consumesAction(), "sneak-wrench not consumed");
			h.assertTrue(h.getBlockState(P).isAir(), "pickup left world block");
			int items = 0;
			for (ItemStack s : p.getInventory().items)
				if (s.is(ContourRegistries.CONTOUR_SLAB_ITEM.get())) {
					items += s.getCount();
					h.assertTrue(ContourBlockItem.storedMaterial(
						h.getLevel().registryAccess(), s) == null
						&& ContourBlockItem.storedPaid(s) == 0,
						"pickup item carried paint");
				}
			h.assertTrue(items == 2, "pickup shape count != 2: " + items);
			h.assertTrue(p.getInventory().countItem(Items.OAK_PLANKS) == 1,
				"paid material not refunded");
			// re-place a blank unit: free look-paint from the clicked stone
			ItemStack blank = null;
			for (ItemStack s : p.getInventory().items)
				if (s.is(ContourRegistries.CONTOUR_SLAB_ITEM.get()))
					blank = s;
			h.setBlock(P, Blocks.STONE);
			p.setItemInHand(InteractionHand.MAIN_HAND, blank);
			h.assertTrue(blank.getItem().useOn(ctx(h, p, blank, P, Direction.UP))
				.consumesAction(), "blank re-place failed");
			ContourBlockEntity nbe = (ContourBlockEntity) h.getLevel()
				.getBlockEntity(h.absolutePos(P.above()));
			h.assertTrue(nbe != null && nbe.getPaidUnits() == 0
				&& nbe.getMaterial().getBlock() == Blocks.STONE,
				"blank re-place lost look paint");
		} finally {
			ContourConfig.CONSUME_MATERIAL.set(true);
		}
		h.succeed();
	}

	@GameTest(template = "test_empty")
	public void codexEmitsBlank(GameTestHelper h) {
		// Shape items never carry paint: codex emits blank stacks even
		// when the payload names a material.
		ItemStack out = net.createcontour.network.ContourNetwork.buildCodexTemplate(
			new ItemStack(ContourRegistries.CONTOUR_SLAB_ITEM.get()),
			Blocks.STONE.defaultBlockState());
		h.assertTrue(ContourBlockItem.storedPaid(out) == 0
			&& ContourBlockItem.storedMaterial(
				h.getLevel().registryAccess(), out) == null,
			"codex grant carried paint payload");
		ItemStack blank = net.createcontour.network.ContourNetwork.buildCodexTemplate(
			new ItemStack(ContourRegistries.CONTOUR_STAIRS_ITEM.get()), null);
		h.assertTrue(ContourBlockItem.storedPaid(blank) == 0
			&& ContourBlockItem.storedMaterial(
				h.getLevel().registryAccess(), blank) == null,
			"blank codex grant carried payload");
		h.succeed();
	}

	@GameTest(template = "test_empty")
	public void placementRollbackSnapshot(GameTestHelper h) {
		Player p = player(h);
		// Occupied cell, no valid target: placement must FAIL with the held
		// frame payload + count completely unchanged.
		h.setBlock(P, Blocks.BEDROCK);
		h.setBlock(P.above(), Blocks.BEDROCK);
		ItemStack frame = new ItemStack(ContourRegistries.CONTOUR_SLAB_ITEM.get());
		ContourBlockItem.writePayload(frame, Blocks.OAK_PLANKS.defaultBlockState(),
			1, Direction.EAST, true);
		p.setItemInHand(InteractionHand.MAIN_HAND, frame);
		var r = frame.getItem().useOn(ctx(h, p, frame, P, Direction.UP));
		h.assertTrue(!r.consumesAction() || h.getBlockState(P.above())
			.getBlock() == Blocks.BEDROCK, "placement into solid not rejected");
		ItemStack held = p.getItemInHand(InteractionHand.MAIN_HAND);
		h.assertTrue(held.getCount() == 1 && ContourBlockItem.storedPaid(held) == 1
			&& ContourBlockItem.storedMaterial(h.getLevel().registryAccess(), held)
				.getBlock() == Blocks.OAK_PLANKS,
			"failed placement mutated frame payload");
		h.succeed();
	}
}
