import { expect, test } from "@playwright/test";

test("creates an outline, shows planned duration and preserves it after refresh", async ({ page }) => {
  await page.goto("/create");
  const blankStory = page.getByRole("button", { name: "Blank Story" });
  await blankStory.click();
  await expect(blankStory).toHaveAttribute("aria-pressed", "true");
  await page.getByRole("button", { name: "Continue" }).click();
  await page.getByLabel("Story idea").fill("A rabbit learns to share.");
  await page.getByRole("button", { name: "Ages 6–8" }).click();
  await page.getByRole("button", { name: "Continue" }).click();
  await expect(page).toHaveURL(/\/stories\/.+\/outline$/);
  await expect(page.getByRole("heading", { name: "Storyboard" })).toBeVisible();
  await expect(page.getByText("Target", { exact: true })).toBeVisible();
  await expect(page.getByRole("button", { name: "+ Add Scene" })).toBeVisible();
  await page.getByRole("button", { name: "+ Add Scene" }).click();
  await page.getByLabel("Scene 1 title").fill("A sharing lesson");
  await page.getByLabel("Scene 1 summary").fill("The rabbit learns to share a carrot with a friend.");
  await page.getByLabel("Scene 1 duration").fill("190");
  await page.getByRole("button", { name: "Save Changes" }).click();
  await expect(page.getByText("Saved", { exact: true })).toBeVisible({ timeout: 15000 });
  await expect(page.getByText("Planned", { exact: true }).locator("..")).toContainText("3:10");
  await expect(page.getByText("+0:10")).toBeVisible();
  await page.reload();
  await expect(page.getByLabel("Scene 1 title")).toHaveValue("A sharing lesson");
  await expect(page.getByLabel("Scene 1 duration")).toHaveValue("190");
});

test("generates an outline and reorders scenes without changing durations", async ({ page }) => {
  await page.goto("/create");
  const blankStory = page.getByRole("button", { name: "Blank Story" });
  await blankStory.click();
  await expect(blankStory).toHaveAttribute("aria-pressed", "true");
  await page.getByRole("button", { name: "Continue" }).click();
  await page.getByLabel("Story idea").fill("A small friend learns kindness.");
  await page.getByRole("button", { name: "Ages 6–8" }).click();
  await page.getByRole("button", { name: "Continue" }).click();
  await expect(page).toHaveURL(/\/stories\/.+\/outline$/);
  await page.getByRole("button", { name: "Generate Outline" }).click();
  await expect(page.getByLabel("Scene 1 title")).toHaveValue("A Small Problem");
  await expect(page.getByText("AI-generated starting point", { exact: true })).toBeVisible();
  const firstDuration = page.getByLabel("Scene 1 duration");
  const secondDuration = page.getByLabel("Scene 2 duration");
  const firstValue = await firstDuration.inputValue();
  const secondValue = await secondDuration.inputValue();
  await page.getByRole("button", { name: "Move scene 2 up" }).click();
  await expect(page.getByText("Creator-shaped outline", { exact: true })).toBeVisible();
  await page.getByRole("button", { name: "Save Changes" }).click();
  await expect(page.getByLabel("Scene 1 duration")).toHaveValue(secondValue);
  await expect(page.getByLabel("Scene 2 duration")).toHaveValue(firstValue);
});

test("prevents continuing with unsaved outline edits", async ({ page }) => {
  await page.goto("/create");
  const blankStory = page.getByRole("button", { name: "Blank Story" });
  await blankStory.click();
  await page.getByRole("button", { name: "Continue" }).click();
  await page.getByLabel("Story idea").fill("A small friend learns kindness.");
  await page.getByRole("button", { name: "Ages 6–8" }).click();
  await page.getByRole("button", { name: "Continue" }).click();
  await expect(page).toHaveURL(/\/stories\/.+\/outline$/);
  await page.getByRole("button", { name: "Generate Outline" }).click();
  await page.getByLabel("Scene 1 title").fill("Creator edit");
  const continueLink = page.getByRole("link", { name: "Continue to Characters →" });
  await expect(continueLink).toHaveAttribute("aria-disabled", "true");
  await expect(page.getByText("Unsaved changes", { exact: true })).toBeVisible();
});

test("supports keyboard scene reordering", async ({ page }) => {
  await page.goto("/create");
  const blankStory = page.getByRole("button", { name: "Blank Story" });
  await blankStory.click();
  await page.getByRole("button", { name: "Continue" }).click();
  await page.getByLabel("Story idea").fill("A small friend learns kindness.");
  await page.getByRole("button", { name: "Ages 6–8" }).click();
  await page.getByRole("button", { name: "Continue" }).click();
  await expect(page).toHaveURL(/\/stories\/.+\/outline$/);
  await page.getByRole("button", { name: "Generate Outline" }).click();
  const moveUp = page.getByRole("button", { name: "Move scene 2 up" });
  await moveUp.focus();
  await page.keyboard.press("Enter");
  await expect(page.getByLabel("Scene 1 title")).toHaveValue("A Helpful Friend");
});
