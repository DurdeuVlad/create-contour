package net.createcontour.client;

import java.util.Arrays;
import java.util.List;
import net.createcontour.blockentity.ContourSignBlockEntity;
import net.createcontour.network.SignTextPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * Four-line sign editor. Sends a validated SignTextPayload on close;
 * the server re-checks distance, BE type and line length - this
 * screen never writes the BE itself.
 */
public final class ContourSignEditScreen extends Screen {

	private static final int PANEL_W = 220, PANEL_H = 132;

	private final BlockPos pos;
	private final EditBox[] fields = new EditBox[ContourSignBlockEntity.LINES];
	private int left, top;
	private boolean dirty;

	public ContourSignEditScreen(BlockPos pos) {
		super(Component.translatable("create_contour.sign.title"));
		this.pos = pos;
	}

	@Override
	protected void init() {
		left = (width - PANEL_W) / 2;
		top = (height - PANEL_H) / 2;
		String[] initial = new String[ContourSignBlockEntity.LINES];
		Arrays.fill(initial, "");
		if (Minecraft.getInstance().level != null
			&& Minecraft.getInstance().level.getBlockEntity(pos)
				instanceof ContourSignBlockEntity be)
			for (int i = 0; i < initial.length; i++)
				initial[i] = be.getLine(i);
		for (int i = 0; i < fields.length; i++) {
			EditBox box = new EditBox(font, left + 10, top + 24 + i * 22,
				PANEL_W - 20, 18, Component.literal("line " + (i + 1)));
			box.setMaxLength(ContourSignBlockEntity.MAX_LINE);
			box.setValue(initial[i]);
			box.setResponder(s -> dirty = true);
			fields[i] = addRenderableWidget(box);
		}
		addRenderableWidget(Button.builder(
			Component.translatable("gui.done"), b -> onClose())
			.bounds(left + PANEL_W / 2 - 50, top + 112, 100, 18).build());
		setInitialFocus(fields[0]);
	}

	@Override
	public void onClose() {
		if (dirty) {
			List<String> lines = new java.util.ArrayList<>(fields.length);
			for (EditBox f : fields)
				lines.add(f.getValue());
			PacketDistributor.sendToServer(new SignTextPayload(pos, lines));
		}
		super.onClose();
	}

	@Override
	public void render(GuiGraphics gg, int mouseX, int mouseY, float partial) {
		super.render(gg, mouseX, mouseY, partial);
		gg.fill(left, top, left + PANEL_W, top + PANEL_H, 0xF0181820);
		gg.renderOutline(left, top, PANEL_W, PANEL_H, 0xFF9090A8);
		gg.drawCenteredString(font, title, left + PANEL_W / 2, top + 8,
			0xFFE8E8F0);
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}
}
