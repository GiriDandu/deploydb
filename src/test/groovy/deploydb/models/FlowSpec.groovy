package deploydb.models

import deploydb.IntegrationTestAppHelper
import deploydb.Status
import deploydb.dao.PromotionResultDAO
import spock.lang.Specification
import dropwizardintegtest.IntegrationModelHelper
import dropwizardintegtest.IntegrationRestApiClient
import deploydb.ModelConfigHelper


class FlowSpec extends Specification {

    def "ensure it can be instantiated"() {
        when:
        def flow = new Flow()

        then:
        flow instanceof Flow
    }
}

class FlowSpecWithArgsSpec extends Specification {

    def "its properties should be correct "() {
        given:
        Artifact artifact = new Artifact('spock.group',
                'spock-test-name',
                'v1.0.0',
                'http://example.com/cucumber.jar')
        Deployment preProdDeployment = new Deployment(artifact, "pre-production",
                "bluffdale", Status.STARTED)
        Deployment prodDeployment = new Deployment(artifact, "production",
                "bluffdale", Status.STARTED)
        HashSet<Deployment> deployments = new HashSet<Deployment>()
        Flow flow = new Flow()

        preProdDeployment.flow = flow
        prodDeployment.flow = flow
        deployments.add(preProdDeployment)
        deployments.add(prodDeployment)
        String service = "exampleService"

        flow.artifact = artifact
        flow.deployments = deployments
        flow.serviceIdent = service


        expect:
        flow.artifact == artifact
        flow.deployments == deployments
        flow.serviceIdent == service
    }

    def "equality passes for same deployments"() {
        Artifact firstArtifact = new Artifact('spock.group',
                'spock-test-name',
                'v1.0.0',
                'http://example.com/cucumber.jar')
        Deployment preProdDeployment = new Deployment(firstArtifact, "pre-production",
                "bluffdale", Status.STARTED)
        Deployment prodDeployment = new Deployment(firstArtifact, "production",
                "bluffdale", Status.STARTED)
        HashSet<Deployment> firstDeployments = new HashSet<Deployment>()
        Flow firstFlow = new Flow()

        preProdDeployment.flow = firstFlow
        prodDeployment.flow = firstFlow
        firstDeployments.add(preProdDeployment)
        firstDeployments.add(prodDeployment)

        firstFlow.artifact = firstArtifact
        firstFlow.deployments = firstDeployments


        Artifact secondArtifact = new Artifact('spock.group',
                'spock-test-name',
                'v1.0.0',
                'http://example.com/cucumber.jar')
        Deployment secondPreProdDeployment = new Deployment(secondArtifact, "pre-production",
                "bluffdale", Status.STARTED)
        Deployment secondProdDeployment = new Deployment(secondArtifact, "production",
                "bluffdale", Status.STARTED)
        HashSet<Deployment> secondDeployments = new HashSet<Deployment>()
        Flow secondFlow = new Flow()

        secondPreProdDeployment.flow = secondFlow
        secondProdDeployment.flow = secondFlow
        secondDeployments.add(secondPreProdDeployment)
        secondDeployments.add(secondProdDeployment)

        secondFlow.artifact = secondArtifact
        secondFlow.deployments = secondDeployments

        expect:
        firstFlow == secondFlow
    }

