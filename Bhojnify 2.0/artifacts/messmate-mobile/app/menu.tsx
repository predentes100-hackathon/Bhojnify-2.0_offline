import { todayISO } from '@/lib/dates';
import { Ionicons } from '@expo/vector-icons';
import { router } from 'expo-router';
import { useMemo, useState } from 'react';
import { Alert, Pressable, StyleSheet, Text, View } from 'react-native';
import { KeyboardAwareScrollViewCompat } from '@/components/KeyboardAwareScrollViewCompat';
import { Badge, FormField, Header, PrimaryButton, SectionHeading } from '@/components/UI';
import { formatDate, Meal, useMess } from '@/context/AppContext';
import { useColors } from '@/hooks/useColors';
import { useTranslation } from '@/hooks/useTranslation';
import { localizedValue } from '@/lib/i18n';

export default function MenuScreen() {
  const colors = useColors();
  const { menus, addMenu } = useMess();
  const { language, t } = useTranslation();

  const todayStr = useMemo(() => todayISO(), []);
  const tomorrowStr = useMemo(() => {
    const d = new Date();
    d.setDate(d.getDate() + 1);
    return d.toISOString().slice(0, 10);
  }, []);

  const [selectedDate, setSelectedDate] = useState(todayStr);
  const [meal, setMeal] = useState<Meal>('Lunch');
  const [dish, setDish] = useState('');
  const [note, setNote] = useState('');

  // Group menus by date
  const groupedMenus = useMemo(() => {
    const map = new Map<string, typeof menus>();
    menus.forEach((item) => {
      const dateKey = item.day;
      if (!map.has(dateKey)) {
        map.set(dateKey, []);
      }
      map.get(dateKey)!.push(item);
    });
    // Sort dates in ascending order
    return Array.from(map.entries()).sort(([a], [b]) => a.localeCompare(b));
  }, [menus]);

  const add = () => {
    if (!dish.trim()) {
      Alert.alert(t('addDish'), t('dishNameMessage'));
      return;
    }
    if (!selectedDate.trim()) {
      Alert.alert(t('date'), t('enterDay'));
      return;
    }
    addMenu({
      day: selectedDate.trim(),
      meal,
      dish: dish.trim(),
      note: note.trim() || t('freshlyPrepared'),
    });
    setDish('');
    setNote('');
  };

  const getDateLabel = (dateStr: string) => {
    if (dateStr === todayStr) return `${t('today')} (${formatDate(dateStr, language)})`;
    if (dateStr === tomorrowStr) return `${t('tomorrow')} (${formatDate(dateStr, language)})`;
    if (/^\d{4}-\d{2}-\d{2}$/.test(dateStr)) return formatDate(dateStr, language);
    return localizedValue(language, dateStr);
  };

  const mealSlots: Meal[] = ['Breakfast', 'Lunch', 'Dinner'];

  return (
    <KeyboardAwareScrollViewCompat
      style={[styles.screen, { backgroundColor: colors.background }]}
      contentContainerStyle={styles.content}
      bottomOffset={24}
      keyboardShouldPersistTaps="handled"
    >
      <Header
        eyebrow={t('ownerTools')}
        title={t('menuPlanner')}
        subtitle={t('shapeMeals')}
        onPress={() => router.push('/(tabs)/profile')}
      />

      <View style={[styles.menuHero, { backgroundColor: colors.primary }]}>
        <Ionicons name="calendar-outline" size={26} color={colors.accent} />
        <View style={styles.heroCopy}>
          <Text style={[styles.heroTitle, { color: colors.primaryForeground }]}>{t('nextUp')}</Text>
          <Text style={[styles.heroDetail, { color: colors.primaryForeground }]}>{t('planAhead')}</Text>
        </View>
      </View>

      <SectionHeading title={t('publishedMenu')} />

      {groupedMenus.length > 0 ? (
        groupedMenus.map(([dateKey, items]) => (
          <View key={dateKey} style={styles.dateGroupContainer}>
            <View style={styles.dateHeaderRow}>
              <Ionicons name="calendar" size={15} color={colors.primary} />
              <Text style={[styles.dateGroupTitle, { color: colors.foreground }]}>
                {getDateLabel(dateKey)}
              </Text>
              <Badge label={`${items.length} ${t('menu').toLowerCase()}`} tone="gray" />
            </View>

            <View style={[styles.menuList, { backgroundColor: colors.card, borderColor: colors.border }]}>
              {items.map((item, index) => (
                <View
                  key={item.id}
                  style={[
                    styles.menuRow,
                    index < items.length - 1 && { borderBottomWidth: 1, borderBottomColor: colors.border },
                  ]}
                >
                  <View style={[styles.mealPill, { backgroundColor: colors.secondary }]}>
                    <Text style={[styles.mealPillText, { color: colors.primary }]}>
                      {localizedValue(language, item.meal).slice(0, 4)}
                    </Text>
                  </View>
                  <View style={styles.menuCopy}>
                    <View style={styles.menuTop}>
                      <Text style={[styles.mealText, { color: colors.mutedForeground }]}>
                        {localizedValue(language, item.meal)}
                      </Text>
                      <Badge label={t('published')} />
                    </View>
                    <Text style={[styles.dishText, { color: colors.foreground }]}>{item.dish}</Text>
                    {item.note ? (
                      <Text style={[styles.noteText, { color: colors.mutedForeground }]}>{item.note}</Text>
                    ) : null}
                  </View>
                </View>
              ))}
            </View>
          </View>
        ))
      ) : (
        <View style={[styles.emptyCard, { backgroundColor: colors.card, borderColor: colors.border }]}>
          <Ionicons name="restaurant-outline" size={28} color={colors.mutedForeground} />
          <Text style={[styles.emptyText, { color: colors.mutedForeground }]}>{t('noMenuItems')}</Text>
        </View>
      )}

      <SectionHeading title={t('addToPlan')} />
      <View style={[styles.formCard, { backgroundColor: colors.card, borderColor: colors.border }]}>
        <View style={styles.fieldSection}>
          <Text style={[styles.fieldLabel, { color: colors.foreground }]}>{t('date')}</Text>
          <View style={styles.quickDateRow}>
            <Pressable
              onPress={() => setSelectedDate(todayStr)}
              style={[
                styles.quickDateChip,
                {
                  borderColor: selectedDate === todayStr ? colors.primary : colors.border,
                  backgroundColor: selectedDate === todayStr ? colors.secondary : colors.background,
                },
              ]}
            >
              <Ionicons
                name="today-outline"
                size={14}
                color={selectedDate === todayStr ? colors.primary : colors.mutedForeground}
              />
              <Text
                style={[
                  styles.quickDateText,
                  { color: selectedDate === todayStr ? colors.primary : colors.foreground },
                ]}
              >
                {t('today')}
              </Text>
            </Pressable>

            <Pressable
              onPress={() => setSelectedDate(tomorrowStr)}
              style={[
                styles.quickDateChip,
                {
                  borderColor: selectedDate === tomorrowStr ? colors.primary : colors.border,
                  backgroundColor: selectedDate === tomorrowStr ? colors.secondary : colors.background,
                },
              ]}
            >
              <Ionicons
                name="arrow-forward-outline"
                size={14}
                color={selectedDate === tomorrowStr ? colors.primary : colors.mutedForeground}
              />
              <Text
                style={[
                  styles.quickDateText,
                  { color: selectedDate === tomorrowStr ? colors.primary : colors.foreground },
                ]}
              >
                {t('tomorrow')}
              </Text>
            </Pressable>
          </View>
          <FormField
            label=""
            value={selectedDate}
            onChangeText={setSelectedDate}
            placeholder="YYYY-MM-DD"
          />
        </View>

        <View style={styles.fieldSection}>
          <Text style={[styles.fieldLabel, { color: colors.foreground }]}>{t('mealSlot')}</Text>
          <View style={styles.mealSlotsRow}>
            {mealSlots.map((slot) => (
              <Pressable
                key={slot}
                onPress={() => setMeal(slot)}
                style={[
                  styles.mealSlotChip,
                  {
                    borderColor: meal === slot ? colors.primary : colors.border,
                    backgroundColor: meal === slot ? colors.secondary : colors.background,
                  },
                ]}
              >
                <Text
                  style={[
                    styles.mealSlotText,
                    { color: meal === slot ? colors.primary : colors.foreground },
                  ]}
                >
                  {localizedValue(language, slot)}
                </Text>
              </Pressable>
            ))}
          </View>
        </View>

        <FormField
          label={t('dish')}
          value={dish}
          onChangeText={setDish}
          placeholder={t('enterDishName')}
        />
        <FormField
          label={t('notes')}
          value={note}
          onChangeText={setNote}
          placeholder={t('enterMenuNotes')}
        />
        <PrimaryButton label={t('publishMenuItem')} icon="check" onPress={add} />
      </View>
    </KeyboardAwareScrollViewCompat>
  );
}

