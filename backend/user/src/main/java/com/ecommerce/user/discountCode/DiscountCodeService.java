package com.ecommerce.user.discountCode;

import com.ecommerce.user.exceptions.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DiscountCodeService {

    private final DiscountCodeRepository discountCodeRepository;

    @Transactional
    public DiscountCodeResponse createDiscountCode (DiscountCodeRequest discountCodeRequest){
        DiscountCode discountCode = new DiscountCode();
        discountCode.setName(discountCodeRequest.getName());
        discountCode.setCode(discountCodeRequest.getCode());
        discountCode.setExpiryDate(discountCodeRequest.getExpiryDate());
        discountCode.setDiscount(discountCodeRequest.getDiscount());
        discountCode.setSingleUse(discountCodeRequest.isSingleUse());
        discountCode.setUsed(false);

        DiscountCode saved = discountCodeRepository.save(discountCode);

        return DiscountCodeResponse.builder()
                .id(saved.getId().toString())
                .name(saved.getName())
                .code(saved.getCode())
                .expiryDate(saved.getExpiryDate())
                .discount(saved.getDiscount())
                .singleUse(saved.isSingleUse())
                .used(saved.isUsed())
                .build();
    }

    @Transactional
    public DiscountCode updateDiscountCode (String discountCodeId, DiscountCodeRequest discountCodeRequest){

        DiscountCode discountCode = discountCodeRepository.findById(discountCodeId).orElseThrow(() -> new NotFoundException("Discount", Optional.of(discountCodeId)));

        discountCode.setName(discountCodeRequest.getName());
        discountCode.setCode(discountCodeRequest.getCode());
        discountCode.setExpiryDate(discountCodeRequest.getExpiryDate());
        discountCode.setDiscount(discountCodeRequest.getDiscount());

        return discountCodeRepository.save(discountCode);
    }

    public DiscountCode getDiscountCodeById(String discountCodeId){
        DiscountCode discountCode = discountCodeRepository.findById(discountCodeId).orElseThrow(() -> new NotFoundException("Discount",Optional.of(discountCodeId)));

        return discountCode;
    }

    public List<DiscountCode> getAllDiscountCode(){
        List<DiscountCode> discountCodes = discountCodeRepository.findAll();

        return discountCodes;
    }

    @Transactional
    public void deleteDiscountCode(String discountCodeId){
        DiscountCode discountCode = discountCodeRepository.findById(discountCodeId).orElseThrow(() -> new NotFoundException("Discount",Optional.of(discountCodeId)));
        discountCodeRepository.delete(discountCode);
    }

    public List<DiscountCode> getActiveDiscountCode(){

        LocalDateTime now = LocalDateTime.now();

        return discountCodeRepository.findByExpiryDateGreaterThanEqual(now);
    }
}
