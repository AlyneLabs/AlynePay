import React from 'react';
import {
  Tabs,
  TabList,
  TabTrigger,
  TabSlot,
  TabTriggerSlotProps,
  TabListProps,
} from 'expo-router/ui';
import { Pressable, View, StyleSheet, Platform } from 'react-native';
import { MaterialIcons } from '@expo/vector-icons';

import { ThemedText } from './themed-text';
import { Colors, Fonts, MaxContentWidth, Radius, Spacing } from '@/constants/theme';
import { useTheme } from '@/hooks/use-theme';

export default function AppTabs() {
  return (
    <Tabs>
      <TabSlot style={{ height: '100%', width: '100%' }} />
      <TabList asChild>
        <CustomTabList>
          <TabTrigger name="index" href="/" asChild>
            <TabButton icon="payments" label="Pay" />
          </TabTrigger>
          <TabTrigger name="configure" href={'/configure' as any} asChild>
            <TabButton icon="tune" label="Configure" />
          </TabTrigger>
        </CustomTabList>
      </TabList>
    </Tabs>
  );
}

interface TabButtonProps extends TabTriggerSlotProps {
  icon: keyof typeof MaterialIcons.glyphMap;
  label: string;
}

export function TabButton({ icon, label, isFocused, ...props }: TabButtonProps) {
  const theme = useTheme();

  return (
    <Pressable
      {...props}
      style={({ pressed }) => [
        styles.tabButton,
        isFocused && styles.tabButtonActive,
        pressed && styles.pressed,
      ]}>
      <View
        style={[
          styles.iconContainer,
          isFocused && { backgroundColor: 'rgba(76, 215, 246, 0.14)' },
        ]}>
        <MaterialIcons
          name={icon}
          size={22}
          color={isFocused ? theme.tertiary : theme.textTertiary}
        />
      </View>
      <ThemedText
        type="labelMono"
        style={[
          styles.tabLabel,
          { color: isFocused ? theme.tertiary : theme.textTertiary },
        ]}>
        {label}
      </ThemedText>
    </Pressable>
  );
}

export function CustomTabList(props: TabListProps) {
  const theme = useTheme();

  return (
    <View {...props} style={styles.tabListContainer}>
      <View style={[styles.innerDock, { backgroundColor: 'rgba(20, 19, 19, 0.82)' }]}>
        <View style={styles.tabsRow}>
          {props.children}
        </View>
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  tabListContainer: {
    position: 'fixed' as any,
    bottom: 0,
    left: 0,
    right: 0,
    width: '100%',
    justifyContent: 'center',
    alignItems: 'center',
    zIndex: 9999,
    paddingBottom: Spacing.sm,
    paddingHorizontal: Spacing.sm,
    pointerEvents: 'box-none' as any,
  },
  innerDock: {
    width: '100%',
    maxWidth: 480,
    paddingVertical: 8,
    paddingHorizontal: 16,
    borderRadius: Radius.xl,
    borderWidth: 1,
    borderColor: 'rgba(255, 255, 255, 0.12)',
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 10 },
    shadowOpacity: 0.5,
    shadowRadius: 20,
    ...Platform.select({
      web: {
        backdropFilter: 'blur(24px)',
      } as any,
    }),
  },
  tabsRow: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-around',
    width: '100%',
  },
  tabButton: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
    paddingVertical: 4,
    borderRadius: Radius.md,
    gap: 4,
  },
  tabButtonActive: {
    transform: [{ scale: 1.02 }],
  },
  iconContainer: {
    width: 44,
    height: 30,
    borderRadius: Radius.md,
    alignItems: 'center',
    justifyContent: 'center',
  },
  tabLabel: {
    fontSize: 11,
    letterSpacing: 0.6,
  },
  pressed: {
    opacity: 0.7,
    transform: [{ scale: 0.95 }],
  },
});

