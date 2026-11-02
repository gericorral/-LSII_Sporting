import { execFileSync } from 'node:child_process';
import path from 'node:path';
import { expect, test } from '@playwright/test';

test('video metadata survives a backend restart because it is stored in PostgreSQL', async ({ page }) => {
  const suffix = `${Date.now()}-${Math.floor(Math.random() * 10000)}`;
  const email = `restart-${suffix}@example.com`;
  const username = `restart_${suffix.replace(/[^a-zA-Z0-9]/g, '').slice(0, 40)}`;
  const videoTitle = `Restart video ${suffix}`;

  await page.goto('/#register');
  await page.getByLabel('Email address').fill(email);
  await page.getByLabel('Username').fill(username);
  await page.getByLabel('Password').fill('RestartPassword123!');
  await page.getByRole('button', { name: 'Create my account' }).click();
  await page.getByRole('link', { name: 'Continue to videos' }).click();
  await page.getByRole('link', { name: 'Upload video' }).click();
  await page.getByLabel('Title').fill(videoTitle);
  await page.getByLabel('Description').fill('Metadata must survive a backend restart.');
  await page.getByLabel('MP4 video').setInputFiles({
    name: 'restart-video.mp4',
    mimeType: 'video/mp4',
    buffer: Buffer.from('synthetic MP4 content for persistence testing'),
  });
  await page.getByRole('button', { name: 'Upload video' }).click();
  await expect(page.getByRole('heading', { name: 'Available videos' })).toBeVisible();
  await page.getByPlaceholder('Search videos').fill(videoTitle);
  await page.getByRole('button', { name: 'Search' }).click();
  await page.getByRole('link', { name: new RegExp(videoTitle) }).click();
  await expect(page.getByRole('heading', { name: videoTitle, exact: true })).toBeVisible();
  await page.locator('video').evaluate((video) => video.dispatchEvent(new Event('play')));
  await page.waitForTimeout(500);

  const composeFile = path.resolve(process.cwd(), '..', 'compose.yaml');
  execFileSync('docker', ['compose', '-f', composeFile, 'down'], { stdio: 'inherit' });
  execFileSync('docker', ['compose', '-f', composeFile, 'up', '-d', '--build'], { stdio: 'inherit' });

  const persistedTitle = execFileSync(
    'docker',
    ['exec', 'postgres_db', 'psql', '-U', 'postgres', '-d', 'protube', '-tAc',
      'SELECT title FROM videos ORDER BY created_at DESC LIMIT 1'],
    { encoding: 'utf8' },
  ).trim();
  const persistedViews = execFileSync(
    'docker',
    ['exec', 'postgres_db', 'psql', '-U', 'postgres', '-d', 'protube', '-tAc',
      'SELECT COUNT(*) FROM video_views vv JOIN videos v ON v.id = vv.video_id WHERE v.title = ' +
      `'${videoTitle}'`],
    { encoding: 'utf8' },
  ).trim();
  expect(persistedTitle).toBe(videoTitle);
  expect(persistedViews).toBe('1');
});
