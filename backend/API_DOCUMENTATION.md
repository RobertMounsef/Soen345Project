# API Documentation

Base URL: `http://localhost:8080`

## Auth

| Method | Endpoint | Access | Description |
|--------|----------|--------|-------------|
| `POST` | `/api/auth/login` | Public | Login with email or phone + password |
| `POST` | `/api/auth/logout` | Logged in | Invalidate session |
| `GET` | `/api/auth/session` | Logged in | Get current session info |

## Users

| Method | Endpoint | Access | Description |
|--------|----------|--------|-------------|
| `GET` | `/api/users` | Admin | Get all users |
| `GET` | `/api/users/{id}` | Admin | Get user by ID |
| `POST` | `/api/users` | Public | Register a new user |
| `PUT` | `/api/users/{id}` | Admin | Update a user |
| `DELETE` | `/api/users/{id}` | Admin | Delete a user |

## Events

| Method | Endpoint | Access | Description |
|--------|----------|--------|-------------|
| `GET` | `/api/events` | Public | Get all events |
| `GET` | `/api/events/{id}` | Public | Get event by ID |
| `POST` | `/api/events` | Admin only | Create a new event |
| `PUT` | `/api/events/{id}` | Admin (owner only) | Update an event |
| `DELETE` | `/api/events/{id}` | Admin (owner only) | Delete an event |

## Reservations

| Method | Endpoint | Access | Description |
|--------|----------|--------|-------------|
| `GET` | `/api/reservations` | Admin → all, Customer → own | Get reservations |
| `GET` | `/api/reservations/{id}` | Admin → any, Customer → own | Get reservation by ID |
| `POST` | `/api/reservations` | Logged in | Create a reservation |
| `DELETE` | `/api/reservations/{id}` | Admin → any, Customer → own | Delete a reservation |

## Notifications

| Method | Endpoint | Access | Description |
|--------|----------|--------|-------------|
| `GET` | `/api/notifications` | Admin | Get all notifications |
| `GET` | `/api/notifications/{id}` | Admin | Get notification by ID |
| `POST` | `/api/notifications` | Logged in | Create a notification |
| `DELETE` | `/api/notifications/{id}` | Admin | Delete a notification |
