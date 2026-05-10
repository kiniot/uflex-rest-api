## 4.2. Tactical-Level Domain-Driven Design

En esta sección se aborda la perspectiva táctica del enfoque Domain-Driven Design (DDD), la cual se centra en representar el dominio del negocio mediante elementos concretos de software. A partir de los límites definidos en el diseño estratégico, se modelan entidades, objetos de valor, servicios y otros componentes clave que encapsulan las reglas del dominio y responden a las necesidades del sistema. Este nivel permite estructurar la solución de manera más alineada con los procesos y problemáticas reales, garantizando una implementación coherente y sostenible.

Cada bounded context contará con su propio apartado, donde se detallará cómo estos elementos se articulan para gestionar los flujos de aplicación, facilitar la interacción con sistemas externos y contribuir al objetivo central de la solución.

### 4.2.1. Bounded Context: IAM

El bounded context **IAM (Identity and Access Management)** concentra todo lo relacionado con la identidad de los usuarios de uFlex y su rol dentro del ecosistema clínico. Este BC se encarga tanto de la **autenticación** (registro, inicio de sesión, hashing de contraseñas con bcrypt y emisión/validación de JWT propios) como del **perfil enriquecido** del usuario dentro del dominio: su rol clínico (Paciente, Fisioterapeuta o Administrador de Clínica), su clínica asociada y su ciclo de vida (pendiente de verificación, verificado, suspendido). Al ser un monolito, uFlex gestiona internamente sus credenciales y tokens sin depender de un identity provider externo. Los comandos y eventos emitidos por este BC (`SignUpCommand`, `SignInCommand`, `VerifyUserCommand`, `UserCreatedEvent`, `UserVerifiedEvent`) fueron identificados durante el Design-Level EventStorming.

#### 4.2.1.1. Domain Layer

En esta sección se describen los elementos del Domain Layer del contexto de IAM, que son necesarios para modelar la gestión de identidades y accesos dentro de uFlex. Estos componentes definen las reglas de negocio y las invariantes asociadas a la autenticación, autorización y administración de usuarios en la plataforma clínica multi-tenant.

**1. User (Aggregate Root)**

Representa al usuario del sistema, con su identidad, credenciales, roles clínicos y asociación al tenant (clínica). El aggregate guarda el hash de la contraseña (bcrypt) calculado por el monolito; uFlex emite y valida sus propios JWT.

**Atributos principales:**

| Atributo             | Tipo                 | Visibilidad | Descripción                                                                                                        |
|----------------------|----------------------|-------------|--------------------------------------------------------------------------------------------------------------------|
| `id`                 | `UserId`             | private     | Identificador interno del usuario.                                                                                 |
| `emailAddress`       | `EmailAddress`       | private     | Correo del usuario (VO compartido con otros BCs).                                                                  |
| `passwordHash`       | `PasswordHash`       | private     | Hash bcrypt de la contraseña; nunca se expone fuera del aggregate.                                                 |
| `fullName`           | `FullName`           | private     | Nombre completo del usuario.                                                                                       |
| `roles`              | `Set<Role>`          | private     | Conjunto de roles clínicos asignados.                                                                              |
| `verificationStatus` | `VerificationStatus` | private     | Estado de verificación de correo (`NOT_VERIFIED` / `VERIFIED`).                                                    |
| `accountStatus`      | `AccountStatus`      | private     | Estado de la cuenta (`PENDING`, `ACTIVE`, `BLOCKED`, `DELETED`).                                                   |
| `verificationCode`   | `VerificationCode`   | private     | Código y expiración para verificación clínica adicional (por ejemplo, validación por el Administrador de Clínica). |
| `clinicId`           | `ClinicId`           | private     | Identificador de la clínica (tenant) asociada; puede quedar sin asignar hasta el onboarding.                       |

**Métodos principales:**

| Método                                                                         | Tipo Retorno | Visibilidad | Descripción                                                                             |
|--------------------------------------------------------------------------------|--------------|-------------|-----------------------------------------------------------------------------------------|
| `User()`                                                                       | Constructor  | public      | Constructor vacío requerido por JPA.                                                    |
| `User(EmailAddress, PasswordHash, FullName, VerificationCode)`                 | Constructor  | public      | Crea un usuario en estado `PENDING` y `NOT_VERIFIED`, con `clinicId` vacío y sin roles. |
| `User(EmailAddress, PasswordHash, FullName, VerificationCode, List<Role>)`     | Constructor  | public      | Crea usuario e inicializa roles usando `validateRoleSet`.                               |
| `addRole(Role role)`                                                           | `User`       | public      | Agrega un rol al conjunto y valida la coherencia con el tenant.                         |
| `addRoles(List<Role> roles)`                                                   | `User`       | public      | Valida y agrega múltiples roles.                                                        |
| `isVerified()`                                                                 | `boolean`    | public      | Devuelve `true` si `verificationStatus == VERIFIED`.                                    |
| `activate()`                                                                   | `void`       | public      | Cambia `accountStatus` a `ACTIVE` solo si el usuario ya está `VERIFIED`.                |
| `assignVerificationCode(String email, String code, Integer expirationMinutes)` | `void`       | public      | Asigna un nuevo `VerificationCode` y publica `UserVerificationCodeAssignedEvent`.       |
| `verifyUser(String code)`                                                      | `void`       | public      | Valida el código, marca `VERIFIED`, activa la cuenta y limpia el `VerificationCode`.    |
| `associateClinic(ClinicId clinicId)`                                           | `void`       | public      | Asocia un `clinicId` si el usuario aún no tenía tenant asignado.                        |
| `disassociateClinic(ClinicId clinicId)`                                        | `void`       | public      | Desasocia si coincide con el tenant actual; de lo contrario lanza excepción.            |

**2. Role (Entity)**

Define un rol clínico asignable a un usuario. Persiste como entidad para permitir nuevas autorizaciones granulares a futuro sin migrar el schema del aggregate.

**Atributos principales:**

| Atributo | Tipo    | Visibilidad | Descripción                                                         |
|----------|---------|-------------|---------------------------------------------------------------------|
| `id`     | `Long`  | private     | Identificador único del rol.                                        |
| `name`   | `Roles` | private     | Nombre del rol (enum `PATIENT`, `PHYSIOTHERAPIST`, `CLINIC_ADMIN`). |

**Métodos principales:**

| Método                                         | Tipo Retorno | Visibilidad | Descripción                                       |
|------------------------------------------------|--------------|-------------|---------------------------------------------------|
| `Role()`                                       | Constructor  | public      | Constructor vacío (JPA/Lombok).                   |
| `Role(Roles name)`                             | Constructor  | public      | Inicializa rol con el enum correspondiente.       |
| `getStringName()`                              | `String`     | public      | Devuelve el nombre del enum como string.          |
| `getDefaultRole()` *(static)*                  | `Role`       | public      | Devuelve el rol por defecto (`PATIENT`).          |
| `toRoleFromName(String name)` *(static)*       | `Role`       | public      | Crea un `Role` a partir del nombre del enum.      |
| `validateRoleSet(List<Role> roles)` *(static)* | `List<Role>` | public      | Si la lista es nula o vacía, retorna `[PATIENT]`. |

**3. AccountStatus (Value Object)**

Estado actual de la cuenta del usuario.

| Atributo  | Tipo | Visibilidad | Descripción                                                             |
|-----------|------|-------------|-------------------------------------------------------------------------|
| `PENDING` | Enum | public      | La cuenta está pendiente de activación.                                 |
| `ACTIVE`  | Enum | public      | La cuenta está activa.                                                  |
| `BLOCKED` | Enum | public      | La cuenta está bloqueada por el Administrador de Clínica o por Soporte. |
| `DELETED` | Enum | public      | La cuenta fue eliminada lógicamente.                                    |

**4. VerificationStatus (Value Object)**

Indica si el correo del usuario ya fue verificado.

| Atributo       | Tipo | Visibilidad | Descripción                            |
|----------------|------|-------------|----------------------------------------|
| `NOT_VERIFIED` | Enum | public      | El correo aún no está verificado.      |
| `VERIFIED`     | Enum | public      | El correo fue verificado exitosamente. |

**5. Roles (Value Object)**

Enumera los roles clínicos disponibles en uFlex.

| Atributo          | Tipo | Visibilidad | Descripción                                                            |
|-------------------|------|-------------|------------------------------------------------------------------------|
| `PATIENT`         | Enum | public      | Paciente en rehabilitación que usa la Mobile App y el sensor vestible. |
| `PHYSIOTHERAPIST` | Enum | public      | Fisioterapeuta que supervisa sesiones y ajusta protocolos clínicos.    |
| `CLINIC_ADMIN`    | Enum | public      | Administrador de clínica que gestiona sedes, usuarios y suscripción.   |

**6. ClinicId (Value Object)**

Identificador del tenant (clínica) al que se asocia un usuario. Es una referencia lógica al BC Subscription; no es una foreign key dura para mantener la autonomía entre bounded contexts.

| Atributo   | Tipo   | Visibilidad | Descripción                                                               |
|------------|--------|-------------|---------------------------------------------------------------------------|
| `clinicId` | `UUID` | private     | Identificador de la clínica; puede ser `null` si aún no ha sido asignado. |

**Métodos principales:**

| Método                    | Tipo Retorno | Visibilidad | Descripción                          |
|---------------------------|--------------|-------------|--------------------------------------|
| `ClinicId()`              | Constructor  | public      | Inicializa con `null` (no asignado). |
| `ClinicId(UUID clinicId)` | Constructor  | public      | Valida que el UUID no sea nulo.      |
| `isAssigned()`            | `boolean`    | public      | `true` si `clinicId != null`.        |

**7. VerificationCode (Value Object)**

Código y fecha de expiración usados para verificar usuarios (activación de correo o re-validación clínica). El envío del código se delega a Resend a través del `EmailService`.

**Atributos principales:**

| Atributo     | Tipo            | Visibilidad | Descripción                                                        |
|--------------|-----------------|-------------|--------------------------------------------------------------------|
| `code`       | `String`        | private     | Código de verificación (puede quedar `null` tras la verificación). |
| `expiration` | `LocalDateTime` | private     | Fecha y hora de expiración del código.                             |

**Métodos principales:**

| Método                                    | Tipo Retorno | Visibilidad | Descripción                                                       |
|-------------------------------------------|--------------|-------------|-------------------------------------------------------------------|
| `VerificationCode(String, LocalDateTime)` | Constructor  | public      | Valida que el código no sea vacío y que la expiración sea futura. |
| `isExpired()`                             | `boolean`    | public      | `true` si `now > expiration`.                                     |
| `matches(String inputCode)`               | `boolean`    | public      | `true` si el código coincide y no ha expirado.                    |

**8. EmailAddress (Value Object)**

VO compartido entre bounded contexts para representar un correo electrónico válido.

| Atributo | Tipo     | Visibilidad | Descripción                                         |
|----------|----------|-------------|-----------------------------------------------------|
| `value`  | `String` | private     | Valor del correo, validado contra formato RFC 5322. |

**9. FullName (Value Object)**

Nombre completo del usuario, compuesto por nombre y apellidos.

| Atributo    | Tipo     | Visibilidad | Descripción            |
|-------------|----------|-------------|------------------------|
| `firstName` | `String` | private     | Nombre(s) del usuario. |
| `lastName`  | `String` | private     | Apellidos del usuario. |

**10. PasswordHash (Value Object)**

Encapsula el hash bcrypt de la contraseña. La contraseña en texto plano nunca cruza la frontera del dominio: se hashea en el application layer (vía `HashingService`) antes de construir el VO.

| Atributo | Tipo     | Visibilidad | Descripción                                          |
|----------|----------|-------------|------------------------------------------------------|
| `value`  | `String` | private     | Hash bcrypt resultante (no se persiste en claro).    |

**Métodos principales:**

| Método                       | Tipo Retorno | Visibilidad | Descripción                                                       |
|------------------------------|--------------|-------------|-------------------------------------------------------------------|
| `PasswordHash(String value)` | Constructor  | public      | Valida que el hash no sea nulo ni vacío y tenga formato bcrypt.   |

**11. SignUpCommand (Command)**

Comando para registrar un nuevo usuario en uFlex.

| Atributo         | Tipo           | Visibilidad | Descripción                                                     |
|------------------|----------------|-------------|-----------------------------------------------------------------|
| `emailAddress`   | `EmailAddress` | public      | Correo del usuario.                                             |
| `password`       | `String`       | public      | Contraseña en texto plano (será hasheada por el service).       |
| `fullName`       | `FullName`     | public      | Nombre completo.                                                |
| `roles`          | `List<Role>`   | public      | Roles iniciales (validados por `validateRoleSet`).              |
| `clinicId`       | `ClinicId`     | public      | Clínica a la que se asocia (opcional en el onboarding inicial). |

**12. SignInCommand (Command)**

Comando para iniciar sesión validando email y contraseña; tras la validación uFlex emite un JWT propio.

| Atributo       | Tipo           | Visibilidad | Descripción                                          |
|----------------|----------------|-------------|------------------------------------------------------|
| `emailAddress` | `EmailAddress` | public      | Correo del usuario.                                  |
| `password`     | `String`       | public      | Contraseña en texto plano para validar contra hash.  |

**13. VerifyUserCommand (Command)**

Comando para verificar un usuario mediante el código enviado por correo.

| Atributo | Tipo     | Visibilidad | Descripción                                 |
|----------|----------|-------------|---------------------------------------------|
| `email`  | `String` | public      | Correo del usuario a verificar.             |
| `code`   | `String` | public      | Código de verificación recibido por correo. |

**14. ResendVerificationCodeCommand (Command)**

Comando para reenviar un código de verificación.

| Atributo | Tipo     | Visibilidad | Descripción                                            |
|----------|----------|-------------|--------------------------------------------------------|
| `email`  | `String` | public      | Correo válido del usuario al que se reenvía el código. |

**15. AssignUserClinicIdCommand (Command)**

Comando para asociar un usuario a una clínica (tenant). Es emitido típicamente por el BC Subscription cuando se activa el plan de la clínica y el administrador invita a sus fisioterapeutas.

| Atributo   | Tipo   | Visibilidad | Descripción                 |
|------------|--------|-------------|-----------------------------|
| `userId`   | `Long` | public      | ID del usuario objetivo.    |
| `clinicId` | `UUID` | public      | ID de la clínica a asociar. |

**16. SeedRolesCommand (Command)**

Comando utilizado al arranque del servicio para sembrar los roles clínicos base si aún no existen en la base de datos.

| Atributo    | Tipo | Visibilidad | Descripción                                                                                       |
|-------------|------|-------------|---------------------------------------------------------------------------------------------------|
| *(ninguno)* | —    | —           | No requiere atributos; su ejecución crea los roles `PATIENT`, `PHYSIOTHERAPIST` y `CLINIC_ADMIN`. |

**17. GetAuthenticatedUserClinicIdQuery (Query)**

Consulta para obtener el `ClinicId` del usuario autenticado en el contexto de seguridad.

| Atributo    | Tipo | Visibilidad | Descripción                                                                                  |
|-------------|------|-------------|----------------------------------------------------------------------------------------------|
| *(ninguno)* | —    | —           | No requiere atributos; retorna el `ClinicId` del usuario autenticado a partir del token JWT. |

**18. GetUserByIdQuery (Query)**

Consulta un usuario por su identificador interno.

| Atributo | Tipo   | Visibilidad | Descripción             |
|----------|--------|-------------|-------------------------|
| `userId` | `Long` | public      | ID interno del usuario. |

**19. GetUsersByClinicIdQuery (Query)**

Lista los usuarios asociados a una clínica (útil para la PWA del Administrador de Clínica).

| Atributo   | Tipo   | Visibilidad | Descripción       |
|------------|--------|-------------|-------------------|
| `clinicId` | `UUID` | public      | ID de la clínica. |

**20. GetUsersByRoleQuery (Query)**

Lista los usuarios de una clínica filtrados por rol (p. ej. todos los fisioterapeutas de una sede).

| Atributo   | Tipo    | Visibilidad | Descripción                              |
|------------|---------|-------------|------------------------------------------|
| `clinicId` | `UUID`  | public      | ID de la clínica sobre la que se filtra. |
| `role`     | `Roles` | public      | Rol a filtrar.                           |

**21. UserCreatedEvent (Domain Event)**

Evento publicado al crear un usuario. Permite al BC Subscription u otros reaccionar (por ejemplo, asignar un asiento del plan).

| Atributo       | Tipo      | Visibilidad | Descripción                |
|----------------|-----------|-------------|----------------------------|
| `userId`       | `Long`    | private     | ID del usuario creado.     |
| `emailAddress` | `String`  | private     | Correo del usuario.        |
| `occurredOn`   | `Instant` | private     | Marca temporal del evento. |

**22. UserVerifiedEvent (Domain Event)**

Evento publicado cuando el usuario completa la verificación.

| Atributo     | Tipo      | Visibilidad | Descripción                        |
|--------------|-----------|-------------|------------------------------------|
| `userId`     | `Long`    | private     | ID del usuario verificado.         |
| `verifiedAt` | `Instant` | private     | Marca temporal de la verificación. |

**23. UserVerificationCodeAssignedEvent (Domain Event)**

Evento publicado al asignar un código de verificación; es consumido por un handler que dispara el envío del correo vía Resend.

| Atributo            | Tipo      | Visibilidad | Descripción                                    |
|---------------------|-----------|-------------|------------------------------------------------|
| `source` (heredado) | `Object`  | private     | Objeto origen del evento (`ApplicationEvent`). |
| `email`             | `String`  | private     | Correo destinatario del código.                |
| `code`              | `String`  | private     | Código generado.                               |
| `expirationMinutes` | `Integer` | private     | Minutos hasta la expiración.                   |

**24. UserCommandService (Domain Service)**

Maneja los commands relacionados con usuarios.

| Método                                  | Tipo Retorno                            | Visibilidad | Descripción                                                                                            |
|-----------------------------------------|-----------------------------------------|-------------|--------------------------------------------------------------------------------------------------------|
| `handle(SignInCommand)`                 | `Optional<ImmutablePair<User, String>>` | public      | Valida email/contraseña y retorna el par (usuario, JWT emitido por uFlex).                             |
| `handle(SignUpCommand)`                 | `Optional<User>`                        | public      | Registra un usuario nuevo en uFlex hasheando la contraseña y generando un código de verificación.      |
| `handle(VerifyUserCommand)`             | `boolean`                               | public      | Verifica el usuario por código y activa la cuenta.                                                     |
| `handle(ResendVerificationCodeCommand)` | `boolean`                               | public      | Reenvía el código de verificación si el usuario aún no está verificado.                                |
| `handle(AssignUserClinicIdCommand)`     | `void`                                  | public      | Asocia un usuario a una clínica.                                                                       |

**25. UserQueryService (Domain Service)**

Maneja las queries relacionadas con usuarios.

| Método                                      | Tipo Retorno         | Visibilidad | Descripción                                          |
|---------------------------------------------|----------------------|-------------|------------------------------------------------------|
| `handle(GetAuthenticatedUserClinicIdQuery)` | `Optional<ClinicId>` | public      | Obtiene el `ClinicId` del usuario autenticado.       |
| `handle(GetUserByIdQuery)`                  | `Optional<User>`     | public      | Recupera un usuario por su ID interno.               |
| `handle(GetUsersByClinicIdQuery)`           | `List<User>`         | public      | Lista los usuarios asociados a una clínica.          |
| `handle(GetUsersByRoleQuery)`               | `List<User>`         | public      | Lista los usuarios de una clínica filtrados por rol. |

**26. RoleCommandService (Domain Service)**

Maneja los commands relacionados con la gestión de roles.

| Método                     | Tipo Retorno | Visibilidad | Descripción                                        |
|----------------------------|--------------|-------------|----------------------------------------------------|
| `handle(SeedRolesCommand)` | `void`       | public      | Siembra los roles clínicos base si aún no existen. |

#### 4.2.1.2. Interface Layer

**1. AuthenticationController (REST Controller)**

Expone las funcionalidades de autenticación y registro a través de endpoints HTTP. Toda la lógica de autenticación (hashing, validación de credenciales y emisión de JWT) corre dentro del monolito de uFlex.

**Endpoints principales:**

| Método       | Ruta base                            | HTTP | Descripción                                                                                                          |
|--------------|--------------------------------------|------|----------------------------------------------------------------------------------------------------------------------|
| `signIn`     | `/api/v1/authentication/sign-in`     | POST | Recibe email y contraseña, valida las credenciales contra el hash bcrypt y retorna el JWT emitido por uFlex.         |
| `signUp`     | `/api/v1/authentication/sign-up`     | POST | Registra un nuevo usuario hasheando la contraseña con bcrypt y generando el código de verificación.                  |
| `verify`     | `/api/v1/authentication/verify`      | POST | Verifica al usuario con el código clínico enviado por correo.                                                        |
| `resendCode` | `/api/v1/authentication/resend-code` | POST | Reenvía el código de verificación al correo del usuario.                                                             |

**2. UserController (REST Controller)**

Expone operaciones de consulta y administración del perfil clínico.

**Endpoints principales:**

| Método             | Ruta base                                 | HTTP  | Descripción                                                          |
|--------------------|-------------------------------------------|-------|----------------------------------------------------------------------|
| `getUserById`      | `/api/v1/users/{id}`                      | GET   | Obtiene el perfil clínico por ID interno.                            |
| `getUsersByClinic` | `/api/v1/users?clinicId={id}`             | GET   | Lista los usuarios de una clínica (requiere rol `CLINIC_ADMIN`).     |
| `getUsersByRole`   | `/api/v1/users?clinicId={id}&role={role}` | GET   | Lista los usuarios de una clínica filtrados por rol.                 |
| `assignClinic`     | `/api/v1/users/{id}/clinic`               | PATCH | Asocia un usuario a una clínica (invocado desde el BC Subscription). |

**3. Resources (DTOs)**

DTOs utilizados para la comunicación REST, modelados como Java Records.

| Resource                         | Atributos principales                                                                                                    | Descripción                                                                |
|----------------------------------|--------------------------------------------------------------------------------------------------------------------------|----------------------------------------------------------------------------|
| `AuthenticatedUserResource`      | `id: Long`, `emailAddress: String`, `roles: List<String>`, `clinicId: UUID`, `token: String`                             | Respuesta del sign-in (perfil enriquecido + JWT emitido por uFlex).        |
| `SignInResource`                 | `emailAddress: String`, `password: String`                                                                               | Credenciales que el cliente envía para iniciar sesión.                     |
| `SignUpResource`                 | `emailAddress: String`, `password: String`, `fullName: String`, `roles: List<String>`, `clinicId: UUID`                  | Datos mínimos para crear un usuario nuevo en uFlex.                        |
| `VerifyUserResource`             | `email: String`, `code: String`                                                                                          | Verificación de usuario por código.                                        |
| `ResendVerificationCodeResource` | `email: String`                                                                                                          | Solicita reenviar el código de verificación.                               |
| `UserResource`                   | `id: Long`, `emailAddress: String`, `fullName: String`, `roles: List<String>`, `clinicId: UUID`, `accountStatus: String` | Usuario expuesto por la API de consulta.                                   |
| `RoleResource`                   | `id: Long`, `name: String`                                                                                               | Representación de un rol clínico.                                          |

**4. Transform (Assemblers)**

Convierten entre entidades del dominio y recursos REST, así como entre recursos y commands/queries.

| Assembler                                            | Entrada                          | Salida                          | Descripción                                                                          |
|------------------------------------------------------|----------------------------------|---------------------------------|--------------------------------------------------------------------------------------|
| `AuthenticatedUserResourceFromEntityAssembler`       | `User`, `token: String`          | `AuthenticatedUserResource`     | Mapea el aggregate `User` y el JWT al recurso de respuesta de sign-in.               |
| `SignInCommandFromResourceAssembler`                 | `SignInResource`                 | `SignInCommand`                 | Construye el command de sign-in con email y contraseña.                              |
| `SignUpCommandFromResourceAssembler`                 | `SignUpResource`                 | `SignUpCommand`                 | Construye el command de registro, mapeando `List<String>` a `List<Role>`.            |
| `VerifyUserCommandFromResourceAssembler`             | `VerifyUserResource`             | `VerifyUserCommand`             | Construye el command de verificación por código.                                     |
| `ResendVerificationCodeCommandFromResourceAssembler` | `ResendVerificationCodeResource` | `ResendVerificationCodeCommand` | Construye el command de reenvío de código.                                           |
| `UserResourceFromEntityAssembler`                    | `User`                           | `UserResource`                  | Expone el aggregate como recurso de consulta.                                        |
| `RoleResourceFromEntityAssembler`                    | `Role`                           | `RoleResource`                  | Expone el rol como recurso.                                                          |

#### 4.2.1.3. Application Layer

**1. IamContextFacadeImpl (ACL Facade)**

Implementa la fachada que otros bounded contexts (Subscription, Therapy, Trends, Analytics) usan para obtener información de identidad sin conocer el modelo interno del BC IAM.

| Atributo           | Tipo               | Visibilidad | Descripción                            |
|--------------------|--------------------|-------------|----------------------------------------|
| `userQueryService` | `UserQueryService` | private     | Servicio de consultas del dominio IAM. |

**Métodos principales:**

| Método                             | Tipo Retorno        | Visibilidad | Descripción                                                                              |
|------------------------------------|---------------------|-------------|------------------------------------------------------------------------------------------|
| `fetchAuthenticatedUserClinicId()` | `UUID`              | public      | Retorna el `clinicId` actual a partir del contexto de seguridad (o `null` si no existe). |
| `fetchUserById(Long userId)`       | `Optional<UserDto>` | public      | Expone un DTO ligero del perfil, sin el aggregate interno.                               |

**2. RoleCommandServiceImpl (Command Service Implementation)**

| Atributo         | Tipo             | Visibilidad | Descripción                        |
|------------------|------------------|-------------|------------------------------------|
| `roleRepository` | `RoleRepository` | private     | Acceso a la persistencia de roles. |

**Métodos principales:**

| Método                     | Tipo Retorno | Visibilidad | Descripción                                        |
|----------------------------|--------------|-------------|----------------------------------------------------|
| `handle(SeedRolesCommand)` | `void`       | public      | Crea los roles del enum `Roles` si aún no existen. |

**3. UserCommandServiceImpl (Command Service Implementation)**

Orquesta registro, autenticación, verificación y asociación de clínica. Hashea contraseñas con bcrypt, valida credenciales y emite JWT internos.

| Atributo              | Tipo                        | Visibilidad | Descripción                                                                            |
|-----------------------|-----------------------------|-------------|----------------------------------------------------------------------------------------|
| `userRepository`      | `UserRepository`            | private     | Persistencia de usuarios.                                                              |
| `hashingService`      | `HashingService`            | private     | Hashea contraseñas con bcrypt y compara hashes contra texto plano.                     |
| `tokenService`        | `TokenService`              | private     | Emite y valida los JWT propios de uFlex.                                               |
| `verificationService` | `VerificationService`       | private     | Generación y validación de códigos de verificación.                                    |
| `roleRepository`      | `RoleRepository`            | private     | Resolución de roles por nombre.                                                        |
| `eventPublisher`      | `ApplicationEventPublisher` | private     | Publicación de domain events.                                                          |

**Métodos principales:**

| Método                                  | Tipo Retorno                            | Visibilidad | Descripción                                                                                               |
|-----------------------------------------|-----------------------------------------|-------------|-----------------------------------------------------------------------------------------------------------|
| `handle(SignInCommand)`                 | `Optional<ImmutablePair<User, String>>` | public      | Valida email/contraseña contra el hash bcrypt y retorna `(user, JWT enriquecido con clinicId)`.           |
| `handle(SignUpCommand)`                 | `Optional<User>`                        | public      | Crea el perfil local hasheando la contraseña con bcrypt, asigna roles y genera el código de verificación. |
| `handle(VerifyUserCommand)`             | `boolean`                               | public      | Valida el código y activa la cuenta.                                                                      |
| `handle(ResendVerificationCodeCommand)` | `boolean`                               | public      | Reenvía el código de verificación si el usuario no está verificado.                                       |
| `handle(AssignUserClinicIdCommand)`     | `void`                                  | public      | Asocia la clínica al usuario objetivo.                                                                    |

**4. UserQueryServiceImpl (Query Service Implementation)**

| Atributo          | Tipo              | Visibilidad | Descripción                                 |
|-------------------|-------------------|-------------|---------------------------------------------|
| `userRepository`  | `UserRepository`  | private     | Lectura del read model de usuarios.         |
| `identityService` | `IdentityService` | private     | Proveedor del contexto de identidad actual. |

**Métodos principales:**

| Método                                      | Tipo Retorno         | Visibilidad | Descripción                                    |
|---------------------------------------------|----------------------|-------------|------------------------------------------------|
| `handle(GetAuthenticatedUserClinicIdQuery)` | `Optional<ClinicId>` | public      | Retorna el `ClinicId` del usuario autenticado. |
| `handle(GetUserByIdQuery)`                  | `Optional<User>`     | public      | Recupera un usuario por su ID interno.         |
| `handle(GetUsersByClinicIdQuery)`           | `List<User>`         | public      | Lista los usuarios asociados a una clínica.    |
| `handle(GetUsersByRoleQuery)`               | `List<User>`         | public      | Lista los usuarios de una clínica por rol.     |

**5. SubscriptionActivatedEventHandler (Domain Event Handler)**

Reacciona al evento `SubscriptionActivatedEvent` emitido por el BC Subscription para sincronizar el `clinicId` del Administrador de Clínica tras la activación del plan.

| Atributo             | Tipo                 | Visibilidad | Descripción                                                 |
|----------------------|----------------------|-------------|-------------------------------------------------------------|
| `userCommandService` | `UserCommandService` | private     | Envía `AssignUserClinicIdCommand` al usuario administrador. |

| Método                           | Tipo Retorno | Visibilidad | Descripción                                                        |
|----------------------------------|--------------|-------------|--------------------------------------------------------------------|
| `on(SubscriptionActivatedEvent)` | `void`       | public      | Asocia el `clinicId` recién creado al administrador de la clínica. |

