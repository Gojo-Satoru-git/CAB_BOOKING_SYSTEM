
const mongoose = require('mongoose')

const cabSchema = new mongoose.Schema({
    driverName:{
        type:String,
        required:true,
    },
    cabModel:{
        type:String,
        required:true
    },
    licensePlate:{
        type:String,
        required:true,
        unique:true
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
    isAvailable:{
        type:Boolean,
        default:true
    }

});
cabSchema.index({ currentLocation: '2dsphere' });

module.exports = mongoose.model('Cab',cabSchema);