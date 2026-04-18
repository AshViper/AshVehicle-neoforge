package Aru.Aru.ashvehicle.client.screen;

import Aru.Aru.ashvehicle.Packet.SetMissileTargetPacket;
import Aru.Aru.ashvehicle.Packet.PreviewCoordinateTargetPacket;
import Aru.Aru.ashvehicle.entity.projectile.BallisticMissileEntity;
import Aru.Aru.ashvehicle.init.CoordinateTargetVehicle;
import Aru.Aru.ashvehicle.init.ModNetwork;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.Collections;
import java.util.List;

public class CoordinateInputScreen extends Screen {
    private static final int MAP_SIZE = 280;
    private static final int PANEL_WIDTH = 190;
    private static final int FIELD_HEIGHT = 18;
    private static final double MIN_RANGE = 50.0;
    private static final double RANGE_STEP = 100.0;
    private static final int TERRAIN_GRID = 56;

    private static final int COL_BG = 0xFF080C10;
    private static final int COL_PANEL = 0xFF101418;
    private static final int COL_ACCENT = 0xFF00CCAA;
    private static final int COL_ACCENT_DIM = 0xFF006655;
    private static final int COL_ACCENT_GLOW = 0x5000FFCC;
    private static final int COL_GRID = 0x30008866;
    private static final int COL_TEXT = 0xFF00FFCC;
    private static final int COL_TEXT_WARN = 0xFFFFAA00;
    private static final int COL_TARGET = 0xFFFF2222;
    private static final int COL_TARGET_ENTITY = 0xFFFF00FF;
    private static final int COL_ENEMY = 0xFFFF6600;
    private static final int COL_MISSILE = 0xFF00DDFF;
    private static final int COL_SELF = 0xFF00FF44;
    private static final int COL_DISABLED = 0xFF555555;

    private final CoordinateTargetVehicle vehicle;

    private int mapX;
    private int mapY;
    private int fireBtnX;
    private int fireBtnY;
    private int fireBtnW;
    private int fireBtnH;

    private double radarRange = 500.0;
    private double lastTerrainRange = -1.0;

    private Vec3 manualTarget;
    private Entity targetEntity;

    private final int[][] terrain = new int[TERRAIN_GRID][TERRAIN_GRID];
    private boolean terrainReady = false;

    private float scanAngle = 0.0F;
    private long lastTime = System.currentTimeMillis();

    private EditBox targetXInput;
    private EditBox targetZInput;
    private Vec3 lastPreviewTarget;
    private boolean previewSent;

    public CoordinateInputScreen(CoordinateTargetVehicle vehicle) {
        super(Component.literal("Coordinate Strike Control"));
        this.vehicle = vehicle;
    }

    @Override
    protected void init() {
        int totalWidth = MAP_SIZE + 15 + PANEL_WIDTH;
        this.mapX = (this.width - totalWidth) / 2;
        this.mapY = (this.height - MAP_SIZE) / 2;

        this.initCoordinateInputs();
        this.loadTerrain();
        this.lastTerrainRange = this.radarRange;
    }

    @Override
    public void tick() {
        super.tick();
        if (this.isEditingCoordinates()) {
            this.targetEntity = null;
            this.syncManualTargetFromInputs();
        }
        this.pushPreviewTarget();
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        this.radarRange -= scrollY * RANGE_STEP;
        this.radarRange = Math.max(MIN_RANGE, Math.min(this.getVehicleMaxRange(), this.radarRange));

        if (Math.abs(this.radarRange - this.lastTerrainRange) > 50.0) {
            this.loadTerrain();
            this.lastTerrainRange = this.radarRange;
        }
        return true;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && this.isFireButtonEnabled()
                && mouseX >= this.fireBtnX && mouseX <= this.fireBtnX + this.fireBtnW
                && mouseY >= this.fireBtnY && mouseY <= this.fireBtnY + this.fireBtnH) {
            this.fire();
            return true;
        }

        if (button == 0 && mouseX >= this.mapX && mouseX <= this.mapX + MAP_SIZE && mouseY >= this.mapY && mouseY <= this.mapY + MAP_SIZE) {
            this.setCoordinateFocus(false);
            this.pickRadarTarget(mouseX, mouseY);
            return true;
        }

        if (this.targetXInput != null && this.targetXInput.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }
        if (this.targetZInput != null && this.targetZInput.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }

