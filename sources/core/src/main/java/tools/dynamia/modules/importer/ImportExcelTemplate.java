package tools.dynamia.modules.importer;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility class to parse and generate a new excel file using an excel template. Add variables using key as coordinates.
 * Like addVar("A1","Name")
 */
public class ImportExcelTemplate {

    private File excelTemplate;
    private Map<String, Object> variables = new HashMap<>();

    public ImportExcelTemplate(File excelTemplate) {
        this.excelTemplate = excelTemplate;
    }

    public void addVar(String cellName, Object value) {
        variables.put(cellName, value);
    }

    public void addVars(Map<String, Object> vars) {
        variables.putAll(vars);
    }

    public File parse(String outputname) throws IOException {

        Workbook workbook = WorkbookFactory.create(excelTemplate);
        Sheet sheet = workbook.getSheetAt(0);

        variables.forEach((k, v) -> {
            Cell cell = ImportUtils.findCellByCoordinate(sheet, k);
            ImportUtils.setCellValue(cell, v);
        });

        File outfile = File.createTempFile(outputname, ".xlsx");
        workbook.write(new FileOutputStream(outfile));
        workbook.close();

        return outfile;
    }


}
