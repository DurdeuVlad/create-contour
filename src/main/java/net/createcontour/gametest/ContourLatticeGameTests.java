package net.createcontour.gametest;

import com.mojang.logging.LogUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import net.createcontour.block.ContourByteBlock;
import net.createcontour.blockentity.ContourBlockEntity;
import net.createcontour.registry.ContourRegistries;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.slf4j.Logger;

/**
 * Issue #8 / ADR-017: lattice conformance gate. The design system
 * requires every Contour shape's collision boxes to sit on the
 * 16x16x16 lattice: every AABB coordinate an integer multiple of
 * 1/16 and inside [0,1] block space. This test scans every
 * blockstate of every registered create_contour block via
 * {@code getShape} and fails on any box that leaves the lattice.
 *
 * <p>Shapes authored before ADR-017 whose collision still carries
 * sub-pixel coordinates are named in {@link #LATTICE_EXCEPTIONS};
 * the set is an exact mirror of the debt - a violation by an
 * unlisted block fails, and a listed block that stops violating
 * also fails (stale entry), so the list cannot silently drift.
 *
 * <p>Render-side declared boxes ({@code ContourModel.boxesFor}) are
 * {@code protected} and not reachable from this package without a
 * refactor, so this gate covers collision shapes only - noted in
 * the issue report.
 */
@net.neoforged.neoforge.gametest.GameTestHolder("create_contour")
@net.neoforged.neoforge.gametest.PrefixGameTestTemplate(false)
public class ContourLatticeGameTests {

	private static final Logger LOG = LogUtils.getLogger();
	private static final BlockPos P = new BlockPos(2, 1, 2);
	private static final double EPS = 1.0e-4;

	/**
	 * Documented ADR-017 exceptions (block registry path -> fractional
	 * coordinate source). Pre-lattice geometry debt; never add new
	 * entries - shrink toward empty as families are reworked.
	 */
	private static final Map<String, String> LATTICE_EXCEPTIONS =
		Map.ofEntries(
			// annular quadrant swept by cos/sin at 22.5-degree steps
			Map.entry("contour_arc_wall", "cos/sin sweep"),
			// sheared plate: yTop = 14.6 - 0.55*n, thickness 1.2
			Map.entry("contour_awning", "14.6 - 0.55n slope"),
			// lathe bands: belly radius 4.2 at y 4..13
			Map.entry("contour_baluster", "4.2 band"),
			// rails/posts at half-px (6.5/9.5, 3.5, 1.5/14.5)
			Map.entry("contour_balustrade", "half-px rails"),
			// vault bands at 5.3 and 7.8
			Map.entry("contour_barrel_vault", "5.3/7.8 bands"),
			// collar/dome bands at 2.5/13.5 and 3.5/12.5
			Map.entry("contour_bollard", "half-px bands"),
			// quarter-arc du>=4.5 keep + bands 0.75/3.10/2.03/0.80/0.19
			Map.entry("contour_bullnose", "arc bands"),
			// octagon chamfers at +-4.24, band walls 3.32/12.68
			Map.entry("contour_cylinder", "4.24 chamfers"),
			// dentil teeth at 12.5/15.5
			Map.entry("contour_dentil", "half-px teeth"),
			// dome widths 7.75/6.93/5.29
			Map.entry("contour_dome", "arc widths"),
			// foot 1.5, taper tip 2.5
			Map.entry("contour_finial", "half-px bands"),
			// bolt studs at 8+-5*cos(45deg)=4.54, flange lip 12.5/3.5
			Map.entry("contour_flange", "cos studs + half-px lip"),
			// quarter-arc strut band r5.5
			Map.entry("contour_flying_buttress", "5.5 arc band"),
			// gothic reveal spans 13.6/15.4
			Map.entry("contour_gothic_arch", "arc spans"),
			// infill panel n at 6.5/9.5
			Map.entry("contour_half_timber", "half-px panel"),
			// keystone steps 5.25/10.75, 4.5/11.5, 3.75/12.25
			Map.entry("contour_keystone", "quarter-px steps"),
			// vanilla-parity wall mount plate at 6.5..9.5 (face=wall only)
			Map.entry("contour_lever", "vanilla wall shape"),
			// mount plate 1.5/14.5, slat pitch 3.5, slat height 2.5
			Map.entry("contour_louver", "half-px plate+slats"),
			// muntin bars centered 7.5..8.5
			Map.entry("contour_mullion", "half-px bars"),
			// shaft/knob 4.8..11.2
			Map.entry("contour_newel", "4.8/11.2 column"),
			// annulus plate n 6.5/9.5, 22.5deg covers via cos/sin
			Map.entry("contour_oculus", "half-px plate + sweep"),
			// ogee bands at 6.75 and 1.25
			Map.entry("contour_ogee", "6.75/1.25 bands"),
			// panel 6.5/9.5, tip rail 7.5/8.5, rail 9.5
			Map.entry("contour_palisade", "half-px panel+tip"),
			// diagonal plate approx: 5 slices of 3.2 + corner cols 1.5/14.5
			Map.entry("contour_panel", "3.2 slices + 1.5 cols"),
			// pediment panel n at 6.5/9.5
			Map.entry("contour_pediment", "half-px panel"),
			// pilaster diameter steps 5.3/7.5
			Map.entry("contour_pilaster", "5.3/7.5 steps"),
			// bars 6.5/9.5, tip rail 7.5/8.5
			Map.entry("contour_portcullis", "half-px bars"),
			// pressed height 0.5 when powered
			Map.entry("contour_pressure_plate", "0.5 pressed"),
			// quarter-arc heights 15.5 and 13.9
			Map.entry("contour_quarter_round", "arc heights"),
			// petal band radii and plate on half-px grid
			Map.entry("contour_rosette", "half-px bands"),
			// quarter-arc radii 15.5/13.9/10.6
			Map.entry("contour_round_corner", "arc radii"),
			// posts/plate/rails at 1.5/14.5 and 8.5
			Map.entry("contour_scaffold", "half-px members"),
			// scotia arc extent 4.33, steps of 2.5
			Map.entry("contour_scotia", "4.33/2.5 arc"),
			// slope cover top 3.5 (from 4 - 0.1875n profile)
			Map.entry("contour_sill", "3.5 cover"),
			// skirt/flare/rim bands at half-px
			Map.entry("contour_smokestack", "half-px bands"),
			// spandrel panel n at 6.5/9.5
			Map.entry("contour_spandrel", "half-px panel"),
			// sphere band widths 5.29/6.93
			Map.entry("contour_sphere", "band widths"),
			// web posts at 3.5/5.5 and 10.5/12.5
			Map.entry("contour_truss", "half-px posts"));

