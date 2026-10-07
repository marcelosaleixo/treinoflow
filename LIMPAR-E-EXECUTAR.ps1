$ErrorActionPreference = 'Stop'
Set-Location $PSScriptRoot
Write-Host 'Limpando classes antigas do TreinoFlow...' -ForegroundColor Cyan
foreach ($dir in @('target','.idea','out')) { if (Test-Path $dir) { Remove-Item $dir -Recurse -Force } }
Write-Host 'Compilando projeto limpo...' -ForegroundColor Cyan
& .\mvnw.cmd clean package -DskipTests
if ($LASTEXITCODE -ne 0) { throw 'Build falhou.' }
Write-Host 'Iniciando TreinoFlow...' -ForegroundColor Green
& .\mvnw.cmd spring-boot:run
