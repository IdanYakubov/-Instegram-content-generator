import { useEffect, useState } from 'react';
import { fetchPosts, fetchBrand } from './api';
import PostForm from './components/PostForm';
import PostList from './components/PostList';
import BrandSettings from './components/BrandSettings';
import './App.css';

export default function App() {
  const [posts, setPosts] = useState([]);
  const [brand, setBrand] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    Promise.all([fetchPosts(), fetchBrand()])
      .then(([posts, brand]) => {
        setPosts(posts);
        setBrand(brand);
      })
      .catch(() => {})
      .finally(() => setLoading(false));
  }, []);

  function handleCreated(post) {
    setPosts((prev) => [post, ...prev]);
  }

  function handleChange(updated) {
    setPosts((prev) => prev.map((p) => (p.id === updated.id ? updated : p)));
  }

  function handleDelete(id) {
    setPosts((prev) => prev.filter((p) => p.id !== id));
  }

  const cssVars = brand
    ? {
        '--primary': brand.colors.primary,
        '--primary-dark': brand.colors.primaryDark,
        '--accent': brand.colors.accent,
        '--light': brand.colors.light,
      }
    : undefined;

  return (
    <div className="app" style={cssVars}>
      <header className="app-header">
        <h1>{brand ? `${brand.logoEmoji} ${brand.name} — מחולל תוכן לאינסטגרם` : 'מחולל תוכן לאינסטגרם'}</h1>
        <p>הכנת פוסטים ורילסים ממותגים, ופרסום ישיר לאינסטגרם</p>
      </header>

      <main className="app-main">
        <div className="form-column">
          {brand && <BrandSettings brand={brand} onChange={setBrand} />}
          <PostForm onCreated={handleCreated} brand={brand} />
        </div>
        <section className="posts-section">
          <h2>תוכן שנוצר</h2>
          {loading ? <p>טוען...</p> : <PostList posts={posts} onChange={handleChange} onDelete={handleDelete} />}
        </section>
      </main>
    </div>
  );
}
