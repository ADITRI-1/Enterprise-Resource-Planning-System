import { DAYS, cn, dayLabel, hhmm, minutes, type Day } from "@/lib/utils";
import { EmptyState } from "@/components/ui/misc";
import { CalendarX } from "lucide-react";

export type TimetableEvent = {
  key: string;
  day: Day;
  start: string;
  end: string;
  title: string;
  subtitle?: string;
  room?: string;
  colorKey: string;
};

// Distinct, readable fills that work in light and dark mode.
const PALETTE = [
  "bg-primary/15 border-primary text-primary",
  "bg-accent/20 border-accent text-accent",
  "bg-warning/15 border-warning text-warning",
  "bg-success/15 border-success text-success",
  "bg-danger/10 border-danger text-danger",
];

function colorFor(key: string) {
  let h = 0;
  for (const ch of key) h = (h * 31 + ch.charCodeAt(0)) >>> 0;
  return PALETTE[h % PALETTE.length];
}

const HOUR_PX = 56;

export function WeeklyTimetable({ events }: { events: TimetableEvent[] }) {
  if (!events.length) {
    return <EmptyState icon={<CalendarX className="h-6 w-6" />} title="No classes scheduled" description="Nothing is timetabled for this term yet." />;
  }
  const weekend = events.some((e) => e.day === "SATURDAY" || e.day === "SUNDAY");
  const days = DAYS.slice(0, weekend ? 7 : 5);
  const startHour = Math.min(8, ...events.map((e) => Math.floor(minutes(e.start) / 60)));
  const endHour = Math.max(18, ...events.map((e) => Math.ceil(minutes(e.end) / 60)));
  const hours = Array.from({ length: endHour - startHour }, (_, i) => startHour + i);

  return (
    <>
      {/* Phones: one list per day */}
      <div className="space-y-4 md:hidden">
        {days.map((day) => {
          const today = events.filter((e) => e.day === day).sort((a, b) => minutes(a.start) - minutes(b.start));
          if (!today.length) return null;
          return (
            <div key={day}>
              <p className="mb-2 text-xs font-semibold uppercase tracking-wide text-fg-muted">{dayLabel(day)}</p>
              <div className="space-y-2">
                {today.map((e) => (
                  <div key={e.key} className={cn("rounded-lg border-l-4 px-3 py-2", colorFor(e.colorKey))}>
                    <p className="text-sm font-semibold">{e.title}</p>
                    <p className="text-xs text-fg-muted">{hhmm(e.start)}–{hhmm(e.end)}{e.room ? ` · ${e.room}` : ""}{e.subtitle ? ` · ${e.subtitle}` : ""}</p>
                  </div>
                ))}
              </div>
            </div>
          );
        })}
      </div>

      {/* Tablets and up: week grid */}
      <div className="hidden overflow-x-auto md:block">
        <div className="grid min-w-[720px]" style={{ gridTemplateColumns: `56px repeat(${days.length}, minmax(0, 1fr))` }}>
          <div />
          {days.map((d) => (
            <div key={d} className="border-b border-border pb-2 text-center text-xs font-semibold uppercase tracking-wide text-fg-muted">
              {dayLabel(d, true)}
            </div>
          ))}
          <div className="relative">
            {hours.map((h) => (
              <div key={h} className="pr-2 text-right text-[11px] tabular-nums text-fg-muted" style={{ height: HOUR_PX }}>
                {String(h).padStart(2, "0")}:00
              </div>
            ))}
          </div>
          {days.map((day) => (
            <div key={day} className="relative border-l border-border" style={{ height: hours.length * HOUR_PX }}>
              {hours.map((h) => (
                <div key={h} className="border-b border-dashed border-border/70" style={{ height: HOUR_PX }} />
              ))}
              {events.filter((e) => e.day === day).map((e) => {
                const top = ((minutes(e.start) - startHour * 60) / 60) * HOUR_PX;
                const height = ((minutes(e.end) - minutes(e.start)) / 60) * HOUR_PX;
                return (
                  <div
                    key={e.key}
                    className={cn("absolute inset-x-1 overflow-hidden rounded-md border-l-[3px] px-2 py-1", colorFor(e.colorKey))}
                    style={{ top, height: Math.max(height - 2, 22) }}
                    title={`${e.title} ${hhmm(e.start)}–${hhmm(e.end)} ${e.room ?? ""}`}
                  >
                    <p className="truncate text-xs font-semibold">{e.title}</p>
                    <p className="truncate text-[11px] text-fg/70">{hhmm(e.start)}–{hhmm(e.end)}</p>
                    {e.room && <p className="truncate text-[11px] text-fg/70">{e.room}</p>}
                  </div>
                );
              })}
            </div>
          ))}
        </div>
      </div>
    </>
  );
}
