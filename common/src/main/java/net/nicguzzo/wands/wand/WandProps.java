package net.nicguzzo.wands.wand;

import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.block.Rotation;
import net.nicguzzo.wands.WandsMod;
import net.nicguzzo.wands.utils.Compat;
import net.nicguzzo.wands.utils.WandUtils;
import net.nicguzzo.wands.wand.modes.*;

import java.util.EnumSet;
import java.util.Map;

public class WandProps {
    public enum Mode {
        DIRECTION {
            public String toString() {
                return "wands.modes.direction";
            }

            public WandMode get_mode() {
                return new DirectionMode();
            }

            public boolean can_target_air() {
                return false;
            }

            public int n_clicks() {
                return 1;
            }
        }, ROW_COL {
            public String toString() {
                return "wands.modes.row_col";
            }

            public WandMode get_mode() {
                return new RowColMode();
            }

            public boolean can_target_air() {
                return true;
            }

            public int n_clicks() {
                return 1;
            }
        }, FILL {
            public String toString() {
                return "wands.modes.fill";
            }

            public WandMode get_mode() {
                return new FillMode();
            }

            public boolean can_target_air() {
                return true;
            }

            public int n_clicks() {
                return 2;
            }
        }, AREA {
            public String toString() {
                return "wands.modes.area";
            }

            public WandMode get_mode() {
                return new AreaMode();
            }

            public boolean can_target_air() {
                return false;
            }

            public int n_clicks() {
                return 1;
            }
        }, GRID {
            public String toString() {
                return "wands.modes.grid";
            }

            public WandMode get_mode() {
                return new GridMode();
            }

            public boolean can_target_air() {
                return true;
            }

            public int n_clicks() {
                return 1;
            }
        }, LINE {
            public String toString() {
                return "wands.modes.line";
            }

            public WandMode get_mode() {
                return new LineMode();
            }

            public boolean can_target_air() {
                return true;
            }

            public int n_clicks() {
                return 2;
            }
        }, CIRCLE {
            public String toString() {
                return "wands.modes.circle";
            }

            public WandMode get_mode() {
                return new CircleMode();
            }

            public boolean can_target_air() {
                return true;
            }

            public int n_clicks() {
                return 2;
            }
        }, COPY {
            public String toString() {
                return "wands.modes.copy";
            }

            public WandMode get_mode() {
                return new CopyMode();
            }

            public boolean can_target_air() {
                return true;
            }

            public int n_clicks() {
                return 2;
            }
        }, PASTE {
            public String toString() {
                return "wands.modes.paste";
            }

            public WandMode get_mode() {
                return new PasteMode();
            }

            public boolean can_target_air() {
                return true;
            }

            public int n_clicks() {
                return 1;
            }
        }, TUNNEL {
            public String toString() {
                return "wands.modes.tunnel";
            }

            public WandMode get_mode() {
                return new TunnelMode();
            }

            public boolean can_target_air() {
                return true;
            }

            public int n_clicks() {
                return 1;
            }
        }, VEIN {
            public String toString() {
                return "wands.modes.vein";
            }

            public WandMode get_mode() {
                return new VeinMode();
            }

            public boolean can_target_air() {
                return false;
            }

            public int n_clicks() {
                return 1;
            }
        }, BLAST {
            public String toString() {
                return "wands.modes.blast";
            }

            public WandMode get_mode() {
                return new BlastMode();
            }

            public boolean can_target_air() {
                return true;
            }

            public int n_clicks() {
                return 1;
            }
        }, SPHERE {
            public String toString() {
                return "wands.modes.sphere";
            }

            public WandMode get_mode() {
                return new SphereMode();
            }

            public boolean can_target_air() {
                return true;
            }

            public int n_clicks() {
                return 2;
            }
        }, ROCK {
            public String toString() {
                return "wands.modes.rock";
            }

            public WandMode get_mode() {
                return new RockMode();
            }

            public boolean can_target_air() {
                return true;
            }

            public int n_clicks() {
                return 1;
            }
        };

        public abstract WandMode get_mode();

        public abstract boolean can_target_air();

        public abstract int n_clicks();
    }

    public enum Orientation {
        ROW {
            public String toString() {
                return "wands.orientation.row";
            }
        }, COL {
            public String toString() {
                return "wands.orientation.col";
            }
        };
    }

    public enum Plane {
        XZ, XY, YZ
    }

    public enum MirrorAxis {
        NONE, LEFT_RIGHT, FRONT_BACK
    }

