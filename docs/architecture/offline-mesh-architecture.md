# Offline Mesh Communication Architecture

## Status

Accepted for prototype implementation.

## Context

The Campus Disaster Response application must continue to exchange
critical emergency information when cellular networks or internet
connectivity are unavailable.

The Android client therefore requires a local peer-to-peer
communication mechanism capable of discovering nearby devices and
transferring lightweight emergency status packets.

The evaluated technologies are:

| Technology | Transport Layer | Strengths | Limitations | Hackathon Suitability |
| --- | --- | --- | --- | --- |
| Google Nearby Connections API | BLE, Bluetooth Classic, Wi-Fi Direct / Wi-Fi Aware | High-level abstraction, discovery, handshakes and encryption handled automatically | Requires Google Play Services; multi-hop routing must be implemented by the application | Optimal |
| Native Wi-Fi Direct | WifiP2pManager | High throughput and independent of Google Play Services | Connection prompts, Group Owner negotiation and complex lifecycle handling | Low |
| Raw BLE / GATT | Bluetooth LE scanner and GATT | Low power usage and broad device compatibility | Small payload sizes and limited simultaneous radio links | Medium |
| Local Network / mDNS / NSD | Existing Wi-Fi infrastructure | Standard TCP or WebSocket communication | Depends on powered campus Wi-Fi infrastructure | Fallback |

## Decision

Use the Google Nearby Connections API with:

    Strategy.P2P_CLUSTER

for the hackathon prototype.

Nearby Connections will provide direct peer discovery and communication
between Android devices without requiring cellular data or internet
access.

## Why Nearby Connections

The hackathon implementation prioritizes:

- reliable discovery across different Android hardware
- rapid implementation
- encrypted peer connections
- reduced Bluetooth / Wi-Fi Direct lifecycle complexity
- offline operation
- lightweight emergency messaging

Native Wi-Fi Direct and raw BLE expose significantly more hardware and
connection-management complexity.

Nearby Connections provides a higher-level abstraction suitable for
rapid prototyping.

## Network Topology

Nearby Connections provides direct communication between connected
peers.

The application will add its own multi-hop relay mechanism above the
Nearby Connections transport.

Conceptually:

    Device A
       |
       v
    Device B
      / \
     v   v
    C     D
          |
          v
          E

A message received by Device B can therefore be rebroadcast to other
connected peers when its TTL permits forwarding.

## Payload Design

Emergency messages should remain small and should be transmitted as
structured JSON encoded into byte payloads.

Suggested packet structure:

```json
{
  "message_id": "uuid",
  "user_id": "user-id",
  "status": "SAFE",
  "location_blueprint_id": "VJTI-MAIN-F2",
  "timestamp": 0,
  "ttl": 5
}
cat << 'EOF' > docs/architecture/offline-mesh-architecture.md
# Offline Mesh Communication Architecture

## Status

Accepted for prototype implementation.

## Context

The Campus Disaster Response application must continue to exchange
critical emergency information when cellular networks or internet
connectivity are unavailable.

The Android client therefore requires a local peer-to-peer
communication mechanism capable of discovering nearby devices and
transferring lightweight emergency status packets.

The evaluated technologies are:

| Technology | Transport Layer | Strengths | Limitations | Hackathon Suitability |
| --- | --- | --- | --- | --- |
| Google Nearby Connections API | BLE, Bluetooth Classic, Wi-Fi Direct / Wi-Fi Aware | High-level abstraction, discovery, handshakes and encryption handled automatically | Requires Google Play Services; multi-hop routing must be implemented by the application | Optimal |
| Native Wi-Fi Direct | WifiP2pManager | High throughput and independent of Google Play Services | Connection prompts, Group Owner negotiation and complex lifecycle handling | Low |
| Raw BLE / GATT | Bluetooth LE scanner and GATT | Low power usage and broad device compatibility | Small payload sizes and limited simultaneous radio links | Medium |
| Local Network / mDNS / NSD | Existing Wi-Fi infrastructure | Standard TCP or WebSocket communication | Depends on powered campus Wi-Fi infrastructure | Fallback |

## Decision

Use the Google Nearby Connections API with:

    Strategy.P2P_CLUSTER

for the hackathon prototype.

Nearby Connections will provide direct peer discovery and communication
between Android devices without requiring cellular data or internet
access.

## Why Nearby Connections

The hackathon implementation prioritizes:

- reliable discovery across different Android hardware
- rapid implementation
- encrypted peer connections
- reduced Bluetooth / Wi-Fi Direct lifecycle complexity
- offline operation
- lightweight emergency messaging

Native Wi-Fi Direct and raw BLE expose significantly more hardware and
connection-management complexity.

Nearby Connections provides a higher-level abstraction suitable for
rapid prototyping.

## Network Topology

Nearby Connections provides direct communication between connected
peers.

The application will add its own multi-hop relay mechanism above the
Nearby Connections transport.

Conceptually:

    Device A
       |
       v
    Device B
      / \
     v   v
    C     D
          |
          v
          E

A message received by Device B can therefore be rebroadcast to other
connected peers when its TTL permits forwarding.

## Payload Design

Emergency messages should remain small and should be transmitted as
structured JSON encoded into byte payloads.

Suggested packet structure:

```json
{
  "message_id": "uuid",
  "user_id": "user-id",
  "status": "SAFE",
  "location_blueprint_id": "VJTI-MAIN-F2",
  "timestamp": 0,
  "ttl": 5
}
