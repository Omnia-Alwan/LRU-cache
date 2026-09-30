# LRU Cache Service

A thread-safe, in-memory **LRU (Least Recently Used) cache** exposed as a REST API, built with Spring Boot. Implements the cache algorithm from scratch using a **HashMap + doubly linked list** — no third-party cache libraries.

## Why this project

LRU caching is a classic systems problem: it shows up in CPU caches, database buffer pools, CDNs, and application-level memoization. The goal here was to implement the eviction algorithm **by hand** — not to wrap Caffeine or Guava — and expose it as a service so the behavior is testable and observable.

This is a small project on purpose. The interesting part is the data structure and the correctness of the eviction logic under edge cases.

## Architecture

The cache is built from three pieces:

```
┌─────────────────────────────────────────────┐
│              LRUCache                       │
│  ┌───────────────┐    ┌──────────────────┐  │
│  │   HashMap     │    │  DoublyLinkedList│  │
│  │ key → Node    │    │  head ↔ ... ↔ tail│  │
│  └───────────────┘    └──────────────────┘  │
└─────────────────────────────────────────────┘
```

- **`HashMap<String, Node>`** — O(1) lookup by key. Maps a key directly to its node in the list.
- **`DoublyLinkedList`** — maintains recency order. `head` side = most recently used, `tail` side = least recently used. Both `head` and `tail` are **sentinel nodes**, so insertion and removal never need null checks.
- **`Node`** — holds `key`, `value`, `prev`, `next`.

The two structures are kept in sync: every `get`/`put` updates both. The map gives O(1) access; the list gives O(1) reordering and eviction.

### Why a doubly linked list?

- **O(1) removal** of any node (needed for `moveToFront` and eviction) requires access to the previous node. A singly linked list would force an O(n) traversal to find it.
- **Sentinel head/tail** eliminate the special cases for "list is empty" and "removing the first/last element." The list always has `head` and `tail`, so `node.prev` and `node.next` are never null for a node that's in the list.

### Why HashMap + linked list instead of `LinkedHashMap`?

Java's `LinkedHashMap` with `accessOrder=true` gives you LRU for free. The point of this project was to implement the mechanism manually, so that decision was deliberately avoided. (A production service would absolutely use `LinkedHashMap` or Caffeine — see "What I'd change in production" below.)

## API

| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/api/{capacity}` | Set the cache capacity |
| `POST` | `/api` | Insert or update a node. Body: `{ "key": "...", "value": ... }` |
| `GET` | `/api/{key}` | Retrieve a value by key. Returns `404` if not present |
| `GET` | `/api` | Display current capacity, size, and all nodes in recency order |

### Example

```bash
# Set capacity to 2
curl -X POST http://localhost:8080/api/2

# Insert two entries
curl -X POST http://localhost:8080/api \
  -H "Content-Type: application/json" \
  -d '{"key": "a", "value": "1"}'

curl -X POST http://localhost:8080/api \
  -H "Content-Type: application/json" \
  -d '{"key": "b", "value": "2"}'

# Touch "a" so "b" becomes least recently used
curl http://localhost:8080/api/a

# Insert "c" — evicts "b"
curl -X POST http://localhost:8080/api \
  -H "Content-Type: application/json" \
  -d '{"key": "c", "value": "3"}'

# "b" should now be gone
curl http://localhost:8080/api/b   # → 404
```

## Key design decisions

**Sentinel nodes.** `head` and `tail` are dummy nodes created in the constructor. This removes all null-checking from `addFirst`, `remove`, and `removeLast` — every real node always has a non-null `prev` and `next`.

**`moveToFront` = `remove` + `addFirst`.** Rather than writing a separate reordering routine, `moveToFront` reuses the two primitives. Less code, fewer places for pointer bugs to hide.

**Eviction removes from both structures.** When capacity is exceeded, `put` calls `removeLast()` on the list (which returns the evicted node) and removes that node's key from the map. The evicted key comes from the *returned node*, not from the inserted key — an easy bug to write, and one I caught by tracing an eviction with a full cache.

**Existing keys are updated, not re-inserted.** If `put` is called with a key that already exists, the existing node is moved to the front and its value is overwritten. Without this check, the list would accumulate duplicate nodes for the same key and drift out of sync with the map.

## How to run

```bash
git clone <repo-url>
cd lru-cache-service
./mvnw spring-boot:run
```

The service starts on `http://localhost:8080`.

## Testing

Unit tests cover the three cases that are easy to get wrong:

- Eviction of the least recently used entry when capacity is exceeded
- Correct recency update when an existing key is accessed
- Safe behavior when `removeLast` is called on an empty cache

```bash
./mvnw test
```

## What I'd change in production

This project is intentionally a from-scratch implementation for learning. A production cache service would differ in several ways:

- **Use `LinkedHashMap` or Caffeine.** Both are battle-tested and handle concurrency, eviction policies, and statistics for you. Reimplementing LRU is an exercise, not a deployment strategy.
- **Replace the global `synchronized` methods with finer-grained locking.** `synchronized` on the service serializes all reads and writes. A `ReentrantReadWriteLock` would allow concurrent reads; a segmented or striped lock would scale further.
- **Use `ConcurrentHashMap` and atomic list operations** if moving to a lock-free or lock-striped design.
- **Add TTL support.** Pure LRU doesn't expire stale entries. Real caches usually combine LRU with time-based eviction.
- **Add metrics.** Hit rate, miss rate, eviction count, and current size are the numbers you actually watch in production (Micrometer + Prometheus).
- **Persist or distribute.** A single-node in-memory cache doesn't survive restarts and doesn't share state across instances. That's what Redis is for — and it's the natural next step from this project.

## Tech stack

- Java 17+
- Spring Boot
- Maven
- Lombok
- JUnit

## Possible extensions

- TTL per entry
- `ReentrantReadWriteLock` instead of `synchronized`
- Micrometer metrics endpoint
- Swap the in-memory store for Redis and expose the same API
- Load test with k6 to show behavior under concurrency

---
