export const APPROXIMATE_VARIANCE_SECONDS = 10;

export function formatDuration(seconds: number): string {
  return `${Math.floor(seconds / 60)}:${String(Math.abs(seconds) % 60).padStart(2, "0")}`;
}

export function calculatePlannedDuration(durations: number[]): number {
  return durations.reduce((total, duration) => total + duration, 0);
}

export function calculateVariance(targetSeconds: number, plannedSeconds: number): number {
  return plannedSeconds - targetSeconds;
}

export function getVarianceState(variance: number) {
  if (Math.abs(variance) <= APPROXIMATE_VARIANCE_SECONDS) return "approximately-on-target" as const;
  return variance < 0 ? "under-target" as const : "over-target" as const;
}
