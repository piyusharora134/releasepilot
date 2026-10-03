import { test, expect } from '@playwright/test'

test('login page renders', async ({ page }) => {
  await page.goto('/login')
  await expect(page.getByRole('heading', { name: /sign in/i })).toBeVisible()
  await expect(page.getByPlaceholder('Email')).toBeVisible()
})

test('register toggle works', async ({ page }) => {
  await page.goto('/login')
  await page.getByRole('button', { name: /register/i }).click()
  await expect(page.getByRole('heading', { name: /create account/i })).toBeVisible()
  await expect(page.getByPlaceholder('Name')).toBeVisible()
})
