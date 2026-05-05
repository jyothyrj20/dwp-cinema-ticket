# dwp-cinema-ticket
# Ticket Service - DWP Coding Exercise

## Overview

This project implements a ticket purchasing service based on given business rules. It validates ticket requests, calculates total cost, and interacts with external services for payment and seat reservation.

## Features

* Supports Adult, Child, and Infant tickets
* Enforces business constraints:

  * Maximum 25 tickets per purchase
  * Adult required for Child/Infant tickets
  * Infants are free and do not require seats
* Clean validation and separation of concerns
* Unit tested using JUnit and Mockito

## Design Approach

* Input validation is separated from business logic for clarity
* Ticket aggregation is handled in a single pass for efficiency
* External dependencies are mocked in tests to ensure isolation

## Technologies Used

* Java
* JUnit 5
* Mockito

## How to Run

1. Clone the repository
2. Run tests:

  bash
   mvn test


## Assumptions

* Account IDs > 0 are valid
* External services always succeed as per specification

## Future Improvements

* Externalize pricing configuration
* Add integration tests
* Introduce logging and monitoring

## Author
Jyothy Rajan

[Your Name]
