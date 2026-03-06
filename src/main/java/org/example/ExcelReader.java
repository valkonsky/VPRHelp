package org.example;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ExcelReader implements Reader{
    public List<Cell> getStudents() {
        return students;
    }

    private List<Cell> students;
    private final Sheet sheet;

    public ExcelReader(String filename) throws IOException {
        try(FileInputStream fileInputStream = new FileInputStream(filename);
            Workbook workbook = new XSSFWorkbook(fileInputStream)) {
            sheet = workbook.getSheetAt(1);
            students = new ArrayList<>();
        }
    }
    public void read() {
            for (Row row : sheet) {
                for (Cell cell : row) {

                    switch (cell.getCellType()) {
                        case STRING:
                            System.out.print(cell.getStringCellValue() + " ");
                            break;

                        case NUMERIC:
                            System.out.print(cell.getNumericCellValue() + " ");
                            break;

                        case BOOLEAN:
                            System.out.print(cell.getBooleanCellValue() + " ");
                            break;

                        default:
                            System.out.print(" ");
                    }

                }
                System.out.println();
            }
    }

    public void initStudentsInExam(){
        for (Row row : sheet){
            if ((row.getCell(0).getCellType()== CellType.BLANK)) {
                break;
            }else if((row.getCell(0).getCellType()== CellType.STRING)){
                continue;
                }
            else{
                students.add(row.getCell(0));
            }
        }
    }

    public void initMarksOfStudents(){
        for (Row row: sheet){
            if(students.contains(row.getCell(0))){
              for (Cell cell:row){
                  System.out.print(cell);
              }
            }
        }
    }


}
