package deploydb

import com.google.common.collect.ImmutableList

import io.dropwizard.Application
import io.dropwizard.assets.AssetsBundle
import io.dropwizard.db.DataSourceFactory
import io.dropwizard.flyway.*
import io.dropwizard.hibernate.HibernateBundle
import io.dropwizard.hibernate.SessionFactoryFactory
import io.dropwizard.setup.Bootstrap
import io.dropwizard.setup.Environment
import io.dropwizard.views.ViewBundle
import org.hibernate.SessionFactory
import org.joda.time.DateTimeZone

import deploydb.resources.*
import deploydb.health.*
import deploydb.models.*
import deploydb.dao.*


class DeployDBApp extends Application<DeployDBConfiguration> {
    private final ImmutableList models = ImmutableList.of(Artifact, Deployment)
    private WebhookManager webhooks

    static void main(String[] args) throws Exception {
        new DeployDBApp().run(args)
    }

    @Override
    String getName() {
        return 'deploydb'
    }

    /** Return the current org.hibernate.SessionFactory
     *
     * This is really only ever intended to be used in integration tests as a
     * means of getting a hold of the same SessionFactory that Resources will
     * be using
     */
    SessionFactory getSessionFactory() {
        return hibernate.sessionFactory
    }

    private final HibernateBundle<DeployDBConfiguration> hibernate =
            new HibernateBundle<DeployDBConfiguration>(models, new SessionFactoryFactory()) {
                @Override
                DataSourceFactory getDataSourceFactory(DeployDBConfiguration config) {
                    return config.getDataSourceFactory()
                }
            }


    @Override
    public void initialize(Bootstrap<DeployDBConfiguration> bootstrap) {
        bootstrap.addBundle(new AssetsBundle())
        bootstrap.addBundle(new ViewBundle())
        bootstrap.addBundle(hibernate)


        webhooks = new WebhookManager()

        /*
         * Force our default timezone to always be UTC
         */
        DateTimeZone.setDefault(DateTimeZone.UTC)

        bootstrap.addBundle(new FlywayBundle<DeployDBConfiguration>() {
            @Override
            DataSourceFactory getDataSourceFactory(DeployDBConfiguration config) {
                return config.getDataSourceFactory()
            }

            @Override
            FlywayFactory getFlywayFactory(DeployDBConfiguration config) {
                return config.getFlywayFactory()
            }
        })
    }

    @Override
    public void run(DeployDBConfiguration configuration,
                    Environment environment) {
        final ArtifactDAO adao = new ArtifactDAO(hibernate.sessionFactory)
        final DeploymentDAO ddao = new DeploymentDAO(hibernate.sessionFactory)

        environment.lifecycle().manage(webhooks)

        environment.healthChecks().register('sanity', new SanityHealthCheck())
        environment.healthChecks().register('webhook', new WebhookHealthCheck(webhooks))

        environment.jersey().register(new RootResource())
        environment.jersey().register(new ArtifactResource(adao))
        environment.jersey().register(new DeploymentResource(ddao, adao))
    }
}
