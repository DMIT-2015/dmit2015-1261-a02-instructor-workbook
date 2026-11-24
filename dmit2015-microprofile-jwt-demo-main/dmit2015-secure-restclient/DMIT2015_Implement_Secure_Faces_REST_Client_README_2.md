# Lesson 31 — Secure Faces REST Client with JWT

## README 2 — Implement the Secure Faces REST Client

### Learning Objectives

In this README, we will extend our existing Jakarta Faces application to:

- Authenticate users with Keycloak.
- Store and refresh JWT access tokens.
- Automatically include JWT access tokens in REST API requests.
- Protect Jakarta Faces pages.
- Handle authentication and authorization failures.
- Implement login and logout functionality.

Continue working inside:

```text
dmit2015-secure-restclient
```

## 1. Configure the Keycloak REST Client

Open:

```text
src/main/resources/META-INF/microprofile-config.properties
```

Keep the existing configuration from README 1.

Append the following property:

```properties
dmit2015.restclient.KeycloakLoginMpRestClient/mp-rest/url=${keycloak.oidc.providerURI}protocol/openid-connect/token
```

This property specifies the Keycloak token endpoint that our new REST Client will use.

The complete configuration should look like this:

```properties
dmit2015.restclient.StudentMpRestClient/mp-rest/url=http://localhost:8090/restapi/students
dmit2015.restclient.TodoItemDtoMpRestClient/mp-rest/url=http://localhost:8091/restapi/todos

# Keycloak configuration
keycloak.oidc.providerURI=http://localhost:8180/realms/%20dmitt2015-realm/
keycloak.oidc.clientId=dmit2015-jwt-client
keycloak.oidc.clientSecret=YOUR_CLIENT_SECRET

# Keycloak REST Client
dmit2015.restclient.KeycloakLoginMpRestClient/mp-rest/url=${keycloak.oidc.providerURI}protocol/openid-connect/token
```

Use your existing realm, client ID and client secret.

Do not commit actual client secrets to Git.

## 2. Create the Keycloak Authentication REST Client

Create the following Java interface:

```text
src/main/java/dmit2015/restclient/KeycloakLoginMpRestClient.java
```

Add:

```java
package dmit2015.restclient;

import jakarta.enterprise.context.RequestScoped;
import jakarta.json.JsonObject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@RequestScoped
@RegisterRestClient
public interface KeycloakLoginMpRestClient {

    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    JsonObject authenticate(
            @FormParam("username") String username,
            @FormParam("password") String password,
            @FormParam("client_id") String clientId,
            @FormParam("client_secret") String clientSecret,
            @FormParam("grant_type") String grantType
    );

    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    JsonObject refreshToken(
            @FormParam("refresh_token") String refreshToken,
            @FormParam("client_id") String clientId,
            @FormParam("client_secret") String clientSecret,
            @FormParam("grant_type") String grantType
    );
}
```

### Understanding the Code

| Method | Purpose |
|---|---|
| `authenticate()` | Authenticates a user using their username and password |
| `refreshToken()` | Obtains a new access token using a refresh token |

Both methods communicate with the Keycloak token endpoint.

In Lesson 29, we submitted these form parameters manually using IntelliJ HTTP Client. We are now performing the same operation programmatically.

This direct access grant approach follows our classroom demonstration. Production web applications generally use the OpenID Connect Authorization Code flow.

## 3. Create the Login Session

After successful authentication, Keycloak returns a JSON response containing an access token, refresh token and expiration information.

We need to store this information throughout the user's session.

Create:

```text
src/main/java/dmit2015/view/LoginSession.java
```

Add:

```java
package dmit2015.view;

import dmit2015.restclient.KeycloakLoginMpRestClient;
import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.json.JsonObject;
import lombok.Getter;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import java.io.Serializable;
import java.time.LocalDateTime;

@Named
@SessionScoped
public class LoginSession implements Serializable {

    @Inject
    @RestClient
    private KeycloakLoginMpRestClient loginRestClient;

    @Inject
    @ConfigProperty(name = "keycloak.oidc.clientId")
    private String oidcClientId;

    @Inject
    @ConfigProperty(name = "keycloak.oidc.clientSecret")
    private String oidcClientSecret;

    @Getter
    private String username;

    private String accessToken;

    @Getter
    private String refreshToken;

    private LocalDateTime expiresIn;

    public void initializeSession(
            String username, JsonObject responsePayload) {

        this.username = username;
        updateTokens(responsePayload);
    }

    private void updateTokens(JsonObject responsePayload) {

        accessToken = responsePayload.getString("access_token");

        refreshToken = responsePayload.getString(
                "refresh_token", refreshToken);

        expiresIn = LocalDateTime.now()
                .plusSeconds(responsePayload.getInt("expires_in"));
    }

    public boolean isAuthenticated() {
        return accessToken != null;
    }

    public String getAuthorization() {

        if (!isAuthenticated()) {
            return null;
        }

        if (!LocalDateTime.now().isBefore(expiresIn)) {

            try {
                JsonObject responsePayload =
                        loginRestClient.refreshToken(
                                refreshToken,
                                oidcClientId,
                                oidcClientSecret,
                                "refresh_token");

                updateTokens(responsePayload);

            } catch (Exception e) {
                clear();
                return null;
            }
        }

        return "Bearer " + accessToken;
    }

    public String checkForToken() {

        if (getAuthorization() == null) {
            return "/login?faces-redirect=true";
        }

        return null;
    }

    public void clear() {

        username = null;
        accessToken = null;
        refreshToken = null;
        expiresIn = null;
    }
}
```

### Understanding the Code

| Method | Purpose |
|---|---|
| `initializeSession()` | Stores authentication information after login |
| `updateTokens()` | Updates tokens and expiration time |
| `isAuthenticated()` | Checks whether an access token is stored |
| `getAuthorization()` | Returns the Bearer token and refreshes it when necessary |
| `checkForToken()` | Redirects unauthenticated users to login |
| `clear()` | Removes authentication information |

The `@SessionScoped` annotation allows the authentication information to remain available while the user navigates between Faces pages.

Each user has a separate session.

## 4. Create the Keycloak Login Managed Bean

Create:

```text
src/main/java/dmit2015/view/KeycloakLogin.java
```

Add:

```java
package dmit2015.view;

import dmit2015.restclient.KeycloakLoginMpRestClient;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.json.JsonObject;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Getter;
import lombok.Setter;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.omnifaces.util.Messages;

@Named
@RequestScoped
public class KeycloakLogin {

    @Inject
    @RestClient
    private KeycloakLoginMpRestClient loginRestClient;

    @Inject
    private LoginSession loginSession;

    @Inject
    private HttpServletRequest request;

    @Inject
    @ConfigProperty(name = "keycloak.oidc.clientId")
    private String oidcClientId;

    @Inject
    @ConfigProperty(name = "keycloak.oidc.clientSecret")
    private String oidcClientSecret;

    @Getter
    @Setter
    private String username;

    @Getter
    @Setter
    private String password;

    public String submit() {

        try {

            JsonObject responsePayload =
                    loginRestClient.authenticate(
                            username,
                            password,
                            oidcClientId,
                            oidcClientSecret,
                            "password");

            request.getSession();
            request.changeSessionId();

            loginSession.initializeSession(
                    username, responsePayload);

            return "/index?faces-redirect=true";

        } catch (Exception e) {

            loginSession.clear();

            Messages.addGlobalError(
                    "Login failed. Verify your credentials "
                            + "and Keycloak configuration.");

            return null;

        } finally {
            password = null;
        }
    }
}
```

The login managed bean submits the user's credentials to Keycloak.

If authentication succeeds, it stores the authentication information in `LoginSession` and redirects the user to the home page.

If authentication fails, an error message is displayed.

The user's password is not stored in the session.

## 5. Create the Keycloak Logout Managed Bean

Create:

```text
src/main/java/dmit2015/view/KeycloakLogout.java
```

Add:

```java
package dmit2015.view;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.Form;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@Named
@RequestScoped
public class KeycloakLogout {

    @Inject
    private LoginSession loginSession;

    @Inject
    private HttpServletRequest request;

    @Inject
    @ConfigProperty(name = "keycloak.oidc.providerURI")
    private String oidcProviderURI;

    @Inject
    @ConfigProperty(name = "keycloak.oidc.clientId")
    private String oidcClientId;

    @Inject
    @ConfigProperty(name = "keycloak.oidc.clientSecret")
    private String oidcClientSecret;

    public String submit() {

        try {

            if (loginSession.getRefreshToken() != null) {

                Form form = new Form();

                form.param("client_id", oidcClientId);
                form.param("client_secret", oidcClientSecret);
                form.param("refresh_token",
                        loginSession.getRefreshToken());

                try (Client client = ClientBuilder.newClient();
                     Response response = client
                             .target(oidcProviderURI
                                     + "protocol/openid-connect/logout")
                             .request()
                             .post(Entity.form(form))) {

                    if (response.getStatus() >= 400) {
                        System.err.println(
                                "Keycloak logout failed: "
                                        + response.getStatus());
                    }
                }
            }

        } catch (Exception e) {

            System.err.println("Unable to complete Keycloak logout.");

        } finally {

            loginSession.clear();

            if (request.getSession(false) != null) {
                request.getSession(false).invalidate();
            }
        }

        return "/index?faces-redirect=true";
    }
}
```

