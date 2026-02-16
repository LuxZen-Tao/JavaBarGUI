import java.util.*;

public class LandlordPromptEventCatalog {
    private static final List<LandlordPromptEventDef> EVENTS = buildEvents();

    public static LandlordPromptEventDef getById(LandlordPromptEventId id) {
        for (LandlordPromptEventDef event : EVENTS) {
            if (event.getId() == id) return event;
        }
        return null;
    }

    public static List<LandlordPromptEventDef> allEvents() {
        return new ArrayList<>(EVENTS);
    }

    private static List<LandlordPromptEventDef> buildEvents() {
        List<LandlordPromptEventDef> events = new ArrayList<>();
        events.add(buildLocalJournalistEvent());
        events.add(buildStaffDisputeEvent());
        events.add(buildCorporateBookingEvent());
        events.add(buildHealthInspectorEvent());
        events.add(buildRegularComplaintEvent());
        events.add(buildStaffReferralEvent());
        return events;
    }

    private static LandlordPromptEventDef buildLocalJournalistEvent() {
        Map<LandlordPromptOption, String> options = new EnumMap<>(LandlordPromptOption.class);
        options.put(LandlordPromptOption.A, "Full Transparency — Let them see everything");
        options.put(LandlordPromptOption.B, "Controlled Access — Show the best bits only");
        options.put(LandlordPromptOption.C, "Decline — No journalists tonight");

        Map<LandlordPromptOption, Map<LandlordPromptResultType, LandlordPromptOutcome>> outcomes = new EnumMap<>(LandlordPromptOption.class);

        // Option A outcomes
        Map<LandlordPromptResultType, LandlordPromptOutcome> aOutcomes = new EnumMap<>(LandlordPromptResultType.class);
        aOutcomes.put(LandlordPromptResultType.GOOD, new LandlordPromptOutcome(
                LandlordPromptResultType.GOOD,
                LandlordPromptEffectPackage.builder().reputation(4).chaos(-2).build(),
                List.of(
                        "The journalist loved the honesty. Glowing write-up incoming.",
                        "Full access paid off—your openness became the story's highlight.",
                        "Transparency built trust. The article praised your authenticity."
                )
        ));
        aOutcomes.put(LandlordPromptResultType.NEUTRAL, new LandlordPromptOutcome(
                LandlordPromptResultType.NEUTRAL,
                LandlordPromptEffectPackage.builder().reputation(2).build(),
                List.of(
                        "They appreciated the access. Nothing dramatic, but fair coverage.",
                        "The journalist took notes. Neutral piece, no drama.",
                        "Open doors got you a balanced mention—nothing groundbreaking."
                )
        ));
        aOutcomes.put(LandlordPromptResultType.BAD, new LandlordPromptOutcome(
                LandlordPromptResultType.BAD,
                LandlordPromptEffectPackage.builder().reputation(-4).chaos(2).supplierTrust(-1).build(),
                List.of(
                        "They saw too much. The piece highlighted all the rough edges.",
                        "Full access backfired—minor issues became headline problems.",
                        "The journalist found every flaw and put it in print."
                )
        ));
        outcomes.put(LandlordPromptOption.A, aOutcomes);

        // Option B outcomes
        Map<LandlordPromptResultType, LandlordPromptOutcome> bOutcomes = new EnumMap<>(LandlordPromptResultType.class);
        bOutcomes.put(LandlordPromptResultType.GOOD, new LandlordPromptOutcome(
                LandlordPromptResultType.GOOD,
                LandlordPromptEffectPackage.builder().reputation(3).build(),
                List.of(
                        "Perfect control. They saw the highlights and wrote a solid piece.",
                        "Your curation worked—focused access led to focused praise.",
                        "Controlled tour, positive outcome. The write-up stayed on message."
                )
        ));
        bOutcomes.put(LandlordPromptResultType.NEUTRAL, new LandlordPromptOutcome(
                LandlordPromptResultType.NEUTRAL,
                LandlordPromptEffectPackage.builder().reputation(1).build(),
                List.of(
                        "They got enough for a short mention. Nothing memorable.",
                        "Controlled access kept things safe—bland but harmless.",
                        "Limited tour, limited impact. Minimal coverage followed."
                )
        ));
        bOutcomes.put(LandlordPromptResultType.BAD, new LandlordPromptOutcome(
                LandlordPromptResultType.BAD,
                LandlordPromptEffectPackage.builder().reputation(-3).build(),
                List.of(
                        "They sensed you were hiding something. The piece felt defensive.",
                        "Too controlled—the journalist called it 'rehearsed' in the article.",
                        "Your curation backfired. Readers questioned what you didn't show."
                )
        ));
        outcomes.put(LandlordPromptOption.B, bOutcomes);

        // Option C outcomes
        Map<LandlordPromptResultType, LandlordPromptOutcome> cOutcomes = new EnumMap<>(LandlordPromptResultType.class);
        cOutcomes.put(LandlordPromptResultType.GOOD, new LandlordPromptOutcome(
                LandlordPromptResultType.GOOD,
                LandlordPromptEffectPackage.builder().reputation(1).build(),
                List.of(
                        "They respected the boundary. No article, but no drama either.",
                        "Declining kept your cards close. Regulars appreciated the privacy.",
                        "Saying no worked—locals respect a pub that guards its own turf."
                )
        ));
        cOutcomes.put(LandlordPromptResultType.NEUTRAL, new LandlordPromptOutcome(
                LandlordPromptResultType.NEUTRAL,
                LandlordPromptEffectPackage.builder().build(),
                List.of(
                        "They moved on to another venue. No article, no consequences.",
                        "Declining was a non-event. Life continued as usual.",
                        "No press, no problem. Business carried on unchanged."
                )
        ));
        cOutcomes.put(LandlordPromptResultType.BAD, new LandlordPromptOutcome(
                LandlordPromptResultType.BAD,
                LandlordPromptEffectPackage.builder().reputation(-3).chaos(2).build(),
                List.of(
                        "They took it personally. The article questioned what you're hiding.",
                        "Declining made you look defensive—the piece speculated on problems.",
                        "Refusal backfired. The journalist's tone turned suspicious."
                )
        ));
        outcomes.put(LandlordPromptOption.C, cOutcomes);

        return new LandlordPromptEventDef(
                LandlordPromptEventId.LOCAL_JOURNALIST,
                "A local journalist asks for access tonight for a feature on independent venues under pressure.",
                options,
                outcomes
        );
    }

