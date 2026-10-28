import { useState } from "react";
import type { Schemas } from "@/lib/api";
import { formatDate } from "@/lib/utils";
import { CrudPage, useOptions, type CrudConfig } from "./CrudPage";
import { TempPasswordDialog } from "./TempPasswordDialog";

const num = (v: string) => (v === "" ? undefined : Number(v));

function useUserCreated() {
  const [created, setCreated] = useState<{ username: string; password: string } | null>(null);
  const onCreated = (res: unknown) => {
    const r = res as Schemas["CreateUserResponse"];
    if (r?.temporaryPassword) setCreated({ username: r.username!, password: r.temporaryPassword });
  };
  return { dialog: <TempPasswordDialog value={created} onClose={() => setCreated(null)} />, onCreated };
}

export function StudentsPage() {
  const programs = useOptions<Schemas["ProgramResponse"]>("/api/admin/programs", (p) => ({ value: p.programId!, label: `${p.programId} · ${p.programDesc}` }));
  const created = useUserCreated();
  const config: CrudConfig<Schemas["StudentResponse"]> = {
    title: "Students",
    description: "Creating a student also creates their login with a temporary password.",
    singular: "Student",
    queryKey: "students",
    listPath: "/api/admin/students",
    createPath: "/api/admin/create/student",
    itemPath: (r) => `/api/admin/student/${r.rollNo}`,
    rowKey: (r) => String(r.rollNo),
    rowLabel: (r) => r.name ?? String(r.rollNo),
    defaultSort: "rollNo",
    columns: [
      { key: "rollNo", header: "Roll no.", sortable: true },
      { key: "name", header: "Name", sortable: true, cell: (r) => <span className="font-medium">{r.name}</span> },
      { key: "programId", header: "Program", sortable: true },
      { key: "yearOfRegistration", header: "Batch", sortable: true, hideOnMobile: true },
      { key: "dateOfBirth", header: "Date of birth", hideOnMobile: true, cell: (r) => formatDate(toIsoDate(r.dateOfBirth)) },
    ],
    fields: [
      { name: "name", label: "Full name", required: true },
      { name: "rollNo", label: "Roll number", type: "number", required: true, createOnly: true },
      { name: "programId", label: "Program", type: "select", options: programs, required: true },
      { name: "yearOfRegistration", label: "Batch (year joined)", type: "number", required: true, placeholder: String(new Date().getFullYear()) },
      { name: "dateOfBirth", label: "Date of birth", type: "date", required: true },
    ],
    toForm: (r) => ({ name: r.name ?? "", rollNo: String(r.rollNo ?? ""), programId: r.programId ?? "", yearOfRegistration: r.yearOfRegistration ?? "", dateOfBirth: toIsoDate(r.dateOfBirth) }),
    toBody: (f, mode) => ({ name: f.name, ...(mode === "create" ? { rollNo: num(f.rollNo) } : {}), programId: f.programId, yearOfRegistration: f.yearOfRegistration, dateOfBirth: f.dateOfBirth }),
    onCreated: created.onCreated,
    deleteMessage: (r) => <>This removes {r.name}'s profile and login, along with their enrollments and grades.</>,
  };
  return <><CrudPage config={config} />{created.dialog}</>;
}

/** Older records store dd-MM-yyyy; date inputs need yyyy-MM-dd. */
function toIsoDate(v?: string) {
  if (!v) return "";
  const m = /^(\d{2})-(\d{2})-(\d{4})$/.exec(v);
  return m ? `${m[3]}-${m[2]}-${m[1]}` : v;
}

export function InstructorsPage() {
  const departments = useOptions<Schemas["DepartmentResponse"]>("/api/admin/departments", (d) => ({ value: d.deptId!, label: `${d.deptId} · ${d.deptDesc}` }));
  const created = useUserCreated();
  const config: CrudConfig<Schemas["InstructorResponse"]> = {
    title: "Instructors",
    description: "Creating an instructor also creates their login with a temporary password.",
    singular: "Instructor",
    queryKey: "instructors",
    listPath: "/api/admin/instructors",
    createPath: "/api/admin/create/instructor",
    itemPath: (r) => `/api/admin/instructor/${r.instructorCode}`,
    rowKey: (r) => r.instructorCode!,
    rowLabel: (r) => r.name ?? r.instructorCode!,
    defaultSort: "instructorCode",
    columns: [
      { key: "instructorCode", header: "Code", sortable: true },
      { key: "name", header: "Name", sortable: true, cell: (r) => <span className="font-medium">{r.name}</span> },
      { key: "deptId", header: "Department", sortable: true },
      { key: "email", header: "Email", hideOnMobile: true },
      { key: "phone", header: "Phone", hideOnMobile: true },
    ],
    fields: [
      { name: "name", label: "Full name", required: true },
      { name: "instructorCode", label: "Instructor code", required: true, createOnly: true, placeholder: "INS003" },
      { name: "deptId", label: "Department", type: "select", options: departments, required: true },
      { name: "dateOfBirth", label: "Date of birth", type: "date", required: true },
      { name: "email", label: "Email", type: "email" },
      { name: "phone", label: "Phone" },
    ],
    toForm: (r) => ({ name: r.name ?? "", instructorCode: r.instructorCode ?? "", deptId: r.deptId ?? "", dateOfBirth: toIsoDate(r.dateOfBirth), email: r.email ?? "", phone: r.phone ?? "" }),
    toBody: (f, mode) => ({ ...f, ...(mode === "edit" ? { instructorCode: undefined } : {}) }),
    onCreated: created.onCreated,
    deleteMessage: (r) => <>Removes {r.name}'s profile and login. Sections they teach must be reassigned first.</>,
  };
  return <><CrudPage config={config} />{created.dialog}</>;
}