    public enum StateMode {
        CLONE {
            public String toString() {
                return "wands.state_mode.clone";
            }
        }, APPLY {
            public String toString() {
                return "wands.state_mode.apply";
            }
        }, APPLY_FLIP {
            public String toString() {
                return "wands.state_mode.apply_flip";
            }
        }, TARGET {
            public String toString() {
                return "wands.state_mode.target";
            }
        }
    }

    public enum Action {
        PLACE {
            public String toString() {
                return "wands.action.place";
            }
        }, REPLACE {
            public String toString() {
                return "wands.action.replace";
            }
        }, DESTROY {
            public String toString() {
                return "wands.action.destroy";
            }
        }, USE {
            public String toString() {
                return "wands.action.use";
            }
        }
    }

    public enum Flag {
        INVERTED {
            public String toString() {
                return "inverted";
            }

            public boolean get_default() {
                return false;
            }

            ;}, CFILLED {
            public String toString() {
                return "cfill";
            }

            public boolean get_default() {
                return false;
            }

            ;}, EVEN {
            public String toString() {
                return "circle_even";
            }

            public boolean get_default() {
                return false;
            }

            ;}, DIAGSPREAD {
            public String toString() {
                return "diag_spread";
            }

            public boolean get_default() {
                return false;
            }

            ;}, MATCHSTATE {
            public String toString() {
                return "match_state";
            }

            public boolean get_default() {
                return false;
            }

            ;}, INCSELBLOCK {
            public String toString() {
                return "inc_sel_block";
            }

            public boolean get_default() {
                return false;
            }

            ;}, STAIRSLAB {
            public String toString() {
                return "stair_slab";
            }

            public boolean get_default() {
                return false;
            }

            ;}, RFILLED {
            public String toString() {
                return "rfill";
            }

            public boolean get_default() {
                return true;
            }

            ;}, TARGET_AIR {
            public String toString() {
                return "target_air";
            }

            public boolean get_default() {
                return false;
            }

            ;},
        CLEAR_P1 {
            public String toString() {
                return "clear_p1";
            }

            public boolean get_default() {
                return true;
            }

            ;};

        public abstract boolean get_default();
    }

    public enum Value {
        MULTIPLIER {
            public String toString() {
                return "multiplier";
            }
        }, ROWCOLLIM {
            public String toString() {
                return "row_col_limit";
            }
        }, AREALIM {
            public String toString() {
                return "area_limit";
            }
        }, BLASTRAD {
            public String toString() {
                return "blast_radius";
            }
        }, GRIDM {
            public String toString() {
                return "grid_m";
            }
        }, GRIDN {
            public String toString() {
                return "grid_n";
            }
        }, GRIDMS {
            public String toString() {
                return "grid_msp";
            }
        }, GRIDNS {
            public String toString() {
                return "grid_nsp";
            }
        }, GRIDMOFF {
            public String toString() {
                return "grid_moff";
            }
        }, GRIDNOFF {
            public String toString() {
                return "grid_noff";
            }
        }, MIRRORAXIS {
            public String toString() {
                return "mirror_axis";
            }
        }, SKIPBLOCK {
            public String toString() {
                return "skip_block";
            }
        }, TUNNEL_W {
            public String toString() {
                return "tunnel_w";
            }
        }, TUNNEL_H {
            public String toString() {
                return "tunnel_h";
            }
        }, TUNNEL_OX {
            public String toString() {
                return "tunnel_ox";
            }
        }, TUNNEL_OY {
            public String toString() {
                return "tunnel_oy";
            }
        }, TUNNEL_DEPTH {
            public String toString() {
                return "tunnel_d";
            }
        }, ROCK_RADIUS {
            public String toString() {
                return "rock_radius";
            }
        }, ROCK_NOISE {
            public String toString() {
                return "rock_noise";
            }
        }, AIR_TARGET_DISTANCE {
            public String toString() {
                return "air_target_distance";
            }
        };
        public int def = 0;
        public int min = 0;
        public int max = 2048;
        public Value coval = null;

        static {
            MULTIPLIER.def = 1;
            MULTIPLIER.min = 1;
            MULTIPLIER.max = 16;
            ROWCOLLIM.def = 0;
            ROWCOLLIM.min = 0;
            BLASTRAD.def = 4;
            BLASTRAD.min = 4;
            BLASTRAD.max = 16;
            GRIDM.coval = GRIDN;
            GRIDN.coval = GRIDM;
            GRIDM.min = 1;
            GRIDN.min = 1;
            MIRRORAXIS.min = 0;
            MIRRORAXIS.max = 2;
            SKIPBLOCK.max = 100;
            TUNNEL_W.min = 1;
            TUNNEL_W.def = 3;
            TUNNEL_H.min = 1;
            TUNNEL_H.def = 3;
            TUNNEL_DEPTH.def = 3;
            TUNNEL_DEPTH.min = 1;
            ROCK_RADIUS.min=0;
            ROCK_RADIUS.def=2;
            ROCK_NOISE.min=0;
            ROCK_NOISE.max=16;
            ROCK_NOISE.def=3;
            AIR_TARGET_DISTANCE.def=0;
            AIR_TARGET_DISTANCE.min=0;
            AIR_TARGET_DISTANCE.max=10;
        }
    }

