# Notas de Diseño: Device, BLE, Edge y API

Idiomas disponibles:
- [Español](./device-ble-edge-design.es.md)
- [English](./device-ble-edge-design.md)

## Objetivo

Este documento resume lo descubierto, entendido y decidido hasta ahora sobre el contexto `device/`, el flujo con BLE, la futura integración con un servidor edge y las implicancias sobre este backend API.

La idea es dejar una base clara para seguir avanzando sin rediscutir los mismos conceptos en cada cambio.

## Contexto general

En `uFlex`, un `Device` representa un **kit IoT físico completo**, no cada una de sus piezas internas por separado.

Ejemplos de partes internas del kit:

- ESP32
- MPU9250
- motor vibrador
- LED RGB
- batería

Por ahora, esas partes **no deben modelarse como entidades separadas dentro del dominio** salvo que en el futuro exista una necesidad real de:

- trazabilidad por componente
- reemplazo por componente
- mantenimiento por componente
- firmware independiente por componente

En esta etapa, el agregado `Device` debe seguir representando el **kit como unidad operativa**, asignable a una clínica y eventualmente a un paciente.

## Arquitectura objetivo del flujo

El flujo real que se quiere soportar es:

1. `embedded app -> mobile app` por BLE
2. `embedded app -> edge server`
3. `edge server -> backend API` (este proyecto)

Esto implica que existen al menos dos fronteras técnicas distintas:

1. autenticación entre dispositivo embebido y edge
2. autenticación entre edge y backend API

No conviene resolver ambas con un único mecanismo ni asumir que solo un actor autentica a todos.

## Qué es BLE en este contexto

BLE no es solo "mandar datos por bluetooth". Tiene al menos estas piezas relevantes:

- `advertising`: anuncios cortos que el dispositivo emite para que la app lo descubra
- `connection`: la app se conecta al kit
- `service`: agrupación lógica de funcionalidades o datos
- `characteristic`: dato puntual que la app puede leer, escribir o al que puede suscribirse

### Interpretación práctica para uFlex

- El kit debe poder ser descubierto por la app móvil
- La app debe poder conectarse al kit
- La app debe poder leer datos de identificación del kit
- La app debe poder recibir datos del sensor en tiempo real

## Descubrimiento BLE vs identificación del kit

Una distinción importante:

- una cosa es **descubrir** el dispositivo por BLE
- otra es **identificarlo con certeza** una vez conectado

### Descubrimiento

Se hace a través del nombre BLE visible o de datos anunciados por advertising.

Ejemplo:

- `UFLEX-DEV-0012`

### Identificación

Se hace leyendo una o más characteristics tras conectarse.

Ejemplo:

- characteristic `serialNumber`
- characteristic `firmwareVersion`
- opcionalmente characteristic `deviceId`

## Sobre el serialNumber

Se decidió que el `serialNumber` es un identificador válido, adecuado y útil para el kit.

Ejemplo de nomenclatura:

- `UFLEX-DEV-0012`

### Rol del serialNumber

El `serialNumber` debe ser:

- único
- estable
- legible para humanos
- visible en el kit
- útil para soporte, inventario, depuración y onboarding

### Conclusiones sobre serialNumber

- sí, puede ser el identificador visible del kit
- sí, puede usarse en BLE
- sí, puede usarse como nombre BLE visible
- sí, conviene exponerlo también como characteristic BLE

### Importante

Aunque el `serialNumber` puede usarse en BLE, no debe cargarse con todos los roles del sistema si luego eso complica la evolución del producto.

## Sobre el deviceId

Este backend ya tiene un `deviceId` interno basado en UUIDv7.

### Rol del deviceId

El `deviceId` debe entenderse como:

- identidad técnica interna del backend
- identidad útil para integraciones sistema-a-sistema
- identidad razonable para edge y procesos internos

### Conclusiones sobre deviceId

- no es demasiado pesado para backend ni edge
- tampoco es problemático por tamaño para BLE si se lee ocasionalmente
- no conviene mandarlo repetidamente en cada paquete de streaming en tiempo real
- no es ideal como identificador humano o visual

## Sobre la macAddress

La `macAddress` no es un dato que el negocio deba inventar manualmente. Normalmente proviene del hardware/radio del dispositivo.

### Conclusiones sobre macAddress

- no debe ser el identificador principal del negocio
- no debe ser la única ancla de identidad del kit
- sí puede mantenerse como dato técnico
- sí es útil para debugging, soporte y correlación técnica

### Decisión actual

Por ahora, **conviene mantener `macAddress` en el modelo**, pero como dato técnico secundario, no como centro del diseño.

## Sobre identidad y payload de tiempo real

Se aclaró que para mover un avatar en pantalla la app móvil recibirá datos del MPU9250 por BLE en tiempo real, por lo que la latencia importa.

