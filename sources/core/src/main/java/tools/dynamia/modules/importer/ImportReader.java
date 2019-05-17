package tools.dynamia.modules.importer;

import org.apache.poi.ss.usermodel.Row;

@FunctionalInterface
public interface ImportReader {

	void read(Row row);

}
