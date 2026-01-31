import { Client, IMessage, StompSubscription } from '@stomp/stompjs';
import SockJS from 'sockjs-client';

/**
 * WebSocket service for managing real-time communication with the backend.
 * Uses SockJS for fallback support and STOMP protocol for messaging.
 */
export class WebSocketService {
  private client: Client | null = null;
  private subscriptions: Map<string, StompSubscription> = new Map();
  private reconnectAttempts = 0;
  private maxReconnectAttempts = 10;
  private reconnectDelay = 1000; // Start with 1 second
  private maxReconnectDelay = 30000; // Max 30 seconds
  private isConnecting = false;
  private isManualDisconnect = false;
  private draftUuid: string | null = null;

  /**
   * Connect to the WebSocket server
   * @param draftUuid The UUID of the draft to connect to
   * @returns Promise that resolves when connected
   */
  connect(draftUuid: string): Promise<void> {
    return new Promise((resolve, reject) => {
      if (this.client?.connected) {
        console.log('WebSocket already connected');
        this.draftUuid = draftUuid; // Update draft UUID
        resolve();
        return;
      }

      if (this.isConnecting) {
        console.log('WebSocket connection already in progress, waiting...');
        // Wait for the current connection attempt to complete
        const checkConnection = setInterval(() => {
          if (this.client?.connected) {
            clearInterval(checkConnection);
            this.draftUuid = draftUuid;
            resolve();
          } else if (!this.isConnecting) {
            clearInterval(checkConnection);
            reject(new Error('Connection attempt failed'));
          }
        }, 100);
        return;
      }

      this.isConnecting = true;
      this.isManualDisconnect = false;
      this.draftUuid = draftUuid;

      // Determine WebSocket URL based on environment
      const wsUrl = this.getWebSocketUrl();

      // Create STOMP client with SockJS
      this.client = new Client({
        webSocketFactory: () => new SockJS(wsUrl) as any,
        debug: (str) => {
          console.log('STOMP Debug:', str);
        },
        reconnectDelay: 0, // We handle reconnection manually
        heartbeatIncoming: 4000,
        heartbeatOutgoing: 4000,
        onConnect: () => {
          console.log('WebSocket connected successfully');
          this.isConnecting = false;
          this.reconnectAttempts = 0;
          this.reconnectDelay = 1000;
          resolve();
        },
        onStompError: (frame) => {
          console.error('STOMP error:', frame);
          this.isConnecting = false;
          reject(new Error(`STOMP error: ${frame.headers['message']}`));
        },
        onWebSocketError: (event) => {
          console.error('WebSocket error:', event);
          this.isConnecting = false;
          reject(new Error('WebSocket connection error'));
        },
        onDisconnect: () => {
          console.log('WebSocket disconnected');
          this.isConnecting = false;
          
          // Attempt reconnection if not manually disconnected
          if (!this.isManualDisconnect) {
            this.attemptReconnect();
          }
        },
      });

      // Activate the client
      this.client.activate();
    });
  }

  /**
   * Disconnect from the WebSocket server
   */
  disconnect(): void {
    this.isManualDisconnect = true;
    
    // Unsubscribe from all topics
    this.subscriptions.forEach((subscription) => {
      subscription.unsubscribe();
    });
    this.subscriptions.clear();

    // Deactivate the client
    if (this.client) {
      this.client.deactivate();
      this.client = null;
    }

    this.draftUuid = null;
    this.reconnectAttempts = 0;
  }

  /**
   * Subscribe to a topic
   * @param topic The topic to subscribe to (e.g., '/topic/draft/{uuid}')
   * @param callback Function to call when a message is received
   * @returns Subscription ID
   */
  subscribe(topic: string, callback: (message: any) => void): string {
    if (!this.client?.connected) {
      throw new Error('WebSocket not connected. Call connect() first.');
    }

    // Check if already subscribed
    if (this.subscriptions.has(topic)) {
      console.warn(`Already subscribed to ${topic}`);
      return topic;
    }

    const subscription = this.client.subscribe(topic, (message: IMessage) => {
      try {
        const parsedMessage = JSON.parse(message.body);
        callback(parsedMessage);
      } catch (error) {
        console.error('Error parsing message:', error);
      }
    });

    this.subscriptions.set(topic, subscription);
    console.log(`Subscribed to ${topic}`);
    return topic;
  }

  /**
   * Unsubscribe from a topic
   * @param topic The topic to unsubscribe from
   */
  unsubscribe(topic: string): void {
    const subscription = this.subscriptions.get(topic);
    if (subscription) {
      subscription.unsubscribe();
      this.subscriptions.delete(topic);
      console.log(`Unsubscribed from ${topic}`);
    }
  }

  /**
   * Send a message to a destination
   * @param destination The destination to send to (e.g., '/app/draft/{uuid}/pick')
   * @param message The message object to send
   */
  send(destination: string, message: any): void {
    if (!this.client?.connected) {
      throw new Error('WebSocket not connected. Cannot send message.');
    }

    this.client.publish({
      destination,
      body: JSON.stringify(message),
    });

    console.log(`Sent message to ${destination}:`, message);
  }

  /**
   * Check if the WebSocket is connected
   */
  isConnected(): boolean {
    return this.client?.connected ?? false;
  }

  /**
   * Get the WebSocket URL based on environment
   */
  private getWebSocketUrl(): string {
    // Check if we're in development or production
    const protocol = window.location.protocol === 'https:' ? 'https:' : 'http:';
    const host = window.location.hostname;
    
    // In development, connect to localhost:8080
    // In production, connect to the same host
    if (host === 'localhost' || host === '127.0.0.1') {
      return `${protocol}//localhost:8080/ws`;
    } else {
      return `${protocol}//${host}/ws`;
    }
  }

  /**
   * Attempt to reconnect with exponential backoff
   */
  private attemptReconnect(): void {
    if (this.reconnectAttempts >= this.maxReconnectAttempts) {
      console.error('Max reconnection attempts reached. Giving up.');
      return;
    }

    if (!this.draftUuid) {
      console.error('Cannot reconnect: no draft UUID stored');
      return;
    }

    this.reconnectAttempts++;
    const delay = Math.min(
      this.reconnectDelay * Math.pow(2, this.reconnectAttempts - 1),
      this.maxReconnectDelay
    );

    console.log(
      `Attempting to reconnect (${this.reconnectAttempts}/${this.maxReconnectAttempts}) in ${delay}ms...`
    );

    setTimeout(() => {
      if (!this.isManualDisconnect && this.draftUuid) {
        this.connect(this.draftUuid)
          .then(() => {
            console.log('Reconnected successfully');
            // Re-subscribe to all previous topics
            // Note: Subscribers will need to re-subscribe after reconnection
          })
          .catch((error) => {
            console.error('Reconnection failed:', error);
          });
      }
    }, delay);
  }
}

// Export a singleton instance
export const webSocketService = new WebSocketService();
