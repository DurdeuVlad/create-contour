package net.createcontour.blockentity;

import com.simibubi.create.content.contraptions.StructureTransform;
import com.simibubi.create.content.decoration.copycat.CopycatModel;
import com.simibubi.create.content.kinetics.simpleRelays.BracketedKineticBlockEntity;
import com.simibubi.create.content.schematics.requirement.ItemRequirement;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import java.util.List;
import net.createcontour.config.ContourConfig;
import net.createcontour.material.MaterialSafety;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.data.ModelData;

/**
 * Kinetic Contour host: a REAL Create kinetic block entity (extends
 * Create's {@link BracketedKineticBlockEntity}, i.e. the cogwheel BE
 * family) carrying the same owned material + paid-ledger + safe-NBT
 * surface as {@link ContourBlockEntity}, through {@link ContourPaintable}.
 *
 * Kinetic state (Speed/Source/Network/stress) is inherited untouched:
 * propagation, direction rules and persistence all run inside Create's
 * own RotationPropagator/KineticBlockEntity machinery. Java has no
 * multiple inheritance, so rather than double-inheriting the copycat
 * private fields we REIMPLEMENT the owned Contour surface on top of the
 * kinetic base - the same fields, keys and rules as ContourBlockEntity,
 * minus the multipart occupant channels this shape does not have.
 */
