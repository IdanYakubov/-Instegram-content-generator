import { Low } from 'lowdb';
import { JSONFile } from 'lowdb/node';
import fs from 'fs';
import { DB_FILE, STORAGE_DIR, UPLOADS_DIR, GENERATED_DIR } from './config.js';

for (const dir of [STORAGE_DIR, UPLOADS_DIR, GENERATED_DIR]) {
  fs.mkdirSync(dir, { recursive: true });
}

const adapter = new JSONFile(DB_FILE);
export const db = new Low(adapter, { posts: [] });

await db.read();
db.data ||= { posts: [] };
await db.write();
