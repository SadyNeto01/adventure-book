# Adventure Book API

Java 21 · Spring Boot 4.1 · Maven · PostgreSQL 17 · Flyway

```bash
docker compose up -d      # start Postgres
./mvnw spring-boot:run    # run the API
./mvnw test               # tests (Testcontainers, needs Docker)
```

Swagger UI: http://localhost:8080/swagger-ui.html

The database starts with 3 sample books: 1 *The Crystal Caverns*, 2 *Pirates of the Jade Sea*, 3 *The Prisoner*.

## Endpoints

| Method | Path | What it does |
|---|---|---|
| `GET` | `/books` | List books, with paging and filters |
| `GET` | `/books/{id}` | Book details, including `beginSectionId` |
| `POST` | `/books` | Add a new book (rejected if invalid) |
| `POST` | `/books/{id}/categories` | Add a category |
| `DELETE` | `/books/{id}/categories/{category}` | Remove a category |
| `GET` | `/books/{bookId}/sections/{sectionId}` | Read any section freely (no game rules) |
| `POST` | `/players` | Create a player |
| `GET` | `/players/{id}` | Get a player |
| `GET` | `/players/{playerId}/books` | Every book the player has started, most recent first |
| `POST` | `/players/{playerId}/books/{bookId}` | Start a book (or restart it after `DEAD`/`FINISHED`) |
| `GET` | `/players/{playerId}/books/{bookId}` | Current progress: health, status and current section |
| `POST` | `/players/{playerId}/books/{bookId}/choices` | Choose an option of the current section |
| `POST` | `/players/{playerId}/books/{bookId}/pause` | Pause the book |
| `POST` | `/players/{playerId}/books/{bookId}/resume` | Resume a paused book |

Errors are returned as `application/problem+json` (RFC 9457). Validation errors add an `errors` list:

```json
{ "status": 400, "detail": "Invalid request content.", "errors": [{ "field": "name", "message": "must not be blank" }] }
```

For a step-by-step demo, from creating a book to playing it, see [DEMO.md](DEMO.md).

## Books

### List and search

```bash
curl "http://localhost:8080/books"
curl "http://localhost:8080/books?title=crystal"
curl "http://localhost:8080/books?author=fuego&difficulty=HARD"
curl "http://localhost:8080/books?category=adventure&category=fiction"   # must have both
curl "http://localhost:8080/books?page=0&size=2&sort=author,desc"
```

- **Filters:**
  - `title` and `author` match part of the text and ignore case;
  - `category` can repeat, and the book must have all of them;
  - `difficulty` is `EASY`, `MEDIUM` or `HARD`.
- **Paging:** `page` starts at 0 and `size` defaults to 10.
- **Sorting:** `sort` accepts `id`, `title`, `author` or `difficulty`, each with an optional `,asc` or `,desc`. The default is `title`.

### Details

```bash
curl "http://localhost:8080/books/1"
```

```json
{ "id": 1, "title": "The Crystal Caverns", "author": "Evelyn Stormrider", "difficulty": "EASY",
  "categories": ["ADVENTURE", "FANTASY"], "beginSectionId": 1 }
```

### Add and remove categories

Categories are trimmed and upper-cased, so `" horror "` and `HORROR` are the same category.

```bash
curl -X POST "http://localhost:8080/books/1/categories" \
     -H "Content-Type: application/json" \
     -d '{"category": "horror"}'                                  # 200, or 409 if the book already has it

curl -X DELETE "http://localhost:8080/books/1/categories/horror"  # 204, or 404 if the book doesn't have it
```

### Add a book

```bash
curl -X POST "http://localhost:8080/books" \
     -H "Content-Type: application/json" \
     -d '{
           "title": "The Lost Lighthouse",
           "author": "Mara Quill",
           "difficulty": "EASY",
           "categories": ["adventure", "mystery"],
           "sections": [
             { "id": 1, "text": "A lighthouse stands on the cliff.", "type": "BEGIN",
               "options": [
                 { "description": "Climb the stairs", "gotoId": 2,
                   "consequence": { "type": "LOSE_HEALTH", "value": 2, "text": "The old steps scrape your knees." } },
                 { "description": "Walk away", "gotoId": 3 } ] },
             { "id": 2, "text": "The lamp is lit. You found the keeper.", "type": "END" },
             { "id": 3, "text": "You go home and never learn the secret.", "type": "END" }
           ]
         }'
```

The response is `201 Created` with the new book's details, including its `id`.

Sections are created together with their book: they go in the `sections` list of `POST /books`.

> **A book can't get new sections after it is created.** There is no endpoint to add, edit or remove a section, so every book in the database stays complete and valid.

The section ids are your own numbers. They only need to be unique within the book, and options point to them with `gotoId`.

A section, with every field it can have:

