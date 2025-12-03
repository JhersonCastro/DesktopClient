package org.example.fronted.controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import org.example.fronted.api.ProjectApi;
import org.example.fronted.dto.SubirFormatoADTO;
import org.example.fronted.dto.SubirFormatoAResponseDTO;
import org.example.fronted.util.SessionManager;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
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

    // Fachada al microservicio de proyectos
    private final ProjectApi projectApi = new ProjectApi();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        sessionManager = SessionManager.getInstance();

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

        Tooltip tooltip = new Tooltip("Escriba nombre, email o código y presione ENTER para buscar");
        estudianteTextField.setTooltip(tooltip);
    }

    private void buscarEstudiantesEnServidor(String busqueda) {
        resultadosBusquedaContainer.setVisible(true);
        resultadosBusquedaContainer.setManaged(true);

        resultadosList.getChildren().clear();

        Label loadingLabel = new Label("Buscando estudiantes...");
        loadingLabel.setStyle("-fx-text-fill: #7f8c8d; -fx-font-style: italic;");
        resultadosList.getChildren().add(loadingLabel);

        new Thread(() -> {
            try {
                Thread.sleep(500); // Simulación

                List<EstudianteAutoComplete> resultados = new ArrayList<>();

                if (busqueda.toLowerCase().contains("juan") || busqueda.contains("perez")) {
                    resultados.add(new EstudianteAutoComplete(
                            "Juan Carlos Pérez", "201810123", "jcperez@unicauca.edu.co", "Ingeniería de Sistemas"
                    ));
                    resultados.add(new EstudianteAutoComplete(
                            "Juan David Martínez", "201810456", "jdmartinez@unicauca.edu.co", "Ingeniería Electrónica"
                    ));
                }

                if (busqueda.toLowerCase().contains("maria")) {
                    resultados.add(new EstudianteAutoComplete(
                            "María Fernanda Gómez", "201810789", "mfgomez@unicauca.edu.co", "Automática Industrial"
                    ));
                }

                if (busqueda.contains("@")) {
                    resultados.add(new EstudianteAutoComplete(
                            "Estudiante por Email", "201810999", busqueda, "Tecnología en Telemática"
                    ));
                }

                Platform.runLater(() -> mostrarResultadosBusqueda(resultados, busqueda));

            } catch (InterruptedException e) {
                e.printStackTrace();
                Platform.runLater(() -> mostrarErrorBusqueda("Error en la búsqueda: " + e.getMessage()));
            }
        }).start();
    }

    private void mostrarResultadosBusqueda(List<EstudianteAutoComplete> resultados, String busqueda) {
        resultadosList.getChildren().clear();

        if (resultados.isEmpty()) {
            Label noResultsLabel = new Label("No se encontraron estudiantes para: \"" + busqueda + "\"");
            noResultsLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-style: italic;");
            resultadosList.getChildren().add(noResultsLabel);
            return;
        }

        for (EstudianteAutoComplete estudiante : resultados) {
            Button resultButton = new Button();
            resultButton.setMaxWidth(Double.MAX_VALUE);
            resultButton.setStyle(
                    "-fx-background-color: white; -fx-border-color: #ddd; -fx-border-width: 1; " +
                            "-fx-text-alignment: left; -fx-alignment: CENTER_LEFT; -fx-padding: 10px;"
            );

            VBox content = new VBox(3);

            Label nombreLabel = new Label("Nombre: " + estudiante.getNombreCompleto());
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

            resultButton.setOnAction(e -> seleccionarEstudiante(estudiante));

            resultadosList.getChildren().add(resultButton);
        }

        Label countLabel = new Label("Encontrados " + resultados.size() + " estudiantes");
        countLabel.setStyle("-fx-text-fill: #27ae60; -fx-font-size: 12px; -fx-padding: 5 0 0 0;");
        resultadosList.getChildren().add(countLabel);
    }

    private void seleccionarEstudiante(EstudianteAutoComplete estudiante) {
        this.estudianteSeleccionado = estudiante;
        estudianteTextField.setText(estudiante.getDisplayText());
        resultadosBusquedaContainer.setVisible(false);
        resultadosBusquedaContainer.setManaged(false);

        mostrarMensaje("Estudiante seleccionado: " + estudiante.getNombreCompleto(), Alert.AlertType.INFORMATION);
    }

    private void mostrarErrorBusqueda(String error) {
        resultadosList.getChildren().clear();
        Label errorLabel = new Label(error);
        errorLabel.setStyle("-fx-text-fill: #e74c3c;");
        resultadosList.getChildren().add(errorLabel);
    }

    private void cargarDatosIniciales() {
        codirectorComboBox.getItems().addAll(
                "Ninguno",
                "Dra. María Fernández",
                "Mg. Luis Martínez",
                "Dr. Jorge Hernández"
        );
        codirectorComboBox.getSelectionModel().select(0);
    }

    private void configurarListeners() {
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

        // Ejecutar el envío en un hilo aparte para no bloquear la UI
        new Thread(() -> {
            boolean exito = procesarEnvioFormatoA();

            Platform.runLater(() -> {
                if (exito) {
                    mostrarAlerta("Éxito", "Formato A enviado correctamente. Se ha notificado al coordinador.", Alert.AlertType.INFORMATION);
                    regresarAlDashboard();
                } else {
                    mostrarAlerta("Error", "No se pudo enviar el Formato A. Intente nuevamente.", Alert.AlertType.ERROR);
                }
            });
        }).start();
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

    /**
     * Ahora sí, implementación real: llama a ProjectApi.subirFormatoA(...)
     */
    private boolean procesarEnvioFormatoA() {
        try {
            SubirFormatoADTO dto = new SubirFormatoADTO();

            dto.setTitulo(tituloTextField.getText().trim());
            dto.setModalidad(radioInvestigacion.isSelected() ? "INVESTIGACION" : "PRACTICA_PROFESIONAL");

            // Director: el docente logueado
            // Ajusta el getter según tu SessionManager (getEmail/getUserEmail)
            dto.setDirectorEmail(sessionManager.getEmail());

            // Codirector: por ahora mandamos null si es "Ninguno"
            String codirectorSeleccionado = codirectorComboBox.getValue();
            if (codirectorSeleccionado != null && !codirectorSeleccionado.equalsIgnoreCase("Ninguno")) {
                // Aquí idealmente deberías mapear el nombre a un email real.
                // Por ahora lo dejamos null o un valor dummy si tu backend lo requiere email.
                dto.setCodirectorEmail(null);
            }

            dto.setEstudiante1Email(estudianteSeleccionado.getEmail());
            dto.setPdfFormatoA(archivoPdfSeleccionado);
            dto.setCartaAceptacion(cartaAceptacionSeleccionada);

            // Llamada al microservicio (bloqueante, pero en hilo aparte)
            SubirFormatoAResponseDTO resp = projectApi.subirFormatoA(dto).block();

            if (resp != null && resp.getIdProyecto() != null) {
                System.out.println("Formato A enviado. ID proyecto = " + resp.getIdProyecto());
                return true;
            } else {
                return false;
            }

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
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