    static public Mode[] modes = Mode.values();
    static public Action[] actions = Action.values();
    static public Orientation[] orientations = Orientation.values();
    static public Plane[] planes = Plane.values();
    static public Direction.Axis[] axes = Direction.Axis.values();
    static public Rotation[] rotations = Rotation.values();
    static public StateMode[] state_modes = StateMode.values();
    static public MirrorAxis[] mirrorAxes = MirrorAxis.values();

    // Block state UI indices - combined StateMode + Axis for UI display
    public static final int BLOCK_STATE_CLONE = 0;
    public static final int BLOCK_STATE_APPLY_X = 1;
    public static final int BLOCK_STATE_APPLY_Y = 2;
    public static final int BLOCK_STATE_APPLY_Z = 3;
    public static final int BLOCK_STATE_FLIP_X = 4;
    public static final int BLOCK_STATE_FLIP_Y = 5;
    public static final int BLOCK_STATE_FLIP_Z = 6;
    public static final int BLOCK_STATE_NORMAL = 7;
    public static final Integer[] BLOCK_STATE_OPTIONS = {0, 1, 2, 3, 4, 5, 6, 7};

    /** Convert current StateMode + Axis to UI index */
    public static int getBlockStateIndex(ItemStack stack) {
        StateMode mode = getStateMode(stack);
        Direction.Axis axis = getAxis(stack);
        switch (mode) {
            case CLONE: return BLOCK_STATE_CLONE;
            case APPLY:
                switch (axis) {
                    case X: return BLOCK_STATE_APPLY_X;
                    case Y: return BLOCK_STATE_APPLY_Y;
                    case Z: return BLOCK_STATE_APPLY_Z;
                }
                break;
            case APPLY_FLIP:
                switch (axis) {
                    case X: return BLOCK_STATE_FLIP_X;
                    case Y: return BLOCK_STATE_FLIP_Y;
                    case Z: return BLOCK_STATE_FLIP_Z;
                }
                break;
            case TARGET: return BLOCK_STATE_NORMAL;
        }
        return BLOCK_STATE_NORMAL;
    }

    /** Set StateMode + Axis from UI index */
    public static void setBlockStateIndex(ItemStack stack, int index) {
        StateMode mode;
        Direction.Axis axis = Direction.Axis.Y;  // Default
        switch (index) {
            case BLOCK_STATE_CLONE:
                mode = StateMode.CLONE;
                break;
            case BLOCK_STATE_APPLY_X:
                mode = StateMode.APPLY;
                axis = Direction.Axis.X;
                break;
            case BLOCK_STATE_APPLY_Y:
                mode = StateMode.APPLY;
                axis = Direction.Axis.Y;
                break;
            case BLOCK_STATE_APPLY_Z:
                mode = StateMode.APPLY;
                axis = Direction.Axis.Z;
                break;
            case BLOCK_STATE_FLIP_X:
                mode = StateMode.APPLY_FLIP;
                axis = Direction.Axis.X;
                break;
            case BLOCK_STATE_FLIP_Y:
                mode = StateMode.APPLY_FLIP;
                axis = Direction.Axis.Y;
                break;
            case BLOCK_STATE_FLIP_Z:
                mode = StateMode.APPLY_FLIP;
                axis = Direction.Axis.Z;
                break;
            case BLOCK_STATE_NORMAL:
            default:
                mode = StateMode.TARGET;
                break;
        }
        setStateMode(stack, mode);
        setAxis(stack, axis);
    }

