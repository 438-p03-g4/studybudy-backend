import { useEffect, useState } from 'react';
import {
  ActivityIndicator,
  ScrollView,
  StyleSheet,
  Text,
  TextInput,
  View,
} from 'react-native';
import { fetchStudyGroups } from '@/api/studyGroups';
import { AppShell } from '@/components/AppShell';
import { colors, spacing } from '@/constants/theme';
import type { StudyGroup } from '@/types/studyGroup';

export default function FindStudyGroupsScreen() {
  const [query, setQuery] = useState('');
  const [groups, setGroups] = useState<StudyGroup[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  useEffect(() => {
    const controller = new AbortController();
    const timeout = setTimeout(async () => {
      setIsLoading(true);
      setErrorMessage(null);

      try {
        setGroups(await fetchStudyGroups(query, controller.signal));
      } catch (error) {
        if (!controller.signal.aborted) {
          setGroups([]);
          setErrorMessage(error instanceof Error ? error.message : 'Unable to load study groups.');
        }
      } finally {
        if (!controller.signal.aborted) setIsLoading(false);
      }
    }, 250);

    return () => {
      controller.abort();
      clearTimeout(timeout);
    };
  }, [query]);

  return (
    <AppShell>
      <View style={styles.pageContent}>
        <Text style={styles.sectionHeading}>Find Study Groups</Text>
        <TextInput
          value={query}
          onChangeText={setQuery}
          placeholder="Search by group name or course"
          placeholderTextColor={colors.mutedText}
          style={styles.searchInput}
          autoCapitalize="none"
          autoCorrect={false}
          accessibilityLabel="Search study groups"
        />

        {isLoading ? (
          <ActivityIndicator color={colors.accent} style={styles.loading} />
        ) : errorMessage ? (
          <Text style={styles.errorMessage}>{errorMessage}</Text>
        ) : groups.length === 0 ? (
          <Text style={styles.emptyState}>No Groups Found</Text>
        ) : (
          <ScrollView
            style={styles.groupList}
            contentContainerStyle={styles.groupListContent}
            showsVerticalScrollIndicator={false}
          >
            {groups.map((group) => (
              <View key={group.id} style={styles.groupCard}>
                <Text style={styles.groupName}>{group.name}</Text>
                <Text style={styles.groupDetail}>{group.course}</Text>
              </View>
            ))}
          </ScrollView>
        )}
      </View>
    </AppShell>
  );
}

const styles = StyleSheet.create({
  pageContent: {
    flex: 1,
    backgroundColor: colors.pageWhite,
    paddingHorizontal: spacing.pageHorizontal,
    paddingVertical: spacing.pageVertical,
    gap: 12,
  },
  sectionHeading: {
    color: colors.textDark,
    fontSize: 20,
    fontWeight: '700',
  },
  searchInput: {
    width: '100%',
    borderWidth: 1,
    borderColor: colors.mutedText,
    borderRadius: 6,
    color: colors.textDark,
    fontSize: 16,
    paddingHorizontal: 12,
    paddingVertical: 12,
  },
  loading: {
    paddingVertical: 18,
  },
  groupList: {
    flex: 1,
  },
  groupListContent: {
    gap: 12,
    paddingBottom: 20,
  },
  groupCard: {
    borderRadius: 8,
    backgroundColor: colors.todoBackground,
    padding: 16,
    gap: 6,
  },
  groupName: {
    color: colors.textDark,
    fontSize: 17,
    fontWeight: '700',
  },
  groupDetail: {
    color: colors.mutedText,
    fontSize: 14,
  },
  emptyState: {
    color: colors.mutedText,
    fontSize: 16,
    paddingTop: 8,
  },
  errorMessage: {
    color: '#B42318',
    fontSize: 15,
  },
});
