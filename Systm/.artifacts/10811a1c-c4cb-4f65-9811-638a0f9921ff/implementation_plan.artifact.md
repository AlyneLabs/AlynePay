# Refactor and Position LogoCard

Refactor the `LogoCard` component to separate its glass styling into `GlassUi.kt` and position it correctly in the main screen according to the Figma design.

## User Review Required

> [!IMPORTANT]
> - `LogoCard.kt` will no longer contain glassmorphism logic. It will only contain the logo and text components.
> - `GlassUi.kt` will be updated to provide a reusable glass container that matches the Figma spec.
> - The card will be positioned at `(35dp, 30dp)` in `MainActivity.kt`.

## Proposed Changes

### UI Components

#### [MODIFY] [GlassUi.kt](file:///C:/Users/Liquid_Ammo/AndroidStudioProjects/Systm/app/src/main/java/com/alynelabs/systm/GlassUi.kt)
- Create or update a glass container (e.g., `SystmLogoCardContainer`) that accepts `content: @Composable () -> Unit`.
- Move the exact `ShadowParams`, `backgroundColor`, and `outlineBrush` from the current `LogoCard.kt` into this component to ensure the glass design is centralized.

#### [MODIFY] [LogoCard.kt](file:///C:/Users/Liquid_Ammo/AndroidStudioProjects/Systm/app/src/main/java/com/alynelabs/systm/LogoCard.kt)
- Remove all `glassmorphism` modifier calls and styling parameters.
- Wrap the logo (Canvas squares) and text in the glass container from `GlassUi.kt`.
- Keep the component minimal, focusing only on the "Logo and Text" details.

#### [MODIFY] [MainActivity.kt](file:///C:/Users/Liquid_Ammo/AndroidStudioProjects/Systm/app/src/main/java/com/alynelabs/systm/MainActivity.kt)
- Update the layout to position `LogoCard` at `x: 35dp, y: 30dp` using `Modifier.offset(x = 35.dp, y = 30.dp)` or similar within the root `Box`.

## Verification Plan

### Manual Verification
- Render the `LogoCardPreview` to ensure the visual design remains unchanged despite the refactoring.
- Deploy the app to verify the new positioning in `MainActivity`.
