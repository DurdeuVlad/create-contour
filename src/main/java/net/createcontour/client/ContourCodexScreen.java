package net.createcontour.client;

import java.util.ArrayList;
import java.util.List;
import net.createcontour.item.ContourBlockItem;
import net.createcontour.network.CodexSelectPayload;
import net.createcontour.registry.ContourRegistries;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * Shape picker emitted by the Contour Codex. Selecting a shape sends a
 * validated request to the server; the server emits a BLANK item -
 * shape items never carry paint, so this screen only offers shapes.
 */
public final class ContourCodexScreen extends Screen {

	private static final int PANEL_W = 176, PANEL_H = 100, CELL = 20;
	private static final int ROWS = 4;

	private final List<ItemStack> shapes = new ArrayList<>();
	private int left, top, scrollRow;

	public ContourCodexScreen() {
		super(Component.translatable("create_contour.codex.title"));
		for (var holder : ContourRegistries.ITEMS.getEntries())
			if (holder.get() instanceof ContourBlockItem)
				shapes.add(new ItemStack(holder.get()));
	}

	@Override
	protected void init() {
		left = (width - PANEL_W) / 2;
		top = (height - PANEL_H) / 2;
	}

	@Override
	public void render(GuiGraphics gg, int mouseX, int mouseY, float partial) {
		super.render(gg, mouseX, mouseY, partial);
		gg.fill(left, top, left + PANEL_W, top + PANEL_H, 0xF0181820);
		gg.renderOutline(left, top, PANEL_W, PANEL_H, 0xFF9090A8);
		gg.drawCenteredString(font, title, left + PANEL_W / 2, top + 7, 0xFFE8E8F0);
		for (int i = scrollRow * 8; i < shapes.size()
			&& i < (scrollRow + ROWS) * 8; i++) {
			int x = left + 8 + (i % 8) * CELL, y = top + 18 + (i / 8 - scrollRow) * CELL;
			slot(gg, x, y);
			gg.renderItem(shapes.get(i), x + 2, y + 2);
		}
		int si = shapeAt(mouseX, mouseY);
		if (si >= 0)
			gg.renderTooltip(font, shapes.get(si), mouseX, mouseY);
	}

	private void slot(GuiGraphics gg, int x, int y) {
		gg.fill(x, y, x + 18, y + 18, 0xFF2A2A38);
		gg.renderOutline(x, y, 18, 18, 0xFF50506A);
	}

	private int shapeAt(int mx, int my) {
		if (my < top + 18)
			return -1;
		int col = (mx - left - 8) / CELL, row = (my - top - 18) / CELL;
		if (row < 0 || row >= ROWS)
			return -1;
		int i = (row + scrollRow) * 8 + col;
		return i >= 0 && i < shapes.size() && col >= 0 && col < 8
			&& mx >= left + 8 + col * CELL && mx < left + 8 + col * CELL + 18
			&& my >= top + 18 + row * CELL && my < top + 18 + row * CELL + 18 ? i : -1;
	}

	@Override
	public boolean mouseScrolled(double mx, double my, double dx, double dy) {
		int max = Math.max(0, (shapes.size() + 7) / 8 - ROWS);
		scrollRow = Math.max(0, Math.min(max, scrollRow + (dy < 0 ? 1 : -1)));
		return true;
	}

	@Override
	public boolean mouseClicked(double mx, double my, int button) {
		if (button == 0) {
			int si = shapeAt((int) mx, (int) my);
			if (si >= 0) {
				ResourceLocation shapeId = BuiltInRegistries.ITEM
					.getKey(shapes.get(si).getItem());
				PacketDistributor.sendToServer(
					new CodexSelectPayload(shapeId, CodexSelectPayload.NO_MATERIAL));
				onClose();
				return true;
			}
		}
		return super.mouseClicked(mx, my, button);
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}
}
