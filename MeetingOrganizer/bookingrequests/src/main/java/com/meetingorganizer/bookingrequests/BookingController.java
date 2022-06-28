package com.meetingorganizer.bookingrequests;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
  
@RestController
@RequestMapping("/booking")
public class BookingController {
     
    @RequestMapping(
    	    value = "/requests", 
    	    method = RequestMethod.POST,
    	    consumes = "text/plain")
    public String processBatchBookings(@RequestBody String batchBookingRequest) {
    	Map<LocalDate, List<MeetingRequestPojo>> bookings = processBookings(batchBookingRequest);
    	return createResponse(bookings);
    }
    
    private String createResponse(Map<LocalDate, List<MeetingRequestPojo>> bookings) {
    	JSONArray responseObj = new JSONArray();
    	if (bookings != null && bookings.size() > 0)
		{
			for (LocalDate localDate : bookings.keySet())
			{
				JSONObject bookingObj = new JSONObject();
				bookingObj.put("data", localDate);
				List<MeetingRequestPojo> filteredMeetingRequestList = bookings.get(localDate);
				JSONArray bookingAry = new JSONArray();
				for (MeetingRequestPojo meetingRequestPojo : filteredMeetingRequestList)
				{
					JSONObject empObj = new JSONObject();
					empObj.put("emp_id", meetingRequestPojo.getEmpID());
					empObj.put("start_time", meetingRequestPojo.getMeetingStartTime().toLocalTime());
					empObj.put("end_time", meetingRequestPojo.getMeetingEndTime().toLocalTime());
					bookingAry.put(empObj);
				}
				bookingObj.put("bookings", bookingAry);
				responseObj.put(bookingObj);
			}
		}
		return responseObj.toString();
	}

