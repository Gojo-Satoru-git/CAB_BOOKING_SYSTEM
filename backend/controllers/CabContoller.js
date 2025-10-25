
const Cab = require('../models/Cab');

exports.addCab = async (req, res) => {
  try {
    const { driverName, cabModel, licensePlate, latitude, longitude } = req.body;
    if (latitude === undefined || longitude === undefined) {
      return res.status(400).json({ message: 'Latitude and longitude are required' });
    }

    const newCab = new Cab({
      driverName,
      cabModel,
      licensePlate,
      currentLocation: {
        type: 'Point',
        coordinates: [longitude, latitude] 
      },
      isAvailable: true
    });

    const cab = await newCab.save();
    res.status(201).json(cab);

  } catch (err) {
    if (err.code === 11000) {
        return res.status(400).json({ message: 'License plate already exists' });
    }
    console.error(err.message);
    res.status(500).send('Server Error');
  }
};

exports.findAvailableCabs = async (req, res) => {
  try {

    const { lat, lng } = req.query;

    if (!lat || !lng) {
      return res.status(400).json({ message: 'Latitude (lat) and longitude (lng) are required query parameters' });
    }

    const latitude = parseFloat(lat);
    const longitude = parseFloat(lng);

    const maxDistanceInMeters = 5000;

    const availableCabs = await Cab.find({
      isAvailable: true,
      currentLocation: {
        $near: {
          $geometry: {
            type: 'Point',
            coordinates: [longitude, latitude] 
          },
          $maxDistance: maxDistanceInMeters
        }
      }
    });

    if (availableCabs.length === 0) {
      return res.status(404).json({ message: 'No available cabs found within 5km' });
    }

    res.status(200).json(availableCabs);

  } catch (err) {
    console.error(err.message);
    res.status(500).send('Server Error');
  }
};

exports.updateCabLocation = async (req, res) => {
  try {
    const { latitude, longitude, isAvailable } = req.body;
    const cabId = req.params.id;

    let cab = await Cab.findById(cabId);
    if (!cab) {
      return res.status(404).json({ message: 'Cab not found' });
    }

    if (latitude !== undefined && longitude !== undefined) {
      cab.currentLocation = {
        type: 'Point',
        coordinates: [longitude, latitude]
      };
    }

    if (isAvailable !== undefined) {
      cab.isAvailable = isAvailable;
    }

    const updatedCab = await cab.save();

    res.status(200).json(updatedCab);

  } catch (err) {
    console.error(err.message);
    res.status(500).send('Server Error');
  }
};