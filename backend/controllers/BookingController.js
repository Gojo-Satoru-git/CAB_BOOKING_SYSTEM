
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
            // We must convert the Mongoose docs to plain objects
            const plainBookings = bookings.map(booking => {
                const b = booking.toObject();
                // Manually stringify IDs for XML
                return {
                    ...b,
                    _id: b._id.toString(),
                    userId: b.userId ? { ...b.userId, _id: b.userId._id.toString() } : "N/A",
                    cabId: b.cabId ? { ...b.cabId, _id: b.cabId._id.toString() } : "N/A"
                };
            });
            
            // 'booking' = tag for each item, 'bookings' = root tag
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