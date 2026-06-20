export async function fetchPosts() {
  const res = await fetch('/api/posts');
  if (!res.ok) throw new Error('שגיאה בטעינת הפוסטים');
  return res.json();
}

export async function createPost(formData) {
  const res = await fetch('/api/posts', { method: 'POST', body: formData });
  if (!res.ok) {
    const body = await res.json().catch(() => ({}));
    throw new Error(body.error || 'שגיאה ביצירת הפוסט');
  }
  return res.json();
}

export async function updateCaption(id, caption) {
  const res = await fetch(`/api/posts/${id}`, {
    method: 'PATCH',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ caption }),
  });
  if (!res.ok) throw new Error('שגיאה בשמירת הכיתוב');
  return res.json();
}

export async function publishPost(id) {
  const res = await fetch(`/api/posts/${id}/publish`, { method: 'POST' });
  if (!res.ok) {
    const body = await res.json().catch(() => ({}));
    throw new Error(body.error || 'שגיאה בפרסום לאינסטגרם');
  }
  return res.json();
}

export async function deletePost(id) {
  const res = await fetch(`/api/posts/${id}`, { method: 'DELETE' });
  if (!res.ok) throw new Error('שגיאה במחיקת הפוסט');
}
