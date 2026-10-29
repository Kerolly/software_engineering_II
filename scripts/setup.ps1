# Prepares a Windows machine for local development (see `just setup`).
# Installs missing tools with winget, creates .env, downloads project dependencies.
# Safe to run repeatedly: anything already in place is skipped.

$ErrorActionPreference = 'Stop'
Set-Location (Split-Path -Parent $PSScriptRoot)

$RequiredJava = 25
$RequiredNode = 22
$installed = $false

function Write-Step($text) { Write-Host "`n==> $text" -ForegroundColor Cyan }
function Write-Ok($text) { Write-Host "  [ok] $text" -ForegroundColor Green }
function Write-Warn($text) { Write-Host "  [!!] $text" -ForegroundColor Yellow }

function Get-MajorVersion($exe, $versionArgs, $pattern) {
    # java -version writes to stderr, which is an error under ErrorActionPreference=Stop.
    $previous = $ErrorActionPreference
    $ErrorActionPreference = "Continue"
    try {
        $out = (& $exe @versionArgs 2>&1 | Out-String)
        if ($out -match $pattern) { return [int]$Matches[1] }
    } catch { } finally { $ErrorActionPreference = $previous }
    return 0
}

function Install-WithWinget($name, $id) {
    $answer = Read-Host "  $name is missing or too old. Install it with winget? [Y/n]"
    if ($answer -match '^(n|no)$') { throw "$name is required. Install it manually and run 'just setup' again." }
    winget install --id $id -e --silent --accept-package-agreements --accept-source-agreements
    # -1978335189 means "already installed, no newer version", which is fine.
    if ($LASTEXITCODE -ne 0 -and $LASTEXITCODE -ne -1978335189) { throw "winget failed to install $name (exit code $LASTEXITCODE). Try an elevated terminal." }
    $script:installed = $true
    Write-Ok "$name installed"
}

if (-not (Get-Command winget -ErrorAction SilentlyContinue)) {
    throw 'winget not found. Install "App Installer" from the Microsoft Store, then run this again.'
}

Write-Step "JDK $RequiredJava"
$javaExe = if ($env:JAVA_HOME -and (Test-Path "$env:JAVA_HOME\bin\java.exe")) { "$env:JAVA_HOME\bin\java.exe" } else { 'java' }
$javaMajor = Get-MajorVersion $javaExe @('-version') 'version "(\d+)'
if ($javaMajor -ge $RequiredJava) { Write-Ok "Java $javaMajor" } else { Install-WithWinget "JDK $RequiredJava" "EclipseAdoptium.Temurin.$RequiredJava.JDK" }

Write-Step "Node.js $RequiredNode or newer"
$nodeMajor = Get-MajorVersion 'node' @('--version') 'v(\d+)'
if ($nodeMajor -ge $RequiredNode) { Write-Ok "Node $nodeMajor" } else { Install-WithWinget 'Node.js (LTS)' 'OpenJS.NodeJS.LTS' }

Write-Step '.env'
if (Test-Path .env) {
    Write-Ok '.env already exists'
} else {
    Copy-Item .env.example .env
    Write-Ok '.env created from .env.example (change the passwords if you want)'
}

if ($installed) {
    Write-Host "`nTools were installed. Open a NEW terminal (so PATH and JAVA_HOME are refreshed) and run 'just setup' again to finish." -ForegroundColor Yellow
    exit 0
}

Write-Step 'Frontend dependencies'
Push-Location frontend
npm install
if ($LASTEXITCODE -ne 0) { throw 'npm install failed' }
Pop-Location
Write-Ok 'npm packages installed'

Write-Step 'Backend dependencies'
Push-Location backend
& .\mvnw.cmd -q -B dependency:go-offline
if ($LASTEXITCODE -ne 0) { throw 'Maven dependency download failed' }
Pop-Location
Write-Ok 'Maven dependencies downloaded'

# Docker is not installed by this script (Docker Desktop needs admin rights, a restart and WSL 2).
# It is only checked here, last, so the warning is the final thing you read.
Write-Step 'Docker'
$dockerReady = $false
if (-not (Get-Command docker -ErrorAction SilentlyContinue)) {
    Write-Warn 'Docker is NOT installed. Install Docker (e.g. Docker Desktop: https://www.docker.com/products/docker-desktop/) before running "just dev".'
} else {
    docker info *> $null
    if ($LASTEXITCODE -ne 0) {
        Write-Warn 'Docker is installed but not running. Start it before running "just dev".'
    } else {
        Write-Ok (docker --version)
        docker compose pull
        if ($LASTEXITCODE -ne 0) { throw 'docker compose pull failed' }
        Write-Ok 'Images pulled'
        $dockerReady = $true
    }
}

if ($dockerReady) {
    Write-Host "`nSetup finished. Start developing with: just dev" -ForegroundColor Green
} else {
    Write-Host "`nSetup finished, but Docker is not ready (see above). Fix that, then start with: just dev" -ForegroundColor Yellow
}
