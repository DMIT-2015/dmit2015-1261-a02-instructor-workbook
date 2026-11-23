# Lesson 30 — Secure Multi-Tenant REST API

## README 3 — Test Multi-Tenant Security

### Learning Objectives

By the end of this lesson, you should be able to:

- Test a JWT-secured TodoItem REST API using IntelliJ HTTP Client.
- Verify role-based permissions for Sales, Shipping, and Administration.
- Confirm that users can access only their own TodoItems.
- Test cross-user access restrictions for GET, PUT, and DELETE.
- Interpret successful and forbidden HTTP responses.

## 1. Start the Required Services

Before testing, make sure the following services are running:

| Service | Address |
|---|---|
| Keycloak | `http://localhost:8180` |
| Lesson 30 TodoItem REST API | `http://localhost:8091/restapi/todos` |

In IntelliJ IDEA, open:

`dmit2015-secure-multitenant-restapi`

Start the REST API:

```bash
mvn wildfly:dev
```

Confirm that the application starts without errors.

**Note:** Opening `/restapi/todos` directly in a browser may display `Access forbidden: role not allowed`. This is expected because the browser request does not include a JWT access token.

We will use IntelliJ HTTP Client to send authenticated requests.

## 2. Prepare Keycloak Users

Use the Keycloak realm and users configured in Lesson 29.

You need three users with the following roles:

| User | Required Role | Purpose |
|---|---|---|
| `colsen` | Sales | Test ownership of the initial TodoItems |
| `jpatel` | Shipping | Test access to another user's records |
| `jwhalen`  | Administration | Test read-only access to all records |

Use your actual Keycloak usernames and passwords.

The three initial TodoItems created by `TodoItemInitializer.java` belong to `colsen`, provided they were inserted into an empty database after the ownership field was added.

## 3. Obtain JWT Access Tokens

As in Lesson 29, obtain an access token for each Keycloak user.

Use the existing Keycloak token request from Lesson 29, updating the environment variables as necessary.

For example:

```http
POST {{keycloakServer}}/realms/{{realm}}/protocol/openid-connect/token
Content-Type: application/x-www-form-urlencoded

grant_type=password&client_id={{jwtClientId}}&client_secret={{jwtClientSecret}}&username={{user1Name}}&password={{user1Password}}&scope=openid
```

Run the request using **Run with → dev**.

Copy the `access_token` value from the response.

Repeat the request for your Shipping and Administration users.

### Save the tokens

In the HTTP Client's `http-client.private.env.json` file, store the tokens under separate variables:

```json
{
  "dev": {
    "user1AccessToken": "PASTE_SALES_ACCESS_TOKEN",
    "user2AccessToken": "PASTE_SHIPPING_ACCESS_TOKEN",
    "user3AccessToken": "PASTE_ADMINISTRATION_ACCESS_TOKEN"
  }
}
```

Add these entries to your existing `dev` environment rather than replacing its other settings.

Keep the private environment file out of version control.

**Important:** Select **Run with → dev** for every HTTP request. Otherwise, IntelliJ may report an unsubstituted variable such as `user1AccessToken`.

Access tokens expire. Obtain new tokens if authenticated requests begin failing because a token has expired.

## 4. Open the TodoItem HTTP Request File

In the Lesson 30 project, open:

`http_request/TodoItemDtoHttpRequest.http`

This file already contains GET, POST, PUT, and DELETE requests for the TodoItem REST API.

You will add an `Authorization` header to the existing requests.

For example:

```http
GET http://localhost:8091/restapi/todos
Authorization: Bearer {{user1AccessToken}}
```

The token determines which authenticated user is making the request.

For POST and PUT, retain the existing `Content-Type: application/json` header.

## 5. Test Sales — GET All

Use the Sales token belonging to `colsen`.

```http
### Sales: Get own TodoItems
GET http://localhost:8091/restapi/todos
Authorization: Bearer {{user1AccessToken}}
```

Run with **dev**.

**Expected result:** `200 OK`

If the initializer created the three sample TodoItems for `colsen`, the response should include those records.

Check that the returned records have:

```json
"username": "colsen"
```

The exact IDs and version values depend on your database.

## 6. Test Shipping — GET All

Run the same GET request using the Shipping token:

```http
### Shipping: Get own TodoItems
GET http://localhost:8091/restapi/todos
Authorization: Bearer {{user2AccessToken}}
```

**Expected result:** `200 OK`

The response should contain only TodoItems owned by the Shipping user.

If that user has not created any TodoItems, the response should be:

```json
[]
```

The Shipping user must not see the initial records owned by `colsen`.

This verifies that `findAllByUsername()` filters records using the authenticated username.

## 7. Test POST — Create a TodoItem

Use the Shipping token to create a new TodoItem:

```http
### Shipping: Create own TodoItem
POST http://localhost:8091/restapi/todos
Authorization: Bearer {{user2AccessToken}}
Content-Type: application/json

{
  "name": "Test multi-tenant ownership",
  "complete": false
}
```

**Expected result:** `201 Created`

The API should assign the Shipping username automatically from the JWT `upn` claim.

