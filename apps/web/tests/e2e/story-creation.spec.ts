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
  await expect(page.locator("label").filter({ hasText: "Story idea" }).first()).toHaveAttribute("for", "idea");
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
  await expect(page.getByRole("heading", { name: "Your Story Draft" })).toBeVisible({ timeout: 15000 });
  await expect(page.getByText("AI proposal", { exact: true })).toBeVisible();
  await expect(page.getByText(/review and shape/i)).toBeVisible();
  await page.getByRole("button", { name: "Continue to Story Outline →" }).click();
  await expect(page).toHaveURL(/\/stories\/.+\/outline$/);
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
});

test("Create Story presents two deliberate creative paths", async ({ page }) => {
  await page.goto("/create");
  await expect(page.getByRole("heading", { name: "Choose how your story begins." })).toBeVisible();
  const ai = page.getByRole("button", { name: /Start with AI/ });
  const blank = page.getByRole("button", { name: /Start Blank/ });
  await expect(ai).toHaveAttribute("aria-pressed", "false");
  await expect(blank).toHaveAttribute("aria-pressed", "false");
  await expect(page.getByRole("button", { name: "Continue →" })).toBeDisabled();
  await ai.click();
  await expect(ai).toHaveAttribute("aria-pressed", "true");
  await expect(page.getByRole("button", { name: "Continue →" })).toBeEnabled();
  await blank.click();
  await expect(blank).toHaveAttribute("aria-pressed", "true");
  await expect(ai).toHaveAttribute("aria-pressed", "false");
});
