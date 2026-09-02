#!/usr/bin/env bash
# seed.sh — Poblar la base de datos con usuarios y datos de ejemplo (Linux/macOS/Git Bash)
# Uso: desde deploy/, con los contenedores en marcha:
#
#   bash seed.sh
#
# En Windows (PowerShell) usar: .\seed.ps1
#
# Se puede repetir: los usuarios existentes se omiten; el SQL trunca y
# vuelve a poblar posts, amistades, comentarios, likes, goals, etc.

set -e

BACKEND_URL="${1:-http://localhost:8080}"
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

echo ""
echo "=== TrainHub Seed ==="
echo ""

# ------------------------------------------------------------
# 1. Esperar a que el backend esté disponible
# ------------------------------------------------------------
echo "Esperando al backend en $BACKEND_URL..."

MAX_RETRIES=40
READY=false
for i in $(seq 1 $MAX_RETRIES); do
    HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" -X POST "$BACKEND_URL/api/auth/login" \
        -H "Content-Type: application/json" -d "{}" 2>/dev/null || true)
    if [ -n "$HTTP_CODE" ] && [ "$HTTP_CODE" != "000" ]; then
        READY=true
        break
    fi
    echo "  Intento $i/$MAX_RETRIES — esperando 3s..."
    sleep 3
done

if [ "$READY" = false ]; then
    echo ""
    echo "ERROR: El backend no responde en $BACKEND_URL."
    echo "Asegurate de haber ejecutado: docker compose up --build"
    exit 1
fi

echo "Backend disponible."
echo ""

# ------------------------------------------------------------
# 2. Registrar los 30 usuarios via API (encripta passwords)
# ------------------------------------------------------------
echo "Registrando 30 usuarios..."

RESPONSE=$(curl -s -X POST "$BACKEND_URL/api/auth/dev/register-bulk" \
    -H "Content-Type: application/json; charset=utf-8" \
    -d @"$SCRIPT_DIR/seed-users.json")

echo "  $RESPONSE"
echo ""

# ------------------------------------------------------------
# 3. Insertar posts, amistades, comentarios y likes via SQL
# ------------------------------------------------------------
echo "Insertando posts, amistades, comentarios y likes..."

docker compose exec -T postgres psql -U dev_user -d trainhub_dev_db < "$SCRIPT_DIR/../bdd/inserts.sql"

echo ""
echo "=== Seed completado ==="
echo ""
echo "Accede a la aplicacion:"
echo "  Frontend : http://localhost:4200"
echo "  Backend  : http://localhost:8080"
echo ""
echo "Usuarios de ejemplo (password: password123):"
echo "  pedro.alonso@example.com"
echo "  lucia.vega@example.com"
echo "  javier.mena@example.com"
echo ""
