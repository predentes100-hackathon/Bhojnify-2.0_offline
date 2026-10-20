import { createInsertSchema } from 'drizzle-zod';
import { pgTable, serial, text, timestamp, uuid } from 'drizzle-orm/pg-core';
import { z } from 'zod';

export const messageTemplatesTable = pgTable('message_templates', {
  id: serial('id').primaryKey(),
  type: text('type').notNull().unique(),
  templateText: text('template_text').notNull(),
  updatedAt: timestamp('updated_at', { withTimezone: true }).notNull().defaultNow(),
});

export const whatsappMessageLogTable = pgTable('whatsapp_message_log', {
  id: uuid('id').defaultRandom().primaryKey(),
  studentId: text('student_id').notNull(),
  subscriptionId: text('subscription_id'),
  type: text('type').notNull(),
  openedAt: timestamp('opened_at', { withTimezone: true }).notNull().defaultNow(),
});

export const insertMessageTemplateSchema = createInsertSchema(messageTemplatesTable).omit({ id: true });
export const insertWhatsappMessageLogSchema = createInsertSchema(whatsappMessageLogTable).omit({ id: true });

export type InsertMessageTemplate = z.infer<typeof insertMessageTemplateSchema>;
export type MessageTemplate = typeof messageTemplatesTable.$inferSelect;
export type InsertWhatsappMessageLog = z.infer<typeof insertWhatsappMessageLogSchema>;
export type WhatsappMessageLog = typeof whatsappMessageLogTable.$inferSelect;
