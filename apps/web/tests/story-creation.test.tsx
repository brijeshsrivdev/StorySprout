import { describe, expect, it, vi, beforeEach } from "vitest";
import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import CreateStoryPage from "../app/create/page";
import StorySetupPage from "../app/create/setup/page";
import * as api from "../lib/api";

vi.mock("../lib/api", () => ({ createProject: vi.fn(), createStory: vi.fn() }));

const replace = vi.fn();
vi.mock("next/navigation", () => ({ useRouter: () => ({ push: vi.fn(), replace }), useSearchParams: () => new URLSearchParams("projectId=p1") }));

beforeEach(() => vi.clearAllMocks());

describe("Create Story flow", () => {
  it("keeps AI and Blank mutually exclusive and starts setup", async () => {
    vi.mocked(api.createProject).mockResolvedValue({ id: "p1", name: "Untitled Story", status: "DRAFT", updatedAt: "2026-09-16T10:00:00Z" });
    const user = userEvent.setup();
    render(<CreateStoryPage />);
    const ai = screen.getByRole("button", { name: /Start with AI/i });
    const blank = screen.getByRole("button", { name: /Blank Story/i });
    await user.click(ai);
    expect(ai).toHaveAttribute("aria-pressed", "true");
    await user.click(blank);
    expect(blank).toHaveAttribute("aria-pressed", "true");
    expect(ai).toHaveAttribute("aria-pressed", "false");
    await user.click(screen.getByRole("button", { name: "Continue" }));
    expect(replace).toHaveBeenCalledWith("/create/setup?projectId=p1&mode=BLANK");
  });

  it("renders only the approved target ages and validates incomplete setup", async () => {
    const user = userEvent.setup();
    render(<StorySetupPage />);
    expect(screen.getByRole("option", { name: "Ages 3–5" })).toBeInTheDocument();
    expect(screen.getByRole("option", { name: "Ages 6–8" })).toBeInTheDocument();
    expect(screen.getByRole("option", { name: "Ages 9–12" })).toBeInTheDocument();
    expect(screen.queryByRole("option", { name: /13/ })).not.toBeInTheDocument();
    await user.click(screen.getByRole("button", { name: "Continue" }));
    expect(screen.getByText("Enter a story idea.")).toBeInTheDocument();
  });

  it("prevents duplicate story submissions", async () => {
    let resolve!: (value: any) => void;
    vi.mocked(api.createStory).mockReturnValue(new Promise((r) => { resolve = r; }));
    const user = userEvent.setup();
    render(<StorySetupPage />);
    await user.type(screen.getByLabelText("Story idea"), "A rabbit learns to share.");
    await user.selectOptions(screen.getByLabelText("Target age"), "6_8");
    await user.click(screen.getByRole("button", { name: "Continue" }));
    expect(api.createStory).toHaveBeenCalledTimes(1);
    expect(screen.getByRole("button", { name: "Saving…" })).toBeDisabled();
    resolve({ id: "s1", projectId: "p1", title: "A Sharing Rabbit", idea: "A rabbit learns to share.", targetAge: "6_8", durationMinutes: 3, visualStyle: "2D", language: "ENGLISH", creationMode: "BLANK", generationStatus: "NOT_REQUESTED", draftContent: null });
  });
});
