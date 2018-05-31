package tools.dynamia.modules.importacion;

import tools.dynamia.domain.Descriptor;
import tools.dynamia.domain.util.DomainUtils;

import java.io.Serializable;


public class ImportStatus implements Serializable {


    private long row;
    private boolean imported;
    private String message;
    private Serializable entityId;
    private String entityName;
    private Object entity;

    public ImportStatus(long row, boolean imported, String message, Object entity) {
        this.row = row;
        this.imported = imported;
        this.message = message;
        this.entity = entity;
        this.entityId = DomainUtils.findEntityId(entity);
        this.entityName = entity.toString();
    }

    public ImportStatus(long row, boolean imported, String message, Serializable entityId, String entityName) {
        this.row = row;
        this.imported = imported;
        this.message = message;
        this.entityId = entityId;
        this.entityName = entityName;
    }

    public ImportStatus(long row, boolean imported, String message) {
        this.row = row;
        this.imported = imported;
        this.message = message;
    }

    public long getRow() {
        return row;
    }

    public boolean isImported() {
        return imported;
    }

    public String getMessage() {
        return message;
    }

    public Serializable getEntityId() {
        return entityId;
    }

    public String getEntityName() {
        return entityName;
    }

    public Object getEntity() {
        return entity;
    }
}