    // Mode-specific flag mappings: which flags apply to which modes
    public static final Map<Flag, EnumSet<Mode>> FLAG_MODES = Map.ofEntries(
        Map.entry(Flag.INVERTED, EnumSet.of(Mode.DIRECTION)),
        Map.entry(Flag.CFILLED, EnumSet.of(Mode.CIRCLE)),
        Map.entry(Flag.EVEN, EnumSet.of(Mode.CIRCLE)),
        Map.entry(Flag.DIAGSPREAD, EnumSet.of(Mode.AREA)),
        Map.entry(Flag.MATCHSTATE, EnumSet.of(Mode.AREA, Mode.VEIN)),
        Map.entry(Flag.INCSELBLOCK, EnumSet.of(Mode.PASTE, Mode.TUNNEL)),
        Map.entry(Flag.STAIRSLAB, EnumSet.allOf(Mode.class)),  // All modes
        Map.entry(Flag.RFILLED, EnumSet.of(Mode.FILL)),
        Map.entry(Flag.TARGET_AIR, EnumSet.of(Mode.ROW_COL, Mode.GRID, Mode.COPY, Mode.PASTE, Mode.TUNNEL, Mode.ROCK)),
        Map.entry(Flag.CLEAR_P1, EnumSet.allOf(Mode.class))  // All modes
    );

    // Mode-specific value mappings: which values apply to which modes
    public static final Map<Value, EnumSet<Mode>> VALUE_MODES = Map.ofEntries(
        Map.entry(Value.MULTIPLIER, EnumSet.of(Mode.DIRECTION)),
        Map.entry(Value.ROWCOLLIM, EnumSet.of(Mode.ROW_COL)),
        Map.entry(Value.AREALIM, EnumSet.of(Mode.AREA, Mode.VEIN)),
        Map.entry(Value.BLASTRAD, EnumSet.of(Mode.BLAST)),
        Map.entry(Value.GRIDM, EnumSet.of(Mode.GRID)),
        Map.entry(Value.GRIDN, EnumSet.of(Mode.GRID)),
        Map.entry(Value.GRIDMS, EnumSet.of(Mode.GRID)),
        Map.entry(Value.GRIDNS, EnumSet.of(Mode.GRID)),
        Map.entry(Value.GRIDMOFF, EnumSet.of(Mode.GRID)),
        Map.entry(Value.GRIDNOFF, EnumSet.of(Mode.GRID)),
        Map.entry(Value.MIRRORAXIS, EnumSet.of(Mode.PASTE)),
        Map.entry(Value.SKIPBLOCK, EnumSet.of(Mode.AREA)),
        Map.entry(Value.TUNNEL_W, EnumSet.of(Mode.TUNNEL)),
        Map.entry(Value.TUNNEL_H, EnumSet.of(Mode.TUNNEL)),
        Map.entry(Value.TUNNEL_OX, EnumSet.of(Mode.TUNNEL)),
        Map.entry(Value.TUNNEL_OY, EnumSet.of(Mode.TUNNEL)),
        Map.entry(Value.TUNNEL_DEPTH, EnumSet.of(Mode.TUNNEL)),
        Map.entry(Value.ROCK_RADIUS, EnumSet.of(Mode.ROCK)),
        Map.entry(Value.ROCK_NOISE, EnumSet.of(Mode.ROCK)),
        Map.entry(Value.AIR_TARGET_DISTANCE, EnumSet.of(Mode.ROW_COL, Mode.GRID, Mode.PASTE, Mode.TUNNEL, Mode.ROCK))
    );

    // Modes where state_mode (block state) applies
    // PASTE returns early in state_for_placement(), COPY/BLAST don't place blocks
    public static final EnumSet<Mode> STATE_MODE_MODES = EnumSet.of(
        Mode.DIRECTION, Mode.ROW_COL, Mode.LINE, Mode.CIRCLE, Mode.SPHERE,
        Mode.FILL, Mode.AREA, Mode.TUNNEL, Mode.GRID, Mode.VEIN, Mode.ROCK
    );

    // Modes where rotation setting is relevant (for HUD display)
    public static final EnumSet<Mode> ROTATION_MODES = EnumSet.of(Mode.GRID, Mode.PASTE);

    // Action-to-mode mappings: which actions apply to which modes
    // COPY and BLAST ignore actions entirely, VEIN doesn't support PLACE
    public static final Map<Action, EnumSet<Mode>> ACTION_MODES = Map.of(
        Action.PLACE, EnumSet.of(Mode.DIRECTION, Mode.ROW_COL, Mode.FILL, Mode.AREA, Mode.GRID, Mode.LINE,
            Mode.CIRCLE, Mode.PASTE, Mode.TUNNEL, Mode.SPHERE, Mode.ROCK),
        Action.REPLACE, EnumSet.of(Mode.DIRECTION, Mode.ROW_COL, Mode.FILL, Mode.AREA, Mode.GRID, Mode.LINE,
            Mode.CIRCLE, Mode.PASTE, Mode.TUNNEL, Mode.VEIN, Mode.SPHERE, Mode.ROCK),
        Action.DESTROY, EnumSet.of(Mode.DIRECTION, Mode.ROW_COL, Mode.FILL, Mode.AREA, Mode.GRID, Mode.LINE,
            Mode.CIRCLE, Mode.PASTE, Mode.TUNNEL, Mode.VEIN, Mode.SPHERE, Mode.ROCK),
        Action.USE, EnumSet.of(Mode.DIRECTION, Mode.ROW_COL, Mode.FILL, Mode.AREA, Mode.GRID, Mode.LINE,
            Mode.CIRCLE, Mode.PASTE, Mode.TUNNEL, Mode.VEIN, Mode.SPHERE, Mode.ROCK)
    );

