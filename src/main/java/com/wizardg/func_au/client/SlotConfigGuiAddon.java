package com.wizardg.func_au.client;

import java.util.ArrayList;
import java.util.List;

import com.buuz135.functionalstorage.block.tile.ControllableDrawerTile;
import com.buuz135.functionalstorage.block.tile.FluidDrawerTile;
import com.buuz135.functionalstorage.block.tile.ItemControllableDrawerTile;
import com.hrznstudio.titanium.client.screen.addon.BasicScreenAddon;
import com.hrznstudio.titanium.client.screen.asset.IAssetProvider;
import com.wizardg.func_au.AdvancedUpgrades;
import com.wizardg.func_au.DrawerSlotAccess;
import com.wizardg.func_au.FluidDrawerAccess;
import com.wizardg.func_au.SlotSettings;
import com.wizardg.func_au.SlotSettingsPayload;

import org.lwjgl.glfw.GLFW;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.network.PacketDistributor;

public class SlotConfigGuiAddon extends BasicScreenAddon {

    private static final int TAB_NONE = 0;
    private static final int TAB_LIMITS = 1;
    private static final int TAB_VOID = 2;

    private static final int TAB_X = 3;
    private static final int TAB_W = 20;
    private static final int TAB_H = 20;
    private static final int TAB_LIMITS_Y = 5;
    private static final int TAB_VOID_Y = 27;

    private static final int PANEL_X = 26;
    private static final int PANEL_Y = 5;
    private static final int PANEL_W = 148;
    private static final int ROW_H = 18;

    private static final int BG = 0xFF1A1A1A;
    private static final int TAB_BG = 0xFF2B2B2B;
    private static final int TAB_BG_ACTIVE = 0xFF3A3A5A;
    private static final int BORDER = 0xFF5B5BFF;
    private static final int BOX = 0xFF8B8B8B;
    private static final int ON = 0xFF4FC3F7;
    private static final int DISABLED_VEIL = 0xC0121212;

    private final ControllableDrawerTile<?> tile;
    private final List<EditBox> capBoxes = new ArrayList<>();

    private int activeTab = TAB_NONE;
    private boolean built;
    private int guiLeft;
    private int guiTop;
    private int hoveredTab = TAB_NONE;
    private SlotSettings local = SlotSettings.EMPTY;
    private SlotSettings lastSeen;

    private static boolean swallowTooltips;
    private static boolean drawingOwnTooltip;
    private static SlotConfigGuiAddon active;

    public static boolean isSwallowingTooltips() {
        return swallowTooltips && !drawingOwnTooltip;
    }

    public SlotConfigGuiAddon(ControllableDrawerTile<?> tile) {
        super(0, 0);
        this.tile = tile;
    }

    private int slots() {
        return AdvancedUpgrades.storageSlots(tile);
    }

    private int slotForRow(int row) {
        return slots() - 1 - row;
    }

    private boolean creative() {
        return tile.isCreative();
    }

    private int panelHeight() {
        return 20 + slots() * ROW_H + 4;
    }

    private boolean installed() {
        return !AdvancedUpgrades.find(tile).isEmpty();
    }

    private boolean canVoid() {
        return AdvancedUpgrades.hasVoidUpgrade(tile);
    }

    private boolean isFluid() {
        return tile instanceof FluidDrawerTile;
    }

    @Override
    public void drawBackgroundLayer(GuiGraphics graphics, Screen screen, IAssetProvider assets,
                                    int guiX, int guiY, int mouseX, int mouseY, float partialTicks) {}

