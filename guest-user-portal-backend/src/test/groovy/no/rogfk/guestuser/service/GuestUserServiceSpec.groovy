package no.rogfk.guestuser.service

import no.rogfk.guestuser.model.GuestUser
import no.rogfk.sms.SmsService
import org.springframework.ldap.NameNotFoundException
import org.springframework.ldap.core.LdapTemplate
import org.springframework.ldap.support.LdapNameBuilder
import spock.lang.Specification

import javax.naming.Name
import javax.naming.directory.SearchControls

class GuestUserServiceSpec extends Specification {
    private configService
    private guestUserObjectService
    private ldapTemplate
    private guestUserService
    private passwordService
    private smsService

    void setup() {
        configService = new ConfigService(guestMessage: "hello", hostMessage: "hello", todaysGuestContainer: "o=gjest", histGuestContainer: "o=hist", guestBaseContainer: "o=guest")
        smsService = Mock(SmsService)
        passwordService = new PasswordService()
        guestUserObjectService = new GuestUserObjectService(configService: configService, passwordService: passwordService,)
        ldapTemplate = Mock(LdapTemplate)
        guestUserService = new GuestUserService(configService: configService,
                guestUserObjectService: guestUserObjectService,
                smsService: smsService,
                ldapTemplate: ldapTemplate)
    }

    def "Create GuestUser"() {
        given:
        def guestUser1 = new GuestUser(mobile: "00000000")
        def guestUser2 = new GuestUser(mobile: "11111111")

        when:
        def created1 = guestUserService.create(guestUser1, false, true)
        def created2 = guestUserService.create(guestUser2, false, false)

        then:
        created1 != null
        created2 == null
        ldapTemplate.lookup(_ as Name) >> { throw new NameNotFoundException("test") } >> null
        1 * ldapTemplate.create(_ as GuestUser)
        1 * smsService.sendSms(_ as String, _ as String) >> ">true<"
    }

    def "Update GuestUser"() {
        given:
        def guestUser1 = new GuestUser(dn: LdapNameBuilder.newInstance("cn=dn1").build())
        def guestUser2 = new GuestUser(dn: LdapNameBuilder.newInstance("cn=dn2").build())

        when:
        def updated1 = guestUserService.update(guestUser1)
        def updated2 = guestUserService.update(guestUser2)

        then:
        updated1 == false
        updated2 == true
        ldapTemplate.lookup(_ as Name) >> { throw new NameNotFoundException("test") } >> null
        1 * ldapTemplate.update(_ as GuestUser)
    }

    def "Historize GuestUser"() {
        given:
        def guestUser = new GuestUser(dn: LdapNameBuilder.newInstance("cn=9999,o=guest").build())

        when:
        def histGuestUser = guestUserService.historizeGuestUser(guestUser)

        then:
        histGuestUser.loginDisabled == true
        histGuestUser.cn != null
        histGuestUser.dn.contains("cn=9999") == false
        1 * ldapTemplate.create(_ as GuestUser)
        1 * ldapTemplate.delete(_ as GuestUser)
    }

    def "Get todays guests"() {
        when:
        def all = guestUserService.getTodaysGuests()

        then:
        all.size() == 2
        1 * ldapTemplate.findAll(_ as Name, _ as SearchControls, _ as Object) >> Arrays.asList(new GuestUser(), new GuestUser())
    }

    def "Get historical guests"() {
        when:
        def all = guestUserService.getHistoricalGuests()

        then:
        all.size() == 2
        1 * ldapTemplate.findAll(_ as Name, _ as SearchControls, _ as Object) >> Arrays.asList(new GuestUser(), new GuestUser())
    }

    def "Get all guests"() {
        when:
        def all = guestUserService.getAllGuests()

        then:
        all.size() == 2
        1 * ldapTemplate.findAll(_ as Name, _ as SearchControls, _ as Object) >> Arrays.asList(new GuestUser(), new GuestUser())
    }

    def "GuestUser exists"() {
        given:
        true

        when:
        def exists1 = guestUserService.exists("o=rfk")
        def exists2 = guestUserService.exists("o=rfk")

        then:
        exists1 == false
        exists2 == true
        2 * ldapTemplate.lookup(_ as Name) >> { throw new NameNotFoundException("test") } >> null

    }
}