    // Helper to check if an action applies to a mode
    public static boolean actionAppliesTo(Action action, Mode mode) {
        EnumSet<Mode> modes = ACTION_MODES.get(action);
        return modes != null && modes.contains(mode);
    }

    // Check if any action applies to a mode (for hiding entire action row)
    public static boolean anyActionAppliesTo(Mode mode) {
        for (Action action : Action.values()) {
            if (actionAppliesTo(action, mode)) {
                return true;
            }
        }
        return false;
    }

    /*
     * Axis (Direction.Axis X/Y/Z) usage:
     *
     * 1. ALL MODES - Pillar block orientation:
     *    When state_mode is APPLY or TARGET, the axis setting determines the
     *    orientation of RotatedPillarBlock types (logs, pillars, etc.)
     *    See: Wand.java get_target_state() - sets RotatedPillarBlock.AXIS
     *
     * 2. FILL MODE ONLY - Hollow fill direction:
     *    When RFILLED flag is OFF (hollow mode), axis determines which direction
     *    the hollow tube runs:
     *    - X axis: Horizontal tube along X (Y and Z walls filled, X ends open)
     *    - Y axis: Vertical tube (X and Z walls filled, top/bottom open)
     *    - Z axis: Horizontal tube along Z (X and Y walls filled, Z ends open)
     *    See: Wand.java fill() method hollow logic
     */

    // Helper to check if state_mode applies to a mode
    public static boolean stateModeAppliesTo(Mode mode) {
        return STATE_MODE_MODES.contains(mode);
    }

    // Helper to check if rotation is relevant for a mode (for HUD display)
    public static boolean rotationAppliesTo(Mode mode) {
        return ROTATION_MODES.contains(mode);
    }

    // Helper methods to check if a flag/value applies to a mode
    public static boolean flagAppliesTo(Flag flag, Mode mode) {
        EnumSet<Mode> modes = FLAG_MODES.get(flag);
        return modes != null && modes.contains(mode);
    }

    public static boolean valueAppliesTo(Value value, Mode mode) {
        EnumSet<Mode> modes = VALUE_MODES.get(value);
        return modes != null && modes.contains(mode);
    }

    public static EnumSet<Mode> getModesForFlag(Flag flag) {
        return FLAG_MODES.getOrDefault(flag, EnumSet.noneOf(Mode.class));
    }

    public static EnumSet<Mode> getModesForValue(Value value) {
        return VALUE_MODES.getOrDefault(value, EnumSet.noneOf(Mode.class));
    }

    // Validates that all Flag, Value, and Action enums have mode mappings
    // Logs warnings for missing mappings to help catch drift during development
    public static void validateMappings() {
        for (Flag flag : Flag.values()) {
            if (!FLAG_MODES.containsKey(flag)) {
                WandsMod.LOGGER.warn("[WandProps] Flag {} has no mode mapping in FLAG_MODES", flag.name());
            }
        }
        for (Value value : Value.values()) {
            if (!VALUE_MODES.containsKey(value)) {
                WandsMod.LOGGER.warn("[WandProps] Value {} has no mode mapping in VALUE_MODES", value.name());
            }
        }
        for (Action action : Action.values()) {
            if (!ACTION_MODES.containsKey(action)) {
                WandsMod.LOGGER.warn("[WandProps] Action {} has no mode mapping in ACTION_MODES", action.name());
            }
        }
    }

    // Run validation when class loads
    static {
        validateMappings();
    }

    static public boolean getFlag(ItemStack stack, Flag flag) {
        if (WandUtils.is_wand(stack)) {
            CompoundTag tag = Compat.getTags(stack);
            if (tag.contains(flag.toString())) {
                return tag.getBoolean(flag.toString()).orElse(flag.get_default());
            } else {
                tag.putBoolean(flag.toString(), flag.get_default());
                return flag.get_default();
            }
        }
        return false;
    }