    @Override
    public void drawForegroundLayer(GuiGraphics graphics, Screen screen, IAssetProvider assets,
                                    int guiX, int guiY, int mouseX, int mouseY, float partialTicks) {
        if (!installed()) {
            activeTab = TAB_NONE;
            swallowTooltips = false;
            return;
        }
        guiLeft = guiX;
        guiTop = guiY;
        active = this;
        build();
        syncFromStack();

        int relMouseX = mouseX - guiX;
        int relMouseY = mouseY - guiY;
        hoveredTab = tabAt(relMouseX, relMouseY);
        if (creative() || (activeTab == TAB_VOID && !canVoid())) {
            activeTab = TAB_NONE;
        }
        swallowTooltips = hoveredTab != TAB_NONE
                || (activeTab != TAB_NONE && insidePanel(relMouseX, relMouseY));

        graphics.pose().pushPose();
        graphics.pose().translate(0.0F, 0.0F, 500.0F);

        boolean usable = !creative();
        drawTab(graphics, TAB_LIMITS_Y, upgrade("diamond_upgrade"), activeTab == TAB_LIMITS, usable);
        drawTab(graphics, TAB_VOID_Y, upgrade("void_upgrade"), activeTab == TAB_VOID, usable && canVoid());

        if (activeTab == TAB_LIMITS) {
            drawLimitsPanel(graphics, relMouseX, relMouseY, partialTicks);
        } else if (activeTab == TAB_VOID) {
            drawVoidPanel(graphics);
        }
        graphics.pose().popPose();
        drawTabTooltip(graphics, relMouseX, relMouseY);
    }

    private void drawTabTooltip(GuiGraphics graphics, int relMouseX, int relMouseY) {
        List<Component> lines = tabTooltip();
        if (lines.isEmpty()) {
            return;
        }
        drawingOwnTooltip = true;
        graphics.pose().pushPose();
        graphics.pose().translate(0.0F, 0.0F, 600.0F);
        graphics.renderComponentTooltip(Minecraft.getInstance().font, lines, relMouseX, relMouseY);
        graphics.pose().popPose();
        drawingOwnTooltip = false;
    }

    private void drawTab(GuiGraphics graphics, int y, ItemStack icon, boolean active, boolean enabled) {
        graphics.fill(TAB_X, y, TAB_X + TAB_W, y + TAB_H, active ? TAB_BG_ACTIVE : TAB_BG);
        graphics.renderOutline(TAB_X, y, TAB_W, TAB_H, active ? BORDER : BOX);
        graphics.renderItem(icon, TAB_X + 2, y + 2);
        if (!enabled) {
            graphics.pose().pushPose();
            graphics.pose().translate(0.0F, 0.0F, 300.0F);
            graphics.fill(TAB_X + 1, y + 1, TAB_X + TAB_W - 1, y + TAB_H - 1, DISABLED_VEIL);
            graphics.pose().popPose();
        }
    }

    private void drawPanelFrame(GuiGraphics graphics, Component title) {
        graphics.fill(PANEL_X, PANEL_Y, PANEL_X + PANEL_W, PANEL_Y + panelHeight(), BG);
        graphics.renderOutline(PANEL_X, PANEL_Y, PANEL_W, panelHeight(), BORDER);
        graphics.drawString(Minecraft.getInstance().font, title, PANEL_X + 20, PANEL_Y + 5, 0xFFFFFF, false);
    }

    private void drawLimitsPanel(GuiGraphics graphics, int relMouseX, int relMouseY, float partialTicks) {
        drawPanelFrame(graphics, Component.translatable("func_au.gui.caps"));
        checkbox(graphics, PANEL_X + 5, PANEL_Y + 4, local.capsEnabled(), true);

        var font = Minecraft.getInstance().font;
        for (int row = 0; row < slots(); row++) {
            int slot = slotForRow(row);
            int ry = PANEL_Y + 20 + row * ROW_H;
            drawSlotIcon(graphics, slot, PANEL_X + 4, ry);
            EditBox box = capBoxes.get(slot);
            box.setPosition(PANEL_X + 22, ry + 3);
            box.setEditable(local.capsEnabled());
            box.renderWidget(graphics, relMouseX, relMouseY, partialTicks);
            graphics.drawString(font, "/ " + abbreviate(natural(slot)), PANEL_X + 96, ry + 5,
                    local.capsEnabled() ? 0xAAAAAA : 0x666666, false);
        }
    }

