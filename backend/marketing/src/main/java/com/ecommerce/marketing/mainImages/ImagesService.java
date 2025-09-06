package com.ecommerce.marketing.mainImages;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ImagesService {

    private final ImagesRepository imagesRepository;

    public List<ImagesResponse> getAllImagesWithOrder() {
        List<Images> images = imagesRepository.findByDisplayOrderIsNotNull();

        return images.stream().
                map(image -> ImagesResponse.builder()
                        .id(image.getId())
                        .url(image.getUrl())
                        .name(image.getName())
                        .displayOrder(image.getDisplayOrder())
                        .build()
        ).collect(Collectors.toList());
    }
}