package com.luxzentao.javabar.core;

// StaffRelationship.java
import java.io.Serializable;

/**
 * Represents the relationship between two staff members.
 */
public class StaffRelationship implements Serializable {
    private static final long serialVersionUID = 1L;

    public enum RelationshipState {
        NEUTRAL,           // No particular relationship
        WORKS_WELL,        // Work well together
        DISLIKES,          // Don't get along
        DATING,            // Romantic relationship
        BREAKUP,           // Recently ended romantic relationship
        MARRIED            // Married (flavor only)
    }

    private final int staffId1;
    private final int staffId2;
    private double affinity;  // -10.0 to +10.0, represents how well they work together
    private RelationshipState state;
    private int weeksSinceStateChange;

    public StaffRelationship(int staffId1, int staffId2) {
        this.staffId1 = Math.min(staffId1, staffId2);  // Normalize ordering
        this.staffId2 = Math.max(staffId1, staffId2);
        this.affinity = 0.0;
        this.state = RelationshipState.NEUTRAL;
        this.weeksSinceStateChange = 0;
    }

    public int getStaffId1() { return staffId1; }
    public int getStaffId2() { return staffId2; }
    public double getAffinity() { return affinity; }
    public RelationshipState getState() { return state; }
    public int getWeeksSinceStateChange() { return weeksSinceStateChange; }

    public void adjustAffinity(double delta) {
        this.affinity = Math.max(-10.0, Math.min(10.0, this.affinity + delta));
    }

    public void setState(RelationshipState newState) {
        if (this.state != newState) {
            this.state = newState;
            this.weeksSinceStateChange = 0;
        }
    }

    public void incrementWeeksSinceStateChange() {
        this.weeksSinceStateChange++;
    }

    public boolean involves(int staffId) {
        return staffId == staffId1 || staffId == staffId2;
    }

    public int getOtherStaffId(int staffId) {
        if (staffId == staffId1) return staffId2;
        if (staffId == staffId2) return staffId1;
        return -1;
    }

    @Override
    public String toString() {
        return "Staff #" + staffId1 + " & #" + staffId2 + " | " + state + " | affinity: " + String.format("%.1f", affinity);
    }
}
