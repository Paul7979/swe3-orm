package at.technikum.orm;

import at.technikum.orm.model.Entity;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.text.StringSubstitutor;

import java.sql.SQLException;
import java.util.*;

@Slf4j
public class Orm {

  private ConnectionFactory connectionFactory;

  public Orm(String url) throws SQLException {
    connectionFactory =  ConnectionFactory.of(url);
  }

  public void save(Object o) throws SQLException {
    var entity = Entity.ofClass(o.getClass());
    //var connection = connectionFactory.get();


    List<String> columnNames = new ArrayList<>(entity.getEntityFields().size());
    List<String> columnNamesWithoutPK = new ArrayList<>(entity.getEntityFields().size());
    List<Object> values = new ArrayList<>();
    List<Object> valuesWithoutPK = new ArrayList<>();

    entity.getEntityFields().forEach(entityField -> {
      if (entityField.isFK()) {
        return;
      }
      columnNames.add(entityField.getName());
      var object = entityField.toDbObject(entityField.getValue(o));
      if (!entityField.isPK()) {
        columnNamesWithoutPK.add(entityField.getName());
        valuesWithoutPK.add(object);
      }
      values.add(object);
    });

    Map<String, String> substituteValues = new HashMap<>();
    substituteValues.put("tableName", entity.getTableName());
    substituteValues.put("pkColumnName", entity.getPrimaryKey().getName());
    substituteValues.put("columNames", String.join(", ", columnNames));
    substituteValues.put("valuePlaceholder", createPlaceholders(columnNames));
    substituteValues.put("columnsWithoutPKWithPlaceholder", createColumnNamesWithPlaceholders(columnNamesWithoutPK));
    StringSubstitutor stringSubstitutor = new StringSubstitutor(substituteValues);

    var insertTemplate = """
      INSERT INTO ${tableName} (${columNames})
      VALUES (${valuePlaceholder})
      ON CONFLICT (${pkColumnName}) DO UPDATE SET ${columnsWithoutPKWithPlaceholder}""";

    values.addAll(valuesWithoutPK);
    var insertStatement = stringSubstitutor.replace(insertTemplate);
    log.info(insertStatement);
    try (
      var connection = connectionFactory.get();
      var preparedStatement = connection.prepareStatement(insertStatement)
    ) {
      for (int i = 0; i < values.size(); i++) {
        preparedStatement.setObject(i+1, values.get(i));
      }
      preparedStatement.execute();
    }
  }

  private String createColumnNamesWithPlaceholders(List<String> columnNamesWithoutPK) {
    return String.join(" = ?, ", columnNamesWithoutPK) + " = ?";
  }

  private String createPlaceholders(List<String> columnNames) {
    return String.join(", ", Collections.nCopies(columnNames.size(), "?"));
  }
}
