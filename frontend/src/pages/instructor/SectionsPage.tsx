import { Link } from "react-router-dom";
import { useTerm } from "@/lib/term";
import { formatDate } from "@/lib/utils";
import type { Schemas } from "@/lib/api";
import { Badge, Card, PageHeader } from "@/components/ui/misc";
import { Table, type Column } from "@/components/ui/table";
import { TermPicker } from "@/components/TermPicker";
import { useInstructorCode, useMySections } from "./queries";

type Row = Schemas["InstructorSectionResponse"];

export function InstructorSectionsPage() {
  const code = useInstructorCode();
  const { term, setTerm } = useTerm();
  const sections = useMySections(code, term);

  const columns: Column<Row>[] = [
    {
      key: "sectionId", header: "Section",
      cell: (r) => (
        <Link to={`/instructor/sections/${encodeURIComponent(r.sectionId!)}`} className="font-medium text-primary hover:underline">
          {r.courseId}<span className="block text-xs font-normal text-fg-muted">{r.sectionId}</span>
        </Link>
      ),
    },
    { key: "enrolled", header: "Enrolled", cell: (r) => <span className="tabular-nums">{r.enrollmentCount} / {r.capacity}</span> },
    { key: "deadline", header: "Registration closes", hideOnMobile: true, cell: (r) => formatDate(r.registrationDeadline) },
    { key: "active", header: "Status", cell: (r) => <Badge tone={r.active ? "success" : "neutral"}>{r.active ? "Active" : "Inactive"}</Badge> },
    { key: "open", header: "", className: "text-right", cell: (r) => <Link to={`/instructor/sections/${encodeURIComponent(r.sectionId!)}`} className="text-sm font-medium text-primary hover:underline">Gradebook →</Link> },
  ];

  return (
    <>
      <PageHeader title="My sections" actions={<TermPicker value={term} onChange={setTerm} />} />
      <Card>
        <Table columns={columns} rows={sections.data} rowKey={(r) => r.sectionId!} loading={sections.isLoading || !code} empty={{ title: "No sections this term" }} />
      </Card>
    </>
  );
}
