#!/bin/bash

curl -s -X POST https://api.telegram.org/bot${TELEGRAM_BOT_TOKEN}/sendMessage \
      -H "Content-Type: application/json" \
      -d "{ \"chat_id\": 171765575, \"text\": \"${JOB_NAME} - $*\" }"
