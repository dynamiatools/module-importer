package tools.dynamia.modules.importer;

import org.apache.poi.ss.usermodel.Row;

@FunctionalInterface
public interface ImportBeanParser<T> {

	T parse(Row row);

}
