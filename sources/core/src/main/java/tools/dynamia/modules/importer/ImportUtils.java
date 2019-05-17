
package tools.dynamia.modules.importer;

import org.apache.poi.ss.usermodel.*;
import tools.dynamia.commons.BeanUtils;
import tools.dynamia.domain.ValidationError;
import tools.dynamia.integration.ProgressMonitor;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author Mario Serrano Leones
 */
public class ImportUtils {

    public static String getCellValue(Row row, int cellIndex) {
        String value = null;
        Cell cell = row.getCell(cellIndex);

        if (cell != null) {
            if (cell.getCellTypeEnum() == CellType.ERROR) {
                value = null;
            } else if (cell.getCellTypeEnum() == CellType.NUMERIC) {
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

    public static Object getCellValueObject(Row row, int cellIndex) {
        Cell cell = row.getCell(cellIndex);

        if (cell != null) {
            CellType type = cell.getCellTypeEnum();
            switch (type) {
                case BLANK:
                    return null;
                case BOOLEAN:
                    return cell.getBooleanCellValue();
                case STRING:
                    return cell.getStringCellValue();
                case NUMERIC:
                    return cell.getNumericCellValue();
                case FORMULA:
                    String formula = cell.getCellFormula();
                    if ("TRUE()".equalsIgnoreCase(formula)) {
                        return Boolean.TRUE;
                    } else if ("FALSE()".equalsIgnoreCase(formula)) {
                        return Boolean.FALSE;
                    } else {
                        return cell.getCellFormula();
                    }
                default:
                    return cell.getStringCellValue();
            }
        }
        return null;
    }

    public static Date getCellValueDate(Row row, int cellIndex) {
        try {
            Cell cell = row.getCell(cellIndex);
            if (cell != null) {
                return cell.getDateCellValue();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static <T> List<T> importExcel(Class<T> clazz, InputStream excelFile, ProgressMonitor monitor,
                                          ImportBeanParser<T> parser) throws Exception {
        if (monitor == null) {
            monitor = new ProgressMonitor();
        }

        Workbook workbook = WorkbookFactory.create(excelFile);
        Sheet sheet = workbook.getSheetAt(0);
        monitor.setMax(sheet.getLastRowNum());
        List<T> lineas = new ArrayList<>();
        int filasOK = 0;
        for (Row row : sheet) {
            if (row.getRowNum() == 0) {
                monitor.setMessage("Procesando Encabezados");
            } else {
                try {
                    T bean = parser.parse(row);
                    if (bean != null) {
                        if (!lineas.contains(bean)) {
                            lineas.add(bean);
                        }
                        filasOK++;
                        monitor.setMessage("Fila " + row.getRowNum() + " importada Ok");
                    }
                } catch (ValidationError validationError) {
                    monitor.setMessage(
                            "Error importando fila " + row.getRowNum() + ". " + validationError.getMessage());
                }
            }
            monitor.setCurrent(row.getRowNum());
        }

        return lineas;
    }

    public static void readExcel(InputStream excelFile, ProgressMonitor monitor, ImportReader reader) throws Exception {

        if (monitor == null) {
            monitor = new ProgressMonitor();
        }

        Workbook workbook = WorkbookFactory.create(excelFile);
        Sheet sheet = workbook.getSheetAt(0);
        monitor.setMax(sheet.getLastRowNum());

        int filasOK = 0;
        for (Row row : sheet) {
            if (row.getRowNum() == 0) {
                monitor.setMessage("Procesando Encabezados");
            } else {
                try {
                    reader.read(row);
                    filasOK++;
                    monitor.setMessage("Fila " + row.getRowNum() + " importada Ok");
                } catch (ValidationError error) {
                    monitor.setMessage(
                            "Error importando fila " + row.getRowNum() + ". " + error.getMessage());
                    throw error;
                }
            }
            monitor.setCurrent(row.getRowNum());
        }
    }

    public static void tryToParse(Row row, Object bean, String... fields) {
        if (fields != null && fields.length > 0) {
            for (int i = 0; i < fields.length; i++) {
                try {
                    String fieldName = fields[i];
                    if (fieldName != null && !fieldName.isEmpty()) {
                        BeanUtils.setFieldValue(fieldName, bean, getCellValueObject(row, i));
                    }
                } catch (Exception e) {
                    // TODO: handle exception
                }
            }
        }
    }

    public static Cell findFirstRowCellByName(Sheet sheet, String columnName) {


        for (Cell cell : sheet.getRow(0)) {
            if (columnName.equalsIgnoreCase(cell.getStringCellValue())) {
                return cell;
            }
        }

        return null;

    }

    /**
     * Get cell value using first row as column name
     *
     * @param row
     * @param columnName
     * @return
     */
    public static String getCellValue(Row row, String columnName) {
        Cell cell = findFirstRowCellByName(row.getSheet(), columnName);
        if (cell != null) {
            return getCellValue(row, cell.getColumnIndex());
        } else {
            return null;
        }

    }

    /**
     * Get cell value using first row as column name
     *
     * @param row
     * @param columnName
     * @return
     */
    public static Object getCellValueObject(Row row, String columnName) {
        Cell cell = findFirstRowCellByName(row.getSheet(), columnName);
        if (cell != null) {
            return getCellValueObject(row, cell.getColumnIndex());
        } else {
            return null;
        }
    }

    /**
     * Get cell date value using first row as column name
     *
     * @param row
     * @param columnName
     * @return
     */
    public static Date getCellValueDate(Row row, String columnName) {
        Cell cell = findFirstRowCellByName(row.getSheet(), columnName);
        if (cell != null) {
            return getCellValueDate(row, cell.getColumnIndex());
        } else {
            return null;
        }
    }

}
