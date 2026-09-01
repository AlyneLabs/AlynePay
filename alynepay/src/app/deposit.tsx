import React, { useState, useCallback } from 'react';
import {
  StyleSheet,
  View,
  Pressable,
  ScrollView,
  TextInput,
} from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { MaterialIcons } from '@expo/vector-icons';
import { useRouter, useFocusEffect } from 'expo-router';

import { ThemedText } from '@/components/themed-text';
import { Radius, Spacing } from '@/constants/theme';
import { useTheme } from '@/hooks/use-theme';
import { useWallet } from '@/context/wallet-context';

export default function DepositScreen() {
  const theme = useTheme();
  const router = useRouter();
  const { balance, addDeposit } = useWallet();

  const [depositAmount, setDepositAmount] = useState('500.00');
  const [depositSuccess, setDepositSuccess] = useState(false);
  const [depositError, setDepositError] = useState<string | null>(null);

  useFocusEffect(
    useCallback(() => {
      setDepositSuccess(false);
      setDepositError(null);
      setDepositAmount('500.00');
    }, [])
  );

  const parsedAmount = parseFloat(depositAmount) || 0;

  const handleConfirmDeposit = () => {
    if (parsedAmount <= 0) {
      setDepositError('Please enter a valid deposit amount');
      return;
    }

    setDepositError(null);
    setDepositSuccess(true);
    addDeposit(parsedAmount);

    setTimeout(() => {
      router.navigate('/');
    }, 1200);
  };

  return (
    <View style={[styles.container, { backgroundColor: theme.background }]}>
      <SafeAreaView edges={['top', 'left', 'right', 'bottom']} style={styles.safeArea}>
        {/* Top Bar with Back Button */}
        <View style={styles.topBar}>
          <Pressable
            onPress={() => router.navigate('/')}
            hitSlop={12}
            style={({ pressed }) => [styles.backBtn, pressed && styles.pressed]}>
            <MaterialIcons name="arrow-back" size={24} color="#E5E2E1" />
          </Pressable>
          <ThemedText type="labelMono" style={styles.headerTitle}>
            DEPOSIT / ADD FUNDS
          </ThemedText>
          <View style={{ width: 40 }} />
        </View>

        <ScrollView
          showsVerticalScrollIndicator={false}
          contentContainerStyle={styles.scrollContent}>
          {depositSuccess ? (
            <View style={styles.successContainer}>
              <View style={styles.successIconCircle}>
                <MaterialIcons name="account-balance-wallet" size={54} color="#34D399" />
              </View>
              <ThemedText type="headlineLg" style={styles.successTitle}>
                Funds Added!
              </ThemedText>
              <ThemedText type="amount" style={{ color: '#34D399', marginVertical: 8 }}>
                + ₹{parsedAmount.toFixed(2)}
              </ThemedText>
              <ThemedText type="labelMono" themeColor="textSecondary">
                New Balance: ₹{(balance).toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}
              </ThemedText>
            </View>
          ) : (
            <>
              {/* Wallet Header Card */}
              <View style={styles.recipientCenterBox}>
                <View style={[styles.recipientAvatarRing, { borderColor: theme.secondary }]}>
                  <View style={styles.recipientAvatar}>
                    <MaterialIcons name="account-balance-wallet" size={48} color={theme.secondary} />
                  </View>
                </View>
                <ThemedText type="headlineLg" style={styles.recipientName}>
                  Add Money to Vault
                </ThemedText>
                <ThemedText type="small" themeColor="textSecondary">
                  Current Vault Balance: ₹{balance.toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}
                </ThemedText>
              </View>

              {/* Amount Input */}
              <View style={styles.amountInputSection}>
                <ThemedText type="labelMono" style={styles.amountLabel}>
                  ENTER DEPOSIT AMOUNT (₹)
                </ThemedText>
                <View style={styles.amountInputRow}>
                  <ThemedText type="headlineLg" style={styles.currencyPrefix}>
                    ₹
                  </ThemedText>
                  <TextInput
                    value={depositAmount}
                    onChangeText={(text) => {
                      setDepositAmount(text);
                      setDepositError(null);
                    }}
                    keyboardType="decimal-pad"
                    placeholder="0.00"
                    placeholderTextColor="#444748"
                    style={styles.amountTextInput}
                    autoFocus
                  />
                </View>

                {/* Quick Increment Chips */}
                <View style={styles.quickChipsRow}>
                  {['100', '500', '1000', '5000'].map((amt) => (
                    <Pressable
                      key={amt}
                      onPress={() => {
                        setDepositAmount(amt);
                        setDepositError(null);
                      }}
                      style={styles.chipButton}>
                      <ThemedText type="labelMono" style={styles.chipText}>
                        +₹{amt}
                      </ThemedText>
                    </Pressable>
                  ))}
                </View>

                {depositError && (
                  <View style={styles.insufficientBanner}>
                    <MaterialIcons name="error-outline" size={18} color="#FFB4AB" />
                    <ThemedText type="labelMono" style={styles.insufficientText}>
                      {depositError}
                    </ThemedText>
                  </View>
                )}

                <ThemedText type="small" themeColor="textSecondary" style={styles.availableBalanceText}>
                  Projected Balance: ₹{(balance + parsedAmount).toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}
                </ThemedText>
              </View>

              {/* Confirm Button */}
              <View style={styles.actionWrapper}>
                <Pressable
                  onPress={handleConfirmDeposit}
                  style={({ pressed }) => [styles.confirmDepositBtn, pressed && styles.pressed]}>
                  <MaterialIcons name="check-circle" size={22} color="#000000" />
                  <ThemedText type="labelMono" style={styles.confirmDepositText}>
                    CONFIRM & ADD FUNDS
                  </ThemedText>
                </Pressable>
              </View>
            </>
          )}
        </ScrollView>
      </SafeAreaView>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#0F0F0F',
  },
  safeArea: {
    flex: 1,
  },
  topBar: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    paddingHorizontal: Spacing.marginMobile,
    paddingVertical: Spacing.sm,
    borderBottomWidth: 1,
    borderBottomColor: 'rgba(255, 255, 255, 0.08)',
  },
  backBtn: {
    width: 40,
    height: 40,
    borderRadius: 20,
    backgroundColor: 'rgba(255, 255, 255, 0.06)',
    alignItems: 'center',
    justifyContent: 'center',
  },
  headerTitle: {
    color: '#E5E2E1',
    letterSpacing: 1,
    fontSize: 13,
  },
  scrollContent: {
    paddingHorizontal: Spacing.marginMobile,
    paddingTop: Spacing.lg,
    paddingBottom: Spacing.xl,
    alignItems: 'center',
  },
  recipientCenterBox: {
    alignItems: 'center',
    gap: 8,
    marginVertical: Spacing.md,
  },
  recipientAvatarRing: {
    width: 96,
    height: 96,
    borderRadius: 48,
    borderWidth: 2,
    alignItems: 'center',
    justifyContent: 'center',
    backgroundColor: 'rgba(210, 187, 255, 0.08)',
    shadowColor: '#D2BBFF',
    shadowOffset: { width: 0, height: 0 },
    shadowOpacity: 0.4,
    shadowRadius: 16,
  },
  recipientAvatar: {
    width: 80,
    height: 80,
    borderRadius: 40,
    backgroundColor: '#000000',
    alignItems: 'center',
    justifyContent: 'center',
  },
  recipientName: {
    fontSize: 26,
    fontWeight: '700',
    color: '#FFFFFF',
    marginTop: 4,
  },
  amountInputSection: {
    width: '100%',
    maxWidth: 420,
    backgroundColor: '#161517',
    borderRadius: Radius.xl,
    padding: Spacing.md,
    borderWidth: 1,
    borderColor: 'rgba(255, 255, 255, 0.10)',
    gap: 12,
    marginVertical: Spacing.md,
  },
  amountLabel: {
    color: '#8E9192',
    fontSize: 11,
    letterSpacing: 1,
  },
  amountInputRow: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 10,
    borderBottomWidth: 1,
    borderBottomColor: 'rgba(255, 255, 255, 0.15)',
    paddingBottom: 8,
  },
  currencyPrefix: {
    color: '#4CD7F6',
    fontSize: 22,
    fontWeight: '700',
  },
  amountTextInput: {
    flex: 1,
    fontSize: 34,
    fontWeight: '700',
    color: '#FFFFFF',
    padding: 0,
  },
  quickChipsRow: {
    flexDirection: 'row',
    gap: 8,
    marginTop: 4,
  },
  chipButton: {
    flex: 1,
    paddingVertical: 8,
    backgroundColor: 'rgba(255, 255, 255, 0.05)',
    borderRadius: Radius.md,
    borderWidth: 1,
    borderColor: 'rgba(255, 255, 255, 0.10)',
    alignItems: 'center',
    justifyContent: 'center',
  },
  chipText: {
    color: '#E5E2E1',
    fontSize: 11,
  },
  availableBalanceText: {
    textAlign: 'center',
    fontSize: 12,
    color: '#C4C7C7',
    marginTop: 4,
  },
  insufficientBanner: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'center',
    gap: 6,
    backgroundColor: 'rgba(255, 180, 171, 0.12)',
    paddingVertical: 8,
    paddingHorizontal: 12,
    borderRadius: Radius.md,
    borderWidth: 1,
    borderColor: 'rgba(255, 180, 171, 0.35)',
    marginTop: 4,
  },
  insufficientText: {
    color: '#FFB4AB',
    fontSize: 12,
    fontWeight: '600',
  },
  actionWrapper: {
    width: '100%',
    maxWidth: 420,
    marginTop: Spacing.md,
  },
  confirmDepositBtn: {
    height: 56,
    backgroundColor: '#4CD7F6',
    borderRadius: Radius.full,
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'center',
    gap: 8,
    shadowColor: '#4CD7F6',
    shadowOffset: { width: 0, height: 4 },
    shadowOpacity: 0.4,
    shadowRadius: 10,
  },
  confirmDepositText: {
    color: '#000000',
    fontSize: 13,
    fontWeight: '700',
    letterSpacing: 0.8,
  },
  successContainer: {
    alignItems: 'center',
    justifyContent: 'center',
    paddingVertical: Spacing.xl * 2,
    gap: 12,
  },
  successIconCircle: {
    width: 90,
    height: 90,
    borderRadius: 45,
    backgroundColor: 'rgba(52, 211, 153, 0.15)',
    borderWidth: 2,
    borderColor: '#34D399',
    alignItems: 'center',
    justifyContent: 'center',
    marginBottom: 8,
    shadowColor: '#34D399',
    shadowOffset: { width: 0, height: 0 },
    shadowOpacity: 0.5,
    shadowRadius: 20,
  },
  successTitle: {
    color: '#FFFFFF',
    fontSize: 28,
  },
  pressed: {
    opacity: 0.7,
  },
});
