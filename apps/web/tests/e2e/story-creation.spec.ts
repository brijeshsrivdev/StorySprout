import { test, expect } from "@playwright/test";

test("blank story continues into outline and persists after dashboard refresh", async ({ page }) => {
  await page.goto("/");
  await page.getByRole("link", { name: "Create Story" }).first().click();
  const blankStory = page.getByRole("button", { name: "Blank Story" });
  await blankStory.click();
  await expect(blankStory).toHaveAttribute("aria-pressed", "true");
  const continueButton = page.getByRole("button", { name: "Continue" });
  await expect(continueButton).toBeEnabled();
  await continueButton.click();
  await expect(page.getByLabel("Story idea")).toBeVisible();
  await page.getByLabel("Story idea").fill("A tiny fox learns to share.");
  await page.getByLabel("Target age").selectOption("3_5");
  await page.getByRole("button", { name: "Continue" }).click();
  await expect(page).toHaveURL(/\/stories\/.+\/outline$/);
  await expect(page.getByText("STORY OUTLINE")).toBeVisible();
  await page.goto("/");
  await expect(page.getByText("Untitled Story").first()).toBeVisible({ timeout: 15000 });
  await page.reload();
  await expect(page.getByText("Untitled Story").first()).toBeVisible({ timeout: 15000 });
});

test("AI story generates and persists its draft", async ({ page }) => {
  await page.goto("/");
  await page.getByRole("link", { name: "Create Story" }).first().click();
  const aiStory = page.getByRole("button", { name: "Start with AI" });
  await aiStory.click();
  await expect(aiStory).toHaveAttribute("aria-pressed", "true");
  const continueButton = page.getByRole("button", { name: "Continue" });
  await expect(continueButton).toBeEnabled();
  await continueButton.click();
  await expect(page.getByLabel("Story idea")).toBeVisible();
  await page.getByLabel("Story idea").fill("A rabbit learns to share.");
  await page.getByLabel("Target age").selectOption("6_8");
  await page.getByRole("button", { name: "Generate Story" }).click();
  await expect(page.getByText("Story draft generated")).toBeVisible({ timeout: 15000 });
  await page.goto("/");
  await expect(page.getByText("Untitled Story").first()).toBeVisible({ timeout: 15000 });
});

test("AI failure is recoverable without losing setup", async ({ page }) => {
  await page.route("**/api/v1/projects/*/stories", async route => {
    await route.fulfill({
      status: 422,
      contentType: "application/json",
      body: JSON.stringify({ error: { code: "GENERATION_FAILED", message: "Story generation failed. Your setup was saved.", fields: [{ field: "storyId", message: "s1" }] } })
    });
  });
  await page.goto("/");
  await page.getByRole("link", { name: "Create Story" }).first().click();
  const aiStory = page.getByRole("button", { name: "Start with AI" });
  await aiStory.click();
  await expect(aiStory).toHaveAttribute("aria-pressed", "true");
  const continueButton = page.getByRole("button", { name: "Continue" });
  await expect(continueButton).toBeEnabled();
  await continueButton.click();
  await expect(page.getByLabel("Story idea")).toBeVisible();
  await page.getByLabel("Story idea").fill("A rabbit learns to share.");
  await page.getByLabel("Target age").selectOption("9_12");
  await page.getByRole("button", { name: "Generate Story" }).click();
  await expect(page.getByText(/Story generation failed/)).toBeVisible({ timeout: 15000 });
  await expect(page.getByLabel("Story idea")).toHaveValue("A rabbit learns to share.");
});
