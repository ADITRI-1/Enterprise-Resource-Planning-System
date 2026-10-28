import type { components } from "@/api/schema";

export type Schemas = components["schemas"];

export type Page<T> = { items: T[]; page: number; size: number; totalItems: number; totalPages: number };

export class ApiError extends Error {
  constructor(
    public status: number,
    public errorCode: string | undefined,
    message: string,
    public details: Record<string, unknown> = {},
  ) {
    super(message);
  }
}

/** Fired on any 401 so the app can drop back to the login screen. */
export const UNAUTHORIZED_EVENT = "erp:unauthorized";

type Options = { method?: string; body?: unknown; params?: Record<string, string | number | boolean | undefined | null> };

function buildUrl(path: string, params?: Options["params"]) {
  const url = new URL(path, window.location.origin);
  Object.entries(params ?? {}).forEach(([k, v]) => {
    if (v !== undefined && v !== null && v !== "") url.searchParams.set(k, String(v));
  });
  return url.pathname + url.search;
}

async function toError(res: Response): Promise<ApiError> {
  let body: { errorCode?: string; message?: string; details?: Record<string, unknown> } = {};
  try {
    body = await res.json();
  } catch {
    /* non-JSON error page */
  }
  return new ApiError(res.status, body.errorCode, body.message ?? `Request failed (${res.status})`, body.details ?? {});
}

export async function api<T>(path: string, opts: Options = {}): Promise<T> {
  const isForm = opts.body instanceof FormData;
  const res = await fetch(buildUrl(path, opts.params), {
    method: opts.method ?? (opts.body ? "POST" : "GET"),
    credentials: "same-origin",
    headers: opts.body && !isForm ? { "Content-Type": "application/json" } : undefined,
    body: opts.body ? (isForm ? (opts.body as FormData) : JSON.stringify(opts.body)) : undefined,
  });
  if (!res.ok) {
    const err = await toError(res);
    if (res.status === 401 && !path.startsWith("/api/auth/login")) {
      window.dispatchEvent(new Event(UNAUTHORIZED_EVENT));
    }
    throw err;
  }
  if (res.status === 204) return undefined as T;
  const type = res.headers.get("content-type") ?? "";
  return (type.includes("json") ? res.json() : res.text()) as Promise<T>;
}

/** Downloads a file endpoint (e.g. CSV) through the browser. */
export async function download(path: string, fallbackName: string) {
  const res = await fetch(path, { credentials: "same-origin" });
  if (!res.ok) throw await toError(res);
  const blob = await res.blob();
  const disposition = res.headers.get("content-disposition") ?? "";
  const name = /filename="?([^";]+)"?/.exec(disposition)?.[1] ?? fallbackName;
  const url = URL.createObjectURL(blob);
  const a = Object.assign(document.createElement("a"), { href: url, download: name });
  document.body.appendChild(a);
  a.click();
  a.remove();
  URL.revokeObjectURL(url);
}

export function errorMessage(err: unknown) {
  if (err instanceof ApiError) {
    const reason = typeof err.details.reason === "string" ? `: ${err.details.reason}` : "";
    return err.message + reason;
  }
  return err instanceof Error ? err.message : "Something went wrong";
}