	private static boolean onLattice(double v) {
		return Math.abs(v * 16.0 - Math.round(v * 16.0)) < EPS;
	}

	private static boolean inBounds(AABB b) {
		return b.minX > -EPS && b.minY > -EPS && b.minZ > -EPS
			&& b.maxX < 1 + EPS && b.maxY < 1 + EPS && b.maxZ < 1 + EPS;
	}

	private static String fmt(AABB b) {
		return String.format("[%.4f,%.4f,%.4f -> %.4f,%.4f,%.4f]",
			b.minX, b.minY, b.minZ, b.maxX, b.maxY, b.maxZ);
	}

	private static String stateKey(BlockState s) {
		String str = s.toString();
		return str.substring(str.indexOf('[') == -1 ? 0
			: str.indexOf('['));
	}

	/** True when all six AABB coords are lattice-aligned and in cell. */
	private static String offLatticeReason(AABB b) {
		double[] c = {b.minX, b.minY, b.minZ, b.maxX, b.maxY, b.maxZ};
		for (double v : c)
			if (!onLattice(v))
				return "coord " + v;
		if (!inBounds(b))
			return "outside cell";
		return null;
	}

	@GameTest(template = "test_empty")
	public void collisionLattice(GameTestHelper h) {
		List<String> unexpected = new ArrayList<>();
		Set<String> stale = new TreeSet<>(LATTICE_EXCEPTIONS.keySet());
		Map<String, String> allowedLog = new TreeMap<>();
		int blocks = 0, states = 0, boxes = 0;
		for (Block b : BuiltInRegistries.BLOCK) {
			ResourceLocation id = BuiltInRegistries.BLOCK.getKey(b);
			if (!"create_contour".equals(id.getNamespace()))
				continue;
			blocks++;
			boolean allowed = LATTICE_EXCEPTIONS.containsKey(id.getPath());
			String firstBad = null;
			int badStates = 0;
			for (BlockState state : b.getStateDefinition()
				.getPossibleStates()) {
				states++;
				VoxelShape shape = state.getShape(
					EmptyBlockGetter.INSTANCE, BlockPos.ZERO,
					CollisionContext.empty());
				boolean stateBad = false;
				for (AABB box : shape.toAabbs()) {
					boxes++;
					String why = offLatticeReason(box);
					if (why == null)
						continue;
					stateBad = true;
					String detail = id.getPath() + " " + stateKey(state)
						+ " " + why + " " + fmt(box);
					if (firstBad == null)
						firstBad = detail;
					if (!allowed)
						unexpected.add(detail);
				}
				if (stateBad)
					badStates++;
			}
			if (firstBad != null) {
				stale.remove(id.getPath());
				if (allowed)
					allowedLog.put(id.getPath(),
						badStates + " bad states, first: " + firstBad);
			}
		}
		for (var e : allowedLog.entrySet())
			LOG.info("lattice exception honored {} ({}) -> {}",
				e.getKey(), LATTICE_EXCEPTIONS.get(e.getKey()), e.getValue());
		for (String d : unexpected)
			LOG.warn("lattice violation (unlisted): {}", d);
		for (String s : stale)
			LOG.warn("stale lattice exception (no violation found): {}", s);
		LOG.info("lattice scan: {} blocks, {} states, {} collision boxes",
			blocks, states, boxes);
		h.assertTrue(blocks >= 114,
			"create_contour block coverage too small: " + blocks);
		// Failure text must fit the 1024-char GameTest lectern book:
		// names only - per-state box detail is in the WARN lines above.
		Set<String> unlistedBlocks = new TreeSet<>();
		for (String d : unexpected)
			unlistedBlocks.add(d.substring(0, d.indexOf(' ')));
		h.assertTrue(unexpected.isEmpty(),
			"unlisted lattice violations: " + unexpected.size()
				+ " boxes in blocks " + unlistedBlocks
				+ " (see log for detail)");
		h.assertTrue(stale.isEmpty(),
			"stale lattice exceptions (no violation found): " + stale);
		h.succeed();
	}

