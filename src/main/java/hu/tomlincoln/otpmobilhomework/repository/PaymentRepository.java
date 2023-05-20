package hu.tomlincoln.otpmobilhomework.repository;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import hu.tomlincoln.otpmobilhomework.entity.CustomerId;
import hu.tomlincoln.otpmobilhomework.entity.Payment;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    @Query("SELECT SUM(p.amount) FROM Payment p WHERE p.customerId=:customerId")
    BigDecimal sumByCustomerId(CustomerId customerId);

    @Query(value = "SELECT p FROM Payment p WHERE p.customerId.webshopId=:webshopId AND p.type='TRANSFER'")
    List<Payment> sumByWebShopTransfer(String webshopId);

    @Query(value = "SELECT p FROM Payment p WHERE p.customerId.webshopId=:webshopId AND p.type='CARD'")
    List<Payment> sumByWebShopCard(String webshopId);

}
