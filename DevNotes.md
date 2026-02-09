# Dev Notes

## System Map (Economy + Weekly Cycles)
- **Entry point:** `Main.java` launches `WineBarGUI` with a fresh `GameState`.
- **Economy state owner:** `GameState` (cash, credit score, credit lines, invoices, wage escalation flags, shark threat tier).
- **Central payment path:** `EconomySystem.tryPay(...)` (cash-first, then credit line draw).
- **Credit lines + banks:** `CreditLineManager` (open lines, apply credit, weekly interest + repayment checks, credit score adjustments).
- **Loan shark escalation:** `Simulation.processSharkThreatWeekly()` + `Simulation.applySharkTierEffects(...)`.
- **Invoices:** `SupplierInvoice` data; weekly aging + late fees in `Simulation.processSupplierInvoicesWeekly()`; payment flow in `Simulation.paySupplierInvoice(...)` and invoice UI in `WineBarGUI`.
- **Wages + collapse:** weekly wage payment in `Simulation.endOfWeek()` via `EconomySystem.endOfWeekPayBills(...)`; escalation in `Simulation.handleWageMiss()` / `Simulation.applyWageMissEffects(...)`.
- **Weekly tick trigger:** `Simulation.closeNight(...)` → `endOfWeek()` when `dayIndex == 0`.
- **Reports + metrics:** `Simulation.buildMetricsSnapshot()` (Mission Control) and `ReportSystem` (weekly + 4-week reports).
