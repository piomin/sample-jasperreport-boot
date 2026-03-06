package pl.piomin.jasperreport;

import java.io.File;
import java.io.InputStream;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ClassPathResource;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.fill.JRFileVirtualizer;
import net.sf.jasperreports.engine.fill.JRSwapFileVirtualizer;
import net.sf.jasperreports.engine.util.JRLoader;
import net.sf.jasperreports.engine.util.JRSaver;
import net.sf.jasperreports.engine.util.JRSwapFile;

@SpringBootApplication
public class JasperApplication {

    @Value("${directory}")
    private String directory;

    public static void main(String[] args) {
        SpringApplication.run(JasperApplication.class, args);
    }

    @Bean
    JasperReport report() throws JRException {
        File f = new File("personReport.jasper");
        if (f.exists()) {
            return (JasperReport) JRLoader.loadObject(f);
        }

        ClassPathResource resource = new ClassPathResource("report.jrxml");
        try (InputStream is = resource.getInputStream()) {
            JasperReport jr = JasperCompileManager.compileReport(is);
            JRSaver.saveObject(jr, "personReport.jasper");
            return jr;
        } catch (java.io.IOException e) {
            throw new JRException("Failed to load report template", e);
        }
    }

    @Bean
    JRFileVirtualizer fileVirtualizer() {
        return new JRFileVirtualizer(100, directory);
    }

    @Bean
    JRSwapFileVirtualizer swapFileVirtualizer() {
        JRSwapFile sf = new JRSwapFile(directory, 1024, 100);
        return new JRSwapFileVirtualizer(20, sf, true);
    }

}
