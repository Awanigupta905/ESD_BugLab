package edu.iiitb.esdlab3.service;

import edu.iiitb.esdlab3.dto.CreateRegistrationRequest;
import edu.iiitb.esdlab3.entity.*;
import edu.iiitb.esdlab3.exception.*;
import edu.iiitb.esdlab3.repository.EventRepository;
import edu.iiitb.esdlab3.repository.RegistrationRepository;
import edu.iiitb.esdlab3.dto.TransferRegistrationRequest;
import edu.iiitb.esdlab3.dto.CheckInRequest;
import java.util.List;
import java.util.Locale;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RegistrationService {

    private final EventRepository eventRepository;
    private final RegistrationRepository registrationRepository;
    private final EventService eventService;

    @Transactional
    public Registration register(
            Long eventId,
            CreateRegistrationRequest request) {

        Event event = eventService.get(eventId);

        long activeCount = registrationRepository
                .countByEvent_IdAndStatus(
                        eventId,
                        RegistrationStatus.ACTIVE
                );

        if (activeCount >= event.getCapacity()) {
            throw new EventFullException();
        }

        String email = request.getStudentEmail()
                .trim()
                .toLowerCase(Locale.ROOT);

        boolean alreadyRegistered = registrationRepository
                .existsByEvent_IdAndStudentEmailIgnoreCaseAndStatus(
                        eventId,
                        email,
                        RegistrationStatus.ACTIVE
                );

        if (alreadyRegistered) {
            throw new DuplicateRegistrationException();
        }
        return registrationRepository.save(
                new Registration(
                        event,
                        email,
                        RegistrationStatus.ACTIVE
                )
        );
    }

    public List<Registration> activeForEvent(Long eventId) {
        eventService.get(eventId);
        return registrationRepository.findActiveByEventId(eventId);
    }

    @Transactional
    public void cancel(Long registrationId) {
        Registration registration = registrationRepository
                .findById(registrationId)
                .orElseThrow(
                        () -> new RegistrationNotFoundException(registrationId)
                );

        if (registration.getStatus() != RegistrationStatus.CANCELLED) {
            registration.setStatus(RegistrationStatus.CANCELLED);
        }
    }

    @Transactional
    public Registration transfer(Long registrationId, TransferRegistrationRequest request) {

        // 1. fetch
        Registration registration = registrationRepository.findById(registrationId)
                .orElseThrow(() -> new RegistrationNotFoundException(registrationId));

        // 2. must be active
        if (registration.getStatus() == RegistrationStatus.CANCELLED) {
            throw new RegistrationConflictException();
        }

        // 3. ownership (case-insensitive)
        String email = request.getStudentEmail().trim();
        if (!email.equalsIgnoreCase(registration.getStudentEmail())) {
            throw new UnauthorizedOperationException();
        }

        // 4. target event must exist
        Event target = eventService.get(request.getTargetEventId());

        // optional edge case: transferring to the same event
        if (target.getId().equals(registration.getEvent().getId())) {
            throw new DuplicateRegistrationException();
        }

        // 5. capacity
        long activeCount = registrationRepository
                .countByEvent_IdAndStatus(target.getId(), RegistrationStatus.ACTIVE);
        if (activeCount >= target.getCapacity()) {
            throw new EventFullException();
        }

        // 6. duplicate on target
        if (registrationRepository.existsByEvent_IdAndStudentEmailIgnoreCaseAndStatus(
                target.getId(), email, RegistrationStatus.ACTIVE)) {
            throw new DuplicateRegistrationException();
        }

        // 7. atomic swap
        registration.setStatus(RegistrationStatus.CANCELLED);
        return registrationRepository.save(
                new Registration(target, registration.getStudentEmail(), RegistrationStatus.ACTIVE));
    }
    @Transactional
    public Registration checkIn(Long eventId, CheckInRequest request) {

        eventService.get(eventId);   // 404 if event missing

        String email = request.getStudentEmail().trim();

        Registration registration = registrationRepository
                .findByEvent_IdAndStudentEmailIgnoreCaseAndStatus(
                        eventId, email, RegistrationStatus.ACTIVE)
                .orElseThrow(() -> new RegistrationNotFoundException(eventId, email));

        if (registration.isCheckedIn()) {
            throw new AlreadyCheckedInException();
        }

        registration.markCheckedIn();
        return registrationRepository.save(registration);
    }
}