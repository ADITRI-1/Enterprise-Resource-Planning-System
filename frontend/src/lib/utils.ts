import { clsx, type ClassValue } from "clsx";
import { twMerge } from "tailwind-merge";

export function cn(...inputs: ClassValue[]) {
  return twMerge(clsx(inputs));
}

export const DAYS = ["MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY", "SUNDAY"] as const;
export type Day = (typeof DAYS)[number];

export function dayLabel(day: Day | number | undefined, short = false) {
  if (day === undefined) return "";
  const name = typeof day === "number" ? DAYS[day - 1] : day;
  const label = name.charAt(0) + name.slice(1).toLowerCase();
  return short ? label.slice(0, 3) : label;
}

/** Sorts class slots Monday-first, then by start time. */
export function byDayAndTime<T extends { day?: Day; startTime?: string }>(a: T, b: T) {
  return DAYS.indexOf(a.day!) - DAYS.indexOf(b.day!) || (a.startTime ?? "").localeCompare(b.startTime ?? "");
}

/** "09:30:00" -> "09:30" */
export function hhmm(time?: string) {
  return time ? time.slice(0, 5) : "";
}

export function minutes(time?: string) {
  if (!time) return 0;
  const [h, m] = time.split(":").map(Number);
  return h * 60 + m;
}

export function formatDate(iso?: string | null) {
  if (!iso) return "—";
  const d = new Date(iso.length === 10 ? iso + "T00:00:00" : iso);
  return d.toLocaleDateString(undefined, { day: "numeric", month: "short", year: "numeric" });
}

export function formatDateTime(iso?: string | null) {
  if (!iso) return "—";
  return new Date(iso).toLocaleString(undefined, { day: "numeric", month: "short", hour: "2-digit", minute: "2-digit" });
}

export function titleCase(s?: string) {
  return s ? s.charAt(0).toUpperCase() + s.slice(1).toLowerCase() : "";
}

export type Term = { semester: "MONSOON" | "WINTER"; year: number };

/** Monsoon runs Jul–Dec, Winter Jan–Jun. */
export function currentTerm(now = new Date()): Term {
  return { semester: now.getMonth() >= 6 ? "MONSOON" : "WINTER", year: now.getFullYear() };
}

export function termLabel(t: { semester?: string; year?: number }) {
  return `${titleCase(t.semester)} ${t.year ?? ""}`.trim();
}

export function initials(name?: string) {
  if (!name) return "?";
  return name.split(/\s+/).slice(0, 2).map((p) => p[0]?.toUpperCase()).join("");
}
