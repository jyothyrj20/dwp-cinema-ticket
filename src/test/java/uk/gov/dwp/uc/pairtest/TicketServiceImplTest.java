package uk.gov.dwp.uc.pairtest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import thirdparty.paymentgateway.TicketPaymentService;
import thirdparty.seatbooking.SeatReservationService;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;
import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TicketServiceImplTest {

    private TicketServiceImpl service;
    @Mock
    private TicketPaymentService paymentService;

    @Mock
    private SeatReservationService reservationService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new TicketServiceImpl(paymentService, reservationService);
    }

//___________________Positive scenarios(Happy Path)----------------------

    @Test
    void shouldProcessAdultTicketsSuccessfully() {
        // Given: Valid request with only adult tickets
        service.purchaseTickets(1L,
                new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 2));

        // Then: Correct payment and seat reservation
        verify(paymentService).makePayment(1L, 50);
        verify(reservationService).reserveSeat(1L, 2);
    }

    @Test
    void shouldProcessMixedTicketsCorrectly() {
        // given: Adult + Child + Infant tickets
        service.purchaseTickets(1L,
                new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 2),
                new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 1),
                new TicketTypeRequest(TicketTypeRequest.Type.INFANT, 1));

        // Then: Payment excludes infants, seats exclude infants
        verify(paymentService).makePayment(1L, 65);
        verify(reservationService).reserveSeat(1L, 3);
    }

//---------------------Negative scenarios ---------------------------

    @Test
    void shouldThrowExceptionForInvalidAccountId() {
        // Given: Invalid account ID
        assertThrows(InvalidPurchaseException.class, () ->
                service.purchaseTickets(0L,
                        new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 1)));
    }

    @Test
    void shouldThrowExceptionForNullRequests() {
        // Given: null ticket requests
        assertThrows(InvalidPurchaseException.class, () ->
                service.purchaseTickets(1L, (TicketTypeRequest[]) null));
    }

    @Test
    void shouldThrowExceptionForEmptyRequests() {
        // Given: No ticket requests
        assertThrows(InvalidPurchaseException.class, () ->
                service.purchaseTickets(1L));
    }

    @Test
    void shouldThrowExceptionForNullRequest() {
        // Given: One of the request  is null
        assertThrows(InvalidPurchaseException.class, () ->
                service.purchaseTickets(1L, (TicketTypeRequest) null));
    }

    @Test
    void shouldThrowExceptionForNegativeTicketCount() {
        // Given: Invalid negative ticket count
        assertThrows(InvalidPurchaseException.class, () ->
                service.purchaseTickets(1L,
                        new TicketTypeRequest(TicketTypeRequest.Type.ADULT, -1)));
    }

//-------------Business Rule Validations-----------------------------

    @Test
    void shouldThrowExceptionWhenNoAdultWithChild() {
        // Rule: Child tickets cannot be purchased without an adult
        assertThrows(InvalidPurchaseException.class, () ->
                service.purchaseTickets(1L,
                        new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 2)));
    }

    @Test
    void shouldThrowExceptionWhenNoAdultWithInfant() {
        // Rule: Infant tickets cannot be purchased without an adult
        assertThrows(InvalidPurchaseException.class, () ->
                service.purchaseTickets(1L,
                        new TicketTypeRequest(TicketTypeRequest.Type.INFANT, 1)));
    }

 //_________________Boundary Test Cases-------------------

    @Test
    void shouldAllowExactly25Tickets() {
        // Boundary: Maximum allowed tickets (25)
        service.purchaseTickets(1L,
                new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 25));

        verify(paymentService).makePayment(1L, 625);
        verify(reservationService).reserveSeat(1L, 25);
    }

    @Test
    void shouldThrowExceptionWhenMoreThan25Tickets() {
        // Boundary: Exceeding maximum limit (26)
        assertThrows(InvalidPurchaseException.class, () ->
                service.purchaseTickets(1L,
                        new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 26)));
    }

//------------------Edge Cases---------------------------------

    @Test
    void shouldNotReserveSeatForInfants() {
        // Edge: Infants should not be counted in seat reservation
        service.purchaseTickets(1L,
                new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 1),
                new TicketTypeRequest(TicketTypeRequest.Type.INFANT, 2));

        verify(paymentService).makePayment(1L, 25);
        verify(reservationService).reserveSeat(1L, 1);

    }

//-------------interaction test ---------------------------
    @Test
    void shouldNotCallServicesWhenValidationFails() {
        // Given: Invalid request (no adult)
        assertThrows(InvalidPurchaseException.class, () ->
                service.purchaseTickets(1L,
                        new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 1)));

        // Then: External services should NOT be invoked
        verifyNoInteractions(paymentService);
        verifyNoInteractions(reservationService);
    }
}