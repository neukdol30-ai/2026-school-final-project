package com.foodlogistics.erp.outbound.service;


import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Component
public class OutboundNumberGenerator {

    public String createOutboundNo() {

        String outboundDate = LocalDate.now()
                .format(DateTimeFormatter.BASIC_ISO_DATE);

        String randomCode = UUID.randomUUID()
                .toString()
                .substring(0,8)
                .toUpperCase();

        return "OB-" + outboundDate + "-" + randomCode;
    }
}