**6. ApplicationReadyEventHandler (Framework Event Handler)**

| Atributo             | Tipo                 | Visibilidad | Descripción                   |
|----------------------|----------------------|-------------|-------------------------------|
| `roleCommandService` | `RoleCommandService` | private     | Orquesta la siembra de roles. |

| Método                      | Tipo Retorno | Visibilidad | Descripción                                         |
|-----------------------------|--------------|-------------|-----------------------------------------------------|
| `on(ApplicationReadyEvent)` | `void`       | public      | Ejecuta `SeedRolesCommand` al arrancar el servicio. |

**7. UserVerificationCodeAssignedEventHandler (Domain Event Handler)**

| Atributo       | Tipo           | Visibilidad | Descripción                              |
|----------------|----------------|-------------|------------------------------------------|
| `emailService` | `EmailService` | private     | Servicio para envío de correos (Resend). |

| Método                                      | Tipo Retorno | Visibilidad | Descripción                                                         |
|---------------------------------------------|--------------|-------------|---------------------------------------------------------------------|
| `handle(UserVerificationCodeAssignedEvent)` | `void`       | public      | Envía el correo con el código y la expiración (ejecución `@Async`). |

**8. EmailService (Outbound Service Port)**

Interfaz para envío de correos (implementada contra Resend en la Infrastructure Layer).

| Método                                                                  | Tipo Retorno | Visibilidad | Descripción                                                                   |
|-------------------------------------------------------------------------|--------------|-------------|-------------------------------------------------------------------------------|
| `sendVerificationEmail(String to, String code, int expirationMinutes)`  | `void`       | public      | Envía un correo de verificación.                                              |
| `sendPasswordResetEmail(String to, String link)`                        | `void`       | public      | Envía un correo de restablecimiento de contraseña con un link tokenizado.     |
| `sendClinicInvitationEmail(String to, String clinicName, String token)` | `void`       | public      | Envía invitación a fisioterapeuta para unirse a una clínica.                  |

**9. HashingService (Outbound Service Port)**

Hashea contraseñas con bcrypt y verifica una contraseña en texto plano contra un hash existente.

| Método                                          | Tipo Retorno | Visibilidad | Descripción                                                       |
|-------------------------------------------------|--------------|-------------|-------------------------------------------------------------------|
| `hash(String rawPassword)`                      | `String`     | public      | Devuelve el hash bcrypt de la contraseña en texto plano.          |
| `matches(String rawPassword, String hash)`      | `boolean`    | public      | `true` si el hash bcrypt corresponde a la contraseña recibida.    |

**10. TokenService (Outbound Service Port)**

Emite y valida los JWT propios de uFlex y extrae sus claims.

| Método                          | Tipo Retorno       | Visibilidad | Descripción                                                                  |
|---------------------------------|--------------------|-------------|------------------------------------------------------------------------------|
| `generateToken(User user)`      | `String`           | public      | Genera un JWT firmado con los claims `sub`, `email`, `roles` y `clinicId`.   |
| `validateToken(String jwt)`     | `boolean`          | public      | Valida firma, emisor y expiración del JWT.                                   |
| `getUserIdFromToken(String jwt)`| `Optional<Long>`   | public      | Extrae el claim `sub` (ID interno del usuario).                              |
| `getEmailFromToken(String jwt)` | `Optional<String>` | public      | Extrae el claim `email`.                                                     |

**11. IdentityService (Outbound Service Port)**

Interfaz para obtener los datos del contexto de seguridad actual (leídos del JWT tras su validación por el filtro de seguridad).

| Método                | Tipo Retorno       | Visibilidad | Descripción                                                                           |
|-----------------------|--------------------|-------------|---------------------------------------------------------------------------------------|
| `getUserId()`         | `Optional<Long>`   | public      | ID interno del usuario autenticado.                                                   |
| `getEmail()`          | `Optional<String>` | public      | Email del contexto.                                                                   |
| `getRoles()`          | `Set<String>`      | public      | Roles del contexto.                                                                   |
| `getClinicId()`       | `Optional<UUID>`   | public      | Clínica asociada al usuario actual.                                                   |
| `isServiceAccount()`  | `boolean`          | public      | Indica si el caller es una service account (por ejemplo, un job interno).             |

**12. VerificationService (Outbound Service Port)**

Interfaz para generar y validar códigos de verificación.

| Método                                                               | Tipo Retorno | Visibilidad | Descripción                                      |
|----------------------------------------------------------------------|--------------|-------------|--------------------------------------------------|
| `generateCode()`                                                     | `String`     | public      | Genera un código con longitud por defecto.       |
| `generateCode(int length)`                                           | `String`     | public      | Genera un código con longitud indicada.          |
| `generateExpirationMinutes()`                                        | `Integer`    | public      | Devuelve los minutos de expiración configurados. |
| `verifyCode(String code, String expected, LocalDateTime expiration)` | `boolean`    | public      | Verifica coincidencia y vigencia del código.     |

#### 4.2.1.4. Infrastructure Layer

**1. UserRepository (Repository Interface)**

Interfaz de acceso a datos para usuarios, implementada por Spring Data JPA sobre Azure Database for PostgreSQL.

| Método                                                | Tipo Retorno     | Visibilidad | Descripción                                      |
|-------------------------------------------------------|------------------|-------------|--------------------------------------------------|
| `findById(Long id)`                                   | `Optional<User>` | public      | Busca un usuario por su identificador interno.   |
| `save(User user)`                                     | `User`           | public      | Persiste o actualiza un usuario.                 |
| `findByEmailAddress(EmailAddress email)`              | `Optional<User>` | public      | Obtiene un usuario por su correo.                |
| `existsByEmailAddress(EmailAddress email)`            | `boolean`        | public      | Verifica la existencia de un usuario por correo. |
| `findAllByClinicId(UUID clinicId)`                    | `List<User>`     | public      | Lista usuarios por clínica.                      |
| `findAllByClinicIdAndRole(UUID clinicId, Roles role)` | `List<User>`     | public      | Lista usuarios por clínica y rol.                |

**2. RoleRepository (Repository Interface)**

| Método                     | Tipo Retorno     | Visibilidad | Descripción                            |
|----------------------------|------------------|-------------|----------------------------------------|
| `findById(Long id)`        | `Optional<Role>` | public      | Busca un rol por su identificador.     |
| `save(Role role)`          | `Role`           | public      | Persiste o actualiza un rol.           |
| `findByName(Roles name)`   | `Optional<Role>` | public      | Obtiene un rol por su enum `Roles`.    |
| `existsByName(Roles name)` | `boolean`        | public      | Verifica existencia por nombre de rol. |

**3. WebSecurityConfiguration (Security Config)**

Configuración de Spring Security stateless con validación del JWT emitido internamente por uFlex.

| Método/Bean                      | Tipo Retorno                | Visibilidad | Descripción                                                                                    |
|----------------------------------|-----------------------------|-------------|------------------------------------------------------------------------------------------------|
| `jwtAuthenticationFilter()`      | `JwtAuthenticationFilter`   | public      | Filtro que extrae y valida el JWT emitido por uFlex y autentica el request.                    |
| `passwordEncoder()`              | `PasswordEncoder`           | public      | Bean `BCryptPasswordEncoder` consumido por el `BcryptHashingService`.                          |
| `authenticationManager(config)`  | `AuthenticationManager`     | public      | Expone el `AuthenticationManager` de Spring Security.                                          |
| `filterChain(HttpSecurity http)` | `SecurityFilterChain`       | public      | CORS, CSRF off, handler 401, stateless; `permitAll` a `/api/v1/authentication/**` y a Swagger. |

**4. JwtAuthenticationFilter (Security Filter)**

Filtro que autentica requests a partir del JWT Bearer emitido por uFlex.

| Método                                       | Tipo Retorno | Visibilidad | Descripción                                                                                                |
|----------------------------------------------|--------------|-------------|------------------------------------------------------------------------------------------------------------|
| `doFilterInternal(request, response, chain)` | `void`       | protected   | Extrae el token, lo valida contra `TokenService`, carga el `UserDetails` local y establece la autenticación.|

**5. UnauthorizedRequestHandlerEntryPoint (Auth EntryPoint)**

Maneja las respuestas 401 no autorizadas.

| Método                                       | Tipo Retorno | Visibilidad | Descripción                                      |
|----------------------------------------------|--------------|-------------|--------------------------------------------------|
| `commence(request, response, authException)` | `void`       | public      | Responde con `401 Unauthorized` en formato JSON. |

**6. UserDetailsServiceImpl (UserDetailsService)**

Carga el perfil local a partir del email o del ID interno extraído del JWT.

| Método                              | Tipo Retorno  | Visibilidad | Descripción                                          |
|-------------------------------------|---------------|-------------|------------------------------------------------------|
| `loadUserByUsername(String email)`  | `UserDetails` | public      | Carga el perfil local a partir del email del usuario.|
| `loadUserById(Long userId)`         | `UserDetails` | public      | Carga el perfil local a partir del ID interno.       |

**7. UserDetailsImpl (Security Model)**

Adaptador con authorities y `clinicId`.

| Método             | Tipo Retorno      | Visibilidad | Descripción                                                                           |
|--------------------|-------------------|-------------|---------------------------------------------------------------------------------------|
| `build(User user)` | `UserDetailsImpl` | public      | Construye desde la entidad `User` (roles → authorities, `clinicId` como claim extra). |

**8. VerificationServiceImpl (Verification Service)**

Generación y validación de códigos OTP con configuración externa.

| Método                                                               | Tipo Retorno | Visibilidad | Descripción                                |
|----------------------------------------------------------------------|--------------|-------------|--------------------------------------------|
| `generateCode()`                                                     | `String`     | public      | Genera un código con longitud por defecto. |
| `generateCode(int length)`                                           | `String`     | public      | Genera un código con la longitud indicada. |
| `generateExpirationMinutes()`                                        | `Integer`    | public      | Minutos de expiración configurados.        |
| `verifyCode(String code, String expected, LocalDateTime expiration)` | `boolean`    | public      | Verifica coincidencia y vigencia.          |

**9. VerificationProperties (Configuration Properties)**

Propiedades externas para OTP (prefijo `uflex.iam.verification`).

| Campo               | Tipo      | Visibilidad | Descripción                        |
|---------------------|-----------|-------------|------------------------------------|
| `expirationMinutes` | `Integer` | private     | Minutos de expiración por defecto. |
| `codeLength`        | `Integer` | private     | Longitud del código OTP.           |

**10. ResendEmailServiceImpl (Email Adapter)**

Implementación de `EmailService` contra la API de Resend.

| Atributo           | Tipo                    | Visibilidad | Descripción                                       |
|--------------------|-------------------------|-------------|---------------------------------------------------|
| `resendClient`     | `ResendHttpClient`      | private     | Cliente HTTP hacia la API de Resend.              |
| `templateRenderer` | `EmailTemplateRenderer` | private     | Motor de plantillas (Thymeleaf) para los correos. |

| Método                                                                  | Tipo Retorno | Visibilidad | Descripción                              |
|-------------------------------------------------------------------------|--------------|-------------|------------------------------------------|
| `sendVerificationEmail(String to, String code, int exp)`                | `void`       | public      | Renderiza la plantilla y llama a Resend. |
| `sendPasswordResetEmail(String to, String link)`                        | `void`       | public      | Envía correo de reseteo con plantilla.   |
| `sendClinicInvitationEmail(String to, String clinicName, String token)` | `void`       | public      | Envía correo de invitación a clínica.    |

**11. BcryptHashingService (Hashing Adapter)**

Implementa `HashingService` sobre `BCryptPasswordEncoder` de Spring Security.

| Atributo          | Tipo                | Visibilidad | Descripción                          |
|-------------------|---------------------|-------------|--------------------------------------|
| `passwordEncoder` | `PasswordEncoder`   | private     | Encoder bcrypt configurado por Spring|

| Método                                     | Tipo Retorno | Visibilidad | Descripción                                                       |
|--------------------------------------------|--------------|-------------|-------------------------------------------------------------------|
| `hash(String rawPassword)`                 | `String`     | public      | Devuelve el hash bcrypt de la contraseña en texto plano.          |
| `matches(String rawPassword, String hash)` | `boolean`    | public      | `true` si el hash bcrypt corresponde a la contraseña recibida.    |

**12. JjwtTokenService (JWT Adapter)**

Implementa `TokenService` usando la librería `jjwt`. Firma los tokens con la clave privada de uFlex y valida los tokens entrantes contra la misma clave.

| Atributo        | Tipo               | Visibilidad | Descripción                                              |
|-----------------|--------------------|-------------|----------------------------------------------------------|
| `signingKey`    | `SecretKey`        | private     | Clave HMAC con la que se firman y verifican los tokens.  |
| `tokenProperties`| `TokenProperties` | private     | Issuer, expiración por defecto y otros parámetros.       |

| Método                          | Tipo Retorno       | Visibilidad | Descripción                                                                  |
|---------------------------------|--------------------|-------------|------------------------------------------------------------------------------|
| `generateToken(User user)`      | `String`           | public      | Construye y firma un JWT con `sub`, `email`, `roles` y `clinicId`.           |
| `validateToken(String jwt)`     | `boolean`          | public      | Valida firma, issuer y expiración del JWT.                                   |
| `getUserIdFromToken(String jwt)`| `Optional<Long>`   | public      | Extrae el claim `sub`.                                                       |
| `getEmailFromToken(String jwt)` | `Optional<String>` | public      | Extrae el claim `email`.                                                     |

**13. CurrentUserProviderImpl (Identity Adapter)**

Implementa `IdentityService` leyendo el contexto de `SecurityContextHolder` de Spring Security.

| Método                | Tipo Retorno       | Visibilidad | Descripción                                  |
|-----------------------|--------------------|-------------|----------------------------------------------|
| `getUserId()`         | `Optional<Long>`   | public      | ID interno del usuario autenticado.          |
| `getEmail()`          | `Optional<String>` | public      | Email del contexto.                          |
| `getRoles()`          | `Set<String>`      | public      | Authorities del contexto.                    |
| `getClinicId()`       | `Optional<UUID>`   | public      | `clinicId` del contexto.                     |
| `isServiceAccount()`  | `boolean`          | public      | Indica si es una cuenta de servicio interna. |

#### 4.2.1.5. Bounded Context Software Architecture Component Level Diagrams

El diagrama de componentes (C4 Nivel 3) muestra cómo se organiza internamente el módulo IAM dentro del monolito (Java/Spring Boot). Se distinguen seis componentes principales: el `Authentication Controller` y el `User Controller` como puntos de entrada REST, los dos application services `User Command Service` y `User Query Service` que materializan el patrón CQRS, el `User Repository (JPA)` como abstracción de persistencia, y los adapters de seguridad `Bcrypt Hashing Service` (hashing de contraseñas) y `JJWT Token Service` (emisión y validación de los JWT propios de uFlex). Todos los componentes viven dentro del *Container Boundary* del REST API (monolito); la `uFlex DB` queda fuera (PostgreSQL en Supabase, consumida por JDBC/SSL).

<div style="text-align: center;">
  <img src="assets/diagrams/software-architecture/components/out/iam-components-diagram.png" alt="uFlex — IAM Bounded Context Component Diagram" style="max-width: 100%; height: auto;">
</div>

*Figura 4.2.1.5. Diagrama de componentes (C4 Nivel 3) del Bounded Context IAM.*

#### 4.2.1.6. Bounded Context Software Architecture Code Level Diagrams

##### 4.2.1.6.1. Bounded Context Domain Layer Class Diagrams

El diagrama de clases del Domain Layer del BC IAM modela exclusivamente los conceptos centrales del dominio, sin incluir las capas de application ni infrastructure. El paquete `domain.model.aggregates` contiene al Aggregate Root `User` y a la Entity `Role`; `domain.model.valueobjects` agrupa los Value Objects (`UserId`, `EmailAddress`, `PasswordHash`, `FullName`, `ClinicId`, `VerificationCode`) y los enumerados (`Roles`, `AccountStatus`, `VerificationStatus`); `domain.model.events` encapsula los Domain Events publicados por el aggregate (`UserCreatedEvent`, `UserVerifiedEvent`, `UserVerificationCodeAssignedEvent`); y `domain.exceptions` reúne las excepciones de negocio que protegen las invariantes del dominio. Las flechas con línea continua marcan composición (el `User` contiene sus Value Objects), las flechas con línea punteada marcan dependencias semánticas (eventos publicados y excepciones lanzadas) y los rombos vacíos indican agregación con cardinalidad opcional o múltiple (relación de `User` con `ClinicId` y con `Role`).

<div style="text-align: center;">
  <img src="assets/diagrams/uml/class/out/iam-domain-layer-class-diagram.png" alt="uFlex — IAM Bounded Context Domain Class Diagram" style="max-width: 100%; height: auto;">
</div>

*Figura 4.2.1.6.1. Diagrama de clases del dominio del Bounded Context IAM.*

##### 4.2.1.6.2. Bounded Context Database Design Diagram

El esquema físico del BC IAM (esquema `iam` dentro de la base PostgreSQL hospedada en Supabase) consta de una tabla principal `users` que almacena el perfil enriquecido (identificador interno, email único, hash bcrypt de la contraseña, nombre completo, rol, estado, clínica asociada y timestamps de auditoría), dos tablas de catálogo `user_roles` y `user_statuses` para mantener normalizados los valores permitidos (usadas también para internacionalizar descripciones en el futuro) y una tabla `user_audit_events` que registra los eventos significativos del ciclo de vida del usuario (creación, verificación, cambios de rol, suspensiones) con un payload JSONB flexible. Los índices incluyen unicidad sobre `email` e índices compuestos por `(role, clinic_id)` y `(clinic_id)` para soportar las queries más frecuentes de la Web Client App (listado por clínica y por rol). Se optó deliberadamente por **no** declarar una foreign key dura sobre `clinic_id` hacia la tabla de clínicas del BC Subscription: cada bounded context aísla su schema y la referencia es lógica, respetando la autonomía entre contextos.

<div style="text-align: center;">
  <img src="assets/diagrams/database/erd/out/iam-database-design-diagram.png" alt="uFlex — IAM Bounded Context Database ER Diagram" style="max-width: 100%; height: auto;">
</div>

*Figura 4.2.1.6.2. Diagrama entidad-relación del Bounded Context IAM.*

<hr class="page-break">

### 4.2.2. Bounded Context: Subscription

El bounded context **Subscription** concentra la gestión comercial del modelo SaaS multi-tenant de uFlex: catálogo de planes, ciclo de vida de la suscripción de cada clínica (compra, activación, renovación, vencimiento, cancelación), emisión de facturas y reconciliación de pagos con la pasarela externa Culqi. A diferencia del BC IAM —que modela la identidad del usuario individual— este contexto trabaja a nivel de *clínica* (tenant) y es disparado típicamente por el Administrador de Clínica. Los comandos y eventos principales (`PurchaseSubscriptionPlanCommand`, `SubscriptionPurchasedEvent`, `SubscriptionLinkedToClinicEvent`) fueron identificados durante el Design-Level EventStorming.

#### 4.2.2.1. Domain Layer

En esta sección se describen los elementos del Domain Layer del contexto de Subscription, que modelan las reglas de negocio asociadas a la venta, activación y facturación de los planes multi-tenant de uFlex. Las invariantes clave son: una clínica puede tener una sola suscripción `ACTIVE` a la vez, una suscripción no puede activarse sin un cobro confirmado por Culqi, y las facturas emitidas son inmutables salvo por transiciones de estado controladas.

**1. Subscription (Aggregate Root)**

Representa la suscripción de una clínica a uFlex. Encapsula el plan contratado, el ciclo de facturación, las ventanas temporales (periodo actual, próxima facturación, periodo de prueba) y la colección de facturas emitidas.

**Atributos principales:**

| Atributo             | Tipo                 | Visibilidad | Descripción                                                                               |
|----------------------|----------------------|-------------|-------------------------------------------------------------------------------------------|
| `id`                 | `SubscriptionId`     | private     | Identificador interno de la suscripción.                                                  |
| `clinicId`           | `ClinicId`           | private     | Tenant (clínica) al que pertenece la suscripción.                                         |
| `plan`               | `SubscriptionPlan`   | private     | Plan contratado (referencia a la Entity del catálogo).                                    |
| `status`             | `SubscriptionStatus` | private     | Estado actual (`PENDING_PAYMENT`, `TRIAL`, `ACTIVE`, `PAST_DUE`, `CANCELLED`, `EXPIRED`). |
| `billingCycle`       | `BillingCycle`       | private     | Ciclo de facturación elegido (`MONTHLY` o `YEARLY`).                                      |
| `currentPeriodStart` | `LocalDate`          | private     | Fecha de inicio del periodo actual.                                                       |
| `currentPeriodEnd`   | `LocalDate`          | private     | Fecha de fin del periodo actual.                                                          |
| `nextBillingDate`    | `LocalDate`          | private     | Fecha en la que se cobrará la renovación automática.                                      |
| `trialUntil`         | `LocalDate`          | private     | Fecha de fin del periodo de prueba (si aplica).                                           |
| `paymentReference`   | `PaymentReference`   | private     | Referencia al medio de pago tokenizado en Culqi.                                          |
| `invoices`           | `List<Invoice>`      | private     | Historial de facturas emitidas para esta suscripción.                                     |

**Métodos principales:**

| Método                                                   | Tipo Retorno | Visibilidad | Descripción                                                                                         |
|----------------------------------------------------------|--------------|-------------|-----------------------------------------------------------------------------------------------------|
| `Subscription()`                                         | Constructor  | public      | Constructor vacío requerido por JPA.                                                                |
| `Subscription(ClinicId, SubscriptionPlan, BillingCycle)` | Constructor  | public      | Crea una suscripción en estado `PENDING_PAYMENT`.                                                   |
| `activate()`                                             | `void`       | public      | Cambia el estado a `ACTIVE` tras confirmar el primer cobro; publica `SubscriptionActivatedEvent`.   |
| `renew()`                                                | `Invoice`    | public      | Genera una nueva factura para el siguiente periodo y actualiza `nextBillingDate`.                   |
| `cancel(String reason)`                                  | `void`       | public      | Cambia el estado a `CANCELLED` y publica `SubscriptionCancelledEvent`.                              |
| `markPastDue()`                                          | `void`       | public      | Marca la suscripción como `PAST_DUE` si un cobro falla.                                             |
| `expire()`                                               | `void`       | public      | Transiciona a `EXPIRED` cuando la cuenta lleva más de N días en `PAST_DUE`.                         |
| `linkToClinic(ClinicId)`                                 | `void`       | public      | Asocia la suscripción a la clínica en la primera compra; publica `SubscriptionLinkedToClinicEvent`. |
| `registerPayment(PaymentReference)`                      | `void`       | public      | Registra una referencia de pago tokenizada para cobros recurrentes.                                 |
| `isActive()`                                             | `boolean`    | public      | Devuelve `true` si el estado actual es `ACTIVE` o `TRIAL`.                                          |

**2. SubscriptionPlan (Entity)**

Define un plan del catálogo comercial (por ejemplo, *Starter*, *Professional*, *Enterprise*). Persiste como entidad para permitir al equipo comercial crear nuevos planes sin redesplegar el código.

**Atributos principales:**

| Atributo              | Tipo          | Visibilidad | Descripción                                          |
|-----------------------|---------------|-------------|------------------------------------------------------|
| `id`                  | `PlanId`      | private     | Identificador del plan.                              |
| `name`                | `String`      | private     | Nombre comercial (p. ej. *Starter*).                 |
| `code`                | `String`      | private     | Código único tipo SKU.                               |
| `monthlyPrice`        | `Money`       | private     | Precio del ciclo mensual.                            |
| `yearlyPrice`         | `Money`       | private     | Precio del ciclo anual (usualmente con descuento).   |
| `maxPatients`         | `Integer`     | private     | Tope de pacientes concurrentes incluidos.            |
| `maxPhysiotherapists` | `Integer`     | private     | Tope de fisioterapeutas incluidos.                   |
| `features`            | `Set<String>` | private     | Funcionalidades incluidas (tags).                    |
| `active`              | `boolean`     | private     | `true` si el plan está disponible para nueva compra. |

**Métodos principales:**

| Método                   | Tipo Retorno | Visibilidad | Descripción                                                          |
|--------------------------|--------------|-------------|----------------------------------------------------------------------|
| `priceFor(BillingCycle)` | `Money`      | public      | Retorna `monthlyPrice` o `yearlyPrice` según el ciclo.               |
| `isActive()`             | `boolean`    | public      | `true` si el plan está activo en el catálogo.                        |
| `deactivate()`           | `void`       | public      | Retira el plan del catálogo (no afecta a suscripciones ya vendidas). |

**3. Invoice (Entity)**

Factura emitida para cada periodo facturable de una suscripción. Una vez emitida es prácticamente inmutable; solo cambia su estado a través de transiciones controladas.

**Atributos principales:**

| Atributo                | Tipo             | Visibilidad | Descripción                                   |
|-------------------------|------------------|-------------|-----------------------------------------------|
| `id`                    | `InvoiceId`      | private     | Identificador de la factura.                  |
| `subscriptionId`        | `SubscriptionId` | private     | Suscripción a la que pertenece.               |
| `amount`                | `Money`          | private     | Monto cobrado.                                |
| `issuedAt`              | `Instant`        | private     | Fecha/hora de emisión.                        |
| `dueAt`                 | `Instant`        | private     | Fecha/hora límite de pago.                    |
| `paidAt`                | `Instant`        | private     | Fecha/hora de confirmación del pago.          |
| `status`                | `InvoiceStatus`  | private     | Estado (`PENDING`, `PAID`, `FAILED`, `VOID`). |
| `providerTransactionId` | `String`         | private     | ID de la transacción en Culqi.                |

**Métodos principales:**

| Método                        | Tipo Retorno | Visibilidad | Descripción                                                                        |
|-------------------------------|--------------|-------------|------------------------------------------------------------------------------------|
| `markAsPaid(String txId)`     | `void`       | public      | Marca la factura como `PAID` y publica `InvoicePaidEvent`.                         |
| `markAsFailed(String reason)` | `void`       | public      | Marca la factura como `FAILED` y publica `InvoicePaymentFailedEvent`.              |
| `voidInvoice()`               | `void`       | public      | Anula la factura (por ejemplo, ante una cancelación dentro del periodo de gracia). |
| `isOverdue()`                 | `boolean`    | public      | `true` si `now > dueAt` y el estado es `PENDING`.                                  |

**4. SubscriptionId / PlanId / InvoiceId / ClinicId (Value Objects)**

Identificadores opacos basados en UUID. `ClinicId` es compartido con el BC IAM (referencia lógica al tenant).

| Atributo | Tipo   | Visibilidad | Descripción                                            |
|----------|--------|-------------|--------------------------------------------------------|
| `value`  | `UUID` | private     | Valor inmutable generado al crear el agregado/entidad. |

**5. Money (Value Object)**

Monto monetario con moneda explícita.

| Atributo   | Tipo         | Visibilidad | Descripción                                                                |
|------------|--------------|-------------|----------------------------------------------------------------------------|
| `amount`   | `BigDecimal` | private     | Valor numérico con precisión suficiente para evitar pérdidas por redondeo. |
| `currency` | `String`     | private     | Código ISO 4217 (por ejemplo `PEN` o `USD`).                               |

**Métodos principales:**

| Método              | Tipo Retorno | Visibilidad | Descripción                     |
|---------------------|--------------|-------------|---------------------------------|
| `plus(Money other)` | `Money`      | public      | Suma dos montos (misma moneda). |
| `isZero()`          | `boolean`    | public      | `true` si el monto es cero.     |

**6. PaymentReference (Value Object)**

Referencia tokenizada al medio de pago registrado en Culqi (uFlex no almacena números de tarjeta).

| Atributo        | Tipo        | Visibilidad | Descripción                                  |
|-----------------|-------------|-------------|----------------------------------------------|
| `providerToken` | `String`    | private     | Token opaco emitido por Culqi.               |
| `last4`         | `String`    | private     | Últimos 4 dígitos (para mostrar al usuario). |
| `expiresOn`     | `YearMonth` | private     | Fecha de expiración de la tarjeta.           |

**7. BillingCycle (Value Object)**

| Atributo  | Tipo | Visibilidad | Descripción                |
|-----------|------|-------------|----------------------------|
| `MONTHLY` | Enum | public      | Cobro mensual recurrente.  |
| `YEARLY`  | Enum | public      | Cobro anual con descuento. |

**8. SubscriptionStatus (Value Object)**

| Atributo          | Tipo | Visibilidad | Descripción                                                   |
|-------------------|------|-------------|---------------------------------------------------------------|
| `PENDING_PAYMENT` | Enum | public      | Suscripción creada, a la espera del primer cobro.             |
| `TRIAL`           | Enum | public      | Periodo de prueba activo.                                     |
| `ACTIVE`          | Enum | public      | Suscripción activa y al día.                                  |
| `PAST_DUE`        | Enum | public      | Falló un cobro recurrente; en periodo de gracia.              |
| `CANCELLED`       | Enum | public      | Cancelada por la clínica; sigue activa hasta fin del periodo. |
| `EXPIRED`         | Enum | public      | Expiró definitivamente.                                       |

**9. InvoiceStatus (Value Object)**

| Atributo  | Tipo | Visibilidad | Descripción                      |
|-----------|------|-------------|----------------------------------|
| `PENDING` | Enum | public      | Factura emitida, pago pendiente. |
| `PAID`    | Enum | public      | Pago confirmado por Culqi.       |
| `FAILED`  | Enum | public      | Pago rechazado por Culqi.        |
| `VOID`    | Enum | public      | Factura anulada.                 |

**10. PurchaseSubscriptionPlanCommand (Command)**

Comando emitido por el Administrador de Clínica al comprar una suscripción.

| Atributo       | Tipo           | Visibilidad | Descripción                                       |
|----------------|----------------|-------------|---------------------------------------------------|
| `clinicId`     | `UUID`         | public      | Tenant que compra.                                |
| `planId`       | `UUID`         | public      | Plan seleccionado.                                |
| `billingCycle` | `BillingCycle` | public      | Ciclo `MONTHLY` o `YEARLY`.                       |
| `paymentToken` | `String`       | public      | Token emitido por el SDK de Culqi en el frontend. |

