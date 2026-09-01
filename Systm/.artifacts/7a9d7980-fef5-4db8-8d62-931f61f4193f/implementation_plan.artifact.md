# Implementation Plan - Figma Design Implementation

Implement the "SYSTM" home screen design from Figma using the provided `GlassUi.kt` utility. The implementation will focus on creating a high-fidelity replica of the design while adhering to the user's request to exclude drop shadows.

## User Review Required

> [!IMPORTANT]
> The design will be implemented using standard Jetpack Compose and the existing `GlassUi.kt` utility. Drop shadows will be excluded as requested, but inner shadows (which contribute to the "glass" look) will be kept unless otherwise specified, as they are part of the `glassmorphism` effect.

## Proposed Changes

### UI Components

#### [NEW] [HomeScreen.kt](file:///C:/Users/Liquid_Ammo/AndroidStudioProjects/Systm/app/src/main/java/com/alynelabs/systm/HomeScreen.kt)
Create a new file to house the dashboard UI.
- Implement `HomeScreen` composable as the main container.
- Implement sub-composables for each section:
    - `TopBar`: Logo pill and Status concentric circles.
    - `HeaderSection`: "Liquid Ammo" title.
    - `QuickActions`: "SYSTM" and "MESH" pills.
    - `ControlGrids`: Two panels containing "Internet Routing", "Wi-Fi Tunnels", "Energy Saver", and "Limit Devices".
    - `NodesPanel`: "CONNECTED NODES" card with stats and a simplified graph visualization.
    - `BottomNav`: The glass navigation bar.

#### [MODIFY] [GlassUi.kt](file:///C:/Users/Liquid_Ammo/AndroidStudioProjects/Systm/app/src/main/java/com/alynelabs/systm/GlassUi.kt)
Update the file to provide a more flexible `GlassContainer` that doesn't hardcode shadows, making it easier to use across the app.
- Add `Modifier.glassContainer` or a similar wrapper that defaults to no shadows.

#### [MODIFY] [MainActivity.kt](file:///C:/Users/Liquid_Ammo/AndroidStudioProjects/Systm/app/src/main/java/com/alynelabs/systm/MainActivity.kt)
- Update `setContent` to display the `HomeScreen`.

## Verification Plan

### Automated Tests
- Run `render_compose_preview` on the `HomeScreen` to verify the visual layout matches the Figma design.

### Manual Verification
- Deploy to the device/emulator to check responsiveness and overall look.
