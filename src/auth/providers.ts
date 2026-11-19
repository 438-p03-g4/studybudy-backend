/**
 * OAuth providers planned for the app. The backend endpoints are intentionally
 * not called until the backend authentication contract is implemented.
 */
export type OAuthProvider = 'google' | 'github';

export const supportedOAuthProviders: OAuthProvider[] = ['google', 'github'];

export type OAuthConfiguration = {
  provider: OAuthProvider;
  configured: boolean;
};

export const oauthConfiguration: OAuthConfiguration[] = supportedOAuthProviders.map(
  (provider) => ({ provider, configured: false }),
);
