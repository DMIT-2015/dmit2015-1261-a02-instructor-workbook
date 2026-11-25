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