**11. LinkSubscriptionToClinicCommand (Command)**

| Atributo         | Tipo   | Visibilidad | Descripción            |
|------------------|--------|-------------|------------------------|
| `subscriptionId` | `UUID` | public      | Suscripción a asociar. |
| `clinicId`       | `UUID` | public      | Clínica destino.       |

**12. RenewSubscriptionCommand (Command)**

Emitido por el scheduler cuando llega la `nextBillingDate`.

| Atributo         | Tipo   | Visibilidad | Descripción            |
|------------------|--------|-------------|------------------------|
| `subscriptionId` | `UUID` | public      | Suscripción a renovar. |

**13. CancelSubscriptionCommand (Command)**

| Atributo         | Tipo     | Visibilidad | Descripción                      |
|------------------|----------|-------------|----------------------------------|
| `subscriptionId` | `UUID`   | public      | Suscripción a cancelar.          |
| `reason`         | `String` | public      | Motivo informado por el usuario. |

**14. RegisterInvoicePaymentCommand (Command)**

Emitido por el webhook de Culqi al confirmar un cobro.

| Atributo                | Tipo     | Visibilidad | Descripción                    |
|-------------------------|----------|-------------|--------------------------------|
| `invoiceId`             | `UUID`   | public      | Factura pagada.                |
| `providerTransactionId` | `String` | public      | ID de la transacción en Culqi. |

**15. CreatePlanCommand (Command)**

Usado por Operaciones para crear nuevos planes en el catálogo.

| Atributo              | Tipo      | Visibilidad | Descripción              |
|-----------------------|-----------|-------------|--------------------------|
| `name`                | `String`  | public      | Nombre comercial.        |
| `code`                | `String`  | public      | Código SKU único.        |
| `monthlyPrice`        | `Money`   | public      | Precio mensual.          |
| `yearlyPrice`         | `Money`   | public      | Precio anual.            |
| `maxPatients`         | `Integer` | public      | Tope de pacientes.       |
| `maxPhysiotherapists` | `Integer` | public      | Tope de fisioterapeutas. |

**16. DeactivatePlanCommand (Command)**

| Atributo | Tipo   | Visibilidad | Descripción                  |
|----------|--------|-------------|------------------------------|
| `planId` | `UUID` | public      | Plan a retirar del catálogo. |

**17. ChangeSubscriptionPlanCommand (Command)**

Emitido por el Administrador de Clínica desde el dashboard para hacer upgrade o downgrade a otro plan. El servicio de precios calcula el monto prorrateado para el resto del periodo vigente.

| Atributo          | Tipo              | Visibilidad | Descripción                                                                                                   |
|-------------------|-------------------|-------------|---------------------------------------------------------------------------------------------------------------|
| `subscriptionId`  | `UUID`            | public      | Suscripción a modificar.                                                                                      |
| `newPlanId`       | `UUID`            | public      | Nuevo plan deseado.                                                                                           |
| `newBillingCycle` | `BillingCycle`    | public      | Ciclo de facturación para el nuevo plan (puede coincidir con el anterior).                                    |
| `effectiveAt`     | `EffectivePolicy` | public      | Política de aplicación: `IMMEDIATE` (prorratea y cobra la diferencia) o `AT_NEXT_PERIOD` (aplica al renovar). |

**18. UpdatePaymentMethodCommand (Command)**

Emitido desde el dashboard cuando el Administrador de Clínica actualiza su tarjeta (por ejemplo, tras un vencimiento). El `paymentToken` es emitido por el SDK de Culqi en el frontend y reemplaza al almacenado en el aggregate.

| Atributo         | Tipo     | Visibilidad | Descripción                                  |
|------------------|----------|-------------|----------------------------------------------|
| `subscriptionId` | `UUID`   | public      | Suscripción cuyo medio de pago se actualiza. |
| `paymentToken`   | `String` | public      | Nuevo token tokenizado por Culqi.            |

**19. GetPlanListQuery (Query)**

Consulta usada por la Web Client App para mostrar el catálogo de planes.

| Atributo     | Tipo      | Visibilidad | Descripción                                                                    |
|--------------|-----------|-------------|--------------------------------------------------------------------------------|
| `activeOnly` | `boolean` | public      | Si `true`, solo planes vigentes; si `false`, incluye deprecados (uso interno). |

**20. GetPlanByIdQuery (Query)**

| Atributo | Tipo   | Visibilidad | Descripción  |
|----------|--------|-------------|--------------|
| `planId` | `UUID` | public      | ID del plan. |

**21. GetSubscriptionByIdQuery (Query)**

| Atributo         | Tipo   | Visibilidad | Descripción           |
|------------------|--------|-------------|-----------------------|
| `subscriptionId` | `UUID` | public      | ID de la suscripción. |

**22. GetSubscriptionByClinicQuery (Query)**

| Atributo   | Tipo   | Visibilidad | Descripción                      |
|------------|--------|-------------|----------------------------------|
| `clinicId` | `UUID` | public      | Clínica dueña de la suscripción. |

**23. GetInvoiceHistoryQuery (Query)**

| Atributo         | Tipo   | Visibilidad | Descripción                             |
|------------------|--------|-------------|-----------------------------------------|
| `subscriptionId` | `UUID` | public      | Suscripción cuyo historial se consulta. |

**24. SubscriptionPurchasedEvent (Domain Event)**

Evento publicado al concretarse una compra. Consumido internamente para disparar la emisión de la primera factura.

| Atributo         | Tipo      | Visibilidad | Descripción                |
|------------------|-----------|-------------|----------------------------|
| `subscriptionId` | `UUID`    | private     | Suscripción recién creada. |
| `clinicId`       | `UUID`    | private     | Clínica compradora.        |
| `planId`         | `UUID`    | private     | Plan contratado.           |
| `amount`         | `Money`   | private     | Monto cobrado.             |
| `occurredOn`     | `Instant` | private     | Marca temporal.            |

**25. SubscriptionLinkedToClinicEvent (Domain Event)**

Evento que **consumen otros BCs** (especialmente IAM) para sincronizar el `clinicId` del Administrador de Clínica.

| Atributo         | Tipo      | Visibilidad | Descripción      |
|------------------|-----------|-------------|------------------|
| `subscriptionId` | `UUID`    | private     | Suscripción.     |
| `clinicId`       | `UUID`    | private     | Tenant asociado. |
| `occurredOn`     | `Instant` | private     | Marca temporal.  |

**26. SubscriptionActivatedEvent (Domain Event)**

| Atributo         | Tipo      | Visibilidad | Descripción           |
|------------------|-----------|-------------|-----------------------|
| `subscriptionId` | `UUID`    | private     | Suscripción activada. |
| `clinicId`       | `UUID`    | private     | Tenant.               |
| `activatedAt`    | `Instant` | private     | Marca temporal.       |

**27. SubscriptionRenewedEvent (Domain Event)**

| Atributo         | Tipo        | Visibilidad | Descripción            |
|------------------|-------------|-------------|------------------------|
| `subscriptionId` | `UUID`      | private     | Suscripción renovada.  |
| `newPeriodEnd`   | `LocalDate` | private     | Fin del nuevo periodo. |

**28. SubscriptionCancelledEvent (Domain Event)**

| Atributo         | Tipo      | Visibilidad | Descripción            |
|------------------|-----------|-------------|------------------------|
| `subscriptionId` | `UUID`    | private     | Suscripción cancelada. |
| `reason`         | `String`  | private     | Motivo.                |
| `cancelledAt`    | `Instant` | private     | Marca temporal.        |

**29. InvoicePaidEvent (Domain Event)**

| Atributo    | Tipo      | Visibilidad | Descripción     |
|-------------|-----------|-------------|-----------------|
| `invoiceId` | `UUID`    | private     | Factura pagada. |
| `amount`    | `Money`   | private     | Monto cobrado.  |
| `paidAt`    | `Instant` | private     | Marca temporal. |

**30. InvoicePaymentFailedEvent (Domain Event)**

| Atributo    | Tipo      | Visibilidad | Descripción         |
|-------------|-----------|-------------|---------------------|
| `invoiceId` | `UUID`    | private     | Factura fallida.    |
| `reason`    | `String`  | private     | Motivo del rechazo. |
| `failedAt`  | `Instant` | private     | Marca temporal.     |

**31. SubscriptionPlanChangedEvent (Domain Event)**

Evento publicado al cambiar el plan de una suscripción. Permite al BC Analytics registrar conversiones y a otros contextos ajustar los topes de uso.

| Atributo         | Tipo      | Visibilidad | Descripción                                           |
|------------------|-----------|-------------|-------------------------------------------------------|
| `subscriptionId` | `UUID`    | private     | Suscripción modificada.                               |
| `oldPlanId`      | `UUID`    | private     | Plan anterior.                                        |
| `newPlanId`      | `UUID`    | private     | Plan nuevo.                                           |
| `proratedAmount` | `Money`   | private     | Monto prorrateado cobrado (cero si `AT_NEXT_PERIOD`). |
| `changedAt`      | `Instant` | private     | Marca temporal.                                       |

**32. PaymentMethodUpdatedEvent (Domain Event)**

Evento publicado al actualizar el medio de pago. Consumido por el `NotificationService` para enviar un correo de confirmación al administrador.

| Atributo         | Tipo      | Visibilidad | Descripción                                           |
|------------------|-----------|-------------|-------------------------------------------------------|
| `subscriptionId` | `UUID`    | private     | Suscripción afectada.                                 |
| `last4`          | `String`  | private     | Últimos 4 dígitos de la nueva tarjeta (para mostrar). |
| `updatedAt`      | `Instant` | private     | Marca temporal.                                       |

**33. SubscriptionCommandService (Domain Service)**

Maneja los commands relacionados con el ciclo de vida de la suscripción.

| Método                                    | Tipo Retorno     | Visibilidad | Descripción                                                                                                        |
|-------------------------------------------|------------------|-------------|--------------------------------------------------------------------------------------------------------------------|
| `handle(PurchaseSubscriptionPlanCommand)` | `SubscriptionId` | public      | Ejecuta la compra: llama a Culqi, crea la suscripción y emite la primera factura.                                  |
| `handle(LinkSubscriptionToClinicCommand)` | `void`           | public      | Asocia la suscripción al tenant.                                                                                   |
| `handle(RenewSubscriptionCommand)`        | `void`           | public      | Dispara la renovación recurrente.                                                                                  |
| `handle(CancelSubscriptionCommand)`       | `void`           | public      | Cancela la suscripción con motivo.                                                                                 |
| `handle(RegisterInvoicePaymentCommand)`   | `void`           | public      | Marca una factura como `PAID` tras el webhook de Culqi.                                                            |
| `handle(ChangeSubscriptionPlanCommand)`   | `Money`          | public      | Cambia el plan (upgrade/downgrade); si `IMMEDIATE`, calcula y cobra el prorrateo con `SubscriptionPricingService`. |
| `handle(UpdatePaymentMethodCommand)`      | `void`           | public      | Reemplaza la `PaymentReference` del aggregate con el nuevo token de Culqi.                                         |

**34. SubscriptionQueryService (Domain Service)**

| Método                                 | Tipo Retorno    | Visibilidad | Descripción                                   |
|----------------------------------------|-----------------|-------------|-----------------------------------------------|
| `handle(GetSubscriptionByIdQuery)`     | `Subscription`  | public      | Obtiene una suscripción por ID.               |
| `handle(GetSubscriptionByClinicQuery)` | `Subscription`  | public      | Obtiene la suscripción activa de una clínica. |
| `handle(GetInvoiceHistoryQuery)`       | `List<Invoice>` | public      | Lista las facturas históricas.                |

**35. PlanCommandService (Domain Service)**

| Método                          | Tipo Retorno | Visibilidad | Descripción                        |
|---------------------------------|--------------|-------------|------------------------------------|
| `handle(CreatePlanCommand)`     | `PlanId`     | public      | Crea un nuevo plan en el catálogo. |
| `handle(DeactivatePlanCommand)` | `void`       | public      | Retira un plan del catálogo.       |

**36. PlanQueryService (Domain Service)**

| Método                     | Tipo Retorno             | Visibilidad | Descripción                   |
|----------------------------|--------------------------|-------------|-------------------------------|
| `handle(GetPlanListQuery)` | `List<SubscriptionPlan>` | public      | Lista los planes disponibles. |
| `handle(GetPlanByIdQuery)` | `SubscriptionPlan`       | public      | Obtiene un plan por ID.       |

**37. SubscriptionPricingService (Domain Service)**

Servicio puro de cálculo de precios y prorrateo.

| Método                                           | Tipo Retorno | Visibilidad | Descripción                                                         |
|--------------------------------------------------|--------------|-------------|---------------------------------------------------------------------|
| `priceFor(SubscriptionPlan, BillingCycle)`       | `Money`      | public      | Calcula el precio del plan para el ciclo indicado.                  |
| `applyProration(Subscription, SubscriptionPlan)` | `Money`      | public      | Calcula el monto prorrateado al cambiar de plan a mitad de periodo. |

#### 4.2.2.2. Interface Layer

**1. SubscriptionController (REST Controller)**

Expone los casos de uso de suscripción al Administrador de Clínica a través de la PWA.

| Método                    | Ruta base                                   | HTTP  | Descripción                                                                         |
|---------------------------|---------------------------------------------|-------|-------------------------------------------------------------------------------------|
| `purchaseSubscription`    | `/api/v1/subscriptions`                     | POST  | Compra un plan usando un `paymentToken` emitido por el SDK de Culqi en el frontend. |
| `getSubscriptionByClinic` | `/api/v1/subscriptions?clinicId={id}`       | GET   | Obtiene la suscripción activa de la clínica.                                        |
| `cancelSubscription`      | `/api/v1/subscriptions/{id}/cancel`         | POST  | Cancela una suscripción vigente.                                                    |
| `changePlan`              | `/api/v1/subscriptions/{id}/plan`           | PATCH | Cambia el plan (upgrade/downgrade) con política `IMMEDIATE` o `AT_NEXT_PERIOD`.     |
| `updatePaymentMethod`     | `/api/v1/subscriptions/{id}/payment-method` | PATCH | Actualiza el medio de pago con un nuevo token de Culqi.                             |
| `getInvoiceHistory`       | `/api/v1/subscriptions/{id}/invoices`       | GET   | Devuelve el historial de facturas.                                                  |

**2. PlanController (REST Controller)**

Catálogo público de planes visible en la landing y en la PWA.

| Método           | Ruta base            | HTTP   | Descripción                                         |
|------------------|----------------------|--------|-----------------------------------------------------|
| `getPlans`       | `/api/v1/plans`      | GET    | Lista los planes activos del catálogo.              |
| `getPlanById`    | `/api/v1/plans/{id}` | GET    | Obtiene un plan específico.                         |
| `createPlan`     | `/api/v1/plans`      | POST   | Crea un plan (operación de administración interna). |
| `deactivatePlan` | `/api/v1/plans/{id}` | DELETE | Retira un plan del catálogo.                        |

**3. CulqiWebhookController (REST Controller)**

Endpoint que recibe los eventos de Culqi (confirmación de cargo, fallo de cobro). Valida la firma HMAC antes de procesar.

| Método          | Ruta base                | HTTP | Descripción                                                                                                 |
|-----------------|--------------------------|------|-------------------------------------------------------------------------------------------------------------|
| `handleWebhook` | `/api/v1/webhooks/culqi` | POST | Recibe y procesa eventos de Culqi; traduce a `RegisterInvoicePaymentCommand` o `InvoicePaymentFailedEvent`. |

**4. Resources (DTOs)**

| Resource                       | Atributos principales                                                                                                                                                                                   | Descripción                                                |
|--------------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|------------------------------------------------------------|
| `PurchaseSubscriptionResource` | `clinicId: UUID`, `planId: UUID`, `billingCycle: String`, `paymentToken: String`                                                                                                                        | Datos de compra recibidos desde el frontend.               |
| `SubscriptionResource`         | `id: UUID`, `clinicId: UUID`, `planName: String`, `status: String`, `billingCycle: String`, `currentPeriodEnd: LocalDate`, `nextBillingDate: LocalDate`                                                 | Representación de una suscripción.                         |
| `PlanResource`                 | `id: UUID`, `name: String`, `code: String`, `monthlyPrice: BigDecimal`, `yearlyPrice: BigDecimal`, `currency: String`, `maxPatients: Integer`, `maxPhysiotherapists: Integer`, `features: List<String>` | Representación de un plan.                                 |
| `InvoiceResource`              | `id: UUID`, `amount: BigDecimal`, `currency: String`, `issuedAt: Instant`, `dueAt: Instant`, `paidAt: Instant`, `status: String`                                                                        | Representación de una factura.                             |
| `CancelSubscriptionResource`   | `reason: String`                                                                                                                                                                                        | Motivo de cancelación.                                     |
| `ChangePlanResource`           | `newPlanId: UUID`, `newBillingCycle: String`, `effectiveAt: String`                                                                                                                                     | Datos para cambiar de plan desde el dashboard.             |
| `UpdatePaymentMethodResource`  | `paymentToken: String`                                                                                                                                                                                  | Nuevo token tokenizado por el SDK de Culqi en el frontend. |

**5. Transform (Assemblers)**

| Assembler                                            | Entrada                                               | Salida                            | Descripción                                              |
|------------------------------------------------------|-------------------------------------------------------|-----------------------------------|----------------------------------------------------------|
| `PurchaseSubscriptionCommandFromResourceAssembler`   | `PurchaseSubscriptionResource`                        | `PurchaseSubscriptionPlanCommand` | Construye el command de compra.                          |
| `SubscriptionResourceFromEntityAssembler`            | `Subscription`                                        | `SubscriptionResource`            | Expone el aggregate como recurso.                        |
| `PlanResourceFromEntityAssembler`                    | `SubscriptionPlan`                                    | `PlanResource`                    | Expone el plan como recurso.                             |
| `InvoiceResourceFromEntityAssembler`                 | `Invoice`                                             | `InvoiceResource`                 | Expone la factura como recurso.                          |
| `CancelSubscriptionCommandFromResourceAssembler`     | `CancelSubscriptionResource`, `subscriptionId: UUID`  | `CancelSubscriptionCommand`       | Construye el command de cancelación.                     |
| `ChangeSubscriptionPlanCommandFromResourceAssembler` | `ChangePlanResource`, `subscriptionId: UUID`          | `ChangeSubscriptionPlanCommand`   | Construye el command de cambio de plan.                  |
| `UpdatePaymentMethodCommandFromResourceAssembler`    | `UpdatePaymentMethodResource`, `subscriptionId: UUID` | `UpdatePaymentMethodCommand`      | Construye el command de actualización del medio de pago. |

#### 4.2.2.3. Application Layer

**1. SubscriptionContextFacadeImpl (ACL Facade)**

Fachada que otros bounded contexts usan para consultar estado de suscripción sin acoplarse al modelo interno.

| Atributo                   | Tipo                       | Visibilidad | Descripción                            |
|----------------------------|----------------------------|-------------|----------------------------------------|
| `subscriptionQueryService` | `SubscriptionQueryService` | private     | Servicio de consultas de Subscription. |

| Método                                      | Tipo Retorno       | Visibilidad | Descripción                                                    |
|---------------------------------------------|--------------------|-------------|----------------------------------------------------------------|
| `isClinicSubscriptionActive(UUID clinicId)` | `boolean`          | public      | Responde si el tenant tiene una suscripción vigente.           |
| `fetchCurrentPlanCode(UUID clinicId)`       | `Optional<String>` | public      | Retorna el código del plan contratado para gating de features. |

**2. SubscriptionCommandServiceImpl**

Orquesta la compra, renovación, cancelación y reconciliación de pagos.

| Atributo                 | Tipo                         | Visibilidad | Descripción                   |
|--------------------------|------------------------------|-------------|-------------------------------|
| `subscriptionRepository` | `SubscriptionRepository`     | private     | Persistencia del aggregate.   |
| `planRepository`         | `PlanRepository`             | private     | Consulta del catálogo.        |
| `invoiceRepository`      | `InvoiceRepository`          | private     | Persistencia de facturas.     |
| `culqiPaymentPort`       | `CulqiPaymentPort`           | private     | ACL hacia la pasarela.        |
| `iamContextFacadePort`   | `IamContextFacadePort`       | private     | Consulta del BC IAM.          |
| `pricingService`         | `SubscriptionPricingService` | private     | Cálculo de precios.           |
| `eventPublisher`         | `ApplicationEventPublisher`  | private     | Publicación de domain events. |

| Método                                    | Tipo Retorno     | Visibilidad | Descripción                                                                                                                                                                                                                           |
|-------------------------------------------|------------------|-------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `handle(PurchaseSubscriptionPlanCommand)` | `SubscriptionId` | public      | Valida admin, tokeniza el pago, crea la suscripción, emite factura y publica `SubscriptionPurchasedEvent`.                                                                                                                            |
| `handle(LinkSubscriptionToClinicCommand)` | `void`           | public      | Asocia la suscripción al tenant y publica `SubscriptionLinkedToClinicEvent`.                                                                                                                                                          |
| `handle(RenewSubscriptionCommand)`        | `void`           | public      | Genera factura y cobra vía Culqi; maneja fallos con reintentos.                                                                                                                                                                       |
| `handle(CancelSubscriptionCommand)`       | `void`           | public      | Marca la suscripción como `CANCELLED` y publica el evento.                                                                                                                                                                            |
| `handle(RegisterInvoicePaymentCommand)`   | `void`           | public      | Marca la factura como `PAID` tras el webhook de Culqi.                                                                                                                                                                                |
| `handle(ChangeSubscriptionPlanCommand)`   | `Money`          | public      | Cambia de plan: si `IMMEDIATE`, usa `SubscriptionPricingService.applyProration` y cobra la diferencia vía `CulqiPaymentPort`; si `AT_NEXT_PERIOD`, difiere el cambio a la próxima renovación. Publica `SubscriptionPlanChangedEvent`. |
| `handle(UpdatePaymentMethodCommand)`      | `void`           | public      | Tokeniza la nueva tarjeta con `CulqiPaymentPort`, reemplaza la `PaymentReference` en el aggregate y publica `PaymentMethodUpdatedEvent`.                                                                                              |

**3. SubscriptionQueryServiceImpl**

| Atributo                 | Tipo                     | Visibilidad | Descripción            |
|--------------------------|--------------------------|-------------|------------------------|
| `subscriptionRepository` | `SubscriptionRepository` | private     | Lectura del aggregate. |
| `invoiceRepository`      | `InvoiceRepository`      | private     | Lectura de facturas.   |

| Método                                 | Tipo Retorno    | Visibilidad | Descripción                                    |
|----------------------------------------|-----------------|-------------|------------------------------------------------|
| `handle(GetSubscriptionByIdQuery)`     | `Subscription`  | public      | Recupera una suscripción por ID.               |
| `handle(GetSubscriptionByClinicQuery)` | `Subscription`  | public      | Recupera la suscripción activa de una clínica. |
| `handle(GetInvoiceHistoryQuery)`       | `List<Invoice>` | public      | Historial de facturas.                         |

**4. PlanCommandServiceImpl**

| Atributo         | Tipo             | Visibilidad | Descripción         |
|------------------|------------------|-------------|---------------------|
| `planRepository` | `PlanRepository` | private     | Acceso al catálogo. |

| Método                          | Tipo Retorno | Visibilidad | Descripción                  |
|---------------------------------|--------------|-------------|------------------------------|
| `handle(CreatePlanCommand)`     | `PlanId`     | public      | Crea un plan nuevo.          |
| `handle(DeactivatePlanCommand)` | `void`       | public      | Retira un plan del catálogo. |

**5. PlanQueryServiceImpl**

| Método                     | Tipo Retorno             | Visibilidad | Descripción             |
|----------------------------|--------------------------|-------------|-------------------------|
| `handle(GetPlanListQuery)` | `List<SubscriptionPlan>` | public      | Lista los planes.       |
| `handle(GetPlanByIdQuery)` | `SubscriptionPlan`       | public      | Obtiene un plan por ID. |

**6. CulqiWebhookEventHandler (Integration Event Handler)**

Procesa los eventos de Culqi recibidos por el `CulqiWebhookController`.

| Método                     | Tipo Retorno | Visibilidad | Descripción                                                                                |
|----------------------------|--------------|-------------|--------------------------------------------------------------------------------------------|
| `on(CulqiChargeSucceeded)` | `void`       | public      | Traduce a `RegisterInvoicePaymentCommand` y lo despacha al Command Service.                |
| `on(CulqiChargeFailed)`    | `void`       | public      | Publica `InvoicePaymentFailedEvent` y marca la suscripción como `PAST_DUE` si corresponde. |

**7. SubscriptionRenewalScheduler (Scheduled Task)**

Job programado que detecta suscripciones cuyo `nextBillingDate` venció y dispara `RenewSubscriptionCommand`.

| Método                   | Tipo Retorno | Visibilidad | Descripción                                                                                                                 |
|--------------------------|--------------|-------------|-----------------------------------------------------------------------------------------------------------------------------|
| `runDailyRenewalBatch()` | `void`       | public      | Ejecuta `@Scheduled(cron)`; recorre las suscripciones con `nextBillingDate <= today` y despacha los commands de renovación. |

**8. SubscriptionExpirationScheduler (Scheduled Task)**

| Método                      | Tipo Retorno | Visibilidad | Descripción                                                                    |
|-----------------------------|--------------|-------------|--------------------------------------------------------------------------------|
| `runDailyExpirationBatch()` | `void`       | public      | Marca como `EXPIRED` las suscripciones que llevan más de N días en `PAST_DUE`. |

**9. ApplicationReadyEventHandler (Framework Event Handler)**

| Método                      | Tipo Retorno | Visibilidad | Descripción                                                              |
|-----------------------------|--------------|-------------|--------------------------------------------------------------------------|
| `on(ApplicationReadyEvent)` | `void`       | public      | Ejecuta la semilla de planes base al arranque si el catálogo está vacío. |

**10. CulqiPaymentPort (Outbound Service Port)**

ACL hacia Culqi.

| Método                                                     | Tipo Retorno       | Visibilidad | Descripción                                                |
|------------------------------------------------------------|--------------------|-------------|------------------------------------------------------------|
| `createCharge(Money amount, String token)`                 | `PaymentReference` | public      | Crea un cargo contra el token emitido por el SDK de Culqi. |
| `refundCharge(PaymentReference)`                           | `void`             | public      | Reversa un cobro.                                          |
| `verifyWebhookSignature(String payload, String signature)` | `boolean`          | public      | Valida la firma HMAC del webhook de Culqi.                 |

**11. IamContextFacadePort (Outbound Service Port)**

Puerto hacia la fachada del BC IAM para validar el usuario que compra.

| Método                              | Tipo Retorno | Visibilidad | Descripción                                          |
|-------------------------------------|--------------|-------------|------------------------------------------------------|
| `fetchClinicIdOfAdmin(Long userId)` | `UUID`       | public      | Obtiene el `clinicId` del administrador autenticado. |
| `validateClinicAdmin(Long userId)`  | `boolean`    | public      | Verifica que el usuario tenga el rol `CLINIC_ADMIN`. |

**12. NotificationServicePort (Outbound Service Port)**

Puerto hacia el Notification Service hermano para enviar correos (que a su vez usa Resend).

| Método                                                                  | Tipo Retorno | Visibilidad | Descripción                                                    |
|-------------------------------------------------------------------------|--------------|-------------|----------------------------------------------------------------|
| `sendInvoiceIssuedNotification(String email, Invoice invoice)`          | `void`       | public      | Solicita al Notification Service enviar la factura por correo. |
| `sendPaymentFailedNotification(String email, Invoice invoice)`          | `void`       | public      | Notifica al admin que un cobro falló.                          |
| `sendSubscriptionCancelledNotification(String email, Subscription sub)` | `void`       | public      | Notifica la cancelación.                                       |

#### 4.2.2.4. Infrastructure Layer

**1. SubscriptionRepository (Repository Interface)**

Spring Data JPA sobre Azure Database for PostgreSQL.

| Método                                  | Tipo Retorno             | Visibilidad | Descripción                                                                |
|-----------------------------------------|--------------------------|-------------|----------------------------------------------------------------------------|
| `save(Subscription sub)`                | `Subscription`           | public      | Persiste o actualiza una suscripción.                                      |
| `findById(UUID id)`                     | `Optional<Subscription>` | public      | Busca por ID.                                                              |
| `findByClinicId(UUID clinicId)`         | `Optional<Subscription>` | public      | Obtiene la suscripción activa de un tenant.                                |
| `findAllPastDue()`                      | `List<Subscription>`     | public      | Lista las suscripciones `PAST_DUE` (usado por el scheduler de expiración). |
| `findAllDueForRenewal(LocalDate today)` | `List<Subscription>`     | public      | Lista las suscripciones con `nextBillingDate <= today`.                    |

**2. PlanRepository (Repository Interface)**

| Método                        | Tipo Retorno                 | Visibilidad | Descripción               |
|-------------------------------|------------------------------|-------------|---------------------------|
| `save(SubscriptionPlan plan)` | `SubscriptionPlan`           | public      | Persiste un plan.         |
| `findById(UUID id)`           | `Optional<SubscriptionPlan>` | public      | Busca por ID.             |
| `findAllActive()`             | `List<SubscriptionPlan>`     | public      | Lista los planes activos. |
| `findByCode(String code)`     | `Optional<SubscriptionPlan>` | public      | Busca por código SKU.     |

**3. InvoiceRepository (Repository Interface)**

| Método                          | Tipo Retorno        | Visibilidad | Descripción                                      |
|---------------------------------|---------------------|-------------|--------------------------------------------------|
| `save(Invoice invoice)`         | `Invoice`           | public      | Persiste una factura.                            |
| `findById(UUID id)`             | `Optional<Invoice>` | public      | Busca por ID.                                    |
| `findBySubscriptionId(UUID id)` | `List<Invoice>`     | public      | Lista las facturas de una suscripción.           |
| `findAllOverdue()`              | `List<Invoice>`     | public      | Lista las facturas vencidas pendientes de cobro. |

