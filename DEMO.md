# Demo: from a new book to playing it

This walkthrough uses every feature of the API in order; see the [README](README.md) for the full endpoint reference. Start the API first (`docker compose up -d` and `./mvnw spring-boot:run`). The book has two endings and a death.

```text
1 BEGIN ── Enter the mill (−3) ──► 10 NODE ── Follow the sound ─────────► 30 END (treasure)
   │                                   ├──── Run through the dark (−8) ──► 40 END (the stairs give way)
   └── Go back home ──► 20 END         └──── Rest a moment (+2) ─────────► 30 END
```

**1. Try an invalid book first.** It has no `END` section and points to a section that doesn't exist, so the API rejects it, lists every problem and saves nothing:

```bash
curl -X POST "http://localhost:8080/books" -H "Content-Type: application/json" -d '{
  "title": "Broken Book", "author": "Nobody", "difficulty": "EASY",
  "sections": [ { "id": 1, "text": "Start", "type": "BEGIN", "options": [ { "description": "Go", "gotoId": 99 } ] } ]
}'
# 400 {"detail": "Book is invalid", "errors": ["Book must have at least one END section", "Section 1 has an option to missing section 99"]}
```

**2. Create the book.**

```bash
curl -X POST "http://localhost:8080/books" -H "Content-Type: application/json" -d '{
  "title": "The Haunted Mill",
  "author": "Ada Brook",
  "difficulty": "MEDIUM",
  "categories": ["horror", "adventure"],
  "sections": [
    { "id": 1, "text": "An old mill stands by the river. The door is open.", "type": "BEGIN",
      "options": [
        { "description": "Enter the mill", "gotoId": 10,
          "consequence": { "type": "LOSE_HEALTH", "value": 3, "text": "A loose board cuts your leg." } },
        { "description": "Go back home", "gotoId": 20 } ] },
    { "id": 10, "text": "A narrow corridor. You hear water dripping.", "type": "NODE",
      "options": [
        { "description": "Follow the sound", "gotoId": 30 },
        { "description": "Run through the dark", "gotoId": 40,
          "consequence": { "type": "LOSE_HEALTH", "value": 8, "text": "You fall down the stairs." } },
        { "description": "Rest a moment", "gotoId": 30,
          "consequence": { "type": "GAIN_HEALTH", "value": 2, "text": "You catch your breath." } } ] },
    { "id": 20, "text": "You go home. Nothing happens.", "type": "END" },
    { "id": 30, "text": "You find the miller'"'"'s hidden treasure.", "type": "END" },
    { "id": 40, "text": "The stairs give way under you.", "type": "END" }
  ]
}'
# 201 {"id": 4, "title": "The Haunted Mill", ..., "categories": ["ADVENTURE", "HORROR"], "beginSectionId": 1}
```

Keep the `id` from the response (`4` on a fresh database):

```bash
BOOK=4
```

Sending the same request again returns `409`, because a book with that title and author already exists.

**3. Find it and look inside.**

```bash
curl "http://localhost:8080/books?title=mill"
curl "http://localhost:8080/books/$BOOK"
curl "http://localhost:8080/books/$BOOK/sections/10"      # free reading: options without consequences
```

**4. Manage its categories.**

```bash
curl -X POST "http://localhost:8080/books/$BOOK/categories" -H "Content-Type: application/json" -d '{"category": "mystery"}'
curl -X DELETE "http://localhost:8080/books/$BOOK/categories/mystery"
```

**5. Create a player** and keep its `id`:

```bash
curl -X POST "http://localhost:8080/players" -H "Content-Type: application/json" -d '{"name": "Sady"}'
# 201 {"id": 1, "name": "Sady"}
PLAYER=1
```

**6. Start the book.** The player is at section 1 with 10 health:

```bash
curl -X POST "http://localhost:8080/players/$PLAYER/books/$BOOK"
# 201 {"health": 10, "status": "IN_PROGRESS", "section": {"id": 1, ...}, "consequence": null}
```

**7. Make a choice.** Option `0`, "Enter the mill", costs 3 health:

```bash
curl -X POST "http://localhost:8080/players/$PLAYER/books/$BOOK/choices" -H "Content-Type: application/json" -d '{"option": 0}'
# 200 {"health": 7, "status": "IN_PROGRESS", "section": {"id": 10, ...}, "consequence": {"type": "LOSE_HEALTH", "value": 3, ...}}
```

An option the current section doesn't have, such as `{"option": 5}`, returns `400`. The player can't jump to another section.

**8. Pause and resume.**

```bash
curl -X POST "http://localhost:8080/players/$PLAYER/books/$BOOK/pause"     # status PAUSED
curl -X POST "http://localhost:8080/players/$PLAYER/books/$BOOK/choices" -H "Content-Type: application/json" -d '{"option": 0}'
# 409 "Player 1 has paused book 4, resume it first"
curl -X POST "http://localhost:8080/players/$PLAYER/books/$BOOK/resume"    # back to IN_PROGRESS, still section 10 with 7 health
```

**9. Finish the book.** Option `2`, "Rest a moment", gives 2 health and leads to an `END`:

```bash
curl -X POST "http://localhost:8080/players/$PLAYER/books/$BOOK/choices" -H "Content-Type: application/json" -d '{"option": 2}'
# 200 {"health": 9, "status": "FINISHED", "section": {"id": 30, "type": "END", ...}}
```

Any further choice returns `409` ("Game over").

**10. Play again and die.** Starting a finished book restarts it. This time the player runs through the dark, and death wins over reaching the `END` section:

```bash
curl -X POST "http://localhost:8080/players/$PLAYER/books/$BOOK"      # 201, back to section 1 with 10 health
curl -X POST "http://localhost:8080/players/$PLAYER/books/$BOOK/choices" -H "Content-Type: application/json" -d '{"option": 0}'   # health 7
curl -X POST "http://localhost:8080/players/$PLAYER/books/$BOOK/choices" -H "Content-Type: application/json" -d '{"option": 1}'
# 200 {"health": -1, "status": "DEAD", "section": {"id": 40, "type": "END", ...}}
```

**11. See the player's books.**

```bash
curl "http://localhost:8080/players/$PLAYER/books"
# [{"bookId": 4, "health": -1, "status": "DEAD", ...}]
```
