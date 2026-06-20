import fs from 'fs/promises';
import path from 'path';
import { spawn } from 'child_process';
import ffmpegPath from 'ffmpeg-static';
import { GENERATED_DIR } from '../config.js';
import { renderSlide, renderOutroSlide } from './slideTemplate.js';

const REEL_SIZE = { width: 1080, height: 1920 };
const FPS = 25;
const SLIDE_DURATION = 2.5; // seconds per screenshot slide
const OUTRO_DURATION = 3.2; // seconds for the closing brand card
const MAX_SCREENSHOTS = 8;

function runFfmpeg(args) {
  return new Promise((resolve, reject) => {
    const proc = spawn(ffmpegPath, args);
    let stderr = '';
    proc.stderr.on('data', (chunk) => {
      stderr += chunk.toString();
    });
    proc.on('error', reject);
    proc.on('close', (code) => {
      if (code === 0) resolve();
      else reject(new Error(`ffmpeg exited with code ${code}\n${stderr.slice(-2000)}`));
    });
  });
}

// Renders one still image into its own zoom-animated video segment. Each slide gets a
// dedicated ffmpeg pass (infinite -loop input + zoompan `d` capped by -frames:v) because
// feeding multiple slides through one filter_complex with zoompan+concat together makes
// zoompan re-multiply already-duplicated input frames (d frames per pre-looped input frame),
// ballooning a slide to thousands of frames and starving every slide after it.
async function renderSegment({ slidePath, durationSeconds, outPath }) {
  const frameCount = Math.round(durationSeconds * FPS);
  const args = [
    '-loop', '1',
    '-i', slidePath,
    '-vf',
    `scale=${REEL_SIZE.width}:${REEL_SIZE.height},zoompan=z='min(zoom+0.0015,1.12)':d=${frameCount}:s=${REEL_SIZE.width}x${REEL_SIZE.height}:fps=${FPS},format=yuv420p`,
    '-frames:v', String(frameCount),
    '-c:v', 'libx264',
    '-pix_fmt', 'yuv420p',
    '-y', outPath,
  ];
  await runFfmpeg(args);
}

export async function generateReelVideo({ id, screenshotPaths, headline, subheadline, ctaText }) {
  const usedScreenshots = screenshotPaths.slice(0, MAX_SCREENSHOTS);
  const tmpDir = path.join(GENERATED_DIR, 'tmp', id);
  await fs.mkdir(tmpDir, { recursive: true });

  const slideFiles = [];
  const durations = [];

  for (let i = 0; i < usedScreenshots.length; i += 1) {
    const screenshotBuffer = await fs.readFile(usedScreenshots[i]);
    const pngBuffer = await renderSlide({
      width: REEL_SIZE.width,
      height: REEL_SIZE.height,
      screenshotBuffer,
      headline: i === 0 ? headline : '',
      subheadline: i === 0 ? subheadline : '',
      showCta: false,
      showLogo: true,
    });
    const slidePath = path.join(tmpDir, `slide_${i}.png`);
    await fs.writeFile(slidePath, pngBuffer);
    slideFiles.push(slidePath);
    durations.push(SLIDE_DURATION);
  }

  const outroBuffer = await renderOutroSlide({
    width: REEL_SIZE.width,
    height: REEL_SIZE.height,
    headline: subheadline || headline,
    ctaText,
  });
  const outroPath = path.join(tmpDir, `slide_${slideFiles.length}.png`);
  await fs.writeFile(outroPath, outroBuffer);
  slideFiles.push(outroPath);
  durations.push(OUTRO_DURATION);

  const fileName = `${id}.mp4`;
  const outPath = path.join(GENERATED_DIR, fileName);
  const totalDuration = durations.reduce((a, b) => a + b, 0);

  const segmentFiles = [];
  for (let i = 0; i < slideFiles.length; i += 1) {
    const segmentPath = path.join(tmpDir, `seg_${i}.mp4`);
    await renderSegment({ slidePath: slideFiles[i], durationSeconds: durations[i], outPath: segmentPath });
    segmentFiles.push(segmentPath);
  }

  const listPath = path.join(tmpDir, 'list.txt');
  const listContent = segmentFiles.map((segmentPath) => `file '${segmentPath}'`).join('\n');
  await fs.writeFile(listPath, listContent);

  const args = [
    '-f', 'concat', '-safe', '0', '-i', listPath,
    '-f', 'lavfi', '-i', 'anullsrc=channel_layout=stereo:sample_rate=44100',
    '-c:v', 'copy',
    '-c:a', 'aac',
    '-b:a', '128k',
    '-shortest',
    '-movflags', '+faststart',
    '-y', outPath,
  ];

  await runFfmpeg(args);
  await fs.rm(tmpDir, { recursive: true, force: true });

  return { fileName, absolutePath: outPath, durationSeconds: totalDuration };
}
