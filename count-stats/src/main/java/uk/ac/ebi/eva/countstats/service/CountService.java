package uk.ac.ebi.eva.countstats.service;

import org.springframework.stereotype.Service;
import uk.ac.ebi.eva.countstats.model.Count;
import uk.ac.ebi.eva.countstats.repository.CountRepository;

@Service
public class CountService {
    private CountRepository countRepository;

    public CountService(CountRepository countRepository) {
        this.countRepository = countRepository;
    }

    public Count saveCount(Count count) {
        return countRepository.save(count);
    }

    public Iterable<Count> getAllCounts() {
        return countRepository.findAll();
    }

    public Long getCountForProcess(String process) {
        return countRepository.getCountForProcess(process);
    }
}