**4. CulqiPaymentAdapter (ACL Adapter)**

Implementa `CulqiPaymentPort`. Único componente que conoce el vocabulario de Culqi.

| Atributo        | Tipo              | Visibilidad | Descripción                                            |
|-----------------|-------------------|-------------|--------------------------------------------------------|
| `culqiClient`   | `CulqiHttpClient` | private     | Cliente HTTP hacia la API pública de Culqi.            |
| `webhookSecret` | `String`          | private     | Secreto compartido con Culqi para validar firmas HMAC. |

| Método                                                     | Tipo Retorno       | Visibilidad | Descripción                                              |
|------------------------------------------------------------|--------------------|-------------|----------------------------------------------------------|
| `createCharge(Money amount, String token)`                 | `PaymentReference` | public      | Invoca `POST /v2/charges` de Culqi y mapea la respuesta. |
| `refundCharge(PaymentReference)`                           | `void`             | public      | Invoca `POST /v2/refunds`.                               |
| `verifyWebhookSignature(String payload, String signature)` | `boolean`          | public      | Valida la firma HMAC-SHA256 del webhook.                 |

**5. CulqiHttpClient (HTTP Client)**

Wrapper de la API REST de Culqi usando `WebClient` de Spring.

| Atributo     | Tipo        | Visibilidad | Descripción                                             |
|--------------|-------------|-------------|---------------------------------------------------------|
| `webClient`  | `WebClient` | private     | Cliente HTTP reactivo.                                  |
| `privateKey` | `String`    | private     | Clave privada de Culqi (cargada desde Azure Key Vault). |

| Método                           | Tipo Retorno | Visibilidad | Descripción                            |
|----------------------------------|--------------|-------------|----------------------------------------|
| `post(String path, Object body)` | `JsonNode`   | public      | Ejecuta una petición POST autenticada. |
| `get(String path)`               | `JsonNode`   | public      | Ejecuta una petición GET autenticada.  |

**6. IamContextFacadeAdapter (ACL Adapter)**

Implementa `IamContextFacadePort` invocando al IAM Service hermano vía HTTP interno.

| Método                              | Tipo Retorno | Visibilidad | Descripción                                        |
|-------------------------------------|--------------|-------------|----------------------------------------------------|
| `fetchClinicIdOfAdmin(Long userId)` | `UUID`       | public      | Llama a `GET /api/v1/users/{id}` del IAM Service.  |
| `validateClinicAdmin(Long userId)`  | `boolean`    | public      | Valida que el usuario tenga el rol `CLINIC_ADMIN`. |

**7. NotificationServiceAdapter (ACL Adapter)**

Implementa `NotificationServicePort` invocando al Notification Service hermano.

| Método                                              | Tipo Retorno | Visibilidad | Descripción                                                                                 |
|-----------------------------------------------------|--------------|-------------|---------------------------------------------------------------------------------------------|
| `sendInvoiceIssuedNotification(email, invoice)`     | `void`       | public      | Encola una petición contra el Notification Service que a su vez envía el correo vía Resend. |
| `sendPaymentFailedNotification(email, invoice)`     | `void`       | public      | Notifica fallo de cobro.                                                                    |
| `sendSubscriptionCancelledNotification(email, sub)` | `void`       | public      | Notifica cancelación.                                                                       |

**8. SubscriptionScheduler (Spring Scheduled)**

Implementación del scheduler basado en Spring `@Scheduled` con cron expressions configurables.

| Método                      | Tipo Retorno | Visibilidad | Descripción                                                   |
|-----------------------------|--------------|-------------|---------------------------------------------------------------|
| `runDailyRenewalBatch()`    | `void`       | public      | `@Scheduled(cron = "${uflex.subscription.renewal-cron}")`.    |
| `runDailyExpirationBatch()` | `void`       | public      | `@Scheduled(cron = "${uflex.subscription.expiration-cron}")`. |

**9. CulqiWebhookController (Integration Controller)**

Endpoint REST que recibe los webhooks de Culqi.

| Método                                         | Tipo Retorno           | Visibilidad | Descripción                                                                            |
|------------------------------------------------|------------------------|-------------|----------------------------------------------------------------------------------------|
| `handleWebhook(String body, String signature)` | `ResponseEntity<Void>` | public      | Valida la firma HMAC, deserializa el evento y delega en el `CulqiWebhookEventHandler`. |

**10. PlanSeederProperties (Configuration Properties)**

Propiedades externas con los planes base que se insertan en un ambiente nuevo.

| Campo       | Tipo             | Visibilidad | Descripción                            |
|-------------|------------------|-------------|----------------------------------------|
| `seedPlans` | `List<PlanSeed>` | private     | Lista de planes a sembrar al arranque. |

#### 4.2.2.5. Bounded Context Software Architecture Component Level Diagrams

El diagrama de componentes del BC Subscription replica el patrón de capas del BC IAM y añade dos Anti-Corruption Layers diferenciadas: **Culqi Payments Service ACL** (contra la pasarela de pagos externa) e **IAM Context Service ACL** (contra el BC IAM hermano, para validar el rol `CLINIC_ADMIN` y obtener el `clinicId` asociado al administrador autenticado). El Infrastructure Layer, además de persistir contra la Subscription DB, también se comunica con el Notification Service hermano para enviar facturas y avisos de cobro fallido por correo. La capa Domain permanece aislada de todas las integraciones externas.

<div style="text-align: center;">
  <img src="assets/diagrams/software-architecture/components/out/subscription-components-diagram.png" alt="uFlex — Subscription Bounded Context Component Diagram" style="max-width: 100%; height: auto;">
</div>

*Figura 4.2.2.5. Diagrama de componentes (C4 Nivel 3) del Bounded Context Subscription.*

#### 4.2.2.6. Bounded Context Software Architecture Code Level Diagrams

##### 4.2.2.6.1. Bounded Context Domain Layer Class Diagrams

El diagrama de clases del Domain Layer del BC Subscription modela exclusivamente los conceptos centrales del dominio, sin incluir las capas de application ni infrastructure. El Aggregate Root `Subscription` está compuesto por la Entity `SubscriptionPlan` (referencia al catálogo) y una colección de Entities `Invoice` (historial de facturas emitidas). Los Value Objects modelan los conceptos monetarios (`Money`, `PaymentReference`) y temporales (`BillingCycle`) así como los estados (`SubscriptionStatus`, `InvoiceStatus`). Los Domain Events publicados (`SubscriptionPurchasedEvent`, `SubscriptionLinkedToClinicEvent`, `SubscriptionActivatedEvent`, `SubscriptionRenewedEvent`, `SubscriptionCancelledEvent`, `InvoicePaidEvent`, `InvoicePaymentFailedEvent`) permiten que el BC IAM (sincronización del `clinicId`), el Notification Service (envío de correos) y la analítica interna reaccionen sin acoplamiento directo al aggregate. El único Domain Service en sentido estricto es `SubscriptionPricingService`, que encapsula la lógica de cálculo de precios y prorrateo entre planes y ciclos de facturación. El paquete `domain.exceptions` reúne las excepciones de negocio que protegen las invariantes del aggregate.

<div style="text-align: center;">
  <img src="assets/diagrams/uml/class/out/subscription-domain-layer-class-diagram.png" alt="uFlex — Subscription Bounded Context Domain Class Diagram" style="max-width: 100%; height: auto;">
</div>

*Figura 4.2.2.6.1. Diagrama de clases del dominio del Bounded Context Subscription.*

##### 4.2.2.6.2. Bounded Context Database Design Diagram

El esquema físico del BC Subscription en Azure Database for PostgreSQL está compuesto por tres tablas principales: `subscription_plans` (catálogo con precios mensuales y anuales, topes de pacientes y fisioterapeutas, y estado activo/inactivo), `subscriptions` (suscripción por tenant, con FK lógica a `subscription_plans`, estado, ventanas del periodo actual, fecha de próxima facturación, referencia de pago tokenizada en Culqi y `clinic_id` como referencia lógica al BC IAM sin FK dura) e `invoices` (facturas emitidas por cada periodo, con FK a `subscriptions`, monto, estado y `provider_transaction_id` para reconciliación con Culqi). Se complementa con dos tablas de catálogo (`subscription_statuses` e `invoice_statuses`) para normalizar los enumerados, e índices compuestos por `(clinic_id, status)` y `(status, next_billing_date)` para soportar las queries más frecuentes (consulta de suscripción activa por clínica y detección de renovaciones vencidas por el scheduler).

<div style="text-align: center;">
  <img src="assets/diagrams/database/erd/out/subscription-database-design-diagram.png" alt="uFlex — Subscription Bounded Context Database ER Diagram" style="max-width: 100%; height: auto;">
</div>

*Figura 4.2.2.6.2. Diagrama entidad-relación del Bounded Context Subscription.*

<hr class="page-break">

### 4.2.3. Bounded Context: Organization

<p>El bounded context <strong>Organization</strong> concentra la información organizacional y el perfil enriquecido de cada actor clínico registrado en uFlex. Mientras que el BC IAM resuelve la identidad técnica del usuario (autenticación, roles y ciclo de vida de la cuenta) y el BC Subscription gobierna el contrato comercial del tenant, el BC Organization se encarga de representar a la <em>clínica como organización</em> (denominación legal, RUC, sedes, datos de contacto, logotipo, horarios) y de mantener el <em>perfil personal y clínico</em> de los usuarios asociados a esa clínica (fisioterapeutas con su número de colegiatura y especialidad, pacientes con sus datos demográficos, contacto de emergencia y breve historial clínico, y administradores con su ámbito de gestión). Este contexto es, por tanto, la fuente autoritativa del <code>ClinicId</code> referenciado lógicamente por el resto de bounded contexts y del árbol de <code>Branches</code> (sedes) sobre el que operan Planning, Device y Therapy. Los comandos y eventos principales (<code>RegisterClinicCommand</code>, <code>AddBranchCommand</code>, <code>RegisterPhysiotherapistProfileCommand</code>, <code>RegisterPatientProfileCommand</code>, <code>AssignPatientToPhysiotherapistCommand</code>, <code>ClinicRegisteredEvent</code>, <code>ClinicActivatedEvent</code>, <code>BranchAddedEvent</code>, <code>PhysiotherapistProfileRegisteredEvent</code>, <code>PatientProfileRegisteredEvent</code>, <code>PatientAssignedToPhysiotherapistEvent</code>) fueron identificados durante el Design-Level EventStorming.</p>

#### 4.2.3.1. Domain Layer

<p>En esta sección se describen los elementos del Domain Layer del contexto de Organization, que modelan la estructura interna de la clínica multi-tenant y el perfil enriquecido de sus usuarios. Las invariantes clave son: una clínica no puede registrarse sin al menos una sede principal, el RUC de una clínica es único dentro de la plataforma, un paciente debe pertenecer a una única clínica a la vez y sólo puede ser asignado a un fisioterapeuta que forme parte de la misma clínica, y el perfil clínico del fisioterapeuta exige un número de colegiatura válido antes de pasar al estado <code>ACTIVE</code>.</p>

<p><strong>1. Clinic (Aggregate Root)</strong></p>

<p>Representa a la clínica (tenant) dentro del dominio: su identidad organizacional, sus datos fiscales y de contacto, sus sedes y el estado operativo. Encapsula la política de multi-sede y es la raíz a partir de la cual se accede a <code>Branch</code> y al directorio de perfiles clínicos.</p>

<p>Atributos principales:</p>

<table>
  <thead>
    <tr><th>Atributo</th><th>Tipo</th><th>Visibilidad</th><th>Descripción</th></tr>
  </thead>
  <tbody>
    <tr><td><code>id</code></td><td><code>ClinicId</code></td><td>private</td><td>Identificador único de la clínica (UUID). Es el valor que el resto de bounded contexts referencia de forma lógica.</td></tr>
    <tr><td><code>legalName</code></td><td><code>LegalName</code></td><td>private</td><td>Razón social registrada de la clínica.</td></tr>
    <tr><td><code>commercialName</code></td><td><code>CommercialName</code></td><td>private</td><td>Nombre comercial visible al paciente en la PWA y en la app móvil.</td></tr>
    <tr><td><code>taxId</code></td><td><code>TaxId</code></td><td>private</td><td>RUC (u otro identificador tributario) de la clínica; único por tenant.</td></tr>
    <tr><td><code>contactInfo</code></td><td><code>ContactInfo</code></td><td>private</td><td>Correo corporativo, teléfono y sitio web públicos de la clínica.</td></tr>
    <tr><td><code>logoUrl</code></td><td><code>LogoUrl</code></td><td>private</td><td>URL del logotipo en Azure Blob Storage, utilizado para personalizar la PWA.</td></tr>
    <tr><td><code>branches</code></td><td><code>List&lt;Branch&gt;</code></td><td>private</td><td>Sedes físicas operadas por la clínica; al menos una es <code>isHeadquarters = true</code>.</td></tr>
    <tr><td><code>status</code></td><td><code>ClinicStatus</code></td><td>private</td><td>Estado del tenant (<code>PENDING_ACTIVATION</code>, <code>ACTIVE</code>, <code>SUSPENDED</code>, <code>ARCHIVED</code>).</td></tr>
    <tr><td><code>createdBy</code></td><td><code>UserId</code></td><td>private</td><td>Identificador del <em>Administrador de Clínica</em> (BC IAM) que realizó el registro inicial.</td></tr>
    <tr><td><code>createdAt</code></td><td><code>Instant</code></td><td>private</td><td>Fecha y hora de alta del tenant.</td></tr>
    <tr><td><code>updatedAt</code></td><td><code>Instant</code></td><td>private</td><td>Fecha y hora de la última actualización organizativa.</td></tr>
  </tbody>
</table>

<p>Métodos principales:</p>

<table>
  <thead>
    <tr><th>Método</th><th>Tipo Retorno</th><th>Visibilidad</th><th>Descripción</th></tr>
  </thead>
  <tbody>
    <tr><td><code>Clinic()</code></td><td>Constructor</td><td>public</td><td>Constructor vacío requerido por JPA.</td></tr>
    <tr><td><code>Clinic(LegalName, CommercialName, TaxId, ContactInfo, UserId createdBy)</code></td><td>Constructor</td><td>public</td><td>Registra una clínica en estado <code>PENDING_ACTIVATION</code> y publica <code>ClinicRegisteredEvent</code>.</td></tr>
    <tr><td><code>addBranch(Branch branch)</code></td><td><code>void</code></td><td>public</td><td>Agrega una nueva sede; valida unicidad del nombre dentro del tenant y publica <code>BranchAddedEvent</code>.</td></tr>
    <tr><td><code>designateHeadquarters(BranchId branchId)</code></td><td><code>void</code></td><td>public</td><td>Marca una sede existente como sede central; sólo una sede puede ser <code>isHeadquarters = true</code> simultáneamente.</td></tr>
    <tr><td><code>activate()</code></td><td><code>void</code></td><td>public</td><td>Cambia el estado a <code>ACTIVE</code> al confirmarse la suscripción; publica <code>ClinicActivatedEvent</code>.</td></tr>
    <tr><td><code>suspend(String reason)</code></td><td><code>void</code></td><td>public</td><td>Cambia el estado a <code>SUSPENDED</code> (por ejemplo, ante una suscripción <code>PAST_DUE</code>); publica <code>ClinicSuspendedEvent</code>.</td></tr>
    <tr><td><code>updateContactInfo(ContactInfo contactInfo)</code></td><td><code>void</code></td><td>public</td><td>Actualiza los datos de contacto públicos de la clínica.</td></tr>
    <tr><td><code>updateLogo(LogoUrl logoUrl)</code></td><td><code>void</code></td><td>public</td><td>Reemplaza la URL del logotipo tras un upload válido al blob.</td></tr>
    <tr><td><code>archive()</code></td><td><code>void</code></td><td>public</td><td>Transición final al estado <code>ARCHIVED</code>; bloquea todo acceso operativo.</td></tr>
  </tbody>
</table>

<p><strong>2. Branch (Entity)</strong></p>

<p>Representa una sede física de la clínica. Es una Entity dentro del aggregate <code>Clinic</code>, por lo que su ciclo de vida se gobierna desde el aggregate root.</p>

<table>
  <thead>
    <tr><th>Atributo</th><th>Tipo</th><th>Visibilidad</th><th>Descripción</th></tr>
  </thead>
  <tbody>
    <tr><td><code>id</code></td><td><code>BranchId</code></td><td>private</td><td>Identificador único de la sede.</td></tr>
    <tr><td><code>name</code></td><td><code>BranchName</code></td><td>private</td><td>Nombre operativo (por ejemplo, "Sede San Isidro").</td></tr>
    <tr><td><code>address</code></td><td><code>Address</code></td><td>private</td><td>Dirección estructurada (calle, distrito, provincia, departamento, país, código postal).</td></tr>
    <tr><td><code>phoneNumber</code></td><td><code>PhoneNumber</code></td><td>private</td><td>Teléfono local de la sede.</td></tr>
    <tr><td><code>openingHours</code></td><td><code>OpeningHours</code></td><td>private</td><td>Horario de atención por día de la semana.</td></tr>
    <tr><td><code>isHeadquarters</code></td><td><code>boolean</code></td><td>private</td><td>Indica si la sede es la central.</td></tr>
    <tr><td><code>status</code></td><td><code>BranchStatus</code></td><td>private</td><td>Estado operativo (<code>ACTIVE</code>, <code>INACTIVE</code>).</td></tr>
  </tbody>
</table>

<p><strong>3. PhysiotherapistProfile (Aggregate Root)</strong></p>

<p>Representa el perfil clínico-personal de un fisioterapeuta dentro de la clínica. El <code>userId</code> referencia lógicamente al usuario en el BC IAM; aquí se añaden los datos que hacen al especialista profesional y laboralmente reconocible.</p>

<table>
  <thead>
    <tr><th>Atributo</th><th>Tipo</th><th>Visibilidad</th><th>Descripción</th></tr>
  </thead>
  <tbody>
    <tr><td><code>id</code></td><td><code>PhysiotherapistProfileId</code></td><td>private</td><td>Identificador del perfil.</td></tr>
    <tr><td><code>userId</code></td><td><code>UserId</code></td><td>private</td><td>Referencia lógica al usuario en el BC IAM.</td></tr>
    <tr><td><code>clinicId</code></td><td><code>ClinicId</code></td><td>private</td><td>Clínica a la que pertenece el fisioterapeuta.</td></tr>
    <tr><td><code>primaryBranchId</code></td><td><code>BranchId</code></td><td>private</td><td>Sede principal en la que atiende.</td></tr>
    <tr><td><code>personalInfo</code></td><td><code>PersonalInfo</code></td><td>private</td><td>Nombre completo, DNI, fecha de nacimiento, género y teléfono de contacto.</td></tr>
    <tr><td><code>licenseNumber</code></td><td><code>LicenseNumber</code></td><td>private</td><td>Número de colegiatura (CMP/CTTMP); validado antes de activar.</td></tr>
    <tr><td><code>specialty</code></td><td><code>Specialty</code></td><td>private</td><td>Especialidad principal (traumatológica, neurológica, deportiva).</td></tr>
    <tr><td><code>yearsOfExperience</code></td><td><code>int</code></td><td>private</td><td>Años acreditados de ejercicio profesional.</td></tr>
    <tr><td><code>hireDate</code></td><td><code>LocalDate</code></td><td>private</td><td>Fecha de ingreso a la clínica.</td></tr>
    <tr><td><code>status</code></td><td><code>ProfileStatus</code></td><td>private</td><td>Estado del perfil (<code>PENDING_VALIDATION</code>, <code>ACTIVE</code>, <code>SUSPENDED</code>, <code>ARCHIVED</code>).</td></tr>
  </tbody>
</table>

<p>Métodos principales:</p>

<table>
  <thead>
    <tr><th>Método</th><th>Tipo Retorno</th><th>Visibilidad</th><th>Descripción</th></tr>
  </thead>
  <tbody>
    <tr><td><code>PhysiotherapistProfile()</code></td><td>Constructor</td><td>public</td><td>Constructor vacío requerido por JPA.</td></tr>
    <tr><td><code>PhysiotherapistProfile(UserId, ClinicId, BranchId, PersonalInfo, LicenseNumber, Specialty)</code></td><td>Constructor</td><td>public</td><td>Crea el perfil en estado <code>PENDING_VALIDATION</code> y publica <code>PhysiotherapistProfileRegisteredEvent</code>.</td></tr>
    <tr><td><code>validate()</code></td><td><code>void</code></td><td>public</td><td>Marca el perfil como <code>ACTIVE</code> tras la verificación del <code>licenseNumber</code>; publica <code>PhysiotherapistProfileActivatedEvent</code>.</td></tr>
    <tr><td><code>assignToBranch(BranchId branchId)</code></td><td><code>void</code></td><td>public</td><td>Actualiza la sede principal del fisioterapeuta.</td></tr>
    <tr><td><code>updatePersonalInfo(PersonalInfo personalInfo)</code></td><td><code>void</code></td><td>public</td><td>Actualiza el bloque de datos personales del especialista.</td></tr>
    <tr><td><code>suspend(String reason)</code></td><td><code>void</code></td><td>public</td><td>Cambia el estado a <code>SUSPENDED</code>.</td></tr>
    <tr><td><code>archive()</code></td><td><code>void</code></td><td>public</td><td>Archiva el perfil al cesar la relación laboral.</td></tr>
  </tbody>
</table>

<p><strong>4. PatientProfile (Aggregate Root)</strong></p>

<p>Representa el perfil personal y clínico del paciente. Incluye datos demográficos, contacto de emergencia y un resumen clínico breve; el historial detallado de tratamientos vive en el BC Planning, no aquí.</p>

<table>
  <thead>
    <tr><th>Atributo</th><th>Tipo</th><th>Visibilidad</th><th>Descripción</th></tr>
  </thead>
  <tbody>
    <tr><td><code>id</code></td><td><code>PatientProfileId</code></td><td>private</td><td>Identificador del perfil de paciente.</td></tr>
    <tr><td><code>userId</code></td><td><code>UserId</code></td><td>private</td><td>Referencia lógica al usuario en el BC IAM.</td></tr>
    <tr><td><code>clinicId</code></td><td><code>ClinicId</code></td><td>private</td><td>Clínica a la que pertenece el paciente.</td></tr>
    <tr><td><code>branchId</code></td><td><code>BranchId</code></td><td>private</td><td>Sede de atención habitual.</td></tr>
    <tr><td><code>assignedPhysiotherapistId</code></td><td><code>PhysiotherapistProfileId</code></td><td>private</td><td>Fisioterapeuta responsable; puede quedar sin asignar hasta que el Administrador complete el onboarding.</td></tr>
    <tr><td><code>personalInfo</code></td><td><code>PersonalInfo</code></td><td>private</td><td>Datos demográficos del paciente.</td></tr>
    <tr><td><code>emergencyContact</code></td><td><code>EmergencyContact</code></td><td>private</td><td>Persona de contacto en caso de emergencia (nombre, parentesco, teléfono).</td></tr>
    <tr><td><code>insurance</code></td><td><code>InsuranceInfo</code></td><td>private</td><td>Datos del seguro o convenio aplicable (opcional).</td></tr>
    <tr><td><code>clinicalSummary</code></td><td><code>ClinicalSummary</code></td><td>private</td><td>Resumen clínico breve: diagnóstico principal, alergias y observaciones relevantes.</td></tr>
    <tr><td><code>status</code></td><td><code>ProfileStatus</code></td><td>private</td><td>Estado del perfil (<code>ACTIVE</code>, <code>DISCHARGED</code>, <code>ARCHIVED</code>).</td></tr>
  </tbody>
</table>

<p>Métodos principales:</p>

<table>
  <thead>
    <tr><th>Método</th><th>Tipo Retorno</th><th>Visibilidad</th><th>Descripción</th></tr>
  </thead>
  <tbody>
    <tr><td><code>PatientProfile()</code></td><td>Constructor</td><td>public</td><td>Constructor vacío requerido por JPA.</td></tr>
    <tr><td><code>PatientProfile(UserId, ClinicId, BranchId, PersonalInfo, EmergencyContact)</code></td><td>Constructor</td><td>public</td><td>Crea el perfil del paciente en estado <code>ACTIVE</code>; publica <code>PatientProfileRegisteredEvent</code>.</td></tr>
    <tr><td><code>assignPhysiotherapist(PhysiotherapistProfileId id)</code></td><td><code>void</code></td><td>public</td><td>Asocia un fisioterapeuta responsable (mismo <code>clinicId</code>); publica <code>PatientAssignedToPhysiotherapistEvent</code>.</td></tr>
    <tr><td><code>updateClinicalSummary(ClinicalSummary summary)</code></td><td><code>void</code></td><td>public</td><td>Actualiza el resumen clínico (invocado por el fisioterapeuta).</td></tr>
    <tr><td><code>updateInsurance(InsuranceInfo insurance)</code></td><td><code>void</code></td><td>public</td><td>Actualiza los datos del seguro/convenio.</td></tr>
    <tr><td><code>discharge(String reason)</code></td><td><code>void</code></td><td>public</td><td>Marca el paciente como <code>DISCHARGED</code> al finalizar el tratamiento.</td></tr>
    <tr><td><code>archive()</code></td><td><code>void</code></td><td>public</td><td>Transición final al estado <code>ARCHIVED</code>.</td></tr>
  </tbody>
</table>

<p><strong>5. ClinicAdminProfile (Entity)</strong></p>

<p>Perfil del Administrador de Clínica. Extiende el rol <code>CLINIC_ADMIN</code> del BC IAM con datos de contacto operativo y nivel de alcance (sede única vs. todas las sedes).</p>

<table>
  <thead>
    <tr><th>Atributo</th><th>Tipo</th><th>Visibilidad</th><th>Descripción</th></tr>
  </thead>
  <tbody>
    <tr><td><code>id</code></td><td><code>ClinicAdminProfileId</code></td><td>private</td><td>Identificador del perfil administrativo.</td></tr>
    <tr><td><code>userId</code></td><td><code>UserId</code></td><td>private</td><td>Referencia al usuario en el BC IAM.</td></tr>
    <tr><td><code>clinicId</code></td><td><code>ClinicId</code></td><td>private</td><td>Clínica administrada.</td></tr>
    <tr><td><code>scope</code></td><td><code>AdminScope</code></td><td>private</td><td>Alcance administrativo (<code>CLINIC_WIDE</code> o <code>BRANCH_SCOPED</code>).</td></tr>
    <tr><td><code>managedBranchIds</code></td><td><code>Set&lt;BranchId&gt;</code></td><td>private</td><td>Sedes bajo su responsabilidad cuando el alcance es <code>BRANCH_SCOPED</code>.</td></tr>
    <tr><td><code>personalInfo</code></td><td><code>PersonalInfo</code></td><td>private</td><td>Datos personales y de contacto operativo.</td></tr>
  </tbody>
</table>

<p><strong>6. Value Objects</strong></p>

<table>
  <thead>
    <tr><th>Value Object</th><th>Atributos</th><th>Descripción</th></tr>
  </thead>
  <tbody>
    <tr><td><code>ClinicId</code></td><td><code>value: UUID</code></td><td>Identificador único y compartido lógicamente con el resto de bounded contexts.</td></tr>
    <tr><td><code>BranchId</code></td><td><code>value: UUID</code></td><td>Identificador de sede.</td></tr>
    <tr><td><code>PhysiotherapistProfileId</code></td><td><code>value: UUID</code></td><td>Identificador del perfil del fisioterapeuta.</td></tr>
    <tr><td><code>PatientProfileId</code></td><td><code>value: UUID</code></td><td>Identificador del perfil del paciente.</td></tr>
    <tr><td><code>LegalName</code> / <code>CommercialName</code></td><td><code>value: String</code></td><td>Denominaciones de la clínica con validación de longitud y caracteres.</td></tr>
    <tr><td><code>TaxId</code></td><td><code>value: String</code></td><td>RUC peruano de 11 dígitos; valida prefijo y dígito verificador.</td></tr>
    <tr><td><code>Address</code></td><td><code>street, district, province, department, country, postalCode</code></td><td>Dirección estructurada.</td></tr>
    <tr><td><code>ContactInfo</code></td><td><code>email, phone, website</code></td><td>Canal público de contacto de la clínica.</td></tr>
    <tr><td><code>PhoneNumber</code></td><td><code>countryCode, number</code></td><td>Teléfono con validación E.164.</td></tr>
    <tr><td><code>OpeningHours</code></td><td><code>Map&lt;DayOfWeek, TimeRange&gt;</code></td><td>Horario semanal de atención.</td></tr>
    <tr><td><code>LogoUrl</code></td><td><code>value: URI</code></td><td>URL del logo en Azure Blob Storage.</td></tr>
    <tr><td><code>PersonalInfo</code></td><td><code>fullName, documentNumber, birthDate, gender, phone</code></td><td>Datos personales comunes a pacientes, fisioterapeutas y administradores.</td></tr>
    <tr><td><code>EmergencyContact</code></td><td><code>fullName, relationship, phone</code></td><td>Contacto de emergencia del paciente.</td></tr>
    <tr><td><code>InsuranceInfo</code></td><td><code>provider, policyNumber, coverage</code></td><td>Datos opcionales del seguro/convenio.</td></tr>
    <tr><td><code>ClinicalSummary</code></td><td><code>primaryDiagnosis, allergies, notes</code></td><td>Resumen clínico de alto nivel del paciente.</td></tr>
    <tr><td><code>LicenseNumber</code></td><td><code>value: String</code></td><td>Colegiatura del fisioterapeuta (CMP/CTTMP).</td></tr>
    <tr><td><code>Specialty</code></td><td><code>Enum</code></td><td>Especialidad: <code>TRAUMATOLOGICAL</code>, <code>NEUROLOGICAL</code>, <code>SPORTS</code>, <code>GENERAL</code>.</td></tr>
    <tr><td><code>ClinicStatus</code></td><td><code>Enum</code></td><td><code>PENDING_ACTIVATION</code>, <code>ACTIVE</code>, <code>SUSPENDED</code>, <code>ARCHIVED</code>.</td></tr>
    <tr><td><code>BranchStatus</code></td><td><code>Enum</code></td><td><code>ACTIVE</code>, <code>INACTIVE</code>.</td></tr>
    <tr><td><code>ProfileStatus</code></td><td><code>Enum</code></td><td><code>PENDING_VALIDATION</code>, <code>ACTIVE</code>, <code>SUSPENDED</code>, <code>DISCHARGED</code>, <code>ARCHIVED</code>.</td></tr>
    <tr><td><code>AdminScope</code></td><td><code>Enum</code></td><td><code>CLINIC_WIDE</code>, <code>BRANCH_SCOPED</code>.</td></tr>
  </tbody>