        this.setCoordinateFocus(false);

        return false;
    }

    @Override
    public void render(GuiGraphics gui, int mouseX, int mouseY, float partialTick) {
        this.refreshTrackedTarget();
        this.updateAnimation();

        gui.fill(0, 0, this.width, this.height, COL_BG);
        this.drawTitle(gui);
        this.drawRadarPanel(gui);
        this.drawInfoPanel(gui);
        if (this.targetXInput != null) {
            this.targetXInput.render(gui, mouseX, mouseY, partialTick);
        }
        if (this.targetZInput != null) {
            this.targetZInput.render(gui, mouseX, mouseY, partialTick);
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void removed() {
        super.removed();
        this.sendPreviewPacket(null);
    }

    private void initCoordinateInputs() {
        int panelX = this.mapX + MAP_SIZE + 15;
        int inputX = panelX + 30;
        int inputWidth = PANEL_WIDTH - 40;

        this.targetXInput = this.addRenderableWidget(new EditBox(this.font, inputX, this.mapY + 122, inputWidth, FIELD_HEIGHT, Component.literal("Target X")));
        this.targetXInput.setMaxLength(12);
        this.targetXInput.setHint(Component.literal("X"));

        this.targetZInput = this.addRenderableWidget(new EditBox(this.font, inputX, this.mapY + 146, inputWidth, FIELD_HEIGHT, Component.literal("Target Z")));
        this.targetZInput.setMaxLength(12);
        this.targetZInput.setHint(Component.literal("Z"));
    }

    private void loadTerrain() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) {
            return;
        }

        Vec3 center = this.getVehiclePos();
        double cellSize = (this.radarRange * 2.0) / TERRAIN_GRID;

        for (int x = 0; x < TERRAIN_GRID; x++) {
            for (int z = 0; z < TERRAIN_GRID; z++) {
                int worldX = (int) (center.x - this.radarRange + x * cellSize);
                int worldZ = (int) (center.z - this.radarRange + z * cellSize);
                this.terrain[x][z] = mc.level.getHeightmapPos(
                        Heightmap.Types.WORLD_SURFACE,
                        new BlockPos(worldX, 0, worldZ)
                ).getY();
            }
        }
        this.terrainReady = true;
    }

    private void drawTitle(GuiGraphics gui) {
        String title = "TACTICAL STRIKE CONTROL";
        int titleWidth = this.font.width(title);
        int titleX = this.width / 2 - titleWidth / 2;
        int titleY = this.mapY - 25;

        gui.fill(titleX - 12, titleY - 3, titleX + titleWidth + 12, titleY + 12, COL_PANEL);
        this.drawBorder(gui, titleX - 12, titleY - 3, titleX + titleWidth + 12, titleY + 12, COL_ACCENT);
        gui.drawString(this.font, title, titleX, titleY, COL_TEXT, false);
    }

    private void drawRadarPanel(GuiGraphics gui) {
        int centerX = this.mapX + MAP_SIZE / 2;
        int centerY = this.mapY + MAP_SIZE / 2;

        gui.fill(this.mapX - 6, this.mapY - 6, this.mapX + MAP_SIZE + 6, this.mapY + MAP_SIZE + 6, COL_PANEL);

        if (this.terrainReady) {
            int cell = MAP_SIZE / TERRAIN_GRID;
            for (int x = 0; x < TERRAIN_GRID; x++) {
                for (int z = 0; z < TERRAIN_GRID; z++) {
                    int heightValue = this.terrain[x][z];
                    int brightness = Math.min(50, Math.max(8, heightValue - 50));
                    int color = 0xFF000000 | (brightness / 4) | ((brightness / 2 + 10) << 8) | (brightness / 3 << 16);
                    gui.fill(this.mapX + x * cell, this.mapY + z * cell, this.mapX + x * cell + cell, this.mapY + z * cell + cell, color);
                }
            }
        }

        this.drawGrid(gui, this.mapX, this.mapY, MAP_SIZE, 6);
        this.drawScanSweep(gui, centerX, centerY, MAP_SIZE / 2);
        this.drawRangeCircles(gui, centerX, centerY);
        this.drawCompass(gui, centerX, centerY);

        Vec3 vehiclePos = this.getVehiclePos();
        double scale = MAP_SIZE / (this.radarRange * 2.0);

        for (Entity entity : this.getRadarEntities()) {
            if (entity.getId() == this.vehicle.getId()) {
                continue;
            }

            Vec3 delta = entity.position().subtract(vehiclePos);
            int entityX = (int) (centerX + delta.x * scale);
            int entityY = (int) (centerY + delta.z * scale);

            if (entityX < this.mapX || entityX > this.mapX + MAP_SIZE || entityY < this.mapY || entityY > this.mapY + MAP_SIZE) {
                continue;
            }

            boolean tracked = this.targetEntity != null && entity.getId() == this.targetEntity.getId();
            if (entity instanceof BallisticMissileEntity) {
                this.drawMissileBlip(gui, entityX, entityY);
            } else if (entity instanceof LivingEntity) {
                this.drawEnemyBlip(gui, entityX, entityY, tracked);
            } else {
                gui.fill(entityX - 2, entityY - 2, entityX + 2, entityY + 2, tracked ? COL_TARGET_ENTITY : 0xFF888888);
            }
        }

        this.drawSelfMarker(gui, centerX, centerY);

        if (this.manualTarget != null) {
            Vec3 delta = this.manualTarget.subtract(vehiclePos);
            int targetX = (int) (centerX + delta.x * scale);
            int targetY = (int) (centerY + delta.z * scale);
            this.drawTargetMarker(gui, targetX, targetY, this.targetEntity != null);
        }

        this.drawGlowBorder(gui, this.mapX - 6, this.mapY - 6, this.mapX + MAP_SIZE + 6, this.mapY + MAP_SIZE + 6);
        this.drawCorners(gui, this.mapX - 6, this.mapY - 6, this.mapX + MAP_SIZE + 6, this.mapY + MAP_SIZE + 6);
    }

    private void drawInfoPanel(GuiGraphics gui) {
        int panelX = this.mapX + MAP_SIZE + 15;
        int panelY = this.mapY;
        int panelWidth = PANEL_WIDTH;
        int panelHeight = MAP_SIZE;

        gui.fill(panelX, panelY, panelX + panelWidth, panelY + panelHeight, COL_PANEL);
        this.drawGlowBorder(gui, panelX, panelY, panelX + panelWidth, panelY + panelHeight);
        this.drawCorners(gui, panelX, panelY, panelX + panelWidth, panelY + panelHeight);

        Vec3 vehiclePos = this.getVehiclePos();
        boolean targetInRange = this.isFireButtonEnabled();

        int textY = panelY + 12;
        gui.drawString(this.font, "STATUS", panelX + 8, textY, COL_ACCENT, false);
        textY += 16;
        gui.fill(panelX + 6, textY, panelX + panelWidth - 6, textY + 1, COL_ACCENT_DIM);
        textY += 8;

        gui.drawString(this.font, "POS X: " + (int) vehiclePos.x, panelX + 8, textY, COL_TEXT, false);
        textY += 12;
        gui.drawString(this.font, "POS Z: " + (int) vehiclePos.z, panelX + 8, textY, COL_TEXT, false);
        textY += 12;
        gui.drawString(this.font, "VIEW: " + (int) this.radarRange + "m", panelX + 8, textY, COL_TEXT, false);
        textY += 12;
        gui.drawString(this.font, "MAX: " + (int) this.getVehicleMaxRange() + "m", panelX + 8, textY, COL_TEXT_WARN, false);
        textY += 18;

        gui.fill(panelX + 6, textY, panelX + panelWidth - 6, textY + 1, COL_ACCENT_DIM);
        textY += 8;
        gui.drawString(this.font, "TARGET", panelX + 8, textY, COL_ACCENT, false);
        textY += 16;

        gui.drawString(this.font, "X", panelX + 8, this.mapY + 127, COL_ACCENT_DIM, false);
        gui.drawString(this.font, "Z", panelX + 8, this.mapY + 151, COL_ACCENT_DIM, false);

        int targetInfoY = this.mapY + 175;
        if (this.manualTarget != null) {
            int targetColor = this.targetEntity != null ? COL_TARGET_ENTITY : COL_TARGET;
            gui.drawString(this.font, "TARGET X: " + (int) this.manualTarget.x, panelX + 8, targetInfoY, targetColor, false);
            gui.drawString(this.font, "TARGET Z: " + (int) this.manualTarget.z, panelX + 8, targetInfoY + 12, targetColor, false);
            gui.drawString(this.font, "DIST: " + (int) this.getTargetDistance() + "m", panelX + 8, targetInfoY + 24, targetInRange ? COL_TEXT_WARN : COL_TARGET, false);
            if (!targetInRange) {
                gui.drawString(this.font, "OUT OF RANGE", panelX + 8, targetInfoY + 36, COL_TARGET, false);
            } else if (this.targetEntity != null) {
                gui.drawString(this.font, "TRACKING ENTITY", panelX + 8, targetInfoY + 36, COL_TARGET_ENTITY, false);
            } else {
                gui.drawString(this.font, "COORDINATE STRIKE", panelX + 8, targetInfoY + 36, COL_TEXT, false);
            }
        } else {
            gui.drawString(this.font, "TYPE X/Z OR CLICK RADAR", panelX + 8, targetInfoY, COL_ACCENT_DIM, false);
        }

        this.fireBtnX = panelX + 8;
        this.fireBtnY = panelY + 220;
        this.fireBtnW = panelWidth - 16;
        this.fireBtnH = 24;
        this.drawFireButton(gui, this.fireBtnX, this.fireBtnY, this.fireBtnW, this.fireBtnH, targetInRange);

        int hintY = panelY + panelHeight - 28;
        gui.fill(panelX + 6, hintY - 4, panelX + panelWidth - 6, hintY - 3, COL_ACCENT_DIM);
        gui.drawString(this.font, "SCROLL: Zoom", panelX + 8, hintY, 0xFF444444, false);
        gui.drawString(this.font, "CLICK/TYPE: Target", panelX + 8, hintY + 10, 0xFF444444, false);
    }

    private void drawFireButton(GuiGraphics gui, int x, int y, int w, int h, boolean enabled) {
        int borderColor = enabled ? COL_TARGET : COL_DISABLED;
        int backgroundColor = enabled ? 0xFF441111 : 0xFF222222;

        if (enabled) {
            long pulse = System.currentTimeMillis() % 1200;
            float glow = (float) (Math.sin(pulse / 1200.0 * Math.PI * 2.0) * 0.5 + 0.5);
            int glowAlpha = (int) (60 * glow);
            gui.fill(x - 3, y - 3, x + w + 3, y + h + 3, (glowAlpha << 24) | 0x00FF0000);
        }

        gui.fill(x, y, x + w, y + h, backgroundColor);
        gui.fill(x, y, x + w, y + 2, borderColor);
        gui.fill(x, y + h - 2, x + w, y + h, borderColor);
        gui.fill(x, y, x + 2, y + h, borderColor);
        gui.fill(x + w - 2, y, x + w, y + h, borderColor);

        String text = enabled ? "FIRE" : "TARGET INVALID";
        int textWidth = this.font.width(text);
        gui.drawString(this.font, text, x + w / 2 - textWidth / 2, y + h / 2 - 4, borderColor, false);
    }

    private void fire() {
        if (this.isEditingCoordinates()) {
            this.syncManualTargetFromInputs();
        }

        if (!this.isFireButtonEnabled()) {
            return;
        }

        ModNetwork.INSTANCE.sendToServer(new SetMissileTargetPacket(
                this.vehicle.getId(),
                this.manualTarget.x,
                this.manualTarget.y,
                this.manualTarget.z
        ));
        this.previewSent = false;
    }

    private void pickRadarTarget(double mouseX, double mouseY) {
        int centerX = this.mapX + MAP_SIZE / 2;
        int centerY = this.mapY + MAP_SIZE / 2;
        double scale = MAP_SIZE / (this.radarRange * 2.0);
        Vec3 vehiclePos = this.getVehiclePos();

        Entity clickedEntity = null;
        double closestDistance = 10.0;
        for (Entity entity : this.getRadarEntities()) {
            if (entity.getId() == this.vehicle.getId()) {
                continue;
            }

            Vec3 delta = entity.position().subtract(vehiclePos);
            int entityX = (int) (centerX + delta.x * scale);
            int entityY = (int) (centerY + delta.z * scale);
            double distance = Math.hypot(mouseX - entityX, mouseY - entityY);
            if (distance < closestDistance) {
                closestDistance = distance;
                clickedEntity = entity;
            }
        }

        if (clickedEntity != null) {
            this.targetEntity = clickedEntity;
            this.manualTarget = clickedEntity.position();
        } else {
            this.targetEntity = null;
            double dx = (mouseX - centerX) / scale;
            double dz = (mouseY - centerY) / scale;
            this.manualTarget = this.getSurfaceTarget(vehiclePos.x + dx, vehiclePos.z + dz);
        }

        this.populateCoordinateFields(this.manualTarget);
    }

    private void refreshTrackedTarget() {
        if (this.targetEntity == null || this.isEditingCoordinates()) {
            return;
        }

        if (!this.targetEntity.isAlive()) {
            this.targetEntity = null;
            this.manualTarget = null;
            return;
        }

        this.manualTarget = this.targetEntity.position();
        this.populateCoordinateFields(this.manualTarget);
    }

    private void updateAnimation() {
        long now = System.currentTimeMillis();
        float dt = (now - this.lastTime) / 1000.0F;
        this.lastTime = now;
        this.scanAngle += dt * 45.0F;
        if (this.scanAngle >= 360.0F) {
            this.scanAngle -= 360.0F;
        }
    }

    private boolean syncManualTargetFromInputs() {
        if (this.targetXInput == null || this.targetZInput == null) {
            return false;
        }

        String xText = this.targetXInput.getValue().trim();
        String zText = this.targetZInput.getValue().trim();
        if (xText.isEmpty() || zText.isEmpty()) {
            this.manualTarget = null;
            return false;
        }

        Double x = this.parseCoordinate(xText);
        Double z = this.parseCoordinate(zText);
        if (x == null || z == null) {
            this.manualTarget = null;
            return false;
        }

        this.manualTarget = this.getSurfaceTarget(x, z);
        return true;
    }

    private void pushPreviewTarget() {
        if (!(this.getVehicleEntity() instanceof CoordinateTargetVehicle)) {
            return;
        }

        if (!this.isFireButtonEnabled()) {
            if (this.previewSent) {
                this.sendPreviewPacket(null);
            }
            return;
        }

        if (!this.previewSent || this.lastPreviewTarget == null || this.lastPreviewTarget.distanceToSqr(this.manualTarget) > 1.0) {
            this.sendPreviewPacket(this.manualTarget);
        }
    }

    private void sendPreviewPacket(Vec3 target) {
        if (target == null) {
            ModNetwork.INSTANCE.sendToServer(new PreviewCoordinateTargetPacket(this.vehicle.getId(), true, 0.0, 0.0, 0.0));
            this.previewSent = false;
            this.lastPreviewTarget = null;
            return;
        }

        ModNetwork.INSTANCE.sendToServer(new PreviewCoordinateTargetPacket(
                this.vehicle.getId(),
                false,
                target.x,
                target.y,
                target.z
        ));
        this.previewSent = true;
        this.lastPreviewTarget = target;
    }

    private void setCoordinateFocus(boolean focused) {
        if (this.targetXInput != null) {
            this.targetXInput.setFocused(focused);
        }
        if (this.targetZInput != null) {
            this.targetZInput.setFocused(focused);
        }
    }

    private void populateCoordinateFields(Vec3 target) {
        if (target == null || this.isEditingCoordinates()) {
            return;
        }

        if (this.targetXInput != null) {
            this.targetXInput.setValue(Integer.toString((int) Math.round(target.x)));
        }
        if (this.targetZInput != null) {
            this.targetZInput.setValue(Integer.toString((int) Math.round(target.z)));
        }
    }

    private boolean isEditingCoordinates() {
        return (this.targetXInput != null && this.targetXInput.isFocused())
                || (this.targetZInput != null && this.targetZInput.isFocused());
    }

    private boolean isFireButtonEnabled() {
        return this.manualTarget != null && this.getTargetDistance() <= this.getVehicleMaxRange();
    }

    private double getTargetDistance() {
        if (this.manualTarget == null) {
            return 0.0;
        }

        Vec3 vehiclePos = this.getVehiclePos();
        double dx = this.manualTarget.x - vehiclePos.x;
        double dz = this.manualTarget.z - vehiclePos.z;
        return Math.sqrt(dx * dx + dz * dz);
    }

    private double getVehicleMaxRange() {
        return Math.max(MIN_RANGE, this.vehicle.getCoordinateTargetMaxRange());
    }

    private Vec3 getSurfaceTarget(double x, double z) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) {
            return new Vec3(x, 0.0, z);
        }

        BlockPos surfacePos = mc.level.getHeightmapPos(Heightmap.Types.WORLD_SURFACE, BlockPos.containing(x, 0.0, z));
        return new Vec3(x, surfacePos.getY(), z);
    }

    private Entity getVehicleEntity() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) {
            return null;
        }
        return mc.level.getEntity(this.vehicle.getId());
    }

    private Vec3 getVehiclePos() {
        Entity entity = this.getVehicleEntity();
        return entity != null ? entity.position() : Vec3.ZERO;
    }

    private List<Entity> getRadarEntities() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) {
            return Collections.emptyList();
        }

        Vec3 center = this.getVehiclePos();
        double radiusSquared = this.radarRange * this.radarRange;
        AABB box = new AABB(
                center.x - this.radarRange, center.y - 500.0, center.z - this.radarRange,
                center.x + this.radarRange, center.y + 500.0, center.z + this.radarRange
        );

        return mc.level.getEntities((Entity) null, box, entity -> entity.isAlive() && entity.distanceToSqr(center) < radiusSquared);
    }

    private Double parseCoordinate(String value) {
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    private void drawGrid(GuiGraphics gui, int x, int y, int size, int divisions) {
        int step = size / divisions;
        for (int i = 1; i < divisions; i++) {
            gui.fill(x + i * step, y, x + i * step + 1, y + size, COL_GRID);
            gui.fill(x, y + i * step, x + size, y + i * step + 1, COL_GRID);
        }
    }

    private void drawScanSweep(GuiGraphics gui, int centerX, int centerY, int radius) {
        int segments = 30;
        float sweepAngle = 45.0F;
        for (int i = 0; i < segments; i++) {
            float angle = this.scanAngle - i * (sweepAngle / segments);
            double radians = Math.toRadians(angle);
            int alpha = (int) (80 * (1.0F - i / (float) segments));
            int color = (alpha << 24) | 0x00FFAA;

            int x1 = centerX + (int) (Math.cos(radians) * radius * 0.1);
            int y1 = centerY + (int) (Math.sin(radians) * radius * 0.1);
            int x2 = centerX + (int) (Math.cos(radians) * radius);
            int y2 = centerY + (int) (Math.sin(radians) * radius);
            this.drawLine(gui, x1, y1, x2, y2, color);
        }
    }

    private void drawLine(GuiGraphics gui, int x1, int y1, int x2, int y2, int color) {
        int dx = Math.abs(x2 - x1);
        int dy = Math.abs(y2 - y1);
        int steps = Math.max(dx, dy);
        if (steps == 0) {
            return;
        }

        for (int i = 0; i <= steps; i++) {
            int x = x1 + (x2 - x1) * i / steps;
            int y = y1 + (y2 - y1) * i / steps;
            gui.fill(x, y, x + 1, y + 1, color);
        }
    }

    private void drawRangeCircles(GuiGraphics gui, int centerX, int centerY) {
        int[] radii = {MAP_SIZE / 6, MAP_SIZE / 3, MAP_SIZE / 2};
        for (int radius : radii) {
            this.drawCircle(gui, centerX, centerY, radius, COL_GRID);
        }
    }

    private void drawCircle(GuiGraphics gui, int centerX, int centerY, int radius, int color) {
        int segments = 64;
        for (int i = 0; i < segments; i++) {
            double angle1 = Math.PI * 2.0 * i / segments;
            double angle2 = Math.PI * 2.0 * (i + 1) / segments;
            int x1 = centerX + (int) (Math.cos(angle1) * radius);
            int y1 = centerY + (int) (Math.sin(angle1) * radius);
            int x2 = centerX + (int) (Math.cos(angle2) * radius);
            int y2 = centerY + (int) (Math.sin(angle2) * radius);
            this.drawLine(gui, x1, y1, x2, y2, color);
        }
    }

    private void drawCompass(GuiGraphics gui, int centerX, int centerY) {
        gui.drawString(this.font, "N", centerX - 3, this.mapY + 4, COL_ACCENT, false);
        gui.drawString(this.font, "S", centerX - 3, this.mapY + MAP_SIZE - 12, COL_ACCENT_DIM, false);
        gui.drawString(this.font, "E", this.mapX + MAP_SIZE - 10, centerY - 4, COL_ACCENT_DIM, false);
        gui.drawString(this.font, "W", this.mapX + 4, centerY - 4, COL_ACCENT_DIM, false);
    }

    private void drawSelfMarker(GuiGraphics gui, int centerX, int centerY) {
        gui.fill(centerX - 1, centerY - 6, centerX + 2, centerY + 7, COL_SELF);
        gui.fill(centerX - 6, centerY - 1, centerX + 7, centerY + 2, COL_SELF);
        gui.fill(centerX - 3, centerY - 3, centerX + 4, centerY + 4, 0xFF003311);
        gui.fill(centerX - 2, centerY - 2, centerX + 3, centerY + 3, COL_SELF);
    }

    private void drawTargetMarker(GuiGraphics gui, int x, int y, boolean entityTarget) {
        long pulse = System.currentTimeMillis() % 1000;
        int size = 6 + (int) (pulse / 200);
        int alpha = (int) (255 * (1 - pulse / 1000.0));
        int baseColor = entityTarget ? 0xFF00FF : 0xFF2222;
        int pulseColor = (alpha << 24) | baseColor;
        int markerColor = entityTarget ? COL_TARGET_ENTITY : COL_TARGET;

        this.drawCircle(gui, x, y, size, pulseColor);
        gui.fill(x - 8, y - 1, x - 3, y + 2, markerColor);
        gui.fill(x + 4, y - 1, x + 9, y + 2, markerColor);
        gui.fill(x - 1, y - 8, x + 2, y - 3, markerColor);
        gui.fill(x - 1, y + 4, x + 2, y + 9, markerColor);
        gui.fill(x - 2, y - 2, x + 3, y + 3, markerColor);
    }

    private void drawEnemyBlip(GuiGraphics gui, int x, int y, boolean tracked) {
        int color = tracked ? COL_TARGET_ENTITY : COL_ENEMY;
        int background = tracked ? 0xFF330033 : 0xFF331100;
        gui.fill(x - 3, y - 3, x + 4, y + 4, background);
        gui.fill(x - 2, y - 2, x + 3, y + 3, color);
    }

    private void drawMissileBlip(GuiGraphics gui, int x, int y) {
        gui.fill(x - 1, y - 4, x + 2, y + 3, COL_MISSILE);
        gui.fill(x - 3, y + 1, x + 4, y + 3, COL_MISSILE);
    }

    private void drawGlowBorder(GuiGraphics gui, int x1, int y1, int x2, int y2) {
        gui.fill(x1 - 2, y1 - 2, x2 + 2, y1, COL_ACCENT_GLOW);
        gui.fill(x1 - 2, y2, x2 + 2, y2 + 2, COL_ACCENT_GLOW);
        gui.fill(x1 - 2, y1, x1, y2, COL_ACCENT_GLOW);
        gui.fill(x2, y1, x2 + 2, y2, COL_ACCENT_GLOW);
        this.drawBorder(gui, x1, y1, x2, y2, COL_ACCENT);
    }

    private void drawBorder(GuiGraphics gui, int x1, int y1, int x2, int y2, int color) {
        gui.fill(x1, y1, x2, y1 + 2, color);
        gui.fill(x1, y2 - 2, x2, y2, color);
        gui.fill(x1, y1, x1 + 2, y2, color);
        gui.fill(x2 - 2, y1, x2, y2, color);
    }

    private void drawCorners(GuiGraphics gui, int x1, int y1, int x2, int y2) {
        int length = 12;
        int thickness = 3;
        gui.fill(x1, y1, x1 + length, y1 + thickness, COL_ACCENT);
        gui.fill(x1, y1, x1 + thickness, y1 + length, COL_ACCENT);
        gui.fill(x2 - length, y1, x2, y1 + thickness, COL_ACCENT);
        gui.fill(x2 - thickness, y1, x2, y1 + length, COL_ACCENT);
        gui.fill(x1, y2 - thickness, x1 + length, y2, COL_ACCENT);
        gui.fill(x1, y2 - length, x1 + thickness, y2, COL_ACCENT);
        gui.fill(x2 - length, y2 - thickness, x2, y2, COL_ACCENT);
        gui.fill(x2 - thickness, y2 - length, x2, y2, COL_ACCENT);
    }
}
