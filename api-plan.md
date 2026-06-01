Auth:
POST : /api/auth/login                                   | login user and return JWT
POST : /api/auth/register                                | create user 

Movie:
POST: /api/movie                                         | create new movie
GET : /api/movie                                         | get page movies
GET: /api/movie/{id}                                     | get information about a movie
GET: /api/movie/filter/duration/{duration}               | get list of movies by duration
GET: /api/movie//filter/genre/{genre}                    | get list of movies by genre
PATCH: /api/movie/{id}                                   | update movie information
DELETE: /api/movie/{id}                                  | delete movie

Cinema room
POST: /api/room                                          | create cinema room
PATCH: /api/room/update/{id}                             | update cinema room
GET: /api/room                                           | get page of rooms
GET: /api/room/{id}                                      | get information about a room
DELETE: /api/room/delete/{id}                            | delete room

Screenings
GET: /api/screening                                      | get page screenings
POST: /api/screening                                     | create a new screening
GET: /api/screening/{id}                                 | get a screening by id
GET: /api/screening/{screeningId}/seats/free             | get list of free screening seats for a screening
PATCH: /api/screenings/{id}                              | update a screening
DELETE: /api/screenings/{id}                             | delete screening

Reservation
GET: /api/reservation/{id}                               | get information about reservation
POST: /api/reservation                                   | create reservation
PATCH: /api/reservation/{id}/cancel                      | cancel a reservation

Reservation Management 
GET: /api/management/reservation                         | get page of reservations
GET: /api/reservation/{id}                               | get information about reservation
PATCH: /api/reservation/{id}/cancel                      | cancel a reservation
PATCH: /api/reservation/{reservationId}/refund           | refund payment for an reservation
PATCH: /api/reservation/{reservationId}/complete/payment | complete payment for a reservation
PATCH: /api/reservation/ticket/{ticketNumber}/used       | set ticket status to used
PATCH: /api/reservation/ticket/expire                    | set ticket status of all expired ticket to "expired"

