
const Cab = require('../models/Cab');
const { parse } = require('js2xmlparser');

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
      return res.status(400).json({ message: 'Latitude (lat) and longitude (lng) are required' });
    }
    
    const latitude = parseFloat(lat);
    const longitude = parseFloat(lng);
    const maxDistanceInMeters = 5000;

    const availableCabs = await Cab.find({
      isAvailable: true,
      currentLocation: {
        $near: {
          $geometry: { type: 'Point', coordinates: [longitude, latitude] },
          $maxDistance: maxDistanceInMeters
        }
      }
    });

    if (availableCabs.length === 0) {
      // If no cabs, just send an empty list for XML
      if (req.headers.accept && req.headers.accept.includes('application/xml')) {
        const emptyXml = parse('cabs', { cab: [] }); // Send <cabs></cabs>
        res.type('application/xml');
        return res.status(200).send(emptyXml);
      }
      
      // Otherwise, send 404 for JSON
      return res.status(404).json({ message: 'No available cabs found within 5km' });
    }

    // --- LOGIC TO RESPOND WITH XML OR JSON ---
    const clientAccepts = req.headers.accept;

    if (clientAccepts && clientAccepts.includes('application/xml')) {
      // Client wants XML

      // --- THIS IS THE FIX ---
      // We manually build a plain object and call .toString() on the _id
      const plainCabs = availableCabs.map(cab => ({
        _id: cab._id.toString(), // <-- Explicitly converts ObjectId to a string
        driverName: cab.driverName,
        cabModel: cab.cabModel,
        licensePlate: cab.licensePlate,
        isAvailable: cab.isAvailable,
        currentLocation: cab.currentLocation // This object is fine
      }));
      // --- END OF FIX ---

      // This call matches your JAXB model Cabs.java
      const xmlResponse = parse('cabs', { cab: plainCabs }); 

      res.type('application/xml');
      res.status(200).send(xmlResponse);

    } else {
      // Client wants JSON (or default)
      res.status(200).json(availableCabs);
    }

  } catch (err) {
    console.error(err.message); // This will log the error to your Node terminal
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