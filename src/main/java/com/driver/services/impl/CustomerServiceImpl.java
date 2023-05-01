package com.driver.services.impl;

import com.driver.model.TripBooking;
import com.driver.services.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.driver.model.Customer;
import com.driver.model.Driver;
import com.driver.repository.CustomerRepository;
import com.driver.repository.DriverRepository;
import com.driver.repository.TripBookingRepository;
import com.driver.model.TripStatus;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class CustomerServiceImpl implements CustomerService {

	@Autowired
	CustomerRepository customerRepository2;

	@Autowired
	DriverRepository driverRepository2;

	@Autowired
	TripBookingRepository tripBookingRepository2;

	@Override
	public void register(Customer customer) {
		//Save the customer in database
		customerRepository2.save(customer);
	}

	@Override
	public void deleteCustomer(Integer customerId) {
		// Delete customer without using deleteById function
		customerRepository2.deleteById(customerId);
	}

	@Override
	public TripBooking bookTrip(int customerId, String fromLocation, String toLocation, int distanceInKm) throws Exception{
		//Book the driver with lowest driverId who is free (cab available variable is Boolean.TRUE). If no driver is available, throw "No cab available!" exception
		//Avoid using SQL query
		Driver driver = null;
		List<Driver> driverList = driverRepository2.findAll();

		// sort the driver on the basics of ID's
		driverList.sort(Comparator.comparingInt(Driver::getDriverId));

		for(Driver st : driverList){
			if(st.getCab().getAvailable() == true)
				driver = st;
		}

		// if no driver is available
		if(driver == null) {
			throw new Exception("No cab available!");
		}

		Customer customer = customerRepository2.findById(customerId).get();

		TripBooking tripBooking = new TripBooking();
		tripBooking.setFromLocation(fromLocation);
		tripBooking.setToLocation(toLocation);
		tripBooking.setDistanceInKm(distanceInKm);
		tripBooking.setStatus(TripStatus.CONFIRMED);
		tripBooking.setBill(distanceInKm * driver.getCab().getPerKmRate());// calculate the bill on the basics of Distance travelled by cab

		tripBooking.setDriver(driver);					   // driver booked for that trip
		tripBooking.setCustomer(customer);                 // customer added for that trip


		// If cab is booked than cab , Driver and customer not available
		customer.getTripBookingList().add(tripBooking);
		driver.getTripBookingList().add(tripBooking);

		// Driver cab not available
		driver.getCab().setAvailable(false);

		// save the history in all three Repository
		tripBookingRepository2.save(tripBooking);
		driverRepository2.save(driver);
		customerRepository2.save(customer);

		return tripBooking;
	}

	@Override
	public void cancelTrip(Integer tripId) throws Exception {
		//Cancel the trip having given tripId and update TripBooking attributes accordingly
       TripBooking tripBooking;
	   try{
		  tripBooking = tripBookingRepository2.findById(tripId).get();
	   } catch(Exception e){
		   throw new Exception("tripId does not exist");
	   }
	   tripBooking.setStatus(TripStatus.CANCELED);
	   tripBooking.setBill(0);
	   tripBooking.getDriver().getCab().setAvailable(true);
	   tripBookingRepository2.save(tripBooking);
	}

	@Override
	public void completeTrip(Integer tripId) throws Exception {
		//Complete the trip having given tripId and update TripBooking attributes accordingly
		TripBooking tripBooking;
		try{
			tripBooking = tripBookingRepository2.findById(tripId).get();
		} catch(Exception e){
			throw new Exception("tripId does not exist");
		}
		tripBooking.setStatus(TripStatus.COMPLETED);
		int bill = tripBooking.getDriver().getCab().getPerKmRate()*tripBooking.getDistanceInKm();
		tripBooking.setBill(bill);
		tripBooking.getDriver().getCab().setAvailable(true);
		tripBookingRepository2.save(tripBooking);
	}
}
