import { BRAND } from '../brand.js';

export function buildCaption({ headline, subheadline, ctaText, hashtags }) {
  const lines = [];

  lines.push(`🧭 ${headline || BRAND.tagline}`);
  if (subheadline) {
    lines.push('');
    lines.push(subheadline);
  }

  lines.push('');
  lines.push(`📲 ${ctaText || BRAND.defaultCta}`);
  lines.push('');
  lines.push(buildHashtags(hashtags));

  return lines.join('\n');
}

export function buildHashtags(extra = []) {
  const tags = new Set([...BRAND.hashtags, ...extra]);
  return [...tags].map((t) => `#${t.replace(/\s+/g, '')}`).join(' ');
}
