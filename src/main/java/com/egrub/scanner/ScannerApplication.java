package com.egrub.scanner;

import com.egrub.scanner.model.upstox.Instrument;
import com.egrub.scanner.service.InstrumentsLoader;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.egrub.scanner.utils.Constants.VALID_INSTRUMENT;

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

        List<String> matches = fetchTodayInstruments();

        VALID_INSTRUMENT.addAll(instrumentList.stream()
                .filter(instrument ->
                        instrument.getMarketCapitalInCrores() != null &&
                                !instrument.getMarketCapitalInCrores().isNaN()) // Check if symbol matches
                .collect(Collectors.toList()));

        /*VALID_INSTRUMENT.addAll(instrumentList.stream()
                .filter(instrument -> matches.contains(instrument.getSymbol())) // Check if symbol matches
                .collect(Collectors.toList()));*/

        log.info("size: {}", instrumentList.size());
    }

    private static List<String> fetchTodayInstruments() {
        List<String> lookup = new ArrayList<>();
        lookup.add("SHAILY");
        lookup.add("SKYGOLD");
        lookup.add("KRISHANA");
        lookup.add("NDRAUTO");
        lookup.add("LUMAXTECH");
        lookup.add("APOLLO");
        lookup.add("INTERARCH");
        lookup.add("BANCOINDIA");
        lookup.add("MANORAMA");
        lookup.add("CARERATING");
        lookup.add("REFEX");
        lookup.add("BBOX");
        lookup.add("FACT");
        lookup.add("SHAKTIPUMP");
        lookup.add("APS");
        lookup.add("DYCL");
        lookup.add("RAMRAT");
        lookup.add("PENIND");
        lookup.add("PGIL");
        lookup.add("HLEGLAS");
        lookup.add("EIMCOELECO");
        lookup.add("TANFACIND");
        lookup.add("FIEMIND");
        lookup.add("KPEL");
        lookup.add("SERVOTECH");
        lookup.add("ORIANA");
        lookup.add("ARKADE");
        lookup.add("INSECTICID");
        lookup.add("LUMAXTECH");
        lookup.add("BIRLAMONEY");
        lookup.add("SAHANA");
        lookup.add("CSBBANK");
        lookup.add("INDRAMEDCO");
        lookup.add("TI");
        lookup.add("HPL");
        lookup.add("PRECWIRE");
        lookup.add("SUBROS");
        lookup.add("GOKULAGRO");
        lookup.add("AJMERA");
        lookup.add("VILAS");
        lookup.add("SIKA");
        lookup.add("FRONTSP");
        lookup.add("MANGCHEFER");
        lookup.add("AFCOM");
        lookup.add("RAJESH");
        lookup.add("CEINSYSTECH");
        lookup.add("PARAS");
        lookup.add("SHILCTECH");
        lookup.add("MARSONS");
        lookup.add("AGIIL");
        lookup.add("RPEL");
        lookup.add("MBAPL");
        lookup.add("AVALON");
        lookup.add("JSLL");
        lookup.add("ALPEXSOLAR");
        lookup.add("FMGOETZE");
        lookup.add("BALUFORGE");
        lookup.add("PREMEXPLN");
        lookup.add("GABRIEL");
        lookup.add("STEELCAS");
        lookup.add("AARTIPHARM");
        lookup.add("AVANTEL");
        lookup.add("TDPOWERSYS");
        lookup.add("SWARAJENG");
        lookup.add("VIMTALABS");
        lookup.add("VISHNU");
        lookup.add("SDBL");
        lookup.add("DHANUKA");
        lookup.add("POWERMECH");
        lookup.add("INDIASHLTR");
        lookup.add("WEL");
        lookup.add("TRANSRAILL");
        lookup.add("SPORTKING");
        lookup.add("SIRCA");
        lookup.add("JAGSNPHARM");
        lookup.add("SURYAROSNI");
        lookup.add("SYNCOMF");
        lookup.add("ONEPOINT");
        lookup.add("GOLDIAM");
        lookup.add("ZAGGLE");
        lookup.add("CAMLINFINE");
        lookup.add("STERTOOLS");
        lookup.add("TITAGARH");
        lookup.add("IRFC");
        lookup.add("INFIBEAM");
        lookup.add("ASTRAZEN");
        lookup.add("PTCIL");
        lookup.add("FINPIPE");
        lookup.add("LLOYDSENT");
        lookup.add("PRECAM");
        lookup.add("MAMATA");
        lookup.add("WELCORP");
        lookup.add("VINYAS");
        lookup.add("MARATHON");
        lookup.add("GPTINFRA");
        lookup.add("DODLA");
        lookup.add("SJS");
        lookup.add("KINGFA");
        lookup.add("CFF");

        return lookup;
    }
}