    private static LandlordPromptEventDef buildStaffDisputeEvent() {
        Map<LandlordPromptOption, String> options = new EnumMap<>(LandlordPromptOption.class);
        options.put(LandlordPromptOption.A, "Send Both Home — End it now, short-staffed tonight");
        options.put(LandlordPromptOption.B, "Mediate — Try to resolve it on the spot");
        options.put(LandlordPromptOption.C, "Ignore — Let them sort it out themselves");

        Map<LandlordPromptOption, Map<LandlordPromptResultType, LandlordPromptOutcome>> outcomes = new EnumMap<>(LandlordPromptOption.class);

        // Option A outcomes
        Map<LandlordPromptResultType, LandlordPromptOutcome> aOutcomes = new EnumMap<>(LandlordPromptResultType.class);
        aOutcomes.put(LandlordPromptResultType.GOOD, new LandlordPromptOutcome(
                LandlordPromptResultType.GOOD,
                LandlordPromptEffectPackage.builder().morale(3).chaos(-2).build(),
                List.of(
                        "Bold call. The team respected the clean break and refocused fast.",
                        "Sending them home killed the tension. The shift recovered.",
                        "Decisive action reset the room. The remaining staff stepped up."
                )
        ));
        aOutcomes.put(LandlordPromptResultType.NEUTRAL, new LandlordPromptOutcome(
                LandlordPromptResultType.NEUTRAL,
                LandlordPromptEffectPackage.builder().serviceEfficiency(-2).build(),
                List.of(
                        "Tension gone, but you're short-handed. Service took a hit tonight.",
                        "Clean break, but stretched thin. The shift limped to close.",
                        "Drama ended, efficiency dropped. A necessary tradeoff."
                )
        ));
        aOutcomes.put(LandlordPromptResultType.BAD, new LandlordPromptOutcome(
                LandlordPromptResultType.BAD,
                LandlordPromptEffectPackage.builder().morale(-4).chaos(3).build(),
                List.of(
                        "The team saw it as unfair. Morale tanked and chaos spiked.",
                        "Both sides felt punished. The remaining staff lost focus.",
                        "Overreaction. Sending them home stirred more resentment than resolution."
                )
        ));
        outcomes.put(LandlordPromptOption.A, aOutcomes);

        // Option B outcomes
        Map<LandlordPromptResultType, LandlordPromptOutcome> bOutcomes = new EnumMap<>(LandlordPromptResultType.class);
        bOutcomes.put(LandlordPromptResultType.GOOD, new LandlordPromptOutcome(
                LandlordPromptResultType.GOOD,
                LandlordPromptEffectPackage.builder().morale(4).build(),
                List.of(
                        "Perfect mediation. They shook hands and the shift ran smooth.",
                        "You defused it brilliantly. Both sides felt heard and moved on.",
                        "Crisis averted. Your intervention kept the team tight."
                )
        ));
        bOutcomes.put(LandlordPromptResultType.NEUTRAL, new LandlordPromptOutcome(
                LandlordPromptResultType.NEUTRAL,
                LandlordPromptEffectPackage.builder().morale(1).build(),
                List.of(
                        "Mediation worked okay. The tension eased but didn't vanish.",
                        "They're not friends, but they're working. Good enough for tonight.",
                        "Temporary peace achieved. They'll manage until shift end."
                )
        ));
        bOutcomes.put(LandlordPromptResultType.BAD, new LandlordPromptOutcome(
                LandlordPromptResultType.BAD,
                LandlordPromptEffectPackage.builder().reputation(-3).chaos(3).build(),
                List.of(
                        "Mediation failed publicly. Guests noticed, and word spread.",
                        "You tried, but it escalated. Both reputation and chaos took hits.",
                        "Intervention backfired. The argument became a spectacle."
                )
        ));
        outcomes.put(LandlordPromptOption.B, bOutcomes);

        // Option C outcomes
        Map<LandlordPromptResultType, LandlordPromptOutcome> cOutcomes = new EnumMap<>(LandlordPromptResultType.class);
        cOutcomes.put(LandlordPromptResultType.GOOD, new LandlordPromptOutcome(
                LandlordPromptResultType.GOOD,
                LandlordPromptEffectPackage.builder().morale(1).build(),
                List.of(
                        "They sorted it out on their own. Respect earned for giving space.",
                        "Ignoring it worked—they cooled down and finished the shift.",
                        "Sometimes the best call is no call. They handled it."
                )
        ));
        cOutcomes.put(LandlordPromptResultType.NEUTRAL, new LandlordPromptOutcome(
                LandlordPromptResultType.NEUTRAL,
                LandlordPromptEffectPackage.builder().morale(-2).build(),
                List.of(
                        "They didn't resolve it. Tension lingered but stayed manageable.",
                        "Ignoring it left unresolved feelings. Morale dipped slightly.",
                        "No resolution, no escalation. An awkward equilibrium."
                )
        ));
        cOutcomes.put(LandlordPromptResultType.BAD, new LandlordPromptOutcome(
                LandlordPromptResultType.BAD,
                LandlordPromptEffectPackage.builder().chaos(4).morale(-3).build(),
                List.of(
                        "Ignoring it made it worse. The fight escalated and chaos erupted.",
                        "No intervention meant no control. Things spiraled publicly.",
                        "They needed a manager. Ignoring it let the problem explode."
                )
        ));
        outcomes.put(LandlordPromptOption.C, cOutcomes);

        return new LandlordPromptEventDef(
                LandlordPromptEventId.STAFF_DISPUTE,
                "Two senior staff argue mid-shift.",
                options,
                outcomes
        );
    }

