import React, { useState, useRef, useCallback } from 'react';
import {
  StyleSheet,
  View,
  Pressable,
  ScrollView,
  TextInput,
  Animated,
  PanResponder,
} from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { MaterialIcons } from '@expo/vector-icons';
import { useLocalSearchParams, useRouter, useFocusEffect } from 'expo-router';

import { ThemedText } from '@/components/themed-text';
import { Radius, Spacing } from '@/constants/theme';
import { useTheme } from '@/hooks/use-theme';
import { useWallet } from '@/context/wallet-context';

function SwipeSlider({
  onSwipeComplete,
  label = 'SWIPE TO PAY',
  disabled = false,
}: {
  onSwipeComplete: () => void;
  label?: string;
  disabled?: boolean;
}) {
  const pan = useRef(new Animated.Value(0)).current;
  const [trackWidth, setTrackWidth] = useState(300);
  const knobWidth = 48;
  const maxSlide = Math.max(0, trackWidth - knobWidth - 12);

  const onSwipeCompleteRef = useRef(onSwipeComplete);
  onSwipeCompleteRef.current = onSwipeComplete;

  const disabledRef = useRef(disabled);
  disabledRef.current = disabled;

  const panResponder = useRef(
    PanResponder.create({
      onStartShouldSetPanResponder: () => !disabledRef.current,
      onMoveShouldSetPanResponder: () => !disabledRef.current,
      onPanResponderMove: (_, gestureState) => {
        if (disabledRef.current) return;
        const newX = Math.min(Math.max(0, gestureState.dx), maxSlide);
        pan.setValue(newX);
      },
      onPanResponderRelease: (_, gestureState) => {
        if (disabledRef.current) return;
        if (gestureState.dx >= maxSlide * 0.75) {
          Animated.timing(pan, {
            toValue: maxSlide,
            duration: 150,
            useNativeDriver: true,
          }).start(() => {
            onSwipeCompleteRef.current();
            setTimeout(() => {
              Animated.spring(pan, {
                toValue: 0,
                useNativeDriver: true,
              }).start();
            }, 500);
          });
        } else {
          Animated.spring(pan, {
            toValue: 0,
            useNativeDriver: true,
          }).start();
        }
      },
    })
  ).current;

  return (
    <View
      style={[styles.swipeButton, disabled && styles.swipeButtonDisabled]}
      onLayout={(e) => setTrackWidth(e.nativeEvent.layout.width)}>
      <ThemedText
        type="labelMono"
        style={[styles.swipeText, disabled && { color: '#8E9192', opacity: 0.5 }]}>
        {disabled ? 'INSUFFICIENT FUNDS' : label}
      </ThemedText>
      <Animated.View
        {...panResponder.panHandlers}
        style={[
          styles.swipeKnob,
          disabled && { backgroundColor: '#353434' },
          {
            transform: [{ translateX: pan }],
          },
        ]}>
        <MaterialIcons
          name={disabled ? 'block' : 'arrow-forward'}
          size={22}
          color={disabled ? '#8E9192' : '#000000'}
        />
      </Animated.View>
    </View>
  );
}

