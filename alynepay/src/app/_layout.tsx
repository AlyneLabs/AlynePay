import { Stack, DarkTheme, ThemeProvider } from 'expo-router';
import * as SplashScreen from 'expo-splash-screen';
import { StatusBar } from 'expo-status-bar';

import { AnimatedSplashOverlay } from '@/components/animated-icon';
import { WalletProvider } from '@/context/wallet-context';

SplashScreen.preventAutoHideAsync();

const AlyneDarkTheme = {
  ...DarkTheme,
  colors: {
    ...DarkTheme.colors,
    background: '#0F0F0F',
    card: '#141313',
    text: '#E5E2E1',
    border: 'rgba(255, 255, 255, 0.10)',
    primary: '#4CD7F6',
  },
};

export default function RootLayout() {
  return (
    <ThemeProvider value={AlyneDarkTheme}>
      <StatusBar style="light" />
      <AnimatedSplashOverlay />
      <WalletProvider>
        <Stack screenOptions={{ headerShown: false, animation: 'fade' }}>
          <Stack.Screen name="index" />
          <Stack.Screen name="send" options={{ animation: 'slide_from_right' }} />
          <Stack.Screen name="deposit" options={{ animation: 'slide_from_bottom' }} />
        </Stack>
      </WalletProvider>
    </ThemeProvider>
  );
}
