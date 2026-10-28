import { Link } from "react-router-dom";
import { CalendarClock, ChevronRight, Layers, Users } from "lucide-react";
import { useInstructorProfile } from "@/lib/session";
import { useTerm } from "@/lib/term";
import { DAYS, hhmm, minutes, termLabel } from "@/lib/utils";
import { Card, CardHeader, EmptyState, PageHeader, Spinner, StatCard } from "@/components/ui/misc";
import { useInstructorTimetable, useMySections } from "./queries";

export function InstructorDashboard() {
  const profile = useInstructorProfile();
  const code = profile.data?.instructorCode;
  const { term } = useTerm();
  const sections = useMySections(code, term);
  const timetable = useInstructorTimetable(code, term);

  const students = sections.data?.reduce((s, x) => s + (x.enrollmentCount ?? 0), 0) ?? 0;
  const today = DAYS[(new Date().getDay() + 6) % 7];
  const todays = (timetable.data ?? []).filter((s) => s.dayOfWeek === today).sort((a, b) => minutes(a.startTime) - minutes(b.startTime));

  return (
    <>
      <PageHeader title={`Welcome, ${profile.data?.name ?? ""}`} description={`${termLabel(term)} · ${profile.data?.departmentId ?? ""} · ${code ?? ""}`} />
      <div className="grid gap-4 sm:grid-cols-3">
        <StatCard label="Sections this term" value={sections.data?.length ?? "–"} icon={<Layers className="h-5 w-5" />} />
        <StatCard label="Students enrolled" value={students} icon={<Users className="h-5 w-5" />} tone="accent" />
        <StatCard label="Classes today" value={todays.length} icon={<CalendarClock className="h-5 w-5" />} tone="warning" />
      </div>
      <div className="mt-6 grid gap-6 lg:grid-cols-5">
        <Card className="lg:col-span-3">
          <CardHeader title="My sections" description="Open a section to grade, compute results and see class statistics." />
          {sections.isLoading || !code ? <Spinner /> : !sections.data?.length ? <EmptyState title="No sections this term" /> : (
            <ul className="divide-y divide-border">
              {sections.data.map((s) => (
                <li key={s.sectionId}>
                  <Link to={`/instructor/sections/${encodeURIComponent(s.sectionId!)}`} className="flex items-center gap-4 px-5 py-3 hover:bg-muted/50">
                    <div className="min-w-0 flex-1">
                      <p className="font-medium">{s.courseId}</p>
                      <p className="text-sm text-fg-muted">{s.sectionId}</p>
                    </div>
                    <span className="text-sm tabular-nums text-fg-muted">{s.enrollmentCount}/{s.capacity}</span>
                    <ChevronRight className="h-4 w-4 text-fg-muted" />
                  </Link>
                </li>
              ))}
            </ul>
          )}
        </Card>
        <Card className="lg:col-span-2">
          <CardHeader title="Today" />
          {todays.length === 0 ? <EmptyState title="No classes today" /> : (
            <ul className="divide-y divide-border">
              {todays.map((s) => (
                <li key={`${s.sectionId}-${s.startTime}`} className="flex gap-4 px-5 py-3">
                  <span className="w-24 text-sm font-medium tabular-nums">{hhmm(s.startTime)}–{hhmm(s.endTime)}</span>
                  <span className="text-sm">{s.courseId} · <span className="text-fg-muted">Room {s.roomId}</span></span>
                </li>
              ))}
            </ul>
          )}
        </Card>
      </div>
    </>
  );
}
