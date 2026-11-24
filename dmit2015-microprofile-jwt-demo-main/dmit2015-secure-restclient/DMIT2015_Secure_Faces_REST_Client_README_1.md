# Lesson 31 — Secure Faces REST Client with JWT

## README 1 — Introduction and Project Setup

### Learning Objectives

By the end of this lesson, you should be able to:

- Explain the authentication workflow between Jakarta Faces, Keycloak, and secured REST APIs.
- Authenticate users using Keycloak.
- Store JWT access tokens in the user's session.
- Automatically include JWT access tokens in REST API requests.
- Protect Jakarta Faces pages from unauthenticated users.
- Handle authentication and authorization failures.
- Implement logout and clear authenticated session state.

## 1. Review Previous Lessons

In Lessons 29 and 30, we implemented security in our backend REST APIs.

### Lesson 29 — Secure RBAC REST API

We secured the Student REST API using:

- Keycloak authentication.
- JWT access tokens.
- Role-based access control (RBAC).
- `@RolesAllowed`.
- Permitted and denied REST API operations.

### Lesson 30 — Secure Multi-Tenant REST API

We secured the TodoItem REST API using:

- JWT `upn` claims.
- Record ownership.
- Username-filtered repository queries.
- Ownership checks for GET, PUT, and DELETE operations.
- Administration read-only access to all records.

In both lessons, we used IntelliJ HTTP Client to manually obtain and send JWT access tokens.

In Lesson 31, we will automate this process through a Jakarta Faces application.

## 2. Introduction to Secure REST Clients

A secured REST API requires clients to provide valid authentication credentials when accessing protected resources.

Previously, we manually included a JWT in an HTTP request:

```http
GET http://localhost:8091/restapi/todos
Authorization: Bearer {{user1AccessToken}}
```

However, users of a web application should not need to manually generate, copy, or paste JWT access tokens.

Instead, the Jakarta Faces application should:

1. Allow the user to enter a username and password.
2. Authenticate the user with Keycloak.
3. Receive a JWT access token.
4. Store the token in the user's session.
5. Automatically include the token in secured REST requests.
6. Handle successful and unsuccessful responses.
7. Clear authentication information when the user logs out.

## 3. Application Architecture

Our application consists of four components:

| Component | Responsibility |
|---|---|
| Jakarta Faces REST Client | Provides the user interface and manages the authenticated session |
| Keycloak | Authenticates users and issues JWT access tokens |
| Secure RBAC REST API | Enforces role-based access to Student records |
| Secure Multi-Tenant REST API | Enforces role-based access and TodoItem ownership |

The authentication and REST request workflow is:

```text
                  User
                    |
                    v
           Jakarta Faces Client
                    |
             Login credentials
                    |
                    v
                 Keycloak
                    |
              JWT access token
                    |
                    v
           Jakarta Faces Client
              (Login Session)
                    |
          Authorization: Bearer JWT
                    |
          +---------+---------+
          |                   |
          v                   v
     Secure RBAC        Secure Multi-Tenant
       REST API              REST API
     (Students)            (TodoItems)
          |                   |
          v                   v
    Role validation     Role and ownership
                            validation
```

Keycloak is responsible for authentication and issuing tokens.

The REST APIs remain responsible for enforcing authorization.

The Faces client is responsible for managing the user's authenticated session and communicating with the secured APIs.

## 4. Authentication and Authorization Workflow

When a user attempts to access a protected Jakarta Faces page, the application must determine whether the user's session contains a usable access token.

### Scenario 1 — User is not authenticated

The application redirects the user to the login page.

### Scenario 2 — User is authenticated

The application includes the access token in the HTTP Authorization header:

```http
Authorization: Bearer <access_token>
```

The secured REST API validates the token and determines whether the user has permission to perform the requested operation.

### Scenario 3 — Access token has expired

The application attempts to obtain a new access token using the refresh token.

If the session cannot be refreshed, the user must authenticate again.

### Scenario 4 — User does not have permission

The REST API denies the operation.

The Faces application displays an appropriate access denied message.

The user should not be logged out simply because an operation is forbidden.

## 5. Understanding Security Responses

The Faces REST client must distinguish authentication failures from authorization failures.

| HTTP Status | Meaning | Client Behavior |
|---|---|---|
| `200 OK` | Request completed successfully | Process and display the returned data |
| `201 Created` | Resource created successfully | Display a success message |
| `204 No Content` | Request completed without a response body | Display a success message |
| `401 Unauthorized` | Authentication is missing, invalid, or expired | Refresh authentication or redirect to login |
| `403 Forbidden` | Authenticated user does not have permission | Display an access denied message |

