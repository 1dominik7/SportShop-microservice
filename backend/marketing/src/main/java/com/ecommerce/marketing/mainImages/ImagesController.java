package com.ecommerce.marketing.mainImages;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("main-images")
public class ImagesController {

    private final ImagesService imagesService;

    @GetMapping("/with-order")
    public ResponseEntity<List<ImagesResponse>> getAllImagesWithOrder(){
        List<ImagesResponse> imagesResponses = imagesService.getAllImagesWithOrder();
        return ResponseEntity.status(HttpStatus.OK).body(imagesResponses);
    }

}
