import React from 'react';
import { StyleSheet, View, ViewProps, Platform } from 'react-native';
import { BlurView } from 'expo-blur';
import { Radius } from '@/constants/theme';

export interface GlassViewProps extends ViewProps {
  intensity?: number;
  tint?: 'dark' | 'light' | 'extraLight' | 'regular' | 'prominent';
  variant?: 'elevated' | 'subtle' | 'outline';
  borderRadius?: number;
}

export function GlassView({
  children,
  style,
  intensity = 24,
  tint = 'dark',
  variant = 'elevated',
  borderRadius = Radius.lg,
  ...props
}: GlassViewProps) {
  const variantStyle =
    variant === 'elevated'
      ? styles.elevated
      : variant === 'subtle'
      ? styles.subtle
      : styles.outline;

  if (Platform.OS === 'web') {
    return (
      <View
        style={[
          styles.webGlass,
          variantStyle,
          { borderRadius },
          style,
        ]}
        {...props}>
        {children}
      </View>
    );
  }

  return (
    <View style={[styles.container, { borderRadius }, style]} {...props}>
      <BlurView
        intensity={intensity}
        tint={tint}
        style={[StyleSheet.absoluteFill, { borderRadius }]}
      />
      <View style={[variantStyle, { borderRadius, flex: 1 }]}>
        {children}
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    overflow: 'hidden',
    position: 'relative',
  },
  webGlass: {
    backgroundColor: 'rgba(255, 255, 255, 0.04)',
    borderColor: 'rgba(255, 255, 255, 0.12)',
    borderWidth: 1,
    ...Platform.select({
      web: {
        backdropFilter: 'blur(24px)',
      } as any,
    }),
  },
  elevated: {
    backgroundColor: 'rgba(255, 255, 255, 0.05)',
    borderColor: 'rgba(255, 255, 255, 0.14)',
    borderWidth: 1,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 8 },
    shadowOpacity: 0.35,
    shadowRadius: 16,
    elevation: 8,
  },
  subtle: {
    backgroundColor: 'rgba(255, 255, 255, 0.02)',
    borderColor: 'rgba(255, 255, 255, 0.08)',
    borderWidth: 1,
  },
  outline: {
    backgroundColor: 'transparent',
    borderColor: 'rgba(255, 255, 255, 0.12)',
    borderWidth: 1,
  },
});

