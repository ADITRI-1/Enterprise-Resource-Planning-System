import { useState } from "react";
import { Download } from "lucide-react";
import { download, type Schemas } from "@/lib/api";
import type { Session } from "@/lib/session";
import { notify } from "@/lib/toast";
import { Button } from "@/components/ui/button";
import { Badge, Card, EmptyState, PageHeader, Spinner } from "@/components/ui/misc";
import { useStudentGrades } from "./queries";

type Course = { code: string; title?: string; components: Schemas["GradeRow"][]; final?: Schemas["GradeRow"] };

function group(rows: Schemas["GradeRow"][]): Course[] {
  const map = new Map<string, Course>();
  rows.forEach((r) => {
    const c = map.get(r.courseCode!) ?? { code: r.courseCode!, title: r.courseTitle, components: [] };
    if (r.component === "FINAL") c.final = r;
    else c.components.push(r);
    map.set(c.code, c);
  });
  return [...map.values()].sort((a, b) => a.code.localeCompare(b.code));
}

export function GradesPage({ session }: { session: Session }) {
  const grades = useStudentGrades(session.userId);
  const [downloading, setDownloading] = useState(false);
  const courses = group(grades.data ?? []);

  const exportCsv = async () => {
    setDownloading(true);
    try {
      await download(`/api/student/${session.userId}/transcript.csv`, "transcript.csv");
    } catch (e) {
      notify.error(e);
    } finally {
      setDownloading(false);
    }
  };

  return (
    <>
      <PageHeader
        title="Grades"
        description="Assessment scores as your instructors enter them; the final grade appears once computed."
        actions={<Button variant="outline" onClick={exportCsv} loading={downloading}><Download className="h-4 w-4" /> Transcript (CSV)</Button>}
      />
      {grades.isLoading ? <Spinner /> : courses.length === 0 ? (
        <Card><EmptyState title="No grades yet" description="Scores will appear here once your instructors start grading." /></Card>
      ) : (
        <div className="grid gap-4 md:grid-cols-2">
          {courses.map((c) => (
            <Card key={c.code} className="overflow-hidden">
              <div className="flex items-start justify-between gap-3 border-b border-border px-5 py-4">
                <div className="min-w-0">
                  <p className="font-semibold">{c.code}</p>
                  <p className="truncate text-sm text-fg-muted">{c.title}</p>
                </div>
                {c.final ? (
                  <div className="text-right">
                    <p className="text-2xl font-bold text-primary">{c.final.finalGrade}</p>
                    <p className="text-xs tabular-nums text-fg-muted">{Number(c.final.score).toFixed(1)} / 100</p>
                  </div>
                ) : <Badge>In progress</Badge>}
              </div>
              <ul className="divide-y divide-border">
                {c.components.map((g) => (
                  <li key={g.component} className="flex items-center justify-between px-5 py-2.5 text-sm">
                    <span>{g.component}</span>
                    <span className="font-medium tabular-nums">{Number(g.score)}</span>
                  </li>
                ))}
                {c.components.length === 0 && <li className="px-5 py-3 text-sm text-fg-muted">No scores entered yet.</li>}
              </ul>
            </Card>
          ))}
        </div>
      )}
    </>
  );
}
