# README 3 — Test the Secured Student REST API Using JWT Tokens

**DMIT2015 — Lesson 29: Secure RBAC REST API**

## Part 1 — Obtain JWT Access Tokens

Ensure Keycloak and WildFly are running.

Use the existing Keycloak users with the following roles:

| User | Role | Token Variable |
| --- | --- | --- |
| User 1 | Sales | `user1AccessToken` | colsen
| User 2 | Shipping | `user2AccessToken` |
| User 3 | Administration | `user3AccessToken` | jwhalen

### Step 1 — Configure User Credentials

Open `http_request/http-client.private.env.json`.

Keep the Keycloak settings from README 1 and add the credentials for your existing users.

```json
{
  "dev": {
    "keycloakServer": "http://localhost:8180",
    "realm": " dmitt2015-realm",
    "jwtClientId": "dmit2015-jwt-client",
    "jwtClientSecret": "YOUR_CLIENT_SECRET",

    "user1Name": "colsen",
    "user1Password": "Password2015",
    "user1AccessToken": "PASTE_SALES_TOKEN",

    "user2Name": "jwhalen",
    "user2Password": "Password2015",
    "user2AccessToken": "PASTE_SHIPPING_TOKEN",

    "user3Name": "YOUR_ADMIN_USERNAME",
    "user3Password": "YOUR_ADMIN_PASSWORD",
    "user3AccessToken": "PASTE_ADMINISTRATION_TOKEN"
  }
}
```

Replace the placeholders with your existing Keycloak credentials. Use the realm name configured in your own Keycloak instance; the example above preserves the leading space in the current project's realm name.

### Step 2 — Generate Tokens for Each User

Open `http_request/KeycloakHttpRequest.http`.

Add the following requests:

```http
### Generate Sales JWT
POST {{keycloakServer}}/realms/{{realm}}/protocol/openid-connect/token
Content-Type: application/x-www-form-urlencoded

grant_type=password&client_id={{jwtClientId}}&client_secret={{jwtClientSecret}}&username={{user1Name}}&password={{user1Password}}&scope=openid


### Generate Shipping JWT
POST {{keycloakServer}}/realms/{{realm}}/protocol/openid-connect/token
Content-Type: application/x-www-form-urlencoded

grant_type=password&client_id={{jwtClientId}}&client_secret={{jwtClientSecret}}&username={{user2Name}}&password={{user2Password}}&scope=openid


### Generate Administration JWT
POST {{keycloakServer}}/realms/{{realm}}/protocol/openid-connect/token
Content-Type: application/x-www-form-urlencoded

grant_type=password&client_id={{jwtClientId}}&client_secret={{jwtClientSecret}}&username={{user3Name}}&password={{user3Password}}&scope=openid
```

Run each POST request using **Run with → dev**.

After each successful request, copy the `access_token` from the response into its corresponding property in `http-client.private.env.json`:

- Sales → `user1AccessToken`
- Shipping → `user2AccessToken`
- Administration → `user3AccessToken`

You only need to obtain a new token when the existing token expires or when you need a token reflecting updated roles.

## Part 2 — Add JWT Authentication to Student HTTP Requests

Open `http_request/StudentDtoHttpRequest.http`.

Your existing GET request is:

```http
GET http://localhost:8090/restapi/students
```

Add the `Authorization` header:

```http
### GET all Students using Sales JWT
GET http://localhost:8090/restapi/students
Authorization: Bearer {{user1AccessToken}}
```

The new line is:

```http
Authorization: Bearer {{user1AccessToken}}
```

Add the appropriate `Authorization` header to your existing GET, POST, PUT, and DELETE requests.

Keep the existing request URLs, JSON bodies, and `Content-Type: application/json` headers for POST and PUT.

## Part 3 — Test Role-Based Access Control

Your Student REST API uses the authorization rules configured in README 2.

| HTTP Method | Allowed Roles |
| --- | --- |
| GET, POST, PUT | Sales, Shipping |
| DELETE | Administration |

### Test 1 — GET Students as Sales

```http
### GET Students as Sales
GET http://localhost:8090/restapi/students
Authorization: Bearer {{user1AccessToken}}
```

**Expected result: `200 OK`**

### Test 2 — GET Students as Shipping

```http
### GET Students as Shipping
GET http://localhost:8090/restapi/students
Authorization: Bearer {{user2AccessToken}}
```

**Expected result: `200 OK`**

### Test 3 — DELETE Student as Sales

```http
### DELETE Student as Sales
DELETE http://localhost:8090/restapi/students/11
Authorization: Bearer {{user1AccessToken}}
```

**Expected result: `403 Forbidden`**

### Test 4 — DELETE Student as Administration

```http
### DELETE Student as Administration
DELETE http://localhost:8090/restapi/students/11
Authorization: Bearer {{user3AccessToken}}
```

**Expected result: `204 No Content`**

Replace `11` with the ID of an existing disposable Student record. Use the same ID for Tests 3 and 4.

### Test 5 — GET Students Without Authentication

```http
### GET Students without JWT
GET http://localhost:8090/restapi/students
```

**Expected result:** Access denied, typically `401 Unauthorized`.

## Part 4 — Verify Your Results

| Test | Expected Result | Actual Result |
| --- | --- | --- |
| GET — Sales | 200 OK | |
| GET — Shipping | 200 OK | |
| DELETE — Sales | 403 Forbidden | |
| DELETE — Administration | 204 No Content | |
| GET — No token | Access denied | |

### IntelliJ HTTP Client Reminder

When running requests, click the green Run icon and select **Run with → dev**.

If IntelliJ displays:

```text
Invalid request because of unsubstituted variable 'user1AccessToken'
```

or:

```text
Environment is not selected.
Cannot resolve variable 'user1AccessToken'
```

Verify that the `dev` environment is selected and that the corresponding token has been saved in `http-client.private.env.json`.

## Summary

In this exercise, you obtained JWT access tokens for multiple Keycloak users, added bearer-token authentication to your existing Student REST API requests, and verified role-based access control.

**Lesson 29 Complete — Secure RBAC REST API.**
