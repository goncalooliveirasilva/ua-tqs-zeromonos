Feature: Booking Management
  As a citizen or staff member
  I want to manage bookings
  So that I can create, view, and update bookings

  Background:
      Given the user is on the homepage

  Scenario: Citizen creates a new account
      Given I am on the registration page
      When I register with the following details:
          | username | testuser           |
          | email    | testuser@example.com |
          | password | pass123       |
      Then my account should be created successfully
      And I should be logged in


  Scenario: Citizen creates a new booking successfully
      Given I am logged in as a citizen
      When I create a booking with the following details:
          | district      | Lisbon              |
          | municipality  | Lisbon              |
          | village       | Chiado              |
          | postalCode    | 1000-100            |
          | date          | 2025-11-10          |
          | time          | 10:00               |
          | description   | Collect electronics |
      Then the booking should be created successfully
      And I should receive a booking token


  Scenario: Retrieve booking details using token
      Given I have a valid booking token
      When I request the booking details using the token
      Then I should see the booking information including:
          | district      |
          | municipality  |
          | village       |
          | postalCode    |
          | date          |
          | time          |
          | description   |
          | state         |


  Scenario: Staff filters bookings by district and municipality
      Given I am logged in as a staff member
      And there are existing bookings in the system
      When I filter bookings by district "Lisbon" and municipality "Lisbon"
      Then I should see only bookings matching the selected district and municipality


  Scenario: Staff updates a booking state
      Given I am logged in as a staff member
      And a booking exists with state "RECEIVED"
      When I update the booking state to "ASSIGNED"
      Then the booking state should be updated to "ASSIGNED"
      And the booking history should include a record for this state change
