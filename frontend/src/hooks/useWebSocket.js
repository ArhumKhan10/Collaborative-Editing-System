/**
 * WebSocket Hook
 * 
 * Custom React hook for managing WebSocket connections to document collaboration.
 * Handles automatic connection, reconnection, message broadcasting, and cleanup.
 * Supports role-based permissions (edit/view).
 * 
 * @module useWebSocket
 */

import { useState, useEffect, useCallback, useRef } from 'react'
import SockJS from 'sockjs-client'
import { Client } from '@stomp/stompjs'

export const useWebSocket = (documentId, userId, username, onMessage, permission = 'edit') => {
  const [connected, setConnected] = useState(false)
  const clientRef = useRef(null)

  useEffect(() => {
    if (!documentId || !userId) return

    // Create WebSocket connection
    const socket = new SockJS('http://localhost:8082/ws')
    const stompClient = new Client({
      webSocketFactory: () => socket,
      debug: () => {
        // Production: Disable STOMP debug logging
      },
      reconnectDelay: 5000,
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000,
    })

    // On connect
    stompClient.onConnect = () => {
      setConnected(true)

      // Subscribe to document topic
      stompClient.subscribe(`/topic/document/${documentId}`, (message) => {
        const data = JSON.parse(message.body)
        onMessage(data)
      })

      // Send join message with permission
      stompClient.publish({
        destination: `/app/document/${documentId}/join`,
        body: JSON.stringify({
          documentId,
          userId,
          username,
          permission,
        }),
      })
    }

    // On disconnect
    stompClient.onDisconnect = () => {
      setConnected(false)
    }

    // On error
    stompClient.onStompError = (frame) => {
      console.error('STOMP error:', frame)
    }

    // Activate connection
    stompClient.activate()
    clientRef.current = stompClient

    // Cleanup on unmount
    return () => {
      if (clientRef.current) {
        // Send leave message
        if (clientRef.current.connected) {
          clientRef.current.publish({
            destination: `/app/document/${documentId}/leave`,
            body: JSON.stringify({
              documentId,
              userId,
              username,
            }),
          })
        }
        clientRef.current.deactivate()
      }
    }
  }, [documentId, userId, username, onMessage, permission])

  const sendMessage = useCallback((content) => {
    if (clientRef.current && clientRef.current.connected) {
      clientRef.current.publish({
        destination: `/app/document/${documentId}/edit`,
        body: JSON.stringify({
          type: 'content-change',
          documentId,
          userId,
          username,
          content,
          timestamp: new Date().toISOString(),
        }),
      })
    }
  }, [documentId, userId, username])

  return { connected, sendMessage }
}
