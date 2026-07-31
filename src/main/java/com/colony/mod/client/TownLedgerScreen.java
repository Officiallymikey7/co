package com.colony.mod.client;

import com.colony.mod.network.TownLedgerQueryPacket;
import com.colony.mod.network.TownLedgerResponsePacket;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

import java.util.List;

/**
 * Town Ledger screen — displays the colony's current status and history.
 *
 * <p>Opened when the player right-clicks the {@link com.colony.mod.block.TownLedgerBlock}.
 * Data is fetched from the server via a {@link TownLedgerQueryPacket} / {@link TownLedgerResponsePacket}
 * round-trip. The screen can be refreshed while open.
 *
 * <p>Sections displayed:
 * <ul>
 *   <li>Colony name, population, and treasury balance</li>
 *   <li>Current tax rate</li>
 *   <li>Active construction projects</li>
 *   <li>Recent crime log (last 10 entries)</li>
 * </ul>
 */
@OnlyIn(Dist.CLIENT)
public class TownLedgerScreen extends Screen {

    private TownLedgerResponsePacket data;

    // Scroll offsets for the crime log
    private int crimeLogScroll = 0;

    public TownLedgerScreen(TownLedgerResponsePacket initialData) {
        super(Component.literal("Town Ledger"));
        this.data = initialData;
    }

    /** Called by the packet handler when fresh data arrives while the screen is open. */
    public void refresh(TownLedgerResponsePacket newData) {
        this.data = newData;
    }

    @Override
    public void render(GuiGraphics gfx, int mouseX, int mouseY, float partialTick) {
        renderBackground(gfx, mouseX, mouseY, partialTick);
        super.render(gfx, mouseX, mouseY, partialTick);

        if (data == null) {
            gfx.drawCenteredString(font, "Loading...", width / 2, height / 2, 0xFFFFFFFF);
            return;
        }

        int x = width / 2 - 130;
        int y = 20;

        // Title
        gfx.drawCenteredString(font, "§6§l" + data.townName() + " — Town Ledger", width / 2, y, 0xFFFFFFFF);
        y += 20;

        // Stats
        gfx.drawString(font, "§ePopulation: §f" + data.population(), x, y, 0xFFFFFFFF, false);
        y += 12;
        gfx.drawString(font, "§eTreasury:   §f" + data.treasury() + " coins", x, y, 0xFFFFFFFF, false);
        y += 12;
        gfx.drawString(font, "§eTax Rate:   §f" + String.format("%.0f%%", data.taxRate() * 100), x, y, 0xFFFFFFFF, false);
        y += 18;

        // Build projects
        gfx.drawString(font, "§bActive Projects:", x, y, 0xFFFFFFFF, false);
        y += 11;
        List<String> projects = data.activeBuildProjects();
        if (projects.isEmpty()) {
            gfx.drawString(font, "  §7None", x, y, 0xFFFFFFFF, false);
            y += 11;
        } else {
            for (String project : projects) {
                gfx.drawString(font, "  §7• " + project, x, y, 0xFFFFFFFF, false);
                y += 10;
            }
        }

        y += 6;

        // Crime log
        gfx.drawString(font, "§cRecent Crimes:", x, y, 0xFFFFFFFF, false);
        y += 11;
        List<String> crimes = data.recentCrimes();
        if (crimes.isEmpty()) {
            gfx.drawString(font, "  §7None on record", x, y, 0xFFFFFFFF, false);
        } else {
            int visibleLines = Math.min(10, crimes.size());
            for (int i = 0; i < visibleLines; i++) {
                int idx = crimes.size() - 1 - i; // newest first
                gfx.drawString(font, "  §7" + (i + 1) + ". " + crimes.get(idx), x, y, 0xFFFFFFFF, false);
                y += 10;
            }
        }

        // Refresh hint
        gfx.drawString(font, "§8[Press R to refresh]", x, height - 20, 0xFFFFFFFF, false);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        // 'R' key — request a data refresh from the server
        if (keyCode == GLFW.GLFW_KEY_R) {
            PacketDistributor.sendToServer(new TownLedgerQueryPacket(true));
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
