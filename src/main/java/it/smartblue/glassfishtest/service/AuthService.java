package it.smartblue.glassfishtest.service;

import it.smartblue.glassfishtest.model.User;
import it.smartblue.glassfishtest.util.Credentials;
import it.smartblue.glassfishtest.util.PasswordUtils;
import jakarta.enterprise.context.RequestScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;


@Path("/auth")
@RequestScoped
public class AuthService {

    @PersistenceContext(unitName = "peoplePU")
    private EntityManager em;

    @POST
    @Path("/login")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response login(Credentials creds) {
        User user = em
                .createQuery("SELECT u FROM User u WHERE u.email = :email", User.class)
                .setParameter("email", creds.email())
                .getResultStream().findFirst().orElse(null);

        if (user == null || !PasswordUtils.verify(creds.password(), user.getPasswordHash())) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        return Response.ok().build();
    }

    @POST
    @Path("/register")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional
    public Response register(Credentials creds) {
        User user = new User();
        user.setEmail(creds.email());
        user.setPasswordHash(PasswordUtils.hash(creds.password()));
        em.persist(user);

        return Response.status(Response.Status.CREATED).build();
    }
}
