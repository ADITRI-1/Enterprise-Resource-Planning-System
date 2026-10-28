import { useEffect, useState } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { CalendarClock, Plus, Trash2 } from "lucide-react";
import { api, errorMessage, type Schemas } from "@/lib/api";
import { useTerm } from "@/lib/term";
import { notify } from "@/lib/toast";
import { DAYS, byDayAndTime, dayLabel, formatDate, hhmm, termLabel, type Day } from "@/lib/utils";
import { Button } from "@/components/ui/button";
import { Badge, Field, Spinner } from "@/components/ui/misc";
import { Modal } from "@/components/ui/modal";
import { TermPicker } from "@/components/TermPicker";
import { CrudPage, useOptions, type CrudConfig } from "./CrudPage";

type Section = Schemas["SectionResponse"];
type Slot = { day: Day; startTime: string; endTime: string; roomId: string };

export function SectionsPage() {
  const { term, setTerm } = useTerm();
  const [scheduling, setScheduling] = useState<Section | null>(null);
  const courses = useOptions<Schemas["CourseResponse"]>("/api/admin/courses", (c) => ({ value: c.courseId!, label: `${c.courseId} · ${c.title}` }));
  const instructors = useOptions<Schemas["InstructorResponse"]>("/api/admin/instructors", (i) => ({ value: i.instructorCode!, label: `${i.instructorCode} · ${i.name}` }));

  const config: CrudConfig<Section> = {
    title: "Sections",
    description: `Sections running in ${termLabel(term)}. Course, instructor and term are fixed once a section exists.`,
    singular: "Section",
    queryKey: "sections",
    listPath: "/api/admin/sections",
    extraParams: { semester: term.semester, year: term.year },
    createPath: (f) => `/api/admin/courses/${encodeURIComponent(f.courseId)}/sections`,
    itemPath: (r) => `/api/admin/sections/${encodeURIComponent(r.sectionId!)}`,
    rowKey: (r) => r.sectionId!,
    rowLabel: (r) => r.sectionId!,
    defaultSort: "sectionId",
    defaults: { semester: term.semester, year: String(term.year), active: "true" },
    headerActions: <TermPicker value={term} onChange={setTerm} />,
    columns: [
      { key: "sectionId", header: "Section", sortable: true, cell: (r) => <div><p className="font-medium">{r.courseId}</p><p className="text-xs text-fg-muted">{r.sectionId}</p></div> },
      { key: "instructorCode", header: "Instructor", sortable: true },
      { key: "enrollmentCount", header: "Enrolled", sortable: true, cell: (r) => <span className="tabular-nums">{r.enrollmentCount}/{r.capacity}</span> },
      { key: "registrationDeadline", header: "Deadline", sortable: true, hideOnMobile: true, cell: (r) => formatDate(r.registrationDeadline) },
      { key: "active", header: "Status", cell: (r) => <Badge tone={r.active ? "success" : "neutral"}>{r.active ? "Active" : "Inactive"}</Badge> },
    ],
    fields: [
      { name: "courseId", label: "Course", type: "select", options: courses, required: true, createOnly: true },
      { name: "instructorCode", label: "Instructor", type: "select", options: instructors, required: true, createOnly: true },
      { name: "semester", label: "Semester", type: "select", options: [{ value: "MONSOON", label: "Monsoon" }, { value: "WINTER", label: "Winter" }], required: true, createOnly: true },
      { name: "year", label: "Year", type: "number", required: true, createOnly: true },
      { name: "capacity", label: "Capacity", type: "number", min: 1, required: true },
      { name: "registrationDeadline", label: "Registration / drop deadline", type: "date", required: true },
      { name: "active", label: "Open for registration", type: "checkbox", editOnly: true },
    ],
    toForm: (r) => ({
      courseId: r.courseId ?? "", instructorCode: r.instructorCode ?? "", semester: r.semester ?? term.semester,
      year: String(r.year ?? term.year), capacity: String(r.capacity ?? ""), registrationDeadline: r.registrationDeadline ?? "",
      active: String(r.active ?? true),
    }),
    toBody: (f) => ({
      instructorCode: f.instructorCode, semester: f.semester || term.semester, year: Number(f.year || term.year),
      capacity: Number(f.capacity), registrationDeadline: f.registrationDeadline, active: f.active !== "false",
    }),
    deleteLabel: "Deactivate",
    deleteMessage: () => "Students can no longer register. Existing enrollments and grades are kept.",
    rowActions: (r) => (
      <Button variant="ghost" size="icon" onClick={() => setScheduling(r)} aria-label={`Schedule ${r.sectionId}`}>
        <CalendarClock className="h-4 w-4" />
      </Button>
    ),
  };

  return (
    <>
      <CrudPage config={config} />
      {scheduling && <ScheduleEditor section={scheduling} onClose={() => setScheduling(null)} />}
    </>
  );
}

