@echo off
setlocal
cd /d "%~dp0"
echo ============================================
echo TreinoFlow - limpeza de classes antigas
echo ============================================
if exist target rmdir /s /q target
if exist .idea rmdir /s /q .idea
if exist out rmdir /s /q out

echo.
echo Compilando projeto limpo...
call mvnw.cmd clean package -DskipTests
if errorlevel 1 (
  echo.
  echo ERRO: o build falhou. Verifique a mensagem acima.
  pause
  exit /b 1
)

echo.
echo Iniciando TreinoFlow...
call mvnw.cmd spring-boot:run
pause
