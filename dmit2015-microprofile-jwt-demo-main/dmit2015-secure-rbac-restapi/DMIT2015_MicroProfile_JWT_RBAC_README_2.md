# README 2 — Configure MicroProfile JWT and Role-Based Access Control


## Part 1 — Understand Role-Based Access Control

Role-Based Access Control (RBAC) determines which operations a user may perform based on their assigned roles.

In README 1, you obtained an access token containing claims similar to:

```json
{
  "upn": "student01",
  "groups": ["Sales"]
}
```

The `upn` claim identifies the authenticated user.

The `groups` claim identifies the user's security roles or groups.

Your Student REST API will use these role values to determine whether a user is authorized to invoke a particular endpoint.

### Student REST API Permission Matrix

For this demonstration, configure the following access rules:

| HTTP Method | Endpoint | Allowed Roles |
|---|---|---|
| GET | `/restapi/students` | Sales, Shipping |
| GET | `/restapi/students/{id}` | Sales, Shipping |
| POST | `/restapi/students` | Sales, Shipping |
| PUT | `/restapi/students/{id}` | Sales, Shipping |
| DELETE | `/restapi/students/{id}` | Administration |

These are the authorization rules you will implement in this lesson.
In this lesson, we use the existing Keycloak roles Sales, Shipping, and Administration to demonstrate role-based access control. Although these roles are not specific to a student-management system, they allow us to practice restricting REST API operations using @RolesAllowed.
**Important:** A user with a valid JWT is not automatically authorized to access every endpoint. The user's roles must satisfy the security restrictions configured for the requested operation.

---

## Part 2 — Verify the Maven Configuration

Open your project:

```text
dmit2015-secure-rbac-restapi
```

Open:

```text
pom.xml
```

### Step 1 — Verify the MicroProfile Dependency

Locate the following dependency:

```xml
<dependency>
    <groupId>org.eclipse.microprofile</groupId>
    <artifactId>microprofile</artifactId>
    <version>7.1</version>
    <type>pom</type>
    <scope>provided</scope>
</dependency>
```

This dependency is already included in the provided Student REST API project.

You do not need to add it again.

The `provided` scope indicates that the application server supplies the MicroProfile implementation at runtime.

### Step 2 — Verify the WildFly JWT Layer

Locate the WildFly Maven plugin configuration.

Confirm that the following layers are present:

```xml
<layers>
    <layer>cloud-server</layer>
    <layer>microprofile-config</layer>
    <layer>microprofile-openapi</layer>
    <layer>microprofile-jwt</layer>
</layers>
```

The `microprofile-jwt` layer enables MicroProfile JWT support in the provisioned WildFly server.

This layer is already configured in your project.

### Step 3 — Verify the WildFly Maven Plugin

Your project uses:

```xml
<groupId>org.wildfly.plugins</groupId>
<artifactId>wildfly-maven-plugin</artifactId>
<version>6.2.0.Final</version>
```

Keep the existing plugin configuration unless you encounter a specific build or runtime compatibility problem.

---

## Part 3 — Configure JWT Verification

Your Student REST API must verify the authenticity of access tokens received from clients.

Keycloak signs JWTs using a private key.

The REST API verifies the signature using the corresponding public key.

### Step 1 — Create the Configuration File

In IntelliJ IDEA, navigate to:

```text
src/main/resources
```

Create a directory named:

```text
META-INF
```

Inside `META-INF`, create a file named:

```text
microprofile-config.properties
```

The completed file path should be:

```text
src/main/resources/META-INF/microprofile-config.properties
```

### Step 2 — Add the JWT Configuration

Add:

```properties
mp.jwt.verify.publickey.location=http://localhost:8180/realms/dmit2015-realm/protocol/openid-connect/certs
mp.jwt.verify.issuer=http://localhost:8180/realms/dmit2015-realm
```


In Keycloak:
1. Click Realm settings in the left menu.
2. Open the General tab.
3. Scroll down to Endpoints.
4. Click OpenID Endpoint Configuration.

This opens a JSON document containing the realm's authentication endpoints.
Step 3 — Find these two values
Look for:

1. issuer
"issuer": "http://localhost:8180/realms/%20dmitt2015-realm"


Copy this value into:
mp.jwt.verify.issuer=http://localhost:8180/realms/%20dmitt2015-realm


2. jwks_uri

Look for a value similar to:
"jwks_uri": "http://localhost:8180/realms/%20dmitt2015-realm/protocol/openid-connect/certs"


Copy this value into:
mp.jwt.verify.publickey.location=http://localhost:8180/realms/%20dmitt2015-realm/protocol/openid-connect/certs



### Step 3 — Understand the Public Key Location

```properties
mp.jwt.verify.publickey.location=http://localhost:8180/realms/dmit2015-realm/protocol/openid-connect/certs
```

