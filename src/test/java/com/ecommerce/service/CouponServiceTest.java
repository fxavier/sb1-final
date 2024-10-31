package com.ecommerce.service;

import com.ecommerce.domain.dto.CouponDTO;
import com.ecommerce.domain.model.Coupon;
import com.ecommerce.domain.model.DiscountType;
import com.ecommerce.domain.repository.CouponRepository;
import com.ecommerce.exception.ResourceNotFoundException;
import io.smallrye.mutiny.Uni;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CouponServiceTest {

    @Mock
    private CouponRepository couponRepository;

    @InjectMocks
    private CouponService couponService;

    private Coupon testCoupon;
    private CouponDTO couponDTO;

    @BeforeEach
    void setUp() {
        testCoupon = new Coupon();
        testCoupon.setId(1L);
        testCoupon.setCode("TEST123");
        testCoupon.setDescription("Test Coupon");
        testCoupon.setDiscountType(DiscountType.PERCENTAGE);
        testCoupon.setDiscountValue(new BigDecimal("10.00"));
        testCoupon.setStartDate(LocalDateTime.now());
        testCoupon.setEndDate(LocalDateTime.now().plusDays(30));
        testCoupon.setUsageLimit(100);
        testCoupon.setActive(true);

        couponDTO = new CouponDTO();
        couponDTO.setCode("TEST123");
        couponDTO.setDescription("Test Coupon");
        couponDTO.setDiscountType(DiscountType.PERCENTAGE);
        couponDTO.setDiscountValue(new BigDecimal("10.00"));
        couponDTO.setStartDate(LocalDateTime.now());
        couponDTO.setEndDate(LocalDateTime.now().plusDays(30));
        couponDTO.setUsageLimit(100);
        couponDTO.setActive(true);
    }

    @Test
    void getActiveCoupons_ReturnsActiveCoupons() {
        List<Coupon> coupons = Arrays.asList(testCoupon);
        when(couponRepository.findActive()).thenReturn(Uni.createFrom().item(coupons));

        List<Coupon> result = couponService.getActiveCoupons()
            .await().indefinitely();

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        verify(couponRepository).findActive();
    }

    @Test
    void createCoupon_ValidCoupon_CreatesCoupon() {
        when(couponRepository.findByCode(couponDTO.getCode())).thenReturn(Uni.createFrom().nullItem());
        when(couponRepository.persist(any(Coupon.class))).thenReturn(Uni.createFrom().item(testCoupon));

        Coupon result = couponService.createCoupon(couponDTO)
            .await().indefinitely();

        assertNotNull(result);
        assertEquals(couponDTO.getCode(), result.getCode());
        assertEquals(couponDTO.getDiscountValue(), result.getDiscountValue());
        verify(couponRepository).findByCode(couponDTO.getCode());
        verify(couponRepository).persist(any(Coupon.class));
    }

    @Test
    void createCoupon_ExistingCode_ThrowsException() {
        when(couponRepository.findByCode(couponDTO.getCode())).thenReturn(Uni.createFrom().item(testCoupon));

        assertThrows(IllegalStateException.class, () -> {
            couponService.createCoupon(couponDTO).await().indefinitely();
        });
        verify(couponRepository).findByCode(couponDTO.getCode());
        verify(couponRepository, never()).persist(any(Coupon.class));
    }

    @Test
    void updateCoupon_ExistingCoupon_UpdatesCoupon() {
        when(couponRepository.findById(1L)).thenReturn(Uni.createFrom().item(testCoupon));
        when(couponRepository.persist(any(Coupon.class))).thenReturn(Uni.createFrom().item(testCoupon));

        couponDTO.setDescription("Updated Description");
        Coupon result = couponService.updateCoupon(1L, couponDTO)
            .await().indefinitely();

        assertNotNull(result);
        assertEquals(couponDTO.getDescription(), result.getDescription());
        verify(couponRepository).findById(1L);
        verify(couponRepository).persist(any(Coupon.class));
    }

    @Test
    void updateCoupon_NonExistingCoupon_ThrowsException() {
        when(couponRepository.findById(99L)).thenReturn(Uni.createFrom().nullItem());

        assertThrows(ResourceNotFoundException.class, () -> {
            couponService.updateCoupon(99L, couponDTO).await().indefinitely();
        });
        verify(couponRepository).findById(99L);
        verify(couponRepository, never()).persist(any(Coupon.class));
    }

    @Test
    void calculateDiscount_ValidCoupon_CalculatesDiscount() {
        BigDecimal cartTotal = new BigDecimal("100.00");
        when(couponRepository.findByCode("TEST123")).thenReturn(Uni.createFrom().item(testCoupon));

        BigDecimal result = couponService.calculateDiscount("TEST123", cartTotal, List.of())
            .await().indefinitely();

        assertNotNull(result);
        assertEquals(new BigDecimal("10.00"), result);
        verify(couponRepository).findByCode("TEST123");
    }

    @Test
    void calculateDiscount_ExpiredCoupon_ReturnsZero() {
        testCoupon.setEndDate(LocalDateTime.now().minusDays(1));
        BigDecimal cartTotal = new BigDecimal("100.00");
        when(couponRepository.findByCode("TEST123")).thenReturn(Uni.createFrom().item(testCoupon));

        BigDecimal result = couponService.calculateDiscount("TEST123", cartTotal, List.of())
            .await().indefinitely();

        assertEquals(BigDecimal.ZERO, result);
        verify(couponRepository).findByCode("TEST123");
    }
}