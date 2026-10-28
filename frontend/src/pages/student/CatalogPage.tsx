import { useState } from "react";
import { keepPreviousData, useQuery } from "@tanstack/react-query";
import { api, type Page, type Schemas } from "@/lib/api";
import { useMaintenance, type Session } from "@/lib/session";
import { useTerm } from "@/lib/term";
import { notify } from "@/lib/toast";
import { byDayAndTime, dayLabel, formatDate, hhmm } from "@/lib/utils";
import { Button } from "@/components/ui/button";
import { Badge, Card, PageHeader } from "@/components/ui/misc";
import { ConfirmDialog } from "@/components/ui/modal";
import { Pagination, SearchInput, Table, useListState, type Column } from "@/components/ui/table";
import { TermPicker } from "@/components/TermPicker";
import { useEnrollmentAction, usePassedCourses, useRegistrations } from "./queries";

type Row = Schemas["CatalogSectionView"];

function SeatBar({ enrolled = 0, capacity = 1 }: { enrolled?: number; capacity?: number }) {
  const pct = Math.min(100, Math.round((enrolled / Math.max(capacity, 1)) * 100));
  const tone = pct >= 100 ? "bg-danger" : pct >= 80 ? "bg-warning" : "bg-success";
  return (
    <div className="w-28">
      <div className="h-1.5 overflow-hidden rounded-full bg-muted">
        <div className={`h-full rounded-full ${tone}`} style={{ width: `${pct}%` }} />
      </div>
      <p className="mt-1 text-xs tabular-nums text-fg-muted">{Math.max(capacity - enrolled, 0)} of {capacity} left</p>
    </div>
  );
}

export function CatalogPage({ session }: { session: Session }) {
  const uid = session.userId;
  const { term, setTerm } = useTerm();
  const list = useListState({ sort: "courseId" });
  const maintenance = useMaintenance().data?.maintenanceOn;
  const [confirm, setConfirm] = useState<Row | null>(null);

  const catalog = useQuery({
    queryKey: ["catalog", term, list.state],
    queryFn: () => api<Page<Row>>("/api/catalog/sections", { params: { ...term, ...list.state } }),
    placeholderData: keepPreviousData,
  });
  const mine = useRegistrations(uid, term);
  const enrolledIn = new Set(mine.data?.filter((r) => r.status === "enrolled").map((r) => r.sectionId));
  const register = useEnrollmentAction(uid, "register");
  const passed = usePassedCourses(uid);
  const missingFor = (r: Row) => (r.prerequisites ?? []).filter((p) => !passed.has(p));

  const columns: Column<Row>[] = [
    {
      key: "courseId", header: "Course", sortable: true,
      cell: (r) => (
        <div className="min-w-[180px]">
          <p className="font-medium">{r.courseId} · {r.title}</p>
          <p className="text-xs text-fg-muted">{r.credits} credits · {r.deptId}<span className="md:hidden"> · {r.seatsLeft} seats left</span></p>
          {!!r.prerequisites?.length && (
            <p className="mt-1 flex flex-wrap items-center gap-1 text-xs text-fg-muted">
              Requires
              {r.prerequisites.map((p) => (
                <span key={p} className={passed.has(p) ? "font-medium text-success" : "font-medium text-warning"}>
                  {passed.has(p) ? "✓" : "✗"} {p}
                </span>
              ))}
            </p>
          )}
        </div>
      ),
    },
    { key: "instructorName", header: "Instructor", sortable: true, hideOnMobile: true },
    {
      key: "schedule", header: "Schedule", hideOnMobile: true,
      cell: (r) => (
        <div className="space-y-0.5 text-xs text-fg-muted">
          {r.schedule?.length ? [...r.schedule].sort(byDayAndTime).map((s) => (
            <p key={`${s.day}${s.startTime}`} className="whitespace-nowrap">{dayLabel(s.day, true)} {hhmm(s.startTime)}–{hhmm(s.endTime)} · {s.roomId}</p>
          )) : "TBA"}
        </div>
      ),
    },
    { key: "seatsLeft", header: "Seats", sortable: true, hideOnMobile: true, cell: (r) => <SeatBar enrolled={r.enrolled} capacity={r.capacity} /> },
    { key: "registrationDeadline", header: "Deadline", sortable: true, hideOnMobile: true, cell: (r) => formatDate(r.registrationDeadline) },
    {
      key: "action", header: "", className: "text-right",
      cell: (r) =>
        enrolledIn.has(r.sectionId) ? <Badge tone="success">Registered</Badge>
          : !r.open ? <Badge tone={r.seatsLeft === 0 ? "danger" : "neutral"}>{r.seatsLeft === 0 ? "Full" : "Closed"}</Badge>
          : missingFor(r).length ? <Badge tone="warning">Needs {missingFor(r).join(", ")}</Badge>
          : <Button size="sm" disabled={maintenance} onClick={() => setConfirm(r)}>Register</Button>,
    },
  ];

  return (
    <>
      <PageHeader title="Course catalog" description="Sections open for registration this term." actions={<TermPicker value={term} onChange={setTerm} />} />
      <Card>
        <div className="flex flex-wrap items-center justify-between gap-3 border-b border-border p-4">
          <SearchInput value={list.state.q} onChange={list.setSearch} placeholder="Search course, title or instructor" />
          <p className="text-sm text-fg-muted">{catalog.data?.totalItems ?? 0} sections</p>
        </div>
        <Table
          columns={columns}
          rows={catalog.data?.items}
          rowKey={(r) => r.sectionId!}
          loading={catalog.isFetching}
          error={catalog.error}
          sort={list.state.sort}
          onSort={list.toggleSort}
          empty={{ title: "No sections found", description: "Try another term or search." }}
        />
        <Pagination page={catalog.data} onPage={list.setPage} />
      </Card>

      <ConfirmDialog
        open={!!confirm}
        onClose={() => setConfirm(null)}
        title={`Register for ${confirm?.courseId}?`}
        message={<>You'll be added to <b>{confirm?.title}</b> with {confirm?.instructorName}. You can drop it until {formatDate(confirm?.registrationDeadline)}.</>}
        confirmLabel="Register"
        loading={register.isPending}
        onConfirm={() =>
          register.mutate(confirm!.sectionId!, {
            onSuccess: () => { notify.success(`Registered for ${confirm!.courseId}`); setConfirm(null); },
            onError: (e) => { notify.error(e); setConfirm(null); },
          })
        }
      />
    </>
  );
}
