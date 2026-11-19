import { StyleSheet, Text, View } from 'react-native';
import { colors } from '@/constants/theme';

export function StudyBuddyLogoMark() {
  return (
    <View style={styles.logo} accessible accessibilityLabel="StudyBuddy logo">
      <Text style={styles.book}>⌁</Text>
    </View>
  );
}

export function UserAvatar() {
  return (
    <View style={styles.avatar} accessible accessibilityLabel="User avatar">
      <View style={styles.head} />
      <View style={styles.body} />
    </View>
  );
}

const styles = StyleSheet.create({
  logo: {
    width: 34,
    height: 34,
    borderRadius: 9,
    alignItems: 'center',
    justifyContent: 'center',
    backgroundColor: colors.accent,
  },
  book: {
    color: colors.pageWhite,
    fontSize: 28,
    lineHeight: 28,
    fontWeight: '700',
  },
  avatar: {
    width: 34,
    height: 34,
    borderRadius: 17,
    backgroundColor: colors.tagBackground,
    alignItems: 'center',
    justifyContent: 'center',
  },
  head: {
    width: 7,
    height: 7,
    borderRadius: 4,
    backgroundColor: colors.accent,
    marginTop: -3,
  },
  body: {
    width: 20,
    height: 10,
    borderTopLeftRadius: 12,
    borderTopRightRadius: 12,
    backgroundColor: colors.accent,
    marginTop: 3,
  },
});
