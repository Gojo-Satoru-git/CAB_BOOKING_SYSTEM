const express = require('express');
const router = express.Router();
const bookingController = require('../controllers/BookingController');

router.post('/', bookingController.createBooking);
router.get('/', bookingController.viewAllBookings);
router.get('/:id', bookingController.getBookingById);
router.get('/user/:userId', bookingController.getBookingsByUser);
router.post('/:id/cancel', bookingController.cancelBooking);
router.post('/:id/complete', bookingController.completeBooking);

module.exports = router;