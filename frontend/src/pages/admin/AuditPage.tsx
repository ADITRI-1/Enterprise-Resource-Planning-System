import { useState } from "react";
import { keepPreviousData, useQuery } from "@tanstack/react-query";
import { api, type Page, type Schemas } from "@/lib/api";
import { formatDateTime, titleCase } from "@/lib/utils";
import { Badge, Card, PageHeader } from "@/components/ui/misc";
import { Pagination, SearchInput, Table, type Column } from "@/components/ui/table";

type Entry = Schemas["AuditEntryResponse"];

/** Turns "/api/student/<uuid>/register" into something an admin can scan. */
function describe(e: Entry) {
  const path = (e.path ?? "").replace(/^\/api\//, "").replace(/[0-9a-f]{8}-[0-9a-f-]{27}/gi, "…");
  return `${e.method} ${path}`;
}

export function AuditPage() {
  const [q, setQ] = useState("");
  const [page, setPage] = useState(0);
  const log = useQuery({
    queryKey: ["admin", "audit", q, page],
    queryFn: () => api<Page<Entry>>("/api/admin/audit", { params: { q, page, size: 25 } }),
    placeholderData: keepPreviousData,
  });

  const columns: Column<Entry>[] = [
    { key: "at", header: "When", cell: (e) => <span className="whitespace-nowrap tabular-nums">{formatDateTime(e.at)}</span> },
    { key: "actor", header: "Who", cell: (e) => <div><p className="font-mono text-[13px] font-medium">{e.actor}</p>{e.role && <p className="text-xs text-fg-muted">{titleCase(e.role)}</p>}</div> },
    { key: "action", header: "Action", cell: (e) => <span className="font-mono text-xs">{describe(e)}</span> },
    {
      key: "status", header: "Result",
      cell: (e) => {
        const s = e.status ?? 0;
        return <Badge tone={s < 300 ? "success" : s === 503 ? "warning" : s < 500 ? "danger" : "danger"}>{s}</Badge>;
      },
    },
    { key: "ip", header: "IP", hideOnMobile: true, cell: (e) => <span className="text-xs text-fg-muted">{e.ip}</span> },
  ];

  return (
    <>
      <PageHeader title="Audit log" description="Every change and sign-in attempt, newest first. Request bodies (like passwords) are never stored." />
      <Card>
        <div className="flex flex-wrap items-center justify-between gap-3 border-b border-border p-4">
          <SearchInput value={q} onChange={(v) => { setQ(v); setPage(0); }} placeholder="Filter by user or path" />
          <p className="text-sm text-fg-muted">{log.data?.totalItems ?? 0} entries</p>
        </div>
        <Table columns={columns} rows={log.data?.items} rowKey={(e) => String(e.id)} loading={log.isFetching}
          error={log.error} empty={{ title: "Nothing logged yet" }} />
        <Pagination page={log.data} onPage={setPage} />
      </Card>
    </>
  );
}
