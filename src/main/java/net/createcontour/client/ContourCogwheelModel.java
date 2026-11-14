package net.createcontour.client;

import com.simibubi.create.content.decoration.copycat.CopycatModel;
import java.util.List;
import net.createcontour.registry.ContourRegistries;
import net.createmod.ponder.render.VirtualRenderHelper;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.client.model.data.ModelProperty;
import org.jetbrains.annotations.Nullable;

/**
 * Painted kinetic-mesh model for {@code contour_cogwheel} (issue #23).
 *
 * Create's own kinetic blocks emit no ordinary chunk geometry either -
 * the rotating teeth/shaft are drawn by a BE visual or renderer so the
 * paint can spin. This model applies the same contract to the Contour
 * painted mesh:
 *
 * - in-world chunk meshing ({@code state != null}, no MESH marker) emits
 *   NOTHING: static painted teeth would look like they never rotate;
 * - {@link #MESH_PROPERTY} data (supplied by the instanced visual and the
 *   baked fallback buffer builders) emits the full painted mesh;
 * - {@link VirtualRenderHelper#isVirtual} data (Ponder scenes and other
 *   virtual previews) also emits, matching Create's virtual-render rule;
 * - {@code state == null} (item display, particles) emits the neutral
 *   mesh, since an item has no BE and no kinetic context.
 *
 * The painted emission itself is the stock Contour path: the same
 * AABB-box shape geometry as the decorative cog, textured by the
 * material's own baked quads via {@link ContourModel#sourcesFor}.
 */
public class ContourCogwheelModel extends ContourBoxModel {

	/**
	 * "Render the kinetic mesh" request marker, set only by the kinetic
	 * render paths (visual + baked fallback) through ModelData.
	 */
	public static final ModelProperty<Boolean> MESH_PROPERTY =
		new ModelProperty<>();

	public ContourCogwheelModel(BakedModel original,
		@Nullable BlockState boundState, @Nullable BlockState boundMaterial) {
		super(original, boundState, boundMaterial,
			net.createcontour.block.ContourCogBlock::boxesFor,
			ContourRegistries.CONTOUR_COGWHEEL);
	}

	private static boolean isMeshRequest(@Nullable ModelData data) {
		return data != null && Boolean.TRUE.equals(data.get(MESH_PROPERTY));
	}

	@Override
	public List<BakedQuad> getQuads(@Nullable BlockState state,
		@Nullable Direction side, RandomSource rand, ModelData data,
		@Nullable RenderType renderType) {
		if (state != null && !isMeshRequest(data)
			&& !VirtualRenderHelper.isVirtual(data))
			return List.of();
		return super.getQuads(state, side, rand, data, renderType);
	}

	/**
	 * The buffer paths hand us a fully-formed ModelData through the
	 * virtual getter; carry it (material + MESH marker) straight through
	 * so getQuads sees the request. In-world data takes the stock path.
	 */
	@Override
	protected ModelData.Builder gatherModelData(ModelData.Builder builder,
		BlockAndTintGetter level, BlockPos pos, BlockState state,
		ModelData data) {
		if (isMeshRequest(data)) {
			return builder.with(MESH_PROPERTY, true)
				.with(CopycatModel.MATERIAL_PROPERTY,
					CopycatModel.getMaterial(data));
		}
		return super.gatherModelData(builder, level, pos, state, data);
	}
}