</table>

<p><strong>7. Domain Events</strong></p>

<table>
  <thead>
    <tr><th>Evento</th><th>Payload</th><th>Descripción</th></tr>
  </thead>
  <tbody>
    <tr><td><code>ClinicRegisteredEvent</code></td><td><code>clinicId, legalName, taxId, createdBy, occurredAt</code></td><td>Se emite al crear una clínica en estado <code>PENDING_ACTIVATION</code>. Lo consume el BC Subscription para inicializar la suscripción base.</td></tr>
    <tr><td><code>ClinicActivatedEvent</code></td><td><code>clinicId, activatedAt</code></td><td>Se emite al pasar el tenant a <code>ACTIVE</code> tras confirmar la suscripción; lo consume el BC IAM para habilitar el login de los usuarios y el BC Device para habilitar el provisioning del kit.</td></tr>
    <tr><td><code>ClinicSuspendedEvent</code></td><td><code>clinicId, reason, occurredAt</code></td><td>Bloquea operaciones mientras dure la suspensión.</td></tr>
    <tr><td><code>BranchAddedEvent</code></td><td><code>clinicId, branchId, branchName, isHeadquarters, occurredAt</code></td><td>Nueva sede disponible para asignación de usuarios y dispositivos.</td></tr>
    <tr><td><code>PhysiotherapistProfileRegisteredEvent</code></td><td><code>profileId, userId, clinicId, primaryBranchId, occurredAt</code></td><td>Se publica al crear el perfil; permite al BC Planning habilitar la creación de <code>TreatmentPlan</code> por parte del fisioterapeuta.</td></tr>
    <tr><td><code>PhysiotherapistProfileActivatedEvent</code></td><td><code>profileId, userId, clinicId, occurredAt</code></td><td>Se publica tras la validación de la colegiatura.</td></tr>
    <tr><td><code>PatientProfileRegisteredEvent</code></td><td><code>profileId, userId, clinicId, branchId, occurredAt</code></td><td>El paciente queda habilitado para recibir un plan de tratamiento.</td></tr>
    <tr><td><code>PatientAssignedToPhysiotherapistEvent</code></td><td><code>patientProfileId, physiotherapistProfileId, clinicId, occurredAt</code></td><td>Relación clínica establecida; consumido por Planning y Therapy.</td></tr>
    <tr><td><code>ClinicArchivedEvent</code></td><td><code>clinicId, occurredAt</code></td><td>Cierre definitivo del tenant.</td></tr>
  </tbody>
</table>

<p><strong>8. Commands</strong></p>

<table>
  <thead>
    <tr><th>Command</th><th>Atributos</th><th>Descripción</th></tr>
  </thead>
  <tbody>
    <tr><td><code>RegisterClinicCommand</code></td><td><code>legalName, commercialName, taxId, contactInfo, headquartersAddress, createdBy</code></td><td>Crea la clínica y su sede central en una misma transacción.</td></tr>
    <tr><td><code>ActivateClinicCommand</code></td><td><code>clinicId</code></td><td>Activa el tenant al confirmarse la suscripción.</td></tr>
    <tr><td><code>SuspendClinicCommand</code></td><td><code>clinicId, reason</code></td><td>Suspende el tenant.</td></tr>
    <tr><td><code>AddBranchCommand</code></td><td><code>clinicId, name, address, phoneNumber, openingHours, isHeadquarters</code></td><td>Agrega una nueva sede.</td></tr>
    <tr><td><code>UpdateClinicContactInfoCommand</code></td><td><code>clinicId, contactInfo</code></td><td>Actualiza los datos públicos de la clínica.</td></tr>
    <tr><td><code>RegisterPhysiotherapistProfileCommand</code></td><td><code>userId, clinicId, primaryBranchId, personalInfo, licenseNumber, specialty, yearsOfExperience</code></td><td>Crea el perfil clínico del fisioterapeuta.</td></tr>
    <tr><td><code>ValidatePhysiotherapistLicenseCommand</code></td><td><code>profileId</code></td><td>Marca la colegiatura como validada y activa el perfil.</td></tr>
    <tr><td><code>RegisterPatientProfileCommand</code></td><td><code>userId, clinicId, branchId, personalInfo, emergencyContact, insurance</code></td><td>Crea el perfil del paciente.</td></tr>
    <tr><td><code>AssignPatientToPhysiotherapistCommand</code></td><td><code>patientProfileId, physiotherapistProfileId</code></td><td>Asigna responsable clínico.</td></tr>
    <tr><td><code>UpdatePatientClinicalSummaryCommand</code></td><td><code>patientProfileId, clinicalSummary</code></td><td>Actualiza el resumen clínico del paciente.</td></tr>
    <tr><td><code>DischargePatientCommand</code></td><td><code>patientProfileId, reason</code></td><td>Marca el alta del paciente.</td></tr>
  </tbody>
</table>

<p><strong>9. Queries</strong></p>

<table>
  <thead>
    <tr><th>Query</th><th>Atributos</th><th>Retorno</th><th>Descripción</th></tr>
  </thead>
  <tbody>
    <tr><td><code>GetClinicByIdQuery</code></td><td><code>clinicId</code></td><td><code>Optional&lt;Clinic&gt;</code></td><td>Recupera la clínica con sus sedes.</td></tr>
    <tr><td><code>GetClinicByTaxIdQuery</code></td><td><code>taxId</code></td><td><code>Optional&lt;Clinic&gt;</code></td><td>Resuelve clínica por RUC (usado en onboarding).</td></tr>
    <tr><td><code>GetBranchesByClinicIdQuery</code></td><td><code>clinicId</code></td><td><code>List&lt;Branch&gt;</code></td><td>Lista las sedes de una clínica.</td></tr>
    <tr><td><code>GetPhysiotherapistProfileByUserIdQuery</code></td><td><code>userId</code></td><td><code>Optional&lt;PhysiotherapistProfile&gt;</code></td><td>Perfil clínico del fisioterapeuta autenticado.</td></tr>
    <tr><td><code>GetPhysiotherapistsByClinicIdQuery</code></td><td><code>clinicId, branchId?</code></td><td><code>List&lt;PhysiotherapistProfile&gt;</code></td><td>Directorio de fisioterapeutas por clínica y sede.</td></tr>
    <tr><td><code>GetPatientProfileByUserIdQuery</code></td><td><code>userId</code></td><td><code>Optional&lt;PatientProfile&gt;</code></td><td>Perfil clínico del paciente autenticado.</td></tr>
    <tr><td><code>GetPatientsByPhysiotherapistIdQuery</code></td><td><code>physiotherapistProfileId</code></td><td><code>List&lt;PatientProfile&gt;</code></td><td>Pacientes asignados a un fisioterapeuta.</td></tr>
    <tr><td><code>GetPatientsByClinicIdQuery</code></td><td><code>clinicId, branchId?</code></td><td><code>List&lt;PatientProfile&gt;</code></td><td>Pacientes de una clínica (vista del Administrador).</td></tr>
  </tbody>
</table>

<p><strong>10. Domain Exceptions</strong></p>

<table>
  <thead>
    <tr><th>Excepción</th><th>Descripción</th></tr>
  </thead>
  <tbody>
    <tr><td><code>ClinicAlreadyRegisteredException</code></td><td>Se lanza cuando ya existe una clínica con el mismo <code>taxId</code>.</td></tr>
    <tr><td><code>ClinicNotActiveException</code></td><td>Se lanza al intentar operar sobre un tenant que no está en estado <code>ACTIVE</code>.</td></tr>
    <tr><td><code>BranchNotFoundException</code></td><td>Sede inexistente dentro del aggregate <code>Clinic</code>.</td></tr>
    <tr><td><code>DuplicateHeadquartersException</code></td><td>Se intenta designar más de una sede central simultáneamente.</td></tr>
    <tr><td><code>PhysiotherapistLicenseInvalidException</code></td><td>Número de colegiatura inválido o duplicado.</td></tr>
    <tr><td><code>PatientAlreadyRegisteredException</code></td><td>Se intenta registrar un perfil de paciente para un <code>userId</code> que ya tiene uno activo.</td></tr>
    <tr><td><code>CrossClinicAssignmentException</code></td><td>Se intenta asignar un paciente a un fisioterapeuta de otra clínica.</td></tr>
    <tr><td><code>InvalidTaxIdException</code></td><td>RUC mal formado o con dígito verificador inválido.</td></tr>
  </tbody>
</table>

#### 4.2.3.2. Interface Layer

<p><strong>1. ClinicController (REST Controller)</strong></p>

<p>Expone las operaciones de registro y administración del tenant. Sólo accesible por usuarios con rol <code>CLINIC_ADMIN</code>, salvo la lectura pública del perfil comercial.</p>

<table>
  <thead>
    <tr><th>Método</th><th>Ruta base</th><th>HTTP</th><th>Descripción</th></tr>
  </thead>
  <tbody>
    <tr><td><code>registerClinic</code></td><td><code>/api/v1/clinics</code></td><td>POST</td><td>Registra una nueva clínica y su sede central.</td></tr>
    <tr><td><code>getClinicById</code></td><td><code>/api/v1/clinics/{clinicId}</code></td><td>GET</td><td>Obtiene el perfil organizacional de la clínica.</td></tr>
    <tr><td><code>updateContactInfo</code></td><td><code>/api/v1/clinics/{clinicId}/contact-info</code></td><td>PATCH</td><td>Actualiza correo, teléfono y web de la clínica.</td></tr>
    <tr><td><code>uploadLogo</code></td><td><code>/api/v1/clinics/{clinicId}/logo</code></td><td>POST</td><td>Sube un nuevo logotipo al blob y actualiza la URL.</td></tr>
    <tr><td><code>suspendClinic</code></td><td><code>/api/v1/clinics/{clinicId}/suspend</code></td><td>POST</td><td>Suspende el tenant (uso administrativo interno).</td></tr>
  </tbody>
</table>

<p><strong>2. BranchController (REST Controller)</strong></p>

<table>
  <thead>
    <tr><th>Método</th><th>Ruta base</th><th>HTTP</th><th>Descripción</th></tr>
  </thead>
  <tbody>
    <tr><td><code>addBranch</code></td><td><code>/api/v1/clinics/{clinicId}/branches</code></td><td>POST</td><td>Agrega una sede a la clínica.</td></tr>
    <tr><td><code>getBranchesByClinic</code></td><td><code>/api/v1/clinics/{clinicId}/branches</code></td><td>GET</td><td>Lista las sedes del tenant.</td></tr>
    <tr><td><code>updateBranch</code></td><td><code>/api/v1/clinics/{clinicId}/branches/{branchId}</code></td><td>PATCH</td><td>Actualiza datos de la sede (dirección, horarios, teléfono).</td></tr>
    <tr><td><code>deactivateBranch</code></td><td><code>/api/v1/clinics/{clinicId}/branches/{branchId}/deactivate</code></td><td>POST</td><td>Inactiva la sede sin eliminarla.</td></tr>
  </tbody>
</table>

<p><strong>3. PhysiotherapistProfileController (REST Controller)</strong></p>

<table>
  <thead>
    <tr><th>Método</th><th>Ruta base</th><th>HTTP</th><th>Descripción</th></tr>
  </thead>
  <tbody>
    <tr><td><code>registerPhysiotherapist</code></td><td><code>/api/v1/physiotherapists</code></td><td>POST</td><td>Crea el perfil clínico del fisioterapeuta tras su alta en IAM.</td></tr>
    <tr><td><code>validateLicense</code></td><td><code>/api/v1/physiotherapists/{id}/validate-license</code></td><td>POST</td><td>Activa el perfil tras verificar la colegiatura.</td></tr>
    <tr><td><code>getPhysiotherapistById</code></td><td><code>/api/v1/physiotherapists/{id}</code></td><td>GET</td><td>Obtiene el perfil del fisioterapeuta.</td></tr>
    <tr><td><code>getPhysiotherapistsByClinic</code></td><td><code>/api/v1/physiotherapists?clinicId={id}&amp;branchId={id}</code></td><td>GET</td><td>Lista los fisioterapeutas del tenant (y opcionalmente por sede).</td></tr>
    <tr><td><code>updatePhysiotherapistProfile</code></td><td><code>/api/v1/physiotherapists/{id}</code></td><td>PATCH</td><td>Actualiza datos personales o sede principal.</td></tr>
  </tbody>
</table>

<p><strong>4. PatientProfileController (REST Controller)</strong></p>

<table>
  <thead>
    <tr><th>Método</th><th>Ruta base</th><th>HTTP</th><th>Descripción</th></tr>
  </thead>
  <tbody>
    <tr><td><code>registerPatient</code></td><td><code>/api/v1/patients</code></td><td>POST</td><td>Crea el perfil del paciente.</td></tr>
    <tr><td><code>assignPhysiotherapist</code></td><td><code>/api/v1/patients/{id}/physiotherapist</code></td><td>PATCH</td><td>Asigna fisioterapeuta responsable.</td></tr>
    <tr><td><code>updateClinicalSummary</code></td><td><code>/api/v1/patients/{id}/clinical-summary</code></td><td>PATCH</td><td>Actualiza el resumen clínico (rol fisioterapeuta).</td></tr>
    <tr><td><code>getPatientById</code></td><td><code>/api/v1/patients/{id}</code></td><td>GET</td><td>Obtiene el perfil del paciente.</td></tr>
    <tr><td><code>getPatientsByPhysiotherapist</code></td><td><code>/api/v1/patients?physiotherapistId={id}</code></td><td>GET</td><td>Lista pacientes asignados a un fisioterapeuta.</td></tr>
    <tr><td><code>dischargePatient</code></td><td><code>/api/v1/patients/{id}/discharge</code></td><td>POST</td><td>Marca al paciente como dado de alta.</td></tr>
  </tbody>
</table>

<p><strong>5. Resources (DTOs)</strong></p>

<p>DTOs modelados como Java Records para la comunicación REST.</p>

<table>
  <thead>
    <tr><th>Resource</th><th>Atributos principales</th><th>Descripción</th></tr>
  </thead>
  <tbody>
    <tr><td><code>RegisterClinicResource</code></td><td><code>legalName, commercialName, taxId, contactInfo, headquartersAddress</code></td><td>Payload de registro de clínica.</td></tr>
    <tr><td><code>ClinicResource</code></td><td><code>id, legalName, commercialName, taxId, contactInfo, logoUrl, status, branches: List&lt;BranchResource&gt;</code></td><td>Representación pública de la clínica.</td></tr>
    <tr><td><code>AddBranchResource</code></td><td><code>name, address, phoneNumber, openingHours, isHeadquarters</code></td><td>Payload para agregar sede.</td></tr>
    <tr><td><code>BranchResource</code></td><td><code>id, name, address, phoneNumber, openingHours, isHeadquarters, status</code></td><td>Representación de la sede.</td></tr>
    <tr><td><code>RegisterPhysiotherapistProfileResource</code></td><td><code>userId, clinicId, primaryBranchId, personalInfo, licenseNumber, specialty, yearsOfExperience</code></td><td>Payload de alta del fisioterapeuta.</td></tr>
    <tr><td><code>PhysiotherapistProfileResource</code></td><td><code>id, userId, clinicId, primaryBranchId, personalInfo, licenseNumber, specialty, yearsOfExperience, status</code></td><td>Representación REST del perfil.</td></tr>
    <tr><td><code>RegisterPatientProfileResource</code></td><td><code>userId, clinicId, branchId, personalInfo, emergencyContact, insurance</code></td><td>Payload de alta del paciente.</td></tr>
    <tr><td><code>PatientProfileResource</code></td><td><code>id, userId, clinicId, branchId, assignedPhysiotherapistId, personalInfo, emergencyContact, insurance, clinicalSummary, status</code></td><td>Representación REST del perfil de paciente.</td></tr>
    <tr><td><code>AssignPhysiotherapistResource</code></td><td><code>physiotherapistProfileId</code></td><td>Payload para asignación.</td></tr>
    <tr><td><code>UpdateContactInfoResource</code></td><td><code>email, phone, website</code></td><td>Datos actualizables de contacto.</td></tr>
    <tr><td><code>UpdateClinicalSummaryResource</code></td><td><code>primaryDiagnosis, allergies, notes</code></td><td>Payload del fisioterapeuta.</td></tr>
  </tbody>
</table>

<p><strong>6. Transform (Assemblers)</strong></p>

<table>
  <thead>
    <tr><th>Assembler</th><th>Entrada</th><th>Salida</th><th>Descripción</th></tr>
  </thead>
  <tbody>
    <tr><td><code>RegisterClinicCommandFromResourceAssembler</code></td><td><code>RegisterClinicResource</code></td><td><code>RegisterClinicCommand</code></td><td>Construye el command de alta de clínica.</td></tr>
    <tr><td><code>ClinicResourceFromEntityAssembler</code></td><td><code>Clinic</code></td><td><code>ClinicResource</code></td><td>Expone el aggregate con sus sedes.</td></tr>
    <tr><td><code>AddBranchCommandFromResourceAssembler</code></td><td><code>AddBranchResource, clinicId</code></td><td><code>AddBranchCommand</code></td><td>Construye el command de alta de sede.</td></tr>
    <tr><td><code>BranchResourceFromEntityAssembler</code></td><td><code>Branch</code></td><td><code>BranchResource</code></td><td>Mapeo entidad → DTO.</td></tr>
    <tr><td><code>RegisterPhysiotherapistProfileCommandFromResourceAssembler</code></td><td><code>RegisterPhysiotherapistProfileResource</code></td><td><code>RegisterPhysiotherapistProfileCommand</code></td><td>Construye el command del perfil.</td></tr>
    <tr><td><code>PhysiotherapistProfileResourceFromEntityAssembler</code></td><td><code>PhysiotherapistProfile</code></td><td><code>PhysiotherapistProfileResource</code></td><td>Mapeo entidad → DTO.</td></tr>
    <tr><td><code>RegisterPatientProfileCommandFromResourceAssembler</code></td><td><code>RegisterPatientProfileResource</code></td><td><code>RegisterPatientProfileCommand</code></td><td>Construye el command del perfil del paciente.</td></tr>
    <tr><td><code>PatientProfileResourceFromEntityAssembler</code></td><td><code>PatientProfile</code></td><td><code>PatientProfileResource</code></td><td>Mapeo entidad → DTO.</td></tr>
    <tr><td><code>AssignPatientToPhysiotherapistCommandFromResourceAssembler</code></td><td><code>AssignPhysiotherapistResource, patientProfileId</code></td><td><code>AssignPatientToPhysiotherapistCommand</code></td><td>Construye el command de asignación.</td></tr>
  </tbody>
</table>

#### 4.2.3.3. Application Layer

<p><strong>1. OrganizationContextFacadeImpl (ACL Facade)</strong></p>

<p>Fachada consumida por los BC hermanos (Planning, Device, Therapy, Subscription) que necesitan resolver datos organizacionales sin conocer el modelo interno.</p>

<table>
  <thead>
    <tr><th>Atributo</th><th>Tipo</th><th>Visibilidad</th><th>Descripción</th></tr>
  </thead>
  <tbody>
    <tr><td><code>clinicQueryService</code></td><td><code>ClinicQueryService</code></td><td>private</td><td>Consultas del aggregate <code>Clinic</code>.</td></tr>
    <tr><td><code>physiotherapistQueryService</code></td><td><code>PhysiotherapistProfileQueryService</code></td><td>private</td><td>Consultas de perfiles de fisioterapeutas.</td></tr>
    <tr><td><code>patientQueryService</code></td><td><code>PatientProfileQueryService</code></td><td>private</td><td>Consultas de perfiles de pacientes.</td></tr>
  </tbody>
</table>

<table>
  <thead>
    <tr><th>Método</th><th>Tipo Retorno</th><th>Visibilidad</th><th>Descripción</th></tr>
  </thead>
  <tbody>
    <tr><td><code>fetchClinicSummaryById(UUID clinicId)</code></td><td><code>Optional&lt;ClinicSummaryDto&gt;</code></td><td>public</td><td>DTO ligero con nombre, estado y RUC del tenant.</td></tr>
    <tr><td><code>fetchActiveBranchIds(UUID clinicId)</code></td><td><code>List&lt;UUID&gt;</code></td><td>public</td><td>Lista de sedes activas (usado por Device al provisionar kits).</td></tr>
    <tr><td><code>fetchPhysiotherapistClinicId(UUID userId)</code></td><td><code>Optional&lt;UUID&gt;</code></td><td>public</td><td>Devuelve el <code>clinicId</code> al que pertenece un fisioterapeuta; usado por Planning para validar la creación de planes.</td></tr>
    <tr><td><code>fetchPatientContextByUserId(UUID userId)</code></td><td><code>Optional&lt;PatientContextDto&gt;</code></td><td>public</td><td>Devuelve <code>patientProfileId</code>, <code>clinicId</code>, <code>branchId</code> y <code>assignedPhysiotherapistId</code>.</td></tr>
  </tbody>
</table>

<p><strong>2. ClinicCommandServiceImpl (Command Service Implementation)</strong></p>

<table>
  <thead>
    <tr><th>Atributo</th><th>Tipo</th><th>Visibilidad</th><th>Descripción</th></tr>
  </thead>
  <tbody>
    <tr><td><code>clinicRepository</code></td><td><code>ClinicRepository</code></td><td>private</td><td>Persistencia del aggregate <code>Clinic</code>.</td></tr>
    <tr><td><code>taxIdValidationPort</code></td><td><code>TaxIdValidationPort</code></td><td>private</td><td>ACL contra el servicio externo de validación de RUC (SUNAT).</td></tr>
    <tr><td><code>blobStoragePort</code></td><td><code>BlobStoragePort</code></td><td>private</td><td>Puerto de Azure Blob Storage para logos.</td></tr>
    <tr><td><code>eventPublisher</code></td><td><code>ApplicationEventPublisher</code></td><td>private</td><td>Publicación de domain events.</td></tr>
  </tbody>
</table>

<table>
  <thead>
    <tr><th>Método</th><th>Tipo Retorno</th><th>Visibilidad</th><th>Descripción</th></tr>
  </thead>
  <tbody>
    <tr><td><code>handle(RegisterClinicCommand)</code></td><td><code>Optional&lt;Clinic&gt;</code></td><td>public</td><td>Valida el RUC, registra la clínica con su sede central y publica <code>ClinicRegisteredEvent</code>.</td></tr>
    <tr><td><code>handle(ActivateClinicCommand)</code></td><td><code>void</code></td><td>public</td><td>Activa el tenant y publica <code>ClinicActivatedEvent</code>.</td></tr>
    <tr><td><code>handle(SuspendClinicCommand)</code></td><td><code>void</code></td><td>public</td><td>Suspende el tenant.</td></tr>
    <tr><td><code>handle(AddBranchCommand)</code></td><td><code>Optional&lt;Branch&gt;</code></td><td>public</td><td>Agrega una sede al tenant.</td></tr>
    <tr><td><code>handle(UpdateClinicContactInfoCommand)</code></td><td><code>void</code></td><td>public</td><td>Actualiza los datos de contacto.</td></tr>
    <tr><td><code>handle(UploadClinicLogoCommand)</code></td><td><code>String</code></td><td>public</td><td>Sube el archivo al blob y devuelve la nueva URL.</td></tr>
  </tbody>
</table>

<p><strong>3. ClinicQueryServiceImpl (Query Service Implementation)</strong></p>

<table>
  <thead>
    <tr><th>Método</th><th>Tipo Retorno</th><th>Visibilidad</th><th>Descripción</th></tr>
  </thead>
  <tbody>
    <tr><td><code>handle(GetClinicByIdQuery)</code></td><td><code>Optional&lt;Clinic&gt;</code></td><td>public</td><td>Recupera el aggregate por su identificador.</td></tr>
    <tr><td><code>handle(GetClinicByTaxIdQuery)</code></td><td><code>Optional&lt;Clinic&gt;</code></td><td>public</td><td>Resuelve clínica por RUC.</td></tr>
    <tr><td><code>handle(GetBranchesByClinicIdQuery)</code></td><td><code>List&lt;Branch&gt;</code></td><td>public</td><td>Lista las sedes del tenant.</td></tr>
  </tbody>
</table>

<p><strong>4. PhysiotherapistProfileCommandServiceImpl (Command Service Implementation)</strong></p>

<table>
  <thead>
    <tr><th>Atributo</th><th>Tipo</th><th>Visibilidad</th><th>Descripción</th></tr>
  </thead>
  <tbody>
    <tr><td><code>physiotherapistRepository</code></td><td><code>PhysiotherapistProfileRepository</code></td><td>private</td><td>Persistencia del aggregate.</td></tr>
    <tr><td><code>licenseValidationPort</code></td><td><code>LicenseValidationPort</code></td><td>private</td><td>ACL contra el registro de colegiatura (CMP/CTTMP).</td></tr>
    <tr><td><code>iamContextFacade</code></td><td><code>IamContextFacade</code></td><td>private</td><td>Verifica que el <code>userId</code> exista y tenga rol <code>PHYSIOTHERAPIST</code>.</td></tr>
    <tr><td><code>eventPublisher</code></td><td><code>ApplicationEventPublisher</code></td><td>private</td><td>Publicación de domain events.</td></tr>
  </tbody>
</table>

<p><strong>5. PatientProfileCommandServiceImpl (Command Service Implementation)</strong></p>

<table>
  <thead>
    <tr><th>Atributo</th><th>Tipo</th><th>Visibilidad</th><th>Descripción</th></tr>
  </thead>
  <tbody>
    <tr><td><code>patientRepository</code></td><td><code>PatientProfileRepository</code></td><td>private</td><td>Persistencia del aggregate <code>PatientProfile</code>.</td></tr>
    <tr><td><code>physiotherapistRepository</code></td><td><code>PhysiotherapistProfileRepository</code></td><td>private</td><td>Permite validar la asignación dentro de la misma clínica.</td></tr>
    <tr><td><code>iamContextFacade</code></td><td><code>IamContextFacade</code></td><td>private</td><td>Verifica que el <code>userId</code> exista y tenga rol <code>PATIENT</code>.</td></tr>
    <tr><td><code>eventPublisher</code></td><td><code>ApplicationEventPublisher</code></td><td>private</td><td>Publicador de eventos del dominio.</td></tr>
  </tbody>
</table>

#### 4.2.3.4. Infrastructure Layer

<p><strong>1. ClinicRepository (Repository Interface)</strong></p>

<p>Interfaz de acceso a datos para el aggregate <code>Clinic</code>, implementada por Spring Data JPA sobre Azure Database for PostgreSQL.</p>

<table>
  <thead>
    <tr><th>Método</th><th>Tipo Retorno</th><th>Visibilidad</th><th>Descripción</th></tr>
  </thead>
  <tbody>
    <tr><td><code>findById(ClinicId id)</code></td><td><code>Optional&lt;Clinic&gt;</code></td><td>public</td><td>Busca la clínica por su identificador.</td></tr>
    <tr><td><code>save(Clinic clinic)</code></td><td><code>Clinic</code></td><td>public</td><td>Persiste o actualiza la clínica y sus sedes.</td></tr>
    <tr><td><code>findByTaxId(TaxId taxId)</code></td><td><code>Optional&lt;Clinic&gt;</code></td><td>public</td><td>Resuelve clínica por RUC.</td></tr>
    <tr><td><code>existsByTaxId(TaxId taxId)</code></td><td><code>boolean</code></td><td>public</td><td>Verifica duplicidad de RUC.</td></tr>
    <tr><td><code>findAllByStatus(ClinicStatus status)</code></td><td><code>List&lt;Clinic&gt;</code></td><td>public</td><td>Lista clínicas por estado (uso interno de mantenimiento).</td></tr>
  </tbody>
</table>

<p><strong>2. BranchRepository (Repository Interface)</strong></p>

<table>
  <thead>
    <tr><th>Método</th><th>Tipo Retorno</th><th>Visibilidad</th><th>Descripción</th></tr>
  </thead>
  <tbody>
    <tr><td><code>findAllByClinicId(ClinicId clinicId)</code></td><td><code>List&lt;Branch&gt;</code></td><td>public</td><td>Lista sedes de un tenant.</td></tr>
    <tr><td><code>findByIdAndClinicId(BranchId id, ClinicId clinicId)</code></td><td><code>Optional&lt;Branch&gt;</code></td><td>public</td><td>Obtiene una sede específica del tenant.</td></tr>
  </tbody>
</table>

<p><strong>3. PhysiotherapistProfileRepository (Repository Interface)</strong></p>

<table>
  <thead>
    <tr><th>Método</th><th>Tipo Retorno</th><th>Visibilidad</th><th>Descripción</th></tr>
  </thead>
  <tbody>
    <tr><td><code>findById(PhysiotherapistProfileId id)</code></td><td><code>Optional&lt;PhysiotherapistProfile&gt;</code></td><td>public</td><td>Busca perfil por ID.</td></tr>
    <tr><td><code>findByUserId(UserId userId)</code></td><td><code>Optional&lt;PhysiotherapistProfile&gt;</code></td><td>public</td><td>Resuelve el perfil del usuario autenticado.</td></tr>
    <tr><td><code>findAllByClinicId(ClinicId clinicId)</code></td><td><code>List&lt;PhysiotherapistProfile&gt;</code></td><td>public</td><td>Lista fisioterapeutas de la clínica.</td></tr>
    <tr><td><code>findAllByClinicIdAndPrimaryBranchId(ClinicId clinicId, BranchId branchId)</code></td><td><code>List&lt;PhysiotherapistProfile&gt;</code></td><td>public</td><td>Filtra fisioterapeutas por sede.</td></tr>
    <tr><td><code>existsByLicenseNumber(LicenseNumber licenseNumber)</code></td><td><code>boolean</code></td><td>public</td><td>Verifica duplicidad de colegiatura.</td></tr>
  </tbody>
