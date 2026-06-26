import { db } from './db.js';
import { DEFAULT_BRAND } from './brand.js';

export async function getBrand() {
  await db.read();
  return { ...DEFAULT_BRAND, ...db.data.brand, colors: { ...DEFAULT_BRAND.colors, ...db.data.brand?.colors } };
}

export async function updateBrand(patch) {
  await db.read();
  const current = { ...DEFAULT_BRAND, ...db.data.brand, colors: { ...DEFAULT_BRAND.colors, ...db.data.brand?.colors } };
  const next = {
    ...current,
    ...patch,
    colors: { ...current.colors, ...patch.colors },
  };
  db.data.brand = next;
  await db.write();
  return next;
}
