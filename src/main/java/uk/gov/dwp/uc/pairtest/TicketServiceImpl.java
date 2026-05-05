package uk.gov.dwp.uc.pairtest;

import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;
import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;
import thirdparty.paymentgateway.TicketPaymentService;
import thirdparty.seatbooking.SeatReservationService;

/**
 * Implementation of Ticketservice class that handle tuicket purchase operations
 * validation of input requests,enforces business rules,calculating payments and seat reservations.
 * Throws InvalidPurchaseException for any invalid scenarios before invoking external systems
 */
public class TicketServiceImpl implements TicketService {

    private static final int MAX_TICKETS_ALLOWED = 25;
    private static final int ADULT_TICKET_PRICE = 25;
    private static final int CHILD_TICKET_PRICE = 15;

    private final TicketPaymentService paymentService;
    private final SeatReservationService reservationService;

    public TicketServiceImpl(TicketPaymentService paymentService,
                             SeatReservationService reservationService) {
        this.paymentService = paymentService;
        this.reservationService = reservationService;
    }

    @Override
    public void purchaseTickets(Long accountId, TicketTypeRequest... requests) {

        validateInput(accountId, requests);

        int totalTickets = 0, adults = 0, children = 0, infants = 0;
        int totalAmount = 0, totalSeats = 0;

        for (TicketTypeRequest req : requests) {
            int count = req.getNoOfTickets();
            totalTickets += count;

            switch (req.getTicketType()) {
                case ADULT:
                    adults += count;
                    totalAmount += count * ADULT_TICKET_PRICE;
                    totalSeats += count;
                    break;
                case CHILD:
                    children += count;
                    totalAmount += count * CHILD_TICKET_PRICE;
                    totalSeats += count;
                    break;
                case INFANT:
                    infants += count;
                    break;
            }
        }

        validateBusinessRules(totalTickets, adults, children, infants);

        paymentService.makePayment(accountId, totalAmount);
        reservationService.reserveSeat(accountId, totalSeats);
    }

    private void validateInput(Long accountId, TicketTypeRequest... requests) {
        if (accountId == null || accountId <= 0) {
            throw new InvalidPurchaseException("Invalid account ID");
        }

        if (requests == null || requests.length == 0) {
            throw new InvalidPurchaseException("No ticket requests provided");
        }

        for (TicketTypeRequest req : requests) {
            if (req == null || req.getNoOfTickets() <= 0) {
                throw new InvalidPurchaseException("Invalid ticket request");
            }
        }
    }

    private void validateBusinessRules(int totalTickets, int adults, int children, int infants) {
        if (totalTickets > MAX_TICKETS_ALLOWED) {
            throw new InvalidPurchaseException("Maximum 25 tickets allowed");
        }

        if (adults == 0 && (children > 0 || infants > 0)) {
            throw new InvalidPurchaseException("Adult ticket required");
        }
    }
}