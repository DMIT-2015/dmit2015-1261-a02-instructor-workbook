### Lesson 20: Jakarta Security and OpenID Connect (OIDC) with Keycloak

In this lesson, we secure the **Movie Manager** Jakarta EE application using Jakarta Security, OpenID Connect (OIDC), Keycloak, and Windows Server 2022 Active Directory.

**Authentication:** Verifies who is logging in.

**Authorization:** Determines what an authenticated user is allowed to access.

#### Application Architecture

```text
Movie Manager (WildFly)
        ↓
Jakarta Security / OpenID Connect (OIDC)
        ↓
Keycloak
        ↓
dmit2015-realm
        ↓
LDAP
        ↓
Windows Server 2022 Active Directory
        ↓
Users and Groups
```

**Movie Manager Roles:**

| Role | Permissions |
|---|---|
| Sales | View, create, and edit movies |
| Administration | View and delete movies |

**Provided Course Document:**

[Jakarta Security and OpenID Connect – LMS](https://lms.nait.ca/d2l/le/lessons/191328/topics/6251874)

The remaining setup instructions and code examples are available in the provided LMS document under the section names and step numbers listed below.

---

#### Part 1 – Start Windows Server 2022




Download and extract the provided [Windows Server 2022 VM](https://naitca.sharepoint.com/teams/DMITStudents/Course%20Materials/Forms/AllItems.aspx?viewid=06f819cc%2Da415%2D4b03%2D8230%2D58e9dcc2a2a5&d=w93560bc0fedd4f928780fdd151ade3e4&csf=1&CID=2f8bfea6%2D50e1%2D46c9%2Da4ca%2D88215ffa4ed2&FolderCTID=0x012000F30D0DFEF4B7F24CADFAC2C27E9B263F&ovuser=5c98fb47%2Dd3b9%2D4649%2D9d94%2Df88cbdd9729c%2CRAFATI%40nait%2Eca&id=%2Fteams%2FDMITStudents%2FCourse%20Materials%2FDMIT2015%20%2D%20Enterprise%20Application%20Development%2F1261%2FDMIT2015%2D1261%2DWindows2022%2DServer%2E7z&parent=%2Fteams%2FDMITStudents%2FCourse%20Materials%2FDMIT2015%20%2D%20Enterprise%20Application%20Development%2F1261)
Start the VM and sign in using the provided course credentials.

Open PowerShell:

```powershell
ipconfig
```

Find the Windows Server VM's IPv4 address.

From Ubuntu, test the connection:

```bash
ping YOUR_WINDOWS_VM_IP
```

Press `Ctrl + C` to stop.

**Configure a Static IP**

On Windows Server:

```text
Network & Internet Settings
→ Advanced Network Settings
→ More Network Adapter Options
→ Ethernet Properties
→ Internet Protocol Version 4 (TCP/IPv4)
```

Configure a suitable static IP address, subnet mask, gateway, and DNS settings for the VM network.

Test the connection again from Ubuntu after changing the IP address.

**What it does:** Starts the Active Directory server containing the course users and groups that Keycloak will use for authentication.

---

#### Part 2 – Get the Movie Manager Project

Fork and clone the provided repository:

```text
DMIT2015 Jakarta Security Movie Manager : https://github.com/DMIT-2015/dmit2015-jakarta-security-movie-manager
```

Open the project in IntelliJ IDEA.

Reload Maven dependencies if required.

**What it does:** Provides the Jakarta EE Movie Manager application that will be secured using Keycloak and OIDC.

---

#### Part 3 – Start Keycloak

In Ubuntu Terminal, start the Keycloak container:

```bash
podman start dmit2015-keycloak
```

View the logs:

```bash
podman logs dmit2015-keycloak
```

Open the Keycloak Admin Console:

```text
http://localhost:8180/admin/
```

**Keycloak Administrator Credentials:**

```text
Username: user2015
Password: Password2015
```

Sign in and verify that the Keycloak administration interface opens.

**What it does:** Starts Keycloak, which manages user authentication for Movie Manager.

---

#### Part 4 – Create the Keycloak Realm

**Provided document section:**

```text
Create the Keycloak realm
```

In Keycloak:

```text
Manage Realms
→ Create Realm
```

Enter:

```text
Realm name: dmit2015-realm
```

Create the realm.

Then go to:

```text
Realm Settings → Tokens
```

Configure:

```text
Access Token Lifespan: 30 minutes
```

Save.

Make sure the selected realm is:

```text
dmit2015-realm
```

Not:

```text
master
```

**What it does:** Creates a separate security environment containing the application's users, roles, clients, and authentication settings.

---

#### Part 5 – Configure LDAP User Federation

**Provided document section:**

```text
Configure Keycloak realm LDAP provider
```

Go to:

```text
User Federation → Add LDAP Provider
```

**LDAP Settings:**

| Setting | Value |
|---|---|
| UI display name | DMIT2015 Windows Server 2022 VM |
| Vendor | Active Directory |
| Connection URL | `ldap://YOUR_WINDOWS_VM_IP` |
| Bind DN | `cn=JSEO,ou=Shipping,ou=Departments,dc=dmit2015,dc=ca` |
| Bind credentials | `Password2015` |
| Edit mode | `READ_ONLY` |
| Users DN | `ou=Departments,dc=dmit2015,dc=ca` |
| Search scope | `Subtree` |

Save the LDAP provider.

Test the LDAP connection and authentication using the available Keycloak controls.

Check that Active Directory users appear under:

```text
Users
```

**What it does:** Connects Keycloak to Windows Server Active Directory so users can authenticate using their directory accounts.

---

#### Part 6 – Configure the LDAP Role Mapper

**Provided document section:**

```text
To configure the role for a LDAP user with the Windows Server 2022 VM
```

Go to:

```text
User Federation
→ DMIT2015 Windows Server 2022 VM
→ Mappers
→ Add Mapper
```

Enter:

```text
Name:          role
Mapper type:   role-ldap-mapper
LDAP Roles DN: ou=Departments,dc=dmit2015,dc=ca
```

Click **Save**.

**What it does:** Maps Active Directory roles into Keycloak so Movie Manager can check user permissions.

---

#### Part 7 – Create an OIDC Client in Keycloak

**Provided document section:**

```text
Configuring Jakarta EE monolith applications for authentication
```

Go to:

```text
Keycloak
→ dmit2015-realm
→ Clients
→ Create Client
```

**Client Settings:**

| Setting | Value |
|---|---|
| Client type | OpenID Connect |
| Client ID | `Jakarta-security-movie-manager` |
| Client authentication | On |

Client ID is your project name.
Continue through the client configuration.

**Valid Redirect URIs:**

```text
http://localhost:8080/*
https://localhost:8443/*
```

**Valid Post Logout Redirect URIs:**

```text
http://localhost:8080/*
https://localhost:8443/*
```

Save the client.

#### Add Client-Scope Mappers

Add the predefined mappers:

```text
groups
upn
```

- `groups` provides group information, such as Sales.
- `upn` provides the user's principal name.

**Steps:**

1. Open **Clients** in Keycloak.
2. Select your client:

   ```text
   Jakarta-security-movie-manager (or whaterever your client name you created)
   ```

3. Open the **Client scopes** tab.
4. Click the dedicated client scope:

   ```text
   Jakarta-security-movie-manager-dedicated
   ```

5. Open **Mappers**.
6. Click **Add mapper predefind → By configuration**.
7. Add a mapper with the following settings: groups, upn (Search by groups, upn in search tab and check mark it)


**What it does:** Registers Movie Manager as an OIDC client so Keycloak can authenticate users and redirect them back to the application.

---

#### Part 8 – Create microprofile-config.properties

**Provided document section:**

```text
Edit Client Scopes
```

Create:

```text
src/main/resources/META-INF/microprofile-config.properties
```

Paste the following configuration code:

```properties
# Keycloak OpenID Connect configuration
# Use http://localhost:8180/realms/dmit2015-realm/.well-known/openid-configuration
# to look up the issuer.

keycloak.oidc.providerURI=http://localhost:8180/realms/d2015-realm/
keycloak.oidc.clientId=Jakarta-security-movie-manager

# Find the client secret on the Credentials tab of the client details.
keycloak.oidc.clientSecret=fgVkcFbrFsMwGbbCl6d6HduEGMyYekIjHPQarVzlNuGsvw6HigURhHNBSo0pTOY7IRLbWcZCNbeVCgh3uKbr5K

keycloak.oidc.redirectURI=http://localhost:8080/callback
```


If the realm is `dmit2015-realm`, the provider URI should normally be:

```properties
keycloak.oidc.providerURI=http://localhost:8180/realms/dmit2015-realm/
```

**Where to Find the Configuration Values:**

| Property | Where to find it |
|---|---|
| `providerURI` | Keycloak → Realm Settings → Endpoints |
| `clientId` | Keycloak → Clients |
| `clientSecret` | Keycloak → Clients → Select Client → Credentials |
| `redirectURI` | Movie Manager callback endpoint |

To check the provider URL:

```text
Realm Settings → Endpoints
```

Open the OpenID Endpoint Configuration link.

To find the client ID:

```text
Clients → Jakarta-security-movie-manager
```

To find the client secret:

```text
Clients
→ Jakarta-security-movie-manager
→ Credentials
```

**What it does:** Stores the Keycloak OIDC settings that Movie Manager uses to connect to the identity provider.


---

#### Part 9 – Create OidcConfig.java

**Provided document section:**

```text
Configuring Jakarta EE monolith applications for authentication
→ Step 6
```

Create the package:

```text
src/main/java/common/security/config/
```

Create the Java class:

```text
OidcConfig.java
```

**Where to find the code:**

Copy the complete `OidcConfig.java` code from **Step 6** of the provided document or paste this code:

```propertiesimport jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.Initialized;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.security.enterprise.authentication.mechanism.http.OpenIdAuthenticationMechanismDefinition;
import lombok.Getter;
import org.eclipse.microprofile.config.inject.ConfigProperty;

/**
 * This class assumes that the Microprofile Config dependency has been added to the project.
 * <pre>@code{
 *
 * <dependency>
 *     <groupId>org.eclipse.microprofile.config</groupId>
 *     <artifactId>microprofile-config-api</artifactId>
 *     <version>3.1</version>
 * </dependency>
 *
 * }</pre>
 */

@OpenIdAuthenticationMechanismDefinition(
		providerURI = "${oidcConfig.oidcProviderURI}",
		clientId = "${oidcConfig.oidcClientId}",
		clientSecret = "${oidcConfig.oidcClientSecret}",
		redirectURI = "${oidcConfig.oidcRedirectURI}",
//		redirectToOriginalResource = true,
		jwksReadTimeout = 5000,
		jwksConnectTimeout = 5000
)

@Named("oidcConfig")
@ApplicationScoped
@Getter
public class OidcConfig {

	@Inject
	@ConfigProperty(name = "keycloak.oidc.providerURI")
	private String oidcProviderURI;

	@Inject
	@ConfigProperty(name = "keycloak.oidc.clientId")
	private String oidcClientId;

	@Inject
	@ConfigProperty(name = "keycloak.oidc.clientSecret")
	private String oidcClientSecret;

	@Inject
	@ConfigProperty(name = "keycloak.oidc.redirectURI")
	private String oidcRedirectURI;

	public void initialize(@Observes @Initialized(ApplicationScoped.class) Object event) {

    }
}
```



The class reads the Keycloak configuration from:

```text
src/main/resources/META-INF/microprofile-config.properties
```

**What it does:** Tells Jakarta Security to authenticate users through Keycloak using OpenID Connect.

---

#### Part 10 – Create CallbackServlet.java

**Provided document section:**

```text
Configuring Jakarta EE monolith applications for authentication
→ Step 7
```

In the same package:

```text
src/main/java/common/security/config/
```

Create:

```text
CallbackServlet.java
```

**Where to find the code:**

Copy the complete `CallbackServlet.java` code and imports from **Step 7** of the provided document or paste this code:

**Main Code:**

```java
import jakarta.inject.Inject;
import jakarta.security.enterprise.authentication.mechanism.http.openid.OpenIdConstant;
import jakarta.security.enterprise.identitystore.openid.OpenIdContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Optional;

@WebServlet("/callback")
public class CallbackServlet extends HttpServlet {
    @Inject
    private OpenIdContext _oidContext;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        if (_oidContext != null) {
            Optional<String> originalRequest = _oidContext.getStoredValue(request, response, OpenIdConstant.ORIGINAL_REQUEST);
            String originalRequestString = originalRequest.orElseThrow();
            response.sendRedirect(originalRequestString);
        }

    }
}

```

**What it does:** Redirects the user back to the originally requested page after successful Keycloak login.

---

#### Part 11 – Create OidcLogout.java

**Provided document section:**

```text
Configuring Jakarta EE monolith applications for authentication
→ Step 8
```

Create the package:

```text
src/main/java/common/security/view/
```

Create:

```text
OidcLogout.java
```

**Where to find the code:**

Copy the complete `OidcLogout.java` code from **Step 8** of the provided document or paste this code:



```java
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.security.enterprise.identitystore.openid.OpenIdContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.omnifaces.util.Messages;

@Named
@RequestScoped
public class OidcLogout {

    @Inject
    private HttpServletRequest request;

    @Inject
    private OpenIdContext _openIdContext;

    @Inject
    @ConfigProperty(name = "keycloak.oidc.providerURI")
    private String oidcProviderURI;

    @Inject
    @ConfigProperty(name = "keycloak.oidc.clientId")
    private String oidcClientId;

    @Inject
    @ConfigProperty(name = "keycloak.oidc.clientSecret")
    private String oidcClientSecret;

    public String submit() throws ServletException {
        final String logoutUrl = String.format("%s/protocol/openid-connect/logout", oidcProviderURI);
        MultivaluedMap<String, String> formData = new MultivaluedHashMap<>();
        formData.add("refresh_token", _openIdContext.getRefreshToken().orElseThrow().getToken());
        formData.add("client_id", oidcClientId);
        formData.add("client_secret", oidcClientSecret);
        try (var restClient = ClientBuilder.newClient();
             Response httpResponse = restClient
                     .target(logoutUrl)
                     .request(MediaType.APPLICATION_FORM_URLENCODED_TYPE)
                     .post(Entity.form(formData))
        ) {
            if (httpResponse.getStatusInfo().getFamily() != Response.Status.Family.SUCCESSFUL) {
                Messages.addGlobalError("Logout failed with status %s", httpResponse.getStatus());
            }
        }

        request.getSession().invalidate();
        return "/index?faces-redirect=true";
    }
}

```


**What it does:** Handles logout from Movie Manager and the required Keycloak OIDC logout process.

---

#### Part 12 – Configure Authorization in web.xml

**Provided document section:**

```text
Configuring Jakarta EE monolith applications for authorization
```

Open:

```text
src/main/webapp/WEB-INF/web.xml
```

**Movie Manager Permissions:**

| Operation | Sales | Administration |
|---|---|---|
| View movie pages | Yes | Yes |
| Create movie | Yes | No |
| Edit movie | Yes | No |
| Delete movie | No | Yes |

**Declare Security Roles**

```xml
<security-role>
    <role-name>Sales</role-name>
</security-role>

<security-role>
    <role-name>Administration</role-name>
</security-role>
```

**View Movies – Both Roles**

```xml
<security-constraint>
    <web-resource-collection>
        <web-resource-name>CanViewMovies</web-resource-name>
        <url-pattern>/movies/index.xhtml</url-pattern>
        <url-pattern>/movies/index</url-pattern>
        <url-pattern>/movies/details.xhtml</url-pattern>
        <url-pattern>/movies/details</url-pattern>
    </web-resource-collection>
    <auth-constraint>
        <role-name>Sales</role-name>
        <role-name>Administration</role-name>
    </auth-constraint>
</security-constraint>
```

**Create and Edit Movies – Sales Only**

```xml
<security-constraint>
    <web-resource-collection>
        <web-resource-name>CanCreateAndEditMovies</web-resource-name>
        <url-pattern>/movies/create.xhtml</url-pattern>
        <url-pattern>/movies/create</url-pattern>
        <url-pattern>/movies/edit.xhtml</url-pattern>
        <url-pattern>/movies/edit</url-pattern>
    </web-resource-collection>
    <auth-constraint>
        <role-name>Sales</role-name>
    </auth-constraint>
</security-constraint>
```

**Delete Movies – Administration Only**

```xml
<security-constraint>
    <web-resource-collection>
        <web-resource-name>CanDeleteMovies</web-resource-name>
        <url-pattern>/movies/delete.xhtml</url-pattern>
        <url-pattern>/movies/delete</url-pattern>
    </web-resource-collection>
    <auth-constraint>
        <role-name>Administration</role-name>
    </auth-constraint>
</security-constraint>
```

Make sure the role names match the roles provided through Keycloak.

**What it does:** Restricts Movie Manager pages according to the authenticated user's role.

---

#### Part 13 – Configure WildFly Elytron

**Provided document section:**

```text
Configuring Jakarta EE monolith applications for authorization
→ Step 2
```

Create a folder in the project root:

```text
wildfly-scripts/
```

Create:

```text
wildfly-scripts/configure-elytron.cli
```


**Code:**

```text
# CLI script to enable elytron for the quickstart application in the application server

# Disable 'integrated-jaspi' as the quickstart will be managing it's own identities
/subsystem=undertow/application-security-domain=other:write-attribute(name=integrated-jaspi, value=false)

# CLI script to disable HTTP/2 as it does not work with self-signed certificates
/subsystem=undertow/server=default-server/https-listener=https:write-attribute(name=enable-http2,value=false)

# Reload the server configuration
#reload
```

**Update pom.xml**

Open:

```text
pom.xml
```

Inside the appropriate WildFly Maven Plugin configuration, add:

```xml
<scripts>
    <script>
        ./wildfly-scripts/configure-elytron.cli
    </script>
</scripts>
```

**What it does:** Configures WildFly Elytron so the Jakarta Security authentication mechanism can manage application identities.

---

#### Part 14 – Add Login and Logout to the JSF Menu

**Provided document section:**

```text
Configuring Jakarta EE monolith applications for authorization
→ Step 3
→ Around line 51
```

Open:

```text
src/main/webapp/WEB-INF/faces-templates/layout.xhtml
```

Inside the main menu, add:

```xhtml
<c:if test="${not empty request.userPrincipal}">
    <p:menuitem
        value="Logout #{request.userPrincipal.name}"
        action="#{oidcLogout.submit}" />
</c:if>

<c:if test="${empty request.userPrincipal}">
    <p:menuitem
        value="Login"
        outcome="/movies/index.xhtml" />
</c:if>
```

**What it does:**

- Shows **Login** when the user is not authenticated.
- Shows **Logout** and the username when authenticated.
- Opens a protected movie page to trigger authentication.

**Note:** The Login outcome is `/movies/index.xhtml`, following the updated value in your notes.

---

#### Part 15 – Run and Test Movie Manager

Start the required services:

```text
Windows Server 2022 VM
Keycloak
WildFly / Movie Manager
```

Run the application using the WildFly Maven Plugin:

```bash
mvn wildfly:dev
```

**Provided Test Accounts:**

```text
Dlee
JWHALEN
jpatel
```

Test the following:

1. Open Movie Manager.
2. Click Login.
3. Verify that Keycloak opens.
4. Sign in using a provided Active Directory account.
5. Verify that the user returns to Movie Manager.
6. Check access to movie pages based on the user's role.
7. Verify that unauthorized access returns HTTP 403.
8. Test Logout.

**What it does:** Confirms that LDAP authentication, Keycloak OIDC login, and role-based authorization are working.

---

#### Part 16 – Add a Custom 403 Error Page

Open:

```text
src/main/webapp/WEB-INF/web.xml
```

Add:

```xml
<error-page>
    <error-code>403</error-code>
    <location>/WEB-INF/errorpages/403.xhtml</location>
</error-page>
```

Create the folder:

```text
src/main/webapp/WEB-INF/errorpages/
```

Create:

```text
403.xhtml
```

**IntelliJ File Template:**

```text
DMIT2015 Security Faces Custom 403 Error Page
```

Use the provided file template to generate the page.

**What it does:** Displays a custom access-denied page when a user tries to access a page without the required role.

---

### Main Lesson 20 Concepts

- **Windows Server Active Directory:** Stores the course users and groups.
- **LDAP:** Allows Keycloak to authenticate Active Directory users.
- **Keycloak:** Manages authentication and OIDC clients.
- **OIDC:** Allows Movie Manager to authenticate users through Keycloak.
- **Jakarta Security:** Integrates authentication into the Jakarta EE application.
- **WildFly Elytron:** Provides the application server's security integration.
- **web.xml:** Defines role-based access restrictions.
- **403.xhtml:** Displays an access-denied page for unauthorized requests.

**Main idea:** Users authenticate through Keycloak using Windows Server Active Directory accounts, while Jakarta Security and WildFly enforce the Sales and Administration permissions in Movie Manager.
