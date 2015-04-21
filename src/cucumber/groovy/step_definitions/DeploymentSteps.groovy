this.metaClass.mixin(cucumber.api.groovy.EN)

import deploydb.dao.ArtifactDAO
import deploydb.dao.DeploymentDAO
import deploydb.dao.FlowDAO
import deploydb.models.Artifact
import deploydb.models.Deployment
import deploydb.models.Flow
import deploydb.models.PromotionResult
import deploydb.Status


Given(~/^there is a deployment$/) { ->
    withSession {

        /**
         * Create sample artifact
         */
        ArtifactDAO adao = new ArtifactDAO(sessionFactory)
        Artifact a1 = sampleArtifact()
        adao.persist(a1)

        /**
         * Create sample promotionResult(s)
         */
        PromotionResult p1 = new PromotionResult("jenkins-smoke", Status.STARTED, null)

        /**
         * Create deployment
         */
        Deployment d1 = sampleDeployment(a1, "pre-prod",Status.STARTED)
        d1.addPromotionResult(p1)

        /* Create a flow */
        Flow f = new Flow(a1, "faas", "0xdead")
        f.addDeployment(d1)

        /**
         * Save flow in DB, which will save the deployments & promotionResults as well
         */
        FlowDAO fdao = new FlowDAO(sessionFactory)
        fdao.persist(f)
    }
}

Given(~/^there are deployments$/) { ->
    withSession {

        /**
         * Create sample artifact
         */
        ArtifactDAO adao = new ArtifactDAO(sessionFactory)
        Artifact a1 = sampleArtifact()
        Artifact a2 = sampleArtifactV2()
        adao.persist(a1)
        adao.persist(a2)

        /**
         * Create sample promotionResult(s)
         */
        PromotionResult p1 = new PromotionResult("jenkins-smoke", Status.STARTED, null)
        PromotionResult p2 = new PromotionResult("status-check", Status.STARTED,
                "http://local.lookout.com/jenkins/job-id/2/results")

        /**
         * Create deployment
         */
        Deployment d1 = sampleDeployment(adao.persist(a1), "pre-prod", Status.STARTED)
        d1.addPromotionResult(p1)
        Deployment d2 = sampleDeployment(adao.persist(a2), "pre-prod", Status.STARTED)
        d2.addPromotionResult(p2)

        /**
         * Save deployment in DB, which will save the promotionResults as well
         */
        DeploymentDAO dao = new DeploymentDAO(sessionFactory)
        dao.persist(d1)
        dao.persist(d2)
    }
}

Given(~/^there are deployments for artifacts$/) { ->
    withSession {

        /**
         * Create sample artifact
         */
        ArtifactDAO adao = new ArtifactDAO(sessionFactory)
        Artifact a1 = sampleArtifact()
        Artifact a2 = sampleArtifactV2()
        adao.persist(a1)
        adao.persist(a2)

        List<Deployment> deployments = []
        [a1, a2].each { Artifact artifact ->
            ['dev-integ', 'integ', 'pre-prod', 'prod'].each { String env ->
                deployments << sampleDeployment(adao.persist(artifact), env, Status.STARTED)
            }
        }

        DeploymentDAO dao = new DeploymentDAO(sessionFactory)
        deployments.each { dao.persist(it) }
    }
}

When(~/I trigger deployment PATCH with:$/) { String path ->
    response = postJsonToPath(path, requestBody, false, null)
}

And(~/there is a deployment in "(.*?)" state$/) { String deploymentState ->

    withSession {

        /**
         * Create sample artifact
         */
        ArtifactDAO adao = new ArtifactDAO(sessionFactory)
        Artifact a1 = sampleArtifact()
        adao.persist(a1)

        /**
         * Create sample promotionResult(s)
         */
        PromotionResult p1 = new PromotionResult("jenkins-smoke", Status.STARTED, null)
        /**
         * Create deployment
         */
        Deployment d1 = sampleDeployment(a1, "pre-prod", Status."${deploymentState}")
        d1.addPromotionResult(p1)

        /* Create a flow */
        Flow f = new Flow(a1, "faas", "0xdead")
        f.addDeployment(d1)

        /**
         * Save flow in DB, which will save the deployments & promotionResults as well
         */
        FlowDAO fdao = new FlowDAO(sessionFactory)
        fdao.persist(f)
    }
}

And(~/the first deployments is in "(.*?)" state$/) { String deploymentState ->

    withSession {

        /**
         * Create sample artifact
         */
        ArtifactDAO adao = new ArtifactDAO(sessionFactory)
        Artifact a1 = sampleArtifact()
        adao.persist(a1)

        /**
         * Create sample promotionResult(s)
         */
        PromotionResult p1 = new PromotionResult("status-check", Status.STARTED, null)
        PromotionResult p2 = new PromotionResult("jenkins-smoke", Status.STARTED, null)
        /**
         * Create deployments
         */
        Deployment d1 = sampleDeployment(a1, "integ", Status."${deploymentState}")
        d1.addPromotionResult(p1)

        Deployment d2 = sampleDeployment(a1, "prod", Status.NOT_STARTED)
        d2.addPromotionResult(p2)

        /* Create a flow */
        Flow f = new Flow(a1, "faas", "0xdead")
        f.addDeployment(d1)
        f.addDeployment(d2)

        /**
         * Save flow in DB, which will save the deployments & promotionResults as well
         */
        FlowDAO fdao = new FlowDAO(sessionFactory)
        fdao.persist(f)
    }
}

Given(~/^there is a deployment with manual LDAP promotion$/) { ->
    withSession {

        /**
         * Create sample artifact
         */
        ArtifactDAO adao = new ArtifactDAO(sessionFactory)
        Artifact a1 = sampleArtifact()
        adao.persist(a1)

        /**
         * Create sample promotion & promotionResult(s)
         */
        PromotionResult p1 = new PromotionResult("manual-promotion", Status.STARTED, null)

        /**
         * Create deployment
         */
        Deployment d1 = sampleDeployment(a1, "pre-prod",Status.COMPLETED)
        d1.addPromotionResult(p1)

        /* Create a flow */
        Flow f = new Flow(a1, "faas", "0xdead")
        f.addDeployment(d1)

        /**
         * Save flow in DB, which will save the deployments & promotionResults as well
         */
        FlowDAO fdao = new FlowDAO(sessionFactory)
        fdao.persist(f)
    }
}