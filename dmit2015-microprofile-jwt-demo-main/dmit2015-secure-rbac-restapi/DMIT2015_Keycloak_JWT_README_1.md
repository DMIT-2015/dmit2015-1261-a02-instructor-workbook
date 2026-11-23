# README 1 — Configure Keycloak and Obtain JWT Access Tokens

**DMIT2015 — Lesson 29: Secure RBAC REST API**

## Learning Objectives

By the end of this lesson, you should be able to:

1. Explain the difference between authentication and authorization.
2. Start Keycloak using Podman.
3. Identify a Keycloak realm, client, user, and role.
4. Configure an OpenID Connect client for JWT authentication.
5. Configure the `upn` and `groups` claims.
6. Request an access token using IntelliJ HTTP Client.
7. Inspect a JWT and explain its claims.
8. Use an access token to retrieve authenticated user information.


---

## Part 1 — Understand Authentication and Authorization

In previous lessons, you developed REST APIs that support CRUD operations.

Your Student REST API provides the following endpoints:

| HTTP Method | Endpoint | Operation |
|---|---|---|
| GET | `/restapi/students` | Retrieve all Students |
| GET | `/restapi/students/{id}` | Retrieve one Student |
| POST | `/restapi/students` | Create a Student |
| PUT | `/restapi/students/{id}` | Update a Student |
| DELETE | `/restapi/students/{id}` | Delete a Student |

Currently, these endpoints do not have role-based security annotations.

In this lesson, you will begin securing the Student REST API so that only authorized users can perform certain operations.

### Authentication versus Authorization

**Authentication** answers the question:

*Who are you?*

For example, Keycloak verifies a user's username and password.

**Authorization** answers the question:

*What are you allowed to do?*


Authentication identifies the user. Authorization determines which operations that user can perform.

---

## Part 1 — Start Keycloak

### Step 1 — Start the Keycloak Container

If your existing Keycloak container is named `dmit2015-keycloak`, start it using:

```bash
podman start dmit2015-keycloak
```

Verify that it is running:

```bash
podman ps
```

If your container has a different name, use that name instead.

### Step 2 — Open the Keycloak Administration Console

Open your browser and navigate to:

http://localhost:8180

Sign in using your Keycloak administrator credentials.
Username: user2015, password:Password2015

Keycloak is an identity and access management server. It authenticates users and issues security tokens that applications can use to verify user identity and access permissions.

---

## Part 3 — Select the Keycloak Realm

In the Keycloak Administration Console, select the realm configured for this course.

The expected classroom realm is:

```text
dmit2015-realm
```


### What is a Realm?

A realm manages authentication and authorization information, including:

- Users
- Roles
- Clients
- Authentication settings
- Token issuance

---

## Part 4 — Create a Keycloak Client

A Keycloak client represents an application or service that uses Keycloak for authentication.

For this lesson, configure a client that can issue JWT access tokens for testing the Student REST API.

### Step 1 — Create the Client

In the Keycloak Administration Console:

1. Select **Clients**.
2. Click **Create client**.
3. Configure the following values:

```text
Client type: OpenID Connect
Client ID: dmit2015-jwt-client
```

4. Click **Next**.

### Step 2 — Configure Client Capabilities

Enable the following options:

- **Client authentication**
- **Client auhorization**        
- **Direct access grants**

Save the client.

Direct access grants allow you to obtain an access token by submitting a username and password to Keycloak.



### Step 3 — Locate the Client Secret

Navigate to:

**Clients → dmit2015-jwt-client → Credentials**

Locate the **Client secret**.

You will use this value when configuring the IntelliJ HTTP Client environment.


---

## Part 5 — Configure JWT Claims

MicroProfile JWT uses information contained in a JWT to identify the authenticated user and determine their roles.

For this lesson, configure the following claims:

- `upn`
- `groups`

### Step 1 — Open the Client Scopes

1. In the Keycloak Administration Console, click **Clients**.
2. Select `dmit2015-jwt-client`.
3. Open the **Client scopes** tab.
4. Select the dedicated client scope:

```text
dmit2015-jwt-client-dedicated
```

5. Open the **Mappers** tab.

### Step 2 — Configure the `upn` Mapper

The `upn` claim represents the **User Principal Name**.

It identifies the authenticated user.

To configure the mapper:

