import { Ionicons } from '@expo/vector-icons';
import { useEffect, useState } from 'react';
import { Alert, Linking, Modal, Pressable, StyleSheet, Text, TextInput, View } from 'react-native';
import { buildWhatsAppLink, MessageType } from '@/lib/whatsapp';
import { useColors } from '@/hooks/useColors';

interface MessagePreviewDialogProps {
  visible: boolean;
  customerName: string;
  phone: string;
  messageType: MessageType;
  initialMessage: string;
  invalidPhone?: boolean;
  onClose: () => void;
  onOpened?: () => void;
}

export function MessagePreviewDialog({ visible, customerName, phone, messageType, initialMessage, invalidPhone, onClose, onOpened }: MessagePreviewDialogProps) {
  const colors = useColors();
  const [message, setMessage] = useState(initialMessage);

  useEffect(() => {
    if (visible) setMessage(initialMessage);
  }, [initialMessage, visible]);

  const openWhatsApp = async () => {
    const link = buildWhatsAppLink(phone, message);
    if (!link) return;
    try {
      await Linking.openURL(link);
      onOpened?.();
      onClose();
    } catch {
      Alert.alert('Unable to open WhatsApp', 'Please check that WhatsApp is installed and try again.');
    }
  };

  return (
    <Modal visible={visible} transparent animationType="fade" onRequestClose={onClose}>
      <View style={styles.backdrop}>
        <View style={[styles.card, { backgroundColor: colors.card, borderColor: colors.border }]}>
          <View style={styles.header}>
            <View style={styles.headerCopy}>
              <Text style={[styles.title, { color: colors.foreground }]}>Message preview</Text>
              <Text style={[styles.subtitle, { color: colors.mutedForeground }]}>{customerName} · {messageType.replace('_', ' ')}</Text>
            </View>
            <Pressable onPress={onClose} style={[styles.close, { backgroundColor: colors.secondary }]}>
              <Ionicons name="close" size={19} color={colors.foreground} />
            </Pressable>
          </View>
          <TextInput multiline value={message} onChangeText={setMessage} textAlignVertical="top" style={[styles.input, { color: colors.foreground, backgroundColor: colors.background, borderColor: colors.border }]} />
          {invalidPhone ? <Text style={[styles.warning, { color: colors.destructive }]}>Add a valid phone number</Text> : null}
          <View style={styles.actions}>
            <Pressable onPress={onClose} style={[styles.button, { backgroundColor: colors.muted }]}><Text style={[styles.buttonText, { color: colors.foreground }]}>Cancel</Text></Pressable>
            <Pressable disabled={invalidPhone} onPress={openWhatsApp} style={[styles.button, { backgroundColor: '#2E7D32', opacity: invalidPhone ? 0.45 : 1 }]}><Ionicons name="logo-whatsapp" size={17} color="#fff" /><Text style={[styles.buttonText, { color: '#fff' }]}>Open in WhatsApp</Text></Pressable>
          </View>
        </View>
      </View>
    </Modal>
  );
}

const styles = StyleSheet.create({
  backdrop: { flex: 1, backgroundColor: 'rgba(0,0,0,0.48)', justifyContent: 'center', padding: 18 },
  card: { borderWidth: 1, borderRadius: 20, padding: 18, gap: 14, maxHeight: '86%' },
  header: { flexDirection: 'row', alignItems: 'flex-start', gap: 12 },
  headerCopy: { flex: 1, gap: 4 },
  title: { fontSize: 19, fontWeight: '700' },
  subtitle: { fontSize: 12, textTransform: 'capitalize' },
  close: { width: 34, height: 34, borderRadius: 17, alignItems: 'center', justifyContent: 'center' },
  input: { minHeight: 180, borderWidth: 1, borderRadius: 13, padding: 13, fontSize: 15, lineHeight: 22 },
  warning: { fontSize: 13, fontWeight: '700' },
  actions: { flexDirection: 'row', gap: 10 },
  button: { flex: 1, minHeight: 48, borderRadius: 13, alignItems: 'center', justifyContent: 'center', flexDirection: 'row', gap: 7, paddingHorizontal: 10 },
  buttonText: { fontSize: 13, fontWeight: '700', textAlign: 'center' },
});
