import { todayISO, daysBetween, addDaysISO } from '@/lib/dates';
import { DEFAULT_TEMPLATES, MessageTemplates, MessageType } from '@/lib/whatsapp';
import AsyncStorage from '@react-native-async-storage/async-storage';
import React, { createContext, ReactNode, useContext, useEffect, useMemo, useState, useRef } from 'react';
import { Language } from '@/lib/i18n';

export type Role = 'owner';
export type Meal = 'Breakfast' | 'Lunch' | 'Dinner';

export interface AttendanceRecord {
  id: string;
  date: string;
  meal: Meal;
  time: string;
  verified: boolean;
  method: string;
}

export interface LeaveRequest {
  id: string;
  customerId?: string;
  from: string;
  to?: string;
  reason: string;
  status: 'Pending' | 'Approved' | 'Declined';
}

export type CustomerPaymentStatus = 'Paid' | 'Unpaid';

export interface Customer {
  id: string;
  name: string;
  plan: string;
  joiningDate: string;
  expiryDate: string;
  paymentStatus: CustomerPaymentStatus;
  phone: string;
  imageUri?: string;
  subscriptionId?: string;
  subscriptionStatus?: 'active' | 'paused' | 'cancelled';
  amount?: number;
  mealsLeft?: number;
  lastRenewedAt?: string;
}

export const MESS_PLANS = [
  'Monthly Two-Meal',
  'Monthly Lunch Only',
  'Monthly Dinner Only',
  '15-Day Flexi Plan',
  'Daily / Per Meal',
  'Custom Plan',
] as const;

export interface Payment {
  id: string;
  customerId?: string;
  date: string;
  amount: number;
  method: string;
  note: string;
}

export interface InventoryItem {
  id: string;
  name: string;
  quantity: number;
  unit: string;
  minimum: number;
  category: string;
}

export interface MenuItem {
  id: string;
  day: string;
  meal: Meal;
  dish: string;
  note: string;
}

export interface StaffMember {
  id: string;
  name: string;
  role: string;
  phone: string;
  salary: number;
}

export interface Expense {
  id: string;
  date: string;
  category: string;
  amount: number;
  note: string;
}

export interface Feedback {
  id: string;
  dish: string;
  rating: number;
  note: string;
  date: string;
}

export interface Reminder {
  id: string;
  title: string;
  detail: string;
  dueDate: string;
  createdAt: string;
}

export interface MessSettings {
  upiId: string;
  breakfastTime: string;
  lunchTime: string;
  dinnerTime: string;
  messPhone: string;
}

export interface WhatsAppMessageLog {
  id: string;
  studentId: string;
  subscriptionId?: string;
  type: MessageType;
  openedAt: string;
}

export interface OwnerProfile {
  name: string;
  messName: string;
  phone: string;
  location: string;
  email: string;
}

export interface OwnerPolicies {
  rules: string;
  privacy: string;
}

interface MessState {
  hydrated: boolean;
  role: Role;
  language: Language;
  onboardingComplete: boolean;
  credits: number;
  expiresOn: string;
  profile: OwnerProfile;
  policies: OwnerPolicies;
  messSettings: MessSettings;
  messageTemplates: MessageTemplates;
  whatsappMessageLogs: WhatsAppMessageLog[];
  attendance: AttendanceRecord[];
  customers: Customer[];
  leaves: LeaveRequest[];
  payments: Payment[];
  inventory: InventoryItem[];
  menus: MenuItem[];
  staff: StaffMember[];
  expenses: Expense[];
  feedback: Feedback[];
  reminders: Reminder[];
}

