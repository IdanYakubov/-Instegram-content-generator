import fs from 'fs/promises';
import path from 'path';
import sharp from 'sharp';
import { GENERATED_DIR } from '../config.js';
import { renderSlide } from './slideTemplate.js';

const POST_SIZE = { width: 1080, height: 1080 };

export async function generatePostImage({ brand, id, screenshotPath, headline, subheadline, ctaText }) {
  const screenshotBuffer = await fs.readFile(screenshotPath);

  const pngBuffer = await renderSlide({
    brand,
    width: POST_SIZE.width,
    height: POST_SIZE.height,
    screenshotBuffer,
    headline,
    subheadline,
    ctaText,
    showCta: true,
    showLogo: true,
  });

  const fileName = `${id}.jpg`;
  const outPath = path.join(GENERATED_DIR, fileName);
  await sharp(pngBuffer).jpeg({ quality: 92 }).toFile(outPath);

  return { fileName, absolutePath: outPath };
}
