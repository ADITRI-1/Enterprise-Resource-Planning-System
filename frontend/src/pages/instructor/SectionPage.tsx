import { useEffect, useMemo, useRef, useState, type FormEvent } from "react";
import { Link, useParams } from "react-router-dom";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { ArrowLeft, Calculator, Download, Pencil, Plus, Save, Trash2, Upload } from "lucide-react";
import { Bar, BarChart, CartesianGrid, ResponsiveContainer, Tooltip, XAxis, YAxis } from "recharts";
import { api, download, errorMessage, type Schemas } from "@/lib/api";
import { useMaintenance } from "@/lib/session";
import { notify } from "@/lib/toast";
import { cn } from "@/lib/utils";
import { Button } from "@/components/ui/button";
import { Badge, Card, CardHeader, EmptyState, Field, PageHeader, Spinner, StatCard, Tabs } from "@/components/ui/misc";
import { ConfirmDialog, Modal } from "@/components/ui/modal";
import { Table, type Column } from "@/components/ui/table";
import { sectionKey, useInstructorCode, useMySections } from "./queries";

type Tab = "gradebook" | "assessments" | "final" | "stats";
type Component = Schemas["AssessmentComponentResponse"];

export function SectionPage() {
  const { sectionId = "" } = useParams();
  const code = useInstructorCode();
  const sections = useMySections(code);
  const section = sections.data?.find((s) => s.sectionId === sectionId);
  const [tab, setTab] = useState<Tab>("gradebook");

  return (
    <>
      <Link to="/instructor/sections" className="mb-3 inline-flex items-center gap-1 text-sm text-fg-muted hover:text-fg">
        <ArrowLeft className="h-4 w-4" /> My sections
      </Link>
      <PageHeader
        title={section?.courseId ?? sectionId}
        description={section ? `${sectionId} · ${section.enrollmentCount}/${section.capacity} students` : sectionId}
      />
      <Tabs<Tab>
        value={tab}
        onChange={setTab}
        tabs={[
          { id: "gradebook", label: "Gradebook" },
          { id: "assessments", label: "Assessments" },
          { id: "final", label: "Final grades" },
          { id: "stats", label: "Statistics" },
        ]}
      />
      <div className="mt-6">
        {!code ? <Spinner /> : (
          <>
            {tab === "gradebook" && <Gradebook code={code} sectionId={sectionId} />}
            {tab === "assessments" && <Assessments code={code} sectionId={sectionId} />}
            {tab === "final" && <FinalGrades code={code} sectionId={sectionId} />}
            {tab === "stats" && <Stats code={code} sectionId={sectionId} />}
          </>
        )}
      </div>
    </>
  );
}

// ------------------------------------------------------------------ gradebook

