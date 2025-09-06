package com.ecommerce.order.clients.dto;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddressRequest {

    private String id;
    private String country;
    private String city;
    private String firstName;
    private String lastName;
    private String postalCode;
    private String street;
    private String phoneNumber;
    private String addressLine1;
    private String addressLine2;
}