### Decisión importante

Ni `serialNumber` ni `deviceId` deben enviarse en cada paquete de streaming.

### Uso correcto

- `serialNumber` y/o `deviceId` se usan para identificar el kit al inicio
- el streaming continuo debe enviar solo datos del sensor

Ejemplos de datos de streaming:

- quaternions
- ángulos
- aceleración
- timestamps
- sequence numbers
- flags de estado

## BLE name vs characteristic

Se aclaró la diferencia:

- `BLE name`: nombre visible cuando la app está escaneando
- `characteristic`: dato legible una vez conectado

### Decisión actual

Se puede usar perfectamente el `serialNumber` como:

- nombre BLE visible
- valor de una characteristic de identificación

También pueden exponerse otras characteristics como:

- `firmwareVersion`
- `model`
- `batteryLevel`
- stream del sensor

## Dónde vive el serialNumber en el kit

Se aclaró que el `serialNumber` debe vivir en el kit si la app móvil o el edge lo van a leer desde el dispositivo.

### Opciones técnicas

1. grabarlo en firmware
2. guardarlo en NVS

### Diferencia

- **firmware**: el valor va embebido en el binario que se flashea
- **NVS**: el firmware es genérico y lee el valor desde almacenamiento persistente del ESP32

### Decisión práctica

Para prototipo:

- se puede grabar en firmware

Para producto más serio:

- conviene usar NVS o un mecanismo de provisioning

## Estado actual del agregado Device

El agregado `Device` actual representa:

- identidad interna (`id`)
- identidad visible (`serialNumber`)
- dato técnico (`macAddress`)
- versión de firmware
- batería
- modelo
- estado de calibración
- estado operativo
- último sync
- clínica
- paciente asignado actual

### Evaluación del modelo actual

El modelo actual:

- sí es viable para empezar
- sí permite un MVP funcional
- no está mal diseñado
- no bloquea la integración con BLE
- no bloquea una futura integración con edge

Pero también:

- no es necesariamente el diseño final
- aún no modela provisioning real
- aún no modela autenticación máquina-a-máquina
- aún no modela credenciales del dispositivo

## Evaluación del POST y GET actuales

### POST actual

Ejemplo actual:

```json
{
  "serialNumber": "UFLEX-DEV-0012",
  "macAddress": "AA:BB:CC:DD:EE:FD",
  "firmwareVersion": "1.0.0",
  "model": "UFlex Tracker Pro"
}
```

### Veredicto

Como endpoint técnico de debug o carga manual interna:

- sí, está bien
- sí, sirve para esta etapa

Como flujo final de negocio para clinic admin:

- no es lo ideal

### Decisión actual

El `POST` puede quedarse por ahora como herramienta interna de pruebas. La idea a futuro es que el clinic admin no cree el device manualmente.

### GET actual

El `GET` actual es suficientemente bueno para una primera etapa porque expone:

- identidad
- estado operativo
- batería
- firmware
- asignación actual
- información de disponibilidad

### Veredicto

- sí, el `GET` actual está bien para MVP
- puede madurar después, pero no necesita rediseño urgente

## Flujo de alta futuro del dispositivo

No se quiere que el clinic admin registre el dispositivo inventando manualmente todos sus datos.

### Opciones futuras razonables

1. uFlex pre-registra los kits
2. el kit se provisiona automáticamente
3. el clinic admin hace claim de un kit ya existente por serial/QR/código

### Decisión actual

Se asume que el `POST` actual es transitorio y útil para debug. El flujo definitivo de alta se definirá después.

## Rutas API: serialNumber vs deviceId

Se discutió si conviene usar `serialNumber` o `deviceId` en los paths.

### Conclusiones

- el mundo físico y BLE puede usar `serialNumber`
- el backend idealmente puede operar por `deviceId`

### Pero en esta etapa

No es obligatorio migrar ya todo a `deviceId`.

#### Visión arquitectónica

- `serialNumber`: sirve para encontrar o resolver el kit físico
- `deviceId`: sirve como identidad interna del recurso REST

#### Visión pragmática actual

Mantener `serialNumber` en los paths por ahora no es un error grave si el dominio aún está madurando.

## Qué atributos podrían aparecer más adelante

Estos campos se identificaron como posibles mejoras futuras, no como obligatorios inmediatos:

- `hardwareRevision`
- `provisioningStatus`
- `lastSeenAt`

### hardwareRevision

Sirve si el kit "UFlex Tracker Pro" existe en varias revisiones de hardware con diferencias reales.

### provisioningStatus

Sirve cuando exista un flujo real de aprovisionamiento del kit.

### lastSeenAt

Podría ser semántica más amplia que `lastSyncAt` si luego hay edge, heartbeats y varias fuentes de actividad.

### Campo que no parece prioritario ahora

