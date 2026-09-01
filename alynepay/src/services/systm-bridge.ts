export interface MeshNodeStatus {
  status: 'ONLINE' | 'OFFLINE';
  nodeId: string;
  publicKey: string;
  meshActive: boolean;
  protocolVersion: string;
}

export interface MeshDiscoveredPeer {
  id: string;
  nodeId: number | string;
  name: string;
  address: string;
  bearer: 'BLE' | 'WIFI' | 'IP';
  connected: boolean;
}

export interface IncomingPaymentEvent {
  type: 'PAYMENT';
  txId: string;
  fromNodeId: number | string;
  fromName: string;
  toNodeId: number | string;
  amount: number;
  timestamp: number;
}

const BRIDGE_BASE_URL = 'http://127.0.0.1:8765';
const REQUEST_TIMEOUT_MS = 2500;

async function fetchWithTimeout(url: string, options: RequestInit = {}): Promise<Response> {
  const controller = new AbortController();
  const timeoutId = setTimeout(() => controller.abort(), REQUEST_TIMEOUT_MS);

  try {
    const response = await fetch(url, {
      ...options,
      signal: controller.signal,
    });
    return response;
  } finally {
    clearTimeout(timeoutId);
  }
}

export class SystmBridgeService {
  /**
   * Ping the local Systm background mesh daemon
   */
  static async checkHealth(): Promise<{ isConnected: boolean; node?: MeshNodeStatus }> {
    try {
      const res = await fetchWithTimeout(`${BRIDGE_BASE_URL}/api/status`);
      if (!res.ok) return { isConnected: false };
      const data = await res.json();
      return {
        isConnected: true,
        node: data,
      };
    } catch {
      return { isConnected: false };
    }
  }

  /**
   * Fetch live discovered physical BLE & Wi-Fi mesh peers
   */
  static async getPeers(): Promise<MeshDiscoveredPeer[]> {
    try {
      const res = await fetchWithTimeout(`${BRIDGE_BASE_URL}/api/peers`);
      if (!res.ok) return [];
      const data = await res.json();
      return data.peers || [];
    } catch {
      return [];
    }
  }

  /**
   * Transmit a decentralized payment packet across the radio mesh
   */
  static async sendPayment(params: {
    recipientNodeId: number | string;
    recipientName: string;
    amount: number;
  }): Promise<{ success: boolean; txId?: string; error?: string }> {
    try {
      const res = await fetchWithTimeout(`${BRIDGE_BASE_URL}/api/pay`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(params),
      });

      if (!res.ok) {
        return { success: false, error: `HTTP ${res.status}` };
      }

      const data = await res.json();
      return {
        success: data.success ?? true,
        txId: data.txId,
      };
    } catch (e: any) {
      return {
        success: false,
        error: e.message || 'Mesh daemon unreachable',
      };
    }
  }

  /**
   * Poll for incoming payment packets received by the Systm radio mesh
   */
  static async pollIncomingEvents(): Promise<IncomingPaymentEvent[]> {
    try {
      const res = await fetchWithTimeout(`${BRIDGE_BASE_URL}/api/events`);
      if (!res.ok) return [];
      const data = await res.json();
      return data.events || [];
    } catch {
      return [];
    }
  }
}