	private Map<LocalDate, List<MeetingRequestPojo>> processBookings(String batchBookingRequest) {
    	List<MeetingRequestPojo> filteredMeetingRequestPojos = new ArrayList<>();
    	Map<LocalDate, List<MeetingRequestPojo>> filteredMeetingRequestMap = new TreeMap<>();
    	if (batchBookingRequest != null && batchBookingRequest.length() > 0)
    	{
    		String[] splitLines = batchBookingRequest.split("\\R");
    		LocalTime startHour = null;
    		LocalTime endHour = null;
    		
    		if (splitLines.length > 3)
    		{
    			String[] officeHoursRange = splitLines[0].split(" ");
    			List<MeetingRequestPojo> meetingRequestPojos = new ArrayList<>();
    			if (officeHoursRange.length == 2)
    			{
			        startHour = LocalTime.parse(officeHoursRange[0], DateTimeFormatter.ofPattern("HHmm"));
					endHour = LocalTime.parse(officeHoursRange[1], DateTimeFormatter.ofPattern("HHmm"));
    			}
    			Set<LocalDate> meetingDays = new HashSet<>();
    			for (int i=1; i<splitLines.length; i+=2)
    			{
    				String empID = splitLines[i].substring(splitLines[i].lastIndexOf(" ") + 1);
    				LocalDateTime submissionTime = LocalDateTime.parse(splitLines[i].substring(0, splitLines[i].lastIndexOf(" ")).trim(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    				LocalDateTime meetingStartTime = LocalDateTime.parse(splitLines[i + 1].substring(0, splitLines[i + 1].lastIndexOf(" ")), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
    				LocalDateTime meetingEndTime = meetingStartTime.plusHours(Integer.parseInt(splitLines[i + 1].substring(splitLines[i + 1].lastIndexOf(' ') + 1)));
    				meetingDays.add(meetingStartTime.toLocalDate());
    				MeetingRequestPojo meetingRequestPojo = new MeetingRequestPojo(submissionTime,
							empID,
							meetingStartTime,
							meetingEndTime);
					meetingRequestPojos.add(meetingRequestPojo);
    			}
    			Collections.sort(meetingRequestPojos, new Comparator<MeetingRequestPojo>() {
					@Override
					public int compare(MeetingRequestPojo o1, MeetingRequestPojo o2) {
						return o1.getRequestSubmissionTime().compareTo(o2.getRequestSubmissionTime());
					};
				});
    			final LocalTime finalStartHour = startHour;
    			final LocalTime finalEndHour = endHour;
    			meetingRequestPojos = meetingRequestPojos.stream().filter(meetingRequestPojo -> {
    				LocalDateTime startDate = LocalDateTime.of(meetingRequestPojo.getMeetingStartTime().toLocalDate(), finalStartHour);
    				LocalDateTime endDate = LocalDateTime.of(meetingRequestPojo.getMeetingStartTime().toLocalDate(), finalEndHour);
    				return ((meetingRequestPojo.getMeetingStartTime().isEqual(startDate) ||
    						meetingRequestPojo.getMeetingStartTime().isAfter(startDate)) &&
    						(meetingRequestPojo.getMeetingEndTime().isEqual(endDate) ||
    	    				meetingRequestPojo.getMeetingEndTime().isBefore(endDate)));
    			}).collect(Collectors.toList());
    			
    			for (MeetingRequestPojo meetingRequestPojo : meetingRequestPojos)
    			{
    				if (filteredMeetingRequestPojos.isEmpty())
    				{
    					filteredMeetingRequestPojos.add(meetingRequestPojo);
    					List<MeetingRequestPojo> filteredMeetingRequestList = new ArrayList<>();
    					filteredMeetingRequestList.add(meetingRequestPojo);
    					filteredMeetingRequestMap.put(meetingRequestPojo.getMeetingStartTime().toLocalDate(), filteredMeetingRequestList);
    				}
    				else
    				{
    					boolean isUniqueRequest = true;
    					for (MeetingRequestPojo filteredRequestPojo : filteredMeetingRequestPojos)
    					{
    						if ((meetingRequestPojo.getMeetingStartTime().equals(filteredRequestPojo.getMeetingStartTime()) &&
    							 meetingRequestPojo.getMeetingEndTime().equals(filteredRequestPojo.getMeetingEndTime())) ||
    							(meetingRequestPojo.getMeetingStartTime().isAfter(filteredRequestPojo.getMeetingStartTime()) &&
    							 meetingRequestPojo.getMeetingStartTime().isBefore(filteredRequestPojo.getMeetingEndTime())) ||
    						    (meetingRequestPojo.getMeetingEndTime().isBefore(filteredRequestPojo.getMeetingEndTime()) &&
    	    					 meetingRequestPojo.getMeetingEndTime().isAfter(filteredRequestPojo.getMeetingStartTime())))
    						{
    							isUniqueRequest = false;
    							break;
    						}
    					}
    					if (isUniqueRequest)
    					{
    						if (filteredMeetingRequestMap.containsKey(meetingRequestPojo.getMeetingStartTime().toLocalDate()))
    						{
    							List<MeetingRequestPojo> filteredMeetingRequestList = filteredMeetingRequestMap.get(meetingRequestPojo.getMeetingStartTime().toLocalDate());
    							filteredMeetingRequestList.add(meetingRequestPojo);
    							filteredMeetingRequestMap.put(meetingRequestPojo.getMeetingStartTime().toLocalDate(), filteredMeetingRequestList);
    						}
    						else
    						{
    							List<MeetingRequestPojo> filteredMeetingRequestList = new ArrayList<>();
    							filteredMeetingRequestList.add(meetingRequestPojo);
    							filteredMeetingRequestMap.put(meetingRequestPojo.getMeetingStartTime().toLocalDate(), filteredMeetingRequestList);
    						}
    						filteredMeetingRequestPojos.add(meetingRequestPojo);
    					}
    				}
    			}
    		}
    	}
    	return filteredMeetingRequestMap;
    }
	
	class BookingsComparator implements Comparator<JSONObject> {

		@Override
		public int compare(JSONObject o1, JSONObject o2) {
		    String v1 = (String) ((JSONObject) o1.get("attributes")).get("COMMERCIALNAME_E");
		    String v3 = (String) ((JSONObject) o2.get("attributes")).get("COMMERCIALNAME_E");
		    return v1.compareTo(v3);
		}
	}
}