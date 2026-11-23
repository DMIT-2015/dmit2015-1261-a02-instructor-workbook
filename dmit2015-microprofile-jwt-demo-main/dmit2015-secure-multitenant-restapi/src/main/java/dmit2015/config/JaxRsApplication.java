package dmit2015.config;

import jakarta.annotation.security.DeclareRoles;
import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;
import org.eclipse.microprofile.auth.LoginConfig;

@LoginConfig(authMethod = "MP-JWT")
@DeclareRoles({"Sales", "Shipping", "Administration"})
@ApplicationPath("/restapi")
public class JaxRsApplication extends Application {

}