This property identifies Keycloak's public-key endpoint.

The endpoint publishes JSON Web Keys that can be used to verify JWT signatures.

Open the following URL in your browser, using your actual realm:

```text
http://localhost:8180/realms/dmit2015-realm/protocol/openid-connect/certs
```

You should receive a JSON response containing signing-key information.

### Step 4 — Understand the Issuer

```properties
mp.jwt.verify.issuer=http://localhost:8180/realms/dmit2015-realm
```

The issuer identifies the trusted system that created the JWT.

The REST API compares this configured issuer with the token's `iss` claim.

A token with an unexpected issuer should be rejected.


---

## Part 4 — Enable JWT Authentication in the REST Application

Open:

```text
src/main/java/dmit2015/config/JaxRsApplication.java
```

### Step 1 — Review the Existing Code

Your existing application class contains:

```java
package dmit2015.config;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;

@ApplicationPath("/restapi")
public class JaxRsApplication extends Application {

}
```

The `@ApplicationPath` annotation defines the base path for the REST API.

At this point, JWT authentication has not yet been enabled in this class.

### Step 2 — Replace the Existing Code

Replace the file contents with:

```java
package dmit2015.config;

import jakarta.annotation.security.DeclareRoles;
import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;
import org.eclipse.microprofile.auth.LoginConfig;

@ApplicationPath("/restapi")
@LoginConfig(
        authMethod = "MP-JWT",
        realmName = "dmit2015-realm"
)
@DeclareRoles({
        "Sales",
        "Shipping",
        "Administration"
})
public class JaxRsApplication extends Application {

}
```

Use the appropriate authentication realm name for your environment.

### Step 3 — Understand `@LoginConfig`

```java
@LoginConfig(
        authMethod = "MP-JWT",
        realmName = "dmit2015-realm"
)
```

The `@LoginConfig` annotation configures the authentication mechanism used by the application.

The value:

```java
authMethod = "MP-JWT"
```

enables MicroProfile JWT authentication.

When a client sends a bearer token, the runtime validates it using the JWT verification configuration.

The `realmName` value identifies the authentication realm. The JWT issuer itself is verified using `mp.jwt.verify.issuer`.

### Step 4 — Understand `@DeclareRoles`

```java
@DeclareRoles({
        "Sales",
        "Shipping",
        "Administration"
})
```

This annotation declares the security roles used by the application.

For this lesson, the roles are:

- `Sales`
- `Shipping`
- `Administration`

These role names must match the values recognized from the authenticated user's JWT.

Role names are case-sensitive.

For example:

```text
Sales
```

is not the same role name as:

```text
sales
```


---

## Part 5 — Enable RESTEasy Role-Based Security

WildFly uses RESTEasy as its Jakarta REST implementation.

Configure RESTEasy to enforce role-based security annotations on REST endpoints.

### Step 1 — Open `web.xml`

Navigate to:

```text
src/main/webapp/WEB-INF/web.xml
```

Your existing file contains a JSON-B configuration:

```xml
<context-param>
    <param-name>resteasy.preferJacksonOverJsonB</param-name>
    <param-value>false</param-value>
</context-param>
```

Keep this configuration.

### Step 2 — Add the Security Context Parameter

Add the following inside the existing `<web-app>` element:

```xml
<context-param>
    <param-name>resteasy.role.based.security</param-name>
    <param-value>true</param-value>
</context-param>
```

The relevant configuration should now look like:

```xml
<context-param>
    <param-name>resteasy.preferJacksonOverJsonB</param-name>
    <param-value>false</param-value>
</context-param>

<context-param>
    <param-name>resteasy.role.based.security</param-name>
    <param-value>true</param-value>
</context-param>
```

Do not delete the existing H2 Console servlet configuration or other entries from `web.xml`.

### Step 3 — Understand the Configuration

The following property enables RESTEasy role-based security:

```text
resteasy.role.based.security=true
```

RESTEasy can then enforce security annotations such as:

```java
@RolesAllowed("Sales")
```

on REST resource methods.

---

## Part 6 — Secure the Student REST API

You will now restrict access to the existing Student CRUD methods.

Open:

```text
src/main/java/dmit2015/resource/StudentDtoResource.java
```

**Important:** Do not replace the entire resource class. Keep the existing Student repository, DTO mapping, validation, exception handling, and persistence logic.

You only need to add the authorization import and annotations.

### Step 1 — Import `@RolesAllowed`

Add the following import:

```java
import jakarta.annotation.security.RolesAllowed;
```

### Step 2 — Secure GET All Students

Locate:

```java
@GET
public Response findAllStudentsStudents() {
```

Add `@RolesAllowed` above the `@GET` annotation:

