import axios from 'axios';
import { config } from '../config.js';

function graphUrl(pathSegment) {
  return `https://graph.facebook.com/${config.graphApiVersion}/${pathSegment}`;
}

async function createContainer({ mediaUrl, mediaType, caption }) {
  const { businessAccountId, accessToken } = config.instagram;
  const params = {
    caption,
    access_token: accessToken,
    ...(mediaType === 'REELS'
      ? { media_type: 'REELS', video_url: mediaUrl }
      : { image_url: mediaUrl }),
  };
  const { data } = await axios.post(graphUrl(`${businessAccountId}/media`), null, { params });
  return data.id;
}

async function waitForContainerReady(containerId, { intervalMs = 3000, timeoutMs = 120000 } = {}) {
  const { accessToken } = config.instagram;
  const deadline = Date.now() + timeoutMs;

  while (Date.now() < deadline) {
    const { data } = await axios.get(graphUrl(containerId), {
      params: { fields: 'status_code', access_token: accessToken },
    });
    if (data.status_code === 'FINISHED') return;
    if (data.status_code === 'ERROR') {
      throw new Error(`Instagram media container ${containerId} failed processing`);
    }
    await new Promise((resolve) => setTimeout(resolve, intervalMs));
  }
  throw new Error(`Instagram media container ${containerId} did not finish processing in time`);
}

async function publishContainer(containerId) {
  const { businessAccountId, accessToken } = config.instagram;
  const { data } = await axios.post(graphUrl(`${businessAccountId}/media_publish`), null, {
    params: { creation_id: containerId, access_token: accessToken },
  });
  return data.id;
}

export async function publishToInstagram({ mediaUrl, mediaType, caption }) {
  const containerId = await createContainer({ mediaUrl, mediaType, caption });
  await waitForContainerReady(containerId);
  const mediaId = await publishContainer(containerId);
  return { containerId, mediaId };
}
