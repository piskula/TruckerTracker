# DevOps — TruckTrack Setup

## Keycloak — Configure `trucktrack` Realm

These steps assume Keycloak is already running at `https://sso.momosi.org`.

---

### 1. Create the Realm

1. Log in to the Keycloak Admin Console.
2. Top-left dropdown → **Create realm**.
3. Realm name: `trucktrack`
4. **Enabled**: ON → **Create**.

---

### 2. Create Realm Roles

Go to **Realm roles** → **Create role** for each:

| Role name | Description |
|---|---|
| `ROLE_DRIVER` | Truck drivers — can report issues, upload photos |
| `ROLE_MECHANIC` | Mechanics — can start and resolve issues |

> **Why `ROLE_` prefix?** Spring Security's `hasRole('DRIVER')` checks for the authority `ROLE_DRIVER`.
> Our `jwtAuthenticationConverter` maps Keycloak role strings directly to `SimpleGrantedAuthority` with no transformation,
> so the prefix must already be present in Keycloak.

---

### 3. Create the Client (Angular / Android)

Go to **Clients** → **Create client**.

**Step 1 — General settings:**
- Client type: `OpenID Connect`
- Client ID: `trucktrack-app`
- Name: `TruckTrack App`
- **Next**

**Step 2 — Capability config:**
- Client authentication: **OFF** (public client — no secret needed for SPA/mobile)
- Authorization: **OFF**
- Authentication flow:
  - Standard flow: **ON** (Authorization Code + PKCE)
  - Direct access grants: **OFF** in production; can be ON temporarily for Postman/testing
  - All others: **OFF**
- **Next**

**Step 3 — Login settings:**

For Angular (web):
- Valid redirect URIs: `http://localhost:4200/*`, `https://your-prod-domain.com/*`
- Valid post logout redirect URIs: `http://localhost:4200/*`, `https://your-prod-domain.com/*`
- Web origins: `http://localhost:4200`, `https://your-prod-domain.com`

For Android (mobile):
- Valid redirect URIs: `com.momosilabs.trucktrack://*`
- Web origins: *(leave empty — not applicable for native apps)*

Click **Save**.

---

### 4. Enable PKCE on the Client

In the client detail page → **Advanced** tab:

- Proof Key for Code Exchange Code Challenge Method: **S256**

Save.

> PKCE prevents authorization code interception attacks. It is mandatory for public clients
> (no client secret) and is natively supported by Angular OAuth2 libraries and Android AppAuth.

---

### 5. Verify JWT Contains Realm Roles

By default Keycloak includes realm roles in the token under `realm_access.roles`.
To confirm, go to **Client scopes** → **roles** → **Mappers** → ensure **realm roles** mapper exists with:
- Mapper type: `User Realm Role`
- Token claim name: `realm_access.roles`
- Add to ID token: ON
- Add to access token: ON

No custom mapper should be needed — this is Keycloak's default behaviour.

---

### 6. Create a Test User and Assign a Role

1. Go to **Users** → **Add user**.
2. Fill in username, email, first/last name → **Create**.
3. Go to the **Credentials** tab → set a password (Temporary: OFF).
4. Go to the **Role mapping** tab → **Assign role** → filter by realm → select `ROLE_DRIVER` or `ROLE_MECHANIC`.

---

### 7. Update `application.yml`

Change the issuer URI to point at the new realm:

```yaml
spring:
  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: https://sso.momosi.org/realms/trucktrack
```

---

### 8. Verify the Token (optional)

Use Postman or the Keycloak built-in token endpoint to get a token and inspect it at [jwt.io](https://jwt.io).
The decoded payload should include:

```json
{
  "realm_access": {
    "roles": ["ROLE_DRIVER", "default-roles-trucktrack", "offline_access", "uma_authorization"]
  }
}
```

Confirm `ROLE_DRIVER` or `ROLE_MECHANIC` is present before testing the API.

---

## Reverse-Proxy Rate Limiting — `/api/v1/push/backchannel-logout`

`POST /api/v1/push/backchannel-logout` is `permitAll` in `SecurityConfig`: the endpoint validates the
Keycloak-issued logout token itself, so unauthenticated traffic reaches the application and is only
rejected after signature verification (which can trigger a JWKS fetch). Shed that traffic at the
reverse proxy instead.

Generic NGINX example — a shared zone keyed on the client IP, a few requests per second with a small
burst. Declare the zone in the `http` block and the location in the `server` block:

```nginx
limit_req_zone $binary_remote_addr zone=backchannel_logout:10m rate=2r/s;

location = /api/v1/push/backchannel-logout {
    limit_req zone=backchannel_logout burst=10 nodelay;
    proxy_pass http://<app-upstream>;
}
```

Keycloak sends one logout request per signed-out session, so 1–5 r/s per IP is well above legitimate
volume; requests beyond the burst are rejected with `503` before reaching the app.

---

## Keycloak — Enable Back-Channel Logout Delivery

Keycloak only sends back-channel logout requests to **confidential** clients, but `trucktrack-app`
was created as a **public** client in step 3 above (Client authentication: OFF — no secret needed for
SPA/mobile). To actually receive logout notifications, switch that client over:

1. Go to **Clients** → `trucktrack-app` → **Capabilities** tab.
2. Set **Client authentication** to **ON** (the client becomes confidential). The client secret is
   generated on the **Credentials** tab — copy it from there; never write it into this repo, docs, or
   logs. How the app authenticates with it at the token endpoint is a client-side concern.
3. In the client's **Advanced** settings, set **Backchannel Logout URL** to the app's public address:
   `https://<host>/api/v1/push/backchannel-logout` — the same URL the rate limit above protects.
   Keycloak POSTs the signed logout token there when a session signs out.
4. Enable **Backchannel Logout Session Required** so the signed logout token includes `sid`.
   Push cleanup requires both `sub` and `sid`; missing, blank or non-string session identifiers
   return `400` without deleting any registration.
5. Ensure the app's access tokens include the same session's `sid`. The backend binds push
   registrations to this authenticated claim, not to an identifier supplied in a request body.
   Existing registrations gain a session binding on their next login or Profile registration.
6. Keep the PKCE settings from steps 3–4 in place.

With a public client (or no **Backchannel Logout URL** set), Keycloak silently sends nothing and the
endpoint stays idle — verify delivery by signing a user out and checking the app log for a processed
(or rejected) logout request. Never log the token or its session identifier.
