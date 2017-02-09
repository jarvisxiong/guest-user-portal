package no.rogfk.guestuser.model;

import lombok.Data;

@Data
public class GuestUserCreateStatus {

    private String guestMessage;
    private String hostMessage;
    private String errorMessage;
    private NotifyStatus hostNotifyStatus;
    private NotifyStatus guestNotifyStatus;
    int timeout = 0;
}
