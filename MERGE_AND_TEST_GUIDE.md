# Guide de Merge et Tests - Waitless

Ce guide explique comment fusionner vos changements de sécurité dans la branche `develop` et comment commencer l'implémentation des tests.

## 1. Fusionner la branche Feature dans Develop

Suivez ces commandes Git pour intégrer votre travail proprement :

```bash
# 1. Assurez-vous que tout est commit sur votre branche actuelle
git add .
git commit -m "feat: implement service-to-service security and gateway validation"
git push origin feature/SCRUM-33-api-gateway

# 2. Basculer sur la branche develop
git checkout develop

# 3. Récupérer les dernières modifications de develop (au cas où)
git pull origin develop

# 4. Fusionner la branche feature dans develop
git merge feature/SCRUM-33-api-gateway

# 5. Résoudre les conflits si nécessaire, puis pousser sur le serveur
git push origin develop
```

---

## 2. Préparation des Tests Unitaires et d'Intégration

Maintenant que vous êtes sur `develop`, voici comment commencer les tests.

### Dépendances nécessaires (déjà présentes dans vos pom.xml)
- `spring-boot-starter-test` : Pour le support global des tests.
- `spring-security-test` : **Essentiel** pour tester les endpoints sécurisés.

### Comment créer un Test d'Intégration pour la sécurité

Voici un exemple pour `UserControllerTest.java` :

```java
@SpringBootTest
@AutoConfigureMockMvc
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void whenAccessingAdminEndpointWithoutToken_thenUnauthorized() throws Exception {
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void whenAccessingAdminEndpointWithAdminRole_thenSuccess() throws Exception {
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "CLIENT")
    void whenAccessingAdminEndpointWithClientRole_thenForbidden() throws Exception {
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isForbidden());
    }
}
```

### Commandes pour exécuter les tests
- **Tout le projet** : `mvn clean test`
- **Un seul service** : `cd services/user-service && mvn test`
- **Un seul test** : `mvn test -Dtest=UserControllerTest`

---

## 3. Checklist de Test pour SCRUM-33
- [ ] Vérifier que chaque microservice démarre sans erreur de Bean.
- [ ] Vérifier que le Gateway bloque les requêtes sans Bearer Token (401).
- [ ] Vérifier que le Gateway et les microservices acceptent les tokens de Keycloak.
- [ ] Vérifier que le rôle `ADMIN` peut accéder à `GET /api/users`.
- [ ] Vérifier que le rôle `CLIENT` reçoit un `403` sur `GET /api/users`.
