import React, { useState, useEffect } from 'react';
import { useColorScheme } from 'react-native';
import { StatusBar } from 'expo-status-bar';
import { SafeAreaProvider } from 'react-native-safe-area-context';
import { NavigationContainer, DefaultTheme, DarkTheme } from '@react-navigation/native';
import { StorageService } from './src/services/storage';
import { Colors } from './src/constants/theme';
import BottomTabNavigator from './src/navigation/BottomTabNavigator';

export default function App() {
  const systemColorScheme = useColorScheme();
  const [themePreference, setThemePreference] = useState('SYSTEM');
  const [isReady, setIsReady] = useState(false);

  useEffect(() => {
    async function init() {
      await StorageService.initStorage();
      const settings = await StorageService.getSettings();
      if (settings?.themeMode) {
        setThemePreference(settings.themeMode);
      }
      setIsReady(true);
    }
    init();
  }, []);

  const isDark =
    themePreference === 'DARK' ||
    (themePreference === 'SYSTEM' && systemColorScheme === 'dark');

  const activeColors = isDark ? Colors.dark : Colors.light;

  const navigationTheme = isDark
    ? {
        ...DarkTheme,
        colors: {
          ...DarkTheme.colors,
          primary: activeColors.primary,
          background: activeColors.background,
          card: activeColors.surface,
          text: activeColors.text,
          border: activeColors.border,
        },
      }
    : {
        ...DefaultTheme,
        colors: {
          ...DefaultTheme.colors,
          primary: activeColors.primary,
          background: activeColors.background,
          card: activeColors.surface,
          text: activeColors.text,
          border: activeColors.border,
        },
      };

  if (!isReady) {
    return null;
  }

  return (
    <SafeAreaProvider>
      <StatusBar style={isDark ? 'light' : 'dark'} />
      <NavigationContainer theme={navigationTheme}>
        <BottomTabNavigator
          colors={activeColors}
          currentTheme={themePreference}
          onThemeChange={setThemePreference}
        />
      </NavigationContainer>
    </SafeAreaProvider>
  );
}
