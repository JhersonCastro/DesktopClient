package org.example.fronted.config;

public class ApiConfig {

    // URLs base de cada microservicio
    public static final String USER_SERVICE_URL = "http://localhost:8081";
    public static final String PROJECT_SERVICE_URL = "http://localhost:8082";
    public static final String DOCUMENT_SERVICE_URL = "http://localhost:8083";
    public static final String NOTIFICATION_SERVICE_URL = "http://localhost:8084";
    public static final String MESSAGING_SERVICE_URL = "http://localhost:8085";

    // Endpoints comunes para User Service
    public static class UserEndpoints {
        public static final String LOGIN = "/api/auth/login";
        public static final String REGISTER = "/api/usuarios/registro";
        public static final String CURRENT_USER = "/api/auth/me";
        public static final String LOGOUT = "/api/auth/logout";
        public static final String ESTUDIANTES = "/api/usuarios/estudiantes";
        public static final String DOCENTES = "/api/usuarios/docentes";
        public static final String COORDINADORES = "/api/usuarios/coordinadores";
        public static final String JEFES = "/api/usuarios/jefes";
    }

    // Endpoints para Project Service
    public static class ProjectEndpoints {
        public static final String BASE = "/api/v1/proyectos";
        public static final String FORMATO_A = "/formatoA";
        public static final String BY_ESTUDIANTE = "/estudiante/{email}";
        public static final String ANTEPROYECTOS_JEFE = "/anteproyectos/jefe/{email}";
        public static final String ASIGNAR_EVALUADORES = "/{idProyecto}/evaluadores";
        public static final String EVALUAR = "/{idProyecto}/evaluar";
        public static final String REINTENTAR = "/{idProyecto}/reintentar";
        public static final String SUBIR_ANTEPROYECTO = "/{idProyecto}/anteproyecto";
    }

    // Endpoints para Document Service
    public static class DocumentEndpoints {
        public static final String UPLOAD = "/upload";
        public static final String DOWNLOAD = "/download/{id}";
        public static final String BY_PROYECTO = "/proyecto/{id}";
        public static final String BY_TIPO = "/tipo/{tipo}";
    }

    // Endpoints para Notification Service
    public static class NotificationEndpoints {
        public static final String BY_USER = "/api/notificaciones/usuario";
        public static final String MARK_READ = "/api/notificaciones/marcar-leida/{id}";
        public static final String UNREAD_COUNT = "/api/notificaciones/no-leidas";
        public static final String CREATE = "/api/notificaciones";
    }

    // Endpoints para Messaging Service
    public static class MessagingEndpoints {
        public static final String CONVERSATIONS = "/api/mensajes/conversaciones";
        public static final String MESSAGES = "/api/mensajes/{conversacionId}";
        public static final String SEND = "/api/mensajes/enviar";
        public static final String START_CONVERSATION = "/api/mensajes/nueva-conversacion";
    }

    // Headers comunes
    public static class Headers {
        public static final String AUTHORIZATION = "Authorization";
        public static final String CONTENT_TYPE = "Content-Type";
        public static final String ACCEPT = "Accept";
    }

    // Media types
    public static class MediaTypes {
        public static final String APPLICATION_JSON = "application/json";
        public static final String MULTIPART_FORM_DATA = "multipart/form-data";
        public static final String FORM_URLENCODED = "application/x-www-form-urlencoded";
    }

    // Timeouts (en milisegundos)
    public static class Timeouts {
        public static final int CONNECT_TIMEOUT = 10000; // 10 segundos
        public static final int READ_TIMEOUT = 30000;    // 30 segundos
        public static final int WRITE_TIMEOUT = 30000;   // 30 segundos
    }
}