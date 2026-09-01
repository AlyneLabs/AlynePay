import { Platform, StyleSheet, Text, type TextProps } from 'react-native';

import { Fonts, ThemeColor } from '@/constants/theme';
import { useTheme } from '@/hooks/use-theme';

export type ThemedTextProps = TextProps & {
  type?:
    | 'default'
    | 'title'
    | 'small'
    | 'smallBold'
    | 'subtitle'
    | 'link'
    | 'linkPrimary'
    | 'code'
    | 'labelMono'
    | 'headlineLg'
    | 'displayLg'
    | 'bodyMd'
    | 'amount';
  themeColor?: ThemeColor;
};

export function ThemedText({ style, type = 'default', themeColor, ...rest }: ThemedTextProps) {
  const theme = useTheme();

  return (
    <Text
      style={[
        { color: theme[themeColor ?? 'text'] },
        type === 'default' && styles.default,
        type === 'title' && styles.title,
        type === 'small' && styles.small,
        type === 'smallBold' && styles.smallBold,
        type === 'subtitle' && styles.subtitle,
        type === 'link' && styles.link,
        type === 'linkPrimary' && styles.linkPrimary,
        type === 'code' && styles.code,
        type === 'labelMono' && styles.labelMono,
        type === 'headlineLg' && styles.headlineLg,
        type === 'displayLg' && styles.displayLg,
        type === 'bodyMd' && styles.bodyMd,
        type === 'amount' && styles.amount,
        style,
      ]}
      {...rest}
    />
  );
}

const styles = StyleSheet.create({
  small: {
    fontSize: 14,
    lineHeight: 20,
    fontWeight: '500',
    fontFamily: Fonts.body,
  },
  smallBold: {
    fontSize: 14,
    lineHeight: 20,
    fontWeight: '700',
    fontFamily: Fonts.body,
  },
  default: {
    fontSize: 16,
    lineHeight: 24,
    fontWeight: '400',
    fontFamily: Fonts.body,
  },
  bodyMd: {
    fontSize: 16,
    lineHeight: 24,
    fontWeight: '400',
    fontFamily: Fonts.body,
  },
  title: {
    fontSize: 32,
    fontWeight: '700',
    lineHeight: 38,
    fontFamily: Fonts.sans,
    letterSpacing: -0.5,
  },
  subtitle: {
    fontSize: 24,
    lineHeight: 32,
    fontWeight: '600',
    fontFamily: Fonts.sans,
  },
  headlineLg: {
    fontSize: 28,
    lineHeight: 34,
    fontWeight: '600',
    fontFamily: Fonts.sans,
    letterSpacing: -0.5,
  },
  displayLg: {
    fontSize: 48,
    lineHeight: 52,
    fontWeight: '700',
    fontFamily: Fonts.sans,
    letterSpacing: -1,
  },
  labelMono: {
    fontFamily: Fonts.mono,
    fontSize: 12,
    lineHeight: 16,
    fontWeight: '500',
    letterSpacing: 0.8,
    textTransform: 'uppercase',
  },
  amount: {
    fontFamily: Fonts.sans,
    fontSize: 26,
    fontWeight: '700',
    letterSpacing: -0.5,
  },
  link: {
    lineHeight: 24,
    fontSize: 14,
    fontFamily: Fonts.body,
  },
  linkPrimary: {
    lineHeight: 24,
    fontSize: 14,
    color: '#4CD7F6',
    fontFamily: Fonts.body,
  },
  code: {
    fontFamily: Fonts.mono,
    fontWeight: Platform.select({ android: '700' }) ?? '500',
    fontSize: 12,
  },
});

