# backend

Petit service Spring Boot minimal pour le projet ai-sandbox.

Prérequis
- JDK 17
- Maven (ou utilisez votre IDE pour lancer l'application)

Compiler et lancer

```powershell
cd backend
mvn package
java -jar target/backend-1.0-SNAPSHOT.jar
```

Endpoints utiles
- Application: http://localhost:8080/sandbox/hello
- Actuator health: http://localhost:8080/sandbox/actuator/health
- Actuator info: http://localhost:8080/sandbox/actuator/info

Notes
- `application.properties` configure le port (8080) et le context-path `/sandbox`.
- Pour le développement, expose tous les endpoints d'actuator listés dans le fichier `application.properties`.
