# cucumber-discount-rules

![CI](https://github.com/xMesas/cucumber-discount-rules/actions/workflows/ci.yml/badge.svg)

## In plain English

Every project in this portfolio so far has been tested from the engineer's side —
JUnit classes an engineer writes and reads. This one flips that: the business rules
for an order-discount engine are written as **Gherkin scenarios**, in plain English
a store owner could read and sign off on directly, and those exact scenarios are
what actually executes and fails the build if the code is wrong. There is no
separate "translation" step where a human-readable spec gets manually turned into a
test — the `.feature` file *is* the test.

## What actually got measured

Real local run, `./mvnw test`:

```
9 Scenarios (9 passed)
37 Steps (37 passed)
0m1,771s

[INFO] Tests run: 9, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

**Every hand-computed expected number in the feature file matched on the first real
run** — no bug to report here, and this project says so plainly rather than
inventing friction that didn't happen (same honest-reporting precedent as
`soap-order-status-service` and `websocket-stomp-auth` elsewhere in this
portfolio).

**One genuinely useful, non-obvious finding, confirmed by actually running it**:
Maven Surefire's `Tests run: 9` line and Cucumber's own `9 Scenarios (9 passed)`
summary line report the **exact same number**, independently. Cucumber's
JUnit-Platform engine (`cucumber-junit-platform-engine`) registers as a normal
`TestEngine` alongside JUnit Jupiter, and Surefire's JUnit Platform provider
faithfully bridges every leaf scenario from the Cucumber engine's test tree into
its own reported count — not just "1 suite ran." That means this portfolio's
established "verify by grepping the log for `Tests run:`, never trust the green
checkmark alone" discipline works unmodified for Cucumber scenarios too, with the
`Scenarios (` line as a second, Cucumber-specific piece of corroborating evidence.

## Approach

- `DiscountEngine` — the one class with any business logic at all: a tiered
  percentage discount by subtotal (5% / 10% / 15% at 50 / 100 / 200), then an
  optional flat-amount coupon code applied on top, floored so the total can never
  go negative. Framework-free — no Spring annotations beyond `@Component`.
- `order_discount.feature` — the actual specification, written as `Scenario
  Outline` + `Examples` tables for the tiered-discount and coupon-stacking rules,
  plus one plain `Scenario` for the unknown-coupon-code edge case.
- `OrderDiscountSteps` — glue code translating each Gherkin line into a call
  against the real, Spring-managed `DiscountEngine` bean (constructor-injected,
  same as any other Spring component) and an AssertJ assertion. No logic lives
  here — only translation from English to a method call.
- `CucumberSpringConfiguration` / `RunCucumberTest` — the two pieces of
  boilerplate Cucumber+Spring+JUnit5 needs: `@CucumberContextConfiguration` +
  `@SpringBootTest` to get a real Spring context injected into step definitions,
  and a `@Suite`/`@IncludeEngines("cucumber")` class so the JUnit Platform Launcher
  (and therefore Surefire) discovers and runs the `.feature` files at all.

## Architecture decisions

- **Tier discount computed first, coupon applied second, on the post-tier amount
  — and documented as a deliberate ordering choice, not an accident.** The Examples
  table for `SAVE10` against a 100.00 subtotal only comes out to 80.00 (not 90.00)
  because the flat €10 coupon is subtracted from the already-tier-discounted 90.00,
  not the original 100.00. Getting this ordering wrong is exactly the kind of thing
  a Gherkin scenario with a concrete worked example catches immediately, and a
  vaguer prose spec ("coupons stack with tier discounts") would not.
- **The coupon amount is capped at whatever remains after the tier discount,
  never allowed to push the total negative.** A `min(couponFlatAmount, afterTier)`
  guard makes a €25 coupon against a €20 cart correctly floor at exactly €0.00
  instead of a nonsensical negative total.
- **No infrastructure at all — no Docker, no database.** The point being
  demonstrated (Gherkin scenarios as executable, Spring-integrated specifications)
  doesn't depend on any external system, so this project runs anywhere a JDK does.
- **`cucumber.plugin=pretty, summary, json:target/cucumber-report.json`** in
  `junit-platform.properties` — `pretty` prints each scenario/step as it runs
  (visible above), `summary` prints the real `N Scenarios (N passed)` line CI
  verification greps for, and the JSON report is uploaded as a CI artifact for
  anyone who wants the full machine-readable results (e.g. to render as living
  documentation in a dashboard).

## Stack

Java 21, Spring Boot 3.4.2, Cucumber 7.20.1 (`cucumber-java`, `cucumber-spring`,
`cucumber-junit-platform-engine`), JUnit Platform Suite, AssertJ.

## Running it

```bash
./mvnw test
```

Watch the scenarios print live, then check the summary line at the end of the
output, or inspect `target/cucumber-report.json` after the run.

## Status

- [x] Real Gherkin feature files driving real Spring-managed business logic
- [x] Every hand-computed expected number verified against real output
- [x] Dual verification confirmed: Surefire's `Tests run:` and Cucumber's own
      `Scenarios (` summary line agree exactly
- [x] CI green with real evidence in the log, JSON report uploaded as an artifact
- [x] No external infrastructure required

## Notes / next steps

- Only one aggregate (order/cart) and one rule set (tiered % + flat coupon) is
  modeled — real BDD suites usually grow many feature files across many domain
  concepts; this project keeps to one to stay focused on the mechanism.
- No `Background:` section or `DataTable`-shaped step arguments were needed for
  this rule set; both are common Cucumber features worth reaching for on a
  larger, multi-feature suite.
