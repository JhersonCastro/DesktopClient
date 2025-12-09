package org.example.fronted.controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.scene.layout.HBox;
import org.example.fronted.util.SessionManager;
import org.example.fronted.api.UserApi; // Importar la API
import reactor.core.publisher.Mono; // Importar Mono

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;

public class DocenteNuevoFormatoAController extends UIBase implements Initializable {

    // ========== COMPONENTES FXML ==========
    @FXML private TextField estudianteTextField; // Cambiado de ComboBox a TextField
    @FXML private TextField tituloTextField;
    @FXML private RadioButton radioInvestigacion;
    @FXML private RadioButton radioPracticaProfesional;
    @FXML private Label nombreDocenteLabel;
    @FXML private ComboBox<String> codirectorComboBox;
    @FXML private ComboBox<String> programaComboBox;
    @FXML private TextArea objetivoGeneralTextArea;
    @FXML private TextArea objetivoEspecifico1TextArea;
    @FXML private TextArea objetivoEspecifico2TextArea;
    @FXML private TextArea objetivoEspecifico3TextArea;
    @FXML private TextField archivoPdfTextField;
    @FXML private VBox cartaAceptacionContainer;
    @FXML private TextField cartaAceptacionTextField;

    // Para mostrar resultados de búsqueda
    @FXML private VBox resultadosBusquedaContainer;
    @FXML private VBox resultadosList;

    // ToggleGroup para radio buttons
    private ToggleGroup modalidadToggleGroup;

    // Variables de estado
    private File archivoPdfSeleccionado;
    private File cartaAceptacionSeleccionada;
    private SessionManager sessionManager;
    private EstudianteAutoComplete estudianteSeleccionado;
    private UserApi userApi; // Instancia de la API

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        sessionManager = SessionManager.getInstance();
        userApi = new UserApi(); // Inicializar la API

        // 1. Inicializar ToggleGroup
        inicializarToggleGroup();

        // 2. Cargar datos del docente
        cargarDatosDocente();

        // 3. Configurar búsqueda por Enter en el TextField
        configurarBusquedaPorEnter();

        // 4. Cargar datos iniciales
        cargarDatosIniciales();

        // 5. Configurar listeners
        configurarListeners();

