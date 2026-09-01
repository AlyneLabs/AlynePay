import React, { useState } from 'react';
import {
  ScrollView,
  StyleSheet,
  View,
  Switch,
  Pressable,
  Platform,
} from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { MaterialIcons } from '@expo/vector-icons';

import { ThemedText } from '@/components/themed-text';
import { GlassView } from '@/components/glass-view';
import { Colors, Spacing, Radius, BottomTabInset } from '@/constants/theme';
import { useTheme } from '@/hooks/use-theme';

export default function ConfigureScreen() {
  const theme = useTheme();
  const [offlineMeshEnabled, setOfflineMeshEnabled] = useState(true);
  const [bleBroadcasting, setBleBroadcasting] = useState(true);
  const [autoSyncOnRelay, setAutoSyncOnRelay] = useState(true);
  const [soundHapticFeedback, setSoundHapticFeedback] = useState(true);

  return (
    <View style={[styles.container, { backgroundColor: theme.background }]}>
      <SafeAreaView style={styles.safeArea} edges={['top', 'left', 'right']}>
        <ScrollView
          showsVerticalScrollIndicator={false}
          contentContainerStyle={styles.scrollContent}>
          {/* Header */}
          <View style={styles.header}>
            <View>
              <ThemedText type="labelMono" style={{ color: theme.tertiary }}>
                NODE SETTINGS
              </ThemedText>
              <ThemedText type="headlineLg" style={styles.headerTitle}>
                Configure
              </ThemedText>
            </View>
            <View style={[styles.statusBadge, { borderColor: theme.tertiary }]}>
              <View style={[styles.statusDot, { backgroundColor: theme.tertiary }]} />
              <ThemedText type="labelMono" style={{ color: theme.tertiary }}>
                OFFLINE ACTIVE
              </ThemedText>
            </View>
          </View>

          {/* Node Identity Card */}
          <GlassView style={styles.card} variant="elevated">
            <ThemedText type="labelMono" themeColor="textSecondary" style={styles.sectionTitle}>
              Mesh Node Identity
            </ThemedText>

            <View style={styles.rowBetween}>
              <ThemedText type="small" themeColor="textSecondary">
                Node ID
              </ThemedText>
              <ThemedText type="code" themeColor="tertiary">
                NODE-7X99-ALYN
              </ThemedText>
            </View>

            <View style={styles.divider} />

            <View style={styles.rowBetween}>
              <ThemedText type="small" themeColor="textSecondary">
                Ledger Nonce
              </ThemedText>
              <ThemedText type="code" themeColor="text">
                #48,921
              </ThemedText>
            </View>

            <View style={styles.divider} />

            <View style={styles.rowBetween}>
              <ThemedText type="small" themeColor="textSecondary">
                Protocol Version
              </ThemedText>
              <ThemedText type="code" themeColor="text">
                Alyne-Mesh v2.4
              </ThemedText>
            </View>
          </GlassView>

          {/* Offline Mesh Network */}
          <GlassView style={styles.card} variant="elevated">
            <ThemedText type="labelMono" themeColor="textSecondary" style={styles.sectionTitle}>
              Offline Transmission
            </ThemedText>

            <View style={styles.settingRow}>
              <View style={styles.settingTextContainer}>
                <ThemedText type="default" style={styles.settingLabel}>
                  Peer-to-Peer Mesh
                </ThemedText>
                <ThemedText type="small" themeColor="textSecondary">
                  Exchange signed tokens offline via local radio mesh
                </ThemedText>
              </View>
              <Switch
                value={offlineMeshEnabled}
                onValueChange={setOfflineMeshEnabled}
                trackColor={{ false: theme.surfaceContainerHighest, true: theme.tertiaryContainer }}
                thumbColor={offlineMeshEnabled ? theme.tertiary : theme.textTertiary}
              />
            </View>

            <View style={styles.divider} />

            <View style={styles.settingRow}>
              <View style={styles.settingTextContainer}>
                <ThemedText type="default" style={styles.settingLabel}>
                  BLE Proximity Beacon
                </ThemedText>
                <ThemedText type="small" themeColor="textSecondary">
                  Broadcast encrypted offline invoice signals
                </ThemedText>
              </View>
              <Switch
                value={bleBroadcasting}
                onValueChange={setBleBroadcasting}
                trackColor={{ false: theme.surfaceContainerHighest, true: theme.tertiaryContainer }}
                thumbColor={bleBroadcasting ? theme.tertiary : theme.textTertiary}
              />
            </View>

            <View style={styles.divider} />

            <View style={styles.settingRow}>
              <View style={styles.settingTextContainer}>
                <ThemedText type="default" style={styles.settingLabel}>
                  Auto-Sync on Gateway Contact
                </ThemedText>
                <ThemedText type="small" themeColor="textSecondary">
                  Reconcile offline transactions when a validator is within reach
                </ThemedText>
              </View>
              <Switch
                value={autoSyncOnRelay}
                onValueChange={setAutoSyncOnRelay}
                trackColor={{ false: theme.surfaceContainerHighest, true: theme.tertiaryContainer }}
                thumbColor={autoSyncOnRelay ? theme.tertiary : theme.textTertiary}
              />
            </View>
          </GlassView>

          {/* Security & Cryptography */}
          <GlassView style={styles.card} variant="elevated">
            <ThemedText type="labelMono" themeColor="textSecondary" style={styles.sectionTitle}>
              Security & Cryptography
            </ThemedText>

            <Pressable style={({ pressed }) => [styles.actionRow, pressed && styles.pressed]}>
              <View style={styles.actionLeft}>
                <MaterialIcons name="vpn-key" size={20} color={theme.tertiary} />
                <ThemedText type="default">Backup Cryptographic Seed</ThemedText>
              </View>
              <MaterialIcons name="chevron-right" size={22} color={theme.textTertiary} />
            </Pressable>

            <View style={styles.divider} />

            <Pressable style={({ pressed }) => [styles.actionRow, pressed && styles.pressed]}>
              <View style={styles.actionLeft}>
                <MaterialIcons name="fingerprint" size={20} color={theme.secondary} />
                <ThemedText type="default">Biometric Authorization</ThemedText>
              </View>
              <MaterialIcons name="chevron-right" size={22} color={theme.textTertiary} />
            </Pressable>
          </GlassView>
        </ScrollView>
      </SafeAreaView>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
  },
  safeArea: {
    flex: 1,
  },
  scrollContent: {
    paddingHorizontal: Spacing.marginMobile,
    paddingTop: Spacing.md,
    paddingBottom: BottomTabInset + Spacing.lg,
    maxWidth: 720,
    width: '100%',
    alignSelf: 'center',
    gap: Spacing.md,
  },
  header: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'flex-start',
    marginBottom: Spacing.xs,
  },
  headerTitle: {
    marginTop: Spacing.base,
  },
  statusBadge: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: Spacing.xs,
    paddingHorizontal: Spacing.sm,
    paddingVertical: Spacing.base,
    borderRadius: Radius.full,
    borderWidth: 1,
    backgroundColor: 'rgba(76, 215, 246, 0.08)',
  },
  statusDot: {
    width: 6,
    height: 6,
    borderRadius: 3,
  },
  card: {
    padding: Spacing.md,
    borderRadius: Radius.lg,
  },
  sectionTitle: {
    marginBottom: Spacing.sm,
  },
  rowBetween: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    paddingVertical: Spacing.xs,
  },
  settingRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    paddingVertical: Spacing.xs,
  },
  settingTextContainer: {
    flex: 1,
    paddingRight: Spacing.sm,
  },
  settingLabel: {
    fontWeight: '500',
    marginBottom: 2,
  },
  actionRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    paddingVertical: Spacing.xs,
  },
  actionLeft: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: Spacing.sm,
  },
  divider: {
    height: 1,
    backgroundColor: 'rgba(255, 255, 255, 0.06)',
    marginVertical: Spacing.xs,
  },
  pressed: {
    opacity: 0.7,
  },
});
