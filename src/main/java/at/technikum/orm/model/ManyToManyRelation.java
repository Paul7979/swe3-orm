package at.technikum.orm.model;

import lombok.*;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ManyToManyRelation {

  private String referenceTableName;

  private String referencedColumnName;
}
