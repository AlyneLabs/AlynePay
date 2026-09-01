import React, { createContext, useContext, useState, useEffect, useCallback } from 'react';
import { SystmBridgeService, MeshDiscoveredPeer, IncomingPaymentEvent } from '@/services/systm-bridge';

export interface Transaction {
  id: string;
  type: 'received' | 'sent';
  title: string;
  timestamp: string;
  amount: string;
}

export interface UserPeer {
  id: string;
  name: string;
  address: string;
  nodeId?: number | string;
  bearer?: string;
  isMeshDiscovered?: boolean;
}

interface WalletContextType {
  balance: number;
  transactions: Transaction[];
  recents: UserPeer[];
  publicKey: string;
  privateKey: string;
  nodeId: string;
  isMeshActive: boolean;
  meshPeers: MeshDiscoveredPeer[];
  deductBalance: (amount: number, recipient: { name: string; address: string; nodeId?: number | string }) => boolean;
  addDeposit: (amount: number) => void;
  addRecent: (user: UserPeer) => void;
  refreshMeshStatus: () => Promise<void>;
}

const WalletContext = createContext<WalletContextType | undefined>(undefined);

export function WalletProvider({ children }: { children: React.ReactNode }) {
  // Live starting balance (₹0.00, fully functional with deposit & send)
  const [balance, setBalance] = useState<number>(0.0);
  // Real transaction history
  const [transactions, setTransactions] = useState<Transaction[]>([]);
  // Real recents list
  const [recents, setRecents] = useState<UserPeer[]>([]);

  // Unique Wallet Node Identity (Synced from Systm daemon when online)
  const [publicKey, setPublicKey] = useState('0x7F2A...3B9C');
  const [privateKey] = useState('0x9E4A...B210');
  const [nodeId, setNodeId] = useState('NODE-7X99-ALYN');

  // Mesh Daemon Connectivity State
  const [isMeshActive, setIsMeshActive] = useState(false);
  const [meshPeers, setMeshPeers] = useState<MeshDiscoveredPeer[]>([]);

  const addRecent = (user: UserPeer) => {
    setRecents((prev) => {
      const filtered = prev.filter(
        (p) => p.name.toLowerCase() !== user.name.toLowerCase() && p.address !== user.address
      );
      return [user, ...filtered];
    });
  };

  const refreshMeshStatus = useCallback(async () => {
    const health = await SystmBridgeService.checkHealth();
    if (health.isConnected && health.node) {
      setIsMeshActive(true);
      if (health.node.nodeId) {
        setNodeId(`NODE-${health.node.nodeId.toString().slice(-4).toUpperCase()}`);
      }
      if (health.node.publicKey) {
        setPublicKey(health.node.publicKey);
      }
      const peers = await SystmBridgeService.getPeers();
      setMeshPeers(peers);
    } else {
      setIsMeshActive(false);
    }
  }, []);

  // Poll for incoming payment packets from the physical BLE/Wi-Fi radio mesh
  useEffect(() => {
    refreshMeshStatus();

    const interval = setInterval(async () => {
      // Check health and peers
      const health = await SystmBridgeService.checkHealth();
      if (health.isConnected) {
        setIsMeshActive(true);
        const peers = await SystmBridgeService.getPeers();
        setMeshPeers(peers);

        // Check for incoming peer payments delivered over radio
        const events = await SystmBridgeService.pollIncomingEvents();
        if (events.length > 0) {
          events.forEach((evt: IncomingPaymentEvent) => {
            const now = new Date();
            const timeStr = now.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });

            setBalance((prev) => Math.round((prev + evt.amount) * 100) / 100);

            const newTx: Transaction = {
              id: evt.txId || `tx-${Date.now()}`,
              type: 'received',
              title: `Received from ${evt.fromName || 'Mesh Peer'}`,
              timestamp: `Today, ${timeStr}`,
              amount: `+ ₹${evt.amount.toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`,
            };
            setTransactions((prev) => [newTx, ...prev]);

            addRecent({
              id: `peer-${evt.fromNodeId}`,
              name: evt.fromName || `Node ${evt.fromNodeId}`,
              address: `0x${evt.fromNodeId.toString(16).toUpperCase().slice(0, 6)}...`,
              nodeId: evt.fromNodeId,
              isMeshDiscovered: true,
            });
          });
        }
      } else {
        setIsMeshActive(false);
      }
    }, 2500);

    return () => clearInterval(interval);
  }, [refreshMeshStatus]);

  const deductBalance = (amount: number, recipient: { name: string; address: string; nodeId?: number | string }): boolean => {
    if (amount <= 0 || balance < amount) {
      return false;
    }

    const newBal = Math.round((balance - amount) * 100) / 100;
    setBalance(newBal);

    const now = new Date();
    const timeStr = now.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });

    const newTx: Transaction = {
      id: `tx-${Date.now()}`,
      type: 'sent',
      title: `Sent to ${recipient.name}`,
      timestamp: `Today, ${timeStr}`,
      amount: `- ₹${amount.toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`,
    };
    setTransactions((prev) => [newTx, ...prev]);

    // Dispatch signed packet across native Systm mesh if recipient has a Node ID
    if (recipient.nodeId) {
      SystmBridgeService.sendPayment({
        recipientNodeId: recipient.nodeId,
        recipientName: recipient.name,
        amount,
      });
    }

    // Automatically add recipient to Recents
    addRecent({
      id: `peer-${Date.now()}`,
      name: recipient.name,
      address: recipient.address,
      nodeId: recipient.nodeId,
    });

    return true;
  };

  const addDeposit = (amount: number) => {
    if (amount <= 0) return;

    const newBal = Math.round((balance + amount) * 100) / 100;
    setBalance(newBal);

    const now = new Date();
    const timeStr = now.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });

    const newTx: Transaction = {
      id: `tx-${Date.now()}`,
      type: 'received',
      title: 'Vault Deposit / Top-up',
      timestamp: `Today, ${timeStr}`,
      amount: `+ ₹${amount.toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`,
    };
    setTransactions((prev) => [newTx, ...prev]);
  };

  return (
    <WalletContext.Provider
      value={{
        balance,
        transactions,
        recents,
        publicKey,
        privateKey,
        nodeId,
        isMeshActive,
        meshPeers,
        deductBalance,
        addDeposit,
        addRecent,
        refreshMeshStatus,
      }}>
      {children}
    </WalletContext.Provider>
  );
}

export function useWallet() {
  const context = useContext(WalletContext);
  if (!context) {
    throw new Error('useWallet must be used within a WalletProvider');
  }
  return context;
}
