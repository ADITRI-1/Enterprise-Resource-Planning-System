import { useEffect, useState, type ReactNode } from "react";
import { AlertTriangle, ArrowDown, ArrowUp, ArrowUpDown, ChevronLeft, ChevronRight, Search } from "lucide-react";
import { cn } from "@/lib/utils";
import { errorMessage, type Page } from "@/lib/api";
import { Button } from "./button";
import { EmptyState, Spinner } from "./misc";

export type Column<T> = {
  key: string;
  header: ReactNode;
  cell?: (row: T) => ReactNode;
  sortable?: boolean;
  className?: string;
  /** Hide below the md breakpoint to keep tables readable on phones. */
  hideOnMobile?: boolean;
};

export type ListState = { q: string; sort: string; page: number; size: number };

/** Search / sort / page state for a server-paged list (matches the backend's ListParams). */
export function useListState(initial: Partial<ListState> = {}) {
  const [state, setState] = useState<ListState>({ q: "", sort: "", page: 0, size: 10, ...initial });
  return {
    state,
    setSearch: (q: string) => setState((s) => ({ ...s, q, page: 0 })),
    toggleSort: (key: string) =>
      setState((s) => ({ ...s, page: 0, sort: s.sort === key ? `${key},desc` : s.sort === `${key},desc` ? "" : key })),
    setPage: (page: number) => setState((s) => ({ ...s, page })),
  };
}

type TableProps<T> = {
  columns: Column<T>[];
  rows: T[] | undefined;
  rowKey: (row: T) => string;
  loading?: boolean;
  empty?: { title: string; description?: string };
  sort?: string;
  onSort?: (key: string) => void;
  rowClassName?: (row: T) => string | undefined;
  error?: unknown;
};

export function Table<T>({ columns, rows, rowKey, loading, empty, sort, onSort, rowClassName, error }: TableProps<T>) {
  if (error && !rows) return <EmptyState icon={<AlertTriangle className="h-6 w-6 text-danger" />} title="Couldn't load this list" description={errorMessage(error)} />;
  if (loading && !rows) return <Spinner />;
  if (!rows?.length) return <EmptyState title={empty?.title ?? "Nothing here yet"} description={empty?.description} />;
  return (
    <div className={cn("overflow-x-auto", loading && "opacity-60 transition-opacity")}>
      <table className="w-full text-left text-sm">
        <thead>
          <tr className="border-b border-border text-xs uppercase tracking-wide text-fg-muted">
            {columns.map((c) => {
              const active = sort === c.key ? "asc" : sort === `${c.key},desc` ? "desc" : null;
              return (
                <th
                  key={c.key}
                  scope="col"
                  aria-sort={active === "asc" ? "ascending" : active === "desc" ? "descending" : undefined}
                  className={cn("whitespace-nowrap px-4 py-3 font-medium", c.hideOnMobile && "hidden md:table-cell", c.className)}
                >
                  {c.sortable && onSort ? (
                    <button className="inline-flex items-center gap-1 uppercase tracking-wide hover:text-fg" onClick={() => onSort(c.key)}>
                      {c.header}
                      {active === "asc" ? <ArrowUp className="h-3.5 w-3.5" /> : active === "desc" ? <ArrowDown className="h-3.5 w-3.5" /> : <ArrowUpDown className="h-3.5 w-3.5 opacity-40" />}
                    </button>
                  ) : (
                    c.header
                  )}
                </th>
              );
            })}
          </tr>
        </thead>
        <tbody>
          {rows.map((row) => (
            <tr key={rowKey(row)} className={cn("border-b border-border/70 last:border-0 hover:bg-muted/40", rowClassName?.(row))}>
              {columns.map((c) => (
                <td key={c.key} className={cn("px-4 py-3 align-middle", c.hideOnMobile && "hidden md:table-cell", c.className)}>
                  {c.cell ? c.cell(row) : String((row as Record<string, unknown>)[c.key] ?? "—")}
                </td>
              ))}
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}

export function SearchInput({ value, onChange, placeholder = "Search…" }: { value: string; onChange: (v: string) => void; placeholder?: string }) {
  const [text, setText] = useState(value);
  useEffect(() => {
    const t = setTimeout(() => text !== value && onChange(text), 250);
    return () => clearTimeout(t);
  }, [text, value, onChange]);
  return (
    <div className="relative w-full sm:w-72">
      <Search className="pointer-events-none absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-fg-muted" />
      <input className="input pl-9" value={text} onChange={(e) => setText(e.target.value)} placeholder={placeholder} aria-label={placeholder} />
    </div>
  );
}

export function Pagination<T>({ page, onPage }: { page?: Page<T>; onPage: (p: number) => void }) {
  if (!page || page.totalItems === 0) return null;
  const from = page.page * page.size + 1;
  const to = Math.min(from + page.items.length - 1, page.totalItems);
  return (
    <div className="flex items-center justify-between gap-3 border-t border-border px-4 py-3 text-sm text-fg-muted">
      <span className="tabular-nums">
        {from}–{to} of {page.totalItems}
      </span>
      <div className="flex gap-1">
        <Button variant="outline" size="icon" disabled={page.page === 0} onClick={() => onPage(page.page - 1)} aria-label="Previous page">
          <ChevronLeft className="h-4 w-4" />
        </Button>
        <Button variant="outline" size="icon" disabled={page.page >= page.totalPages - 1} onClick={() => onPage(page.page + 1)} aria-label="Next page">
          <ChevronRight className="h-4 w-4" />
        </Button>
      </div>
    </div>
  );
}
