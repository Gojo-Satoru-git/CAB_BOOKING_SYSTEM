
const Booking = require('../models/Booking');
const Cab = require('../models/Cab');
const User = require('../models/User');
const { parse } = require('js2xmlparser');


exports.createBooking = async (req, res) => {
  try {
    const data = req.body.booking ? req.body.booking : req.body;
    const { userId, cabId, pickupLocation, dropLocation } = data;
    const user = await User.findById(userId);
    if (!user) {
      return res.status(404).json({ message: 'User not found' });
    }

    const cab = await Cab.findById(cabId);
    if (!cab) {
      return res.status(404).json({ message: 'Cab not found' });
    }
    if (!cab.isAvailable) {
      return res.status(400).json({ message: 'Selected cab is not available' });
    }
    const newBooking = new Booking({
      userId,
      cabId,
      pickupLocation,
      dropLocation,
      status: 'CONFIRMED' // for demo need some work
    });

    cab.isAvailable = false;
    await cab.save();
    const booking = await newBooking.save();

    res.status(201).json({
        message: 'Booking confirmed!',
        bookingDetails: booking
    });

  } catch (err) {
    console.error(err.message);
    res.status(500).send('Server Error');
  }
};

// View all bookings (for demo) 
exports.viewAllBookings = async (req, res) => {
    try {
        const bookings = await Booking.find()
            .populate('userId', 'name email') 
            .populate('cabId', 'driverName cabModel licensePlate');

        // --- ADD BILINGUAL RESPONSE LOGIC ---
        const clientAccepts = req.headers.accept;

        if (clientAccepts && clientAccepts.includes('application/xml')) {
            // Client wants XML
            // Convert to flat objects compatible with JAXB Booking.java
            const plainBookings = bookings.map(booking => ({
                _id: booking._id.toString(),
                userId: booking.userId ? booking.userId._id.toString() : '',
                cabId: booking.cabId ? booking.cabId._id.toString() : '',
                pickupLocation: booking.pickupLocation,
                dropLocation: booking.dropLocation,
                bookingTime: booking.bookingTime,
                status: booking.status
            }));

            const xmlResponse = parse('bookings', { booking: plainBookings }); 
            res.type('application/xml');
            res.status(200).send(xmlResponse);
        } else {
            // Client wants JSON (or default)
            res.status(200).json(bookings);
        }
        // --- END OF NEW LOGIC ---
        
    } catch (err) {
        console.error(err.message);
        res.status(500).send('Server Error');
    }
};

// Get booking by id
exports.getBookingById = async (req, res) => {
  try {
    const booking = await Booking.findById(req.params.id)
      .populate('userId', 'name email')
      .populate('cabId', 'driverName cabModel licensePlate');

    if (!booking) {
      return res.status(404).json({ message: 'Booking not found' });
    }

    const clientAccepts = req.headers.accept;
    if (clientAccepts && clientAccepts.includes('application/xml')) {
      const b = booking;
      const xml = parse('bookings', { booking: [{
        _id: b._id.toString(),
        userId: b.userId ? b.userId._id.toString() : '',
        cabId: b.cabId ? b.cabId._id.toString() : '',
        pickupLocation: b.pickupLocation,
        dropLocation: b.dropLocation,
        bookingTime: b.bookingTime,
        status: b.status
      }]});
      res.type('application/xml');
      return res.status(200).send(xml);
    }

    res.status(200).json(booking);
  } catch (err) {
    console.error(err.message);
    res.status(500).send('Server Error');
  }
};

// Get bookings by user
exports.getBookingsByUser = async (req, res) => {
  try {
    const { userId } = req.params;
    const bookings = await Booking.find({ userId })
      .populate('userId', 'name email')
      .populate('cabId', 'driverName cabModel licensePlate');

    const clientAccepts = req.headers.accept;
    if (clientAccepts && clientAccepts.includes('application/xml')) {
      const plainBookings = bookings.map(b => ({
        _id: b._id.toString(),
        userId: b.userId ? b.userId._id.toString() : '',
        cabId: b.cabId ? b.cabId._id.toString() : '',
        pickupLocation: b.pickupLocation,
        dropLocation: b.dropLocation,
        bookingTime: b.bookingTime,
        status: b.status
      }));
      const xml = parse('bookings', { booking: plainBookings });
      res.type('application/xml');
      return res.status(200).send(xml);
    }

    res.status(200).json(bookings);
  } catch (err) {
    console.error(err.message);
    res.status(500).send('Server Error');
  }
};

// Cancel booking
exports.cancelBooking = async (req, res) => {
  try {
    const booking = await Booking.findById(req.params.id);
    if (!booking) {
      return res.status(404).json({ message: 'Booking not found' });
    }

    if (booking.status === 'CANCELLED') {
      return res.status(400).json({ message: 'Booking already cancelled' });
    }

    booking.status = 'CANCELLED';
    await booking.save();

    // Free up the cab
    if (booking.cabId) {
      await Cab.findByIdAndUpdate(booking.cabId, { isAvailable: true });
    }

    res.status(200).json({ message: 'Booking cancelled', booking });
  } catch (err) {
    console.error(err.message);
    res.status(500).send('Server Error');
  }
};

// Complete booking
exports.completeBooking = async (req, res) => {
  try {
    const booking = await Booking.findById(req.params.id);
    if (!booking) {
      return res.status(404).json({ message: 'Booking not found' });
    }

    if (booking.status === 'COMPLETED') {
      return res.status(400).json({ message: 'Booking already completed' });
    }

    booking.status = 'COMPLETED';
    await booking.save();

    // Free up the cab
    if (booking.cabId) {
      await Cab.findByIdAndUpdate(booking.cabId, { isAvailable: true });
    }

    res.status(200).json({ message: 'Booking completed', booking });
  } catch (err) {
    console.error(err.message);
    res.status(500).send('Server Error');
  }
};