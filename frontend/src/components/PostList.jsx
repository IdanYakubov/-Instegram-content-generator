import PostCard from './PostCard';

export default function PostList({ posts, onChange, onDelete }) {
  if (posts.length === 0) {
    return <p className="empty-state">עדיין לא נוצר תוכן. מלאו את הטופס כדי ליצור פוסט או רילס ראשון.</p>;
  }

  return (
    <div className="post-list">
      {posts.map((post) => (
        <PostCard key={post.id} post={post} onChange={onChange} onDelete={onDelete} />
      ))}
    </div>
  );
}
