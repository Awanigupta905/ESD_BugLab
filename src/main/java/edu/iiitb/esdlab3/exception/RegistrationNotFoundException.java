package edu.iiitb.esdlab3.exception;

public class RegistrationNotFoundException extends RuntimeException {

    public RegistrationNotFoundException(Long id) {
        super("Registration not found: " + id);
    }
    public RegistrationNotFoundException(Long eventId, String studentEmail) {
        super("Registration not found for event " + eventId + " and student " + studentEmail);
    }
}