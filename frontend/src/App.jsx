import { useEffect, useState } from 'react';
import { fetchPosts } from './api';
import PostForm from './components/PostForm';
import PostList from './components/PostList';
import './App.css';

export default function App() {
  const [posts, setPosts] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchPosts()
      .then(setPosts)
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

  return (
    <div className="app">
      <header className="app-header">
        <h1>🧭 The Compass — מחולל תוכן לאינסטגרם</h1>
        <p>הכנת פוסטים ורילסים ממותגים לשיווק האפליקציה, ופרסום ישיר לאינסטגרם</p>
      </header>

      <main className="app-main">
        <PostForm onCreated={handleCreated} />
        <section className="posts-section">
          <h2>תוכן שנוצר</h2>
          {loading ? <p>טוען...</p> : <PostList posts={posts} onChange={handleChange} onDelete={handleDelete} />}
        </section>
      </main>
    </div>
  );
}