</table>

<p><strong>4. PatientProfileRepository (Repository Interface)</strong></p>

<table>
  <thead>
    <tr><th>Método</th><th>Tipo Retorno</th><th>Visibilidad</th><th>Descripción</th></tr>
  </thead>
  <tbody>
    <tr><td><code>findById(PatientProfileId id)</code></td><td><code>Optional&lt;PatientProfile&gt;</code></td><td>public</td><td>Busca paciente por ID.</td></tr>
    <tr><td><code>findByUserId(UserId userId)</code></td><td><code>Optional&lt;PatientProfile&gt;</code></td><td>public</td><td>Resuelve el perfil del usuario autenticado.</td></tr>
    <tr><td><code>findAllByClinicId(ClinicId clinicId)</code></td><td><code>List&lt;PatientProfile&gt;</code></td><td>public</td><td>Lista pacientes del tenant.</td></tr>
    <tr><td><code>findAllByAssignedPhysiotherapistId(PhysiotherapistProfileId id)</code></td><td><code>List&lt;PatientProfile&gt;</code></td><td>public</td><td>Lista pacientes asignados a un fisioterapeuta.</td></tr>
    <tr><td><code>existsByUserId(UserId userId)</code></td><td><code>boolean</code></td><td>public</td><td>Evita duplicidad de perfil por usuario.</td></tr>
  </tbody>
</table>

<p><strong>5. External Adapters</strong></p>

<table>
  <thead>
    <tr><th>Adapter</th><th>Responsabilidad</th></tr>
  </thead>
  <tbody>
    <tr><td><code>SunatTaxIdAdapter</code></td><td>Valida el RUC de la clínica contra un servicio externo.</td></tr>
    <tr><td><code>LicenseRegistryAdapter</code></td><td>Valida la colegiatura del fisioterapeuta en el registro profesional correspondiente.</td></tr>
    <tr><td><code>AzureBlobStorageAdapter</code></td><td>Gestiona la carga y recuperación de logos y adjuntos clínicos.</td></tr>
  </tbody>
</table>

#### 4.2.3.5. Bounded Context Software Architecture Component Level Diagrams

El diagrama de componentes (C4 Nivel 3) muestra cómo se organiza internamente el contenedor Organization API (Java/Spring Boot). Se distinguen como piezas centrales los controladores ClinicController, BranchController, PhysiotherapistProfileController y PatientProfileController como puntos de entrada REST; los application services ClinicCommandServiceImpl, ClinicQueryServiceImpl, PhysiotherapistProfileCommandServiceImpl y PatientProfileCommandServiceImpl como responsables de materializar la lógica de aplicación; la fachada OrganizationContextFacadeImpl como ACL consumida por bounded contexts hermanos; los repositorios ClinicRepository, PhysiotherapistProfileRepository y PatientProfileRepository como abstracciones de persistencia; y los adaptadores SunatTaxIdAdapter, LicenseRegistryAdapter y AzureBlobStorageAdapter para integrarse con servicios externos de validación y almacenamiento. Todos estos componentes viven dentro del Container Boundary del Planning Service; el API Gateway queda fuera como mecanismo de enrutamiento y validación JWT, y la Organization DB también se modela externamente como Azure Database for PostgreSQL.

<div style="text-align: center;"> <img src="assets/diagrams/software-architecture/components/out/organization-components-diagram.png" alt="uFlex — Organization Bounded Context Component Diagram" style="max-width: 100%; height: auto;"> </div>

Figura 4.2.3.5. Diagrama de componentes (C4 Nivel 3) del Bounded Context Organization.

#### 4.2.3.6. Bounded Context Software Architecture Code Level Diagrams
##### 4.2.3.6.1. Bounded Context Domain Layer Class Diagrams

El diagrama de clases del Domain Layer del BC Organization modela exclusivamente los conceptos centrales del dominio, sin incluir las capas de application ni infrastructure. El paquete domain.model.aggregates contiene los Aggregate Roots Clinic, PhysiotherapistProfile y PatientProfile; domain.model.entities incluye las Entities Branch y ClinicAdminProfile; domain.model.valueobjects agrupa los Value Objects que representan la identidad organizacional, los datos de contacto, la información clínica resumida y las referencias lógicas hacia otros bounded contexts, además de los enumerados que gobiernan los estados de clínicas, sedes y perfiles; domain.model.events encapsula los Domain Events publicados por los aggregates, como ClinicRegisteredEvent, ClinicActivatedEvent, BranchAddedEvent, PhysiotherapistProfileRegisteredEvent, PatientProfileRegisteredEvent y PatientAssignedToPhysiotherapistEvent; y domain.exceptions reúne las excepciones de negocio que protegen las invariantes del dominio, por ejemplo la unicidad del RUC, la existencia de una única sede central o la restricción de asignar pacientes únicamente a fisioterapeutas de la misma clínica. Las flechas con línea continua representan composición entre aggregates y sus Value Objects, las flechas con línea punteada expresan dependencias semánticas hacia eventos publicados y excepciones lanzadas, y los rombos vacíos indican relaciones de agregación o asociación opcional dentro del modelo del dominio.

<div style="text-align: center;"> <img src="assets/diagrams/uml/class/out/organization-domain-layer-class-diagram.png" alt="uFlex — Organization Bounded Context Domain Class Diagram" style="max-width: 100%; height: auto;"> </div>

Figura 4.2.3.6.1. Diagrama de clases del dominio del Bounded Context Organization.

##### 4.2.3.6.2. Bounded Context Database Design Diagram

El esquema físico del BC Organization en Azure Database for PostgreSQL se estructura alrededor de tablas principales para la gestión organizacional y de perfiles clínicos. La tabla clinics almacena la identidad de la clínica como tenant, incluyendo razón social, nombre comercial, RUC, datos de contacto, logotipo, estado y datos de auditoría; branches registra las sedes asociadas a cada clínica, con su dirección, teléfono, horarios y estado operativo; physiotherapist_profiles conserva la información profesional y laboral del fisioterapeuta, como colegiatura, especialidad, experiencia, sede principal y estado del perfil; y patient_profiles mantiene la información demográfica y clínica resumida del paciente, así como su asignación al fisioterapeuta responsable, contacto de emergencia y datos de seguro. Adicionalmente, pueden considerarse tablas de apoyo o catálogos para normalizar estados como clinic_statuses, branch_statuses, profile_statuses y admin_scopes. Los índices priorizan búsquedas por tax_id, por clinic_id, por user_id y por relaciones de asignación clínica, a fin de optimizar consultas operativas frecuentes. De manera coherente con el enfoque de bounded contexts, las referencias hacia elementos gestionados en otros contextos, como usuarios del BC IAM, se mantienen como referencias lógicas y no como foreign keys duras cruzadas entre dominios.

<div style="text-align: center;"> <img src="assets/diagrams/database/erd/out/organization-database-design-diagram.png" alt="uFlex — Organization Bounded Context Database ER Diagram" style="max-width: 100%; height: auto;"> </div>

Figura 4.2.3.6.2. Diagrama entidad-relación del Bounded Context Organization.

<hr class="page-break">


### 4.2.4. Bounded Context: Device

#### 4.2.4.1. Domain Layer

En esta sección se describen los elementos del Domain Layer del contexto de Device, los cuales gestionan el ciclo de vida del hardware IoT. Este componente asegura la integridad operativa de los kits de sensores, permitiendo su registro, calibración y monitoreo de salud antes de ser vinculados a un plan terapéutico.

**1. IotKit (Aggregate Root)**

Es el componente central que encapsula el estado físico del dispositivo IoT. Controla las invariantes de negocio, asegurando que un kit no pueda ser vinculado si no está en estado `REGISTERED` o si presenta fallos de calibración críticos.

**Atributos principales:**

| Atributo          | Tipo            | Visibilidad | Descripción                                                               |
|-------------------|-----------------|-------------|---------------------------------------------------------------------------|
| `id`              | `IotKitId`      | private     | Identificador único del dispositivo.                                      |
| `serialNumber`    | `SerialNumber`  | private     | Código de fabricante único del hardware.                                  |
| `status`          | `KitStatus`     | private     | Estado operativo (`REGISTERED`, `LINKED`, `CALIBRATING`, `DISCONNECTED`). |
| `batteryLevel`    | `BatteryLevel`  | private     | Porcentaje de carga actual (0-100%).                                      |
| `firmwareVersion` | `String`        | private     | Versión del firmware instalado.                                           |
| `createdAt`       | `LocalDateTime` | private     | Fecha de alta en el sistema.                                              |

**Métodos principales:**

| Método                        | Tipo Retorno | Visibilidad | Descripción                                                       |
|-------------------------------|--------------|-------------|-------------------------------------------------------------------|
| `IotKit()`                    | Constructor  | public      | Constructor para persistencia.                                    |
| `register(SerialNumber)`      | `IotKit`     | public      | Crea un nuevo kit con estado `REGISTERED`.                        |
| `calibrate(CalibrationData)`  | `void`       | public      | Actualiza parámetros de calibración y publica `IotKitCalibrated`. |
| `link()`                      | `void`       | public      | Cambia estado a `LINKED`.                                         |
| `updateBattery(BatteryLevel)` | `void`       | public      | Registra nivel de batería y dispara alerta si es < 15%.           |
| `disconnect()`                | `void`       | public      | Cambia estado a `DISCONNECTED` y publica `IotKitDisconnected`.    |

**2. CalibrationData (Value Object)**

Define los parámetros de ajuste necesarios para que el sensor IoT sea preciso durante la terapia.

| Atributo              | Tipo            | Visibilidad | Descripción                               |
|-----------------------|-----------------|-------------|-------------------------------------------|
| `offset`              | `Double`        | private     | Valor de corrección de ángulo del sensor. |
| `lastCalibrationDate` | `LocalDateTime` | private     | Fecha del último ajuste.                  |

**3. KitStatus (Value Object - Enum)**

| Atributo       | Tipo | Visibilidad | Descripción                        |
|----------------|------|-------------|------------------------------------|
| `REGISTERED`   | Enum | public      | Kit disponible en inventario.      |
| `LINKED`       | Enum | public      | Kit en uso activo por un paciente. |
| `CALIBRATING`  | Enum | public      | Proceso de calibración en curso.   |
| `DISCONNECTED` | Enum | public      | Kit fuera de línea o sin señal.    |

**4. DeviceCommandService (Domain Service)**

Coordina la lógica compleja de estados del hardware.

| Método                          | Tipo Retorno | Visibilidad | Descripción                                              |
|---------------------------------|--------------|-------------|----------------------------------------------------------|
| `handle(RegisterIotKitCommand)` | `UUID`       | public      | Registra un nuevo kit validando que el serial sea único. |
| `handle(CalibrateKitCommand)`   | `void`       | public      | Ejecuta la calibración en el agregado `IotKit`.          |

#### 4.2.4.2. Interface Layer

En esta sección se presentan los contratos REST para la gestión de inventario de hardware y telemetría de sensores.

**1. IotKitController (REST Controller)**

| Método          | Ruta                             | HTTP  | Descripción                                                |
|-----------------|----------------------------------|-------|------------------------------------------------------------|
| `register`      | `/api/v1/devices`                | POST  | Registra un nuevo kit IoT en el sistema.                   |
| `getById`       | `/api/v1/devices/{id}`           | GET   | Obtiene el estado y salud de un kit específico.            |
| `updateBattery` | `/api/v1/devices/{id}/battery`   | PATCH | Endpoint para telemetría de batería (usado por el sensor). |
| `calibrate`     | `/api/v1/devices/{id}/calibrate` | POST  | Inicia el proceso de calibración técnica.                  |

**2. Resources (DTOs)**

| Resource                 | Atributos principales                                | Descripción                               |
|--------------------------|------------------------------------------------------|-------------------------------------------|
| `RegisterIotKitResource` | `serialNumber: String`                               | Datos requeridos para dar de alta un kit. |
| `IotKitResource`         | `id: UUID`, `serialNumber: String`, `status: String` | Representación pública del kit.           |

**3. Transform (Assemblers)**

| Assembler                           | Entrada                  | Salida                  | Descripción                            |
|-------------------------------------|--------------------------|-------------------------|----------------------------------------|
| `IotKitFromResourceAssembler`       | `RegisterIotKitResource` | `RegisterIotKitCommand` | Mapea el request a comando de dominio. |
| `IotKitResourceFromEntityAssembler` | `IotKit`                 | `IotKitResource`        | Convierte entidad a DTO de salida.     |

#### 4.2.4.3. Application Layer

**1. IotKitCommandServiceImpl (Command Service)**

| Método                          | Tipo Retorno | Visibilidad | Descripción                                                       |
|---------------------------------|--------------|-------------|-------------------------------------------------------------------|
| `handle(RegisterIotKitCommand)` | `UUID`       | public      | Persiste el nuevo kit y valida duplicidad de serial.              |
| `handle(UpdateBatteryCommand)`  | `void`       | public      | Procesa telemetría de batería y publica eventos de mantenimiento. |

**2. IotKitQueryServiceImpl (Query Service)**

| Método                    | Tipo Retorno   | Visibilidad | Descripción                          |
|---------------------------|----------------|-------------|--------------------------------------|
| `handle(GetAllKitsQuery)` | `List<IotKit>` | public      | Retorna inventario completo de kits. |

#### 4.2.4.4. Infrastructure Layer

**1. IotKitRepository (Repository Interface)**

Interfaz de persistencia para el agregado `IotKit` usando Spring Data JPA.

| Método                            | Tipo Retorno       | Visibilidad | Descripción                                         |
|-----------------------------------|--------------------|-------------|-----------------------------------------------------|
| `findById(IotKitId id)`           | `Optional<IotKit>` | public      | Busca kit por ID.                                   |
| `findBySerialNumber(String sn)`   | `Optional<IotKit>` | public      | Busca kit por número de serie físico.               |
| `save(IotKit kit)`                | `IotKit`           | public      | Persiste el estado del kit.                         |
| `existsBySerialNumber(String sn)` | `boolean`          | public      | Valida existencia de serial para evitar duplicados. |

#### 4.2.4.5. Bounded Context Software Architecture Component Level Diagrams

El diagrama de componentes (C4 Nivel 3) muestra cómo se organiza internamente el contenedor Device Service (Java/Spring Boot). Se distinguen componentes clave: el IotKitController y el TelemetryController como puntos de entrada REST para la gestión administrativa y la recepción de telemetría, los application services IotKitCommandServiceImpl e IotKitQueryServiceImpl que materializan el patrón CQRS para separar la lógica de modificación del estado del hardware de las consultas de inventario, el IotKitRepository (JPA) como abstracción de persistencia y el DeviceContextFacade como ACL para exponer la disponibilidad y salud de los kits IoT a otros contextos como Planning o Therapy.

<div style="text-align: center;">
  <img src="assets/diagrams/software-architecture/components/out/device-components-diagram.png" alt="uFlex — Device Bounded Context Component Diagram" style="max-width: 100%; height: auto;">
</div>

*Figura 4.2.4.5. Diagrama de componentes (C4 Nivel 3) del Bounded Context Device.*

<hr class="page-break">

#### 4.2.4.6. Bounded Context Software Architecture Code Level Diagrams

##### 4.2.4.6.1. Bounded Context Domain Layer Class Diagrams

El diagrama de clases del Domain Layer del BC Device modela exclusivamente los conceptos centrales de la gestión de hardware IoT, sin incluir las capas de application ni infrastructure. El paquete domain.model.aggregates contiene al Aggregate Root IotKit; domain.model.valueobjects agrupa los Value Objects (IotKitId, SerialNumber, BatteryLevel, CalibrationData) y el enumerado (KitStatus); domain.model.events encapsula los Domain Events publicados por el aggregate (IotKitRegisteredEvent, IotKitCalibratedEvent, BatteryLevelChangedEvent, IotKitStatusChangedEvent); y domain.exceptions reúne las excepciones de negocio que protegen las invariantes del dominio (por ejemplo, evitar la duplicidad de registros o calibraciones en estados inválidos). Las flechas con línea continua marcan composición (el IotKit contiene sus Value Objects de estado y calibración), las flechas con línea punteada marcan dependencias semánticas (eventos publicados y excepciones lanzadas).

<div style="text-align: center;">
  <img src="assets/diagrams/uml/class/out/device-domain-layer-class-diagram.png" alt="uFlex — Device Bounded Context Domain Class Diagram" style="max-width: 100%; height: auto;">
</div>

*Figura 4.2.4.6.1. Diagrama de clases del dominio del Bounded Context Device.*

##### 4.2.4.6.2. Bounded Context Database Design Diagram

El esquema físico del BC Device en Azure Database for PostgreSQL consta de una tabla principal iot_kits que almacena el estado operativo y de salud del sensor (identificador único, serial_number, status_code, nivel de batería, versión de firmware, datos de calibración y timestamps de auditoría), y una tabla de catálogo kit_statuses para normalizar los estados permitidos del ciclo de vida del hardware (REGISTERED, LINKED, CALIBRATING, DISCONNECTED). Los índices incluyen búsquedas por serial_number para validar la unicidad y trazabilidad física del equipo, y por status_code para monitorear rápidamente flotas de dispositivos disponibles o en error. Se optó deliberadamente por no declarar foreign keys duras hacia tablas de otros bounded contexts para mantener la autonomía entre módulos, gestionando la relación lógica del kit con los pacientes y terapeutas a través de los servicios de aplicación.

<div style="text-align: center;">
  <img src="assets/diagrams/database/erd/out/device-database-design-diagram.png" alt="uFlex — Device Bounded Context Database ER Diagram" style="max-width: 100%; height: auto;">
</div>

*Figura 4.2.4.6.2. Diagrama entidad-relación del Bounded Context Device.*

<hr class="page-break">

### 4.2.5. Bounded Context: Planning

#### 4.2.5.1. Domain Layer

En esta sección se describen los elementos del Domain Layer del contexto de Planning, los cuales modelan la prescripción clínica y el ciclo de vida del tratamiento. Este lenguaje técnico permite al Fisioterapeuta definir metas biomecánicas y asegurar que el equipamiento IoT esté correctamente asignado al paciente.

**1. TreatmentPlan (Aggregate Root)**

Es el núcleo del proceso de rehabilitación. Define qué ejercicios debe realizar el paciente, bajo qué límites angulares y con qué equipo físico. Controla la invariante de negocio de que un paciente no puede tener dos planes activos simultáneamente para la misma lesión.

**Atributos principales:**

| Atributo            | Tipo                | Visibilidad | Descripción                                                    |
|---------------------|---------------------|-------------|----------------------------------------------------------------|
| `id`                | `TreatmentPlanId`   | private     | Identificador único del plan de tratamiento.                   |
| `patientId`         | `PatientId`         | private     | Referencia lógica al paciente (del BC IAM).                    |
| `physiotherapistId` | `PhysiotherapistId` | private     | Referencia al especialista responsable.                        |
| `jointType`         | `JointType`         | private     | Articulación a tratar (`ELBOW`, `WRIST`).                      |
| `targetROM`         | `TargetROM`         | private     | Rangos de movimiento objetivo (metas angulares).               |
| `status`            | `PlanStatus`        | private     | Estado del plan (`CREATED`, `ACTIVE`, `FINALIZED`, `REMOVED`). |
| `deviceId`          | `DeviceId`          | private     | Identificador del kit IoT vinculado al plan.                   |
| `createdAt`         | `LocalDateTime`     | private     | Fecha de creación del plan.                                    |

**Métodos principales:**

| Método                                    | Tipo Retorno    | Visibilidad | Descripción                                                                             |
|-------------------------------------------|-----------------|-------------|-----------------------------------------------------------------------------------------|
| `TreatmentPlan()`                         | Constructor     | public      | Constructor requerido para persistencia.                                                |
| `create(PatientId, JointType, TargetROM)` | `TreatmentPlan` | public      | Crea un nuevo plan en estado `CREATED`.                                                 |
| `linkDevice(DeviceId)`                    | `void`          | public      | Vincula un kit IoT al plan, cambia estado a `ACTIVE` y publica `IoTKitLinkedToPatient`. |
| `updatePlan(TargetROM)`                   | `void`          | public      | Actualiza las metas angulares y publica `TreatmentPlanUpdated`.                         |
| `remove()`                                | `void`          | public      | Ejecuta eliminación lógica del plan y publica `TreatmentPlanRemoved`.                   |
| `finalize(ClinicalReport)`                | `void`          | public      | Cierra el plan tras el alta médica y publica `TreatmentPlanFinalized`.                  |

**2. TargetROM (Value Object)**

Define los límites cinemáticos que el paciente debe alcanzar o no exceder durante su terapia.

| Atributo   | Tipo     | Visibilidad | Descripción                                             |
|------------|----------|-------------|---------------------------------------------------------|
| `minAngle` | `Double` | private     | Angulo minimo permitido (por ejemplo, extension total). |
| `maxAngle` | `Double` | private     | Angulo maximo objetivo (por ejemplo, flexion deseada).  |
| `unit`     | `String` | private     | Unidad de medida, siempre `degrees`.                    |

**3. PlanStatus (Value Object)**

Estado de ciclo de vida del plan terapéutico.

| Atributo    | Tipo | Visibilidad | Descripción                                 |
|-------------|------|-------------|---------------------------------------------|
| `CREATED`   | Enum | public      | Plan diseñado pero sin equipo vinculado.    |
| `ACTIVE`    | Enum | public      | Plan en ejecución con equipo IoT vinculado. |
| `FINALIZED` | Enum | public      | Tratamiento concluido exitosamente (alta).  |
| `REMOVED`   | Enum | public      | Plan cancelado o descartado.                |

**4. JointType (Value Object)**

Clasifica la articulación objetivo del tratamiento.

| Atributo | Tipo | Visibilidad | Descripción                                           |
|----------|------|-------------|-------------------------------------------------------|
| `ELBOW`  | Enum | public      | Tratamiento enfocado en la articulación del codo.     |
| `WRIST`  | Enum | public      | Tratamiento enfocado en la articulación de la muñeca. |

**5. DeviceId (Value Object)**

Referencia al kit de sensores asignado. Asegura que el código del hardware sea válido antes de la vinculación.

| Atributo       | Tipo     | Visibilidad | Descripción                                            |
|----------------|----------|-------------|--------------------------------------------------------|
| `serialNumber` | `String` | private     | Código único del kit IoT (por ejemplo, `KT-2026-001`). |

**6. ClinicalReport (Entity)**

Documento generado al finalizar el tratamiento que resume el desempeño biomecánico del paciente.

**Atributos principales:**

| Atributo         | Tipo     | Visibilidad | Descripción                                            |
|------------------|----------|-------------|--------------------------------------------------------|
| `id`             | `Long`   | private     | Identificador del reporte clínico.                     |
| `summary`        | `String` | private     | Observaciones finales del fisioterapeuta.              |
| `completionRate` | `Double` | private     | Porcentaje de cumplimiento de las sesiones prescritas. |

**10. PlanningCommandService (Domain Service)**

Coordina las operaciones complejas que involucran el estado de los planes de tratamiento.

| Método                               | Tipo Retorno | Visibilidad | Descripción                                                        |
|--------------------------------------|--------------|-------------|--------------------------------------------------------------------|
| `handle(CreateTreatmentPlanCommand)` | `UUID`       | public      | Registra un nuevo plan y valida que el paciente sea apto.          |
| `handle(LinkIoTKitCommand)`          | `boolean`    | public      | Verifica disponibilidad del kit y lo vincula al paciente.          |
| `handle(DischargePatientCommand)`    | `void`       | public      | Ejecuta el alta, genera el reporte y libera el kit IoT (`Unlink`). |

**11. PlanningQueryService (Domain Service)**

Maneja las consultas de información sobre planes activos e históricos.

| Método                                  | Tipo Retorno              | Visibilidad | Descripción                                                     |
|-----------------------------------------|---------------------------|-------------|-----------------------------------------------------------------|
| `handle(GetActivePlanByPatientIdQuery)` | `Optional<TreatmentPlan>` | public      | Obtiene el plan activo que el paciente debe ejecutar en su app. |
| `handle(GetClinicalHistoryQuery)`       | `List<TreatmentPlan>`     | public      | Lista todos los planes (activos y finalizados) de un paciente.  |

#### 4.2.5.2. Interface Layer

En esta sección se describen los elementos del Interface Layer del contexto de Planning. Esta capa expone las capacidades de prescripción clínica, asignación de dispositivos IoT y cierre terapéutico mediante contratos REST claros para la aplicación web clínica y los consumidores internos.

**1. TreatmentPlanController (REST Controller)**

Este controlador expone las capacidades de prescripción clínica y gestión de planes. Permite que el Fisioterapeuta defina la ruta de recuperación del paciente y que el Administrador supervise la asignación de recursos.

**Endpoints principales:**

| Método                   | Ruta base                          | HTTP   | Descripción                                                                   |
|--------------------------|------------------------------------|--------|-------------------------------------------------------------------------------|
| `createTreatmentPlan`    | `/api/v1/plans`                    | POST   | Registra un nuevo plan de tratamiento para un paciente específico.            |
| `updateTreatmentPlan`    | `/api/v1/plans/{id}`               | PUT    | Actualiza las metas angulares (`TargetROM`) o detalles del plan existente.    |
| `getPlanById`            | `/api/v1/plans/{id}`               | GET    | Recupera la información detallada de un plan de tratamiento.                  |
| `getActivePlanByPatient` | `/api/v1/plans/active/{patientId}` | GET    | Obtiene el plan vigente que el paciente debe ejecutar en su aplicación móvil. |
| `removeTreatmentPlan`    | `/api/v1/plans/{id}`               | DELETE | Realiza la eliminación lógica de un plan que no ha sido iniciado.             |

**2. ClinicalDischargeController (REST Controller)**

Controlador especializado en el cierre del ciclo terapéutico y la liberación de recursos de hardware.

**Endpoints principales:**

| Método               | Ruta base                           | HTTP  | Descripción                                                                                |
|----------------------|-------------------------------------|-------|--------------------------------------------------------------------------------------------|
| `dischargePatient`   | `/api/v1/plans/{id}/discharge`      | POST  | Ejecuta el alta médica, genera el reporte final y cambia el estado del plan a `FINALIZED`. |
| `linkIoTKit`         | `/api/v1/plans/{id}/link-device`    | PATCH | Vincula un kit de sensores (`DeviceId`) a un plan de tratamiento activo.                   |
| `getClinicalHistory` | `/api/v1/plans/history/{patientId}` | GET   | Lista todos los planes previos y reportes clínicos del paciente.                           |

**3. Resources (DTOs)**

Representaciones de datos optimizadas para la comunicación externa, implementadas como Java Records.

| Resource                      | Atributos principales                                                                                                            | Descripción                                                   |
|-------------------------------|----------------------------------------------------------------------------------------------------------------------------------|---------------------------------------------------------------|
| `CreateTreatmentPlanResource` | `patientId: UUID`, `physiotherapistId: UUID`, `jointType: String`, `minAngle: Double`, `maxAngle: Double`                        | Datos necesarios para la creación inicial del plan.           |
| `TreatmentPlanResource`       | `id: UUID`, `patientId: UUID`, `jointType: String`, `status: String`, `minAngle: Double`, `maxAngle: Double`, `deviceId: String` | Representación completa del plan para consulta.               |
| `UpdateTreatmentPlanResource` | `minAngle: Double`, `maxAngle: Double`                                                                                           | Datos permitidos para la actualización de metas terapéuticas. |
| `LinkDeviceResource`          | `serialNumber: String`                                                                                                           | Contiene el identificador del hardware a vincular.            |
| `DischargeResource`           | `summary: String`, `completionRate: Double`                                                                                      | Información necesaria para cerrar el caso clínico.            |
| `ClinicalReportResource`      | `planId: UUID`, `summary: String`, `finalizedAt: Date`                                                                           | Resumen ejecutivo del alta médica.                            |

**4. Transform (Assemblers)**

Componentes encargados de la traducción entre el modelo de dominio y la representación externa.

| Assembler                                   | Entrada                       | Salida                       | Descripción                                                       |
|---------------------------------------------|-------------------------------|------------------------------|-------------------------------------------------------------------|
| `TreatmentPlanFromResourceAssembler`        | `CreateTreatmentPlanResource` | `CreateTreatmentPlanCommand` | Transforma el JSON de creación en un command de dominio.          |
| `UpdatePlanCommandFromResourceAssembler`    | `UpdateTreatmentPlanResource` | `UpdateTreatmentPlanCommand` | Mapea la actualización de metas angulares.                        |
| `TreatmentPlanResourceFromEntityAssembler`  | `TreatmentPlan`               | `TreatmentPlanResource`      | Convierte el aggregate root en un recurso de lectura.             |
| `LinkDeviceCommandFromResourceAssembler`    | `LinkDeviceResource`          | `LinkDeviceCommand`          | Crea el command para asociar el hardware al paciente.             |
| `DischargeCommandFromResourceAssembler`     | `DischargeResource`           | `DischargePatientCommand`    | Prepara los datos para el proceso de alta y liberación de equipo. |
| `ClinicalReportResourceFromEntityAssembler` | `ClinicalReport`              | `ClinicalReportResource`     | Mapea la entidad de reporte a su representación REST.             |

#### 4.2.5.3. Application Layer

**1. PlanningContextFacadeImpl (ACL Facade)**

Proporciona un punto de entrada simplificado para que otros bounded contexts consulten el estado de los planes de tratamiento sin exponer la complejidad interna del aggregate.

| Atributo               | Tipo                   | Visibilidad | Descripción                                 |
|------------------------|------------------------|-------------|---------------------------------------------|
| `planningQueryService` | `PlanningQueryService` | private     | Servicio de consultas del dominio Planning. |

