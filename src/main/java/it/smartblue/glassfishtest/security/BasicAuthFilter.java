package it.smartblue.glassfishtest.security;

import it.smartblue.glassfishtest.model.User;
import it.smartblue.glassfishtest.util.PasswordUtils;
import jakarta.annotation.Priority;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;
import java.nio.charset.StandardCharsets;
import java.util.Base64;


@Provider
@Priority(Priorities.AUTHENTICATION)
public class BasicAuthFilter implements ContainerRequestFilter {

    @PersistenceContext(unitName = "peoplePU")
    private EntityManager em;

    @Override
    public void filter(ContainerRequestContext requestCtx) {
        // Escludi login e register dalla protezione Basic Auth
        String path = requestCtx.getUriInfo().getPath();
        if (path.startsWith("auth/login") || path.startsWith("auth/register")) {
            return;
        }

        String auth = requestCtx.getHeaderString("Authorization");
        if (auth == null || !auth.startsWith("Basic ")) {
            abort(requestCtx);
            return;
        }

        String[] creds = new String(Base64
                .getDecoder()
                .decode(auth.substring(6)), StandardCharsets.UTF_8)
                .split(":", 2);
        String email = creds[0];
        String password = creds[1];

        User user = em.createQuery(
                        "SELECT u FROM User u WHERE u.email = :email", User.class)
                .setParameter("email", email)
                .getResultStream().findFirst().orElse(null);

        if (user == null || !PasswordUtils.verify(password, user.getPasswordHash())) {
            abort(requestCtx);
        }
    }

    private void abort(ContainerRequestContext ctx) {
        ctx.abortWith(Response.status(Response.Status.UNAUTHORIZED)
                .header("WWW-Authenticate", "Basic realm=\"Users\"")
                .build());
    }
}
