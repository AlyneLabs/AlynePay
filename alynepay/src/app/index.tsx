import React, { useState, useRef, useEffect } from 'react';
import {
  ScrollView,
  StyleSheet,
  View,
  Pressable,
  useWindowDimensions,
  Platform,
  Modal,
  TextInput,
  Keyboard,
} from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { LinearGradient } from 'expo-linear-gradient';
import { BlurView } from 'expo-blur';
import { MaterialIcons } from '@expo/vector-icons';
import { CameraView, useCameraPermissions } from 'expo-camera';
import { useRouter } from 'expo-router';

import { ThemedText } from '@/components/themed-text';
import { Radius, Spacing, BottomTabInset } from '@/constants/theme';
import { useTheme } from '@/hooks/use-theme';
import { useWallet } from '@/context/wallet-context';

export default function PayScreen() {
  const theme = useTheme();
  const router = useRouter();
  const { height: windowHeight } = useWindowDimensions();
  const { balance, transactions, recents, publicKey, privateKey } = useWallet();

  // State
  const [showPrivateKey, setShowPrivateKey] = useState(false);
  const [showMyQrModal, setShowMyQrModal] = useState(false);

  // Search Overlay State
  const [isSearchActive, setIsSearchActive] = useState(false);
  const [searchQuery, setSearchQuery] = useState('');
  const searchInputRef = useRef<TextInput>(null);

  // Camera & Scan State
  const [isCameraActive, setIsCameraActive] = useState(false);
  const [permission, requestPermission] = useCameraPermissions();
  const [isScanningDisabled, setIsScanningDisabled] = useState(false);

  // Top quarter screen division height
  const topQuarterHeight = Math.max(260, Math.round(windowHeight * 0.30));

  useEffect(() => {
    if (isSearchActive) {
      setTimeout(() => {
        searchInputRef.current?.focus();
      }, 100);
    }
  }, [isSearchActive]);

  const handleStartCamera = async () => {
    if (!permission?.granted) {
      const res = await requestPermission();
      if (!res.granted) {
        return;
      }
    }
    setIsCameraActive(true);
    setIsScanningDisabled(false);
  };

  const handleStopCamera = () => {
    setIsCameraActive(false);
    setIsScanningDisabled(false);
  };

  const handleBarcodeScanned = ({ data }: { data: string }) => {
    if (isScanningDisabled) return;
    setIsScanningDisabled(true);
    setIsCameraActive(false);

    let name = 'Alyne Peer';
    let address = data || '0x7F2A...3B9C';

    if (data.includes('user=')) {
      try {
        const urlParams = new URLSearchParams(data.split('?')[1]);
        name = urlParams.get('user') || 'Alyne Peer';
      } catch {
        name = 'Peer ' + data.slice(0, 4);
      }
    } else if (data.length > 8) {
      name = 'Peer ' + data.slice(0, 4);
      address = data.length > 16 ? `${data.slice(0, 6)}...${data.slice(-4)}` : data;
    }

    router.push({
      pathname: '/send',
      params: { name, address },
    });
  };

  const navigateToSend = (peer: { name: string; address: string }) => {
    setIsSearchActive(false);
    setSearchQuery('');
    router.push({
      pathname: '/send',
      params: { name: peer.name, address: peer.address },
    });
  };

  // Filter existing recents
  const filteredRecents = recents.filter(
    (p) =>
      p.name.toLowerCase().includes(searchQuery.toLowerCase()) ||
      p.address.toLowerCase().includes(searchQuery.toLowerCase())
  );

  return (
    <View style={[styles.container, { backgroundColor: theme.background }]}>
      {/* Fixed Top Left Profile Pic & Username */}
      {!isCameraActive && !isSearchActive && (
        <SafeAreaView edges={['top']} style={styles.fixedProfileHeader} pointerEvents="box-none">
          <View style={styles.profileContainer}>
            <View style={styles.avatarCircle}>
              <MaterialIcons name="person" size={20} color="#E5E2E1" />
            </View>
            <ThemedText type="default" style={styles.userNameText}>
              user
            </ThemedText>
          </View>
        </SafeAreaView>
      )}

      <ScrollView
        showsVerticalScrollIndicator={false}
        contentContainerStyle={styles.scrollContent}>
        {/* Top 1/4th White Frosted Glass Layer with Active Camera Viewfinder */}
        <View style={[styles.frostedTopLayer, { minHeight: topQuarterHeight }]}>
          {isCameraActive ? (
            <View style={StyleSheet.absoluteFill}>
              <CameraView
                style={StyleSheet.absoluteFill}
                facing="back"
                barcodeScannerSettings={{
                  barcodeTypes: ['qr'],
                }}
                onBarcodeScanned={handleBarcodeScanned}
              />

              <View style={styles.cameraOverlay}>
                <View style={styles.reticleBox}>
                  <View style={[styles.reticleCorner, styles.reticleTopLeft]} />
                  <View style={[styles.reticleCorner, styles.reticleTopRight]} />
                  <View style={[styles.reticleCorner, styles.reticleBottomLeft]} />
                  <View style={[styles.reticleCorner, styles.reticleBottomRight]} />
                </View>
                <ThemedText type="labelMono" style={styles.cameraHint}>
                  ALIGN QR CODE WITHIN FRAME
                </ThemedText>
              </View>

              <SafeAreaView edges={['top', 'right']} style={styles.cameraCloseSafeArea}>
                <Pressable
                  onPress={handleStopCamera}
                  style={({ pressed }) => [styles.cameraCloseBtn, pressed && styles.pressed]}
                  hitSlop={12}
                  accessibilityLabel="Close Camera">
                  <MaterialIcons name="close" size={24} color="#FFFFFF" />
                </Pressable>
              </SafeAreaView>

              <LinearGradient
                colors={['rgba(15, 15, 15, 0)', 'rgba(15, 15, 15, 0.85)', '#0F0F0F']}
                style={styles.bottomFadeMask}
                pointerEvents="none"
              />
            </View>
          ) : (
            <>
              {Platform.OS === 'ios' && (
                <BlurView intensity={25} tint="light" style={StyleSheet.absoluteFill} />
              )}

              <LinearGradient
                colors={[
                  'rgba(255, 255, 255, 0.12)',
                  'rgba(255, 255, 255, 0.05)',
                  'rgba(15, 15, 15, 0.50)',
                  '#0F0F0F',
                ]}
                locations={[0, 0.45, 0.8, 1]}
                start={{ x: 0.5, y: 0 }}
                end={{ x: 0.5, y: 1 }}
                style={StyleSheet.absoluteFill}
              />

              <SafeAreaView edges={['top', 'left', 'right']} style={styles.topSafeArea}>
                <View style={styles.topHeaderSpacer} />

                {/* Middle: Scan QR Code with Square Border */}
                <Pressable
                  onPress={handleStartCamera}
                  style={({ pressed }) => [styles.scanQrAction, pressed && styles.pressed]}
                  accessibilityRole="button"
                  accessibilityLabel="Scan QR Code">
                  <View style={styles.qrSquareGlow}>
                    <MaterialIcons name="qr-code-scanner" size={44} color={theme.tertiary} />
                  </View>
                  <ThemedText type="labelMono" style={styles.scanQrText}>
                    SCAN QR CODE
                  </ThemedText>
                </Pressable>
              </SafeAreaView>
            </>
          )}
        </View>

        {/* Below Section */}
        <View style={styles.belowSectionContainer}>
          {/* 1. Search Bar to Pay */}
          <Pressable
            onPress={() => setIsSearchActive(true)}
            style={({ pressed }) => [styles.searchBarTrigger, pressed && styles.pressed]}>
            <View style={styles.searchIconContainer}>
              <MaterialIcons name="search" size={22} color={theme.tertiary} />
            </View>
            <ThemedText type="default" style={styles.searchBarPlaceholder}>
              Search username to pay
            </ThemedText>
            <View style={styles.searchPillBadge}>
              <MaterialIcons name="arrow-forward" size={16} color="#C4C7C7" />
            </View>
          </Pressable>

          {/* 2. Recents Row (Real recents only) */}
          <View style={styles.recentsSection}>
            <ThemedText type="labelMono" style={styles.recentsTitle}>
              RECENTS
            </ThemedText>
            {recents.length === 0 ? (
              <View style={styles.emptyRecentsBox}>
                <MaterialIcons name="history" size={18} color="#8E9192" />
                <ThemedText type="small" themeColor="textSecondary" style={{ fontSize: 11 }}>
                  Recent peers will appear here after your first payment
                </ThemedText>
              </View>
            ) : (
              <ScrollView
                horizontal
                showsHorizontalScrollIndicator={false}
                contentContainerStyle={styles.recentsScrollList}>
                {recents.map((user) => (
                  <Pressable
                    key={user.id}
                    onPress={() => navigateToSend(user)}
                    style={({ pressed }) => [styles.recentUserItem, pressed && styles.pressed]}>
                    <View style={styles.recentAvatarCircle}>
                      <MaterialIcons name="person" size={22} color="#C4C7C7" />
                    </View>
                    <ThemedText type="labelMono" numberOfLines={1} style={styles.recentUserName}>
                      {user.name}
                    </ThemedText>
                  </Pressable>
                ))}
              </ScrollView>
            )}
          </View>

          {/* Heading "Wallet" */}
          <View style={styles.walletHeaderRow}>
            <ThemedText type="default" style={styles.walletHeading}>
              Wallet
            </ThemedText>
          </View>

          {/* 3. Current Balance Galaxy Card */}
          <View style={styles.cardContainer}>
            <LinearGradient
              colors={['#0F111A', '#1A0F2E']}
              start={{ x: 0, y: 0 }}
              end={{ x: 0, y: 1 }}
              style={styles.galaxyCard}>
              <View style={styles.cardTopRow}>
                <View>
                  <ThemedText type="labelMono" style={styles.cardSublabel}>
                    CURRENT BALANCE
                  </ThemedText>
                  <ThemedText type="amount" style={styles.balanceText}>
                    ₹{balance.toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}
                  </ThemedText>
                </View>
                <View style={styles.contactlessIcon}>
                  <MaterialIcons name="contactless" size={28} color={theme.tertiary} />
                </View>
              </View>

              <View style={styles.cardDetails}>
                <View style={styles.keyRow}>
                  <ThemedText type="labelMono" style={styles.cardKeyLabel}>
                    PUBLIC KEY
                  </ThemedText>
                  <ThemedText type="labelMono" style={{ color: theme.primary }}>
                    {publicKey}
                  </ThemedText>
                </View>

                <View style={styles.keyDivider} />

                <View style={styles.keyRow}>
                  <ThemedText type="labelMono" style={styles.cardKeyLabel}>
                    PRIVATE KEY
                  </ThemedText>
                  <View style={styles.privateKeyContainer}>
                    <ThemedText type="labelMono" style={styles.privateKeyText}>
                      {showPrivateKey ? privateKey : '••••••••••••'}
                    </ThemedText>
                    <Pressable
                      onPress={() => setShowPrivateKey(!showPrivateKey)}
                      hitSlop={8}
                      style={styles.eyeButton}>
                      <MaterialIcons
                        name={showPrivateKey ? 'visibility-off' : 'visibility'}
                        size={16}
                        color={theme.onSurfaceVariant}
                      />
                    </Pressable>
                  </View>
                </View>
              </View>
            </LinearGradient>
          </View>

          {/* Actions below Balance Card: Add Amount + My QR */}
          <View style={styles.walletActionsRow}>
            <Pressable
              onPress={() => router.push('/deposit')}
              style={({ pressed }) => [styles.addAmountButton, pressed && styles.pressed]}>
              <MaterialIcons name="add" size={20} color={theme.tertiary} />
              <ThemedText type="labelMono" style={styles.addAmountText}>
                ADD AMOUNT TO WALLET
              </ThemedText>
            </Pressable>

            <Pressable
              onPress={() => setShowMyQrModal(true)}
              style={({ pressed }) => [styles.myQrButton, pressed && styles.pressed]}>
              <MaterialIcons name="qr-code" size={20} color={theme.onSurface} />
              <ThemedText type="labelMono" style={styles.myQrText}>
                MY QR
              </ThemedText>
            </Pressable>
          </View>

          {/* 4. Payment History Section */}
          <View style={styles.freeHistorySection}>
            <ThemedText type="headlineLg" style={styles.bigHistoryHeading}>
              Payment History
            </ThemedText>

            {transactions.length === 0 ? (
              <View style={styles.emptyHistoryBox}>
                <MaterialIcons name="receipt-long" size={26} color="#8E9192" />
                <ThemedText type="small" themeColor="textSecondary" style={{ textAlign: 'center' }}>
                  No transaction history yet. Send or deposit to view activity.
                </ThemedText>
              </View>
            ) : (
              <View style={styles.freeTransactionsList}>
                {transactions.map((tx, idx) => (
                  <React.Fragment key={tx.id}>
                    {idx > 0 && <View style={styles.txDivider} />}
                    <Pressable
                      style={({ pressed }) => [styles.txItem, pressed && styles.txItemPressed]}>
                      <View style={styles.txLeft}>
                        <View
                          style={[
                            styles.txIconContainer,
                            {
                              backgroundColor:
                                tx.type === 'received'
                                ? 'rgba(76, 215, 246, 0.12)'
                                : 'rgba(210, 187, 255, 0.12)',
                            },
                          ]}>
                          <MaterialIcons
                            name={tx.type === 'received' ? 'arrow-downward' : 'arrow-upward'}
                            size={20}
                            color={tx.type === 'received' ? theme.tertiary : theme.secondary}
                          />
                        </View>
                        <View style={styles.txTextContainer}>
                          <ThemedText type="default" style={styles.txTitle}>
                            {tx.title}
                          </ThemedText>
                          <ThemedText type="labelMono" style={styles.txTimestamp}>
                            {tx.timestamp}
                          </ThemedText>
                        </View>
                      </View>
                      <ThemedText
                        type="default"
                        style={[
                          styles.txAmount,
                          tx.type === 'received' ? { color: theme.tertiary } : { color: theme.onSurface },
                        ]}>
                        {tx.amount}
                      </ThemedText>
                    </Pressable>
                  </React.Fragment>
                ))}
              </View>
            )}
          </View>
        </View>
      </ScrollView>

      {/* SEARCH OVERLAY */}
      <Modal
        visible={isSearchActive}
        animationType="fade"
        transparent
        onRequestClose={() => {
          setIsSearchActive(false);
          setSearchQuery('');
        }}>
        <View style={styles.searchOverlayContainer}>
          {Platform.OS === 'ios' && (
            <BlurView intensity={45} tint="dark" style={StyleSheet.absoluteFill} />
          )}

          <SafeAreaView edges={['top', 'left', 'right', 'bottom']} style={styles.searchSafeArea}>
            <View style={styles.searchHeaderTop}>
              <View style={styles.searchActiveInputBox}>
                <MaterialIcons name="search" size={22} color={theme.tertiary} />
                <TextInput
                  ref={searchInputRef}
                  value={searchQuery}
                  onChangeText={setSearchQuery}
                  placeholder="Search username to pay"
                  placeholderTextColor="#8E9192"
                  style={styles.searchInputField}
                  autoCapitalize="none"
                  returnKeyType="search"
                />
                {searchQuery.length > 0 && (
                  <Pressable onPress={() => setSearchQuery('')} hitSlop={8}>
                    <MaterialIcons name="cancel" size={18} color="#8E9192" />
                  </Pressable>
                )}
              </View>

              <Pressable
                onPress={() => {
                  Keyboard.dismiss();
                  setIsSearchActive(false);
                  setSearchQuery('');
                }}
                style={styles.searchCancelBtn}>
                <ThemedText type="labelMono" style={styles.searchCancelText}>
                  Cancel
                </ThemedText>
              </Pressable>
            </View>

            <ScrollView
              keyboardShouldPersistTaps="handled"
              showsVerticalScrollIndicator={false}
              contentContainerStyle={styles.searchResultsScroll}>
              <ThemedText type="labelMono" style={styles.searchSectionHeading}>
                {searchQuery.trim().length > 0 ? 'SEARCH RESULT' : 'RECENT PEERS'}
              </ThemedText>

              {/* Show matching recents */}
              {filteredRecents.map((user) => (
                <Pressable
                  key={user.id}
                  onPress={() => navigateToSend(user)}
                  style={({ pressed }) => [styles.searchResultCard, pressed && styles.pressed]}>
                  <View style={styles.searchResultLeft}>
                    <View style={styles.searchResultAvatar}>
                      <MaterialIcons name="person" size={24} color="#E5E2E1" />
                    </View>
                    <View style={styles.searchResultInfo}>
                      <ThemedText type="default" style={styles.searchResultName}>
                        {user.name}
                      </ThemedText>
                      <ThemedText type="labelMono" style={styles.searchResultAddress}>
                        {user.address}
                      </ThemedText>
                    </View>
                  </View>
                  <View style={styles.searchPayAction}>
                    <ThemedText type="labelMono" style={{ color: theme.tertiary, fontSize: 11 }}>
                      PAY
                    </ThemedText>
                    <MaterialIcons name="arrow-forward" size={16} color={theme.tertiary} />
                  </View>
                </Pressable>
              ))}

              {/* Dynamic User Lookup for typed username */}
              {searchQuery.trim().length > 0 &&
                !filteredRecents.some(
                  (p) => p.name.toLowerCase() === searchQuery.trim().toLowerCase()
                ) && (
                  <Pressable
                    onPress={() =>
                      navigateToSend({
                        name: searchQuery.trim(),
                        address: `0x${Math.random().toString(16).slice(2, 6).toUpperCase()}...${Math.random().toString(16).slice(2, 6).toUpperCase()}`,
                      })
                    }
                    style={({ pressed }) => [styles.searchResultCard, styles.customMatchCard, pressed && styles.pressed]}>
                    <View style={styles.searchResultLeft}>
                      <View style={[styles.searchResultAvatar, { borderColor: theme.tertiary }]}>
                        <MaterialIcons name="account-circle" size={26} color={theme.tertiary} />
                      </View>
                      <View style={styles.searchResultInfo}>
                        <ThemedText type="default" style={styles.searchResultName}>
                          {searchQuery.trim()}
                        </ThemedText>
                        <ThemedText type="labelMono" style={{ color: theme.tertiary, fontSize: 11 }}>
                          Direct Offline Peer Lookup
                        </ThemedText>
                      </View>
                    </View>
                    <View style={styles.searchPayAction}>
                      <ThemedText type="labelMono" style={{ color: theme.tertiary, fontSize: 11 }}>
                        SEND
                      </ThemedText>
                      <MaterialIcons name="arrow-forward" size={16} color={theme.tertiary} />
                    </View>
                  </Pressable>
                )}

              {searchQuery.trim().length === 0 && recents.length === 0 && (
                <View style={styles.emptySearchNotice}>
                  <ThemedText type="small" themeColor="textSecondary" style={{ textAlign: 'center' }}>
                    Type any username to send payments directly via mesh
                  </ThemedText>
                </View>
              )}
            </ScrollView>
          </SafeAreaView>
        </View>
      </Modal>

      {/* MY QR CODE MODAL */}
      <Modal
        visible={showMyQrModal}
        transparent
        animationType="fade"
        onRequestClose={() => setShowMyQrModal(false)}>
        <Pressable style={styles.modalOverlay} onPress={() => setShowMyQrModal(false)}>
          <Pressable style={styles.modalContent} onPress={(e) => e.stopPropagation()}>
            <View style={styles.modalHeader}>
              <ThemedText type="headlineLg" style={styles.modalTitle}>
                My QR Code
              </ThemedText>
              <Pressable onPress={() => setShowMyQrModal(false)} hitSlop={8}>
                <MaterialIcons name="close" size={24} color={theme.onSurfaceVariant} />
              </Pressable>
            </View>

            <View style={styles.qrDisplayBox}>
              <MaterialIcons name="qr-code-2" size={180} color="#0F0F0F" />
            </View>

            <ThemedText type="labelMono" style={styles.qrAddressText}>
              {publicKey}
            </ThemedText>
            <ThemedText type="small" themeColor="textSecondary" style={styles.qrHintText}>
              Scan to send offline ₹ payments
            </ThemedText>
          </Pressable>
        </Pressable>
      </Modal>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
  },
  fixedProfileHeader: {
    position: 'absolute',
    top: 0,
    left: 0,
    right: 0,
    zIndex: 100,
    paddingHorizontal: Spacing.marginMobile,
    paddingTop: Spacing.xs,
  },
  topHeaderSpacer: {
    height: 40,
  },
  scrollContent: {
    paddingBottom: Spacing.xl * 1.5,
  },
  frostedTopLayer: {
    width: '100%',
    position: 'relative',
    justifyContent: 'space-between',
    paddingBottom: Spacing.md,
    overflow: 'hidden',
    ...Platform.select({
      web: {
        backdropFilter: 'blur(20px)',
      } as any,
    }),
  },
  topSafeArea: {
    paddingHorizontal: Spacing.marginMobile,
    paddingTop: Spacing.xs,
    maxWidth: 720,
    width: '100%',
    alignSelf: 'center',
    zIndex: 2,
    flex: 1,
    justifyContent: 'space-between',
  },
  cameraOverlay: {
    ...StyleSheet.absoluteFill,
    justifyContent: 'center',
    alignItems: 'center',
    zIndex: 3,
  },
  cameraCloseSafeArea: {
    position: 'absolute',
    top: Spacing.sm,
    right: Spacing.marginMobile,
    zIndex: 10,
  },
  cameraCloseBtn: {
    width: 40,
    height: 40,
    borderRadius: 20,
    backgroundColor: 'rgba(0, 0, 0, 0.70)',
    alignItems: 'center',
    justifyContent: 'center',
    borderWidth: 1,
    borderColor: 'rgba(255, 255, 255, 0.25)',
  },
  reticleBox: {
    width: 140,
    height: 140,
    position: 'relative',
    marginBottom: 12,
  },
  reticleCorner: {
    position: 'absolute',
    width: 24,
    height: 24,
    borderColor: '#4CD7F6',
  },
  reticleTopLeft: {
    top: 0,
    left: 0,
    borderTopWidth: 3,
    borderLeftWidth: 3,
  },
  reticleTopRight: {
    top: 0,
    right: 0,
    borderTopWidth: 3,
    borderRightWidth: 3,
  },
  reticleBottomLeft: {
    bottom: 0,
    left: 0,
    borderBottomWidth: 3,
    borderLeftWidth: 3,
  },
  reticleBottomRight: {
    bottom: 0,
    right: 0,
    borderBottomWidth: 3,
    borderRightWidth: 3,
  },
  cameraHint: {
    color: '#E5E2E1',
    fontSize: 11,
    letterSpacing: 1,
    backgroundColor: 'rgba(0,0,0,0.6)',
    paddingHorizontal: 12,
    paddingVertical: 4,
    borderRadius: Radius.full,
  },
  bottomFadeMask: {
    position: 'absolute',
    left: 0,
    right: 0,
    bottom: 0,
    height: 50,
    zIndex: 4,
  },
  profileContainer: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 12,
    alignSelf: 'flex-start',
  },
  avatarCircle: {
    width: 38,
    height: 38,
    borderRadius: 19,
    backgroundColor: '#000000',
    borderWidth: 1.5,
    borderColor: 'rgba(255, 255, 255, 0.35)',
    alignItems: 'center',
    justifyContent: 'center',
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.5,
    shadowRadius: 5,
  },
  userNameText: {
    fontSize: 16,
    fontWeight: '600',
    color: '#E5E2E1',
    letterSpacing: 0.2,
    textShadowColor: 'rgba(0, 0, 0, 0.75)',
    textShadowOffset: { width: 0, height: 1 },
    textShadowRadius: 4,
  },
  scanQrAction: {
    alignItems: 'center',
    justifyContent: 'center',
    paddingVertical: Spacing.sm,
    gap: 12,
    alignSelf: 'center',
  },
  qrSquareGlow: {
    width: 124,
    height: 124,
    borderRadius: Radius.md,
    backgroundColor: 'rgba(76, 215, 246, 0.10)',
    alignItems: 'center',
    justifyContent: 'center',
    borderWidth: 1.5,
    borderColor: 'rgba(76, 215, 246, 0.35)',
    shadowColor: '#4CD7F6',
    shadowOffset: { width: 0, height: 0 },
    shadowOpacity: 0.5,
    shadowRadius: 18,
  },
  scanQrText: {
    color: '#E5E2E1',
    letterSpacing: 1.2,
    fontSize: 13,
    fontWeight: '600',
  },
  belowSectionContainer: {
    paddingHorizontal: Spacing.marginMobile,
    paddingTop: Spacing.xs,
    maxWidth: 720,
    width: '100%',
    alignSelf: 'center',
    gap: Spacing.md,
  },
  searchBarTrigger: {
    height: 56,
    backgroundColor: '#1E1D21',
    borderWidth: 1,
    borderColor: 'rgba(255, 255, 255, 0.12)',
    borderRadius: Radius.full,
    flexDirection: 'row',
    alignItems: 'center',
    paddingHorizontal: 16,
    gap: 12,
    width: '100%',
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 4 },
    shadowOpacity: 0.3,
    shadowRadius: 8,
  },
  searchIconContainer: {
    width: 32,
    height: 32,
    borderRadius: 16,
    backgroundColor: 'rgba(76, 215, 246, 0.12)',
    alignItems: 'center',
    justifyContent: 'center',
  },
  searchBarPlaceholder: {
    flex: 1,
    color: '#8E9192',
    fontSize: 14,
    fontWeight: '500',
  },
  searchPillBadge: {
    width: 28,
    height: 28,
    borderRadius: 14,
    backgroundColor: 'rgba(255, 255, 255, 0.06)',
    alignItems: 'center',
    justifyContent: 'center',
  },
  recentsSection: {
    gap: 10,
    marginBottom: Spacing.sm,
  },
  recentsTitle: {
    color: '#8E9192',
    fontSize: 11,
    letterSpacing: 1,
  },
  emptyRecentsBox: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 8,
    paddingVertical: 10,
    paddingHorizontal: 12,
    backgroundColor: 'rgba(255, 255, 255, 0.03)',
    borderRadius: Radius.md,
    borderWidth: 1,
    borderColor: 'rgba(255, 255, 255, 0.06)',
  },
  recentsScrollList: {
    gap: 16,
    paddingVertical: 4,
  },
  recentUserItem: {
    alignItems: 'center',
    gap: 6,
    width: 56,
  },
  recentAvatarCircle: {
    width: 48,
    height: 48,
    borderRadius: 24,
    backgroundColor: '#000000',
    borderWidth: 1.5,
    borderColor: 'rgba(255, 255, 255, 0.22)',
    alignItems: 'center',
    justifyContent: 'center',
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.4,
    shadowRadius: 4,
  },
  recentUserName: {
    fontSize: 11,
    color: '#C4C7C7',
    textAlign: 'center',
  },
  walletHeaderRow: {
    marginTop: Spacing.xs,
    marginBottom: -19,
  },
  walletHeading: {
    fontSize: 24,
    fontWeight: '600',
    color: '#E5E2E1',
    letterSpacing: 0.3,
  },
  cardContainer: {
    borderRadius: Radius.lg,
    overflow: 'hidden',
    borderWidth: 0.6,
    borderColor: 'rgba(0, 0, 0, 0.65)',
    shadowColor: '#9900ffff',
    shadowOffset: { width: 0, height: 0 },
    shadowOpacity: 0.25,
    shadowRadius: 10,
    elevation: 8,
    width: '100%',
  },
  galaxyCard: {
    padding: Spacing.md,
    minHeight: 160,
    justifyContent: 'space-between',
  },
  cardTopRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'flex-start',
    marginBottom: Spacing.md,
  },
  cardSublabel: {
    color: '#C4C7C7',
    marginBottom: 6,
    fontSize: 11,
    letterSpacing: 1,
  },
  balanceText: {
    color: '#FFFFFF',
    fontSize: 30,
    fontWeight: '700',
  },
  contactlessIcon: {
    opacity: 0.9,
    paddingTop: 2,
  },
  cardDetails: {
    gap: 10,
  },
  keyRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
  },
  cardKeyLabel: {
    color: '#C4C7C7',
    fontSize: 11,
  },
  keyDivider: {
    height: 1,
    backgroundColor: 'rgba(255, 255, 255, 0.08)',
  },
  privateKeyContainer: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 8,
  },
  privateKeyText: {
    color: '#C8C6C5',
    letterSpacing: 2,
    fontWeight: '700',
    fontSize: 12,
  },
  eyeButton: {
    padding: 4,
  },
  walletActionsRow: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 10,
    width: '100%',
    marginTop: -2,
    marginBottom: Spacing.sm,
  },
  addAmountButton: {
    flex: 1,
    height: 48,
    backgroundColor: '#1C1B1B',
    borderRadius: Radius.lg,
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'center',
    gap: 8,
    borderWidth: 1,
    borderColor: 'rgba(255, 255, 255, 0.10)',
    paddingHorizontal: 12,
  },
  addAmountText: {
    color: '#E5E2E1',
    fontSize: 11,
    fontWeight: '600',
    letterSpacing: 0.6,
  },
  myQrButton: {
    height: 48,
    paddingHorizontal: 16,
    backgroundColor: '#1C1B1B',
    borderRadius: Radius.lg,
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'center',
    gap: 6,
    borderWidth: 1,
    borderColor: 'rgba(255, 255, 255, 0.10)',
  },
  myQrText: {
    color: '#E5E2E1',
    fontSize: 11,
    fontWeight: '600',
    letterSpacing: 0.6,
  },
  freeHistorySection: {
    width: '100%',
    marginTop: Spacing.xs,
    gap: Spacing.sm,
  },
  bigHistoryHeading: {
    fontSize: 22,
    fontWeight: '700',
    color: '#E5E2E1',
    letterSpacing: -0.4,
  },
  emptyHistoryBox: {
    alignItems: 'center',
    justifyContent: 'center',
    paddingVertical: Spacing.lg,
    paddingHorizontal: Spacing.md,
    backgroundColor: 'rgba(255, 255, 255, 0.02)',
    borderRadius: Radius.lg,
    borderWidth: 1,
    borderColor: 'rgba(255, 255, 255, 0.05)',
    gap: 8,
  },
  freeTransactionsList: {
    width: '100%',
    gap: 2,
  },
  txDivider: {
    height: 1,
    backgroundColor: 'rgba(255, 255, 255, 0.06)',
  },
  txItem: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    paddingVertical: 12,
    paddingHorizontal: 2,
    borderRadius: Radius.default,
    width: '100%',
  },
  txItemPressed: {
    backgroundColor: 'rgba(255, 255, 255, 0.04)',
  },
  txLeft: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 12,
    flex: 1,
  },
  txIconContainer: {
    width: 42,
    height: 42,
    borderRadius: 21,
    alignItems: 'center',
    justifyContent: 'center',
  },
  txTextContainer: {
    gap: 2,
  },
  txTitle: {
    fontSize: 15,
    fontWeight: '500',
  },
  txTimestamp: {
    fontSize: 11,
    color: '#8E9192',
  },
  txAmount: {
    fontSize: 15,
    fontWeight: '600',
    textAlign: 'right',
  },
  pressed: {
    opacity: 0.8,
    transform: [{ scale: 0.98 }],
  },
  modalOverlay: {
    flex: 1,
    backgroundColor: 'rgba(0, 0, 0, 0.75)',
    justifyContent: 'center',
    alignItems: 'center',
    padding: Spacing.marginMobile,
  },
  modalContent: {
    width: '100%',
    maxWidth: 360,
    backgroundColor: '#1C1B1B',
    borderRadius: Radius.xl,
    padding: Spacing.md,
    alignItems: 'center',
    borderWidth: 1,
    borderColor: 'rgba(255, 255, 255, 0.12)',
    gap: Spacing.sm,
  },
  modalHeader: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    width: '100%',
    marginBottom: Spacing.xs,
  },
  modalTitle: {
    fontSize: 20,
    fontWeight: '700',
  },
  qrDisplayBox: {
    backgroundColor: '#FFFFFF',
    padding: 16,
    borderRadius: Radius.lg,
    alignItems: 'center',
    justifyContent: 'center',
    marginVertical: Spacing.xs,
  },
  qrAddressText: {
    color: '#4CD7F6',
    fontSize: 13,
    letterSpacing: 1,
  },
  qrHintText: {
    textAlign: 'center',
    fontSize: 12,
  },

  // SEARCH OVERLAY STYLES
  searchOverlayContainer: {
    flex: 1,
    backgroundColor: 'rgba(15, 15, 15, 0.94)',
  },
  searchSafeArea: {
    flex: 1,
  },
  searchHeaderTop: {
    flexDirection: 'row',
    alignItems: 'center',
    paddingHorizontal: Spacing.marginMobile,
    paddingVertical: Spacing.sm,
    gap: 12,
    borderBottomWidth: 1,
    borderBottomColor: 'rgba(255, 255, 255, 0.10)',
  },
  searchActiveInputBox: {
    flex: 1,
    height: 48,
    backgroundColor: '#201F1F',
    borderRadius: Radius.full,
    borderWidth: 1,
    borderColor: 'rgba(76, 215, 246, 0.35)',
    flexDirection: 'row',
    alignItems: 'center',
    paddingHorizontal: 14,
    gap: 10,
  },
  searchInputField: {
    flex: 1,
    color: '#FFFFFF',
    fontSize: 15,
    fontWeight: '500',
    padding: 0,
  },
  searchCancelBtn: {
    paddingVertical: 8,
    paddingHorizontal: 4,
  },
  searchCancelText: {
    color: '#4CD7F6',
    fontSize: 13,
    fontWeight: '600',
  },
  searchResultsScroll: {
    paddingHorizontal: Spacing.marginMobile,
    paddingTop: Spacing.md,
    paddingBottom: Spacing.xl,
    gap: 10,
  },
  searchSectionHeading: {
    color: '#8E9192',
    fontSize: 11,
    letterSpacing: 1,
    marginBottom: 4,
  },
  searchResultCard: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    backgroundColor: 'rgba(32, 31, 31, 0.75)',
    borderWidth: 1,
    borderColor: 'rgba(255, 255, 255, 0.08)',
    borderRadius: Radius.lg,
    paddingVertical: 12,
    paddingHorizontal: 14,
  },
  customMatchCard: {
    borderColor: 'rgba(76, 215, 246, 0.25)',
    backgroundColor: 'rgba(76, 215, 246, 0.06)',
  },
  searchResultLeft: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 12,
    flex: 1,
  },
  searchResultAvatar: {
    width: 44,
    height: 44,
    borderRadius: 22,
    backgroundColor: '#000000',
    borderWidth: 1.5,
    borderColor: 'rgba(255, 255, 255, 0.20)',
    alignItems: 'center',
    justifyContent: 'center',
  },
  searchResultInfo: {
    flex: 1,
    gap: 2,
  },
  searchResultName: {
    fontSize: 16,
    fontWeight: '600',
    color: '#FFFFFF',
  },
  searchResultAddress: {
    fontSize: 11,
    color: '#8E9192',
  },
  searchPayAction: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 4,
    backgroundColor: 'rgba(76, 215, 246, 0.12)',
    paddingHorizontal: 10,
    paddingVertical: 6,
    borderRadius: Radius.full,
    borderWidth: 1,
    borderColor: 'rgba(76, 215, 246, 0.25)',
  },
  emptySearchNotice: {
    paddingVertical: Spacing.xl,
    paddingHorizontal: Spacing.md,
    alignItems: 'center',
  },
});
