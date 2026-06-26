import express from 'express';
import { getBrand, updateBrand } from '../brandStore.js';

const router = express.Router();

router.get('/', async (req, res) => {
  res.json(await getBrand());
});

router.put('/', async (req, res) => {
  const { name, logoEmoji, tagline, defaultCta, hashtags, colors } = req.body;
  const patch = {};
  if (name !== undefined) patch.name = name;
  if (logoEmoji !== undefined) patch.logoEmoji = logoEmoji;
  if (tagline !== undefined) patch.tagline = tagline;
  if (defaultCta !== undefined) patch.defaultCta = defaultCta;
  if (Array.isArray(hashtags)) patch.hashtags = hashtags;
  if (colors && typeof colors === 'object') patch.colors = colors;

  res.json(await updateBrand(patch));
});

export default router;
