import { useState } from "react";
import { keepPreviousData, useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { KeyRound, LockOpen } from "lucide-react";
import { api, type Page, type Schemas } from "@/lib/api";
import { notify } from "@/lib/toast";
import { formatDateTime, titleCase } from "@/lib/utils";
import { Button } from "@/components/ui/button";
import { Badge, Card, PageHeader } from "@/components/ui/misc";
import { ConfirmDialog } from "@/components/ui/modal";
import { Pagination, SearchInput, Table, useListState, type Column } from "@/components/ui/table";
import { TempPasswordDialog } from "./TempPasswordDialog";

type Account = Schemas["AccountResponse"];
const ROLE_TONE = { admin: "danger", instructor: "accent", student: "primary" } as const;

export function AccountsPage() {
  const list = useListState({ sort: "username" });
  const qc = useQueryClient();
  const [resetting, setResetting] = useState<Account | null>(null);
  const [issued, setIssued] = useState<{ username: string; password: string } | null>(null);

  const accounts = useQuery({
    queryKey: ["admin", "accounts", list.state],
    queryFn: () => api<Page<Account>>("/api/admin/users", { params: { ...list.state } }),
    placeholderData: keepPreviousData,
  });

  const unlock = useMutation({
    mutationFn: (username: string) => api(`/api/admin/users/${encodeURIComponent(username)}/unlock`, { method: "POST" }),
    onSuccess: (_, username) => { notify.success(`${username} unlocked`); qc.invalidateQueries({ queryKey: ["admin"] }); },
    onError: notify.error,
  });
  const reset = useMutation({
    mutationFn: (username: string) => api<Schemas["PasswordResetResponse"]>(`/api/admin/users/${encodeURIComponent(username)}/reset-password`, { method: "POST" }),
    onSuccess: (res) => {
      setResetting(null);
      setIssued({ username: res.username!, password: res.temporaryPassword! });
      qc.invalidateQueries({ queryKey: ["admin"] });
    },
    onError: (e) => { notify.error(e); setResetting(null); },
  });

  const columns: Column<Account>[] = [
    { key: "username", header: "Username", sortable: true, cell: (a) => <span className="font-mono text-[13px] font-medium">{a.username}</span> },
    { key: "role", header: "Role", sortable: true, cell: (a) => <Badge tone={ROLE_TONE[a.role as keyof typeof ROLE_TONE] ?? "neutral"}>{titleCase(a.role)}</Badge> },
    {
      key: "state", header: "State",
      cell: (a) => (
        <div className="flex flex-wrap gap-1">
          {a.locked ? <Badge tone="danger">Locked</Badge> : <Badge tone="success">Active</Badge>}
          {a.mustChangePassword && <Badge tone="warning">Temp password</Badge>}
        </div>
      ),
    },
    { key: "lastLogin", header: "Last sign-in", sortable: true, hideOnMobile: true, cell: (a) => formatDateTime(a.lastLogin) },
    {
      key: "actions", header: "", className: "text-right",
      cell: (a) => (
        <div className="flex justify-end gap-1">
          {a.locked && <Button size="sm" variant="outline" loading={unlock.isPending && unlock.variables === a.username} onClick={() => unlock.mutate(a.username!)}><LockOpen className="h-4 w-4" /> Unlock</Button>}
          <Button size="sm" variant="ghost" onClick={() => setResetting(a)}><KeyRound className="h-4 w-4" /> Reset</Button>
        </div>
      ),
    },
  ];

  return (
    <>
      <PageHeader title="Accounts" description="Sign-in accounts. Five wrong passwords lock an account for 15 minutes." />
      <Card>
        <div className="flex flex-wrap items-center justify-between gap-3 border-b border-border p-4">
          <SearchInput value={list.state.q} onChange={list.setSearch} placeholder="Search username or role" />
          <p className="text-sm text-fg-muted">{accounts.data?.totalItems ?? 0} accounts</p>
        </div>
        <Table columns={columns} rows={accounts.data?.items} rowKey={(a) => a.userId!} loading={accounts.isFetching}
          error={accounts.error} sort={list.state.sort} onSort={list.toggleSort} />
        <Pagination page={accounts.data} onPage={list.setPage} />
      </Card>
      <ConfirmDialog
        open={!!resetting}
        onClose={() => setResetting(null)}
        title={`Reset password for ${resetting?.username}?`}
        message="Their current password stops working immediately. They'll get a temporary one that must be changed at next sign-in."
        confirmLabel="Reset password"
        loading={reset.isPending}
        onConfirm={() => reset.mutate(resetting!.username!)}
      />
      <TempPasswordDialog value={issued} onClose={() => setIssued(null)} />
    </>
  );
}
