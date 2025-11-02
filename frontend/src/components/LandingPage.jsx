import { useState } from 'react'
import { Link } from 'react-router-dom'
import {
  Button,
  Container,
  Typography,
  TextField,
  Box,
  Paper,
} from '@mui/material'
import axios from 'axios'

const LandingPage = () => {
  const [token, setToken] = useState('')
  const [booking, setBooking] = useState(null)
  const [error, setError] = useState('')

  const fetchBooking = async () => {
    if (!token) return

    try {
      const res = await axios.get(`/api/v1/bookings/public/${token}`)
      setBooking(res.data)
      setError('')
    } catch (err) {
      console.log(err)
      setBooking(null)
      setError('Booking not found')
    }
  }

  return (
    <Container sx={{ textAlign: 'center', mt: 10 }}>
      <Typography variant="h3" gutterBottom>
        Welcome to ZeroMonos Collection
      </Typography>
      <Typography variant="subtitle1" gutterBottom>
        Book and manage waste collection appointments easily.
      </Typography>

      <Button
        component={Link}
        to="/login"
        variant="contained"
        color="primary"
        sx={{ mt: 4 }}
      >
        Get Started
      </Button>

      <Box sx={{ mt: 6 }}>
        <Typography variant="h5" gutterBottom>
          Check your booking
        </Typography>
        <TextField
          label="Booking Token"
          value={token}
          onChange={(e) => setToken(e.target.value)}
          sx={{ mr: 2, width: '300px' }}
        />
        <Button variant="outlined" onClick={fetchBooking}>
          Lookup
        </Button>

        {error && (
          <Typography color="error" sx={{ mt: 2 }}>
            {error}
          </Typography>
        )}

        {booking && (
          <Paper
            sx={{ mt: 4, p: 3, display: 'inline-block', textAlign: 'left' }}
          >
            <Typography>
              <strong>Municipality:</strong> {booking.municipality}
            </Typography>
            <Typography>
              <strong>Village:</strong> {booking.village}
            </Typography>
            <Typography>
              <strong>Postal Code:</strong> {booking.postalCode}
            </Typography>
            <Typography>
              <strong>Date:</strong> {booking.date}
            </Typography>
            <Typography>
              <strong>Time:</strong> {booking.time}
            </Typography>
            <Typography>
              <strong>Description:</strong> {booking.description}
            </Typography>
            <Typography>
              <strong>Status:</strong> {booking.state}
            </Typography>
          </Paper>
        )}
      </Box>
    </Container>
  )
}

export default LandingPage
