import { render, screen } from "@testing-library/react";
import { describe, expect, it } from "vitest";
import { Button, StudioHeader, StudioShell } from "../components/ui";

describe("StorySprout visual foundation", () => {
  it("keeps primary and AI actions on the shared token system", () => {
    render(
      <StudioShell>
        <Button>Primary action</Button>
        <Button variant="ai">AI action</Button>
      </StudioShell>,
    );

    expect(screen.getByRole("button", { name: "Primary action" })).toHaveClass("bg-[var(--ss-primary)]");
    expect(screen.getByRole("button", { name: "AI action" })).toHaveClass("bg-violet-50");
  });

  it("keeps the creator shell branded and contextual", () => {
    render(<StudioHeader context="Story Outline" actions={<span>Saved</span>} />);
    expect(screen.getByText("StorySprout")).toBeInTheDocument();
    expect(screen.getByText("Story Outline")).toBeInTheDocument();
    expect(screen.getByText("Saved")).toBeInTheDocument();
  });
});
