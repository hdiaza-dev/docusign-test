# DocuSign Questionnaire Signing App

Stack:
- **Backend**: Spring Boot 3 (Java 17) + DocuSign Java SDK (v2.1 API)
- **Frontend**: React (Vite)

## E2E rápido
1. Backend: rellena `backend/src/main/resources/application.properties` y coloca tu `private_key.pem` (JWT).
2. `cd backend && ./gradlew bootRun`
3. `cd ../frontend && npm install && npm run dev`
4. Entra a `http://localhost:5173`, completa el formulario y firma en el iframe.

## Seguridad y producción
- Usa HTTPS en frontend/backend.
- Considera **DocuSign Connect** para actualizaciones del estado del sobre (HMAC).
- Limita CORS, valida emails y sanea entradas.
