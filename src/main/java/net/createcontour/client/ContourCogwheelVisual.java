package net.createcontour.client;

/*
 * The structure of this visual is adapted from the Create mod's
 * SingleAxisRotatingVisual:
 *   com.simibubi.create.content.kinetics.base.SingleAxisRotatingVisual
 * Create code is licensed under the MIT License:
 *
 * MIT License
 *
 * Copyright (c) The Create Team / The Creators of Create
 *
 * Permission is hereby granted, free of charge, to any person obtaining a
 * copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
 * CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

import com.simibubi.create.content.kinetics.base.KineticBlockEntityVisual;
import com.simibubi.create.content.kinetics.base.RotatingInstance;
import com.simibubi.create.foundation.render.AllInstanceTypes;
import dev.engine_room.flywheel.api.instance.Instance;
import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import java.util.function.Consumer;
import net.createcontour.blockentity.ContourCogwheelBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * Instanced kinetic visual for {@code contour_cogwheel} (issue #23).
 * Same structure as Create's SingleAxisRotatingVisual - one
 * {@link RotatingInstance} (spin axis, speed and the 22.5-deg small-cog
 * parity offset all come from KineticBlockEntity/RotatingInstance.setup)
 * - but the instanced model is the PAINTED Contour mesh from
 * {@link ContourKineticGeometry#meshModel}, keyed by (axis, material).
 * On paint changes (BE sync -> requestModelDataUpdate -> queueUpdate)
 * the instance is rebuilt against the new material's cached model.
 *
 * Note: Create's red/green overstress tint is not reproduced - the
 * effect field lives behind package-private access in Create and no
 * public accessor exists; the spin, offset and color state are all real.
 */
public class ContourCogwheelVisual
	extends KineticBlockEntityVisual<ContourCogwheelBlockEntity> {

	protected RotatingInstance rotatingModel;
	@Nullable
	private BlockState lastMaterial;
	private BlockState lastBlockState;

	public ContourCogwheelVisual(VisualizationContext context,
		ContourCogwheelBlockEntity blockEntity, float partialTick) {
		super(context, blockEntity, partialTick);
		lastMaterial = blockEntity.getMaterial();
		lastBlockState = blockEntity.getBlockState();
		var model = ContourKineticGeometry.meshModel(lastBlockState,
			lastMaterial);
		rotatingModel = instancerProvider()
			.instancer(AllInstanceTypes.ROTATING, model)
			.createInstance()
			.setup(blockEntity)
			.setPosition(getVisualPosition());
		rotatingModel.setChanged();
		com.mojang.logging.LogUtils.getLogger().info(
			"[contour-cog] visual created pos={} mat={} meshes={}",
			getVisualPosition(), lastMaterial, model.meshes().size());
	}

	@Override
	public void update(float pt) {
		if (blockEntity.getMaterial() != lastMaterial
			|| blockEntity.getBlockState() != lastBlockState) {
			lastMaterial = blockEntity.getMaterial();
			lastBlockState = blockEntity.getBlockState();
			rotatingModel.delete();
			rotatingModel = instancerProvider()
				.instancer(AllInstanceTypes.ROTATING, ContourKineticGeometry
					.meshModel(lastBlockState, lastMaterial))
				.createInstance()
				.setPosition(getVisualPosition());
		}
		rotatingModel.setup(blockEntity)
			.setChanged();
	}

	@Override
	public void updateLight(float partialTick) {
		relight(rotatingModel);
	}

	@Override
	protected void _delete() {
		rotatingModel.delete();
	}

	@Override
	public void collectCrumblingInstances(Consumer<Instance> consumer) {
		consumer.accept(rotatingModel);
	}
}
