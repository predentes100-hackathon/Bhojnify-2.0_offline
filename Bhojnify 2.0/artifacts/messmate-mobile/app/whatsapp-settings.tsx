import { router } from 'expo-router';
import { useMemo, useState } from 'react';
import { Alert, Pressable, StyleSheet, Text, TextInput, View } from 'react-native';
import { Header, PrimaryButton, Screen, SectionHeading } from '@/components/UI';
import { useMess } from '@/context/AppContext';
import { useColors } from '@/hooks/useColors';
import { useTranslation } from '@/hooks/useTranslation';
import { DEFAULT_TEMPLATES, MessageType, renderTemplate } from '@/lib/whatsapp';

const TEMPLATE_TYPES: MessageType[] = ['EXPIRY_REMINDER', 'EXPIRY_TODAY', 'ENROLLED', 'RENEWED'];

const TEMPLATE_LABELS: Record<MessageType, string> = {
  EXPIRY_REMINDER: 'templateExpiryReminder',
  EXPIRY_TODAY: 'templateExpiryToday',
  ENROLLED: 'templateEnrolled',
  RENEWED: 'templateRenewed',
};

const SAMPLE_DATA = {
  name: 'Aarav',
  days_left: '5',
  end_date: '23 Oct 2026',
  start_date: '24 Sep 2026',
  plan: 'Monthly Two-Meal',
  amount: '₹4,500',
  meals_left: '18',
  upi_id: 'aaravmess@upi',
  mess_name: 'Annapurna Mess',
  phone: '+91 98765 43210',
  breakfast_time: '8:00 AM',
  lunch_time: '1:00 PM',
  dinner_time: '8:00 PM',
};

export default function WhatsAppSettingsScreen() {
  const colors = useColors();
  const { t } = useTranslation();
  const { profile, messSettings, messageTemplates, updateOwnerProfile, updateMessSettings, updateMessageTemplate } = useMess();
  const [messName, setMessName] = useState(profile.messName);
  const [ownerPhone, setOwnerPhone] = useState(profile.phone);
  const [upiId, setUpiId] = useState(messSettings.upiId);
  const [messPhone, setMessPhone] = useState(messSettings.messPhone);
  const [breakfastTime, setBreakfastTime] = useState(messSettings.breakfastTime);
  const [lunchTime, setLunchTime] = useState(messSettings.lunchTime);
  const [dinnerTime, setDinnerTime] = useState(messSettings.dinnerTime);
  const [templates, setTemplates] = useState({ ...messageTemplates });

  const sampleContext = useMemo(() => ({
    ...SAMPLE_DATA,
    mess_name: messName || SAMPLE_DATA.mess_name,
    upi_id: upiId || SAMPLE_DATA.upi_id,
    phone: messPhone || ownerPhone || SAMPLE_DATA.phone,
    breakfast_time: breakfastTime || SAMPLE_DATA.breakfast_time,
    lunch_time: lunchTime || SAMPLE_DATA.lunch_time,
    dinner_time: dinnerTime || SAMPLE_DATA.dinner_time,
  }), [messName, upiId, messPhone, ownerPhone, breakfastTime, lunchTime, dinnerTime]);

  const save = () => {
    updateOwnerProfile({ messName, phone: ownerPhone });
    updateMessSettings({ upiId, messPhone, breakfastTime, lunchTime, dinnerTime });
    TEMPLATE_TYPES.forEach((type) => updateMessageTemplate(type, templates[type]));
    Alert.alert(t('success'), t('whatsappSettingsSaved'));
  };

  return (
    <Screen>
      <Header eyebrow={t('ownerTools')} title={t('whatsappSettings')} subtitle={t('whatsappSettingsSubtitle')} onPress={() => router.back()} />

      <SectionHeading title={t('contactInfo')} />
      <View style={[styles.card, { backgroundColor: colors.card, borderColor: colors.border }]}>
        <Field label={t('messName')} value={messName} onChangeText={setMessName} placeholder={t('enterMessName')} colors={colors} />
        <Field label={t('ownerPhone') || t('phoneNumber')} value={ownerPhone} onChangeText={setOwnerPhone} placeholder={t('enterPhoneNumber')} keyboardType="phone-pad" colors={colors} />
        <Field label={t('messPhone')} value={messPhone} onChangeText={setMessPhone} placeholder={t('enterPhoneNumber')} keyboardType="phone-pad" colors={colors} />
        <Field label={t('upiId')} value={upiId} onChangeText={setUpiId} placeholder="yourmess@upi" colors={colors} />
      </View>

      <SectionHeading title={t('mealSlot')} />
      <View style={[styles.card, { backgroundColor: colors.card, borderColor: colors.border }]}>
        <Field label={t('breakfastTime')} value={breakfastTime} onChangeText={setBreakfastTime} placeholder="8:00 AM" colors={colors} />
        <Field label={t('lunchTime')} value={lunchTime} onChangeText={setLunchTime} placeholder="1:00 PM" colors={colors} />
        <Field label={t('dinnerTime')} value={dinnerTime} onChangeText={setDinnerTime} placeholder="8:00 PM" colors={colors} />
      </View>

      <SectionHeading title={t('whatsappCustomer')} />
      {TEMPLATE_TYPES.map((type) => (
        <View key={type} style={[styles.templateCard, { backgroundColor: colors.card, borderColor: colors.border }]}>
          <View style={styles.templateHeader}>
            <Text style={[styles.templateTitle, { color: colors.foreground }]}>{t(TEMPLATE_LABELS[type])}</Text>
            <Pressable onPress={() => setTemplates((current) => ({ ...current, [type]: DEFAULT_TEMPLATES[type] }))}>
              <Text style={[styles.resetText, { color: colors.primary }]}>{t('resetToDefault')}</Text>
            </Pressable>
          </View>
          <TextInput
            multiline
            textAlignVertical="top"
            value={templates[type]}
            onChangeText={(value) => setTemplates((current) => ({ ...current, [type]: value }))}
            style={[styles.templateInput, { color: colors.foreground, borderColor: colors.input, backgroundColor: colors.background }]}
          />
          <Text style={[styles.previewLabel, { color: colors.mutedForeground }]}>{t('livePreview')}</Text>
          <Text style={[styles.preview, { color: colors.foreground, backgroundColor: colors.secondary }]}>{renderTemplate(templates[type], sampleContext)}</Text>
        </View>
      ))}

      <PrimaryButton label={t('saveChanges')} icon="check" onPress={save} />
    </Screen>
  );
}