    private void drawVoidPanel(GuiGraphics graphics) {
        drawPanelFrame(graphics, Component.translatable("func_au.gui.void"));
        checkbox(graphics, PANEL_X + 5, PANEL_Y + 4, local.voidEnabled(), true);

        var font = Minecraft.getInstance().font;
        for (int row = 0; row < slots(); row++) {
            int slot = slotForRow(row);
            int ry = PANEL_Y + 20 + row * ROW_H;
            drawSlotIcon(graphics, slot, PANEL_X + 4, ry);
            checkbox(graphics, PANEL_X + 24, ry + 3, local.voids(slot), local.voidEnabled());
            graphics.drawString(font, Component.translatable(
                            local.voids(slot) ? "func_au.gui.voids_overflow" : "func_au.gui.keeps_overflow"),
                    PANEL_X + 40, ry + 5, local.voidEnabled() ? 0xAAAAAA : 0x666666, false);
        }
    }

    private static final int[][] TICK = {{1, 4}, {2, 5}, {3, 6}, {4, 5}, {5, 4}, {6, 3}, {7, 2}};

    private void checkbox(GuiGraphics graphics, int x, int y, boolean checked, boolean enabled) {
        graphics.fill(x, y, x + 10, y + 10, 0xFF202020);
        graphics.renderOutline(x, y, 10, 10, BOX);
        if (checked) {
            for (int[] pixel : TICK) {
                graphics.fill(x + pixel[0], y + pixel[1], x + pixel[0] + 2, y + pixel[1] + 2, ON);
            }
        }
        if (!enabled) {
            graphics.fill(x, y, x + 10, y + 10, DISABLED_VEIL);
        }
    }

    @Override
    public List<Component> getTooltipLines() {
        return List.of();
    }

    private List<Component> tabTooltip() {
        List<Component> lines = new ArrayList<>();
        if (hoveredTab == TAB_LIMITS) {
            lines.add(Component.translatable(isFluid() ? "func_au.gui.tab_limits_fluid" : "func_au.gui.tab_limits")
                    .withStyle(ChatFormatting.WHITE));
        } else if (hoveredTab == TAB_VOID) {
            lines.add(Component.translatable("func_au.gui.tab_void").withStyle(ChatFormatting.WHITE));
            if (!creative() && !canVoid()) {
                lines.add(Component.translatable("func_au.gui.needs_void_upgrade").withStyle(ChatFormatting.RED));
            }
        }
        if (!lines.isEmpty() && creative()) {
            lines.add(Component.translatable("func_au.gui.creative_disabled").withStyle(ChatFormatting.RED));
        }
        return lines;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!installed() || !built) {
            return false;
        }
        double mx = mouseX - guiLeft;
        double my = mouseY - guiTop;

