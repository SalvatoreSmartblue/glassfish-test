package it.smartblue.glassfishtest.service;

import it.smartblue.glassfishtest.model.Person;
import jakarta.enterprise.context.RequestScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;

@Path("/people")
@Produces(MediaType.APPLICATION_JSON)
@RequestScoped
public class PersonService {

    @PersistenceContext(unitName = "peoplePU")
    private EntityManager em;

    @GET
    public List<Person> getPeople() {
        return em.createQuery("SELECT p FROM Person p", Person.class).getResultList();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Transactional
    public Response addPerson(Person person) {
        if (!valid(person)) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Invalid Person").build();
        }
        em.persist(person);

        return Response.status(Response.Status.CREATED).entity(person).build();
    }

    @DELETE
    @Path("/{id}")
    @Transactional
    public Response deletePerson(@PathParam("id") Integer id) {
        Person p = em.find(Person.class, id);
        if (p == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        em.remove(p);
        return Response.ok(p).build();
    }

    @PUT
    @Path("/{id}")
    @Transactional
    public Response updatePerson(@PathParam("id") Integer id, Person person) {
        Person existing = em.find(Person.class, id);
        if (existing == null || !valid(person)) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
        existing.setName(person.getName());
        existing.setSurname(person.getSurname());
        existing.setAge(person.getAge());
        em.merge(existing);

        return Response.ok(existing).build();
    }

    private boolean valid(Person p) {
        return p.getName() != null && p.getSurname() != null && p.getAge() >= 0;
    }
}
