const User = require("../models/User");
const Cab = require("../models/Cab");

exports.register = async (req,res) => {
    try{
        const {name,email,password,role} = req.body;
        let user = await User.findOne({email});
        if(user){
            return res.status(400).json({message:'User already exits'});
        }

        user = new User({
            name,
            email,
            password,
            role: role && ['USER','DRIVER'].includes(role) ? role : 'USER'
        });
        // HASH BEFORE STORE PASS
        await user.save();

        // Auto-create Cab for DRIVER
        let createdCab = null;
        if (user.role === 'DRIVER') {
            const { driverName, cabModel, licensePlate, latitude, longitude, locationName } = req.body;
            if (!licensePlate || !cabModel || !driverName) {
                return res.status(400).json({ message: 'Missing cab details for driver registration' });
            }
            const lat = latitude !== undefined ? parseFloat(latitude) : 0;
            const lon = longitude !== undefined ? parseFloat(longitude) : 0;
            const cab = new Cab({
                driverName,
                cabModel,
                licensePlate,
                driverUserId: user._id,
                currentLocation: {
                    type: 'Point',
                    coordinates: [lon, lat]
                },
                locationName: locationName || '',
                isAvailable: true
            });
            createdCab = await cab.save();
        }

        res.status(201).json({
            _id:user._id,
            name:user.name,
            email:user.email,
            role:user.role,
            cab: createdCab ? {
                _id: createdCab._id,
                licensePlate: createdCab.licensePlate
            } : undefined
        });
    } catch(err){
        console.error(err.message);
        res.status(500).send('Server Error');
    }
};

exports.login = async (req,res) => {
    try {
        const {email,password} = req.body;

        const user = await User.findOne({email});    
        if(!user){
            return res.status(400).json({message:'Invalid credentials'});
        }

        if(password != user.password){
            return res.status(400).json({message:'Invalid credentials'});
        }
        // JWT YET TO ADD 
        res.status(200).json({
            message:'Login successful',
            user:{
                _id:user._id,
                name:user.name,
                email:user.email,
                role:user.role
            }
        });
    } catch (err) {
        console.error(err.message);
        res.status(500).send('Server Error')
    }
}