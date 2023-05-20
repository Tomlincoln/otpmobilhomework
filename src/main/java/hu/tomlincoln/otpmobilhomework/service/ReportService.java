package hu.tomlincoln.otpmobilhomework.service;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Service;

import hu.tomlincoln.otpmobilhomework.OtpmobilhomeworkApplication;
import hu.tomlincoln.otpmobilhomework.entity.Customer;
import hu.tomlincoln.otpmobilhomework.entity.Payment;
import hu.tomlincoln.otpmobilhomework.repository.CustomerRepository;
import hu.tomlincoln.otpmobilhomework.repository.PaymentRepository;

@Service
@DependsOn("dataLoadService")
public class ReportService {

    private static final Logger LOG = LoggerFactory.getLogger(ReportService.class);
    private static final String CSV_DELIMITER = ";";
    private final CustomerRepository customerRepository;
    private final PaymentRepository paymentRepository;

    @Autowired
    ReportService(CustomerRepository customerRepository, PaymentRepository paymentRepository) {
        this.customerRepository = customerRepository;
        this.paymentRepository = paymentRepository;
        doReports();
        // Of course never do this in production!
        OtpmobilhomeworkApplication.done = true;
    }

    private void doReports() {
        Map<Customer, BigDecimal> sumByCustomer = sumPaymentForCustomers();
        reportTopTwoByPaymentAmount(sumByCustomer);
        reportSumByWebshops();
    }

    private Map<Customer, BigDecimal> sumPaymentForCustomers() {
        Map<Customer, BigDecimal> toReturn = new HashMap<>();
        List<String> summedLines = new ArrayList<>();
        List<Customer> customers = customerRepository.findAll();
        customers.forEach(customer -> {
            BigDecimal sum = paymentRepository.sumByCustomerId(customer.getId());
            if (sum != null) {
                toReturn.put(customer, sum);
                summedLines.add(customer.getName() + CSV_DELIMITER + customer.getAddress() + CSV_DELIMITER + sum);
            }
        });
        writeToFile("report01.csv", String.join("\n", summedLines));
        return toReturn;
    }

    private void reportTopTwoByPaymentAmount(Map<Customer, BigDecimal> sumByCustomer) {
        List<String> topTwoLines = new ArrayList<>();
        sumByCustomer.entrySet().stream()
                .sorted(Map.Entry.comparingByValue((Comparator.reverseOrder())))
                .limit(2)
                .forEach(entry -> topTwoLines.add(entry.getKey().getName() + CSV_DELIMITER + entry.getKey().getAddress() + CSV_DELIMITER + entry.getValue()));
        writeToFile("top.csv", String.join("\n", topTwoLines));
    }

    private void reportSumByWebshops() {
        Set<String> webShopIds = paymentRepository.findAll().stream().map(p -> p.getCustomerId().getWebshopId()).collect(Collectors.toSet());
        List<String> sumsByWebshop = new ArrayList<>();
        webShopIds.forEach(id -> {
            BigDecimal cardAmount = paymentRepository.sumByWebShopCard(id).stream().map(Payment::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal transferAmount = paymentRepository.sumByWebShopTransfer(id).stream().map(Payment::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
            sumsByWebshop.add(id + CSV_DELIMITER + cardAmount + CSV_DELIMITER + transferAmount);
        });
        writeToFile("report02.csv", String.join("\n", sumsByWebshop));
    }

    public void writeToFile(String filename, String text) {
        try {
            Files.write(Paths.get("./" + filename), text.getBytes(StandardCharsets.UTF_8));
        } catch (IOException ex) {
            LOG.error(ex.getMessage(), ex);
        }
    }
}
