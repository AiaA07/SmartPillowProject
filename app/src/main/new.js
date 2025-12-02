   const express = require('express');
   const app = express();
   const port = 3000; // The port your server will listen on

   // Middleware to parse JSON bodies from incoming requests
   app.use(express.json());

   // A simple route to handle creating a new user
   // This corresponds to your 'insert' method
   app.post('/users', (req, res) => {
       const userData = req.body;
       console.log('Received user data:', userData);

       // TODO: Add code here to save the data to a database on your machine
       // (e.g., MySQL, PostgreSQL, or a local file)

       res.status(201).json({ message: 'User created successfully', data: userData });
   });

   // Add more routes for fetching, updating, and deleting users...
   // app.get('/users', ...);
   // app.put('/users/:id', ...);
   // app.delete('/users/:id', ...);

   app.listen(port, () => {
       console.log(`Server is running on http://localhost:${port}`);
   });
   