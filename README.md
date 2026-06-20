# The Compass — Instagram Content System

A system for generating and publishing branded Instagram posts and reels to market
**The Compass** app (a life-direction app for entrepreneurs, discharged soldiers, and
anyone who feels they've strayed off course).

Given screenshots of the app plus a headline/subheadline/CTA, it renders a branded
static post image (Sharp/SVG) or a reel video (FFmpeg, Ken Burns zoom per slide,
closing brand card), builds a matching caption with hashtags, and can publish the
result directly to Instagram via the Graph API.

## Structure

- `backend/` — Node.js/Express API: media generation (`src/services`), post storage
  (lowdb, `storage/db.json`), Instagram Graph API publishing.
- `frontend/` — Vite + React app for creating content, editing captions, and
  publishing.

## Running locally

```bash
cd backend
cp .env.example .env
npm install
npm run dev   # listens on PORT (default 4000)
```

```bash
cd frontend
npm install
npm run dev   # http://localhost:5173, proxies /api and /media to :4000
```

## Instagram publishing requires a public URL

The Instagram Graph API fetches media by URL — it cannot accept a raw file upload —
so `PUBLIC_BASE_URL` in `backend/.env` must point to somewhere the internet can reach
your `/media/*` files (e.g. a deployed backend, or a tunnel like ngrok during local
testing). `localhost` will not work for the actual publish step; everything else
(generating posts/reels, editing captions) works fully offline.

You'll also need an Instagram **Business or Creator** account connected to a Facebook
Page, plus `IG_BUSINESS_ACCOUNT_ID` and a long-lived `IG_ACCESS_TOKEN` with the
`instagram_content_publish` permission, set in `backend/.env`.
