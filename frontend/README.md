# ReleasePilot Frontend

React + TypeScript + Tailwind CSS dashboard for ReleasePilot.

## Prerequisites

- Node.js 20+
- ReleasePilot backend running on `http://localhost:8080`

## Development

```bash
cd frontend
npm install
npm run dev
```

Open `http://localhost:5173`. API requests are proxied to the backend.

## Pages

- **Dashboard** — org/project/flag counts
- **Flags** — list all feature flags
- **Sandbox** — live SDK evaluation tester
- **Audit Log** — organization audit trail
- **SDK Code** — curl/JS/Java integration snippets
