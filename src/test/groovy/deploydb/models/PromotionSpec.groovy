package deploydb.models

import deploydb.ModelLoader
import deploydb.auth.User
import deploydb.registry.ModelRegistry
import io.dropwizard.configuration.ConfigurationParsingException
import spock.lang.Ignore
import spock.lang.Specification

class PromotionSpec extends Specification {
    def "ensure Promotion object can be instantiated"() {
        when:
        def promotion = new Promotion()

        then:
        promotion instanceof Promotion
    }
}

class PromotionWithArgsSpec extends Specification {
    private final ModelRegistry<Promotion> promotionRegistry =
            new ModelRegistry<Promotion>()
    private final ModelLoader<Promotion> promotionLoader =
            new ModelLoader<Promotion>(Promotion.class)

    @Ignore
    def "Loading of valid basic promotion config content succeeds"() {
        given:
        Promotion promotion = promotionLoader.loadFromString("""
type: deploydb.models.Promotion.BasicPromotionImpl
description: Basic Promotion Smoke Test
""")
        promotion.ident = "basic"
        promotionRegistry.put(promotion.ident, promotion)

        expect:
        promotion.type == 'deploydb.models.Promotion.BasicPromotionImpl'
        promotion.description == "Basic Promotion Smoke Test"
        promotion.attributes == null
        promotionRegistry.get("basic") == promotion
        promotionRegistry.getAll()== [promotion]
        User user = new User("foo", new HashSet())
        promotion.validate(user) == true
    }

    @Ignore
    def "When allowedGroup matches with User authorized groups, then validation succeeds"() {
        given:
        Promotion promotion = promotionLoader.loadFromString("""
type: deploydb.models.Promotion.ManualLDAPPromotionImpl
description: Manual LDAP Promotion Smoke Test
attributes:
  allowedGroup : "fooGroup"
""")
        promotion.ident = "manualldap"
        promotionRegistry.put(promotion.ident, promotion)

        expect:
        promotion.type == 'deploydb.models.Promotion.ManualLDAPPromotionImpl'
        promotion.description == "Manual LDAP Promotion Smoke Test"
        promotion.attributes.size() == 1
        promotion.attributes["allowedGroup"] == "fooGroup"
        promotionRegistry.get("manualldap") == promotion
        promotionRegistry.getAll()== [promotion]
        User user = new User("foo", [ "fooGroup" ] as Set)
        promotion.validate(user) == true
    }

    @Ignore
    def "When allowedGroup is NOT part of User authorized groups, then validation fails"() {
        given:
        Promotion promotion = promotionLoader.loadFromString("""
type: deploydb.models.Promotion.ManualLDAPPromotionImpl
description: Manual LDAP Promotion Smoke Test
attributes:
  allowedGroup : "fooGroup"
""")
        expect:
        promotion.type == 'deploydb.models.Promotion.ManualLDAPPromotionImpl'
        promotion.attributes.size() == 1
        promotion.attributes["allowedGroup"] == "fooGroup"
        User user = new User("foo", [ "barGroup" ] as Set)
        promotion.validate(user) == false
    }

    @Ignore
    def "When no allowedGroup is configured, then validation fails"() {
        given:
        Promotion promotion = promotionLoader.loadFromString("""
type: deploydb.models.Promotion.ManualLDAPPromotionImpl
description: Manual LDAP Promotion Smoke Test
""")
        expect:
        promotion.type == 'deploydb.models.Promotion.ManualLDAPPromotionImpl'
        promotion.attributes == null
        User user = new User("foo", [ "fooGroup" ] as Set)
        promotion.validate(user) == false
    }

    @Ignore
    def "If multiple allowedGroup(s) are configured, last entry superscedes and validation fails"() {
        given:
        Promotion promotion = promotionLoader.loadFromString("""
type: deploydb.models.Promotion.ManualLDAPPromotionImpl
description: Manual LDAP Promotion Smoke Test
attributes:
  allowedGroup : "fooGroup"
  allowedGroup : "barGroup"
""")
        expect:
        promotion.type == 'deploydb.models.Promotion.ManualLDAPPromotionImpl'
        promotion.attributes.size() == 1
        promotion.attributes["allowedGroup"] == "barGroup"
        User user = new User("foo", [ "fooGroup" ] as Set)
        promotion.validate(user) == false
    }

    @Ignore
    def "The allowedGroup is configured, but authentication is not, hence validate fails"() {
        given:
        Promotion promotion = promotionLoader.loadFromString("""
type: deploydb.models.Promotion.ManualLDAPPromotionImpl
description: Manual LDAP Promotion Smoke Test
attributes:
  allowedGroup : "fooGroup"
""")
        expect:
        promotion.type == 'deploydb.models.Promotion.ManualLDAPPromotionImpl'
        promotion.attributes.size() == 1
        promotion.attributes["allowedGroup"] == "fooGroup"
        User user = null
        promotion.validate(user) == false
    }

    @Ignore
    def "Neither allowedGroup, nor authentication is configured, hence validate fails"() {
        given:
        Promotion promotion = promotionLoader.loadFromString("""
type: deploydb.models.Promotion.ManualLDAPPromotionImpl
description: Manual LDAP Promotion Smoke Test
""")
        expect:
        promotion.type == 'deploydb.models.Promotion.ManualLDAPPromotionImpl'
        promotion.attributes == null
        User user = null
        promotion.validate(user) == false
    }

    def "Loading of empty model promotion throws a parsing exception"(){
        when:
        Promotion promotion = promotionLoader.loadFromString("")

        then:
        thrown(ConfigurationParsingException)
    }

    def "Loading a malformed promotion throws a parsing exception"(){
        when:
        Promotion promotion = promotionLoader.loadFromString("type")

        then:
        thrown(ConfigurationParsingException)
    }

    def "Loading a empty model promotion config succeeds"(){
        given:
        Promotion promotion = promotionLoader.loadFromString("type:\n"+
                  "description:\n")

        expect:
        promotion.type == ""
        promotion.description == ""
    }

}
