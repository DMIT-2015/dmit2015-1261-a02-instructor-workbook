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