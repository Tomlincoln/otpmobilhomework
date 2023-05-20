package hu.tomlincoln.otpmobilhomework.service;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.text.MessageFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;

import hu.tomlincoln.otpmobilhomework.ParseException;
import hu.tomlincoln.otpmobilhomework.entity.Customer;
import hu.tomlincoln.otpmobilhomework.entity.CustomerId;
import hu.tomlincoln.otpmobilhomework.entity.Payment;
import hu.tomlincoln.otpmobilhomework.entity.PaymentType;
import hu.tomlincoln.otpmobilhomework.repository.CustomerRepository;
import hu.tomlincoln.otpmobilhomework.repository.PaymentRepository;

@Service
public class DataLoadService {

    private static final Logger LOG = LoggerFactory.getLogger(DataLoadService.class);
    private static final int PAYMENT_WEBSHOP_ID_INDEX = 0;
    private static final int PAYMENT_USER_ID_INDEX = 1;
    private static final int CUSTOMER_NAME_INDEX = 2;
    private static final int PAYMENT_TYPE_INDEX = 2;
    private static final int PAYMENT_AMOUNT_INDEX = 3;
    private static final int PAYMENT_TYPE_TRANSFER_INDEX = 4;
    private static final int PAYMENT_TYPE_CARD_INDEX = 5;
    private static final int PAYMENT_PAID_AT_INDEX = 6;
    private static final int PAYMENT_PAID_AT_YEAR_INDEX = 0;
    private static final int PAYMENT_PAID_AT_MONTH_INDEX = 1;
    private static final int PAYMENT_PAID_AT_DAY_INDEX = 2;
    private static final int YEAR_LENGTH = 4;
    private static final int MONTH_LENGTH = 2;
    private static final int DAY_LENGTH = 2;
    private static final String CUSTOMERS_FILE = ResourceUtils.CLASSPATH_URL_PREFIX + "customer.csv";
    private static final String PAYMENTS_FILE = ResourceUtils.CLASSPATH_URL_PREFIX + "payments.csv";
    private static final String MALFORMED_PAYMENT_LINE_TEXT = "Malformed payment line! Line number: {0}, Line text: {1}";
    private static final String MALFORMED_CUSTOMER_LINE_TEXT = "Malformed customer line! Line number: {0}, Line text: {1}";
    private static final String CSV_SEPARATOR = ";";
    private static final int CUSTOMER_DATA_FIELDS_COUNT = 4;
    private static final int CUSTOMER_WEBSHOP_ID_INDEX = 0;
    private static final int CUSTOMER_CUSTOMER_ID_INDEX = 1;
    private static final int CUSTOMER_ADDRESS_INDEX = 3;

    private final CustomerRepository customerRepository;
    private final PaymentRepository paymentRepository;

    @Autowired
    public DataLoadService(CustomerRepository customerRepository, PaymentRepository paymentRepository) {
        this.customerRepository = customerRepository;
        this.paymentRepository = paymentRepository;
        loadCustomers();
        loadPayments();
    }

    private void loadCustomers() {
        try (var reader = new BufferedReader(new InputStreamReader(new FileInputStream(ResourceUtils.getFile(CUSTOMERS_FILE))))) {
            int lineCounter = PAYMENT_WEBSHOP_ID_INDEX;
            while (reader.ready()) {
                String line = reader.readLine();
                lineCounter++;
                String[] splitData = line.split(CSV_SEPARATOR);
                if (splitData.length != CUSTOMER_DATA_FIELDS_COUNT) {
                    throw new IOException(MessageFormat.format(MALFORMED_CUSTOMER_LINE_TEXT, lineCounter, line));
                }
                Customer customer = createCustomer(splitData);
                customerRepository.save(customer);
            }
        } catch (IOException ex) {
            LOG.warn(ex.getMessage());
        }
    }

    private void loadPayments() {
        try (var reader = new BufferedReader(new InputStreamReader(new FileInputStream(ResourceUtils.getFile(PAYMENTS_FILE))))) {
            int lineCounter = PAYMENT_WEBSHOP_ID_INDEX;
            while (reader.ready()) {
                String line = reader.readLine();
                lineCounter++;
                String[] splitData = line.split(CSV_SEPARATOR);
                if (splitData.length != 7) {
                    LOG.error(MessageFormat.format(MALFORMED_PAYMENT_LINE_TEXT, lineCounter, line));
                    continue;
                }
                Payment payment = null;
                try {
                    payment = createPayment(splitData);
                } catch (ParseException e) {
                    LOG.error(MessageFormat.format(MALFORMED_PAYMENT_LINE_TEXT, lineCounter, line));
                    continue;
                }
                paymentRepository.save(payment);
            }
        } catch (IOException ex) {
            LOG.error(ex.getMessage(), ex);
        }
    }

    private Payment createPayment(String[] paymentData) throws IOException, ParseException {
        Payment payment = new Payment();
        payment.setCustomerId(new CustomerId(paymentData[PAYMENT_WEBSHOP_ID_INDEX], paymentData[PAYMENT_USER_ID_INDEX]));
        payment.setType(PaymentType.valueOf(paymentData[PAYMENT_TYPE_INDEX].toUpperCase()));
        payment.setAmount(BigDecimal.valueOf(Double.parseDouble(paymentData[PAYMENT_AMOUNT_INDEX])));
        payment.setSource(payment.getType() == PaymentType.TRANSFER ? paymentData[PAYMENT_TYPE_TRANSFER_INDEX] : paymentData[PAYMENT_TYPE_CARD_INDEX]);
        String[] paidAt = (paymentData[PAYMENT_PAID_AT_INDEX].split("\\."));
        boolean yearCorrect = paidAt[PAYMENT_PAID_AT_YEAR_INDEX].length() == YEAR_LENGTH;
        boolean monthCorrect = paidAt[PAYMENT_PAID_AT_MONTH_INDEX].length() == MONTH_LENGTH;
        boolean dayCorrect = paidAt[PAYMENT_PAID_AT_DAY_INDEX].length() == DAY_LENGTH;
        if (!yearCorrect || !monthCorrect || !dayCorrect) {
            throw new ParseException();
        }
        payment.setPaidAt(LocalDate.parse(paymentData[6], DateTimeFormatter.ofPattern("yyyy.MM.dd")));
        return payment;
    }

    private Customer createCustomer(String[] splitData) {
        Customer customer = new Customer();
        customer.setId(new CustomerId(splitData[CUSTOMER_WEBSHOP_ID_INDEX], splitData[CUSTOMER_CUSTOMER_ID_INDEX]));
        customer.setName(splitData[CUSTOMER_NAME_INDEX]);
        customer.setAddress(splitData[CUSTOMER_ADDRESS_INDEX]);
        return customer;
    }
}
