/**
 * Production : pas de proxy — URL Keycloak complète (adapter au déploiement).
 */
export const environment = {
  production: true,
  apiUrl: '/api',
  keycloak: {
    realm: 'waitless-realm',
    tokenUrl: 'http://localhost:8180/realms/waitless-realm/protocol/openid-connect/token',
    clientId: 'waitless-client',
    clientSecret: '',
    publicBaseUrl: 'http://localhost:8180',
  },
};
