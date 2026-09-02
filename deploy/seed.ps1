# seed.ps1 — Poblar la base de datos con usuarios y datos de ejemplo (Windows)
# Uso: desde deploy/, con los contenedores en marcha:
#
#   .\seed.ps1
#
# Opcional: .\seed.ps1 http://localhost:8080

$ErrorActionPreference = "Stop"

$BackendUrl = if ($args.Count -ge 1 -and $args[0]) { $args[0] } else { "http://localhost:8080" }
$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$UsersJson = Join-Path $ScriptDir "seed-users.json"
$InsertsSql = Join-Path $ScriptDir "..\bdd\inserts.sql"

Push-Location $ScriptDir
try {
    Write-Host ""
    Write-Host "=== TrainHub Seed ==="
    Write-Host ""

    # ------------------------------------------------------------
    # 1. Esperar a que el backend esté disponible
    # ------------------------------------------------------------
    Write-Host "Esperando al backend en $BackendUrl..."

    $MaxRetries = 40
    $Ready = $false
    for ($i = 1; $i -le $MaxRetries; $i++) {
        try {
            Invoke-WebRequest -Uri "$BackendUrl/api/auth/login" `
                -Method POST `
                -ContentType "application/json" `
                -Body "{}" `
                -UseBasicParsing `
                -TimeoutSec 5 | Out-Null
            $Ready = $true
            break
        } catch {
            # Cualquier respuesta HTTP (p. ej. 400) indica que el backend está arriba
            if ($_.Exception.Response) {
                $Ready = $true
                break
            }
        }
        Write-Host "  Intento $i/$MaxRetries — esperando 3s..."
        Start-Sleep -Seconds 3
    }

    if (-not $Ready) {
        Write-Host ""
        Write-Host "ERROR: El backend no responde en $BackendUrl."
        Write-Host "Asegurate de haber ejecutado: docker compose up --build"
        exit 1
    }

    Write-Host "Backend disponible."
    Write-Host ""

    # ------------------------------------------------------------
    # 2. Registrar usuarios via API (encripta passwords)
    # ------------------------------------------------------------
    Write-Host "Registrando usuarios..."

    $Body = Get-Content -Path $UsersJson -Raw -Encoding UTF8
    $Response = Invoke-RestMethod -Uri "$BackendUrl/api/auth/dev/register-bulk" `
        -Method POST `
        -ContentType "application/json; charset=utf-8" `
        -Body $Body
    Write-Host "  $Response"
    Write-Host ""

    # ------------------------------------------------------------
    # 3. Insertar posts, amistades, comentarios y likes via SQL
    # ------------------------------------------------------------
    Write-Host "Insertando posts, amistades, comentarios y likes..."

    Get-Content -Path $InsertsSql -Raw -Encoding UTF8 |
        docker compose exec -T postgres psql -U dev_user -d trainhub_dev_db

    Write-Host ""
    Write-Host "=== Seed completado ==="
    Write-Host ""
    Write-Host "Accede a la aplicacion:"
    Write-Host "  Frontend : http://localhost:4200"
    Write-Host "  Backend  : http://localhost:8080"
    Write-Host ""
    Write-Host "Usuarios de ejemplo (password: password123):"
    Write-Host "  pedro.alonso@example.com"
    Write-Host "  lucia.vega@example.com"
    Write-Host "  javier.mena@example.com"
    Write-Host ""
}
finally {
    Pop-Location
}
