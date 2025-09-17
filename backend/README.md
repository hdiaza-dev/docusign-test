# Backend (Spring Boot + DocuSign Java SDK)

## Configuración
1. Crea una cuenta **Developer** en DocuSign y un **Integration Key** (aplicación).
2. Habilita **JWT Grant** y genera/par de claves RSA en la app de DocuSign. Descarga la **clave privada PEM** y guárdala como `src/main/resources/private_key.pem`.
3. En **API and Keys** copia:
   - Integration Key (clientId)
   - User ID (GUID del usuario a impersonar)
   - Account ID
4. Concede **consent** una sola vez visitando:
   `https://account-d.docusign.com/oauth/auth?response_type=code&scope=signature%20impersonation&client_id=YOUR_INTEGRATION_KEY_GUID&redirect_uri=https://www.docusign.com`
5. Rellena `src/main/resources/application.properties` con tus valores.

## Ejecutar
```bash
./gradlew bootRun
```

## Endpoint principal
`POST /api/envelopes`  
Body JSON:
```json
{
  "signerName": "Nombre Apellido",
  "signerEmail": "email@ejemplo.com",
  "returnUrl": "http://localhost:5173/firmado",
  "answers": {
    "Pregunta 1": "Respuesta A",
    "Edad": 34,
    "Acepto condiciones": true
  }
}
```
Respuesta:
```json
{"envelopeId":"...","signingUrl":"https://demo.docusign.net/..."}
```

Abre `signingUrl` en una ventana/iframe para **firma embebida**.

## Notas
- Usamos anclaje `/firma/` para colocar el **SignHere**. No muevas ese texto en el HTML.
- Para producción: usa tu dominio HTTPS, maneja renovación del token JWT y considera **DocuSign Connect (webhooks)** con HMAC.
