package com.ecommerce.marketing.email;

import lombok.Getter;

@Getter
public enum EmailTemplateName {

    NEWSLETTER("newsletter"),
    ORDER_EMAIL("order-email");

    private final String name;

    EmailTemplateName(String name){
        this.name = name;
    }
}
