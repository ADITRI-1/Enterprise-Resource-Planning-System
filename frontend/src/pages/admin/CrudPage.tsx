import { useState, type FormEvent, type ReactNode } from "react";
import { keepPreviousData, useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { Pencil, Plus, Trash2 } from "lucide-react";
import { api, errorMessage, type Page } from "@/lib/api";
import { useMaintenance } from "@/lib/session";
import { notify } from "@/lib/toast";
import { Button } from "@/components/ui/button";
import { Card, Field, PageHeader } from "@/components/ui/misc";
import { ConfirmDialog, Modal } from "@/components/ui/modal";
import { Pagination, SearchInput, Table, useListState, type Column } from "@/components/ui/table";

export type Option = { value: string; label: string };

export type FieldDef = {
  name: string;
  label: string;
  type?: "text" | "number" | "date" | "email" | "select" | "checkbox";
  options?: Option[];
  required?: boolean;
  /** Part of the record's identity: editable when creating, read-only afterwards. */
  createOnly?: boolean;
  /** Only shown when editing (e.g. "active"). */
  editOnly?: boolean;
  hint?: string;
  placeholder?: string;
  min?: number;
};

export type FormValues = Record<string, string>;

export type CrudConfig<T> = {
  title: string;
  description?: string;
  singular: string;
  queryKey: string;
  listPath: string;
  extraParams?: Record<string, string | number | undefined>;
  createPath: string | ((form: FormValues) => string);
  itemPath: (row: T) => string;
  rowKey: (row: T) => string;
  rowLabel: (row: T) => string;
  columns: Column<T>[];
  fields: FieldDef[];
  defaultSort?: string;
  toForm: (row: T) => FormValues;
  /** Initial values for the create form. */
  defaults?: FormValues;
  toBody: (form: FormValues, mode: "create" | "edit") => unknown;
  /** E.g. show a one-time temporary password after creating a user. */
  onCreated?: (response: unknown) => void;
  deleteLabel?: string;
  deleteMessage?: (row: T) => ReactNode;
  rowActions?: (row: T) => ReactNode;
  headerActions?: ReactNode;
};

export function CrudPage<T>({ config }: { config: CrudConfig<T> }) {
  const list = useListState({ sort: config.defaultSort ?? "" });
  const qc = useQueryClient();
  const maintenance = useMaintenance().data?.maintenanceOn;
  const [editing, setEditing] = useState<{ mode: "create" | "edit"; row?: T } | null>(null);
  const [deleting, setDeleting] = useState<T | null>(null);

  const data = useQuery({
    queryKey: ["admin", config.queryKey, list.state, config.extraParams],
    queryFn: () => api<Page<T>>(config.listPath, { params: { ...config.extraParams, ...list.state } }),
    placeholderData: keepPreviousData,
  });

  const remove = useMutation({
    mutationFn: (row: T) => api(config.itemPath(row), { method: "DELETE" }),
    onSuccess: () => {
      notify.success(`${config.singular} deleted`);
      setDeleting(null);
      qc.invalidateQueries({ queryKey: ["admin"] });
    },
    onError: (e) => { notify.error(e); setDeleting(null); },
  });

  const columns: Column<T>[] = [
    ...config.columns,
    {
      key: "__actions", header: "", className: "text-right",
      cell: (row) => (
        <div className="flex justify-end gap-1">
          {config.rowActions?.(row)}
          <Button variant="ghost" size="icon" disabled={maintenance} onClick={() => setEditing({ mode: "edit", row })} aria-label={`Edit ${config.rowLabel(row)}`}>
            <Pencil className="h-4 w-4" />
          </Button>
          <Button variant="ghost" size="icon" disabled={maintenance} onClick={() => setDeleting(row)} aria-label={`Delete ${config.rowLabel(row)}`}>
            <Trash2 className="h-4 w-4 text-danger" />
          </Button>
        </div>
      ),
    },
  ];

  return (
    <>
      <PageHeader
        title={config.title}
        description={config.description}
        actions={<>{config.headerActions}<Button disabled={maintenance} onClick={() => setEditing({ mode: "create" })}><Plus className="h-4 w-4" /> New {config.singular.toLowerCase()}</Button></>}
      />
      <Card>
        <div className="flex flex-wrap items-center justify-between gap-3 border-b border-border p-4">
          <SearchInput value={list.state.q} onChange={list.setSearch} placeholder={`Search ${config.title.toLowerCase()}`} />
          <p className="text-sm text-fg-muted">{data.data?.totalItems ?? 0} total</p>
        </div>
        <Table
          columns={columns}
          rows={data.data?.items}
          rowKey={config.rowKey}
          loading={data.isFetching}
          sort={list.state.sort}
          onSort={list.toggleSort}
          empty={{ title: `No ${config.title.toLowerCase()} found` }}
        />
        <Pagination page={data.data} onPage={list.setPage} />
      </Card>

      {editing && <CrudForm config={config} mode={editing.mode} row={editing.row} onClose={() => setEditing(null)} />}

      <ConfirmDialog
        open={!!deleting}
        onClose={() => setDeleting(null)}
        danger
        title={`${config.deleteLabel ?? "Delete"} ${deleting ? config.rowLabel(deleting) : ""}?`}
        message={deleting && config.deleteMessage ? config.deleteMessage(deleting) : "This can't be undone."}
        confirmLabel={config.deleteLabel ?? "Delete"}
        loading={remove.isPending}
        onConfirm={() => remove.mutate(deleting!)}
      />
    </>
  );
}

function CrudForm<T>({ config, mode, row, onClose }: { config: CrudConfig<T>; mode: "create" | "edit"; row?: T; onClose: () => void }) {
  const qc = useQueryClient();
  const fields = config.fields.filter((f) => (mode === "create" ? !f.editOnly : true));
  const [form, setForm] = useState<FormValues>(() => {
    const initial: FormValues = Object.fromEntries(fields.map((f) => [f.name, f.type === "checkbox" ? "true" : ""]));
    return row ? { ...initial, ...config.toForm(row) } : { ...initial, ...config.defaults };
  });
  const save = useMutation({
    mutationFn: () =>
      mode === "create"
        ? api(typeof config.createPath === "function" ? config.createPath(form) : config.createPath, { body: config.toBody(form, mode) })
        : api(config.itemPath(row!), { method: "PUT", body: config.toBody(form, mode) }),
    onSuccess: (res) => {
      notify.success(`${config.singular} ${mode === "create" ? "created" : "updated"}`);
      qc.invalidateQueries({ queryKey: ["admin"] });
      onClose();
      if (mode === "create") config.onCreated?.(res);
    },
  });
  const set = (name: string, value: string) => setForm((f) => ({ ...f, [name]: value }));
  const submit = (e: FormEvent) => { e.preventDefault(); save.mutate(); };

  return (
    <Modal
      open
      onClose={onClose}
      size="lg"
      title={mode === "create" ? `New ${config.singular.toLowerCase()}` : `Edit ${config.rowLabel(row!)}`}
      footer={<><Button variant="outline" onClick={onClose}>Cancel</Button><Button type="submit" form="crud-form" loading={save.isPending}>Save</Button></>}
    >
      <form id="crud-form" onSubmit={submit} className="grid gap-4 sm:grid-cols-2">
        {fields.map((f) => {
          const locked = mode === "edit" && f.createOnly;
          if (f.type === "checkbox") {
            return (
              <label key={f.name} className="flex items-center gap-2 text-sm sm:col-span-2">
                <input type="checkbox" className="h-4 w-4 accent-[rgb(var(--primary))]" checked={form[f.name] === "true"} onChange={(e) => set(f.name, String(e.target.checked))} />
                {f.label}
              </label>
            );
          }
          return (
            <Field key={f.name} label={f.label} hint={locked ? "Can't be changed" : f.hint}>
              {f.type === "select" ? (
                <select className="input" value={form[f.name]} disabled={locked} required={f.required} onChange={(e) => set(f.name, e.target.value)}>
                  <option value="">Select…</option>
                  {f.options?.map((o) => <option key={o.value} value={o.value}>{o.label}</option>)}
                </select>
              ) : (
                <input
                  className="input"
                  type={f.type ?? "text"}
                  min={f.min}
                  value={form[f.name]}
                  disabled={locked}
                  required={f.required}
                  placeholder={f.placeholder}
                  onChange={(e) => set(f.name, e.target.value)}
                />
              )}
            </Field>
          );
        })}
        {save.error && <p role="alert" className="text-sm text-danger sm:col-span-2">{errorMessage(save.error)}</p>}
      </form>
    </Modal>
  );
}

/** Options for a select, loaded from another admin list endpoint. */
export function useOptions<T>(path: string, toOption: (row: T) => Option) {
  const q = useQuery({
    queryKey: ["admin", "options", path],
    queryFn: () => api<Page<T>>(path, { params: { size: 200 } }),
    staleTime: 60_000,
  });
  return (q.data?.items ?? []).map(toOption);
}