When the user logs out, the application:

1. Sends a logout request to Keycloak.
2. Clears the authentication information.
3. Invalidates the local HTTP session.
4. Redirects the user to the home page.

## 6. Create the Login Page

Create:

```text
src/main/webapp/login.xhtml
```

Add:

```xml
<ui:composition
        xmlns="http://www.w3.org/1999/xhtml"
        xmlns:ui="jakarta.faces.facelets"
        xmlns:h="jakarta.faces.html"
        xmlns:p="primefaces"
        template="/WEB-INF/faces-templates/layout.xhtml">

    <ui:param name="title" value="Login"/>

    <ui:define name="maincontent">

        <h:panelGroup
                rendered="#{loginSession.authenticated}">

            <h:outputText
                    value="Logged in as #{loginSession.username}"/>

        </h:panelGroup>

        <h:form id="loginForm"
                rendered="#{not loginSession.authenticated}">

            <h1>Login</h1>

            <div class="field">

                <p:outputLabel for="username"
                               value="Username"/>

                <p:inputText id="username"
                             value="#{keycloakLogin.username}"
                             required="true"
                             styleClass="inputfield w-full"/>

            </div>

            <div class="field">

                <p:outputLabel for="password"
                               value="Password"/>

                <p:password id="password"
                            value="#{keycloakLogin.password}"
                            required="true"
                            styleClass="inputfield w-full"/>

            </div>

            <p:commandButton
                    value="Login"
                    icon="pi pi-sign-in"
                    action="#{keycloakLogin.submit}"
                    ajax="false"/>

        </h:form>

    </ui:define>

</ui:composition>
```

The login page collects the username and password and invokes:

```xml
action="#{keycloakLogin.submit}"
```

If the user is already authenticated, the page displays their username instead of the login form.

## 7. Create the Authorization Header Factory

Instead of manually passing the JWT access token to every REST Client method, we will use a reusable `ClientHeadersFactory`.

Create:

```text
src/main/java/dmit2015/restclient/AuthHeaderFactory.java
```

Add:

```java
package dmit2015.restclient;

import dmit2015.view.LoginSession;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import org.eclipse.microprofile.rest.client.ext.ClientHeadersFactory;

@ApplicationScoped
public class AuthHeaderFactory implements ClientHeadersFactory {

    @Inject
    private LoginSession loginSession;

    @Override
    public MultivaluedMap<String, String> update(
            MultivaluedMap<String, String> incomingHeaders,
            MultivaluedMap<String, String> clientOutgoingHeaders) {

        MultivaluedMap<String, String> headers =
                new MultivaluedHashMap<>();

        String authorization = loginSession.getAuthorization();

        if (authorization != null) {
            headers.putSingle("Authorization", authorization);
        }

        return headers;
    }
}
```

This factory automatically adds the following header to secured REST API requests:

```http
Authorization: Bearer <access_token>
```

We will register this factory with both existing REST Clients.

## 8. Handle Security Responses

Our secured REST APIs can return two important security responses.

| HTTP Status | Client Behavior |
|---|---|
| `401 Unauthorized` | Clear authentication and redirect to login |
| `403 Forbidden` | Display an access denied message |

### 8.1 Create the Forbidden Response Mapper

Create:

```text
src/main/java/dmit2015/restclient/ForbiddenResponseMapper.java
```

Add:

```java
package dmit2015.restclient;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.ext.ResponseExceptionMapper;

public class ForbiddenResponseMapper
        implements ResponseExceptionMapper<WebApplicationException> {

    @Override
    public WebApplicationException toThrowable(Response response) {

        return new WebApplicationException(
                "Access denied. You do not have permission "
                        + "to perform this operation.",
                Response.Status.FORBIDDEN);
    }

    @Override
    public boolean handles(
            int status,
            MultivaluedMap<String, Object> headers) {

        return status == 403;
    }
}
```

This mapper provides a meaningful error message when a user attempts an operation that is not permitted.

### 8.2 Create the Unauthorized Response Mapper

Create:

