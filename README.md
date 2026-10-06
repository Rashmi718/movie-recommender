# Movie Semantic Search

A small Spring Boot application that demonstrates semantic search over a movie catalog using embeddings and cosine similarity.

Instead of keyword matching, the app converts movie descriptions and user queries into vector embeddings via Ollama and returns the most semantically similar movies.

## Features

- REST API for natural language movie search
- Embedding-based semantic matching (not keyword search)
- In-memory movie catalog loaded from JSON at startup
- Cosine similarity ranking with top-3 results
- Built with Spring Boot and Spring AI

## Tech Stack

- Java 17
- Spring Boot 4.1.1
- Spring AI 2.0.1 (`spring-ai-starter-model-ollama`)
- Spring WebMVC
- Lombok
- Jackson (Tools Jackson via Spring AI)
- Maven wrapper
- Ollama with `nomic-embed-text` embedding model

## Project Structure

```
src/main/java/com/example/Movie/
├── MovieApplication.java        # Spring Boot entry point
├── controller/
│   └── MovieController.java     # GET /movie/search endpoint
├── service/
│   └── MovieService.java        # Embedding, similarity, and ranking logic
└── model/
    ├── Movie.java               # Internal entity with title, description, embedding
    ├── MovieData.java           # Public DTO returned by the API
    └── MovieMatch.java          # Internal DTO with similarity score

src/main/resources/
├── application.properties       # Ollama configuration
└── movie.json                   # Movie catalog (30 titles with descriptions)
```

## How It Works

1. On startup, `MovieService.initializeMovies()` loads `movie.json` from the classpath.
2. Each movie description is converted to a `float[]` embedding using the configured Ollama `EmbeddingModel`.
3. On a search request:
   - The user query is embedded into a vector.
   - Cosine similarity is computed against every stored movie embedding.
   - Results are sorted descending by similarity.
   - The top 3 matches are returned as `MovieData` (title + description).

Cosine similarity formula used:

```
similarity(A, B) = dot(A, B) / (norm(A) * norm(B))
```

## Prerequisites

- Java 17 or later
- Maven 3.9+ (or use the included `./mvnw` wrapper)
- Ollama running locally on port 11434

Install and pull the embedding model:

```bash
ollama pull nomic-embed-text
ollama serve
```

Verify Ollama is reachable:

```bash
curl http://localhost:11434/api/tags
```

## Configuration

`src/main/resources/application.properties`:

```properties
spring.application.name=Movie
spring.ai.model.embedding=ollama
spring.ai.ollama.base-url=http://localhost:11434
spring.ai.ollama.embedding.model=nomic-embed-text
```

Change `spring.ai.ollama.base-url` if Ollama runs on a different host or port.

## Running the Application

```bash
./mvnw spring-boot:run
```

Or build and run the jar:

```bash
./mvnw clean package
java -jar target/Movie-0.0.1-SNAPSHOT.jar
```

The app starts on the default port:

```
http://localhost:8080
```

On startup you should see in the logs:

```
Movies loaded successfully
```

## API Usage

### Search movies

```
GET /movie/search?query={text}
```

Parameters:

- `query` (required, string): natural language description of what you want to watch.

Example:

```bash
curl "http://localhost:8080/movie/search?query=a%20space%20movie%20about%20astronauts"
```

Example response:

```json
[
  {
    "title": "Interstellar",
    "description": "A group of astronauts travels through a wormhole in search of a new home for humanity as Earth becomes increasingly uninhabitable."
  },
  {
    "title": "The Matrix",
    "description": "A computer programmer discovers that the world he knows is an artificial reality and joins a rebellion against its creators."
  },
  {
    "title": "Alien",
    "description": "A spaceship crew encounters a deadly extraterrestrial creature that begins hunting them one by one."
  }
]
```

More example queries:

```bash
curl "http://localhost:8080/movie/search?query=a%20dream%20heist%20thriller"
curl "http://localhost:8080/movie/search?query=animated%20movie%20about%20family%20and%20music"
curl "http://localhost:8080/movie/search?query=boxing%20underdog%20story"
```

## Movie Dataset

The catalog is defined in `src/main/resources/movie.json` as an array of:

```json
{
  "title": "Inception",
  "description": "A skilled thief enters people's dreams..."
}
```

To add movies, append entries to `movie.json` and restart the application. Embeddings are regenerated at startup.

## Limitations and Possible Improvements

- Embeddings are computed on every startup and held in memory; no vector database or caching is used.
- The result count is fixed at 3 and the search is limited to the local JSON catalog.
- No error handling for empty queries, missing Ollama connection, or empty result sets.
- Possible improvements: make `topK` configurable via request param, return similarity scores in the response, add a vector store such as pgvector or Chroma, and add tests for `cosineSimilarity` and ranking.