        int tab = tabAt(mx, my);
        if (tab != TAB_NONE) {
            if (creative() || (tab == TAB_VOID && !canVoid())) {
                return true;
            }
            commitAll();
            activeTab = activeTab == tab ? TAB_NONE : tab;
            capBoxes.forEach(box -> box.setFocused(false));
            return true;
        }
        if (activeTab == TAB_LIMITS) {
            return clickLimits(mx, my, button);
        }
        if (activeTab == TAB_VOID) {
            return clickVoid(mx, my);
        }
        return false;
    }

    private boolean clickLimits(double mx, double my, int button) {
        if (inside(mx, my, PANEL_X + 5, PANEL_Y + 4, 10, 10)) {
            commitAll();
            push(local.withCapsEnabled(!local.capsEnabled()));
            return true;
        }
        boolean handled = false;
        for (int slot = 0; slot < capBoxes.size(); slot++) {
            EditBox box = capBoxes.get(slot);
            boolean hit = box.isMouseOver(mx, my);
            if (box.isFocused() && !hit) {
                commitCap(slot);
            }
            box.setFocused(hit && local.capsEnabled());
            if (hit) {
                box.mouseClicked(mx, my, button);
                handled = true;
            }
        }
        return handled || insidePanel(mx, my);
    }

    private boolean clickVoid(double mx, double my) {
        if (inside(mx, my, PANEL_X + 5, PANEL_Y + 4, 10, 10)) {
            push(local.withVoidEnabled(!local.voidEnabled()));
            return true;
        }
        if (local.voidEnabled()) {
            for (int row = 0; row < slots(); row++) {
                int slot = slotForRow(row);
                int ry = PANEL_Y + 20 + row * ROW_H;
                if (inside(mx, my, PANEL_X + 24, ry + 3, 10, 10)) {
                    push(local.withVoid(slot, !local.voids(slot), slots()));
                    return true;
                }
            }
        }
        return insidePanel(mx, my);
    }

    @Override
    public boolean keyPressed(int key, int scan, int modifiers) {
        EditBox box = focusedBox();
        if (box == null) {
            return false;
        }
        if (key == GLFW.GLFW_KEY_ENTER || key == GLFW.GLFW_KEY_KP_ENTER) {
            commitAll();
            capBoxes.forEach(each -> each.setFocused(false));
            return true;
        }
        return box.keyPressed(key, scan, modifiers);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        EditBox box = focusedBox();
        return box != null && box.charTyped(codePoint, modifiers);
    }

    @Override
    public boolean isFocused() {
        return focusedBox() != null;
    }

    @Override
    public void setFocused(boolean focused) {
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        if (!installed()) {
            return false;
        }
        return tabAt(mouseX, mouseY) != TAB_NONE
                || (activeTab != TAB_NONE && insidePanel(mouseX, mouseY));
    }

    private int tabAt(double mx, double my) {
        if (inside(mx, my, TAB_X, TAB_LIMITS_Y, TAB_W, TAB_H)) {
            return TAB_LIMITS;
        }
        if (inside(mx, my, TAB_X, TAB_VOID_Y, TAB_W, TAB_H)) {
            return TAB_VOID;
        }
        return TAB_NONE;
    }

    private boolean insidePanel(double mx, double my) {
        return inside(mx, my, PANEL_X, PANEL_Y, PANEL_W, panelHeight());
    }

    private EditBox focusedBox() {
        for (EditBox box : capBoxes) {
            if (box.isFocused()) {
                return box;
            }
        }
        return null;
    }

    private static boolean inside(double mouseX, double mouseY, int x, int y, int w, int h) {
        return mouseX >= x && mouseX < x + w && mouseY >= y && mouseY < y + h;
    }

    private void build() {
        if (built) {
            return;
        }
        var font = Minecraft.getInstance().font;
        for (int i = 0; i < slots(); i++) {
            final int slot = i;

            EditBox box = new EditBox(font, 0, 0, 70, 12, Component.translatable("func_au.gui.cap"));

            box.setMaxLength(10);
            box.setFilter(text -> text.isEmpty() || text.chars().allMatch(Character::isDigit));
            capBoxes.add(box);
        }
        built = true;
    }

    private void syncFromStack() {
        SlotSettings actual = AdvancedUpgrades.settingsIn(tile);
        if (actual.equals(lastSeen)) {
            return;
        }
        lastSeen = actual;
        local = actual;
        for (int i = 0; i < capBoxes.size(); i++) {
            int cap = i < actual.caps().size() ? actual.caps().get(i) : 0;
            String text = cap > 0 ? Integer.toString(cap) : "";
            if (!capBoxes.get(i).getValue().equals(text)) {
                setBoxText(i, text);
            }
        }
    }

    private void commitCap(int slot) {
        String text = capBoxes.get(slot).getValue();
        int value = text.isEmpty() ? 0 : clampCap(safeParse(text), slot);
        if (local.cap(slot) != value) {
            push(local.withCap(slot, value, slots()));
        }
        String settled = value > 0 ? Integer.toString(value) : "";
        if (!settled.equals(text)) {
            setBoxText(slot, settled);
        }
    }

    private void commitAll() {
        if (!built) {
            return;
        }
        for (int slot = 0; slot < capBoxes.size(); slot++) {
            commitCap(slot);
        }
    }

    public static void commitActive() {
        SlotConfigGuiAddon addon = active;
        active = null;
        if (addon != null) {
            addon.commitAll();
        }
    }

    private int clampCap(int value, int slot) {
        int ceiling = natural(slot);
        return ceiling > 0 ? Math.min(Math.max(1, value), ceiling) : Math.max(1, value);
    }

    private void setBoxText(int slot, String text) {
        capBoxes.get(slot).setValue(text);
    }

    // Formatted since high numbers don't sit well beside the field
    private static String abbreviate(int value) {
        if (value >= 1_000_000_000) {
            return String.format("%.1fB", value / 1_000_000_000.0);
        }
        if (value >= 1_000_000) {
            return String.format("%.1fM", value / 1_000_000.0);
        }
        if (value >= 10_000) {
            return String.format("%.1fK", value / 1_000.0);
        }
        return Integer.toString(value);
    }

    private static int safeParse(String text) {
        try {
            return (int) Math.min(Integer.MAX_VALUE, Long.parseLong(text));
        } catch (NumberFormatException e) {
            return Integer.MAX_VALUE;
        }
    }

    private void push(SlotSettings settings) {
        local = settings;
        lastSeen = settings;
        PacketDistributor.sendToServer(new SlotSettingsPayload(tile.getBlockPos(), settings));
    }

    private int natural(int slot) {
        if (tile instanceof FluidDrawerTile fluidTile
                && fluidTile.getFluidHandler() instanceof FluidDrawerAccess access) {
            return access.func_au$naturalCapacity();
        }
        if (tile instanceof ItemControllableDrawerTile<?> item
                && item.getStorage() instanceof DrawerSlotAccess access) {
            return access.func_au$naturalLimit(slot);
        }
        return 0;
    }

    private void drawSlotIcon(GuiGraphics graphics, int slot, int x, int y) {
        if (tile instanceof FluidDrawerTile fluidTile) {
            FluidStack fluid = fluidTile.getFluidHandler().getFluidInTank(slot);
            if (fluid.isEmpty()) {
                return;
            }
            IClientFluidTypeExtensions extensions = IClientFluidTypeExtensions.of(fluid.getFluid());
            TextureAtlasSprite sprite = Minecraft.getInstance()
                    .getTextureAtlas(InventoryMenu.BLOCK_ATLAS)
                    .apply(extensions.getStillTexture(fluid));
            int tint = extensions.getTintColor(fluid);
            graphics.setColor(((tint >> 16) & 0xFF) / 255.0F, ((tint >> 8) & 0xFF) / 255.0F,
                    (tint & 0xFF) / 255.0F, 1.0F);
            graphics.blit(x, y, 0, 16, 16, sprite);
            graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
            return;
        }
        graphics.renderItem(slotStack(slot), x, y);
    }

    private ItemStack slotStack(int slot) {
        if (tile instanceof ItemControllableDrawerTile<?> item) {
            ItemStack stack = item.getStorage().getStackInSlot(slot);
            if (!stack.isEmpty()) {
                return stack;
            }
        }
        return ItemStack.EMPTY;
    }

    private static ItemStack upgrade(String path) {
        return new ItemStack(BuiltInRegistries.ITEM.get(
                ResourceLocation.fromNamespaceAndPath("functionalstorage", path)));
    }

    @Override
    public int getXSize() {
        return TAB_W;
    }

    @Override
    public int getYSize() {
        return TAB_H;
    }
}