1. Click **Add mapper**.
2. Choose **By configuration** or **Add predefined mapper**, depending on your Keycloak version.
3. Select or create a mapper for `upn`.
4. Configure the mapper to use the authenticated user's username.
5. Ensure the claim is included in the access token.
6. Save the mapper.

### Step 3 — Configure the `groups` Mapper

The `groups` claim contains the security roles or groups assigned to the authenticated user.

To configure the mapper:

1. In the same **Mappers** tab, click **Add mapper**.
2. Choose **By configuration** or **Add predefined mapper**.
3. Select a mapper that provides the required roles or groups.
4. Configure the token claim name as `groups`.
5. Ensure the claim is included in the access token.
6. Save the mapper.

### Step 4 — Check the MicroProfile JWT Client Scope

Return to:

**Clients → dmit2015-jwt-client → Client scopes**

Locate the `microprofile-jwt` client scope.

If this scope is available in your Keycloak configuration, ensure it is assigned as a **Default** client scope.

The required result is an access token that contains the `upn` and `groups` claims.

You will verify these claims after obtaining an access token.

---

## Part 6 — Create an IntelliJ HTTP Request for Keycloak

You will now create an HTTP request to obtain a JWT access token.

### Step 1 — Open the Student REST API Project

Open the following project in IntelliJ IDEA:

```text
dmit2015-secure-rbac-restapi
```

Locate the existing directory:

```text
http_request/
```

Create a new file:

```text
KeycloakHttpRequest.http
```

### Step 2 — Add the Token Request

Add the following HTTP request:

```http
### Obtain a JWT access token from Keycloak
POST {{keycloakServer}}/realms/{{realm}}/protocol/openid-connect/token
Content-Type: application/x-www-form-urlencoded

grant_type=password&client_id={{jwtClientId}}&client_secret={{jwtClientSecret}}&username={{user1Name}}&password={{user1Password}}&scope=openid
```

### Understand the Request

| Field | Purpose |
|---|---|
| `grant_type=password` | Requests a token using a username and password |
| `client_id` | Identifies the Keycloak client |
| `client_secret` | Authenticates the confidential client |
| `username` | Specifies the user requesting the token |
| `password` | Provides the user's password |
| `scope=openid` | Requests the OpenID Connect scope needed for UserInfo |

The double curly braces, such as `{{realm}}`, represent IntelliJ HTTP Client environment variables.

---

## Part 7 — Create a Private HTTP Environment

To avoid placing credentials directly in your HTTP request file, create a private HTTP environment.

### Step 1 — Create the Environment File

In the `http_request` directory, create:

```text
http-client.private.env.json
```

### Step 2 — Add the Environment Variables

Add the following JSON:

```json
{
  "dev": {
    "keycloakServer": "http://localhost:8180",
    "realm": "dmit2015-realm",
    "jwtClientId": "dmit2015-jwt-client",
    "jwtClientSecret": "REPLACE_WITH_CLIENT_SECRET",
    "user1Name": "REPLACE_WITH_USERNAME",
    "user1Password": "REPLACE_WITH_PASSWORD", //colsen
    "user1AccessToken": "PASTE_ACCESS_TOKEN_HERE" //Password2015
  }
}
```

Replace the placeholders with the values from your own Keycloak environment.

To obtain the client secret, navigate to:

**Clients → dmit2015-jwt-client → Credentials**

Use the username and password of a Keycloak test user who has been assigned the appropriate security role.



### Step 3 — Run the Token Request

1. Open `KeycloakHttpRequest.http`.
2. Select the `dev` environment.
3. Click the run icon beside the POST request.
4. Examine the HTTP response.

A successful request should return:

```http
HTTP/1.1 200 OK
Content-Type: application/json
```

Example response:

```json
{
  "access_token": "eyJ...",
  "expires_in": 1800,
  "refresh_token": "eyJ...",
  "token_type": "Bearer",
  "scope": "openid email profile"
}
```

This response is illustrative. Token values, expiration times, and available scopes may differ.

### Step 4 — Save the Access Token

Copy the value of `access_token` from the POST response.

Update the following property in your private environment file:

```json
"user1AccessToken": "PASTE_NEW_ACCESS_TOKEN_HERE"
```

Save the file.

**Important:** Each successful token request may generate a different access token. If you request a new token, update `user1AccessToken` before running subsequent requests.

---

## Part 8 — Understand the JWT

