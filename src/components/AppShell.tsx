import { useRouter } from 'expo-router';
import type { ReactNode } from 'react';
import { useState } from 'react';
import { Modal, Pressable, SafeAreaView, StyleSheet, Text, View } from 'react-native';
import { StudyBuddyLogoMark, UserAvatar } from '@/components/StudyBuddyLogoMark';
import { colors, spacing } from '@/constants/theme';

const MENU_ITEMS = ['Home', 'My Study Groups', 'Find Study Groups', 'Create Study Timer'];

type AppShellProps = {
  children: ReactNode;
};

export function AppShell({ children }: AppShellProps) {
  const router = useRouter();
  const [menuVisible, setMenuVisible] = useState(false);

  const handleMenuItemPress = (item: string) => {
    setMenuVisible(false);

    if (item === 'Home') {
      router.push('/');
    } else if (item === 'My Study Groups') {
      router.push('/study-groups');
    } else if (item === 'Find Study Groups') {
      router.push('/find-study-groups');
    }
  };

  return (
    <SafeAreaView style={styles.safeArea}>
      <View style={styles.page}>
        <View style={styles.header}>
          <Pressable
            onPress={() => router.push('/')}
            style={styles.brandRow}
            accessibilityRole="button"
            accessibilityLabel="Go to StudyBuddy homepage"
          >
            <StudyBuddyLogoMark />
            <Text style={styles.brandName}>StudyBuddy</Text>
          </Pressable>

          <Pressable
            onPress={() => undefined}
            style={styles.accountButton}
            accessibilityRole="button"
            accessibilityLabel="Open account settings"
          >
            <UserAvatar />
            <Text style={styles.username}>{'<username>'}</Text>
          </Pressable>
        </View>

        <View style={styles.accentBar}>
          <Pressable
            onPress={() => setMenuVisible(true)}
            style={styles.menuButton}
            accessibilityRole="button"
            accessibilityLabel="Open menu"
          >
            <Text style={styles.menuIcon}>☰</Text>
            <Text style={styles.menuText}>Menu</Text>
          </Pressable>
        </View>

        {children}
      </View>

      <Modal
        visible={menuVisible}
        transparent
        animationType="fade"
        onRequestClose={() => setMenuVisible(false)}
      >
        <Pressable style={styles.modalBackdrop} onPress={() => setMenuVisible(false)}>
          <View style={styles.menuPopup}>
            {MENU_ITEMS.map((item) => (
              <Pressable
                key={item}
                onPress={() => handleMenuItemPress(item)}
                style={styles.menuItem}
              >
                <Text style={styles.menuItemText}>{item}</Text>
              </Pressable>
            ))}
          </View>
        </Pressable>
      </Modal>
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
    width: '100%',
    paddingHorizontal: spacing.pageHorizontal,
    paddingVertical: 14,
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
  },
  brandRow: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 10,
  },
  brandName: {
    color: colors.textDark,
    fontSize: 22,
    fontWeight: '700',
  },
  accountButton: {
    borderRadius: 12,
    paddingVertical: 4,
    flexDirection: 'row',
    alignItems: 'center',
    gap: 10,
  },
  username: {
    color: colors.textDark,
    fontSize: 15,
    fontWeight: '500',
  },
  accentBar: {
    height: 52,
    backgroundColor: colors.accent,
    justifyContent: 'center',
    alignItems: 'flex-start',
  },
  menuButton: {
    marginLeft: 20,
    borderRadius: 8,
    paddingHorizontal: 18,
    paddingVertical: 10,
    flexDirection: 'row',
    alignItems: 'center',
    gap: 12,
  },
  menuIcon: {
    color: colors.pageWhite,
    fontSize: 23,
    lineHeight: 24,
  },
  menuText: {
    color: colors.pageWhite,
    fontSize: 15,
    fontWeight: '600',
  },
  modalBackdrop: {
    flex: 1,
    backgroundColor: 'rgba(0, 0, 0, 0.15)',
    alignItems: 'flex-start',
    paddingTop: 124,
    paddingLeft: 20,
  },
  menuPopup: {
    minWidth: 220,
    borderRadius: 8,
    backgroundColor: colors.pageWhite,
    paddingVertical: 6,
    shadowColor: '#000000',
    shadowOpacity: 0.2,
    shadowRadius: 8,
    shadowOffset: { width: 0, height: 3 },
    elevation: 6,
  },
  menuItem: {
    paddingHorizontal: 16,
    paddingVertical: 12,
  },
  menuItemText: {
    color: colors.textDark,
    fontSize: 15,
  },
});
