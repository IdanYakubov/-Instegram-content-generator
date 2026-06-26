import { useState } from 'react';
import { updateBrand } from '../api';

export default function BrandSettings({ brand, onChange }) {
  const [open, setOpen] = useState(false);
  const [form, setForm] = useState(brand);
  const [hashtagsText, setHashtagsText] = useState(brand.hashtags.join(', '));
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');

  function setField(field, value) {
    setForm((prev) => ({ ...prev, [field]: value }));
  }

  function setColor(field, value) {
    setForm((prev) => ({ ...prev, colors: { ...prev.colors, [field]: value } }));
  }

  async function handleSave(e) {
    e.preventDefault();
    setSaving(true);
    setError('');
    try {
      const hashtags = hashtagsText.split(',').map((t) => t.trim()).filter(Boolean);
      const updated = await updateBrand({ ...form, hashtags });
      onChange(updated);
      setOpen(false);
    } catch (err) {
      setError(err.message);
    } finally {
      setSaving(false);
    }
  }

  if (!open) {
    return (
      <div className="brand-settings-collapsed">
        <button type="button" onClick={() => setOpen(true)}>
          ⚙️ הגדרות מותג ({brand.name})
        </button>
      </div>
    );
  }

  return (
    <form className="post-form brand-settings" onSubmit={handleSave}>
      <h2>הגדרות מותג</h2>

      <label className="field">
        שם המותג
        <input value={form.name} onChange={(e) => setField('name', e.target.value)} />
      </label>

      <label className="field">
        אמוג'י לוגו
        <input value={form.logoEmoji} onChange={(e) => setField('logoEmoji', e.target.value)} maxLength={4} />
      </label>

      <label className="field">
        סלוגן (כותרת ברירת מחדל)
        <input value={form.tagline} onChange={(e) => setField('tagline', e.target.value)} />
      </label>

      <label className="field">
        קריאה לפעולה (ברירת מחדל)
        <input value={form.defaultCta} onChange={(e) => setField('defaultCta', e.target.value)} />
      </label>

      <label className="field">
        האשטגים קבועים (מופרדים בפסיק)
        <input value={hashtagsText} onChange={(e) => setHashtagsText(e.target.value)} />
      </label>

      <div className="field-row brand-colors">
        <label className="field">
          צבע ראשי
          <input type="color" value={form.colors.primary} onChange={(e) => setColor('primary', e.target.value)} />
        </label>
        <label className="field">
          צבע ראשי כהה
          <input type="color" value={form.colors.primaryDark} onChange={(e) => setColor('primaryDark', e.target.value)} />
        </label>
        <label className="field">
          צבע הדגשה
          <input type="color" value={form.colors.accent} onChange={(e) => setColor('accent', e.target.value)} />
        </label>
        <label className="field">
          צבע בהיר
          <input type="color" value={form.colors.light} onChange={(e) => setColor('light', e.target.value)} />
        </label>
      </div>

      {error && <p className="error">{error}</p>}

      <div className="post-actions">
        <button type="submit" disabled={saving}>
          {saving ? 'שומר...' : 'שמירת מותג'}
        </button>
        <button type="button" onClick={() => setOpen(false)}>
          ביטול
        </button>
      </div>
    </form>
  );
}
