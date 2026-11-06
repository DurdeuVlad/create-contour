package net.createcontour.client;

import java.util.ArrayList;
import java.util.List;
import net.createcontour.item.ContourBlockItem;
import net.createcontour.item.ContourPlacerItem;
import net.createcontour.network.PlacerSelectPayload;
import net.createcontour.registry.ContourRegistries;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * The back-mounted shape selector of the place gun: a scrollable
 * grid of every ContourBlockItem with the gun's stored selection
 * highlighted. Clicking a cell sends a validated request to the
 * server and closes - the screen never writes payloads itself.
 */
public final class ContourPlacerScreen extends Screen {

	private static final int PANEL_W = 176, PANEL_H = 166, CELL = 20;
	private static final int COLS = 8, ROWS = 5;

	private final List<ItemStack> shapes = new ArrayList<>();
	private int selected = 0;
	private int scroll; // first visible row
	private int left, top;

	public ContourPlacerScreen() {
		super(Component.translatable("create_contour.placer.title"));
		for (var holder : ContourRegistries.ITEMS.getEntries())
			if (holder.get() instanceof ContourBlockItem)
				shapes.add(new ItemStack(holder.get()));
		Minecraft mc = Minecraft.getInstance();
		if (mc.player != null)
			for (InteractionHand hand : InteractionHand.values()) {
				ItemStack held = mc.player.getItemInHand(hand);
				if (held.getItem() instanceof ContourPlacerItem) {
					ResourceLocation sel =
						ContourPlacerItem.selectedShapeId(held);
					if (sel != null)
						for (int i = 0; i < shapes.size(); i++)
							if (BuiltInRegistries.ITEM.getKey(shapes.get(i)
								.getItem()).equals(sel))
								selected = i;
					break;
				}
			}
		scroll = Math.max(0, Math.min(maxScroll(), selected / COLS - ROWS + 1));
	}

	private int maxScroll() {
		return Math.max(0, (shapes.size() + COLS - 1) / COLS - ROWS);
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
		gg.drawCenteredString(font, Component.translatable(
			"create_contour.placer.subtitle"), left + PANEL_W / 2,
			top + 19, 0xFF9A9AB0);
		for (int r = 0; r < ROWS; r++)
			for (int c = 0; c < COLS; c++) {
				int i = (scroll + r) * COLS + c;
				if (i >= shapes.size())
					break;
				int x = left + 8 + c * CELL, y = top + 34 + r * CELL;
				slot(gg, x, y, i == selected);
				gg.renderItem(shapes.get(i), x + 2, y + 2);
			}
		if (maxScroll() > 0) {
			int barH = ROWS * CELL - 4;
			int th = Math.max(12, barH * ROWS * COLS / shapes.size());
			int ty = top + 34 + 2 + (barH - th) * scroll / maxScroll();
			gg.fill(left + PANEL_W - 6, ty, left + PANEL_W - 3, ty + th, 0xFF707090);
		}
		int si = shapeAt(mouseX, mouseY);
		if (si >= 0)
			gg.renderTooltip(font, shapes.get(si), mouseX, mouseY);
	}

	private void slot(GuiGraphics gg, int x, int y, boolean sel) {
		gg.fill(x, y, x + 18, y + 18, 0xFF2A2A38);
		gg.renderOutline(x, y, 18, 18, sel ? 0xFFFFD75E : 0xFF50506A);
	}

	private int shapeAt(int mx, int my) {
		if (my < top + 34 || my >= top + 34 + ROWS * CELL)
			return -1;
		int c = (mx - left - 8) / CELL, r = (my - top - 34) / CELL;
		int i = (scroll + r) * COLS + c;
		return c >= 0 && c < COLS && r >= 0 && i < shapes.size()
			&& mx >= left + 8 + c * CELL && mx < left + 8 + c * CELL + 18
			&& my >= top + 34 + r * CELL && my < top + 34 + r * CELL + 18 ? i : -1;
	}

	@Override
	public boolean mouseClicked(double mx, double my, int button) {
		if (button == 0) {
			int si = shapeAt((int) mx, (int) my);
			if (si >= 0) {
				selected = si;
				PacketDistributor.sendToServer(new PlacerSelectPayload(
					BuiltInRegistries.ITEM.getKey(shapes.get(si).getItem())));
				onClose();
				return true;
			}
		}
		return super.mouseClicked(mx, my, button);
	}

	@Override
	public boolean mouseScrolled(double mx, double my, double sx, double sy) {
		if (maxScroll() > 0) {
			scroll = Math.max(0, Math.min(maxScroll(), scroll - (int) Math.signum(sy)));
			return true;
		}
		return super.mouseScrolled(mx, my, sx, sy);
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}
}
