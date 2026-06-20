import sharp from 'sharp';
import { BRAND } from '../brand.js';
import { wrapText, escapeXml } from './textWrap.js';

async function screenshotToDataUri(screenshotBuffer) {
  const resized = await sharp(screenshotBuffer)
    .resize({ width: 900, withoutEnlargement: false })
    .jpeg({ quality: 88 })
    .toBuffer();
  return `data:image/jpeg;base64,${resized.toString('base64')}`;
}

function renderTextBlock(lines, { x, y, fontSize, lineHeight, fill, weight = 700, anchor = 'middle' }) {
  return lines
    .map(
      (line, i) =>
        `<text x="${x}" y="${y + i * lineHeight}" font-size="${fontSize}" font-weight="${weight}" fill="${fill}" text-anchor="${anchor}" font-family="Noto Sans Hebrew, DejaVu Sans, sans-serif" direction="rtl">${escapeXml(
          line
        )}</text>`
    )
    .join('\n');
}

/**
 * Builds a single branded slide as a PNG buffer: gradient background, decorative
 * compass rose, a phone mockup containing the screenshot, headline/subheadline
 * text and an optional CTA pill. Shared by the static post image generator and
 * the reel slideshow generator so both outputs look like the same product.
 */
export async function renderSlide({
  width,
  height,
  screenshotBuffer,
  headline,
  subheadline,
  ctaText,
  showCta = false,
  showLogo = true,
}) {
  const { primary, primaryDark, accent, light } = BRAND.colors;
  const pad = width * 0.08;
  const contentWidth = width - pad * 2;

  const logoFontSize = width * 0.034;
  const headlineFontSize = width * 0.062;
  const subFontSize = width * 0.034;
  const ctaFontSize = width * 0.032;

  const logoY = height * 0.075;
  const headlineTopY = height * 0.15;
  const headlineLineHeight = headlineFontSize * 1.18;

  const phoneTopY = height * 0.34;
  const phoneHeight = height * 0.44;
  const phoneWidth = phoneHeight * 0.5;
  const phoneX = (width - phoneWidth) / 2;
  const bezel = phoneWidth * 0.045;
  const screenX = phoneX + bezel;
  const screenY = phoneTopY + bezel;
  const screenW = phoneWidth - bezel * 2;
  const screenH = phoneHeight - bezel * 2;

  const subY = phoneTopY + phoneHeight + height * 0.05;
  const ctaY = height * 0.93;

  const headlineLines = wrapText(headline, { maxWidthPx: contentWidth, fontSize: headlineFontSize, maxLines: 2 });
  const subLines = subheadline
    ? wrapText(subheadline, { maxWidthPx: contentWidth, fontSize: subFontSize, maxLines: 1 })
    : [];

  const screenshotDataUri = await screenshotToDataUri(screenshotBuffer);

  const ctaLabel = ctaText || BRAND.defaultCta;
  const ctaPillWidth = Math.min(contentWidth, Math.max(width * 0.5, ctaLabel.length * ctaFontSize * 0.62));
  const ctaPillHeight = ctaFontSize * 2.3;

  const compassR = width * 0.55;

  const svg = `
<svg width="${width}" height="${height}" viewBox="0 0 ${width} ${height}" xmlns="http://www.w3.org/2000/svg">
  <defs>
    <linearGradient id="bg" x1="0" y1="0" x2="0" y2="1">
      <stop offset="0%" stop-color="${primary}" />
      <stop offset="100%" stop-color="${primaryDark}" />
    </linearGradient>
    <clipPath id="screenClip">
      <rect x="${screenX}" y="${screenY}" width="${screenW}" height="${screenH}" rx="${phoneWidth * 0.07}" />
    </clipPath>
  </defs>

  <rect x="0" y="0" width="${width}" height="${height}" fill="url(#bg)" />

  <g opacity="0.08" stroke="${accent}" fill="none" stroke-width="3">
    <circle cx="${width * 0.85}" cy="${height * 0.04}" r="${compassR}" />
    <line x1="${width * 0.85 - compassR}" y1="${height * 0.04}" x2="${width * 0.85 + compassR}" y2="${height * 0.04}" />
    <line x1="${width * 0.85}" y1="${height * 0.04 - compassR}" x2="${width * 0.85}" y2="${height * 0.04 + compassR}" />
  </g>

  ${
    showLogo
      ? `<text x="${width / 2}" y="${logoY}" font-size="${logoFontSize}" font-weight="700" fill="${accent}" text-anchor="middle" font-family="Noto Sans Hebrew, DejaVu Sans, sans-serif">🧭 THE COMPASS</text>`
      : ''
  }

  ${renderTextBlock(headlineLines, {
    x: width / 2,
    y: headlineTopY,
    fontSize: headlineFontSize,
    lineHeight: headlineLineHeight,
    fill: '#FFFFFF',
    weight: 800,
  })}

  <rect x="${phoneX}" y="${phoneTopY}" width="${phoneWidth}" height="${phoneHeight}" rx="${phoneWidth * 0.12}" fill="#10141A" stroke="#2A2F38" stroke-width="3" />
  <image x="${screenX}" y="${screenY}" width="${screenW}" height="${screenH}" href="${screenshotDataUri}" preserveAspectRatio="xMidYMid slice" clip-path="url(#screenClip)" />

  ${renderTextBlock(subLines, {
    x: width / 2,
    y: subY,
    fontSize: subFontSize,
    lineHeight: subFontSize * 1.3,
    fill: light,
    weight: 500,
  })}

  ${
    showCta
      ? `<rect x="${(width - ctaPillWidth) / 2}" y="${ctaY - ctaPillHeight * 0.7}" width="${ctaPillWidth}" height="${ctaPillHeight}" rx="${ctaPillHeight / 2}" fill="${accent}" />
         <text x="${width / 2}" y="${ctaY}" font-size="${ctaFontSize}" font-weight="700" fill="${primaryDark}" text-anchor="middle" font-family="Noto Sans Hebrew, DejaVu Sans, sans-serif" direction="rtl">${escapeXml(
          ctaLabel
        )}</text>`
      : ''
  }
</svg>`;

  return sharp(Buffer.from(svg)).png().toBuffer();
}