interface MessActions {
  setLanguage: (language: Language) => void;
  completeOwnerSetup: (profile: OwnerProfile) => void;
  updateOwnerProfile: (updates: Partial<OwnerProfile>) => void;
  updatePolicies: (policies: OwnerPolicies) => void;
  updateMessSettings: (settings: MessSettings) => void;
  updateMessageTemplate: (type: MessageType, value: string) => void;
  resetMessageTemplates: () => void;
  logWhatsAppOpen: (studentId: string, subscriptionId: string | undefined, type: MessageType) => void;
  markAttendance: (meal: Meal, verified: boolean, method: string) => void;
  addCustomer: (customer: Omit<Customer, 'id'>) => string;
  markCustomerPaid: (id: string, amount: number) => void;
  addLeave: (customerId: string, from: string, to: string | undefined, reason: string) => void;
  updateLeaveStatus: (id: string, status: LeaveRequest['status']) => void;
  updateLeave: (id: string, updates: Partial<Omit<LeaveRequest, 'id'>>) => void;
  deleteLeave: (id: string) => void;
  completeLeave: (leaveId: string, returnDate: string) => void;
  updateCustomer: (id: string, updates: Partial<Omit<Customer, 'id'>>) => void;
  deleteCustomer: (id: string) => void;
  addFeedback: (dish: string, rating: number, note: string) => void;
  addInventory: (item: Omit<InventoryItem, 'id'>) => void;
  removeInventory: (id: string) => void;
  addMenu: (item: Omit<MenuItem, 'id'>) => void;
  addStaff: (item: Omit<StaffMember, 'id'>) => void;
  addExpense: (item: Omit<Expense, 'id'>) => void;
  addPayment: (item: Omit<Payment, 'id'>) => void;
  addReminder: (title: string, daysFromNow: number, detail?: string) => void;
  resolveReminder: (id: string) => void;
}

const STORAGE_KEY = '@messmate/state-v1';
const today = todayISO();
const id = () => `${Date.now()}-${Math.random().toString(36).slice(2, 8)}`;

