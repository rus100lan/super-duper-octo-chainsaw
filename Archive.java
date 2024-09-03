import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import java.util.zip.ZipInputStream;
import java.util.zip.Deflater;

public class Archiver extends Application {

    private static final Logger logger = Logger.getLogger(Archiver.class.getName());
    private TextArea logTextArea;

    @Override
    public void start(Stage primaryStage) {
        // Create the interface
        VBox root = new VBox(10);
        root.setPadding(new Insets(10));

        Label label = new Label("Архиватор");
        root.getChildren().add(label);

        HBox fileBox = new HBox(5);
        fileBox.getChildren().add(new Label("Введите имя файла:"));
        TextField textField = new TextField();
        textField.setPromptText("Введите имя файла");
        fileBox.getChildren().add(textField);
        Button chooseFileButton = new Button("Выбрать файл");
        chooseFileButton.setOnAction(event -> {
            FileChooser fileChooser = new FileChooser();
            File selectedFile = fileChooser.showOpenDialog(primaryStage);
            if (selectedFile != null) {
                textField.setText(selectedFile.getAbsolutePath());
            }
        });
        fileBox.getChildren().add(chooseFileButton);
        root.getChildren().add(fileBox);

        HBox compressionBox = new HBox(5);
        compressionBox.getChildren().add(new Label("Уровень сжатия:"));
        ComboBox<String> compressionComboBox = new ComboBox<>();
        compressionComboBox.getItems().addAll("BEST_COMPRESSION", "BEST_SPEED", "DEFAULT_COMPRESSION");
        compressionComboBox.setValue("BEST_COMPRESSION");
        compressionBox.getChildren().add(compressionComboBox);
        root.getChildren().add(compressionBox);

        HBox buttonBox = new HBox(5);
        Button archiveButton = new Button("Архивировать");
        archiveButton.setOnAction(event -> {
            // Archive the file
            String fileName = textField.getText();
            if (fileName.isEmpty()) {
                log("Имя файла не может быть пустым!");
                return;
            }
            File file = new File(fileName);
            if (file.exists()) {
                try (ZipOutputStream zipOutputStream = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(fileName + ".zip")))) {
                    zipOutputStream.putNextEntry(new ZipEntry(file.getName()));

                    // Create a Deflater for compression
                    int compressionLevel = getCompressionLevel(compressionComboBox.getValue());
                    zipOutputStream.setLevel(compressionLevel);

                    try (BufferedInputStream fileInputStream = new BufferedInputStream(new FileInputStream(file))) {
                        byte[] buffer = new byte[1024];
                        int bytesRead;
                        while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                            zipOutputStream.write(buffer, 0, bytesRead);
                        }
                    }
                    log("Файл успешно заархивирован!");
                } catch (IOException e) {
                    log("Ошибка при архивировании файла: " + e.getMessage());
                    logger.log(Level.SEVERE, "Ошибка при архивировании файла", e);
                }
            } else {
                log("Файл не найден!");
            }
        });
        buttonBox.getChildren().add(archiveButton);

        Button extractButton = new Button("Распаковать");
        extractButton.setOnAction(event -> {
            // Extract the file
            String fileName = textField.getText();
            if (fileName.isEmpty()) {
                log("Имя файла не может быть пустым!");
                return;
            }
            File file = new File(fileName + ".zip");
            if (file.exists()) {
                try (ZipInputStream zipInputStream = new ZipInputStream(new BufferedInputStream(new FileInputStream(file)))) {
                    ZipEntry zipEntry = zipInputStream.getNextEntry();
                    if (zipEntry != null) {
                        try (BufferedOutputStream fileOutputStream = new BufferedOutputStream(new FileOutputStream(zipEntry.getName()))) {
                            byte[] buffer = new byte[1024];
                            int bytesRead;
                            while ((bytesRead = zipInputStream.read(buffer)) != -1) {
                                fileOutputStream.write(buffer, 0, bytesRead);
                            }
                        }
                        log("Файл распакован успешно!");
                    } else {
                        log("Ошибка: пустой архив.");
                    }
                } catch (IOException e) {
                    log("Ошибка при распаковке файла: " + e.getMessage());
                    logger.log(Level.SEVERE, "Ошибка при распаковке файла", e);
                }
            } else {
                log("Архив не найден!");
            }
        });
        buttonBox.getChildren().add(extractButton);
        root.getChildren().add(buttonBox);

        logTextArea = new TextArea();
        logTextArea.setEditable(false);
        root.getChildren().add(logTextArea);

        Scene scene = new Scene(root, 300, 300);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private int getCompressionLevel(String compressionLevelString) {
        switch (compressionLevelString) {
            case "BEST_COMPRESSION":
                return Deflater.BEST_COMPRESSION;
            case "BEST_SPEED":
                return Deflater.BEST_SPEED;
            case "DEFAULT_COMPRESSION":
                return Deflater.DEFAULT_COMPRESSION;
            default:
                return Deflater.DEFAULT_COMPRESSION;
        }
    }

    private void log(String message) {
        logTextArea.appendText(message + "\n");
        logger.info(message);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
