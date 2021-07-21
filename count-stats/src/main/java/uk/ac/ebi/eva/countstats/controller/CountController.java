package uk.ac.ebi.eva.countstats.controller;

import org.springframework.web.bind.annotation.*;
import uk.ac.ebi.eva.countstats.model.Count;
import uk.ac.ebi.eva.countstats.service.CountService;

@RestController
@RequestMapping("/v1/countstats")
public class CountController {
    private CountService countService;

    public CountController(CountService countService) {
        this.countService = countService;
    }

    @PostMapping("count")
    public Count saveCount(@RequestBody Count count) {
        return countService.saveCount(count);
    }

    @GetMapping("count")
    public Iterable<Count> getCount() {
        return countService.getAllCounts();
    }

    @GetMapping("count/{process}")
    public Long getCountForProcess(@PathVariable("process") String process, @RequestParam("study") String study) {
        return countService.getCountForProcess(process, study);
    }
}