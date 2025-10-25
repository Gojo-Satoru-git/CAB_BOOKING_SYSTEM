const User = require("../models/User");

exports.register = async (req,res) => {
    try{
        const {name,email,password} = req.body;
        let user = await User.findOne({email});
        if(user){
            return res.status(400).json({message:'User already exits'});
        }

        user = new User({
            name,
            email,
            password,
        });
        // HASH BEFORE STORE PASS
        await user.save();

        res.status(201).json({
            _id:user._id,
            name:user.name,
            email:user.email
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
                email:user.email
            }
        });
    } catch (err) {
        console.error(err.message);
        res.status(500).send('Server Error')
    }
}