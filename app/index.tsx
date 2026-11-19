import { useFocusEffect, useRouter } from 'expo-router';
import { useCallback, useState } from 'react';
import {
  ActivityIndicator,
  Pressable,
  ScrollView,
  StyleSheet,
  Text,
  View,
} from 'react-native';
import { AppShell } from '@/components/AppShell';
import { colors, spacing } from '@/constants/theme';
import { TodoListItem } from '@/components/TodoListItem';
import { loadTodos } from '@/storage/todoStore';
import type { TodoItem } from '@/types/todo';

export default function HomeScreen() {
  const router = useRouter();
  const [todos, setTodos] = useState<TodoItem[]>([]);
  const [isLoading, setIsLoading] = useState(true);

  const refreshTodos = useCallback(async () => {
    setIsLoading(true);
    setTodos(await loadTodos());
    setIsLoading(false);
  }, []);

  useFocusEffect(
    useCallback(() => {
      void refreshTodos();
    }, [refreshTodos]),
  );

  return (
    <AppShell>
        <ScrollView
          style={styles.content}
          contentContainerStyle={styles.contentContainer}
          showsVerticalScrollIndicator={false}
        >
          <View style={styles.todoHeader}>
            <Text style={styles.sectionHeading}>My Todos</Text>
            <Pressable
              onPress={() => router.push('/create-todo')}
              style={styles.addButton}
              accessibilityRole="button"
              accessibilityLabel="Add Todo"
            >
              <Text style={styles.addButtonText}>+ Add Todo</Text>
            </Pressable>
          </View>

          {isLoading ? (
            <ActivityIndicator color={colors.accent} style={styles.loading} />
          ) : todos.length === 0 ? (
            <Text style={styles.mutedText}>No todos yet. Tap "+ Add Todo" to create one.</Text>
          ) : (
            todos.map((todo) => <TodoListItem item={todo} key={todo.id} />)
          )}

          <View style={styles.timerSection}>
            <Text style={styles.sectionHeading}>Active Study Timers</Text>
            <Text style={styles.noTimers}>No Active Timers</Text>
          </View>
        </ScrollView>
    </AppShell>
  );
}

const styles = StyleSheet.create({
  content: {
    flex: 1,
    backgroundColor: colors.pageWhite,
  },
  contentContainer: {
    paddingHorizontal: spacing.pageHorizontal,
    paddingVertical: spacing.pageVertical,
    gap: 12,
  },
  todoHeader: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
  },
  sectionHeading: {
    color: colors.textDark,
    fontSize: 20,
    fontWeight: '700',
  },
  addButton: {
    borderRadius: 8,
    backgroundColor: colors.accent,
    paddingHorizontal: 14,
    paddingVertical: 10,
  },
  addButtonText: {
    color: colors.pageWhite,
    fontSize: 14,
    fontWeight: '600',
  },
  mutedText: {
    color: colors.mutedText,
    fontSize: 16,
  },
  loading: {
    paddingVertical: 14,
  },
  timerSection: {
    alignItems: 'center',
    paddingTop: 26,
    gap: 30,
  },
  noTimers: {
    color: colors.mutedText,
    fontSize: 16,
  },
});