export function CoursesPage() {
  const departments = useOptions<Schemas["DepartmentResponse"]>("/api/admin/departments", (d) => ({ value: d.deptId!, label: `${d.deptId} · ${d.deptDesc}` }));
  const config: CrudConfig<Schemas["CourseResponse"]> = {
    title: "Courses",
    singular: "Course",
    queryKey: "courses",
    listPath: "/api/admin/courses",
    createPath: "/api/admin/courses",
    itemPath: (r) => `/api/admin/courses/${r.courseId}`,
    rowKey: (r) => r.courseId!,
    rowLabel: (r) => r.courseId!,
    defaultSort: "courseId",
    columns: [
      { key: "courseId", header: "Code", sortable: true, cell: (r) => <span className="font-medium">{r.courseId}</span> },
      { key: "title", header: "Title", sortable: true },
      { key: "deptId", header: "Department", sortable: true, hideOnMobile: true },
      { key: "credits", header: "Credits", sortable: true },
    ],
    fields: [
      { name: "courseId", label: "Course code", required: true, createOnly: true, placeholder: "CSE102" },
      { name: "title", label: "Title", required: true },
      { name: "deptId", label: "Department", type: "select", options: departments, required: true },
      { name: "credits", label: "Credits", type: "number", min: 1, required: true },
    ],
    toForm: (r) => ({ courseId: r.courseId ?? "", title: r.title ?? "", deptId: r.deptId ?? "", credits: String(r.credits ?? "") }),
    toBody: (f) => ({ courseId: f.courseId, title: f.title, deptId: f.deptId, credits: num(f.credits) }),
  };
  return <CrudPage config={config} />;
}

export function DepartmentsPage() {
  const config: CrudConfig<Schemas["DepartmentResponse"]> = {
    title: "Departments",
    singular: "Department",
    queryKey: "departments",
    listPath: "/api/admin/departments",
    createPath: "/api/admin/departments",
    itemPath: (r) => `/api/admin/departments/${r.deptId}`,
    rowKey: (r) => r.deptId!,
    rowLabel: (r) => r.deptId!,
    defaultSort: "deptId",
    columns: [
      { key: "deptId", header: "Code", sortable: true, cell: (r) => <span className="font-medium">{r.deptId}</span> },
      { key: "deptDesc", header: "Name", sortable: true },
    ],
    fields: [
      { name: "deptId", label: "Code", required: true, createOnly: true, placeholder: "ECE" },
      { name: "deptDesc", label: "Name", required: true },
    ],
    toForm: (r) => ({ deptId: r.deptId ?? "", deptDesc: r.deptDesc ?? "" }),
    toBody: (f) => f,
  };
  return <CrudPage config={config} />;
}

export function ProgramsPage() {
  const config: CrudConfig<Schemas["ProgramResponse"]> = {
    title: "Programs",
    singular: "Program",
    queryKey: "programs",
    listPath: "/api/admin/programs",
    createPath: "/api/admin/programs",
    itemPath: (r) => `/api/admin/programs/${r.programId}`,
    rowKey: (r) => r.programId!,
    rowLabel: (r) => r.programId!,
    defaultSort: "programId",
    columns: [
      { key: "programId", header: "Code", sortable: true, cell: (r) => <span className="font-medium">{r.programId}</span> },
      { key: "programDesc", header: "Name", sortable: true },
      { key: "programType", header: "Type", sortable: true },
      { key: "duration", header: "Years", sortable: true },
    ],
    fields: [
      { name: "programId", label: "Code", required: true, createOnly: true, placeholder: "MTECH-CSE" },
      { name: "programDesc", label: "Name", required: true },
      { name: "programType", label: "Type", required: true, hint: "B.Tech students get usernames ending in _bt, others _mt" },
      { name: "duration", label: "Duration (years)", type: "number", min: 1, required: true },
    ],
    toForm: (r) => ({ programId: r.programId ?? "", programDesc: r.programDesc ?? "", programType: r.programType ?? "", duration: String(r.duration ?? "") }),
    toBody: (f) => ({ ...f, duration: num(f.duration) }),
  };
  return <CrudPage config={config} />;
}

export function RoomsPage() {
  const config: CrudConfig<Schemas["RoomResponse"]> = {
    title: "Rooms",
    singular: "Room",
    queryKey: "rooms",
    listPath: "/api/admin/rooms",
    createPath: "/api/admin/rooms",
    itemPath: (r) => `/api/admin/rooms/${r.roomId}`,
    rowKey: (r) => r.roomId!,
    rowLabel: (r) => r.roomId!,
    defaultSort: "roomId",
    columns: [
      { key: "roomId", header: "Room", sortable: true, cell: (r) => <span className="font-medium">{r.roomId}</span> },
      { key: "building", header: "Building", sortable: true },
      { key: "capacity", header: "Capacity", sortable: true },
      { key: "type", header: "Type", sortable: true, hideOnMobile: true },
    ],
    fields: [
      { name: "roomId", label: "Room number", required: true, createOnly: true, placeholder: "C103" },
      { name: "building", label: "Building", required: true },
      { name: "capacity", label: "Capacity", type: "number", min: 1, required: true },
      { name: "type", label: "Type", placeholder: "Classroom, Lab…" },
    ],
    toForm: (r) => ({ roomId: r.roomId ?? "", building: r.building ?? "", capacity: String(r.capacity ?? ""), type: r.type ?? "" }),
    toBody: (f) => ({ ...f, capacity: num(f.capacity) }),
  };
  return <CrudPage config={config} />;
}