    private static LandlordPromptEventDef buildCorporateBookingEvent() {
        Map<LandlordPromptOption, String> options = new EnumMap<>(LandlordPromptOption.class);
        options.put(LandlordPromptOption.A, "Accept — Take the money and the risk");
        options.put(LandlordPromptOption.B, "Accept With Conditions — Control the booking");
        options.put(LandlordPromptOption.C, "Decline — Protect your regulars and vibe");

        Map<LandlordPromptOption, Map<LandlordPromptResultType, LandlordPromptOutcome>> outcomes = new EnumMap<>(LandlordPromptOption.class);

        // Option A outcomes
        Map<LandlordPromptResultType, LandlordPromptOutcome> aOutcomes = new EnumMap<>(LandlordPromptResultType.class);
        aOutcomes.put(LandlordPromptResultType.GOOD, new LandlordPromptOutcome(
                LandlordPromptResultType.GOOD,
                LandlordPromptEffectPackage.builder().cash(1200).reputation(3).build(),
                List.of(
                        "Perfect booking. Big spenders, zero drama, glowing reviews.",
                        "Corporate group loved the pub. Cash in, reputation up.",
                        "High-value booking went flawlessly. Everyone left happy."
                )
        ));
        aOutcomes.put(LandlordPromptResultType.NEUTRAL, new LandlordPromptOutcome(
                LandlordPromptResultType.NEUTRAL,
                LandlordPromptEffectPackage.builder().cash(700).chaos(2).build(),
                List.of(
                        "Money came through, but they were loud. Regulars felt sidelined.",
                        "Corporate cash landed, but the vibe took a hit. Chaotic night.",
                        "Profitable but messy. The booking disrupted your usual crowd."
                )
        ));
        aOutcomes.put(LandlordPromptResultType.BAD, new LandlordPromptOutcome(
                LandlordPromptResultType.BAD,
                LandlordPromptEffectPackage.builder().cash(400).chaos(5).reputation(-4).build(),
                List.of(
                        "Disaster. They trashed the vibe, upset regulars, and left a mess.",
                        "Corporate booking spiraled. Money wasn't worth the chaos and reputation hit.",
                        "Ugly night. High chaos, angry regulars, minimal profit."
                )
        ));
        outcomes.put(LandlordPromptOption.A, aOutcomes);

        // Option B outcomes
        Map<LandlordPromptResultType, LandlordPromptOutcome> bOutcomes = new EnumMap<>(LandlordPromptResultType.class);
        bOutcomes.put(LandlordPromptResultType.GOOD, new LandlordPromptOutcome(
                LandlordPromptResultType.GOOD,
                LandlordPromptEffectPackage.builder().cash(900).chaos(-1).build(),
                List.of(
                        "Smart conditions kept it tight. Good profit, no problems.",
                        "Controlled booking ran smooth. Cash in, chaos down.",
                        "Conditions worked. Professional group, clean execution."
                )
        ));
        bOutcomes.put(LandlordPromptResultType.NEUTRAL, new LandlordPromptOutcome(
                LandlordPromptResultType.NEUTRAL,
                LandlordPromptEffectPackage.builder().cash(600).build(),
                List.of(
                        "Conditions kept it manageable. Decent profit, no drama.",
                        "Controlled booking was fine. Average night, average cash.",
                        "Middling result. Conditions limited both profit and problems."
                )
        ));
        bOutcomes.put(LandlordPromptResultType.BAD, new LandlordPromptOutcome(
                LandlordPromptResultType.BAD,
                LandlordPromptEffectPackage.builder().reputation(-3).build(),
                List.of(
                        "Conditions felt too tight. They complained publicly about restrictions.",
                        "Your rules annoyed them. Negative reviews followed.",
                        "Overcontrolled. They felt micromanaged and said so online."
                )
        ));
        outcomes.put(LandlordPromptOption.B, bOutcomes);

        // Option C outcomes
        Map<LandlordPromptResultType, LandlordPromptOutcome> cOutcomes = new EnumMap<>(LandlordPromptResultType.class);
        cOutcomes.put(LandlordPromptResultType.GOOD, new LandlordPromptOutcome(
                LandlordPromptResultType.GOOD,
                LandlordPromptEffectPackage.builder().reputation(3).morale(2).build(),
                List.of(
                        "Declining protected your identity. Regulars and staff respected the choice.",
                        "Saying no earned loyalty. Your crowd appreciated prioritizing vibe over cash.",
                        "Standing firm paid off. Reputation and morale climbed."
                )
        ));
        cOutcomes.put(LandlordPromptResultType.NEUTRAL, new LandlordPromptOutcome(
                LandlordPromptResultType.NEUTRAL,
                LandlordPromptEffectPackage.builder().reputation(1).build(),
                List.of(
                        "Declining was neutral. No money, but no problems either.",
                        "Saying no kept things simple. No drama, modest reputation bump.",
                        "Safe decline. Nothing gained, nothing lost."
                )
        ));
        cOutcomes.put(LandlordPromptResultType.BAD, new LandlordPromptOutcome(
                LandlordPromptResultType.BAD,
                LandlordPromptEffectPackage.builder().reputation(-3).build(),
                List.of(
                        "They were offended and told everyone. Bad press followed.",
                        "Declining backfired. Word spread that you're unwelcoming.",
                        "Refusal damaged your reputation. They painted you as difficult."
                )
        ));
        outcomes.put(LandlordPromptOption.C, cOutcomes);

        return new LandlordPromptEventDef(
                LandlordPromptEventId.CORPORATE_BOOKING,
                "Corporate group wants late booking. Big money. Risky.",
                options,
                outcomes
        );
    }

