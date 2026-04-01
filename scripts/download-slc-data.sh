#!/usr/bin/env bash
# ---------------------------------------------------------------------------
# download-slc-data.sh
#
# Downloads the Utah OpenStreetMap extract from Geofabrik.
# This file contains all of the address data needed to geocode
# Salt Lake City (and the rest of Utah).
#
# Usage:
#   ./scripts/download-slc-data.sh
#
# The file is saved to  data/utah-latest.osm.pbf  (~150 MB).
# ---------------------------------------------------------------------------
set -euo pipefail

DATA_DIR="$(cd "$(dirname "$0")/.." && pwd)/data"
URL="https://download.geofabrik.de/north-america/us/utah-latest.osm.pbf"
FILE="$DATA_DIR/utah-latest.osm.pbf"

echo "============================================"
echo "  GeoCoda – Salt Lake City Data Downloader"
echo "============================================"
echo ""

# Create the data directory if it doesn't exist
mkdir -p "$DATA_DIR"

# Check if the file already exists
if [ -f "$FILE" ]; then
    echo "Data file already exists: $FILE"
    echo "To re-download, delete it first and run this script again."
    echo ""
    echo "  rm $FILE"
    echo "  ./scripts/download-slc-data.sh"
    echo ""
    exit 0
fi

echo "Downloading Utah OSM data from Geofabrik..."
echo "  URL:  $URL"
echo "  Dest: $FILE"
echo ""

# Use curl or wget, whichever is available
if command -v curl &>/dev/null; then
    curl -L --progress-bar -o "$FILE" "$URL"
elif command -v wget &>/dev/null; then
    wget --show-progress -O "$FILE" "$URL"
else
    echo "Error: neither curl nor wget is installed." >&2
    exit 1
fi

echo ""
echo "Download complete! ($(du -h "$FILE" | cut -f1))"
echo ""
echo "Next steps:"
echo "  1. Start GeoCoda           →  docker compose up --build"
echo "  2. Search an address       →  curl 'http://localhost:8080/geocode?q=200+S+State+St+Salt+Lake+City'"
echo ""