        // 6. Ocultar contenedor de resultados inicialmente
        resultadosBusquedaContainer.setVisible(false);
    }

    private void inicializarToggleGroup() {
        modalidadToggleGroup = new ToggleGroup();
        radioInvestigacion.setToggleGroup(modalidadToggleGroup);
        radioPracticaProfesional.setToggleGroup(modalidadToggleGroup);
        radioInvestigacion.setSelected(true);
    }

    private void cargarDatosDocente() {
        String docenteNombre = sessionManager.getUserFullName();
        nombreDocenteLabel.setText(docenteNombre + " (Usted)");
    }

    private void configurarBusquedaPorEnter() {
        // Configurar para que al presionar Enter en el TextField, se realice la búsqueda
        estudianteTextField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                String busqueda = estudianteTextField.getText().trim();
                if (busqueda.length() >= 2) {
                    buscarEstudiantesEnServidor(busqueda);
                } else {
                    mostrarMensaje("Ingrese al menos 2 caracteres para buscar", Alert.AlertType.WARNING);
                }
            }
        });

        // Tooltip para indicar al usuario que use Enter
        Tooltip tooltip = new Tooltip("Escriba nombre, email o código y presione ENTER para buscar");
        estudianteTextField.setTooltip(tooltip);
    }

    private void buscarEstudiantesEnServidor(String busqueda) {
        // Mostrar contenedor de resultados
        resultadosBusquedaContainer.setVisible(true);
        resultadosBusquedaContainer.setManaged(true);

        // Limpiar resultados anteriores
        resultadosList.getChildren().clear();

        // Mostrar indicador de carga
        Label loadingLabel = new Label("Buscando estudiantes...");
        loadingLabel.setStyle("-fx-text-fill: #7f8c8d; -fx-font-style: italic;");
        resultadosList.getChildren().add(loadingLabel);

        // Usar el método real de la API
        userApi.buscarUsuarios(busqueda)
                .subscribe(usuarios -> {
                    Platform.runLater(() -> {
                        mostrarResultadosBusqueda(convertirUsuariosAEstudiantes(usuarios), busqueda);
                    });
                }, error -> {
                    Platform.runLater(() -> {
                        mostrarErrorBusqueda("Error en la búsqueda: " + error.getMessage());
                    });
                });
    }

    private List<EstudianteAutoComplete> convertirUsuariosAEstudiantes(List<Map<String, Object>> usuarios) {
        List<EstudianteAutoComplete> estudiantes = new ArrayList<>();

        if (usuarios != null) {
            for (Map<String, Object> usuario : usuarios) {
                try {
                    // Extraer datos del mapa según la estructura esperada del backend
                    String nombreCompleto = (String) usuario.get("nombreCompleto");
                    String codigo = (String) usuario.get("codigo");
                    String email = (String) usuario.get("email");

                    // El campo "programa" podría venir de diferentes formas
                    String programa = "";
                    if (usuario.get("programa") != null) {
                        programa = (String) usuario.get("programa");
                    } else if (usuario.get("programaAcademico") != null) {
                        programa = (String) usuario.get("programaAcademico");
                    }

                    // Filtrar solo estudiantes (opcional, si la API no filtra por rol)
                    String rol = (String) usuario.get("rol");
                    if (rol == null || "ESTUDIANTE".equalsIgnoreCase(rol) || "estudiante".equalsIgnoreCase(rol)) {
                        estudiantes.add(new EstudianteAutoComplete(
                                nombreCompleto != null ? nombreCompleto : "Nombre no disponible",
                                codigo != null ? codigo : "N/A",
                                email != null ? email : "N/A",
                                programa != null ? programa : "Programa no especificado"
                        ));
                    }
                } catch (Exception e) {
                    System.err.println("Error procesando usuario: " + e.getMessage());
                }
            }
        }

        return estudiantes;
    }

    private void mostrarResultadosBusqueda(List<EstudianteAutoComplete> resultados, String busqueda) {
        // Limpiar resultados
        resultadosList.getChildren().clear();

        if (resultados.isEmpty()) {
            Label noResultsLabel = new Label("No se encontraron estudiantes para: \"" + busqueda + "\"");
            noResultsLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-style: italic;");
            resultadosList.getChildren().add(noResultsLabel);
            return;
        }

        // Mostrar cada resultado como un botón seleccionable
        for (EstudianteAutoComplete estudiante : resultados) {
            Button resultButton = new Button();
            resultButton.setMaxWidth(Double.MAX_VALUE);
            resultButton.setStyle("-fx-background-color: white; -fx-border-color: #ddd; -fx-border-width: 1; " +
                    "-fx-text-alignment: left; -fx-alignment: CENTER_LEFT; -fx-padding: 10px;");

            // Crear contenido del botón
            VBox content = new VBox(3);

            Label nombreLabel = new Label(estudiante.getNombreCompleto()); // CORREGIDO: Quité "Nombre" extra
            nombreLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: black");

            HBox infoRow = new HBox(15);
            Label codigoLabel = new Label("Código: " + estudiante.getCodigo());
            codigoLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #666;");
            Label emailLabel = new Label("Email: " + estudiante.getEmail());
            emailLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #3498db;");
            Label programaLabel = new Label("Programa: " + estudiante.getPrograma());
            programaLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #666;");

            infoRow.getChildren().addAll(codigoLabel, emailLabel, programaLabel);
            content.getChildren().addAll(nombreLabel, infoRow);

            resultButton.setGraphic(content);

            // Acción al hacer clic en el resultado
            resultButton.setOnAction(e -> {
                seleccionarEstudiante(estudiante);
            });

            resultadosList.getChildren().add(resultButton);
        }

        // Agregar contador de resultados
        Label countLabel = new Label("Encontrados " + resultados.size() + " estudiantes");
        countLabel.setStyle("-fx-text-fill: #27ae60; -fx-font-size: 12px; -fx-padding: 5 0 0 0;");
        resultadosList.getChildren().add(countLabel);
    }

    private void seleccionarEstudiante(EstudianteAutoComplete estudiante) {
        this.estudianteSeleccionado = estudiante;
        estudianteTextField.setText(estudiante.getDisplayText());
        resultadosBusquedaContainer.setVisible(false);
        resultadosBusquedaContainer.setManaged(false);

        // Mostrar mensaje de confirmación
        mostrarMensaje("Estudiante seleccionado: " + estudiante.getNombreCompleto(), Alert.AlertType.INFORMATION);

        // Opcional: Automáticamente cargar el programa del estudiante en el ComboBox
        if (estudiante.getPrograma() != null && !estudiante.getPrograma().isEmpty()) {
            programaComboBox.setValue(estudiante.getPrograma());
        }
    }

    private void mostrarErrorBusqueda(String error) {
        resultadosList.getChildren().clear();
        Label errorLabel = new Label(error);
        errorLabel.setStyle("-fx-text-fill: #e74c3c;");
        resultadosList.getChildren().add(errorLabel);
    }

    private void cargarDatosIniciales() {
        // Cargar codirectores
        codirectorComboBox.getItems().addAll(
                "Ninguno",
                "Dra. María Fernández",
                "Mg. Luis Martínez",
                "Dr. Jorge Hernández"
        );
        codirectorComboBox.getSelectionModel().select(0);

        // Cargar programas académicos (puedes cargarlos desde la API si es necesario)
        programaComboBox.getItems().addAll(
                "Ingeniería de Sistemas",
                "Ingeniería Electrónica",
                "Automática Industrial",
                "Tecnología en Telemática",
                "Ingeniería Civil",
                "Ingeniería Ambiental"
        );
    }

    private void configurarListeners() {
        // Mostrar/ocultar carta de aceptación según modalidad
        modalidadToggleGroup.selectedToggleProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == radioPracticaProfesional) {
                cartaAceptacionContainer.setVisible(true);
                cartaAceptacionContainer.setManaged(true);
            } else {
                cartaAceptacionContainer.setVisible(false);
                cartaAceptacionContainer.setManaged(false);
                cartaAceptacionTextField.clear();
                cartaAceptacionSeleccionada = null;
            }
        });

        // Limpiar selección de estudiante si se modifica el texto manualmente
        estudianteTextField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (estudianteSeleccionado != null &&
                    !newVal.equals(estudianteSeleccionado.getDisplayText())) {
                estudianteSeleccionado = null;
            }
        });
    }

    @FXML
    private void seleccionarArchivoPdf() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Seleccionar Formato A en PDF");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Archivos PDF", "*.pdf")
        );

        File archivo = fileChooser.showOpenDialog(archivoPdfTextField.getScene().getWindow());
        if (archivo != null) {
            archivoPdfTextField.setText(archivo.getAbsolutePath());
            archivoPdfSeleccionado = archivo;
        }
    }

    @FXML
    private void seleccionarCartaAceptacion() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Seleccionar Carta de Aceptación");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Archivos PDF", "*.pdf")
        );

        File archivo = fileChooser.showOpenDialog(cartaAceptacionTextField.getScene().getWindow());
        if (archivo != null) {
            cartaAceptacionTextField.setText(archivo.getAbsolutePath());
            cartaAceptacionSeleccionada = archivo;
        }
    }

    @FXML
    private void agregarObjetivoEspecifico() {
        // Mostrar diálogo para agregar objetivo
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Agregar Objetivo Específico");
        dialog.setHeaderText("Nuevo objetivo específico");
        dialog.setContentText("Ingrese el objetivo:");

        Optional<String> resultado = dialog.showAndWait();
        resultado.ifPresent(objetivo -> {
            mostrarMensaje("Objetivo agregado: " + objetivo, Alert.AlertType.INFORMATION);
            // Aquí podrías agregar el objetivo a una lista dinámica
        });
    }

    @FXML
    private void enviarFormatoA() {
        if (!validarFormulario()) {
            return;
        }

        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Confirmar envío");
        confirmacion.setHeaderText("¿Está seguro de enviar el Formato A?");
        confirmacion.setContentText("Una vez enviado, no podrá modificarlo hasta recibir evaluación del coordinador.");

        if (confirmacion.showAndWait().orElse(null) != ButtonType.OK) {
            return;
        }

        // Lógica de envío
        boolean exito = procesarEnvioFormatoA();

        if (exito) {
            mostrarAlerta("Éxito", "Formato A enviado correctamente. Se ha notificado al coordinador.", Alert.AlertType.INFORMATION);
            regresarAlDashboard();
        } else {
            mostrarAlerta("Error", "No se pudo enviar el Formato A. Intente nuevamente.", Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void cancelar() {
        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Confirmar cancelación");
        confirmacion.setHeaderText("¿Desea cancelar el formulario?");
        confirmacion.setContentText("Se perderán todos los datos no guardados.");

        if (confirmacion.showAndWait().orElse(null) == ButtonType.OK) {
            regresarAlDashboard();
        }
    }

    @FXML
    private void regresarAlDashboard() {
        loadView("/views/professor/dashboard_professor.fxml");
    }

    private boolean validarFormulario() {
        StringBuilder errores = new StringBuilder();

        if (estudianteSeleccionado == null) {
            errores.append("• Debe seleccionar un estudiante (busque y seleccione uno de la lista)\n");
        }

        if (tituloTextField.getText().trim().isEmpty()) {
            errores.append("• El título del proyecto es obligatorio\n");
        }

        if (programaComboBox.getValue() == null) {
            errores.append("• Debe seleccionar un programa académico\n");
        }

        if (objetivoGeneralTextArea.getText().trim().isEmpty()) {
            errores.append("• El objetivo general es obligatorio\n");
        }

        if (archivoPdfSeleccionado == null) {
            errores.append("• Debe seleccionar el archivo PDF del Formato A\n");
        }

        if (radioPracticaProfesional.isSelected() && cartaAceptacionSeleccionada == null) {
            errores.append("• Para Práctica Profesional, debe adjuntar la carta de aceptación\n");
        }

        if (errores.length() > 0) {
            mostrarAlerta("Errores en el formulario", errores.toString(), Alert.AlertType.WARNING);
            return false;
        }

        return true;
    }

    private boolean procesarEnvioFormatoA() {
        // Implementar lógica real de envío al backend
        // 1. Crear DTO con datos del formulario
        // 2. Llamar servicio para guardar en BD
        // 3. Subir archivos
        // 4. Enviar notificación al coordinador

        // Ejemplo de cómo podrías usar los datos:
        System.out.println("Estudiante seleccionado: " + estudianteSeleccionado.getNombreCompleto());
        System.out.println("Código estudiante: " + estudianteSeleccionado.getCodigo());
        System.out.println("Título: " + tituloTextField.getText());
        System.out.println("Modalidad: " + (radioInvestigacion.isSelected() ? "Investigación" : "Práctica Profesional"));

        return true;
    }

    private void mostrarAlerta(String titulo, String mensaje, Alert.AlertType tipo) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }

    private void mostrarMensaje(String mensaje, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle("Información");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    // ========== CLASE PARA ESTUDIANTES ==========
    public static class EstudianteAutoComplete {
        private String nombreCompleto;
        private String codigo;
        private String email;
        private String programa;

        public EstudianteAutoComplete(String nombreCompleto, String codigo, String email, String programa) {
            this.nombreCompleto = nombreCompleto;
            this.codigo = codigo;
            this.email = email;
            this.programa = programa;
        }

        public String getNombreCompleto() { return nombreCompleto; }
        public String getCodigo() { return codigo; }
        public String getEmail() { return email; }
        public String getPrograma() { return programa; }

        public String getDisplayText() {
            return nombreCompleto + " (" + email + ")";
        }

        @Override
        public String toString() {
            return getDisplayText();
        }
    }
}