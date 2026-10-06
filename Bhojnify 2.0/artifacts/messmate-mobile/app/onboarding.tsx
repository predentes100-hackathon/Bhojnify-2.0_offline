import { Ionicons } from '@expo/vector-icons';
import { router } from 'expo-router';
import { useState } from 'react';
import { Alert, Pressable, StyleSheet, Text, View } from 'react-native';
import { KeyboardAwareScrollViewCompat } from '@/components/KeyboardAwareScrollViewCompat';
import { FormField, PrimaryButton } from '@/components/UI';
import { OwnerProfile, useMess } from '@/context/AppContext';
import { Redirect } from 'expo-router';
import { useColors } from '@/hooks/useColors';
import { useTranslation } from '@/hooks/useTranslation';

export default function OnboardingScreen() {
  const colors = useColors();
  const { completeOwnerSetup, onboardingComplete, hydrated } = useMess();
  const { language, setLanguage, t } = useTranslation();
  if (hydrated && onboardingComplete) return <Redirect href="/(tabs)" />;

  const [name, setName] = useState('');
  const [messName, setMessName] = useState('');
  const [phone, setPhone] = useState('');
  const [location, setLocation] = useState('');
  const [email, setEmail] = useState('');

  const finishSetup = () => {
    const profile: OwnerProfile = {
      name: name.trim(),
      messName: messName.trim(),
      phone: phone.trim(),
      location: location.trim(),
      email: email.trim(),
    };
    if (!profile.name || !profile.messName || !profile.phone || !profile.location) {
      Alert.alert(t('completeOwnerProfile'), t('requiredProfileMessage'));
      return;
    }
    completeOwnerSetup(profile);
    router.replace('/(tabs)');
  };

  return (
    <KeyboardAwareScrollViewCompat style={[styles.screen, { backgroundColor: colors.background }]} contentContainerStyle={styles.content} bottomOffset={24} keyboardShouldPersistTaps="handled">
      <View style={[styles.brandMark, { backgroundColor: colors.primary }]}><Ionicons name="restaurant-outline" size={28} color={colors.accent} /></View>
      <View style={styles.languageHeader}><Text style={[styles.languageLabel, { color: colors.mutedForeground }]}>{t('chooseLanguage')}</Text><View style={[styles.languagePicker, { backgroundColor: colors.card, borderColor: colors.border }]}><Pressable onPress={() => setLanguage('en')} style={[styles.languageOption, language === 'en' && { backgroundColor: colors.primary }]}><Text style={[styles.languageOptionText, { color: language === 'en' ? colors.primaryForeground : colors.foreground }]}>{t('english')}</Text></Pressable><Pressable onPress={() => setLanguage('mr')} style={[styles.languageOption, language === 'mr' && { backgroundColor: colors.primary }]}><Text style={[styles.languageOptionText, { color: language === 'mr' ? colors.primaryForeground : colors.foreground }]}>{t('marathi')}</Text></Pressable></View></View>
      <Text style={[styles.eyebrow, { color: colors.primary }]}>{t('ownerSetup')}</Text>
      <Text style={[styles.title, { color: colors.foreground }]}>{t('runMess')}</Text>
      <Text style={[styles.subtitle, { color: colors.mutedForeground }]}>{t('setupWorkspace')}</Text>
      <View style={[styles.formCard, { backgroundColor: colors.card, borderColor: colors.border }]}>
        <Text style={[styles.formTitle, { color: colors.foreground }]}>{t('tellAboutYou')}</Text>
        <Text style={[styles.formDetail, { color: colors.mutedForeground }]}>{t('requiredFields')}</Text>
        <FormField label={t('ownerName')} value={name} onChangeText={setName} placeholder={t('enterOwnerName')} />
        <FormField label={t('messName')} value={messName} onChangeText={setMessName} placeholder={t('enterMessName')} />
        <FormField label={t('phoneNumber')} value={phone} onChangeText={setPhone} placeholder={t('enterPhoneNumber')} keyboardType="numeric" />
        <FormField label={t('cityLocation')} value={location} onChangeText={setLocation} placeholder={t('enterCityLocation')} />
        <FormField label={t('emailOptional')} value={email} onChangeText={setEmail} placeholder={t('enterEmail')} />
        <PrimaryButton label={t('createWorkspace')} icon="arrow-right" onPress={finishSetup} />
      </View>
      <View style={styles.localNote}><Ionicons name="lock-closed-outline" size={16} color={colors.primary} /><Text style={[styles.localNoteText, { color: colors.mutedForeground }]}>{t('localOnlySetup')}</Text></View>
    </KeyboardAwareScrollViewCompat>
  );
}

const styles = StyleSheet.create({
  screen: { flex: 1 },
  content: { paddingHorizontal: 20, paddingTop: 46, paddingBottom: 40, gap: 14 },
  languageHeader: { alignItems: 'flex-end', gap: 7, marginBottom: 2 },
  languageLabel: { fontSize: 12, fontWeight: '600' },
  languagePicker: { flexDirection: 'row', borderWidth: 1, borderRadius: 12, padding: 3 },
  languageOption: { paddingHorizontal: 12, paddingVertical: 7, borderRadius: 9 },
  languageOptionText: { fontSize: 12, fontWeight: '700' },
  brandMark: { width: 58, height: 58, borderRadius: 20, alignItems: 'center', justifyContent: 'center', marginBottom: 7 },
  eyebrow: { fontSize: 11, letterSpacing: 1.4, fontWeight: '700' },
  title: { fontSize: 32, lineHeight: 37, fontWeight: '700', letterSpacing: -0.8, maxWidth: 350 },
  subtitle: { fontSize: 14, lineHeight: 21, marginBottom: 7 },
  formCard: { borderWidth: 1, borderRadius: 23, padding: 17, gap: 15 },
  formTitle: { fontSize: 19, fontWeight: '700' },
  formDetail: { fontSize: 12, lineHeight: 17, marginTop: -8 },
  localNote: { flexDirection: 'row', alignItems: 'center', justifyContent: 'center', gap: 7, paddingVertical: 4 },
  localNoteText: { fontSize: 12, fontWeight: '600' },
});