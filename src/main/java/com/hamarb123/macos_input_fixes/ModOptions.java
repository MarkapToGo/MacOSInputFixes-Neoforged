package com.hamarb123.macos_input_fixes;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.List;
import org.apache.commons.io.IOUtils;

import com.google.common.base.Splitter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.neoforged.fml.loading.FMLPaths;

/**
 * Mod options management - simplified for 1.21.1 only (no multi-version
 * reflection).
 */
public class ModOptions {

    private static final Splitter COLON_SPLITTER = Splitter.on(':');

    // Option values
    public static double trackpadSensitivity = 20.0;
    public static boolean reverseHotbarScrolling = false;
    public static boolean reverseScrolling = false;
    public static boolean momentumScrolling = false;
    public static boolean interfaceSmoothScroll = false;
    public static boolean disableCtrlClickFix = false;
    public static boolean useCommandKey = false; // false = use Ctrl (STRG), true = use Command (⌘)
    public static boolean blockCommandQQuit = true; // true = block Command+Q from quitting game

    // Option UI instances
    public static OptionInstance<Double> TRACKPAD_SENSITIVITY;
    public static OptionInstance<Boolean> REVERSE_HOTBAR_SCROLLING;
    public static OptionInstance<Boolean> REVERSE_SCROLLING;
    public static OptionInstance<Boolean> MOMENTUM_SCROLLING;
    public static OptionInstance<Boolean> INTERFACE_SMOOTH_SCROLL;
    public static OptionInstance<Boolean> DISABLE_CTRL_CLICK_FIX;
    public static OptionInstance<Boolean> USE_COMMAND_KEY;
    public static OptionInstance<Boolean> BLOCK_COMMAND_Q_QUIT;

    private static boolean loadedInterface = false;
    public static Path optionsFile;

    /**
     * Get the list of options to show in the settings menu.
     */
    public static OptionInstance<?>[] getModOptions() {
        loadInterface();
        if (Common.IS_SYSTEM_MAC) {
            return new OptionInstance<?>[] {
                    REVERSE_SCROLLING,
                    REVERSE_HOTBAR_SCROLLING,
                    TRACKPAD_SENSITIVITY,
                    MOMENTUM_SCROLLING,
                    INTERFACE_SMOOTH_SCROLL,
                    DISABLE_CTRL_CLICK_FIX,
                    USE_COMMAND_KEY,
                    BLOCK_COMMAND_Q_QUIT
            };
        } else {
            return new OptionInstance<?>[] {
                    REVERSE_SCROLLING,
                    REVERSE_HOTBAR_SCROLLING
            };
        }
    }

    private static void loadInterface() {
        if (loadedInterface)
            return;
        try {
            if (Common.IS_SYSTEM_MAC) {
                TRACKPAD_SENSITIVITY = new OptionInstance<>(
                        "options.macos_input_fixes.trackpad_sensitivity",
                        OptionInstance.cachedConstantTooltip(Component.literal(
                                "The grouping feature only affects hotbar scrolling.\n" +
                                        "This feature only affects scrolling from the trackpad.\n" +
                                        "Default: 20.0\n" +
                                        "0.0: Disable custom trackpad scroll processing.")),
                        (text, value) -> Component.literal("Trackpad Sensitivity: " + Math.round(value * 100.0)),
                        OptionInstance.UnitDouble.INSTANCE,
                        trackpadSensitivity / 100.0,
                        value -> {
                            setTrackpadSensitivity(value * 100.0);
                            saveOptions();
                        });

                MOMENTUM_SCROLLING = OptionInstance.createBoolean(
                        "options.macos_input_fixes.momentum_scrolling",
                        OptionInstance.cachedConstantTooltip(Component.literal(
                                "Only affects hotbar scrolling.\n" +
                                        "Default: OFF\n" +
                                        "OFF: ignore 'momentum scroll' events.")),
                        momentumScrolling,
                        value -> {
                            setMomentumScrolling(value);
                            saveOptions();
                        });

                INTERFACE_SMOOTH_SCROLL = OptionInstance.createBoolean(
                        "options.macos_input_fixes.smooth_scroll",
                        OptionInstance.cachedConstantTooltip(Component.literal(
                                "Affects all scrolling from legacy input devices.\n" +
                                        "Default: OFF")),
                        interfaceSmoothScroll,
                        value -> {
                            setInterfaceSmoothScroll(value);
                            saveOptions();
                        });

                DISABLE_CTRL_CLICK_FIX = OptionInstance.createBoolean(
                        "options.macos_input_fixes.disable_ctrl_click_fix",
                        OptionInstance.cachedConstantTooltip(Component.literal(
                                "When enabled, disables the fix for the bug which causes\n" +
                                        "Minecraft to map Control + Left Click to Right Click.")),
                        disableCtrlClickFix,
                        value -> {
                            disableCtrlClickFix = value;
                            saveOptions();
                        });

                USE_COMMAND_KEY = OptionInstance.createBoolean(
                        "options.macos_input_fixes.use_command_key",
                        OptionInstance.cachedConstantTooltip(Component.literal(
                                "Choose which key to use as the modifier for drop stack/Ctrl+Click.\n" +
                                        "OFF = Use Ctrl key (STRG on German keyboards)\n" +
                                        "ON = Use Command key (⌘)")),
                        useCommandKey,
                        value -> {
                            useCommandKey = value;
                            saveOptions();
                        });

                BLOCK_COMMAND_Q_QUIT = OptionInstance.createBoolean(
                        "options.macos_input_fixes.block_command_q",
                        OptionInstance.cachedConstantTooltip(Component.literal(
                                "Prevents Command+Q from quitting Minecraft.\n" +
                                        "ON (default) = Command+Q is blocked\n" +
                                        "OFF = Command+Q quits the game as normal")),
                        blockCommandQQuit,
                        value -> {
                            blockCommandQQuit = value;
                            saveOptions();
                        });
            }

            REVERSE_HOTBAR_SCROLLING = OptionInstance.createBoolean(
                    "options.macos_input_fixes.reverse_hotbar_scrolling",
                    OptionInstance.cachedConstantTooltip(Component.literal(
                            "Reverses the direction that scrolling goes for the hotbar.")),
                    reverseHotbarScrolling,
                    value -> {
                        reverseHotbarScrolling = value;
                        saveOptions();
                    });

            REVERSE_SCROLLING = OptionInstance.createBoolean(
                    "options.macos_input_fixes.reverse_scrolling",
                    OptionInstance.cachedConstantTooltip(Component.literal(
                            "Reverses the direction of all scrolling when enabled.")),
                    reverseScrolling,
                    value -> {
                        reverseScrolling = value;
                        saveOptions();
                    });

            loadedInterface = true;
        } catch (Throwable t) {
            throw new RuntimeException("Failed to initialise option interface elements.", t);
        }
    }

