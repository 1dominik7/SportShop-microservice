package com.ecommerce.marketing.mainImages;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ImagesResponse {

    private Integer id;
    private String url;
    private String name;
    private Integer displayOrder;
}