    static public void setFlag(ItemStack stack, Flag flag, boolean f) {
        if (WandUtils.is_wand(stack)) {
            CompoundTag tag = Compat.getTags(stack);
            tag.putBoolean(flag.toString(), f);
            CustomData.set(DataComponents.CUSTOM_DATA, stack, tag);
        }
    }

    static public void toggleFlag(ItemStack stack, Flag flag) {
        if (WandUtils.is_wand(stack)) {
            CompoundTag tag = Compat.getTags(stack);
            boolean b = getFlag(stack, flag);
            tag.putBoolean(flag.toString(), !b);
            CustomData.set(DataComponents.CUSTOM_DATA, stack, tag);
        }
    }

    static public void setVal(ItemStack stack, Value v, int n) {
        if (WandUtils.is_wand(stack)) {
            if (n > v.max) n = v.max;
            if (n < v.min) n = v.min;
            //stack.getOrCreateTag().putInt(v.toString(), n);
            CompoundTag tag = Compat.getTags(stack);
            tag.putInt(v.toString(), n);
            CustomData.set(DataComponents.CUSTOM_DATA, stack, tag);
        }
    }

    static public void incVal(ItemStack stack, Value v, int inc, int max) {
        if (WandUtils.is_wand(stack)) {
            CompoundTag tag = Compat.getTags(stack);
            int n = tag.getInt(v.toString()).orElse(v.def);
            if (n > v.max) n = v.max;
            if (n < v.min) n = v.min;
            int newVal = Math.min(n + inc, max);
            if (newVal != n) {
                tag.putInt(v.toString(), newVal);
                CustomData.set(DataComponents.CUSTOM_DATA, stack, tag);
            }
        }
    }

    static public void incVal(ItemStack stack, Value v, int inc) {
        incVal(stack, v, inc, v.max);
    }

    static public void decVal(ItemStack stack, Value v, int dec, int min) {
        if (WandUtils.is_wand(stack)) {
            CompoundTag tag = Compat.getTags(stack);
            int n = tag.getInt(v.toString()).orElse(v.def);
            if (n > v.max) n = v.max;
            if (n < v.min) n = v.min;
            int newVal = Math.max(n - dec, min);
            if (newVal != n) {
                tag.putInt(v.toString(), newVal);
                CustomData.set(DataComponents.CUSTOM_DATA, stack, tag);
            }
        }
    }

    static public void decVal(ItemStack stack, Value v, int dec) {
        decVal(stack, v, dec, v.min);
    }

    static public int getVal(ItemStack stack, Value v) {
        if (!WandUtils.is_wand(stack)) {
            return -1;
        }
        CompoundTag tag = Compat.getTags(stack);
        int i;
        if(!tag.contains(v.toString())){
            i=v.def;
        }else {
            i = tag.getInt(v.toString()).orElse(v.def);
        }
        if (i > v.max) {
            i = v.max;
            tag.putInt(v.toString(), i);
            CustomData.set(DataComponents.CUSTOM_DATA, stack, tag);
        }
        if (i < v.min) {
            i = v.min;
            tag.putInt(v.toString(), i);
            CustomData.set(DataComponents.CUSTOM_DATA, stack, tag);
        }
        if (i < v.min) {
            return v.min;
        } else if (i > v.max) {
            return v.max;
        }
        return i;
    }

    static public Mode getMode(ItemStack stack) {
        if (!WandUtils.is_wand(stack)) {
            return Mode.DIRECTION;
        }
        CompoundTag tag = Compat.getTags(stack);
        int m = tag.getInt("mode").orElse(0);
        if (m >= 0 && m < modes.length) {
            return modes[m];
        }
        return Mode.DIRECTION;
    }

    static public void setMode(ItemStack stack, Mode mode) {
        if (!WandUtils.is_wand(stack)) {
            return;
        }
        CompoundTag tag = Compat.getTags(stack);
        //WandsMod.LOGGER.info("tag: "+tag);
        tag.putInt("mode", mode.ordinal());
        CustomData.set(DataComponents.CUSTOM_DATA, stack, tag);
    }

    static public void nextMode(ItemStack stack, boolean can_blast) {
        if (!WandUtils.is_wand(stack)) {
            return;
        }
        CompoundTag tag = Compat.getTags(stack);
        //WandsMod.LOGGER.info("tag: "+tag);
        int mode = (tag.getInt("mode").orElse(0) + 1) % (modes.length);

        if (mode == Mode.VEIN.ordinal() && !WandsMod.config.enable_vein_mode) {
            mode = Mode.BLAST.ordinal();
        }
        if ((!can_blast && mode == Mode.BLAST.ordinal()) || (!WandsMod.config.enable_blast_mode && mode == Mode.BLAST.ordinal())) {
            mode = Mode.DIRECTION.ordinal();
        }
        tag.putInt("mode", mode);
        CustomData.set(DataComponents.CUSTOM_DATA, stack, tag);
    }

