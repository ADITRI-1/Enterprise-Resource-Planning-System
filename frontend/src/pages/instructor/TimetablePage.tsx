import { useTerm } from "@/lib/term";
import { Card, PageHeader, Spinner } from "@/components/ui/misc";
import { TermPicker } from "@/components/TermPicker";
import { WeeklyTimetable } from "@/components/WeeklyTimetable";
import { useInstructorCode, useInstructorTimetable } from "./queries";

export function InstructorTimetablePage() {
  const code = useInstructorCode();
  const { term, setTerm } = useTerm();
  const timetable = useInstructorTimetable(code, term);
  const events = (timetable.data ?? []).map((s) => ({
    key: `${s.sectionId}-${s.dayOfWeek}-${s.startTime}`,
    day: s.dayOfWeek!,
    start: s.startTime!,
    end: s.endTime!,
    title: s.courseId!,
    subtitle: s.sectionId,
    room: s.roomId,
    colorKey: s.courseId!,
  }));
  return (
    <>
      <PageHeader title="Teaching timetable" actions={<TermPicker value={term} onChange={setTerm} />} />
      <Card className="p-4 sm:p-5">{timetable.isLoading || !code ? <Spinner /> : <WeeklyTimetable events={events} />}</Card>
    </>
  );
}
