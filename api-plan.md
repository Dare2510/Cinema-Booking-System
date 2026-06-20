Auth:
POST : /api/auth/login                         | login user and return JWT

User-Management(For staff and admin)
/api/management/user
GET:                                           | get page of users
POST: /register                                | register new user
POST: /register/{role}                         | admin user can register new user with any role
PATCH: /{userId}/update                        | update user information
DELETE: /{userId}/delete                       | delete users

User(for customers)
/api/user
POST: /register                                | create account
PATCH: /{password}/update                      | update account information
DELETE:/{password}/delete                      | delete account

Movie-Management(For staff and admin)
/api/management/movies
POST:                                          | create new movie
GET :                                          | get page movies
GET: /{id}                                     | get information about a movie
GET: /filter/duration/{duration}               | get list of movies by duration
GET: /filter/genre/{genre}                     | get list of movies by genre
PATCH: /{id}                                   | update movie information
DELETE: /{id}                                  | delete movie

Cinema-room-Management(For staff and admin)
/api/management/rooms
POST:                                          | create cinema room
PATCH:/{id}                                    | update cinema room
GET:                                           | get page of rooms
GET: /{id}                                     | get information about a room
DELETE: /delete/{id}                           | delete room

Screenings-Management(For staff and admin)
/api/management/screening
GET:                                          | get page screenings
POST:                                         | create a new screening
GET: /{id}                                    | get a screening by id
GET: /{screeningId}/seats/free                | get list of free screening seats for a screening
PATCH: /{id}                                  | update a screening
DELETE: /{id}                                 | delete screening
GET: /upcoming                                | get a list of upcoming screenings - period = 1 month

Screenings(for customers)
/api/screening
GET: /upcoming                               | get a list of upcoming screenings - period = 1 month
GET: /{screeningId}/seats/free               | get list of free screening seats for a screening

Reservation-User(for customers)
/api/reservation
GET: /{id}                                   | get information about reservation
POST:                                        | create reservation
PATCH: /{id}/cancel                          | cancel a reservation

Reservation-Management(For staff and admin)
/api/management/reservation
GET:                                         | get page of reservations
POST:                                        | create reservation
GET: /{id}                                   | get information about reservation
PATCH: /{id}/cancel                          | cancel a reservation
PATCH: /{reservationId}/refund               | refund payment for a reservation
PATCH: /{reservationId}/complete/payment     | complete payment for a reservation
PATCH: /ticket/{ticketNumber}/used           | set ticket status to used
PATCH: /ticket/expire                        | set ticket status of all expired ticket to "expired"