    static public void prevMode(ItemStack stack, boolean can_blast) {
        if (!WandUtils.is_wand(stack)) {
            return;
        }
        CompoundTag tag = Compat.getTags(stack);
        //WandsMod.LOGGER.info("tag: "+tag);
        int mode = tag.getInt("mode").orElse(1) - 1;
        if (mode < 0) {
            mode = modes.length - 1;
        }
        if ((!can_blast && mode == Mode.BLAST.ordinal()) || (!WandsMod.config.enable_blast_mode && mode == Mode.BLAST.ordinal())) {
            mode = Mode.BLAST.ordinal() - 1;
        }
        if (mode == Mode.VEIN.ordinal() && !WandsMod.config.enable_vein_mode) {
            mode = Mode.VEIN.ordinal() - 1;
        }
        tag.putInt("mode", mode);
        CustomData.set(DataComponents.CUSTOM_DATA, stack, tag);
    }

    static public Orientation getOrientation(ItemStack stack) {
        if (!WandUtils.is_wand(stack)) {
            return Orientation.ROW;
        }
        CompoundTag tag = Compat.getTags(stack);
        return orientations[tag.getInt("orientation").orElse(0)];
    }

    static public void setOrientation(ItemStack stack, Orientation o) {
        if (!WandUtils.is_wand(stack)) {
            return;
        }
        CompoundTag tag = Compat.getTags(stack);
        tag.putInt("orientation", o.ordinal());
        CustomData.set(DataComponents.CUSTOM_DATA, stack, tag);

    }

    static public void nextOrientation(ItemStack stack) {
        if (!WandUtils.is_wand(stack)) {
            return;
        }
        CompoundTag tag = Compat.getTags(stack);
        int o = (tag.getInt("orientation").orElse(0) + 1) % 2;
        tag.putInt("orientation", o);

        CustomData.set(DataComponents.CUSTOM_DATA, stack, tag);

    }

    static public Plane getPlane(ItemStack stack) {
        Plane plane = Plane.XZ;
        if (!WandUtils.is_wand(stack)) {
            return plane;
        }
        CompoundTag tag = Compat.getTags(stack);
        int p = tag.getInt("plane").orElse(0);
        CustomData.set(DataComponents.CUSTOM_DATA, stack, tag);
        if (p >= 0 && p < planes.length) plane = planes[p];
        return plane;
    }

    static public void setPlane(ItemStack stack, Plane p) {
        if (!WandUtils.is_wand(stack)) {
            return;
        }
        CompoundTag tag = Compat.getTags(stack);
        tag.putInt("plane", p.ordinal());
        CustomData.set(DataComponents.CUSTOM_DATA, stack, tag);

    }

    static public void nextPlane(ItemStack stack) {
        if (!WandUtils.is_wand(stack)) {
            return;
        }
        CompoundTag tag = Compat.getTags(stack);
        int plane = (tag.getInt("plane").orElse(0) + 1) % 3;
        tag.putInt("plane", plane);

        CustomData.set(DataComponents.CUSTOM_DATA, stack, tag);

    }

    static public Rotation getRotation(ItemStack stack) {
        if (!WandUtils.is_wand(stack)) {
            return Rotation.NONE;
        }
        CompoundTag tag = Compat.getTags(stack);
        return rotations[tag.getInt("rotation").orElse(0)];
    }

    static public void nextRotation(ItemStack stack) {
        if (!WandUtils.is_wand(stack)) {
            return;
        }
        CompoundTag tag = Compat.getTags(stack);
        int rot = (tag.getInt("rotation").orElse(0) + 1) % rotations.length;
        tag.putInt("rotation", rot);
        CustomData.set(DataComponents.CUSTOM_DATA, stack, tag);
    }

    static public void setRotation(ItemStack stack, Rotation rot) {
        if (!WandUtils.is_wand(stack)) {
            return;
        }
        CompoundTag tag = Compat.getTags(stack);
        tag.putInt("rotation", rot.ordinal());
        CustomData.set(DataComponents.CUSTOM_DATA, stack, tag);
    }

    static public Action getAction(ItemStack stack) {
        if (WandUtils.is_wand(stack)) {
            CompoundTag tag = Compat.getTags(stack);
            int m = tag.getInt("action").orElse(0);
            if (m < actions.length) return actions[m];
        }
        return Action.PLACE;
    }

