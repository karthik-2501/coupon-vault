package com.couponvault;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.couponvault.domain.CouponStatus;
import com.couponvault.domain.DiscountType;
import com.couponvault.repository.CouponRepository;
import com.couponvault.service.PaymentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CouponFlowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CouponRepository couponRepository;

    @MockBean
    private PaymentService paymentService;

    @Test
    void createCoupon_validatesToValid_andMarketplaceLists() throws Exception {
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "name", "Seller One",
                                "email", "seller1@test.com",
                                "password", "password123"
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists());

        var login = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "email", "seller1@test.com",
                                "password", "password123"
                        ))))
                .andExpect(status().isOk())
                .andReturn();

        var token = objectMapper.readTree(login.getResponse().getContentAsString()).get("token").asText();

        var createBody = Map.of(
                "storeName", "TestMart",
                "storeWebsiteUrl", "https://testmart.example.com",
                "code", "VALID-TEST10",
                "description", "Test coupon",
                "discountType", "PERCENTAGE",
                "discountValue", new BigDecimal("10"),
                "minOrderValue", new BigDecimal("25"),
                "expiryDate", LocalDate.now().plusDays(30).toString(),
                "askingPrice", new BigDecimal("5.00")
        );

        var createRes = mockMvc.perform(post("/coupons")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createBody)))
                .andExpect(status().isOk())
                .andReturn();

        String couponId = objectMapper.readTree(createRes.getResponse().getContentAsString()).get("id").asText();

        var coupon = couponRepository.findById(couponId).orElseThrow();
        assertThat(coupon.getStatus()).isEqualTo(CouponStatus.VALID);

        mockMvc.perform(get("/coupons"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(couponId));
    }

    @Test
    void createCoupon_invalidPrefix_notInMarketplace() throws Exception {
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "name", "Seller Two",
                                "email", "seller2@test.com",
                                "password", "password123"
                        ))));

        var login = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "email", "seller2@test.com",
                                "password", "password123"
                        ))))
                .andReturn();

        var token = objectMapper.readTree(login.getResponse().getContentAsString()).get("token").asText();

        var createBody = Map.of(
                "storeName", "BadShop",
                "storeWebsiteUrl", "https://badshop.example.com",
                "code", "INVALID-CODE",
                "description", "Bad",
                "discountType", DiscountType.FIXED.name(),
                "discountValue", new BigDecimal("5"),
                "expiryDate", LocalDate.now().plusDays(10).toString(),
                "askingPrice", new BigDecimal("1.00")
        );

        var createRes = mockMvc.perform(post("/coupons")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createBody)))
                .andExpect(status().isOk())
                .andReturn();

        String couponId = objectMapper.readTree(createRes.getResponse().getContentAsString()).get("id").asText();
        assertThat(couponRepository.findById(couponId).orElseThrow().getStatus()).isEqualTo(CouponStatus.INVALID);

        mockMvc.perform(get("/coupons"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void cannotPurchaseSoldOrInvalidCoupon() throws Exception {
        when(paymentService.charge(anyString())).thenReturn(true);

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "name", "Buyer",
                                "email", "buyer@test.com",
                                "password", "password123"
                        ))));

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "name", "Seller",
                                "email", "seller3@test.com",
                                "password", "password123"
                        ))));

        var sellerLogin = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "email", "seller3@test.com",
                                "password", "password123"
                        ))))
                .andReturn();
        var sellerToken = objectMapper.readTree(sellerLogin.getResponse().getContentAsString()).get("token").asText();

        var createBody = Map.of(
                "storeName", "BuyMart",
                "storeWebsiteUrl", "https://buymart.example.com",
                "code", "VALID-BUY1",
                "description", "Buyable",
                "discountType", "FIXED",
                "discountValue", new BigDecimal("5"),
                "expiryDate", LocalDate.now().plusDays(10).toString(),
                "askingPrice", new BigDecimal("3.00")
        );

        var createRes = mockMvc.perform(post("/coupons")
                        .header("Authorization", "Bearer " + sellerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createBody)))
                .andReturn();
        String couponId = objectMapper.readTree(createRes.getResponse().getContentAsString()).get("id").asText();

        var buyerLogin = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "email", "buyer@test.com",
                                "password", "password123"
                        ))))
                .andReturn();
        var buyerToken = objectMapper.readTree(buyerLogin.getResponse().getContentAsString()).get("token").asText();

        mockMvc.perform(post("/transactions")
                        .header("Authorization", "Bearer " + buyerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("couponId", couponId))))
                .andExpect(status().isOk());

        mockMvc.perform(post("/transactions")
                        .header("Authorization", "Bearer " + buyerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("couponId", couponId))))
                .andExpect(status().isConflict());
    }
}
