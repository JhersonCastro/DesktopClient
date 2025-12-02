package org.example.fronted.controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.Parent;
import javafx.fxml.FXMLLoader;
import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.List;
import java.util.ArrayList;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert.AlertType;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import java.io.IOException;
import javafx.util.Callback;
import javafx.util.StringConverter;
import javafx.scene.layout.HBox;

public class DocenteNuevoFormatoAController implements Initializable {

    // ========== COMPONENTES FXML ==========
    @FXML private ComboBox<EstudianteAutoComplete> estudianteComboBox;
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

    // ToggleGroup para radio buttons
    private ToggleGroup modalidadToggleGroup;

    // Variables de estado
    private File archivoPdfSeleccionado;
    private File cartaAceptacionSeleccionada;
    private ObservableList<EstudianteAutoComplete> todosEstudiantes = FXCollections.observableArrayList();
    private ObservableList<EstudianteAutoComplete> estudiantesFiltrados = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // 1. Inicializar ToggleGroup
        inicializarToggleGroup();

        // 2. Cargar datos del docente
        cargarDatosDocente();

        // 3. Configurar autocompletado para estudiantes
        configurarAutocompletadoEstudiantes();

        // 4. Cargar datos iniciales
        cargarDatosIniciales();

        // 5. Configurar listeners
        configurarListeners();
    }

    private void inicializarToggleGroup() {
        modalidadToggleGroup = new ToggleGroup();
        radioInvestigacion.setToggleGroup(modalidadToggleGroup);
        radioPracticaProfesional.setToggleGroup(modalidadToggleGroup);
        radioInvestigacion.setSelected(true);
    }

    private void cargarDatosDocente() {
        // Obtener datos del docente desde JWT/sesión
        String docenteNombre = "Dr. Juan Pérez";
        nombreDocenteLabel.setText(docenteNombre + " (Usted)");
    }

    private void configurarAutocompletadoEstudiantes() {
        // 1. Hacer el ComboBox editable
        estudianteComboBox.setEditable(true);

        // 2. Configurar StringConverter para mostrar texto personalizado
        estudianteComboBox.setConverter(new StringConverter<EstudianteAutoComplete>() {
            @Override
            public String toString(EstudianteAutoComplete estudiante) {
                if (estudiante == null) {
                    return "";
                }
                return estudiante.getDisplayText();
            }

            @Override
            public EstudianteAutoComplete fromString(String string) {
                // Buscar estudiante por texto ingresado
                return todosEstudiantes.stream()
                        .filter(e -> e.matches(string))
                        .findFirst()
                        .orElse(null);
            }
        });

        // 3. Configurar CellFactory para mostrar múltiples líneas
        estudianteComboBox.setCellFactory(new Callback<ListView<EstudianteAutoComplete>,
                ListCell<EstudianteAutoComplete>>() {
            @Override
            public ListCell<EstudianteAutoComplete> call(ListView<EstudianteAutoComplete> param) {
                return new ListCell<EstudianteAutoComplete>() {
                    @Override
                    protected void updateItem(EstudianteAutoComplete estudiante, boolean empty) {
                        super.updateItem(estudiante, empty);
                        if (empty || estudiante == null) {
                            setText(null);
                            setGraphic(null);
                        } else {
                            // Crear VBox con múltiples líneas
                            VBox vbox = new VBox(3);
                            vbox.setStyle("-fx-padding: 5px;");

                            // Nombre en negrita
                            Label nombreLabel = new Label(estudiante.getNombreCompleto());
                            nombreLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #2e7d32;");

                            // Información adicional
                            HBox infoLine = new HBox(15);

                            Label codigoLabel = new Label("Código: " + estudiante.getCodigo());
                            codigoLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #666;");

                            Label emailLabel = new Label("Email: " + estudiante.getEmail());
                            emailLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #2196f3;");

                            Label programaLabel = new Label("Programa: " + estudiante.getPrograma());
                            programaLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #666;");

                            infoLine.getChildren().addAll(codigoLabel, emailLabel, programaLabel);
                            vbox.getChildren().addAll(nombreLabel, infoLine);

                            setGraphic(vbox);
                        }
                    }
                };
            }
        });

        // 4. Configurar el filtro de autocompletado
        configurarFiltroAutocompletado();
    }

    private void configurarFiltroAutocompletado() {
        // Escuchar cambios en el editor
        estudianteComboBox.getEditor().textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null || newValue.isEmpty()) {
                estudianteComboBox.setItems(todosEstudiantes);
            } else {
                filtrarEstudiantes(newValue.toLowerCase());
            }
        });

        // Mostrar todos los estudiantes al hacer clic
        estudianteComboBox.setOnShowing(event -> {
            if (estudianteComboBox.getEditor().getText().isEmpty()) {
                estudianteComboBox.setItems(todosEstudiantes);
            }
        });
    }

    private void filtrarEstudiantes(String textoBusqueda) {
        estudiantesFiltrados.clear();

        List<EstudianteAutoComplete> filtrados = todosEstudiantes.stream()
                .filter(estudiante ->
                        estudiante.getNombreCompleto().toLowerCase().contains(textoBusqueda) ||
                                estudiante.getEmail().toLowerCase().contains(textoBusqueda) ||
                                estudiante.getCodigo().toLowerCase().contains(textoBusqueda) ||
                                estudiante.getPrograma().toLowerCase().contains(textoBusqueda)
                )
                .limit(50) // Limitar resultados para mejor rendimiento
                .collect(java.util.stream.Collectors.toList());

        estudiantesFiltrados.addAll(filtrados);
        estudianteComboBox.setItems(estudiantesFiltrados);

        // Mostrar dropdown si hay resultados
        if (!filtrados.isEmpty()) {
            estudianteComboBox.show();
        }
    }

    private void cargarDatosIniciales() {
        // Cargar estudiantes de ejemplo (en producción, cargar desde backend)
        cargarEstudiantesEjemplo();

        // Cargar codirectores
        ObservableList<String> codirectores = FXCollections.observableArrayList(
                "Ninguno",
                "Dra. María Fernández",
                "Mg. Luis Martínez",
                "Dr. Jorge Hernández"
        );
        codirectorComboBox.setItems(codirectores);
        codirectorComboBox.getSelectionModel().select(0);

        // Programas ya cargados en FXML
    }

    private void cargarEstudiantesEjemplo() {
        // Datos de ejemplo - en producción esto vendría del backend
        todosEstudiantes.addAll(
                new EstudianteAutoComplete("Carlos López", "123456", "carlos.lopez@unicauca.edu.co", "Ingeniería de Sistemas"),
                new EstudianteAutoComplete("Ana García", "789012", "ana.garcia@unicauca.edu.co", "Ingeniería Electrónica"),
                new EstudianteAutoComplete("Pedro Rodríguez", "345678", "pedro.rodriguez@unicauca.edu.co", "Automática Industrial"),
                new EstudianteAutoComplete("María Fernández", "901234", "maria.fernandez@unicauca.edu.co", "Telemática")
        );

        estudianteComboBox.setItems(todosEstudiantes);
    }

    private void configurarListeners() {
        // Listener para mostrar/ocultar carta de aceptación según modalidad
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
        // Implementar lógica para agregar objetivos dinámicamente
    }

    @FXML
    private void enviarFormatoA() {
        // Implementar validación y envío
        if (!validarFormulario()) {
            return;
        }

        Alert confirmacion = new Alert(AlertType.CONFIRMATION);
        confirmacion.setTitle("Confirmar envío");
        confirmacion.setHeaderText("¿Está seguro de enviar el Formato A?");
        confirmacion.setContentText("Una vez enviado, no podrá modificarlo hasta recibir evaluación del coordinador.");

        if (confirmacion.showAndWait().orElse(null) != ButtonType.OK) {
            return;
        }

        // Lógica de envío
        boolean exito = procesarEnvioFormatoA();

        if (exito) {
            mostrarAlerta("Éxito", "Formato A enviado correctamente. Se ha notificado al coordinador.", AlertType.INFORMATION);
            volverAlDashboard();
        } else {
            mostrarAlerta("Error", "No se pudo enviar el Formato A. Intente nuevamente.", AlertType.ERROR);
        }
    }

    @FXML
    private void cancelar() {
        Alert confirmacion = new Alert(AlertType.CONFIRMATION);
        confirmacion.setTitle("Confirmar cancelación");
        confirmacion.setHeaderText("¿Desea cancelar el formulario?");
        confirmacion.setContentText("Se perderán todos los datos no guardados.");

        if (confirmacion.showAndWait().orElse(null) == ButtonType.OK) {
            volverAlDashboard();
        }
    }

    private boolean validarFormulario() {
        StringBuilder errores = new StringBuilder();

        if (estudianteComboBox.getValue() == null) {
            errores.append("• Debe seleccionar un estudiante\n");
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
            mostrarAlerta("Errores en el formulario", errores.toString(), AlertType.WARNING);
            return false;
        }

        return true;
    }

    private boolean procesarEnvioFormatoA() {
        // Implementar lógica real de envío al backend
        // 1. Crear DTO con datos del formulario
        // 2. Llamar servicio para guardar en BD
        // 3. Subir archivos
        // 4. Enviar notificación al coordinador (RF 2)
        return true; // Cambiar por resultado real
    }

    private void volverAlDashboard() {
        try {
            Stage stage = (Stage) tituloTextField.getScene().getWindow();
            Parent root = FXMLLoader.load(getClass().getResource("/org/example/fronted/views/dashboard-docente.fxml"));
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            mostrarAlerta("Error", "No se pudo regresar al dashboard: " + e.getMessage(), AlertType.ERROR);
        }
    }

    private void mostrarAlerta(String titulo, String mensaje, AlertType tipo) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }

    // ========== CLASE PARA AUTOMATICO DE ESTUDIANTES ==========
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
            return nombreCompleto + " - " + email + " (" + codigo + ")";
        }

        public boolean matches(String texto) {
            String textoLower = texto.toLowerCase();
            return nombreCompleto.toLowerCase().contains(textoLower) ||
                    email.toLowerCase().contains(textoLower) ||
                    codigo.toLowerCase().contains(textoLower);
        }

        @Override
        public String toString() {
            return getDisplayText();
        }
    }
}