    private static LandlordPromptEventDef buildHealthInspectorEvent() {
        Map<LandlordPromptOption, String> options = new EnumMap<>(LandlordPromptOption.class);
        options.put(LandlordPromptOption.A, "Cooperate — Full access, nothing to hide");
        options.put(LandlordPromptOption.B, "Delay — Ask for more time to prepare");
        options.put(LandlordPromptOption.C, "Defensive — Challenge their authority");

        Map<LandlordPromptOption, Map<LandlordPromptResultType, LandlordPromptOutcome>> outcomes = new EnumMap<>(LandlordPromptOption.class);

        // Option A outcomes
        Map<LandlordPromptResultType, LandlordPromptOutcome> aOutcomes = new EnumMap<>(LandlordPromptResultType.class);
        aOutcomes.put(LandlordPromptResultType.GOOD, new LandlordPromptOutcome(
                LandlordPromptResultType.GOOD,
                LandlordPromptEffectPackage.builder().reputation(5).build(),
                List.of(
                        "Clean inspection, glowing report. Your cooperation was noted.",
                        "Full access paid off. Inspector praised your transparency.",
                        "Perfect cooperation. Report highlighted your high standards."
                )
        ));
        aOutcomes.put(LandlordPromptResultType.NEUTRAL, new LandlordPromptOutcome(
                LandlordPromptResultType.NEUTRAL,
                LandlordPromptEffectPackage.builder().reputation(1).build(),
                List.of(
                        "Inspection passed. No drama, minor report filed.",
                        "Cooperation was fine. Standard pass, nothing special.",
                        "Clean enough. Report was neutral, no issues raised."
                )
        ));
        aOutcomes.put(LandlordPromptResultType.BAD, new LandlordPromptOutcome(
                LandlordPromptResultType.BAD,
                LandlordPromptEffectPackage.builder().reputation(-5).chaos(2).build(),
                List.of(
                        "They found violations you didn't know existed. Report was damaging.",
                        "Full access exposed problems. Public report hurt your reputation badly.",
                        "Cooperation backfired. Inspector flagged multiple issues publicly."
                )
        ));
        outcomes.put(LandlordPromptOption.A, aOutcomes);

        // Option B outcomes
        Map<LandlordPromptResultType, LandlordPromptOutcome> bOutcomes = new EnumMap<>(LandlordPromptResultType.class);
        bOutcomes.put(LandlordPromptResultType.GOOD, new LandlordPromptOutcome(
                LandlordPromptResultType.GOOD,
                LandlordPromptEffectPackage.builder().build(),
                List.of(
                        "They accepted the delay. Time bought, no penalties.",
                        "Delay granted. You fixed issues before the real inspection.",
                        "Extra time helped. Inspection went smoothly later."
                )
        ));
        bOutcomes.put(LandlordPromptResultType.NEUTRAL, new LandlordPromptOutcome(
                LandlordPromptResultType.NEUTRAL,
                LandlordPromptEffectPackage.builder().reputation(-2).build(),
                List.of(
                        "Delay noted in report. Minor reputation ding for evasiveness.",
                        "They let you delay but marked it. Slight reputation loss.",
                        "Time granted, suspicion raised. Report mentioned hesitation."
                )
        ));
        bOutcomes.put(LandlordPromptResultType.BAD, new LandlordPromptOutcome(
                LandlordPromptResultType.BAD,
                LandlordPromptEffectPackage.builder().reputation(-6).build(),
                List.of(
                        "Delay denied. They came back harder and found everything.",
                        "Asking for time made them suspicious. Thorough inspection destroyed your rep.",
                        "Delay backfired badly. Report was harsh and public."
                )
        ));
        outcomes.put(LandlordPromptOption.B, bOutcomes);

        // Option C outcomes
        Map<LandlordPromptResultType, LandlordPromptOutcome> cOutcomes = new EnumMap<>(LandlordPromptResultType.class);
        cOutcomes.put(LandlordPromptResultType.GOOD, new LandlordPromptOutcome(
                LandlordPromptResultType.GOOD,
                LandlordPromptEffectPackage.builder().reputation(1).build(),
                List.of(
                        "Your pushback caught them off guard. They backed down.",
                        "Defensive stance worked. They left without escalating.",
                        "Challenging them rattled their confidence. They retreated."
                )
        ));
        cOutcomes.put(LandlordPromptResultType.NEUTRAL, new LandlordPromptOutcome(
                LandlordPromptResultType.NEUTRAL,
                LandlordPromptEffectPackage.builder().reputation(-2).build(),
                List.of(
                        "Defensive approach soured the interaction. Report was lukewarm.",
                        "Pushback created tension. Neutral pass but poor rapport.",
                        "Challenging authority didn't help. Minor reputation loss."
                )
        ));
        cOutcomes.put(LandlordPromptResultType.BAD, new LandlordPromptOutcome(
                LandlordPromptResultType.BAD,
                LandlordPromptEffectPackage.builder().reputation(-6).chaos(4).build(),
                List.of(
                        "They escalated. Full investigation, devastating report.",
                        "Defensive stance enraged them. Report was brutal and public.",
                        "Challenging authority was a disaster. Reputation collapsed."
                )
        ));
        outcomes.put(LandlordPromptOption.C, cOutcomes);

        return new LandlordPromptEventDef(
                LandlordPromptEventId.HEALTH_INSPECTOR,
                "Health inspector arrives unannounced during peak hours.",
                options,
                outcomes
        );
    }

