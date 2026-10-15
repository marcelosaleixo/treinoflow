#!/usr/bin/env sh
set -eu
BASE_URL="${TREINOFLOW_BASE_URL:-http://localhost:8080}"
URL="${BASE_URL%/}/actuator/health"
if ! command -v curl >/dev/null 2>&1; then
  echo "ERRO: curl não está instalado." >&2
  exit 2
fi
BODY="$(curl --fail --silent --show-error --max-time 10 "$URL")" || {
  echo "ERRO: health check falhou em $URL" >&2
  exit 1
}
printf '%s' "$BODY" | grep -Eq '"status"[[:space:]]*:[[:space:]]*"UP"' || {
  echo "ERRO: aplicação não reportou status UP." >&2
  exit 2
}
# O endpoint público deve informar somente o estado agregado, sem componentes/detalhes.
if printf '%s' "$BODY" | grep -Eq '"(components|details)"[[:space:]]*:'; then
  echo "ERRO: health check está expondo componentes/detalhes internos." >&2
  exit 3
fi
echo "OK: TreinoFlow respondeu UP sem detalhes internos em $URL"