```text
src/main/java/dmit2015/restclient/UnauthorizedResponseMapper.java
```

Add:

```java
package dmit2015.restclient;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.ext.ResponseExceptionMapper;

public class UnauthorizedResponseMapper
        implements ResponseExceptionMapper<WebApplicationException> {

    @Override
    public WebApplicationException toThrowable(Response response) {

        return new WebApplicationException(
                "Authentication required. Please log in again.",
                Response.Status.UNAUTHORIZED);
    }

    @Override
    public boolean handles(
            int status,
            MultivaluedMap<String, Object> headers) {

        return status == 401;
    }
}
```

We will handle the login redirection in our existing CRUD managed beans.

## 9. Secure the Existing REST Clients

We do not need to rewrite our existing REST Client methods.

We only need to register the authorization header factory and response mappers.

### 9.1 Secure the Student REST Client

Open:

```text
src/main/java/dmit2015/restclient/StudentMpRestClient.java
```

Add the following imports:

```java
import org.eclipse.microprofile.rest.client.annotation.RegisterClientHeaders;
import org.eclipse.microprofile.rest.client.annotation.RegisterProvider;
```

Update the annotations above the interface declaration:

```java
@RequestScoped
@RegisterRestClient
@RegisterClientHeaders(AuthHeaderFactory.class)
@RegisterProvider(ForbiddenResponseMapper.class)
@RegisterProvider(UnauthorizedResponseMapper.class)
public interface StudentMpRestClient {
```

Keep all existing REST Client methods unchanged.

### 9.2 Secure the TodoItem REST Client

Open:

```text
src/main/java/dmit2015/restclient/TodoItemDtoMpRestClient.java
```

Add:

```java
import org.eclipse.microprofile.rest.client.annotation.RegisterClientHeaders;
```

Update the annotations above the interface declaration:

```java
@RequestScoped
@RegisterRestClient
@RegisterClientHeaders(AuthHeaderFactory.class)
@RegisterProvider(TodoItemRestApiResponseMapper.class)
@RegisterProvider(ForbiddenResponseMapper.class)
@RegisterProvider(UnauthorizedResponseMapper.class)
public interface TodoItemDtoMpRestClient {
```

Keep all existing REST Client methods unchanged.

Do not remove the existing `TodoItemRestApiResponseMapper` because it handles validation errors.

Both REST Clients will now automatically include the JWT access token.

## 10. Update the CRUD Managed Beans

Our existing managed beans retrieve records using an `init()` method annotated with `@PostConstruct`.

We need to check authentication before retrieving secured records.

### 10.1 Update StudentCrudView

Open:

```text
src/main/java/dmit2015/view/StudentCrudView.java
```

Remove the `@PostConstruct` annotation from the existing `init()` method.

Remove the unused import:

```java
import jakarta.annotation.PostConstruct;
```

Add the following imports:

```java
import jakarta.ws.rs.WebApplicationException;
import org.omnifaces.util.Faces;
```

Inject the login session:

```java
@Inject
private LoginSession loginSession;
```

Add the following helper method inside the class:

```java
private boolean handleUnauthorized(Exception ex) {

    Throwable cause = ex;

    while (cause != null) {

        if (cause instanceof WebApplicationException webEx
                && webEx.getResponse().getStatus() == 401) {

            loginSession.clear();

            Faces.redirect("login.xhtml");

            return true;
        }

        cause = cause.getCause();
    }

    return false;
}
```

At the beginning of the existing `init()` method, add:

```java
if (Faces.isPostback()
        || Faces.getContext().getResponseComplete()) {
    return;
}

if (loginSession.getAuthorization() == null) {
    Faces.redirect("login.xhtml");
    return;
}
```

Keep the existing code that retrieves Student records.

Inside the existing exception handler in `init()`, add the following before displaying the error message:

```java
if (handleUnauthorized(e)) {
    return;
}
```

Next, locate the exception handlers in `onSave()` and `onDelete()`.

Add the following at the beginning of each existing catch block:

```java
if (handleUnauthorized(ex)) {
    return;
}
```

For example:

```java
} catch (RuntimeException ex) {

    if (handleUnauthorized(ex)) {
        return;
    }

    Messages.addGlobalError(ex.getMessage());
}
```

Keep the remaining CRUD logic unchanged.

### 10.2 Update TodoItemDtoCrudView

Open:

```text
src/main/java/dmit2015/view/TodoItemDtoCrudView.java
```

Apply the same changes:

1. Remove `@PostConstruct` and its unused import.
2. Add the `WebApplicationException` and `Faces` imports.
3. Inject `LoginSession`.
4. Add the `handleUnauthorized()` helper method.
5. Add the authentication and postback checks at the beginning of `init()`.
6. Add the unauthorized response check to the existing exception handlers in `init()`, `onSave()` and `onDelete()`.

Do not replace the existing CRUD methods.

These changes allow the managed beans to redirect users to login when the REST API rejects their authentication.

A `403 Forbidden` response will continue to be displayed using the existing Faces error messages.

## 11. Protect the Jakarta Faces Pages

We will now check authentication before allowing users to access the Student and TodoItem management pages.

### 11.1 Protect the Student Page

Open:

```text
src/main/webapp/student/manage-students.xhtml
```

Locate:

```xml
<ui:param name="title" value="Student - CRUD"/>
```

Immediately after this line, add:

```xml
<f:metadata>

    <f:viewParam name="dummy"/>

    <f:viewAction
            action="#{loginSession.checkForToken}"
            onPostback="true"/>

    <f:event
            type="postInvokeAction"
            listener="#{currentStudentCrudView.init}"/>

</f:metadata>
```

### 11.2 Protect the TodoItem Page

Open:

```text
src/main/webapp/todo/manage-todos.xhtml
```

Locate:

```xml
<ui:param name="title" value="TodoItemDto - CRUD"/>
```

Immediately after this line, add:

```xml
<f:metadata>

    <f:viewParam name="dummy"/>

    <f:viewAction
            action="#{loginSession.checkForToken}"
            onPostback="true"/>

    <f:event
            type="postInvokeAction"
            listener="#{currentTodoItemDtoCrudView.init}"/>

</f:metadata>
```

### Understanding the Changes

The `f:viewAction` checks whether the user has a usable access token.

If the user is not authenticated, the application redirects to the login page.

The `postInvokeAction` event invokes the existing `init()` method.

This replaces the previous `@PostConstruct` initialization approach and allows Faces error messages to be displayed appropriately.

The REST APIs remain responsible for enforcing role-based access and record ownership.

## 12. Display TodoItem Ownership

In Lesson 30, we implemented multi-tenancy using the TodoItem username field.

We will now display this information in our Faces application.

### 12.1 Update the TodoItem Model

Open:

```text
src/main/java/dmit2015/model/TodoItemDto.java
```

Add the following field if it does not already exist:

```java
private String username;
```

Keep the existing fields, Lombok annotations and methods unchanged.

### 12.2 Update the TodoItem Data Table

Open:

```text
src/main/webapp/todo/manage-todos.xhtml
```

Locate the existing TodoItem data table.

Add the following column alongside the existing columns:

```xml
<p:column headerText="Username"
          sortBy="#{currentItem.username}">

    <h:outputText value="#{currentItem.username}"/>

</p:column>
```

This allows us to see which user owns each TodoItem.

Do not add a username input field to the create or update form.

The secured REST API determines record ownership using the authenticated JWT identity.

## 13. Update the Application Navigation

Open:

```text
src/main/webapp/WEB-INF/faces-templates/layout.xhtml
```

Locate the existing navigation menu:

```xml
<p:menubar role="navigation">
```

Add the following menu items inside the existing menubar:

```xml
<p:menuitem
        value="Login"
        icon="pi pi-sign-in"
        outcome="/login"
        rendered="#{not loginSession.authenticated}"/>

<p:menuitem
        value="Logout (#{loginSession.username})"
        icon="pi pi-sign-out"
        action="#{keycloakLogout.submit}"
        ajax="false"
        rendered="#{loginSession.authenticated}"/>
```

Keep the existing Home, Student and TodoItem menu items.

The Login menu item is displayed when no access token is stored.

The Logout menu item is displayed when the user has an authenticated session.

## 14. Review the Implementation


### Authentication Workflow

```text
User
  |
  v
Login Page
  |
  v
Keycloak Authentication
  |
  v
LoginSession
  |
  v
AuthHeaderFactory
  |
  v
Secured REST API
  |
  +-- Success: Display data
  |
  +-- 401: Redirect to login
  |
  +-- 403: Display access denied
```

## Next: README 3 — Test the Secure Faces REST Client

In README 3, we will compile and start the application and verify:

- Login and logout.
- Protected Faces pages.
- Student role-based permissions.
- TodoItem record ownership.
- Administration read-only access to TodoItems.
- Authentication and authorization failures.
- Access token refresh.

The same implementation patterns can be applied to the secure Bill REST Client in Assignment 7.