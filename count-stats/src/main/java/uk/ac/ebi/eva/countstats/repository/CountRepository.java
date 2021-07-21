package uk.ac.ebi.eva.countstats.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;
import uk.ac.ebi.eva.countstats.model.Count;

import java.math.BigInteger;
import java.sql.Types;
import java.util.List;

@Repository
public class CountRepository {
    @Autowired
    private NamedParameterJdbcOperations namedParameterJdbcOperations;

    public int saveCount(Count count) {
        String sql = "insert into process_count_metric values (:process, :identifier, :metric, :count)";

        SqlParameterSource sqlParameterSource = new MapSqlParameterSource("process", count.getProcess())
                .addValue("identifier", count.getIdentifier(), Types.OTHER)
                .addValue("metric", count.getMetric())
                .addValue("count", count.getCount(), Types.BIGINT);

        return namedParameterJdbcOperations.update(sql, sqlParameterSource);
    }

    public List<Count> getAllCounts() {
        String sql = "select * from process_count_metric";

        return namedParameterJdbcOperations.query(sql, (rs, rownu) -> new Count(rs.getString("process"),
                rs.getString("identifier"), rs.getString("metric"), rs.getObject("count", BigInteger.class))
        );
    }
}
