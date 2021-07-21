package uk.ac.ebi.eva.countstats.model;

import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import javax.persistence.*;

@Data
@NoArgsConstructor
@Entity
@Table(name = "process_count_metric")
@TypeDef(name = "jsonb", typeClass = JsonBinaryType.class)
public class Count {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String process;
    @Type(type = "jsonb")
    @Column(columnDefinition = "jsonb")
    private String identifier;
    private String metric;
    private long count;
}
