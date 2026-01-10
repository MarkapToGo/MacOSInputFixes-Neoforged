# MacOS Input Fixes - NeoForge Edition

A NeoForge port of [hamarb123's MCMacOSInputFixes](https://github.com/hamarb123/MCMacOSInputFixes) mod.

## Authors

| Author | Contribution |
|--------|--------------|
| **hamarb123** | Original Fabric mod, native macOS code, core fix implementations |
| **Markap** | NeoForge 1.21.1 port, simplified codebase for single-version support |

## What This Mod Fixes

- **MC-122296**: Ctrl+Left Click becomes Right Click on macOS
- **MC-121772**: Shift+scroll doesn't work correctly  
- **MC-59810**: Trackpad scrolling is way too sensitive
- **MC-22882**: Momentum scrolling issues
- Control+Tab and Control+Escape not being detected

## Requirements

- Minecraft 1.21.1
- NeoForge 21.1+
- macOS (the mod loads safely on other platforms but only applies fixes on macOS)

## Building

### Prerequisites: XCode Command Line Tools

**Why are XCode tools required?**

This mod uses **native macOS code** (Objective-C++) to intercept and fix input events at the operating system level. The native library (`macos_input_fixes.dylib`) hooks into macOS's Cocoa framework to:

1. **Intercept scroll events** before they reach GLFW/Minecraft, allowing us to normalize trackpad sensitivity and filter momentum scrolling
2. **Detect Control key state** separately from Command key, since macOS treats Ctrl+Click as right-click by default
3. **Capture special key combinations** (Ctrl+Tab, Ctrl+Escape) that GLFW doesn't properly detect on macOS

This native code must be compiled with Apple's Clang compiler, which is only available through XCode or its command line tools.

### Install XCode Command Line Tools

```bash
xcode-select --install
```

### Build the Native Library

```bash
cd src/main/native
make clean && make
```

This creates a universal binary supporting both Intel (x86_64) and Apple Silicon (arm64) Macs.

### Build the Mod

```bash
./gradlew build
```

Output: `build/libs/macos_input_fixes-1.0.0.jar`

## Testing

```bash
./gradlew runClient
```

Then go to Options → Controls → Mouse Settings to see the mod options.

## License

BSD-3-Clause (same as original)
