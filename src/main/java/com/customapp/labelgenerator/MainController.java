package com.customapp.labelgenerator;

import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.VerticalAlignment;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import java.nio.file.Files;
import java.nio.file.Paths;

public class MainController {
    @FXML
    private TextField fileNameField;
    @FXML
    private TextField numberOfCartonField;
    @FXML
    private TextField orderQuantityField;
    @FXML
    private Label numberOfCartonError;
    @FXML
    private Label orderQuantityError;
    @FXML
    private Label generalErrorMsg;

    @FXML
    protected void onGenerateBtnClick() {
        String fileName = fileNameField.getText().trim();
        String numberOfCartonStr = numberOfCartonField.getText().trim();
        String orderQuantityStr = orderQuantityField.getText().trim();

        if (fileName.length() == 0 || numberOfCartonStr.length() == 0 || orderQuantityStr.length() == 0) {
            //Print to general error message and highlight message
            generalErrorMsg.setText("Please Fill Out the Form Completely");
            generalErrorMsg.setTextFill(Color.web("#ff0000", 0.8));
            return;
        }

        //remove previous error message if set
        generalErrorMsg.setText("");
        generalErrorMsg.setStyle(null);

        //Initialize numberOfCarton and orderQuantity
        int numberOfCarton;
        int orderQuantity;

        //Validate if numberOfCarton and orderQuantity are integer
        if (isInt(numberOfCartonStr) && isInt(orderQuantityStr)) {
            clearHighlight(numberOfCartonField, numberOfCartonError);
            clearHighlight(orderQuantityField, orderQuantityError);

            numberOfCarton = Integer.parseInt(numberOfCartonStr);
            orderQuantity = Integer.parseInt(orderQuantityStr);
        } else {
            if (!isInt(numberOfCartonStr)) highlightError(numberOfCartonField, numberOfCartonError, "Number Only");
            if (!isInt(orderQuantityStr)) highlightError(orderQuantityField, orderQuantityError, "Number Only");
            return;
        }

        //Validate if numberOfCarton and orderQuantity are positive
        if (numberOfCarton < 0 || orderQuantity < 0) {
            //Set error message and highlight textfield if not
            if (numberOfCarton < 0) highlightError(numberOfCartonField, numberOfCartonError, "Number Must Be Positive");
            if (orderQuantity < 0) highlightError(orderQuantityField, orderQuantityError, "Number Must Be Positive");
            return;
        } else {
            //Clear error message and styling on textfield
            clearHighlight(numberOfCartonField, numberOfCartonError);
            clearHighlight(orderQuantityField, orderQuantityError);
        }

        //generating label
        labelGenerator(fileName, numberOfCarton, orderQuantity);
    }

    /**
     * Validate if input is an integer
     *
     * @param input value from textfield
     * @return boolean
     */
    private boolean isInt(String input) {
        try {
            Integer.parseInt(input);
            return true;
        } catch (Exception e) {
            //exception
            return false;
        }
    }

    /**
     * Highlight the textfield and print error message
     *
     * @param textField    the desired textfield
     * @param errorLabel   the desired error label
     * @param errorMessage the desired error message to be printed
     */
    private void highlightError(TextField textField, Label errorLabel, String errorMessage) {
        //Format error label
        errorLabel.setText(errorMessage);
        errorLabel.setTextFill(Color.web("#ff0000", 0.8));

        //Highlight text field
        textField.setStyle("-fx-border-color:red ; -fx-border-width: 2px ;");
    }

    /**
     * Clear error styling on textfield and error message
     *
     * @param textField  textfield to be cleared
     * @param errorLabel error message to be cleared
     */
    private void clearHighlight(TextField textField, Label errorLabel) {
        //Clear error message and styling on textfield
        errorLabel.setText("");
        textField.setStyle(null);
    }

