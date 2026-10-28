# Demo: Configuring HTTPS in WildFly

Based on Slides 3–5 and 20 of your DMIT2015 Jakarta Security slide.

## Introduction — What are we doing?


> “In our previous lesson, we configured Keycloak and LDAP to authenticate users in our Movie Manager application.
>
> Today, we're going to secure the communication between the browser and WildFly using HTTPS.
>
> Keycloak tells us who the user is, and our security roles determine what they can access. But we also need to protect the information travelling between the browser and our application.
>
> We're going to generate an SSL/TLS certificate, configure WildFly to use it, and then make sure our application requires HTTPS instead of HTTP.”


We want all application requests to use a secure connection.

## Step 1 — Configure Jakarta Security in WildFly

SLIDE 3 You will also find the code: Before configuring HTTPS, we prepare WildFly to work with Jakarta Security. We create a CLI script containing commands that change the WildFly server configuration.

Where: In the root of your Movie Manager Maven project, create the folder:

`wildfly-scripts`

Inside it, create:

`configure-elytron.cli`

Code:

```
/subsystem=undertow/application-security-domain=other:write-attribute(name=integrated-jaspi,value=false)

/subsystem=undertow/server=default-server/https-listener=https:write-attribute(name=enable-http2,value=false)

# Reload the server configuration

#reload
```

Code explanation:

- The first command disables `integrated-jaspi` so the course's Jakarta Security authentication mechanism can manage identities.
- The second command disables HTTP/2 for the classroom certificate setup.





## Step 2 — Tell Maven to execute the configuration

Where: Open `pom.xml` in the root of the project.

Find the `wildfly-maven-plugin` and add the script inside its `<configuration>` section:

We're telling Maven to apply our WildFly configuration automatically when the plugin provisions the server. This means we don't have to enter the same configuration commands manually each time.

```
<scripts>
    <script>./wildfly-scripts/configure-elytron.cli</script>
</scripts>
```

Code explanation:

- `<scripts>` lists WildFly CLI scripts.
- `<script>` identifies the file to execute.
- `./wildfly-scripts/` points to the folder in our project.





## Step 3 — Create a folder for our SSL certificate

SLIDE 4 You will also find the code.

First, we're creating a folder to store the certificate and private key that WildFly will use for HTTPS.

Where: Open a terminal in Ubuntu.

Code:

```
mkdir -p ~/jdk/ssl
```

Code explanation:

- `mkdir` means make directory.
- `-p` creates missing parent folders.
- `~/jdk/ssl` is where we'll store the keystore.




## Step 4 — Generate the SSL/TLS certificate


Now we're generating our own self-signed certificate using Java's keytool. WildFly will use this certificate to establish encrypted connections with the browser.

We're using `localhost` because we're testing on our local computer. Since this certificate isn't issued by a trusted certificate authority, our browser will display a warning. That's expected for us.


Where: In the same Ubuntu terminal.


```
keytool -genkeypair -alias localhost -keyalg RSA -keysize 2048 -validity 3650 \
    -storetype PKCS12 -keystore ~/jdk/ssl/server.keystore \
    -dname "CN=localhost, OU=DMIT, O=NAIT, L=Edmonton, ST=Alberta, C=CA" \
    -keypass ServerPassword2015 -storepass ServerPassword2015
```

Code explanation:

| Code                        | Meaning                                                       |
| --------------------------- | ------------------------------------------------------------- |
| `keytool`                   | Java's tool for managing keys and certificates                |
| `-genkeypair`               | Creates a public/private key pair and self-signed certificate |
| `-alias localhost`          | Gives the key entry a name                                    |
| `-keyalg RSA`               | Uses RSA encryption technology                                |
| `-keysize 2048`             | Specifies the RSA key size                                    |
| `-validity 3650`            | Makes the certificate valid for about 10 years                |
| `-storetype PKCS12`         | Sets the keystore format                                      |
| `-keystore`                 | Specifies where to save the keystore                          |
| `-dname`                    | Provides information identifying the certificate              |
| `-keypass` and `-storepass` | Protect the private key and keystore                          |



## Step 5 — Create the HTTPS configuration script

We've created the certificate, but WildFly doesn't automatically know where it is. This script tells WildFly to use our keystore for HTTPS connections.

It's called one-way HTTPS because the server presents its certificate to the browser. The browser doesn't need to provide a client certificate.

Where: Return to IntelliJ IDEA.

