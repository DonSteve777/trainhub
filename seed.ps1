# seed.ps1 — Poblar la base de datos con usuarios y datos de ejemplo
# Uso: ejecutar desde la raiz del repo DESPUES de: docker compose up --build
#
#   .\seed.ps1
#
# Solo ejecutar una vez. Si se repite, los usuarios se omiten (ya existen)
# pero los posts, amistades y comentarios se duplicarian.

param(
    [string]$BackendUrl = "http://localhost:8080"
)

$ErrorActionPreference = "Stop"

Write-Host ""
Write-Host "=== TrainHub Seed ===" -ForegroundColor Cyan
Write-Host ""

# ------------------------------------------------------------
# 1. Esperar a que el backend esté disponible
# ------------------------------------------------------------
Write-Host "Esperando al backend en $BackendUrl..."

$maxRetries = 40
$ready = $false
for ($i = 1; $i -le $maxRetries; $i++) {
    try {
        Invoke-WebRequest -Uri "$BackendUrl/api/auth/login" -Method Post `
            -ContentType "application/json" -Body "{}" -ErrorAction Stop | Out-Null
    } catch {
        if ($_.Exception.Response) {
            $ready = $true
            break
        }
    }
    if ($i -eq $maxRetries) {
        Write-Host ""
        Write-Host "ERROR: El backend no responde en $BackendUrl." -ForegroundColor Red
        Write-Host "Asegurate de haber ejecutado: docker compose up --build" -ForegroundColor Yellow
        exit 1
    }
    Write-Host "  Intento $i/$maxRetries — esperando 3s..."
    Start-Sleep -Seconds 3
}

Write-Host "Backend disponible." -ForegroundColor Green
Write-Host ""

# ------------------------------------------------------------
# 2. Registrar los 30 usuarios via API (encripta passwords)
# ------------------------------------------------------------
Write-Host "Registrando 30 usuarios..."

$usersJson = Get-Content "$PSScriptRoot\seed-users.json" -Raw -Encoding UTF8
$response = curl.exe -s -X POST "$BackendUrl/api/auth/dev/register-bulk" `
    -H "Content-Type: application/json; charset=utf-8" `
    -d $usersJson

Write-Host "  $response" -ForegroundColor Green
Write-Host ""

# ------------------------------------------------------------
# 3. Insertar posts, amistades, comentarios y likes via SQL
# ------------------------------------------------------------
Write-Host "Insertando posts, amistades, comentarios y likes..."

Get-Content "$PSScriptRoot\inserts.sql" -Encoding UTF8 | `
    docker compose exec -T postgres psql -U dev_user -d trainhub_dev_db

Write-Host ""
Write-Host "=== Seed completado ===" -ForegroundColor Cyan
Write-Host ""
Write-Host "Accede a la aplicacion:" -ForegroundColor White
Write-Host "  Frontend : http://localhost:4200" -ForegroundColor Cyan
Write-Host "  Backend  : http://localhost:8080" -ForegroundColor Cyan
Write-Host ""
Write-Host "Usuarios de ejemplo (password: password123):" -ForegroundColor White
Write-Host "  pedro.alonso@example.com"
Write-Host "  lucia.vega@example.com"
Write-Host "  javier.mena@example.com"
Write-Host ""
