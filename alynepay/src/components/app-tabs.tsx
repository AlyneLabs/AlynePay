import React from 'react';
import { Tabs } from 'expo-router';
import { Platform, StyleSheet, View } from 'react-native';
import { MaterialIcons } from '@expo/vector-icons';
import { BlurView } from 'expo-blur';

import { Radius, Spacing } from '@/constants/theme';
import { useTheme } from '@/hooks/use-theme';

export default function AppTabs() {
  const theme = useTheme();

  return (
    <Tabs
      screenOptions={{
        headerShown: false,
        tabBarActiveTintColor: theme.tertiary,
        tabBarInactiveTintColor: theme.textTertiary,
        tabBarStyle: styles.tabBar,
        tabBarItemStyle: styles.tabBarItem,
        tabBarLabelStyle: styles.tabBarLabel,
        tabBarBackground: () =>
          Platform.OS === 'ios' ? (
            <BlurView
              intensity={40}
              tint="dark"
              style={[StyleSheet.absoluteFill, styles.blurContainer]}
            />
          ) : (
            <View style={[StyleSheet.absoluteFill, styles.androidBackground]} />
          ),
      }}>
      <Tabs.Screen
        name="index"
        options={{
          title: 'Pay',
          tabBarIcon: ({ color, focused }) => (
            <View style={[styles.iconWrapper, focused && styles.activeIconWrapper]}>
              <MaterialIcons name="payments" size={24} color={color} />
            </View>
          ),
        }}
      />
      <Tabs.Screen
        name="configure"
        options={{
          title: 'Configure',
          tabBarIcon: ({ color, focused }) => (
            <View style={[styles.iconWrapper, focused && styles.activeIconWrapper]}>
              <MaterialIcons name="tune" size={24} color={color} />
            </View>
          ),
        }}
      />
      <Tabs.Screen
        name="send"
        options={{
          href: null,
          tabBarStyle: { display: 'none' },
        }}
      />
      <Tabs.Screen
        name="deposit"
        options={{
          href: null,
          tabBarStyle: { display: 'none' },
        }}
      />
    </Tabs>
  );
}

const styles = StyleSheet.create({
  tabBar: {
    position: 'absolute',
    bottom: 0,
    left: 0,
    right: 0,
    height: Platform.select({ ios: 78, default: 68 }),
    paddingBottom: Platform.select({ ios: 20, default: 10 }),
    paddingTop: 8,
    backgroundColor: Platform.select({
      ios: 'rgba(20, 19, 19, 0.70)',
      default: 'rgba(20, 19, 19, 0.94)',
    }),
    borderTopWidth: 1,
    borderTopColor: 'rgba(255, 255, 255, 0.10)',
    borderTopLeftRadius: Radius.lg,
    borderTopRightRadius: Radius.lg,
    elevation: 20,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: -4 },
    shadowOpacity: 0.35,
    shadowRadius: 16,
  },
  tabBarItem: {
    paddingVertical: 2,
  },
  tabBarLabel: {
    fontSize: 11,
    fontWeight: '600',
    letterSpacing: 0.6,
    textTransform: 'uppercase',
    marginTop: 2,
  },
  iconWrapper: {
    alignItems: 'center',
    justifyContent: 'center',
    width: 36,
    height: 32,
    borderRadius: Radius.md,
  },
  activeIconWrapper: {
    backgroundColor: 'rgba(76, 215, 246, 0.12)',
  },
  blurContainer: {
    borderTopLeftRadius: Radius.lg,
    borderTopRightRadius: Radius.lg,
    overflow: 'hidden',
  },
  androidBackground: {
    backgroundColor: 'rgba(20, 19, 19, 0.95)',
    borderTopLeftRadius: Radius.lg,
    borderTopRightRadius: Radius.lg,
  },
});