    private static LandlordPromptEventDef buildRegularComplaintEvent() {
        Map<LandlordPromptOption, String> options = new EnumMap<>(LandlordPromptOption.class);
        options.put(LandlordPromptOption.A, "Apologise & Comp — Fix it with goodwill");
        options.put(LandlordPromptOption.B, "Stand Firm — Back your staff's call");
        options.put(LandlordPromptOption.C, "Remove Them — End the problem now");

        Map<LandlordPromptOption, Map<LandlordPromptResultType, LandlordPromptOutcome>> outcomes = new EnumMap<>(LandlordPromptOption.class);

        // Option A outcomes
        Map<LandlordPromptResultType, LandlordPromptOutcome> aOutcomes = new EnumMap<>(LandlordPromptResultType.class);
        aOutcomes.put(LandlordPromptResultType.GOOD, new LandlordPromptOutcome(
                LandlordPromptResultType.GOOD,
                LandlordPromptEffectPackage.builder().reputation(4).chaos(-2).build(),
                List.of(
                        "Perfect de-escalation. Regular left happy, staff felt supported.",
                        "Comp worked beautifully. Reputation boosted, chaos calmed.",
                        "Graceful resolution. Everyone moved on in good spirits."
                )
        ));
        aOutcomes.put(LandlordPromptResultType.NEUTRAL, new LandlordPromptOutcome(
                LandlordPromptResultType.NEUTRAL,
                LandlordPromptEffectPackage.builder().cash(-100).build(),
                List.of(
                        "Comp stopped the complaint but cost you. No reputation gain.",
                        "Free drinks quieted them. Money lost, nothing gained.",
                        "Goodwill gesture absorbed the problem but drained cash."
                )
        ));
        aOutcomes.put(LandlordPromptResultType.BAD, new LandlordPromptOutcome(
                LandlordPromptResultType.BAD,
                LandlordPromptEffectPackage.builder().reputation(-3).build(),
                List.of(
                        "Apologizing made you look weak. Regular left unsatisfied, word spread.",
                        "Comp failed to appease them. Public complaint continued online.",
                        "Goodwill gesture backfired. They demanded more and trashed you anyway."
                )
        ));
        outcomes.put(LandlordPromptOption.A, aOutcomes);

        // Option B outcomes
        Map<LandlordPromptResultType, LandlordPromptOutcome> bOutcomes = new EnumMap<>(LandlordPromptResultType.class);
        bOutcomes.put(LandlordPromptResultType.GOOD, new LandlordPromptOutcome(
                LandlordPromptResultType.GOOD,
                LandlordPromptEffectPackage.builder().reputation(3).build(),
                List.of(
                        "Standing firm earned respect. Regular backed down, staff felt valued.",
                        "Your support for staff resonated. Crowd respected the boundary.",
                        "Firm stance worked. Regular left quietly, reputation held strong."
                )
        ));
        bOutcomes.put(LandlordPromptResultType.NEUTRAL, new LandlordPromptOutcome(
                LandlordPromptResultType.NEUTRAL,
                LandlordPromptEffectPackage.builder().build(),
                List.of(
                        "Standing firm was neutral. No drama, no gain.",
                        "Firm stance ended it. Regular left, nothing changed.",
                        "Backing staff was fine. Night continued without incident."
                )
        ));
        bOutcomes.put(LandlordPromptResultType.BAD, new LandlordPromptOutcome(
                LandlordPromptResultType.BAD,
                LandlordPromptEffectPackage.builder().reputation(-4).chaos(3).build(),
                List.of(
                        "Standing firm escalated it. Public scene damaged reputation badly.",
                        "Your firmness looked stubborn. Regular's complaint went viral.",
                        "Backing staff backfired. Argument spiraled, reputation tanked."
                )
        ));
        outcomes.put(LandlordPromptOption.B, bOutcomes);

        // Option C outcomes
        Map<LandlordPromptResultType, LandlordPromptOutcome> cOutcomes = new EnumMap<>(LandlordPromptResultType.class);
        cOutcomes.put(LandlordPromptResultType.GOOD, new LandlordPromptOutcome(
                LandlordPromptResultType.GOOD,
                LandlordPromptEffectPackage.builder().morale(3).reputation(1).build(),
                List.of(
                        "Clean removal. Staff loved your decisiveness, crowd respected it.",
                        "Removing them was bold. Team morale surged, reputation held.",
                        "Swift action protected the vibe. Staff and regulars approved."
                )
        ));
        cOutcomes.put(LandlordPromptResultType.NEUTRAL, new LandlordPromptOutcome(
                LandlordPromptResultType.NEUTRAL,
                LandlordPromptEffectPackage.builder().reputation(-2).build(),
                List.of(
                        "Removal ended the problem but looked harsh. Minor reputation loss.",
                        "Ejecting them was quick but felt aggressive. Slight backlash.",
                        "Swift removal solved it but left a sour impression."
                )
        ));
        cOutcomes.put(LandlordPromptResultType.BAD, new LandlordPromptOutcome(
                LandlordPromptResultType.BAD,
                LandlordPromptEffectPackage.builder().reputation(-6).chaos(4).build(),
                List.of(
                        "Removal escalated into a spectacle. Reputation destroyed, chaos spiked.",
                        "Ejecting them backfired badly. Public drama, viral complaint.",
                        "Harsh removal became a PR disaster. Reputation collapsed."
                )
        ));
        outcomes.put(LandlordPromptOption.C, cOutcomes);

        return new LandlordPromptEventDef(
                LandlordPromptEventId.REGULAR_COMPLAINT,
                "A regular makes a loud public complaint about service.",
                options,
                outcomes
        );
    }

