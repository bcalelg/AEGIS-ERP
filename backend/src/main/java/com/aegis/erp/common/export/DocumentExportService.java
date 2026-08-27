package com.aegis.erp.common.export;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;

@Service
public class DocumentExportService {
    public byte[] excel(
            String sheetName, List<String> headers, List<? extends List<?>> dataRows) {
        try (XSSFWorkbook workbook = new XSSFWorkbook();
                ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet(sheetName);
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            writeExcelRow(sheet.createRow(0), headers, headerStyle);
            for (int index = 0; index < dataRows.size(); index++) {
                writeExcelRow(sheet.createRow(index + 1), dataRows.get(index), null);
            }
            for (int index = 0; index < headers.size(); index++) {
                sheet.autoSizeColumn(index);
            }

            workbook.write(output);
            return output.toByteArray();
        } catch (IOException exception) {
            throw new UncheckedIOException("No fue posible generar el archivo Excel.", exception);
        }
    }

    public byte[] pdf(String title, List<String> headers, List<? extends List<?>> dataRows) {
        try (PDDocument document = new PDDocument();
                ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            PdfTableWriter writer = new PdfTableWriter(document, title, headers);
            for (List<?> row : dataRows) {
                writer.write(row);
            }
            writer.close();
            document.save(output);
            return output.toByteArray();
        } catch (IOException exception) {
            throw new UncheckedIOException("No fue posible generar el archivo PDF.", exception);
        }
    }

    private void writeExcelRow(Row row, List<?> values, CellStyle style) {
        for (int index = 0; index < values.size(); index++) {
            Cell cell = row.createCell(index);
            Object value = values.get(index);
            if (value instanceof Number number) {
                cell.setCellValue(number.doubleValue());
            } else {
                cell.setCellValue(value == null ? "" : value.toString());
            }
            if (style != null) {
                cell.setCellStyle(style);
            }
        }
    }

    private static final class PdfTableWriter {
        private static final float MARGIN = 42;
        private static final float ROW_HEIGHT = 20;
        private static final float FONT_SIZE = 9;
        private final PDDocument document;
        private final String title;
        private final List<String> headers;
        private final PDType1Font regular =
                new PDType1Font(Standard14Fonts.FontName.HELVETICA);
        private final PDType1Font bold =
                new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
        private PDPageContentStream content;
        private float y;

        private PdfTableWriter(PDDocument document, String title, List<String> headers)
                throws IOException {
            this.document = document;
            this.title = title;
            this.headers = headers;
            newPage();
        }

        private void write(List<?> values) throws IOException {
            if (y < MARGIN + ROW_HEIGHT) {
                newPage();
            }
            writeCells(values, regular);
        }

        private void newPage() throws IOException {
            closeContent();
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);
            content = new PDPageContentStream(document, page);
            y = page.getMediaBox().getHeight() - MARGIN;
            writeText(title, MARGIN, y, bold, 16);
            y -= 28;
            writeCells(headers, bold);
        }

        private void writeCells(List<?> values, PDType1Font font) throws IOException {
            float cellWidth = (PDRectangle.A4.getWidth() - (2 * MARGIN)) / headers.size();
            for (int index = 0; index < headers.size(); index++) {
                String value =
                        index < values.size() && values.get(index) != null
                                ? values.get(index).toString()
                                : "";
                writeText(
                        fit(value, cellWidth - 8, font),
                        MARGIN + (index * cellWidth) + 4,
                        y,
                        font,
                        FONT_SIZE);
            }
            y -= ROW_HEIGHT;
        }

        private String fit(String value, float width, PDType1Font font) throws IOException {
            String normalized = value.replace('\n', ' ').replace('\r', ' ');
            String fitted = normalized;
            while (!fitted.isEmpty()
                    && font.getStringWidth(fitted) / 1000 * FONT_SIZE > width) {
                fitted = fitted.substring(0, fitted.length() - 1);
            }
            if (fitted.length() < normalized.length() && fitted.length() > 3) {
                fitted = fitted.substring(0, fitted.length() - 3) + "...";
            }
            return fitted;
        }

        private void writeText(
                String value, float x, float positionY, PDType1Font font, float fontSize)
                throws IOException {
            content.beginText();
            content.setFont(font, fontSize);
            content.newLineAtOffset(x, positionY);
            content.showText(value);
            content.endText();
        }

        private void close() throws IOException {
            closeContent();
        }

        private void closeContent() throws IOException {
            if (content != null) {
                content.close();
            }
        }
    }
}
