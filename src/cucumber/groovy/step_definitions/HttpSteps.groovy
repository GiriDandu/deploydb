
import cucumber.api.DataTable
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import deploydb.WorkFlow
import org.glassfish.jersey.client.JerseyInvocation
import org.joda.time.DateTime

// Add functions to register hooks and steps to this script.
this.metaClass.mixin(cucumber.api.groovy.Hooks)
this.metaClass.mixin(cucumber.api.groovy.EN)

When(~/^I GET "(.*?)" from the admin app$/) { String path ->
    response = getFromPath(path, true)
}

When(~/^I GET "(.*?)"$/) { String path ->
    response = getFromPath(path, false)
}

When(~/^I DELETE "(.*?)"$/) { String path ->
    response = deleteFromPath(path)
}

When(~/^I POST to "(.*?)" with:$/) { String path, String requestBody ->
    response = postJsonToPath(path, requestBody, false)
}

When(~/^I POST to "(.*?)" with a (.*?) over ([1-9][0-9]*) characters$/) { String path, String var, int varSize ->
    // Create a randomString of size varSize+1
    String randomString = "test-".padRight(varSize+1, "a")

    group = var == "group" ? randomString : "com.example.cucumber"
    name  = var == "name" ? randomString : "cukes"
    version = var == "version" ? randomString : "1.2.345"
    sourceUrl = var == "sourceUrl" ? randomString : "http://example.com/cucumber.jar"

    requestBody = """ {
        "group" : "$group",
        "name" : "$name",
        "version" : "$version",
        "sourceUrl" : "$sourceUrl"
      }
    """

    response = postJsonToPath(path, requestBody, false)
}

When(~/^I PATCH "(.*?)" with:$/) { String path, String requestBody ->
    response = patchJsonToPath(path, requestBody)
}

When(~/^I POST to "(.*?)"$/) { String path ->
    response = postJsonToPath(path, null, false)
}

When(~/^I POST to "(.*?)" from the admin app$/) { String path ->
    response = postJsonToPath(path, null, true)
}

When(~/^I GET "(.*?)" with custom headers:$/) { String path, DataTable headers ->
    JerseyInvocation.Builder builder = client.target(urlWithPort(path, false)).request()

    List<List<String>> rawHeaders = headers.raw()

    rawHeaders.subList(1, rawHeaders.size()).each { List<String> row ->
        builder.header(row[0] as String, row[1] as Object)
    }

    response = builder.build('GET', null).invoke()
}


Then(~/^the response should be (\d+)$/) { int statusCode ->
    assert response.status == statusCode
}

Then(~/^the response body should be:$/) { String expectedBody ->
    String responseBody = response.readEntity(String.class)
    assert responseBody.trim() == expectedBody.trim()
}

Then(~/^the body should be JSON:$/) { String expectedBody ->


    ObjectMapper mapper = new ObjectMapper()
    String body = response.readEntity(String.class)
    templateVariables = [
        'created_timestamp' : DateTime.now().withMillisOfSecond(0),
    ]
    expectedBody = processTemplate(expectedBody, templateVariables)

    JsonNode expectedNode = mapper.readTree(expectedBody)
    JsonNode bodyNode = mapper.readTree(body)

    assert bodyNode == expectedNode
}

Given(~/^Models configuration directory path is "(.*?)"$/) { String configDir ->
    setConfigDirectory(configDir)
}

Given(~/^Models configuration is reloaded from directory path "(.*?)"$/) { String configDir ->
    setConfigDirectory(configDir)
    withWorkFlow { WorkFlow workFlow ->
        withSession {
            workFlow.loadConfigModels()
        }
    }
}