package com.egrub.scanner;

import com.egrub.scanner.model.upstox.Instrument;
import com.egrub.scanner.service.InstrumentsLoader;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.annotation.PostConstruct;
import java.util.List;

@SpringBootApplication
@Log4j2
public class ScannerApplication {

    public static void main(String[] args) {

        SpringApplication.run(ScannerApplication.class, args);
    }

    @PostConstruct
    public void load() {
        log.info("loading the instruments");
        List<Instrument> instrumentList = InstrumentsLoader.loadInstrumentsFromCsv();
        log.info("size: {}", instrumentList.size());
    }
}