export default function SendScreen() {
  const theme = useTheme();
  const router = useRouter();
  const params = useLocalSearchParams<{ name?: string; address?: string }>();
  const { balance, deductBalance } = useWallet();

  const recipientName = params.name || 'Direct Peer';
  const recipientAddress = params.address || '0x7F2A...3B9C';

  const [paymentAmount, setPaymentAmount] = useState('100.00');
  const [paymentSuccess, setPaymentSuccess] = useState(false);
  const [paymentError, setPaymentError] = useState<string | null>(null);

  // Keep a ref to the latest amount so swipe always reads real current value
  const paymentAmountRef = useRef(paymentAmount);
  paymentAmountRef.current = paymentAmount;

  // Always reset screen state when focused with new params
  useFocusEffect(
    useCallback(() => {
      setPaymentSuccess(false);
      setPaymentError(null);
      setPaymentAmount('100.00');
    }, [params.name, params.address])
  );

  const parsedAmount = parseFloat(paymentAmount) || 0;
  const isInsufficient = balance <= 0 || parsedAmount > balance;

  const handleExecutePayment = () => {
    const rawVal = paymentAmountRef.current;
    const num = parseFloat(rawVal) || 0;

    if (num <= 0) {
      setPaymentError('Please enter a valid amount');
      return;
    }

    if (balance <= 0 || num > balance) {
      setPaymentError('Insufficient money in wallet');
      return;
    }

    const success = deductBalance(num, {
      name: recipientName,
      address: recipientAddress,
    });

    if (success) {
      setPaymentError(null);
      setPaymentSuccess(true);
      setTimeout(() => {
        router.navigate('/');
      }, 1200);
    } else {
      setPaymentError('Insufficient money in wallet');
    }
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
            OFFLINE PAYMENT
          </ThemedText>
          <View style={{ width: 40 }} />
        </View>

        <ScrollView
          showsVerticalScrollIndicator={false}
          contentContainerStyle={styles.scrollContent}>
          {paymentSuccess ? (
            <View style={styles.successContainer}>
              <View style={styles.successIconCircle}>
                <MaterialIcons name="check" size={54} color="#34D399" />
              </View>
              <ThemedText type="headlineLg" style={styles.successTitle}>
                Payment Sent!
              </ThemedText>
              <ThemedText type="amount" style={{ color: '#34D399', marginVertical: 8 }}>
                - ₹{(parseFloat(paymentAmountRef.current) || 0).toFixed(2)}
              </ThemedText>
              <ThemedText type="labelMono" themeColor="textSecondary">
                Signed Offline Tx to {recipientName}
              </ThemedText>
            </View>
          ) : (
            <>
              {/* Recipient Identity Details */}
              <View style={styles.recipientCenterBox}>
                <View style={styles.recipientAvatarRing}>
                  <View style={styles.recipientAvatar}>
                    <MaterialIcons name="person" size={48} color="#E5E2E1" />
                  </View>
                </View>
                <ThemedText type="headlineLg" style={styles.recipientName}>
                  {recipientName}
                </ThemedText>
                <View style={styles.addressPill}>
                  <MaterialIcons name="lock" size={14} color={theme.tertiary} />
                  <ThemedText type="labelMono" style={styles.recipientAddress}>
                    {recipientAddress}
                  </ThemedText>
                </View>
                <View style={styles.meshBadge}>
                  <View style={styles.greenPulse} />
                  <ThemedText type="labelMono" style={{ color: '#34D399', fontSize: 11 }}>
                    DIRECT PEER LINK ACTIVE
                  </ThemedText>
                </View>
              </View>

              {/* Amount Input */}
              <View style={styles.amountInputSection}>
                <ThemedText type="labelMono" style={styles.amountLabel}>
                  ENTER AMOUNT (₹)
                </ThemedText>
                <View style={styles.amountInputRow}>
                  <ThemedText type="headlineLg" style={styles.currencyPrefix}>
                    ₹
                  </ThemedText>
                  <TextInput
                    value={paymentAmount}
                    onChangeText={(text) => {
                      setPaymentAmount(text);
                      setPaymentError(null);
                    }}
                    keyboardType="decimal-pad"
                    placeholder="0.00"
                    placeholderTextColor="#444748"
                    style={styles.amountTextInput}
                    autoFocus
                  />
                </View>

                {/* Quick Selection Chips */}
                <View style={styles.quickChipsRow}>
                  {['50', '100', '250', '500'].map((amt) => (
                    <Pressable
                      key={amt}
                      onPress={() => {
                        setPaymentAmount(amt);
                        setPaymentError(null);
                      }}
                      style={styles.chipButton}>
                      <ThemedText type="labelMono" style={styles.chipText}>
                        +₹{amt}
                      </ThemedText>
                    </Pressable>
                  ))}
                  <Pressable
                    onPress={() => {
                      setPaymentAmount(balance.toFixed(2));
                      setPaymentError(null);
                    }}
                    style={[styles.chipButton, { borderColor: theme.tertiary }]}>
                    <ThemedText type="labelMono" style={{ color: theme.tertiary, fontSize: 11 }}>
                      MAX
                    </ThemedText>
                  </Pressable>
                </View>

                <ThemedText
                  type="small"
                  style={[
                    styles.availableBalanceText,
                    balance <= 0 && { color: '#FFB4AB' },
                  ]}>
                  Available Vault Balance: ₹{balance.toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}
                </ThemedText>

                {(isInsufficient || paymentError) && (
                  <View style={styles.insufficientBanner}>
                    <MaterialIcons name="error-outline" size={18} color="#FFB4AB" />
                    <ThemedText type="labelMono" style={styles.insufficientText}>
                      {paymentError || 'Insufficient money in wallet'}
                    </ThemedText>
                  </View>
                )}
              </View>

              {/* Swipe to Pay Slider */}
              <View style={styles.sliderPaymentWrapper}>
                <SwipeSlider
                  label="SLIDE TO SEND PAYMENT"
                  disabled={isInsufficient}
                  onSwipeComplete={handleExecutePayment}
                />
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
    borderColor: '#4CD7F6',
    alignItems: 'center',
    justifyContent: 'center',
    backgroundColor: 'rgba(76, 215, 246, 0.08)',
    shadowColor: '#4CD7F6',
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
  addressPill: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 6,
    backgroundColor: 'rgba(255, 255, 255, 0.05)',
    paddingHorizontal: 12,
    paddingVertical: 6,
    borderRadius: Radius.full,
    borderWidth: 1,
    borderColor: 'rgba(255, 255, 255, 0.10)',
  },
  recipientAddress: {
    color: '#C8C6C5',
    fontSize: 12,
  },
  meshBadge: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 6,
    backgroundColor: 'rgba(52, 211, 153, 0.10)',
    paddingHorizontal: 10,
    paddingVertical: 4,
    borderRadius: Radius.full,
    borderWidth: 1,
    borderColor: 'rgba(52, 211, 153, 0.30)',
    marginTop: 4,
  },
  greenPulse: {
    width: 6,
    height: 6,
    borderRadius: 3,
    backgroundColor: '#34D399',
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
  sliderPaymentWrapper: {
    width: '100%',
    maxWidth: 420,
    marginTop: Spacing.md,
  },
  swipeButton: {
    position: 'relative',
    height: 60,
    backgroundColor: '#201F1F',
    borderWidth: 1,
    borderColor: 'rgba(255, 255, 255, 0.10)',
    borderRadius: Radius.full,
    flexDirection: 'row',
    alignItems: 'center',
    paddingHorizontal: 6,
    width: '100%',
    justifyContent: 'center',
    overflow: 'hidden',
  },
  swipeButtonDisabled: {
    backgroundColor: '#171618',
    borderColor: 'rgba(255, 255, 255, 0.05)',
  },
  swipeKnob: {
    position: 'absolute',
    left: 6,
    width: 48,
    height: 48,
    borderRadius: 24,
    backgroundColor: '#D2BBFF',
    alignItems: 'center',
    justifyContent: 'center',
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.35,
    shadowRadius: 6,
    zIndex: 2,
  },
  swipeText: {
    textAlign: 'center',
    color: '#C4C7C7',
    opacity: 0.85,
    fontSize: 12,
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
