import { describe, expect, it } from "vitest";
import { calculatePlannedDuration, calculateVariance, formatDuration, getVarianceState } from "./outline";

describe("Story Outline duration planning", () => {
  it("calculates under-target variance", () => {
    expect(calculatePlannedDuration([40, 50])).toBe(90);
    expect(calculateVariance(180, 90)).toBe(-90);
    expect(getVarianceState(-90)).toBe("under-target");
  });

  it("calculates approximately-on-target variance", () => {
    expect(calculateVariance(180, 190)).toBe(10);
    expect(getVarianceState(10)).toBe("approximately-on-target");
  });

  it("calculates over-target variance", () => {
    expect(calculateVariance(180, 220)).toBe(40);
    expect(getVarianceState(40)).toBe("over-target");
  });

  it("formats target and planned durations", () => {
    expect(formatDuration(180)).toBe("3:00");
    expect(formatDuration(190)).toBe("3:10");
  });
});
