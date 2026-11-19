import { useRouter } from 'expo-router';
import { useState } from 'react';
import {
  KeyboardAvoidingView,
  Platform,
  Pressable,
  SafeAreaView,
  StyleSheet,
  Text,
  TextInput,
  View,
} from 'react-native';
import { colors } from '@/constants/theme';
import { createTodo, loadTodos, saveTodos } from '@/storage/todoStore';

export default function CreateTodoScreen() {
  const router = useRouter();
  const [title, setTitle] = useState('');
  const [tag, setTag] = useState('');

  const handleCreate = async () => {
    const trimmedTitle = title.trim();
    if (!trimmedTitle) return;

    const existing = await loadTodos();
    await saveTodos([...existing, createTodo(trimmedTitle, tag.trim())]);
    setTitle('');
    setTag('');
    router.back();
  };

  return (
    <SafeAreaView style={styles.safeArea}>
      <KeyboardAvoidingView
        style={styles.page}
        behavior={Platform.OS === 'ios' ? 'padding' : undefined}
      >
        <View style={styles.header}>
          <Pressable
            onPress={() => router.back()}
            style={styles.backButton}
            accessibilityRole="button"
            accessibilityLabel="Back"
          >
            <Text style={styles.backArrow}>←</Text>
            <Text style={styles.headerText}>Back</Text>
          </Pressable>
          <Text style={styles.headerTitle}>Create New Todo</Text>
        </View>

        <View style={styles.form}>
          <Text style={styles.heading}>My Todos</Text>
          <TextInput
            value={title}
            onChangeText={setTitle}
            placeholder="Title (required)"
            placeholderTextColor={colors.mutedText}
            style={styles.input}
            returnKeyType="next"
            accessibilityLabel="Title (required)"
          />
          <TextInput
            value={tag}
            onChangeText={setTag}
            placeholder="Tag (optional)"
            placeholderTextColor={colors.mutedText}
            style={styles.input}
            returnKeyType="done"
            accessibilityLabel="Tag (optional)"
          />
          <Pressable
            onPress={() => void handleCreate()}
            disabled={title.trim().length === 0}
            style={[styles.createButton, title.trim().length === 0 && styles.disabledButton]}
            accessibilityRole="button"
            accessibilityLabel="Create Todo"
          >
            <Text style={styles.createButtonText}>Create Todo</Text>
          </Pressable>
        </View>
      </KeyboardAvoidingView>
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  safeArea: {
    flex: 1,
    backgroundColor: colors.pageWhite,
  },
  page: {
    flex: 1,
    backgroundColor: colors.pageWhite,
  },
  header: {
    height: 56,
    backgroundColor: colors.accent,
    paddingHorizontal: 16,
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
  },
  backButton: {
    borderRadius: 8,
    paddingHorizontal: 8,
    paddingVertical: 6,
    flexDirection: 'row',
    alignItems: 'center',
    gap: 8,
  },
  backArrow: {
    color: colors.pageWhite,
    fontSize: 20,
    fontWeight: '700',
  },
  headerText: {
    color: colors.pageWhite,
    fontSize: 15,
    fontWeight: '600',
  },
  headerTitle: {
    color: colors.pageWhite,
    fontSize: 18,
    fontWeight: '700',
  },
  form: {
    paddingHorizontal: 20,
    paddingVertical: 24,
    gap: 12,
  },
  heading: {
    color: colors.textDark,
    fontSize: 20,
    fontWeight: '700',
  },
  input: {
    width: '100%',
    borderWidth: 1,
    borderColor: colors.mutedText,
    borderRadius: 4,
    color: colors.textDark,
    fontSize: 16,
    paddingHorizontal: 12,
    paddingVertical: 12,
  },
  createButton: {
    alignSelf: 'flex-start',
    borderRadius: 8,
    backgroundColor: colors.accent,
    paddingHorizontal: 16,
    paddingVertical: 12,
  },
  disabledButton: {
    opacity: 0.5,
  },
  createButtonText: {
    color: colors.pageWhite,
    fontSize: 15,
    fontWeight: '600',
  },
});
