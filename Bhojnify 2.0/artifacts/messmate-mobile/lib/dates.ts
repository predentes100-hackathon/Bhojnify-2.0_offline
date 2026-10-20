export function toLocalISO(date: Date): string {
  const tzOffset = date.getTimezoneOffset() * 60000; 
  return new Date(date.getTime() - tzOffset).toISOString().slice(0, 10);
}

export function todayISO(): string {
  return toLocalISO(new Date());
}

export function addDaysISO(dateStr: string, days: number): string {
  const d = new Date(`${dateStr}T12:00:00`);
  d.setDate(d.getDate() + days);
  return d.toISOString().slice(0, 10);
}

export function daysBetween(dateA: string, dateB: string): number {
  if (!dateA || !dateB) return 0;
  const a = new Date(`${dateA}T12:00:00`).getTime();
  const b = new Date(`${dateB}T12:00:00`).getTime();
  return Math.round((b - a) / 86400000);
}

/** Returns whole calendar days from referenceDate until expiryDate. */
export function daysLeft(expiryDate: string, referenceDate: string = todayISO()): number {
  return daysBetween(referenceDate, expiryDate);
}

export function isValidISODate(dateStr: string): boolean {
  if (!/^\d{4}-\d{2}-\d{2}$/.test(dateStr)) return false;
  const d = new Date(`${dateStr}T12:00:00`);
  return !isNaN(d.getTime());
}
