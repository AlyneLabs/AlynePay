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
  deductBalance: (amount: number, recipient: { name: string; address: string }) => boolean;
  addDeposit: (amount: number) => void;
  addRecent: (user: UserPeer) => void;
}

const INITIAL_TRANSACTIONS: Transaction[] = [
  {
    id: 'tx-1',
    type: 'received',
    title: 'Received from Node 7',
    timestamp: 'Today, 14:32',
    amount: '+ 500.00 ALY',
  },
  {
    id: 'tx-2',
    type: 'sent',
    title: 'Sent to Orbit Station',
    timestamp: 'Yesterday, 09:15',
    amount: '- 1,200.00 ALY',
  },
  {
    id: 'tx-3',
    type: 'sent',
    title: 'Sent to Sector 4 Vendor',
    timestamp: 'Oct 24, 18:45',
    amount: '- 45.50 ALY',
  },
];

const WalletContext = createContext<WalletContextType | undefined>(undefined);

export function WalletProvider({ children }: { children: React.ReactNode }) {
  const [balance, setBalance] = useState<number>(12450.0);
  const [transactions, setTransactions] = useState<Transaction[]>(INITIAL_TRANSACTIONS);
  // Clean initial recents (no mock users)
  const [recents, setRecents] = useState<UserPeer[]>([]);

  const addRecent = (user: UserPeer) => {
    setRecents((prev) => {
      // Remove duplicate by name or address if exists
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

    const newTx: Transaction = {
      id: `tx-${Date.now()}`,
      type: 'sent',
      title: `Sent to ${recipient.name}`,
      timestamp: 'Just now',
      amount: `- ${amount.toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 })} ALY`,
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

    const newTx: Transaction = {
      id: `tx-${Date.now()}`,
      type: 'received',
      title: 'Vault Deposit / Top-up',
      timestamp: 'Just now',
      amount: `+ ${amount.toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 })} ALY`,
    };
    setTransactions((prev) => [newTx, ...prev]);
  };

  return (
    <WalletContext.Provider
      value={{
        balance,
        transactions,
        recents,
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
