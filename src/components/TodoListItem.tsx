import { StyleSheet, Text, View } from 'react-native';
import { colors } from '@/constants/theme';
import type { TodoItem } from '@/types/todo';

export function TodoListItem({ item }: { item: TodoItem }) {
  return (
    <View style={styles.container} accessible accessibilityLabel={`Todo ${item.title}`}>
      <Text style={styles.title}>{item.title}</Text>
      {item.tag.length > 0 && <Text style={styles.tag}>{item.tag}</Text>}
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    width: '100%',
    borderRadius: 12,
    backgroundColor: colors.todoBackground,
    padding: 16,
    gap: 8,
  },
  title: {
    color: colors.textDark,
    fontSize: 16,
    fontWeight: '500',
  },
  tag: {
    alignSelf: 'flex-start',
    color: colors.textDark,
    fontSize: 12,
    borderRadius: 8,
    backgroundColor: colors.tagBackground,
    paddingHorizontal: 10,
    paddingVertical: 4,
  },
});