```json
{
  "id": 10,
  "text": "A narrow corridor. You hear water dripping.",
  "type": "NODE",
  "options": [
    { "description": "Follow the sound", "gotoId": 30 },
    { "description": "Run through the dark", "gotoId": 40,
      "consequence": { "type": "LOSE_HEALTH", "value": 8, "text": "You fall down the stairs." } },
    { "description": "Rest a moment", "gotoId": 30,
      "consequence": { "type": "GAIN_HEALTH", "value": 2, "text": "You catch your breath." } }
  ]
}
```

| Field | Required | Notes |
|---|---|---|
| `id` | yes | Section number, unique within the book |
| `text` | yes | What the reader sees |
| `type` | yes | `BEGIN` (exactly one per book), `NODE` or `END` (at least one) |
| `options` | yes, unless `END` | The choices; can be left out on `END` sections |
| `options[].description` | yes | Text of the choice |
| `options[].gotoId` | yes | `id` of the section this choice leads to; it must exist in the book |
| `options[].consequence` | no | Effect on the player's health when this option is chosen |
| `consequence.type` | yes, if `consequence` is sent | `LOSE_HEALTH` or `GAIN_HEALTH` |
| `consequence.value` | yes, if `consequence` is sent | A positive number; the direction comes from `type` |
| `consequence.text` | no | Shown to the player after the choice |

**A book is rejected** with `400` and nothing is saved if it:
- doesn't have exactly one `BEGIN` section;
- has no `END` section;
- has an option whose `gotoId` points to a section that doesn't exist;
- has a section that isn't `END` and has no options;
- repeats a section id.

Every problem is listed in `errors`:

```json
{ "status": 400, "detail": "Book is invalid",
  "errors": ["Book must have at least one END section", "Section 1 has an option to missing section 99"] }
```

A book with the same `title` and `author` as an existing one returns `409`.

### Read a section freely

```bash
curl "http://localhost:8080/books/1/sections/1"
```

```json
{ "id": 1, "text": "You stand at the entrance of the legendary Crystal Caverns...", "type": "BEGIN",
  "options": [ { "description": "Cross the rope bridge carefully", "gotoId": 100 },
               { "description": "Search the rocky walls for another path", "gotoId": 20 } ] }
```

This endpoint lets you browse a book like a physical one: it has no game rules and doesn't hide any section. It also never shows consequences. To play by the rules, use a player.

## Players and playing

### Create a player

```bash
curl -X POST "http://localhost:8080/players" \
     -H "Content-Type: application/json" \
     -d '{"name": "Sady"}'
```

```json
{ "id": 1, "name": "Sady" }
```

### Play a book

A player starts at the `BEGIN` section with 10 health. Each choice is the **index** of an option in the current section, starting at 0. The server only accepts options of the section the player is in, so there is no way to jump to another section.

```bash
curl -X POST "http://localhost:8080/players/1/books/1"        # start: 201

curl -X POST "http://localhost:8080/players/1/books/1/choices" \
     -H "Content-Type: application/json" \
     -d '{"option": 1}'                                         # choose the second option

curl "http://localhost:8080/players/1/books/1"                 # current progress
```

Example response after choosing option `1` and then option `0` in *The Crystal Caverns*:

```json
{ "playerId": 1, "bookId": 1, "health": 6, "status": "IN_PROGRESS",
  "section": { "id": 200, "text": "...", "type": "NODE", "options": [ ... ] },
  "consequence": { "type": "LOSE_HEALTH", "value": 4, "text": "You scrape your shoulder squeezing through the narrow gap." } }
```

- `consequence` is the effect of this choice. It is `null` when the option had none, and on start and get.
- `status` is one of:
  - `IN_PROGRESS`;
  - `PAUSED`;
  - `DEAD`, when health reaches 0 or less, even if the section is an `END`;
  - `FINISHED`, when the player reaches an `END` section alive.
- Health has no upper limit, so `GAIN_HEALTH` can take it above 10.

| Situation | Response |
|---|---|
| Starting a book that is already `IN_PROGRESS` or `PAUSED` | `409` |
| Starting a book that is `DEAD` or `FINISHED` | `201`, restarts at `BEGIN` with 10 health |
| Choosing an option the current section doesn't have | `400` |
| Choosing when the book is `PAUSED`, `DEAD` or `FINISHED` | `409` |

### Pause, resume and list

```bash
curl -X POST "http://localhost:8080/players/1/books/1/pause"   # IN_PROGRESS -> PAUSED
curl -X POST "http://localhost:8080/players/1/books/1/resume"  # PAUSED -> IN_PROGRESS, same section and health
curl "http://localhost:8080/players/1/books"                   # all books the player started
```

Pausing a book that isn't in progress, or resuming one that isn't paused, returns `409`.
