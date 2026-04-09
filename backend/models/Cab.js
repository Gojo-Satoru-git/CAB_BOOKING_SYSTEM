// models/Cab.js
const mongoose = require('mongoose');

const cabSchema = new mongoose.Schema({
  driverName: {
    type: String,
    required: true,
  },
  cabModel: {
    type: String,
    required: true
  },
  licensePlate: {
    type: String,
    required: true,
    unique: true
  },
  driverUserId: {
    type: mongoose.Schema.Types.ObjectId,
    ref: 'User'
  },
  currentLocation: {
    type: {
      type: String,
      enum: ['Point'],
      required: true,
      default: 'Point'
    },
    coordinates: {
      type: [Number],
      required: true
    }
  },
  
  // --- ADD THIS NEW FIELD ---
  locationName: {
    type: String,
    default: ''
  },
  // --- END OF ADDITION ---

  isAvailable: {
    type: Boolean,
    default: true
  }
});

cabSchema.index({ currentLocation: '2dsphere' });

module.exports = mongoose.model('Cab', cabSchema);