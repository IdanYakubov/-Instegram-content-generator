export function buildCaption({ brand, headline, subheadline, ctaText, hashtags }) {
  const lines = [];

  lines.push(`${brand.logoEmoji} ${headline || brand.tagline}`);
  if (subheadline) {
    lines.push('');
    lines.push(subheadline);
  }

  lines.push('');
  lines.push(`📲 ${ctaText || brand.defaultCta}`);
  lines.push('');
  lines.push(buildHashtags(brand, hashtags));

  return lines.join('\n');
}

export function buildHashtags(brand, extra = []) {
  const tags = new Set([...brand.hashtags, ...extra]);
  return [...tags].map((t) => `#${t.replace(/\s+/g, '')}`).join(' ');
}
