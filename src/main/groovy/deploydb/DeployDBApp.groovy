package deploydb

import com.google.common.collect.ImmutableList
import com.google.common.collect.ImmutableMap
import io.dropwizard.Application
import io.dropwizard.assets.AssetsBundle
import io.dropwizard.cli.CheckCommand
import io.dropwizard.cli.Cli
import io.dropwizard.cli.ServerCommand
import io.dropwizard.db.DataSourceFactory
import io.dropwizard.flyway.FlywayBundle
import io.dropwizard.flyway.FlywayFactory
import io.dropwizard.hibernate.HibernateBundle
import io.dropwizard.hibernate.SessionFactoryFactory
import io.dropwizard.setup.Bootstrap
import io.dropwizard.setup.Environment
import io.dropwizard.util.JarLocation
import io.dropwizard.views.ViewBundle
import org.hibernate.SessionFactory
import org.joda.time.DateTimeZone
import org.slf4j.Logger
import org.slf4j.LoggerFactory


class DeployDBApp extends Application<DeployDBConfiguration> {
    private final ImmutableList models = ImmutableList.of(
            models.Artifact, models.Deployment,
            models.PromotionResult, models.Flow)
    private static final Logger logger = LoggerFactory.getLogger(DeployDBApp.class)
    private WebhookManager webhooksManager
    private WorkFlow workFlow
    private provider.V1TypeProvider typeProvider
    private String configDirectory

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
        bootstrap.addBundle(hibernate)
        workFlow = new WorkFlow(this)


        bootstrap.addBundle(new ViewBundle<DeployDBConfiguration>() {
            @Override
            ImmutableMap<String, ImmutableMap<String, String>> getViewConfiguration(DeployDBConfiguration config) {
                return config.viewRendererConfiguration
            }
        })

        /*
         * Force our default timezone to always be UTC
         */
        DateTimeZone.setDefault(DateTimeZone.UTC)
        logger.info("Set default timezone to UTC")

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

        typeProvider = new provider.V1TypeProvider(bootstrap.objectMapper,
                bootstrap.validatorFactory.validator)
    }

    @Override
    public void run(String... arguments) throws Exception {
        try {
            final Bootstrap<DeployDBConfiguration> bootstrap = new Bootstrap<>(this);
            bootstrap.addCommand(new ServerCommand<>(this));
            bootstrap.addCommand(new CheckCommand<>(this));
            initialize(bootstrap);
            final Cli cli = new Cli(new JarLocation(getClass()), bootstrap, System.out, System.err);
            if (!cli.run(arguments)) {
                println("\n\nExample usage to run: deploydb server config/deploydb.launch.yml")
                println("Example usage to do database migration: deploydb db migrate config/deploydb.launch.yml\n\n")
                // only exit if there's an error running the command
                System.exit(1);
            }
        } catch(Exception e) {
            println(e.getMessage())
            println("\n\nExample usage to run: deploydb server config/deploydb.launch.yml")
            println("Example usage to do database migration: deploydb db migrate config/deploydb.launch.yml\n\n")
            // only exit if there's an error running the command
            System.exit(1);
        }
    }
    @Override
    public void run(DeployDBConfiguration configuration,
                    Environment environment) {
        /*
         * Create webhook manager based on configuration
         */
        webhooksManager = new WebhookManager(configuration)

         /**
         * Initialize the workflow object
         */
        workFlow.initializeDao()
        workFlow.initializeRegistry()

        /**
         * Load configuration models
         */
        this.configDirectory = configuration.configDirectory
        try {
            workFlow.loadConfigModels(false)
        } catch (Exception e) {
            logger.error("failed to read config from directory: " +
                    "${configDirectory}, error: ${e.toString()}")
        }

        /**
         * webhooksManager
         */
        environment.lifecycle().manage(webhooksManager)

        /**
         * Healthchecks
         */
        environment.healthChecks().register('sanity', new health.SanityHealthCheck())
        environment.healthChecks().register('webhook', new health.WebhookHealthCheck(webhooksManager))

        /** Add admin task for config reload */
        environment.admin().addTask(new ConfigReloadTask(workFlow));

        /**
         * Instantiate Resources classes for Jersey handlers
         */
        environment.jersey().register(typeProvider)
        environment.jersey().register(new resources.RootResource())
        environment.jersey().register(new resources.ArtifactResource(workFlow))
        environment.jersey().register(new resources.DeploymentResource(workFlow))
        environment.jersey().register(new resources.FlowResource(workFlow))
        environment.jersey().register(new resources.PromotionResource(workFlow.promotionRegistry))
        environment.jersey().register(new resources.EnvironmentResource(workFlow.environmentRegistry))
        environment.jersey().register(new resources.PipelineResource(workFlow.pipelineRegistry))
        environment.jersey().register(new resources.ServiceResource(workFlow.serviceRegistry))
    }
}
