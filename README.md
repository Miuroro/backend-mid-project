# Ticket Tracking System REST API Specifications

This document describes the REST API for the Ticket Tracking System backend. It provides an overview of the endpoints and their request/response formats for the Spring Boot application using JdbcTemplate and PostgreSQL.

---

## 1. Global Configuration & Constants

### Base URL
`http://localhost:8080/api`

### Standard Workflow Enums
* **Ticket Status:** `open`, `in progress`, `closed`

---

## 2. Users Endpoints (`/api/users`)
Manages application users. No authentication or login system is required.

| Method | URL | Description | Request Body / Params | Expected Response | Validations / Constraints |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **POST** | `/api/users` | Create a new user | `{ "name": "string", "email": "string" }` | `201 Created`<br>`{ "id": 1, "name": "...", "email": "..." }` | Name $\ge$ 3 chars (required), valid email format (required, unique). |
| **GET** | `/api/users` | List all users | *None* | `200 OK`<br>`[ { "id": 1, ... } ]` | Fetches full system roster. |
| **GET** | `/api/users/{id}` | Get a single user's details | Path Variable: `id` | `200 OK` or `404 Not Found` | ID must exist in the database. |
| **PUT** | `/api/users/{id}` | Update an existing user | Path Variable: `id`<br>`{ "name": "string", "email": "string" }` | `200 OK`<br>`{ "id": 1, "name": "...", "email": "..." }` | ID must exist; Name $\ge$ 3 chars, valid unique email format. |
| **DELETE** | `/api/users/{id}` | Delete a user from the system | Path Variable: `id` | `204 No Content` | ID must exist. |

---

## 3. Projects Endpoints (`/api/projects`)
Focuses purely on operational metrics. *Note: Creating, updating, or deleting projects is not required via the API.*

| Method | URL | Description | Request Body / Params | Expected Response | Validations / Notes |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **GET** | `/api/projects` | List all projects with status metrics | *None* | `200 OK`<br>`[ { "id": 1, "name": "Project Alpha", "openTickets": 10, "inProgressTickets": 5, "closedTickets": 30 } ]` | Aggregates ticket statuses dynamically per project using a SQL `GROUP BY` query. |

---

## 4. Tickets Endpoints (`/api/tickets`)
Manages system tasks, bugs, and user stories. *Note: Deleting core tickets is disabled in this system.*

| Method | URL                                    | Description                      | Request Body / Params | Expected Response | Validations / Constraints |
| :--- |:---------------------------------------|:---------------------------------| :--- | :--- | :--- |
| **POST** | `/api/tickets`                         | Create a new ticket              | `{ "title": "string", "description": "string", "projectId": 1 }` | `201 Created`<br>`{ "id": 101, "title": "...", "description": "...", "status": "open", "projectId": 1, "createdAt": "2026-06-12...", "updatedAt": null }` | Title is required. Project ID must exist. Status defaults automatically to `open`. `createdAt` generated automatically. `updatedAt` is initially `null`. |
| **GET** | `/api/tickets` | Get all tickets or filter by search criteria |`?status=string&search=string`| `200 OK`<br>`[ { "id": 101, "title": "...", "description": "...", "status": "open", "projectId": 1, "createdAt": "...", "updatedAt": null } ]` | Returns a complete list of all records stored in the tickets table. If query parameters are provided, it filters the results dynamically using `AND` logic. || **GET** | `/api/tickets/{id}`                    | Get details of a single ticket   | Path Variable: `id` | `200 OK` or `404 Not Found` | ID must exist. Performs a `LEFT JOIN` on a bridge table to fetch all current assignees. |
| **PUT** | `/api/tickets/{id}`                    | Update ticket attributes         | Path Variable: `id`<br>`{ "title": "...", "description": "...", "status": "in progress", "projectId": 1 }` | `200 OK`<br>`{ updated ticket payload }` | ID must exist. Title required. Project ID must exist. Status must strictly equal `open`, `in progress`, or `closed`. **Triggers Email Notification.** |
| **POST** | `/api/tickets/{id}/assignees`          | Add an assignee to a ticket      | Path Variable: `id`<br>`{ "userId": 5 }` | `200 OK`<br>`{ updated ticket payload }` | Ticket ID and User ID must both exist in the database before making attachment. **Triggers Email Notification.** |
| **DELETE** | `/api/tickets/{id}/assignees/{userId}` | Remove an assignee from a ticket | Path Variables: `id`, `userId` | `200 OK` or `204 No Content` | Ticket ID and User ID must both exist. Breaks association in mapping table. **Triggers Email Notification.** |


---

## 5. Email Notification Plan

### Trigger Protocol
When a ticket is changed either by updating its main details ```(PUT /api/tickets/{id})```or changing who it’s assigned to ```(POST or DELETE on assignees)``` an email is sent automatically.
### Target Audience & Layout
* **Recipients:** Every user currently attached to that ticket's assignment collection.
* **Payload Structure:**
  ```text
  Ticket #{id} updated:
  Title: "{title}"
  Status: "{status}"

  Current assignees: {Name1}, {Name2}, {Name3}
* **If Sending Fails:** If the email fails to send (due to network or API errors), the system logs the error, but the main API request still completes successfully without crashing.
  
## 6. Database 
### Database Entity-Relationship Diagram

```text
                                            ┌────────────────┐                    ┌─────────────────┐
                                            │    projects    │                    │      users      │
                                            ├────────────────┤                    ├─────────────────┤
                                            │ PK │ id        │                    │ PK │ id         │
                                            │    │ name  (RE)│                    │    │ name   (RE)│
                                            └───────┬────────┘                    │    │ email  (RE)│
                                                    │                             └───────┬─────────┘
                                                    │ 1                                   │ 1
                                                    │                                     │
                                                    │ N                                   │ N
                                            ┌───────▼───────────────┐              ┌──────▼────────────┐
                                            │    tickets            │ 1          M │ ticket_assignees  │
                                            ├───────────────────────┤──────────────┼───────────────────┤
                                            │ PK │ id               │              │ PK,FK1 │ ticket_id│
                                            │ FK │ project_id   (RE)│              │ PK,FK2 │ user_id  │
                                            │    │ title        (RE)│              └───────────────────┘
                                            │    │ description  (OP)│
                                            │    │ status       (RE)│
                                            │    │ created_at   (RE)│
                                            │    │ updated_at   (OP)│
                                            └───────────────────────┘

(RE): Required
(OP): optional