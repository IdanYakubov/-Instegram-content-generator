// Rough heuristic word-wrap for SVG text (no real text-measurement available here).
// Hebrew/Latin glyphs in Noto Sans average ~0.56 * fontSize in width.
export function wrapText(text, { maxWidthPx, fontSize, maxLines = 2 }) {
  if (!text) return [];
  const charWidth = fontSize * 0.56;
  const maxChars = Math.max(4, Math.floor(maxWidthPx / charWidth));

  const words = text.trim().split(/\s+/);
  const lines = [];
  let current = '';

  for (const word of words) {
    const candidate = current ? `${current} ${word}` : word;
    if (candidate.length > maxChars && current) {
      lines.push(current);
      current = word;
    } else {
      current = candidate;
    }
    if (lines.length === maxLines - 1 && current.length >= maxChars) {
      break;
    }
  }
  if (current) lines.push(current);

  if (lines.length > maxLines) {
    lines.length = maxLines;
  }

  const consumed = lines.join(' ').length;
  if (consumed < text.trim().length && lines.length === maxLines) {
    const last = lines[maxLines - 1];
    lines[maxLines - 1] = last.slice(0, Math.max(0, maxChars - 1)).trimEnd() + '…';
  }

  return lines;
}

export function escapeXml(str = '') {
  return String(str)
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&apos;');
}