```java
@RolesAllowed({"Sales", "Shipping"})
@GET
public Response findAllStudentsStudents() {
    return Response.ok(
            studentRepository
                    .findAll()
                    .stream()
                    .map(StudentMapper.INSTANCE::toDto)
                    .collect(Collectors.toList())
    ).build();
}
```

This allows users with either the `Sales` or `Shipping` role to retrieve all Students.

### Step 3 — Secure GET Student by ID

Locate:

```java
@Path("{id}")
@GET
public Response findStudentByIdStudentById(@PathParam("id") Long id) {
```

Add:

```java
@RolesAllowed({"Sales", "Shipping"})
@Path("{id}")
@GET
public Response findStudentByIdStudentById(@PathParam("id") Long id) {
```

Keep the existing method body unchanged.

This restricts access to individual Student records to users with the `Sales` or `Shipping` role.

### Step 4 — Secure POST Student

Locate:

```java
@POST
public Response createStudentStudent(StudentDto dto, @Context UriInfo uriInfo) {
```

Add:

```java
@RolesAllowed({"Sales", "Shipping"})
@POST
public Response createStudentStudent(StudentDto dto, @Context UriInfo uriInfo) {
```

Keep the existing validation, repository insertion, and HTTP response logic.

Users with the `Sales` or `Shipping` role will be authorized to create Students.

### Step 5 — Secure PUT Student

Locate:

```java
@PUT
@Path("{id}")
public Response updateStudentStudent(@PathParam("id") Long id, StudentDto dto) {
```

Add:

```java
@RolesAllowed({"Sales", "Shipping"})
@PUT
@Path("{id}")
public Response updateStudentStudent(@PathParam("id") Long id, StudentDto dto) {
```

Keep the existing validation and optimistic-locking logic.

Users with the `Sales` or `Shipping` role will be authorized to update Students.

### Step 6 — Secure DELETE Student

Locate:

```java
@DELETE
@Path("{id}")
public Response deleteStudentStudent(@PathParam("id") Long id) {
```

Add:

```java
@RolesAllowed("Administration")
@DELETE
@Path("{id}")
public Response deleteStudentStudent(@PathParam("id") Long id) {
```

Keep the existing deletion logic unchanged.

Only users with the `Administration` role will be authorized to delete Students.

---

## Part 7 — Understand How JWT Authorization Works

After completing the configuration, the Student REST API will use the following process:

### Step 1 — Authenticate with Keycloak

A user sends their credentials to Keycloak.

Keycloak authenticates the user and issues a signed JWT.

For example, the JWT may contain:

```json
{
  "upn": "student01",
  "groups": ["Sales"]
}
```

### Step 2 — Send the JWT to the Student REST API

The client includes the token in the HTTP Authorization header:

```http
GET http://localhost:8090/restapi/students
Authorization: Bearer {{user1AccessToken}}
Accept: application/json
```

### Step 3 — Validate the JWT

MicroProfile JWT validates the token, including:

- Its digital signature.
- Its issuer.
- Its expiration.
- Other applicable token validation requirements.

### Step 4 — Check the User's Roles

RESTEasy checks the authorization annotation on the requested resource method.

For example:

```java
@RolesAllowed({"Sales", "Shipping"})
```

If the authenticated user has the `Sales` role, the request is authorized.

If the user does not have an allowed role, access is denied.

### Step 5 — Execute the REST Method

If authentication and authorization succeed, the existing Student CRUD method executes.

The method continues using the same repository, DTO mapping, validation, and database operations.


---

## Part 8 — Build and Run the Secured Student REST API


### Step 1 — Start WildFly

Run:

```bash
mvn wildfly:dev
```

Wait for WildFly to start and the application to deploy.

### Step 5 — Verify the REST API Address

visit StudentDtoResource.java and do the testing.
```text
dmit2015-secure-rbac-restapi/
└── src/
    └── main/
        ├── java/
           └── dmit2015/
               └── resource/
                   └── StudentDtoResource.java
     
```

---


### Summary of Changes

| File | Change |
|---|---|
| `pom.xml` | Verified existing MicroProfile JWT support |
| `microprofile-config.properties` | Created JWT public-key and issuer configuration |
| `JaxRsApplication.java` | Added `@LoginConfig` and `@DeclareRoles` |
| `web.xml` | Enabled RESTEasy role-based security |
| `StudentDtoResource.java` | Added `@RolesAllowed` to CRUD methods |

---


## Summary

In this lesson, you configured your Student REST API to validate Keycloak-issued JWTs using MicroProfile JWT.

You also declared security roles and applied role-based access restrictions to the existing Student CRUD endpoints.

The REST API now has the configuration needed to authenticate requests and enforce role-based authorization.

**Next: README 3 — Test the Secured Student REST API Using JWT Bearer Tokens.**