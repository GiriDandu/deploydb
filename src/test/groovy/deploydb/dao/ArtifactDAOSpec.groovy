package deploydb.dao

import spock.lang.*

import io.dropwizard.hibernate.AbstractDAO
import org.hibernate.SessionFactory
import org.hibernate.Criteria

import deploydb.models.Artifact

class ArtifactDAOSpec extends Specification {
    private ArtifactDAO dao
    private SessionFactory sessionFactory = Mock(SessionFactory)

    def "ensure the constructor works"() {
        given:
        ArtifactDAO dao

        when:
        dao = new ArtifactDAO(sessionFactory)

        then:
        dao instanceof AbstractDAO
    }

    def "findByGroupAndName() should return null if there are no results"() {
        given:
        ArtifactDAO dao = Spy(ArtifactDAO, constructorArgs: [sessionFactory])
        def criteria = Mock(Criteria)
        criteria./add|set|addOrder|setMaxResults|setFirstResult/(_) >> criteria
        _ * dao.criteria() >> criteria
        1 * criteria.list() >> []

        expect:
        dao.findByGroupAndName('spock-group', 'spock-name', 1, 5).isEmpty()
    }

    def "artifactExists() should return false if there are no artifacts"() {
        given:
        ArtifactDAO dao = Spy(ArtifactDAO, constructorArgs: [sessionFactory])
        def criteria = Mock(Criteria)
        criteria./add|set|addOrder|setMaxResults|setFirstResult/(_) >> criteria
        _ * dao.criteria() >> criteria
        1 * criteria.uniqueResult() >> null

        expect:
        false == dao.artifactExists('spock-group', 'spock-name', '1.1.1')

    }

    def "artifactExists() should return true if there is artifact"() {
        given:
        Artifact a = new Artifact('spock-group', 'spock-name', '1.1.1', '')
        ArtifactDAO dao = Spy(ArtifactDAO, constructorArgs: [sessionFactory])
        def criteria = Mock(Criteria)
        criteria./add|set|addOrder|setMaxResults|setFirstResult/(_) >> criteria
        _ * dao.criteria() >> criteria
        1 * criteria.uniqueResult() >> a

        expect:
        true == dao.artifactExists('spock-group', 'spock-name', '1.1.1')

    }

}
