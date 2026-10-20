-- Manual WhatsApp messaging tables.
-- This migration is additive and is not applied by the offline mobile app.
-- gen_random_uuid() is supplied by pgcrypto on PostgreSQL installations.
CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE IF NOT EXISTS message_templates (
  id            SERIAL PRIMARY KEY,
  type          TEXT NOT NULL UNIQUE CHECK (type IN ('EXPIRY_REMINDER', 'EXPIRY_TODAY', 'ENROLLED', 'RENEWED')),
  template_text TEXT NOT NULL,
  updated_at    TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

INSERT INTO message_templates (type, template_text) VALUES
  ('EXPIRY_REMINDER', 'Hi {name}, your mess plan ends in {days_left} day(s), on {end_date}. Meals left: {meals_left}. Renewal: ₹{amount} for 30 days. Pay via UPI {upi_id} or cash at the counter. Reply RENEW to continue. – {mess_name}'),
  ('EXPIRY_TODAY', 'Hi {name}, your mess plan expires today. Renew before dinner to continue tomorrow. UPI: {upi_id} – {mess_name}'),
  ('ENROLLED', 'Welcome to {mess_name}, {name}! Your plan is active from {start_date} to {end_date}. Paid: ₹{amount}. Timings: Breakfast {breakfast_time}, Lunch {lunch_time}, Dinner {dinner_time}. Show your ID/QR at every meal. Questions: {phone}'),
  ('RENEWED', 'Thanks for renewing, {name}! Payment received: ₹{amount}. New validity: {start_date} to {end_date}. Next reminder will reach you 5 days before expiry. – {mess_name}')
ON CONFLICT (type) DO NOTHING;

CREATE TABLE IF NOT EXISTS whatsapp_message_log (
  id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  student_id      TEXT NOT NULL,
  subscription_id TEXT,
  type            TEXT NOT NULL CHECK (type IN ('EXPIRY_REMINDER', 'EXPIRY_TODAY', 'ENROLLED', 'RENEWED')),
  opened_at       TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_whatsapp_message_log_student ON whatsapp_message_log (student_id);
CREATE INDEX IF NOT EXISTS idx_whatsapp_message_log_opened_at ON whatsapp_message_log (opened_at);
