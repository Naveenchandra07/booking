package com.meetingorganizer.bookingrequests;

import java.time.LocalDateTime;

public class MeetingRequestPojo {
	
	LocalDateTime requestSubmissionTime;
	String empID;
	LocalDateTime meetingStartTime;
	LocalDateTime meetingEndTime;
	
	public MeetingRequestPojo(LocalDateTime requestSubmissionTime, String empID, LocalDateTime meetingStartTime, LocalDateTime meetingEndTime)
	{
		this.requestSubmissionTime = requestSubmissionTime;
		this.empID = empID;
		this.meetingStartTime = meetingStartTime;
		this.meetingEndTime = meetingEndTime;
	}
	
	public LocalDateTime getRequestSubmissionTime() {
		return requestSubmissionTime;
	}
	public void setRequestSubmissionTime(LocalDateTime requestSubmissionTime) {
		this.requestSubmissionTime = requestSubmissionTime;
	}
	public String getEmpID() {
		return empID;
	}
	public void setEmpID(String empID) {
		this.empID = empID;
	}
	public LocalDateTime getMeetingStartTime() {
		return meetingStartTime;
	}
	public void setMeetingStartTime(LocalDateTime meetingStartTime) {
		this.meetingStartTime = meetingStartTime;
	}
	public LocalDateTime getMeetingEndTime() {
		return meetingEndTime;
	}
	public void setMeetingEndTime(LocalDateTime meetingEndTime) {
		this.meetingEndTime = meetingEndTime;
	}
	
	@Override
	public String toString() {
		return "Employee ID: " + empID + ", Submission Time: " + requestSubmissionTime.toString() + ", Meeting Start Time: " + meetingStartTime.toString() + ", Meeting End Time: " + meetingEndTime.toString();
	}
}
