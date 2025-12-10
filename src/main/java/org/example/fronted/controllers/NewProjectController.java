package org.example.fronted.controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.input.KeyCode;

import org.example.fronted.api.UserApi;
import org.example.fronted.api.ProyectoApi;
import org.example.fronted.dto.ProyectoRequest;
import org.example.fronted.models.ProyectoGrado;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class NewProjectController extends UIBase {

    // ========== FXML ==========
    @FXML private TextField tituloField;
    @FXML private TextField estudianteField;
    @FXML private VBox estudiantesSeleccionados;
    @FXML private VBox resultadosEstudiantes;

    @FXML private TextField directorField;
    @FXML private VBox resultadosDirectores;

    @FXML private TextField codirectorField;
    @FXML private VBox resultadosCodirectores;

    @FXML private ComboBox<String> programaCombo;
    @FXML private TextArea objetivoGeneralArea;
    @FXML private VBox objetivosEspecificosContainer;

    @FXML private RadioButton radioInvestigacion;
    @FXML private RadioButton radioPractica;

    // ========== VARIABLES ==========
    // Guardamos el texto completo y el email separados por "|"
    private final List<String> estudiantes = new ArrayList<>();
    private String directorSeleccionado;
    private String codirectorSeleccionado;

    private UserApi userApi;
    private ProyectoApi proyectoApi;

    // ========== INIT ==========
    @FXML
    public void initialize() {
        userApi = new UserApi();
        proyectoApi = new ProyectoApi();

        // Agrupar RadioButtons para modalidad
        ToggleGroup modalidadGroup = new ToggleGroup();
        radioInvestigacion.setToggleGroup(modalidadGroup);
        radioPractica.setToggleGroup(modalidadGroup);

        // Configurar búsquedas con ENTER
        estudianteField.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) {
                buscarEstudiantes(estudianteField.getText());
            }
        });

        directorField.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) {
                buscarDocentes(directorField.getText(), resultadosDirectores, true);
            }
        });

        codirectorField.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) {
                buscarDocentes(codirectorField.getText(), resultadosCodirectores, false);
            }
        });
    }

    /* --------- AUTOCOMPLETADO ESTUDIANTES ---------- */

    private void buscarEstudiantes(String query) {
        if (query == null || query.trim().isEmpty()) return;

        resultadosEstudiantes.getChildren().clear();
        resultadosEstudiantes.setVisible(true);
        resultadosEstudiantes.setManaged(true);

        Label loading = new Label("Buscando estudiantes...");
        loading.getStyleClass().add("cnp-result-item");
        resultadosEstudiantes.getChildren().add(loading);

        userApi.buscarUsuarios(query).subscribe(usuarios -> Platform.runLater(() -> {
            resultadosEstudiantes.getChildren().clear();

            for (Map<String, Object> u : usuarios) {
                String nombre = (String) u.get("nombreCompleto");
                String email = (String) u.get("email");
                String rol = (String) u.getOrDefault("rol", "ESTUDIANTE");

                // Filtrar solo estudiantes
                if (!rol.equalsIgnoreCase("ESTUDIANTE")) continue;

                String texto = nombre + " (" + email + ")";
                Label item = new Label(texto);
                item.getStyleClass().add("cnp-result-item");
                item.setOnMouseClicked(ev -> agregarEstudiante(texto, email));

                resultadosEstudiantes.getChildren().add(item);
            }

            if (resultadosEstudiantes.getChildren().isEmpty()) {
                Label noResults = new Label("No se encontraron estudiantes");
                noResults.getStyleClass().add("cnp-result-item");
                resultadosEstudiantes.getChildren().add(noResults);
            }
        }), err -> {
            Platform.runLater(() -> {
                resultadosEstudiantes.getChildren().clear();
                Label error = new Label("Error al buscar estudiantes");
                error.getStyleClass().add("cnp-result-item");
                resultadosEstudiantes.getChildren().add(error);
            });
        });
    }

    /* --------- AUTOCOMPLETADO DOCENTES ---------- */

    private void buscarDocentes(String query, VBox cont, boolean esDirector) {
        if (query == null || query.trim().isEmpty()) return;

        cont.getChildren().clear();
        cont.setVisible(true);
        cont.setManaged(true);

        Label loading = new Label("Buscando docentes...");
        loading.getStyleClass().add("cnp-result-item");
        cont.getChildren().add(loading);

        userApi.buscarUsuarios(query).subscribe(usuarios -> Platform.runLater(() -> {
            cont.getChildren().clear();

            for (Map<String, Object> u : usuarios) {
                String nombre = (String) u.get("nombreCompleto");
                String email = (String) u.get("email");
                String rol = (String) u.getOrDefault("rol", "DOCENTE");

                // Filtrar solo docentes
                if (!rol.equalsIgnoreCase("DOCENTE")) continue;

                String texto = nombre + " (" + email + ")";
                Label item = new Label(texto);
                item.getStyleClass().add("cnp-result-item");
                item.setOnMouseClicked(ev -> seleccionarDocente(cont, texto, email, esDirector));

                cont.getChildren().add(item);
            }

            if (cont.getChildren().isEmpty()) {
                Label noResults = new Label("No se encontraron docentes");
                noResults.getStyleClass().add("cnp-result-item");
                cont.getChildren().add(noResults);
            }
        }), err -> Platform.runLater(() -> {
            cont.getChildren().clear();
            Label error = new Label("Error al buscar docentes");
            error.getStyleClass().add("cnp-result-item");
            cont.getChildren().add(error);
        }));
    }

    private void seleccionarDocente(VBox cont, String texto, String email, boolean esDirector) {
        if (esDirector) {
            directorField.setText(texto);
            directorSeleccionado = email;
        } else {
            codirectorField.setText(texto);
            codirectorSeleccionado = email;
        }

        cont.setVisible(false);
        cont.setManaged(false);
    }

    /* --------- ESTUDIANTES (TAGS) ---------- */

    private void agregarEstudiante(String nombre, String email) {
        if (estudiantes.size() >= 2) {
            mostrarAlerta("Límite alcanzado", "Solo puede agregar hasta 2 estudiantes", Alert.AlertType.WARNING);
            return;
        }

        // Verificar si ya está agregado (comparando por email)
        if (estudiantes.stream().anyMatch(e -> e.split("\\|")[1].equals(email))) {
            mostrarAlerta("Estudiante duplicado", "Este estudiante ya está agregado", Alert.AlertType.WARNING);
            return;
        }

        // Guardar como "nombre|email"
        String estudianteData = nombre + "|" + email;
        estudiantes.add(estudianteData);

        // Crear el tag visual
        HBox tag = new HBox();
        tag.getStyleClass().add("cnp-tag");
        tag.setSpacing(10);

        Label label = new Label(nombre);
        Button remove = new Button("×");
        remove.getStyleClass().add("cnp-tag-remove");
        remove.setOnAction(e -> {
            estudiantes.remove(estudianteData);
            estudiantesSeleccionados.getChildren().remove(tag);
        });

        tag.getChildren().addAll(label, remove);
        estudiantesSeleccionados.getChildren().add(tag);

        // Limpiar búsqueda
        resultadosEstudiantes.setVisible(false);
        resultadosEstudiantes.setManaged(false);
        estudianteField.clear();
    }

    /* --------- OBJETIVOS ---------- */

    @FXML
    private void agregarObjetivo() {
        TextArea area = new TextArea();
        area.setPromptText("Objetivo específico...");
        area.getStyleClass().add("cnp-textarea-small");
        area.setPrefRowCount(2);
        objetivosEspecificosContainer.getChildren().add(area);
    }

    /* --------- NAVEGACIÓN / ACCIONES ---------- */

    @FXML
    private void regresar() {
        loadView("/views/coordinator/dashboard_coordinator.fxml");
    }

    @FXML
    private void cancelar() {
        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Confirmar cancelación");
        confirmacion.setHeaderText(null);
        confirmacion.setContentText("¿Está seguro que desea cancelar? Se perderán todos los datos ingresados.");

        confirmacion.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                limpiarFormulario();
                loadView("/views/coordinator/dashboard_coordinator.fxml");
            }
        });
    }

    /* --------- CREAR PROYECTO (ENVIAR AL BACKEND) ---------- */

    @FXML
    private void crearProyecto() {
        // 1. Validar formulario
        if (!validarFormulario()) {
            return;
        }

        // 2. Construir el DTO
        ProyectoRequest request = construirProyectoRequest();

        // 3. Mostrar indicador de carga (opcional)
        Alert loading = new Alert(Alert.AlertType.INFORMATION);
        loading.setTitle("Creando proyecto");
        loading.setHeaderText(null);
        loading.setContentText("Por favor espere...");
        loading.show();

        // 4. Enviar al backend usando la API
        proyectoApi.crearProyecto(request).subscribe(
                proyectoCreado -> Platform.runLater(() -> {
                    loading.close();
                    mostrarAlerta(
                            "Éxito",
                            "El proyecto '" + proyectoCreado.getTitulo() + "' ha sido creado exitosamente",
                            Alert.AlertType.INFORMATION
                    );
                    limpiarFormulario();
                    loadView("/views/coordinator/dashboard_coordinator.fxml");
                }),
                error -> {
                    Platform.runLater(() -> {
                        loading.close();
                        mostrarAlerta(
                                "Error",
                                "No se pudo crear el proyecto: " + error.getMessage(),
                                Alert.AlertType.ERROR
                        );
                    });
                }
        );
    }

    /* --------- CONSTRUCCIÓN DEL DTO ---------- */

    private ProyectoRequest construirProyectoRequest() {
        ProyectoRequest request = new ProyectoRequest();

        // Título
        request.setTitulo(tituloField.getText().trim());

        // Modalidad
        request.setModalidad(getModalidadSeleccionada());

        // Director y Codirector
        request.setDirectorEmail(directorSeleccionado);
        request.setCodirectorEmail(codirectorSeleccionado);

        // Estudiantes (extraer emails)
        List<String> emailsEstudiantes = estudiantes.stream()
                .map(e -> e.split("\\|")[1])
                .collect(Collectors.toList());

        if (!emailsEstudiantes.isEmpty()) {
            request.setEstudiante1Email(emailsEstudiantes.get(0));
        }
        if (emailsEstudiantes.size() > 1) {
            request.setEstudiante2Email(emailsEstudiantes.get(1));
        }

        // Objetivos
        request.setObjetivoGeneral(objetivoGeneralArea.getText().trim());
        request.setObjetivosEspecificos(obtenerObjetivosEspecificos());

        return request;
    }

    /* --------- VALIDACIONES ---------- */

    private boolean validarFormulario() {
        if (tituloField.getText().trim().isEmpty()) {
            mostrarAlerta("Campo requerido", "El título del proyecto es obligatorio", Alert.AlertType.WARNING);
            tituloField.requestFocus();
            return false;
        }

        if (estudiantes.isEmpty()) {
            mostrarAlerta("Campo requerido", "Debe seleccionar al menos un estudiante", Alert.AlertType.WARNING);
            estudianteField.requestFocus();
            return false;
        }

        if (directorSeleccionado == null || directorSeleccionado.isEmpty()) {
            mostrarAlerta("Campo requerido", "Debe seleccionar un director", Alert.AlertType.WARNING);
            directorField.requestFocus();
            return false;
        }

        if (programaCombo.getValue() == null || programaCombo.getValue().isEmpty()) {
            mostrarAlerta("Campo requerido", "Debe seleccionar un programa", Alert.AlertType.WARNING);
            programaCombo.requestFocus();
            return false;
        }

        if (objetivoGeneralArea.getText().trim().isEmpty()) {
            mostrarAlerta("Campo requerido", "El objetivo general es obligatorio", Alert.AlertType.WARNING);
            objetivoGeneralArea.requestFocus();
            return false;
        }

        // Validar que haya al menos un objetivo específico
        String objetivosEsp = obtenerObjetivosEspecificos();
        if (objetivosEsp.isEmpty()) {
            mostrarAlerta("Campo requerido", "Debe ingresar al menos un objetivo específico", Alert.AlertType.WARNING);
            return false;
        }

        return true;
    }

    /* --------- UTILIDADES ---------- */

    private String getModalidadSeleccionada() {
        if (radioInvestigacion.isSelected()) {
            return (String) radioInvestigacion.getUserData();
        } else if (radioPractica.isSelected()) {
            return (String) radioPractica.getUserData();
        }
        return "INVESTIGACION"; // Por defecto
    }

    private String obtenerObjetivosEspecificos() {
        List<String> objetivos = new ArrayList<>();

        for (int i = 0; i < objetivosEspecificosContainer.getChildren().size(); i++) {
            if (objetivosEspecificosContainer.getChildren().get(i) instanceof TextArea) {
                TextArea area = (TextArea) objetivosEspecificosContainer.getChildren().get(i);
                String texto = area.getText().trim();
                if (!texto.isEmpty()) {
                    objetivos.add(texto);
                }
            }
        }

        // Unir con "|" como separador (ajusta según tu backend)
        return String.join("|", objetivos);
    }

    private void limpiarFormulario() {
        // Limpiar campos de texto
        tituloField.clear();
        estudianteField.clear();
        directorField.clear();
        codirectorField.clear();
        objetivoGeneralArea.clear();

        // Limpiar combo
        programaCombo.getSelectionModel().clearSelection();

        // Limpiar estudiantes seleccionados
        estudiantes.clear();
        estudiantesSeleccionados.getChildren().clear();

        // Limpiar docentes seleccionados
        directorSeleccionado = null;
        codirectorSeleccionado = null;

        // Reset modalidad
        radioInvestigacion.setSelected(true);

        // Limpiar contenedores de resultados
        resultadosEstudiantes.getChildren().clear();
        resultadosEstudiantes.setVisible(false);
        resultadosEstudiantes.setManaged(false);

        resultadosDirectores.getChildren().clear();
        resultadosDirectores.setVisible(false);
        resultadosDirectores.setManaged(false);

        resultadosCodirectores.getChildren().clear();
        resultadosCodirectores.setVisible(false);
        resultadosCodirectores.setManaged(false);

        // Restaurar objetivos específicos a los 2 iniciales
        objetivosEspecificosContainer.getChildren().clear();

        TextArea obj1 = new TextArea();
        obj1.setPromptText("Objetivo específico 1...");
        obj1.getStyleClass().add("cnp-textarea-small");
        obj1.setPrefRowCount(2);

        TextArea obj2 = new TextArea();
        obj2.setPromptText("Objetivo específico 2...");
        obj2.getStyleClass().add("cnp-textarea-small");
        obj2.setPrefRowCount(2);

        objetivosEspecificosContainer.getChildren().addAll(obj1, obj2);
    }

    /* ----- PRECARGA (OPCIONAL) ----- */

    public void precargar(String titulo, List<String> ests, String director) {
        if (titulo != null && !titulo.isEmpty()) {
            tituloField.setText(titulo);
        }
        if (director != null && !director.isEmpty()) {
            directorField.setText(director);
            // Nota: necesitarías el email real para directorSeleccionado
        }
        // Para estudiantes necesitarías tener formato "nombre|email"
    }

    /* --------- ALERTA ---------- */

    private void mostrarAlerta(String titulo, String msg, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}