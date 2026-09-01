# Implementation Plan: Fix and Execute 3-Node Hop Test

This plan fixes the bearer mismatch and multiple trigger issues in `MeshManager` to ensure the 3-node hop test executes successfully across three connected devices.

## User Review Required

> [!IMPORTANT]
> The test is hardcoded to trigger when Phone A (Node ID: -2472472724180024548) sees Phone B (Node ID: -8824097300144855975). Ensure these devices are connected.

## Proposed Changes

### Mesh & Routing

#### [MODIFY] [MeshManager.kt](file:///C:/Users/Liquid_Ammo/AndroidStudioProjects/Systm/app/src/main/java/com/alynelabs/systm/mesh/MeshManager.kt)

- Add `is3NodeTestTriggered` flag to prevent duplicate test triggers.
- Update `handleIncomingRaw` to accept an explicit `bearerType` argument so hardware mapping correctly identifies if a node is reachable via BLE, WiFi, or IP.
- Update `broadcastLSA` to use the bearer type of the radio it's being broadcast from (or a generic one if broadcasting to all).
- Fix the logic where `InternetModule` was being used for MAC addresses.

## Verification Plan

### Automated Tests
- N/A (Functional multi-device test)

### Manual Verification
- Deploy updated app to all 3 connected devices:
    - HEZXKBVWHQHYWKPJ (Phone B)
    - R5CW11421XK (Phone A)
    - RZCY225PW5T (Phone C)
- Monitor Logcat for `--- Starting Real 3-Node Hop Test ---` and `[CONSUME] Data from ...: REAL_3_NODE_HOP_SUCCESS`.
