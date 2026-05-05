package uk.gov.dwp.uc.pairtest.domain;

public final class TicketTypeRequest {

    public enum Type {
        ADULT, CHILD, INFANT
    }

    private final Type ticketType;
    private final int noOfTickets;

    public TicketTypeRequest(Type ticketType, int noOfTickets) {
        this.ticketType = ticketType;
        this.noOfTickets = noOfTickets;
    }

    public Type getTicketType() {
        return ticketType;
    }

    public int getNoOfTickets() {
        return noOfTickets;
    }
}