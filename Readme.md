# MS Teams Integration Demo

Proof of concept application that acts as a proxy between an organization’s user and Microsoft Teams.

* Inbound flow: Microsoft Graph webhooks to receive Teams events.
* Outbound flow: acting on behalf of a user (OBO flow) using the user’s OAuth token.

The project runs locally in Docker and relies on Azure AD and ngrok.

## Architecture

* Java 17 / Spring Boot 2.6.6
* Runs via Docker Compose (app container + PostgreSQL)
* ngrok exposes local app for Graph webhooks
* OAuth2 client (Azure) with both Authorization Code and OBO flows
* Graph subscription encryption keys (RSA/AES) generated using java key-tool and loaded using .env

## Prerequisites

* JDK 17
* Docker
* ngrok
* An Azure AD tenant to register (in new app)
* Admin consent in the tenant, where teams organization is (app permissions and installation require admin consent)

## Repository Configuration

Key files you may need to adjust:
* `.env` — environment variables (create from `.env.example`). All variables are required.

### In case teams app has not been registered yet

You must create a new app registration in your Azure tenant.

1. [Register a new application](https://portal.azure.com/#view/Microsoft_AAD_RegisteredApps/CreateApplicationBlade/quickStartType~/null/isMSAApp~/false):

   * Name: e.g., `ms-teams-integration-demo-app`
   * Supported account types: Multitenant
   * Redirect URI (Web): `<your-ngrok-host>/login/oauth2/code/azure`
   * Go to [app registrations](https://portal.azure.com/#view/Microsoft_AAD_RegisteredApps/ApplicationsListBlade) again, choose your newly created app and add a secret for the app(Manage -> Certificates & secrets -> New client secret)
!Important: save the secret value to the `MS_APP_SECRET`, it's not accessible in future.

2. Client credentials:

   * Go to [app registrations](https://portal.azure.com/#view/Microsoft_AAD_RegisteredApps/ApplicationsListBlade) -> All applications -> Choose your app -> Overview
   * Save the Application (client) ID → set as `MS_APP_ID`.
   * Save your Directory (tenant) ID → set as `MS_TENANT_ID`.

3. API permissions (Grant admin consent):

   * Go to Manage -> API permissions -> Add a permission -> Microsoft Graph
     * Delegated: `openid`, `profile`, `email`, `offline_access`, `User.Read.All`, `Chat.ReadWrite`,
`ChatMessage.Send`, `Group.ReadWrite.All`, `ChannelMessage.Read.All`
     * App permissions: `Chat.ReadWrite.All`, `Chat.Read.All`, `Channel.ReadBasic.All`, `ChannelMessage.Read.All`,  `Group.ReadWrite.All`, `Team.ReadBasic.All`,
`TeamMember.Read.All`, `User.Read.All`, `Directory.Read.All`
   * At API permissions if you're admin choose option `Grant admin consent for <your tenant>`. If you're not an admin ask your org admin to give consent.

## Teams App (Manifest)

To be able to use the app in MS Teams Org you have to use `Upload an app` option in the Ms teams app.
This allows you to add a custom app without completing verification of the app by Microsoft.

### Archive creation / upload

* Go to `app-upload-folder` in the root and open `manifest.json`
* `id`, `webApplicationInfo.id` should equal your `MS_APP_ID`.
* `validDomains` must include your current ngrok hostname (e.g. `"validDomains": ["eab53e719355.ngrok-free.app"]`,).
* Zip the manifest together with icons (`color.png`, `outline.png`).
* Upload the custom app via Teams Admin Center or directly in Teams.
* Admin must allow the app and grant required permissions.

### After app is added to MS Teams org

* `application.properties` — change api.base.url to your `ngrok` address (e.g. `https://8069b2ef96ee.ngrok-free.app`).
* Database in `.env`: `SPRING_DATASOURCE_URL`, `DATASOURCE_USERNAME`, `DATASOURCE_PASSWORD`
* Azure / OAuth: `MS_APP_ID`, `MS_APP_SECRET`, `MS_TENANT_ID` (Go to Azure -> App registration -> choose your app -> Overview)
App secret is not accessible after creation so you can create a new one under  `Certificates & secrets` section
* Graph webhook encryption keys ([how to create](#certificate-generation-graph-webhook-encryption)):
  `GRAPH_EVENTS_ENCRYPTION_KEY_ID`, `GRAPH_EVENTS_ENCRYPTION_KEY_PASS`,
  `GRAPH_EVENTS_ENCRYPTION_PUBLIC_KEY`, `GRAPH_EVENTS_ENCRYPTION_PRIVATE_KEY`


## Start the project

1. Start ngrok:

   ```
   ngrok http 8080
   ```
2. Copy the generated HTTPS URL (e.g., `https://xxxx.ngrok-free.app`).
3. Update these to match the ngrok URL:

    * `api.base.url` in `application.properties`
    * The Teams `manifest.json` `validDomains` array

Any time ngrok restarts, update these values.

## Build and Run (Docker)

1. Prepare `.env` in repo root (see `.env.example`).

2. Start the stack:

   ```
   docker-compose up --build
   ```

   This will:

    * Build and start the application container (exposes 8080).
    * Start PostgreSQL (exposes 5432) with credentials from `.env`.

3. Verify logs:

    * App should start on port `8080`.
    * Hibernate will auto-create/update schema.

4. Confirm ngrok is running and `application.properties` has the correct `api.base.url`.

## Testing and Validation (Postman)

Import `postman/ms-teams-integraion-test.json`. It contains example requests. 
User sign-in: visit the app and log in with a user from your tenant. Then through dev console
in browser copy the `JSESSIONID` cookie and attach it in postman, or you can use Postman interceptor
to sync cookies.

## Current Limitations

* Keys and secrets stored in env vars for local testing.
* Postman examples and manifest require manual URL updates per ngrok session.
* Inbound event deduplication for multiple operators in single group is not implemented.


## Certificate Generation (Graph Webhook Encryption)

RSA keys are stored in environment variables for local development. This is acceptable only for a demo;
use a keystore or secret manager in production.

Example key generation (Java KeyTool):

```bash
# Generate RSA keypair in PKCS12 keystore
keytool -genkeypair \
  -alias graph-subscription \
  -keyalg RSA -keysize 2048 -sigalg SHA256withRSA \
  -storetype PKCS12 \
  -keystore graph-subscription.p12 \
  -storepass changeit \
  -keypass changeit \
  -dname "CN=graphsubscription"

# Export public key
keytool -exportcert \
  -alias graph-subscription \
  -keystore graph-subscription.p12 \
  -storepass changeit \
  -rfc > graph-subscription-public.cer
  
# Export private key
openssl pkcs12 -in graph-subscription.p12 -nocerts -nodes -passin pass:changeit -out graphencryption-private.pem
```

Then:

* Copy the content from `graph-subscription-public.cer` file without the header, footer and line breaks for `GRAPH_EVENTS_ENCRYPTION_PUBLIC_KEY` (it should be single line value).
* Copy the content from `graphencryption-private.pem` file without the header, footer and line breaks for `GRAPH_EVENTS_ENCRYPTION_PRIVATE_KEY` (it should be single line value).
* Set `GRAPH_EVENTS_ENCRYPTION_KEY_PASS` to the value, you used instead of `changeit` in `-keypass`
* Create and set `GRAPH_EVENTS_ENCRYPTION_KEY_ID`. You can choose the value by yourself.
  (It's sent with each inbound event to describe how to decrypt it).
