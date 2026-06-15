# Design Notes: Device, BLE, Edge, and API

Available languages:
- [Español](./device-ble-edge-design.es.md)
- [English](./device-ble-edge-design.md)

## Goal

This document summarizes what has been discovered, understood, and decided so far about the `device` context, the BLE flow, the future integration with an edge server, and the implications for this backend API.

The goal is to keep a clear baseline so the team can continue moving forward without re-debating the same concepts every time.

## General context

In `uFlex`, a `Device` represents a **complete physical IoT kit**, not each of its internal parts separately.

Examples of internal kit parts:

- ESP32
- MPU9250
- vibration motor
- RGB LED
- battery

For now, these parts **should not be modeled as separate entities in the domain** unless there is a real future need for:

- component-level traceability
- component replacement
- component-level maintenance
- independent firmware per component

At this stage, the `Device` aggregate should continue to represent the **kit as an operational unit**, assignable to a clinic and eventually to a patient.

## Target architecture for the flow

The real flow the system is expected to support is:

1. `embedded app -> mobile app` through BLE
2. `embedded app -> edge server`
3. `edge server -> backend API` (this project)

This means there are at least two distinct technical boundaries:

1. authentication between the embedded device and the edge
2. authentication between the edge and the backend API

Both should not be solved with a single mechanism, and the design should not assume that only one actor authenticates everyone.

## What BLE means in this context

BLE is not just "sending data over Bluetooth". It has at least these relevant pieces:

- `advertising`: short broadcasts emitted by the device so the app can discover it
- `connection`: the app connects to the kit
- `service`: logical grouping of capabilities or data
- `characteristic`: a specific piece of data that the app can read, write, or subscribe to

### Practical interpretation for uFlex

- The kit must be discoverable by the mobile app
- The app must be able to connect to the kit
- The app must be able to read kit identification data
- The app must be able to receive sensor data in real time

## BLE discovery vs kit identification

An important distinction:

- one thing is to **discover** the device through BLE
- another thing is to **identify it with certainty** once connected

### Discovery

This happens through the visible BLE name or through advertised data.

Example:

- `UFLEX-DEV-0012`

### Identification

This happens by reading one or more characteristics after connecting.

Example:

- `serialNumber` characteristic
- `firmwareVersion` characteristic
- optionally `deviceId` characteristic

## About the serialNumber

It was decided that `serialNumber` is a valid, appropriate, and useful identifier for the kit.

Example naming scheme:

- `UFLEX-DEV-0012`

### Role of the serialNumber

The `serialNumber` should be:

- unique
- stable
- human-readable
- visible on the kit
- useful for support, inventory, debugging, and onboarding

### Conclusions about serialNumber

- yes, it can be the visible identifier of the kit
- yes, it can be used in BLE
- yes, it can be used as the visible BLE name
- yes, it should also be exposed as a BLE characteristic

### Important note

Although `serialNumber` can be used in BLE, it should not carry every role in the system if doing so makes the product harder to evolve.

## About the deviceId

This backend already has an internal `deviceId` based on UUIDv7.

### Role of the deviceId

The `deviceId` should be understood as:

- the backend's internal technical identity
- a useful identity for system-to-system integrations
- a reasonable identity for edge and internal processes

### Conclusions about deviceId

- it is not too heavy for backend or edge
- it is also not problematic in BLE from a size standpoint if read occasionally
- it should not be sent repeatedly in every real-time streaming packet
- it is not ideal as a human-facing or visual identifier

## About the macAddress

The `macAddress` is not a business value that should be invented manually. It normally comes from the device hardware/radio.

### Conclusions about macAddress

- it should not be the primary business identifier
- it should not be the only identity anchor for the kit
- it can remain as technical data
- it is useful for debugging, support, and technical correlation

### Current decision

For now, it is still useful to **keep `macAddress` in the model**, but as secondary technical data, not as the center of the design.

## About identity and real-time payloads

It was clarified that, to move an avatar on screen, the mobile app will receive MPU9250 data over BLE in real time, so latency matters.

