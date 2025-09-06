package com.ecommerce.marketing.newsletter;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "newsletter")
public class Newsletter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String couponId;
    private String email;
    private String subscribedAt;
}
