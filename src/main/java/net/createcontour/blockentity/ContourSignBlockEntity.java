package net.createcontour.blockentity;

import java.util.Arrays;
import net.createcontour.registry.ContourRegistries;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Contour sign block entity: the shared material/ledger/orientation
 * surface plus 4 editable text lines. Lines persist through save,
 * sync and schematics (writeSafe). A frozen unknown-version BE
 * refuses text writes like every other mutation.
 */
public class ContourSignBlockEntity extends ContourBlockEntity {

	public static final int LINES = 4;
	public static final int MAX_LINE = 24;

	private final String[] lines = new String[LINES];

	public ContourSignBlockEntity(BlockPos pos, BlockState state) {
		super(ContourRegistries.CONTOUR_SIGN_BE.get(), pos, state);
		Arrays.fill(lines, "");
	}

	public String getLine(int i) {
		return lines[i];
	}

	public static String sanitize(String s) {
		String t = s.replace('\n', ' ').replace('\r', ' ').strip();
		return t.length() > MAX_LINE ? t.substring(0, MAX_LINE) : t;
	}

	/** Server-authoritative line update (validated by the packet). */
	public void setLines(String[] newLines) {
		if (isFrozen())
			return;
		for (int i = 0; i < LINES; i++)
			lines[i] = i < newLines.length ? sanitize(newLines[i]) : "";
		notifyUpdate();
		if (level != null && !level.isClientSide)
			level.sendBlockUpdated(getBlockPos(), getBlockState(),
				getBlockState(), Block.UPDATE_CLIENTS);
	}

	@Override
	protected void read(CompoundTag tag, HolderLookup.Provider provider, boolean clientPacket) {
		super.read(tag, provider, clientPacket);
		Arrays.fill(lines, "");
		if (!isFrozen() && tag.contains("SignLines", Tag.TAG_LIST)) {
			ListTag lt = tag.getList("SignLines", Tag.TAG_STRING);
			for (int i = 0; i < Math.min(LINES, lt.size()); i++)
				lines[i] = sanitize(lt.getString(i));
		}
	}

	@Override
	protected void write(CompoundTag tag, HolderLookup.Provider provider, boolean clientPacket) {
		super.write(tag, provider, clientPacket);
		if (isFrozen())
			return; // the preserved future payload already carries its own
		ListTag lt = new ListTag();
		for (String l : lines)
			lt.add(StringTag.valueOf(l));
		tag.put("SignLines", lt);
	}

	@Override
	public void writeSafe(CompoundTag tag, HolderLookup.Provider provider) {
		super.writeSafe(tag, provider);
		if (isFrozen())
			return;
		ListTag lt = new ListTag();
		for (String l : lines)
			lt.add(StringTag.valueOf(l));
		tag.put("SignLines", lt);
	}
}
