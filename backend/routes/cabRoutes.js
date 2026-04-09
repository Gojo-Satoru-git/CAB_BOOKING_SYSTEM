
const express = require('express');
const router = express.Router();
const cabController = require('../controllers/CabContoller');

router.post('/', cabController.addCab);
router.get('/search', cabController.findAvailableCabs);
router.patch('/:id/location',cabController.updateCabLocation);
router.get('/search-by-name', cabController.findCabsByName);
router.patch('/:id/availability', cabController.updateAvailability);

module.exports = router;