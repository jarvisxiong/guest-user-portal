package no.rogfk.guestuser.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import no.rogfk.guestuser.exception.GuestAllreadyRegisteredException;
import no.rogfk.guestuser.model.ErrorResponse;
import no.rogfk.guestuser.model.GuestUser;
import no.rogfk.guestuser.model.GuestUserCreateStatus;
import no.rogfk.guestuser.service.GuestUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@Api(tags = "GuestUser")
@CrossOrigin()
@RequestMapping(value = "/api/guest")
public class GuestUserController {

    @Autowired
    GuestUserService guestUserService;

    @ApiOperation("Request new guest user")
    @RequestMapping(value = "user",
            method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity createGuestUser(@ModelAttribute GuestUser guestUser,
                                                                 @RequestParam(value = "notifyHost", defaultValue = "false") Boolean notifyHost) {
        log.info("GuestUser: {}", guestUser);
        GuestUserCreateStatus guestUserCreateStatus = guestUserService.create(guestUser, notifyHost);
        if (guestUserCreateStatus != null) {
            return ResponseEntity.status(HttpStatus.CREATED).body(guestUserCreateStatus);
        }

        throw new GuestAllreadyRegisteredException("Du er allerede registrert som gjest.");


    }

    @ExceptionHandler(GuestAllreadyRegisteredException.class)
    public ResponseEntity handleGuestAllreadyRegisteredFound(Exception e) {
        return ResponseEntity.status(HttpStatus.FOUND).body(new ErrorResponse(e.getMessage()));
    }


}