The POST request does not need to supply a username.

Run the Shipping GET-all request again.

The newly created TodoItem should now appear in the Shipping user's results.

Run the Sales GET-all request and confirm that the Shipping user's new TodoItem does not appear.

### Verify server-assigned ownership

Optionally, submit a POST request containing an incorrect username:

```http
### Shipping: Attempt to supply another owner
POST http://localhost:8091/restapi/todos
Authorization: Bearer {{user2AccessToken}}
Content-Type: application/json

{
  "name": "Verify server-assigned owner",
  "complete": false,
  "username": "colsen"
}
```

**Expected result:** `201 Created`, with the stored owner set to the authenticated Shipping user, not `colsen`.

Retrieve the new record to verify its actual owner.

## 8. Test Cross-User GET by ID

First, use the Sales token to retrieve the initial TodoItems and identify an existing ID owned by `colsen`.

Then use the Shipping token to request that ID.

For example, if TodoItem ID `1` belongs to `colsen`:

```http
### Shipping: Attempt to read Sales TodoItem
GET http://localhost:8091/restapi/todos/1
Authorization: Bearer {{user2AccessToken}}
```

**Expected result:** `403 Forbidden`

The ownership check should prevent the Shipping user from reading another user's record.

Now repeat the request with the Sales token:

```http
### Sales: Read own TodoItem
GET http://localhost:8091/restapi/todos/1
Authorization: Bearer {{user1AccessToken}}
```

**Expected result:** `200 OK`

Use an ID confirmed to exist in your database. Do not assume that ID `1` is always available.

## 9. Test Cross-User PUT

Select an existing TodoItem owned by `colsen`.

Retrieve it first to confirm its ID and current version.

Then attempt to update it using the Shipping token:

```http
### Shipping: Attempt to update Sales TodoItem
PUT http://localhost:8091/restapi/todos/1
Authorization: Bearer {{user2AccessToken}}
Content-Type: application/json

{
  "id": 1,
  "name": "Unauthorized update attempt",
  "complete": true,
  "version": 0
}
```

Replace the ID and version with the values of an actual TodoItem owned by `colsen`.

**Expected result:** `403 Forbidden`

The Shipping user should not be able to modify another user's record.

Retrieve the TodoItem again using the Sales token and confirm that its values have not changed.

### Test an authorized update

Repeat the PUT request using the Sales token and the current version.

**Expected result:** `200 OK`

The TodoItem should be updated successfully.

If you receive `409 Conflict`, retrieve the record again and use its current version.

## 10. Test Cross-User DELETE

Use the Shipping token to attempt to delete an existing TodoItem owned by `colsen`.

```http
### Shipping: Attempt to delete Sales TodoItem
DELETE http://localhost:8091/restapi/todos/1
Authorization: Bearer {{user2AccessToken}}
```

**Expected result:** `403 Forbidden`

The TodoItem should remain in the database.

### Test an authorized deletion

Create a disposable TodoItem using the Shipping token.

Retrieve the newly created record to identify its ID.

Then delete that record using the same Shipping token:

```http
### Shipping: Delete own TodoItem
DELETE http://localhost:8091/restapi/todos/REPLACE_WITH_ID
Authorization: Bearer {{user2AccessToken}}
```

Replace `REPLACE_WITH_ID` with the actual ID.

**Expected result:** `204 No Content`

Do not delete the initial sample records unless you intend to recreate them.

## 11. Test Administration — Read All Records

Use the Administration token:

```http
### Administration: Get all TodoItems
GET http://localhost:8091/restapi/todos
Authorization: Bearer {{user3AccessToken}}
```

**Expected result:** `200 OK`

The response should contain TodoItems belonging to multiple users.

Administration users are not restricted to their own records when reading.

Also test GET by ID for a record owned by Sales or Shipping:

```http
### Administration: Read another user's TodoItem
GET http://localhost:8091/restapi/todos/1
Authorization: Bearer {{user3AccessToken}}
```

**Expected result:** `200 OK`, provided the record exists.

## 12. Test Administration — Denied Modifications

Administration is read-only in Lesson 30.

Test a POST request:

```http
### Administration: Attempt to create TodoItem
POST http://localhost:8091/restapi/todos
Authorization: Bearer {{user3AccessToken}}
Content-Type: application/json

{
  "name": "Administration create attempt",
  "complete": false
}
```

**Expected result:** `403 Forbidden`

Also attempt PUT and DELETE using the Administration token.

Both should return `403 Forbidden`.

Administration can read all TodoItems but cannot create, update, or delete them.

## 13. Test a Request Without a JWT

Remove the Authorization header from a GET request:

```http
### Unauthenticated request
GET http://localhost:8091/restapi/todos
```

**Expected result:** Access denied.

A secured API commonly returns `401 Unauthorized` when authentication is missing, although the precise response can depend on the WildFly security configuration.

The important requirement is that the request must not return protected TodoItem data.

In **Lesson 31 — Secure Faces REST Client**, you will build a Jakarta Faces application that authenticates users and sends JWT tokens when calling secured REST APIs.