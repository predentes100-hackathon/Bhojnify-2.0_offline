import { daysLeft as calculateDaysLeft } from '@/lib/dates';

export type MessageType = 'EXPIRY_REMINDER' | 'EXPIRY_TODAY' | 'ENROLLED' | 'RENEWED';

export interface TemplateData {
  name: string;
  days_left: string;
  end_date: string;
  start_date: string;
  plan: string;
  amount: string;
  meals_left: string;
  upi_id: string;
  mess_name: string;
  phone: string;
  breakfast_time: string;
  lunch_time: string;
  dinner_time: string;
}

export type TemplateDataInput = Partial<TemplateData>;

export interface MessageTemplates {
  EXPIRY_REMINDER: string;
  EXPIRY_TODAY: string;
  ENROLLED: string;
  RENEWED: string;
}

export const DEFAULT_TEMPLATES: MessageTemplates = {
  EXPIRY_REMINDER:
    'Hi {name}, your mess plan ends in {days_left} day(s), on {end_date}. Meals left: {meals_left}. Renewal: ₹{amount} for 30 days. Pay via UPI {upi_id} or cash at the counter. Reply RENEW to continue. – {mess_name}',
  EXPIRY_TODAY:
    'Hi {name}, your mess plan expires today. Renew before dinner to continue tomorrow. UPI: {upi_id} – {mess_name}',
  ENROLLED:
    'Welcome to {mess_name}, {name}! Your plan is active from {start_date} to {end_date}. Paid: ₹{amount}. Timings: Breakfast {breakfast_time}, Lunch {lunch_time}, Dinner {dinner_time}. Show your ID/QR at every meal. Questions: {phone}',
  RENEWED:
    'Thanks for renewing, {name}! Payment received: ₹{amount}. New validity: {start_date} to {end_date}. Next reminder will reach you 5 days before expiry. – {mess_name}',
};

const SUPPORTED_PLACEHOLDERS = new Set<keyof TemplateData>([
  'name',
  'days_left',
  'end_date',
  'start_date',
  'plan',
  'amount',
  'meals_left',
  'upi_id',
  'mess_name',
  'phone',
  'breakfast_time',
  'lunch_time',
  'dinner_time',
]);

export function normalizePhoneNumber(phone: string): string | null {
  if (!phone) return null;

  let digits = phone.replace(/\D/g, '');
  if (digits.startsWith('0')) digits = digits.slice(1);
  if (digits.length === 10) digits = `91${digits}`;

  if (!/^91\d{10}$/.test(digits)) return null;
  return digits;
}

export function renderTemplate(template: string, data: TemplateDataInput): string {
  return template.replace(/\{([a-z_]+)\}/g, (placeholder, name: string) => {
    if (!SUPPORTED_PLACEHOLDERS.has(name as keyof TemplateData)) return placeholder;
    const value = data[name as keyof TemplateData];
    return value === undefined || value === null || String(value).trim() === '' ? '—' : String(value);
  });
}

const WHATSAPP_URL_LIMIT = 2000;
const WHATSAPP_PREFIX = 'https://wa.me/';

export function buildWhatsAppLink(phone: string, message: string): string | null {
  const normalizedPhone = normalizePhoneNumber(phone);
  if (!normalizedPhone) return null;

  const prefix = `${WHATSAPP_PREFIX}${normalizedPhone}?text=`;
  const fullMessage = String(message ?? '');
  const fullUrl = `${prefix}${encodeURIComponent(fullMessage)}`;
  if (fullUrl.length <= WHATSAPP_URL_LIMIT) return fullUrl;

  let low = 0;
  let high = fullMessage.length;
  let best = '';
  while (low <= high) {
    const middle = Math.floor((low + high) / 2);
    const candidate = `${fullMessage.slice(0, middle)}…`;
    const candidateUrl = `${prefix}${encodeURIComponent(candidate)}`;
    if (candidateUrl.length <= WHATSAPP_URL_LIMIT) {
      best = candidate;
      low = middle + 1;
    } else {
      high = middle - 1;
    }
  }

  return `${prefix}${encodeURIComponent(best)}`;
}

export function formatWhatsAppDate(isoDate: string): string {
  if (!isoDate) return '—';
  const date = new Date(`${isoDate}T12:00:00`);
  if (Number.isNaN(date.getTime())) return '—';
  return date.toLocaleDateString('en-IN', {
    day: '2-digit',
    month: 'short',
    year: 'numeric',
    timeZone: 'Asia/Kolkata',
  });
}

export function getDaysLeft(expiryDate: string, referenceDate?: string): number {
  return calculateDaysLeft(expiryDate, referenceDate);
}

export function getDefaultMessageType(daysLeft: number): MessageType | null {
  if (daysLeft === 0) return 'EXPIRY_TODAY';
  if (daysLeft >= 1 && daysLeft <= 5) return 'EXPIRY_REMINDER';
  return null;
}
