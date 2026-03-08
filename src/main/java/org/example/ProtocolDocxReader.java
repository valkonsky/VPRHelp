package org.example;

import org.apache.poi.xwpf.usermodel.IBodyElement;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class ProtocolDocxReader {

    public Map<String, String> read(String filePath) throws Exception {
        Map<String, String> result = new HashMap<String, String>();

        InputStream inputStream = new FileInputStream(filePath);
        try {
            XWPFDocument document = new XWPFDocument(inputStream);

            for (IBodyElement element : document.getBodyElements()) {
                if (element instanceof XWPFTable) {
                    readTable((XWPFTable) element, result);
                } else if (element instanceof XWPFParagraph) {
                    readParagraph((XWPFParagraph) element, result);
                }
            }

            return result;
        } finally {
            inputStream.close();
        }
    }

    private void readTable(XWPFTable table, Map<String, String> result) {
        for (XWPFTableRow row : table.getRows()) {
            String code = null;
            String name = null;

            for (XWPFTableCell cell : row.getTableCells()) {
                String text = normalize(cell.getText());
                if (text.isEmpty()) {
                    continue;
                }

                if (isStudentCode(text)) {
                    code = extractCode(text);
                } else if (looksLikeFullName(text)) {
                    name = text;
                }
            }

            if (code != null && name != null && !name.isEmpty()) {
                result.put(code, name);
            }
        }
    }

    private void readParagraph(XWPFParagraph paragraph, Map<String, String> result) {
        String text = normalize(paragraph.getText());
        if (text.isEmpty()) {
            return;
        }

        String[] parts = text.split("\\s+", 2);
        if (parts.length < 2) {
            return;
        }

        String first = parts[0];
        String second = parts[1].trim();

        if (isStudentCode(first) && looksLikeFullName(second)) {
            result.put(extractCode(first), second);
        }
    }

    private boolean isStudentCode(String text) {
        return extractCode(text) != null;
    }

    private String extractCode(String text) {
        if (text == null) {
            return null;
        }

        String normalized = text.replaceAll("[^0-9]", "");
        if (normalized.matches("\\d{5}")) {
            return normalized;
        }

        return null;
    }

    private boolean looksLikeFullName(String text) {
        if (text == null) {
            return false;
        }

        String normalized = text.trim();
        if (normalized.isEmpty()) {
            return false;
        }

        if (normalized.equalsIgnoreCase("ФИО учащегося")) {
            return false;
        }

        return normalized.matches("[А-Яа-яЁёA-Za-z\\-\\s]{6,}");
    }

    private String normalize(String text) {
        if (text == null) {
            return "";
        }
        return text.replace('\n', ' ')
                .replace('\r', ' ')
                .replaceAll("\\s+", " ")
                .trim();
    }
}