- `advertisedName`

Si el nombre BLE siempre se deriva del serial, probablemente no hace falta persistirlo en el dominio por ahora.

## Qué no hace falta modelar aún

Por ahora no hace falta agregar al agregado `Device`:

- ESP32 como entidad
- MPU9250 como entidad
- motor vibrador como entidad
- LED RGB como entidad
- batería como entidad separada

Tampoco hace falta modelar todavía:

- credenciales avanzadas dentro del aggregate como campos planos
- flujo completo de provisioning
- integración final de edge

## Revisión del proyecto edge de clase

Se revisó el proyecto `smart-band-edge-service-master-master/`.

### Hallazgos principales

Ese proyecto autentica requests con:

- `device_id`
- `X-API-Key`

Es decir:

- identidad del dispositivo
- más un secreto compartido

### Lo que eso confirma para uFlex

Para edge no basta con saber "qué dispositivo es". También hace falta saber "cómo prueba que realmente es ese dispositivo".

## Autenticación en la arquitectura real

Se concluyó que la autenticación debe existir en dos capas distintas.

### 1. embedded app -> edge

El edge debe autenticar al dispositivo embebido.

Eso implica que el dispositivo necesitará:

- una identidad (`serialNumber`, `deviceId` o ambos)
- una credencial (`apiKey`, `deviceSecret`, etc.)

### 2. edge -> backend API

El backend debe autenticar al edge como actor técnico.

Eso implica que el edge necesitará su propia credencial frente al backend.

Ejemplos:

- `X-Edge-Api-Key`
- token técnico tipo Bearer

## Debe existir autenticación del device en el backend principal

No necesariamente de forma directa desde el primer día.

Hay dos modelos posibles:

### Modelo A

El backend solo confía en el edge:

- el embedded se autentica con el edge
- el edge autentica al embedded
- el edge se autentica con el backend
- el backend recibe la identidad del device reenviada por el edge

### Modelo B

El backend también modela credenciales propias del device y eventualmente podría validarlas o gestionarlas.

### Decisión conceptual actual

Aunque el backend no autentique hoy directamente al embedded, **sí conviene prever que el dispositivo tendrá credenciales técnicas**.

## Deben vivir credenciales en el dominio del backend API

La conclusión fue:

- sí conviene que el backend tenga algún modelo de credenciales del dispositivo
- no hace falta meter esas credenciales como campos planos dentro del aggregate `Device`

### Mejor enfoque a futuro

Mantener separado:

- `Device`: inventario, estado, asignación, firmware, clínica
- `DeviceCredential` o equivalente: autenticación técnica del dispositivo

Eso mantiene el dominio más limpio.

## Debe vivir deviceId dentro del embedded

No necesariamente desde el día 1.

### Opciones identificadas

1. el embedded solo conoce `serialNumber`
2. el embedded aprende y guarda `deviceId` luego de un provisioning o claim
3. el embedded nace ya provisionado con `deviceId` y secreto

### Decisión pragmática actual

No es obligatorio que el ESP32 conozca el `deviceId` desde la primera versión si ya puede trabajar con `serialNumber`.

## Resumen ejecutivo de decisiones actuales

### Decisiones fuertes

- `Device` representa el kit IoT completo
- `serialNumber` es válido y adecuado como identidad visible
- `serialNumber` puede usarse en BLE
- `serialNumber` puede ser el nombre BLE visible
- `serialNumber` conviene exponerlo también como characteristic BLE
- `deviceId` sigue siendo la identidad interna fuerte del backend
- `macAddress` se mantiene por ahora como dato técnico secundario
- el stream BLE en tiempo real no debe incluir identificadores repetidos
- el `POST` actual sirve como debug interno, no como flujo final para clinic admin
- el modelo actual de `Device` sirve para MVP

### Decisiones de arquitectura

- habrá autenticación `embedded -> edge`
- habrá autenticación `edge -> backend API`
- para edge se necesitará identidad del device más una credencial
- el backend debería prever un modelo futuro de credenciales de dispositivo

### Cosas que no urgen cambiar ahora

- no hace falta rediseñar por completo el agregado `Device`
- no hace falta eliminar `macAddress`
- no hace falta modelar componentes internos del kit
- no hace falta migrar inmediatamente todos los endpoints a `deviceId`

## Estado final de la conclusión

La API y el agregado `Device` actuales están **lo suficientemente bien para comenzar un flujo MVP realista** con:

- inventario de kits
- asignación a clínica/paciente
- descubrimiento e identificación BLE
- telemetría básica
- futura integración con edge

Sin embargo, para una versión más madura del producto, más adelante hará falta:

- provisioning real de kits
- contrato BLE formal
- modelo de credenciales de dispositivo
- autenticación formal del edge frente al backend
- definición del flujo definitivo de alta/claim de dispositivos