    def "equality fails for different deployments"() {
        Artifact firstArtifact = new Artifact('spock.group',
                'spock-test-name',
                'v1.0.0',
                'http://example.com/cucumber.jar')
        Deployment preProdDeployment = new Deployment(firstArtifact, "pre-production",
                "bluffdale", Status.STARTED)
        Deployment prodDeployment = new Deployment(firstArtifact, "production",
                "bluffdale", Status.STARTED)
        HashSet<Deployment> firstDeployments = new HashSet<Deployment>()
        Flow firstFlow = new Flow()

        preProdDeployment.flow = firstFlow
        prodDeployment.flow = firstFlow
        firstDeployments.add(preProdDeployment)
        firstDeployments.add(prodDeployment)

        firstFlow.artifact = firstArtifact
        firstFlow.deployments = firstDeployments


        Artifact secondArtifact = new Artifact('spock.group',
                'spock-test-name',
                'v1.0.0',
                'http://example.com/cucumber.jar')
        Deployment secondPreProdDeployment = new Deployment(secondArtifact, "dev-integ",
                "bluffdale", Status.STARTED)
        Deployment secondProdDeployment = new Deployment(secondArtifact, "dev-beta",
                "bluffdale", Status.STARTED)
        HashSet<Deployment> secondDeployments = new HashSet<Deployment>()
        Flow secondFlow = new Flow()

        secondPreProdDeployment.flow = secondFlow
        secondProdDeployment.flow = secondFlow
        secondDeployments.add(secondPreProdDeployment)
        secondDeployments.add(secondProdDeployment)

        secondFlow.artifact = secondArtifact
        secondFlow.deployments = secondDeployments

        expect:
        firstFlow != secondFlow
    }

    def "equality passes for same service "() {
        String service = "exampleService"
        Flow firstFlow = new Flow()
        Flow secondFlow = new Flow()

        firstFlow.serviceIdent = service
        secondFlow.serviceIdent = service

        expect:
        firstFlow == secondFlow
    }

    def "equality fails for different service "() {
        String firstService = "firstService"
        String secondService = "secondService"
        Flow firstFlow = new Flow()
        Flow secondFlow = new Flow()

        firstFlow.serviceIdent = firstService
        secondFlow.serviceIdent = secondService

        expect:
        firstFlow != secondFlow
    }

    def "hash compare passes for same deployments"() {
        Artifact firstArtifact = new Artifact('spock.group',
                'spock-test-name',
                'v1.0.0',
                'http://example.com/cucumber.jar')
        Deployment preProdDeployment = new Deployment(firstArtifact, "pre-production",
                "bluffdale", Status.STARTED)
        Deployment prodDeployment = new Deployment(firstArtifact, "production",
                "bluffdale", Status.STARTED)
        HashSet<Deployment> firstDeployments = new HashSet<Deployment>()
        Flow firstFlow = new Flow()

        preProdDeployment.flow = firstFlow
        prodDeployment.flow = firstFlow
        firstDeployments.add(preProdDeployment)
        firstDeployments.add(prodDeployment)

        firstFlow.artifact = firstArtifact
        firstFlow.deployments = firstDeployments

        Artifact secondArtifact = new Artifact('spock.group',
                'spock-test-name',
                'v1.0.0',
                'http://example.com/cucumber.jar')
        Deployment secondPreProdDeployment = new Deployment(secondArtifact, "pre-production",
                "bluffdale", Status.STARTED)
        Deployment secondProdDeployment = new Deployment(secondArtifact, "production",
                "bluffdale", Status.STARTED)
        HashSet<Deployment> secondDeployments = new HashSet<Deployment>()
        Flow secondFlow = new Flow()

        secondPreProdDeployment.flow = secondFlow
        secondProdDeployment.flow = secondFlow
        secondDeployments.add(secondPreProdDeployment)
        secondDeployments.add(secondProdDeployment)

        secondFlow.artifact = secondArtifact
        secondFlow.deployments = secondDeployments

        expect:
        firstFlow.hashCode() == secondFlow. hashCode()
    }