	/**
	 * Multi-occupant byte: the union shape over the blockstate octant
	 * plus two added occupants must stay integer-px and the occupant
	 * boxes must not overlap. Octants 0 (blockstate), 3 (X1Y1Z0) and
	 * 5 (X1Y0Z1) touch only along edges, so the union has exactly
	 * three disjoint 8px cells.
	 */
	@GameTest(template = "test_empty")
	public void byteOccupantLattice(GameTestHelper h) {
		h.setBlock(P, ContourRegistries.CONTOUR_BYTE.get()
			.defaultBlockState().setValue(ContourByteBlock.OCTANT,
				ContourByteBlock.Octant.X0Y0Z0));
		ContourBlockEntity be = (ContourBlockEntity) h.getLevel()
			.getBlockEntity(h.absolutePos(P));
		h.assertTrue(be != null, "byte BE missing");
		h.assertTrue(be.addByteOccupant(3,
			Blocks.STONE.defaultBlockState(), 1),
			"octant 3 occupant refused");
		h.assertTrue(be.addByteOccupant(5,
			Blocks.OAK_LOG.defaultBlockState(), 1),
			"octant 5 occupant refused");

		VoxelShape shape = h.getBlockState(P).getShape(h.getLevel(),
			h.absolutePos(P), CollisionContext.empty());
		List<AABB> boxes = shape.toAabbs();
		h.assertTrue(boxes.size() == 3,
			"expected 3 disjoint octant cells, got " + boxes);
		for (AABB b : boxes) {
			String why = offLatticeReason(b);
			h.assertTrue(why == null,
				"byte union box off lattice: " + why + " " + fmt(b));
		}
		for (int i = 0; i < boxes.size(); i++)
			for (int j = i + 1; j < boxes.size(); j++)
				h.assertTrue(!boxes.get(i).intersects(boxes.get(j)),
					"occupant boxes overlap: " + fmt(boxes.get(i))
						+ " vs " + fmt(boxes.get(j)));
		h.succeed();
	}
}