For example, an Administration user may successfully log in and retrieve all TodoItems but receive `403 Forbidden` when attempting to create a TodoItem.

This is an authorization failure, not a login failure.

## 6. Open the Lesson 31 Starter Project

Open the existing `dmit2015-microprofile-jwt-demo-main` project.

It contains the following three projects:

```text
dmit2015-microprofile-jwt-demo-main/
|
|-- dmit2015-secure-rbac-restapi/
|
|-- dmit2015-secure-multitenant-restapi/
|
|-- dmit2015-secure-restclient/
```

For this lesson, we will work inside:

```text
dmit2015-secure-restclient
```

Do not create a new project.

Do not replace the starter project with the  completed project.

The Lesson 29 and Lesson 30 projects will continue to provide our secured backend REST APIs.

## 7. Explore the Existing Starter Project

The starter project already contains a Jakarta Faces application that communicates with the Student and TodoItem REST APIs.


### Existing Components

| Component | Purpose |
|---|---|
| `Student.java` | Represents Student data returned by the REST API |
| `TodoItemDto.java` | Represents TodoItem data returned by the REST API |
| `StudentMpRestClient.java` | Communicates with the Student REST API |
| `TodoItemDtoMpRestClient.java` | Communicates with the TodoItem REST API |
| `service` package | Provides service methods for the managed beans |
| `StudentCrudView.java` | Manages Student CRUD operations |
| `TodoItemDtoCrudView.java` | Manages TodoItem CRUD operations |
| `manage-students.xhtml` | Displays and manages Student records |
| `manage-todos.xhtml` | Displays and manages TodoItem records |
| `layout.xhtml` | Provides the shared application layout and navigation |

We will preserve these existing components and extend them to support authentication.

## 8. Review the Existing REST Client Workflow

Before adding security, review how the application communicates with the REST APIs.

For example, when a user opens the Manage TodoItems page:

```text
manage-todos.xhtml
        |
        v
TodoItemDtoCrudView
        |
        v
TodoItemDtoService
        |
        v
MpRestClientTodoItemDtoService
        |
        v
TodoItemDtoMpRestClient
        |
        v
TodoItem REST API
```

The existing application already supports REST API communication.

However, the REST client does not yet automatically include a JWT access token when sending requests.

Since our backend REST APIs are now secured, we need to extend the existing REST client to support authenticated requests.

## 9. Start Keycloak

Make sure your existing Keycloak container is running.

If you are using Podman, check the running containers:

```bash
podman ps
```

Open the Keycloak Administration Console:

```text
http://localhost:8180
```

Verify that:

- Your existing realm is available.
- Your JWT client is available.
- Your existing users are available.
- The Sales, Shipping, and Administration roles are configured.
- Users have the appropriate roles.
- The client supports the direct access grant used in our earlier lessons.

If you configured Active Directory user federation in previous lessons, verify that the connection is still working.

We will reuse the Keycloak configuration from Lessons 29 and 30.

There is no need to create another realm or duplicate the existing users.

## 10. Verify the Required Application Ports

Our application uses four different services.

| Application | Port | URL |
|---|---:|---|
| Keycloak | 8180 | `http://localhost:8180` |
| Secure RBAC REST API | 8090 | `http://localhost:8090/restapi/students` |
| Secure Multi-Tenant REST API | 8091 | `http://localhost:8091/restapi/todos` |
| Secure Faces REST Client | 8080 | `http://localhost:8080` |

Each application must use a different HTTP port to avoid conflicts.

## 11. Start the Secure RBAC REST API

Open a terminal in:

```text
dmit2015-secure-rbac-restapi
```

Run:

```bash
mvn wildfly:dev
```

The Student REST API should be available at:

```text
http://localhost:8090/restapi/students
```

Keep the server running.

Because this API is secured, opening the URL directly in a browser may result in an access denied response.

## 12. Start the Secure Multi-Tenant REST API

Open a separate terminal in:

```text
dmit2015-secure-multitenant-restapi
```

Run:

```bash
mvn wildfly:dev
```

The TodoItem REST API should be available at:

```text
http://localhost:8091/restapi/todos
```

Keep the server running.

Before proceeding to the final client tests, verify that both REST APIs accept valid JWT access tokens using the HTTP Client requests from Lessons 29 and 30.

If a valid authenticated request returns `401 Unauthorized`, resolve the authentication issue before testing the Faces client.

