Auth:
POST : /api/auth/login    | login user and return JWT
POST : /api/auth/register | create user 

Movies:
GET : /api/movies                 | get list of all movies
GET : /api/movies?movieGenre=...  | get a filtered list of movies
GET: /api/movies/{id}             | get information about a movie
POST: /api/movies                 | create new movie
PATCH: /api/movies/{id}           | update movie information
DELETE: /api/movies/{id}          | delete movie

Screenings
GET: /api/screenings                | get list of all screenings
GET: /api/screenings/{id}           | get a screening by id
GET: /api/screenings?movieId=...    | get screening by movie id
POST: /api/screenings               | create a new screening
PATCH: /api/screenings/{id}         | update a screening
DELETE: /api/screenings/{id}        | delete screening

Reservations
GET: /api/reservations/{id}             | get information about reservation
POST: /api/reservations                 | create reservation
PATCH: /api/reservations/{id}/cancel    | cancel a reservation
PATCH: /api/reservations/{id}           | update reservation

Cinema rooms
GET: /api/rooms                        | get list of rooms
GET: /api/rooms/{id}                   | get information about a room
POST: /api/rooms                       | create cinema room
PATCH: /api/update/{id}                 | update room
DELETE: /api/delete/{id}                | delete room

