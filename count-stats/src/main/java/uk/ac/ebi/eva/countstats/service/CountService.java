package uk.ac.ebi.eva.countstats.service;

import org.springframework.stereotype.Service;
import uk.ac.ebi.eva.countstats.model.Count;
import uk.ac.ebi.eva.countstats.repository.CountRepository;

import java.util.List;

@Service
public class CountService {
    private CountRepository countRepository;

    public CountService(CountRepository countRepository) {
        this.countRepository = countRepository;
    }

    public int saveCount(Count count) {
        return countRepository.saveCount(count);
    }

    public List<Count> getAllCounts() {
        return countRepository.getAllCounts();
    }

}
