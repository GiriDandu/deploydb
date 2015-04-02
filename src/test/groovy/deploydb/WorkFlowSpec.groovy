package deploydb

import deploydb.dao.FlowDAO
import deploydb.dao.ModelConfigDAO
import deploydb.models.Flow
import spock.lang.*

class WorkFlowSpec extends Specification {

    def "ensure it can be instantiated"() {
        when:
        def app = new DeployDBApp()
        def workFlow = new WorkFlow(app)

        then:
        workFlow instanceof WorkFlow
    }
}

class workFlowWithArgsSpec extends Specification {
    private String baseCfgDirName = "./build/tmp/config"
    private DeployDBApp app = new DeployDBApp()
    private WorkFlow workFlow
    private FlowDAO fdao = Mock(FlowDAO)
    private ModelConfigDAO mdao = Mock(ModelConfigDAO)

    def setup() {
        app.configDirectory = baseCfgDirName
        app.configChecksum = null
        workFlow = new WorkFlow(app)
        workFlow.initializeRegistry()
        workFlow.flowDAO = fdao
        workFlow.modelConfigDAO = mdao
    }

    def cleanup() {
        File baseCfgDir = new File(baseCfgDirName)
        if (baseCfgDir.exists()) {
            baseCfgDir.deleteDir()
        }
    }

    def createPromotionConfigFile() {
        String fileContents = """
type:  BasicPromotion
description: "Basic Smoke test for the Basic Service"
"""
        /* Create temp file */
        File promotionsDir = new File("${baseCfgDirName}/promotions")
        if (!promotionsDir.exists()) {
            promotionsDir.mkdirs()
        }

        File promotionFile = new File(promotionsDir, "basicPromo.yml")
        promotionFile.write(fileContents)
    }

    def createAnotherPromotionConfigFile() {
        String fileContents = """
type:  AdvancedPromotion
description: "Advanced Smoke test for the Basic Service"
"""
        /* Create temp file */
        File promotionsDir = new File("${baseCfgDirName}/promotions")
        if (!promotionsDir.exists()) {
            promotionsDir.mkdirs()
        }

        File promotionFile = new File(promotionsDir, "advancedPromo.yml")
        promotionFile.write(fileContents)
    }

    def createEnvironmentConfigFile() {
        String fileContents = """
description: "Basic Environment"
webhooks:
  deployment:
    created:
      - http://jenkins.example.com/job/basicEnv-deploy-created/build
    completed:
      - http://jenkins.example.com/job/basicEnv-deploy-completed/build
"""
        /* Create temp file */
        File environmentsDir = new File("${baseCfgDirName}/environments")
        if (!environmentsDir.exists()) {
            environmentsDir.mkdirs()
        }

        File environmentFile = new File(environmentsDir, "basicEnv.yml")
        environmentFile.write(fileContents)
    }

    def createPipelineConfigFile() {
        String fileContents = """
description: "Basic pipeline"
environments:
     basicEnv:
       promotions:
          - basicPromo
"""
        /* Create temp file */
        File pipelinesDir = new File("${baseCfgDirName}/pipelines")
        if (!pipelinesDir.exists()) {
            pipelinesDir.mkdirs()
        }

        File pipelineFile = new File(pipelinesDir, "basicPipe.yml")
        pipelineFile.write(fileContents)
    }

    def createServiceConfigFile() {
        String fileContents = """
description: "Basic Service"
artifacts:
  - basic.group.1:bg1
  - basic.group.2:bg2
pipelines:
  - basicPipe
promotions:
  - basicPromo
"""
        /* Create temp file */
        File servicesDir = new File("${baseCfgDirName}/services")
        if (!servicesDir.exists()) {
            servicesDir.mkdirs()
        }

        File serviceFile = new File(servicesDir, "basicServ.yml")
        serviceFile.write(fileContents)
    }

    def createWebhookConfigFile() {
        String fileContents = """
deployment:
  created:
     - http://localhost:10000/job/notify-deployment-created/build
  started:
     - http://localhost:10000/job/notify-deployment-started/build
  completed:
     - http://localhost:10000/job/notify-deployment-completed1/build
     - http://localhost:10000/job/notify-deployment-completed2/build
promotion:
  completed:
     - http://localhost:10000/job/notify-promotion-completed/build
"""
        /* Create temp file */
        File webhookDir = new File("${baseCfgDirName}/webhook")
        if (!webhookDir.exists()) {
            webhookDir.mkdirs()
        }

        File webhookFile = new File(webhookDir, "basicWebhook.yml")
        webhookFile.write(fileContents)
    }

    def "Load entire config from a directory and make sure it passes"() {
        given:
        createPromotionConfigFile()
        createEnvironmentConfigFile()
        createPipelineConfigFile()
        createServiceConfigFile()
        createWebhookConfigFile()
        workFlow.loadConfigModels()
        mdao.persist(_) >> _

        expect:
        workFlow.promotionRegistry.getAll().size() == 1
        workFlow.promotionRegistry.get("basicPromo").ident == "basicPromo"
        workFlow.environmentRegistry.getAll().size() == 1
        workFlow.environmentRegistry.get("basicEnv").ident == "basicEnv"
        workFlow.pipelineRegistry.getAll().size() == 1
        workFlow.pipelineRegistry.get("basicPipe").ident == "basicPipe"
        workFlow.serviceRegistry.getAll().size() == 1
        workFlow.serviceRegistry.get("basicServ").ident == "basicServ"
        workFlow.globalWebhook != null
    }

    def "If promotion is missing, then loading pipeline in loadConfigModels throws an exception"() {
        when:
        createEnvironmentConfigFile()
        createPipelineConfigFile()
        workFlow.loadConfigModels()
        mdao.persist(_) >> _

        then:
        thrown(IllegalArgumentException)
    }

    def "If environment is missing, then loading pipeline in loadConfigModels throws an exception"() {
        when:
        createPromotionConfigFile()
        createPipelineConfigFile()
        workFlow.loadConfigModels()
        mdao.persist(_) >> _

        then:
        thrown(IllegalArgumentException)
    }

    def "If pipeline is missing, then loading service in loadConfigModels throws an exception"() {
        when:
        createPromotionConfigFile()
        createEnvironmentConfigFile()
        createServiceConfigFile()
        workFlow.loadConfigModels()
        mdao.persist(_) >> _

        then:
        thrown(IllegalArgumentException)
    }


    def "Reload unchanged config from a directory and make sure its ignored"() {
        given:
        createPromotionConfigFile()
        createEnvironmentConfigFile()
        createPipelineConfigFile()
        createServiceConfigFile()
        createWebhookConfigFile()
        workFlow.loadConfigModels()
        String oldChecksum = app.configChecksum
        mdao.persist(_) >> _

        when:
        workFlow.loadConfigModels()

        then:
        app.configChecksum != null
        oldChecksum == app.configChecksum
    }

    def "Reload changed config from a directory and make sure it passes"() {
        given:
        createPromotionConfigFile()
        createEnvironmentConfigFile()
        createPipelineConfigFile()
        createServiceConfigFile()
        createWebhookConfigFile()
        workFlow.loadConfigModels()
        String oldChecksum = app.configChecksum
        createAnotherPromotionConfigFile()
        mdao.persist(_) >> _

        when:
        workFlow.loadConfigModels()

        then:
        app.configChecksum != null
        oldChecksum != app.configChecksum
    }
}