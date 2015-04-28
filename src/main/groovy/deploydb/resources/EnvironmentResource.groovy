package deploydb.resources

import com.codahale.metrics.annotation.Timed
import deploydb.WorkFlow
import deploydb.models.Deployment
import io.dropwizard.hibernate.UnitOfWork
import io.dropwizard.jersey.params.IntParam
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import javax.ws.rs.Consumes
import javax.ws.rs.DefaultValue
import javax.ws.rs.GET
import javax.ws.rs.Path
import javax.ws.rs.PathParam
import javax.ws.rs.Produces
import javax.ws.rs.QueryParam
import javax.ws.rs.WebApplicationException
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

import deploydb.registry.ModelRegistry
import deploydb.models.Environment

/**
 * EnvironmentResource class registered with JettyClient for servicing REST request
 */
@Path("/api/environments")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(['application/json', 'application/vnd.deploydb.v1+json'])
public class EnvironmentResource {
    private final WorkFlow workFlow
    private final Logger logger = LoggerFactory.getLogger(EnvironmentResource.class)

    EnvironmentResource(WorkFlow workFlow) {
        this.workFlow = workFlow
    }

    /**
     * Returns all Environment objects
     */
    @GET
    @UnitOfWork
    @Timed(name = "get-requests")
    List<Environment> getAll() {
        List<Environment> environmentTable = this.workFlow.environmentRegistry.getAll()

        if (environmentTable.isEmpty()) {
            throw new WebApplicationException(Response.Status.NOT_FOUND)
        }
        return environmentTable
    }

    /**
     * Returns a Environment object
     */
    @GET
    @Path("{name}")
    @UnitOfWork
    @Timed(name = "get-requests")
    Environment byName(@PathParam("name") String environmentIdent) {
        Environment environment = this.workFlow.environmentRegistry.get(environmentIdent)

        if (environment == null) {
            throw new WebApplicationException(Response.Status.NOT_FOUND)
        }
        return environment
    }


    /**
     * Returns the deployments using artifact id
     *
     * @param artifactId id of the artifact
     * @return deployments
     */
    @GET
    @Path("/{name}/deployments")
    @UnitOfWork
    @Timed(name = "get-requests")
    List<Deployment> getDeployments(@PathParam("name") String environmentIdent,
                                    @QueryParam("pageNumber") @DefaultValue("0")
                                            IntParam pageNumber,
                                    @QueryParam("perPageSize") @DefaultValue("20")
                                            deploydb.ModelPageSizeParam perPageSize) {
        List<Deployment> deploymentList = this.workFlow.deploymentDAO
                .getByEnvironmentIdent(environmentIdent, pageNumber.get(), perPageSize.get())
        if (deploymentList == null || deploymentList.isEmpty()) {
            throw new WebApplicationException(Response.Status.NOT_FOUND)
        }
        return deploymentList
    }
}

