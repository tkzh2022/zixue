import { test, expect } from '@playwright/test';

test.describe('full-stack library workflow', () => {
  test.skip(!process.env.E2E_FULLSTACK_URL, 'requires the Docker full-stack environment');

  test('registers a reader and completes borrow, renew and return', async ({ page, request }) => {
    const suffix = Date.now().toString();
    const username = `reader${suffix}`;
    const password = 'readerPass123';
    const barcode = `E2E-COPY-${suffix}`;

    const registration = await request.post('/api/v1/auth/register', {
      data: {
        username,
        password,
        name: 'E2E Reader',
        phone: '13800138000',
        email: `${username}@example.com`,
      },
    });
    expect(registration.ok()).toBeTruthy();
    const registered = await registration.json();
    expect(registered.code).toBe(0);
    const readerNo = `R${String(registered.data.user.id).padStart(8, '0')}`;

    const librarianLogin = await request.post('/api/v1/auth/login', {
      data: { username: 'librarian', password: 'librarian123' },
    });
    expect(librarianLogin.ok()).toBeTruthy();
    const librarianToken = (await librarianLogin.json()).data.accessToken;
    const headers = { Authorization: `Bearer ${librarianToken}` };

    const createdBook = await request.post('/api/v1/books', {
      headers,
      data: {
        isbn: `E2E-${suffix}`,
        title: `E2E Book ${suffix}`,
        publisher: 'Codex Press',
        publishYear: 2026,
        location: 'E2E-1',
        summary: 'Full-stack test fixture',
        authorNames: ['Codex'],
        categoryCodes: ['TEST'],
      },
    });
    expect(createdBook.ok()).toBeTruthy();
    const bookId = (await createdBook.json()).data;
    const addedCopy = await request.post(`/api/v1/books/${bookId}/copies`, {
      headers,
      data: { barcode },
    });
    expect(addedCopy.ok()).toBeTruthy();

    await page.goto('/login');
    await page.getByPlaceholder('Username').fill('librarian');
    await page.getByPlaceholder('Password').fill('librarian123');
    await page.getByRole('button', { name: /Login|登录/ }).click();
    await expect(page).toHaveURL(/\/dashboard$/);

    await page.goto('/admin/borrows');
    await page.getByRole('textbox', { name: 'Reader No' }).fill(readerNo);
    await page.getByRole('textbox', { name: 'Barcode' }).first().fill(barcode);
    await page.getByRole('button', { name: 'Borrow', exact: true }).click();
    await expect(page.getByText('Book borrowed successfully')).toBeVisible();

    await page.evaluate(() => localStorage.clear());
    await page.goto('/login');
    await page.getByPlaceholder('Username').fill(username);
    await page.getByPlaceholder('Password').fill(password);
    await page.getByRole('button', { name: /Login|登录/ }).click();
    await expect(page).toHaveURL(/\/catalog$/);

    await page.goto('/reader/borrows');
    await expect(page.getByText('BORROWING')).toBeVisible();
    await page.getByRole('button', { name: 'Renew' }).click();
    await expect(page.getByText('Book renewed successfully')).toBeVisible();

    const returned = await request.put(`/api/v1/borrows/return/${barcode}`, { headers });
    expect(returned.ok()).toBeTruthy();
    const returnedBody = await returned.json();
    expect(returnedBody.code).toBe(0);
  });
});