    private static LandlordPromptEventDef buildStaffReferralEvent() {
        Map<LandlordPromptOption, String> options = new EnumMap<>(LandlordPromptOption.class);
        options.put(LandlordPromptOption.A, "Hire Immediately — Trust your team's judgment");
        options.put(LandlordPromptOption.B, "Trial Shift — Test them first");
        options.put(LandlordPromptOption.C, "Decline — Not hiring right now");

        Map<LandlordPromptOption, Map<LandlordPromptResultType, LandlordPromptOutcome>> outcomes = new EnumMap<>(LandlordPromptOption.class);

        // Option A outcomes
        Map<LandlordPromptResultType, LandlordPromptOutcome> aOutcomes = new EnumMap<>(LandlordPromptResultType.class);
        aOutcomes.put(LandlordPromptResultType.GOOD, new LandlordPromptOutcome(
                LandlordPromptResultType.GOOD,
                LandlordPromptEffectPackage.builder().morale(2).serviceEfficiency(2).build(),
                List.of(
                        "Perfect hire. New staff clicked instantly, service improved.",
                        "Trusting your team paid off. New hire is a star.",
                        "Immediate hire was brilliant. Morale and efficiency surged."
                )
        ));
        aOutcomes.put(LandlordPromptResultType.NEUTRAL, new LandlordPromptOutcome(
                LandlordPromptResultType.NEUTRAL,
                LandlordPromptEffectPackage.builder().build(),
                List.of(
                        "New hire is fine. Nothing special, nothing bad.",
                        "Immediate hire was okay. Average addition to the team.",
                        "Trusting your team was neutral. New hire blends in."
                )
        ));
        aOutcomes.put(LandlordPromptResultType.BAD, new LandlordPromptOutcome(
                LandlordPromptResultType.BAD,
                LandlordPromptEffectPackage.builder().morale(-3).serviceEfficiency(-1).build(),
                List.of(
                        "Bad hire. New staff caused friction and slowed service.",
                        "Immediate hire backfired. Morale dropped, efficiency tanked.",
                        "Trusting your team didn't work. New hire is a problem."
                )
        ));
        outcomes.put(LandlordPromptOption.A, aOutcomes);

        // Option B outcomes
        Map<LandlordPromptResultType, LandlordPromptOutcome> bOutcomes = new EnumMap<>(LandlordPromptResultType.class);
        bOutcomes.put(LandlordPromptResultType.GOOD, new LandlordPromptOutcome(
                LandlordPromptResultType.GOOD,
                LandlordPromptEffectPackage.builder().serviceEfficiency(3).build(),
                List.of(
                        "Trial shift proved their worth. Efficiency boost confirmed.",
                        "Testing them first worked. Strong hire after validation.",
                        "Trial shift was smart. New staff earned their place."
                )
        ));
        bOutcomes.put(LandlordPromptResultType.NEUTRAL, new LandlordPromptOutcome(
                LandlordPromptResultType.NEUTRAL,
                LandlordPromptEffectPackage.builder().build(),
                List.of(
                        "Trial shift was fine. No hire, no loss.",
                        "Testing them was neutral. They didn't stand out.",
                        "Trial shift ended without commitment. No impact."
                )
        ));
        bOutcomes.put(LandlordPromptResultType.BAD, new LandlordPromptOutcome(
                LandlordPromptResultType.BAD,
                LandlordPromptEffectPackage.builder().morale(-2).build(),
                List.of(
                        "Trial shift upset the team. They felt you didn't trust their judgment.",
                        "Testing them damaged morale. Referring staff felt undermined.",
                        "Trial requirement backfired. Team saw it as lack of trust."
                )
        ));
        outcomes.put(LandlordPromptOption.B, bOutcomes);

        // Option C outcomes
        Map<LandlordPromptResultType, LandlordPromptOutcome> cOutcomes = new EnumMap<>(LandlordPromptResultType.class);
        cOutcomes.put(LandlordPromptResultType.GOOD, new LandlordPromptOutcome(
                LandlordPromptResultType.GOOD,
                LandlordPromptEffectPackage.builder().reputation(1).build(),
                List.of(
                        "Declining was wise. Team understood you're selective.",
                        "Not hiring now was the right call. No drama, reputation stable.",
                        "Saying no protected standards. Team respected the boundary."
                )
        ));
        cOutcomes.put(LandlordPromptResultType.NEUTRAL, new LandlordPromptOutcome(
                LandlordPromptResultType.NEUTRAL,
                LandlordPromptEffectPackage.builder().morale(-2).build(),
                List.of(
                        "Declining was fine but disappointed the referring staff.",
                        "Not hiring now was neutral. Minor morale dip.",
                        "Saying no ended it. Team felt slightly undervalued."
                )
        ));
        cOutcomes.put(LandlordPromptResultType.BAD, new LandlordPromptOutcome(
                LandlordPromptResultType.BAD,
                LandlordPromptEffectPackage.builder().morale(-4).build(),
                List.of(
                        "Declining was seen as disrespect. Team morale tanked.",
                        "Not hiring now hurt feelings badly. Referring staff felt rejected.",
                        "Saying no backfired. Team trust and morale collapsed."
                )
        ));
        outcomes.put(LandlordPromptOption.C, cOutcomes);

        return new LandlordPromptEventDef(
                LandlordPromptEventId.STAFF_REFERRAL,
                "A trusted staff member refers a friend who needs work.",
                options,
                outcomes
        );
    }
}
