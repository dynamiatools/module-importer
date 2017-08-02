package tools.dynamia.modules.importacion;

import org.apache.poi.ss.usermodel.Row;

@FunctionalInterface
public interface ImportReader {

	void read(Row row);

}
