from pathlib import Path

pay_path = Path("C:/Users/vedan/OneDrive/Desktop/App_offline/Bhojnify-2.0_offline/Bhojnify 2.0/artifacts/messmate-mobile/app/payments.tsx")
content = pay_path.read_text('utf-8')
content = content.replace("Alert.alert(t('paymentRecorded'), t('paymentRecordedMessage')); }; addPayment({ amount: Number(amount), method, note, date: todayISO() }); setAmount(''); setNote(''); Alert.alert(t('paymentRecorded'), t('paymentRecordedMessage')); };", "Alert.alert(t('paymentRecorded'), t('paymentRecordedMessage')); };")
pay_path.write_text(content, 'utf-8')
