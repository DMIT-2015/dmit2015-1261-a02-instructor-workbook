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