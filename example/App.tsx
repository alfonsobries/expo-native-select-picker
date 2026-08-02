import { pickOption } from 'expo-native-select-picker';
import { useState } from 'react';
import { Button, SafeAreaView, ScrollView, Text, View } from 'react-native';

const COUNTRIES = [
  { value: 'ar', label: '🇦🇷 Argentina' },
  { value: 'br', label: '🇧🇷 Brasil' },
  { value: 'ca', label: '🇨🇦 Canadá' },
  { value: 'cl', label: '🇨🇱 Chile' },
  { value: 'co', label: '🇨🇴 Colombia' },
  { value: 'es', label: '🇪🇸 España' },
  { value: 'us', label: '🇺🇸 Estados Unidos' },
  { value: 'fr', label: '🇫🇷 Francia' },
  { value: 'it', label: '🇮🇹 Italia' },
  { value: 'jp', label: '🇯🇵 Japón' },
  { value: 'mx', label: '🇲🇽 México' },
  { value: 'pe', label: '🇵🇪 Perú' },
  { value: 'pt', label: '🇵🇹 Portugal' },
  { value: 'gb', label: '🇬🇧 Reino Unido' },
  { value: 'uy', label: '🇺🇾 Uruguay' },
];

const PRIORITIES = [
  { value: 'low', label: 'Low' },
  { value: 'medium', label: 'Medium' },
  { value: 'high', label: 'High' },
];

export default function App() {
  const [country, setCountry] = useState('mx');
  const [priority, setPriority] = useState<string | null>(null);

  return (
    <SafeAreaView style={styles.container}>
      <ScrollView style={styles.container}>
        <Text style={styles.header}>expo-native-select-picker</Text>

        <Group name="Searchable list, preselected">
          <Button
            title="Pick a country"
            onPress={async () => {
              const value = await pickOption({
                title: 'Country',
                options: COUNTRIES,
                selectedValue: country,
                searchPlaceholder: 'Search country',
                grouped: true,
              });
              if (value !== null) {
                setCountry(value);
              }
            }}
          />
          <Text style={styles.value}>
            {COUNTRIES.find((option) => option.value === country)?.label}
          </Text>
        </Group>

        <Group name="Short list, no search">
          <Button
            title="Pick a priority"
            onPress={async () => {
              const value = await pickOption({
                title: 'Priority',
                options: PRIORITIES,
                selectedValue: priority ?? undefined,
                searchable: false,
              });
              if (value !== null) {
                setPriority(value);
              }
            }}
          />
          <Text style={styles.value}>
            {PRIORITIES.find((option) => option.value === priority)?.label ?? '—'}
          </Text>
        </Group>
      </ScrollView>
    </SafeAreaView>
  );
}

function Group(props: { name: string; children: React.ReactNode }) {
  return (
    <View style={styles.group}>
      <Text style={styles.groupHeader}>{props.name}</Text>
      {props.children}
    </View>
  );
}

const styles = {
  header: { fontSize: 30, margin: 20 },
  groupHeader: { fontSize: 20, marginBottom: 20 },
  group: { margin: 20, backgroundColor: '#fff', borderRadius: 10, padding: 20 },
  container: { flex: 1, backgroundColor: '#eee' },
  value: { fontSize: 18, marginTop: 12, textAlign: 'center' as const },
};
