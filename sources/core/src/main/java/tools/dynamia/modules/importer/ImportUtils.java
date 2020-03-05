package tools.dynamia.modules.importer;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellReference;
import tools.dynamia.commons.BeanUtils;
import tools.dynamia.commons.logger.LoggingService;
import tools.dynamia.commons.logger.SLF4JLoggingService;
import tools.dynamia.domain.ValidationError;
import tools.dynamia.domain.Validator;
import tools.dynamia.integration.ProgressMonitor;

import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * @author Mario Serrano Leones
 */
public class ImportUtils {

    private final static LoggingService LOGGER = new SLF4JLoggingService(ImportUtils.class);

    public static String getCellValue(Row row, int cellIndex) {
        String value = null;
        Cell cell = row.getCell(cellIndex);

        if (cell != null) {
            if (cell.getCellType() == CellType.ERROR) {
                value = null;
            } else if (cell.getCellType() == CellType.NUMERIC) {
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
            CellType type = cell.getCellType();
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

    /**
     * Find cell using excel coordinator like A1 or D30
     *
     * @param sheet
     * @param coordinate
     * @return
     */
    public static Cell findCellByCoordinate(Sheet sheet, String coordinate) {
        try {
            CellReference ref = new CellReference(coordinate);
            Row row = sheet.getRow(ref.getRow());
            if (row != null) {
                return row.getCell(ref.getCol());
            }
        } catch (IllegalArgumentException e) {
            //ignore
        }
        return null;
    }

    public static void setCellValue(Cell cell, Object value) {
        if (cell != null && value != null) {
            if (value instanceof String) {
                cell.setCellValue((String) value);
            } else if (value instanceof Boolean) {
                cell.setCellValue(((Boolean) value));
            } else if (value instanceof Number) {
                cell.setCellValue(((Number) value).doubleValue());
            } else if (value instanceof Date) {
                cell.setCellValue((Date) value);
            } else if (value instanceof Calendar) {
                cell.setCellValue((Calendar) value);
            } else {
                cell.setCellValue(value.toString());
            }
        }
    }


    public static BigDecimal parseBigDecimal(Row row, int cellIndex, BigDecimal defaultValue, String message) {
        try {
            Object value = ImportUtils.getCellValueObject(row, cellIndex);
            if (value != null) {
                if (value instanceof Number) {
                    double precioVta = ((Number) value).doubleValue();
                    return BigDecimal.valueOf(precioVta);
                } else {
                    return new BigDecimal(value.toString());
                }
            }
        } catch (Exception e) {
            LOGGER.error("Error parsing BigDecimal: " + message + "  Location: " + row.getRowNum() + "  / " + cellIndex, e);
        }
        return defaultValue;
    }

    public static boolean parseBoolean(Row row, int cellIndex, boolean defaultValue, String message) {
        try {
            Object value = ImportUtils.getCellValueObject(row, cellIndex);
            return value.toString().equalsIgnoreCase("si") || value.toString().equalsIgnoreCase("1") || value.toString().equalsIgnoreCase("true") || value.toString().equalsIgnoreCase("yes");
        } catch (Exception e) {
            LOGGER.error("Error parsing Boolean: " + message + "  Location: " + row.getRowNum() + "  / " + cellIndex, e);
        }
        return defaultValue;
    }

    public static double parseDouble(Row row, int cellIndex, double defaultValue, String message) {
        try {
            Object value = ImportUtils.getCellValueObject(row, cellIndex);
            if (value != null) {
                if (value instanceof Number) {
                    return ((Number) value).doubleValue();
                } else {
                    return Double.parseDouble(value.toString());
                }
            }
        } catch (Exception e) {
            LOGGER.error("Error parsing Double: " + message + "  Location: " + row.getRowNum() + "  / " + cellIndex, e);
        }
        return defaultValue;
    }

    public static int parseInt(Row row, int cellIndex, int defaultValue, String message) {
        try {
            Object value = ImportUtils.getCellValueObject(row, cellIndex);
            if (value != null) {
                if (value instanceof Number) {
                    return ((Number) value).intValue();
                } else {
                    return Integer.parseInt(value.toString());
                }
            }
        } catch (Exception e) {
            LOGGER.error("Error parsing Int: " + message + "  Location: " + row.getRowNum() + "  / " + cellIndex, e);
        }
        return defaultValue;
    }

    public static String parseString(Row row, int cellIndex, String defaultValue, String message) {
        try {
            Object value = ImportUtils.getCellValueObject(row, cellIndex);
            if (value != null) {
                if (value instanceof String) {
                    return (String) value;
                } else if (value instanceof Number) {
                    return String.valueOf(((Number) value).longValue());
                } else {
                    return value.toString();
                }
            }
        } catch (Exception e) {
            LOGGER.error("Error parsing String: " + message + "  Location: " + row.getRowNum() + "  / " + cellIndex, e);
        }
        return defaultValue;
    }

    public static Date parseDate(Row row, int cellIndex, Date defaultValue, String message) {
        try {
            Object value = ImportUtils.getCellValueObject(row, cellIndex);
            if (value != null) {
                if (value instanceof Date) {
                    return (Date) value;
                } else if (value instanceof Number) {
                    return DateUtil.getJavaDate(((Number) value).doubleValue());
                }
            }
        } catch (Exception e) {
            LOGGER.error("Error parsing Date: " + message + "  Location: " + row.getRowNum() + "  / " + cellIndex, e);
        }
        return defaultValue;
    }
}
