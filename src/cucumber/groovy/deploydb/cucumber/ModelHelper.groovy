package deploydb.cucumber

import deploydb.models.Artifact
import deploydb.models.Deployment
import deploydb.models.Promotion.Promotion
import deploydb.models.Service
import deploydb.models.Webhook.Webhook
import deploydb.models.pipeline.Pipeline


class ModelHelper {

    Artifact sampleArtifact() {
        return  new Artifact('com.example.cucumber',
                             'cucumber-artifact',
                             '1.0.1',
                             'http://example.com/maven/com.example.cucumber/cucumber-artifact/1.0.1/cucumber-artifact-1.0.1.jar')
    }

    Artifact sampleArtifactV2() {
        return  new Artifact('com.example.cucumber',
                             'cucumber-artifact',
                             '1.0.2',
                             'http://example.com/maven/com.example.cucumber/cucumber-artifact/1.0.2/cucumber-artifact-1.0.2.jar')
    }

    /**
     * Creates a sample service object
     */
    Service sampleService1() {
        Service service = new Service('faas', 'Fun as a Service',
                                      [ 'com.github.lookout:foas',
                                        'com.example.cucumber:cukes',
                                        'com.github.lookout.puppet:puppet-foas',
                                        'com.github.lookout:puppet-mysql' ],
                                      [ 'devtoprod' ],
                                      [ 'status-check' ])
        return service
    }

    /**
     * Creates a sample promotion object
     */
    Promotion samplePromotion1() {
        return new Promotion('status-check',
                'deploydb.models.Promotion.BasicPromotionImpl',
                'Status Check for Fun as a Service')
    }

    Promotion samplePromotion2() {
        return new Promotion('jenkins-smoke',
                'deploydb.models.Promotion.BasicPromotionImpl',
                'jenkins-smoke for Fun as a Service')
    }

    Promotion sampleManualLDAPPromotion() {
        Promotion promotion = new Promotion("manual-promotion",
                "deploydb.models.Promotion.ManualLDAPPromotionImpl",
                "Manual Promotion smoke test")
        promotion.attributes = ["allowedGroup":"fox"]
        return promotion
    }

    /**
     * Creates a sample environment object
     */
    deploydb.models.Environment sampleEnvironment1() {
        Webhook webhook = new Webhook()
        deploydb.models.Environment environment =
            new deploydb.models.Environment('integ',
                'DeployDB Primary Integration',
                webhook)

        return environment
    }


    /**
     * Creates a sample environment object
     */
    deploydb.models.Environment sampleEnvironment2() {
        Webhook webhook = new Webhook()
        deploydb.models.Environment environment =
                new deploydb.models.Environment('prod',
                        'DeployDB Primary Integration',
                        webhook)

        return environment
    }


    /**
     * Creates a sample pipeline object
     */
    Pipeline samplePipeline1() {
         deploydb.models.pipeline.Environment  environment =
                 new deploydb.models.pipeline.Environment(["status-check"])

         Pipeline pipeline = new Pipeline('devtoprod',
                'Development to Production',
                ["integ" : environment ])

        return pipeline
    }

    /**
     * Creates a multi environment pipeline object
     */
    Pipeline samplePipeline2() {
        deploydb.models.pipeline.Environment  environment =
                new deploydb.models.pipeline.Environment(["status-check"])
        deploydb.models.pipeline.Environment  environment1 =
                new deploydb.models.pipeline.Environment(["jenkins-smoke"])

        Pipeline pipeline = new Pipeline('devtoprod',
                'Development to Production',
                ["integ" : environment, "prod": environment1 ])

        return pipeline
    }


    Deployment sampleDeployment(Artifact a, String env, deploydb.Status s) {
        deploydb.models.Deployment deployment = new deploydb.models.Deployment(a,
        env, "faas", s)
        return deployment
    }
}
