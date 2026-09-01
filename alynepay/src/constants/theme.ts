/**
 * AlynePay Theme System
 * Based on the Onyx Black & Cosmic Glassmorphism design specifications.
 */

import '@/global.css';

import { Platform } from 'react-native';

export const Colors = {
  // AlynePay is an exclusively dark mode environment
  dark: {
    // Canvas & Surfaces
    background: '#0F0F0F',
    surface: '#141313',
    surfaceDim: '#141313',
    surfaceBright: '#3A3939',
    surfaceContainerLowest: '#0E0E0E',
    surfaceContainerLow: '#1C1B1B',
    surfaceContainer: '#201F1F',
    surfaceContainerHigh: '#2B2A2A',
    surfaceContainerHighest: '#353434',
    surfaceVariant: '#353434',

    // Text & Typography
    text: '#E5E2E1',
    textSecondary: '#C4C7C7',
    textTertiary: '#8E9192',
    onSurface: '#E5E2E1',
    onSurfaceVariant: '#C4C7C7',
    onBackground: '#E5E2E1',

    // Accents & Brand Colors
    primary: '#C8C6C5',
    onPrimary: '#313030',
    primaryContainer: '#0F0F0F',
    onPrimaryContainer: '#7D7B7B',

    secondary: '#D2BBFF',
    secondaryPurple: '#7C3AED',
    onSecondary: '#3F008E',
    secondaryContainer: '#6001D1',
    onSecondaryContainer: '#C9AEFF',

    tertiary: '#4CD7F6',
    onTertiary: '#003640',
    tertiaryContainer: '#001217',
    onTertiaryContainer: '#00889F',

    // Status & Utility
    error: '#FFB4AB',
    onError: '#690005',
    errorContainer: '#93000A',
    success: '#34D399',
    warning: '#FBBF24',

    // Borders & Glass
    outline: '#8E9192',
    outlineVariant: '#444748',
    glassBackground: 'rgba(255, 255, 255, 0.05)',
    glassBorder: 'rgba(255, 255, 255, 0.12)',
    glassBorderHighlight: 'rgba(255, 255, 255, 0.20)',

    // Legacy aliases for template compatibility
    backgroundElement: '#201F1F',
    backgroundSelected: '#2B2A2A',
  },
  light: {
    // Fallback mirroring dark mode as AlynePay operates in dark mode
    background: '#0F0F0F',
    surface: '#141313',
    surfaceDim: '#141313',
    surfaceBright: '#3A3939',
    surfaceContainerLowest: '#0E0E0E',
    surfaceContainerLow: '#1C1B1B',
    surfaceContainer: '#201F1F',
    surfaceContainerHigh: '#2B2A2A',
    surfaceContainerHighest: '#353434',
    surfaceVariant: '#353434',

    text: '#E5E2E1',
    textSecondary: '#C4C7C7',
    textTertiary: '#8E9192',
    onSurface: '#E5E2E1',
    onSurfaceVariant: '#C4C7C7',
    onBackground: '#E5E2E1',

    primary: '#C8C6C5',
    onPrimary: '#313030',
    primaryContainer: '#0F0F0F',
    onPrimaryContainer: '#7D7B7B',

    secondary: '#D2BBFF',
    secondaryPurple: '#7C3AED',
    onSecondary: '#3F008E',
    secondaryContainer: '#6001D1',
    onSecondaryContainer: '#C9AEFF',

    tertiary: '#4CD7F6',
    onTertiary: '#003640',
    tertiaryContainer: '#001217',
    onTertiaryContainer: '#00889F',

    error: '#FFB4AB',
    onError: '#690005',
    errorContainer: '#93000A',
    success: '#34D399',
    warning: '#FBBF24',

    outline: '#8E9192',
    outlineVariant: '#444748',
    glassBackground: 'rgba(255, 255, 255, 0.05)',
    glassBorder: 'rgba(255, 255, 255, 0.12)',
    glassBorderHighlight: 'rgba(255, 255, 255, 0.20)',

    backgroundElement: '#201F1F',
    backgroundSelected: '#2B2A2A',
  },
} as const;

export type ThemeColor = keyof typeof Colors.dark;

export const Gradients = {
  galaxy: ['#7C3AED', '#06B6D4', '#EC4899'] as const,
  cardBackground: ['#0F111A', '#1A0F2E'] as const,
  actionButton: ['#7C3AED', '#6001D1'] as const,
  tertiaryGlow: ['rgba(76, 215, 246, 0.2)', 'rgba(76, 215, 246, 0)'] as const,
};

export const Fonts = Platform.select({
  ios: {
    sans: 'Geist, -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif',
    body: 'Inter, -apple-system, BlinkMacSystemFont, sans-serif',
    mono: 'JetBrains Mono, Menlo, Courier, monospace',
    rounded: 'ui-rounded',
  },
  android: {
    sans: 'sans-serif',
    body: 'sans-serif',
    mono: 'monospace',
    rounded: 'sans-serif-rounded',
  },
  default: {
    sans: 'Geist, Inter, sans-serif',
    body: 'Inter, sans-serif',
    mono: 'JetBrains Mono, monospace',
    rounded: 'normal',
  },
  web: {
    sans: 'Geist, -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif',
    body: 'Inter, -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif',
    mono: 'JetBrains Mono, Menlo, Monaco, Consolas, monospace',
    rounded: 'var(--font-rounded)',
  },
});

export const Spacing = {
  base: 4,
  xs: 8,
  sm: 16,
  md: 24,
  lg: 40,
  xl: 64,
  gutter: 16,
  marginMobile: 20,
  marginDesktop: 120,

  // Legacy template numeric aliases
  half: 2,
  one: 4,
  two: 8,
  three: 16,
  four: 24,
  five: 32,
  six: 64,
} as const;

export const Radius = {
  sm: 4,
  default: 8,
  md: 12,
  lg: 16,
  xl: 24,
  full: 9999,
} as const;

export const BottomTabInset = Platform.select({ ios: 70, android: 80 }) ?? 75;
export const MaxContentWidth = 720;

