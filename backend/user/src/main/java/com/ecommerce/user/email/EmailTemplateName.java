package com.ecommerce.user.email;

import lombok.Getter;
import lombok.Setter;

@Getter
public enum EmailTemplateName {

    ACTIVATE_ACCOUNT("activate_account"),
    RESET_PASSWORD("reset_password"),
    PASSWORD_CHANGED("password_changed");

    private final String name;

    EmailTemplateName(String name){
        this.name = name;
    }
}
