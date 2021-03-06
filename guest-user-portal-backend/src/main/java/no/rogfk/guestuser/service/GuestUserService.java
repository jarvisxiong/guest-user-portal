package no.rogfk.guestuser.service;

import no.rogfk.guestuser.model.GuestUser;
import no.rogfk.guestuser.model.GuestUserCreateStatus;
import no.rogfk.guestuser.model.NotifyStatus;
import no.rogfk.sms.SmsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.support.LdapNameBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;

import javax.naming.directory.SearchControls;
import java.util.List;

@Component
public class GuestUserService {

    @Autowired
    ConfigService configService;

    @Autowired
    GuestUserObjectService guestUserObjectService;
    @Autowired
    EmployeeNotifyService employeeNotifyService;
    @Autowired
    private LdapTemplate ldapTemplate;
    @Autowired
    private SmsService smsService;
    private SearchControls searchControls;


    public GuestUserService() {

        searchControls = new SearchControls();
        searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);
    }

    public GuestUserCreateStatus create(GuestUser guestUser, boolean notifyHost, boolean notifyGuest) {
        GuestUserCreateStatus guestUserCreateStatus = new GuestUserCreateStatus();

        guestUserObjectService.setupTodaysGuestUser(guestUser);

        if (!exists(guestUser.getDn())) {
            ldapTemplate.create(guestUser);
        } else {
            return null;
        }

        if (notifyGuest) {
            notifyGuest(guestUser, guestUserCreateStatus);
        }

        if (notifyHost) {
            String guestFullname = String.format("%s %s", guestUser.getFirstName(), guestUser.getLastName());
            employeeNotifyService.notifyEmployee(guestUser.getOwner(), guestFullname, guestUserCreateStatus);
        } else {
            guestUserCreateStatus.setHostNotifyStatus(NotifyStatus.NO_NOTIFICATION_NEED);
        }
        return guestUserCreateStatus;
    }

    private void notifyGuest(GuestUser guestUser, GuestUserCreateStatus guestUserCreateStatus) {
        String message = String.format(configService.getGuestMessage(), guestUser.getCn(), guestUser.getPassword());
        try {
            String notifyGuestResponse = smsService.sendSms(message, guestUser.getMobile());

            if (notifyGuestResponse.contains(">true<")) {
                guestUserCreateStatus.setGuestNotifyStatus(NotifyStatus.NOTIFIED);
                guestUserCreateStatus.setGuestMessage(configService.getGuestNotifiedMessage());
            } else {
                guestUserCreateStatus.setGuestNotifyStatus(NotifyStatus.UNABLE_TO_NOTIFY);
                guestUserCreateStatus.setGuestMessage(configService.getUnableToNotifyGuestMessage());
            }
        } catch (ResourceAccessException e) {
            guestUserCreateStatus.setGuestNotifyStatus(NotifyStatus.UNABLE_TO_NOTIFY);
            guestUserCreateStatus.setGuestMessage(configService.getUnableToNotifyGuestMessage());
        }

    }


    public boolean update(GuestUser guestUser) {
        if (exists(guestUser.getDn())) {
            ldapTemplate.update(guestUser);
            return true;
        }
        return false;
    }

    public GuestUser historizeGuestUser(GuestUser guestUser) {

        GuestUser histGuestUser = GuestUser.newInstance(guestUser);

        guestUserObjectService.setupHistoricalGuestUser(histGuestUser);

        ldapTemplate.create(histGuestUser);
        ldapTemplate.delete(guestUser);

        return histGuestUser;
    }

    public List<GuestUser> getTodaysGuests() {
        return ldapTemplate.findAll(configService.getTodaysGuestBase(), searchControls, GuestUser.class);
    }

    public List<GuestUser> getHistoricalGuests() {
        return ldapTemplate.findAll(configService.getHistGuestBase(), searchControls, GuestUser.class);
    }

    public List<GuestUser> getAllGuests() {
        return ldapTemplate.findAll(configService.getGuestBase(), searchControls, GuestUser.class);
    }

    public boolean exists(String dn) {
        try {
            ldapTemplate.lookup(LdapNameBuilder.newInstance(dn).build());
            return true;
        } catch (org.springframework.ldap.NamingException e) {
            return false;
        }
    }
}
