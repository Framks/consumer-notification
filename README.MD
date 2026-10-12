# Aplicação que consome e publica mensagens.

## Objetivo:

Dispatcher de notificações com retry e DLQ de verdade: recebe uma notificação via HTTP,
publica na fila, e um consumer tenta "entregar" via um provedor externo simulado. Falhas
transitórias são reprocessadas automaticamente; falhas definitivas caem na DLQ, visível e
reprocessável via API — o objetivo é ter um motivo real pra existir fila+DLQ, não só decoração.

## Endpoints

- `POST /notification` — `{"message": "...", "type": "..."}`. Publica na fila e responde
  imediatamente (a entrega acontece de forma assíncrona, pelo consumer).
- `GET /dlq` — lista as notificações que esgotaram as tentativas e caíram na DLQ.
- `POST /dlq/{id}/retry` — republica uma notificação da DLQ de volta na fila principal.

## Simulação de falhas (pra testar retry/DLQ na mão)

O provedor de envio é simulado (`NotificationProviderSimulator`) e aceita gatilhos
explícitos no campo `message`, além de uma falha aleatória residual (10% por tentativa):

- `"FAIL_PERMANENT"` no texto → falha sempre → esgota as `notification.max-retries`
  tentativas (padrão 3) e cai na DLQ.
- `"FAIL_ONCE"` no texto → falha só na 1ª tentativa → o retry recupera e entrega normalmente.
- qualquer outro texto → ~90% de chance de sucesso de primeira.

## primeiros passos

- preparar o ambiente de infra
  - deve ter: uma fila e uma dlq para consumir e publicar.
  - projeto [consumer-infra](https://github.com/Framks/consumer-infra)
- depois fazer o deploy da app no ambiente de qa para gerar a imagem que vai ser usado em prod.