    def "hash code fails for different deployments"() {
        Artifact firstArtifact = new Artifact('spock.group',
                'spock-test-name',
                'v1.0.0',
                'http://example.com/cucumber.jar')
        Deployment preProdDeployment = new Deployment(firstArtifact, "pre-production",
                "bluffdale", Status.STARTED)
        Deployment prodDeployment = new Deployment(firstArtifact, "production",
                "bluffdale", Status.STARTED)
        HashSet<Deployment> firstDeployments = new HashSet<Deployment>()
        Flow firstFlow = new Flow()

        preProdDeployment.flow = firstFlow
        prodDeployment.flow = firstFlow
        firstDeployments.add(preProdDeployment)
        firstDeployments.add(prodDeployment)

        firstFlow.artifact = firstArtifact
        firstFlow.deployments = firstDeployments


        Artifact secondArtifact = new Artifact('spock.group',
                'spock-test-name',
                'v1.0.0',
                'http://example.com/cucumber.jar')
        Deployment secondPreProdDeployment = new Deployment(secondArtifact, "dev-integ",
                "bluffdale", Status.STARTED)
        Deployment secondProdDeployment = new Deployment(secondArtifact, "dev-beta",
                "bluffdale", Status.STARTED)
        HashSet<Deployment> secondDeployments = new HashSet<Deployment>()
        Flow secondFlow = new Flow()

        secondPreProdDeployment.flow = secondFlow
        secondProdDeployment.flow = secondFlow
        secondDeployments.add(secondPreProdDeployment)
        secondDeployments.add(secondProdDeployment)

        secondFlow.artifact = secondArtifact
        secondFlow.deployments = secondDeployments

        expect:
        firstFlow.hashCode() != secondFlow.hashCode()
    }

    def "hash code passes for same service "() {
        String service = "exampleService"
        Flow firstFlow = new Flow()
        Flow secondFlow = new Flow()

        firstFlow.serviceIdent = service
        secondFlow.serviceIdent = service

        expect:
        firstFlow.hashCode() == secondFlow.hashCode()
    }

    def "hash code fails for different service "() {
        String firstService = "firstService"
        String secondService = "secondService"
        Flow firstFlow = new Flow()
        Flow secondFlow = new Flow()

        firstFlow.serviceIdent = firstService
        secondFlow.serviceIdent = secondService

        expect:
        firstFlow.hashCode() != secondFlow.hashCode()
    }

}

class FlowCleanupSpec extends Specification {

    IntegrationTestAppHelper integAppHelper = new IntegrationTestAppHelper()
    IntegrationRestApiClient integrationRestApiClient = new IntegrationRestApiClient()
    IntegrationModelHelper integModelHelper = new IntegrationModelHelper(integrationRestApiClient)
    private ModelConfigHelper mcfgHelper = new ModelConfigHelper()

    boolean foundArtifact = true
    List<Deployment> deployments
    List<PromotionResult> promotionResults

    def setup() {
        mcfgHelper.setup()
        integAppHelper.startAppWithConfiguration('deploydb.spock.yml')
        integAppHelper.runner.getApplication().configDirectory = mcfgHelper.baseCfgDirName

        foundArtifact = true
    }

    def cleanup() {
        integAppHelper.stopApp()
        mcfgHelper.cleanup()
    }

    void updateModelsExistence() {
        // update the existence of flow, deployments and promotion results
        PromotionResultDAO promotionResultDAO = new PromotionResultDAO(
                integAppHelper.runner.getApplication().getSessionFactory())

        integAppHelper.runner.getApplication().withHibernateSession {
            foundArtifact = integAppHelper.runner.getApplication().workFlow.artifactDAO.
                    artifactExists("basic.group.1", "bg1", "1.2.345")
            deployments = integAppHelper.runner.getApplication().workFlow.deploymentDAO.
                    getByPage(0, 10)
            promotionResults = promotionResultDAO.getByPage(0, 10)
        }
    }

    def "delete of existing flow cleans up deployments and artifact"() {
        given:
        // Create the required config
        mcfgHelper.createServicePromoitionPipelineModelsConfigFiles()
        mcfgHelper.createEnvironmentNoWebhooksConfigFile()

        // load up the configuration
        integAppHelper.runner.getApplication().loadModelConfiguration()

        // create the artifact, which will create deployments and flow
        integModelHelper.sendCreateArtifact()

        when:

        // remove the flow
        integAppHelper.runner.getApplication().withHibernateSession {
            integAppHelper.runner.getApplication().workFlow.flowDAO.
                    deleteFlowByArtifactNameGroupVersion("basic.group.1", "bg1", "1.2.345")
        }


        then:
        updateModelsExistence()

        foundArtifact == false
        deployments.isEmpty()
        promotionResults.isEmpty()
    }
}