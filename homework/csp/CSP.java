package csp;

import java.util.ArrayList;
import java.util.List;
import java.time.LocalDate;
import java.util.Set;

/**
 * CSP: Calendar Satisfaction Problem Solver Provides a solution for scheduling
 * some n meetings in a given period of time and according to some unary and
 * binary constraints on the dates of each meeting.
 */
public class CSP {

    /**
     * Public interface for the CSP solver in which the number of meetings, range of
     * allowable dates for each meeting, and constraints on meeting times are
     * specified.
     * 
     * @param nMeetings   The number of meetings that must be scheduled, indexed
     *                    from 0 to n-1
     * @param rangeStart  The start date (inclusive) of the domains of each of the n
     *                    meeting-variables
     * @param rangeEnd    The end date (inclusive) of the domains of each of the n
     *                    meeting-variables
     * @param constraints Date constraints on the meeting times (unary and binary
     *                    for this assignment)
     * @return A list of dates that satisfies each of the constraints for each of
     *         the n meetings, indexed by the variable they satisfy, or null if no
     *         solution exists.
     */
    public static List<LocalDate> solve(int nMeetings, LocalDate rangeStart, LocalDate rangeEnd,
            Set<DateConstraint> constraints) {
        List<MeetingVariable> variables = populateVariables(new ArrayList<MeetingVariable>(), nMeetings, rangeStart,
                rangeEnd);
        handlePreprocessing(variables, constraints);
        return handleAssignments(variables, constraints);
    }

    /** Helper Methods */

    private static List<MeetingVariable> populateVariables(ArrayList<MeetingVariable> variables, int totalMeetings,
            LocalDate startDate, LocalDate endDate) {
        for (int i = 0; i < totalMeetings; i++) {
            variables.add(new MeetingVariable(startDate, endDate));
        }
        return variables;
    }

    private static List<MeetingVariable> handlePreprocessing(List<MeetingVariable> variables,
            Set<DateConstraint> constraints) {
        for (MeetingVariable meeting : variables) {
            List<LocalDate> meetingDomain = meeting.domain;
            for (int i = 0; i < meetingDomain.size(); i++) {
                LocalDate date = meetingDomain.get(i);
                for (DateConstraint constraint : constraints) {
                    if (constraint.arity() == 1) {
                        LocalDate rightDate = ((UnaryDateConstraint) constraint).R_VAL;
                        if (!isValidConstraint(date, constraint, rightDate)) {
                            meetingDomain.remove(date);
                            i--;
                            break;
                        }
                    }
                }
            }
        }
        return variables;
    }

    private static boolean isValidConstraint(LocalDate leftDate, DateConstraint constraint, LocalDate rightDate) {
        boolean isValid = false;
        switch (constraint.OP) {
        case "==":
            if (leftDate.isEqual(rightDate))
                isValid = true;
            break;
        case "!=":
            if (!leftDate.isEqual(rightDate))
                isValid = true;
            break;
        case ">":
            if (leftDate.isAfter(rightDate))
                isValid = true;
            break;
        case "<":
            if (leftDate.isBefore(rightDate))
                isValid = true;
            break;
        case ">=":
            if (leftDate.isAfter(rightDate) || leftDate.isEqual(rightDate))
                isValid = true;
            break;
        case "<=":
            if (leftDate.isBefore(rightDate) || leftDate.isEqual(rightDate))
                isValid = true;
            break;
        }
        return isValid;
    }

    private static List<LocalDate> gatherCompleteAssignment(List<LocalDate> completeAssignment,
            List<MeetingVariable> variables) {
        for (MeetingVariable meeting : variables) {
            completeAssignment.add(meeting.getAssignment());
        }
        return completeAssignment;
    }

    private static List<LocalDate> handleAssignments(List<MeetingVariable> variables, Set<DateConstraint> constraints) {
        if (variables.get(variables.size() - 1).getAssignment() != null) {
            return gatherCompleteAssignment(new ArrayList<LocalDate>(), variables);
        }
        MeetingVariable current = getCurrentMeeting(variables);
        for (LocalDate date : current.domain) {
            current.setAssignment(date);
            if (checkConsistency(variables, constraints)) {
                List<LocalDate> completeAssignment = handleAssignments(variables, constraints);
                if (completeAssignment != null) {
                    return completeAssignment;
                }
            }
            current.removeAssignment();
        }
        return null;
    }

    private static MeetingVariable getCurrentMeeting(List<MeetingVariable> variables) {
        for (MeetingVariable meeting : variables) {
            if (meeting.getAssignment() == null) {
                return meeting;
            }
        }
        return null;
    }

    public static boolean checkConsistency(List<MeetingVariable> variables, Set<DateConstraint> constraints) {
        boolean isValid = false;
        for (DateConstraint constraint : constraints) {
            LocalDate leftDate = variables.get(constraint.L_VAL).getAssignment();
            LocalDate rightDate = (constraint.arity() == 1) ? ((UnaryDateConstraint) constraint).R_VAL
                    : variables.get(((BinaryDateConstraint) constraint).R_VAL).getAssignment();
            isValid = false;
            if (leftDate == null || rightDate == null) {
                isValid = true;
                continue;
            }
            switch (constraint.OP) {
            case "==":
                if (leftDate.isEqual(rightDate))
                    isValid = true;
                break;
            case "!=":
                if (!leftDate.isEqual(rightDate))
                    isValid = true;
                break;
            case ">":
                if (leftDate.isAfter(rightDate))
                    isValid = true;
                break;
            case "<":
                if (leftDate.isBefore(rightDate))
                    isValid = true;
                break;
            case ">=":
                if (leftDate.isAfter(rightDate) || leftDate.isEqual(rightDate))
                    isValid = true;
                break;
            case "<=":
                if (leftDate.isBefore(rightDate) || leftDate.isEqual(rightDate))
                    isValid = true;
                break;
            }
            if (!isValid) {
                return false;
            }
        }
        return isValid;
    }

    /** Private Classes */
    private static class MeetingVariable {
        List<LocalDate> domain;
        LocalDate assignment;

        MeetingVariable(LocalDate startDate, LocalDate endDate) {
            this.domain = populateDomain(new ArrayList<LocalDate>(), startDate, endDate);
        }

        private List<LocalDate> populateDomain(List<LocalDate> domain, LocalDate startDate, LocalDate endDate) {
            for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
                domain.add(date);
            }
            return domain;
        }

        public LocalDate getAssignment() {
            return this.assignment;
        }

        public void setAssignment(LocalDate date) {
            this.assignment = date;
        }

        public void removeAssignment() {
            this.assignment = null;
        }

    }

}
