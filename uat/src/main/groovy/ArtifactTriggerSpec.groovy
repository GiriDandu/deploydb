package uat

import spock.lang.*
import dropwizardintegtest.IntegrationModelHelper
import dropwizardintegtest.IntegrationRestApiClient

class ArtifactTriggerSpec extends Specification {

    IntegrationRestApiClient integrationRestApiClient = new IntegrationRestApiClient()
    IntegrationModelHelper integModelHelper = new IntegrationModelHelper(integrationRestApiClient)

    def setup() {
        //integrationRestApiClient.host = "http://10.32.10.63"
        integrationRestApiClient.host = "http://localhost"
    }

    def "create artifact should return success"() {

        when:
        boolean success = integModelHelper.sendCreateArtifact()

        then:
        success == true
    }

    def "read artifact should return success"() {
        when:
        boolean success = integModelHelper.sendGetApi("/api/artifacts/1")

        then:
        success == true
    }
}