public class ContourCogwheelBlockEntity extends BracketedKineticBlockEntity
	implements ContourPaintable {

	public static final int SCHEMA_VERSION = 1;

	private BlockState material = MaterialSafety.neutral();
	private int paidUnits;
	private Direction facing = Direction.NORTH;
	private boolean top;
	private CompoundTag unknownTag; // future-version payload, preserved raw
	private boolean frozen;         // unknown future version: no mutations

	public ContourCogwheelBlockEntity(BlockPos pos, BlockState state) {
		// Type resolves lazily at BE creation, after registration binds.
		this(net.createcontour.registry.ContourRegistries.CONTOUR_COGWHEEL_BE
			.get(), pos, state);
	}

	protected ContourCogwheelBlockEntity(BlockEntityType<?> type,
		BlockPos pos, BlockState state) {
		super(type, pos, state);
	}

	/**
	 * Skips BracketedKineticBlockEntity's BracketedBlockEntityBehaviour
	 * deliberately: our render path draws only the painted kinetic mesh,
	 * so an attached bracket would consume the item but be invisible.
	 * KineticBlockEntity's own addBehaviours contributes nothing else.
	 */
	@Override
	public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
	}

	// --- material surface (owned, mirrors ContourBlockEntity) ---

	@Override
	public BlockState getMaterial() {
		return material;
	}

	@Override
	public boolean hasCustomMaterial() {
		return !MaterialSafety.isNeutral(material);
	}

	@Override
	public void setMaterial(BlockState blockState) {
		if (frozen)
			return;
		material = MaterialSafety.sanitize(blockState);
		afterOwnedMutation();
	}

	@Override
	public int getPaidUnits() {
		return paidUnits;
	}

	@Override
	public void setPaidUnits(int units) {
		if (frozen)
			return;
		paidUnits = Math.max(0, Math.min(units, hostUnits()));
		if (MaterialSafety.isNeutral(material))
			paidUnits = 0;
		notifyUpdate();
	}

	@Override
	public Direction getFacing() {
		return facing;
	}

	@Override
	public boolean isTop() {
		return top;
	}

	@Override
	public boolean isFrozen() {
		return frozen;
	}

	@Override
	public void setCanonical(Direction f, boolean t) {
		if (frozen)
			return;
		facing = (f != null && f.getAxis().isHorizontal()) ? f : Direction.NORTH;
		top = t;
	}

	/** A cogwheel is always a single host unit. */
	@Override
	public int hostUnits() {
		return 1;
	}

	/**
	 * Owned appearance refresh, same rules as ContourBlockEntity: aux
	 * light + light engine + client model-data invalidation (which also
	 * queues a Flywheel visual update on KineticBlockEntity) + remesh.
	 */
	private void refreshMaterialAppearance() {
		if (level == null)
			return;
		try {
			level.getAuxLightManager(getBlockPos()).setLightAt(getBlockPos(),
				hasCustomMaterial()
					? Math.min(15, material.getLightEmission(level, getBlockPos()))
					: 0);
		} catch (RuntimeException ignored) {
		}
		level.getLightEngine().checkBlock(getBlockPos());
		if (level.isClientSide) {
			if (!isVirtual())
				requestModelDataUpdate();
			level.sendBlockUpdated(getBlockPos(), getBlockState(),
				getBlockState(), Block.UPDATE_CLIENTS);
		}
	}

	/** Sync + light + model-data refresh after owned fields settled. */
	private void afterOwnedMutation() {
		if (!MaterialSafety.isNeutral(material) || paidUnits != 0)
			paidUnits = MaterialSafety.isNeutral(material) ? 0
				: Math.min(paidUnits, hostUnits());
		notifyUpdate();
		if (level != null) {
			refreshMaterialAppearance();
			if (!level.isClientSide) {
				level.blockUpdated(getBlockPos(), getBlockState().getBlock());
				level.sendBlockUpdated(getBlockPos(), getBlockState(),
					getBlockState(), Block.UPDATE_CLIENTS);
			}
		}
	}

	@Override
	public void applyPayload(BlockState payloadMaterial, int payloadPaid,
		Direction payloadFacing, boolean payloadTop) {
		if (frozen)
			return;
		material = MaterialSafety.sanitize(payloadMaterial);
		setCanonical(payloadFacing, payloadTop);
		setPaidUnits(payloadPaid);
		afterOwnedMutation();
	}

	// --- schematic/requirement surface ---

	/**
	 * Schematic checklist contribution, unioned with the default
	 * host-item entry: the copied material at one unit per host unit
	 * (consume-on only). No bracket behaviour exists to contribute.
	 */
	@Override
	public ItemRequirement getRequiredItems(BlockState state) {
		ItemRequirement base = super.getRequiredItems(state);
		if (hasCustomMaterial() && ContourConfig.consumeMaterial()
			&& !material.getBlock().asItem().getDefaultInstance().isEmpty()) {
			return base.union(new ItemRequirement(
				ItemRequirement.ItemUseType.CONSUME,
				List.of(new ItemStack(material.getBlock().asItem(),
					hostUnits()))));
		}
		return base;
	}

	@Override
	public void transform(BlockEntity be, StructureTransform transform) {
		super.transform(be, transform);
		if (frozen)
			return;
		if (hasCustomMaterial() && ContourConfig.rotateTextures())
			material = transform.apply(material);
		facing = transform.mirrorFacing(transform.rotation.rotate(facing));
		notifyUpdate();
	}

	// --- persistence ---

	/**
	 * Same owned-key contract as ContourBlockEntity: future-version
	 * payloads are preserved raw and freeze every owned mutation; the
	 * kinetic base receives the tag untouched because its keys (Speed,
	 * Source, Network...) never collide with ours.
	 */
	@Override
	protected void read(CompoundTag tag, HolderLookup.Provider provider,
		boolean clientPacket) {
		int version = tag.getInt("ContourVersion");
		if (version > SCHEMA_VERSION) {
			// Preserve the RAW unknown payload untouched, but reset the
			// operational view: a frozen BE shows neutral/no-credit.
			unknownTag = tag.copy();
			frozen = true;
			material = MaterialSafety.neutral();
			paidUnits = 0;
			facing = Direction.NORTH;
			top = false;
			super.read(tag, provider, clientPacket);
			if (level != null)
				refreshMaterialAppearance();
			return;
		}
		frozen = false;
		unknownTag = null;
		BlockState parsedMaterial = parseMaterial(tag, provider);
		Direction parsedFacing = parseFacing(tag);
		boolean parsedTop = tag.getBoolean("Top");
		int parsedPaid = Math.max(0, tag.getInt("PaidUnits"));
		super.read(tag, provider, clientPacket);
		material = parsedMaterial;
		facing = parsedFacing;
		top = parsedTop;
		paidUnits = Math.min(parsedPaid, hostUnits());
		if (MaterialSafety.isNeutral(material))
			paidUnits = 0;
		if (level != null)
			refreshMaterialAppearance();
	}

	private BlockState parseMaterial(CompoundTag tag,
		HolderLookup.Provider provider) {
		if (!tag.contains("Material"))
			return MaterialSafety.neutral();
		try {
			return MaterialSafety.sanitize(NbtUtils.readBlockState(
				provider.lookupOrThrow(Registries.BLOCK),
				tag.getCompound("Material")));
		} catch (RuntimeException e) {
			return MaterialSafety.neutral();
		}
	}

	private static Direction parseFacing(CompoundTag tag) {
		if (tag.contains("Facing")) {
			try {
				Direction d = Direction.valueOf(tag.getString("Facing"));
				return d.getAxis().isHorizontal() ? d : Direction.NORTH;
			} catch (IllegalArgumentException ignored) {
			}
		}
		return Direction.NORTH;
	}

	@Override
	protected void write(CompoundTag tag, HolderLookup.Provider provider,
		boolean clientPacket) {
		if (frozen && unknownTag != null) {
			// Re-emit the unknown future payload untouched; no downwrite.
			tag.merge(unknownTag.copy());
			return;
		}
		super.write(tag, provider, clientPacket);
		tag.putInt("ContourVersion", SCHEMA_VERSION);
		tag.put("Material", NbtUtils.writeBlockState(material));
		tag.putInt("PaidUnits", paidUnits);
		tag.putString("Facing", facing.name());
		tag.putBoolean("Top", top);
	}

	/**
	 * Safe schematic projection, same policy as ContourBlockEntity:
	 * frozen hosts export a neutralized view (never the raw unknown
	 * payload); credit is exported only as what the print's own
	 * requirement will charge; consume-off exports no credit.
	 */
	@Override
	public void writeSafe(CompoundTag tag, HolderLookup.Provider provider) {
		if (frozen) {
			tag.putInt("ContourVersion", SCHEMA_VERSION);
			tag.put("Material",
				NbtUtils.writeBlockState(MaterialSafety.neutral()));
			tag.putInt("PaidUnits", 0);
			return;
		}
		super.writeSafe(tag, provider);
		tag.putInt("ContourVersion", SCHEMA_VERSION);
		tag.put("Material", NbtUtils.writeBlockState(material));
		tag.putString("Facing", facing.name());
		tag.putBoolean("Top", top);
		tag.putInt("PaidUnits",
			hasCustomMaterial() && ContourConfig.consumeMaterial()
				? hostUnits() : 0);
		tag.remove("Item");
	}

	// --- model data for the painted kinetic mesh ---

	@Override
	public ModelData getModelData() {
		return ModelData.builder()
			.with(CopycatModel.MATERIAL_PROPERTY, material)
			.build();
	}

	// --- lifecycle ---

	@Override
	public void onLoad() {
		super.onLoad();
		if (level != null) {
			refreshMaterialAppearance();
			sendData();
		}
	}
}
