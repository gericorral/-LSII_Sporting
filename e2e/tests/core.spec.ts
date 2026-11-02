import { expect, test } from '@playwright/test';

test('authenticated user can register, upload, search, play and comment', async ({ page }) => {
  const suffix = `${Date.now()}-${Math.floor(Math.random() * 10000)}`;
  const email = `e2e-${suffix}@example.com`;
  const username = `e2e_${suffix.replace(/[^a-zA-Z0-9]/g, '').slice(0, 40)}`;
  const password = 'E2ePassword123!';
  const videoTitle = `E2E video ${suffix}`;

  await page.goto('/#register');
  await page.getByLabel('Email address').fill(email);
  await page.getByLabel('Username').fill(username);
  await page.getByLabel('Password').fill(password);
  await page.getByRole('button', { name: 'Create my account' }).click();

  await expect(page.getByRole('heading', { name: `Welcome, ${username}!` })).toBeVisible();
  await page.getByRole('link', { name: 'Continue to videos' }).click();
  await page.getByRole('link', { name: 'Upload video' }).click();
  await expect(page.getByRole('heading', { name: 'Upload a video' })).toBeVisible();

  await page.getByLabel('Title').fill(videoTitle);
  await page.getByLabel('Description').fill('Video created by the end-to-end test.');
  await page.getByLabel('MP4 video').setInputFiles({
    name: 'e2e-video.mp4',
    mimeType: 'video/mp4',
    buffer: Buffer.from('synthetic MP4 content for the upload contract'),
  });
  await page.getByRole('button', { name: 'Upload video' }).click();

  await expect(page.getByRole('heading', { name: 'Available videos' })).toBeVisible();
  await page.getByPlaceholder('Search videos').fill(videoTitle);
  await page.getByRole('button', { name: 'Search' }).click();
  await expect(page.getByRole('heading', { name: `Search results for "${videoTitle}"` })).toBeVisible();

  await page.getByRole('link', { name: new RegExp(videoTitle) }).click();
  await expect(page.getByRole('heading', { name: videoTitle, exact: true })).toBeVisible();
  await expect(page.locator('video')).toHaveAttribute('src', /\/media\/.+\.mp4/);

  await page.getByLabel('Add a comment').fill('End-to-end comment');
  await page.getByRole('button', { name: 'Post comment' }).click();
  await expect(page.getByText('End-to-end comment')).toBeVisible();

  await page.getByRole('link', { name: 'Back to videos' }).click();
  await page.getByRole('button', { name: 'Log out' }).click();
  await page.getByRole('link', { name: 'Log in' }).click();
  await page.getByLabel('Email address').fill(email);
  await page.getByLabel('Password').fill(password);
  await page.getByRole('button', { name: 'Log in' }).click();
  await expect(page.getByRole('link', { name: `Hi, ${username}` })).toBeVisible();
});

test('unauthenticated users can search and receive an empty state', async ({ page }) => {
  await page.goto('/');
  await expect(page.getByRole('heading', { name: 'Available videos' })).toBeVisible();

  await page.getByPlaceholder('Search videos').fill('query-with-no-e2e-match');
  await page.getByRole('button', { name: 'Search' }).click();

  await expect(page.getByText('No videos found.')).toBeVisible();
});