**Métodos principales:**

| Método                                          | Tipo Retorno              | Visibilidad | Descripción                                                                                       |
|-------------------------------------------------|---------------------------|-------------|---------------------------------------------------------------------------------------------------|
| `fetchActivePlanByPatientId(UUID patientId)`    | `Optional<ActivePlanDto>` | public      | Retorna información básica del plan activo (metas y dispositivo) para la ejecución de la terapia. |
| `isPatientUnderActiveTreatment(UUID patientId)` | `boolean`                 | public      | Verifica si el paciente tiene un plan en estado `ACTIVE`.                                         |

**2. TreatmentPlanCommandServiceImpl (Command Service Implementation)**

Orquesta la lógica de creación, actualización y cierre de planes. Coordina la publicación de eventos para notificar al BC de Inventario cuando un equipo es vinculado o liberado.

| Atributo                   | Tipo                        | Visibilidad | Descripción                                             |
|----------------------------|-----------------------------|-------------|---------------------------------------------------------|
| `treatmentPlanRepository`  | `TreatmentPlanRepository`   | private     | Acceso a la persistencia de planes.                     |
| `externalInventoryService` | `InventoryService`          | private     | ACL para validar la existencia de dispositivos físicos. |
| `eventPublisher`           | `ApplicationEventPublisher` | private     | Publicador de eventos de dominio.                       |

**Métodos principales:**

| Método                               | Tipo Retorno | Visibilidad | Descripción                                                                                            |
|--------------------------------------|--------------|-------------|--------------------------------------------------------------------------------------------------------|
| `handle(CreateTreatmentPlanCommand)` | `UUID`       | public      | Crea el plan en estado `CREATED` y valida que no existan planes activos duplicados.                    |
| `handle(LinkIoTKitCommand)`          | `void`       | public      | Valida el `serialNumber` con el BC de Inventario, vincula el equipo y publica `IoTKitLinkedToPatient`. |
| `handle(UpdateTreatmentPlanCommand)` | `void`       | public      | Actualiza los `TargetROM` y publica `TreatmentPlanUpdated`.                                            |
| `handle(DischargePatientCommand)`    | `void`       | public      | Genera el reporte clínico, finaliza el plan y publica `TreatmentPlanFinalized`.                        |

**3. PlanningQueryServiceImpl (Query Service Implementation)**

| Atributo                  | Tipo                      | Visibilidad | Descripción                                    |
|---------------------------|---------------------------|-------------|------------------------------------------------|
| `treatmentPlanRepository` | `TreatmentPlanRepository` | private     | Acceso al read model de planes de tratamiento. |

**Métodos principales:**

| Método                                  | Tipo Retorno              | Visibilidad | Descripción                                                   |
|-----------------------------------------|---------------------------|-------------|---------------------------------------------------------------|
| `handle(GetActivePlanByPatientIdQuery)` | `Optional<TreatmentPlan>` | public      | Recupera el plan vigente para el paciente.                    |
| `handle(GetClinicalHistoryQuery)`       | `List<TreatmentPlan>`     | public      | Lista todos los planes históricos asociados a un `PatientId`. |

#### 4.2.5.4. Infrastructure Layer

**1. TreatmentPlanRepository (Repository Interface)**

Interfaz de acceso a datos para los planes de tratamiento, utilizando Spring Data JPA sobre PostgreSQL.

| Método                                                    | Tipo Retorno              | Visibilidad | Descripción                                                                    |
  |-----------------------------------------------------------|---------------------------|-------------|--------------------------------------------------------------------------------|
| `findById(TreatmentPlanId id)`                            | `Optional<TreatmentPlan>` | public      | Recupera un plan por su identificador único.                                   |
| `save(TreatmentPlan plan)`                                | `TreatmentPlan`           | public      | Persiste o actualiza el estado del aggregate.                                  |
| `findByPatientIdAndStatus(PatientId pId, PlanStatus s)`   | `Optional<TreatmentPlan>` | public      | Busca un plan específico de un paciente por su estado (por ejemplo, `ACTIVE`). |
| `findAllByPatientId(PatientId patientId)`                 | `List<TreatmentPlan>`     | public      | Obtiene el historial completo de tratamientos del paciente.                    |
| `existsByPatientIdAndStatus(PatientId pId, PlanStatus s)` | `boolean`                 | public      | Invariante: verifica si ya hay un plan activo para evitar duplicidad.          |

#### 4.2.5.5. Bounded Context Software Architecture Component Level Diagrams

El diagrama de componentes (C4 Nivel 3) muestra cómo se organiza internamente el contenedor Planning Service (Java/Spring Boot). Se distinguen seis componentes principales: el TreatmentPlanController y el ClinicalDischargeController como puntos de entrada REST, los application services TreatmentPlanCommandServiceImpl y PlanningQueryServiceImpl que materializan el patrón CQRS, el TreatmentPlanRepository (JPA) como abstracción de persistencia y el ExternalInventoryServiceAdapter como ACL para validar la disponibilidad de kits IoT. Todos los componentes viven dentro del Container Boundary del Planning Service; el API Gateway queda fuera (delega tráfico) y la Planning DB también (Azure Database for PostgreSQL, consumida por JDBC/SSL).

<div style="text-align: center;">
  <img src="assets/diagrams/software-architecture/components/out/planning-components-diagram.png" alt="uFlex — Planning Bounded Context Component Diagram" style="max-width: 100%; height: auto;">
</div>

*Figura 4.2.5.5. Diagrama de componentes (C4 Nivel 3) del Bounded Context Planning.*

#### 4.2.5.6. Bounded Context Software Architecture Code Level Diagrams

##### 4.2.5.6.1. Bounded Context Domain Layer Class Diagrams

El diagrama de clases del Domain Layer del BC Planning modela exclusivamente los conceptos centrales del dominio, sin incluir las capas de application ni infrastructure. El paquete `domain.model.aggregates` contiene al Aggregate Root `TreatmentPlan`; `domain.model.entities` incluye la Entity `ClinicalReport`; `domain.model.valueobjects` agrupa los Value Objects (`TreatmentPlanId`, `PatientId`, `PhysiotherapistId`, `TargetROM`, `DeviceId`) y los enumerados (`PlanStatus`, `JointType`); `domain.model.events` encapsula los Domain Events publicados por el aggregate (`IoTKitLinkedToPatient`, `TreatmentPlanUpdated`, `TreatmentPlanRemoved`, `TreatmentPlanFinalized`); y `domain.exceptions` reúne las excepciones de negocio que protegen las invariantes del dominio (por ejemplo, evitar más de un plan activo por paciente/lesión). Las flechas con línea continua marcan composición (el `TreatmentPlan` contiene sus Value Objects), las flechas con línea punteada marcan dependencias semánticas (eventos publicados y excepciones lanzadas) y los rombos vacíos indican agregación con cardinalidad opcional o múltiple (relación del plan con `DeviceId` y con `ClinicalReport` al cierre terapéutico).

<div style="text-align: center;">
  <img src="assets/diagrams/uml/class/out/planning-domain-layer-class-diagram.png" alt="uFlex — IAM Bounded Context Domain Class Diagram" style="max-width: 100%; height: auto;">
</div>

*Figura 4.2.5.6.1. Diagrama de clases del dominio del Bounded Context Planning.*

##### 4.2.5.6.2. Bounded Context Database Design Diagram

El esquema físico del BC Planning en Azure Database for PostgreSQL consta de una tabla principal `treatment_plans` que almacena el estado clínico-operativo del tratamiento (identificador del plan, `patient_id`, `physiotherapist_id`, articulación objetivo, rangos `min_angle`/`max_angle`, estado del plan, `device_id` y timestamps de auditoría), una tabla `clinical_reports` para registrar el resultado de alta asociado al plan (resumen y porcentaje de cumplimiento), y una tabla de catálogo `plan_statuses` para normalizar los estados permitidos del ciclo de vida (`CREATED`, `ACTIVE`, `FINALIZED`, `REMOVED`). Los índices incluyen búsquedas por `(patient_id, status)` para obtener rápidamente el plan activo, por `patient_id` para el historial clínico, y por `device_id` para trazabilidad del kit IoT asignado. Se optó deliberadamente por **no** declarar foreign keys duras hacia tablas de otros bounded contexts (`patient_id` del BC IAM y `device_id` del BC Device/Inventory): las referencias son lógicas para mantener la autonomía entre contextos.

<div style="text-align: center;">
  <img src="assets/diagrams/database/erd/out/planning-database-design-diagram.png" alt="uFlex — IAM Bounded Context Database ER Diagram" style="max-width: 100%; height: auto;">
</div>

*Figura 4.2.5.6.2. Diagrama entidad-relación del Bounded Context Planning.*

<hr class="page-break">

### 4.2.6. Bounded Context: Therapy

El Bounded Context **Therapy** encapsula toda la logica de negocio relacionada con la ejecucion de sesiones de terapia fisica asistida por dispositivos IoT dentro de uFlex. Su responsabilidad central es orquestar el ciclo de vida completo de una sesion terapeutica: desde la preparacion del hardware y la identificacion de la rutina diaria, pasando por la ejecucion y validacion de series de ejercicios con captura de datos de movimiento articular en tiempo real, hasta la finalizacion y cierre de la sesion.

Este contexto modela la interaccion entre el paciente, los sensores IoT y las reglas clinicas que determinan si un movimiento fue ejecutado correctamente, si se alcanzo un umbral angular prescrito o si corresponde emitir una alerta por movimiento excesivo o anomalo. Por ello, la integridad de la sesion terapeutica y la trazabilidad de cada repeticion validada son responsabilidades exclusivas del contexto Therapy.

Desde una perspectiva de integracion entre bounded contexts, Therapy se comunica con **Planning** para obtener la rutina diaria y los parametros clinicos asignados al paciente, y se integra con **Device** para confirmar disponibilidad, estado y posicionamiento correcto del kit IoT antes y durante la ejecucion. Esta separacion de responsabilidades permite mantener un modelo de dominio cohesivo, centrado en el valor diferencial de uFlex: monitoreo biomecanico en tiempo real con retroalimentacion clinica continua.

#### 4.2.6.1. Domain Layer

En esta sección se describen los elementos iniciales del Domain Layer del contexto de Therapy, que modelan la ejecución de la sesión terapéutica y las invariantes clínicas asociadas al monitoreo biomecánico en tiempo real.

**1. TherapySession (Aggregate Root)**

Es el núcleo del proceso terapéutico remoto. Controla el ciclo de vida completo de una sesión: preparación del hardware, inicio de rutina, registro de dolor, validación de repeticiones, detección de movimientos anómalos y cierre formal. Como aggregate root, protege las invariantes para evitar transiciones inválidas de estado y asegurar la trazabilidad clínica de cada sesión.

**Atributos principales:**

| Atributo            | Tipo                | Visibilidad | Descripción                                                                              |
|---------------------|---------------------|-------------|------------------------------------------------------------------------------------------|
| `id`                | `TherapySessionId`  | private     | Identificador único de la sesión.                                                        |
| `patientId`         | `PatientId`         | private     | Referencia al paciente que ejecuta la sesión.                                            |
| `treatmentPlanId`   | `TreatmentPlanId`   | private     | Referencia desnormalizada al plan de tratamiento origen para trazabilidad clínica.       |
| `iotDeviceId`       | `DeviceId`          | private     | Referencia al dispositivo IoT que reporta telemetría en tiempo real.                     |
| `routine`           | `Routine`           | private     | Rutina asignada a ejecutar en la sesión.                                                 |
| `sensorSnapshot`    | `IoTSensorSnapshot` | private     | Estado del posicionamiento de sensores al iniciar.                                       |
| `painLevelReported` | `PainLevel`         | private     | Nivel de dolor reportado por el paciente durante la sesión.                              |
| `status`            | `SessionStatus`     | private     | Estado actual de la sesión (`Pending`, `Ready`, `InProgress`, `Completed`, `Cancelled`). |
| `startedAt`         | `DateTime`          | private     | Fecha y hora de inicio de la sesión.                                                     |
| `finalizedAt`       | `DateTime`          | private     | Fecha y hora de cierre de la sesión.                                                     |

**Métodos principales:**

| Método                                                                  | Tipo Retorno     | Visibilidad | Descripción                                                                                                                                          |
|-------------------------------------------------------------------------|------------------|-------------|------------------------------------------------------------------------------------------------------------------------------------------------------|
| `initiatePreparation(patientId, treatmentPlanId, iotDeviceId, routine)` | `TherapySession` | public      | Crea la sesión con los datos base y la deja en estado `Pending`.                                                                                     |
| `confirmHardwareReadiness(snapshot)`                                    | `void`           | public      | Registra el snapshot de sensores y avanza el estado a `Ready`.                                                                                       |
| `startRoutine()`                                                        | `void`           | public      | Inicia la ejecución de la rutina y cambia el estado a `InProgress`.                                                                                  |
| `reportPainLevel(painLevel)`                                            | `void`           | public      | Registra el nivel de dolor reportado por el paciente.                                                                                                |
| `recordAnomalousMovement(alertType)`                                    | `void`           | public      | Registra la anomalía detectada y publica el evento de dominio correspondiente (`ExcessiveMovementAlertIssued` o `AnomalousMovementDetected`).        |
| `recordValidRepetition(serieId)`                                        | `void`           | public      | Registra una repetición válida en la serie indicada, delega en `Routine`/`Serie` y actualiza el estado de validación de la rutina según corresponda. |
| `finalizeSession()`                                                     | `void`           | public      | Cierra la sesión exitosamente; valida que la rutina esté `Completed` y cambia estado a `Completed`.                                                  |
| `cancelSession()`                                                       | `void`           | public      | Cancela la sesión antes de completarse y cambia estado a `Cancelled`.                                                                                |
| `ensureHardwareReady()`                                                 | `void`           | private     | Invariante: no se puede iniciar la rutina si el estado no es `Ready`.                                                                                |
| `ensureSensorsPlaced()`                                                 | `void`           | private     | Invariante: el snapshot debe confirmar que los sensores están posicionados correctamente.                                                            |
| `ensureRoutineAssigned()`                                               | `void`           | private     | Invariante: debe existir una rutina asociada antes de iniciar.                                                                                       |
| `ensureNotFinalized()`                                                  | `void`           | private     | Invariante: una sesión en estado `Completed` o `Cancelled` no acepta más operaciones.                                                                |

**Notas de diseño:**

- `recordAnomalousMovement` mantiene el dominio puro: el aggregate registra el hecho y publica el evento; la ejecución física de la respuesta (vibración/alerta visual) se resuelve en handlers de capas superiores.
- `recordValidRepetition` conecta la validación de repeticiones con el estado de la sesión: el aggregate delega en `Routine` y `Serie`, y propaga el cierre de serie/rutina cuando se alcanzan los objetivos.

**2. Routine (Entity)**

Entidad con identidad local dentro de la sesión. Agrupa y ordena las series de ejercicios a ejecutar. Su ciclo de vida depende completamente de `TherapySession`; si la sesión se cancela, la rutina deja de tener validez operativa.

**Atributos principales:**

| Atributo | Tipo            | Visibilidad | Descripción                                              |
|----------|-----------------|-------------|----------------------------------------------------------|
| `id`     | `RoutineId`     | private     | Identificador local dentro de la sesión.                 |
| `name`   | `String`        | private     | Nombre descriptivo (por ejemplo, `Rutina 1`).            |
| `series` | `List<Serie>`   | private     | Lista ordenada de series que componen la rutina.         |
| `status` | `RoutineStatus` | private     | Estado de ejecución (`Pending`, `Started`, `Completed`). |

**Métodos principales:**

| Método                                  | Tipo Retorno      | Visibilidad | Descripción                                                                                                                             |
|-----------------------------------------|-------------------|-------------|-----------------------------------------------------------------------------------------------------------------------------------------|
| `start()`                               | `void`            | public      | Marca la rutina como iniciada.                                                                                                          |
| `startNextSerie()`                      | `void`            | public      | Inicia la siguiente serie pendiente en orden.                                                                                           |
| `recordValidRepetitionInSerie(serieId)` | `void`            | public      | Delega el registro de repetición válida en la serie correspondiente; si la serie se completa, evalúa si toda la rutina está completada. |
| `isCompleted()`                         | `boolean`         | public      | Retorna `true` si todas las series están en estado `Validated`.                                                                         |
| `currentSerie()`                        | `Optional<Serie>` | public      | Retorna la serie actualmente en ejecución.                                                                                              |
| `markAsCompleted()`                     | `void`            | private     | Cambia el estado a `Completed` cuando todas las series están validadas.                                                                 |
| `findSerie(serieId)`                    | `Serie`           | private     | Localiza una serie por su id local; lanza `SerieNotFoundException` si no existe.                                                        |

**3. Serie (Entity)**

Unidad de ejecución dentro de una rutina. Combina una referencia al ejercicio base con sus parámetros clínicos de ejecución. Mantiene estado mutable: contador de repeticiones válidas acumuladas y estado de progreso.

**Atributos principales:**

| Atributo                | Tipo                        | Visibilidad | Descripción                                                       |
|-------------------------|-----------------------------|-------------|-------------------------------------------------------------------|
| `id`                    | `SerieId`                   | private     | Identificador local dentro de la rutina.                          |
| `exerciseId`            | `ExerciseId`                | private     | Referencia al identificador del ejercicio en el catálogo maestro. |
| `targetRepetitions`     | `RepetitionCount`           | private     | Número de repeticiones objetivo.                                  |
| `angleThreshold`        | `AngleThreshold`            | private     | Rango angular válido para esta serie.                             |
| `instructionalVideoUrl` | `String`                    | private     | URL del video instruccional asociado.                             |
| `currentRepetitions`    | `int`                       | private     | Contador de repeticiones válidas acumuladas.                      |
| `completedRepetitions`  | `List<CompletedRepetition>` | private     | Historial inmutable de repeticiones validadas.                    |
| `status`                | `SerieStatus`               | private     | Estado de la serie (`Pending`, `Started`, `Validated`, `Failed`). |

**Métodos principales:**

| Método                                       | Tipo Retorno | Visibilidad | Descripción                                                                                                              |
|----------------------------------------------|--------------|-------------|--------------------------------------------------------------------------------------------------------------------------|
| `start()`                                    | `void`       | public      | Inicia la serie y cambia estado a `Started`.                                                                             |
| `playInstructionalVideo()`                   | `void`       | public      | Registra que el video instruccional fue reproducido.                                                                     |
| `recordValidRepetition(completedRepetition)` | `void`       | public      | Incrementa `currentRepetitions` y agrega el `CompletedRepetition`; si se alcanza el objetivo, invoca `markAsAchieved()`. |
| `isValidated()`                              | `boolean`    | public      | Retorna `true` si el estado es `Validated`.                                                                              |
| `markAsAchieved()`                           | `void`       | private     | Cambia el estado a `Validated` cuando `hasReachedTarget()` es verdadero.                                                 |
| `hasReachedTarget()`                         | `boolean`    | private     | Retorna `true` si `currentRepetitions >= targetRepetitions.value`.                                                       |

**4. ExerciseId (Value Object)**

Identificador del ejercicio definido en el catálogo maestro (bounded context externo). El dominio Therapy sólo requiere esta referencia para operar; los datos visuales del ejercicio pertenecen al read model.

| Atributo | Tipo   | Visibilidad | Descripción                                               |
|----------|--------|-------------|-----------------------------------------------------------|
| `value`  | `UUID` | private     | Identificador único del ejercicio en el catálogo maestro. |

**5. CompletedRepetition (Value Object)**

Registro inmutable de una repetición ya ejecutada y validada por el Edge App. Representa un hecho consumado y no cambia una vez persistido.

| Atributo             | Tipo       | Visibilidad | Descripción                                                      |
|----------------------|------------|-------------|------------------------------------------------------------------|
| `achievedAngle`      | `Float`    | private     | Ángulo articular final alcanzado en la repetición.               |
| `wasWithinThreshold` | `Boolean`  | private     | Indica si el ángulo estuvo dentro del `AngleThreshold` definido. |
| `recordedAt`         | `DateTime` | private     | Timestamp de la captura validada.                                |

**6. IoTSensorSnapshot (Value Object)**

Instantánea inmutable del estado de posicionamiento de sensores IoT al momento de confirmar el hardware para inicio de sesión.

| Atributo        | Tipo       | Visibilidad | Descripción                                                    |
|-----------------|------------|-------------|----------------------------------------------------------------|
| `deviceId`      | `String`   | private     | Identificador del dispositivo IoT confirmado.                  |
| `sensorsPlaced` | `Boolean`  | private     | Indica si todos los sensores están correctamente posicionados. |
| `recordedAt`    | `DateTime` | private     | Timestamp del momento de confirmación.                         |

**7. AngleThreshold (Value Object)**

Define el rango angular aceptable para validar el movimiento de una repetición.

| Atributo   | Tipo    | Visibilidad | Descripción                        |
|------------|---------|-------------|------------------------------------|
| `minAngle` | `Float` | private     | Ángulo mínimo aceptable en grados. |
| `maxAngle` | `Float` | private     | Ángulo máximo aceptable en grados. |

| Método                 | Tipo Retorno | Visibilidad | Descripción                                                          |
|------------------------|--------------|-------------|----------------------------------------------------------------------|
| `isWithinRange(angle)` | `boolean`    | public      | Retorna `true` si el ángulo recibido está dentro del rango definido. |

**8. PainLevel (Value Object)**

Nivel de dolor autorreportado por el paciente sobre una escala clínica acotada.

| Atributo | Tipo      | Visibilidad | Descripción             |
|----------|-----------|-------------|-------------------------|
| `value`  | `Integer` | private     | Valor entre `0` y `10`. |

| Método      | Tipo Retorno | Visibilidad | Descripción                                            |
|-------------|--------------|-------------|--------------------------------------------------------|
| `isValid()` | `boolean`    | public      | Retorna `true` si el valor está en el rango `[0, 10]`. |

**9. RepetitionCount (Value Object)**

Número de repeticiones objetivo para una serie. Garantiza que el valor sea positivo.

| Atributo | Tipo      | Visibilidad | Descripción                                |
|----------|-----------|-------------|--------------------------------------------|
| `value`  | `Integer` | private     | Cantidad de repeticiones objetivo (`> 0`). |

**10. SessionStatus (Value Object)**

Estado del ciclo de vida de la sesión terapéutica.

| Atributo     | Tipo | Visibilidad | Descripción                                          |
|--------------|------|-------------|------------------------------------------------------|
| `Pending`    | Enum | public      | Sesión creada, pendiente de preparación de hardware. |
| `Ready`      | Enum | public      | Hardware y sensores confirmados; lista para iniciar. |
| `InProgress` | Enum | public      | Rutina en ejecución activa.                          |
| `Completed`  | Enum | public      | Rutina finalizada y sesión cerrada correctamente.    |
| `Cancelled`  | Enum | public      | Sesión cancelada antes de completarse.               |

**11. RoutineStatus (Value Object)**

Estado de ejecución de la rutina.

| Atributo    | Tipo | Visibilidad | Descripción                         |
|-------------|------|-------------|-------------------------------------|
| `Pending`   | Enum | public      | Rutina creada pero aún no iniciada. |
| `Started`   | Enum | public      | Rutina en ejecución.                |
| `Completed` | Enum | public      | Rutina validada en su totalidad.    |

**12. SerieStatus (Value Object)**

Estado de ejecución de una serie.

| Atributo    | Tipo | Visibilidad | Descripción                                                |
|-------------|------|-------------|------------------------------------------------------------|
| `Pending`   | Enum | public      | Serie pendiente de ejecución.                              |
| `Started`   | Enum | public      | Serie iniciada.                                            |
| `Validated` | Enum | public      | Serie completada y validada clínicamente.                  |
| `Failed`    | Enum | public      | Serie finalizada con incumplimiento de criterios clínicos. |

**13. MovementAlertType (Value Object)**

Tipo de alerta de movimiento registrada durante la sesión y propagada como evento de dominio.

| Atributo            | Tipo | Visibilidad | Descripción                                              |
|---------------------|------|-------------|----------------------------------------------------------|
| `ExcessiveMovement` | Enum | public      | Alerta por exceso de rango o intensidad de movimiento.   |
| `AnomalousMovement` | Enum | public      | Alerta por patrón de movimiento compensatorio o atípico. |

**14. IDs (Value Objects)**

Todos los identificadores del dominio (`TherapySessionId`, `PatientId`, `TreatmentPlanId`, `DeviceId`, `RoutineId`, `SerieId`) envuelven un `UUID` para garantizar type safety y evitar intercambios incorrectos de tipos en operaciones del dominio.

| Atributo | Tipo   | Visibilidad | Descripción                                                    |
|----------|--------|-------------|----------------------------------------------------------------|
| `value`  | `UUID` | private     | Identificador inmutable tipado para cada concepto del dominio. |

**15. MotionAnalysisService (Domain Service)**

Analiza los datos de movimiento articular capturados y preprocesados por el Edge App para determinar si existe movimiento excesivo o anómalo. Esta lógica se modela como servicio de dominio porque evalúa reglas clínicas transversales que no pertenecen a una sola entidad.

| Método                               | Tipo Retorno                  | Visibilidad | Descripción                                                                                                                                                          |
|--------------------------------------|-------------------------------|-------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `analyze(motionData, threshold)`     | `Optional<MovementAlertType>` | public      | Evalúa los datos de movimiento contra el `AngleThreshold` de la serie activa; retorna un `MovementAlertType` si detecta anomalía o vacío si el movimiento es normal. |
| `isAnomalous(motionData, threshold)` | `boolean`                     | private     | Determina si el ángulo medido está fuera del rango seguro definido por el threshold.                                                                                 |
| `isExcessive(motionData)`            | `boolean`                     | private     | Determina si la amplitud del movimiento supera límites de seguridad absolutos, independientemente del threshold de la serie.                                         |

**16. RepetitionValidationService (Domain Service)**

Evalúa si una repetición procesada por el Edge App cumple el `AngleThreshold` definido en la serie y retorna un `CompletedRepetition` listo para registrarse. Centraliza la regla clínica de aceptación de repetición fuera de las entidades.

| Método                               | Tipo Retorno          | Visibilidad | Descripción                                                                                                               |
|--------------------------------------|-----------------------|-------------|---------------------------------------------------------------------------------------------------------------------------|
| `validate(achievedAngle, threshold)` | `CompletedRepetition` | public      | Compara el ángulo alcanzado con el `AngleThreshold` y retorna un `CompletedRepetition` con `wasWithinThreshold` resuelto. |

**17. Commands**

| Command                             | Atributos principales                                      | Descripción                                                                     |
|-------------------------------------|------------------------------------------------------------|---------------------------------------------------------------------------------|
| `InitiateTherapyPreparationCommand` | `patientId`, `treatmentPlanId`, `iotDeviceId`, `routineId` | Crea y prepara la sesión de terapia dejándola en estado `Pending`.              |
| `ConfirmHardwareReadinessCommand`   | `sessionId`, `deviceId`, `sensorsPlaced`                   | Confirma hardware y sensores; avanza la sesión a `Ready`.                       |
| `StartRoutineCommand`               | `sessionId`                                                | Inicia la ejecución de la rutina asignada; avanza la sesión a `InProgress`.     |
| `StartSerieCommand`                 | `sessionId`, `serieId`                                     | Inicia una serie específica dentro de la rutina.                                |
| `RecordValidRepetitionCommand`      | `sessionId`, `serieId`, `achievedAngle`, `recordedAt`      | Registra una repetición validada por el Edge App en la serie indicada.          |
| `RecordAnomalousMovementCommand`    | `sessionId`, `alertType`                                   | Registra una anomalía de movimiento y emite el evento correspondiente.          |
| `ReportPainLevelCommand`            | `sessionId`, `painLevel`                                   | Registra el nivel de dolor reportado por el paciente.                           |
| `FinalizeTherapySessionCommand`     | `sessionId`                                                | Cierra y finaliza formalmente la sesión; valida que la rutina esté `Completed`. |
| `CancelTherapySessionCommand`       | `sessionId`, `reason`                                      | Cancela la sesión en cualquier punto antes de completarse.                      |

**18. Queries**

| Query                            | Atributos principales  | Descripción                                                                                    |
|----------------------------------|------------------------|------------------------------------------------------------------------------------------------|
| `GetDailyTherapyScheduleQuery`   | `patientId`, `date`    | Obtiene la rutina asignada al paciente para el día consultado.                                 |
| `GetSessionProgressQuery`        | `sessionId`            | Retorna el estado de la sesión: serie activa, repeticiones por serie y estado general.         |
| `GetPainLevelHistoryQuery`       | `patientId`            | Retorna el historial de niveles de dolor reportados por el paciente.                           |
| `GetSerieDetailsQuery`           | `sessionId`, `serieId` | Retorna parámetros clínicos y progreso de una serie específica.                                |
| `GetSessionSummaryQuery`         | `sessionId`            | Retorna resumen de sesión finalizada: repeticiones por serie, alertas, dolor y duración total. |
| `GetActiveSessionByPatientQuery` | `patientId`            | Retorna la sesión actualmente en progreso para un paciente, si existe.                         |

**19. Domain Exceptions**

| Excepción                                 | Descripción                                                                               |
|-------------------------------------------|-------------------------------------------------------------------------------------------|
| `HardwareNotReadyException`               | Se lanza cuando se intenta iniciar la rutina sin hardware confirmado (`status != Ready`). |
| `IoTSensorsNotPlacedException`            | Se lanza cuando el snapshot indica que los sensores no están correctamente posicionados.  |
| `RoutineNotAssignedToSessionException`    | Se lanza al iniciar rutina sin una rutina asociada a la sesión.                           |
| `TherapySessionAlreadyFinalizedException` | Se lanza al intentar operar sobre una sesión en estado `Completed` o `Cancelled`.         |
| `SerieNotFoundException`                  | Se lanza al referenciar un `SerieId` inexistente dentro de la rutina.                     |
| `SerieNotStartedException`                | Se lanza al intentar registrar una repetición en una serie aún no iniciada.               |
| `SerieAlreadyAchievedException`           | Se lanza al intentar registrar una repetición en una serie ya marcada como `Validated`.   |
| `InvalidPainLevelException`               | Se lanza cuando el valor de dolor está fuera del rango permitido `[0, 10]`.               |
| `InvalidAngleThresholdException`          | Se lanza cuando `minAngle` es mayor o igual a `maxAngle`.                                 |
| `InvalidRepetitionCountException`         | Se lanza cuando el número de repeticiones objetivo es menor o igual a cero.               |

