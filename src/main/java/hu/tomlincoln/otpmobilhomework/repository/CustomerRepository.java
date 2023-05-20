package hu.tomlincoln.otpmobilhomework.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import hu.tomlincoln.otpmobilhomework.entity.Customer;
import hu.tomlincoln.otpmobilhomework.entity.CustomerId;

public interface CustomerRepository extends JpaRepository<Customer, CustomerId> {
}
