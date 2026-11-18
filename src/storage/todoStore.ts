import AsyncStorage from '@react-native-async-storage/async-storage';
import type { TodoItem } from '@/types/todo';

const TODO_STORAGE_KEY = 'studybuddy.todos';

export async function loadTodos(): Promise<TodoItem[]> {
  const stored = await AsyncStorage.getItem(TODO_STORAGE_KEY);
  if (!stored) return [];

  try {
    const parsed: unknown = JSON.parse(stored);
    if (!Array.isArray(parsed)) return [];

    return parsed.filter(isTodoItem);
  } catch {
    return [];
  }
}

export async function saveTodos(todos: TodoItem[]): Promise<void> {
  await AsyncStorage.setItem(TODO_STORAGE_KEY, JSON.stringify(todos));
}

function isTodoItem(value: unknown): value is TodoItem {
  if (!value || typeof value !== 'object') return false;
  const item = value as Record<string, unknown>;
  return (
    typeof item.id === 'string' &&
    typeof item.title === 'string' &&
    typeof item.tag === 'string'
  );
}

export function createTodo(title: string, tag: string): TodoItem {
  return {
    id: `${Date.now()}-${Math.random().toString(36).slice(2)}`,
    title,
    tag,
  };
}
