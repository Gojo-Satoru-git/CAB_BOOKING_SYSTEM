
const mongoose = require('mongoose');
const { Schema } = mongoose;

const bookingSchema = new mongoose.Schema({
  userId: {
    type: Schema.Types.ObjectId,
    ref: 'User',
    required: true
  },
  cabId: {
    type: Schema.Types.ObjectId,
    ref: 'Cab',
    required: true
  },
  pickupLocation: {
    type: String,
    required: true
  },
  dropLocation: {
    type: String,
    required: true
  },
  bookingTime: {
    type: Date,
    default: Date.now
  },
  status: {
    type: String,
    enum: ['PENDING', 'CONFIRMED', 'COMPLETED', 'CANCELLED'],
    default: 'PENDING'
  }
});

module.exports = mongoose.model('Booking', bookingSchema);