    /**
     * This function will take in data and write to PDF file
     *
     * @param fileName       input filename
     * @param numberOfCarton number of boxes
     * @param orderQuantity  number of labels per box
     */
    private void labelGenerator(String fileName, int numberOfCarton, int orderQuantity) {
        try {
            if (numberOfCarton <= 0 || orderQuantity <= 0) {
                //Print to general error message and highlight message
                generalErrorMsg.setText("Invalid input");
                generalErrorMsg.setTextFill(Color.web("#ff0000", 0.8));
                return;
            }

            Files.createDirectories(Paths.get(".\\Pdf_Labels"));
            String path = ".\\Pdf_Labels\\" + fileName + ".pdf";

            //Pdf file configuration
            PdfWriter pdfWriter = new PdfWriter(path.trim());
            PdfDocument pdfDocument = new PdfDocument(pdfWriter);
            Document document = new Document(pdfDocument);
            float docMargin = 15f;
            document.setLeftMargin(docMargin);
            document.setRightMargin(docMargin);

            //Table configuration
            float width = 200f;
            float[] columnWidth = {width, width, width};
            Table table = new Table(columnWidth);

            //Analyze and calculate how labels should be printed
            int numberOfLoop = orderQuantity / 3; //each row has 3 label
            int remainder = orderQuantity % 3 == 0 ? 0 : orderQuantity % 3;
            int labelQuantity = 1;

            //Main loop
            for (int i = 0; i < numberOfLoop; i++) {
                for (int j = 1; j <= numberOfCarton; j++) {
                    for (int z = labelQuantity; z < labelQuantity + 3; z++) {
                        generateContent(table, numberOfCarton, j, orderQuantity, z);
                    }
                }
                labelQuantity += 3;
            }

            //Remainder loop
            if (remainder == 1) {
                for (int j = 1; j <= numberOfCarton; j++) {
                    for (int z = labelQuantity; z < labelQuantity + 3; z++) {
                        if (z <= orderQuantity) {
                            generateContent(table, numberOfCarton, j, orderQuantity, z);
                        }
                    }

                    //Create empty cell
                    table.addCell(new Cell().add(new Paragraph("")));
                    table.addCell(new Cell().add(new Paragraph("")));
                }
            } else if (remainder == 2) {
                for (int j = 1; j <= numberOfCarton; j++) {
                    for (int z = labelQuantity; z < labelQuantity + 3; z++) {
                        if (z <= orderQuantity) {
                            generateContent(table, numberOfCarton, j, orderQuantity, z);
                        }
                    }

                    //Create empty cell
                    table.addCell(new Cell().add(new Paragraph("")));
                }
            }

            //Add table to the document
            document.add(table);

            //Close document after all content has been successfully added
            document.close();

            System.out.println("Pdf created successfully");
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    /**
     * This function will generate content inside a table cell and format the cell
     *
     * @param table          targeted table
     * @param numberOfCarton the input number of cartons
     * @param carton         carton count
     * @param orderQuantity  the input order quantity
     * @param quantity       quantity count
     */
    private void generateContent(Table table, int numberOfCarton, int carton, int orderQuantity, int quantity) {
        try {
            Cell tableCell = new Cell();

            //Table content
            tableCell.add(new Paragraph("Qty/Ctn: 1 PC / " + numberOfCarton + " Cartons"));
            tableCell.add(new Paragraph("Carton No: Box " + carton + " of " + numberOfCarton));
            tableCell.add(new Paragraph("Order Qty: " + quantity + " of " + orderQuantity));

            //Content format
            float fontSize = 15f;
            float cellHeight = 105f;
            PdfFont font = PdfFontFactory.createFont(StandardFonts.TIMES_ROMAN);

            //Table format
            tableCell
                    .setFont(font)
                    .setFontSize(fontSize)
                    .setPaddingLeft(10f)
                    .setHeight(cellHeight)
                    .setVerticalAlignment(VerticalAlignment.MIDDLE);

            table.addCell(tableCell);
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    /**
     * This function is used for developing and testing new functionalities
     */
    private void dummy() {
        try {
            System.out.println("****Entering Dummy Function****");

            String path = "/media/storage/data_drive/sample.pdf";

            PdfWriter pdfWriter = new PdfWriter(path);
            PdfDocument pdfDocument = new PdfDocument(pdfWriter);
            Document document = new Document(pdfDocument);

            float width = 200f;
            float[] columnWidth = {width, width, width};
            Table table = new Table(columnWidth);


            Cell cell_11 = new Cell();
            cell_11.add(new Paragraph("Item"));
            table.addCell(cell_11);

            table.addCell(new Cell().add(new Paragraph("Qty")));
            table.addCell(new Cell().add(new Paragraph("Available")));


            for (int i = 0; i < 60; i++) {
                Cell cell = new Cell().setPaddings(2f, 0, 2f, 1f);
                cell.add(new Paragraph("Qty/Ctn: 1 PC / 5 Cartons"));
                cell.add(new Paragraph("Carton No: Box 1 of 5"));
                cell.add(new Paragraph("Order Qty: 1 of 57"));

                table.addCell(cell);
            }

            document.add(table);
            document.close();

            System.out.println("Pdf created successfully");
            System.out.println("****Existing Dummy Function****");
        } catch (Exception e) {
            System.out.println(e);
        }
    }
}