export async function renderOutroSlide({ width, height, headline, ctaText }) {
  const { primary, primaryDark, accent, light } = BRAND.colors;
  const logoFontSize = width * 0.07;
  const taglineFontSize = width * 0.038;
  const ctaFontSize = width * 0.034;
  const compassR = width * 0.45;

  const headlineLines = wrapText(headline || BRAND.tagline, {
    maxWidthPx: width * 0.8,
    fontSize: taglineFontSize,
    maxLines: 2,
  });

  const svg = `
<svg width="${width}" height="${height}" viewBox="0 0 ${width} ${height}" xmlns="http://www.w3.org/2000/svg">
  <defs>
    <linearGradient id="bg" x1="0" y1="0" x2="0" y2="1">
      <stop offset="0%" stop-color="${primary}" />
      <stop offset="100%" stop-color="${primaryDark}" />
    </linearGradient>
  </defs>
  <rect x="0" y="0" width="${width}" height="${height}" fill="url(#bg)" />
  <g opacity="0.1" stroke="${accent}" fill="none" stroke-width="3">
    <circle cx="${width / 2}" cy="${height * 0.4}" r="${compassR}" />
  </g>
  <text x="${width / 2}" y="${height * 0.36}" font-size="${logoFontSize}" font-weight="800" fill="${accent}" text-anchor="middle" font-family="Noto Sans Hebrew, DejaVu Sans, sans-serif">🧭</text>
  <text x="${width / 2}" y="${height * 0.46}" font-size="${width * 0.06}" font-weight="800" fill="#FFFFFF" text-anchor="middle" font-family="Noto Sans Hebrew, DejaVu Sans, sans-serif">THE COMPASS</text>
  ${renderTextBlock(headlineLines, {
    x: width / 2,
    y: height * 0.55,
    fontSize: taglineFontSize,
    lineHeight: taglineFontSize * 1.3,
    fill: light,
    weight: 500,
  })}
  <rect x="${width * 0.2}" y="${height * 0.85}" width="${width * 0.6}" height="${ctaFontSize * 2.3}" rx="${(ctaFontSize * 2.3) / 2}" fill="${accent}" />
  <text x="${width / 2}" y="${height * 0.85 + ctaFontSize * 1.55}" font-size="${ctaFontSize}" font-weight="700" fill="${primaryDark}" text-anchor="middle" font-family="Noto Sans Hebrew, DejaVu Sans, sans-serif" direction="rtl">${escapeXml(
    ctaText || BRAND.defaultCta
  )}</text>
</svg>`;

  return sharp(Buffer.from(svg)).png().toBuffer();
}