### Important decision

Neither `serialNumber` nor `deviceId` should be sent in every streaming packet.

### Correct usage

- `serialNumber` and/or `deviceId` are used to identify the kit at the beginning
- continuous streaming should send only sensor data

Examples of streaming data:

- quaternions
- angles
- acceleration
- timestamps
- sequence numbers
- status flags

## BLE name vs characteristic

The distinction was clarified as follows:

- `BLE name`: visible name while the app is scanning
- `characteristic`: data readable once connected

### Current decision

It is perfectly valid to use `serialNumber` as:

- the visible BLE name
- the value of an identification characteristic

Other characteristics can also be exposed, such as:

- `firmwareVersion`
- `model`
- `batteryLevel`
- sensor stream

## Where the serialNumber lives in the kit

It was clarified that `serialNumber` must live in the kit if the mobile app or the edge will read it directly from the device.

### Technical options

1. store it in firmware
2. store it in NVS

### Difference

- **firmware**: the value is embedded in the flashed binary
- **NVS**: the firmware is generic and reads the value from persistent ESP32 storage

### Practical decision

For a prototype:

- storing it in firmware is acceptable

For a more serious product:

- NVS or a provisioning mechanism is better

## Current state of the Device aggregate

The current `Device` aggregate represents:

- internal identity (`id`)
- visible identity (`serialNumber`)
- technical data (`macAddress`)
- firmware version
- battery
- model
- calibration state
- operational state
- last sync
- clinic
- current assigned patient

### Evaluation of the current model

The current model:

- is viable to start with
- supports a functional MVP
- is not badly designed
- does not block BLE integration
- does not block future edge integration

But it is also true that:

- it is not necessarily the final design
- it does not yet model real provisioning
- it does not yet model machine-to-machine authentication
- it does not yet model device credentials

## Evaluation of the current POST and GET

### Current POST

Current example:

```json
{
  "serialNumber": "UFLEX-DEV-0012",
  "macAddress": "AA:BB:CC:DD:EE:FD",
  "firmwareVersion": "1.0.0",
  "model": "UFlex Tracker Pro"
}
```

### Verdict

As a technical endpoint for debugging or internal manual loading:

- yes, it is acceptable
- yes, it works for this stage

As the final business flow for a clinic admin:

- it is not ideal

### Current decision

The `POST` can remain for now as an internal testing tool. The long-term idea is that a clinic admin should not manually create the device.

### Current GET

The current `GET` is good enough for an early stage because it exposes:

- identity
- operational status
- battery
- firmware
- current assignment
- availability information

### Verdict

- yes, the current `GET` is fine for an MVP
- it can evolve later, but it does not need an urgent redesign

## Future device enrollment flow

The product should not require a clinic admin to register a device by inventing all of its data manually.

### Reasonable future options

1. uFlex pre-registers kits
2. the kit provisions itself automatically
3. the clinic admin claims an existing kit through serial/QR/code

### Current decision

The current `POST` is assumed to be temporary and useful for debugging. The final enrollment flow will be defined later.

## API routes: serialNumber vs deviceId

There was discussion about whether to use `serialNumber` or `deviceId` in paths.

### Conclusions

- the physical world and BLE can use `serialNumber`
- the backend can ideally operate using `deviceId`

### But at this stage

It is not mandatory to migrate everything to `deviceId` immediately.

#### Architectural view

- `serialNumber`: useful to find or resolve the physical kit
- `deviceId`: useful as the internal identity of the REST resource

#### Pragmatic view for now

Keeping `serialNumber` in paths is not a serious mistake if the domain is still maturing.

## Attributes that may appear later

These fields were identified as possible future improvements, not as immediate requirements:

- `hardwareRevision`
- `provisioningStatus`
- `lastSeenAt`

### hardwareRevision

Useful if the "UFlex Tracker Pro" kit exists in multiple hardware revisions with real differences.

### provisioningStatus

Useful once a real device provisioning flow exists.

### lastSeenAt

Could be broader in meaning than `lastSyncAt` if edge, heartbeats, and multiple activity sources are introduced later.