function ScheduleEditor({ section, onClose }: { section: Section; onClose: () => void }) {
  const path = `/api/admin/sections/${encodeURIComponent(section.sectionId!)}/schedule`;
  const qc = useQueryClient();
  const current = useQuery({ queryKey: ["admin", "schedule", section.sectionId], queryFn: () => api<Schemas["SectionScheduleResponse"]>(path) });
  const [slots, setSlots] = useState<Slot[] | null>(null);
  const [draft, setDraft] = useState<Slot>({ day: "MONDAY", startTime: "09:00", endTime: "10:30", roomId: "" });

  useEffect(() => {
    if (current.data && slots === null) {
      setSlots((current.data.slots ?? []).map((s) => ({ day: s.day!, startTime: hhmm(s.startTime), endTime: hhmm(s.endTime), roomId: s.roomId! })).sort(byDayAndTime));
    }
  }, [current.data, slots]);

  const validTime = draft.startTime < draft.endTime;
  const rooms = useQuery({
    queryKey: ["admin", "rooms-available", draft.day, draft.startTime, draft.endTime, section.capacity],
    queryFn: () => api<Schemas["RoomAvailabilityResponse"][]>("/api/admin/rooms/available", {
      params: { day: draft.day, start: draft.startTime, end: draft.endTime, minCapacity: section.capacity },
    }),
    enabled: validTime,
  });

  const save = useMutation({
    mutationFn: () => (slots?.length ? api(path, { method: "PUT", body: { slots } }) : api(path, { method: "DELETE" })),
    onSuccess: () => {
      notify.success("Schedule saved");
      qc.invalidateQueries({ queryKey: ["admin"] });
      qc.invalidateQueries({ queryKey: ["catalog"] });
      onClose();
    },
  });

  const add = () => {
    setSlots((s) => [...(s ?? []), draft].sort(byDayAndTime));
    setDraft((d) => ({ ...d, roomId: "" }));
  };

  return (
    <Modal
      open
      onClose={onClose}
      size="lg"
      title={`Schedule · ${section.sectionId}`}
      description={`Needs a room for ${section.capacity} students. Clashes with other classes in the room or for the instructor are rejected.`}
      footer={<><Button variant="outline" onClick={onClose}>Cancel</Button><Button onClick={() => save.mutate()} loading={save.isPending}>Save schedule</Button></>}
    >
      {current.isLoading || slots === null ? <Spinner /> : (
        <div className="space-y-5">
          <div>
            <p className="mb-2 text-sm font-medium">Weekly slots</p>
            {slots.length === 0 ? <p className="rounded-lg bg-muted p-3 text-sm text-fg-muted">No slots yet.</p> : (
              <ul className="divide-y divide-border rounded-lg border border-border">
                {slots.map((s, i) => (
                  <li key={`${s.day}-${s.startTime}-${i}`} className="flex items-center justify-between px-3 py-2 text-sm">
                    <span><b>{dayLabel(s.day)}</b> {s.startTime}–{s.endTime} · Room {s.roomId}</span>
                    <Button variant="ghost" size="icon" onClick={() => setSlots(slots.filter((_, j) => j !== i))} aria-label="Remove slot">
                      <Trash2 className="h-4 w-4 text-danger" />
                    </Button>
                  </li>
                ))}
              </ul>
            )}
          </div>

          <div className="rounded-xl border border-dashed border-border p-4">
            <p className="mb-3 text-sm font-medium">Add a slot</p>
            <div className="grid gap-3 sm:grid-cols-4">
              <Field label="Day">
                <select className="input" value={draft.day} onChange={(e) => setDraft({ ...draft, day: e.target.value as Day, roomId: "" })}>
                  {DAYS.slice(0, 6).map((d) => <option key={d} value={d}>{dayLabel(d)}</option>)}
                </select>
              </Field>
              <Field label="Start"><input className="input" type="time" step={900} value={draft.startTime} onChange={(e) => setDraft({ ...draft, startTime: e.target.value, roomId: "" })} /></Field>
              <Field label="End" error={validTime ? undefined : "After start"}><input className="input" type="time" step={900} value={draft.endTime} onChange={(e) => setDraft({ ...draft, endTime: e.target.value, roomId: "" })} /></Field>
              <Field label="Free room">
                <select className="input" value={draft.roomId} disabled={!validTime || rooms.isLoading} onChange={(e) => setDraft({ ...draft, roomId: e.target.value })}>
                  <option value="">{rooms.isLoading ? "Checking…" : rooms.data?.length ? "Select…" : "None free"}</option>
                  {rooms.data?.map((r) => <option key={r.roomId} value={r.roomId}>{r.roomId} ({r.capacity})</option>)}
                </select>
              </Field>
            </div>
            {rooms.error && <p className="mt-2 text-sm text-danger">{errorMessage(rooms.error)}</p>}
            <Button className="mt-3" size="sm" variant="secondary" disabled={!draft.roomId || !validTime} onClick={add}>
              <Plus className="h-4 w-4" /> Add slot
            </Button>
          </div>
          {save.error && <p role="alert" className="text-sm text-danger">{errorMessage(save.error)}</p>}
        </div>
      )}
    </Modal>
  );
}