const initialState: MessState = {
  role: 'owner',
  hydrated: false,
  language: 'en',
  onboardingComplete: false,
  credits: 18,
  expiresOn: '2026-09-18',
  profile: { name: '', messName: '', phone: '', location: '', email: '' },
  policies: { rules: '', privacy: '' },
  messSettings: { upiId: '', breakfastTime: '', lunchTime: '', dinnerTime: '', messPhone: '' },
  messageTemplates: { ...DEFAULT_TEMPLATES },
  whatsappMessageLogs: [],
  attendance: [
    { id: 'a1', date: today, meal: 'Breakfast', time: '08:04 AM', verified: true, method: 'Biometric + GPS' },
    { id: 'a2', date: today, meal: 'Lunch', time: '01:12 PM', verified: true, method: 'Biometric + GPS' },
    { id: 'a3', date: '2026-09-01', meal: 'Dinner', time: '08:06 PM', verified: true, method: 'Biometric + GPS' },
  ],
  customers: [
    {
      id: 'c1',
      name: 'Rohan Patil',
      plan: 'Monthly Two-Meal',
      joiningDate: '2026-09-01',
      expiryDate: '2026-10-01',
      paymentStatus: 'Paid' as const,
      phone: '+91 98221 44550',
    },
    {
      id: 'c2',
      name: 'Priya Sharma',
      plan: 'Monthly Lunch Only',
      joiningDate: '2026-08-13',
      expiryDate: '2026-09-12',
      paymentStatus: 'Paid' as const,
      phone: '+91 97654 32100',
    },
    {
      id: 'c3',
      name: 'Amit Deshmukh',
      plan: 'Monthly Dinner Only',
      joiningDate: '2026-08-11',
      expiryDate: today,
      paymentStatus: 'Paid' as const,
      phone: '+91 98900 11223',
    },
    {
      id: 'c4',
      name: 'Sneha Kulkarni',
      plan: '15-Day Flexi Plan',
      joiningDate: '2026-08-20',
      expiryDate: '2026-09-05',
      paymentStatus: 'Unpaid',
      phone: '+91 94230 99887',
    },
  ],
  leaves: [
    { id: 'l1', customerId: 'c1', from: '2026-09-08', to: '2026-09-10', reason: 'Family function', status: 'Pending' },
    { id: 'l2', customerId: 'c2', from: '2026-09-09', reason: 'Going home for festival', status: 'Pending' },
  ],
  payments: [
    { id: 'p1', date: '2026-08-18', amount: 4200, method: 'UPI', note: 'September meal plan' },
    { id: 'p2', date: '2026-07-18', amount: 4200, method: 'UPI', note: 'August meal plan' },
  ],
  inventory: [
    { id: 'i1', name: 'Basmati rice', quantity: 32, unit: 'kg', minimum: 24, category: 'Grains' },
    { id: 'i2', name: 'Toor dal', quantity: 18, unit: 'kg', minimum: 20, category: 'Pulses' },
    { id: 'i3', name: 'Cooking oil', quantity: 12, unit: 'L', minimum: 10, category: 'Essentials' },
    { id: 'i4', name: 'Onions', quantity: 24, unit: 'kg', minimum: 18, category: 'Produce' },
  ],
  menus: [
    { id: 'm1', day: today, meal: 'Breakfast', dish: 'Masala dosa', note: 'Coconut chutney · Sambar' },
    { id: 'm2', day: today, meal: 'Lunch', dish: 'Rajma rice', note: 'Cucumber salad · Buttermilk' },
    { id: 'm3', day: today, meal: 'Dinner', dish: 'Paneer bhurji', note: 'Phulka · Seasonal vegetables' },
    { id: 'm4', day: '2026-09-11', meal: 'Breakfast', dish: 'Poha & chai', note: 'Peanuts · Fresh fruit' },
  ],
  staff: [
    { id: 's1', name: 'Meena Joshi', role: 'Head Cook', phone: '+91 98204 16320', salary: 28000 },
    { id: 's2', name: 'Rakesh Kumar', role: 'Server', phone: '+91 99102 76211', salary: 19000 },
  ],
  expenses: [
    { id: 'e1', date: '2026-09-01', category: 'Inventory', amount: 12400, note: 'Weekly produce purchase' },
    { id: 'e2', date: '2026-09-01', category: 'Gas', amount: 1800, note: 'Cylinder refill' },
    { id: 'e3', date: '2026-08-30', category: 'Maintenance', amount: 950, note: 'Exhaust fan service' },
  ],
  feedback: [
    { id: 'f1', dish: 'Rajma rice', rating: 4, note: 'Comforting and nicely spiced.', date: '2026-09-01' },
  ],
  reminders: [
    { id: 'r1', title: 'Check cooking oil supply', detail: 'Review reorder quantity before the next delivery.', dueDate: today, createdAt: today },
  ],
};

const MessContext = createContext<(MessState & MessActions) | null>(null);