### Field that does not look urgent now

- `advertisedName`

If the BLE name is always derived from the serial, persisting it in the domain may not be necessary yet.

## What does not need to be modeled yet

For now, there is no need to add these to the `Device` aggregate:

- ESP32 as an entity
- MPU9250 as an entity
- vibration motor as an entity
- RGB LED as an entity
- battery as a separate entity

There is also no need yet to model:

- advanced credentials as flat fields inside the aggregate
- the complete provisioning flow
- the final edge integration

## Review of the class edge project

The project `smart-band-edge-service-master-master/` was reviewed.

### Main findings

That project authenticates requests with:

- `device_id`
- `X-API-Key`

That means:

- device identity
- plus a shared secret

### What that confirms for uFlex

For edge, it is not enough to know "which device this is". It is also necessary to know "how it proves it really is that device".

## Authentication in the real architecture

It was concluded that authentication must exist in two different layers.

### 1. embedded app -> edge

The edge must authenticate the embedded device.

That implies the device will need:

- an identity (`serialNumber`, `deviceId`, or both)
- a credential (`apiKey`, `deviceSecret`, etc.)

### 2. edge -> backend API

The backend must authenticate the edge as a technical actor.

That implies the edge will need its own credential against the backend.

Examples:

- `X-Edge-Api-Key`
- technical Bearer token

## Should device authentication exist in the main backend

Not necessarily in a direct way from day one.

There are two possible models:

### Model A

The backend trusts only the edge:

- the embedded authenticates with the edge
- the edge authenticates the embedded
- the edge authenticates with the backend
- the backend receives the device identity as forwarded data from the edge

### Model B

The backend also models device-specific credentials and could eventually validate or manage them.

### Current conceptual decision

Even if the backend does not directly authenticate the embedded device today, **it is still wise to anticipate that the device will have technical credentials**.

## Should credentials live in the backend API domain

The conclusion was:

- yes, it is useful for the backend to have some model of device credentials
- no, those credentials do not need to be stored as flat fields inside the `Device` aggregate

### Better future approach

Keep these separate:

- `Device`: inventory, status, assignment, firmware, clinic
- `DeviceCredential` or equivalent: technical authentication of the device

That keeps the domain cleaner.

## Should deviceId live inside the embedded device

Not necessarily from day one.

### Identified options

1. the embedded knows only the `serialNumber`
2. the embedded learns and stores the `deviceId` after provisioning or claim
3. the embedded is born already provisioned with `deviceId` and secret

### Current pragmatic decision

It is not mandatory for the ESP32 to know the `deviceId` in the first version if it can already work with `serialNumber`.

## Executive summary of current decisions

### Strong decisions

- `Device` represents the complete IoT kit
- `serialNumber` is valid and appropriate as the visible identity
- `serialNumber` can be used in BLE
- `serialNumber` can be the visible BLE name
- `serialNumber` should also be exposed as a BLE characteristic
- `deviceId` remains the strong internal backend identity
- `macAddress` stays for now as secondary technical data
- real-time BLE streaming should not include repeated identifiers
- the current `POST` is useful for internal debugging, not as the final clinic admin flow
- the current `Device` model is good enough for an MVP

### Architectural decisions

- there will be `embedded -> edge` authentication
- there will be `edge -> backend API` authentication
- edge will require both device identity and a credential
- the backend should anticipate a future model for device credentials

### Things that are not urgent to change now

- no need to redesign the entire `Device` aggregate
- no need to remove `macAddress`
- no need to model the internal kit components
- no need to immediately migrate every endpoint to `deviceId`

## Final conclusion

The current API and `Device` aggregate are **good enough to start a realistic MVP flow** with:

- kit inventory
- clinic/patient assignment
- BLE discovery and identification
- basic telemetry
- future edge integration

However, for a more mature version of the product, the following will still be needed later:

- real kit provisioning
- a formal BLE contract
- a device credentials model
- formal edge-to-backend authentication
- the final device enrollment/claim flow definition

