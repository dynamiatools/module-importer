package tools.dynamia.modules.importacion;

import org.apache.poi.ss.usermodel.Row;

@FunctionalInterface
public interface ImportBeanParser<T> {

	T parse(Row row);

}