Inside the project's `wildfly-scripts` folder, create:

`enable-oneway-https.cli`

Code:

```
# CLI script to enable one-way SSL/TLS for Applications
# This script assumes that you have already generated a keystore file

security enable-ssl-http-server --key-store-path=~/jdk/ssl/server.keystore --key-store-password=ServerPassword2015 --override-ssl-context
```

Code explanation:

- `security enable-ssl-http-server` enables HTTPS for WildFly's HTTP server.
- `--key-store-path` tells WildFly where the keystore is located.
- `--key-store-password` provides the password to open it.
- `--override-ssl-context` allows the command to replace the existing SSL context.





## Step 6 — Add the second script to `pom.xml`

SLIDE 5 You will also find the code.

Now we're telling Maven to run both scripts. One prepares WildFly for Jakarta Security, and the other configures the HTTPS certificate.

Where: Open `pom.xml` again.

Inside the existing `wildfly-maven-plugin` configuration, update the scripts to include both:

```
<scripts>
    <script>./wildfly-scripts/configure-elytron.cli</script>
    <script>./wildfly-scripts/enable-oneway-https.cli</script>
</scripts>
```

Code explanation:

The first script configures Jakarta Security support. The second script configures HTTPS.



## Step 7 — Run WildFly and test HTTPS


Now we're starting WildFly and checking whether it accepts HTTPS connections.

Notice we're using `https` instead of `http`, and port `8443` instead of `8080`.

If the browser warns us about the self-signed certificate, we'll inspect the certificate and confirm that it's the one we created.

At this point, HTTPS may work, but HTTP may still be accessible.


Where: Open the terminal in IntelliJ IDEA, from the project root.

Run:

```
mvn wildfly:run
```

Then open your browser and navigate to:

`https://localhost:8443`

Or open the full URL of your Movie Manager application using port `8443`.



## Step 8 — Force the application to use HTTPS

SLIDE 20 you will also find the code.

Until now, we've enabled HTTPS, but users might still be able to open the application using HTTP.

This security constraint tells WildFly that every URL in our application must use a secure connection.

The important line is `CONFIDENTIAL`. This means the server must protect the communication using HTTPS.

Where: In IntelliJ IDEA, open:

`src/main/webapp/WEB-INF/web.xml`

Add this inside the `<web-app>` element, alongside your existing security constraints:

```
<security-constraint>
    <display-name>Use HTTPS only</display-name>

    <web-resource-collection>
        <web-resource-name>sslResources</web-resource-name>
        <url-pattern>/*</url-pattern>
    </web-resource-collection>

    <user-data-constraint>
        <transport-guarantee>CONFIDENTIAL</transport-guarantee>
    </user-data-constraint>
</security-constraint>
```

Code explanation:

- `<security-constraint>` defines a security rule.
- `<url-pattern>/*</url-pattern>` applies to all URLs in the application.
- `<user-data-constraint>` defines how communication must be protected.
- `CONFIDENTIAL` requires a secure connection, normally HTTPS.



## Step 9 — Restart and verify the redirect


Now let's test whether our security rule works.

I'm deliberately entering an HTTP URL. Notice that WildFly redirects us to HTTPS.

That means we're not just supporting HTTPS; we're requiring it for the application.


Where: Terminal and browser.

Stop WildFly with `Ctrl+C`, then start it again:

```
mvn wildfly:run
```

Now enter your application's HTTP URL, for example:

`http://localhost:8080/your-application/`

Expected result: The browser should automatically redirect to the HTTPS version, using port `8443`, provided WildFly's redirect configuration is working.



## Final recap

| File or location                          | What we configured                      |
| ----------------------------------------- | --------------------------------------- |
| `wildfly-scripts/configure-elytron.cli`   | Jakarta Security support                |
| `~/jdk/ssl/server.keystore`               | Certificate and private key             |
| `wildfly-scripts/enable-oneway-https.cli` | WildFly HTTPS configuration             |
| `pom.xml`                                 | Runs both CLI scripts                   |
| `web.xml`                                 | Requires HTTPS for all application URLs |



> “Today, we've learned how to protect communication between the browser and our Jakarta EE application.
>
> We generated a certificate, configured WildFly to use it, and added a security constraint requiring HTTPS.
>
> Remember, Keycloak authenticates the user, security roles control what they can access, and HTTPS protects the information travelling between the browser and the server.
>
> You'll apply this same HTTPS configuration in Assignment 5.”