function Field({ label, value, onChangeText, placeholder, keyboardType, colors }: { label: string; value: string; onChangeText: (value: string) => void; placeholder: string; keyboardType?: 'default' | 'phone-pad'; colors: any }) {
  return (
    <View style={styles.field}>
      <Text style={[styles.label, { color: colors.foreground }]}>{label}</Text>
      <TextInput value={value} onChangeText={onChangeText} placeholder={placeholder} placeholderTextColor={colors.mutedForeground} keyboardType={keyboardType} style={[styles.input, { color: colors.foreground, borderColor: colors.input, backgroundColor: colors.background }]} />
    </View>
  );
}

const styles = StyleSheet.create({
  card: { borderWidth: 1, borderRadius: 18, padding: 15, gap: 14 },
  field: { gap: 7 },
  label: { fontSize: 13, fontWeight: '700' },
  input: { minHeight: 46, borderWidth: 1, borderRadius: 12, paddingHorizontal: 12, fontSize: 15 },
  templateCard: { borderWidth: 1, borderRadius: 18, padding: 15, gap: 10 },
  templateHeader: { flexDirection: 'row', alignItems: 'center', justifyContent: 'space-between', gap: 10 },
  templateTitle: { fontSize: 15, fontWeight: '700', flex: 1 },
  resetText: { fontSize: 12, fontWeight: '700' },
  templateInput: { minHeight: 135, borderWidth: 1, borderRadius: 12, padding: 12, fontSize: 14, lineHeight: 20 },
  previewLabel: { fontSize: 11, fontWeight: '700', textTransform: 'uppercase' },
  preview: { borderRadius: 11, padding: 11, fontSize: 13, lineHeight: 19 },
});
