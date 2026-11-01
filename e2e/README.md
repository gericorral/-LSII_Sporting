# ProTube end-to-end tests

The E2E suite runs Playwright against the complete application through the browser.
It does not import frontend components or backend classes: requests cross the real
frontend, HTTP API, session adapter, application services, persistence adapters and
video storage.

## Run locally

Start the complete stack first:

```powershell
docker compose up --build -d
```

Then install the browser and run the suite:

```powershell
cd e2e
npm ci
npx playwright install chromium
npm test
```

The base URL defaults to `http://127.0.0.1`. Override it with `BASE_URL` when the
frontend is exposed on another host or port.

The upload scenario uses an in-memory synthetic MP4 payload. The current upload
contract validates the MP4 MIME type and size, creates the thumbnail in the storage
adapter, and publishes all artifacts atomically; no generated media is committed.