A JSON Web Token (JWT) is a signed token that contains information about an authenticated user.

A JWT consists of three parts:

```text
HEADER.PAYLOAD.SIGNATURE
```

The three parts are separated by periods.

### 1. Header

The header identifies the token type and signing algorithm.

Example:

```json
{
  "alg": "RS256",
  "typ": "JWT"
}
```

### 2. Payload

The payload contains claims about the user and token.

Example:

```json
{
  "iss": "http://localhost:8180/realms/dmit2015-realm",
  "upn": "student01",
  "groups": ["Sales"],
  "scope": "openid email profile",
  "iat": 1790000000,
  "exp": 1790003600
}
```

This is an example payload, not an actual token from Keycloak.

### 3. Signature

The signature allows the REST API to verify that the JWT was issued by a trusted authority and has not been modified.

### Important JWT Claims

| Claim | Meaning |
|---|---|
| `iss` | Token issuer |
| `sub` | Subject identifier |
| `upn` | User principal name |
| `groups` | User's roles or groups |
| `scope` | Scopes granted to the token |
| `iat` | Issued-at time |
| `exp` | Expiration time |

### Inspect the JWT

You can examine the structure of a JWT using a local JWT decoder.

The website [jwt.io](https://jwt.io) can also be used to learn about JWT structure, but do not paste live access tokens into external websites.

When examining the payload, locate the following claims:

```json
{
  "upn": "student01",
  "groups": ["Sales"]
}
```

These claims will be important when configuring role-based access control in the Student REST API.

**Remember:** JWT payloads are encoded, not encrypted. Do not store passwords, client secrets, or other sensitive credentials inside JWT claims.

---

## Part 9 — Test the UserInfo Endpoint

Keycloak provides a UserInfo endpoint that returns information about the authenticated user.

### Step 1 — Add the GET Request

In `KeycloakHttpRequest.http`, add:

```http
### Retrieve information about the authenticated user
GET {{keycloakServer}}/realms/{{realm}}/protocol/openid-connect/userinfo
Authorization: Bearer {{user1AccessToken}}
Accept: application/json
```
Add the access token obtained earlier to your private HTTP environment:
    "user1AccessToken": "PASTE_ACCESS_TOKEN_HERE"

Your HTTP request file should now contain both the POST token request and the GET UserInfo request.


### Step 2 — Run the UserInfo Request

Select the `dev` environment and run the GET request.

A successful response returns:

```http
HTTP/1.1 200 OK
Content-Type: application/json
```

Example response:

```json
{
  "sub": "example-user-id",
  "upn": "student01",
  "email_verified": false,
  "name": "Example Student",
  "groups": [
    "Sales"
  ],
  "preferred_username": "student01",
  "given_name": "Example",
  "family_name": "Student",
  "email": "student01@example.com"
}
```

The actual response fields depend on your Keycloak user and client scope configuration.

### Understand the Authorization Header

The request contains:

```http
Authorization: Bearer {{user1AccessToken}}
```

This header sends the access token to Keycloak.

The server validates the token before returning the user's information.

---

## Part 10 — Connect JWT Authentication to the Student REST API

Return to your project:

```text
dmit2015-secure-rbac-restapi
```

Open:

```text
src/main/java/dmit2015/resource/StudentDtoResource.java
```

The resource class contains the following annotations:

```java
@ApplicationScoped
@Path("students")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class StudentDtoResource {

    // Existing Student CRUD methods

}
```

Your existing Student REST API already supports CRUD operations.

The next step is to restrict access to those operations using JWT authentication and role-based authorization.

In README 2, you will:

1. Configure MicroProfile JWT.
2. Configure JWT signature verification using Keycloak.
3. Add `@LoginConfig` to `JaxRsApplication.java`.
4. Enable RESTEasy role-based security.
5. Add `@RolesAllowed` to the Student CRUD methods.

You will keep the existing Student DTO, repository, validation, and persistence logic.

---


## Summary

In this lesson, you configured Keycloak to issue JWT access tokens, created HTTP requests using IntelliJ HTTP Client, and verified authenticated user information through the Keycloak UserInfo endpoint.

You also examined the `upn`, `groups`, and `scope` claims and learned how these claims support authentication and role-based authorization.

**Next: README 2 — Secure the Student REST API Using MicroProfile JWT and `@RolesAllowed`.**