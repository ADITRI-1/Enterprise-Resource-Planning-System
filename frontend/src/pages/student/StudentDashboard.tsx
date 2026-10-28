import { Link } from "react-router-dom";
import { ArrowRight, BookOpen, CalendarClock, GraduationCap, Layers } from "lucide-react";
import { useStudentProfile, type Session } from "@/lib/session";
import { useTerm } from "@/lib/term";
import { DAYS, hhmm, minutes, termLabel } from "@/lib/utils";
import { Card, CardHeader, EmptyState, PageHeader, Spinner, StatCard } from "@/components/ui/misc";
import { useAcademicRecord, useRegistrations, useStudentTimetable } from "./queries";

export function StudentDashboard({ session }: { session: Session }) {
  const uid = session.userId;
  const { term } = useTerm();
  const profile = useStudentProfile();
  const registrations = useRegistrations(uid, term);
  const timetable = useStudentTimetable(uid, term);
  const record = useAcademicRecord(uid);

  const active = registrations.data?.filter((r) => r.status === "enrolled") ?? [];
  const credits = active.reduce((sum, r) => sum + (r.credits ?? 0), 0);

  const todayIndex = (new Date().getDay() + 6) % 7; // Monday = 0
  const nowMinutes = new Date().getHours() * 60 + new Date().getMinutes();
  const today = (timetable.data ?? [])
    .filter((s) => (s.dayOfWeek ?? 0) - 1 === todayIndex)
    .sort((a, b) => minutes(a.startTime) - minutes(b.startTime));

  return (
    <>
      <PageHeader
        title={`Hello${profile.data?.name ? `, ${profile.data.name.split(" ")[0]}` : ""}`}
        description={`${termLabel(term)} · Roll no. ${profile.data?.rollNo ?? "…"}`}
      />
      <div className="grid gap-4 sm:grid-cols-2 xl:grid-cols-4">
        <StatCard label="Registered courses" value={active.length} icon={<Layers className="h-5 w-5" />} hint={termLabel(term)} />
        <StatCard label="Credits" value={credits} icon={<BookOpen className="h-5 w-5" />} tone="accent" />
        <StatCard label="Classes today" value={today.length} icon={<CalendarClock className="h-5 w-5" />} tone="warning" hint={DAYS[todayIndex].charAt(0) + DAYS[todayIndex].slice(1).toLowerCase()} />
        <StatCard label="CGPA" value={record.data?.cgpa?.toFixed(2) ?? "—"} icon={<GraduationCap className="h-5 w-5" />} tone="success" hint={`${record.data?.creditsEarned ?? 0} credits earned`} />
      </div>

      <div className="mt-6 grid gap-6 lg:grid-cols-5">
        <Card className="lg:col-span-3">
          <CardHeader title="Today's classes" action={<Link to="/student/timetable" className="text-sm font-medium text-primary hover:underline">Full timetable</Link>} />
          {timetable.isLoading ? <Spinner /> : today.length === 0 ? (
            <EmptyState title="No classes today" description="Enjoy the free time, or browse the catalog for something new." />
          ) : (
            <ul className="divide-y divide-border">
              {today.map((s) => {
                const live = nowMinutes >= minutes(s.startTime) && nowMinutes < minutes(s.endTime);
                return (
                  <li key={`${s.courseCode}-${s.startTime}`} className="flex items-center gap-4 px-5 py-3">
                    <div className="w-20 text-sm font-medium tabular-nums">{hhmm(s.startTime)}<span className="block text-xs text-fg-muted">{hhmm(s.endTime)}</span></div>
                    <div className="min-w-0 flex-1">
                      <p className="truncate font-medium">{s.courseCode} · {s.courseTitle}</p>
                      <p className="text-sm text-fg-muted">Room {s.room}</p>
                    </div>
                    {live && <span className="rounded-full bg-success/15 px-2 py-0.5 text-xs font-medium text-success">Now</span>}
                  </li>
                );
              })}
            </ul>
          )}
        </Card>

        <Card className="lg:col-span-2">
          <CardHeader title="My courses" action={<Link to="/student/registrations" className="text-sm font-medium text-primary hover:underline">Manage</Link>} />
          {registrations.isLoading ? <Spinner /> : active.length === 0 ? (
            <EmptyState title="Not registered yet" action={<Link to="/student/catalog" className="text-sm font-medium text-primary hover:underline">Browse the catalog</Link>} />
          ) : (
            <ul className="divide-y divide-border">
              {active.map((r) => (
                <li key={r.enrollmentId} className="flex items-center justify-between gap-3 px-5 py-3">
                  <div className="min-w-0">
                    <p className="truncate font-medium">{r.courseId} · {r.courseTitle}</p>
                    <p className="text-sm text-fg-muted">{r.instructorName} · {r.credits} credits</p>
                  </div>
                  {r.finalGrade && <span className="text-lg font-semibold text-primary">{r.finalGrade}</span>}
                </li>
              ))}
            </ul>
          )}
          <Link to="/student/catalog" className="flex items-center justify-center gap-2 border-t border-border px-5 py-3 text-sm font-medium text-primary hover:bg-muted/50">
            Find more courses <ArrowRight className="h-4 w-4" />
          </Link>
        </Card>
      </div>
    </>
  );
}