export function MessProvider({ children }: { children: ReactNode }) {
  const [state, setState] = useState<MessState>(initialState);
  const [hydrated, setHydrated] = useState(false);

  useEffect(() => {
    AsyncStorage.getItem(STORAGE_KEY)
      .then((stored) => {
        if (stored) {
          const parsed = JSON.parse(stored) as Partial<MessState>;
          setState({
            ...initialState,
            ...parsed,
            language: parsed.language === 'mr' ? 'mr' : 'en',
            profile: { ...initialState.profile, ...(parsed.profile ?? {}) },
            policies: { ...initialState.policies, ...(parsed.policies ?? {}) },
            messSettings: { ...initialState.messSettings, ...(parsed.messSettings ?? {}) },
            messageTemplates: { ...initialState.messageTemplates, ...(parsed.messageTemplates ?? {}) },
            whatsappMessageLogs: Array.isArray(parsed.whatsappMessageLogs) ? parsed.whatsappMessageLogs : [],
            onboardingComplete: parsed.onboardingComplete === true,
            role: 'owner',
            hydrated: false,
          });
        }
      })
      .catch(() => undefined)
      .finally(() => setHydrated(true));
  }, []);

const saveTimeout = useRef<ReturnType<typeof setTimeout> | null>(null);

  useEffect(() => {
    if (!hydrated) return;
    if (saveTimeout.current) clearTimeout(saveTimeout.current);
    saveTimeout.current = setTimeout(() => {
      try {
        const jsonValue = JSON.stringify(state);
        AsyncStorage.setItem(STORAGE_KEY, jsonValue).catch(e => {
          console.error("Failed to persist state: ", e);
        });
      } catch (error) {
        console.error("Failed to serialize state: ", error);
      }
    }, 500);
    return () => {
      if (saveTimeout.current) clearTimeout(saveTimeout.current);
    };
  }, [state, hydrated]);

  const actions = useMemo<MessActions>(() => ({
    setLanguage: (language) => setState((prev) => ({ ...prev, language })),
    completeOwnerSetup: (profile) => setState((prev) => ({ ...prev, profile, onboardingComplete: true, role: 'owner' })),
    updateOwnerProfile: (updates) => setState((prev) => ({ ...prev, profile: { ...prev.profile, ...updates } })),
    updatePolicies: (policies) => setState((prev) => ({ ...prev, policies })),
    updateMessSettings: (settings) => setState((prev) => ({ ...prev, messSettings: settings })),
    updateMessageTemplate: (type, value) => setState((prev) => ({
      ...prev,
      messageTemplates: { ...prev.messageTemplates, [type]: value },
    })),
    resetMessageTemplates: () => setState((prev) => ({ ...prev, messageTemplates: { ...DEFAULT_TEMPLATES } })),
    logWhatsAppOpen: (studentId, subscriptionId, type) => setState((prev) => ({
      ...prev,
      whatsappMessageLogs: [
        { id: `${Date.now()}-${Math.random().toString(36).slice(2, 8)}`, studentId, subscriptionId, type, openedAt: new Date().toISOString() },
        ...prev.whatsappMessageLogs,
      ],
    })),
    markAttendance: (meal, verified, method) => setState((prev) => ({
      ...prev,
      credits: Math.max(0, prev.credits - 1),
      attendance: [
        { id: id(), date: todayISO(), meal, time: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }), verified, method },
        ...prev.attendance,
      ],
    })),
    addCustomer: (customer) => {
      const newId = id();
      setState((prev) => ({ ...prev, customers: [{ ...customer, id: newId }, ...prev.customers] }));
      return newId;
    },
    markCustomerPaid: (customerId, amount) => setState((prev) => {
      const customers = prev.customers.map((c) => c.id === customerId ? { ...c, paymentStatus: 'Paid' as const } : c);
      const payment: Payment = { id: id(), date: todayISO(), amount, method: 'Cash', note: 'Marked as paid', customerId };
      return { ...prev, customers, payments: [payment, ...prev.payments] };
    }),
    addLeave: (customerId, from, to, reason) => setState((prev) => ({ ...prev, leaves: [{ id: id(), customerId, from, to, reason, status: 'Pending' }, ...prev.leaves] })),
    updateLeaveStatus: (requestId, status) => setState((prev) => ({ ...prev, leaves: prev.leaves.map((leave) => leave.id === requestId ? { ...leave, status } : leave) })),
    updateLeave: (requestId, updates) => setState((prev) => ({ ...prev, leaves: prev.leaves.map((leave) => leave.id === requestId ? { ...leave, ...updates } : leave) })),
    deleteLeave: (requestId) => setState((prev) => ({ ...prev, leaves: prev.leaves.filter((leave) => leave.id !== requestId) })),
    updateCustomer: (customerId, updates) => setState((prev) => ({ ...prev, customers: prev.customers.map((customer) => customer.id === customerId ? { ...customer, ...updates } : customer) })),
    deleteCustomer: (customerId) => setState((prev) => ({ ...prev, customers: prev.customers.filter((customer) => customer.id !== customerId) })),
    completeLeave: (requestId, returnDate) => setState((prev) => {
      const leave = prev.leaves.find((l) => l.id === requestId);
      if (!leave || !leave.customerId) return prev;
      
      
      const daysMissed = Math.max(0, daysBetween(leave.from, returnDate));
      return {
        ...prev,
        leaves: prev.leaves.map((l) => l.id === requestId ? { ...l, to: returnDate, status: 'Approved' } : l),
        customers: prev.customers.map((customer) => {
          if (customer.id !== leave.customerId || daysMissed <= 0) return customer;
          return { ...customer, expiryDate: addDaysISO(customer.expiryDate, daysMissed) };
        }),
      };
    }),
    addFeedback: (dish, rating, note) => setState((prev) => ({ ...prev, feedback: [{ id: id(), dish, rating, note, date: todayISO() }, ...prev.feedback] })),
    addInventory: (item) => setState((prev) => ({ ...prev, inventory: [{ ...item, id: id() }, ...prev.inventory] })),
    removeInventory: (itemId) => setState((prev) => ({ ...prev, inventory: prev.inventory.filter((item) => item.id !== itemId) })),
    addMenu: (item) => setState((prev) => ({ ...prev, menus: [{ ...item, id: id() }, ...prev.menus] })),
    addStaff: (item) => setState((prev) => ({ ...prev, staff: [{ ...item, id: id() }, ...prev.staff] })),
    addExpense: (item) => setState((prev) => ({ ...prev, expenses: [{ ...item, id: id() }, ...prev.expenses] })),
    addPayment: (item) => setState((prev) => ({ ...prev, payments: [{ ...item, id: id() }, ...prev.payments] })),
    addReminder: (title, daysFromNow, detail = 'Inventory follow-up') => setState((prev) => {
      const dueDate = new Date();
      dueDate.setDate(dueDate.getDate() + Math.max(0, Math.round(daysFromNow)));
      return {
        ...prev,
        reminders: [
          {
            id: id(),
            title,
            detail,
            dueDate: dueDate.toISOString().slice(0, 10),
            createdAt: todayISO(),
          },
          ...prev.reminders,
        ],
      };
    }),
    resolveReminder: (reminderId) => setState((prev) => ({ ...prev, reminders: prev.reminders.filter((reminder) => reminder.id !== reminderId) })),
  }), []);

  return <MessContext.Provider value={{ ...state, ...actions, hydrated }}>{children}</MessContext.Provider>;
}

