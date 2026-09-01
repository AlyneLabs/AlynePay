# Link SYSTM Button to Start Core

The goal is to enable the "SYSTM" button in the `HomeScreenContent` to trigger the `core.start()` method in `MainActivity`.

## Proposed Changes

### [app]

#### [MODIFY] [ToggleCard.kt](file:///C:/Users/Liquid_Ammo/AndroidStudioProjects/Systm/app/src/main/java/com/alynelabs/systm/ToggleCard.kt)
- Add `onClick: () -> Unit` parameter to `ToggleCard` composable.
- Wrap the `Box` content with `Modifier.clickable` or add it to the existing `modifier`.

#### [MODIFY] [MainActivity.kt](file:///C:/Users/Liquid_Ammo/AndroidStudioProjects/Systm/app/src/main/java/com/alynelabs/systm/MainActivity.kt)
- Update `MainScreen`, `HomeScreenContent` to accept `onSystmClick: () -> Unit`.
- Pass `core.start()` as the callback for `onSystmClick`.

## Verification Plan

### Manual Verification
- Deploy the app to a device.
- Tap the "SYSTM" button.
- Verify in Logcat that "Starting hardware modules..." is printed.
