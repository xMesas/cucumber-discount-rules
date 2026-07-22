Feature: Order discount calculation
  As a store owner
  I want tiered subtotal discounts and coupon codes applied in the right order
  So that customers get the correct total, and it never goes below zero

  Scenario Outline: Tiered percentage discount by subtotal
    Given a cart with a subtotal of <subtotal>
    When the discount engine calculates the total
    Then the applied discount tier should be "<tier>"
    And the final total should be <total>

    Examples:
      | subtotal | tier       | total  |
      | 40.00    | none       | 40.00  |
      | 50.00    | 5 percent  | 47.50  |
      | 99.99    | 5 percent  | 94.99  |
      | 100.00   | 10 percent | 90.00  |
      | 199.99   | 10 percent | 179.99 |
      | 200.00   | 15 percent | 170.00 |

  Scenario Outline: A valid coupon stacks with the tier discount but never pushes the total below zero
    Given a cart with a subtotal of <subtotal>
    And the coupon code "<coupon>" is applied
    When the discount engine calculates the total
    Then the final total should be <total>

    Examples:
      | subtotal | coupon | total |
      | 100.00   | SAVE10 | 80.00 |
      | 20.00    | SAVE25 | 0.00  |

  Scenario: An unknown coupon code is rejected without affecting the tier discount
    Given a cart with a subtotal of 50.00
    And the coupon code "BADCODE" is applied
    When the discount engine calculates the total
    Then the coupon should be rejected
    And the final total should be 47.50