const styles = StyleSheet.create({
  screen: { flex: 1 },
  content: { paddingHorizontal: 20, paddingTop: 54, paddingBottom: 40, gap: 18 },
  menuHero: { padding: 18, borderRadius: 20, flexDirection: 'row', gap: 12, alignItems: 'center' },
  heroCopy: { flex: 1, gap: 3 },
  heroTitle: { fontSize: 17, fontWeight: '700' },
  heroDetail: { fontSize: 12, opacity: 0.76 },
  dateGroupContainer: { gap: 8 },
  dateHeaderRow: { flexDirection: 'row', alignItems: 'center', gap: 6, paddingHorizontal: 2 },
  dateGroupTitle: { fontSize: 14, fontWeight: '700', flex: 1 },
  menuList: { borderWidth: 1, borderRadius: 19, paddingHorizontal: 14 },
  menuRow: { flexDirection: 'row', gap: 11, paddingVertical: 14 },
  mealPill: { width: 44, height: 38, borderRadius: 12, alignItems: 'center', justifyContent: 'center' },
  mealPillText: { fontSize: 11, fontWeight: '700' },
  menuCopy: { flex: 1, gap: 3 },
  menuTop: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center' },
  mealText: { fontSize: 11, fontWeight: '600' },
  dishText: { fontSize: 15, fontWeight: '700' },
  noteText: { fontSize: 11 },
  emptyCard: { borderWidth: 1, borderRadius: 19, padding: 24, alignItems: 'center', gap: 8 },
  emptyText: { fontSize: 13 },
  formCard: { borderWidth: 1, borderRadius: 20, padding: 16, gap: 15 },
  fieldSection: { gap: 7 },
  fieldLabel: { fontSize: 13, fontWeight: '700' },
  quickDateRow: { flexDirection: 'row', gap: 8 },
  quickDateChip: {
    flex: 1,
    minHeight: 38,
    borderWidth: 1,
    borderRadius: 11,
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'center',
    gap: 6,
    paddingHorizontal: 8,
  },
  quickDateText: { fontSize: 12, fontWeight: '700' },
  mealSlotsRow: { flexDirection: 'row', gap: 8 },
  mealSlotChip: {
    flex: 1,
    minHeight: 40,
    borderWidth: 1,
    borderRadius: 12,
    alignItems: 'center',
    justifyContent: 'center',
  },
  mealSlotText: { fontSize: 12, fontWeight: '700' },
});