import type { Term } from "@/lib/utils";

export function TermPicker({ value, onChange }: { value: Term; onChange: (t: Term) => void }) {
  const thisYear = new Date().getFullYear();
  const years = [thisYear - 1, thisYear, thisYear + 1];
  return (
    <div className="flex gap-2">
      <select
        className="input w-36"
        aria-label="Semester"
        value={value.semester}
        onChange={(e) => onChange({ ...value, semester: e.target.value as Term["semester"] })}
      >
        <option value="MONSOON">Monsoon</option>
        <option value="WINTER">Winter</option>
      </select>
      <select className="input w-28" aria-label="Year" value={value.year} onChange={(e) => onChange({ ...value, year: Number(e.target.value) })}>
        {years.map((y) => (
          <option key={y} value={y}>{y}</option>
        ))}
      </select>
    </div>
  );
}
