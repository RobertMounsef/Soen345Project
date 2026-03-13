# API Documentation

Base URL: `http://localhost:8080`

---

## Auth

| Method | Endpoint | Access | Description |
|--------|----------|--------|-------------|
| `POST` | `/api/auth/login` | Public | Login with email or phone + password |
| `GET` | `/api/auth/session` | Logged in | Get current session info |
| `POST` | `/api/auth/logout` | Logged in | Invalidate session |

**Login request body:**
```json
{ "identifier": "user@example.com or 5141234567", "password": "secret" }
```

**Login / session response:**
```json
{ "userId": 1, "role": "CUSTOMER" }
```

---

## Users

> Email is mandatory. Phone is optional.

| Method | Endpoint | Access | Description |
|--------|----------|--------|-------------|
| `POST` | `/api/users` | Public | Register a new user |

**Request body:**
```json
{
  "name": "Jane Doe",
  "email": "jane@example.com",
  "phone": "514-555-0123",
  "password": "secret123",
  "role": "CUSTOMER"
}
```

---

## Events

> Browsing is public. Creating, editing, and deleting require an Admin session.
> Admins can only edit and delete their own events.
> `availableSpots` is automatically set to `totalSpots` on creation.

| Method | Endpoint | Access | Description |
|--------|----------|--------|-------------|
| `GET` | `/api/events` | Public | Get all events (supports filters) |
| `GET` | `/api/events/{id}` | Public | Get event by ID |
| `POST` | `/api/events` | Admin | Create a new event |
| `PUT` | `/api/events/{id}` | Admin (organizer only) | Update an event |
| `DELETE` | `/api/events/{id}` | Admin (organizer only) | Delete an event |

**Search query params (mutually exclusive):**

| Param | Example | Description |
|-------|---------|-------------|
| `category` | `?category=music` | Filter by exact category (case-insensitive) |
| `location` | `?location=montreal` | Filter by partial location match |
| `date` | `?date=2026-06-15` | Filter by date (format: `yyyy-MM-dd`) |

---

## Reservations

> Creating and cancelling reservations is Customer only.
> A reservation's owner is always set to the logged-in customer — it cannot be spoofed.
> Creating a reservation decrements `availableSpots`. Cancelling increments it back.
> A confirmation or cancellation email is sent automatically.
> Reservations are blocked if the event has 0 available spots.

| Method | Endpoint | Access | Description |
|--------|----------|--------|-------------|
| `GET` | `/api/reservations` | Customer → their own, Admin → their events' reservations | Get reservations |
| `GET` | `/api/reservations/{id}` | Customer → their own, Admin → their events' only | Get reservation by ID |
| `GET` | `/api/reservations/event/{eventId}` | Customer → their booking for that event, Admin → all bookings (must own event) | Get reservations for a specific event |
| `POST` | `/api/reservations` | Customer only | Create a reservation |
| `DELETE` | `/api/reservations/{id}` | Customer only (their own) | Cancel a reservation |

**Create reservation request body:**
```json
{ "event": { "eventId": 1 } }
```

---

## HTTP Status Codes

| Code | Meaning |
|------|---------|
| `200` | OK |
| `204` | No content (successful delete) |
| `400` | Bad request (e.g. sold out, missing email) |
| `401` | Not logged in |
| `403` | Forbidden (wrong role or not your resource) |
| `404` | Resource not found |
