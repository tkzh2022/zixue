import { test, expect } from '@playwright/test';

const mockLoginResponse = {
  code: 0,
  message: 'success',
  data: {
    accessToken: 'mock-access-token',
    refreshToken: 'mock-refresh-token',
    user: {
      id: 1,
      username: 'admin',
      role: 'LIBRARIAN',
      name: 'Admin User',
    },
  },
  traceId: 'e2e-trace-id',
};

const mockDashboardResponse = {
  code: 0,
  message: 'success',
  data: {
    totalBooks: 100,
    totalReaders: 50,
    activeBorrows: 10,
    unpaidFines: 0,
  },
  traceId: 'e2e-trace-id',
};

test.describe('Login', () => {
  test('allows a librarian to log in and reach the dashboard', async ({ page }) => {
    await page.route('**/api/v1/auth/login', async (route) => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify(mockLoginResponse),
      });
    });

    await page.route('**/api/v1/dashboard/admin', async (route) => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify(mockDashboardResponse),
      });
    });

    await page.goto('/login');

    await expect(page.getByRole('heading', { name: /Login|登录/ })).toBeVisible();

    await page.getByPlaceholder('Username').fill('admin');
    await page.getByPlaceholder('Password').fill('password123');

    const loginRequest = page.waitForResponse(
      (resp) => resp.url().includes('/api/v1/auth/login') && resp.request().method() === 'POST',
    );
    await page.getByRole('button', { name: /Login|登录/ }).click();
    await loginRequest;

    // Verify auth state was persisted on successful login
    await expect.poll(async () => page.evaluate(() => localStorage.getItem('library:role'))).toBe('LIBRARIAN');
    await expect
      .poll(async () => page.evaluate(() => localStorage.getItem('library:access_token')))
      .not.toBeNull();

    // Navigate to dashboard via router; route guard will allow LIBRARIAN
    await page.goto('/dashboard');

    await expect(page).toHaveURL(/\/dashboard$/);
    await expect(page.getByRole('heading', { name: /Dashboard|控制台/ })).toBeVisible();
  });
});
