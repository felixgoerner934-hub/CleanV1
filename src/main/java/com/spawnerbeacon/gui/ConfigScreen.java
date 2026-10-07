package com.spawnerbeacon.gui;

import com.spawnerbeacon.BeaconConfig;
import com.spawnerbeacon.spawner.SpawnerInfo;
import com.spawnerbeacon.spawner.SpawnerTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

/** Clean Cherry Blossom configuration screen. No hex text field is used. */
public final class ConfigScreen extends Screen {
    private static final int CHERRY = 0xFFE76F9A;
    private static final int DARK = 0xFF34252B;
    private static final int MUTED = 0xFF765B64;
    private static final int PANEL = 0xFFFDF8F9;
    private static final int SOFT = 0xFFF4E8EC;
    private static final int LINE = 0xFFDCC4CC;
    private static final int[] PALETTE = {
            0xE76F9A, 0xFF5A7D, 0xB93B60, 0xF4A6C1, 0xFF9F68, 0xFF9D32,
            0xF3C969, 0x91E65A, 0x48D9D0, 0x5AA9E6, 0x8D6BFF, 0xB06BE8,
            0xF4F1E8, 0xA6A6A6, 0x4A343B
    };
    private static final List<String> TABS = List.of("general", "colors", "spawners", "list");

    private final BeaconConfig cfg = BeaconConfig.get();
    private String tab = "general";
    private String selectedType = "zombie";
    private int listScroll;
    private boolean dirty;
    private long lastChange;
    private final long openedAt = System.nanoTime();

    public ConfigScreen() {
        super(Component.translatable("screen.spawnerbeacon.title"));
    }

    @Override
    protected void init() {
        clearWidgets();
        int panelLeft = Math.max(18, width / 2 - 280);
        int panelRight = Math.min(width - 18, width / 2 + 280);
        int top = 78;

        int tabWidth = (panelRight - panelLeft - 30) / 4;
        for (int i = 0; i < TABS.size(); i++) {
            String t = TABS.get(i);
            int x = panelLeft + i * (tabWidth + 10);
            addRenderableWidget(new CherryButton(x, 54, tabWidth, 22,
                    Component.translatable("screen.spawnerbeacon.tab." + t), () -> {
                        tab = t;
                        rebuildWidgets();
                    }));
        }

        switch (tab) {
            case "general" -> initGeneral(panelLeft, panelRight, top);
            case "colors" -> initColors(panelLeft, panelRight, top);
            case "spawners" -> initSpawners(panelLeft, panelRight, top);
            case "list" -> initList(panelLeft, panelRight, top);
        }

        addRenderableWidget(new CherryButton(panelLeft, height - 40, 180, 24,
                Component.translatable("screen.spawnerbeacon.reset"), () -> {
                    cfg.resetToDefaults();
                    dirty = false;
                    rebuildWidgets();
                }));
        addRenderableWidget(new CherryButton(panelRight - 180, height - 40, 180, 24,
                Component.translatable("gui.done"), this::onClose));
    }

    private void initGeneral(int left, int right, int y) {
        int w = (right - left - 14) / 2;
        addRenderableWidget(new CherryButton(left, y, w, 26,
                Component.translatable(cfg.enabled ? "screen.spawnerbeacon.enabled_on" : "screen.spawnerbeacon.enabled_off"), () -> {
                    cfg.enabled = !cfg.enabled; markChanged(); rebuildWidgets();
                }));
        addRenderableWidget(new CherryButton(left + w + 14, y, w, 26,
                Component.translatable("screen.spawnerbeacon.animation", cfg.animate ? "ON" : "OFF"), () -> {
                    cfg.animate = !cfg.animate; markChanged(); rebuildWidgets();
                }));
        y += 50;

        addRenderableWidget(new CherrySlider(left, y, right - left, 28, tr("screen.spawnerbeacon.thickness"), 0.1, 5.0,
                cfg.thickness, "%.2f", v -> { cfg.thickness = v; markChanged(); }));
        y += 48;
        addRenderableWidget(new CherrySlider(left, y, right - left, 28, tr("screen.spawnerbeacon.height"), 64, 512,
                cfg.maxY, "%.0f", v -> { cfg.maxY = (int) Math.round(v); markChanged(); }));
        y += 48;
        addRenderableWidget(new CherrySlider(left, y, right - left, 28, tr("screen.spawnerbeacon.opacity"), 0.05, 1.0,
                cfg.opacity, "%.2f", v -> { cfg.opacity = v; markChanged(); }));
        y += 48;
        addRenderableWidget(new CherrySlider(left, y, right - left, 28, tr("screen.spawnerbeacon.distance"), 2, 32,
                cfg.renderDistanceChunks, "%.0f chunks", v -> { cfg.renderDistanceChunks = (int) Math.round(v); markChanged(); }));
        y += 48;

        addRenderableWidget(new CherryButton(left, y, w, 26,
                Component.translatable("screen.spawnerbeacon.fade", cfg.distanceFade ? "ON" : "OFF"), () -> {
                    cfg.distanceFade = !cfg.distanceFade; markChanged(); rebuildWidgets();
                }));
        addRenderableWidget(new CherryButton(left + w + 14, y, w, 26,
                Component.translatable("screen.spawnerbeacon.rainbow", cfg.rainbow ? "ON" : "OFF"), () -> {
                    cfg.rainbow = !cfg.rainbow; markChanged(); rebuildWidgets();
                }));
        y += 42;
        addRenderableWidget(new CherryButton(left, y, w, 26,
                Component.translatable("screen.spawnerbeacon.dimension_overworld", cfg.overworld ? "ON" : "OFF"), () -> {
                    cfg.overworld = !cfg.overworld; markChanged(); rebuildWidgets();
                }));
        y += 42;
        addRenderableWidget(new CherryButton(left + w + 14, y, w, 26,
                Component.translatable("screen.spawnerbeacon.dimension_nether", cfg.nether ? "ON" : "OFF"), () -> {
                    cfg.nether = !cfg.nether; markChanged(); rebuildWidgets();
                }));
        y += 42;
        addRenderableWidget(new CherryButton(left, y, w, 26,
                Component.translatable("screen.spawnerbeacon.dimension_end", cfg.end ? "ON" : "OFF"), () -> {
                    cfg.end = !cfg.end; markChanged(); rebuildWidgets();
                }));
    }

