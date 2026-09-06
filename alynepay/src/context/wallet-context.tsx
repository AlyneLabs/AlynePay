import React, { createContext, useContext, useState } from 'react';

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
}

interface WalletContextType {
  balance: number;
  transactions: Transaction[];
  recents: UserPeer[];
  publicKey: string;
  privateKey: string;
  nodeId: string;
  deductBalance: (amount: number, recipient: { name: string; address: string }) => boolean;
  addDeposit: (amount: number) => void;
  addRecent: (user: UserPeer) => void;
}

const WalletContext = createContext<WalletContextType | undefined>(undefined);

export function WalletProvider({ children }: { children: React.ReactNode }) {
  // Live starting balance (0.00 ALY, fully functional with deposit & send)
  const [balance, setBalance] = useState<number>(0.0);
  // Real transaction history (starts empty, populated on actual transfers & deposits)
  const [transactions, setTransactions] = useState<Transaction[]>([]);
  // Real recents list (starts empty, populated on actual payments)
  const [recents, setRecents] = useState<UserPeer[]>([]);

  // Unique Wallet Node Identity
  const publicKey = '0x7F2A...3B9C';
  const privateKey = '0x9E4A...B210';
  const nodeId = 'NODE-7X99-ALYN';

  const addRecent = (user: UserPeer) => {
    setRecents((prev) => {
      const filtered = prev.filter(
        (p) => p.name.toLowerCase() !== user.name.toLowerCase() && p.address !== user.address
      );
      return [user, ...filtered];
    });
  };

  const deductBalance = (amount: number, recipient: { name: string; address: string }): boolean => {
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

    // Automatically add recipient to Recents
    addRecent({
      id: `peer-${Date.now()}`,
      name: recipient.name,
      address: recipient.address,
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
        deductBalance,
        addDeposit,
        addRecent,
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