    public static void loadOptions() {
        // Locate options file in config directory
        optionsFile = FMLPaths.CONFIGDIR.get().resolve("macos_input_fixes.txt");

        // Check if we need to migrate from old path
        if (!Files.exists(optionsFile)) {
            Path oldFile = Minecraft.getInstance().gameDirectory.toPath().resolve("options_macos_input_fixes.txt");
            if (Files.exists(oldFile)) {
                try {
                    Files.createDirectories(optionsFile.getParent());
                    Files.copy(oldFile, optionsFile);
                    Files.deleteIfExists(oldFile);
                } catch (IOException e) {
                    throw new RuntimeException("Failed to migrate old macos input fixes options file", e);
                }
            }
        }

        try {
            if (!Files.exists(optionsFile)) {
                return;
            }
            List<String> lines = IOUtils.readLines(Files.newInputStream(optionsFile), StandardCharsets.UTF_8);
            CompoundTag compoundTag = new CompoundTag();
            for (String line : lines) {
                try {
                    Iterator<String> iterator = COLON_SPLITTER.omitEmptyStrings().limit(2).split(line).iterator();
                    compoundTag.putString(iterator.next(), iterator.next());
                } catch (Exception ex1) {
                    ex1.printStackTrace(System.err);
                }
            }

            if (compoundTag.contains("trackpadSensitivity")) {
                try {
                    setTrackpadSensitivity(Double.parseDouble(compoundTag.getString("trackpadSensitivity")));
                } catch (Exception ex) {
                    ex.printStackTrace(System.err);
                }
            }
            if (compoundTag.contains("reverseHotbarScrolling")) {
                reverseHotbarScrolling = Boolean.parseBoolean(compoundTag.getString("reverseHotbarScrolling"));
            }
            if (compoundTag.contains("reverseScrolling")) {
                reverseScrolling = Boolean.parseBoolean(compoundTag.getString("reverseScrolling"));
            }
            if (compoundTag.contains("momentumScrolling")) {
                setMomentumScrolling(Boolean.parseBoolean(compoundTag.getString("momentumScrolling")));
            }
            if (compoundTag.contains("interfaceSmoothScroll")) {
                setInterfaceSmoothScroll(Boolean.parseBoolean(compoundTag.getString("interfaceSmoothScroll")));
            }
            if (compoundTag.contains("disableCtrlClickFix")) {
                disableCtrlClickFix = Boolean.parseBoolean(compoundTag.getString("disableCtrlClickFix"));
            }
            if (compoundTag.contains("useCommandKey")) {
                useCommandKey = Boolean.parseBoolean(compoundTag.getString("useCommandKey"));
            }
            if (compoundTag.contains("blockCommandQQuit")) {
                blockCommandQQuit = Boolean.parseBoolean(compoundTag.getString("blockCommandQQuit"));
            }

            loadedInterface = false;
        } catch (Exception ex2) {
            ex2.printStackTrace(System.err);
        }
    }

    public static void saveOptions() {
        try (PrintWriter printWriter = new PrintWriter(
                new OutputStreamWriter(Files.newOutputStream(optionsFile), StandardCharsets.UTF_8))) {
            printWriter.println("trackpadSensitivity:" + trackpadSensitivity);
            printWriter.println("reverseHotbarScrolling:" + reverseHotbarScrolling);
            printWriter.println("reverseScrolling:" + reverseScrolling);
            printWriter.println("momentumScrolling:" + momentumScrolling);
            printWriter.println("interfaceSmoothScroll:" + interfaceSmoothScroll);
            printWriter.println("disableCtrlClickFix:" + disableCtrlClickFix);
            printWriter.println("useCommandKey:" + useCommandKey);
            printWriter.println("blockCommandQQuit:" + blockCommandQQuit);
        } catch (Exception ex2) {
            ex2.printStackTrace(System.err);
        }
    }

    public static void setTrackpadSensitivity(double value) {
        trackpadSensitivity = value;
        if (!Common.IS_SYSTEM_MAC)
            return;

        if (value < 0)
            value = 0.0;
        else if (value > 100.0)
            value = 100.0;
        MacOSInputFixesMod.setTrackpadSensitivity(value);
    }

    public static void setMomentumScrolling(boolean value) {
        momentumScrolling = value;
        if (!Common.IS_SYSTEM_MAC)
            return;
        MacOSInputFixesMod.setMomentumScrolling(value);
    }

    public static void setInterfaceSmoothScroll(boolean value) {
        interfaceSmoothScroll = value;
        if (!Common.IS_SYSTEM_MAC)
            return;
        MacOSInputFixesMod.setInterfaceSmoothScroll(value);
    }
}
