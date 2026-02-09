# Java Bar Sim v3

A lightweight pub-tycoon simulation built in Java/Swing. You run a neighborhood bar, manage staff and inventory, set pricing, schedule events, and juggle reputation, security, and profitability across nightly and weekly cycles.

## What this game is
You’re the owner-operator of a small pub. The game simulates nights of service and the downtime between them. Customers (punters) arrive with different budgets and moods, demand drinks/food, and react to your pricing, stock, and service quality. Random events, rumors, and identity shifts push you to specialize the pub over time.

## Core gameplay loop
- Start with a small stock of wine, limited staff, and modest cash.
- Open for the night: serve punters, handle fights/theft, and keep reputation stable.
- Between nights: restock, buy upgrades, schedule activities, and hire staff.
- Each week: pay rent/wages, deal with rumors, and review performance reports.

## Key systems & features
### Economy & costs
- Cash-first spending with optional credit lines (banks + loan shark).
- Weekly rent, wages, security upkeep, and credit-line repayments.
- Credit score impacts pricing, invoice terms, and lender access.
- Operating costs that scale with staffing and crowd size.

### Inventory & suppliers
- Wine and food suppliers with bulk discounts and random deals/shortages.
- Inventory caps and spoilage to prevent hoarding.
- Emergency restocking during open nights (with penalties and staffing requirements).

### Staff & morale
- Front-of-house, back-of-house, and managers with capacity and skill impacts.
- Morale system influenced by chaos, service quality, tips, and security.
- Promotions, leveling, and weekly morale checks that can trigger resignations.

### Customers (punters)
- Reputation-driven customer pool with different budgets and behaviors.
- Service failures lead to reputation loss, fights, and kickouts.
- Pricing affects affordability and complaint risk.

### Security & risk
- Base security upgrades, bouncers, and manager effects.
- Theft and fights that scale with reputation and chaos.

### Events, activities, and identity
- Between-night events like vandalism, inspections, or fires.
- Nightly activities (quiz night, DJ night, sports screening, etc.) with bonuses.
- Pub identity system (e.g., Respectable, Rowdy, Artsy) shaped by weekly performance.

### Reports & progression
- Nightly, weekly, and four-week performance reports.
- Milestones unlock upgrades and premium activities.
- Pub levels grant global bonuses as key upgrades are acquired.

## Entry point
The game boots from `Main.java`, which creates a new `GameState` and launches the Swing UI.

---
If you want to add new content, the data tables live in `GameFactory.java` (starting stock + suppliers), `PubActivity.java` (activities), and `PubUpgrade.java` (upgrade definitions).
