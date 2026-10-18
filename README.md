# DuesGuard 🛡️

> **“Know who owes you. Know what to do next.”**
>
> *Payment recovery command centre for freelancers, micro businesses, agencies, tuition centres, and local entrepreneurs.*

---

## 🌟 What is DuesGuard?

DuesGuard is **not another generic accounting suite**. It is a purpose-built desktop payment recovery command centre that answers 7 critical questions for non-technical small business owners:

1. **Who owes me money?**
2. **How much is overdue?**
3. **Which customer should I follow up with first?**
4. **What proof or document is missing?**
5. **What message should I send?**
6. **When can I realistically expect the money?**
7. **Will I have enough cash for upcoming expenses?**

---

## 🚀 The Core Innovation: Explainable Follow-Up Priority Score

Every overdue invoice receives an auditable, transparent score from **0 to 100**:

$$\text{Priority Score} = \text{Amount Due (35\%)} + \text{Days Overdue (25\%)} + \text{Client Behaviour (20\%)} + \text{Evidence Gap (10\%)} + \text{Reminder Response (10\%)}$$

- **35% Outstanding Amount**: Scaled up to ₹1,00,000 for maximum cash-flow recovery impact.
- **25% Overdue Duration**: Reaches peak urgency at 30+ days overdue.
- **20% Client Payment Behaviour**: Based on recorded historical payment delay (neutral, not judgmental).
- **10% Evidence Gap**: Invoices lacking completion proofs or delivery confirmations receive urgency to document.
- **10% Reminder Response**: Unanswered previous follow-ups escalate priority.

---

## ⚡ Key Product Features

| Feature | Description |
|---|---|
| **Payment Recovery Command Centre** | 4-column KPI grid, 30-day cash flow overview, and Today's Action List. |
| **Follow-Up Priority Queue** | Top ranked overdue invoices with 1-click **⚡ Focus Mode** (single next action). |
| **JavaFX 3D Constellation** | Interactive 3D celestial visualization: Clients as hubs, invoices as orbiting nodes glowing red/amber/green. |
| **JavaFX 3D Cash-Flow Orb** | Glowing electric mint 3D sphere with orbital ring and smooth floating motion. |
| **Payment-Readiness Engine** | 5-point dispute-proofing checklist: Quotation, Invoice, Completion Proof, Delivery Proof, Acknowledgement. |
| **Follow-Up Composer** | Instant WhatsApp/Email/SMS message generation across 3 tones (Friendly, Professional, Firm). |
| **Cash-Flow Radar & What-If Simulator** | 3-scenario forecasts (Best, Likely, Conservative) + "What if invoices clear this week?" simulator. |
| **Celebration Engine** | Subtle particle burst and glowing rising shield when overdue dues are successfully recovered. |
| **Judge Demo Mode** | 90-second guided tour across 10 structured milestone steps for hackathon jurors. |

---

## 🛠️ Technology Stack & Constraints

- **Language**: Java 21 (Temurin LTS)
- **GUI Toolkit**: Pure JavaFX 21 (Controls, Charts, SubScene, Shapes)
- **3D Graphics**: Built-in JavaFX 3D (`Sphere`, `Box`, `Cylinder`, `PhongMaterial`, `PointLight`, `AmbientLight`)
- **Styling**: Pure JavaFX CSS (`duesguard-dark.css`)
- **Persistence**: File-based JSON storage via Jackson in `~/.duesguard/` (zero external database)
- **Architecture**: MVC / Service layer pattern with singleton AppContext and NavigationManager

---

## 🖥️ How to Run

### Prerequisites
- JDK 21+ installed and configured on your `PATH`.
- Maven 3.9+ (or use the provided portable Maven binary).

### 1. Build and Run via Maven
```powershell
cd C:\Users\a_ven\.gemini\antigravity\scratch\duesguard
mvn clean javafx:run
```

### 2. Run Tests
```powershell
mvn test
```

### 3. Reset Demo Data
Click **"↻ Reset Demo Data"** in either **Settings** or **Judge Demo Mode** to restore the default state:
- **Business**: Lakshmi Creative Studio (Owner: Lakshmi V., Chennai)
- **Overdue**: ₹48,500 across 4 invoices
- **Top Client**: GreenLeaf Events (INV-103: ₹42,000, 24 days overdue)

---

## 🏆 Hackathon Demo Guide (90 Seconds)

1. **Dashboard**: Highlight the top alert banner: *“₹48,500 needs attention across 4 invoices.”*
2. **3D Cash Flow Orb**: Point out the floating JavaFX 3D sphere embedded in the Cash-Flow Overview card.
3. **Priority Queue**: Click *“Open priority queue”* to show GreenLeaf Events ranked #1.
4. **3D Constellation**: Click *“🌌 3D Constellation”* to display orbiting 3D client/invoice spheres.
5. **Focus Mode**: Click *“⚡ Focus Mode”* to reveal the single recommended action and 1-click WhatsApp message copy.
6. **Payment Readiness**: Open INV-103 to reveal the dispute-readiness meter and missing completion proof.
7. **What-If Simulator**: Navigate to *Cash-flow Radar* and simulate: *“What happens if GreenLeaf pays this week?”*
8. **Recovery Celebration**: Record payment to trigger the particle burst and recovery banner: *“₹42,000 recovered. Your expected cash flow improved.”*
