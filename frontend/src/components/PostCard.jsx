import { useState } from 'react';
import { updateCaption, publishPost, deletePost } from '../api';

const STATUS_LABELS = {
  draft: 'טיוטה',
  published: 'פורסם',
};

export default function PostCard({ post, onChange, onDelete }) {
  const [caption, setCaption] = useState(post.caption);
  const [saving, setSaving] = useState(false);
  const [publishing, setPublishing] = useState(false);
  const [error, setError] = useState('');

  const mediaUrl = `/media/${post.mediaFileName}`;
  const captionDirty = caption !== post.caption;

  async function handleSaveCaption() {
    setSaving(true);
    setError('');
    try {
      const updated = await updateCaption(post.id, caption);
      onChange(updated);
    } catch (err) {
      setError(err.message);
    } finally {
      setSaving(false);
    }
  }

  async function handlePublish() {
    setPublishing(true);
    setError('');
    try {
      const updated = await publishPost(post.id);
      onChange(updated);
    } catch (err) {
      setError(err.message);
    } finally {
      setPublishing(false);
    }
  }

  async function handleDelete() {
    if (!confirm('למחוק את הפוסט הזה?')) return;
    await deletePost(post.id);
    onDelete(post.id);
  }

  return (
    <div className="post-card">
      <div className="post-media">
        {post.type === 'reel' ? (
          <video src={mediaUrl} controls />
        ) : (
          <img src={mediaUrl} alt={post.headline} />
        )}
      </div>

      <div className="post-details">
        <div className="post-meta">
          <span className={`status status-${post.status}`}>{STATUS_LABELS[post.status]}</span>
          <span className="post-type">{post.type === 'reel' ? 'רילס' : 'פוסט'}</span>
        </div>

        <textarea
          className="caption-editor"
          value={caption}
          onChange={(e) => setCaption(e.target.value)}
          rows={10}
        />

        {error && <p className="error">{error}</p>}

        <div className="post-actions">
          {captionDirty && (
            <button onClick={handleSaveCaption} disabled={saving}>
              {saving ? 'שומר...' : 'שמירת כיתוב'}
            </button>
          )}
          {post.status !== 'published' && (
            <button className="primary" onClick={handlePublish} disabled={publishing || captionDirty}>
              {publishing ? 'מפרסם...' : 'פרסום לאינסטגרם'}
            </button>
          )}
          <button className="danger" onClick={handleDelete}>
            מחיקה
          </button>
        </div>
      </div>
    </div>
  );
}