#### 4.2.6.2. Interface Layer

En esta sección se describen los elementos del Interface Layer del bounded context de Therapy. Esta capa expone las capacidades de ejecución terapéutica mediante contratos REST claros para la aplicación móvil del paciente y para el Edge App que reporta la telemetría de movimiento en tiempo real.

**1. TherapySessionController (REST Controller)**

Controlador principal del ciclo de vida de la sesión terapéutica. Permite al paciente iniciar, ejecutar y cerrar su sesión diaria, y al fisioterapeuta consultar el progreso y resumen de cada sesión.

| Método                      | Ruta base                                     | HTTP  | Descripción                                                                                               |
|-----------------------------|-----------------------------------------------|-------|-----------------------------------------------------------------------------------------------------------|
| `initiatePreparation`       | `/api/v1/therapy-sessions`                    | POST  | Crea y prepara una nueva sesión de terapia para un paciente, dejándola en estado `Pending`.               |
| `confirmHardwareReadiness`  | `/api/v1/therapy-sessions/{id}/hardware`      | PATCH | Confirma el posicionamiento correcto de los sensores IoT; avanza la sesión a `Ready`.                     |
| `startRoutine`              | `/api/v1/therapy-sessions/{id}/start`         | PATCH | Inicia la ejecución de la rutina asignada; avanza la sesión a `InProgress`.                               |
| `getSessionProgress`        | `/api/v1/therapy-sessions/{id}/progress`      | GET   | Retorna el estado actual de la sesión: serie activa, repeticiones completadas por serie y estado general. |
| `getActiveSessionByPatient` | `/api/v1/therapy-sessions/active/{patientId}` | GET   | Retorna la sesión actualmente en progreso para un paciente dado.                                          |
| `getSessionSummary`         | `/api/v1/therapy-sessions/{id}/summary`       | GET   | Retorna el resumen completo de una sesión finalizada.                                                     |
| `finalizeSession`           | `/api/v1/therapy-sessions/{id}/finalize`      | PATCH | Cierra formalmente la sesión una vez completada la rutina.                                                |
| `cancelSession`             | `/api/v1/therapy-sessions/{id}/cancel`        | PATCH | Cancela la sesión antes de completarse.                                                                   |

**2. TherapyExecutionController (REST Controller)**

Controlador especializado en la ejecución en tiempo real de series y registro de progreso durante la sesión activa. Recibe los datos procesados por el Edge App y los reportes del paciente.

| Método                    | Ruta base                                                    | HTTP  | Descripción                                                                     |
|---------------------------|--------------------------------------------------------------|-------|---------------------------------------------------------------------------------|
| `startSerie`              | `/api/v1/therapy-sessions/{id}/series/{serieId}/start`       | PATCH | Inicia una serie específica dentro de la rutina activa.                         |
| `recordValidRepetition`   | `/api/v1/therapy-sessions/{id}/series/{serieId}/repetitions` | POST  | Registra una repetición válida procesada y enviada por el Edge App.             |
| `recordAnomalousMovement` | `/api/v1/therapy-sessions/{id}/anomalies`                    | POST  | Registra una anomalía de movimiento detectada durante la ejecución de la serie. |
| `reportPainLevel`         | `/api/v1/therapy-sessions/{id}/pain`                         | PATCH | Registra el nivel de dolor autorreportado por el paciente.                      |
| `getSerieDetails`         | `/api/v1/therapy-sessions/{id}/series/{serieId}`             | GET   | Retorna los parámetros clínicos y el progreso actual de una serie específica.   |
| `getDailySchedule`        | `/api/v1/therapy-sessions/schedule/{patientId}`              | GET   | Obtiene la rutina asignada al paciente para el día consultado.                  |

**3. Resources (DTOs)**

Representaciones de datos optimizadas para la comunicación externa, implementadas como Java Records.

| Resource                             | Atributos principales                                                                                                                                                                        | Descripción                                                                                 |
|--------------------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|---------------------------------------------------------------------------------------------|
| `InitiateTherapyPreparationResource` | `patientId: UUID`, `treatmentPlanId: UUID`, `iotDeviceId: String`, `routineId: UUID`                                                                                                         | Datos necesarios para crear e iniciar la preparación de una sesión.                         |
| `ConfirmHardwareReadinessResource`   | `deviceId: String`, `sensorsPlaced: Boolean`                                                                                                                                                 | Datos del snapshot de posicionamiento de sensores para confirmar el hardware.               |
| `RecordValidRepetitionResource`      | `achievedAngle: Double`, `recordedAt: DateTime`                                                                                                                                              | Datos de una repetición procesada por el Edge App listos para registrar.                    |
| `RecordAnomalousMovementResource`    | `alertType: String`                                                                                                                                                                          | Tipo de anomalía detectada durante la ejecución (`ExcessiveMovement`, `AnomalousMovement`). |
| `ReportPainLevelResource`            | `painLevel: Integer`                                                                                                                                                                         | Nivel de dolor autorreportado por el paciente en escala `[0, 10]`.                          |
| `CancelTherapySessionResource`       | `reason: String`                                                                                                                                                                             | Motivo de cancelación de la sesión.                                                         |
| `TherapySessionResource`             | `id: UUID`, `patientId: UUID`, `treatmentPlanId: UUID`, `iotDeviceId: String`, `status: String`, `startedAt: DateTime`, `finalizedAt: DateTime`                                              | Representación completa de la sesión para consulta.                                         |
| `SessionProgressResource`            | `sessionId: UUID`, `status: String`, `currentSerieId: UUID`, `seriesProgress: List<SerieProgressResource>`                                                                                   | Estado de avance de la sesión con detalle por serie.                                        |
| `SerieProgressResource`              | `serieId: UUID`, `exerciseId: UUID`, `currentRepetitions: Integer`, `targetRepetitions: Integer`, `status: String`                                                                           | Estado de avance de una serie individual.                                                   |
| `SerieDetailsResource`               | `serieId: UUID`, `exerciseId: UUID`, `targetRepetitions: Integer`, `minAngle: Double`, `maxAngle: Double`, `instructionalVideoUrl: String`, `status: String`                                 | Parámetros clínicos completos de una serie.                                                 |
| `SessionSummaryResource`             | `sessionId: UUID`, `patientId: UUID`, `totalSeries: Integer`, `completedSeries: Integer`, `painLevel: Integer`, `anomaliesDetected: Integer`, `startedAt: DateTime`, `finalizedAt: DateTime` | Resumen ejecutivo de una sesión finalizada.                                                 |
| `DailyScheduleResource`              | `patientId: UUID`, `date: Date`, `routineId: UUID`, `totalSeries: Integer`, `estimatedDurationMinutes: Integer`                                                                              | Rutina asignada al paciente para el día consultado.                                         |

**4. Transform (Assemblers)**

Componentes encargados de la traducción entre el modelo de dominio y la representación externa.

| Assembler                                                | Entrada                              | Salida                              | Descripción                                                                                       |
|----------------------------------------------------------|--------------------------------------|-------------------------------------|---------------------------------------------------------------------------------------------------|
| `InitiateTherapyPreparationCommandFromResourceAssembler` | `InitiateTherapyPreparationResource` | `InitiateTherapyPreparationCommand` | Transforma el JSON de creación en el command de dominio correspondiente.                          |
| `ConfirmHardwareReadinessCommandFromResourceAssembler`   | `ConfirmHardwareReadinessResource`   | `ConfirmHardwareReadinessCommand`   | Mapea los datos del snapshot de sensores al command de confirmación de hardware.                  |
| `RecordValidRepetitionCommandFromResourceAssembler`      | `RecordValidRepetitionResource`      | `RecordValidRepetitionCommand`      | Traduce los datos de repetición enviados por el Edge App al command de dominio.                   |
| `RecordAnomalousMovementCommandFromResourceAssembler`    | `RecordAnomalousMovementResource`    | `RecordAnomalousMovementCommand`    | Mapea el tipo de alerta al command de registro de anomalía.                                       |
| `ReportPainLevelCommandFromResourceAssembler`            | `ReportPainLevelResource`            | `ReportPainLevelCommand`            | Traduce el nivel de dolor reportado al command de dominio.                                        |
| `CancelTherapySessionCommandFromResourceAssembler`       | `CancelTherapySessionResource`       | `CancelTherapySessionCommand`       | Mapea el motivo de cancelación al command correspondiente.                                        |
| `TherapySessionResourceFromEntityAssembler`              | `TherapySession`                     | `TherapySessionResource`            | Convierte el aggregate root en su representación REST para consulta.                              |
| `SessionProgressResourceFromEntityAssembler`             | `TherapySession`                     | `SessionProgressResource`           | Construye la vista de progreso de la sesión a partir del estado interno del agregado y su rutina. |
| `SerieDetailsResourceFromEntityAssembler`                | `Serie`                              | `SerieDetailsResource`              | Mapea los parámetros clínicos y el estado de una serie a su representación REST.                  |
| `SessionSummaryResourceFromEntityAssembler`              | `TherapySession`                     | `SessionSummaryResource`            | Construye el resumen ejecutivo de la sesión finalizada a partir del agregado completo.            |

#### 4.2.6.3. Application Layer

En esta sección se explican las clases responsables de orquestar los casos de uso del Bounded Context de Therapy. Esta capa recibe los Commands y Queries de la Interface Layer, coordina la validación con los Domain Services, recupera el Aggregate Root desde la base de datos y publica los Domain Events correspondientes.

**1. TherapyContextFacadeImpl (ACL Facade)**

Actúa como una capa anticorrupción (Anti-Corruption Layer) y punto de entrada simplificado para que otros bounded contexts (como Planning o Gamification) puedan consultar datos de la sesión sin acoplarse al modelo interno de Therapy.

| Atributo              | Tipo                  | Visibilidad | Descripción                                        |
|-----------------------|-----------------------|-------------|----------------------------------------------------|
| `therapyQueryService` | `TherapyQueryService` | private     | Servicio interno de consultas del dominio Therapy. |

**Métodos principales:**

| Método                                      | Tipo Retorno                  | Visibilidad | Descripción                                                                                                       |
|---------------------------------------------|-------------------------------|-------------|-------------------------------------------------------------------------------------------------------------------|
| `isPatientInActiveSession(UUID patientId)`  | `boolean`                     | public      | Verifica si el paciente tiene una sesión en estado `Ready` o `InProgress` para evitar duplicidades.               |
| `fetchLastSessionSummary(UUID patientId)`   | `Optional<SessionSummaryDto>` | public      | Retorna el reporte de la última sesión para que Planning pueda actualizar el historial clínico general.           |
| `countCompletedSessionsByPlan(UUID planId)` | `int`                         | public      | Retorna el número de sesiones completadas asociadas a un plan de tratamiento, útil para calcular el avance macro. |

**2. TherapySessionCommandServiceImpl (Command Service)**

Orquesta los casos de uso relacionados con el ciclo de vida general de la sesión (preparación, inicio, cancelación y cierre).

| Atributo            | Tipo                        | Visibilidad | Descripción                                               |
|---------------------|-----------------------------|-------------|-----------------------------------------------------------|
| `sessionRepository` | `TherapySessionRepository`  | private     | Puerto para acceder a la persistencia del aggregate root. |
| `eventPublisher`    | `ApplicationEventPublisher` | private     | Publicador de eventos de dominio hacia el bus de eventos. |

**Métodos principales:**

| Método                                      | Tipo Retorno | Visibilidad | Descripción                                                                                                                           |
|---------------------------------------------|--------------|-------------|---------------------------------------------------------------------------------------------------------------------------------------|
| `handle(InitiateTherapyPreparationCommand)` | `UUID`       | public      | Instancia el agregado `TherapySession` en estado `Pending` y lo persiste.                                                             |
| `handle(ConfirmHardwareReadinessCommand)`   | `void`       | public      | Recupera la sesión, invoca `confirmHardwareReadiness()`, guarda el snapshot y publica `HardwareReadinessConfirmed`.                   |
| `handle(StartRoutineCommand)`               | `void`       | public      | Cambia el estado a `InProgress`, inicia la rutina y publica `RoutineStarted`.                                                         |
| `handle(ReportPainLevelCommand)`            | `void`       | public      | Recupera la sesión, registra el dolor del paciente y publica `PainLevelReported`.                                                     |
| `handle(FinalizeTherapySessionCommand)`     | `void`       | public      | Ejecuta `finalizeSession()`, persiste el estado `Completed` y publica `TherapySessionCompleted`.                                      |
| `handle(CancelTherapySessionCommand)`       | `void`       | public      | Recupera la sesión, invoca `cancelSession()` con el motivo indicado, persiste estado `Cancelled` y publica `TherapySessionCancelled`. |

**3. TherapyExecutionCommandServiceImpl (Command Service)**

Orquesta los casos de uso de alta frecuencia (ejecución en tiempo real). En este servicio se inyectan los domain services para procesar la lógica clínica antes de afectar al agregado.

| Atributo                | Tipo                          | Visibilidad | Descripción                                                                     |
|-------------------------|-------------------------------|-------------|---------------------------------------------------------------------------------|
| `sessionRepository`     | `TherapySessionRepository`    | private     | Puerto para acceder a la persistencia del agregado.                             |
| `motionAnalysisService` | `MotionAnalysisService`       | private     | Domain service que evalúa si el movimiento infringe límites de seguridad.       |
| `validationService`     | `RepetitionValidationService` | private     | Domain service que valida si el ángulo alcanzado califica como repetición útil. |
| `eventPublisher`        | `ApplicationEventPublisher`   | private     | Publicador de eventos de dominio en tiempo real.                                |

**Métodos principales:**

| Método                                   | Tipo Retorno | Visibilidad | Descripción                                                                                                                                                                     |
|------------------------------------------|--------------|-------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `handle(StartSerieCommand)`              | `void`       | public      | Busca la rutina activa, inicia la serie indicada y publica `SerieStarted`.                                                                                                      |
| `handle(RecordValidRepetitionCommand)`   | `void`       | public      | Recupera la sesión, evalúa movimiento con `MotionAnalysisService`, valida repetición con `RepetitionValidationService`, registra en el agregado y publica `RepetitionRecorded`. |
| `handle(RecordAnomalousMovementCommand)` | `void`       | public      | Registra la anomalía en la sesión y publica (`AnomalousMovementDetected` o `ExcessiveMovementAlertIssued`) para gatillar alertas físicas.                                       |

**4. TherapyQueryServiceImpl (Query Service)**

Encargado de resolver consultas para las pantallas del Frontend y el Edge App, accediendo a proyecciones o al modelo de lectura optimizado.

| Atributo                | Tipo                           | Visibilidad | Descripción                                           |
|-------------------------|--------------------------------|-------------|-------------------------------------------------------|
| `sessionReadRepository` | `TherapySessionReadRepository` | private     | Acceso optimizado para lectura de datos (Read Model). |

**Métodos principales:**

| Método                                 | Tipo Retorno         | Visibilidad | Descripción                                                                           |
|----------------------------------------|----------------------|-------------|---------------------------------------------------------------------------------------|
| `handle(GetSessionProgressQuery)`      | `SessionProgressDto` | public      | Recupera el estado de avance en vivo (serie actual, conteos) para la UI de ejecución. |
| `handle(GetSessionSummaryQuery)`       | `SessionSummaryDto`  | public      | Recupera la información consolidada tras el cierre de la sesión.                      |
| `handle(GetDailyTherapyScheduleQuery)` | `DailyScheduleDto`   | public      | Consulta la rutina proyectada para el paciente en una fecha específica.               |

**5. TherapyEventHandlers (Event Handlers)**

Componentes de la capa de aplicación que escuchan de forma asíncrona los eventos emitidos por el dominio para ejecutar efectos secundarios en la infraestructura, actualizar la UI o comunicarse con otros bounded contexts.

| Event Handler                        | Evento Escuchado                                            | Descripción                                                                                                         |
|--------------------------------------|-------------------------------------------------------------|---------------------------------------------------------------------------------------------------------------------|
| `IoTFeedbackEventHandler`            | `AnomalousMovementDetected`, `ExcessiveMovementAlertIssued` | Usa `IoTHardwareGateway` para enviar señal de vibración y alerta visual al dispositivo IoT del paciente.            |
| `InstructionalVideoEventHandler`     | `SerieStarted`                                              | Escucha el inicio de una serie y gatilla la reproducción del video instruccional en la app móvil.                   |
| `SessionProgressNotificationHandler` | `RepetitionRecorded`, `SerieAchieved`                       | Notifica vía WebSocket a la app del paciente y dashboard del fisioterapeuta cambios de progreso en tiempo real.     |
| `SessionClosureEventHandler`         | `TherapySessionCompleted`                                   | Escucha la finalización exitosa de la sesión y notifica asíncronamente al contexto de Planning.                     |
| `SessionCancellationEventHandler`    | `TherapySessionCancelled`                                   | Notifica a Planning la cancelación de la sesión y solicita la liberación del dispositivo IoT para su reutilización. |

**Notas adicionales de diseño arquitectónico:**

- **Publicación de eventos post-commit con `ApplicationEventPublisher`:** Los Domain Events se publican con el mecanismo nativo de Spring después del commit de la transacción. Es una decisión pragmática orientada a simplicidad operacional para el alcance actual del proyecto.
- **Idempotencia de telemetría:** Los commands originados por el Edge App (por ejemplo, `RecordValidRepetitionCommand`) incluyen un `edgeSequenceId` para evitar duplicidad de repeticiones ante reintentos de red.
- **Convención DTO vs Resource:** En Interface Layer se usa el sufijo `Resource` para contratos REST; en Application Layer se usa `Dto` para modelos internos de lectura retornados por Query Services y consumidos por el ACL Facade.

#### 4.2.6.4. Infrastructure Layer

En esta capa se ubican los componentes que acceden a servicios externos: base de datos PostgreSQL para la persistencia del agregado, gateway hacia el dispositivo IoT para feedback físico, publicador WebSocket para actualizaciones en tiempo real e integración con otros bounded contexts mediante ACL clients. Aquí se encuentran las implementaciones concretas de las interfaces definidas en la Domain Layer (Repositories) y en la Application Layer (Gateways, Publishers, ACL Clients).

**1. TherapySessionRepository (Repository Interface)**

Interfaz única de acceso a datos para el aggregate root `TherapySession`, implementada con Spring Data JPA sobre PostgreSQL. Maneja operaciones de escritura y lectura transaccional para Command Services y Query Services.

| Método                                                                          | Tipo Retorno               | Visibilidad | Descripción                                                                |
|---------------------------------------------------------------------------------|----------------------------|-------------|----------------------------------------------------------------------------|
| `findById(TherapySessionId id)`                                                 | `Optional<TherapySession>` | public      | Recupera la sesión completa con su rutina y series por identificador.      |
| `save(TherapySession session)`                                                  | `TherapySession`           | public      | Persiste o actualiza el estado completo del aggregate.                     |
| `findByPatientIdAndStatusIn(PatientId pId, List<SessionStatus> statuses)`       | `Optional<TherapySession>` | public      | Retorna la sesión en progreso (`Ready`, `InProgress`) para un paciente.    |
| `findAllByPatientId(PatientId patientId)`                                       | `List<TherapySession>`     | public      | Obtiene el historial completo de sesiones del paciente.                    |
| `findAllByTreatmentPlanId(TreatmentPlanId planId)`                              | `List<TherapySession>`     | public      | Obtiene sesiones asociadas a un plan para trazabilidad clínica.            |
| `findByPatientIdAndDate(PatientId pId, LocalDate date)`                         | `Optional<TherapySession>` | public      | Recupera la sesión programada del paciente para una fecha específica.      |
| `existsByPatientIdAndStatusIn(PatientId pId, List<SessionStatus> statuses)`     | `boolean`                  | public      | Invariante: verifica si existe una sesión activa para evitar duplicidades. |
| `countByTreatmentPlanIdAndStatus(TreatmentPlanId planId, SessionStatus status)` | `int`                      | public      | Cuenta sesiones completadas de un plan, consumido por el ACL Facade.       |

**2. IoTHardwareGateway (Infrastructure Port)**

Puerto de salida hacia el dispositivo IoT. Implementa la respuesta a eventos de dominio capturados por `IoTFeedbackEventHandler` mediante MQTT sobre broker Mosquitto.

| Método                                                       | Tipo Retorno | Visibilidad | Descripción                                                                    |
|--------------------------------------------------------------|--------------|-------------|--------------------------------------------------------------------------------|
| `triggerVibrationFeedback(DeviceId deviceId)`                | `void`       | public      | Publica un mensaje MQTT solicitando feedback de vibración al dispositivo.      |
| `triggerVisualAlert(DeviceId deviceId, AlertColor color)`    | `void`       | public      | Envía señal de alerta visual (por ejemplo, luz roja ante movimiento anómalo).  |
| `playInstructionalVideo(DeviceId deviceId, String videoUrl)` | `void`       | public      | Solicita al Edge App la reproducción del video instruccional de la serie.      |
| `releaseDevice(DeviceId deviceId)`                           | `void`       | public      | Libera el dispositivo al cancelar o finalizar la sesión para su reutilización. |

**3. SessionProgressWebSocketPublisher (WebSocket Publisher)**

Publicador en tiempo real hacia clientes conectados (app móvil del paciente y dashboard clínico del fisioterapeuta), sobre WebSocket/STOMP con Spring WebSocket.

| Método                                                                 | Tipo Retorno | Visibilidad | Descripción                                                                 |
|------------------------------------------------------------------------|--------------|-------------|-----------------------------------------------------------------------------|
| `broadcastProgressUpdate(UUID sessionId, SessionProgressDto progress)` | `void`       | public      | Emite el progreso actualizado al tópico STOMP de la sesión activa.          |
| `broadcastAnomalyAlert(UUID sessionId, MovementAlertType alertType)`   | `void`       | public      | Notifica al fisioterapeuta supervisor sobre una anomalía detectada.         |
| `broadcastSessionClosed(UUID sessionId)`                               | `void`       | public      | Notifica a los clientes suscritos que la sesión fue finalizada o cancelada. |

**4. PlanningContextClient (ACL Client)**

Cliente saliente hacia el bounded context de Planning. Permite comunicar cierre y cancelación de sesiones sin acoplamiento directo, vía HTTP/REST con tolerancia a fallos usando Resilience4j.

| Método                                                                        | Tipo Retorno                   | Visibilidad | Descripción                                                                       |
|-------------------------------------------------------------------------------|--------------------------------|-------------|-----------------------------------------------------------------------------------|
| `notifySessionCompleted(UUID treatmentPlanId, SessionSummaryDto summary)`     | `void`                         | public      | Informa a Planning que una sesión fue completada para actualizar avance del plan. |
| `notifySessionCancelled(UUID treatmentPlanId, UUID sessionId, String reason)` | `void`                         | public      | Informa la cancelación para que Planning ajuste cronograma y libere recursos.     |
| `requestRoutineForDate(UUID patientId, LocalDate date)`                       | `Optional<RoutineSnapshotDto>` | public      | Solicita a Planning la rutina asignada al paciente para una fecha específica.     |

**Notas de diseño de esta capa:**

- **Repositorio único de lectura y escritura:** Se mantiene un solo `TherapySessionRepository` para reducir complejidad y acelerar la implementación inicial.
- **Persistencia directa del agregado con anotaciones JPA:** `TherapySession`, `Routine` y `Serie` se anotan directamente con JPA, priorizando velocidad de desarrollo sobre separación estricta de mapeo.
- **Publicación de eventos post-commit con Spring:** Se usa `ApplicationEventPublisher` sin bus externo ni Outbox, aceptando como riesgo acotado la posible pérdida de evento ante caída del publicador justo después del commit.
- **MQTT para comunicación con IoT:** Se adopta MQTT/Mosquitto por baja latencia y eficiencia energética en dispositivos de borde.
- **ACL Client con circuit breaker:** Resilience4j permite que el flujo clínico principal continúe disponible aunque Planning esté temporalmente no disponible.

#### 4.2.6.5. Bounded Context Software Architecture Component Level Diagrams

El diagrama de componentes (C4 Nivel 3) describe la organización interna del **Therapy Service** (Java 25 / Spring Boot 4). Dentro del *Container Boundary* se distinguen cinco bloques: **Interface Layer** (controladores REST), **Application Layer** (CQRS con command/query services y event handlers), **Planning Context ACL** (adaptador de salida), **Domain Layer** (aggregate, entidades, value objects y domain services) e **Infrastructure Layer** (repositorio JPA, gateway MQTT, publicador WebSocket/STOMP y `ApplicationEventPublisher`).

Los clientes externos acceden por HTTPS: apps móviles nativas (Android/iOS), PWA clínica (Angular) e IoT Edge App (C++/ESP-IDF). La PWA mantiene además un canal WebSocket/STOMP para progreso y alertas en tiempo real, mientras el Edge App reporta telemetría preprocesada y recibe comandos de feedback por MQTT.

Fuera del *Container Boundary* quedan tres dependencias: broker **Mosquitto** (MQTT/TLS), bounded context **Planning** consumido vía ACL REST con Resilience4j, y **Therapy Relational SQL Database** (Azure PostgreSQL por JDBC/SSL). El flujo de dependencias mantiene la estructura táctica: `Interface -> Application -> (Domain + Infrastructure)`.

<div style="text-align: center;">
  <img src="assets/diagrams/software-architecture/components/out/therapy-components-diagram.png" alt="uFlex — Therapy Bounded Context Component Diagram" style="max-width: 100%; height: auto;">
</div>

*Figura 4.2.6.5. Diagrama de componentes (C4 Nivel 3) del Bounded Context Therapy.*

#### 4.2.6.6. Bounded Context Software Architecture Code Level Diagrams

##### 4.2.6.6.1. Bounded Context Domain Layer Class Diagrams

El diagrama de clases del Domain Layer del BC Therapy modela los conceptos centrales de la ejecución terapéutica, sin incluir application ni infrastructure. `domain.model.aggregates` contiene al Aggregate Root `TherapySession`; `domain.model.entities` incorpora `Routine` y `Serie`; `domain.model.valueobjects` agrupa IDs tipados, estados (`SessionStatus`, `RoutineStatus`, `SerieStatus`, `MovementAlertType`) y objetos inmutables como `AngleThreshold`, `PainLevel`, `CompletedRepetition` e `IoTSensorSnapshot`; `domain.model.events` encapsula los Domain Events de ciclo de vida y ejecución; y `domain.exceptions` reúne las excepciones que protegen invariantes.

En la estructura del modelo, `TherapySession` compone a `Routine` (`1..1`), `Routine` agrega `Serie` (`1..*`) y cada `Serie` contiene `CompletedRepetition` como hechos inmutables validados por el Edge App. Esto preserva trazabilidad clínica desde sesión hasta repetición.

Los Domain Services (`MotionAnalysisService`, `RepetitionValidationService`) se modelan como interfaces en `domain.services`, siguiendo inversión de dependencias: el dominio define contratos y las implementaciones se resuelven en capas superiores. Ambos usan `AngleThreshold` como referencia clínica común. En la notación, líneas continuas indican composición/agregación, líneas punteadas dependencias semánticas (eventos y excepciones) y la paleta distingue aggregate root, entities, value objects, domain events, domain services y domain exceptions.

<div style="text-align: center;">
  <img src="assets/diagrams/uml/class/out/therapy-domain-layer-class-diagram.png" alt="uFlex — Therapy Bounded Context Domain Class Diagram" style="max-width: 100%; height: auto;">
</div>

*Figura 4.2.6.6.1. Diagrama de clases del dominio del Bounded Context Therapy.*

##### 4.2.6.6.2. Bounded Context Database Design Diagram

El diagrama de base de datos del BC Therapy modela la persistencia relacional del agregado `TherapySession` y su jerarquía clínica (`Routine`, `Serie`, `CompletedRepetition`) sobre Azure Database for PostgreSQL. El esquema se organiza en cinco tablas operativas (`therapy_sessions`, `routines`, `series`, `completed_repetitions`, `movement_alerts`) y cuatro catálogos de estado (`session_statuses`, `routine_statuses`, `serie_statuses`, `movement_alert_types`).

`therapy_sessions` es la raíz persistente del agregado e incluye referencias lógicas a otros bounded contexts (`patient_id`, `treatment_plan_id`, `iot_device_id`), junto con el snapshot de sensores embebido como columnas y los timestamps del ciclo de vida. `routines` mantiene relación 1:1 con la sesión, mientras que `series` se relaciona 1:N con `routines`, conservando orden clínico (`sequence_order`), parámetros terapéuticos (`target_repetitions`, `min_angle`, `max_angle`) y progreso (`current_repetitions`). `completed_repetitions` registra hechos inmutables de ejecución (sin operaciones de update) y `movement_alerts` almacena alertas clínicas para auditoría y trazabilidad.

En integridad y rendimiento, el diseño incorpora reglas alineadas al dominio: índice único por paciente y fecha para evitar sesiones duplicadas por día, índice parcial por `iot_device_id` para identificar dispositivos en uso (`READY`, `IN_PROGRESS`), y constraints para proteger invariantes de serie (`current_repetitions <= target_repetitions`, `min_angle < max_angle`). Se mantiene, además, la estrategia de autonomía entre contexts: las referencias externas son lógicas (sin foreign keys duras hacia IAM, Planning o Device).

<div style="text-align: center;">
  <img src="assets/diagrams/database/erd/out/therapy-database-design-diagram.png" alt="uFlex — Therapy Bounded Context Database ER Diagram" style="max-width: 100%; height: auto;">
</div>

*Figura 4.2.6.6.2. Diagrama entidad-relación del Bounded Context Therapy.*


<hr class="page-break">
