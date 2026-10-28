import { useQuery } from "@tanstack/react-query";
import { Bar, BarChart, CartesianGrid, Cell, ResponsiveContainer, Tooltip, XAxis, YAxis } from "recharts";
import { api, type Schemas } from "@/lib/api";
import { useTerm } from "@/lib/term";
import { termLabel } from "@/lib/utils";
import { Card, CardHeader, EmptyState, PageHeader, Spinner } from "@/components/ui/misc";
import { TermPicker } from "@/components/TermPicker";

const axis = { tickLine: false, axisLine: false, tick: { fill: "rgb(var(--fg-muted))", fontSize: 12 } } as const;
const tooltip = {
  cursor: { fill: "rgb(var(--muted))" },
  contentStyle: { background: "rgb(var(--surface))", border: "1px solid rgb(var(--border))", borderRadius: 8, color: "rgb(var(--fg))" },
};

function ChartCard({ title, description, empty, children }: { title: string; description?: string; empty: boolean; children: React.ReactElement }) {
  return (
    <Card>
      <CardHeader title={title} description={description} />
      <div className="h-72 p-4">{empty ? <EmptyState title="No data yet" /> : <ResponsiveContainer>{children}</ResponsiveContainer>}</div>
    </Card>
  );
}

export function AnalyticsPage() {
  const { term, setTerm } = useTerm();
  const data = useQuery({
    queryKey: ["admin", "analytics", term],
    queryFn: () => api<Schemas["AnalyticsView"]>("/api/admin/analytics", { params: { ...term } }),
  });
  if (data.isLoading || !data.data) return <Spinner />;
  const a = data.data;

  const fill = (a.sectionFill ?? []).map((s) => ({
    name: s.courseId,
    section: s.sectionId,
    pct: Math.round(((s.enrolled ?? 0) / Math.max(s.capacity ?? 1, 1)) * 100),
    label: `${s.enrolled}/${s.capacity}`,
  }));
  const grades = Object.entries(a.gradeDistribution ?? {}).map(([grade, count]) => ({ grade, count: Number(count) }));
  const days = (a.registrationsByDay ?? []).map((d) => ({
    day: new Date(d.date + "T00:00:00").toLocaleDateString(undefined, { day: "numeric", month: "short" }),
    registrations: d.registrations,
  }));

  return (
    <>
      <PageHeader title="Analytics" description={`Enrollment and results for ${termLabel(term)}.`} actions={<TermPicker value={term} onChange={setTerm} />} />
      <div className="grid gap-6 lg:grid-cols-2">
        <ChartCard title="Section fill" description="Share of seats taken" empty={!fill.length}>
          <BarChart data={fill} layout="vertical" margin={{ left: 8, right: 24 }}>
            <CartesianGrid horizontal={false} stroke="rgb(var(--border))" />
            <XAxis type="number" domain={[0, 100]} unit="%" {...axis} />
            <YAxis type="category" dataKey="name" width={64} {...axis} />
            <Tooltip {...tooltip} formatter={(v, _n, p) => [`${v}% (${p.payload.label})`, p.payload.section]} />
            <Bar dataKey="pct" radius={[0, 4, 4, 0]} maxBarSize={22}>
              {fill.map((f) => <Cell key={f.section} fill={f.pct >= 100 ? "rgb(var(--danger))" : f.pct >= 80 ? "rgb(var(--warning))" : "rgb(var(--primary))"} />)}
            </Bar>
          </BarChart>
        </ChartCard>

        <ChartCard title="Grade distribution" description="Final grades computed this term" empty={!grades.some((g) => g.count)}>
          <BarChart data={grades} margin={{ left: -16, right: 8 }}>
            <CartesianGrid vertical={false} stroke="rgb(var(--border))" />
            <XAxis dataKey="grade" {...axis} />
            <YAxis allowDecimals={false} {...axis} />
            <Tooltip {...tooltip} formatter={(v) => [v, "Students"]} />
            <Bar dataKey="count" fill="rgb(var(--accent))" radius={[4, 4, 0, 0]} maxBarSize={40} />
          </BarChart>
        </ChartCard>

        <ChartCard title="Enrollments by department" empty={!a.enrollmentsByDepartment?.length}>
          <BarChart data={a.enrollmentsByDepartment} margin={{ left: -16, right: 8 }}>
            <CartesianGrid vertical={false} stroke="rgb(var(--border))" />
            <XAxis dataKey="deptId" {...axis} />
            <YAxis allowDecimals={false} {...axis} />
            <Tooltip {...tooltip} formatter={(v) => [v, "Enrolled"]} />
            <Bar dataKey="enrolled" fill="rgb(var(--primary))" radius={[4, 4, 0, 0]} maxBarSize={48} />
          </BarChart>
        </ChartCard>

        <ChartCard title="Registrations" description="Last 30 days, all terms" empty={!days.length}>
          <BarChart data={days} margin={{ left: -16, right: 8 }}>
            <CartesianGrid vertical={false} stroke="rgb(var(--border))" />
            <XAxis dataKey="day" {...axis} />
            <YAxis allowDecimals={false} {...axis} />
            <Tooltip {...tooltip} />
            <Bar dataKey="registrations" fill="rgb(var(--primary))" radius={[4, 4, 0, 0]} maxBarSize={28} />
          </BarChart>
        </ChartCard>
      </div>
    </>
  );
}