    private void initColors(int left, int right, int y) {
        addRenderableWidget(new CherryButton(left, y, 180, 26,
                Component.translatable("screen.spawnerbeacon.global_color"), () -> { selectedType = "other"; rebuildWidgets(); }));
        y += 42;
        int sw = 28;
        for (int i = 0; i < PALETTE.length; i++) {
            int row = i / 7, col = i % 7;
            addRenderableWidget(new ColorSwatch(left + col * (sw + 7), y + row * (sw + 7), sw, PALETTE[i], c -> {
                setSelectedColor(c);
            }));
        }
        y += 3 * (sw + 7) + 22;
        int sliderWidth = right - left;
        addRenderableWidget(new CherrySlider(left, y, sliderWidth, 28, "R", 0, 255, red(selectedColor()), "%.0f", v -> setChannel(0, (int) Math.round(v))));
        y += 42;
        addRenderableWidget(new CherrySlider(left, y, sliderWidth, 28, "G", 0, 255, green(selectedColor()), "%.0f", v -> setChannel(1, (int) Math.round(v))));
        y += 42;
        addRenderableWidget(new CherrySlider(left, y, sliderWidth, 28, "B", 0, 255, blue(selectedColor()), "%.0f", v -> setChannel(2, (int) Math.round(v))));
    }

    private void initSpawners(int left, int right, int y) {
        int row = 0;
        for (String type : BeaconConfig.TYPES) {
            int yy = y + row * 43;
            if (yy > height - 72) break;
            addRenderableWidget(new CherryButton(left, yy, 190, 30, typeName(type), () -> {
                selectedType = type;
                tab = "colors";
                rebuildWidgets();
            }));
            addRenderableWidget(new ColorSwatch(left + 202, yy + 2, 26, cfg.colorFor(type), c -> {
                cfg.typeColors.put(type, c & 0xFFFFFF); markChanged();
            }));
            addRenderableWidget(new CherryButton(left + 240, yy, 110, 30,
                    Component.translatable(cfg.typeEnabled(type) ? "screen.spawnerbeacon.visible" : "screen.spawnerbeacon.hidden"), () -> {
                        cfg.typeEnabled.put(type, !cfg.typeEnabled(type)); markChanged(); rebuildWidgets();
                    }));
            addRenderableWidget(new CherrySlider(left + 362, yy, right - left - 362, 30, tr("screen.spawnerbeacon.type_thickness"), 0.1, 5.0,
                    cfg.thicknessFor(type), "%.1f", v -> { cfg.typeThickness.put(type, v); markChanged(); }));
            row++;
        }
    }

    private void initList(int left, int right, int y) {
        // The actual list is drawn in extractRenderState; this button is intentionally outside the scissor area.
        addRenderableWidget(new CherryButton(right - 190, y, 190, 26,
                Component.translatable("screen.spawnerbeacon.copy_closest"), () -> {
                    List<SpawnerInfo> list = SpawnerTracker.sortedByDistance();
                    if (!list.isEmpty()) org.lwjgl.glfw.GLFW.glfwSetClipboardString(Minecraft.getInstance().getWindow().handle(), formatPos(list.get(0).pos().getX(), list.get(0).pos().getY(), list.get(0).pos().getZ()));
                }));
    }

    private String selectedColorName() { return selectedType.equals("other") ? "screen.spawnerbeacon.default_color" : typeName(selectedType); }
    private int selectedColor() { return selectedType.equals("other") ? cfg.defaultColor : cfg.colorFor(selectedType); }
    private void setSelectedColor(int color) {
        if (selectedType.equals("other")) cfg.defaultColor = color & 0xFFFFFF;
        else cfg.typeColors.put(selectedType, color & 0xFFFFFF);
        markChanged(); rebuildWidgets();
    }
    private void setChannel(int channel, int value) {
        int c = selectedColor();
        int r = red(c), g = green(c), b = blue(c);
        if (channel == 0) r = value; else if (channel == 1) g = value; else b = value;
        int next = (r << 16) | (g << 8) | b;
        if (selectedType.equals("other")) cfg.defaultColor = next & 0xFFFFFF;
        else cfg.typeColors.put(selectedType, next & 0xFFFFFF);
        markChanged();
    }
    private int red(int c) { return (c >> 16) & 255; }
    private int green(int c) { return (c >> 8) & 255; }
    private int blue(int c) { return c & 255; }