export function useMess() {
  const context = useContext(MessContext);
  if (!context) throw new Error('useMess must be used inside MessProvider');
  return context;
}

export function formatDate(value: string, language: Language = 'en') {
  return new Date(`${value}T12:00:00`).toLocaleDateString(language === 'mr' ? 'mr-IN' : undefined, { day: 'numeric', month: 'short' });
}


export interface ExpiryInfo {
  daysRemaining: number;
  status: 'expired' | 'today' | 'soon' | 'active';
  isExpired: boolean;
  isExpiringSoon: boolean;
  isExpiringToday: boolean;
}

export function getExpiryInfo(expiryDate: string): ExpiryInfo {
  if (!expiryDate) {
    return { daysRemaining: 0, status: 'active', isExpired: false, isExpiringSoon: false, isExpiringToday: false };
  }
  const todayStr = todayISO();
  const diffMs = new Date(`${expiryDate}T12:00:00`).getTime() - new Date(`${todayStr}T12:00:00`).getTime();
  const daysRemaining = Math.round(diffMs / 86400000);

  if (daysRemaining < 0) {
    return { daysRemaining, status: 'expired', isExpired: true, isExpiringSoon: false, isExpiringToday: false };
  }
  if (daysRemaining === 0) {
    return { daysRemaining: 0, status: 'today', isExpired: false, isExpiringSoon: true, isExpiringToday: true };
  }
  if (daysRemaining <= 5) {
    return { daysRemaining, status: 'soon', isExpired: false, isExpiringSoon: true, isExpiringToday: false };
  }
  return { daysRemaining, status: 'active', isExpired: false, isExpiringSoon: false, isExpiringToday: false };
}