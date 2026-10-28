import { useState } from "react";
import { Link } from "react-router-dom";
import { useMaintenance, type Session } from "@/lib/session";
import { useTerm } from "@/lib/term";
import { notify } from "@/lib/toast";
import { formatDate, termLabel } from "@/lib/utils";
import type { Schemas } from "@/lib/api";
import { Button } from "@/components/ui/button";
import { Badge, Card, PageHeader } from "@/components/ui/misc";
import { ConfirmDialog } from "@/components/ui/modal";
import { Table, type Column } from "@/components/ui/table";
import { TermPicker } from "@/components/TermPicker";
import { useEnrollmentAction, useRegistrations } from "./queries";

type Row = Schemas["RegistrationView"];

const STATUS_TONE = { enrolled: "success", dropped: "neutral", completed: "primary" } as const;

export function RegistrationsPage({ session }: { session: Session }) {
  const uid = session.userId;
  const { term, setTerm } = useTerm();
  const registrations = useRegistrations(uid, term);
  const drop = useEnrollmentAction(uid, "drop");
  const maintenance = useMaintenance().data?.maintenanceOn;
  const [confirm, setConfirm] = useState<Row | null>(null);

  const active = registrations.data?.filter((r) => r.status === "enrolled") ?? [];
  const credits = active.reduce((s, r) => s + (r.credits ?? 0), 0);

  const columns: Column<Row>[] = [
    { key: "course", header: "Course", cell: (r) => <div><p className="font-medium">{r.courseId} · {r.courseTitle}</p><p className="text-xs text-fg-muted">{r.sectionId}</p></div> },
    { key: "instructorName", header: "Instructor", hideOnMobile: true },
    { key: "credits", header: "Credits", hideOnMobile: true },
    { key: "status", header: "Status", cell: (r) => <Badge tone={STATUS_TONE[r.status as keyof typeof STATUS_TONE] ?? "neutral"}>{r.status}</Badge> },
    { key: "finalGrade", header: "Grade", cell: (r) => r.finalGrade ? <span className="font-semibold text-primary">{r.finalGrade}</span> : <span className="text-fg-muted">—</span> },
    { key: "deadline", header: "Drop by", hideOnMobile: true, cell: (r) => formatDate(r.registrationDeadline) },
    {
      key: "action", header: "", className: "text-right",
      cell: (r) => r.canDrop ? <Button size="sm" variant="outline" disabled={maintenance} onClick={() => setConfirm(r)}>Drop</Button> : null,
    },
  ];

  return (
    <>
      <PageHeader
        title="My registrations"
        description={`${active.length} course${active.length === 1 ? "" : "s"} · ${credits} credits in ${termLabel(term)}`}
        actions={<><TermPicker value={term} onChange={setTerm} /><Link to="/student/catalog"><Button>Add courses</Button></Link></>}
      />
      <Card>
        <Table
          columns={columns}
          rows={registrations.data}
          rowKey={(r) => r.enrollmentId!}
          loading={registrations.isLoading}
          rowClassName={(r) => (r.status === "dropped" ? "opacity-60" : undefined)}
          empty={{ title: "No registrations this term", description: "Browse the catalog to register for sections." }}
        />
      </Card>
      <ConfirmDialog
        open={!!confirm}
        onClose={() => setConfirm(null)}
        danger
        title={`Drop ${confirm?.courseId}?`}
        message={<>You'll lose your seat in <b>{confirm?.courseTitle}</b>. You can register again later only if seats are still available.</>}
        confirmLabel="Drop course"
        loading={drop.isPending}
        onConfirm={() =>
          drop.mutate(confirm!.sectionId!, {
            onSuccess: () => { notify.success(`Dropped ${confirm!.courseId}`); setConfirm(null); },
            onError: (e) => { notify.error(e); setConfirm(null); },
          })
        }
      />
    </>
  );
}
