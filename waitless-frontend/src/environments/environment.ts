/**
 * Dev (ng serve) : token via proxy /keycloak → http://localhost:8180 (évite CORS navigateur).
 * Identifiants de test : docker/keycloak-realm.json (ex. client_user / Client@1234).
 */
export const environment = {
  production: false,
  apiUrl: '/api',
  keycloak: {
    realm: 'waitless-realm',
    /** Même origine que l’app → proxy.conf.json relaie vers Keycloak */
    tokenUrl: '/keycloak/realms/waitless-realm/protocol/openid-connect/token',
    clientId: 'waitless-client',
    clientSecret: 'waitless-secret-99b3-4f2a',
    /** URL vue par le navigateur pour redirection inscription (hors proxy) */
    publicBaseUrl: 'http://localhost:8180',
  },
};
