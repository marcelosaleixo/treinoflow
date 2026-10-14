$ErrorActionPreference = "Stop"

Write-Host "=== TreinoFlow - Configuracao local do PostgreSQL ===" -ForegroundColor Cyan
Write-Host "Informe as credenciais do banco local. A senha nao sera salva no projeto."

$defaultUrl = "jdbc:postgresql://localhost:5432/treinoflow"
$url = Read-Host "URL JDBC [$defaultUrl]"
if ([string]::IsNullOrWhiteSpace($url)) { $url = $defaultUrl }
$username = Read-Host "Usuario PostgreSQL [postgres]"
if ([string]::IsNullOrWhiteSpace($username)) { $username = "postgres" }
$securePassword = Read-Host "Senha PostgreSQL" -AsSecureString
$ptr = [Runtime.InteropServices.Marshal]::SecureStringToBSTR($securePassword)
try { $password = [Runtime.InteropServices.Marshal]::PtrToStringBSTR($ptr) }
finally { [Runtime.InteropServices.Marshal]::ZeroFreeBSTR($ptr) }
if ([string]::IsNullOrWhiteSpace($password)) {
    throw "A senha nao pode ficar vazia. O PostgreSQL exige autenticacao por senha."
}

$env:SPRING_DATASOURCE_URL = $url
$env:SPRING_DATASOURCE_USERNAME = $username
$env:SPRING_DATASOURCE_PASSWORD = $password
$env:SPRING_JPA_DATABASE_PLATFORM = "org.hibernate.dialect.PostgreSQLDialect"

Write-Host "Iniciando TreinoFlow com as variaveis definidas somente nesta sessao..." -ForegroundColor Green
& .\mvnw.cmd spring-boot:run
exit $LASTEXITCODE