    private String typeName(String type) {
        if (type.equals("other")) return tr("screen.spawnerbeacon.type.other");
        return Component.translatable("entity.minecraft." + type).getString();
    }
    private String tr(String key) { return Component.translatable(key).getString(); }

    private void markChanged() { dirty = true; lastChange = System.currentTimeMillis(); }

    @Override
    public void tick() {
        super.tick();
        if (dirty && System.currentTimeMillis() - lastChange > 500) {
            cfg.save();
            dirty = false;
        }
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        super.extractRenderState(graphics, mouseX, mouseY, delta);
        float open = Math.min(1f, (System.nanoTime() - openedAt) / 320_000_000f);
        int alpha = (int) (235 * open);
        int overlay = (alpha << 24) | 0xFFFDF8F9;
        graphics.fill(0, 0, width, height, overlay);

        int left = Math.max(18, width / 2 - 280);
        int right = Math.min(width - 18, width / 2 + 280);
        graphics.fillGradient(left, 16, right, height - 18, 0xFFFDF8F9, 0xFFF7EDEF);
        graphics.outline(left, 16, right - left, height - 34, LINE);
        graphics.text(font, Component.literal("SPAWNERBEACON"), left + 24, 25, DARK, false);
        graphics.text(font, Component.translatable("screen.spawnerbeacon.subtitle"), left + 24, 40, MUTED, false);
        graphics.horizontalLine(left + 24, right - 24, 48, LINE);

        drawBlossoms(graphics, left, 12, right);

        if (tab.equals("list")) drawSpawnerList(graphics, left, right, 120, mouseX, mouseY);
        if (tab.equals("colors")) {
            graphics.text(font, Component.translatable("screen.spawnerbeacon.selected_color"), left, 108, MUTED, false);
            graphics.fill(left, 112, left + 18, 130, 0xFF000000 | selectedColor());
            graphics.text(font, Component.literal(selectedColorName()), left + 28, 114, DARK, false);
        }
    }

    private void drawBlossoms(GuiGraphicsExtractor g, int left, int top, int right) {
        long t = System.currentTimeMillis() / 80;
        for (int i = 0; i < 7; i++) {
            int x = left + 18 + i * 35;
            int y = top + 2 + (int) ((Math.sin((t + i * 11) * 0.03) + 1) * 1.8);
            g.fill(x, y, x + 7, y + 3, 0xFFE9A0B5);
            g.fill(x + 2, y - 2, x + 5, y + 5, 0xFFFFC8D7);
        }
        for (int i = 0; i < 7; i++) {
            int x = right - 20 - i * 35;
            int y = top + 4 + (int) ((Math.cos((t + i * 9) * 0.03) + 1) * 1.8);
            g.fill(x, y, x + 7, y + 3, 0xFFE9A0B5);
            g.fill(x + 2, y - 2, x + 5, y + 5, 0xFFFFC8D7);
        }
    }

    private void drawSpawnerList(GuiGraphicsExtractor g, int left, int right, int top, int mouseX, int mouseY) {
        List<SpawnerInfo> list = SpawnerTracker.sortedByDistance();
        int rowH = 38;
        int bottom = height - 58;
        g.enableScissor(left, top, right, bottom);
        int y = top - listScroll;
        Vec3 camera = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
        for (SpawnerInfo info : list) {
            if (y + rowH >= top && y <= bottom) {
                int color = cfg.colorFor(info.type());
                g.fill(left, y, right, y + rowH - 4, 0xFFEFE1E5);
                g.fill(left, y, left + 4, y + rowH - 4, 0xFF000000 | color);
                g.text(font, typeName(info.type()), left + 14, y + 5, DARK, false);
                double distance = Math.sqrt(info.pos().distToCenterSqr(camera.x, camera.y, camera.z));
                String details = info.pos().getX() + " / " + info.pos().getY() + " / " + info.pos().getZ() + "   " + String.format(java.util.Locale.ROOT, "%.0fm", distance);
                g.text(font, details, left + 14, y + 20, MUTED, false);
                if (mouseX >= left && mouseX <= right && mouseY >= y && mouseY < y + rowH - 4) {
                    g.outline(left, y, right - left, rowH - 4, CHERRY);
                }
            }
            y += rowH;
        }
        g.disableScissor();
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (tab.equals("list")) {
            listScroll = Math.max(0, listScroll - (int) Math.round(scrollY * 30));
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public void onClose() {
        cfg.save();
        super.onClose();
    }

    private String formatPos(int x, int y, int z) { return x + " " + y + " " + z; }
}
