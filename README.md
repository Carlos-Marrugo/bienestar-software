**Authentication Arquitectura Flujo**
![diagram-export-1-2-2025-18_02_51](https://github.com/user-attachments/assets/b2523d3a-23d0-4b98-a3fd-2d5c446fba08)

![image](https://github.com/user-attachments/assets/92f15e33-18e8-4039-a994-b6a6ccf363a6)


## Email Service

El sistema envía automáticamente emails en estos casos:
1. Registro de nuevo usuario
2. Inscripción a actividades
3. Recuperación de contraseña (pendiente)

Configuración requerida:
- Credenciales OAuth2 de Gmail
- Plantillas Thymeleaf en `resources/templates/emails`

## Configuración Segura

1. `application.properties` con variables 
2. Cree un archivo `.env` con sus credenciales
3. Nunca suba archivos con datos sensibles

Variables requeridas:
- DB_URL, DB_USER, DB_PASSWORD
- EMAIL_USER, EMAIL_PASSWORD
