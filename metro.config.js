const { getDefaultConfig } = require('expo/metro-config');

const config = getDefaultConfig(__dirname);
const defaultResolveRequest = config.resolver.resolveRequest;

// Expo's web transform currently requests the old AssetRegistry path while
// React Native Web exposes the module under its current path.
config.resolver.resolveRequest = (context, moduleName, platform) => {
  if (moduleName === 'react-native-web/dist/exports/AssetRegistry') {
    return context.resolveRequest(
      context,
      'react-native-web/dist/modules/AssetRegistry',
      platform,
    );
  }

  return defaultResolveRequest
    ? defaultResolveRequest(context, moduleName, platform)
    : context.resolveRequest(context, moduleName, platform);
};

module.exports = config;
