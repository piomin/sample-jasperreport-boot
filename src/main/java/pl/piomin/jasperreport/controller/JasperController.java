package pl.piomin.jasperreport.controller;

import java.io.ByteArrayOutputStream;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import net.sf.jasperreports.engine.JRParameter;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import net.sf.jasperreports.engine.fill.JRFileVirtualizer;
import net.sf.jasperreports.engine.fill.JRSwapFileVirtualizer;
import net.sf.jasperreports.engine.util.JRSwapFile;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;

@RestController
public class JasperController {

    private static final Logger logger = LoggerFactory.getLogger(JasperController.class);
    private final AtomicInteger count = new AtomicInteger(0);

    private final JRFileVirtualizer fv;
    private final String directory;
    private final DataSource datasource;
    private final JasperReport jasperReport;

    @Autowired
    public JasperController(JRFileVirtualizer fv,
                          @Value("${directory}") String directory,
                          DataSource datasource,
                          JasperReport jasperReport) {
        this.fv = fv;
        this.directory = directory;
        this.datasource = datasource;
        this.jasperReport = jasperReport;
    }

    @GetMapping("/pdf/{age}")
    public ResponseEntity<byte[]> getReport(@PathVariable int age) {
        logger.info("getReport({})", age);
        Map<String, Object> m = new HashMap<>();
        m.put("age", age);
        String name = count.incrementAndGet() + "personReport.pdf";
        return generateReport(name, m);
    }

    @GetMapping("/pdf/fv/{age}")
    public ResponseEntity<byte[]> getReportFv(@PathVariable int age) {
        logger.info("getReportFv({})", age);
        Map<String, Object> m = new HashMap<>();
        m.put(JRParameter.REPORT_VIRTUALIZER, fv);
        m.put("age", age);
        String name = count.incrementAndGet() + "personReport.pdf";
        return generateReport(name, m);
    }

    @GetMapping("/pdf/sfv/{age}")
    public ResponseEntity<byte[]> getReportSfv(@PathVariable int age) {
        logger.info("getReportSfv({})", age);
        JRSwapFile sf = new JRSwapFile(directory, 1024, 100);
        JRSwapFileVirtualizer sfv = new JRSwapFileVirtualizer(20, sf, true);
        Map<String, Object> m = new HashMap<>();
        m.put(JRParameter.REPORT_VIRTUALIZER, sfv);
        m.put("age", age);
        String name = count.incrementAndGet() + "personReport.pdf";
        return generateReport(name, m, sfv);
    }

    private ResponseEntity<byte[]> generateReport(String name, Map<String, Object> params) {
        return generateReport(name, params, null);
    }

    private ResponseEntity<byte[]> generateReport(String name, Map<String, Object> params, JRSwapFileVirtualizer sfv) {
        try (Connection cc = datasource.getConnection()) {
            JasperPrint p = JasperFillManager.fillReport(jasperReport, params, cc);
            JRPdfExporter exporter = new JRPdfExporter();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            SimpleOutputStreamExporterOutput c = new SimpleOutputStreamExporterOutput(baos);
            exporter.setExporterInput(new SimpleExporterInput(p));
            exporter.setExporterOutput(c);
            exporter.exportReport();

            byte[] pdfBytes = baos.toByteArray();
            HttpHeaders responseHeaders = new HttpHeaders();
            responseHeaders.setContentType(MediaType.APPLICATION_PDF);
            responseHeaders.setContentDispositionFormData("attachment", name);
            responseHeaders.setContentLength(pdfBytes.length);
            return new ResponseEntity<>(pdfBytes, responseHeaders, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Failed to generate report: {}", name, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } finally {
            if (sfv != null) {
                sfv.cleanup();
            }
            if (fv != null && sfv == null) {
                fv.cleanup();
            }
        }
    }
}
