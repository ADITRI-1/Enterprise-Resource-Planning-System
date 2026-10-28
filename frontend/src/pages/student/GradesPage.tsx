import { useState } from "react";
import { Award, Download, FileText, GraduationCap } from "lucide-react";
import { download, type Schemas } from "@/lib/api";
import type { Session } from "@/lib/session";
import { notify } from "@/lib/toast";
import { termLabel } from "@/lib/utils";
import { Button } from "@/components/ui/button";
import { Badge, Card, CardHeader, EmptyState, PageHeader, Spinner, StatCard } from "@/components/ui/misc";
import { useAcademicRecord, useStudentGrades } from "./queries";

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
  const uid = session.userId;
  const grades = useStudentGrades(uid);
  const record = useAcademicRecord(uid);
  const [busy, setBusy] = useState<"pdf" | "csv" | null>(null);
  const courses = group(grades.data ?? []);

  const get = async (kind: "pdf" | "csv") => {
    setBusy(kind);
    try {
      await download(`/api/student/${uid}/transcript.${kind}`, `transcript.${kind}`);
    } catch (e) {
      notify.error(e);
    } finally {
      setBusy(null);
    }
  };

  const r = record.data;
  return (
    <>
      <PageHeader
        title="Grades"
        description="Scores appear as instructors enter them; final grades and GPA once they're computed."
        actions={
          <>
            <Button variant="outline" onClick={() => get("csv")} loading={busy === "csv"}><Download className="h-4 w-4" /> CSV</Button>
            <Button onClick={() => get("pdf")} loading={busy === "pdf"}><FileText className="h-4 w-4" /> Transcript PDF</Button>
          </>
        }
      />

      <div className="grid gap-4 sm:grid-cols-3">
        <StatCard label="CGPA" value={r?.cgpa?.toFixed(2) ?? "—"} icon={<GraduationCap className="h-5 w-5" />} hint="10-point scale" />
        <StatCard label="Credits earned" value={r?.creditsEarned ?? 0} icon={<Award className="h-5 w-5" />} tone="success" />
        <StatCard label="Credits graded" value={r?.creditsGraded ?? 0} icon={<FileText className="h-5 w-5" />} tone="accent" />
      </div>

      {r && r.terms && r.terms.length > 0 && (
        <Card className="mt-6">
          <CardHeader title="Results by term" description="SGPA = Σ(grade points × credits) ÷ Σ credits. A 10, A- 9, B 8, B- 7, C 6, C- 5, D 4, F 0." />
          <div className="divide-y divide-border">
            {[...r.terms].reverse().map((t) => (
              <div key={`${t.semester}-${t.year}`} className="px-5 py-4">
                <div className="mb-2 flex items-center justify-between">
                  <p className="font-semibold">{termLabel(t)}</p>
                  <p className="text-sm">SGPA <span className="font-semibold text-primary">{t.sgpa?.toFixed(2) ?? "—"}</span></p>
                </div>
                <div className="flex flex-wrap gap-2">
                  {t.courses?.map((c) => (
                    <span key={c.courseId} className="inline-flex items-center gap-2 rounded-lg border border-border px-3 py-1.5 text-sm">
                      <span className="font-medium">{c.courseId}</span>
                      {c.grade ? <span className="font-bold text-primary">{c.grade}</span> : <Badge>In progress</Badge>}
                    </span>
                  ))}
                </div>
              </div>
            ))}
          </div>
        </Card>
      )}

      <h2 className="mb-3 mt-8 text-lg font-semibold">Assessment scores</h2>
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
