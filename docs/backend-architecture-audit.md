# Production website API audit

## Sources and confidence

The repository is already the Android app (`app/` Gradle module); it contains no
website source or backend source. The production Vercel page was accessible and
its deployed bundle, `assets/index-B81o5OmK.js`, was inspected. Its Axios client
sets `baseURL` to `https://expencetrack.onrender.com/api`, checks JSON response
objects using `success` and `data`, and attaches a Bearer token from the website
auth store. The website stores that auth state in browser local storage under
`expense-rack-auth`.

This documents the contract used by the deployed frontend. It is not a substitute
for backend implementation details not exposed to the client (server-side
validation, exact database types, or fields the frontend does not read).
Unauthenticated GET probes to some data routes returned 403; no write requests
were sent during inspection.

## Retrofit configuration and authentication

- Retrofit base URL: `https://expencetrack.onrender.com/api/` (trailing slash
  required).
- Token request header: `Authorization: Bearer <token>`.
- Login: `POST /api/auth/login` with `{"identifier","password"}`. On success the
  website reads `response.data.success`, takes `response.data.data.token`, and
  stores the remaining properties of `data.data` as the user object.
- Registration is a two-step OTP flow:
  1. `POST /api/auth/register/send-otp` with `{"name","username","email","password"}`.
  2. `POST /api/auth/register/verify-otp` with `{"email","code"}`. A successful
     response is also consumed as `data.data.token` plus user properties.
- The same verify-OTP URL is also called by the website's other OTP-auth branch
  with `{"otp","email"}`. The Android app maps registration's `{email, code}`
  flow; the alternate login OTP branch is exposed in the Retrofit service but
  is not currently surfaced in the Android UI.
- There is no frontend call to `/auth/me`. User data comes from the auth
  response and the auth store.
- Android stores its bearer token in encrypted Android preferences and attaches
  it to requests. It does not assume a refresh-token endpoint; a 401 returns
  the user to sign-in.

## Discovered API calls

All paths below are relative to `/api`. JSON bodies are shown with the property
names used by the production client. The common response envelope consumed by
the website is `{"success": boolean, "data": ..., "message"?: string}`.

| Method | Path | Request | Response used by website |
| --- | --- | --- | --- |
| POST | `/auth/login` | `{"identifier","password"}` | `success`; `data` includes `token` and user fields |
| POST | `/auth/register/send-otp` | `{"name","username","email","password"}` | `success`; optional `message` |
| POST | `/auth/register/verify-otp` | `{"email","code"}` for registration; also `{"otp","email"}` in alternate OTP-auth UI | `success`; `data` includes `token` and user fields |
| POST | `/auth/forgot-password/request` | `{"email"}` | `success`; optional `message` |
| POST | `/auth/forgot-password/reset` | `{"email","code","newPassword"}` | `success`; optional `message` |
| POST | `/auth/forgot-username/request` | `{"email"}` | `success`; website displays `message` |
| POST | `/auth/forgot-username/change-via-otp` | `{"email","code","newUsername"}` | `success`; website displays `message` |
| POST | `/auth/forgot-username/change-via-password` | `{"email","password","newUsername"}` | `success`; website displays `message` |
| GET | `/transactions` | Query: `month`, `year`, or `startDate`, `endDate`; the client also calls it with no explicit query | `success`, `data` transaction array |
| POST | `/transactions` | `{"type","amount","category","description","date","isRecurring","walletId?"}` | `success`; created transaction in `data` |
| PUT | `/transactions/{id}` | Same transaction form fields | `success`; updated transaction in `data` |
| DELETE | `/transactions/{id}` | None | Website checks `success` |
| GET | `/wallet` | None | `success`, `data` wallet array |
| POST | `/wallet` | Wallet fields listed below | `success`, created wallet in `data` |
| PUT | `/wallet/{id}` | Wallet fields listed below, including its existing `id` | `success`, updated wallet in `data` |
| PATCH | `/wallet/{id}/add-money` | Query: `amount`; no JSON body | `success`, wallet in `data` |
| DELETE | `/wallet/{id}` | None | Website checks `success` |
| POST | `/budget` | `{"month","year","budgetLimit","monthlyIncome"}` | `success`; response consumed for success |
| GET | `/goals` | Query: `month`, `year` | `success`, `data` goals array |
| POST | `/goals` | `{"title","amount","completed","month","year","medium"}` | `success`; created goal in `data` |
| PUT | `/goals/{id}` | Goal properties, including its existing `id` | `success`; updated goal in `data` |
| PATCH | `/goals/{id}/status` | Query: `completed`; no JSON body | `success`, updated goal in `data` |
| DELETE | `/goals/{id}` | None | Website checks `success` |
| GET | `/dashboard/summary` | None | `success`, `data` summary |
| PUT | `/users/settings` | Partial object: website sends `{name,profilePicture}`, `{accentColor}`, `{currency}`, or `{gmailConnected}` | `success`, updated user in `data` |

