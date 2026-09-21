package com.foodlogistics.erp.salesorder.service;


import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Component
public class SalesOrderNumberGenerator {

    public String generate() {
        String orderDate = LocalDate.now()
                .format(DateTimeFormatter.BASIC_ISO_DATE);

        String randomCode = UUID.randomUUID()
                .toString()
                .substring(0 , 8)
                .toUpperCase();

        return "SO-" + orderDate + "-" + randomCode;
    }
}
