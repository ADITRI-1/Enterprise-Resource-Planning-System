import { DAYS } from "@/lib/utils";
import type { Session } from "@/lib/session";
import { useTerm } from "@/lib/term";
import { Card, PageHeader, Spinner } from "@/components/ui/misc";
import { TermPicker } from "@/components/TermPicker";
import { WeeklyTimetable } from "@/components/WeeklyTimetable";
import { useStudentTimetable } from "./queries";

export function StudentTimetablePage({ session }: { session: Session }) {
  const { term, setTerm } = useTerm();
  const timetable = useStudentTimetable(session.userId, term);
  const events = (timetable.data ?? []).map((s) => ({
    key: `${s.courseCode}-${s.dayOfWeek}-${s.startTime}`,
    day: DAYS[(s.dayOfWeek ?? 1) - 1],
    start: s.startTime!,
    end: s.endTime!,
    title: s.courseCode!,
    subtitle: s.courseTitle,
    room: s.room,
    colorKey: s.courseCode!,
  }));
  return (
    <>
      <PageHeader title="Timetable" description="Your weekly classes for registered sections." actions={<TermPicker value={term} onChange={setTerm} />} />
      <Card className="p-4 sm:p-5">{timetable.isLoading ? <Spinner /> : <WeeklyTimetable events={events} />}</Card>
    </>
  );
}
