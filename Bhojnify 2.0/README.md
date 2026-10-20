# Bhojnify

Bhojnify is an offline-first Expo app for owner-managed mess operations.

## Manual WhatsApp messaging

The customer WhatsApp workflow is intentionally manual and one student at a time:

1. The owner taps **WhatsApp** for an eligible customer (or opens the expiry queue).
2. Bhojnify renders the selected editable template and shows a preview.
3. The owner taps **Open in WhatsApp**.
4. WhatsApp opens with a prefilled message; the owner must review and press **Send** inside WhatsApp.

There is no WhatsApp API, cron job, background sender, bulk sender, delivery confirmation, or automatic sending. A log entry means **Opened in WhatsApp**, not Sent or Delivered. Re-opening a student is always allowed, including when they were already opened today.

The four templates (expiry reminder, expiry today, enrolled, and renewed) and message settings are stored locally with the current AsyncStorage-backed mobile workspace. They can be edited from Admin → WhatsApp Settings and reset to the exact defaults. The expiry queue is deliberately one-at-a-time and provides Skip / Next.

The PostgreSQL/Drizzle migration at `lib/db/migrations/0001_manual_whatsapp.sql` and matching schema definitions are additive future-backend deliverables. They are not applied by the offline app.

## Run and verify

```text
pnpm --filter @workspace/messmate-mobile run dev
pnpm --filter @workspace/messmate-mobile run typecheck
pnpm run typecheck
```

Before applying the SQL migration to a real database, confirm the target connection and review the additive migration. The mobile build does not require a database connection.