function Gradebook({ code, sectionId }: { code: string; sectionId: string }) {
  const base = `/api/instructor/${code}/sections/${encodeURIComponent(sectionId)}`;
  const qc = useQueryClient();
  const maintenance = useMaintenance().data?.maintenanceOn;
  const overview = useQuery({
    queryKey: [...sectionKey(code, sectionId), "gradebook"],
    queryFn: () => api<Schemas["InstructorScoreOverviewResponse"]>(`${base}/students`),
  });
  // Edited cells only, keyed "enrollmentId|componentId"
  const [edits, setEdits] = useState<Record<string, string>>({});
  const fileInput = useRef<HTMLInputElement>(null);

  useEffect(() => setEdits({}), [overview.data]);

  const components = overview.data?.components ?? [];
  const students = overview.data?.students ?? [];

  const invalid = Object.entries(edits).some(([key, v]) => {
    const max = components.find((c) => c.componentId === key.split("|")[1])?.maxScore ?? 0;
    const n = Number(v);
    return v === "" || Number.isNaN(n) || n < 0 || n > max;
  });

  const invalidate = () => qc.invalidateQueries({ queryKey: sectionKey(code, sectionId) });

  const save = useMutation({
    mutationFn: () => {
      const byEnrollment = new Map<string, { componentId: string; score: number }[]>();
      Object.entries(edits).forEach(([key, v]) => {
        const [enrollmentId, componentId] = key.split("|");
        byEnrollment.set(enrollmentId, [...(byEnrollment.get(enrollmentId) ?? []), { componentId, score: Number(v) }]);
      });
      return api(`${base}/scores`, {
        method: "PUT",
        body: { entries: [...byEnrollment].map(([enrollmentId, comps]) => ({ enrollmentId, components: comps })) },
      });
    },
    onSuccess: () => { notify.success("Scores saved"); invalidate(); },
    onError: notify.error,
  });

  const importCsv = useMutation({
    mutationFn: (file: File) => {
      const form = new FormData();
      form.append("file", file);
      return api(`${base}/scores/import`, { body: form });
    },
    onSuccess: () => { notify.success("Scores imported"); invalidate(); },
    onError: notify.error,
  });

  if (overview.isLoading) return <Spinner />;
  if (overview.error) return <Card><EmptyState title="Couldn't load the gradebook" description={errorMessage(overview.error)} /></Card>;
  if (!components.length) {
    return <Card><EmptyState title="No assessments yet" description="Add assessments (e.g. Quiz 20%, Midsem 30%, Endsem 50%) in the Assessments tab first." /></Card>;
  }

  const dirty = Object.keys(edits).length;

  return (
    <Card>
      <CardHeader
        title="Gradebook"
        description="Edit scores inline. Each cell must be between 0 and the component's maximum."
        action={
          <div className="flex flex-wrap gap-2">
            <Button variant="outline" size="sm" onClick={() => download(`${base}/scores/export`, `scores-${sectionId}.csv`).catch(notify.error)}>
              <Download className="h-4 w-4" /> Export CSV
            </Button>
            <Button variant="outline" size="sm" disabled={maintenance} loading={importCsv.isPending} onClick={() => fileInput.current?.click()}>
              <Upload className="h-4 w-4" /> Import CSV
            </Button>
            <input
              ref={fileInput}
              type="file"
              accept=".csv,text/csv"
              className="hidden"
              onChange={(e) => { const f = e.target.files?.[0]; if (f) importCsv.mutate(f); e.target.value = ""; }}
            />
            <Button size="sm" disabled={!dirty || invalid || maintenance} loading={save.isPending} onClick={() => save.mutate()}>
              <Save className="h-4 w-4" /> Save{dirty ? ` (${dirty})` : ""}
            </Button>
          </div>
        }
      />
      {!students.length ? <EmptyState title="No students enrolled" /> : (
        <div className="overflow-x-auto">
          <table className="w-full text-sm">
            <thead>
              <tr className="border-b border-border text-left text-xs uppercase tracking-wide text-fg-muted">
                <th className="sticky left-0 bg-surface px-4 py-3 font-medium">Student</th>
                {components.map((c) => (
                  <th key={c.componentId} className="px-3 py-3 text-center font-medium">
                    {c.name}
                    <span className="block font-normal normal-case">/ {c.maxScore} · {Math.round((c.weight ?? 0) * 100)}%</span>
                  </th>
                ))}
              </tr>
            </thead>
            <tbody>
              {students.map((s) => (
                <tr key={s.enrollmentId} className="border-b border-border/70 last:border-0">
                  <td className="sticky left-0 bg-surface px-4 py-2">
                    <p className="font-medium">{s.studentName}</p>
                    <p className="text-xs text-fg-muted">{s.rollNo}</p>
                  </td>
                  {components.map((c) => {
                    const key = `${s.enrollmentId}|${c.componentId}`;
                    const original = s.scores?.find((x) => x.componentId === c.componentId)?.score ?? 0;
                    const value = edits[key] ?? String(original);
                    const n = Number(value);
                    const bad = value === "" || Number.isNaN(n) || n < 0 || n > (c.maxScore ?? 0);
                    return (
                      <td key={c.componentId} className="px-3 py-2 text-center">
                        <input
                          type="number"
                          inputMode="decimal"
                          min={0}
                          max={c.maxScore}
                          step="0.5"
                          aria-label={`${s.studentName} ${c.name}`}
                          disabled={maintenance}
                          value={value}
                          onChange={(e) => {
                            const v = e.target.value;
                            setEdits((prev) => {
                              const next = { ...prev };
                              if (Number(v) === original && v !== "") delete next[key];
                              else next[key] = v;
                              return next;
                            });
                          }}
                          className={cn(
                            "input h-9 w-20 text-center tabular-nums",
                            edits[key] !== undefined && "border-primary bg-primary/5",
                            bad && "border-danger bg-danger/5",
                          )}
                        />
                      </td>
                    );
                  })}
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </Card>
  );
}

// ------------------------------------------------------------------ assessments

function Assessments({ code, sectionId }: { code: string; sectionId: string }) {
  const base = `/api/instructor/${code}/sections/${encodeURIComponent(sectionId)}/assessments`;
  const qc = useQueryClient();
  const maintenance = useMaintenance().data?.maintenanceOn;
  const list = useQuery({ queryKey: [...sectionKey(code, sectionId), "assessments"], queryFn: () => api<Component[]>(base) });
  const [editing, setEditing] = useState<Component | "new" | null>(null);
  const [deleting, setDeleting] = useState<Component | null>(null);

  const invalidate = () => qc.invalidateQueries({ queryKey: sectionKey(code, sectionId) });
  const remove = useMutation({
    mutationFn: (id: string) => api(`${base}/${id}`, { method: "DELETE" }),
    onSuccess: () => { notify.success("Assessment deleted"); setDeleting(null); invalidate(); },
    onError: (e) => { notify.error(e); setDeleting(null); },
  });

  const total = Math.round((list.data ?? []).reduce((s, c) => s + (c.weight ?? 0), 0) * 1000) / 10;

  const columns: Column<Component>[] = [
    { key: "name", header: "Name", cell: (c) => <span className="font-medium">{c.name}</span> },
    { key: "weight", header: "Weight", cell: (c) => `${Math.round((c.weight ?? 0) * 1000) / 10}%` },
    { key: "maxScore", header: "Out of" },
    {
      key: "actions", header: "", className: "text-right",
      cell: (c) => (
        <div className="flex justify-end gap-1">
          <Button variant="ghost" size="icon" disabled={maintenance} onClick={() => setEditing(c)} aria-label={`Edit ${c.name}`}><Pencil className="h-4 w-4" /></Button>
          <Button variant="ghost" size="icon" disabled={maintenance} onClick={() => setDeleting(c)} aria-label={`Delete ${c.name}`}><Trash2 className="h-4 w-4 text-danger" /></Button>
        </div>
      ),
    },
  ];

  return (
    <Card>
      <CardHeader
        title="Assessment components"
        description={
          <span className="flex items-center gap-2">
            Weights total {total}%
            {total === 100 ? <Badge tone="success">Ready to compute</Badge> : <Badge tone="warning">Must total 100%</Badge>}
          </span>
        }
        action={<Button size="sm" disabled={maintenance || total >= 100} onClick={() => setEditing("new")}><Plus className="h-4 w-4" /> Add</Button>}
      />
      <Table columns={columns} rows={list.data} rowKey={(c) => c.componentId!} loading={list.isLoading} empty={{ title: "No assessments", description: "Add a quiz, midsem or endsem to start grading." }} />
      {editing && (
        <AssessmentForm
          base={base}
          existing={editing === "new" ? undefined : editing}
          remaining={100 - total + (editing !== "new" ? Math.round((editing.weight ?? 0) * 1000) / 10 : 0)}
          onClose={() => setEditing(null)}
          onSaved={invalidate}
        />
      )}
      <ConfirmDialog
        open={!!deleting}
        onClose={() => setDeleting(null)}
        danger
        title={`Delete ${deleting?.name}?`}
        message="All scores entered for this component will be deleted too."
        confirmLabel="Delete"
        loading={remove.isPending}
        onConfirm={() => remove.mutate(deleting!.componentId!)}
      />
    </Card>
  );
}

function AssessmentForm({ base, existing, remaining, onClose, onSaved }: {
  base: string; existing?: Component; remaining: number; onClose: () => void; onSaved: () => void;
}) {
  const [name, setName] = useState(existing?.name ?? "");
  const [weight, setWeight] = useState(existing ? String(Math.round((existing.weight ?? 0) * 1000) / 10) : String(remaining));
  const [maxScore, setMaxScore] = useState(String(existing?.maxScore ?? 100));
  const save = useMutation({
    mutationFn: () => {
      const body = { name: name.trim(), weight: Number(weight) / 100, maxScore: Number(maxScore) };
      return existing ? api(`${base}/${existing.componentId}`, { method: "PUT", body }) : api(base, { body });
    },
    onSuccess: () => { notify.success(existing ? "Assessment updated" : "Assessment added"); onSaved(); onClose(); },
  });
  const w = Number(weight);
  const weightError = w <= 0 || w > remaining ? `Between 0 and ${remaining}%` : undefined;
  const submit = (e: FormEvent) => { e.preventDefault(); if (!weightError) save.mutate(); };

  return (
    <Modal
      open
      onClose={onClose}
      title={existing ? `Edit ${existing.name}` : "Add assessment"}
      footer={<><Button variant="outline" onClick={onClose}>Cancel</Button><Button type="submit" form="assessment-form" loading={save.isPending} disabled={!!weightError || !name.trim()}>Save</Button></>}
    >
      <form id="assessment-form" onSubmit={submit} className="space-y-4">
        <Field label="Name"><input className="input" value={name} onChange={(e) => setName(e.target.value)} placeholder="e.g. Midsem" required /></Field>
        <div className="grid grid-cols-2 gap-4">
          <Field label="Weight (%)" error={weightError}><input className="input" type="number" step="0.5" value={weight} onChange={(e) => setWeight(e.target.value)} /></Field>
          <Field label="Out of"><input className="input" type="number" min={1} value={maxScore} onChange={(e) => setMaxScore(e.target.value)} /></Field>
        </div>
        {save.error && <p role="alert" className="text-sm text-danger">{errorMessage(save.error)}</p>}
      </form>
    </Modal>
  );
}

// ------------------------------------------------------------------ final grades

function FinalGrades({ code, sectionId }: { code: string; sectionId: string }) {
  const base = `/api/instructor/${code}/sections/${encodeURIComponent(sectionId)}/grades`;
  const qc = useQueryClient();
  const maintenance = useMaintenance().data?.maintenanceOn;
  const grades = useQuery({ queryKey: [...sectionKey(code, sectionId), "final"], queryFn: () => api<Schemas["FinalGradeResponse"][]>(base) });
  const compute = useMutation({
    mutationFn: () => api<Schemas["FinalGradeResponse"][]>(`${base}/compute`, { method: "POST" }),
    onSuccess: (rows) => { notify.success(`Computed ${rows.length} final grade${rows.length === 1 ? "" : "s"}`); qc.invalidateQueries({ queryKey: sectionKey(code, sectionId) }); },
    onError: notify.error,
  });

  const columns: Column<Schemas["FinalGradeResponse"]>[] = [
    { key: "rollNo", header: "Roll no." },
    { key: "studentName", header: "Student", cell: (r) => <span className="font-medium">{r.studentName}</span> },
    { key: "totalScore", header: "Total", cell: (r) => (r.totalScore == null ? "—" : <span className="tabular-nums">{Number(r.totalScore).toFixed(2)}</span>) },
    { key: "finalGrade", header: "Grade", cell: (r) => (r.finalGrade ? <span className="text-base font-bold text-primary">{r.finalGrade}</span> : <Badge>Not computed</Badge>) },
  ];

  return (
    <Card>
      <CardHeader
        title="Final grades"
        description="Total = Σ (score ÷ out of × weight) × 100. A ≥ 90, A- ≥ 80, B ≥ 70, B- ≥ 60, C ≥ 50, C- ≥ 40, D ≥ 30, otherwise F."
        action={<Button size="sm" disabled={maintenance} loading={compute.isPending} onClick={() => compute.mutate()}><Calculator className="h-4 w-4" /> Compute grades</Button>}
      />
      <Table columns={columns} rows={grades.data} rowKey={(r) => r.enrollmentId!} loading={grades.isLoading} empty={{ title: "No students enrolled" }} />
    </Card>
  );
}

// ------------------------------------------------------------------ statistics

function Stats({ code, sectionId }: { code: string; sectionId: string }) {
  const stats = useQuery({
    queryKey: [...sectionKey(code, sectionId), "stats"],
    queryFn: () => api<Schemas["SectionStatsView"]>(`/api/instructor/${code}/sections/${encodeURIComponent(sectionId)}/stats`),
  });
  const distribution = useMemo(
    () => Object.entries(stats.data?.finalGrades?.distribution ?? {}).map(([grade, count]) => ({ grade, count: Number(count) })),
    [stats.data],
  );
  if (stats.isLoading) return <Spinner />;
  const s = stats.data;
  if (!s) return null;
  const fmt = (v?: number | null) => (v == null ? "—" : v.toFixed(1));

  return (
    <div className="space-y-6">
      <div className="grid gap-4 sm:grid-cols-3">
        <StatCard label="Students" value={s.enrolled ?? 0} />
        <StatCard label="Class average" value={fmt(s.finalGrades?.average)} hint={s.finalGrades?.computed ? `${s.finalGrades.computed} graded` : "Compute grades first"} tone="accent" />
        <StatCard label="Range" value={s.finalGrades?.computed ? `${fmt(s.finalGrades.min)} – ${fmt(s.finalGrades.max)}` : "—"} tone="warning" />
      </div>
      <div className="grid gap-6 lg:grid-cols-2">
        <Card>
          <CardHeader title="Grade distribution" />
          <div className="h-64 p-4">
            {s.finalGrades?.computed ? (
              <ResponsiveContainer>
                <BarChart data={distribution} margin={{ top: 8, right: 8, left: -16, bottom: 0 }}>
                  <CartesianGrid vertical={false} stroke="rgb(var(--border))" />
                  <XAxis dataKey="grade" tickLine={false} axisLine={false} tick={{ fill: "rgb(var(--fg-muted))", fontSize: 12 }} />
                  <YAxis allowDecimals={false} tickLine={false} axisLine={false} tick={{ fill: "rgb(var(--fg-muted))", fontSize: 12 }} />
                  <Tooltip
                    cursor={{ fill: "rgb(var(--muted))" }}
                    contentStyle={{ background: "rgb(var(--surface))", border: "1px solid rgb(var(--border))", borderRadius: 8, color: "rgb(var(--fg))" }}
                    formatter={(v) => [v, "Students"]}
                  />
                  <Bar dataKey="count" fill="rgb(var(--primary))" radius={[4, 4, 0, 0]} maxBarSize={40} />
                </BarChart>
              </ResponsiveContainer>
            ) : <EmptyState title="No final grades yet" description="Compute grades to see the distribution." />}
          </div>
        </Card>
        <Card>
          <CardHeader title="By assessment" description="Over scores that have been entered." />
          <Table
            rows={s.components}
            rowKey={(c) => c.componentId!}
            empty={{ title: "No assessments" }}
            columns={[
              { key: "name", header: "Assessment", cell: (c) => <span className="font-medium">{c.name}</span> },
              { key: "graded", header: "Graded", cell: (c) => `${c.graded}/${s.enrolled}` },
              { key: "averagePercent", header: "Avg %", cell: (c) => fmt(c.averagePercent) },
              { key: "median", header: "Median", hideOnMobile: true, cell: (c) => fmt(c.median) },
              { key: "range", header: "Min–Max", hideOnMobile: true, cell: (c) => (c.min == null ? "—" : `${fmt(c.min)}–${fmt(c.max)}`) },
            ]}
          />
        </Card>
      </div>
    </div>
  );
}