If WildFly reports a port conflict, verify that the HTTP, management, and debugging ports do not conflict between the running applications.

## 13. Configure MicroProfile Config Properties

In the Lesson 31 project, open:

```text
src/main/resources/META-INF/microprofile-config.properties
```

The starter project already contains the following REST API configuration:

```properties
dmit2015.restclient.StudentMpRestClient/mp-rest/url=http://localhost:8090/restapi/students
dmit2015.restclient.TodoItemDtoMpRestClient/mp-rest/url=http://localhost:8091/restapi/todos
```

Keep these existing properties.

They specify the URLs used by the MicroProfile REST Client interfaces.

### Add Keycloak Configuration

Append the following properties to the existing file:

```properties
# Keycloak configuration
keycloak.oidc.providerURI=http://localhost:8180/realms/dmit2015-realm/
keycloak.oidc.clientId=dmit2015-jwt-client
keycloak.oidc.clientSecret=${KEYCLOAK_CLIENT_SECRET}
```

The realm and client ID shown above are based on the configuration.

Use the actual realm and client ID configured in your Keycloak server during Lesson 29.

### Understanding the Properties

| Property | Purpose |
|---|---|
| `keycloak.oidc.providerURI` | Base URL of the Keycloak realm |
| `keycloak.oidc.clientId` | Identifies the application requesting authentication |
| `keycloak.oidc.clientSecret` | Authenticates the confidential client with Keycloak |

### Locate the Client Secret

In the Keycloak Administration Console:

1. Select your existing realm.
2. Select **Clients**.
3. Select your JWT client.
4. Open the **Credentials** tab.
5. Copy the client secret.


In README 2, we will create a MicroProfile REST Client interface that communicates with this endpoint.

## 14. Review the Implementation Plan

In README 2, we will implement the following components.

### New Java Classes

| Class | Purpose |
|---|---|
| `KeycloakLoginMpRestClient.java` | Authenticates users and refreshes access tokens |
| `LoginSession.java` | Stores authentication information in session scope |
| `KeycloakLogin.java` | Processes login form submissions |
| `KeycloakLogout.java` | Logs out the user and clears the session |
| `AuthHeaderFactory.java` | Automatically includes JWT access tokens in REST requests |
| `ForbiddenResponseMapper.java` | Handles forbidden REST API responses |

We will also implement appropriate handling for `401 Unauthorized` responses.

### New Jakarta Faces Page

| File | Purpose |
|---|---|
| `login.xhtml` | Provides the login form |

### Existing Components to Modify

| File | Required Change |
|---|---|
| `StudentMpRestClient.java` | Include authentication headers |
| `TodoItemDtoMpRestClient.java` | Include authentication headers |
| `StudentCrudView.java` | Adjust initialization and security response handling |
| `TodoItemDtoCrudView.java` | Adjust initialization and security response handling |
| `TodoItemDto.java` | Include the username ownership field |
| `manage-students.xhtml` | Require authentication |
| `manage-todos.xhtml` | Require authentication and display ownership |
| `layout.xhtml` | Add login and logout navigation |

### Automatic Authorization Headers

There are two approaches to including JWT access tokens in MicroProfile REST Client requests:

1. Pass the Authorization header as a parameter to every REST client method.
2. Use a reusable client header factory to automatically include the Authorization header.

We will follow the second approach using `AuthHeaderFactory.java`.

This avoids modifying every REST client method to accept an additional parameter.

## 16. Connection to Assignment 7

Assignment 7 requires a secure Jakarta Faces REST Client that communicates with secured Bill REST APIs.

Our Lesson 31 demonstration uses Student and TodoItem REST APIs.

The authentication workflow is the same.

| Lesson 31 | Assignment 7 |
|---|---|
| Student REST API | Secure RBAC Bill REST API |
| TodoItem REST API | Secure Multi-Tenant Bill REST API |
| Keycloak login | Keycloak login |
| JWT session storage | JWT session storage |
| Automatic Bearer headers | Automatic Bearer headers |
| Protected Faces pages | Protected Faces pages |
| Security response handling | Security response handling |
| Logout | Logout |

After completing Lesson 31, you can apply the same authentication and REST client security patterns to the Assignment 7 starter project.

Remember that Assignment 7 uses its own entities, roles, endpoints, and authorization requirements.

## Next: README 2 — Implement the Secure Faces REST Client

In README 2, we will implement the Keycloak authentication REST client, session management, login and logout functionality, automatic JWT authorization headers, and protected Jakarta Faces pages.

