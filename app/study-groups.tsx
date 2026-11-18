import { ScrollView, StyleSheet, Text } from 'react-native';
import { AppShell } from '@/components/AppShell';
import { colors, spacing } from '@/constants/theme';

export default function StudyGroupsScreen() {
  return (
    <AppShell>
      <ScrollView
        style={styles.content}
        contentContainerStyle={styles.contentContainer}
        showsVerticalScrollIndicator={false}
      >
        <Text style={styles.sectionHeading}>My Study Groups</Text>
        <Text style={styles.emptyState}>No Active Study Groups</Text>
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
  sectionHeading: {
    color: colors.textDark,
    fontSize: 20,
    fontWeight: '700',
  },
  emptyState: {
    color: colors.mutedText,
    fontSize: 16,
    paddingTop: 8,
  },
});
