param(
    [string]$BaseUrl = $(if ($env:TREINOFLOW_BASE_URL) { $env:TREINOFLOW_BASE_URL } else { 'http://localhost:8080' }),
    [int]$TimeoutSeconds = 10
)
$ErrorActionPreference = 'Stop'
$healthUrl = "$($BaseUrl.TrimEnd('/'))/actuator/health"
try {
    $response = Invoke-RestMethod -Uri $healthUrl -Method Get -TimeoutSec $TimeoutSeconds
    if ($response.status -ne 'UP') {
        Write-Error "TreinoFlow indisponivel: status=$($response.status)"
        exit 2
    }
    $json = $response | ConvertTo-Json -Depth 10 -Compress
    if ($json -match '"(components|details)"\s*:') {
        Write-Error "Health check expôs componentes ou detalhes internos. Revise management.endpoint.health.show-details."
        exit 3
    }
    Write-Output "OK: TreinoFlow respondeu UP sem detalhes internos em $healthUrl"
    exit 0
} catch {
    Write-Error "Falha no health check de $healthUrl : $($_.Exception.Message)"
    exit 1
}