    static public void setAction(ItemStack stack, Action a) {
        if (WandUtils.is_wand(stack)) {
            CompoundTag tag = Compat.getTags(stack);
            if (WandsMod.config.disable_destroy_replace && (a == Action.DESTROY || a == Action.REPLACE)) {
                a = Action.PLACE;
            }
            tag.putInt("action", a.ordinal());
            CustomData.set(DataComponents.CUSTOM_DATA, stack, tag);
        }
    }

    static public void nextAction(ItemStack stack) {
        if (WandUtils.is_wand(stack)) {
            CompoundTag tag = Compat.getTags(stack);
            int a = (tag.getInt("action").orElse(0) + 1) % (actions.length);
            if (WandsMod.config.disable_destroy_replace && (a == Action.DESTROY.ordinal() || a == Action.REPLACE.ordinal())) {
                a = Action.USE.ordinal();
            }
            tag.putInt("action", a);
            CustomData.set(DataComponents.CUSTOM_DATA, stack, tag);
        }
    }

    static public void prevAction(ItemStack stack) {
        if (WandUtils.is_wand(stack)) {
            CompoundTag tag = Compat.getTags(stack);
            int a = tag.getInt("action").orElse(0) - 1;
            if (a < 0) {
                a = actions.length - 1;
            }
            if (WandsMod.config.disable_destroy_replace && (a == Action.DESTROY.ordinal() || a == Action.REPLACE.ordinal())) {
                a = Action.PLACE.ordinal();
            }
            tag.putInt("action", a);
            CustomData.set(DataComponents.CUSTOM_DATA, stack, tag);
        }
    }

    static public Direction.Axis getAxis(ItemStack stack) {
        Direction.Axis axis = Direction.Axis.Y;
        CompoundTag tag = Compat.getTags(stack);
        if (WandUtils.is_wand(stack)) {
            int p = tag.getInt("axis").orElse(0);
            if (p >= 0 && p < axes.length) axis = axes[p];
        }
        return axis;
    }

    static public void setAxis(ItemStack stack, Direction.Axis axis) {
        if (WandUtils.is_wand(stack)) {
            CompoundTag tag = Compat.getTags(stack);
            tag.putInt("axis", axis.ordinal());
            CustomData.set(DataComponents.CUSTOM_DATA, stack, tag);
        }
    }

    static public void setAxis(ItemStack stack, int a) {
        if (stack != null && WandUtils.is_wand(stack) && !stack.isEmpty()) {
            CompoundTag tag = Compat.getTags(stack);
            tag.putInt("axis", a);
            CustomData.set(DataComponents.CUSTOM_DATA, stack, tag);
        }
    }

    static public void nextAxis(ItemStack stack) {
        if (WandUtils.is_wand(stack)) {
            CompoundTag tag = Compat.getTags(stack);
            int axis = (tag.getInt("axis").orElse(0) + 1) % 3;
            tag.putInt("axis", axis);
            CustomData.set(DataComponents.CUSTOM_DATA, stack, tag);
        }
    }

    static public StateMode getStateMode(ItemStack stack) {
        if (WandUtils.is_wand(stack)) {
            CompoundTag tag = Compat.getTags(stack);
            int m = tag.getInt("state_mode").orElse(0);
            if (m < state_modes.length) return state_modes[m];
        }
        return StateMode.TARGET;
    }

    static public void setStateMode(ItemStack stack, StateMode mode) {
        if (WandUtils.is_wand(stack)) {
            CompoundTag tag = Compat.getTags(stack);
            tag.putInt("state_mode", mode.ordinal());
            CustomData.set(DataComponents.CUSTOM_DATA, stack, tag);
        }
    }

    static public void setGridVal(ItemStack stack, Value v, int n, int limit) {
        if (WandUtils.is_wand(stack)) {
            if (n > v.max) n = v.max;
            if (n < v.min) n = v.min;
            CompoundTag tag = Compat.getTags(stack);
            if (v.coval != null) {
                int c = tag.getInt(v.coval.toString()).orElse(v.coval.def);
                if (c * n > limit) return;  // Would exceed limit
            }
            tag.putInt(v.toString(), n);
            CustomData.set(DataComponents.CUSTOM_DATA, stack, tag);
        }
    }

    static public void incGrid(ItemStack stack, Value v, int inc, int limit) {
        int current = getVal(stack, v);
        setGridVal(stack, v, current + inc, limit);
    }

    static public void decGrid(ItemStack stack, Value v, int dec, int limit) {
        int current = getVal(stack, v);
        setGridVal(stack, v, current - dec, limit);
    }
}
