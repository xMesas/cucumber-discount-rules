package com.xmesas.cucumberdiscount;

import com.xmesas.cucumberdiscount.domain.DiscountEngine;
import com.xmesas.cucumberdiscount.domain.DiscountResult;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

public class OrderDiscountSteps {

    private final DiscountEngine discountEngine;

    private BigDecimal subtotal;
    private String couponCode;
    private DiscountResult result;

    public OrderDiscountSteps(DiscountEngine discountEngine) {
        this.discountEngine = discountEngine;
    }

    @Given("a cart with a subtotal of {bigdecimal}")
    public void aCartWithASubtotalOf(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }

    @And("the coupon code {string} is applied")
    public void theCouponCodeIsApplied(String couponCode) {
        this.couponCode = couponCode;
    }

    @When("the discount engine calculates the total")
    public void theDiscountEngineCalculatesTheTotal() {
        result = discountEngine.calculate(subtotal, couponCode);
    }

    @Then("the applied discount tier should be {string}")
    public void theAppliedDiscountTierShouldBe(String tier) {
        assertThat(result.tierLabel()).isEqualTo(tier);
    }

    @Then("the final total should be {bigdecimal}")
    public void theFinalTotalShouldBe(BigDecimal total) {
        assertThat(result.total()).isEqualByComparingTo(total);
    }

    @Then("the coupon should be rejected")
    public void theCouponShouldBeRejected() {
        assertThat(result.couponAccepted()).isFalse();
    }
}
