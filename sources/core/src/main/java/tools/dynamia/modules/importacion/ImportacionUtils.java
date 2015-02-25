/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tools.dynamia.modules.importacion;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;

/**
 *
 * @author mario_2
 */
public class ImportacionUtils {

    public static String getCellValue(Row row, int cellIndex) {
        String value = null;
        Cell cell = row.getCell(cellIndex);
        if (cell != null) {
            if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
                DataFormatter df = new DataFormatter();
                value = df.formatCellValue(cell);
            } else {
                value = cell.getStringCellValue();
            }
        }
        if (value != null) {
            value = value.trim();
        }
        return value;

    }

}
