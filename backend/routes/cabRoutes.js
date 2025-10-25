
const express = require('express');
const router = express.Router();
const cabController = require('../controllers/CabContoller');

router.post('/', cabController.addCab);
router.get('/search', cabController.findAvailableCabs);
router.patch('/:id/location',cabController.updateCabLocation);

module.exports = router;