package uk.ac.ebi.eva.countstats.controller;

import org.springframework.web.bind.annotation.*;
import uk.ac.ebi.eva.countstats.model.Count;
import uk.ac.ebi.eva.countstats.service.CountService;

import java.util.List;

@RestController
@RequestMapping("/v1/countstats")
public class CountController {
    private CountService countService;

    public CountController(CountService countService) {
        this.countService = countService;
    }

    @PostMapping("count")
    public void saveCount(@RequestBody Count count) {
        countService.saveCount(count);
    }

    @GetMapping("count")
    public List<Count> getCount() {
        return countService.getAllCounts();
    }
}