### Data models visible to the frontend

- User: login response includes at least a `token`; the remaining `data.data`
  object is retained by the website as the user. Profile/settings code reads
  `name`, `profilePicture`, `accentColor`, `currency`, and `gmailConnected`.
- Transaction (both expense and income): `type`, `amount`, `category`,
  `description`, `date`, `isRecurring`, optional `walletId`, and server ID.
  The frontend uses one transactions endpoint and distinguishes income by
  `type`; it does not call a separate income endpoint.
- Wallet/card: `cardType`, `cardBrand`, `bankName`, `cardHolderName`,
  `cardNumber`, `expiryDate`, numeric `balance`, `designPreset`,
  `primaryColor`, `secondaryColor`, `textColor`, `cardIcon`,
  `backSignatureText`, and `backContactInfo`. The website passes the submitted
  wallet object to create/update; CVV is deliberately not collected/stored.
- Budget: `month`, `year`, `budgetLimit`, `monthlyIncome`. The website saves
  through POST `/budget`; its dashboard reads budget summary values from
  `/dashboard/summary`, whose displayed fields include `availableBalance`,
  `monthlyIncome`, `monthlyBudgetLimit`, and `monthlySpent`.
- Goal: `title`, numeric `amount`, `completed`, `month`, `year`, and `medium`.
- Categories are read from transaction `category` values and frontend category
  options. No categories endpoint is called by the deployed website.

## Android implementation notes

- Android calls the existing API through Retrofit/OkHttp at the audited `/api`
  base URL. Room is retained only as a cache: the repository updates it from
  successful server responses, clears/replaces cached lists on sync, and clears
  per-user cached records on logout/account change. Failed writes are surfaced
  instead of being treated as successful local-only writes.
- The wallet `balance` returned by the API is the authoritative account amount.
  The editable card-preview amount is stored separately in Room by wallet ID,
  is cleared with account data, and is never sent to wallet or transaction
  endpoints; dashboard totals, transaction history, and top-ups continue using
  the server wallet balance.
- Endpoints whose website contract only consumes the `success`/`message`
  envelope use a typed status response rather than `ApiResponse<Unit>`, for
  which Moshi cannot create a JSON converter. Transaction updates use the
  server ID in the `PUT /transactions/{id}` path and cache the returned record;
  transaction deletes remove the cached record only after a successful
  `DELETE /transactions/{id}` envelope. Empty or unsuccessful responses leave
  the transaction cache unchanged.
- Login, registration OTP, forgot-password/reset, transactions, wallets/cards,
  budget, goals, dashboard summary, and the website's user-settings fields are
  wired to the audited endpoints. The diagnostics screen only performs
  read-only GET checks; it no longer creates or deletes test data on the live
  account.
- The Android UI does not currently implement forgot-username screens, the
  alternate login OTP branch, profile-picture editing, or Gmail integration.
  The corresponding website routes/settings payloads are recorded above, and
  no Android UI flow has been invented for them.
- The deployed frontend bundle did not reveal a category CRUD endpoint, a
  separate income endpoint, a profile GET endpoint, or a Gemini/AI API route.
  No such Android routes are added.
