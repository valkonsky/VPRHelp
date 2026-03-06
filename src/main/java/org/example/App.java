package org.example;

import java.io.IOException;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {
        ExcelReader reader = null;
        try {
            reader = new ExcelReader("template.xlsx");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        reader.initStudentsInExam();
        reader.initMarksOfStudents();
        System.out.println(reader.getStudents());
    }
}
