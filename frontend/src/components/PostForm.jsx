import { useState } from 'react';
import { createPost } from '../api';

export default function PostForm({ onCreated, brand }) {
  const [type, setType] = useState('post');
  const [headline, setHeadline] = useState('');
  const [subheadline, setSubheadline] = useState('');
  const [ctaText, setCtaText] = useState('');
  const [hashtags, setHashtags] = useState('');
  const [files, setFiles] = useState([]);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState('');

  async function handleSubmit(e) {
    e.preventDefault();
    if (files.length === 0 || !headline.trim()) {
      setError('יש להעלות לפחות צילום מסך אחד ולמלא כותרת');
      return;
    }
    setSubmitting(true);
    setError('');
    try {
      const formData = new FormData();
      formData.set('type', type);
      formData.set('headline', headline);
      formData.set('subheadline', subheadline);
      formData.set('ctaText', ctaText);
      formData.set('hashtags', hashtags);
      files.forEach((file) => formData.append('screenshots', file));

      const post = await createPost(formData);
      onCreated(post);
      setHeadline('');
      setSubheadline('');
      setCtaText('');
      setHashtags('');
      setFiles([]);
      e.target.reset();
    } catch (err) {
      setError(err.message);
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <form className="post-form" onSubmit={handleSubmit}>
      <h2>יצירת תוכן חדש</h2>

      <div className="field-row">
        <label>
          <input type="radio" name="type" checked={type === 'post'} onChange={() => setType('post')} />
          פוסט (תמונה)
        </label>
        <label>
          <input type="radio" name="type" checked={type === 'reel'} onChange={() => setType('reel')} />
          רילס (וידאו)
        </label>
      </div>

      <label className="field">
        צילומי מסך {type === 'reel' ? '(ניתן להעלות כמה, לפי סדר התצוגה)' : '(התמונה הראשונה תשמש את הפוסט)'}
        <input
          type="file"
          accept="image/*"
          multiple={type === 'reel'}
          onChange={(e) => setFiles(Array.from(e.target.files))}
        />
      </label>

      <label className="field">
        כותרת
        <input value={headline} onChange={(e) => setHeadline(e.target.value)} placeholder={brand?.tagline || 'כותרת ראשית'} />
      </label>

      <label className="field">
        כותרת משנה
        <input
          value={subheadline}
          onChange={(e) => setSubheadline(e.target.value)}
          placeholder="כותרת משנה (אופציונלי)"
        />
      </label>

      <label className="field">
        טקסט לכפתור הקריאה לפעולה
        <input value={ctaText} onChange={(e) => setCtaText(e.target.value)} placeholder={brand?.defaultCta || 'קריאה לפעולה'} />
      </label>

      <label className="field">
        האשטגים נוספים (מופרדים בפסיק)
        <input value={hashtags} onChange={(e) => setHashtags(e.target.value)} placeholder="חדשנות, סטארטאפ" />
      </label>

      {error && <p className="error">{error}</p>}

      <button type="submit" disabled={submitting}>
        {submitting ? 'יוצר תוכן...' : 'צרו תוכן'}
      </button>
    </form>
  );
}
