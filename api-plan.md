Auth:
POST : /api/auth/login                         | login user and return JWT
POST : /api/auth/register                      | create user 

Movie-Management
/api/management/movies
POST:                                          | create new movie
GET :                                          | get page movies
GET: /{id}                                     | get information about a movie
GET: /filter/duration/{duration}               | get list of movies by duration
GET: /filter/genre/{genre}                     | get list of movies by genre
PATCH: /{id}                                   | update movie information
DELETE: /{id}                                  | delete movie

Cinema-room-Management
/api/management/rooms
POST:                                          | create cinema room
PATCH:/{id}                                    | update cinema room
GET:                                           | get page of rooms
GET: /{id}                                     | get information about a room
DELETE: /delete/{id}                           | delete room

Screenings-Management
/api/management/screening
GET:                                          | get page screenings
POST:                                         | create a new screening
GET: /{id}                                    | get a screening by id
GET: /{screeningId}/seats/free                | get list of free screening seats for a screening
PATCH: /{id}                                  | update a screening
DELETE: /{id}                                 | delete screening
GET: /upcoming                                | get a list of upcoming screenings - period = 1 month

Screenings-User
/api/screening
GET: /upcoming                               | get a list of upcoming screenings - period = 1 month
GET: /{screeningId}/seats/free               | get list of free screening seats for a screening

Reservation-User
/api/reservation
GET: /{id}                                   | get information about reservation
POST:                                        | create reservation
PATCH: /{id}/cancel                          | cancel a reservation

Reservation-Management
/api/management/reservation
GET:                                         | get page of reservations
POST:                                        | create reservation
GET: /{id}                                   | get information about reservation
PATCH: /{id}/cancel                          | cancel a reservation
PATCH: /{reservationId}/refund               | refund payment for an reservation
PATCH: /{reservationId}/complete/payment     | complete payment for a reservation
PATCH: /ticket/{ticketNumber}/used           | set ticket status to used
PATCH: /ticket/expire                        | set ticket status of all expired ticket to "expired"

