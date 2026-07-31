package com.colony.mod.client;

import com.colony.mod.network.ColonistInspectPacket;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

/**
 * Client-side HUD overlay that displays colonist inspection data.
 *
 * <p>When the player right-clicks a colonist, the server sends a {@link ColonistInspectPacket}
 * which is stored here. This renderer draws a translucent panel showing:
 * <ul>
 *   <li>Current AI goal / state</li>
 *   <li>Four need bars (Hunger, Energy, Social, Safety)</li>
 *   <li>Up to three notable relationships with affinity values</li>
 * </ul>
 *
 * <p>The overlay dismisses automatically after {@link #DISPLAY_TICKS} ticks or when the
 * player moves.
 */
@Environment(EnvType.CLIENT)
public final class ColonistInspectHud {

    /** Number of ticks the HUD remains visible after receiving inspect data. */
    private static final int DISPLAY_TICKS = 200; // 10 seconds

    private static ColonistInspectPacket currentData = null;
    private static int ticksRemaining = 0;

    private static double lastPlayerX = Double.NaN;
    private static double lastPlayerY = Double.NaN;
    private static double lastPlayerZ = Double.NaN;

    private ColonistInspectHud() {}

    /**
     * Called when a {@link ColonistInspectPacket} arrives from the server.
     * Starts the display timer and resets the player position snapshot.
     */
    public static void showInspectData(ColonistInspectPacket data) {
        currentData = data;
        ticksRemaining = DISPLAY_TICKS;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {
            lastPlayerX = mc.player.getX();
            lastPlayerY = mc.player.getY();
            lastPlayerZ = mc.player.getZ();
        }
    }

    /**
     * Called each frame by {@link net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback}.
     */
    public static void onHudRender(GuiGraphics drawContext, net.minecraft.client.DeltaTracker tickDelta) {
        if (currentData == null || ticksRemaining <= 0) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        // Dismiss if player has moved
        double dx = mc.player.getX() - lastPlayerX;
        double dy = mc.player.getY() - lastPlayerY;
        double dz = mc.player.getZ() - lastPlayerZ;
        if (dx * dx + dy * dy + dz * dz > 0.5) {
            currentData = null;
            return;
        }

        ticksRemaining--;
        renderPanel(drawContext);
    }

    private static void renderPanel(GuiGraphics gfx) {
        if (currentData == null) return;

        Minecraft mc = Minecraft.getInstance();
        int screenWidth = mc.getWindow().getGuiScaledWidth();

        int panelX = screenWidth - 160;
        int panelY = 10;
        int panelW = 150;
        int panelH = 100 + currentData.topRelationships().size() * 10;

        // Translucent background (ARGB: alpha=0xAA, black)
        gfx.fill(panelX - 2, panelY - 2, panelX + panelW + 2, panelY + panelH + 2, 0xAA000000);

        int y = panelY;
        int textColor = 0xFFFFFFFF;

        // Current goal
        gfx.drawString(mc.font, "§e" + currentData.currentGoal(), panelX, y, textColor, false);
        y += 12;

        // Need bars
        y = renderNeedBar(gfx, mc, panelX, y, "§cHunger", currentData.hungerValue(), 0xFF_FF4444);
        y = renderNeedBar(gfx, mc, panelX, y, "§aEnergy", currentData.energyValue(), 0xFF_44FF44);
        y = renderNeedBar(gfx, mc, panelX, y, "§bSocial", currentData.socialValue(), 0xFF_44BBFF);
        y = renderNeedBar(gfx, mc, panelX, y, "§6Safety", currentData.safetyValue(), 0xFF_FFAA00);

        y += 4;
        // Relationships
        if (!currentData.topRelationships().isEmpty()) {
            gfx.drawString(mc.font, "§7Relationships:", panelX, y, textColor, false);
            y += 10;
            for (ColonistInspectPacket.RelationshipEntry rel : currentData.topRelationships()) {
                String sign = rel.affinity() >= 0 ? "§a+" : "§c";
                String line = "  " + rel.name() + " " + sign + String.format("%.0f", rel.affinity());
                gfx.drawString(mc.font, line, panelX, y, textColor, false);
                y += 10;
            }
        }
    }

    private static int renderNeedBar(GuiGraphics gfx, Minecraft mc, int x, int y,
                                      String label, float value, int barColor) {
        gfx.drawString(mc.font, label, x, y, 0xFFFFFFFF, false);
        int barX = x + 50;
        int barW = 90;
        int barH = 5;
        // Background
        gfx.fill(barX, y + 2, barX + barW, y + 2 + barH, 0xFF333333);
        // Fill
        int fillW = (int) (barW * Math.min(1f, value / 100f));
        if (fillW > 0) gfx.fill(barX, y + 2, barX + fillW, y + 2 + barH, barColor);
        return y + 12;
    }
}
