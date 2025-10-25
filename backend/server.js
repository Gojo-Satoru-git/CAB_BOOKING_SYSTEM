const cors = require("cors")
const express = require("express")
const bodyParser = require('body-parser')
const mongoose = require('mongoose')

const userRoutes = require('./routes/userRoutes');
const cabRoutes = require('./routes/cabRoutes');          
const bookingRoutes = require('./routes/bookingRoutes');
require('body-parser-xml')(bodyParser);

const app = express();
const PORT = process.env.PORT || 3000;

const DB_URL = "mongodb+srv://praveenchandru1209:1209@cluster1.dbb1j.mongodb.net/CabBookingSystem"

mongoose.connect(DB_URL)
  .then(() => console.log('MongoDB connected successfully'))
  .catch((err) => console.error('MongoDB connection error:', err.message));
//MIDDLEWARE
app.use(cors());
app.use(bodyParser.json());
app.use(bodyParser.urlencoded({extended:true}));
app.use(bodyParser.xml({
    limit:'1MB',
    xmlParseOptions:{
        normalize:true,
        explicitArray:false
    }
}));

app.get('/',(req,res) =>{
    res.json({message:'Cab Booking API is running!'});
});

app.use('/api/users',userRoutes)
app.use('/api/cabs', cabRoutes);           
app.use('/api/bookings', bookingRoutes);

app.listen(PORT,()=>{
    console.log(`Server is running on http://localhost:${PORT}`);
});