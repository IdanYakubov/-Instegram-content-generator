import express from 'express';
import multer from 'multer';
import fs from 'fs/promises';
import path from 'path';
import { nanoid } from 'nanoid';
import { db } from '../db.js';
import { config, UPLOADS_DIR, GENERATED_DIR } from '../config.js';
import { generatePostImage } from '../services/imageGenerator.js';
import { generateReelVideo } from '../services/videoGenerator.js';
import { buildCaption } from '../services/captionGenerator.js';
import { publishToInstagram } from '../services/instagram.js';

const upload = multer({ dest: UPLOADS_DIR });
const router = express.Router();

router.get('/', async (req, res) => {
  await db.read();
  res.json(db.data.posts);
});

router.get('/:id', async (req, res) => {
  await db.read();
  const post = db.data.posts.find((p) => p.id === req.params.id);
  if (!post) return res.status(404).json({ error: 'Post not found' });
  res.json(post);
});

router.post('/', upload.array('screenshots', 8), async (req, res) => {
  const { type = 'post', headline, subheadline, ctaText } = req.body;
  const hashtags = req.body.hashtags ? req.body.hashtags.split(',').map((t) => t.trim()).filter(Boolean) : [];
  const screenshotPaths = (req.files || []).map((f) => f.path);

  if (screenshotPaths.length === 0) {
    return res.status(400).json({ error: 'At least one screenshot is required' });
  }
  if (!headline) {
    return res.status(400).json({ error: 'headline is required' });
  }

  const id = nanoid(10);

  let mediaFileName;
  if (type === 'reel') {
    const result = await generateReelVideo({ id, screenshotPaths, headline, subheadline, ctaText });
    mediaFileName = result.fileName;
  } else {
    const result = await generatePostImage({ id, screenshotPath: screenshotPaths[0], headline, subheadline, ctaText });
    mediaFileName = result.fileName;
  }

  await Promise.all(screenshotPaths.map((p) => fs.rm(p, { force: true })));

  const caption = buildCaption({ headline, subheadline, ctaText, hashtags });

  const post = {
    id,
    type,
    mediaFileName,
    headline,
    subheadline: subheadline || '',
    ctaText: ctaText || '',
    caption,
    status: 'draft',
    igMediaId: null,
    createdAt: new Date().toISOString(),
  };

  await db.read();
  db.data.posts.unshift(post);
  await db.write();

  res.status(201).json(post);
});

router.patch('/:id', async (req, res) => {
  await db.read();
  const post = db.data.posts.find((p) => p.id === req.params.id);
  if (!post) return res.status(404).json({ error: 'Post not found' });

  const { caption } = req.body;
  if (caption !== undefined) post.caption = caption;

  await db.write();
  res.json(post);
});

router.post('/:id/publish', async (req, res) => {
  await db.read();
  const post = db.data.posts.find((p) => p.id === req.params.id);
  if (!post) return res.status(404).json({ error: 'Post not found' });
  if (post.status === 'published') {
    return res.status(400).json({ error: 'Post already published' });
  }
  if (!config.instagram.businessAccountId || !config.instagram.accessToken) {
    return res.status(400).json({ error: 'Instagram credentials are not configured' });
  }

  const mediaUrl = `${config.publicBaseUrl}/media/${post.mediaFileName}`;
  const mediaType = post.type === 'reel' ? 'REELS' : 'IMAGE';

  try {
    const { mediaId } = await publishToInstagram({ mediaUrl, mediaType, caption: post.caption });
    post.status = 'published';
    post.igMediaId = mediaId;
    post.publishedAt = new Date().toISOString();
    await db.write();
    res.json(post);
  } catch (err) {
    res.status(502).json({ error: 'Failed to publish to Instagram', details: err.message });
  }
});

router.delete('/:id', async (req, res) => {
  await db.read();
  const index = db.data.posts.findIndex((p) => p.id === req.params.id);
  if (index === -1) return res.status(404).json({ error: 'Post not found' });

  const [post] = db.data.posts.splice(index, 1);
  await db.write();

  await fs.rm(path.join(GENERATED_DIR, post.mediaFileName), { force: true });

  res.status(204).send();
});

export default router;
