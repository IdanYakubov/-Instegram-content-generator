import express from 'express';
import cors from 'cors';
import { config, GENERATED_DIR } from './config.js';
import postsRouter from './routes/posts.js';

const app = express();

app.use(cors());
app.use(express.json());
app.use('/media', express.static(GENERATED_DIR));
app.use('/api/posts', postsRouter);

app.get('/api/health', (req, res) => {
  res.json({ ok: true });
});

app.listen(config.port, () => {
  console.log(`The Compass content backend listening on port ${config.port}`);
});
