import 'dotenv/config';
import path from 'path';
import { fileURLToPath } from 'url';

const __dirname = path.dirname(fileURLToPath(import.meta.url));
export const ROOT_DIR = path.resolve(__dirname, '..');
export const STORAGE_DIR = path.join(ROOT_DIR, 'storage');
export const UPLOADS_DIR = path.join(STORAGE_DIR, 'uploads');
export const GENERATED_DIR = path.join(STORAGE_DIR, 'generated');
export const DB_FILE = path.join(STORAGE_DIR, 'db.json');

export const config = {
  port: Number(process.env.PORT || 4000),
  // Public base URL where /media/* files are reachable from the internet.
  // Required for Instagram Graph API to fetch the generated image/video.
  // In local dev this will be something like an ngrok URL.
  publicBaseUrl: process.env.PUBLIC_BASE_URL || `http://localhost:${process.env.PORT || 4000}`,
  graphApiVersion: process.env.GRAPH_API_VERSION || 'v21.0',
  instagram: {
    businessAccountId: process.env.IG_BUSINESS_ACCOUNT_ID || '',
    accessToken: process.env.IG_ACCESS_TOKEN || '',
  },
};
