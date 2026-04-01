# GeoCoda – Geocoding API

A lightweight geocoding service that converts addresses into GPS coordinates.
Powered by [OpenStreetMap](https://www.openstreetmap.org/) data and
[Apache Lucene](https://lucene.apache.org/) full-text search.

---

## Quick Start (Salt Lake City)

Three commands are all you need:

```bash
# 1. Download the Utah / Salt Lake City address data (~150 MB)
./scripts/download-slc-data.sh

# 2. Build and start the server
docker compose up --build

# 3. Geocode an address (in another terminal)
curl "http://localhost:8080/geocode?q=200+S+State+St+Salt+Lake+City"
```

The first startup takes a minute or two while the address data is indexed.
Subsequent startups are instant because the index is persisted.

---

## Example Queries

Once the server is running, try these Salt Lake City addresses:

| Query | URL |
|-------|-----|
| State Capitol | `http://localhost:8080/geocode?q=350+State+St+Salt+Lake+City` |
| Temple Square area | `http://localhost:8080/geocode?q=50+N+Temple+Salt+Lake+City` |
| University of Utah | `http://localhost:8080/geocode?q=201+Presidents+Cir+Salt+Lake+City` |
| Sugar House | `http://localhost:8080/geocode?q=2100+S+Highland+Dr+Salt+Lake+City` |
| Downtown zip code | `http://localhost:8080/geocode?q=84101` |

You can also open **http://localhost:8080** in your browser to use the
search UI.

---

## Prerequisites

- **Docker** and **Docker Compose** (recommended), _or_
- **Java 25+** and **Maven 3.9+** for running without Docker
- **curl** or **wget** for downloading the data file

---

## Setup Without Docker

```bash
# 1. Download the data
./scripts/download-slc-data.sh

# 2. Build the project
mvn package -DskipTests

# 3. Run the server
java -jar target/*.jar
```

The API will be available at `http://localhost:8080`.

---

## Frontend Development

The Angular frontend lives in the `frontend/` directory:

```bash
cd frontend
npm install
npx ng serve
```

This starts a dev server at `http://localhost:4200` that proxies API
requests to the backend on port 8080.

---

## API Reference

### Search addresses

```
GET /geocode?q=<query>
```

Returns a JSON array of matching addresses with coordinates:

```json
[
  {
    "name": "City Hall",
    "houseNumber": "451",
    "street": "S State St",
    "city": "Salt Lake City",
    "postcode": "84111",
    "latitude": 40.7608,
    "longitude": -111.891,
    "score": 4.23,
    "formattedAddress": "City Hall, 451 S State St, Salt Lake City 84111"
  }
]
```

### Import data

```
POST /import?path=<filename>
```

Imports an OSM PBF file from the `data/` directory into the search index.
This happens automatically on startup when `geocoda.pbf-file-path` is set
in `application.properties`.

---

## Configuration

Settings in `src/main/resources/application.properties`:

| Property | Default | Description |
|----------|---------|-------------|
| `geocoda.data-dir` | `data` | Directory containing PBF files |
| `geocoda.pbf-file-path` | `utah-latest.osm.pbf` | PBF file to import on startup (relative to data dir) |
| `geocoda.index-dir` | `index` | Directory for the Lucene search index |
| `geocoda.max-results` | `10` | Maximum number of search results returned |

---

## Using a Different Region

To geocode a different area, download a different extract from
[Geofabrik](https://download.geofabrik.de/) and update the
`geocoda.pbf-file-path` property. For example:

```bash
# Download Colorado instead
curl -L -o data/colorado-latest.osm.pbf \
  https://download.geofabrik.de/north-america/us/colorado-latest.osm.pbf

# Start with the new file
docker compose up --build -e GEOCODA_PBF_FILE_PATH=colorado-latest.osm.pbf
```

---

## License

This project uses OpenStreetMap data, which is © OpenStreetMap contributors
and available under the [Open Data Commons Open Database License](https://opendatacommons.org/licenses/odbl/).
