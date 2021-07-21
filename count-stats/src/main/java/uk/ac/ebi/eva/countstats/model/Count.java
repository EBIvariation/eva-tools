package uk.ac.ebi.eva.countstats.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.TypeDefs;
import uk.ac.ebi.eva.countstats.configuration.StringJsonUserType;

import javax.persistence.*;

@Data
@NoArgsConstructor
@Entity
@Table(name = "process_count_metric")
@TypeDefs({@TypeDef(name = "StringJsonObject", typeClass = StringJsonUserType.class)})
public class Count {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String process;
    @Type(type = "StringJsonObject")
    private String identifier;
    private String metric;
    private long count;
}
